package au.edu.unimelb.cat.socialnetwork.method;

import java.util.concurrent.atomic.AtomicIntegerArray;
import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.cat.Game;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.cat.core.Trader;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 122 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:07:27 +1100 (Sun, 28 Feb 2010) $
 */
public class RandomMimickingMarketSelectionMethod extends
		AbstractMimickingMarketSelectionMethod {

	private static Logger logger = Logger
			.getLogger(RandomMimickingMarketSelectionMethod.class);
	private Uniform dist;

	public RandomMimickingMarketSelectionMethod() {
		super();
	}

	@Override
	public void reward(Trader trader, double reward) { //

		//purposefully empty.

	} //

	@Override
	public String queryMatrix(String tId) {
		AtomicIntegerArray row = mSAdjacencyMatrix.get(mSTraderIdIdx.get(tId));
		for (int i = 0; i < row.length(); i++) {
			if (row.get(i) == 1) {
				return packQueryResult(mSHistory.get(mSTraderIds.get(i)),
					tExperience.get(mSTraderIds.get(i)));
			}
		}
		// beware that in this method, each row must have an entry with value 1
		logger.fatal("RandomMimickingMarketSelectionMethod failure!");
		return null;
	}

	@Override
	public void updateMatrix() {
		/* produce random adjacency matrix each day */
		for (int i = 0; i < mSAdjacencyMatrix.length(); i++) {
			for (int j = 0; j < mSAdjacencyMatrix.length(); j++) {
				mSAdjacencyMatrix.get(i).set(j, 0);
			}
			mSAdjacencyMatrix.get(i).set(dist.nextInt(), 1);
		}
		//Utils.print2DIntArray(mSAdjacencyMatrix.)
		super.updateMatrix();
	}

	@Override
	public void initMatrix() {
		super.initMatrix();

		dist = new Uniform(0, mSAdjacencyMatrix.length() - 1, Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class).getEngine());

		updateMatrix();
	}
}
