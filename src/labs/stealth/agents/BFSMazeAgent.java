package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


import java.util.HashSet;       // will need for bfs
import java.util.Queue;         // will need for bfs; FIFO
import java.util.LinkedList;    // will need for bfs
import java.util.Set;           // will need for bfs
import java.util.Stack;


// JAVA PROJECT IMPORTS


public class BFSMazeAgent
    extends MazeAgent
{

    public BFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    /**************************************** 
    (1) search() implements the BFS algorithm 
        to find a path from the src vertex to the goal vertex. 
    (2) It returns a Path object that implements a path from the source coordinate 
        to a coordinate that is adjacent to the goal coordinate. 
        Do not include the goal coordinate in this path.
    (3) BFS does not care about edge weights, 
        so make sure to use an edge weight of 1f.
    *****************************************/
    {
        Queue<Path> traversalPath = new LinkedList<>();  // a bfs-traversed ordered queue of paths with different nodes as the source
        Set<Vertex> visited = new HashSet<>();  // set of visited nodes

        traversalPath.add(new Path(src));
        visited.add(src);

        while(!traversalPath.isEmpty()) {
            Path pathToGrow = traversalPath.poll();
            Vertex nodeToBegin = pathToGrow.getDestination();
            
            if(isAdjacent(nodeToBegin, goal)) {  // we are next to the goal vertex
                System.out.println("return at isAdjacent");
                System.out.println(pathToGrow);
                System.out.println(pathToGrow.getTrueCost());
                return pathToGrow; // traversal finished
            }

            for(Vertex neighbor: getNeighbors(nodeToBegin, state)) {
                if(!visited.contains(neighbor)) {  // make sure this neighbor is UNvisited
                    visited.add(neighbor); 
                    traversalPath.add(new Path(neighbor, 1f, pathToGrow));
                }
            }
        }
        System.out.println("return at new Path(src)");
        return new Path(src);
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    /*****************************************
    shouldReplacePlan() returns true if the current plan is now invalid 
    (for instance if enemy unit(s) or trees cross the path), and false otherwise. 
    *****************************************/
    {
        Stack<Vertex> currentPlan = getCurrentPlan();
        // a stack of coordinates from player unit's current location to a square adjacent to the enemy base

        if (currentPlan == null) {
            System.out.println("need to replace plan due to null currentPlan");
            return true;
        }

        for (Vertex vtx : currentPlan) {
            int x = vtx.getXCoordinate();
            int y = vtx.getYCoordinate();
    
            if (!state.inBounds(x, y)){
                System.out.println("need to replace plan as any coords is out of bound");
                return true;
            }
            
            if (state.isResourceAt(x, y)) {
                System.out.println("need to replace plan as any coords is tree");
                return true; 
            }
        }

        return false;
    }
    
    private boolean isAdjacent(Vertex u, Vertex v) {
        int x = Math.abs(u.getXCoordinate() - v.getXCoordinate());
        int y = Math.abs(u.getYCoordinate() - v.getYCoordinate());
        return (x <= 1 && y <= 1); //&& (x + y != 0);
    }

    private Set<Vertex> getNeighbors(Vertex vtx, StateView state)
    /*****************************************
    getNeighbors() returns a set of neighbors given the node vtx. 
    *****************************************/
    {
        Set<Vertex> neighbors = new HashSet<Vertex>();

        int x = vtx.getXCoordinate();
        int y = vtx.getYCoordinate();

        // for coords within 1 Chebyshev distance, dx /in [-1, 1]; dy /in [-1, 1]
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                // valid neighbor should not be tree, unit, vtx, out of bound
                if ((dx != 0 || dy != 0) 
                    && !state.isResourceAt(x+dx, y+dy) 
                    && !state.isUnitAt(x+dx, y+dy) 
                    && state.inBounds(x+dx, y+dy)) 
                {
                    neighbors.add(new Vertex(x+dx, y+dy));;
                }
            }
        }
        return neighbors;
    }

}
