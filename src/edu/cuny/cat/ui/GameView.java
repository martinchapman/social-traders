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

import javax.swing.JInternalFrame;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
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
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.ui.UiUtils;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A framework for an internal frame inside the cat game console.
 * 
 * @see GuiConsole
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.21 $
 */
public abstract class GameView extends JInternalFrame implements
		Parameterizable, AuctionEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(GameView.class);

	protected GuiConsole console;

	public GameView(final String title) {
		super(title, true, true, true, true);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		UiUtils.setup(this, parameters, base);
	}

	public void setConsole(final GuiConsole console) {
		this.console = console;
	}

	public GuiConsole getConsole() {
		return console;
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof TraderCheckInEvent) {
			processTraderCheckIn((TraderCheckInEvent) event);
		} else if (event instanceof SpecialistCheckInEvent) {
			processSpecialistCheckIn((SpecialistCheckInEvent) event);
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
		} else if (event instanceof RoundOpenedEvent) {
			processRoundOpened((RoundOpenedEvent) event);
		} else if (event instanceof RoundClosingEvent) {
			processRoundClosing((RoundClosingEvent) event);
		} else if (event instanceof RoundClosedEvent) {
			processRoundClosed((RoundClosedEvent) event);
		} else if (event instanceof ShoutReceivedEvent) {
			processShoutReceived((ShoutReceivedEvent) event);
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
		} else {
			GameView.logger.error("has yet to be implemented in GameView : "
					+ event.getClass().getSimpleName());
		}

	}

	protected void processDayStatPass(final DayStatPassEvent event) {
	}

	protected void processFundTransfer(final FundTransferEvent event) {
	}

	protected void processRoundClosing(final RoundClosingEvent event) {
	}

	protected void processRoundClosed(final RoundClosedEvent event) {
	}

	protected void processRoundOpened(final RoundOpenedEvent event) {
	}

	protected void processDayClosed(final DayClosedEvent event) {
	}

	protected void processDayOpening(final DayOpeningEvent event) {
	}

	protected void processDayOpened(final DayOpenedEvent event) {
	}

	protected void processGameOver(final GameOverEvent event) {
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
}
