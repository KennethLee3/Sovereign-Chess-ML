package sovereignchessml;

import java.util.Vector;

public abstract class Piece implements Cloneable {
    private char printChar;
    private int color;
    private final int value;
    
    public Piece(int color, char printChar, int value) {
        this.color = color;
        this.printChar = printChar;
        this.value = value;
    }
    
    @Override
    public Piece clone() {
        try {
            Piece clonedPiece = (Piece) super.clone();
            return clonedPiece;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public char getPrintChar() {
        return printChar;
    }
    public int getColor() {
        return color;
    }
    public int getValue() {
        return value;
    }
    public boolean isLegalMove(Board board, Square start, Square end, int oppPlayer) {
        // Check whether the piece is owned by the current player. 
        if (board.getCurrentPlayer().checkColorControl(board.getPieceColor(start))) {
            // Check whether matching destination square is occupied. 
            if (board.matchingSquareEmpty(end)) {
                // Check if move is valid. 
                return isValidMove(board, start, end, oppPlayer);
            }
        }
        return false;
    }

    public abstract boolean isValidMove(Board board, Square start, Square end, int oppPlayer);
    public abstract double positionMultiplier(Board board, Square square);
    public abstract Vector<Square> getAllPossibleMoves(Board board, Square start);
}
