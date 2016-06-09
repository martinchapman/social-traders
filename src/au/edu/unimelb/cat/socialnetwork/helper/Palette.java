package au.edu.unimelb.cat.socialnetwork.helper;

import java.awt.*;

/**
 * 
 * @author Guan Gui
 * @version $Rev: 97 $ $LastChangedBy: Guan Gui $ $LastChangedDate: 2010-02-28
 *          21:19:07 +1100 (Sun, 28 Feb 2010) $
 */
public class Palette {

	public static int rgb[][] = { { 0x00, 0x00, 0x00 }, // 0 Black
			{ 0xff, 0xff, 0xff }, // 1 White
			{ 0xff, 0x00, 0x00 }, // 2 Red
			{ 0xff, 0xff, 0x00 }, // 3 Bright Yellow
			{ 0xaa, 0xaa, 0xff }, // 4 Medium Blue
			{ 0x00, 0xff, 0x00 }, // 5 Bright green
			{ 0xff, 0x88, 0x00 }, // 6 Orange
			{ 0xff, 0x00, 0xff }, // 7 Purple
			{ 0x66, 0x66, 0x66 }, // 8 Dk Grey
			{ 0xaa, 0xaa, 0xaa }, // 9 Lt Grey
			{ 0x88, 0x00, 0x00 }, // 10 Dk Red
			{ 0xcc, 0xaa, 0x00 }, // 11 Dk Yellow
			{ 0x00, 0x44, 0x89 }, // 12 Dk Blue
			{ 0x00, 0x77, 0x00 }, // 13 Dk Green
			{ 0x00, 0xcc, 0xff }, // 14 Bri Blue
			{ 0x77, 0x44, 0x00 }, // 15 Brown
			// -------
			{ 177, 216, 254 }, // 16 Skyish blue
			{ 0xff, 0xff, 0xff }, // 17 White
			{ 0xff, 0x00, 0x00 }, // 18 Red
			{ 0xff, 0xff, 0x00 }, // 19 Bright Yellow
			{ 0x00, 0xaa, 0xff }, // 20 Medium Blue
			{ 0x00, 0xff, 0x00 }, // 21 Bright green
			{ 0xff, 0x88, 0x00 }, // 22 Orange
			{ 0xff, 0x00, 0xff }, // 23 Purple
			{ 0x66, 0x66, 0x66 }, // 24 Dk Grey
			{ 0xaa, 0xaa, 0xaa }, // 25 Lt Grey
			{ 0x88, 0x00, 0x00 }, // 26 Dk Red
			{ 0xcc, 0xaa, 0x00 }, // 27 Dk Yellow
			{ 0x00, 0x44, 0x88 }, // 28 Dk Blue
			{ 0x00, 0x77, 0x00 }, // 29 Dk Green
			{ 0x00, 0xcc, 0xff }, // 30 Bri Blue
			{ 0x77, 0x44, 0x00 }, // 31 Brown
			// ----------- ** Greys
			{ 0x77, 0x44, 0x00 }, // 32 Brown
			{ 0x11, 0x11, 0x11 }, // 33 **
			{ 0x22, 0x22, 0x22 }, // 34 White
			{ 0x33, 0x33, 0x33 }, // 35 Red
			{ 0x44, 0x44, 0x44 }, // 36 Bright Yellow
			{ 0x55, 0x55, 0x55 }, // 37 Medium Blue
			{ 0x66, 0x66, 0x66 }, // 38 Bright green
			{ 0x77, 0x77, 0x77 }, // 39 Orange
			{ 0x88, 0x88, 0x88 }, // 40 Purple
			{ 0x99, 0x99, 0x99 }, // 41 Dk Grey
			{ 0xaa, 0xaa, 0xaa }, // 42 Lt Grey
			{ 0xbb, 0xbb, 0xbb }, // 43 Dk Red
			{ 0xcc, 0xcc, 0xcc }, // 44 Dk Yellow
			{ 0xdd, 0xdd, 0xdd }, // 45 Dk Blue
			{ 0xee, 0xee, 0xee }, // 46 Dk Green
			{ 0xff, 0xff, 0xff }, // 47 White
			// shades of Blue..
			{ 0x77, 0x44, 0x01 }, // 48
			{ 0x00, 0x00, 0x02 }, // 49
			{ 0xff, 0xff, 0x03 }, // 50
			{ 0xff, 0x00, 0x04 }, // 51
			{ 0xff, 0xff, 0x05 }, // 52
			{ 0x00, 0xaa, 0x06 }, // 53
			{ 0x00, 0xff, 0x07 }, // 54
			{ 0xff, 0x88, 0x08 }, // 55
			{ 0xff, 0x00, 0xa9 }, // 56
			{ 0x66, 0x66, 0x0a }, // 57
			{ 0xaa, 0xaa, 0x0e }, // 58
			{ 0x88, 0x00, 0xd0 }, // 59
			{ 0xcc, 0xaa, 0xe0 }, // 60
			{ 0x00, 0x44, 0xf0 }, // 61
			{ 0x00, 0x77, 0xfa }, // 62
			{ 0x00, 0xcc, 0xff } // 63
	};

