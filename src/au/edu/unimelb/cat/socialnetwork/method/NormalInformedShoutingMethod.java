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

import edu.cuny.ai.learning.NArmedBanditLearner; //

/**
 * 
 * @author Guan Gui
 * @version $Rev: 122 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-03-03
 *          11:35:31 +1100 (Wed, 03 Mar 2010) $
 */
public class NormalInformedShoutingMethod extends
		AbstractMimickingShoutingMethod implements Parameterizable {

	private static Logger logger = Logger
			.getLogger(NormalInformedShoutingMethod.class);

	private NArmedBanditLearner learner;//

	public static final String P_LEARNER = "learner"; //

	private Set<Integer> linkedTraders;//

	public void reward(Trader trader, double reputation) { //

		learner.getReturnUpdatingPolicy().reward(snr.getIndexFromTrader(trader), reputation); //

	} //

	@Override
	public synchronized String queryMatrix(String tId) {

		// randomly select an entry with value 1 inside the trader's market
		ArrayList<Integer> value1Entry = new ArrayList<Integer>();
		
		linkedTraders = new HashSet<Integer>(); //

		Trader curT = snr.getTrader(tId); //

		AtomicIntegerArray row = mSAdjacencyMatrix.get(mSTraderIdIdx.get(tId));

		for (int i = 0; i < row.length(); i++) {

			Trader posT = snr.getTrader(mSTraderIds.get(i));

			if (row.get(i) == 1
					
					&& posT.getSpecialistId().equals(curT.getSpecialistId())

					&& posT.isSeller() == curT.isSeller()) {
					
				logger.info(tId + " is linked to " + posT.getId());

				ShoutingHistoryEntry cpSHE = sHistory.get(posT.getId());

				if (!(cpSHE == null)) {

					logger.info(tId + " Current trader / potential trader price: " + curT.getPrivateValue() + " | " + cpSHE.getPrice()); 
					
					logger.info(tId + " Current trader / potential trader quantity: " + tradeEntitlements.get(curT.getId()) + " | " + cpSHE.getQuantity()); 

					if (curT.isSeller()) {
						if (cpSHE.getPrice() >= curT.getPrivateValue()
								&& tradeEntitlements.get(curT.getId()) >= cpSHE.getQuantity()) {
							value1Entry.add(i);
							final Integer index = snr.getIndexFromTrader(snr.getTrader(mSTraderIds.get(i))); //
							linkedTraders.add(index); //
						}
					} else {
						if (cpSHE.getPrice() <= curT.getPrivateValue()
								&& tradeEntitlements.get(curT.getId()) >= cpSHE.getQuantity()) {
							value1Entry.add(i);
							final Integer index = snr.getIndexFromTrader(snr.getTrader(mSTraderIds.get(i))); //
							linkedTraders.add(index); //
						}
					}
				} else {
				
					logger.info(tId + " No shouting information available for " + posT.getId());
				
				}
			}

		}

		if (value1Entry.isEmpty()) {

			// copy not available
			logger.info(tId + " has no relevant, socially-linked shouting advice available.");
			return packQueryResult(new ShoutingHistoryEntry("", -1, -1), false);

		} else {

			final Integer recommendationIndex = learner.act(linkedTraders); //

			Iterator iter = linkedTraders.iterator();
    		
			while (iter.hasNext()) 
			{
				int index = (Integer)iter.next();				
				logger.info(tId + " " + snr.getTraderFromIndex(index).getId() + " has reputation " + learner.getReturnUpdatingPolicy().getReturns()[index]);      
			}

			Trader referrer = snr.getTraderFromIndex(recommendationIndex); //
			Trader referee = snr.getTrader(tId); //
			referee.setReferrerId(referrer.getId()); //	
			logger.info(tId + " " + referrer.getId() + " is selected to give advice to " + referee.getId()); // 	
			
			ShoutingHistoryEntry cSHE = sHistory.get(referrer.getId()); //

			//learner.reward(referrer.getReputation());	 //
			
			logger.info(tId + " " + referrer.getId() + " is holding advice on shout value " + cSHE.getPrice() + " and quantity " + cSHE.getQuantity() + " and this advice is " + cSHE.getExperience()); //

			return packQueryResult(cSHE, cSHE.getExperience()); //
		}
	}

	@Override
	public void initMethod() {

		super.initMethod();

		learner.setNumberOfActions(snr.getTraderIndices().size()); //

		learner.initialize(); //

		learner.reset(); // 

	}

	@Override
	public void updateMatrix() {
		// TODO Update social network structure inside a market
	}

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {
		super.setup(parameters, base);

		learner = parameters
				.getInstanceForParameterEq(
						base
								.push(NormalInformedShoutingMethod.P_LEARNER),
						null, NArmedBanditLearner.class); //
		if (learner instanceof Parameterizable) { //
			((Parameterizable) learner).setup(parameters, base
					.push(NormalInformedShoutingMethod.P_LEARNER)); //
		}	
	}

}
