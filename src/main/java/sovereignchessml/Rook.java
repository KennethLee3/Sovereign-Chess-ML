package sovereignchessml;

import java.util.Vector;

public class Rook extends Piece {

    public Rook(int color) {
        super(color, 'r', 6);
    }

    @Override
    public boolean isValidMove(Board board, Square start, Square end, int oppPlayer) {
        boolean valid = false;
        if (board.isEmptyPath(start, end, oppPlayer)) {
            // Vertical movement
            if (board.sameCol(start, end) && board.distanceRowAbs(start, end) <= 8) {
                valid = true;
            }
            // Horizontal movement
            if (board.sameRow(start, end) && board.distanceColAbs(start, end) <= 8) {
                valid = true;
            }
        }
        return valid;
    }
    @Override
    public Vector<Square> getAllPossibleMoves(Board board, Square start) {
        Vector<Square> moveVector = new Vector<>();
        // NORTH
        for (int i = 1; i <= 8 && start.row + i < 16; i++) moveVector.add(new Square(start.row + i, start.col));
        // SOUTH
        for (int i = 1; i <= 8 && start.row - i >= 0; i++) moveVector.add(new Square(start.row - i, start.col));
        // EAST
        for (int i = 1; i <= 8 && start.col + i < 16; i++) moveVector.add(new Square(start.row, start.col + i));
        // WEST
        for (int i = 1; i <= 8 && start.col - i >= 0; i++) moveVector.add(new Square(start.row, start.col - i));

        return moveVector;
    }
    @Override
    public double positionMultiplier(Board board, Square square) {
        double multiplier = board.distanceCenterRowAbs(square) + board.distanceCenterColAbs(square);
        multiplier = 1 + (2 / multiplier);
        return multiplier;
    }
}