	static Color palette0 = new Color(rgb[0][0], rgb[0][1], rgb[0][2]);
	static Color palette1 = new Color(rgb[1][0], rgb[1][1], rgb[1][2]);
	static Color palette2 = new Color(rgb[2][0], rgb[2][1], rgb[2][2]);
	static Color palette3 = new Color(rgb[3][0], rgb[3][1], rgb[3][2]);
	static Color palette4 = new Color(rgb[4][0], rgb[4][1], rgb[4][2]);
	static Color palette5 = new Color(rgb[5][0], rgb[5][1], rgb[5][2]);
	static Color palette6 = new Color(rgb[6][0], rgb[6][1], rgb[6][2]);
	static Color palette7 = new Color(rgb[7][0], rgb[7][1], rgb[7][2]);
	static Color palette8 = new Color(rgb[8][0], rgb[8][1], rgb[8][2]);
	static Color palette9 = new Color(rgb[9][0], rgb[9][1], rgb[9][2]);
	static Color palette10 = new Color(rgb[10][0], rgb[10][1], rgb[10][2]);
	static Color palette11 = new Color(rgb[11][0], rgb[11][1], rgb[11][2]);
	static Color palette12 = new Color(rgb[12][0], rgb[12][1], rgb[12][2]);
	static Color palette13 = new Color(rgb[13][0], rgb[13][1], rgb[13][2]);
	static Color palette14 = new Color(rgb[14][0], rgb[14][1], rgb[14][2]);
	static Color palette15 = new Color(rgb[15][0], rgb[15][1], rgb[15][2]);

	static Color palette16 = new Color(rgb[16][0], rgb[16][1], rgb[16][2]);
	static Color palette17 = new Color(rgb[17][0], rgb[17][1], rgb[17][2]);
	static Color palette18 = new Color(rgb[18][0], rgb[18][1], rgb[18][2]);
	static Color palette19 = new Color(rgb[19][0], rgb[19][1], rgb[19][2]);
	static Color palette20 = new Color(rgb[20][0], rgb[20][1], rgb[20][2]);
	static Color palette21 = new Color(rgb[21][0], rgb[21][1], rgb[21][2]);
	static Color palette22 = new Color(rgb[22][0], rgb[22][1], rgb[22][2]);
	static Color palette23 = new Color(rgb[23][0], rgb[23][1], rgb[23][2]);
	static Color palette24 = new Color(rgb[24][0], rgb[24][1], rgb[24][2]);
	static Color palette25 = new Color(rgb[25][0], rgb[25][1], rgb[25][2]);
	static Color palette26 = new Color(rgb[26][0], rgb[26][1], rgb[26][2]);
	static Color palette27 = new Color(rgb[27][0], rgb[27][1], rgb[27][2]);
	static Color palette28 = new Color(rgb[28][0], rgb[28][1], rgb[28][2]);
	static Color palette29 = new Color(rgb[29][0], rgb[29][1], rgb[29][2]);
	static Color palette30 = new Color(rgb[30][0], rgb[30][1], rgb[30][2]);
	static Color palette31 = new Color(rgb[31][0], rgb[31][1], rgb[31][2]);

