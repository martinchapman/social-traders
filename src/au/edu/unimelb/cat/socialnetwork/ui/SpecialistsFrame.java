package au.edu.unimelb.cat.socialnetwork.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import edu.cuny.cat.registry.SocialNetworkRegistry;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Hashtable;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 95 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          00:54:38 +1100 (Sun, 28 Feb 2010) $
 */
public class SpecialistsFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private SocialNetworkRegistry snr;
	private Hashtable<String, SpecialistVisualization> sVs;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SpecialistsFrame frame = new SpecialistsFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SpecialistsFrame() {
		setTitle("Specialists Frame");
		this.snr = SocialNetworkRegistry.getInstance();
		setBounds(100, 100, 786, 400);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		vScrollBarWidth = scrollPane.getVerticalScrollBar().getPreferredSize().width;

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 0));
		panel.setBackground(Color.WHITE);
		panel.setBorder(null);
		scrollPane.setViewportView(panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		contentPane.revalidate();

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SpecialistsFrame f = (SpecialistsFrame) e.getSource();
				f.resizePanel();
			}
		});

		// copying references from snr
		sVs = snr.getsVs();

		for (SpecialistVisualization sv : sVs.values()) {
			JPanel p = sv.getPanel();
			p.setPreferredSize(new Dimension(0, 200));
			addNewPanel(p);
		}
	}

	private JScrollPane scrollPane;
	private int vScrollBarWidth;

	public void addNewPanel(JPanel p) {
		p.setPreferredSize(new Dimension(getWidth() - vScrollBarWidth, p
				.getPreferredSize().height));
		panel.setPreferredSize(new Dimension(p.getPreferredSize().width, panel
				.getPreferredSize().height
				+ p.getPreferredSize().height));
		p.setBorder(null);
		panel.add(p);
	}

	public JPanel getPanel() {
		return panel;
	}

	public int getvScrollBarWidth() {
		return vScrollBarWidth;
	}

	public void resizePanel() {
		int newWidth;

		newWidth = scrollPane.getViewport().getWidth();

		for (Component c : panel.getComponents()) {
			c.setPreferredSize(new Dimension(newWidth,
					c.getPreferredSize().height));
		}
		panel.setPreferredSize(new Dimension(newWidth,
				panel.getPreferredSize().height));
		panel.revalidate();
	}

	public void updatesVs() {
		// update graph
		for (SpecialistVisualization f : sVs.values()) {
			f.updateGraph();
		}
	}
}
