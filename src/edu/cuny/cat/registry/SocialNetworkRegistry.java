package edu.cuny.cat.registry;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.io.IOException; //

import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import com.google.common.collect.BiMap; //
import com.google.common.collect.HashBiMap; //
import com.google.common.collect.Maps; //
import au.edu.unimelb.cat.socialnetwork.helper.Utils;
import au.edu.unimelb.cat.socialnetwork.method.AbstractMimickingMarketSelectionMethod;
import au.edu.unimelb.cat.socialnetwork.method.AbstractMimickingShoutingMethod;
import au.edu.unimelb.cat.socialnetwork.method.ShoutingHistoryEntry;
import au.edu.unimelb.cat.socialnetwork.ui.MainFrame;
import au.edu.unimelb.cat.socialnetwork.ui.MarketSelectionAMFrame;
import au.edu.unimelb.cat.socialnetwork.ui.SpecialistVisualization;
import au.edu.unimelb.cat.socialnetwork.ui.SpecialistsFrame;
import uk.ac.liv.cat.socialnetwork.ui.SocialTraderFrame; //
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.FundTransferEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutReceivedEvent;
import edu.cuny.cat.event.ShoutRejectedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TraderCheckInEvent;
import edu.cuny.cat.event.TraderProfitEvent; //
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionRejectedEvent;
import edu.cuny.cat.server.GameClock;
import edu.cuny.cat.Game;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

import edu.uci.ics.jung.graph.Graph; //
import edu.uci.ics.jung.graph.SparseMultigraph; //
import edu.uci.ics.jung.graph.util.EdgeType;

import edu.cuny.prng.GlobalPRNG; //
import edu.cuny.util.Galaxy; //
import cern.jet.random.Uniform; //


/**
 * This registry provides central controls for the social network. It is an
 * extension to {@link SimpleRegistry}.
 * 
 * @author Guan Gui (Extensions by Martin Chapman)
 * @version $Rev: 121 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          01:05:46 +1100 (Sun, 28 Feb 2010) $
 */
