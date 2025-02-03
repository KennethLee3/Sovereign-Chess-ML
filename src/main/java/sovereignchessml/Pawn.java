package sovereignchessml;

import java.util.Vector;

public class Pawn extends Piece {

    public Pawn(int color) {
        super(color, 'p', 1);
    }

    @Override
    public boolean isValidMove(Board board, Square start, Square end, int oppPlayer) {
        boolean valid = false;
        // Vertical movement
        if (board.sameCol(start, end) && board.isEmptyPath(start, end, oppPlayer) && board.isEmptySquare(end)) {
            if (start.row < 7 && board.distanceRow(start, end) == 1) {
                valid = true;
            } else if (start.row < 2 && board.distanceRow(start, end) == 2) {
                valid = true;
            } else if (start.row > 8 && board.distanceRow(start, end) == -1) {
                valid = true;
            } else if (start.row > 13 && board.distanceRow(start, end) == -2) {
                valid = true;
            }
        }
        // Horizontal movement
        else if (board.sameRow(start, end) && board.isEmptyPath(start, end, oppPlayer) && board.isEmptySquare(end)) {
            if (start.col < 7 && board.distanceCol(start, end) == 1) {
                valid = true;
            } else if (start.col < 2 && board.distanceCol(start, end) == 2) {
                valid = true;
            } else if (start.col > 8 && board.distanceCol(start, end) == -1) {
                valid = true;
            } else if (start.col > 13 && board.distanceCol(start, end) == -2) {
                valid = true;
            }
        }
        // Diagonal capture
        else if (board.distanceRowAbs(start, end) == 1 && board.distanceColAbs(start, end) == 1 && 
                !board.sameRow(start, end) && !board.sameCol(start, end) && 
                !board.isEmptySquare(end) && board.isAvailableSquare(end, oppPlayer)) {
            valid = true;
            if (start.row <= 7 && board.distanceRow(start, end) == -1) {
                if (start.col <= 8 && board.distanceCol(start, end) == -1) {
                    valid = false;
                } else if (start.col >= 7 && board.distanceCol(start, end) == 1) {
                    valid = false;
                }
            } else if (start.row >= 8 && board.distanceRow(start, end) == 1) {
                if (start.col <= 8 && board.distanceCol(start, end) == -1) {
                    valid = false;
                } else if (start.col >= 7 && board.distanceCol(start, end) == 1) {
                    valid = false;
                }
            }
        }
        return valid;
    }
    @Override
    public Vector<Square> getAllPossibleMoves(Board board, Square start) {
        Vector<Square> moveVector = new Vector<>();
        // NORTH
        for (int i = 1; i <= 2 && start.row + i < 16; i++) moveVector.add(new Square(start.row + i, start.col));
        // SOUTH
        for (int i = 1; i <= 2 && start.row - i >= 0; i++) moveVector.add(new Square(start.row - i, start.col));
        // EAST
        for (int i = 1; i <= 2 && start.col + i < 16; i++) moveVector.add(new Square(start.row, start.col + i));
        // WEST
        for (int i = 1; i <= 2 && start.col - i >= 0; i++) moveVector.add(new Square(start.row, start.col - i));
        // NORTHEAST
        if (start.row + 1 < 16 && start.col + 1 < 16) moveVector.add(new Square(start.row + 1, start.col + 1));
        // SOUTHEAST
        if (start.row - 1 >= 0 && start.col + 1 < 16) moveVector.add(new Square(start.row - 1, start.col + 1));
        // SOUTHWEST
        if (start.row - 1 >= 0 && start.col - 1 >= 0) moveVector.add(new Square(start.row - 1, start.col - 1));
        // NORTHWEST
        if (start.row + 1 < 16 && start.col - 1 >= 0) moveVector.add(new Square(start.row + 1, start.col - 1));
        
        return moveVector;
    }
    @Override
    public double positionMultiplier(Board board, Square square) {
        double multiplier = board.distanceCenterRowAbs(square) + board.distanceCenterColAbs(square);
        multiplier = 1 + (2 / multiplier);
        return multiplier;
    }
}
