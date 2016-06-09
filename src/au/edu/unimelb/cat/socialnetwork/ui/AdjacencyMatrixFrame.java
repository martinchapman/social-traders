package au.edu.unimelb.cat.socialnetwork.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.jfree.ui.ExtensionFileFilter;

import edu.cuny.cat.registry.SocialNetworkRegistry;

import au.edu.unimelb.cat.socialnetwork.helper.STable;
import au.edu.unimelb.cat.socialnetwork.helper.Utils;

import java.awt.event.MouseAdapter;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 94 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          00:56:19 +1100 (Sun, 28 Feb 2010) $
 */
public class AdjacencyMatrixFrame extends JFrame {

	private static Logger logger = Logger.getLogger(AdjacencyMatrixFrame.class);
	private static final long serialVersionUID = -3598163349874732308L;
	private JPanel contentPane;
	protected STable data;
	protected JTable colHeader;
	protected JTable rowHeader;
	private static int cellSize;
	protected SocialNetworkRegistry snr;
	protected JLabel corner;
	private boolean metaDown;
	private boolean allSelected;
	protected HashSet<Point> selectedCells;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AdjacencyMatrixFrame frame = new AdjacencyMatrixFrame();
					// frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected class DataTM extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return 1;
		}

		@Override
		public int getRowCount() {
			return 10;
		}

		@Override
		public int getColumnCount() {
			return 10;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			try {
				int newValue = Integer.parseInt((String) aValue);
				if (allSelected) {
					for (int i = 0; i < getRowCount(); i++) {
						for (int j = 0; j < getColumnCount(); j++) {
							updateMatrix(i, j, newValue);
						}
					}
					allSelected = false;
					selectedCells.clear();
					fireTableDataChanged();
				} else if (selectedCells.isEmpty()) {
					int rowIndexStart = data.getSelectedRow();
					int rowIndexEnd = data.getSelectionModel()
							.getMaxSelectionIndex();
					int colIndexStart = data.getSelectedColumn();
					int colIndexEnd = data.getColumnModel().getSelectionModel()
							.getMaxSelectionIndex();
					// Check each cell in the range
					for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
						for (int c = colIndexStart; c <= colIndexEnd; c++) {
							if (data.isCellSelected(r, c)) {
								updateMatrix(r, c, newValue);
								fireTableCellUpdated(r, c);
							}
						}
					}
				} else {
					for (Point p : selectedCells) {
						updateMatrix(p.x, p.y, newValue);
						fireTableCellUpdated(p.x, p.y);
					}
				}
			} catch (NumberFormatException e) {
				logger.error("Invalid value change at row " + rowIndex
						+ " col " + columnIndex);
			}
		}

		public void updateMatrix(int r, int c, int newValue) {
			return;
		}
	}

	protected class ColHeaderTM extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return "Col" + columnIndex;
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount() {
			return 10;
		}
	}

	protected class RowHeaderTM extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return "row" + rowIndex;
		}

		@Override
		public int getRowCount() {
			return 10;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}
	}

	/**
	 * Create the frame.
	 */
	public AdjacencyMatrixFrame() {
		cellSize = 50;
		snr = SocialNetworkRegistry.getInstance();
		selectedCells = new HashSet<Point>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.add(scrollPane, BorderLayout.CENTER);

		TableModel dataTM = new DataTM();

		TableColumnModel dataTCM = new DefaultTableColumnModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void addColumn(TableColumn aColumn) {
				aColumn.setMaxWidth(cellSize);
				super.addColumn(aColumn);
			};
		};

		data = new STable(dataTM, dataTCM) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component prepareEditor(TableCellEditor editor, int row,
					int column) {
				JTextComponent comp = (JTextComponent) super.prepareEditor(
						editor, row, column);
				comp.selectAll();

				return comp;
			}

			@Override
			public void changeSelection(int rowIndex, int columnIndex,
					boolean toggle, boolean extend) {
				if (metaDown) {
					int[] sCols = data.getSelectedColumns();
					int[] sRows = data.getSelectedRows();
					for (int i = 0; i < sRows.length; i++) {
						for (int j = 0; j < sCols.length; j++) {
							selectedCells.add(new Point(sRows[i], sCols[j]));
						}
					}
					selectedCells.add(new Point(rowIndex, columnIndex));
					super.changeSelection(rowIndex, columnIndex, false, false);
				} else {
					clearSelectedCells();
					if (allSelected) {
						allSelected = false;
						((AbstractTableModel) data.getModel())
								.fireTableDataChanged();
					}
					super
							.changeSelection(rowIndex, columnIndex, toggle,
									extend);
				}
			}

			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				return String.format("(%d, %d)", rowIndex, colIndex);
			}
		};
		data.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				data.requestFocusInWindow();
			}
		});
		data.setTableHeader(null);
		data.createDefaultColumnsFromModel();
		data.setRowHeight(cellSize);
		data.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		data.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		data.setCellSelectionEnabled(true);
		data.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int r = data.rowAtPoint(p);
				int c = data.columnAtPoint(p);
				if (!(r == selectedRowIdx && c == selectedColIdx)) {
					((AbstractTableModel) data.getModel())
							.fireTableCellUpdated(r, c);
					((AbstractTableModel) data.getModel())
							.fireTableCellUpdated(selectedRowIdx,
									selectedColIdx);
					if (r != selectedRowIdx) {
						((AbstractTableModel) rowHeader.getModel())
								.fireTableCellUpdated(r, 0);
						((AbstractTableModel) rowHeader.getModel())
								.fireTableCellUpdated(selectedRowIdx, 0);
						selectedRowIdx = r;
					}
					if (c != selectedColIdx) {
						((AbstractTableModel) colHeader.getModel())
								.fireTableCellUpdated(0, c);
						((AbstractTableModel) colHeader.getModel())
								.fireTableCellUpdated(0, selectedColIdx);
						selectedColIdx = c;
					}
				}
			}
		});
		data.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					if (data.getCellEditor() != null) {
						data.getCellEditor().stopCellEditing();
					}
				}

				if (evt.getKeyCode() != KeyEvent.VK_META && metaDown) {
					if (evt.getKeyCode() == KeyEvent.VK_A) {
						// select all
						data.changeSelection(0, 0, false, false);
						allSelected = true;
					}
				} else if (metaDown = evt.isMetaDown()) {
					evt.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				metaDown = e.isMetaDown();
			}
		});
		data.setDefaultRenderer(Object.class, new DataCellRender());
		JTextField tf = new JTextField();
		tf.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		tf.setHorizontalAlignment(SwingConstants.CENTER);
		data.setDefaultEditor(Object.class, new DefaultCellEditor(tf));
		// data.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		JViewport jvData = new JViewport();
		jvData.setView(data);
		jvData.setPreferredSize(data.getMaximumSize());
		scrollPane.setViewport(jvData);

		TableModel colHeaderTM = new ColHeaderTM();

		TableColumnModel colHeaderTCM = new DefaultTableColumnModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void addColumn(TableColumn aColumn) {
				aColumn.setMaxWidth(cellSize);
				super.addColumn(aColumn);
			};
		};

		colHeader = new JTable(colHeaderTM, colHeaderTCM);
		colHeader.setTableHeader(null);
		colHeader.createDefaultColumnsFromModel();
		colHeader.setRowSelectionAllowed(false);
		colHeader.setCellSelectionEnabled(false);
		colHeader.setRowHeight(cellSize);
		colHeader.setBackground(Color.LIGHT_GRAY);
		colHeader.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		colHeader.setDefaultRenderer(Object.class, new HeaderCellRenderer());

		JViewport jvColHeader = new JViewport();
		jvColHeader.setView(colHeader);
		jvColHeader.setPreferredSize(colHeader.getMaximumSize());
		data.setColHeader(jvColHeader);

		TableModel rowHeaderTM = new RowHeaderTM();

		TableColumnModel rowHeaderTCM = new DefaultTableColumnModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void addColumn(TableColumn aColumn) {
				aColumn.setMaxWidth(cellSize);
				super.addColumn(aColumn);
			};
		};

		rowHeader = new JTable(rowHeaderTM, rowHeaderTCM);
		rowHeader.setTableHeader(null);
		rowHeader.createDefaultColumnsFromModel();
		rowHeader.setColumnSelectionAllowed(false);
		rowHeader.setCellSelectionEnabled(false);
		rowHeader.setRowHeight(cellSize);
		rowHeader.setBackground(Color.LIGHT_GRAY);
		rowHeader.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		rowHeader.setDefaultRenderer(Object.class, new HeaderCellRenderer());

		JViewport jvRowHeader = new JViewport();
		jvRowHeader.setView(rowHeader);
		jvRowHeader.setPreferredSize(rowHeader.getMaximumSize());
		scrollPane.setRowHeader(jvRowHeader);

		corner = new JLabel();
		corner.setText("size");
		corner.setFont(new Font("Arial", Font.PLAIN, 10));
		corner.setHorizontalAlignment(SwingConstants.CENTER);
		corner.setForeground(Color.DARK_GRAY);
		scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);

		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem saveAsItem = new JMenuItem("Save As...");

		saveItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Utils.saveMatrixAsADM(snr.getmSAdjacencyMatrix(),
						"savedMatrix.adm");
			}
		});

		saveAsItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				FileFilter type1 = new ExtensionFileFilter(
						"Adjacency Matrix (.adm)", ".adm");
				FileFilter type2 = new ExtensionFileFilter(
						"Comma-separated Value (.csv)", ".csv");
				FileFilter type3 = new ExtensionFileFilter(
						"Adjacency List (.adl)", ".adl");
				c.addChoosableFileFilter(type1);
				c.addChoosableFileFilter(type3);
				c.addChoosableFileFilter(type2);
				c.setFileFilter(type1);
				// Demonstrate "Save" dialog:
				int rVal = c.showSaveDialog(AdjacencyMatrixFrame.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					if (c.getFileFilter().equals(type1)) {
						Utils.saveMatrixAsADM(snr.getmSAdjacencyMatrix(), c
								.getSelectedFile().getPath()
								+ ".adm");
					} else if (c.getFileFilter().equals(type2)) {
						Utils.saveMatrixAsCSV(snr.getmSAdjacencyMatrix(), c
								.getSelectedFile().getPath()
								+ ".csv");
					} else if (c.getFileFilter().equals(type3)) {
						Utils.saveMatrixAsADL(snr.getmSAdjacencyMatrix(), c
								.getSelectedFile().getPath()
								+ ".adl");
					} else {
						Utils.saveMatrixAsADM(snr.getmSAdjacencyMatrix(), c
								.getSelectedFile().getPath());
					}
				}
			}
		});

		popupMenu.add(saveItem);
		popupMenu.add(saveAsItem);

		data.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON3)
					popupMenu.show(event.getComponent(), event.getX(), event
							.getY());
			}
		});

	}

	public static String wrapJLabelText(String text, JLabel l) {
		Font xx = l.getFont();
		FontMetrics fm = l.getFontMetrics(xx);
		StringBuilder sb = new StringBuilder();
		int beginIndex, endIndex, curIndex;
		int width = cellSize - 8;

		curIndex = 0;

		sb.append("<html>");

		while (curIndex < text.length()) {
			beginIndex = curIndex;
			endIndex = text.length();

			int lastLen = 0;
			// do binary search
			while (fm.stringWidth(text.substring(curIndex, endIndex)) > width
					&& beginIndex < endIndex) {
				int mid = (endIndex + beginIndex) / 2;
				int curLen = fm.stringWidth(text.substring(curIndex, mid));
				if (curLen < width) {
					if (curLen != lastLen) {
						beginIndex = mid;
					} else {
						beginIndex = endIndex;
					}
				} else if (curLen > width) {
					endIndex = mid;
				} else {
					beginIndex = endIndex = mid;
				}
				lastLen = curLen;
			}

			sb.append(text.substring(curIndex, endIndex));
			curIndex = endIndex;

			if (curIndex < text.length())
				sb.append("<br />");
		}

		sb.append("</html>");

		return sb.toString();
	}

	private int selectedRowIdx = -1;
	private int selectedColIdx = -1;

	public class HeaderCellRenderer extends JLabel implements TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HeaderCellRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (table == rowHeader) {
				if (row == selectedRowIdx) {
					this.setBackground(Color.GRAY);
				} else {
					this.setBackground(Color.LIGHT_GRAY);
				}
			} else {
				if (column == selectedColIdx) {
					this.setBackground(Color.GRAY);
				} else {
					this.setBackground(Color.LIGHT_GRAY);
				}
			}
			this.setForeground(Color.WHITE);
			this.setFont(new Font("Arial", Font.BOLD, 10));
			this.setHorizontalAlignment(SwingConstants.CENTER);
			this.setText(wrapJLabelText(value.toString(), this));
			return this;
		}
	}

	public class DataCellRender extends JLabel implements TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DataCellRender() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if ((row == selectedRowIdx && column == selectedColIdx)) {
				this.setBackground(Color.LIGHT_GRAY);
				this.setForeground(Color.WHITE);
			} else {
				this.setBackground(Color.WHITE);
				this.setForeground(Color.DARK_GRAY);
			}

			if (isSelected || selectedCells.contains(new Point(row, column))
					|| allSelected) {
				this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			} else {
				this.setBorder(null);
			}

			this.setFont(new Font("Arial", Font.PLAIN, 14));
			this.setHorizontalAlignment(SwingConstants.CENTER);
			this.setText(value.toString());
			return this;
		}

	}

	protected void clearSelectedCells() {
		if (selectedCells.size() == 0)
			return;
		AbstractTableModel atm = (AbstractTableModel) data.getModel();
		for (Point p : selectedCells) {
			atm.fireTableCellUpdated(p.x, p.y);
		}
		selectedCells.clear();
	}

}