public class SocialNetworkRegistry extends SimpleRegistry implements
		Parameterizable {

	private static Logger logger = Logger
			.getLogger(SocialNetworkRegistry.class);

	private static SocialNetworkRegistry instance;

	public SocialNetworkRegistry() {
		super();

		instance = this;
	}

	// ~MDC A uniform distribution for generating random number.
	private Uniform dist; //

	// ~MDC The name of the current social network being used.
	private String network; //
	
	// ~MDC The name of the profile being used uniformly across the traders 
	private String profile; //

	// -------------------------------
	// SETUP
	// -------------------------------

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {
		String outputFolder = parameters.getString(base.push("outputfolder"), null);

		enableGUI = parameters.getBoolean(base.push("enableGUI"), null,
				enableGUI);

		marketSelectionStrategy = parameters.getInstanceForParameter(base
				.push("mimickingmarketselectionmethod"), null,
				AbstractMimickingMarketSelectionMethod.class);

		if (marketSelectionStrategy instanceof Parameterizable) {
			((Parameterizable) marketSelectionStrategy).setup(parameters, base
					.push("mimickingmarketselectionmethod"));
		}

		mimickingShoutingMethod = parameters.getInstanceForParameter(base
				.push("mimickingshoutingmethod"), null,
				AbstractMimickingShoutingMethod.class);

		if (mimickingShoutingMethod instanceof Parameterizable) {
			((Parameterizable) mimickingShoutingMethod).setup(parameters, base
					.push("mimickingshoutingmethod"));
		}	

		/* ~MDC Instantiate the given distribution with a minimum value, maximum value
            and a random number generator */ 
		dist = new Uniform(0, 1, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine()); //

		// ~MDC If a folder path is specified:
		if (outputFolder != null) {
			
			try {

				// ~MDC 
				network = parameters.getString(base.push("mimickingmarketselectionmethod.inputfile"), null); //

				/* ~MDC Create a log file at the given location, automatically named by the type of network
					and traders used and uniquely distinguised by a random number. */
				outputPW = new PrintWriter(outputFolder + (network = (network.substring(network.lastIndexOf("/"), network.length()))) +
			    "-" + (profile = parameters.getString(new Parameter("cat.agent.0.strategy.profile"), null)) +
			    "-" + dist.nextDouble() + ".csv"); //

			} catch (FileNotFoundException e) {

				e.printStackTrace();

				System.exit(1);

			}

		}

		// ~MDC Create a map linking each trader to a unique identifier.
		traderIndices = Maps.synchronizedBiMap(
       									HashBiMap.<Trader, Integer>create()); //
						
	}

	// -------------------------------
	// EVENTS
	// -------------------------------

	@Override
	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof TraderCheckInEvent) {
			processTraderCheckIn((TraderCheckInEvent) event);
		} else if (event instanceof SpecialistCheckInEvent) {
			processSpecialistCheckIn((SpecialistCheckInEvent) event);
		} else if (event instanceof ShoutReceivedEvent) {
			processShoutReceived((ShoutReceivedEvent) event);
		} else if (event instanceof ShoutRejectedEvent) {
			processShoutRejected((ShoutRejectedEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof TransactionRejectedEvent) { //
			processTransactionRejected((TransactionRejectedEvent) event); //
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof SubscriptionEvent) {
			processSubscription((SubscriptionEvent) event);
		} else if (event instanceof PrivateValueAssignedEvent) {
			processPrivateValueAssigned((PrivateValueAssignedEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayOpeningEvent) {
			processDayOpening((DayOpeningEvent) event);
		} else if (event instanceof DayOpenedEvent) {
			processDayOpened((DayOpenedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof SimulationStartedEvent) {
			processSimulationStarted((SimulationStartedEvent) event);
		} else if (event instanceof SimulationOverEvent) {
			processSimulationOver((SimulationOverEvent) event);
		} else if (event instanceof TraderProfitEvent) { //
			processTraderProfit((TraderProfitEvent) event); //
		} else if (event instanceof ClientStateUpdatedEvent) {
			processClientStatusUpdated((ClientStateUpdatedEvent) event);
		} else if (event instanceof FundTransferEvent) {
			processFundTransfer((FundTransferEvent) event);
		} else {
			SocialNetworkRegistry.logger
					.error("has yet to be implemented in SimpleRegistry : "
							+ event.getClass().getSimpleName());
		}
	}

	// -------------------------------
	// CLASS PROPERTIES
	// -------------------------------

	private AbstractMimickingMarketSelectionMethod marketSelectionStrategy;
	private AbstractMimickingShoutingMethod mimickingShoutingMethod;

	/**
	 * Market Selection Adjacency Matrix Data Section
	 */
	private AtomicReferenceArray<AtomicIntegerArray> mSAdjacencyMatrix;
	private AtomicReferenceArray<String> mSTraderIds;
	private Hashtable<String, Integer> mSTraderIdIdx;
	private Hashtable<String, String> mSHistory;
	
	/**
	 * Shouting Data Section
	 */
	private Hashtable<String, ShoutingHistoryEntry> sHistory;
	private Hashtable<String, Integer> tradeEntitlements;
	
	/**
	 * Trader Data	 
	 */
	
	/* ~MDC A map linking each trader's unique name to their profit
		level from the previous trading day */
	private Hashtable<String, Double> tProfit;
	
	/* ~MDC A map linking each trader's unique name to their experience
		with the specialist they used in the previous day. */	
	private Hashtable<String, Boolean> tExperience;
	
	// ~MDC A map linking 
	public BiMap<Trader, Integer> traderIndices;

	// ~MDC 
	private Hashtable<String, Double> tReputation;

	// ~MDC 
	private Hashtable<String, Integer> numTraderMatches;

	// ~MDC 
	private Hashtable<String, Integer> advisors;	
	
	/**
	 * Specialist Visualization Data Section
	 */
	private Hashtable<String, SpecialistVisualization> sVs;
	private Hashtable<String, Double> mProfit;
	private Hashtable<String, Integer> numMatches;

	/**
	 * GUI References
	 */
	private boolean enableGUI = false;
	private SpecialistsFrame sFrame;
	private MainFrame mFrame;
	private MarketSelectionAMFrame msAMFrame;

	// ~MDC
	private SocialTraderFrame stFrame;
	
	// ~MDC	
	private static Graph<String, Integer> sGraph = new SparseMultigraph<String, Integer>(); //

	private PrintWriter outputPW;

	private int curIteration = 0;

	// -------------------------------
	// GETTERS AND SETTERS
	// -------------------------------

	public static SocialNetworkRegistry getInstance() {
		return instance;
	}

	public AbstractMimickingMarketSelectionMethod getMarketSelectionStrategy() {
		return marketSelectionStrategy;
	} 

	public AbstractMimickingShoutingMethod getInformedShoutingMethod() { //
		return mimickingShoutingMethod; //
	} 

	// ~MDC Returns a list mapping each trader to a unique number.
	public BiMap<Trader, Integer> getTraderIndices() //
	{
		return traderIndices; //
	}	

	// ~MDC Returns the numeric index associated with the given trader.
	public Integer getIndexFromTrader(Trader trader)  { //

		return traderIndices.get(trader); //

	}

	// ~MDC Returns the trader associated with the given numeric index.
	public Trader getTraderFromIndex(Integer index) { //

		return traderIndices.inverse().get(index); //

	} //

	/* ~MDC Increases the advice purveying frequency count for the
	    given trader. */
	public void incrementAdvice(String trader) { //

		advisors.put(trader, advisors.get(trader) + 1); //

	}

	public Hashtable<String, Integer> getTradeEntitlements() {
		return tradeEntitlements;
	}

	public AtomicReferenceArray<AtomicIntegerArray> getmSAdjacencyMatrix() {
		return mSAdjacencyMatrix;
	}

	public AtomicReferenceArray<String> getmSTraderIds() {
		return mSTraderIds;
	}

	public Hashtable<String, String> getmSHistory() {
		return mSHistory;
	}

	// ~MDC Returns a map linking each trader to an experience
	//  of their specialist in the previous day.
	public Hashtable<String, Boolean> getTExperience() { //
		return tExperience; //
	}

	public SpecialistsFrame getsFrame() {
		return sFrame;
	}

	public void setsFrame(SpecialistsFrame sFrame) {
		this.sFrame = sFrame;
	}

	public MarketSelectionAMFrame getMsAMFrame() {
		return msAMFrame;
	}

	public void setMsAMFrame(MarketSelectionAMFrame msAMFrame) {
		this.msAMFrame = msAMFrame;
	}

	/* ~MDC Returns the GUI attributed to the 'social' element
		of JCAT */
	public SocialTraderFrame getstFrame() { //
		return stFrame; //
	}

	public void setstFrame(SocialTraderFrame stFrame) {
		this.stFrame = stFrame;
	}

	public Hashtable<String, SpecialistVisualization> getsVs() {
		return sVs;
	}

	public Hashtable<String, Integer> getmSTraderIdIdx() {
		return mSTraderIdIdx;
	}

	public Hashtable<String, ShoutingHistoryEntry> getsHistory() {
		return sHistory;
	}
	
	/* ~MDC Returns a map linking each trader to their
		reputation as a numeric utility. */
	public Hashtable<String, Double> getTReputation() { //
		return tReputation; //
	}

	// -------------------------------
	// EVENT METHODS
	// -------------------------------

	// -------------------------------
	// OPENING EVENTS
	// -------------------------------

	private static int count = 0;

	@Override
	protected void processRegistration(final RegistrationEvent event) {
		super.processRegistration(event);

		final Trader trader = workingTraders.get(event.getTraderId());
		final Specialist specialist = getSpecialist(event.getSpecialistId());
		
		if (trader != null) {
			
			/* ~MDC Add this registering trader to a graphical
				representation of the agents in the system */
			traderOnGraph(event.getTraderId()); //

			if (specialist != null) {
				SocialNetworkRegistry.logger.info(event.getTraderId()
						+ "! enters " + specialist.getId() + "\n");
				count++;
			
				sVs.get(trader.getSpecialistId()).AddTrader(trader.getId());
				if (count == getNumOfWorkingTraders()) {
				
					buffersVs(event);

					if (sFrame != null && sFrame.isVisible()) 
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								sFrame.updatesVs();
							}
						});
					
					// ~MDC If the social frame is instantiated and visible:
					if (stFrame != null && stFrame.isVisible()) //
					
						// ~MDC Add the UI update to the swing event queue.
						SwingUtilities.invokeLater(new Runnable() { //

							// ~MDC In a a new thread:
							@Override // 
							public void run() { //

								// ~MDC Update the graphical representation of the traders.
								stFrame.updateGraph(sGraph); //

								// ~MDC Update the activity log as necessary.
								stFrame.updateLog();	 //

								// ~MDC Add a list of all the traders now registered.
								stFrame.addTraders(getWorkingTraderIds()); //

								/* ~MDC Update misc. stats information on the GUI.
									(current specialist, profile and network). */
								stFrame.updateStats(event.getSpecialistId(), profile, network); //

							} // 

						});

					count = 0;
				}
			} else {
				SocialNetworkRegistry.logger.fatal("Attempting to register "
						+ event.getTraderId() + " to non-existing specialist "
						+ event.getSpecialistId() + " !");
				return;
			}
		} else {
			SocialNetworkRegistry.logger
					.fatal("Attempting to register non-existing trader "
							+ event.getTraderId() + " to "
							+ event.getSpecialistId() + " !");
			return;
		}
	}

	@Override
	protected void processSimulationStarted(final SimulationStartedEvent event) {
		super.processSimulationStarted(event);
		
		// ~MDC Attribute a unique integer to each working trader
		for (int i = 0; i < getWorkingTraders().length; i++) { //
			traderIndices.put(getWorkingTraders()[i], new Integer(i));  //
		} //
		
		mSAdjacencyMatrix = new AtomicReferenceArray<AtomicIntegerArray>(
				getNumOfTraders());
				
		for (int i = 0; i < mSAdjacencyMatrix.length(); i++) {
			mSAdjacencyMatrix.set(i, new AtomicIntegerArray(getNumOfTraders()));
			for (int j = 0; j < mSAdjacencyMatrix.length(); j++) {
				mSAdjacencyMatrix.get(i).set(j, 0);
			}
		}

		mSTraderIds = new AtomicReferenceArray<String>(Utils
				.shuffleIds(getTraderIds()));

		mSTraderIdIdx = new Hashtable<String, Integer>();
		for (int i = 0; i < mSTraderIds.length(); i++) {
			mSTraderIdIdx.put(mSTraderIds.get(i), i);
		}
				
		sHistory = new Hashtable<String, ShoutingHistoryEntry>(
				getNumOfTraders());

		mSHistory = new Hashtable<String, String>(getNumOfTraders());
		tradeEntitlements = new Hashtable<String, Integer>(getNumOfTraders());

		// ~MDC Instantiate a record for each trader's successful transactions
		numTraderMatches = new Hashtable<String, Integer>(getNumOfTraders()); //

		// ~MDC
		tExperience = new Hashtable<String, Boolean>(getNumOfTraders()); //

		// ~MDC
		tProfit = new Hashtable<String, Double>(getNumOfTraders()); //

		// ~MDC
		advisors = new Hashtable<String, Integer>(getNumOfTraders()); //

		for (String id : getTraderIds()) {
			mSHistory.put(id, "");
			tradeEntitlements.put(id, 0);

			// ~MDC
			numTraderMatches.put(id, 0); //

			// ~MDC
			advisors.put(id, 0); //

			// ~MDC
			tExperience.put(id, false); //

			// ~MDC
			tProfit.put(id, 0.0); //
		}

		// initialize numMatches and mProfit
		numMatches = new Hashtable<String, Integer>(getNumOfSpecialists());
		mProfit = new Hashtable<String, Double>(getNumOfSpecialists());

		for (String id : getSpecialistIds()) {
			numMatches.put(id, 0);
			mProfit.put(id, 0.0);
		}

		marketSelectionStrategy.initMatrix();
		mimickingShoutingMethod.initMethod();

		// initialize specialist visualization
		sVs = new Hashtable<String, SpecialistVisualization>();

		for (final Specialist s : getSpecialists()) {
			try {
				SpecialistVisualization sv = new SpecialistVisualization(s
						.getId());
				sVs.put(s.getId(), sv);
				for (String id : getWorkingTraderIds()) {
					String type = SpecialistVisualization
							.getTraderTypeFromId(id);
					sv.AddTraderType(type);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// initialize social network GUI
		if (enableGUI)
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						createMainFrame();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		outputPW.println("## Total Number of traders");
		outputPW.println(getNumOfTraders());
		outputPW.println(" ");
		outputPW.println("## Total Number of markets");
		outputPW.println(getNumOfSpecialists());
		outputPW.flush();
	
	}

	@Override
	protected void processGameStarting(GameStartingEvent event) {
		super.processGameStarting(event);

		outputPW.println("#Game");
		outputPW.println(curIteration++);
		outputPW.flush(); //
	}

	@Override
	protected void processDayOpening(DayOpeningEvent event) {
		super.processDayOpening(event);
	}

	static int profitPostedCount = 0;

	/**
	 * ~MDC
	 *
	 *
	 */
	protected void processTraderProfit(TraderProfitEvent event) { // 

		profitPostedCount++;

		// ~MDC
		tProfit.put(event.getTraderId(), event.getTraderProfit()); // 

		// ~MDC		
		Trader ree = getTrader(event.getTraderId()); //

		// ~MDC		
		if (ree.getReferrerId() != null && event.getEarnedWithAdvice()) { //

			// ~MDC
		    Trader rer = getTrader(ree.getReferrerId()); //

			/* ~MDC Reward profit as a utility to the trader who gave the 
			    advice upon which said profit was made. */
			getMarketSelectionStrategy().reward(rer, event.getTraderProfit()); //
			getInformedShoutingMethod().reward(rer, event.getTraderProfit()); //

			// ~MDC
			logger.info("Attributing " + event.getTraderProfit() + " to " + ree.getReferrerId()); //

			// ~MDC
			addVisualLink(rer.getId(), ree.getId()); //

		}

		if (profitPostedCount == getNumOfWorkingTraders()) {

			// ~MDC
			if (stFrame != null && stFrame.isVisible()) 

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						stFrame.updateProfit(tProfit);

					}

			});

			profitPostedCount = 0;
			
		}

	}

	@Override
	protected void processDayOpened(DayOpenedEvent event) {
		super.processDayOpened(event);

		logger.info("All! =========== Day: " + event.getDay() + " =========== "); //

		// ~MDC
		for (String id : getTraderIds()) { //

			// ~MDC
			numTraderMatches.put(id, 0); //

		}

		for (Entry<String, Integer> e : tradeEntitlements.entrySet()) {
			e.setValue(getTrader(e.getKey()).getEntitlement());
		}
	}

	@Override
	protected void processRoundOpened(RoundOpenedEvent event) {
	
		logger.info("All! =========== Day: " + event.getDay() + " | Round: " + event.getRound() + " =========== "); //
		
		super.processRoundOpened(event);

	}

	// -------------------------------
	// DAY RUNNING EVENTS
	// -------------------------------

	@Override
	protected void processShoutRejected(ShoutRejectedEvent event) {
	
		super.processShoutRejected(event); //
		
		// ~MDC
		logger.info(event.getShout().getTrader().getId() + "! has their shout rejected."); //
	
	}

	@Override
	protected void processShoutPlaced(ShoutPlacedEvent event) {

		super.processShoutPlaced(event);

		// update a trader's shouting history
		Shout s = event.getShout();
		String tId = s.getTrader().getId();
		if (sHistory.containsKey(tId)) {
			ShoutingHistoryEntry she = sHistory.get(tId);
			she.setId(s.getId());			
			she.setPrice(s.getPrice());
			she.setQuantity(s.getQuantity());
		} else {
			sHistory.put(tId, new ShoutingHistoryEntry(s.getId(), s.getPrice(), s
					.getQuantity()));
			logger.info(tId + "! has their shout placed."); //
			logger.info(tId + " New Shouting History Entry: " + s.getId() + " " + s.getPrice() + " "  + s.getQuantity()); //
		}

		// ~MDC
		if (stFrame != null && stFrame.isVisible())
			
			// ~MDC 
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					stFrame.updateLog(); //

				}

		});

	}
	
	protected void processTransactionRejected(TransactionRejectedEvent event) { //
		
		// ~MDC
		logger.info(event.getTransaction().getAsk().getTrader().getId() + "! has their shout placed, but not matched."); //
		logger.info(event.getTransaction().getBid().getTrader().getId() + "! has their shout placed, but not matched."); //

	}
	

	@Override
	protected void processTransactionExecuted(TransactionExecutedEvent event) {

		super.processTransactionExecuted(event);

		// ~MDC
		String id = event.getTransaction().getSpecialist().getId();//

		// ~MDC
		Shout ask = event.getTransaction().getAsk();//
		Shout bid = event.getTransaction().getBid();// 

		// ~MDC
		String sellerId = ask.getTrader().getId();//
		String buyerId = bid.getTrader().getId();// 

		// ~MDC	
		final Trader seller = getTrader(sellerId);//
		final Trader buyer = getTrader(buyerId);//
		
		numMatches.put(id, numMatches.get(id) + 1);

		logger.info(sellerId + "! successful match!"); //
		logger.info(buyerId + "! successful match!"); //

		// ~MDC		
		numTraderMatches.put(sellerId, numTraderMatches.get(sellerId) + 1);// 	
		numTraderMatches.put(buyerId, numTraderMatches.get(buyerId) + 1);//

		// ~MDC
		if (sHistory.containsKey(sellerId)) { //

			// ~MDC
			ShoutingHistoryEntry aShe = sHistory.get(sellerId); //
			// ~MDC
			aShe.setExperience(true);//

		} // 

		if (sHistory.containsKey(buyerId)) { //
			ShoutingHistoryEntry bShe = sHistory.get(buyerId); //
			
			bShe.setExperience(true); //
		} //
		
		int quantityExchanged = event.getTransaction().getQuantity();
		tradeEntitlements.put(sellerId, tradeEntitlements.get(sellerId)
				- quantityExchanged);
		tradeEntitlements.put(buyerId, tradeEntitlements.get(buyerId)
				- quantityExchanged);
	}

	@Override
	protected void processFundTransfer(FundTransferEvent event) {
		super.processFundTransfer(event);

		if (event.getPayer() instanceof Specialist) {
			String id = event.getPayer().getId();
			mProfit.put(id, -event.getAmount() + mProfit.get(id));
		}

		if (event.getPayee() instanceof Specialist) {
			String id = event.getPayee().getId();
			mProfit.put(id, event.getAmount() + mProfit.get(id));
		}
	}

	// -------------------------------
	// CLOSING EVENTS
	// -------------------------------

	@Override
	protected void processRoundClosed(RoundClosedEvent event) {
		super.processRoundClosed(event);

		// update network structure inside current market for next round
		mimickingShoutingMethod.updateMatrix();
	}

	@Override
	protected void processDayClosed(DayClosedEvent event) {

		super.processDayClosed(event);

		outputPW.println(" "); //

		// ~MDC 
		if (event.getDay() != 0) {

			outputPW.println("## Individual Trader Profits"); //

			for (Entry<String, Double> e : tProfit.entrySet()) { //
				outputPW.println(e.getKey()); //
				outputPW.println(e.getValue()); //
			}

			outputPW.println(" ");

		}

		outputPW.println("########################################"); //
		outputPW.println("#Day");	
		outputPW.println(event.getDay());

		for (SpecialistVisualization sv : sVs.values()) {

			outputPW.println(" ");
			outputPW.println("########################################");
			outputPW.println("#Market");
			outputPW.println(sv.getSpecialistId());
			outputPW.println(" ");

			outputPW.println("## Market profit");
			outputPW.println(mProfit.get(sv.getSpecialistId()));
			// clear count for market profit on each day
			mProfit.put(sv.getSpecialistId(), 0.0);
			outputPW.println(" ");
			
			outputPW.println("## Number of matches");
			outputPW.println(numMatches.get(sv.getSpecialistId()));
			// clear count for next day
			numMatches.put(sv.getSpecialistId(), 0);
			outputPW.println(" ");

			outputPW.println("#Populations");
			for (Entry<String, HashMap<String, ArrayList<String>>> en : sv
					.getTraders().entrySet()) {
				outputPW.printf("%s buyer, %d", en.getKey(), en.getValue().get(
						"buyer").size());
				outputPW.println();
				outputPW.printf("%s seller, %d", en.getKey(), en.getValue()
						.get("seller").size());
				outputPW.println();
			}
			outputPW.println(" ");

			sv.ClearTraders();
		}
		
		int numBCM, numSCM;
		numBCM = numSCM = 0;
		for (Entry<String, String> en : mSHistory.entrySet()) {
			Trader t = getTrader(en.getKey());
			if (!en.getValue().isEmpty()
					&& !t.getSpecialistId().equals(en.getValue())) {
				if (t.isSeller()) {
					numSCM++;
				} else {
					numBCM++;
				}
			}
		}

		outputPW.println("########################################");
		outputPW.println("#Traders");
		outputPW.println(" ");

		outputPW.println("## Number of buyers that changed market");
		outputPW.println(numBCM);
		outputPW.println(" ");

		outputPW.println("## Number of sellers that changed market");
		outputPW.println(numSCM);
		outputPW.println(" ");

		outputPW.println("## Individual Trader Matches"); //

		for (Entry<String, Integer> e : numTraderMatches.entrySet()) { //

			outputPW.println(e.getKey()); //
			outputPW.println(e.getValue()); //

		}

		outputPW.println("## Amount of Advice given:");
		
		for (Entry<String, Integer> e : advisors.entrySet()) { //

				outputPW.println(e.getKey()); //
				outputPW.println(e.getValue()); //

		}

		outputPW.flush();

		// ~MDC
		if (stFrame != null && stFrame.isVisible()) 

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					stFrame.updateMatches(numTraderMatches);
					

				}

		});
		
		// update market selection adjacency matrix before next day
		marketSelectionStrategy.updateMatrix();

		// update market selection histories at the end
		for (Trader t : getWorkingTraders()) {
			mSHistory.put(t.getId(), t.getSpecialistId());

			// ~MDC
			tExperience.put(t.getId(), (numTraderMatches.get(t.getId())) > (t.getEntitlement() / 2) ? true : false); //
		}

		// clear shouting histories for next day
		sHistory.clear();
	}

	
	@Override
	protected void processSimulationOver(SimulationOverEvent event) {
		super.processSimulationOver(event);

		if (outputPW != null) {
			outputPW.close();
		}
	}

	// -------------------------------
	// UTILITIES
	// -------------------------------
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public GameClock getClock() {
		return clock;
	}

	/**
	 * Buffer market selections for later drawing
	 * 
	 * @param event
	 */
	public void buffersVs(final RegistrationEvent event) {
		for (SpecialistVisualization f : sVs.values()) {
			f.bufferMS(event.getDay());
		}
	}	

	/**
	 * 
	 */
	private synchronized void addVisualLink(String tFrom, String tTo) { //

		// ~MDC
		sGraph.addEdge(sGraph.getEdgeCount() + 1, tFrom, tTo, EdgeType.DIRECTED); //

	}

	/**
	 * 
	 */
	private void traderOnGraph(String traderId) { //

		// ~MDC
		sGraph.addVertex(traderId); //

	}

	protected void createMainFrame() {

		mFrame = new MainFrame();
		sFrame = mFrame.getsFrame();
		msAMFrame = mFrame.getMsAMFrame();

		// ~MDC
		stFrame = mFrame.getstFrame(); //

	}

}
