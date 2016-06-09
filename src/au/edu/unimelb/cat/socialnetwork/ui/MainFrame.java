package au.edu.unimelb.cat.socialnetwork.ui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

import edu.cuny.cat.registry.SocialNetworkRegistry;
import uk.ac.liv.cat.socialnetwork.ui.SocialTraderFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 94 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          00:55:47 +1100 (Sun, 28 Feb 2010) $
 */
public class MainFrame {

	private JFrame frmCatSocialNetwork;
	private SpecialistsFrame sFrame;
	private MarketSelectionAMFrame msAMFrame;
	private SocialTraderFrame stFrame;

	/**
	 * Create the application.
	 */
	public MainFrame() {
		initialize();
		frmCatSocialNetwork.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCatSocialNetwork = new JFrame();
		frmCatSocialNetwork.setTitle("CAT Social Network");
		frmCatSocialNetwork.setResizable(false);
		frmCatSocialNetwork.setBounds(50, 50, 137, 136);
		frmCatSocialNetwork.setUndecorated(true);
		//frmCatSocialNetwork.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCatSocialNetwork.getContentPane().setLayout(null);

		/* JButton btnOpenSpecialists = new JButton("View Specialists");
		btnOpenSpecialists.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sFrame == null) {
					sFrame = new SpecialistsFrame();
					SocialNetworkRegistry.getInstance().setsFrame(sFrame);
				}
				sFrame.setVisible(true);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						sFrame.updatesVs();
					}
				});
			}
		});
		btnOpenSpecialists.setBounds(29, 38, 136, 29);
		frmCatSocialNetwork.getContentPane().add(btnOpenSpecialists);

		JButton btnViewMarketSelection = new JButton(
				"View Market Selection Adjacency Matrix");
		btnViewMarketSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (msAMFrame == null) {
					msAMFrame = new MarketSelectionAMFrame();
					SocialNetworkRegistry.getInstance().setMsAMFrame(msAMFrame);
				}
				msAMFrame.setVisible(true);
			}
		});
		btnViewMarketSelection.setBounds(177, 38, 137, 29);
		frmCatSocialNetwork.getContentPane().add(btnViewMarketSelection); */

		JButton btnViewSocialTraders = new JButton(new ImageIcon("resources/images/st-large.png"));

        btnViewSocialTraders.setBorderPainted(false); 

        btnViewSocialTraders.setContentAreaFilled(false); 

        btnViewSocialTraders.setFocusPainted(false); 

        btnViewSocialTraders.setOpaque(false);

		btnViewSocialTraders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (stFrame == null) {
					stFrame = new SocialTraderFrame();
					SocialNetworkRegistry.getInstance().setstFrame(stFrame);
				}
				stFrame.setVisible(true);
			}
		});
		btnViewSocialTraders.setBounds(0, 0, 137, 136);
		frmCatSocialNetwork.getContentPane().add(btnViewSocialTraders);
	}

	public SpecialistsFrame getsFrame() {
		return sFrame;
	}

	public MarketSelectionAMFrame getMsAMFrame() {
		return msAMFrame;
	}

	public SocialTraderFrame getstFrame() {
		return stFrame;
	}
}
