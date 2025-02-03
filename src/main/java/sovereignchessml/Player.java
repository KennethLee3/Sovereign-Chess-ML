package sovereignchessml;

import java.util.Vector;

public class Player implements Cloneable {
    private String name;
    private Integer colorOwn;
    private Vector<Integer> colorControl;

    public Player(String name, Integer color) {
        this.name = name;
        this.colorOwn = color;
        this.colorControl = new Vector<>();
    }

    @Override
    public Player clone() {
        try {
            Player clonedPlayer = (Player) super.clone();
            Vector<Integer> deepCopy = new Vector<>();
            for (Integer item : colorControl) {
                deepCopy.add(item);
            }
            clonedPlayer.colorControl = deepCopy;
            return clonedPlayer;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getName() {
        return name;
    }
    public int getColorOwn() {
        return colorOwn;
    }
    public void addColorControl(Integer color) {
        colorControl.add(color);
    }
    public boolean checkColorControl(Integer color) {
        boolean controls = false;
        if (colorControl.contains(color) || colorOwn == color) {
            controls = true;
        }
        return controls;
    }
    public void removeColorControl(Integer color) {
        colorControl.remove(color);
    }
    public void updateColorControl(Board board) {
        colorControl.clear();
        boolean found = true;
        while (found) {
            found = false;
            for (int i = 4; i < 12; i++) {
                for (int j = 4; j < 12; j++) {
                    // Check if square is colored and occupied. 
                    if (board.colorSQ[i][j].getColor() != 0 && !board.isEmptySquare(i, j)) {
                        // Check if piece is ours and this is not yet controlled.
                        if (checkColorControl(board.getPieceColor(board.pieces[i][j])) && 
                                !checkColorControl(board.getPieceColor(board.colorSQ[i][j].getColor()))) {
                            found = true;
                            addColorControl(board.colorSQ[i][j].getColor());
                        }
                    }
                }
            }
        }
    }
    public String printColors() {
        //return "";    // Uncomment to exit debug mode.
        String str = " ";
        str = str.concat(colorOwn + " ");
        for (int i = 0; i < colorControl.size(); i++) {
            str = str.concat(colorControl.elementAt(i) + " ");
        }
        return str;
    }
}
