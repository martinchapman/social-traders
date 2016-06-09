package au.edu.unimelb.cat.socialnetwork.method;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import edu.cuny.cat.core.Trader;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

public abstract class AbstractMimickingShoutingMethod extends
		AbstractMimickingMethod implements Parameterizable {

	protected AtomicReferenceArray<AtomicIntegerArray> mSAdjacencyMatrix;
	protected AtomicReferenceArray<String> mSTraderIds;
	protected Hashtable<String, Integer> mSTraderIdIdx;
	protected Hashtable<String, ShoutingHistoryEntry> sHistory;
	protected Hashtable<String, Integer> tradeEntitlements;
	protected Map<String, Trader> workingTraders;
	protected Boolean experience;

	public AbstractMimickingShoutingMethod() {
		super();

		experience = false;
	}

	public void initMethod() {
		// Copying references from snr
		mSAdjacencyMatrix = snr.getmSAdjacencyMatrix();
		mSTraderIds = snr.getmSTraderIds();
		mSTraderIdIdx = snr.getmSTraderIdIdx();
		sHistory = snr.getsHistory();
		tradeEntitlements = snr.getTradeEntitlements();
	}

	public String packQueryResult(ShoutingHistoryEntry she, Boolean e) {
		return String.format("price=%f, quantity=%d, experience=%s", she.getPrice(),
				she.getQuantity(), e);
	}

	@Override
	public void setup(ParameterDatabase parameters, Parameter base) {
		//if (parameters.exists(base.push("chancestomimick")))
			//percentage = parameters.getDouble(base.push("chancestomimick"),
					//null, 0);
	}

}
