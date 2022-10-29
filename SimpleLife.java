/* Simple Life - Simple command-line version of Conway's Game of Life (B3/S23).
 * Copyright (C) 2022 Carlos Rica ( jasampler AT gmail DOT com )
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see https://www.gnu.org/licenses/ */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Simple and inefficient implementation of the Conway's Game of Life (B3/S23).
 *
 * <UL>
 * <LI>Any dead cell with 3 neighbors will born, otherwise remains dead.</LI>
 * <LI>Any alive cell with 2 or 3 neighbors will survive, otherwise dies.</LI>
 * </UL>
 *
 * <P><B>Note:</B> Currently does not allow to change the rule because it
 * requires to re-think how to calculate the state of the outside cells.</P> */
public class SimpleLife {

	private final int born;
	private final int survive;
	private final List<Row> negRows;
	private final List<Row> posRows;
	private int firstColumn = 0;
	private int lastColumnPlusOne = 0;

	/** Creates the game object with the standard rule B3/S23. */
	public SimpleLife() {
		born = 1 << 3;
		survive = (1 << 2) | (1 << 3);
		negRows = new ArrayList<>();
		posRows = new ArrayList<>();
	}

	/** Internal class to store a row of the board. */
	private static class Row {
		private BitSet negCells;
		private BitSet posCells;

		public boolean get(int colIdx) {
			if (colIdx < 0) {
				if (negCells == null) {
					negCells = new BitSet();
				}
				return negCells.get(-(colIdx + 1));
			}
			if (posCells == null) {
				posCells = new BitSet();
			}
			return posCells.get(colIdx);
		}

		public void set(int colIdx, boolean value) {
			if (colIdx < 0) {
				if (negCells == null) {
					negCells = new BitSet();
				}
				negCells.set(-(colIdx + 1), value);
				return;
			}
			if (posCells == null) {
				posCells = new BitSet();
			}
			posCells.set(colIdx, value);
		}

		public void clear() {
			if (negCells != null) {
				negCells.clear();
			}
			if (posCells != null) {
				posCells.clear();
			}
		}

	}

	/** Gets the content of a cell of the board. */
	public boolean get(int rowIdx, int colIdx) {
		int i;
		List<Row> rows;
		if (rowIdx < 0) {
			i = -(rowIdx + 1);
			rows = negRows;
		} else {
			i = rowIdx;
			rows = posRows;
		}
		if (i >= rows.size()) {
			return false;
		}
		Row row = rows.get(i);
		if (row == null) {
			return false;
		}
		return row.get(colIdx);
	}

	/** Sets the content of a cell of the board to the given value. */
	public void set(int rowIdx, int colIdx, boolean value) {
		int i;
		List<Row> rows;
		if (rowIdx < 0) {
			i = -(rowIdx + 1);
			rows = negRows;
		} else {
			i = rowIdx;
			rows = posRows;
		}
		if (value || i < rows.size()) {
			while (i >= rows.size()) {
				rows.add(null);
			}
			Row row = rows.get(i);
			if (row == null) {
				row = new Row();
				rows.set(i, row);
			}
			row.set(colIdx, value);
			if (value) { //updates the first and last columns:
				if (colIdx < firstColumn) {
					firstColumn = colIdx;
				}
				if (colIdx >= lastColumnPlusOne) {
					lastColumnPlusOne = colIdx + 1;
				}
			}
		}
	}

	/** Returns the state of the given cell in the next generation. */
	public boolean next(int i, int j) {
		int neighbours =  (get(i - 1, j - 1) ? 1 : 0)
				+ (get(i - 1, j    ) ? 1 : 0)
				+ (get(i - 1, j + 1) ? 1 : 0)
				+ (get(i,     j - 1) ? 1 : 0)
				+ (get(i,     j + 1) ? 1 : 0)
				+ (get(i + 1, j - 1) ? 1 : 0)
				+ (get(i + 1, j   )  ? 1 : 0)
				+ (get(i + 1, j + 1) ? 1 : 0);
		if (get(i, j)) {
			return 0 != (survive & (1 << neighbours));
		}
		return 0 != (born & (1 << neighbours));
	}

