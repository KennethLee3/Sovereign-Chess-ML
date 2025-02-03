package sovereignchessml;

public class Move implements Cloneable {
    public Player player;
    public Square start;
    public Square end;
    
    public Move (Square start, Square end, Player player) {
        this.start = start;
        this.end = end;
        this.player = player;
    }
    
    @Override
    public Move clone() {
        try {
            Move clonedMove = (Move) super.clone();
            clonedMove.start = this.start.clone();
            clonedMove.end = this.end.clone();
            clonedMove.player = this.player.clone();
            
            return clonedMove;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void printMove(String moveDescription) {
        System.out.println(moveDescription + " move is " + this.start.printCoordinate() + 
                " to " + this.end.printCoordinate());
        System.out.println();
    }
    public void updateMove(Board board, Player player) {
        //this.start = board.getSquare(start.row, start.col);
        //this.end = board.getSquare(end.row, end.col);
        this.player = player;
    }
}
