package sovereignchessml;

public class Square implements Cloneable {
    protected int row;
    protected int col;
    private int color;
    private Square matchingSquare;
    
    public Square(int row, int col) {
        this.row = row;
        this.col = col;
        this.color = 0;
        this.matchingSquare = null;
    }
    
    @Override
    public Square clone() {
        try {
            Square clonedSquare = (Square) super.clone();
            return clonedSquare;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Square getMatchingSquare() {
        return matchingSquare;
    }
    public void setColor(int color, Square matchingSquare) {
        this.color = color;
        this.matchingSquare = matchingSquare;
    }
    public int getColor() {
        return color;
    }
    public String printCoordinate() {
        return String.valueOf((char) ('A' + col)) + String.valueOf(row + 1);
    }
}
