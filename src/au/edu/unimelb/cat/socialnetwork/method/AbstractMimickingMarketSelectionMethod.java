package au.edu.unimelb.cat.socialnetwork.method;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.swing.SwingUtilities;

import edu.cuny.cat.registry.SocialNetworkRegistry;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

import au.edu.unimelb.cat.socialnetwork.ui.MarketSelectionAMFrame;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 115 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:07:27 +1100 (Sun, 28 Feb 2010) $
 */
public abstract class AbstractMimickingMarketSelectionMethod extends
		AbstractMimickingMethod implements Parameterizable {

	protected AtomicReferenceArray<AtomicIntegerArray> mSAdjacencyMatrix;
	protected AtomicReferenceArray<String> mSTraderIds;
	protected Hashtable<String, Integer> mSTraderIdIdx;
	protected Hashtable<String, String> mSHistory;
	protected Hashtable<String, Boolean> tExperience;
	protected boolean experience;

	public AbstractMimickingMarketSelectionMethod() {
		super();

		experience = false;
	}

	public void initMatrix() {
		// Copying references from snr
		mSAdjacencyMatrix = snr.getmSAdjacencyMatrix();
		mSTraderIds = snr.getmSTraderIds();
		mSTraderIdIdx = snr.getmSTraderIdIdx();
		mSHistory = snr.getmSHistory();
		tExperience = snr.getTExperience();
	}

	public String packQueryResult(String sId, boolean e) {
		return String.format("specialistid=%s, experience=%s", sId, e);
	}

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {
		//if (parameters.exists(base.push("chancestomimick")))
			//percentage = parameters.getDouble(base.push("chancestomimick"),
					//null, 0);
	}

	@Override
	public void updateMatrix() {
		final MarketSelectionAMFrame msAMFrame = SocialNetworkRegistry
				.getInstance().getMsAMFrame();
		if (msAMFrame != null && msAMFrame.isVisible())
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						msAMFrame.getMsDataTM().fireTableDataChanged();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	}
}
