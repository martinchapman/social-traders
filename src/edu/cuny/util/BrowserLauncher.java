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

package edu.cuny.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * provides utitlies to launch a web browser from a Java application on various
 * platforms, Windows, Mac, or Linux.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class BrowserLauncher {

	private static boolean loadClasses() {
		switch (BrowserLauncher.jvm) {
		default:
			break;

		case MRJ_2_0: // '\0'
			try {
				final Class<?> class1 = Class.forName("com.apple.MacOS.AETarget");
				// BrowserLauncher.macOSErrorClass = Class
				// .forName("com.apple.MacOS.MacOSError");
				final Class<?> class2 = Class.forName("com.apple.MacOS.OSUtils");
				final Class<?> class3 = Class.forName("com.apple.MacOS.AppleEvent");
				final Class<?> class4 = Class.forName("com.apple.MacOS.ae");
				BrowserLauncher.aeDescClass = Class.forName("com.apple.MacOS.AEDesc");
				BrowserLauncher.aeTargetConstructor = class1
						.getDeclaredConstructor(new Class[] { Integer.TYPE });
				BrowserLauncher.appleEventConstructor = class3
						.getDeclaredConstructor(new Class[] { Integer.TYPE, Integer.TYPE,
								class1, Integer.TYPE, Integer.TYPE });
				BrowserLauncher.aeDescConstructor = BrowserLauncher.aeDescClass
						.getDeclaredConstructor(new Class[] { java.lang.String.class });
				BrowserLauncher.makeOSType = class2.getDeclaredMethod("makeOSType",
						new Class[] { java.lang.String.class });
				BrowserLauncher.putParameter = class3.getDeclaredMethod("putParameter",
						new Class[] { Integer.TYPE, BrowserLauncher.aeDescClass });
				BrowserLauncher.sendNoReply = class3.getDeclaredMethod("sendNoReply",
						new Class[0]);
				final Field field1 = class4.getDeclaredField("keyDirectObject");
				BrowserLauncher.keyDirectObject = (Integer) field1.get(null);
				final Field field2 = class3.getDeclaredField("kAutoGenerateReturnID");
				BrowserLauncher.kAutoGenerateReturnID = (Integer) field2.get(null);
				final Field field3 = class3.getDeclaredField("kAnyTransactionID");
				BrowserLauncher.kAnyTransactionID = (Integer) field3.get(null);
				break;
			} catch (final ClassNotFoundException classnotfoundexception) {
				BrowserLauncher.errorMessage = classnotfoundexception.getMessage();
				return false;
			} catch (final NoSuchMethodException nosuchmethodexception) {
				BrowserLauncher.errorMessage = nosuchmethodexception.getMessage();
				return false;
			} catch (final NoSuchFieldException nosuchfieldexception1) {
				BrowserLauncher.errorMessage = nosuchfieldexception1.getMessage();
				return false;
			} catch (final IllegalAccessException illegalaccessexception) {
				BrowserLauncher.errorMessage = illegalaccessexception.getMessage();
			}
			return false;

		case MRJ_2_1: // '\001'
			try {
				BrowserLauncher.mrjFileUtilsClass = Class
						.forName("com.apple.mrj.MRJFileUtils");
				BrowserLauncher.mrjOSTypeClass = Class
						.forName("com.apple.mrj.MRJOSType");
				final Field field = BrowserLauncher.mrjFileUtilsClass
						.getDeclaredField("kSystemFolderType");
				BrowserLauncher.kSystemFolderType = field.get(null);
				BrowserLauncher.findFolder = BrowserLauncher.mrjFileUtilsClass
						.getDeclaredMethod("findFolder",
								new Class[] { BrowserLauncher.mrjOSTypeClass });
				BrowserLauncher.getFileType = BrowserLauncher.mrjFileUtilsClass
						.getDeclaredMethod("getFileType",
								new Class[] { java.io.File.class });
				break;
			} catch (final ClassNotFoundException classnotfoundexception1) {
				BrowserLauncher.errorMessage = classnotfoundexception1.getMessage();
				return false;
			} catch (final NoSuchFieldException nosuchfieldexception) {
				BrowserLauncher.errorMessage = nosuchfieldexception.getMessage();
				return false;
			} catch (final NoSuchMethodException nosuchmethodexception1) {
				BrowserLauncher.errorMessage = nosuchmethodexception1.getMessage();
				return false;
			} catch (final SecurityException securityexception) {
				BrowserLauncher.errorMessage = securityexception.getMessage();
				return false;
			} catch (final IllegalAccessException illegalaccessexception1) {
				BrowserLauncher.errorMessage = illegalaccessexception1.getMessage();
			}
			return false;

		case MRJ_2_2: // '\002'
			try {
				BrowserLauncher.mrjFileUtilsClass = Class
						.forName("com.apple.mrj.MRJFileUtils");
				break;
			} catch (final Exception exception) {
				BrowserLauncher.errorMessage = exception.getMessage();
			}
			return false;
		}
		return true;
	}

	private static Object locateBrowser() {
		if (BrowserLauncher.browser != null) {
			return BrowserLauncher.browser;
		}
		switch (BrowserLauncher.jvm) {
		case MRJ_2_0: // '\0'
			try {
				final Integer integer = (Integer) BrowserLauncher.makeOSType.invoke(
						null, new Object[] { BrowserLauncher.FINDER_CREATOR });
				final Object obj = BrowserLauncher.aeTargetConstructor
						.newInstance(new Object[] { integer });
				final Integer integer1 = (Integer) BrowserLauncher.makeOSType.invoke(
						null, new Object[] { BrowserLauncher.GURL_EVENT });
				final Object obj1 = BrowserLauncher.appleEventConstructor
						.newInstance(new Object[] { integer1, integer1, obj,
								BrowserLauncher.kAutoGenerateReturnID,
								BrowserLauncher.kAnyTransactionID });
				return obj1;
			} catch (final IllegalAccessException illegalaccessexception) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = illegalaccessexception.getMessage();
				return BrowserLauncher.browser;
			} catch (final InstantiationException instantiationexception) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = instantiationexception.getMessage();
				return BrowserLauncher.browser;
			} catch (final InvocationTargetException invocationtargetexception) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = invocationtargetexception.getMessage();
				return BrowserLauncher.browser;
			}

		case MRJ_2_1: // '\001'
			File file;
			try {
				file = (File) BrowserLauncher.findFolder.invoke(null,
						new Object[] { BrowserLauncher.kSystemFolderType });
			} catch (final IllegalArgumentException illegalargumentexception) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = illegalargumentexception.getMessage();
				return BrowserLauncher.browser;
			} catch (final IllegalAccessException illegalaccessexception1) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = illegalaccessexception1.getMessage();
				return BrowserLauncher.browser;
			} catch (final InvocationTargetException invocationtargetexception1) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = invocationtargetexception1
						.getTargetException().getClass()
						+ ": "
						+ invocationtargetexception1.getTargetException().getMessage();
				return BrowserLauncher.browser;
			}
			final String as[] = file.list();
			for (final String element : as) {
				try {
					final File file1 = new File(file, element);
					if (file1.isFile()) {
						final Object obj2 = BrowserLauncher.getFileType.invoke(null,
								new Object[] { file1 });
						if (BrowserLauncher.FINDER_TYPE.equals(obj2.toString())) {
							BrowserLauncher.browser = file1.toString();
							return BrowserLauncher.browser;
						}
					}
				} catch (final IllegalArgumentException illegalargumentexception1) {
					BrowserLauncher.browser = null;
					BrowserLauncher.errorMessage = illegalargumentexception1.getMessage();
					return null;
				} catch (final IllegalAccessException illegalaccessexception2) {
					BrowserLauncher.browser = null;
					BrowserLauncher.errorMessage = illegalaccessexception2.getMessage();
					return BrowserLauncher.browser;
				} catch (final InvocationTargetException invocationtargetexception2) {
					BrowserLauncher.browser = null;
					BrowserLauncher.errorMessage = invocationtargetexception2
							.getTargetException().getClass()
							+ ": "
							+ invocationtargetexception2.getTargetException().getMessage();
					return BrowserLauncher.browser;
				}
			}

			break;

		case MRJ_2_2: // '\002'
			try {
				final String s = "openURL";
				final Class<?> aclass[] = { s.getClass() };
				final Method method = BrowserLauncher.mrjFileUtilsClass
						.getDeclaredMethod(s, aclass);
				return method;
			} catch (final Exception exception) {
				BrowserLauncher.browser = null;
				BrowserLauncher.errorMessage = exception.getMessage();
				return BrowserLauncher.browser;
			}

		case WINDOWS_NT: // '\003'
			BrowserLauncher.browser = "cmd.exe";
			break;

		case WINDOWS_9x: // '\004'
			BrowserLauncher.browser = "command.com";
			break;

		case WINDOWS_ME: // '\005'
			BrowserLauncher.browser = "command.com";
			break;

		case OTHER:
		default:
			BrowserLauncher.browser = "netscape";
			break;
		}
		return BrowserLauncher.browser;
	}

	/**
	 * launches a web browser and opens a URL.
	 * 
	 * @param s
	 *          the URL.
	 * @throws IOException
	 */
	public static void openURL(final String s) throws IOException {
		if (!BrowserLauncher.loadedWithoutErrors) {
			throw new IOException("Exception in finding browser: "
					+ BrowserLauncher.errorMessage);
		}
		Object obj = BrowserLauncher.locateBrowser();
		if (obj == null) {
			throw new IOException("Unable to locate browser: "
					+ BrowserLauncher.errorMessage);
		}
		switch (BrowserLauncher.jvm) {
		case MRJ_2_0: // '\0'
			// final Object obj1 = null;
			try {
				try {
					final Object obj2 = BrowserLauncher.aeDescConstructor
							.newInstance(new Object[] { s });
					BrowserLauncher.putParameter.invoke(obj, new Object[] {
							BrowserLauncher.keyDirectObject, obj2 });
					BrowserLauncher.sendNoReply.invoke(obj, new Object[0]);
				} catch (final InvocationTargetException invocationtargetexception) {
					throw new IOException(
							"InvocationTargetException while creating AEDesc: "
									+ invocationtargetexception.getMessage());
				} catch (final IllegalAccessException illegalaccessexception) {
					throw new IOException(
							"IllegalAccessException while building AppleEvent: "
									+ illegalaccessexception.getMessage());
				} catch (final InstantiationException instantiationexception) {
					throw new IOException(
							"InstantiationException while creating AEDesc: "
									+ instantiationexception.getMessage());
				}
				break;
			} finally {
				obj = null;
			}

		case MRJ_2_1: // '\001'
			Runtime.getRuntime().exec(new String[] { (String) obj, s });
			break;

		case MRJ_2_2: // '\002'
			try {
				((Method) obj).invoke(null, new Object[] { s });
			} catch (final InvocationTargetException invocationtargetexception1) {
				throw new IOException(
						"InvocationTargetException while creating openURL: "
								+ invocationtargetexception1.getMessage());
			} catch (final IllegalAccessException illegalaccessexception1) {
				throw new IOException("IllegalAccessException while building openURL: "
						+ illegalaccessexception1.getMessage());
			}
			break;

		case WINDOWS_NT: // '\003'
		case WINDOWS_9x: // '\004'
		case WINDOWS_ME: // '\005'
			Runtime.getRuntime().exec(
					new String[] { (String) obj, BrowserLauncher.FIRST_WINDOWS_PARAMETER,
							BrowserLauncher.SECOND_WINDOWS_PARAMETER, s });
			break;

		case OTHER:
			final String as[] = { "netscape", "mozilla", "konqueror", "galeon" };
			int i;
			for (i = 0; i < as.length;) {
				try {
					Runtime.getRuntime().exec(new String[] { as[i], s });
					break;
				} catch (final IOException ioexception) {
					i++;
				}
			}

			if (i != as.length) {
				break;
			}
			String s1 = "";
			for (int j = 0; j < as.length; j++) {
				if (j == as.length - 1) {
					s1 = s1 + "or " + as[j];
				} else {
					s1 = s1 + as[j] + ", ";
				}
			}

			throw new IOException(s1);

		default:
			Runtime.getRuntime().exec(new String[] { (String) obj, s });
			break;
		}
	}

	private static int jvm;

	private static Object browser;

	private static boolean loadedWithoutErrors;

	private static Class<?> mrjFileUtilsClass;

	private static Class<?> mrjOSTypeClass;

	// private static Class macOSErrorClass;

	private static Class<?> aeDescClass;

	private static Constructor<?> aeTargetConstructor;

	private static Constructor<?> appleEventConstructor;

	private static Constructor<?> aeDescConstructor;

	private static Method findFolder;

	private static Method getFileType;

	private static Method makeOSType;

	private static Method putParameter;

	private static Method sendNoReply;

	private static Object kSystemFolderType;

	private static Integer keyDirectObject;

	private static Integer kAutoGenerateReturnID;

	private static Integer kAnyTransactionID;

	private static final int MRJ_2_0 = 0;

	private static final int MRJ_2_1 = 1;

	private static final int MRJ_2_2 = 2;

	private static final int WINDOWS_NT = 3;

	private static final int WINDOWS_9x = 4;

	private static final int WINDOWS_ME = 5;

	private static final int OTHER = -1;

	private static final String FINDER_TYPE = "FNDR";

	private static final String FINDER_CREATOR = "MACS";

	private static final String GURL_EVENT = "GURL";

	private static final String FIRST_WINDOWS_PARAMETER = "/c";

	private static final String SECOND_WINDOWS_PARAMETER = "start";

	private static String errorMessage;

	static {
		BrowserLauncher.loadedWithoutErrors = true;
		final String s = System.getProperty("os.name");
		if ("Mac OS".equals(s)) {
			final String s1 = System.getProperty("mrj.version");
			final String s2 = s1.substring(0, 3);
			try {
				final double d = Double.valueOf(s2).doubleValue();
				if (d == 2D) {
					BrowserLauncher.jvm = BrowserLauncher.MRJ_2_0;
				} else if (d == 2.1000000000000001D) {
					BrowserLauncher.jvm = BrowserLauncher.MRJ_2_1;
				} else if (d >= 2.2000000000000002D) {
					BrowserLauncher.jvm = BrowserLauncher.MRJ_2_2;
				} else {
					BrowserLauncher.loadedWithoutErrors = false;
					BrowserLauncher.errorMessage = "Unsupported MRJ version: " + d;
				}
			} catch (final NumberFormatException numberformatexception) {
				BrowserLauncher.loadedWithoutErrors = false;
				BrowserLauncher.errorMessage = "Invalid MRJ version: " + s1;
			}
		} else if (s.startsWith("Windows")) {
			if (s.indexOf("9") != -1) {
				BrowserLauncher.jvm = BrowserLauncher.WINDOWS_9x;
			} else if ((s.indexOf("MI") != -1) || (s.indexOf("Me") != -1)
					|| (s.indexOf("ME") != -1) || (s.indexOf("Mi") != -1)) {
				BrowserLauncher.jvm = BrowserLauncher.WINDOWS_ME;
			} else {
				BrowserLauncher.jvm = BrowserLauncher.WINDOWS_NT;
			}
		} else {
			BrowserLauncher.jvm = BrowserLauncher.OTHER;
		}

		if (BrowserLauncher.loadedWithoutErrors) {
			BrowserLauncher.loadedWithoutErrors = BrowserLauncher.loadClasses();
		}
	}
}
