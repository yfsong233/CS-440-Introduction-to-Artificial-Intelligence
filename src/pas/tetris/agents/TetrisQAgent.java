package src.pas.tetris.agents;


// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;

import edu.bu.battleship.utils.Coordinate;
// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;


public class TetrisQAgent
    extends QAgent
{

    public static final double EXPLORATION_PROB = 0.05;
    public static int NUM_EXPLORED = 1;  // to be changed

    private Random random;


    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int numPixelsInImage = Board.NUM_ROWS * Board.NUM_COLS;
        final int hiddenDim = 2 * numPixelsInImage;
        final int outDim = 1;

        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(numPixelsInImage, hiddenDim));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(hiddenDim, outDim));

        return qFunction;
    }


    @Override
public Matrix getQFunctionInput(final GameView game, final Mino potentialAction) {
    try 
    {
        Matrix grayscaleImage = null;  // a grayscale image of the game board which indicates block positions and types
        try {
            grayscaleImage = game.getGrayscaleImage(potentialAction);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Matrix featureMatrix = Matrix.zeros(1, 8); // assume 8 features for now

        // features
        int holes = 0;
        int maxHeightBefore = 22; // Height of the tallest column before placing the current mino, measured from the top
            boolean mHBSet = false; // Flag to check if maxHeightBefore has been set
        int maxHeightAfter = 22; // Height of the tallest column after placing the current mino
            boolean mHASet = false; // Flag to check if maxHeightAfter has been set
        int heightDelta = 0; // Difference in max height due to current mino placement
        int bumpiness = 0; // Sum of the absolute differences in height between adjacent columns
            Integer[] colHeights = new Integer[10]; // Array to store the height of each column
        int emptyBelow = 0; // Number of empty spaces below the highest block in each column
        int lowHighDelta = -1; // Difference between the highest and lowest blocks on the board
        int minoType = -1; // Type of the mino being considered for placement
        Mino.MinoType curMino = potentialAction.getType(); // Get the current mino type

        // Loop through the game matrix to extract features
        for (int y = 0; y < grayscaleImage.getShape().getNumRows(); y++) {
            for (int x = 0; x < grayscaleImage.getShape().getNumCols(); x++) {
                if (grayscaleImage.get(y, x) == 0.5) { // Check if the coordinate is occupied by a previously placed piece
                    if (!mHBSet) {
                        maxHeightBefore = y; // Set the highest column height before placement
                        mHBSet = true;
                    }
                    if (colHeights[x] == null) {
                        colHeights[x] = y; // Set column height for calculating bumpiness
                    }
                }

                if (grayscaleImage.get(y, x) == 1.0) { // Check if the coordinate is being considered for the current piece
                    if (!mHASet) {
                        maxHeightAfter = y; // Set the highest column height after placement
                        mHASet = true;
                    }
                    if (colHeights[x] == null) {
                        colHeights[x] = y;
                    }
                }

                if (grayscaleImage.get(y, x) == 0.0) { // Check if the coordinate is empty
                    if (y > 0 && (grayscaleImage.get(y - 1, x) == 1.0 || grayscaleImage.get(y - 1, x) == 0.5)) {
                        holes++; // Count holes
                    }
                    if (colHeights[x] != null) {
                        emptyBelow++; // Count empty spaces below the highest block
                    }
                    if (lowHighDelta < y) {
                        lowHighDelta = y; // Find the lowest empty space
                    }
                }
            }
        }

        // Calculate the delta in column height for heightDelta
        heightDelta = Math.max(0, maxHeightBefore - maxHeightAfter);

        // Calculate bumpiness between columns
        for (int i = 0; i < 9; i++) {
            int current = (colHeights[i] == null) ? 22 : colHeights[i];
            int next = (colHeights[i + 1] == null) ? 22 : colHeights[i + 1];
            bumpiness += Math.abs(current - next);
        }

        // Calculate the overall delta between the highest and lowest blocks
        lowHighDelta = maxHeightAfter - lowHighDelta;

        // Determine the type of mino from the enum
        if (curMino != null) {
            minoType = curMino.ordinal(); // Convert mino type to its ordinal value
        }

        // Set all extracted features into the feature matrix
        featureMatrix.set(0, 0, holes);
        featureMatrix.set(0, 1, maxHeightBefore);
        featureMatrix.set(0, 2, maxHeightAfter);
        featureMatrix.set(0, 3, heightDelta);
        featureMatrix.set(0, 4, bumpiness);
        featureMatrix.set(0, 5, emptyBelow);
        featureMatrix.set(0, 6, lowHighDelta);
        featureMatrix.set(0, 7, minoType);

        return featureMatrix; // Return the feature matrix to be used by the Q-function


    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


    private int minoTypeToInt(Mino.MinoType minoType) {
        switch (minoType) {
            case I: return 0;
            case O: return 1;
            case T: return 2;
            case S: return 3;
            case Z: return 4;
            case J: return 5;
            case L: return 6;
            default: return -1; // Unknown type
        }
    }

    public int countHolesFromGrayscale(GameView game, Mino currentMino) {
        Matrix matrix = null;
        try {
            matrix = game.getGrayscaleImage(currentMino); // Get the grayscale image of the board with the current Mino placed
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Handle the exception appropriately
        }
    
        int holes = 0;
        int cols = matrix.getShape().getNumCols(); // Assuming the matrix gives you columns as the second dimension
    
        // Iterate over each column
        for (int col = 0; col < cols; col++) {
            boolean blockFound = false;
            // Traverse each column from top to bottom
            for (int row = 0; row < matrix.getShape().getNumRows(); row++) {
                double value = matrix.get(row, col);
                if (value > 0) { // Any occupied space (0.5 or 1.0)
                    blockFound = true;
                } else if (blockFound && value == 0) { // An empty space (0.0) below an occupied space
                    holes++;
                }
            }
        }
    
        return holes;
    }
    
    


    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    

    @Override
    public boolean shouldExplore(final GameView game,
                                 final GameCounter gameCounter)
    {
        int gameIdx = (int)gameCounter.getTotalGamesPlayed();
        double INITIAL_EXPLORATION_RATE = 1.0; 
        double FINAL_EXPLORATION_RATE = 0.05;
        int EXPLORATION_DECAY_STEPS = 1500;
        double DECAY_RATE = EXPLORATION_DECAY_STEPS / (EXPLORATION_DECAY_STEPS + 7);
        
        // double explore = Math.max(FINAL_EXPLORATION_RATE, INITIAL_EXPLORATION_RATE - turnIdx * (INITIAL_EXPLORATION_RATE - FINAL_EXPLORATION_RATE) / EXPLORATION_DECAY_STEPS);
        double explore = Math.max(FINAL_EXPLORATION_RATE, 
        INITIAL_EXPLORATION_RATE * Math.pow(DECAY_RATE, gameIdx));

        if (this.getRandom().nextDouble() <= explore) { 
            return true;
        }
        return false;
        // return this.getRandom().nextDouble() <= EXPLORATION_PROB;
    }
    


    

    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */
    @Override
    public Mino getExplorationMove(final GameView game)
    {
        // int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        // return game.getFinalMinoPositions().get(randIdx);
        // Retrieve the total number of potential final positions for the current Mino piece in the game
        int permutes = game.getFinalMinoPositions().size();

        // Initialize a matrix to store the Q-values for each possible final position of the Mino
        Matrix results = Matrix.zeros(1, permutes);

        // Loop through each possible Mino position to calculate its Q-value using the Q-function
        for (int i = 0; i < permutes; i++) {
            try {
                // Generate the feature vector for the current position of the Mino
                Matrix cur = this.getQFunctionInput(game, game.getFinalMinoPositions().get(i));
                // Compute the Q-value using the neural network and store the exponential of the Q-value
                results.set(0, i, Math.exp(this.initQFunction().forward(cur).get(0, 0)));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        // Sum all the exponentiated Q-values to normalize them later
        double qTotal = results.sum().get(0, 0);

        // Prepare a matrix to hold the sum of Q-values for normalization
        Matrix denom = Matrix.zeros(1, 1);
        denom.set(0, 0, qTotal);

        // Normalize the Q-values by dividing each by the total sum of Q-values
        Matrix finalResults = null;
        try {
            finalResults = results.ediv(denom);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Identify the action with the smallest normalized Q-value as the exploration move
        int minInd = 0;
        double minVal = Double.POSITIVE_INFINITY;
        for (int j = 0; j < permutes; j++) {
            if (finalResults.get(0, j) < minVal) {
                minVal = finalResults.get(0, j);
                minInd = j;
            }
        }

        // Return the Mino corresponding to the least likely action as determined by the normalized Q-values
        return game.getFinalMinoPositions().get(minInd);
    }

    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a phase, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
                               LossFunction lossFunction,
                               Optimizer optimizer,
                               long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                                                  lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */

    public Matrix getLastMinoGrayscaleImage(GameView game) {
        try {
            List<Mino> minos = game.getFinalMinoPositions();
            int lastElem = minos.size() - 1;
            Mino lastMino = minos.get(lastElem);
            return game.getGrayscaleImage(lastMino);
        } catch (Exception e) {
            e.printStackTrace();
            return null;  
        }
    }

    public Matrix getSecondLastMinoGrayscaleImage(GameView game) {
        try {
            List<Mino> minos = game.getFinalMinoPositions();
            int lastElem = minos.size() - 2;
            Mino lastMino = minos.get(lastElem);
            return game.getGrayscaleImage(lastMino);
        } catch (Exception e) {
            e.printStackTrace();
            return null;  
        }
    }

    public int calculateMaxHeightDifference(Matrix firstMatrix, Matrix secondMatrix) {
    
        int cols = firstMatrix.getShape().getNumCols();
    
        int maxHeightFirst = 100;
        int maxHeightSecond = 100;
    
        // Find the highest occupied cell in each column for the first matrix
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < firstMatrix.getShape().getNumRows(); row++) {
                if (firstMatrix.get(row, col) > 0) {  // Assuming non-zero value indicates block presence
                    maxHeightFirst = Math.min(maxHeightFirst, row);
                }
            }
        }
    
        // Find the highest occupied cell in each column for the second matrix
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < secondMatrix.getShape().getNumRows(); row++) {
                if (secondMatrix.get(row, col) > 0) {  // Assuming non-zero value indicates block presence
                    maxHeightSecond = Math.min(maxHeightSecond, row);
                }
            }
        }
        System.out.println("second, first: " + maxHeightSecond + maxHeightFirst);
    
        // Calculate the maximum height difference between corresponding columns
        int maxHeightDifference = maxHeightSecond - maxHeightFirst;
    
        return maxHeightDifference;
    }
    
    
    // @Override
    // public double getReward(final GameView game)
    // {
    //     List<Mino> minos = game.getFinalMinoPositions();
    //     if (minos.size() != 0) {
    //         Matrix finalMatrix = getLastMinoGrayscaleImage(game);
    //         Matrix secondFinalMatrix = getSecondLastMinoGrayscaleImage(game);
    //         int maxHeightChange = calculateMaxHeightDifference(finalMatrix, secondFinalMatrix);
    //         System.out.println("Maximum Height Change: " + maxHeightChange);

    //     }
    //     System.out.println("total score: " + game.getTotalScore());
    //     // can also punish small minos
    //     return minos.size();
    // }

    

    // @Override
    // public double getReward(final GameView game) {
    //     Matrix grayscale = null;
    //     int cols = 0;
    //     int rows = 0;
    //     try {
    //         grayscale = getLastMinoGrayscaleImage(game); // This might throw an Exception
    //         cols = grayscale.getShape().getNumCols();
    //         rows = grayscale.getShape().getNumRows();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         // Handle the exception, e.g., by logging and/or setting a fallback value
    //         return -1.0; // Return an error code or perform other error handling
    //     }

    //     int mingrayHeight = 100;
    //     for (int col = 0; col < cols; col++) {
    //         for (int row = 0; row < rows; row++) {
    //             if (grayscale.get(row, col) > 0) {  // Assuming non-zero value indicates block presence
    //                 mingrayHeight = Math.min(mingrayHeight, row);
    //             }
    //         }
    //     }

    //     int maxHeightDifference = minHeight - mingrayHeight;
    //     minHeight = Math.min(mingrayHeight, minHeight);
    //     return maxHeightDifference;
    // }

    private int minHeight = 21;

    @Override
    public double getReward(final GameView game) {
        Board b = game.getBoard();
        // Coordinate highest = null;
        int yHighest = 100;
        int first = 0;   // represents if the column already found the top piece, will be 0 or 1
        int row = 22;
        int col = 10;
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++) {
                if (b.isCoordinateOccupied(i, j) && i < yHighest && first == 0) {
                    // highest = new Coordinate(i, j);
                    first = 1;
                    yHighest = i;
                }
            }
            first = 0;
        }
        int difference = yHighest - minHeight;
        int reward = 5 + difference;
        if (minHeight < yHighest) {
            minHeight = yHighest;
        }
        return reward;
    }


}