	/** Updates the entire board one generation. */
	public void next() {
		int firstRow = firstRow() - 1; //sets the previous to the first
		int lastRow1 = lastRowPlusOne() + 1; //sets the next to the last
		int firstCol = firstCol() - 1;
		int lastCol1 = lastColPlusOne() + 1;
		//two temporary rows
		Row[] next = new Row[] {new Row(), new Row()};
		boolean order = false;
		int i;
		for (i = firstRow; i < lastRow1; i++) {
			copyNextRow(i, next[order ? 1 : 0], firstCol, lastCol1);
			setRow(i - 1, next[order ? 0 : 1], firstCol, lastCol1);
			order = ! order;
		}
		//sets the next to the last:
		setRow(i - 1, next[order ? 0 : 1], firstCol, lastCol1);
	}

	private void copyNextRow(int i, Row row, int firstCol, int lastCol1) {
		for (int j = firstCol; j < lastCol1; j++) {
			row.set(j, next(i, j));
		}
	}

	private void setRow(int i, Row row, int firstCol, int lastCol1) {
		for (int j = firstCol; j < lastCol1; j++) {
			set(i, j, row.get(j));
		}
	}

	/** Returns the index of the first stored row of the board. */
	public int firstRow() {
		return -negRows.size();
	}

	/** Returns the index of the last stored row of the board plus one. */
	public int lastRowPlusOne() {
		return posRows.size();
	}

	/** Returns the index of the first stored column of the board. */
	public int firstCol() {
		return firstColumn;
	}

	/** Returns the index of the last stored column of the board plus one.*/
	public int lastColPlusOne() {
		return lastColumnPlusOne;
	}

	/** Empties the entire board. */
	public void clear() {
		for (Row row : negRows) {
			if (row != null) {
				row.clear();
			}
		}
		for (Row row : posRows) {
			if (row != null) {
				row.clear();
			}
		}
	}

	/////////////// STATIC PROGRAM ////////////////

	/** Configuration obtained from the command-line arguments. */
	private static class Config {
		public int numGen;
		public int numPrint;
		public boolean numPrintIsSet = false;
		public int numRows;
		public int numCols;
		public boolean minCellIsSet = false;
		public int minRow;
		public int minCol;
		public String startLine = null; //default
		public String endLine = null; //default
		public String sepLine = ""; //default
		public String outAlive = "o"; //default
		public String outDead = "."; //default
		public String outSep = " "; //default
		public String outStart = " "; //default
		public String outEnd = ""; //default
		public String inAlive = "o"; //default
		public String inDead = "."; //default

		public Config() {
			setSize(23, 39); //default
			setNumGen(0); //default
		}

		public void setSize(int numRows, int numCols) {
			this.numRows = numRows;
			this.numCols = numCols;
			if (! minCellIsSet) {
				minRow = -((numRows - 1) / 2);
				minCol = -((numCols - 1) / 2);
			}
		}

		public void setMinCell(int minRow, int minCol) {
			this.minRow = minRow;
			this.minCol = minCol;
			minCellIsSet = true;
		}

		public void setNumGen(int numGen) {
			this.numGen = numGen;
			if (! numPrintIsSet) {
				numPrint = numGen + 1;
			}
		}

		public void setNumPrint(int numPrint) {
			this.numPrint = numPrint;
			numPrintIsSet = true;
		}
	}

	/** Returns null if the parsing of the argument throws exception. */
	private static Integer parseInt(String arg) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Returns true if the given character is one of [a-zA-Z]. */
	private static boolean isAsciiLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

	/** Returns true if the given argument starts with "-a" or "--a". */
	private static boolean isOptionArg(String arg) {
		return (arg.length() > 1 && arg.charAt(0) == '-'
				&& isAsciiLetter(arg.charAt(1)))
			|| (arg.length() > 2 && arg.charAt(0) == '-'
				&& arg.charAt(1) == '-'
				&& isAsciiLetter(arg.charAt(2)));
	}

	private static void throwMissingArg(String argName, int i, String arg) {
		throw new IllegalArgumentException("Missing " + argName
					+ " argument in " + i + ": " + arg);
	}

	private static void throwInvalidArg(String argName, int i, String arg) {
		throw new IllegalArgumentException("Invalid " + argName
					+ " argument in " + i + ": " + arg);
	}

	/** Checks if the given position has a non-option parameter or not. */
	private static String checkArg(String argName, int i, String[] args) {
		String arg = (i < args.length ? args[i] : null);
		if (arg == null || isOptionArg(arg)) {
			throwMissingArg(argName, i, arg);
		}
		return arg;
	}

