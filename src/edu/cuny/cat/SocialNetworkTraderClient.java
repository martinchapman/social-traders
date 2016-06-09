package edu.cuny.cat;

import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import au.edu.unimelb.cat.socialnetwork.method.ShoutingHistoryEntry;

import edu.cuny.cat.comm.CatException;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.comm.CatpMessageErrorException;
import edu.cuny.cat.comm.CatpProactiveSession;
import edu.cuny.cat.comm.CatpRequest;
import edu.cuny.cat.comm.CatpResponse;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.InformedMSDecisionArrivedEvent;
import edu.cuny.cat.event.InformedShoutingDecisionArrivedEvent;
import edu.cuny.cat.trader.SocialNetworkTradingAgent;
import edu.cuny.cat.trader.marketselection.SocialNetworkMarketSelectionStrategy;
import edu.cuny.cat.trader.strategy.SocialNetworkShoutingStrategy;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 116 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:45:45 +1100 (Sun, 28 Feb 2010) $
 */
public class SocialNetworkTraderClient extends TraderClient implements Observer {

	static Logger logger = Logger.getLogger(SocialNetworkTraderClient.class);

	double lastDayProfit = 0.0;
	
	public SocialNetworkTraderClient() {

		removeAuctionEventListener(agent);

		agent = new SocialNetworkTradingAgent();

		addAuctionEventListener(agent);

	}
	
	// ~MDC 13/2/11
	class PostTraderProfit extends CatpProactiveSession { //
	
		public PostTraderProfit() //
		{

			super(connection); //
			
			setRequest(CatpRequest.createRequest(CatpMessage.POST, new String[] { //
					CatpMessage.TYPE, "traderProfit", CatpMessage.ID, agent.getTraderId(), //
					CatpMessage.VALUE, String.valueOf(lastDayProfit), //
					CatpMessage.TEXT, String.valueOf(agent.getAdviceTaken()) })); //

			getRequest().setTag(tag); //
	
		}

		@Override
		public void processResponse(CatpResponse response) throws CatException { //

			super.processResponse(response); //

			if ((response.getStatusCode().equalsIgnoreCase(CatpMessage.OK))) { //

				GetInformedDecisionSession newSession = 
									new GetInformedDecisionSession("MSS");

				startProactiveSession(newSession);
			
			} 
			else {

				final String msg = "Unexpected response received to get " //
						+ request.getHeader(CatpMessage.TYPE) + " message:\n" //
						+ response; //

				throw new CatpMessageErrorException(msg); //

			}

		}

	}
	

	class GetInformedDecisionSession extends CatpProactiveSession {

		public GetInformedDecisionSession(String strategy) {
			super(connection);

			setRequest(CatpRequest.createRequest(CatpMessage.GET, new String[] {
					CatpMessage.TYPE, "MD", CatpMessage.TEXT,
					String.format("strategy=%s", strategy) }));
			getRequest().setTag(tag);
		}

		@Override
		public void processResponse(CatpResponse response) throws CatException {
			super.processResponse(response);

			if (response.getStatusCode().equalsIgnoreCase(CatpMessage.OK)) {
				String strategy = null;

				Pattern p = Pattern.compile("(\\w*)=([^, ]*)");
				Matcher m = p.matcher(response.getHeader(CatpMessage.TEXT));

				if (m.find()) {
					if (m.group(1).equals("strategy")) {
						strategy = m.group(2);
					}
				}

				if (strategy != null) {
				
					if (strategy.equalsIgnoreCase("MSS")) {
						String specialistId = null;
						boolean experience = false;

						if (m.find()) {
							if (m.group(1).equals("specialistid")) {
								specialistId = m.group(2);
							}
						}
						if (m.find()) {
							if (m.group(1).equals("experience")) {
								experience = Boolean.parseBoolean(m.group(2));
							}
						}

						if (specialistId != null) {
							if (specialistId.equals("")) {
								dispatchEvent(new InformedMSDecisionArrivedEvent());
							} else {
								dispatchEvent(new InformedMSDecisionArrivedEvent(
										specialistId, experience));
							}
						} else {
							final String msg = "Unexpected response received to get "
									+ request.getHeader(CatpMessage.TYPE)
									+ " message:\n"
									+ "!!! >>> no specialistid received <<< !!!";

							throw new CatpMessageErrorException(msg);
						}
					} else if (strategy.equalsIgnoreCase("SS")) {
						ShoutingHistoryEntry she = new ShoutingHistoryEntry();
						boolean experience = false;

						while (m.find()) {
							if (m.group(1).equals("price")) {
								she.setPrice(Double.parseDouble(m.group(2)));
							} else if (m.group(1).equals("quantity")) {
								she.setQuantity(Integer.parseInt(m.group(2)));
							} else if (m.group(1).equals("experience")) {
								experience = Boolean.parseBoolean(m.group(2));
							}
						}

						if (she.getPrice() != -1 && she.getQuantity() != -1) {
							dispatchEvent(new InformedShoutingDecisionArrivedEvent(
									she, experience));
						} else {
							dispatchEvent(new InformedShoutingDecisionArrivedEvent());
						}

					}
				}
			} else {
				final String msg = "Unexpected response received to get "
						+ request.getHeader(CatpMessage.TYPE) + " message:\n"
						+ response;

				throw new CatpMessageErrorException(msg);
			}
		}
	}

	@Override
	public void update(Observable source, Object arg) {
		if (arg instanceof SocialNetworkMarketSelectionStrategy) {
			PostTraderProfit newSession = new PostTraderProfit();
			startProactiveSession(newSession);
		} else if (arg instanceof SocialNetworkShoutingStrategy) {
			GetInformedDecisionSession newSession = new GetInformedDecisionSession(
					"SS");
			startProactiveSession(newSession);
		} else {
			super.update(source, arg);
		}
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);
		
		if (event instanceof DayClosedEvent) {

			lastDayProfit = agent.getLastDayProfit(); //

		}
	} 

}
