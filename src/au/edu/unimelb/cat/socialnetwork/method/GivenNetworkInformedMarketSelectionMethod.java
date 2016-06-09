package au.edu.unimelb.cat.socialnetwork.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.TreeSet;
import org.apache.log4j.Logger;

import cern.jet.random.Uniform;

import edu.cuny.cat.Game;
import edu.cuny.cat.core.Trader;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import au.edu.unimelb.cat.socialnetwork.helper.*;

//~MDC 15/2/11
import edu.cuny.ai.learning.NArmedBanditLearner;


/**
 * 
 * @author Guan Gui
 * @version $Rev: 122 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:07:27 +1100 (Sun, 28 Feb 2010) $
 */
public class GivenNetworkInformedMarketSelectionMethod extends
		AbstractMimickingMarketSelectionMethod implements Parameterizable {
		
	static Logger logger = Logger.getLogger(GivenNetworkInformedMarketSelectionMethod.class);
			
	private String inputFile;
	
	private Uniform dist;

	private NArmedBanditLearner learner; //

	public static final String P_LEARNER = "learner"; //
	
	public GivenNetworkInformedMarketSelectionMethod() {

		super();

	}

	public void reward(Trader trader, double reputation) {

		learner.getReturnUpdatingPolicy().reward(snr.getIndexFromTrader(trader), reputation);

	}

	private Set<Integer> linkedTraders; //

	@Override
	public synchronized String queryMatrix(String tId) {
		
		linkedTraders = new HashSet<Integer>(); //

		AtomicIntegerArray row = mSAdjacencyMatrix.get(mSTraderIdIdx.get(tId));

		ArrayList<Integer> value1Entry = new ArrayList<Integer>();
		
		for (int i = 0; i < row.length(); i++) 
		{

			if (row.get(i) == 1) {	

				value1Entry.add(i);				

				final Integer index = snr.getIndexFromTrader(snr.getTrader(mSTraderIds.get(i))); //
				
				linkedTraders.add(index); //

			}

		}
		
		if (value1Entry.isEmpty()) {

			return packQueryResult("", false);

		} else {

			final Integer recommendationIndex = learner.act(linkedTraders); //

			logger.info(tId + "! is on row " + mSTraderIdIdx.get(tId) + " and is therefore linked to:"); //
		
			Iterator iter = linkedTraders.iterator();
    		
			while (iter.hasNext()) {

				int index = (Integer)iter.next();	

				logger.info(tId + "! " + snr.getTraderFromIndex(index).getId() + " with reputation " + learner.getReturnUpdatingPolicy().getReturns()[index]);      
			}
		
			Trader referrer = snr.getTraderFromIndex(recommendationIndex); //

			Trader referee = snr.getTrader(tId); //

			referee.setReferrerId(referrer.getId()); //

			logger.info(tId + "! " + referrer.getId() + " is selected to give advice to " + referee.getId()); //

			snr.incrementAdvice(referrer.getId()); 

			logger.info(tId + "! " + referrer.getId() + " holds advice on " + mSHistory.get(referrer.getId()) + " and this advice is " + tExperience.get(referrer.getId())); //

			return packQueryResult(mSHistory.get(referrer.getId()),
					tExperience.get(referrer.getId())); //
		}
	}

	@Override
	public void updateMatrix() {

		int chancesToRewire = -1; //

		for (int i = 0; i < mSAdjacencyMatrix.length(); i++) {

			// randomly pick 1 neighbor to re-wire
			ArrayList<Integer> value1Entry = new ArrayList<Integer>();

			AtomicIntegerArray row = mSAdjacencyMatrix.get(i);

			for (int j = 0; j < row.length(); j++) {

				if (row.get(j) == 1) {
					value1Entry.add(j);
				}

			}

			for (int j = 0; j < value1Entry.size(); j++) {

				if (dist.nextDouble() <= chancesToRewire) {
					mSAdjacencyMatrix.get(i).set(value1Entry.get(j), 0);
					mSAdjacencyMatrix.get(i).set(
							dist.nextIntFromTo(0,
									mSAdjacencyMatrix.length() - 1), 1);
				}

			}
		}

		super.updateMatrix();
	}

	@Override
	public void initMatrix() {

		super.initMatrix();

		learner.setNumberOfActions(snr.getTraderIndices().size()); //
		
		learner.initialize(); //

		learner.reset(); //

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				mSAdjacencyMatrix.get(i).set(j, matrix[i][j]);
			}
		}

	}

	private int[][] matrix;

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {

		dist = new Uniform(0, 1, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine());

		learner = parameters
				.getInstanceForParameterEq(
						base
								.push(GivenNetworkInformedMarketSelectionMethod.P_LEARNER),
						null, NArmedBanditLearner.class); //
		if (learner instanceof Parameterizable) { //
			((Parameterizable) learner).setup(parameters, base
					.push(GivenNetworkInformedMarketSelectionMethod.P_LEARNER)); //
		}	

		inputFile = parameters.getString(base.push("inputfile"), null);

		try {

			if (Utils.getFileNameSuffix(inputFile).equalsIgnoreCase("adm")) {

				// Adjacency Matrix
				matrix = Utils.readADMFile(inputFile);

			} else if (Utils.getFileNameSuffix(inputFile).equalsIgnoreCase(
					"adl")) {

				// Adjacency List
				matrix = Utils.readADLFile(inputFile);

			} else if ((inputFile.substring(inputFile.lastIndexOf("/") + 1, inputFile.lastIndexOf("."))).equalsIgnoreCase("random")) {

				matrix = Utils.randomMatrix(Integer.parseInt(inputFile.substring(inputFile.lastIndexOf(".") + 1, inputFile.length())));

			} else {

				throw new RuntimeException(
						"Unexpected file type! Supported file type: .adm, .adl");

			}

		} catch (Exception e) {

			logger.error(e.getMessage());

		}

		
		/*int numTypes = 2;
		int initialtradeentitlement = 3;
		int remNum = matrix.length;
		int typeSize = matrix.length / numTypes;

		parameters
				.set(new Parameter("cat.agent.n"), Integer.toString(numTypes));

		// trader type 0
		parameters.set(new Parameter("cat.agent.0"),
				"edu.cuny.cat.SocialNetworkTraderClient");
		parameters.set(new Parameter("cat.agent.0.n"), Integer
				.toString(typeSize));
		remNum -= typeSize;
		parameters.set(new Parameter("cat.agent.0.isseller"), "false");
		parameters.set(new Parameter("cat.agent.0.type"), "buyer");
		parameters.set(new Parameter("cat.agent.0.id"), "buyerGD");
		parameters.set(new Parameter("cat.agent.0.initialtradeentitlement"),
				Integer.toString(initialtradeentitlement));
		parameters.set(new Parameter("cat.agent.0.strategy"),
				"edu.cuny.cat.trader.strategy.SocialNetworkShoutingStrategy");
		parameters.set(
				new Parameter("cat.agent.0.strategy.underlyingstrategy"),
				"edu.cuny.cat.trader.strategy.RandomConstrainedStrategy");
		parameters
				.set(new Parameter("cat.agent.0.marketselectionstrategy"),
						"edu.cuny.cat.trader.marketselection.SocialNetworkMarketSelectionStrategy");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy"),
						"edu.cuny.cat.trader.marketselection.StimuliResponseMarketSelectionStrategy");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy.learner"),
						"edu.cuny.ai.learning.NArmedBanditLearner");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy.learner.action_choosing"),
						"edu.cuny.ai.learning.EpsilonGreedyActionChoosingPolicy");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy.learner.action_choosing.epsilon"),
						"0.1");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy.learner.return_updating"),
						"edu.cuny.ai.learning.AdaptiveReturnUpdatingPolicy");
		parameters
				.set(
						new Parameter(
								"cat.agent.0.marketselectionstrategy.underlyingstrategy.learner.return_updating.learner"),
						"edu.cuny.ai.learning.WidrowHoffLearner");

		// trader type 1
		parameters.set(new Parameter("cat.agent.1"),
				"edu.cuny.cat.SocialNetworkTraderClient");
		parameters
				.set(new Parameter("cat.agent.1.n"), Integer.toString(remNum));
		parameters.set(new Parameter("cat.agent.1.isseller"), "true");
		parameters.set(new Parameter("cat.agent.1.type"), "seller");
		parameters.set(new Parameter("cat.agent.1.id"), "sellerGD");
		parameters.set(new Parameter("cat.agent.1.initialtradeentitlement"),
				Integer.toString(initialtradeentitlement));
		parameters.set(new Parameter("cat.agent.1.strategy"),
				"edu.cuny.cat.trader.strategy.SocialNetworkShoutingStrategy");
		parameters.set(
				new Parameter("cat.agent.1.strategy.underlyingstrategy"),
				"edu.cuny.cat.trader.strategy.RandomConstrainedStrategy");
		parameters
				.set(new Parameter("cat.agent.1.marketselectionstrategy"),
						"edu.cuny.cat.trader.marketselection.SocialNetworkMarketSelectionStrategy");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy"),
						"edu.cuny.cat.trader.marketselection.StimuliResponseMarketSelectionStrategy");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy.learner"),
						"edu.cuny.ai.learning.NArmedBanditLearner");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy.learner.action_choosing"),
						"edu.cuny.ai.learning.EpsilonGreedyActionChoosingPolicy");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy.learner.action_choosing.epsilon"),
						"0.1");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy.learner.return_updating"),
						"edu.cuny.ai.learning.AdaptiveReturnUpdatingPolicy");
		parameters
				.set(
						new Parameter(
								"cat.agent.1.marketselectionstrategy.underlyingstrategy.learner.return_updating.learner"),
						"edu.cuny.ai.learning.WidrowHoffLearner"); */
	}

	public void loadIdentityMatrix() {

		// set identity matrix
		for (int i = 0; i < mSTraderIds.length(); i++) {

			for (int j = 0; j < mSTraderIds.length(); j++) {

				if (i == j) {

					mSAdjacencyMatrix.get(i).set(j, 1);

				} else {

					mSAdjacencyMatrix.get(i).set(j, 0);

				}

			}

		}

	}

}
		