	private static int parseGenArg(int i, String[] args, Config config) {
		String arg = checkArg("NUM_GEN", i, args);
		Integer numGen = parseInt(arg);
		if (numGen == null || numGen < 0) {
			throwInvalidArg("NUM_GEN", i, arg);
		}
		config.setNumGen(numGen);
		return i + 1;
	}

	private static int parsePrintArg(int i, String[] args, Config config) {
		if (i >= args.length || isOptionArg(args[i])) {
			//restart numPrint value:
			config.setNumGen(config.numGen);
			config.numPrintIsSet = false;
			return i;
		}
		Integer numPrint = parseInt(args[i]);
		if (numPrint == null || numPrint < 1) {
			throwInvalidArg("NUM_PRINT", i, args[i]);
		}
		config.setNumPrint(numPrint);
		return i + 1;
	}

	private static int parseSizeArg(int i, String[] args, Config config) {
		String arg = checkArg("NUM_ROWS,NUM_COLS", i, args);
		if (! arg.matches("-?[0-9]+,-?[0-9]+")) {
			throwInvalidArg("NUM_ROWS,NUM_COLS", i, arg);
		}
		String[] arr = arg.split(",");
		Integer numRows = parseInt(arr[0]);
		if (numRows == null || numRows < 1) {
			throwInvalidArg("NUM_ROWS", i, arr[0]);
		}
		Integer numCols = parseInt(arr[1]);
		if (numCols == null || numCols < 1) {
			throwInvalidArg("NUM_COLS", i, arr[1]);
		}
		config.setSize(numRows, numCols);
		return i + 1;
	}

	private static int parseMinArg(int i, String[] args, Config config) {
		if (i >= args.length || isOptionArg(args[i])) {
			//restart minCell values:
			config.setSize(config.numRows, config.numCols);
			config.numPrintIsSet = false;
			return i;
		}
		String arg = args[i];
		if (! arg.matches("-?[0-9]+,-?[0-9]+")) {
			throwInvalidArg("MIN_ROW,MIN_COL", i, arg);
		}
		String[] arr = arg.split(",");
		Integer minRow = parseInt(arr[0]);
		if (minRow == null) {
			throwInvalidArg("MIN_ROW", i, arr[0]);
		}
		Integer minCol = parseInt(arr[1]);
		if (minCol == null) {
			throwInvalidArg("MIN_COL", i, arr[1]);
		}
		config.setMinCell(minRow, minCol);
		return i + 1;
	}

	private static int parseStartArg(int i, String[] args, Config config) {
		if (i >= args.length || isOptionArg(args[i])) {
			config.startLine = null;
			return i;
		}
		config.startLine = args[i];
		return i + 1;
	}

	private static int parseEndArg(int i, String[] args, Config config) {
		if (i >= args.length || isOptionArg(args[i])) {
			config.endLine = null;
			return i;
		}
		config.endLine = args[i];
		return i + 1;
	}

	private static int parseSepArg(int i, String[] args, Config config) {
		if (i >= args.length || isOptionArg(args[i])) {
			config.sepLine = null;
			return i;
		}
		config.sepLine = args[i];
		return i + 1;
	}

	/** Returns the substrings of a string divided by the given character.*/
	private static String[] splitByChar(String str, char c) {
		int last = 0, i;
		ArrayList<String> result = new ArrayList<>();
		for (i = 0; i < str.length(); i++) {
			if (c == str.charAt(i)) {
				result.add(str.substring(last, i));
				last = i + 1;
			}
		}
		result.add(str.substring(last, i));
		return result.toArray(new String[0]);
	}

