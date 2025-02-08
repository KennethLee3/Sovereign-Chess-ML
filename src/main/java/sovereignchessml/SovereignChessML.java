package sovereignchessml;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class SovereignChessML extends JFrame {
    public final int SIZE = 16;
    public final int INVALID_MOVE_PENALTY = -2;
    public final boolean AUTO_PLAY = true;
    public final boolean CPU_VS_CPU = true;

    private JButton[][] sqML;
    private JProgressBar progressBar;
    private JLabel turnLabel;
    
    private Board board;
    private ChessEngine engine;
    
    private Square startLoc;


    /**
     * Creates new form SovereignChessML
     */
    public SovereignChessML() {
        initComponentsX();
        
        board = new Board(sqML);
        board.players = new Player[]{
            new Player("Player", board.WHITE),
            new Player("Computer", board.BLACK)
        };
        board.currPlayer = 0;
        board.updateBoard(startLoc, sqML, turnLabel);
        //board.updatePieces();
        printEvaluation(conductEvaluation(board.players, board));
        
        engine = new ChessEngine();
        
    }
    
    private void initComponentsX() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setTitle("Sovereign Chess");
        setTitle("Sovereign Chess (ML)");

        sqML = new JButton[SIZE][SIZE];
        JPanel chessboardPanel = new JPanel(new GridLayout(SIZE, SIZE));

        for (int i = SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < SIZE; j++) {
                sqML[i][j] = new JButton();
                sqML[i][j].setBackground(new Color(245, 222, 179));  // Set default background color
                sqML[i][j].setForeground(new Color(0, 0, 0));  // Set default text color
                sqML[i][j].setFont(new Font(" ", Font.BOLD, 20));
                sqML[i][j].addActionListener(new SquareButtonListener(i, j));
                chessboardPanel.add(sqML[i][j]);
            }
        }

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBackground(Color.BLACK);
        progressBar.setForeground(Color.WHITE);
        progressBar.setBorderPainted(false);
        progressBar.setFont(new Font(" ", Font.BOLD, 18));
        
        turnLabel = new JLabel(" ");
        turnLabel.setForeground(Color.BLUE);

        add(turnLabel, "North");
        add(chessboardPanel);
        add(progressBar, "South");  // Add progress bar at the bottom

        pack();
        setLocationRelativeTo(null);
    }
    
    private class SquareButtonListener implements ActionListener {

        private final int row;
        private final int col;

        public SquareButtonListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Handle button click for the square at (row, col)
            System.out.println("Button clicked: " + (char) ('A' + col) + (row + 1));
            
            if (startLoc == null) {
                if (isValidStartLoc(row, col)) {
                    startLoc = board.colorSQ[row][col];
                }
                else {
                    printError("Invalid start location.");
                }
                board.updateBoard(startLoc, sqML, turnLabel);
            }
            else {
                Square endLoc = board.colorSQ[row][col];
                int oppPlayer = (board.currPlayer + 1) % 2;
                if (board.makePiece(startLoc).isValidMove(board, startLoc, endLoc, oppPlayer)) {
                    double previousScore = getPositionEvaluation();
                    engine.currentBoard = engine.getCurrentBoardState(board);
                    boolean checkmate = board.movePiece(startLoc, endLoc, getCurrentPlayer());
                    double reward = engine.computeReward(board, previousScore, getPositionEvaluation());
                    if (engine.predictions != null) {
                        engine.target = engine.predictions.dup();
                        engine.target.putScalar(board.getNumFromMove(new Move(startLoc, endLoc, null)), reward);
                    }
                    switchPlayer();
                    Evaluation eval = conductEvaluation(board.players, board);
                    startLoc = null;
                    board.updateBoard(startLoc, sqML, turnLabel);
                    if (board.currPlayer == 1) {
                        if (AUTO_PLAY) {
                            int moveIterator = 0;
                            previousScore = getPositionEvaluation();
                            engine.currentBoard = engine.getCurrentBoardState(board);
                            engine.predictions = engine.model.output(engine.currentBoard).reshape(1, engine.NUM_MOVES);
                            int[] sortedPredictions = engine.getSortedPredictions();
                            int engineMove = sortedPredictions[moveIterator];
                            engine.target = engine.predictions.dup();
                            while (!engine.checkMove(board, engineMove)) {
                                engine.target.putScalar(engineMove, INVALID_MOVE_PENALTY);
                                moveIterator++;
                                engineMove = sortedPredictions[moveIterator];
                                engineMove = engine.invertMove(board, engineMove);
                                engine.target = engine.predictions.dup();
                            }
                            checkmate = engine.makeMove(board, engineMove) || checkmate;
                            reward = engine.computeReward(board, previousScore, getPositionEvaluation());
                            engine.target.putScalar(engineMove, reward); // Update target for the played move
                            engine.saveModel(board.moveNum);
                            switchPlayer();
                            eval = conductEvaluation(board.players, board);
                            board.updateBoard(null, sqML, turnLabel);
                        }
                    }
                    printEvaluation(eval);
                    if (checkmate) {
                        board = new Board(sqML, board.moveNum);
                        board.players = new Player[]{
                            new Player("Computer0", board.WHITE),
                            new Player("Computer1", board.BLACK)
                        };
                        board.currPlayer = 0;
                        board.updateBoard(startLoc, sqML, turnLabel);
                        printEvaluation(conductEvaluation(board.players, board));
                    }
                    
                }
                else {
                    printError("Invalid move.");
                    startLoc = null;
                    board.updateBoard(startLoc, sqML, turnLabel);
                }
            }
        }
    }
    
    private void computerVScomputer() {
        new Thread(() -> {
            while (true) {
                try {
                    int moveIterator = 0;
                    double previousScore = getPositionEvaluation();
                    engine.currentBoard = engine.getCurrentBoardState(board);
                    engine.predictions = engine.model.output(engine.currentBoard).reshape(1, engine.NUM_MOVES);
                    int[] sortedPredictions = engine.getSortedPredictions();
                    //int engineMove = engine.predictions.argMax(1).getInt(0);
                    int engineMove = sortedPredictions[moveIterator];
                    engineMove = engine.invertMove(board, engineMove);
                    engine.target = engine.predictions.dup();
                    while (!engine.checkMove(board, engineMove)) {
                        engine.target.putScalar(engineMove, INVALID_MOVE_PENALTY);
                        moveIterator++;
                        engineMove = sortedPredictions[moveIterator];
                        engineMove = engine.invertMove(board, engineMove);
                        engine.target = engine.predictions.dup();
                    }
                    boolean checkmate = engine.makeMove(board, engineMove);
                    double reward = engine.computeReward(board, previousScore, getPositionEvaluation());
                    engine.target.putScalar(engine.invertMove(board, engineMove), reward); // Update target for the played move
                    engine.saveModel(board.moveNum);

                    // Switch player and update board
                    switchPlayer();
                    if (checkmate) {
                        board = new Board(sqML, board.moveNum);
                        board.players = new Player[]{
                            new Player("Player", board.WHITE),
                            new Player("Computer", board.BLACK)
                        };
                        board.currPlayer = 0;
                        board.updateBoard(startLoc, sqML, turnLabel);
                    }
                    Evaluation eval = conductEvaluation(board.players, board);

                    // Schedule GUI updates on the EDT
                    SwingUtilities.invokeLater(() -> {
                        board.updateBoard(null, sqML, turnLabel);
                        printEvaluation(eval);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean isValidStartLoc(int row, int col) {
        boolean valid = false;
        // Check if you control the piece you've selected. 
        if (!board.isEmptySquare(row, col)) {
            if (board.players[board.currPlayer].checkColorControl(board.getPieceColor(board.pieces[row][col]))) {
                valid = true;
            }
        }
        return valid;
    }
    public void printError(String errorMessage) {
        System.out.println(errorMessage);
        System.out.println();
    }
    private Evaluation conductEvaluation(Player[] players, Board board) {
        // Start timer
        Evaluation eval = new Evaluation(0, 0, board, players, board.currPlayer);
        eval.engineML();
        
        // Stop and print timer
        return eval;
    }
    private double getPositionEvaluation() {
        return new Evaluation(0, 0, board, board.players, board.currPlayer).evaluatePosition(board.players, board);
    }
    private void printEvaluation(Evaluation eval) {
        String print = "";
        if (eval.getScore() > 0.0) print = "+";
        else if (eval.getScore() == 0.0) print = "=";
        DecimalFormat df = new DecimalFormat("0.00");
        progressBar.setString(print + df.format(eval.getScore()));
        progressBar.setValue(eval.getProgressBarEvaluation(eval.getScore()));
    }
    public Player getCurrentPlayer() {
        return board.players[board.currPlayer];
    }
    private void switchPlayer() {
        board.currPlayer = (board.currPlayer + 1) % board.players.length;
    }

    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SovereignChessML game = new SovereignChessML();
                game.setSize(800,820);
                game.setVisible(true);
                
                if (game.CPU_VS_CPU) {
                    game.computerVScomputer();
                    return;
                }
            }
        });
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main2(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SovereignChessML.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SovereignChessML.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SovereignChessML.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SovereignChessML.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SovereignChessML().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
