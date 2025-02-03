package sovereignchessml;

import java.util.Vector;
import java.util.Stack;

public class Evaluation {
    
    public int DEPTH;
    private int currentDepth;
    private int currPlayer;
    private double score;
    private Board board;
    private Move move;
    private Player[] players;
    private Evaluation bestMove;
    // BFS only
    private Vector<Evaluation> childMoves;
    // DFS only
    private Evaluation parentMove;

    public Evaluation(int DEPTH, int currDepth, Board board, Player[] players, int currPlayer) {
        this.DEPTH = DEPTH;
        this.currentDepth = currDepth;
        this.currPlayer = currPlayer;
        this.score = (((DEPTH % 2) * 2) - 1) * -1000;
        this.board = board;
        this.players = players;
        this.childMoves = new Vector<>();
    }
    public Evaluation(int DEPTH, int currDepth, Board board, Player[] players, Move move, Evaluation parentMove, int currPlayer) {
        this.DEPTH = DEPTH;
        this.currentDepth = currDepth;
        this.score = (((DEPTH % 2) * 2) - 1) * -1000;
        this.board = board;
        this.players = players;
        this.move = move;
        this.parentMove = parentMove;
        this.currPlayer = currPlayer;
    }
    
    public void engineML() {
        setScore();
    }
    
    public double evaluatePosition(Player[] players, Board board) {
        double player1 = evaluatePlayer(players[0], board);
        double player2 = evaluatePlayer(players[1], board);
        return player1 - player2;
    }
    public double evaluatePlayer(Player p, Board board) {
        double score = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int piece = board.pieces[i][j];
                if (p.checkColorControl(board.getPieceColor(piece))) {
                    double pieceValue = getPieceValue(board, board.colorSQ[i][j], board.makePiece(piece));
                    if (p.getColorOwn() != board.getPieceColor(piece)) pieceValue *= 0.4;
                    score += pieceValue;
                }
            }
        }
        return score;
    }
    public double getPieceValue(Board board, Square square, Piece piece) {
        double value = piece.getValue();
        value *= piece.positionMultiplier(board, square);
        return value;
    }
    public double getScore() {
        return score;
    }
    public Move getMove() {
        return move;
    }
    public Evaluation getBestMove() {
        return bestMove;
    }
    public int getNumChildMoves() {
        return childMoves.size();
    }
    public int getProgressBarEvaluation(double score) {
        if (score > 0) {
            score = Math.log10(score + 1) * 12.5;
            score = 50 + score;
        } else if (score < 0) {
            score = Math.log10(Math.abs(score - 1)) * 12.5;
            score = 50 - score;
        } else {
            score = 50;
        }
        return (int) score;
    }
    public void setScore() {
        this.score = this.evaluatePosition(this.players, this.board);
    }
    public int nextPlayer() {
        return (currPlayer + 1) % players.length;
    }
}
