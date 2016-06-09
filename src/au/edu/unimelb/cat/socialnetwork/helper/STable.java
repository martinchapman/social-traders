package au.edu.unimelb.cat.socialnetwork.helper;

import java.awt.Container;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.DefaultListCellRenderer.UIResource;
import javax.swing.border.Border;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 97 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:19:07 +1100 (Sun, 28 Feb 2010) $
 */
public class STable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JViewport colHeader = null;

	public STable(TableModel tm, TableColumnModel tcm) {
		super(tm, tcm);
	}

	@Override
	protected void configureEnclosingScrollPane() {
		if (colHeader == null) {
			super.configureEnclosingScrollPane();
		} else {
			Container p = getParent();
			if (p instanceof JViewport) {
				Container gp = p.getParent();
				if (gp instanceof JScrollPane) {
					JScrollPane scrollPane = (JScrollPane) gp;
					// Make certain we are the viewPort's view and not, for
					// example, the rowHeaderView of the scrollPane -
					// an implementor of fixed columns might do this.
					JViewport viewport = scrollPane.getViewport();
					if (viewport == null || viewport.getView() != this) {
						return;
					}
					scrollPane.setColumnHeader(colHeader);
					// scrollPane.getViewport().setBackingStoreEnabled(true);
					Border border = scrollPane.getBorder();
					if (border == null || border instanceof UIResource) {
						Border scrollPaneBorder = UIManager
								.getBorder("Table.scrollPaneBorder");
						if (scrollPaneBorder != null) {
							scrollPane.setBorder(scrollPaneBorder);
						}
					}
				}
			}
		}
	}

	public void setColHeader(JViewport colHeader) {
		this.colHeader = colHeader;
	}
}
