package sovereignchessml;

import java.util.Vector;

public class Bishop extends Piece {

    public Bishop(int color) {
        super(color, 'b', 4);
    }

    @Override
    public boolean isValidMove(Board board, Square start, Square end, int oppPlayer) {
        boolean valid = false;
        if (board.isEmptyPath(start, end, oppPlayer)) {
            if (board.distanceRowAbs(start, end) <= 8 && 
                    board.distanceColAbs(start, end) <= 8 && 
                    !board.sameRow(start, end) && !board.sameCol(start, end)) {
                valid = true;
            }
        }
        return valid;
    }
    @Override
    public Vector<Square> getAllPossibleMoves(Board board, Square start) {
        Vector<Square> moveVector = new Vector<>();
        // NORTHEAST
        for (int i = 1; i <= 8 && start.row + i < 16 && start.col + i < 16; i++) moveVector.add(new Square(start.row + i, start.col + i));
        // SOUTHEAST
        for (int i = 1; i <= 8 && start.row - i >= 0 && start.col + i < 16; i++) moveVector.add(new Square(start.row - i, start.col + i));
        // SOUTHWEST
        for (int i = 1; i <= 8 && start.row - i >= 0 && start.col - i >= 0; i++) moveVector.add(new Square(start.row - i, start.col - i));
        // NORTHWEST
        for (int i = 1; i <= 8 && start.row + i < 16 && start.col - i >= 0; i++) moveVector.add(new Square(start.row + i, start.col - i));
        
        return moveVector;
    }
    @Override
    public double positionMultiplier(Board board, Square square) {
        double multiplier = board.distanceCenterRowAbs(square) + board.distanceCenterColAbs(square);
        multiplier = 1 + (2 / multiplier);
        return multiplier;
    }
}
