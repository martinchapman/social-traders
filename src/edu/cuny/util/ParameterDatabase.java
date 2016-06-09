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
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */

package edu.cuny.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * <p>
 * This extension of the Properties class allows you to set, get, and delete
 * Parameters in a hierarchical tree-like database. The database consists of a
 * list of Parameters, plus an array of "parent databases" which it falls back
 * on when it can't find the Parameter you're looking for. Parents may also have
 * arrays of parents, and so on..
 * 
 * <p>
 * The parameters are loaded from a Java property-list file, which is basically
 * a collection of parameter=value pairs, one per line. Empty lines and lines
 * beginning with # are ignored. These parameters and their values are
 * <b>case-sensitive </b>, and whitespace is trimmed I believe.
 * 
 * <p>
 * An optional set of parameters, "parent. <i>n </i>", where <i>n </i> are
 * consecutive integers starting at 0, define the filenames of the database's
 * parents.
 * 
 * <p>
 * An optional set of parameters, "print-params", specifies whether or not
 * parameters should be printed as they are used (through one of the get(...)
 * methods). If print-params is unset, or set to false or FALSE, nothing is
 * printed. If set to non-false, then the parameters are printed prepended with
 * a "P:" when their values are requested, "E:" when their existence is tested.
 * Prior to the "P:" or "E:" you may see a "!" (meaning that the parameter isn't
 * in the database), or a "&lt;" (meaning that the parameter was a default
 * parameter which was never looked up because the primary parameter contained
 * the value).
 * 
 * <p>
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase(), it is
 * created thus:
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase( <i>file
 * </i>), it is created by loading the database file, and its parent file tree,
 * thus:
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * <td><tt>&nbsp;-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;(file)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase( <i>file,argv
 * </i>), the preferred way, it is created thus:
 * 
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * <td><tt>&nbsp;-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>(argv)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;(file)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * ...that is, the actual top database is empty, and stores parameters added
 * programmatically; its parent is a database formed from arguments passed in on
 * the command line; <i>its </i> parent is the parameter database which actually
 * loads from foo. This allows you to programmatically add parameters which
 * override those in foo, then delete them, thus bringing foo's parameters back
 * in view.
 * 
 * <p>
 * Once a parameter database is loaded, you query it with the <tt>get</tt>
 * methods. The database, then its parents, are searched until a match is found
 * for your parameter. The search rules are thus: (1) the root database is
 * searched first. (2) If a database being searched doesn't contain the data, it
 * searches its parents recursively, starting with parent 0, then moving up,
 * until all searches are exhausted or something was found. (3) No database is
 * searched twice.
 * 
 * <p>
 * The various <tt>get</tt> methods all take two parameters. The first parameter
 * is fetched and retrieved first. If that fails, the second one (known as the
 * <i>default parameter</i>) is fetched and retrieved. You can pass in
 * <tt>null</tt> for the default parameter if you don't have one.
 * 
 * <p>
 * You can test a parameter for existence with the <tt>exists</tt> methods.
 * 
 * <p>
 * You can set a parameter (in the topmost database <i>only </i> with the
 * <tt>set</tt> command. The <tt>remove</tt> command removes a parameter from
 * the topmost database only. The <tt>removeDeeply</tt> command removes that
 * parameter from every database.
 * 
 * <p>
 * The values stored in a parameter database must not contain "#", "=",
 * non-ascii values, or whitespace.
 * 
 * <p>
 * <b>Note for JDK 1.1 </b>. Finally recovering from stupendous idiocy, JDK 1.2
 * included parseDouble() and parseFloat() commands; now you can READ A FLOAT
 * FROM A STRING without having to create a Float object first! Anyway, you will
 * need to modify the getFloat() method below if you're running on JDK 1.1, but
 * understand that large numbers of calls to the method may be inefficient.
 * Sample JDK 1.1 code is given with those methods, but is commented out.
 * 
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author Sean Luke
 * @version $Revision: 1.22 $
 */

public final class ParameterDatabase extends Properties implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String C_HERE = "$";

	public static final String UNKNOWN_VALUE = "";

	public static final String PRINT_PARAMS = "print-params";

	public static final int PS_UNKNOWN = -1;

	public static final int PS_NONE = 0;

	public static final int PS_PRINT_PARAMS = 1;

	public int printState = ParameterDatabase.PS_UNKNOWN;

	Vector<ParameterDatabase> parents;

	File directory;

	String filename;

	boolean checked;

	Hashtable<String, Boolean> gotten;

	Hashtable<String, Boolean> accessed;

	Vector<ParameterDatabaseListener> listeners;

	URL imgBase;

	public static final URL getURL(final String path) {
		// System.out.println("getURL: " + path);
		URL url = null;

		final File file = new File(path);
		if (file.exists()) {
			try {
				url = file.toURI().toURL();
				// System.out.println("FILE: " + url.toString());
			} catch (final MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				url = new URL(path);
				// System.out.println("URL: " + url.toString());
			} catch (final MalformedURLException e1) {
				if (path.startsWith("/")) {
					url = ParameterDatabase.class.getResource(path);
				} else {
					url = ParameterDatabase.class.getResource("/" + path);
				}

				if (url == null) {
					e1.printStackTrace();
				} else {
					// System.out.println("RES: " + url.toString());
				}
			}
		}

		return url;

	}

	public final Color getColor(final Parameter parameter,
			final Parameter defaultParameter) {
		return getColorWithDefault(parameter, defaultParameter, null);
	}

	public final synchronized Color getColorWithDefault(
			final Parameter parameter, final Parameter defaultParameter,
			final Color defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getColorWithDefault(parameter, defaultValue);
		} else {
			return getColorWithDefault(defaultParameter, defaultValue);
		}
	}

	final Color getColorWithDefault(final Parameter parameter,
			final Color defaultValue) {
		if (_exists(parameter)) {
			final String colorText = get(parameter);
			final Color color = mapNamedColor(colorText);
			if (color == null) {
				return defaultValue;
			} else {
				return color;
			}
		} else {
			return defaultValue;
		}

	}

	Color mapNamedColor(String colorText) {
		if (colorText == null) {
			return null;
		}
		Color color = ColorNameTable.getColor(colorText);
		if (color == null) {
			if ((colorText.indexOf(35) < 0) && (colorText.indexOf("0x") < 0)) {
				colorText = "#" + colorText;
			}
			try {
				color = Color.decode(colorText);
			} catch (final NumberFormatException _ex) {
				color = null;
			}
		}
		return color;
	}

	public final Font getFont(final Parameter parameter,
			final Parameter defaultParameter) {
		return getFontWithDefault(parameter, defaultParameter, null);
	}

	public final synchronized Font getFontWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final Font defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getFontWithDefault(parameter, defaultValue);
		} else {
			return getFontWithDefault(defaultParameter, defaultValue);
		}
	}

	Font getFontWithDefault(final Parameter parameter, final Font defaultValue) {
		if (_exists(parameter)) {
			final String fontText = get(parameter);
			final Font font = Font.decode(fontText);
			if (font == null) {
				return defaultValue;
			} else {
				return font;
			}
		} else {
			return defaultValue;
		}

	}

	public final synchronized Image getImage(final Parameter parameter,
			final Parameter defaultParameter) {
		if (_exists(parameter)) {
			return getImage(parameter);
		} else {
			return getImage(defaultParameter);
		}
	}

	final Image getImage(final Parameter parameter) {
		if (_exists(parameter)) {
			final URL url = getImageURL(get(parameter));
			if (url != null) {
				return Toolkit.getDefaultToolkit().getImage(url);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	URL getImageURL(final String imagePath) {
		if (imagePath == null) {
			return null;
		}

		if (imgBase == null) {
			imgBase = ParameterDatabase.getURL(get(new Parameter("images.url")));
		}

		try {
			URL url;
			if (imgBase != null) {
				url = new URL(imgBase, imagePath);
			} else {
				url = new URL(imagePath);
			}

			return url;
		} catch (final MalformedURLException _ex) {
			return null;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a full Class name, and the class must be a descendent of but not equal
	 * to <i>mustCastTosuperclass </i>. Loads the class and returns an instance
	 * (constructed with the default constructor), or throws a
	 * ParamClassLoadException if there is no such Class. If the parameter is not
	 * found, the defaultParameter is used. The parameter chosen is marked "used".
	 */
	public final <V> V getInstanceForParameter(final Parameter parameter,
			final Parameter defaultParameter, final Class<V> mustCastTosuperclass)
			throws ParamClassLoadException {
		printGotten(parameter, defaultParameter, false);
		Parameter p;

		if (_exists(parameter)) {
			p = parameter;
		} else if (_exists(defaultParameter)) {
			p = defaultParameter;
		} else {
			throw new ParamClassLoadException(
					"No class name provided.\nPARAMETER: "
							+ parameter
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter));
		}
		try {
			final Class<?> c = Class.forName(get(p));
			if (!mustCastTosuperclass.isAssignableFrom(c)) {
				throw new ParamClassLoadException("The class "
						+ c.getName()
						+ "\ndoes not cast into the superclass "
						+ mustCastTosuperclass.getName()
						+ "\nPARAMETER: "
						+ parameter
						+ (defaultParameter == null ? "" : "\n     ALSO: "
								+ defaultParameter));
			}
			if (mustCastTosuperclass == c) {
				throw new ParamClassLoadException("The class "
						+ c.getName()
						+ "\nmust not be the same as the required superclass "
						+ mustCastTosuperclass.getName()
						+ "\nPARAMETER: "
						+ parameter
						+ (defaultParameter == null ? "" : "\n     ALSO: "
								+ defaultParameter));
			}
			return mustCastTosuperclass.cast(c.newInstance());
		} catch (final ClassNotFoundException e) {
			throw new ParamClassLoadException(
					"Class not found: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final IllegalArgumentException e) {
			throw new ParamClassLoadException(
					"Could not load class: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final InstantiationException e) {
			throw new ParamClassLoadException(
					"The requested class is an interface or an abstract class: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final IllegalAccessException e) {
			throw new ParamClassLoadException(
					"The requested class cannot be initialized with the default initializer: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a full Class name, and the class must be a descendent, or equal to,
	 * <i>mustCastTosuperclass </i>. Loads the class and returns an instance
	 * (constructed with the default constructor), or throws a
	 * ParamClassLoadException if there is no such Class. The parameter chosen is
	 * marked "used".
	 */
	public final <V> V getInstanceForParameterEq(final Parameter parameter,
			final Parameter defaultParameter, final Class<V> mustCastTosuperclass)
			throws ParamClassLoadException {
		printGotten(parameter, defaultParameter, false);
		Parameter p;
		if (_exists(parameter)) {
			p = parameter;
		} else if (_exists(defaultParameter)) {
			p = defaultParameter;
		} else {
			throw new ParamClassLoadException(
					"No class name provided.\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter));
		}
		try {
			final Class<?> c = Class.forName(get(p));
			if (!mustCastTosuperclass.isAssignableFrom(c)) {
				throw new ParamClassLoadException("The class "
						+ c.getName()
						+ "\ndoes not cast into the superclass "
						+ mustCastTosuperclass.getName()
						+ "\nPARAMETER: "
						+ parameter
						+ "\n     ALSO: "
						+ (defaultParameter == null ? "" : "\n     ALSO: "
								+ defaultParameter));
			}
			return mustCastTosuperclass.cast(c.newInstance());
		} catch (final ClassNotFoundException e) {
			throw new ParamClassLoadException(
					"Class not found: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final IllegalArgumentException e) {
			throw new ParamClassLoadException(
					"Could not load class: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final InstantiationException e) {
			throw new ParamClassLoadException(
					"The requested class is an interface or an abstract class: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final IllegalAccessException e) {
			throw new ParamClassLoadException(
					"The requested class cannot be initialized with the default initializer: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		}
	}

	/**
	 * Searches down through databases to find a given parameter. The value
	 * associated with this parameter must be a full Class name, and the class
	 * must be a descendent of but not equal to <i>mustCastTosuperclass </i>.
	 * Loads and returns the associated Class, or throws a ParamClassLoadException
	 * if there is no such Class. If the parameter is not found, the
	 * defaultParameter is used. The parameter chosen is marked "used".
	 */
	public final <U> Class<? extends U> getClassForParameter(
			final Parameter parameter, final Parameter defaultParameter,
			final Class<U> mustCastTosuperclass) throws ParamClassLoadException {
		printGotten(parameter, defaultParameter, false);
		Parameter p;
		if (_exists(parameter)) {
			p = parameter;
		} else if (_exists(defaultParameter)) {
			p = defaultParameter;
		} else {
			throw new ParamClassLoadException(
					"No class name provided.\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter));
		}
		try {
			final Class<?> c = Class.forName(get(p));
			if (!mustCastTosuperclass.isAssignableFrom(c)) {
				throw new ParamClassLoadException("The class "
						+ c.getName()
						+ "\ndoes not cast into the superclass "
						+ mustCastTosuperclass.getName()
						+ "\nPARAMETER: "
						+ parameter
						+ "\n     ALSO: "
						+ (defaultParameter == null ? "" : "\n     ALSO: "
								+ defaultParameter));
			}
			return c.asSubclass(mustCastTosuperclass);
		} catch (final ClassNotFoundException e) {
			throw new ParamClassLoadException(
					"Class not found: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		} catch (final IllegalArgumentException e) {
			throw new ParamClassLoadException(
					"Could not load class: "
							+ get(p)
							+ "\nPARAMETER: "
							+ parameter
							+ "\n     ALSO: "
							+ (defaultParameter == null ? "" : "\n     ALSO: "
									+ defaultParameter) + "\nEXCEPTION: \n\n" + e);
		}
	}

	/**
	 * Searches down through databases to find a given parameter; If the parameter
	 * does not exist, defaultValue is returned. If the parameter exists, and it
	 * is set to "false" (case insensitive), false is returned. Else true is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	public final boolean getBoolean(final Parameter parameter,
			final Parameter defaultParameter, final boolean defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getBoolean(parameter, defaultValue);
		} else {
			return getBoolean(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter; If the parameter
	 * does not exist, defaultValue is returned. If the parameter exists, and it
	 * is set to "false" (case insensitive), false is returned. Else true is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	final boolean getBoolean(final Parameter parameter, final boolean defaultValue) {
		if (!_exists(parameter)) {
			return defaultValue;
		}
		return (!get(parameter).equalsIgnoreCase("false"));
	}

	/**
	 * Parses an integer from a string, either in decimal or (if starting with an
	 * x) in hex
	 */
	// we assume that the string has been trimmed already
	/* protected */final int parseInt(final String string)
			throws NumberFormatException {
		char c;
		if ((string != null) && (string.length() > 0)
				&& ((string.charAt(0) == (c = 'x')) || (c == 'X'))) {
			// it's a hex int, load it as hex
			return Integer.parseInt(string.substring(1), 16);
		} else {
			// it's decimal
			return Integer.parseInt(string);
		}
	}

	/**
	 * Parses a long from a string, either in decimal or (if starting with an x)
	 * in hex
	 */
	// we assume that the string has been trimmed already
	/* protected */final long parseLong(final String string)
			throws NumberFormatException {
		char c;
		if ((string != null) && (string.length() > 0)
				&& ((string.charAt(0) == (c = 'x')) || (c == 'X'))) {
			// it's a hex int, load it as hex
			return Long.parseLong(string.substring(1), 16);
		} else {
			// it's decimal
			return Long.parseLong(string);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer. It returns the value, else throws a NumberFormatException
	 * exception if there is an error in parsing the parameter. The parameter
	 * chosen is marked "used" if it exists. Integers may be in decimal or (if
	 * preceded with an X or x) in hexadecimal.
	 */
	/* protected */final int getInt(final Parameter parameter)
			throws NumberFormatException {
		if (_exists(parameter)) {
			try {
				return parseInt(get(parameter));
			} catch (final NumberFormatException e) {
				throw new NumberFormatException("Bad integer (" + get(parameter)
						+ " ) for parameter " + parameter);
			}
		} else {
			throw new NumberFormatException("Integer does not exist for parameter "
					+ parameter);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer. It returns the value, else throws a NumberFormatException
	 * exception if there is an error in parsing the parameter. The parameter
	 * chosen is marked "used" if it exists. Integers may be in decimal or (if
	 * preceded with an X or x) in hexadecimal.
	 */
	public final int getInt(final Parameter parameter,
			final Parameter defaultParameter) throws NumberFormatException {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getInt(parameter);
		} else if (_exists(defaultParameter)) {
			return getInt(defaultParameter);
		} else {
			throw new NumberFormatException(
					"Integer does not exist for either parameter " + parameter + "\nor\n"
							+ defaultParameter);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer >= minValue. It returns the value, or minValue-1 if the value
	 * is out of range or if there is an error in parsing the parameter. The
	 * parameter chosen is marked "used" if it exists. Integers may be in decimal
	 * or (if preceded with an X or x) in hexadecimal.
	 */
	public final int getInt(final Parameter parameter,
			final Parameter defaultParameter, final int minValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getInt(parameter, minValue);
		} else {
			return getInt(defaultParameter, minValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer >= minValue. It returns the value, or minValue-1 if the value
	 * is out of range or if there is an error in parsing the parameter. The
	 * parameter chosen is marked "used" if it exists. Integers may be in decimal
	 * or (if preceded with an X or x) in hexadecimal.
	 */
	/* protected */final int getInt(final Parameter parameter, final int minValue) {
		if (_exists(parameter)) {
			try {
				final int i = parseInt(get(parameter));
				if (i < minValue) {
					return minValue - 1;
				}
				return i;
			} catch (final NumberFormatException e) {
				return minValue - 1;
			}
		} else {
			return minValue - 1;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be an
	 * integer. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists. Integers may
	 * be in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	public final int getIntWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final int defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getIntWithDefault(parameter, defaultValue);
		} else {
			return getIntWithDefault(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be an
	 * integer. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists. Integers may
	 * be in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	final int getIntWithDefault(final Parameter parameter, final int defaultValue) {
		if (_exists(parameter)) {
			try {
				return parseInt(get(parameter));
			} catch (final NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer >= minValue and <= maxValue. It returns the value, or
	 * minValue-1 if the value is out of range or if there is an error in parsing
	 * the parameter. The parameter chosen is marked "used" if it exists. Integers
	 * may be in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	public final int getIntWithMax(final Parameter parameter,
			final Parameter defaultParameter, final int minValue, final int maxValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getIntWithMax(parameter, minValue, maxValue);
		} else {
			return getIntWithMax(defaultParameter, minValue, maxValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be an integer >= minValue and <= maxValue. It returns the value, or
	 * minValue-1 if the value is out of range or if there is an error in parsing
	 * the parameter. The parameter chosen is marked "used" if it exists. Integers
	 * may be in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	final int getIntWithMax(final Parameter parameter, final int minValue,
			final int maxValue) {
		if (_exists(parameter)) {
			try {
				final int i = parseInt(get(parameter));
				if (i < minValue) {
					return minValue - 1;
				}
				if (i > maxValue) {
					return minValue - 1;
				}
				return i;
			} catch (final NumberFormatException e) {
				return minValue - 1;
			}
		} else {
			return minValue - 1;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a float >= minValue. If not, this method returns minvalue-1, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists.
	 */

	public final float getFloat(final Parameter parameter,
			final Parameter defaultParameter, final double minValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getFloat(parameter, minValue);
		} else {
			return getFloat(defaultParameter, minValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a float >= minValue. If not, this method returns minvalue-1, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists.
	 */

	final float getFloat(final Parameter parameter, final double minValue) {
		if (_exists(parameter)) {
			try {
				final float i = Float.valueOf(get(parameter)).floatValue(); // what
				// stupidity...

				// For JDK 1.2 and later, this is more efficient...
				// float i = Float.parseFloat(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1

				if (i < minValue) {
					return (float) (minValue - 1);
				}
				return i;
			} catch (final NumberFormatException e) {
				return (float) (minValue - 1);
			}
		} else {
			return (float) (minValue - 1);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * float. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	public final float getFloatWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final double defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getFloatWithDefault(parameter, defaultValue);
		} else {
			return getFloatWithDefault(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * float. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	final float getFloatWithDefault(final Parameter parameter,
			final double defaultValue) {
		if (_exists(parameter)) {
			try {
				// For JDK 1.2 and later, this is more efficient...
				// return Float.parseFloat(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1
				return Float.valueOf(get(parameter)).floatValue(); // what
				// stupidity...
			} catch (final NumberFormatException e) {
				return (float) (defaultValue);
			}
		} else {
			return (float) (defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a float >= minValue and <= maxValue. If not, this method returns
	 * minvalue-1, else it returns the parameter value. The parameter chosen is
	 * marked "used" if it exists.
	 */

	public final float getFloat(final Parameter parameter,
			final Parameter defaultParameter, final double minValue,
			final double maxValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getFloat(parameter, minValue, maxValue);
		} else {
			return getFloat(defaultParameter, minValue, maxValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a float >= minValue and <= maxValue. If not, this method returns
	 * minvalue-1, else it returns the parameter value. The parameter chosen is
	 * marked "used" if it exists.
	 */

	final float getFloat(final Parameter parameter, final double minValue,
			final double maxValue) {
		if (_exists(parameter)) {
			try {
				final float i = Float.valueOf(get(parameter)).floatValue(); // what
				// stupidity...

				// For JDK 1.2 and later, this is more efficient...
				// float i = Float.parseFloat(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1

				if (i < minValue) {
					return (float) (minValue - 1);
				}
				if (i > maxValue) {
					return (float) (minValue - 1);
				}
				return i;
			} catch (final NumberFormatException e) {
				return (float) (minValue - 1);
			}
		} else {
			return (float) (minValue - 1);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a double >= minValue. If not, this method returns minvalue-1, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists.
	 */

	public final double getDouble(final Parameter parameter,
			final Parameter defaultParameter, final double minValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getDouble(parameter, minValue);
		} else {
			return getDouble(defaultParameter, minValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a double >= minValue. If not, this method returns minvalue-1, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists.
	 */

	final double getDouble(final Parameter parameter, final double minValue) {
		if (_exists(parameter)) {
			try {
				final double i = Double.valueOf(get(parameter)).doubleValue(); // what
				// stupidity...

				// For JDK 1.2 and later, this is more efficient...
				// double i = Double.parseDouble(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1

				if (i < minValue) {
					return (minValue - 1);
				}
				return i;
			} catch (final NumberFormatException e) {
				return (minValue - 1);
			}
		} else {
			return (minValue - 1);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a double >= minValue and <= maxValue. If not, this method returns
	 * minvalue-1, else it returns the parameter value. The parameter chosen is
	 * marked "used" if it exists.
	 */

	public final double getDouble(final Parameter parameter,
			final Parameter defaultParameter, final double minValue,
			final double maxValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getDouble(parameter, minValue, maxValue);
		} else {
			return getDouble(defaultParameter, minValue, maxValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a double >= minValue and <= maxValue. If not, this method returns
	 * minvalue-1, else it returns the parameter value. The parameter chosen is
	 * marked "used" if it exists.
	 */

	final double getDouble(final Parameter parameter, final double minValue,
			final double maxValue) {
		if (_exists(parameter)) {
			try {
				final double i = Double.valueOf(get(parameter)).doubleValue(); // what
				// stupidity...

				// For JDK 1.2 and later, this is more efficient...
				// double i = Double.parseDouble(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1

				if (i < minValue) {
					return (minValue - 1);
				}
				if (i > maxValue) {
					return (minValue - 1);
				}
				return i;
			} catch (final NumberFormatException e) {
				return (minValue - 1);
			}
		} else {
			return (minValue - 1);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * float. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	public final double getDoubleWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final double defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getDoubleWithDefault(parameter, defaultValue);
		} else {
			return getDoubleWithDefault(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * float. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists.
	 */
	final double getDoubleWithDefault(final Parameter parameter,
			final double defaultValue) {
		if (_exists(parameter)) {
			try {
				// For JDK 1.2 and later, this is more efficient...
				// return Double.parseDouble(get(parameter));
				// ...but we can't use it and still be compatible with JDK 1.1
				return Double.valueOf(get(parameter)).doubleValue(); // what
				// stupidity...
			} catch (final NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a long. It returns the value, else throws a NumberFormatException
	 * exception if there is an error in parsing the parameter. The parameter
	 * chosen is marked "used" if it exists. Longs may be in decimal or (if
	 * preceded with an X or x) in hexadecimal.
	 */
	/* protected */final long getLong(final Parameter parameter)
			throws NumberFormatException {
		if (_exists(parameter)) {
			try {
				return parseLong(get(parameter));
			} catch (final NumberFormatException e) {
				throw new NumberFormatException("Bad long (" + get(parameter)
						+ " ) for parameter " + parameter);
			}
		} else {
			throw new NumberFormatException("Long does not exist for parameter "
					+ parameter);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a long. It returns the value, else throws a NumberFormatException
	 * exception if there is an error in parsing the parameter. The parameter
	 * chosen is marked "used" if it exists. Longs may be in decimal or (if
	 * preceded with an X or x) in hexadecimal.
	 */
	public final long getLong(final Parameter parameter,
			final Parameter defaultParameter) throws NumberFormatException {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getLong(parameter);
		} else if (_exists(defaultParameter)) {
			return getLong(defaultParameter);
		} else {
			throw new NumberFormatException(
					"Long does not exist for either parameter " + parameter + "\nor\n"
							+ defaultParameter);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a long >= minValue. If not, this method returns errValue, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists. Longs may be in decimal or (if preceded with an X or x) in
	 * hexadecimal.
	 */

	public final long getLong(final Parameter parameter,
			final Parameter defaultParameter, final long minValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getLong(parameter, minValue);
		} else {
			return getLong(defaultParameter, minValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a long >= minValue. If not, this method returns errValue, else it
	 * returns the parameter value. The parameter chosen is marked "used" if it
	 * exists. Longs may be in decimal or (if preceded with an X or x) in
	 * hexadecimal.
	 */
	final long getLong(final Parameter parameter, final long minValue) {
		if (_exists(parameter)) {
			try {
				final long i = parseLong(get(parameter));
				if (i < minValue) {
					return minValue - 1;
				}
				return i;
			} catch (final NumberFormatException e) {
				return minValue - 1;
			}
		} else {
			return (minValue - 1);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * long. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists. Longs may be
	 * in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	public final long getLongWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final long defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getLongWithDefault(parameter, defaultValue);
		} else {
			return getLongWithDefault(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter, which must be a
	 * long. If there is an error in parsing the parameter, then default is
	 * returned. The parameter chosen is marked "used" if it exists. Longs may be
	 * in decimal or (if preceded with an X or x) in hexadecimal.
	 */
	final long getLongWithDefault(final Parameter parameter,
			final long defaultValue) {
		if (_exists(parameter)) {
			try {
				return parseLong(get(parameter));
			} catch (final NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Searches down through databases to find a given parameter, whose value must
	 * be a long >= minValue and = < maxValue. If not, this method returns
	 * errValue, else it returns the parameter value. The parameter chosen is
	 * marked "used" if it exists. Longs may be in decimal or (if preceded with an
	 * X or x) in hexadecimal.
	 */
	public final long getLongWithMax(final Parameter parameter,
			final Parameter defaultParameter, final long minValue, final long maxValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getLong(parameter, minValue, maxValue);
		} else {
			return getLong(defaultParameter, minValue, maxValue);
		}
	}

	/**
	 * Use getLongWithMax(...) instead. Searches down through databases to find a
	 * given parameter, whose value must be a long >= minValue and = < maxValue.
	 * If not, this method returns errValue, else it returns the parameter value.
	 * The parameter chosen is marked "used" if it exists. Longs may be in decimal
	 * or (if preceded with an X or x) in hexadecimal.
	 */
	final long getLongWithMax(final Parameter parameter, final long minValue,
			final long maxValue) {
		if (_exists(parameter)) {
			try {
				final long i = parseLong(get(parameter));
				if (i < minValue) {
					return minValue - 1;
				}
				if (i > maxValue) {
					return minValue - 1;
				}
				return i;
			} catch (final NumberFormatException e) {
				return minValue - 1;
			}
		} else {
			return (minValue - 1);
		}
	}

	/**
	 * Use getLongWithMax(...) instead. Searches down through databases to find a
	 * given parameter, whose value must be a long >= minValue and = < maxValue.
	 * If not, this method returns errValue, else it returns the parameter value.
	 * The parameter chosen is marked "used" if it exists. Longs may be in decimal
	 * or (if preceded with an X or x) in hexadecimal.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public final long getLong(final Parameter parameter,
			final Parameter defaultParameter, final long minValue, final long maxValue) {
		printGotten(parameter, defaultParameter, false);
		return getLongWithMax(parameter, defaultParameter, minValue, maxValue);
	}

	/**
	 * Use getLongWithMax(...) instead. Searches down through databases to find a
	 * given parameter, whose value must be a long >= minValue and = < maxValue.
	 * If not, this method returns errValue, else it returns the parameter value.
	 * The parameter chosen is marked "used" if it exists.
	 * 
	 * @deprecated
	 */
	@Deprecated
	final long getLong(final Parameter parameter, final long minValue,
			final long maxValue) {
		return getLongWithMax(parameter, minValue, maxValue);
	}

	/**
	 * Searches down through the databases to find a given parameter, whose value
	 * must be an absolute or relative path name. If it is absolute, a File is
	 * made based on the path name. If it is relative, a file is made by resolving
	 * the path name with respect to the directory in which the file was which
	 * defined this ParameterDatabase in the ParameterDatabase hierarchy. If the
	 * parameter is not found, this returns null. The File is not checked for
	 * validity. The parameter chosen is marked "used" if it exists.
	 */

	public final File getFile(final Parameter parameter,
			final Parameter defaultParameter) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getFile(parameter);
		} else {
			return getFile(defaultParameter);
		}
	}

	/**
	 * Searches down through the databases to find a given parameter, whose value
	 * must be an absolute or relative path name. If the parameter begins with a
	 * "$", a file is made based on the relative path name and returned directly.
	 * Otherwise, if it is absolute, a File is made based on the path name, or if
	 * it is relative, a file is made by resolving the path name with respect to
	 * the directory in which the file was which defined this ParameterDatabase in
	 * the ParameterDatabase hierarchy. If the parameter is not found, this
	 * returns null. The File is not checked for validity. The parameter chosen is
	 * marked "used" if it exists.
	 */

	final File getFile(final Parameter parameter) {
		if (_exists(parameter)) {
			final String p = get(parameter);
			if (p == null) {
				return null;
			}
			if (p.startsWith(ParameterDatabase.C_HERE)) {
				return new File(p.substring(ParameterDatabase.C_HERE.length()));
			} else {
				final File f = new File(p);
				if (f.isAbsolute()) {
					return f;
				} else {
					return new File(directoryFor(parameter), p);
				}
			}
		} else {
			return null;
		}
	}

	/**
	 * Searches down through databases to find a given parameter. Returns the
	 * parameter's value (trimmed) or null if not found or if the trimmed result
	 * is empty. The parameter chosen is marked "used" if it exists.
	 */

	public final synchronized String getString(final Parameter parameter,
			final Parameter defaultParameter) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getString(parameter);
		} else {
			return getString(defaultParameter);
		}
	}

	/**
	 * Searches down through databases to find a given parameter. Returns the
	 * parameter's value (trimmed) or null if not found or if the trimmed result
	 * is empty. The parameter chosen is marked "used" if it exists.
	 */

	/* protected */final synchronized String getString(final Parameter parameter) {
		if (_exists(parameter)) {
			return get(parameter);
		} else {
			return null;
		}
	}

	/**
	 * Searches down through databases to find a given parameter. Returns the
	 * parameter's value trimmed of whitespace, or defaultValue.trim() if the
	 * result is not found or the trimmed result is empty.
	 */
	public final String getStringWithDefault(final Parameter parameter,
			final Parameter defaultParameter, final String defaultValue) {
		printGotten(parameter, defaultParameter, false);
		if (_exists(parameter)) {
			return getStringWithDefault(parameter, defaultValue);
		} else {
			return getStringWithDefault(defaultParameter, defaultValue);
		}
	}

	/**
	 * Searches down through databases to find a given parameter. Returns the
	 * parameter's value trimmed of whitespace, or defaultValue.trim() if the
	 * result is not found or the trimmed result is empty.
	 */
	/* protected */final String getStringWithDefault(final Parameter parameter,
			final String defaultValue) {
		if (_exists(parameter)) {
			String result = get(parameter);
			if (result == null) {
				if (defaultValue == null) {
					return null;
				} else {
					result = defaultValue.trim();
				}
			} else {
				result = result.trim();
				if (result.length() == 0) {
					if (defaultValue == null) {
						return null;
					} else {
						result = defaultValue.trim();
					}
				}
			}
			return result;
		} else {
			if (defaultValue == null) {
				return null;
			} else {
				return defaultValue.trim();
			}
		}
	}

	/** Clears the checked flag */
	/* protected */final synchronized void uncheck() {
		if (!checked) {
			return; // we already unchecked this path -- this is dangerous if
		}
		// parents are used without children
		checked = false;
		final int size = parents.size();
		for (int x = size - 1; x >= 0; x--) {
			(parents.elementAt(x)).uncheck();
		}
	}

	/**
	 * @param l
	 */
	public final synchronized void addListener(final ParameterDatabaseListener l) {
		listeners.add(l);
	}

	/**
	 * @param l
	 */
	public final synchronized void removeListener(
			final ParameterDatabaseListener l) {
		listeners.remove(l);
	}

	/**
	 * Fires a parameter set event.
	 * 
	 * @param parameter
	 * @param value
	 */
	public synchronized final void fireParameterSet(final Parameter parameter,
			final String value) {
		for (final ParameterDatabaseListener l : listeners) {
			l.parameterSet(new ParameterDatabaseEvent(this, parameter, value,
					ParameterDatabaseEvent.SET));
		}
	}

	/**
	 * Fires a parameter accessed event.
	 * 
	 * @param parameter
	 * @param value
	 */
	public synchronized final void fireParameterAccessed(
			final Parameter parameter, final String value) {
		for (final ParameterDatabaseListener l : listeners) {
			l.parameterSet(new ParameterDatabaseEvent(this, parameter, value,
					ParameterDatabaseEvent.ACCESSED));
		}
	}

	/**
	 * Sets a parameter in the topmost database to a given value, trimmed of
	 * whitespace.
	 */
	public final synchronized void set(final Parameter parameter,
			final String value) {
		final String tmp = value.trim();
		put(parameter.param, tmp);
		fireParameterSet(parameter, tmp);
	}

	/**
	 * Prints out all the parameters marked as used, plus their values. If a
	 * parameter was listed as "used" but not's actually in the database, the
	 * value printed is UNKNOWN_VALUE (set to "?????")
	 */

	public final synchronized void listGotten(final PrintWriter p) {

		final String[] array = gotten.keySet().toArray(new String[0]);

		// Uncheck and print each item
		for (final String s : array) {
			String v = null;
			if (s != null) {
				v = _get(s);
				uncheck();
			}
			if (v == null) {
				v = ParameterDatabase.UNKNOWN_VALUE;
			}
			p.println(s + " = " + v);
		}
		p.flush();
	}

	/** Prints out all the parameters NOT marked as used, plus their values. */

	public final synchronized void listNotGotten(final PrintWriter p) {
		// final Vector vec = new Vector();
		//
		// final Hashtable all = new Hashtable();
		Map<String, String> all;
		all = _list(null, false, null); // grab all the nonshadowed keys

		// Enumeration e = gotten.keys();
		// while (e.hasMoreElements()) {
		// all.remove(e.nextElement());
		// }
		for (final String key : gotten.keySet()) {
			all.remove(key);
		}

		// e = all.keys();
		// while (e.hasMoreElements()) {
		// vec.addElement(e.nextElement());
		// }
		//
		// // sort the keys
		// final Object[] array = new Object[vec.size()];
		// vec.copyInto(array);

		// java.util.Collections.sort(vec);

		final String[] array = all.keySet().toArray(new String[0]);

		// Uncheck and print each item
		for (final String s : array) {
			String v = null;
			if (s != null) {
				v = _get(s);
				uncheck();
			}
			if (v == null) {
				v = ParameterDatabase.UNKNOWN_VALUE;
			}
			p.println(s + " = " + v);
		}
		p.flush();
	}

	/** Prints out all the parameters NOT marked as used, plus their values. */

	public final synchronized void listNotAccessed(final PrintWriter p) {

		final Map<String, String> all = _list(null, false, null);
		for (final String key : accessed.keySet()) {
			all.remove(key);
		}

		final String[] array = all.keySet().toArray(new String[0]);

		// Uncheck and print each item
		for (final String s : array) {
			String v = null;
			if (s != null) {
				v = _get(s);
				uncheck();
			}
			if (v == null) {
				v = ParameterDatabase.UNKNOWN_VALUE;
			}
			p.println(s + " = " + v);
		}
		p.flush();
	}

	/**
	 * Prints out all the parameters marked as accessed ("gotten" by some
	 * getFoo(...) method), plus their values. If this method ever prints
	 * UNKNOWN_VALUE ("?????"), that's a bug.
	 */

	public final synchronized void listAccessed(final PrintWriter p) {

		final String[] array = accessed.keySet().toArray(new String[0]);

		// Uncheck and print each item
		for (final String s : array) {
			String v = null;
			if (s != null) {
				v = _get(s);
				uncheck();
			}
			if (v == null) {
				v = ParameterDatabase.UNKNOWN_VALUE;
			}
			p.println(s + " = " + v);
		}
		p.flush();
	}

	/** Returns true if parameter exist in the database */
	public final synchronized boolean exists(final Parameter parameter) {
		printGotten(parameter, null, true);
		return _exists(parameter);
	}

	/* protected */final synchronized boolean _exists(final Parameter parameter) {
		if (parameter == null) {
			return false;
		}
		final String result = _get(parameter.param);
		uncheck();

		accessed.put(parameter.param, Boolean.TRUE);
		return (result != null);
	}

	/**
	 * Returns true if either parameter or defaultParameter exists in the database
	 */
	public final synchronized boolean exists(final Parameter parameter,
			final Parameter defaultParameter) {
		printGotten(parameter, defaultParameter, true);
		if (exists(parameter)) {
			return true;
		}
		if (exists(defaultParameter)) {
			return true;
		}
		return false;
	}

	/*
	 * P: Successfully retrieved parameter !P: Unsuccessfully retrieved parameter
	 * <P: Would have retrieved parameter
	 * 
	 * E: Successfully tested for existence of parameter !E: Unsuccessfully tested
	 * for existence of parameter <E: Would have tested for exidstence of
	 * parameter
	 */

	/* protected */void printGotten(final Parameter parameter,
			final Parameter defaultParameter, final boolean exists) {
		if (printState == ParameterDatabase.PS_UNKNOWN) {
			final Parameter p = new Parameter(ParameterDatabase.PRINT_PARAMS);
			final String jp = get(p);
			if ((jp == null) || jp.equalsIgnoreCase("false")) {
				printState = ParameterDatabase.PS_NONE;
			} else {
				printState = ParameterDatabase.PS_PRINT_PARAMS;
			}
			uncheck();
			printGotten(p, null, false);
		}

		if (printState == ParameterDatabase.PS_PRINT_PARAMS) {
			String p = "P: ";
			if (exists) {
				p = "E: ";
			}

			if ((parameter == null) && (defaultParameter == null)) {
				return;
			} else if (parameter == null) {
				final String result = _get(defaultParameter.param);
				uncheck();
				if (result == null) {
					// null parameter, didn't find defaultParameter
					System.err.println("\t!" + p + defaultParameter.param);
				} else {
					// null parameter, found defaultParameter
					System.err.println("\t " + p + defaultParameter.param + " = "
							+ result);
				}
			}

			else if (defaultParameter == null) {
				final String result = _get(parameter.param);
				uncheck();
				if (result == null) {
					// null defaultParameter, didn't find parameter
					System.err.println("\t!" + p + parameter.param);
				} else {
					// null defaultParameter, found parameter
					System.err.println("\t " + p + parameter.param + " = " + result);
				}
			}

			else {
				String result = _get(parameter.param);
				uncheck();
				if (result == null) {
					// didn't find parameter
					System.err.println("\t!" + p + parameter.param);
					result = _get(defaultParameter.param);
					uncheck();
					if (result == null) {
						// didn't find defaultParameter
						System.err.println("\t!" + p + defaultParameter.param);
					} else {
						// found defaultParameter
						System.err.println("\t " + p + defaultParameter.param + " = "
								+ result);
					}
				} else {
					// found parameter
					System.err.println("\t " + p + parameter.param + " = " + result);
					System.err.println("\t<" + p + defaultParameter.param);
				}
			}
		}
	}

	/**
	 * detects the pattern of <code>&xxx;</code> in the specified string and
	 * replaces the part with the value of parameter <code>xxx</code>.
	 * 
	 * @param value
	 *          the string possibly containing <code>&xxx;</code>
	 * @return the expanded string
	 */
	protected String expand(String value) {
		int j = 0;

		if (value != null) {
			while ((j = value.indexOf('&', j)) >= 0) {

				final int k = value.indexOf(';', j);
				if (k < 0) {
					// if missing ';', then '&' is viewed as a regular char
					// k = value.length();

					// TODO: do more to allow the regular '&' and ';' even when they
					// appear in pair
					break;
				}

				// added by jzniu to support the &&s1;;
				j = value.lastIndexOf('&', k);

				final String s3 = value.substring(j + 1, k);
				final String s4 = value.substring(0, j);
				String s5;
				if (k == value.length()) {
					s5 = "";
				} else {
					s5 = value.substring(k + 1);
				}
				String s6 = get(new Parameter(s3));

				if (s6 == null) {
					s6 = "";
				}

				value = s4 + s6 + s5;
				j = 0;
			}
		}

		return value;

	}

	/* protected */final synchronized String get(final Parameter parameter) {
		String result = _get(parameter.param);
		uncheck();

		// set hashtable appropriately
		if (parameter != null) {
			accessed.put(parameter.param, Boolean.TRUE);
		}
		if (parameter != null) {
			gotten.put(parameter.param, Boolean.TRUE);
		}

		// added by jzniu to detect the pattern of &xxx;
		result = expand(result);

		if ((result != null) && !result.equals("")) {
			result = result.replace('\t', ' ');
		}

		return result;
	}

	/** Private helper function */
	final synchronized String _get(final String parameter) {
		if (parameter == null) {
			return null;
		}
		if (checked) {
			return null; // we already searched this path
		}
		checked = true;
		String result = getProperty(parameter);
		if (result == null) {
			final int size = parents.size();
			for (int x = size - 1; x >= 0; x--) {
				result = (parents.elementAt(x))._get(parameter);
				if (result != null) {
					return result;
				}
			}
		} else // preprocess
		{
			result = result.trim();
			if (result.length() == 0) {
				result = null;
			}
		}
		return result;
	}

	/* protected */
	Set<String> _getShadowedValues(final Parameter parameter,
			final Set<String> vals) {
		if (parameter == null) {
			return vals;
		}

		if (checked) {
			return vals;
		}

		checked = true;
		String result = getProperty(parameter.param);
		if (result != null) {
			result = result.trim();
			if (result.length() != 0) {
				vals.add(result);
			}
		}

		final int size = parents.size();
		for (int x = size - 1; x >= 0; x--) {
			(parents.elementAt(x))._getShadowedValues(parameter, vals);
		}

		return vals;
	}

	public Set<String> getShadowedValues(final Parameter parameter) {
		Set<String> vals = new LinkedHashSet<String>();
		vals = _getShadowedValues(parameter, vals);
		uncheck();
		return vals;
	}

	/**
	 * Searches down through databases to find the directory for the database
	 * which holds a given parameter. Returns the directory name or null if not
	 * found.
	 */

	public final File directoryFor(final Parameter parameter) {
		final File result = _directoryFor(parameter);
		uncheck();
		return result;
	}

	/** Private helper function */
	final synchronized File _directoryFor(final Parameter parameter) {
		if (checked) {
			return null; // we already searched this path
		}
		checked = true;
		File result = null;
		final String p = getProperty(parameter.param);
		if (p == null) {
			final int size = parents.size();
			for (int x = size - 1; x >= 0; x--) {
				result = (parents.elementAt(x))._directoryFor(parameter);
				if (result != null) {
					return result;
				}
			}
			return result;
		} else {
			return directory;
		}
	}

	/**
	 * Searches down through databases to find the parameter file which holds a
	 * given parameter. Returns the filename or null if not found.
	 */

	public final File fileFor(final Parameter parameter) {
		final File result = _fileFor(parameter);
		uncheck();
		return result;
	}

	final synchronized File _fileFor(final Parameter parameter) {
		if (checked) {
			return null;
		}

		checked = true;
		File result = null;
		final String p = getProperty(parameter.param);
		if (p == null) {
			final int size = parents.size();
			for (int x = size - 1; x >= 0; x--) {
				result = (parents.elementAt(x))._fileFor(parameter);
				if (result != null) {
					return result;
				}
			}
			return result;
		} else {
			return new File(directory, filename);
		}
	}

	/** Removes a parameter from the topmost database. */
	public final synchronized void remove(final Parameter parameter) {
		if (parameter.param.equals(ParameterDatabase.PRINT_PARAMS)) {
			printState = ParameterDatabase.PS_UNKNOWN;
		}
		remove(parameter.param);
	}

	/** Removes a parameter from the database and all its parent databases. */
	public final synchronized void removeDeeply(final Parameter parameter) {
		_removeDeeply(parameter);
		uncheck();
	}

	/** Private helper function */
	synchronized void _removeDeeply(final Parameter parameter) {
		if (checked) {
			return; // already removed from this path
		}
		checked = true;
		remove(parameter);
		final int size = parents.size();
		for (int x = size - 1; x >= 0; x--) {
			(parents.elementAt(x)).removeDeeply(parameter);
		}
	}

	/** Creates an empty parameter database. */
	public ParameterDatabase() {
		super();
		accessed = new Hashtable<String, Boolean>();
		gotten = new Hashtable<String, Boolean>();
		try {
			directory = new File(new File("").getAbsolutePath()); // uses the user
		} catch (final Exception e) {
			e.printStackTrace();
		}
		// path
		filename = "";
		parents = new Vector<ParameterDatabase>();
		checked = false; // unnecessary
		listeners = new Vector<ParameterDatabaseListener>();
	}

	/** Creates a parameter database for command line arguments */
	public ParameterDatabase(final String args[]) {
		this();

		for (int x = 0; x < args.length - 1; x++) {
			if (args[x].equals("-p")) {
				parseParameter(args[x + 1]);
			}
		}
	}

	/**
	 * Creates a new parameter database from the given Dictionary. Both the keys
	 * and values will be run through toString() before adding to the dataase.
	 * Keys are parameters. Values are the values of the parameters. Beware that a
	 * ParameterDatabase is itself a Dictionary; but if you pass one in here you
	 * will only get the lowest-level elements. If parent.n are defined, parents
	 * will be attempted to be loaded -- that's the reason for the
	 * FileNotFoundException and IOException.
	 */
	public ParameterDatabase(final java.util.Dictionary<String, String> map)
			throws FileNotFoundException, IOException {
		this();
		final java.util.Enumeration<String> keys = map.keys();
		while (keys.hasMoreElements()) {
			final Object obj = keys.nextElement();
			set(new Parameter("" + obj), "" + map.get(obj));
		}

		// load parents
		for (int x = 0;; x++) {
			final String s = expand(getProperty("parent." + x));
			if (s == null) {
				return; // we're done
			}

			if (new File(s).isAbsolute()) {
				parents.addElement(new ParameterDatabase(new File(s)));
			} else {
				throw new FileNotFoundException(
						"Attempt to load a relative file, but there's no parent file: " + s);
			}
		}
	}

	static String prefix = "";

	public ParameterDatabase(final URL url) throws IOException {
		this();

		load(url.openStream());

		String path = url.toString();
		// System.out.println("path: " + path);
		path = path.substring(0, path.lastIndexOf("/") + 1);

		// load parents
		for (int x = 0;; x++) {
			final String s = expand(getProperty("parent." + x));
			if (s == null) {
				return; // we're done
			} else {
				parents.addElement(new ParameterDatabase(ParameterDatabase.getURL(path
						+ s)));
			}
		}
	}

	public ParameterDatabase(final URL url, final String args[])
			throws IOException {
		this();

		// Create the Parameter Database for the arguments
		final ParameterDatabase a = new ParameterDatabase(args);
		// Set me up
		parents.addElement(a);

		// Create the Parameter Database tree for the stream
		final ParameterDatabase b = new ParameterDatabase(url);
		a.parents.addElement(b);
	}

	/**
	 * Creates a new parameter database loaded from the given stream. If parent.n
	 * are defined, parents will be attempted to be loaded -- that's the reason
	 * for the FileNotFoundException and IOException.
	 */

	public ParameterDatabase(final java.io.InputStream stream)
			throws FileNotFoundException, IOException {
		this();
		load(stream);

		// listeners = new Vector();

		// load parents
		for (int x = 0;; x++) {
			final String s = expand(getProperty("parent." + x));
			if (s == null) {
				return; // we're done
			} else {
				parents.addElement(new ParameterDatabase(ParameterDatabase.getURL(s)
						.openStream()));
			}
		}
	}

	public ParameterDatabase(final java.io.InputStream stream,
			final String args[]) throws FileNotFoundException, IOException {
		this();

		// Create the Parameter Database for the arguments
		final ParameterDatabase a = new ParameterDatabase(args);
		// Set me up
		parents.addElement(a);

		// Create the Parameter Database tree for the stream
		final ParameterDatabase b = new ParameterDatabase(stream);
		a.parents.addElement(b);
	}

	/**
	 * Creates a new parameter database tree from a given database file and its
	 * parent files.
	 */
	public ParameterDatabase(final File file) throws FileNotFoundException,
			IOException {
		this();
		filename = file.getName();
		directory = new File(file.getParent()); // get the directory
		// filename is in
		load(new FileInputStream(file));

		// listeners = new Vector();

		// load parents
		for (int x = 0;; x++) {
			final String s = expand(getProperty("parent." + x));
			if (s == null) {
				return; // we're done
			}

			if (new File(s).isAbsolute()) {
				parents.addElement(new ParameterDatabase(new File(s)));
			} else {
				// it's relative to my path
				parents
						.addElement(new ParameterDatabase(new File(file.getParent(), s)));
			}
		}
	}

	/**
	 * Creates a new parameter database from a given database file and argv list.
	 * The top-level database is completely empty, pointing to a second database
	 * which contains the parameter entries stored in args, which points to a tree
	 * of databases constructed using ParameterDatabase(filename).
	 */

	public ParameterDatabase(final File file, final String[] args)
			throws FileNotFoundException, IOException {
		this();

		// Create the Parameter Database for the arguments
		final ParameterDatabase a = new ParameterDatabase(args);
		// Set me up
		parents.addElement(a);

		// this.filename = file.getName();
		// directory = new File(file.getParent()); // get the directory
		// filename is in

		// Create the Parameter Database tree for the files
		final ParameterDatabase files = new ParameterDatabase(file);
		a.parents.addElement(files);

		// listeners = new Vector();
	}

	public void addParent(final ParameterDatabase parent) {
		parents.addElement(parent);
	}

	public void removeParent(final ParameterDatabase parent) {
		parents.removeElement(parent);
	}

	public void removeParents() {
		parents.clear();
	}

	public Vector<ParameterDatabase> getParents() {
		return parents;
	}

	/**
	 * Parses and adds s to the database. Returns true if there was actually
	 * something to parse.
	 */
	final boolean parseParameter(String s) {
		s = s.trim();
		if (s.length() == 0) {
			return false;
		}
		if (s.charAt(0) == '#') {
			return false;
		}
		final int eq = s.indexOf('=');
		if (eq < 0) {
			return false;
		}
		put(s.substring(0, eq), s.substring(eq + 1));
		return true;
	}

	/**
	 * Prints out all the parameters in the database. Useful for debugging. If
	 * listShadowed is true, each parameter is printed with the parameter database
	 * it's located in. If listShadowed is false, only active parameters are
	 * listed, and they're all given in one big chunk.
	 */
	public final void list(final PrintStream p, final boolean listShadowed) {
		list(new PrintWriter(p), listShadowed);
	}

	/**
	 * Prints out all the parameters in the database, but not shadowed parameters.
	 */
	public final void list(final PrintStream p) {
		list(new PrintWriter(p), false);
	}

	/**
	 * Prints out all the parameters in the database, but not shadowed parameters.
	 */
	public final void list(final PrintWriter p) {
		list(p, false);
	}

	/**
	 * Prints out all the parameters in the database. Useful for debugging. If
	 * listShadowed is true, each parameter is printed with the parameter database
	 * it's located in. If listShadowed is false, only active parameters are
	 * listed, and they're all given in one big chunk.
	 */
	public final void list(final PrintWriter p, final boolean listShadowed) {
		if (listShadowed) {
			_list(p, listShadowed, "root");
		} else {
			final Map<String, String> gather = _list(p, listShadowed, "root");

			final String[] array = gather.keySet().toArray(new String[0]);

			Arrays.sort(array);

			// Uncheck and print each item
			for (final String s : array) {
				String v = null;
				if (s != null) {
					v = gather.get(s);
				}
				if (v == null) {
					v = ParameterDatabase.UNKNOWN_VALUE;
				}
				p.println(s + " = " + v);
			}
		}
		p.flush();
	}

	/** Private helper function. */
	final Map<String, String> _list(final PrintWriter p,
			final boolean listShadowed, final String prefix) {
		final Map<String, String> gather = new HashMap<String, String>();

		if (listShadowed) {
			// Print out my header
			p.println("\n########" + prefix);
			super.list(p);
			final int size = parents.size();
			for (int x = 0; x < size; x++) {
				gather.putAll((parents.elementAt(x))._list(p, listShadowed, prefix
						+ "." + x));
			}
		} else {
			// load in reverse order so things get properly overwritten
			final int size = parents.size();
			for (int x = 0; x < size; x++) {
				gather.putAll(parents.elementAt(x)._list(p, listShadowed, prefix));
			}
			// TODO:
			final Enumeration<Object> e = keys();
			while (e.hasMoreElements()) {
				final String key = (String) e.nextElement();
				gather.put(key, getProperty(key));
			}
		}
		p.flush();

		return gather;
	}

	public String toString() {
		String s = super.toString();
		if (parents.size() > 0) {
			s += " : (";
			for (int x = 0; x < parents.size(); x++) {
				if (x > 0) {
					s += ", ";
				}
				s += parents.elementAt(x);
			}
			s += ")";
		}
		return s;
	}

	/**
	 * Builds a TreeModel from the available property keys.
	 * 
	 * @return tree model
	 */
	public TreeModel buildTreeModel() {
		final String sep = System.getProperty("file.separator");
		final ParameterDatabaseTreeNode root = new ParameterDatabaseTreeNode(
				directory.getAbsolutePath() + sep + filename);
		final ParameterDatabaseTreeModel model = new ParameterDatabaseTreeModel(
				root);

		_buildTreeModel(model, root);

		model.sort(root, new Comparator<ParameterDatabaseTreeNode>() {
			@SuppressWarnings("unchecked")
			public int compare(final ParameterDatabaseTreeNode t1,
					final ParameterDatabaseTreeNode t2) {
				return ((Comparable) t1.getUserObject()).compareTo(t2.getUserObject());
			}
		});

		// In order to add elements to the tree model, the leaves need to be
		// visible. This is because some properties have values *and* sub-
		// properties. Otherwise, if the nodes representing these properties did
		// not yet have children, then they would be invisible and the tree model
		// would be unable to add child nodes to them.
		model.setVisibleLeaves(false);

		// addListener(new ParameterDatabaseAdapter() {
		// public void parameterSet(ParameterDatabaseEvent evt) {
		// model.setVisibleLeaves(true);
		// _addNodeForParameter(model, root, evt.getParameter().param);
		// model.setVisibleLeaves(false);
		// }
		// });

		return model;
	}

	void _buildTreeModel(final DefaultTreeModel model,
			final DefaultMutableTreeNode root) {
		final Enumeration<Object> e = keys();
		while (e.hasMoreElements()) {
			_addNodeForParameter(model, root, (String) e.nextElement());
		}

		final int size = parents.size();
		for (int x = size - 1; x >= 0; x--) {
			final ParameterDatabase parentDB = parents.elementAt(x);
			parentDB._buildTreeModel(model, root);
		}
	}

	/**
	 * @param model
	 * @param root
	 * @param e
	 */
	void _addNodeForParameter(final DefaultTreeModel model,
			final DefaultMutableTreeNode root, final String key) {
		if (key.indexOf("parent.") == -1) {
			/*
			 * TODO split is new to 1.4. To maintain 1.2 compatability we need to use
			 * a different approach. Just use a string tokenizer.
			 */
			final StringTokenizer tok = new StringTokenizer(key, ".");
			final String[] path = new String[tok.countTokens()];
			int t = 0;
			while (tok.hasMoreTokens()) {
				path[t++] = tok.nextToken();
			}
			DefaultMutableTreeNode parent = root;

			for (int i = 0; i < path.length; ++i) {
				final int children = model.getChildCount(parent);
				if (children > 0) {
					int c = 0;
					for (; c < children; ++c) {
						final DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent
								.getChildAt(c);
						if (child.getUserObject().equals(path[i])) {
							parent = child;
							break;
						}
					}

					if (c == children) {
						final DefaultMutableTreeNode child = new ParameterDatabaseTreeNode(
								path[i]);
						model.insertNodeInto(child, parent, parent.getChildCount());
						parent = child;
					}
				}
				// If the parent has no children, just add the node.
				else {
					final DefaultMutableTreeNode child = new ParameterDatabaseTreeNode(
							path[i]);
					model.insertNodeInto(child, parent, 0);
					parent = child;
				}
			}
		}
	}

	/** Test the ParameterDatabase */
	public static void main(final String[] args) throws FileNotFoundException,
			IOException {
		final ParameterDatabase pd = new ParameterDatabase(new File(args[0]), args);
		pd.set(new Parameter("Hi there"), "Whatever");
		pd.set(new Parameter(new String[] { "1", "2", "3" }), " Whatever ");
		pd.set(new Parameter(new String[] { "a", "b", "c" }).pop().push("d"),
				"Whatever");

		System.err.println("\n\n PRINTING ALL PARAMETERS \n\n");
		pd.list(new PrintWriter(System.err, true), true);
		System.err.println("\n\n PRINTING ONLY VALID PARAMETERS \n\n");
		pd.list(new PrintWriter(System.err, true), false);
	}

}
