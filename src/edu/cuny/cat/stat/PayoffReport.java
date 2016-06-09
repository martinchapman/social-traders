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

package edu.cuny.cat.stat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.Message;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayInitPassEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.ClientBehaviorController;
import edu.cuny.cat.server.GameController;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.CSVReader;
import edu.cuny.util.io.CSVWriter;

/**
 * <p>
 * This class aims to support an ecological simulation of a set of specialists
 * in an all-round cat game. It uses the past normalized payoffs of specialists
 * to control how frequently (in terms of days) the same set of specialists
 * participate in the game this time.
 * </p>
 * 
 * <p>
 * It reads in the past normalized payoffs from the last line of a .CSV file and
 * uses the payoffs as probability thresholds to control whether a specialist
 * should be banned to reflect restrict on its participation.
 * </p>
 * 
 * <p>
 * At the end of the game, game scores of specialists are normalized and
 * appended to the the .CSV file as the latest payoffs.
 * </p>
 * 
 * <p>
 * In this way, one run of the cat game represents a simulation of one
 * generation.
 * </p>
 * 
 * <p>
 * TODO: note that the scores from {@link ScoreReport} is the cumulative score
 * across games, not for the last game only. So temporarily, force it to reset
 * scores when a new game starts.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class PayoffReport implements GameReport, Parameterizable {

	static Logger logger = Logger.getLogger(PayoffReport.class);

	public static final String P_INPUTFILE = "inputfile";

	public static final String P_OUTPUTFILE = "outputfile";

	public static final char DEFAULT_SEPARATOR = ',';

	Registry registry = null;

	ClientBehaviorController behaviorController = null;

	ScoreReport scoreReport = null;

	/**
	 * stores the specialist IDs in order from/for the payoff file.
	 */
	String specialistIds[] = null;

	/**
	 * the payoffs from the last game, used in this run as participation control
	 */
	double payoffs[] = null;

	/**
	 * uniform distributions, each for participation control on a specialist
	 */
	Uniform uniformDists[] = null;

	protected CSVWriter writer = null;

	/**
	 * the name of the payoff file; if existing, contains previous normalized
	 * payoffs
	 */
	protected String inputFileName = null;

	protected String outputFileName = null;

	public PayoffReport() {
		registry = GameController.getInstance().getRegistry();
		behaviorController = GameController.getInstance().getBehaviorController();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		inputFileName = parameters.getString(base.push(PayoffReport.P_INPUTFILE),
				null);
		if ((inputFileName == null) || (inputFileName.length() == 0)) {
			PayoffReport.logger.error("input file is not configured !");
		}

		outputFileName = parameters.getString(base.push(PayoffReport.P_OUTPUTFILE),
				null);
		if ((outputFileName == null) || (outputFileName.length() == 0)) {
			PayoffReport.logger.error("output file is not configured !");
		}
	}

	/**
	 * TODO: writes the IDs of specialists and an initial even payoffs to the
	 * beginning of the payoff file.
	 */
	protected void generateHeader() {
		specialistIds = registry.getSpecialistIds();
		writer.newData(specialistIds);
		writer.endRecord();
		writer.flush();
	}

	/**
	 * initialize the probabilities of participation based on payoffs of the
	 * previous game.
	 * 
	 * @param payoffList
	 */
	protected void setPayoffs(final List<Double> payoffList) {
		double max = Double.NEGATIVE_INFINITY;

		payoffs = new double[specialistIds.length];

		for (int i = 0; i < payoffs.length; i++) {
			if ((payoffList != null) && (payoffList.get(i) != null)) {
				payoffs[i] = payoffList.get(i).doubleValue();
				if ((payoffs[i] > 1.0) || (payoffs[i] < 0.0)) {
					PayoffReport.logger.error("Normalized payoff should be in [0,1] !");
				}
			} else {
				// by default, all specialists have equal rights to participate.
				payoffs[i] = 1;
			}

			if (payoffs[i] > max) {
				max = payoffs[i];
			}
		}

		// normalize if needed
		if (max < 1.0) {
			for (int i = 0; i < payoffs.length; i++) {
				payoffs[i] /= max;
			}
		}

	}

	/**
	 * reads in the previous payoffs of specialists.
	 * 
	 * @return true if successfully obtains payoffs of previous run; or false
	 *         otherwise.
	 */
	protected boolean initPreviousPayoffs() {

		final File file = new File(inputFileName);

		final int numOfSpecialists = registry.getNumOfSpecialists();

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileInputStream(file),
					PayoffReport.DEFAULT_SEPARATOR);

		} catch (final FileNotFoundException e) {
			PayoffReport.logger.debug("Normalized payoff file doesn't exist !");
			return false;
		}

		String[] record = null;
		try {
			record = reader.nextRecord();
			if ((record == null) || (record.length != numOfSpecialists)) {
				PayoffReport.logger
						.fatal("Dismatching specialist list in payoff file !");
				return false;
			}

			// initialize the specialist list
			specialistIds = record;

			for (final String specialistId : specialistIds) {
				if (registry.getSpecialist(specialistId) == null) {
					PayoffReport.logger
							.fatal("Invalid specialist ID in payoff file header: "
									+ specialistId + " !");
					return false;
				}
			}
		} catch (final IOException e) {
			PayoffReport.logger
					.fatal("Failed to read specialist IDs from payoff file header !");
			return false;
		}

		String[] last = null;
		try {
			record = reader.nextRecord();
			while (record != null) {
				last = record;
				record = reader.nextRecord();
			}
		} catch (final IOException e) {
			PayoffReport.logger.fatal(
					"Failed to read payoff lines from payoff file !", e);
			return false;
		}

		if (last != null) {
			// existing previous payoffs
			if (last.length != numOfSpecialists) {
				PayoffReport.logger
						.fatal("Dismatched specialist payoff record in payoff file !");
				return false;
			} else {
				setPayoffs(Arrays.asList(Utils.convert(last, Double.class)));
			}
		} else {
			PayoffReport.logger.fatal("No valid payoff lines found in payoff file !");
			return false;
		}

		reader.close();
		return true;
	}

	protected void initWriter() {
		writer = new CSVWriter();
		writer.setFileName(outputFileName);
		writer.setSeparator(PayoffReport.DEFAULT_SEPARATOR);
		writer.setAppend(true);
		writer.setAutowrap(false);

		writer.open();
	}

	protected void closeWriter() {
		writer.close();
	}

	protected double getAbsolutePayoff(final String specialistId) {
		final Score score = scoreReport.getScore(specialistId);
		if (score == null) {
			return 0;
		} else {
			return score.total;
		}
	}

	/**
	 * writes normalized payoffs of the ending game to payoff file. When the
	 * payoffs are not available, actually uses 1 for all specialists.
	 */
	protected void writePayoffs() {

		// use specialists' scores as absolute payoffs
		double max = Double.NEGATIVE_INFINITY;
		payoffs = new double[specialistIds.length];
		for (int i = 0; i < payoffs.length; i++) {
			payoffs[i] = getAbsolutePayoff(specialistIds[i]);
			if (payoffs[i] > max) {
				max = payoffs[i];
			}
		}

		// normalize payoffs
		for (int i = 0; i < payoffs.length; i++) {
			if (max <= 0) {
				payoffs[i] = 1;
			} else {
				payoffs[i] /= max;
			}
		}

		// append to the payoff file
		for (final double payoff : payoffs) {
			writer.newData(GameReport.Formatter.format(payoff));
		}

		writer.endRecord();
		writer.flush();
	}

	protected void initScoreReport() {
		scoreReport = GameController.getInstance().getReport(ScoreReport.class);
	}

	protected void initRandomGenerators() {
		uniformDists = new Uniform[specialistIds.length];

		for (int i = 0; i < uniformDists.length; i++) {
			uniformDists[i] = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
					GlobalPRNG.class).getEngine());
		}
	}

	/**
	 * apply probablistic day banning penalty to specialists that are supposed to
	 * have less presence in the game.
	 */
	protected void applyPossblePenalties() {
		for (int i = 0; i < specialistIds.length; i++) {
			if (uniformDists[i].nextDouble() > payoffs[i]) {
				// ban the specialist from the next day
				behaviorController.addPenalty(specialistIds[i],
						ClientBehaviorController.DAY_BANNING_PENALTY);
				PayoffReport.logger.info("Ban once " + specialistIds[i]);
				PayoffReport.logger.info("");
			}
		}
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof SimulationStartedEvent) {
			initScoreReport();

			if (initPreviousPayoffs()) {
				initWriter();
			} else {
				initWriter();
				generateHeader();

				PayoffReport.logger
						.info("The participation of specialists in the game will be assumed equal.");
				writePayoffs(); // writes same payoffs of 1
			}

			initRandomGenerators();
		} else if (event instanceof GameStartedEvent) {

			PayoffReport.logger.info("Specialists:        " + specialistIds);
			PayoffReport.logger.info("Normalized Payoffs: "
					+ Message.concatenate(payoffs));
			PayoffReport.logger.info("");

			// TODO: force score report to clear scores from previous game runs.
			scoreReport.initScoreRecords();
		} else if (event instanceof DayInitPassEvent) {
			switch (((DayInitPassEvent) event).getPass()) {
			case DayInitPassEvent.FIRST_PASS:
				applyPossblePenalties();
			}
		} else if (event instanceof GameOverEvent) {
			writePayoffs();
		} else if (event instanceof SimulationOverEvent) {
			closeWriter();
		}
	}

	public void produceUserOutput() {
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}