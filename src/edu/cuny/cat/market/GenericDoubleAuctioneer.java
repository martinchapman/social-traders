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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.market;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Buffer;
import org.apache.commons.collections15.BufferUtils;
import org.apache.commons.collections15.buffer.UnboundedFifoBuffer;
import org.apache.log4j.Logger;

import edu.cuny.cat.MarketRegistry;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.IdAssignedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionRejectedEvent;
import edu.cuny.cat.market.accepting.ShoutAcceptingPolicy;
import edu.cuny.cat.market.charging.ChargingPolicy;
import edu.cuny.cat.market.clearing.MarketClearingCondition;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;
import edu.cuny.cat.market.matching.ShoutEngine;
import edu.cuny.cat.market.pricing.PricingPolicy;
import edu.cuny.cat.market.quoting.DoubleSidedQuotingPolicy;
import edu.cuny.cat.market.quoting.QuotingPolicy;
import edu.cuny.cat.market.subscribing.SelfSubscribingPolicy;
import edu.cuny.cat.market.subscribing.SubscribingPolicy;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A generic implementation of {@link Auctioneer} framework.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.shoutengine</tt><br>
 * <font size=-1>class, inheriting {@link ShoutEngine}</font></td>
 * <td valign=top>(the engine matching shouts)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.quoting</tt><br>
 * <font size=-1>class, implementing {@link QuotingPolicy}</font></td>
 * <td valign=top>(the quoting policy)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.charging</tt><br>
 * <font size=-1>class, inheriting {@link ChargingPolicy}</font></td>
 * <td valign=top>(the charging policy)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.clearing</tt><br>
 * <font size=-1>class, inheriting {@link MarketClearingCondition}</font></td>
 * <td valign=top>(the clearing condition)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.accepting</tt><br>
 * <font size=-1>class, inheriting {@link ShoutAcceptingPolicy}</font></td>
 * <td valign=top>(the shout accepting policy)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.pricing</tt><br>
 * <font size=-1>class, inheriting {@link PricingPolicy}</font></td>
 * <td valign=top>(the pricing policy)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>generic_double_auctioneer</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.40 $
 */

