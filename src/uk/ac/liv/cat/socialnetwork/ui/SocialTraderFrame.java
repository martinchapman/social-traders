package uk.ac.liv.cat.socialnetwork.ui;

import edu.cuny.cat.registry.SocialNetworkRegistry;
import edu.cuny.cat.core.Trader;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import java.util.Hashtable;
import java.io.IOException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;

import uk.ac.liv.cat.socialnetwork.util.LogReader;

/**
 * 
 * @author Martin Chapman
 * @version 1
 */

public class SocialTraderFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private SocialNetworkRegistry snr;

	private LogReader logReader;

	private JPanel linkVisuals; 

	private JTextPane traderLog;

	private JComboBox traderList;

	private JScrollPane traderScroll;

	private boolean validToScroll = false;

	private JLabel profitAmount;

    private JLabel matchesAmount;

	private JLabel currentSpecialist;

	private JLayeredPane mainView;

	private JLabel header;

	private JLabel loading;
	
	private JLabel profileText;

	private JLabel networkText;

	private static final Graph<String, Integer> g = new SparseMultigraph<String, Integer>();

	private BasicVisualizationServer<String, Integer> vv;

	private AbstractLayout<String,Integer> layout = null;

	private String target = "Pending";

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			public void run() {

				try {

					SocialTraderFrame frame = new SocialTraderFrame();

					frame.setVisible(true);

				} catch (Exception e) {

					e.printStackTrace();

				}

			}

		});

	}

	public SocialTraderFrame() {

		/* Set parent JFRAME properties. */

		setTitle("Social Trader Frame");
		setBounds(100, 100, 1300, 661);


		/* Instantiate the necessary instances. */

		this.snr = SocialNetworkRegistry.getInstance();	 
		logReader = new LogReader();


		/* Container for layered Visual Link content. */

		mainView = new JLayeredPane();


		/* Loading image and icon. */
		
		header = new JLabel();

		header.setIcon(new ImageIcon("resources/images/st-logo.png")); 	
		header.setBounds(275, 75, 264, 377);

		loading = new JLabel();
		
        mainView.add(header, new Integer(2));
		mainView.add(loading, new Integer(3));

		loading.setIcon(new ImageIcon("resources/images/ajax-loader.gif"));
		loading.setText("\n Waiting for next day...");
		loading.setBounds(315, 65, 580, 580);


		/* Visual Links Window. */
		
		linkVisuals = new JPanel();
		//setTitledBorder(linkVisuals, "Advice Links");
		linkVisuals.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		linkVisuals.setBackground(Color.WHITE);


		 /* Trader Nodes and Edge Visuals */

		layout = new FRLayout<String, Integer>(g);
		vv = new BasicVisualizationServer<String, Integer>(layout);
		vv.setPreferredSize(new Dimension(771,575));
		vv.setBackground(Color.WHITE);

 		Transformer<String,Icon> vertexIcon = new Transformer<String,Icon>() {
            public Icon transform(String i) {
                Icon icon = 
                    new ImageIcon("resources/images/st-small.png");
				return icon;
            }
        };  

		vv.getRenderContext().setVertexIconTransformer(vertexIcon);
		
		try {
	
	        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
	
		} catch(Exception e) {}
		
        //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S); 
		
        GroupLayout linkVisualsLayout = new GroupLayout(linkVisuals);
		

		/* Link Visualisation Layout */

		linkVisuals.setLayout(linkVisualsLayout);
		
        linkVisualsLayout.setHorizontalGroup(
            linkVisualsLayout.createParallelGroup(GroupLayout.LEADING)
            .add(linkVisualsLayout.createSequentialGroup()
				.add(vv))
                
        );
        linkVisualsLayout.setVerticalGroup(
            linkVisualsLayout.createParallelGroup(GroupLayout.LEADING)
            .add(linkVisualsLayout.createSequentialGroup()
				.add(vv))
               
        );

		linkVisuals.setBounds(0, 0, 801, 600);

		mainView.add(linkVisuals, new Integer(1));
		

		/* Trader Log Screen. */
			
		traderLog = new JTextPane();
		traderLog.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		traderLog.setMargin(new Insets(10,10,10,10));
		
		traderLog.setEditable(false);
		traderLog.setCaretPosition(0);


		/* Scroll Pane for Trader Log. */
	
		traderScroll = new JScrollPane();

		final JScrollBar traderScrollBar = traderScroll.getVerticalScrollBar();


		// ~MDC 31/03/11 - Needs work.
		/* Action Listener implemented to automatically scroll window when
		   bar is moved to the bottom of the window. */
		traderScrollBar.addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent ae) {

				if ((traderScrollBar.getMaximum() - ae.getValue()) == 580) {

					validToScroll = true;

			    }
				else {

					validToScroll = false;

				}
			
			}

		});
		
		traderScroll.setViewportView(traderLog);

			
        /* Dropdown list of traders. */

		traderList = new JComboBox();
		traderList.setModel(new DefaultComboBoxModel( new String[] { "(Pending)" } ));
		

		/* Labels */

        JLabel matches = new JLabel();
		matches.setText("Successful transactions yesterday:");

        JLabel profit = new JLabel();
		profit.setText("Trader's profit yesterday:");

        JLabel specialist = new JLabel();
		specialist.setText("Currently trading within:");

		profitAmount = new JLabel();
		profitAmount.setFont(new java.awt.Font("Lucida Grande", 1, 13)); 
        profitAmount.setText("(Pending)");

        matchesAmount = new JLabel();
		matchesAmount.setFont(new java.awt.Font("Lucida Grande", 1, 13)); 
        matchesAmount.setText("(Pending)");

		JLabel tracking = new JLabel();
		tracking.setText("Currently tracking:");

		currentSpecialist = new JLabel();  
		currentSpecialist.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        currentSpecialist.setText("(Pending)");

		JLabel profile = new JLabel();
		profile.setText("Trader profile:");

		profileText = new JLabel();
		profileText.setText("(Pending)");
		profileText.setFont(new java.awt.Font("Lucida Grande", 1, 13)); 

		JLabel network = new JLabel();
		network.setText("Social Network in use:");

		networkText = new JLabel();
		networkText.setText("(Pending)");
		networkText.setFont(new java.awt.Font("Lucida Grande", 1, 13)); 


		/* Layout Initialisation. */
	
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        getContentPane().setLayout(groupLayout);
		
		/* Horizontal Layout (modified from NetBeans IDE). */

 		groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(GroupLayout.LEADING)
            .add(groupLayout.createSequentialGroup()
				.add(10, 10, 10)
				.add(groupLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(groupLayout.createSequentialGroup()
                        .add(mainView, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5))
                    .add(groupLayout.createSequentialGroup()
                        .add(network, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(networkText, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
                        .add(560, 560, 560)))
                .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(traderScroll, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .add(groupLayout.createSequentialGroup()
                        .add(tracking)
                        .add(18, 18, 18)
                        .add(traderList, 0, 163, Short.MAX_VALUE))
                    .add(GroupLayout.TRAILING, groupLayout.createSequentialGroup()
                        .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(matches, GroupLayout.PREFERRED_SIZE, 314, GroupLayout.PREFERRED_SIZE)
                            .add(profit, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
							.add(specialist, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
							.add(profile, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.RELATED, 28, Short.MAX_VALUE)
                        .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, profitAmount)
							.add(GroupLayout.TRAILING, matchesAmount)  
							.add(GroupLayout.TRAILING, currentSpecialist)                           
							.add(GroupLayout.TRAILING, profileText)))
						.add(org.jdesktop.layout.GroupLayout.TRAILING, groupLayout.createSequentialGroup()
		                    .add(profile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
		                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 156, Short.MAX_VALUE)
		                    .add(profileText)))
                .addContainerGap())
        );
		
		/* Vertical Layout (modified from NetBeans IDE). */
		
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(GroupLayout.LEADING)
            .add(groupLayout.createSequentialGroup()
                .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(groupLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(mainView, GroupLayout.PREFERRED_SIZE, 601, GroupLayout.PREFERRED_SIZE))
                    .add(GroupLayout.LEADING, groupLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(traderScroll, GroupLayout.PREFERRED_SIZE, 451, GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
                            .add(traderList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(tracking))
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(groupLayout.createSequentialGroup()
                                .add(profitAmount)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(matchesAmount)
								.addPreferredGap(LayoutStyle.RELATED)
								.add(currentSpecialist)
								.addPreferredGap(LayoutStyle.RELATED)
								.add(profileText))
                            .add(groupLayout.createSequentialGroup()
                                .add(profit)
                                .addPreferredGap(LayoutStyle.RELATED)
								.add(matches)
                                .addPreferredGap(LayoutStyle.RELATED)
								.add(specialist)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(profile)))))
                .add(groupLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(groupLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(networkText, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                        .add(network, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)))
                .add(24, 24, 24)));
		//

		
		logReader.addSearchTerm("All");
		
	}

	public void addTraders(String[] traders) {

		if (traderList.getItemCount() == 1) {

			traderList.setModel(new DefaultComboBoxModel( traders ));

			target = (String)traderList.getSelectedItem();
		
			logReader.addSearchTerm(target);					

			traderList.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					traderLog.setText("");

					profitAmount.setText("(Pending)");

					matchesAmount.setText("(Pending)");
					
					currentSpecialist.setText("(Pending)");

					JComboBox cb = (JComboBox)e.getSource();

					logReader.removeSearchTerm(target);

		    		    target = (String)cb.getSelectedItem();

				    logReader.addSearchTerm(target);					

				}

			});

		}

	}

	public void updateGraph(Graph<String, Integer> g) {		

			layout = new FRLayout<String, Integer>(g);
			
			layout.setSize(new Dimension(771,575));
			
			vv.getModel().setGraphLayout(layout);
			
			vv.repaint();
			
			linkVisuals.repaint();
			
			linkVisuals.revalidate();

			header.setVisible(false);

			loading.setVisible(false);

			mainView.repaint();
			
			mainView.revalidate();

	}

	public void updateLog() {

		try {

			String newLog;

			if ((newLog = logReader.read()) != "") {

				append(newLog);

				if (validToScroll) {

					traderLog.setCaretPosition(traderLog.getDocument().getLength());

				} 
			
			}

		} catch(IOException e) {};
		
	}
	
	public void append(String string) { 
        
		StyledDocument doc = traderLog.getStyledDocument();
		
		SimpleAttributeSet style = new SimpleAttributeSet();
		
		if (string.startsWith("!")) {	
			
			StyleConstants.setBold(style, true);
		
		}
		
		try {
			
			doc.insertString( doc.getLength(), target + ": " + string.substring(1, string.length()) + "\n\n", style );

		} catch (Exception e) {}

    }

	public void updateStats(String specialist, String profile, String network) {

		currentSpecialist.setText(specialist);

		profileText.setText(profile);

		networkText.setText(network);

	}

	public void updateProfit(Hashtable<String, Double> profit) {

		if (target != "Pending") {

			profitAmount.setText(Long.toString(Math.round(profit.get(target))));

		}
		
	}

	public void updateMatches(Hashtable<String, Integer> matches) {

		if (target != "Pending") {

			matchesAmount.setText(matches.get(target).toString());

		}
		
	}

	public void setTitledBorder(final JPanel panel, final String header) {
	
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(20, 5, 5, 20), BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(header), BorderFactory
						.createEmptyBorder(5, 5, 5, 5))));
	
	}

}
