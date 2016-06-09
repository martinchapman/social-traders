package au.edu.unimelb.cat.socialnetwork.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.*;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.ui.Layer;

import au.edu.unimelb.cat.socialnetwork.helper.Palette;

import edu.cuny.cat.registry.SocialNetworkRegistry;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 117 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          00:57:53 +1100 (Sun, 28 Feb 2010) $
 */
public class SpecialistVisualization {

	private static final long serialVersionUID = 1L;
	private CategoryTableXYDataset localCategoryTableXYDataset = new CategoryTableXYDataset();
	private XYPlot localXYPlot;
	private TreeMap<String, HashMap<String, ArrayList<String>>> traders = new TreeMap<String, HashMap<String, ArrayList<String>>>();
	private SocialNetworkRegistry registry;
	private String specialistId;
	private JPanel panel;
	private LinkedBlockingQueue<MSBufferElem> mSBuffer;

	public class MSBufferElem {
		public int day;
		public int size;
		public String tag;

		public MSBufferElem(int day, int size, String tag) {
			this.day = day;
			this.size = size;
			this.tag = tag;
		}
	}

	public void AddTraderType(String type) {
		if (!traders.containsKey(type)) {
			traders.put(type, new HashMap<String, ArrayList<String>>());
			traders.get(type).put("seller", new ArrayList<String>());
			traders.get(type).put("buyer", new ArrayList<String>());
		}
	}

	public static Matcher parseTraderId(String id) {
		Pattern p = Pattern.compile("^(buyer|seller)([^_]+)(_\\d+)?$");
		Matcher m = p.matcher(id);
		return m;
	}

	public static String getTraderTypeFromId(String id) {
		Matcher m = parseTraderId(id);
		if (m.matches()) {
			return m.group(2);
		} else {
			return null;
		}
	}

	public void AddTrader(String id) {
		Matcher m = parseTraderId(id);
		if (m.matches()) {
			traders.get(m.group(2)).get(m.group(1)).add(id);
		}
	}

	public void ClearTraderType(String type) {
		traders.get(type).get("buyer").clear();
		traders.get(type).get("seller").clear();
	}

	public void ClearTraders() {
		for (String s : traders.keySet()) {
			ClearTraderType(s);
		}
	}

	/**
	 * Create the frame.
	 */
	public SpecialistVisualization(String specialistId) {
		this.mSBuffer = new LinkedBlockingQueue<MSBufferElem>();
		this.registry = SocialNetworkRegistry.getInstance();
		this.specialistId = specialistId;
		// setBounds(0, 0, 786, 267);
	}

	private int lastColorPos = 5;

	public JPanel getPanel() {
		if (panel == null) {
			panel = new ChartPanel(createStackedXYAreaChart());
			// set series colors so that the same type of traders will have the
			// same color and those buyers and sellers of that type will be
			// distinguished by brighter and darker version of its type color
			StackedXYAreaRenderer2 renderer = (StackedXYAreaRenderer2) localXYPlot
					.getRenderer(0);
			for (int i = 0; i < traders.size(); i++) {
				renderer.setSeriesPaint(i * 2, Palette.palette[lastColorPos]);
				renderer
						.setSeriesPaint(
								i * 2 + 1,
								Palette.palette[lastColorPos].brighter()
										.equals(Palette.palette[lastColorPos]) ? Palette.palette[lastColorPos]
										.darker()
										: Palette.palette[lastColorPos]
												.brighter());
				lastColorPos = (1 + lastColorPos) % Palette.palette.length;
			}
		}
		return panel;
	}

	/**
	 * Buffer market selection history
	 * 
	 * @param day
	 */
	public void bufferMS(int day) {
		for (Map.Entry<String, HashMap<String, ArrayList<String>>> e : traders
				.entrySet()) {
			try {
				mSBuffer.put(new MSBufferElem(day, e.getValue().get("buyer")
						.size(), e.getKey() + " buyer"));
				mSBuffer.put(new MSBufferElem(day, e.getValue().get("seller")
						.size(), e.getKey() + " seller"));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private int curDay;

	public void updateGraph() {
		while (!mSBuffer.isEmpty()) {
			MSBufferElem be = mSBuffer.poll();
			localCategoryTableXYDataset.add(be.day, be.size, be.tag);
			curDay = be.day;
		}
		localXYPlot.clearDomainMarkers();
		localXYPlot.addDomainMarker(new ValueMarker(curDay), Layer.FOREGROUND);
	}

	private JFreeChart createStackedXYAreaChart() {
		JFreeChart localJFreeChart = ChartFactory.createStackedXYAreaChart(
				null, "Day", "Number of Agents", localCategoryTableXYDataset,
				PlotOrientation.VERTICAL, true, true, false);
		localJFreeChart.getLegend().setFrame(BlockBorder.NONE);
		localJFreeChart.setTitle(specialistId);
		localXYPlot = (XYPlot) localJFreeChart.getPlot();
		StackedXYAreaRenderer2 localStackedXYAreaRenderer2 = new StackedXYAreaRenderer2();
		localStackedXYAreaRenderer2.setRoundXCoordinates(true);
		localStackedXYAreaRenderer2
				.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		localXYPlot.setRenderer(0, localStackedXYAreaRenderer2);
		localXYPlot.setBackgroundPaint(null);
		localXYPlot.setDomainGridlinePaint(Color.GRAY);
		localXYPlot.setRangeGridlinePaint(Color.GRAY);
		ValueAxis localValueAxis = localXYPlot.getDomainAxis();
		localValueAxis.setAutoRange(true);
		localValueAxis.setFixedAutoRange(registry.getClock().getGameLen() - 1);
		localValueAxis.setAutoTickUnitSelection(false);
		localValueAxis.setLabelFont(new Font("Arial", Font.BOLD, 10));
		localValueAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
		localValueAxis = localXYPlot.getRangeAxis();
		localValueAxis.setLabelFont(new Font("Arial", Font.BOLD, 10));
		localValueAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10));
		return localJFreeChart;
	}

	public TreeMap<String, HashMap<String, ArrayList<String>>> getTraders() {
		return traders;
	}

	public String getSpecialistId() {
		return specialistId;
	}
}
