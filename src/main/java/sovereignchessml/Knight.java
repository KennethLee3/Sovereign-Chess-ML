package sovereignchessml;

import java.util.Vector;

public class Knight extends Piece {

    public Knight(int color) {
        super(color, 'n', 3);
    }

    @Override
    public boolean isValidMove(Board board, Square start, Square end, int oppPlayer) {
        boolean valid = false;
        if (board.isAvailableSquare(end, oppPlayer)) {
            // Case 1
            if (board.distanceRowAbs(start, end) == 2 && board.distanceColAbs(start, end) == 1) {
                valid = true;
            }
            // Case 2
            else if (board.distanceRowAbs(start, end) == 1 && board.distanceColAbs(start, end) == 2) {
                valid = true;
            }
        }
        return valid;
    }
    @Override
    public Vector<Square> getAllPossibleMoves(Board board, Square start) {
        Vector<Square> moveVector = new Vector<>();
        // NNE
        if (start.row + 2 < 16 && start.col + 1 < 16) moveVector.add(new Square(start.row + 2, start.col + 1));
        // ENE
        if (start.row + 1 < 16 && start.col + 2 < 16) moveVector.add(new Square(start.row + 1, start.col + 2));
        // ESE
        if (start.row - 1 < 16 && start.col + 2 < 16) moveVector.add(new Square(start.row - 1, start.col + 2));
        // SSE
        if (start.row - 2 < 16 && start.col + 1 < 16) moveVector.add(new Square(start.row - 2, start.col + 1));
        // NNW
        if (start.row + 2 < 16 && start.col - 1 < 16) moveVector.add(new Square(start.row + 2, start.col - 1));
        // WNW
        if (start.row + 1 < 16 && start.col - 2 < 16) moveVector.add(new Square(start.row + 1, start.col - 2));
        // WSW
        if (start.row - 1 < 16 && start.col - 2 < 16) moveVector.add(new Square(start.row - 1, start.col - 2));
        // SSW
        if (start.row - 2 < 16 && start.col - 1 < 16) moveVector.add(new Square(start.row - 2, start.col - 1));
        
        return moveVector;
    }
    @Override
    public double positionMultiplier(Board board, Square square) {
        double multiplier = board.distanceCenterRowAbs(square) + board.distanceCenterColAbs(square);
        multiplier = 1 + (2 / multiplier);
        return multiplier;
    }
}
