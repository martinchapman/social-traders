package au.edu.unimelb.cat.socialnetwork.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.apache.commons.collections15.Transformer;

import cern.jet.random.Uniform;
import edu.cuny.cat.Game;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 121 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:19:07 +1100 (Sun, 28 Feb 2010) $
 */
public class Utils {

	/**
	 * Return the suffix of the passed file name.
	 * 
	 * @param fileName
	 *            File name to retrieve suffix for.
	 * 
	 * @return Suffix for <TT>fileName</TT> or an empty string if unable to get
	 *         the suffix.
	 * 
	 * @throws IllegalArgumentException
	 *             if <TT>null</TT> file name passed.
	 */
	public static String getFileNameSuffix(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException("file name == null");
		}
		int pos = fileName.lastIndexOf('.');
		if (pos > 0 && pos < fileName.length() - 1) {
			return fileName.substring(pos + 1);
		}
		return "";
	}

	private static Uniform matrixDist;

	public static int[][] randomMatrix(int size) {

		int[][] matrix = new int[size][size];
		int curRow = 0;
		matrixDist = new Uniform(0, 1, Galaxy.getInstance()
					.getTyped(Game.P_CAT, GlobalPRNG.class).getEngine());

		while (curRow < size) {

			for (int curCol = 0; curCol < matrix.length; curCol++) {
				
				matrix[curRow][curCol] = matrixDist.nextInt();

			}

			curRow++;
		}

		return matrix;

	}

	public static int[][] readADMFile(String path) throws Exception {
		// get matrix size
		Scanner sc = new Scanner(new File(path));
		Scanner scl = new Scanner(sc.nextLine());
		int size = 0;
		while (scl.hasNext()) {
			try {
				Integer.parseInt(scl.next());
				size++;
			} catch (NumberFormatException nfe) {
				if (scl.hasNext()) {
					throw new RuntimeException("Incorect matrix first line!");
				}
			}
		}
		scl.close();

		sc.close();
		sc = new Scanner(new File(path));
		int[][] matrix = new int[size][size];
		int curRow = 0;
		// get matrix entries
		while (curRow < size && sc.hasNextLine()) {
			scl = new Scanner(sc.nextLine());

			for (int curCol = 0; curCol < matrix.length; curCol++) {
				try {
					matrix[curRow][curCol] = Integer.parseInt(scl.next());
				} catch (NumberFormatException nfe) {
					throw new RuntimeException(String.format(
							"Corrupted matrix entry (%d, %d)!", curRow + 1,
							curCol + 1));
				}
			}

			scl.close();
			curRow++;
		}
		sc.close();

		return matrix;
	}

	public static int[][] readADLFile(String path) throws Exception {
		// get matrix size
		Scanner sc = new Scanner(new File(path));
		int size = 0;

		while (sc.hasNextLine()) {
			sc.nextLine();
			size++;
		}

		sc.close();
		sc = new Scanner(new File(path));
		int[][] matrix = new int[size][size];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				matrix[i][j] = 0;
			}
		}

		int curRow = 0;
		// construct adjacency matrix
		while (sc.hasNextLine()) {
			Scanner scl = new Scanner(sc.nextLine());

			while (scl.hasNext()) {
				int tmp = Integer.parseInt(scl.next());
				matrix[curRow][tmp] = 1;
			}

			scl.close();
			curRow++;
		}
		sc.close();

		return matrix;
	}

	public static void print2DIntArray(int[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void print2DIntArray(
			AtomicReferenceArray<AtomicIntegerArray> array) {
		for (int i = 0; i < array.length(); i++) {
			for (int j = 0; j < array.get(i).length(); j++) {
				System.out.print(array.get(i).get(j) + " ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws Exception {
		print2DIntArray(readADMFile("/Users/guiguan/Desktop/5-5.net.adm"));
	}

	private static Uniform dist;

	public static String[] shuffleIds(String[] ids) {
		if (dist == null) {
			dist = new Uniform(0, ids.length - 1, Galaxy.getInstance()
					.getTyped(Game.P_CAT, GlobalPRNG.class).getEngine());
		}

		for (int i = 0; i < ids.length; i++) {
			int randomPosition = dist.nextInt();
			String temp = ids[i];
			ids[i] = ids[randomPosition];
			ids[randomPosition] = temp;
		}

		return ids;
	}

	public static void saveMatrixAsADM(
			AtomicReferenceArray<AtomicIntegerArray> matrix, String path) {
		PrintWriter pw;

		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < matrix.length(); i++) {
			for (int j = 0; j < matrix.length(); j++) {
				pw.print(matrix.get(i).get(j) + " ");
			}
			pw.println();
		}

		pw.close();
	}

	public static void saveMatrixAsADL(
			AtomicReferenceArray<AtomicIntegerArray> matrix, String path) {
		PrintWriter pw;

		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < matrix.length(); i++) {
			for (int j = 0; j < matrix.length(); j++) {
				if (matrix.get(i).get(j) == 1) {
					pw.print(j + " ");
				}
			}
			pw.println();
		}

		pw.close();
	}

	public static void saveMatrixAsCSV(
			AtomicReferenceArray<AtomicIntegerArray> matrix, String path) {
		PrintWriter pw;

		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < matrix.length(); i++) {
			for (int j = 0; j < matrix.length(); j++) {
				pw.print(matrix.get(i).get(j));
				if (j < matrix.length() - 1)
					pw.print(", ");
			}
			pw.println();
		}

		pw.close();
	}
}
