package sovereignchessml;

import java.util.Vector;

public class King extends Piece {

    public King(int color) {
        super(color, 'k', 500);
    }

    @Override
    public boolean isValidMove(Board board, Square start, Square end, int oppPlayer) {
        boolean valid = false;
        if (board.isAvailableSquare(end, oppPlayer)) {
            if (board.distanceRowAbs(start, end) <= 1 && board.distanceColAbs(start, end) <= 1) {
                valid = true;
            }
        }
        return valid;
    }
    @Override
    public Vector<Square> getAllPossibleMoves(Board board, Square start) {
        Vector<Square> moveVector = new Vector<>();
        // NORTH
        if (start.row + 1 < 16) moveVector.add(new Square(start.row + 1, start.col));
        // SOUTH
        if (start.row - 1 >= 0) moveVector.add(new Square(start.row - 1, start.col));
        // EAST
        if (start.col + 1 < 16) moveVector.add(new Square(start.row, start.col + 1));
        // WEST
        if (start.col - 1 >= 0) moveVector.add(new Square(start.row, start.col - 1));
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
        multiplier = 1 + (multiplier / 5000);
        return multiplier;
    }
}
