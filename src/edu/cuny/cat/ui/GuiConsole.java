/*
 * JCAT - TAC Market Design Competition Platform
 * Copyright (C) 2006-2010 Jinzhong Niu, Kai Cai
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package edu.cuny.cat.ui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayInitPassEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.event.FundTransferEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.RegistrationEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutReceivedEvent;
import edu.cuny.cat.event.ShoutRejectedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.event.SubscriptionEvent;
import edu.cuny.cat.event.TraderCheckInEvent;
import edu.cuny.cat.event.TraderProfitEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.server.Console;
import edu.cuny.cat.server.EventBasedClockController;
import edu.cuny.cat.server.GameClock;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;
import edu.cuny.ui.AboutDialog;
import edu.cuny.ui.UiUtils;
import edu.cuny.ui.UserCheckboxMenuItem;
import edu.cuny.ui.UserMenuBar;
import edu.cuny.ui.UserMenuItem;
import edu.cuny.util.BrowserLauncher;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * The graphical console for monitoring cat games.
 * </p>
 * 
 * <p>
 * The console may include multiple views, each appearing as an internal frame
 * inside the console. One of the views that is always enabled is
 * {@link OverView}, which provides the main display of game console, and entry
 * to other views.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.homeurl</tt><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the url of the cat project)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.overview</tt><br>
 * <font size=-1></font></td>
 * <td valign=top>(the parameter base for the {@link OverView} instance)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */
