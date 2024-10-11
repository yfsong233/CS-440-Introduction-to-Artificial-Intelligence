package src.pas.battleship.agents;


// SYSTEM IMPORTS
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.Constants; // our import
import edu.bu.battleship.game.Difficulty; // our import
import edu.bu.battleship.game.PlayerView;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;

public class ProbabilisticAgent
    extends Agent
{

    private static final Difficulty Difficulty = null;

    public ProbabilisticAgent(String name)
    {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game)
    {
        Random r = new Random();

        int col = game.getGameConstants().getNumCols();
        int row = game.getGameConstants().getNumRows();

        int i = row/2;
        int j = col/2;
        // int count = 1;
        Coordinate cor = new Coordinate(i, j);

        if (game.isInBounds(cor) && game.getEnemyBoardView()[i][j].toString().equals("UNKNOWN")){
            System.out.println("Hi we are inside" + i + j);
            return cor;
        }

        Map<Coordinate, Double> probMap = createProbMatrix(game, row, col);

        double highestProb = -1;

        // List<Coordinate> keys = new ArrayList<Coordinate>(probMap.keySet());
        // Collections.shuffle(keys);

        List<Map.Entry<Coordinate, Double>> entries = new ArrayList<>(probMap.entrySet());

        // Shuffle the list to randomize order
        Collections.shuffle(entries);

        for (Map.Entry<Coordinate, Double> entry : entries) {
            double value = entry.getValue();
            // System.out.println("We are in the loop." + value);
            // if (entry.getKey().getXCoordinate() != row/2 && entry.getKey().getYCoordinate() != col/2) {
            //     cor = new Coordinate(6, 5);
            // }

            if (value >= highestProb && value < 1.0 && game.getEnemyBoardView()[entry.getKey().getXCoordinate()][entry.getKey().getYCoordinate()].toString().equals("UNKNOWN")) {

                    highestProb = entry.getValue();
                    cor = entry.getKey();
                
                
            }
        }

        // while (!game.getEnemyBoardView()[i][j].toString().equals("UNKNOWN")){
        //     i--;
        //     j--;   
        //     cor = new Coordinate(i, j);
        //     if (!game.isInBounds(cor)) {
        //         i = row/2 - count;
                
        //         j = col/2 + count;
        //         // System.out.println("i, j" + i + j);
        //         count++;
        //         cor = new Coordinate(i, j);
        //     }
        //     // System.out.println("count " + count);
        // }
        
        return cor;
    }

    public Map<Coordinate, Double> createProbMatrix(final GameView game, int row, int col) {
        Map<Coordinate, Double> probMap = new HashMap<>();



        for (int x = 0; x < row; x++) {
            for (int y = 0; y < col; y++) {
                Coordinate cor = new Coordinate(x, y);
                String status = game.getEnemyBoardView()[x][y].toString();

                switch (status) {
                    case "HIT":
                        probMap.put(cor, 1.0);
                        setAdjacentProbabilitiesHit(game, probMap, x, y, 0.91);
                        break;
                    case "MISS":
                        probMap.put(cor, -1.0);
                        setAdjacentProbabilities(game, probMap, x, y, -1);
                        break;
                    case "SUNK":
                        probMap.put(cor, -2.0);
                        break;
                    default:
                        if (!probMap.containsKey(cor)) { // Ensure default probabilities aren't overwritten
                            probMap.put(cor, 0.0);
                        }
                        break;
                }
            }
        }
        return probMap;
    }

    private void setAdjacentProbabilities(GameView game, Map<Coordinate, Double> probMap, int x, int y, double probability) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            Coordinate adjCoord = new Coordinate(newX, newY);
            Double probCoord = probMap.get(adjCoord);
            if (game.isInBounds(newX, newY) && (game.getEnemyBoardView()[newX][newY].toString() == "UNKNOWN") && probCoord == null) {
                probMap.put(adjCoord, probability);
            }
            else if (game.isInBounds(newX, newY) && (game.getEnemyBoardView()[newX][newY].toString() == "UNKNOWN") && probCoord < 0.5) {
                probMap.put(adjCoord, probability);
            }
        }
    }

    private void setAdjacentProbabilitiesHit(GameView game, Map<Coordinate, Double> probMap, int x, int y, double probability) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            Coordinate adjCoord = new Coordinate(newX, newY);
            if (game.isInBounds(newX, newY) && (game.getEnemyBoardView()[newX][newY].toString() == "UNKNOWN")) {
                probMap.put(adjCoord, probability);
            }
        }
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
ProbabilisticAgent.java
6 KB