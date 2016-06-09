package au.edu.unimelb.cat.socialnetwork.method;

import edu.cuny.cat.registry.SocialNetworkRegistry;
import edu.cuny.cat.core.Trader;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 93 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:07:27 +1100 (Sun, 28 Feb 2010) $
 */
public abstract class AbstractMimickingMethod {

	protected SocialNetworkRegistry snr;

	public AbstractMimickingMethod() {
		snr = SocialNetworkRegistry.getInstance();
	}

	/**
	 * Update the network structure matrix
	 */
	public abstract void updateMatrix();

	/**
	 * Query the network structure matrix
	 * 
	 * @param tId
	 *            trader Id
	 * @return query result to be returned as in TEXT header of the response
	 *         message
	 */
	public abstract String queryMatrix(String tId);

	public abstract void reward(Trader trader, double reputation); //

}
