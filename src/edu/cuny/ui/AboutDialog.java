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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JDialog} that
 * shows information about a program.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class AboutDialog extends UserDialog implements ActionListener,
		Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_TEXT = "text";

	JLabel label;

	JButton button;

	public AboutDialog(final Frame parent) {
		super(parent);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {

		super.setup(parameters, base);

		final PanelWithInsets p1 = new PanelWithInsets();
		getContentPane().add(p1, BorderLayout.CENTER);
		p1.setLayout(new BorderLayout());

		label = new JLabel();
		label.setText(parameters.getStringWithDefault(
				base.push(AboutDialog.P_TEXT), null, "Cool !"));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		p1.add(BorderLayout.CENTER, label);

		final PanelWithInsets p2 = new PanelWithInsets();
		p2.setLayout(new FlowLayout(1));
		getContentPane().add(p2, BorderLayout.SOUTH);

		button = new JButton("OK");
		button.addActionListener(this);
		p2.add(BorderLayout.CENTER, button);

		pack();

		setResizable(false);
	}

	public void actionPerformed(final ActionEvent actionevent) {
		setVisible(false);
	}
}
