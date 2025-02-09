package sovereignchessml;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Color;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class Board implements Cloneable {
    public static final int WHITE =      1;
    public static final int BLACK =      2;
    public static final int SILVER =     3;
    public static final int GOLD =       4;
    public static final int PURPLE =     5;
    public static final int BROWN =      6;
    public static final int ROYAL_BLUE = 7;
    public static final int ORANGE =     8;
    public static final int YELLOW =     9;
    public static final int GREEN =     10;
    public static final int RED =       11;
    public static final int BLUE =      12;
    
    public static final int EMPTY =      0;
    public static final int PAWN =     100;
    public static final int KNIGHT =   200;
    public static final int BISHOP =   300;
    public static final int ROOK =     400;
    public static final int QUEEN =    500;
    public static final int KING =     600;
    
    public static final int SIZE = 16;
    
    public int[][] pieces;
    public Square[][] colorSQ;
    public Player[] players;
    public int currPlayer;
    public int moveNum;
    public Colors[] colorArray;
    Map<Integer, String> possibleMoves;
    

    public Board(JButton[][] squares) {
        this.moveNum = 0;
        this.pieces = new int[SIZE][SIZE];
        this.colorSQ = new Square[SIZE][SIZE];
        this.colorArray = new Colors[13];
        this.possibleMoves = new HashMap<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                pieces[i][j] = EMPTY;
                colorSQ[i][j] = new Square(i, j);
            }
        }
        initializeSquares(squares);
        initializePieces();
        initializeMoves();
    }
    
    @Override
    public Board clone() {
        try {
            Board clonedBoard = (Board) super.clone();

            // Clone the squares array (deep copy)
            clonedBoard.pieces = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    clonedBoard.pieces[i][j] = this.pieces[i][j];
                    clonedBoard.colorSQ[i][j] = this.colorSQ[i][j].clone();
                }
            }

            return clonedBoard;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(); // Should not happen, as we implement Cloneable
            return null;
        }
    }

    private void initializeSquares(JButton[][] squares) {
        String pathname = "C:\\Users\\kelee\\SovereignChessML\\src\\main\\java\\sovereignchessml\\square_setup.txt";
        try (Scanner scanner = new Scanner(new File(pathname))) {
            int colorIncrement = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\\s+");
                if (tokens.length == 8) { // Ensure there are eight elements in the line
                    colorIncrement++;
                    try {
                        int row1 = Integer.parseInt(tokens[0]);
                        int col1 = Integer.parseInt(tokens[1]);
                        int row2 = Integer.parseInt(tokens[2]);
                        int col2 = Integer.parseInt(tokens[3]);
                        String color = tokens[4];
                        int red = Integer.parseInt(tokens[5]);
                        int green = Integer.parseInt(tokens[6]);
                        int blue = Integer.parseInt(tokens[7]);

                        colorArray[colorIncrement] = new Colors(red, green, blue, color);
                        squares[row1][col1].setBackground(new Color(red, green, blue));
                        squares[row2][col2].setBackground(new Color(red, green, blue));
                        this.colorSQ[row1][col1].setColor(colorIncrement, this.colorSQ[row2][col2]);
                        this.colorSQ[row2][col2].setColor(colorIncrement, this.colorSQ[row1][col1]);

                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric values in line: " + line);
                    }
                } else {
                    System.err.println("Invalid format in line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    private void initializePieces() {
        String pathname = "C:\\Users\\kelee\\SovereignChessML\\src\\main\\java\\sovereignchessml\\piece_setup.txt";
        try (Scanner scanner = new Scanner(new File(pathname))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\\s+");
                if (tokens.length == 9) { // Ensure there are nine elements in the line
                    try {
                        String id = tokens[0];
                        int rowS = Integer.parseInt(tokens[1]);
                        int colS = Integer.parseInt(tokens[2]);
                        int rowE = Integer.parseInt(tokens[3]);
                        int colE = Integer.parseInt(tokens[4]);
                        String color = tokens[5];
                        int red = Integer.parseInt(tokens[6]);
                        int green = Integer.parseInt(tokens[7]);
                        int blue = Integer.parseInt(tokens[8]);

                        for (int i = rowS; i <= rowE; i++) {
                            for (int j = colS; j <= colE; j++) {
                                pieces[i][j] = getColor(color) + PAWN;
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric values in line: " + line);
                    }
                } else if (tokens.length == 7) { // Ensure there are seven elements in the line
                    try {
                        String id = tokens[0];
                        int row = Integer.parseInt(tokens[1]);
                        int col = Integer.parseInt(tokens[2]);
                        String color = tokens[3];
                        int red = Integer.parseInt(tokens[4]);
                        int green = Integer.parseInt(tokens[5]);
                        int blue = Integer.parseInt(tokens[6]);

                        switch (id) {
                            case "k":
                                pieces[row][col] = getColor(color) + KING;
                                break;
                            case "q":
                                pieces[row][col] = getColor(color) + QUEEN;
                                break;
                            case "r":
                                pieces[row][col] = getColor(color) + ROOK;
                                break;
                            case "b":
                                pieces[row][col] = getColor(color) + BISHOP;
                                break;
                            case "n":
                                pieces[row][col] = getColor(color) + KNIGHT;
                                break;
                        }
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric values in line: " + line);
                    }
                } else {
                    System.err.println("Invalid format in line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    private void initializeMoves() {
        int mapCounter = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // NORTH
                for (int d = 1; d <= 8 && isInBounds(i + d, j); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + d)+'a') + (char)(j+'a'));
                    mapCounter++;
                }
                // SOUTH
                for (int d = 1; d <= 8 && isInBounds(i - d, j); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - d)+'a') + (char)(j+'a'));
                    mapCounter++;
                }
                // EAST
                for (int d = 1; d <= 8 && isInBounds(i, j + d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)(i+'a') + (char)((j + d)+'a'));
                    mapCounter++;
                }
                // WEST
                for (int d = 1; d <= 8 && isInBounds(i, j - d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)(i+'a') + (char)((j - d)+'a'));
                    mapCounter++;
                }
                // NORTHEAST
                for (int d = 1; d <= 8 && isInBounds(i + d, j + d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + d)+'a') + (char)((j + d)+'a'));
                    mapCounter++;
                }
                // SOUTHEAST
                for (int d = 1; d <= 8 && isInBounds(i - d, j + d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - d)+'a') + (char)((j + d)+'a'));
                    mapCounter++;
                }
                // NORTHWEST
                for (int d = 1; d <= 8 && isInBounds(i + d, j - d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + d)+'a') + (char)((j - d)+'a'));
                    mapCounter++;
                }
                // SOUTHWEST
                for (int d = 1; d <= 8 && isInBounds(i - d, j - d); d++) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - d)+'a') + (char)((j - d)+'a'));
                    mapCounter++;
                }
                // NNE
                if (isInBounds(i + 2, j + 1)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + 2)+'a') + (char)((j + 1)+'a'));
                    mapCounter++;
                }
                // ENE
                if (isInBounds(i + 1, j + 2)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + 1)+'a') + (char)((j + 2)+'a'));
                    mapCounter++;
                }
                // SSE
                if (isInBounds(i - 2, j + 1)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - 2)+'a') + (char)((j + 1)+'a'));
                    mapCounter++;
                }
                // ESE
                if (isInBounds(i - 1, j + 2)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - 1)+'a') + (char)((j + 2)+'a'));
                    mapCounter++;
                }
                // NNW
                if (isInBounds(i + 2, j - 1)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + 2)+'a') + (char)((j - 1)+'a'));
                    mapCounter++;
                }
                // WNW
                if (isInBounds(i + 1, j - 2)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i + 1)+'a') + (char)((j - 2)+'a'));
                    mapCounter++;
                }
                // SSW
                if (isInBounds(i - 2, j - 1)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - 2)+'a') + (char)((j - 1)+'a'));
                    mapCounter++;
                }
                // WSW
                if (isInBounds(i - 1, j - 2)) {
                    possibleMoves.put(mapCounter, "" + (char)(i+'a') + (char)(j+'a') + (char)((i - 1)+'a') + (char)((j - 2)+'a'));
                    mapCounter++;
                }
            }
        }
    }
    public void updateBoard(Square startLoc, JButton[][] squares, JLabel turnLabel) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!isEmptySquare(i, j)) {
                    Colors thisColor = colorArray[getPieceColor(pieces[i][j])];
                    squares[i][j].setForeground(new Color(thisColor.r, thisColor.g, thisColor.b));
                    squares[i][j].setText("" + makePiece(pieces[i][j]).getPrintChar());
                    //squares[i][j].setText("" + pieces[i][j]);
                }
                else {
                    squares[i][j].setText("");
                }
            }
        }
        if(startLoc == null) {
            turnLabel.setText("It is " + players[currPlayer].getName() + "'s turn. " + players[currPlayer].printColors());
        }
        else {
            turnLabel.setText(players[currPlayer].getName() + "'s turn: " + startLoc.printCoordinate());
        }
        players[0].updateColorControl(this);
        players[1].updateColorControl(this);
    }
    public void printBoard() {
        System.out.println();
        System.out.println("     a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p");
        System.out.println("   +------------------------------------------------+");
        for (int i = SIZE - 1; i >= 0; i--) {
            System.out.print(i + 1);
            if (i < 9) System.out.print("  |");
            else System.out.print(" |");
            for (int j = 0; j < SIZE; j++) {
                Piece piece = makePiece(pieces[i][j]);
                if (piece != null) {
                    System.out.print(" " + piece.getPrintChar() + " ");
                } else {
                    System.out.print("\u001B[31m" + " . " + "\u001B[0m");
                }
            }
            System.out.println("| " + (char)(i + 1));
        }
        System.out.println("   +------------------------------------------------+");
        System.out.println("     a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p");
        System.out.println();
    }
    public void printBoard(Player playerTurn) {
        System.out.println();
        System.out.println("It is " + playerTurn.getName() + "'s turn.");
        printBoard();
    }
    public boolean movePiece(Square start, Square end, Player p) {
        moveNum++;
        // Check for checkmate.
        boolean checkmate = false;
        if (getPieceValue(end) == KING) {
            checkmate = true;
        }
        
        // Move piece.
        pieces[end.row][end.col] = pieces[start.row][start.col];
        pieces[start.row][start.col] = EMPTY;
        // Promote pawn.
        if (isPromotionReady(end)) {
            pieces[end.row][end.col] += (QUEEN - PAWN);
        }
        
        return checkmate;
    }
    public boolean movePiece(Move m) {
        return movePiece(m.start, m.end, m.player);
    }
    public Board processBoard(Board inputBoard, Square inputSquare) {
        printBoard();
        return inputBoard;
    }
    public boolean isInBounds(Square square) {
        return isInBounds(square.row, square.col);
    }
    public boolean isInBounds(int row, int col) {
        return (row >= 0 && row < 16 && col >= 0 && col < 16);
    }
    public boolean isPromotionReady(Square square) {
        return (getPieceValue(square) == PAWN && square.row > 5 && square.row < 10 && square.col > 5 && square.col < 10);
    }
    public boolean isAvailableSquare(Square square, int oppPlayer) {
        boolean available = true;
        // Check if the matching square is occupied.
        if (square.getColor() != 0) {
            if (!isEmptySquare(square.getMatchingSquare())) {
                available = false;
            }
        }
        // Check if the square contains a piece you can't capture.
        if (!isEmptySquare(square)) {
            if (!players[oppPlayer].checkColorControl(getPieceColor(square))) {
                available = false;
            }
            /*
            if (square.getPiece().controlPlayer == null) {
                available = false;
            } else if (!square.getPiece().controlPlayer.checkColorControl(oppColor)) {
                available = false;
            }
            */
        }
        return available;
    }
    public boolean isAvailableSquare(int row, int col, int oppPlayer) {
        return isAvailableSquare(colorSQ[row][col], oppPlayer);
    }
    public boolean isEmptySquare(Square square) {
        return getPieceValue(square) == EMPTY;
    }
    public boolean isEmptySquare(int row, int col) {
        return pieces[row][col] == EMPTY;
    }
    public boolean isEmptyPath(Square start, Square end, int oppPlayer) {
        boolean empty = true;
        if (!isAvailableSquare(end, oppPlayer)) {
            empty = false;
        }
        // Horizontal path
        else if (start.row == end.row) {
            int direction = (end.col - start.col) / Math.abs(end.col - start.col);
            for (int i = start.col + direction; i != end.col; i = i + direction) {
                if (!isEmptySquare(start.row, i)) empty = false;
            }
        }
        // Vertical path
        else if (start.col == end.col) {
            int direction = (end.row - start.row) / Math.abs(end.row - start.row);
            for (int i = start.row + direction; i != end.row; i = i + direction) {
                if (!isEmptySquare(i, start.col)) empty = false;
            }
        }
        // Diagonal path
        else if (Math.abs(end.row - start.row) == Math.abs(end.col - start.col)) {
            int directionH = (end.col - start.col) / Math.abs(end.col - start.col);
            int directionV = (end.row - start.row) / Math.abs(end.row - start.row);
            int j = start.row + directionV;
            for (int i = start.col + directionH; i != end.col; i = i + directionH) {
                if (!isEmptySquare(j, i)) empty = false;
                j = j + directionV;
            }
        }
        else {
            empty = false;
        }
        
        return empty;
    }
    public boolean sameRow(Square start, Square end) {
        return (start.row == end.row);
    }
    public boolean sameCol(Square start, Square end) {
        return (start.col == end.col);
    }
    public int distanceRow(Square start, Square end) {
        return (end.row - start.row);
    }
    public int distanceCol(Square start, Square end) {
        return (end.col - start.col);
    }
    public int distanceRowAbs(Square start, Square end) {
        return Math.abs(end.row - start.row);
    }
    public int distanceColAbs(Square start, Square end) {
        return Math.abs(end.col - start.col);
    }
    public int distanceCenterRowAbs(Square square) {
        int distance = 0;
        if (square.row <= 7) {
            distance = 8 - square.row;
        } else if (square.row >= 8) {
            distance = square.row - 7;
        }
        return distance;
    }
    public int distanceCenterColAbs(Square square) {
        int distance = 0;
        if (square.col <= 7) {
            distance = 8 - square.col;
        } else if (square.col >= 8) {
            distance = square.col - 7;
        }
        return distance;
    }
    public Player getCurrentPlayer() {
        return players[currPlayer];
    }
    public Player getNextPlayer() {
        return players[(currPlayer + 1) % 2];
    }
    public Vector<Move> getAllLegalMoves(Square start, Player[] players, int currPlayer) {
        Piece thisPiece = makePiece(start);
        Vector<Move> allMoves = new Vector<>();
        Vector<Square> allEndSquares = thisPiece.getAllPossibleMoves(this, start);
        while (!allEndSquares.isEmpty()) {
            Square end = allEndSquares.remove(0);
            if (isInBounds(end)) {
                //end = board.getSquare(end.row, end.col);
                if (thisPiece.isValidMove(this, start, end, (currPlayer + 1) % 2)) {
                    Move m = new Move(start.clone(), end.clone(), players[currPlayer].clone());
                    allMoves.add(m);
                }
            }
        }
        return allMoves;
    }
    public int getNumFromMove(Move m) {
        String s = "" + (char)(m.start.row+'a') + (char)(m.start.col+'a') + (char)(m.end.row+'a') + (char)(m.end.col+'a');
        for (Map.Entry<Integer, String> entry : possibleMoves.entrySet()) {
            if (entry.getValue().equals(s)) {
                return entry.getKey();
            }
        }
        return 0;
    }
    public Move getMoveFromNum(int num) {
        String move = possibleMoves.get(num);
        Square start = new Square(move.charAt(0) - 'a', move.charAt(1) - 'a');
        Square end = new Square(move.charAt(2) - 'a', move.charAt(3) - 'a');
        return new Move(start, end, null);
    }
    public int getPieceColor(int squareValue) {
        return squareValue % PAWN;
        /*
        if (squareValue % BLUE == 0) return BLUE;
        if (squareValue % RED == 0) return RED;
        if (squareValue % GREEN == 0) return GREEN;
        if (squareValue % YELLOW == 0) return YELLOW;
        if (squareValue % ORANGE == 0) return ORANGE;
        if (squareValue % ROYAL_BLUE == 0) return ROYAL_BLUE;
        if (squareValue % BROWN == 0) return BROWN;
        if (squareValue % PURPLE == 0) return PURPLE;
        if (squareValue % GOLD == 0) return GOLD;
        if (squareValue % SILVER == 0) return SILVER;
        if (squareValue % BLACK == 0) return BLACK;
        if (squareValue % WHITE == 0) return WHITE;
        else return EMPTY;
        */
    }
    public int getPieceColor(Square square) {
        return Board.this.getPieceColor(pieces[square.row][square.col]);
    }
    public int getPieceValue(int squareValue) {
        return PAWN * ((int) (squareValue / PAWN));
        /*
        if (squareValue % KING == 0) return KING;
        if (squareValue % QUEEN == 0) return QUEEN;
        if (squareValue % ROOK == 0) return ROOK;
        if (squareValue % BISHOP == 0) return BISHOP;
        if (squareValue % KNIGHT == 0) return KNIGHT;
        if (squareValue % PAWN == 0) return PAWN;
        else return EMPTY;
        */
    }
    public int getPieceValue(Square square) {
        return Board.this.getPieceValue(pieces[square.row][square.col]);
    }
    public int getColor(String name) {
        for (int i = 1; i < colorArray.length; i++) {
            if (name.equals(colorArray[i].name)) {
                return i;
            }
        }
        return 0;
    }
    public Piece makePiece(int squareValue) {
        switch(getPieceValue(squareValue)) {
            case PAWN:
                return new Pawn(getPieceColor(squareValue));
            case KNIGHT:
                return new Knight(getPieceColor(squareValue));
            case BISHOP:
                return new Bishop(getPieceColor(squareValue));
            case ROOK:
                return new Rook(getPieceColor(squareValue));
            case QUEEN:
                return new Queen(getPieceColor(squareValue));
            case KING:
                return new King(getPieceColor(squareValue));
        }
        return null;
    }
    public Piece makePiece(Square square) {
        return makePiece(pieces[square.row][square.col]);
    }

}