public class GuiConsole extends JFrame implements Console, EventListener,
		ActionListener, ItemListener, InternalFrameListener, Parameterizable,
		AuctionEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(GuiConsole.class);

	public static final String P_HOMEURL = "homeurl";

	public static final String P_ABOUTDIALOG = "aboutdialog";

	public static final String P_OVERVIEW = "overview";

	public static final String P_BUTTON = "button";

	public static final String P_START = "start";

	public static final String P_PAUSE = "pause";

	public static final String P_RESUME = "resume";

	public static final String P_EXIT = "exit";

	private UserMenuBar menuBar = null;

	private JDesktopPane desktop = null;

	private AboutDialog dialog = null;

	private String homeURL = null;

	private JPanel buttonP = null;

	private JButton startB = null;

	private JButton pauseB = null;

	private JButton resumeB = null;

	private JButton exitB = null;

	private final SortedMap<String, GameView> views;

	ViewPositionTracker posTracker;

	protected EventEngine eventEngine;

	private static GuiConsole instance;

	public static GuiConsole getInstance() {
		return GuiConsole.instance;
	}

	public GuiConsole() {

		GuiConsole.instance = this;

		eventEngine = Galaxy.getInstance().getDefaultTyped(EventEngine.class);

		// JApplet applet = Preferences.getInstance().getApplet();
		// if (applet != null) {
		// applet.getContentPane().removeAll();
		// applet.getContentPane().add(this, BorderLayout.CENTER);
		// }
		//		
		desktop = new JDesktopPane();
		getContentPane().add(desktop, BorderLayout.CENTER);

		views = new TreeMap<String, GameView>();

		startB = new JButton();
		startB.addActionListener(this);

		pauseB = new JButton();
		pauseB.addActionListener(this);
		pauseB.setEnabled(false);

		resumeB = new JButton();
		resumeB.addActionListener(this);
		resumeB.setEnabled(false);

		exitB = new JButton();
		exitB.addActionListener(this);
		exitB.setEnabled(false);

		buttonP = new JPanel();
		buttonP.add(startB);
		buttonP.add(pauseB);
		buttonP.add(resumeB);
		buttonP.add(exitB);

		getContentPane().add(buttonP, BorderLayout.NORTH);

		menuBar = new UserMenuBar();
		setJMenuBar(menuBar);

		dialog = new AboutDialog(this);

		posTracker = new ViewPositionTracker();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exit(e.getSource());
			}
		});
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		menuBar.setup(parameters, base);
		dialog.setup(parameters, base.push(GuiConsole.P_ABOUTDIALOG));

		homeURL = parameters.getStringWithDefault(base.push(GuiConsole.P_HOMEURL),
				null, "http://www.cs.gc.cuny.edu/~jniu/");

		UiUtils.setup(startB, parameters, base.push(GuiConsole.P_BUTTON).push(
				GuiConsole.P_START));
		UiUtils.setup(pauseB, parameters, base.push(GuiConsole.P_BUTTON).push(
				GuiConsole.P_PAUSE));
		UiUtils.setup(resumeB, parameters, base.push(GuiConsole.P_BUTTON).push(
				GuiConsole.P_RESUME));
		UiUtils.setup(exitB, parameters, base.push(GuiConsole.P_BUTTON).push(
				GuiConsole.P_EXIT));

		registerListenerToMenus();

		initOverView(parameters, base);

		// pack();
	}

	public void start() {
		setVisible(true);
	}

	public void stop() {
		setVisible(false);
		dispose();
	}

	public boolean isInteractive() {
		return true;
	}

	private void initOverView(final ParameterDatabase parameters,
			final Parameter base) {
		final OverView view = new OverView();
		addView(OverView.class.getName(), view);
		view.setup(parameters, base.push(GuiConsole.P_OVERVIEW));
	}

	public void addView(final String key, final GameView view) {
		view.setConsole(this);
		views.put(key, view);
		view.addInternalFrameListener(this);
		desktop.add(view, JLayeredPane.DEFAULT_LAYER);
		view.setLocation(posTracker.getPosition());
		posTracker.nextPosition();
		view.setVisible(true);
	}

	public void removeView(final Object key) {
		final GameView view = getView(key);
		if (view != null) {
			views.remove(key);
			view.setVisible(false);
			view.removeInternalFrameListener(this);
			desktop.remove(view);
			view.dispose();
		} else {
			// TODO
		}
	}

	public GameView getView(final Object key) {
		return views.get(key);
	}

	public void eventOccurred(final AuctionEvent event) {
		for (final GameView view : views.values()) {
			view.eventOccurred(event);
		}

		if (event instanceof TraderCheckInEvent) {
			processTraderCheckIn((TraderCheckInEvent) event);
		} else if (event instanceof SpecialistCheckInEvent) {
			processSpecialistCheckIn((SpecialistCheckInEvent) event);
		} else if (event instanceof ShoutReceivedEvent) {
			processShoutReceived((ShoutReceivedEvent) event);
		} else if (event instanceof ShoutRejectedEvent) {
			processShoutRejected((ShoutRejectedEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof FeesAnnouncedEvent) {
			processFeesAnnounced((FeesAnnouncedEvent) event);
		} else if (event instanceof SubscriptionEvent) {
			processSubscription((SubscriptionEvent) event);
		} else if (event instanceof PrivateValueAssignedEvent) {
			processPrivateValueAssigned((PrivateValueAssignedEvent) event);
		} else if (event instanceof RegistrationEvent) {
			processRegistration((RegistrationEvent) event);
		} else if (event instanceof GameStartingEvent) {
			processGameStarting((GameStartingEvent) event);
		} else if (event instanceof GameStartedEvent) {
			processGameStarted((GameStartedEvent) event);
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayOpeningEvent) {
			processDayOpening((DayOpeningEvent) event);
		} else if (event instanceof DayOpenedEvent) {
			processDayOpened((DayOpenedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof TraderProfitEvent) {
			processTraderProfit((TraderProfitEvent) event);
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof SimulationStartedEvent) {
			processSimulationStarted((SimulationStartedEvent) event);
		} else if (event instanceof SimulationOverEvent) {
			processSimulationOver((SimulationOverEvent) event);
		} else if (event instanceof ClientStateUpdatedEvent) {
			processClientStatusUpdated((ClientStateUpdatedEvent) event);
		} else if (event instanceof FundTransferEvent) {
			processFundTransfer((FundTransferEvent) event);
		} else if (event instanceof DayStatPassEvent) {
			processDayStatPass((DayStatPassEvent) event);
		} else if (event instanceof DayInitPassEvent) {
			processDayInitPass((DayInitPassEvent) event);
		} else {
			GuiConsole.logger.error("has yet to be implemented in GuiConsole : "
					+ event.getClass().getSimpleName());
		}

	}

	protected void processDayStatPass(final DayStatPassEvent event) {
	}

	protected void processDayInitPass(final DayInitPassEvent event) {
	}

	protected void processFundTransfer(final FundTransferEvent event) {
	}

	protected void processRoundClosing(final RoundClosingEvent event) {
	}

	protected void processRoundClosed(final RoundClosedEvent event) {
	}

	protected void processTraderProfit(final TraderProfitEvent event) {
	}

	protected void processDayOpening(final DayOpeningEvent event) {
	}

	protected void processRoundOpened(final RoundOpenedEvent event) {
	}

	protected void processDayClosed(final DayClosedEvent event) {
	}

	protected void processDayOpened(final DayOpenedEvent event) {
	}

	protected void processGameOver(final GameOverEvent event) {
		exitB.setEnabled(true);
	}

	protected void processGameStarting(final GameStartingEvent event) {
	}

	protected void processGameStarted(final GameStartedEvent event) {
	}

	protected void processRegistration(final RegistrationEvent event) {
	}

	protected void processPrivateValueAssigned(
			final PrivateValueAssignedEvent event) {
	}

	protected void processSubscription(final SubscriptionEvent event) {
	}

	protected void processFeesAnnounced(final FeesAnnouncedEvent event) {
	}

	protected void processShoutRejected(final ShoutRejectedEvent event) {
	}

	protected void processTransactionExecuted(final TransactionExecutedEvent event) {
	}

	protected void processShoutPlaced(final ShoutPlacedEvent event) {
	}

	protected void processShoutReceived(final ShoutReceivedEvent event) {
	}

	protected void processSpecialistCheckIn(final SpecialistCheckInEvent event) {
	}

	protected void processTraderCheckIn(final TraderCheckInEvent event) {
	}

	protected void processSimulationStarted(final SimulationStartedEvent event) {
	}

	protected void processSimulationOver(final SimulationOverEvent event) {
	}

	protected void processClientStatusUpdated(final ClientStateUpdatedEvent event) {
	}

	private void registerListenerToMenus() {
		eventEngine.checkIn(UserCheckboxMenuItem.class, this);
		eventEngine.checkIn(UserMenuItem.class, this);
	}

	private void unregisterListenerToMenus() {
		eventEngine.checkOut(UserCheckboxMenuItem.class, this);
		eventEngine.checkOut(UserMenuItem.class, this);
	}

	public void eventOccurred(final Event te) {
		if (te.getUserObject() instanceof ActionEvent) {
			actionPerformed((ActionEvent) te.getUserObject());
		} else if (te.getUserObject() instanceof ItemEvent) {
			itemStateChanged((ItemEvent) te.getUserObject());
		}
	}

	// ActionListener Interface
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == startB) {
			eventEngine.dispatchEvent(GameClock.class, new Event(startB,
					EventBasedClockController.START));

			startB.setEnabled(false);
			pauseB.setEnabled(true);
			exitB.setEnabled(true);

		} else if (e.getSource() == pauseB) {

			eventEngine.dispatchEvent(GameClock.class, new Event(pauseB,
					EventBasedClockController.PAUSE));
			pauseB.setEnabled(false);
			resumeB.setEnabled(true);

		} else if (e.getSource() == resumeB) {

			eventEngine.dispatchEvent(GameClock.class, new Event(resumeB,
					EventBasedClockController.RESUME));
			resumeB.setEnabled(false);
			pauseB.setEnabled(true);

		} else if (e.getSource() == exitB) {
			exit(exitB);
		}

		final String action = e.getActionCommand();

		if (action.equals("menu.help.home")) {
			try {
				BrowserLauncher.openURL(homeURL);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		} else if (action.equals("menu.help.about")) {

			UiUtils.center(dialog);
			dialog.setVisible(true);

		}

		return;
	}

	public void itemStateChanged(final ItemEvent e) {
		final JCheckBoxMenuItem cbMI = (JCheckBoxMenuItem) e.getItemSelectable();
		GuiConsole.logger.info(cbMI.getName());
	}

	private void exit(final Object source) {
		unregisterListenerToMenus();
		eventEngine.dispatchEvent(GameClock.class, new Event(source,
				EventBasedClockController.QUIT));
	}

	/**
	 * helps avoid game views overlapping over one another when they are created.
	 */
	class ViewPositionTracker {
		public int x;

		public int y;

		public int xInc = 20;

		public int yInc = 20;

		public int xMax = 300;

		public int yMax = 200;

		public ViewPositionTracker() {
			x = 0;
			y = 0;
		}

		public void nextPosition() {
			if (x + xInc + xMax > desktop.getWidth()) {
				x = (x % xInc) + xInc;
			} else {
				x += xInc;
			}

			if (y + yInc + yMax > desktop.getHeight()) {
				y = (y % yInc) + yInc;
			} else {
				y += yInc;
			}
		}

		public Point getPosition() {
			return new Point(x, y);
		}
	}

	public void internalFrameActivated(final InternalFrameEvent e) {
	}

	public void internalFrameClosed(final InternalFrameEvent e) {
	}

	public void internalFrameClosing(final InternalFrameEvent e) {
		final String title = e.getInternalFrame().getTitle();
		removeView(title);
	}

	public void internalFrameDeactivated(final InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(final InternalFrameEvent e) {
	}

	public void internalFrameIconified(final InternalFrameEvent e) {
	}

	public void internalFrameOpened(final InternalFrameEvent e) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