public class GenericDoubleAuctioneer implements Serializable, Auctioneer,
		Resetable, Prototypeable, Cloneable, Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_MATCHING = "matching";

	public static final String P_QUOTING = "quoting";

	public static final String P_PRICING = "pricing";

	public static final String P_CLEARING = "clearing";

	public static final String P_ACCEPTING = "accepting";

	public static final String P_CHARGING = "charging";

	public static final String P_SUBSCRIBING = "subscribing";

	public static final String P_DEF_BASE = "generic_double_auctioneer";

	/**
	 * The shout engine for this auction.
	 */
	protected ShoutEngine shoutEngine = null;

	/**
	 * The set of transactions that have been made in the current round.
	 * 
	 */
	protected Map<String, Transaction> executedTransactions = Collections
			.synchronizedMap(new HashMap<String, Transaction>());

	protected Buffer<Transaction> pendingTransactions = BufferUtils
			.synchronizedBuffer(new UnboundedFifoBuffer<Transaction>());

	/**
	 * records all the shouts still standing at this auction, which may be in
	 * shoutEngine or involved in pendingTransactions.
	 */
	protected Map<String, Shout> shouts = new HashMap<String, Shout>();

	/**
	 * The current quote
	 */
	protected MarketQuote currentQuote = null;

	protected String name;

	protected QuotingPolicy quotingPolicy;

	protected PricingPolicy pricingPolicy;

	protected ChargingPolicy chargingPolicy;

	protected MarketClearingCondition clearingCondition;

	protected ShoutAcceptingPolicy acceptingPolicy;

	protected SubscribingPolicy subscribingPolicy;

	protected MarketRegistry registry;

	static Logger logger = Logger.getLogger(GenericDoubleAuctioneer.class);

	public GenericDoubleAuctioneer() {
		init0();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(GenericDoubleAuctioneer.P_DEF_BASE);

		try {
			shoutEngine = parameters.getInstanceForParameterEq(base
					.push(GenericDoubleAuctioneer.P_MATCHING), defBase
					.push(GenericDoubleAuctioneer.P_MATCHING), ShoutEngine.class);
		} catch (final ParamClassLoadException e) {
			// if undefined, use the default engine
			shoutEngine = new FourHeapShoutEngine();
		}
		shoutEngine.setAuctioneer(this);
		if (shoutEngine instanceof Parameterizable) {
			((Parameterizable) shoutEngine).setup(parameters, base
					.push(GenericDoubleAuctioneer.P_MATCHING));
		}
		shoutEngine.initialize();

		try {
			quotingPolicy = parameters.getInstanceForParameterEq(base
					.push(GenericDoubleAuctioneer.P_QUOTING), defBase
					.push(GenericDoubleAuctioneer.P_QUOTING), QuotingPolicy.class);
		} catch (final ParamClassLoadException e) {
			// if undefined, use the double-sided quoting policy
			quotingPolicy = new DoubleSidedQuotingPolicy();
		}
		quotingPolicy.setAuctioneer(this);
		quotingPolicy.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_QUOTING));
		quotingPolicy.initialize();

		pricingPolicy = parameters.getInstanceForParameterEq(base
				.push(GenericDoubleAuctioneer.P_PRICING), defBase
				.push(GenericDoubleAuctioneer.P_PRICING), PricingPolicy.class);
		pricingPolicy.setAuctioneer(this);
		pricingPolicy.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_PRICING));
		pricingPolicy.initialize();

		clearingCondition = parameters.getInstanceForParameterEq(base
				.push(GenericDoubleAuctioneer.P_CLEARING), defBase
				.push(GenericDoubleAuctioneer.P_CLEARING),
				MarketClearingCondition.class);
		clearingCondition.setAuctioneer(this);
		clearingCondition.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_CLEARING));
		clearingCondition.initialize();

		acceptingPolicy = parameters.getInstanceForParameterEq(base
				.push(GenericDoubleAuctioneer.P_ACCEPTING), defBase
				.push(GenericDoubleAuctioneer.P_ACCEPTING), ShoutAcceptingPolicy.class);
		acceptingPolicy.setAuctioneer(this);
		acceptingPolicy.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_ACCEPTING));
		acceptingPolicy.initialize();

		chargingPolicy = parameters.getInstanceForParameterEq(base
				.push(GenericDoubleAuctioneer.P_CHARGING), defBase
				.push(GenericDoubleAuctioneer.P_CHARGING), ChargingPolicy.class);
		chargingPolicy.setAuctioneer(this);
		chargingPolicy.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_CHARGING));
		chargingPolicy.initialize();

		try {
			subscribingPolicy = parameters
					.getInstanceForParameterEq(base
							.push(GenericDoubleAuctioneer.P_SUBSCRIBING), defBase
							.push(GenericDoubleAuctioneer.P_SUBSCRIBING),
							SubscribingPolicy.class);
		} catch (final ParamClassLoadException e) {
			// if undefined, use the self subscribing policy
			subscribingPolicy = new SelfSubscribingPolicy();
		}
		subscribingPolicy.setAuctioneer(this);
		subscribingPolicy.setup(parameters, base
				.push(GenericDoubleAuctioneer.P_SUBSCRIBING));
		subscribingPolicy.initialize();
	}

	private void init0() {
		currentQuote = null;
	}

	public void reset() {
		init0();

		if (acceptingPolicy instanceof Resetable) {
			((Resetable) acceptingPolicy).reset();
		}

		if (chargingPolicy instanceof Resetable) {
			((Resetable) chargingPolicy).reset();
		}

		if (clearingCondition instanceof Resetable) {
			((Resetable) clearingCondition).reset();
		}

		if (pricingPolicy instanceof Resetable) {
			((Resetable) pricingPolicy).reset();
		}

		if (quotingPolicy instanceof Resetable) {
			((Resetable) quotingPolicy).reset();
		}

		if (subscribingPolicy instanceof Resetable) {
			((Resetable) subscribingPolicy).reset();
		}
	}

	public Object protoClone() {
		try {
			final GenericDoubleAuctioneer clone = (GenericDoubleAuctioneer) clone();
			clone.shoutEngine = new FourHeapShoutEngine();
			clone.reset();
			return clone;
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public QuotingPolicy getQuotingPolicy() {
		return quotingPolicy;
	}

	public void setQuotingPolicy(final QuotingPolicy quotingPolicy) {
		this.quotingPolicy = quotingPolicy;
	}

	public PricingPolicy getPricingPolicy() {
		return pricingPolicy;
	}

	public void setPricingPolicy(final PricingPolicy pricingPolicy) {
		this.pricingPolicy = pricingPolicy;
	}

	public void setClearingCondition(
			final MarketClearingCondition clearingCondition) {
		this.clearingCondition = clearingCondition;
	}

	public MarketClearingCondition getClearingCondition() {
		return clearingCondition;
	}

	public void setChargingPolicy(final ChargingPolicy chargingPolicy) {
		this.chargingPolicy = chargingPolicy;
	}

	public ChargingPolicy getChargingPolicy() {
		return chargingPolicy;
	}

	public void setAcceptingPolicy(final ShoutAcceptingPolicy acceptingPolicy) {
		this.acceptingPolicy = acceptingPolicy;
	}

	public ShoutAcceptingPolicy getAcceptingPolicy() {
		return acceptingPolicy;
	}

	public SubscribingPolicy getSubscribingPolicy() {
		return subscribingPolicy;
	}

	public void setSubscribingPolicy(SubscribingPolicy subscribingPolicy) {
		this.subscribingPolicy = subscribingPolicy;
	}

	public void setShoutEngine(final ShoutEngine shoutEngine) {
		this.shoutEngine = shoutEngine;
	}

	public ShoutEngine getShoutEngine() {
		return shoutEngine;
	}

	public MarketRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(MarketRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Code for handling a new shout in the auction. Subclasses should override
	 * this method if they wish to provide different handling for different
	 * auction rules.
	 * 
	 * @param shout
	 *          The new shout to be processed
	 * 
	 * @exception IllegalShoutException
	 *              Thrown if the shout is invalid in some way.
	 */
	public void newShout(final Shout shout) throws IllegalShoutException,
			DuplicateShoutException {
		if (!shout.isValid()) {
			GenericDoubleAuctioneer.logger.warn("malformed shout: " + shout);
			throw new IllegalShoutException("Malformed shout");
		} else if (acceptingPolicy == null) {
			GenericDoubleAuctioneer.logger.warn("No accepting policy set up !");
			throw new IllegalShoutException("Null accepting policy");
		} else {
			acceptingPolicy.check(getShout(shout.getId()), shout);
			shoutEngine.newShout(shout);
		}
	}

	/**
	 * Handle a request to retract a shout.
	 */
	public void removeShout(final Shout shout) {
		if (shout.getState() == Shout.PLACED) {
			shoutEngine.removeShout(shout);
			shouts.remove(shout.getId());
		} else {
			GenericDoubleAuctioneer.logger
					.fatal("Trying to remove a shout from GDA that is not in the state of PLACED !");
		}
	}

	/**
	 * retrieves the shout with the specified id, which may be still in
	 * shoutEngine, or in a pending transaction.
	 */
	public Shout getShout(final String shoutId) {
		return shouts.get(shoutId);
	}

	/**
	 * Log the current status of the auction.
	 */
	public void printState() {
		shoutEngine.printState();
	}

	public Iterator<Shout> askIterator() {
		return shoutEngine.askIterator();
	}

	public Iterator<Shout> bidIterator() {
		return shoutEngine.bidIterator();
	}

	/**
	 * TODO: the quote returned may not be the latest quote for the market. Modify
	 * it !
	 */
	public MarketQuote getQuote() {
		updateQuote();
		return currentQuote;
	}

	protected void updateQuote() {
		currentQuote = new MarketQuote(askQuote(), bidQuote());
	}

	public double askQuote() {
		return quotingPolicy.askQuote(shoutEngine);
	}

	public double bidQuote() {
		return quotingPolicy.bidQuote(shoutEngine);
	}

	public void clear() {
		updateQuote();

		final List<Shout> shouts = shoutEngine.getMatchedShouts();
		final Iterator<Shout> i = shouts.iterator();
		while (i.hasNext()) {
			final Shout bid = i.next();
			final Shout ask = i.next();
			final double price = determineClearingPrice(bid, ask, currentQuote);
			clear(ask, bid, price);
		}
	}

	protected void clear(final Shout ask, final Shout bid, final double price) {
		final Transaction transaction = new Transaction(ask, bid, price);
		pendingTransactions.add(transaction);

		if ((ask.getState() != Shout.PLACED) || (bid.getState() != Shout.PLACED)) {
			GenericDoubleAuctioneer.logger
					.fatal(
							"GDA attempts to trade between two shouts that are not both in state PLACED !",
							new Exception());
		}

		ask.setState(Shout.PENDING);
		bid.setState(Shout.PENDING);

		final Event event = new Event(this, transaction);
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).dispatchEvent(this,
				event);
	}

	public double determineClearingPrice(final Shout bid, final Shout ask,
			final MarketQuote clearingQuote) {
		return pricingPolicy.determineClearingPrice(bid, ask, clearingQuote);
	}

	public void eventOccurred(final AuctionEvent event) {

		if (event instanceof IdAssignedEvent) {
			setName(((IdAssignedEvent) event).getId());
		} else if (event instanceof GameOverEvent) {
			processGameOver((GameOverEvent) event);
		} else if (event instanceof DayClosedEvent) {
			processDayClosed((DayClosedEvent) event);
		} else if (event instanceof ShoutPlacedEvent) {
			processShoutPlaced((ShoutPlacedEvent) event);
		} else if (event instanceof TransactionExecutedEvent) {
			processTransactionExecuted((TransactionExecutedEvent) event);
		} else if (event instanceof TransactionRejectedEvent) {
			processTransactionRejected((TransactionRejectedEvent) event);
		}

		if (clearingCondition != null) {
			clearingCondition.eventOccurred(event);
		}

		if (acceptingPolicy != null) {
			acceptingPolicy.eventOccurred(event);
		}

		if (pricingPolicy != null) {
			pricingPolicy.eventOccurred(event);
		}

		if (chargingPolicy != null) {
			chargingPolicy.eventOccurred(event);
		}

		if (quotingPolicy != null) {
			quotingPolicy.eventOccurred(event);
		}

		if (subscribingPolicy != null) {
			subscribingPolicy.eventOccurred(event);
		}
	}

	protected void processGameOver(final GameOverEvent event) {
		reset();
	}

	protected void processDayClosed(final DayClosedEvent event) {
		shoutEngine.reset();
		executedTransactions.clear();
		pendingTransactions.clear();
		shouts.clear();
	}

	/**
	 * caches all the successfully placed shout in shouts.
	 * 
	 * @param event
	 */
	protected void processShoutPlaced(final ShoutPlacedEvent event) {
		final Shout shout = event.getShout();

		// TODO: now ShoutPlacedEvent delivers only local shouts
		if ((shout.getSpecialist() == null)
				|| !name.equals(shout.getSpecialist().getId())) {
			// if not shouts placed at this specialist, simple disregard
			GenericDoubleAuctioneer.logger
					.fatal("Info about shout placed somewhere else received at " + name
							+ " and disregarded !");
			return;
		}

		if (shout.getState() != Shout.PLACED) {
			final Exception e = new Exception(
					"shout in ShoutPlacedEvent expected to be at the state of PLACED !");
			e.printStackTrace();
			GenericDoubleAuctioneer.logger.fatal("Shout at wrong state: " + shout);
			return;
		}

		// TODO: checks if possibly updated info in new shout is lost in the
		// following process

		if (shouts.containsKey(shout.getId())) {
			// the placed shout has been known, then update info if needed
			final Shout oldShout = shouts.get(shout.getId());
			if (oldShout.getTrader() == null) {
				oldShout.setTrader(shout.getTrader());
			}

			if (oldShout.getSpecialist() == null) {
				oldShout.setSpecialist(shout.getSpecialist());
			}

			if (oldShout.getPrice() != shout.getPrice()) {
				if (oldShout.getState() == Shout.PLACED) {
					oldShout.setPrice(shout.getPrice());
				} else {
					GenericDoubleAuctioneer.logger
							.error("Only prices of placed shouts can be modified !");
				}
			}
		} else {
			shouts.put(shout.getId(), shout);
		}
	}

	protected void processTransactionExecuted(final TransactionExecutedEvent event) {
		final Transaction transaction = event.getTransaction();
		final Specialist specialist = transaction.getSpecialist();

		// TODO: TransactionExecutedEvent now only delivers transactions made by me
		if (specialist == null) {
			GenericDoubleAuctioneer.logger.fatal(getName()
					+ " received transaction made by an unknown specialist !");
			return;
		} else if (!specialist.getId().equals(getName())) {
			GenericDoubleAuctioneer.logger.fatal(getName()
					+ " received transaction executed by another specialist, "
					+ specialist.getId() + " !");
			return;
		}

		if (executedTransactions.containsKey(transaction.getId())) {
			GenericDoubleAuctioneer.logger.error(getName()
					+ " received duplicate transaction executed events !");
			return;
		}

		if (pendingTransactions.isEmpty()) {
			GenericDoubleAuctioneer.logger
					.error("Pending transaction queue expected not to be empty since transaction execution received !");
			return;
		} else {
			final Transaction pendingTrans = pendingTransactions.get();

			// pending ask and bid should be the very objects in the event
			if ((pendingTrans.getAsk() == transaction.getAsk())
					&& (pendingTrans.getBid() == transaction.getBid())) {
				pendingTransactions.remove();
				executedTransactions.put(transaction.getId(), transaction);

				if ((event.getTransaction().getAsk().getState() != Shout.PENDING)
						|| (event.getTransaction().getBid().getState() != Shout.PENDING)) {
					GenericDoubleAuctioneer.logger
							.fatal(
									"GDA attempts to trade between two shouts that are not both in state PENDING !",
									new Exception());
				}

				event.getTransaction().getAsk().setState(Shout.MATCHED);
				event.getTransaction().getBid().setState(Shout.MATCHED);

				// TODO:
				// shouts.remove(event.getTransaction().getAsk().getId());
				// shouts.remove(event.getTransaction().getBid().getId());

			} else {
				GenericDoubleAuctioneer.logger
						.fatal("Unmatched pending transaction and successful transaction !");
			}
		}
	}

	protected void processTransactionRejected(final TransactionRejectedEvent event) {
		final Transaction transaction = event.getTransaction();
		final Specialist specialist = transaction.getSpecialist();

		// only process transactions matched by this specialist
		if (specialist == null) {
			GenericDoubleAuctioneer.logger.error(getName()
					+ " received rejected transaction attempted by unknown specialist !");
			return;
		} else if (!specialist.getId().equals(getName())) {
			GenericDoubleAuctioneer.logger.error(getName()
					+ " notified of rejected transaction attempted by "
					+ specialist.getId() + " !");
			return;
		}

		if (pendingTransactions.isEmpty()) {
			GenericDoubleAuctioneer.logger
					.error("Pending transaction queue expected not to be empty since transaction rejection received!");
			return;
		} else {
			final Transaction pendingTrans = pendingTransactions.get();

			// pending ask and bid should be the very objects in the event
			if ((pendingTrans.getAsk() == transaction.getAsk())
					&& (pendingTrans.getBid() == transaction.getBid())) {
				pendingTransactions.remove();

				try {

					if ((transaction.getAsk().getState() != Shout.PENDING)
							|| (transaction.getBid().getState() != Shout.PENDING)) {
						GenericDoubleAuctioneer.logger
								.fatal(
										"GDA attempts to cancel trade between two shouts that are not both in state PENDING !",
										new Exception());
					}

					// put back the ask and the bid
					transaction.getAsk().setState(Shout.PLACED);
					shoutEngine.newShout(transaction.getAsk());
					transaction.getBid().setState(Shout.PLACED);
					shoutEngine.newShout(transaction.getBid());
				} catch (final DuplicateShoutException e) {
					GenericDoubleAuctioneer.logger.debug(e);
					// e.printStackTrace();
				}
			} else {
				GenericDoubleAuctioneer.logger
						.fatal("Unmatched pending transaction and rejected transaction !");
			}
		}
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		s += "\n" + Utils.indent(shoutEngine.toString());

		if (quotingPolicy != null) {
			s += "\n" + Utils.indent(quotingPolicy.toString());
		}

		if (pricingPolicy != null) {
			s += "\n" + Utils.indent(pricingPolicy.toString());
		}

		if (clearingCondition != null) {
			s += "\n" + Utils.indent(clearingCondition.toString());
		}

		if (acceptingPolicy != null) {
			s += "\n" + Utils.indent(acceptingPolicy.toString());
		}

		if (chargingPolicy != null) {
			s += "\n" + Utils.indent(chargingPolicy.toString());
		}

		if (subscribingPolicy != null) {
			s += "\n" + Utils.indent(subscribingPolicy.toString());
		}

		return s;
	}
}
