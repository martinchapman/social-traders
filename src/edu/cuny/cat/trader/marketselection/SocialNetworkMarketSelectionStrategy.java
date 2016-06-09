package edu.cuny.cat.trader.marketselection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import org.apache.log4j.Logger;

import cern.jet.random.Uniform;

import edu.cuny.cat.Game;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.InformedMSDecisionArrivedEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;

/**
 * 
 * @author Guan Gui (Modifications by Martin Chapman (~MDC))
 * @version $Rev: 123 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:34:54 +1100 (Sun, 28 Feb 2010) $
 */
public class SocialNetworkMarketSelectionStrategy extends
		AbstractMarketSelectionStrategy implements Parameterizable, Resetable {

	static Logger logger = Logger.getLogger(SocialNetworkMarketSelectionStrategy.class);
	protected AbstractMarketSelectionStrategy underlyingStrategy;
	protected Map<String, Integer> marketIdIndices;
	private boolean isValid;

	/* ~MDC An indication of whether experience attributed to
	    the advice given is positive or negative. */
	private boolean experience; //

	// ~MDC The market index to which the reccomendation pertains. 
	private int recommendationId = -1; //

	private Uniform dist;

	// ~MDC A random number from 0 to the number of specialists.
	private Uniform marketDist; //

	// ~MDC The social profile of this trader.
	private String profile; //

	// ~MDC The default percentage by which to take a trader's advice.
	private double p_advice = 0.49; //

	// ~MDC The default percentage by which to take use a normal strategy.
	private double p_underlying = 0.49; //

	// ~MDC The constant percentage by which to use a random strategy.
	private static final double P_RANDOM = 0.02; // 

	public static final String P_UNDERLYING_STRATEGY = "underlyingstrategy";

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		underlyingStrategy = parameters
				.getInstanceForParameter(
						base
								.push(SocialNetworkMarketSelectionStrategy.P_UNDERLYING_STRATEGY),
						null, AbstractMarketSelectionStrategy.class);
		if (underlyingStrategy instanceof SocialNetworkMarketSelectionStrategy) {
			logger
					.error("Error: the underlying strategy can't be an instance of AbstractMimickingStrategy!");
			System.exit(1);
		}
		((Parameterizable) underlyingStrategy)
				.setup(
						parameters,
						base
								.push(SocialNetworkMarketSelectionStrategy.P_UNDERLYING_STRATEGY));
		marketIdIndices = Collections
				.synchronizedMap(new HashMap<String, Integer>());

		isValid = false;

		// ~MDC Instantiate the random number generator instance.
		dist = new Uniform(0, 1, Galaxy.getInstance().getTyped(Game.P_CAT,
			   GlobalPRNG.class).getEngine()); //

		// ~MDC If a profile specification exists:		
		if (parameters.exists(base.push("profile"))) { //

			// ~MDC Store the profile specification.
			profile = parameters.getString(base.push("profile"), null); //
			
			// ~MDC If the profile is sceptical, set the percentages as appropriate.
			if (profile.contains("sceptical")) //
			{
				p_advice = 0.04; p_underlying = 0.94; //
			} 
			// ~MDC If the profile is credulous, set the percentages as appropriate.
			else if (profile.contains("credulous")) //
			{
				p_advice = 0.94; p_underlying = 0.04; //
			}	

			// ~MDC Otherwise the profile remains 'normal' (the default).

		}

		super.initialize();

	}

	@Override
	public void initialize() {
		underlyingStrategy.initialize();
		super.initialize();
	}

	@Override
	public void reset() {
		underlyingStrategy.reset();
		super.reset();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		
		if (event instanceof InformedMSDecisionArrivedEvent) {

			InformedMSDecisionArrivedEvent me = (InformedMSDecisionArrivedEvent) event; //
			
			if (isValid = me.isValid()) {

				/* ~MDC Store whether experience attributed to this 
				    recommendation is positive or negative */
				experience = me.getExperience(); //

				// ~MDC Store the specialist to which the reccomendation pertains.
				recommendationId = marketIdIndices.get(me.getSpecialistId()); //

			}
			
			if (activeMarkets.size() > 0) {

				selectMarket();
				

			} else {

				logger.debug("No markets are available for "
						+ agent.getTraderId() + " !");

				currentMarketIndex = -1;

			}

			registerMarket();

		} else if (event instanceof DayOpenedEvent) {

			setChanged();

			notifyObservers(this);

			// underlyingStrategy.eventOccurred(event);
			// super.eventOccurred(event);

		} else {

			underlyingStrategy.eventOccurred(event);

			super.eventOccurred(event);

		}
	}

	@Override
	/**
	 * selects a specialist from the list of active ones in the game, which is
	 * stored in {@link #activeMarkets} either randomly, using another trader's
	 * advice or using their standard strategy according to a given probability.
	 */
	public void selectMarket() {

		// ~MDC Generate a random number to simulate probability.
		double tmp = dist.nextDouble(); //

		logger.debug(agent.getTraderId() + " receives probability " + tmp); //		

		// ~MDC Probability range for performing a random action.
		if (tmp <= P_RANDOM) { //

		    useRandomStrategy(); //
			
		} 
		// ~MDC Probability range for performing a standard strategy.
		else if ((tmp > P_RANDOM) && (tmp <= p_underlying)) { //

		    useUnderlyingStrategy(); //

		} 
		// ~MDC Probability range for using another trader's advice. 
		else if ((tmp > p_underlying) && (tmp <= 1.0)) { //

			useInformedStrategy(); //
			
		} 
		else {

			logger.debug("Probability error: " + tmp); //

		}

	}

	/**
 	 * ~MDC Causes the trader using this strategy to use an informed 
	 *  strategy, that is, a strategy which either copies the 
	 *  actions of another trader if those actions were deemed to 
	 *  successful by that trader or purposefully deviates from
	 *  them if the actions were perceived to be negative.  
	 * 
	 */
	protected void useInformedStrategy() {

		/* ~MDC For use by the server's reputation system, flag whether any upcoming
		    actions undertaken by the trader are due to the advice of another trader. */
		agent.setAdviceTaken(true); //
	
		/* ~MDC If the recommended index is valid and the experience associated  
		    with it is a positive one: */
		if (activeMarkets.contains(recommendationId) && experience == true) { //
			
			logger.info(agent.getTraderId() + "! receives information to use " 
					 + markets[recommendationId].getId() + " and is doing so."); //	

			/* ~MDC Set the market index of the super class
			    to be the index recommended by another trader. */
			currentMarketIndex = recommendationId; //

		} 
		/* ~MDC If the recommended index is valid and the experience associated with
		    it is NOT a positive one: */
		else if (activeMarkets.contains(recommendationId) && experience == false) { //

			logger.info(agent.getTraderId() + "! receives probability to use a linked trader's advice " 
					 + (activeMarkets.contains(recommendationId) ? "and they were advised not to use specialist " 
				     + markets[recommendationId].getId() : "but this market is not valid."));

			// ~MDC Store the current state of the markets.
			SortedSet<Integer> copiedMarkets = activeMarkets; //
		
			/* ~MDC Remove the market with which the recommending trader
			    has had a negative experience from the "proxy" store. */
			copiedMarkets.remove(recommendationId); //

			// ~MDC If removing the market leaves some markets still available:
			if (copiedMarkets.size() != 0) {

				// ~MDC Restore the full list of markets.
				copiedMarkets = activeMarkets; //
				
				/* ~MDC Remove the market to which the negative recommendation
				    pertains from the actual set of markets available to the 
					trader. */
				underlyingStrategy.activeMarkets.remove(recommendationId); //	

				/* ~MDC Use the underlying algorithms of the standard market
					selection strategy, minus the index removed. */
				underlyingStrategy.selectMarket(); //

				// ~MDC Select the actual market.
				currentMarketIndex = underlyingStrategy.currentMarketIndex; //
		
				// ~MDC Restore the state of the markets.
				activeMarkets = copiedMarkets; //

			} 
			else {

				useUnderlyingStrategy();

			}

		} else {

			useUnderlyingStrategy();

		}

	}

	/**
 	 * ~MDC Causes the trader using their underlying strategy;
	 *  the strategy present without social involvement.
	 * 
	 */
	protected void useUnderlyingStrategy() {
		
		/* ~MDC Flag that any gains made by the trader are NOT
		    due to a social approach/ */
		agent.setAdviceTaken(false);

		logger.info(agent.getTraderId() + "! is using their underlying strategy.");

		// ~MDC Use the underlying strategy to select the market.
		underlyingStrategy.selectMarket();

		// ~MDC Set the current market to the one selected by the strategy.
		currentMarketIndex = underlyingStrategy.currentMarketIndex;
		
	}

	protected void useRandomStrategy() {

		agent.setAdviceTaken(false);

		Long marketChoice = Math.round((marketDist.nextDouble()));
		
		int tmp = marketChoice.intValue();		

		if (recommendationId != -1 && activeMarkets.contains(tmp)) {

			underlyingStrategy.selectMarket();

			currentMarketIndex = underlyingStrategy.currentMarketIndex;

			currentMarketIndex = tmp;

			logger.info(agent.getTraderId() + "! is using a random strategy and selects " + (hasValidCurrentMarket() ? getCurrenMarket().getId(): "INVALID")); 

		} else {

			useUnderlyingStrategy();

		} 	

	}

	@Override
	/**
	 * sets up the trading agent that plays this strategy.
	 * 
	 * @param agent
	 */
	public void setAgent(final AbstractTradingAgent agent) {
		underlyingStrategy.setAgent(agent);
		underlyingStrategy.addObserver(agent);
		super.setAgent(agent);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	protected void setupMarkets(Collection<Specialist> marketColl) {

		super.setupMarkets(marketColl);

		for (int i = 0; i < markets.length; i++) {

			marketIdIndices.put(markets[i].getId(), new Integer(i));

		}

		// Instantiate a random number generator from 0 to the number of specialists available.
		marketDist = new Uniform(0, marketIdIndices.size() - 1, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine());

	}
}
