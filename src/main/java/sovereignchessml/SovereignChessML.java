package sovereignchessml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.text.DecimalFormat;


public class SovereignChessML extends JFrame {
    enum PlayerType {HUMAN, ML, HEURISTIC}
    public final static PlayerType p1 = PlayerType.HUMAN;
    public final static PlayerType p2 = PlayerType.HEURISTIC;
    
    public final int SIZE = 16;
    public final double INVALID_MOVE_PENALTY = -2;
    public final double REPEATED_MOVE_PENALTY = -4;
    public final int ENGINE_HISTORY_DIST = 1;
    public final int HEURISTIC_SEARCH_DEPTH = 3;
    public final int MIN = -5000;
    public final int MAX = 5000;
    public final int MOVE_PAUSE_TIME = 0;

    private JButton[][] sqML;
    private JProgressBar progressBar;
    private JLabel turnLabel;
    private JPanel moveHistoryPanel;
    private JScrollPane scrollPane;
    
    private Board board;
    private ChessEngine engine;
    
    private Square startLoc;


    /**
     * Creates new form SovereignChessML
     */
    public SovereignChessML() {
        initComponentsX();
        
        board = new Board(sqML);
        engine = new ChessEngine(0.001);
        board.players = new Player[]{
            new Player(p1.toString(), board.WHITE),
            new Player(p2.toString(), board.BLACK)
        };
        board.currPlayer = 0;
        board.updateBoard(startLoc, sqML, turnLabel);
        //board.updatePieces();
        printEvaluation(conductEvaluation(board.players, board));
        moveHistoryPanel.repaint();
        
    }
    
    private void initComponentsX() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sovereign Chess (ML)");

        sqML = new JButton[SIZE][SIZE];
        JPanel chessboardPanel = new JPanel(new GridLayout(SIZE, SIZE));
        chessboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