	static Color palette32 = new Color(rgb[32][0], rgb[32][1], rgb[32][2]);
	static Color palette33 = new Color(rgb[33][0], rgb[33][1], rgb[33][2]);
	static Color palette34 = new Color(rgb[34][0], rgb[34][1], rgb[34][2]);
	static Color palette35 = new Color(rgb[35][0], rgb[35][1], rgb[35][2]);
	static Color palette36 = new Color(rgb[36][0], rgb[36][1], rgb[36][2]);
	static Color palette37 = new Color(rgb[37][0], rgb[37][1], rgb[37][2]);
	static Color palette38 = new Color(rgb[38][0], rgb[38][1], rgb[38][2]);
	static Color palette39 = new Color(rgb[39][0], rgb[39][1], rgb[39][2]);
	static Color palette40 = new Color(rgb[40][0], rgb[40][1], rgb[40][2]);
	static Color palette41 = new Color(rgb[41][0], rgb[41][1], rgb[41][2]);
	static Color palette42 = new Color(rgb[42][0], rgb[42][1], rgb[42][2]);
	static Color palette43 = new Color(rgb[43][0], rgb[43][1], rgb[43][2]);
	static Color palette44 = new Color(rgb[44][0], rgb[44][1], rgb[44][2]);
	static Color palette45 = new Color(rgb[45][0], rgb[45][1], rgb[45][2]);
	static Color palette46 = new Color(rgb[46][0], rgb[46][1], rgb[46][2]);
	static Color palette47 = new Color(rgb[47][0], rgb[47][1], rgb[47][2]);

	static Color palette48 = new Color(rgb[48][0], rgb[48][1], rgb[48][2]);
	static Color palette49 = new Color(rgb[49][0], rgb[49][1], rgb[49][2]);
	static Color palette50 = new Color(rgb[50][0], rgb[50][1], rgb[50][2]);
	static Color palette51 = new Color(rgb[51][0], rgb[51][1], rgb[51][2]);
	static Color palette52 = new Color(rgb[52][0], rgb[52][1], rgb[52][2]);
	static Color palette53 = new Color(rgb[53][0], rgb[53][1], rgb[53][2]);
	static Color palette54 = new Color(rgb[54][0], rgb[54][1], rgb[54][2]);
	static Color palette55 = new Color(rgb[55][0], rgb[55][1], rgb[55][2]);
	static Color palette56 = new Color(rgb[56][0], rgb[56][1], rgb[56][2]);
	static Color palette57 = new Color(rgb[57][0], rgb[57][1], rgb[57][2]);
	static Color palette58 = new Color(rgb[58][0], rgb[58][1], rgb[58][2]);
	static Color palette59 = new Color(rgb[59][0], rgb[59][1], rgb[59][2]);
	static Color palette60 = new Color(rgb[60][0], rgb[60][1], rgb[60][2]);
	static Color palette61 = new Color(rgb[61][0], rgb[61][1], rgb[61][2]);
	static Color palette62 = new Color(rgb[62][0], rgb[62][1], rgb[62][2]);
	static Color palette63 = new Color(rgb[63][0], rgb[63][1], rgb[63][2]);

	public static Color palette[] = { palette0, palette1, palette2, palette3,
			palette4, palette5, palette6, palette7, palette8, palette9,
			palette10, palette11, palette12, palette13, palette14, palette15,
			palette16, palette17, palette18, palette19, palette20, palette21,
			palette22, palette23, palette24, palette25, palette26, palette27,
			palette28, palette29, palette30, palette31, palette32, palette33,
			palette34, palette35, palette36, palette37, palette38, palette39,
			palette40, palette41, palette42, palette43, palette44, palette45,
			palette46, palette47, palette48, palette49, palette50, palette51,
			palette52, palette53, palette54, palette55, palette56, palette57,
			palette58, palette59, palette60, palette61, palette62, palette63 };
}
