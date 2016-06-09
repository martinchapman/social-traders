package au.edu.unimelb.cat.socialnetwork.ui;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 94 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          00:55:20 +1100 (Sun, 28 Feb 2010) $
 */
public class MarketSelectionAMFrame extends AdjacencyMatrixFrame implements
		TableModelListener {

	private static final long serialVersionUID = 1L;
	private AtomicReferenceArray<AtomicIntegerArray> mSAdjacencyMatrix;
	private AtomicReferenceArray<String> mSTraderIds;
	private MSDataTM msDataTM;

	public class MSDataTM extends DataTM {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return mSAdjacencyMatrix.get(rowIndex).get(columnIndex);
		}

		@Override
		public int getRowCount() {
			return mSTraderIds.length();
		}

		@Override
		public int getColumnCount() {
			return mSTraderIds.length();
		}

		@Override
		public void updateMatrix(int r, int c, int value) {
			mSAdjacencyMatrix.get(r).set(c, value);
		}
	}

	private class MSColHeaderTM extends ColHeaderTM {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return mSTraderIds.get(columnIndex);
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return mSTraderIds.length();
		}
	}

	private class MSRowHeaderTM extends RowHeaderTM {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return mSTraderIds.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return mSTraderIds.length();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}
	}

	public MarketSelectionAMFrame() {
		super();

		this.setTitle("Market Selection Adjacency Matrix Frame");
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// Copying references from snr
		mSAdjacencyMatrix = snr.getmSAdjacencyMatrix();
		mSTraderIds = snr.getmSTraderIds();

		colHeader.setModel(new MSColHeaderTM());
		colHeader.createDefaultColumnsFromModel();
		rowHeader.setModel(new MSRowHeaderTM());
		rowHeader.createDefaultColumnsFromModel();
		msDataTM = new MSDataTM();
		data.setModel(msDataTM);
		data.createDefaultColumnsFromModel();

		corner.setText(wrapJLabelText(mSTraderIds.length() + " x "
				+ mSTraderIds.length(), corner));
	}

	public MSDataTM getMsDataTM() {
		return msDataTM;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
	}
}