        // Create a fixed header panel for column labels
        JPanel headerPanel = new JPanel(new GridLayout(1, 4));
        headerPanel.setBackground(Color.DARK_GRAY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding


        JLabel moveNum = new JLabel("Turn", SwingConstants.CENTER);
        JLabel player1 = new JLabel("Player1", SwingConstants.CENTER);
        JLabel player2 = new JLabel("Player2", SwingConstants.CENTER);
        JLabel evaluation = new JLabel("Eval", SwingConstants.CENTER);

        moveNum.setForeground(Color.WHITE);
        player1.setForeground(Color.WHITE);
        player2.setForeground(Color.WHITE);
        evaluation.setForeground(Color.WHITE);

        headerPanel.add(moveNum);
        headerPanel.add(player1);
        headerPanel.add(player2);
        headerPanel.add(evaluation);

        // Move history panel (scrollable)
        moveHistoryPanel = new JPanel();
        moveHistoryPanel.setLayout(new GridBagLayout()); // Allows top-aligned moves
        moveHistoryPanel.setBackground(Color.DARK_GRAY);
        moveHistoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding

        scrollPane = new JScrollPane(moveHistoryPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        
        // Sidebar container (Header stays fixed, moves scroll)
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.add(headerPanel, BorderLayout.NORTH);
        sidebarPanel.add(scrollPane, BorderLayout.CENTER);

        // Main panel layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(turnLabel, BorderLayout.NORTH);
        mainPanel.add(chessboardPanel, BorderLayout.CENTER);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(sidebarPanel, BorderLayout.EAST);

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
                        if (ENGINE_HISTORY_DIST > 1) {
                            addMoveToQueue(board.getNumFromMove(new Move(startLoc, endLoc, null)), board.moveNum + 1, previousScore);
                            retireMoves(board.moveNum - ENGINE_HISTORY_DIST);
                        } else {
                            engine.target.putScalar(engine.invertMove(board, board.getNumFromMove(new Move(startLoc, endLoc, null))), reward);
                        }
                    }
                    Evaluation eval = conductEvaluation(board.players, board);
                    addMoveToHistory(board.possibleMoves.get(board.getNumFromMove(new Move(startLoc, endLoc, null))), null);
                    switchPlayer();
                    startLoc = null;
                    board.updateBoard(startLoc, sqML, turnLabel);
                    if (board.currPlayer == 1) {
                        if (p2 != PlayerType.HUMAN) {
                            if (getCurrentPlayerType() == PlayerType.ML) {
                                machineLearningMove();
                            }
                            if (getCurrentPlayerType() == PlayerType.HEURISTIC) {
                                heuristicMove();
                            }
                            /*
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
                            if (ENGINE_HISTORY_DIST > 1) {
                                addMoveToQueue(engineMove, board.moveNum + 1, previousScore);
                                retireMoves(board.moveNum - ENGINE_HISTORY_DIST);
                            } else {
                                reward = engine.computeReward(board, previousScore, getPositionEvaluation());
                                engine.printReward(reward);
                                engine.target.putScalar(engine.invertMove(board, engineMove), reward);
                            }
                            eval = conductEvaluation(board.players, board);
                            addMoveToHistory(board.possibleMoves.get(engineMove), getFormattedEvaluation(eval.getScore()));
                            switchPlayer();
                            board.updateBoard(null, sqML, turnLabel);
                            */
                        }
                    }
                    if (checkmate) {
                        //engine.saveModel();
                        board = new Board(sqML);
                        board.players = new Player[]{
                            new Player("Player", board.WHITE),
                            new Player("Computer", board.BLACK)
                        };
                        board.currPlayer = 0;
                        moveHistoryPanel.removeAll();
                        board.retiredMoves.clear();
                    }
                    printEvaluation(eval);
                    board.updateBoard(startLoc, sqML, turnLabel);
                    
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
                    Thread.sleep(MOVE_PAUSE_TIME);
                    boolean checkmate = false;
                    if (getCurrentPlayerType() == PlayerType.ML) {
                        checkmate = machineLearningMove();
                    }
                    if (getCurrentPlayerType() == PlayerType.HEURISTIC) {
                        checkmate = heuristicMove();
                    }
                    
                    if (checkmate || board.moveNum >= 1000) {
                        board = new Board(sqML);
                        board.players = new Player[]{
                            new Player(p1.toString(), board.WHITE),
                            new Player(p2.toString(), board.BLACK)
                        };
                        board.currPlayer = 0;
                        board.updateBoard(startLoc, sqML, turnLabel);
                        board.retiredMoves.clear();
                        moveHistoryPanel.removeAll();
                    }
                    
                    // Schedule GUI updates on the EDT
                    SwingUtilities.invokeLater(() -> {
                        board.updateBoard(null, sqML, turnLabel);
                        printEvaluation(conductEvaluation(board.players, board));
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private boolean machineLearningMove() {
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
        Move currMove = addMoveToQueue(engineMove, board.moveNum + 1, previousScore);
        retireMoves(board.moveNum - ENGINE_HISTORY_DIST);
        if (board.moveNum % 200 == 0) {
            engine.fitModel();
        }
        Evaluation eval = conductEvaluation(board.players, board);
        addMoveToHistory(board.possibleMoves.get(engineMove), getFormattedEvaluation(eval.getScore()));

        // Switch player and update board
        switchPlayer();
        
        // Count repetitions
        int numRepetitions = 0;
        for (Move m : board.retiredMoves) {
            //if (m.start == currMove.start && m.end == currMove.end) numRepetitions++;
        }
        /*
        if (checkmate || board.moveNum >= 1000 || numRepetitions >= 8) {
            engine.saveModel();
            board = new Board(sqML);
            board.players = new Player[]{
                new Player(p1.toString(), board.WHITE),
                new Player(p2.toString(), board.BLACK)
            };
            board.currPlayer = 0;
            board.updateBoard(startLoc, sqML, turnLabel);
            board.retiredMoves.clear();
            moveHistoryPanel.removeAll();
        }

        // Schedule GUI updates on the EDT
        SwingUtilities.invokeLater(() -> {
            board.updateBoard(null, sqML, turnLabel);
            printEvaluation(eval);
        });
        */
        
        return checkmate;
    }
    private boolean heuristicMove() {
        double best = MIN + (2 * board.currPlayer * MAX);
        Board bestBoard = null;

        Vector<Board> children = board.getAllChildBoards();
        for (Board childBoard : children) {
            if (board.currPlayer == 0) {
                double val = minimax(1, childBoard, false, MIN, MAX);
                if (val > best) {
                    best = val;
                    bestBoard = childBoard;
                }
            }
            if (board.currPlayer == 1) {
                double val = minimax(1, childBoard, true, MIN, MAX);
                if (val < best) {
                    best = val;
                    bestBoard = childBoard;
                }
            }
        }
            
        //double previousScore = getPositionEvaluation();
        int heuristicMove = bestBoard.moveNum;
        Move m = board.getMoveFromNum(heuristicMove);
        boolean checkmate = board.movePiece(m.start, m.end, board.players[board.currPlayer]);
        //addMoveToQueue(heuristicMove, board.moveNum + 1, previousScore);
        Evaluation eval = conductEvaluation(board.players, board);
        addMoveToHistory(board.possibleMoves.get(heuristicMove), getFormattedEvaluation(eval.getScore()));

        // Switch player and update board
        switchPlayer();
        /*
        if (checkmate || board.moveNum >= 1000) {
            board = new Board(sqML);
            board.players = new Player[]{
                new Player(p1.toString(), board.WHITE),
                new Player(p2.toString(), board.BLACK)
            };
            board.currPlayer = 0;
            board.updateBoard(startLoc, sqML, turnLabel);
            board.retiredMoves.clear();
            moveHistoryPanel.removeAll();
        }

        // Schedule GUI updates on the EDT
        SwingUtilities.invokeLater(() -> {
            board.updateBoard(null, sqML, turnLabel);
            printEvaluation(eval);
        });
        */
        
        return checkmate;
    }
    private double minimax(int depth, Board parentBoard, Boolean maximizingPlayer, double alpha, double beta) {
        // Terminating condition. 
        if (depth == HEURISTIC_SEARCH_DEPTH) {
            return parentBoard.evaluatePosition(parentBoard.players);
        }
        if (maximizingPlayer) {
            double best = MIN;

            Vector<Board> children = parentBoard.getAllChildBoards();
            for (Board childBoard : children) {
                double val = minimax(depth + 1, childBoard, false, alpha, beta);
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);

                // Alpha Beta Pruning
                if (beta <= alpha) {
                    break;
                }
            }
            return best;
        } else {
            double best = MAX;

            Vector<Board> children = parentBoard.getAllChildBoards();
            for (Board childBoard : children) {
                double val = minimax(depth + 1, childBoard, true, alpha, beta);
                best = Math.min(best, val);
                beta = Math.min(beta, best);

                // Alpha Beta Pruning
                if (beta <= alpha) {
                    break;
                }
            }
            return best;
        }
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
    public void addMoveToHistory(String playerMove, String evaluation) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = moveHistoryPanel.getComponentCount() / 4; // Each row has 4 elements
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align items at the top
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Add turn number.
        if (board.currPlayer == 0) {
            JLabel moveNumLabel = new JLabel("" + (board.moveNum + 1) / 2);
            moveNumLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            moveHistoryPanel.add(moveNumLabel, gbc);
            gbc.gridx = 1;
        }
        
        // Add new move. 
        JLabel playerLabel = new JLabel(board.latestMove);
        playerLabel.setForeground(Color.WHITE);
        moveHistoryPanel.add(playerLabel, gbc);
        
        // Add evaluation. 
        if (board.currPlayer == 1) {
            JLabel evaluationLabel = new JLabel(evaluation);
            evaluationLabel.setForeground(Color.WHITE);
            gbc.gridx = 3;
            moveHistoryPanel.add(evaluationLabel, gbc);
        }
        
        moveHistoryPanel.revalidate();
        moveHistoryPanel.repaint();
    }
    public Move addMoveToQueue(int move, int moveNum, double priorReward) {
        Move newMove = new Move(move, moveNum, priorReward);
        board.unretiredMoves.add(newMove);
        return newMove;
    }
    public void retireMoves(int moveRetirePoint) {
        if (board.unretiredMoves.isEmpty()) return;
        while (board.unretiredMoves.firstElement().moveNum <= moveRetirePoint) {
            Move m = board.unretiredMoves.remove(0);
            board.retiredMoves.add(m);
            double reward = engine.computeReward(board, m.eval, getPositionEvaluation());
            for (Move otherMove : board.retiredMoves) {
                if (m.thisMove == otherMove.thisMove) {
                    reward = reward + REPEATED_MOVE_PENALTY;
                }
            }
            engine.printReward(reward);
            engine.target.putScalar(engine.invertMove(board, m.thisMove), reward);
            if (board.unretiredMoves.isEmpty()) return;
        } 
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
    private String getFormattedEvaluation(double score) {
        String print = "";
        if (score > 0.0) print = "+";
        else if (score == 0.0) print = "=";
        DecimalFormat df = new DecimalFormat("0.00");
        return print + df.format(score);
    }
    private void printEvaluation(Evaluation eval) {
        progressBar.setString(getFormattedEvaluation(eval.getScore()));
        progressBar.setValue(eval.getProgressBarEvaluation(eval.getScore()));
    }
    private PlayerType getCurrentPlayerType() {
        if (board.currPlayer == 0) {
            return p1;
        }
        return p2;
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
                game.setSize(1000,800);
                game.setVisible(true);
                
                if (p1 != PlayerType.HUMAN && p2 != PlayerType.HUMAN) {
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
