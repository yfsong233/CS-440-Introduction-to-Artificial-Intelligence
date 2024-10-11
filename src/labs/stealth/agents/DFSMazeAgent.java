package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


import java.util.HashSet;   // will need for dfs
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;     // will need for dfs
import java.util.Set;       // will need for dfs


// JAVA PROJECT IMPORTS


public class DFSMazeAgent
    extends MazeAgent
{

    public DFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        Stack<Path> traversalPath = new Stack<Path>();  // a dfs-traversed ordered stack of paths with different nodes as the source
        Set<Vertex> visited = new HashSet<Vertex>();  // set of visited nodes

        traversalPath.push(new Path(src));
        visited.add(src);

        while(!traversalPath.isEmpty()) {
            Path pathToGrow = traversalPath.pop();
            Vertex nodeToBegin = pathToGrow.getDestination();

            if(isAdjacent(nodeToBegin, goal)) {  // we are next to the goal vertex
                // System.out.println("traversal finished");
                return pathToGrow; // traversal finished
            }

            for(Vertex neighbor: getNeighbors(nodeToBegin, state)) {
                if(!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        traversalPath.push(new Path(neighbor, 1.0f, pathToGrow));
                }
            }
        }
        return new Path(src);
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        return false;
    }

    private boolean isAdjacent(Vertex u, Vertex v) {
        int x = Math.abs(u.getXCoordinate() - v.getXCoordinate());
        int y = Math.abs(u.getYCoordinate() - v.getYCoordinate());
        return (x <= 1 && y <= 1) && (x + y != 0);
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