	/** Counts the occurrences of the given character in a string. */
	private static int countChar(String str, char c) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (c == str.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	private static final String OUT_FORMAT = ",ALIVE,DEAD[,SEP,START,END]";
	private static final String IN_FORMAT = ",ALIVE,DEAD";
	private static final String INVALID_ALIVE_DEAD =
			"The ALIVE and DEAD arguments must not be "
			+ "equal, or empty, or with different lengths";

	private static int parseOutFmtArg(int i, String[] args, Config config) {
		String arg = checkArg(OUT_FORMAT, i, args);
		if (arg.length() < 2) { // ",,"
			throwInvalidArg(OUT_FORMAT, i, arg);
		}
		char sep = arg.charAt(0);
		int count = countChar(arg, sep);
		if (count != 2 && count != 5) { // ",,,,,"
			throwInvalidArg(OUT_FORMAT, i, arg);
		}
		String[] fields = splitByChar(arg.substring(1), sep);
		if (fields[0].length() == 0 || fields[1].length() == 0
				|| fields[0].length() != fields[1].length()
				|| fields[0].equals(fields[1])) {
			throw new IllegalArgumentException("Invalid output "
					+ "format argument in " + i + ": "
					+ INVALID_ALIVE_DEAD + ": " + arg);
		}
		config.outAlive = fields[0];
		config.outDead = fields[1];
		config.outSep = (count == 5 ? fields[2] : "");
		config.outStart = (count == 5 ? fields[3] : "");
		config.outEnd = (count == 5 ? fields[4] : "");
		return i + 1;
	}

	private static int parseInFmtArg(int i, String[] args, Config config) {
		String arg = checkArg(IN_FORMAT, i, args);
		if (arg.length() < 2) { // ",,"
			throwInvalidArg(IN_FORMAT, i, arg);
		}
		char sep = arg.charAt(0);
		int count = countChar(arg, sep);
		if (count != 2) { // ",,"
			throwInvalidArg(IN_FORMAT, i, arg);
		}
		String[] fields = splitByChar(arg.substring(1), sep);
		if (fields[0].length() == 0 || fields[1].length() == 0
				|| fields[0].length() != fields[1].length()
				|| fields[0].equals(fields[1])) {
			throw new IllegalArgumentException("Invalid input "
					+ "format argument in " + i + ": "
					+ INVALID_ALIVE_DEAD + ": " + arg);
		}
		config.inAlive = fields[0];
		config.inDead = fields[1];
		return i + 1;
	}

	/** Initializes a row of the board starting in the given coordinates
	 * from the given text line and counting only the two configured
	 * strings as ALIVE or DEAD cells. When the first ALIVE cell is found,
	 * sets the initial column for next rows to put that cell in (0, 0). */
	private static void readLine(SimpleLife game, int[] startCoords,
			boolean[] firstFound, String line, Config config) {
		int i = startCoords[0];
		int j = startCoords[1];
		int k = 0, ia, id;
		while (k < line.length()) {
			ia = line.indexOf(config.inAlive, k);
			id = line.indexOf(config.inDead, k);
			if (ia < 0 && id < 0) {
				break;
			}
			if (id < 0 || (ia > -1 && ia < id)) { //ALIVE cell:
				if (! firstFound[0]) { //if is the first:
					firstFound[0] = true;
					//sets the initial column for next rows:
					startCoords[1] = -j;
					i = j = 0;
				}
				game.set(i, j++, true);
				k = ia + config.inAlive.length();
			} else { //DEAD cell:
				j++; //not setting the DEAD cells, is default
				k = id + config.inDead.length();
			}
		}
		startCoords[0] = i + 1;
	}

	/** Initializes the board with cells read from the standard input,
	 * counting only the two configured strings as ALIVE or DEAD cells
	 * and placing the first ALIVE cell in (0, 0). */
	public static void readInput(SimpleLife game, Config config)
			throws IOException {
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(System.in));
		int[] startCoords = new int[2]; // (0, 0)
		boolean[] firstFound = new boolean[1]; //false
		game.clear();
		String line;
		while ((line = reader.readLine()) != null) {
			readLine(game, startCoords, firstFound, line, config);
		}
	}

	private static final String LN = System.lineSeparator();

	/** Prints the board to a StringBuilder using the given configuration.*/
	private static StringBuilder printBoard(SimpleLife game, Config config,
			StringBuilder output) {
		int firstRow = config.minRow;
		int firstCol = config.minCol;
		int lastRow1 = config.minRow + config.numRows;
		int lastCol1 = config.minCol + config.numCols;
		output.setLength(0);
		if (config.startLine != null) {
			output.append(config.startLine).append(LN);
		}
		for (int i = firstRow; i < lastRow1; i++) {
			output.append(config.outStart);
			for (int j = firstCol; j < lastCol1; j++) {
				if (j != firstCol) {
					output.append(config.outSep);
				}
				output.append(game.get(i, j)
					? config.outAlive : config.outDead);
			}
			output.append(config.outEnd).append(LN);
		}
		if (config.endLine != null) {
			output.append(config.endLine).append(LN);
		}
		return output;
	}

	private static void p(String line) {
		System.out.println(line);
	}

	private static void q(String line) {
		System.out.println(line.replaceAll("Q", "\""));
	}

