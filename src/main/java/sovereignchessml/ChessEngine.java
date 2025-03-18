package sovereignchessml;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.deeplearning4j.core.storage.StatsStorage;


import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Arrays;
import java.util.Comparator;
import java.text.DecimalFormat;

public class ChessEngine {

    public MultiLayerNetwork model;
    public INDArray currentBoard, predictions, target;
    public static final String MODEL_PATH = "chessModel.zip";
    public static final int NUM_CHANNELS = 12 * 6;
    public static final int SIZE = 16;
    public final int NUM_MOVES = 11968;
    
    public ChessEngine(double learningRate) {
        // Load model if it exists, otherwise create a new one
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            System.out.println("Loading existing model...");
            try {
                model = loadModel();
            }
            catch (IOException e) {
                System.out.println("oops");
            }
        } else {
            System.out.println("Creating new model...");
            model = createNewModel(learningRate);
            model.init();
        }
        try {
            // Initialize the UI server
            UIServer uiServer = UIServer.getInstance();

            // Create an in-memory StatsStorage instance
            //StatsStorage statsStorage = new InMemoryStatsStorage();
            StatsStorage statsStorage = new FileStatsStorage(new File(System.getProperty("java.io.tmpdir"), "ui-stats.dl4j"));

            // Attach the StatsStorage instance to the UI server
            uiServer.attach(statsStorage);

            // Add StatsListener to the model for monitoring
            model.setListeners(new StatsListener(statsStorage, 1)); // 1 indicates logging every iteration
        }
        catch (Exception e) {
            System.out.println("No UIServer");
        }
    }

    // This is not used for anything. 
    public void main(String[] args) throws IOException {
        
        Board board = new Board(null);

        // Load model if it exists, otherwise create a new one
        File modelFile = new File(MODEL_PATH);
        if (modelFile.exists()) {
            System.out.println("Loading existing model...");
            model = loadModel();
        } else {
            System.out.println("Creating new model...");
            model = createNewModel(0.001);
        }

        for (int i = 0; i < 300; i++) {
            // Example: Get the current board state (12x8x8 matrix)
            currentBoard = getCurrentBoardState(board);

            // Predict the next move
            predictions = model.output(currentBoard).getRow(0);
            int predictedMove = predictions.argMax(1).getInt(0);

            // Execute the move in your game engine
            makeMove(board, predictedMove);

            int actualMove = predictedMove; // Track the move to train the model

            if (!checkMove(board, actualMove)) {
                System.out.println("Illegal move detected. Picking a random legal move...");
                actualMove = getRandomLegalMove(board);
            }
            
            makeMove(board, actualMove); // Ensure this move gets played

            // Compute reward based on the game's current state
            double reward = computeReward(board, 0, 0);

            // Update the model
            target = predictions.dup();
            target.putScalar(actualMove, reward); // Update target for the played move
            model.fit(new DataSet(currentBoard, target));
        }

        // Save the model after training
        saveModel();

    }

    public MultiLayerNetwork createNewModel(double learningRate) {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                .list()
                .layer(0, new ConvolutionLayer.Builder(3, 3)
                        .nIn(NUM_CHANNELS) // channels for board
                        .nOut(NUM_CHANNELS) // Output channels
                        .stride(1, 1)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new SubsamplingLayer.Builder()
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                // Add a preprocessor to flatten the output
                .inputPreProcessor(2, new CnnToFeedForwardPreProcessor(((SIZE / 2) - 1), ((SIZE / 2) - 1), NUM_CHANNELS))
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .nIn(((SIZE / 2) - 1) * ((SIZE / 2) - 1) * NUM_CHANNELS) // Match the previous layer's output size
                        .nOut(NUM_MOVES) // All possible moves
                        .activation(Activation.SOFTMAX)
                        .build())
                .build());
    }

    public void fitModel() {
        model.fit(new DataSet(currentBoard, target));
    }
    
    public void saveModel() {
        System.out.println();
        System.out.println("Starting to save model... Don't close program.");
        
        model.fit(new DataSet(currentBoard, target));
        String filePath = MODEL_PATH;
        File file = new File(filePath);
        try {
            ModelSerializer.writeModel(model, file, true);
        }
        catch (IOException e) {
            System.err.println("oops");
        }
        System.out.println("Model saved!");
        System.out.println();
    }

    public MultiLayerNetwork loadModel() throws IOException {
        String filePath = MODEL_PATH;
        File file = new File(filePath);
        return ModelSerializer.restoreMultiLayerNetwork(file);
    }
    
    public double computeReward(Board board, double oldScore, double newScore) {
        // Positive for good moves, negative for bad moves
        double reward = 0.0;
        if (board.currPlayer == 0) reward = newScore - oldScore;
        if (board.currPlayer == 1) reward = -1 * (newScore - oldScore);
        return reward; // Example reward for demonstration
    }
    
    public void printReward(double reward) {
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("This is the reward for that move: " + df.format(reward));
    }

    public INDArray getCurrentBoardState(Board board) {
        // Initialize an empty matrix
        INDArray boardMatrix = Nd4j.zeros(1, NUM_CHANNELS, SIZE, SIZE);
        
        // Add other pieces based on the board state
        // Loop through your game board representation and set the values
        if (board.currPlayer == 0) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    int pColor = board.getPieceColor(board.pieces[row][col]);
                    if (pColor % 2 == 0) pColor--;
                    else pColor++;
                    int channel = pColor * (board.getPieceValue(board.pieces[row][col]) / board.PAWN);
                    if (channel < 0 || channel >= NUM_CHANNELS) {
                        System.err.println("Invalid channel: " + channel);
                        continue; // Skip invalid entries
                    }
                    boardMatrix.putScalar(new int[]{0, channel, 15 - row, col}, 1);
                }
            }
        }
        if (board.currPlayer == 1) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    int channel = board.getPieceColor(board.pieces[row][col]) * (board.getPieceValue(board.pieces[row][col]) / board.PAWN);
                    if (channel < 0 || channel >= NUM_CHANNELS) {
                        System.err.println("Invalid channel: " + channel);
                        continue; // Skip invalid entries
                    }
                    boardMatrix.putScalar(new int[]{0, channel, row, col}, 1);
                }
            }
        }

        return boardMatrix;
    }
    
    public boolean checkMove(Board board, int move) {
        /*
        int startRow = (move / SIZE / SIZE / SIZE) % SIZE;
        int startCol = (move / SIZE / SIZE) % SIZE;
        int endRow = (move / SIZE) % SIZE;
        int endCol = move % SIZE;
        Square start = new Square(startRow, startCol);
        Square end = new Square(endRow, endCol);
        */
        Move m = board.getMoveFromNum(move);
        if (!board.isEmptySquare(m.start)) {
            Piece p = board.makePiece(m.start);
            if (p.isLegalMove(board, m.start, m.end, (board.currPlayer + 1) % 2)) {
                return true;
            }
        }
        //m.printMove("Chosen engine");

        return false;
    }

    public boolean makeMove(Board board, int move) {
        Move m = board.getMoveFromNum(move);
        // Print actual move
        m.printMove("Actual engine");
        // Execute the move in your game engine
        return board.movePiece(m.start, m.end, board.players[board.currPlayer]);
    }

    public int getRandomLegalMove(Board board) {
        Vector<Move> allMoves = new Vector<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (!board.isEmptySquare(i, j)) {
                    Square start = new Square(i, j);
                    Player currPlayer = board.players[board.currPlayer];
                    if (currPlayer.checkColorControl(board.getPieceColor(start))) {
                        allMoves.addAll(board.getAllPieceLegalMoves(start, board.players, board.currPlayer));
                    }
                }
            }
        }

        int randomMove, moveIndex;
        do {
            randomMove = (int) (Math.random() * allMoves.size());
            Move m = allMoves.get(randomMove);
            moveIndex = (m.start.row * SIZE * SIZE * SIZE) + (m.start.col * SIZE * SIZE) + (m.end.row * SIZE) + m.end.col;
        } while (!checkMove(board, moveIndex));
        
        return moveIndex;
    }
    
    public int[] getSortedPredictions() {
        Double[] probabilities = new Double[NUM_MOVES];
        Integer[] indices = new Integer[NUM_MOVES];

        // Extract data from INDArray into Java array
        for (int i = 0; i < NUM_MOVES; i++) {
            probabilities[i] = predictions.getDouble(i);
            indices[i] = i;
        }

        // Sort indices based on probabilities in descending order
        Arrays.sort(indices, Comparator.comparing(i -> -probabilities[i])); // Sort by probability descending

        // Get the top-N indices
        return Arrays.copyOfRange(Arrays.stream(indices).mapToInt(Integer::intValue).toArray(), 0, NUM_MOVES);
    }
    
    public int invertMove(Board board, int move) {
        if (board.currPlayer == 1) {
            return move;
        }
        Move m = board.getMoveFromNum(move);
        m.start.row = 15 - m.start.row;
        m.end.row = 15 - m.end.row;
        return board.getNumFromMove(m);
        /*
        int startRow = 15 - (move / SIZE / SIZE / SIZE) % SIZE;
        int startCol = (move / SIZE / SIZE) % SIZE;
        int endRow = 15 - (move / SIZE) % SIZE;
        int endCol = move % SIZE;
        return (startRow * SIZE * SIZE * SIZE) + (startCol * SIZE * SIZE) + (endRow * SIZE) + endCol;
        */
    }

}