	private static void printHelp() {
	p("Usage: java SimpleLife [OPTION]... < INPUT_FILE");
	p("Simple command-line version of Conway's Game of Life (B3/S23).");
	p("");
	p("Gets the cells of the board reading lines from the standard input");
	p("interpreting by default the symbols o and . as ALIVE/DEAD cells,");
	p("and then prints the ASCII board in each generation of cells.");
	p("The position (0, 0) is assigned to the first ALIVE cell found, so");
	p("the board read will be the same if the cells are just shifted:");
	p(" . . . . . . . ");
	p(" . . . o o . . ");
	p(" . . . . o o . ");
	p(" . . . . o . . ");
	p("");
	p("  -g|--gen NUM_GEN");
	p("              Number of generations to calculate, 0 by default!");
	p("  -p|--print [NUM_PRINT]");
	p("              Number of boards to print counting from the last,");
	p("              by default is NUM_GEN+1 to print the generation 0");
	p("  -s|--size NUM_ROWS,NUM_COLS");
	p("              The size of the portion of the board to be printed,");
	p("              as the number of rows and columns, by default 23,39");
	p("  -m|--min [MIN_ROW,MIN_COL]");
	p("              The minimum row and minimum colum to be printed, or");
	p("              the coordinates of the first upper-left cell shown,");
	p("              by default calculated to center the board in (0, 0)");
	p("  --startline [LINE] / --endline [LINE] / --sepline [LINE]");
	p("              Prints the given line before each board, after it");
	p("              or between two boards. By default only --sepline QQ");
	q("              is active, but can be removed just using --sepline");
	p("  -o|--outfmt \",ALIVE,DEAD[,SEP,START,END]\"");
	p("              Format to print the lines, using the first character");
	p("              to separate the fields that will be concatenated as:");
	p("              START + (ALIVE|DEAD) + [SEP + (ALIVE|DEAD)]... + END");
	p("              The default format is \",o,., , ,\" but try these:");
	q("                Q,[],  Q / Q,@,_,|,|,|Q / Q,[_], _ Q / Q,(o), . Q");
	p("  -i|--infmt ,ALIVE,DEAD");
	p("              The two character sequences to recognize from the");
	p("              input for ALIVE and DEAD cells, \",o,.\" by default");
	p("  -h|--help   Prints this help");
	p("");
	p("Example of use with the default command-line options:");
	p("");
	p("  java SimpleLife --gen 0 --print 1 --size 23,39 --min -11,-19 \\");
	q("            --sepline QQ --outfmt Q,o,., , ,Q --infmt Q,o,.Q");
	}

	/** Runs the program with the given arguments reading lines from the
	 * standard input and then printing the board to the standard output. */
	public static void main(String[] args) throws IOException {
		Config config = new Config();
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-h") || arg.equals("--help")) {
				printHelp();
				return;
			}
			else if (arg.equals("-g") || arg.equals("--gen")) {
				i = parseGenArg(++i, args, config);
			}
			else if (arg.equals("-p") || arg.equals("--print")) {
				i = parsePrintArg(++i, args, config);
			}
			else if (arg.equals("-s") || arg.equals("--size")) {
				i = parseSizeArg(++i, args, config);
			}
			else if (arg.equals("-m") || arg.equals("--min")) {
				i = parseMinArg(++i, args, config);
			}
			else if (arg.equals("--startline")) {
				i = parseStartArg(++i, args, config);
			}
			else if (arg.equals("--endline")) {
				i = parseEndArg(++i, args, config);
			}
			else if (arg.equals("--sepline")) {
				i = parseSepArg(++i, args, config);
			}
			else if (arg.equals("-o") || arg.equals("--outfmt")) {
				i = parseOutFmtArg(++i, args, config);
			}
			else if (arg.equals("-i") || arg.equals("--infmt")) {
				i = parseInFmtArg(++i, args, config);
			}
			else {
				throw new IllegalArgumentException(
				"Unknown argument in " + i + ": " + arg
				+ " (HINT: use the -h option for help)");
			}
		}
		SimpleLife game = new SimpleLife();
		readInput(game, config);
		StringBuilder out = new StringBuilder();
		boolean prevPrint = false;
		if (config.numPrint > config.numGen) {
			System.out.print(printBoard(game, config, out));
			prevPrint = true;
		}
		for (int g = 1; g <= config.numGen; g++) {
			if (config.sepLine != null && prevPrint) {
				System.out.println(config.sepLine);
			}
			game.next();
			if (config.numPrint > config.numGen - g) {
				System.out.print(printBoard(game, config, out));
				prevPrint = true;
			} else {
				prevPrint = false;
			}
		}
	}

}
