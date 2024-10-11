package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;                           // Directions in Sepia


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue; // heap in java
import java.util.Set;
import java.util.Stack;


// JAVA PROJECT IMPORTS


public class DijkstraMazeAgent
    extends MazeAgent
{

    public DijkstraMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    /*
     * a heap/priority-queue ordered by the cost of path
     * a hash set for vertices (“contain”) to check if a vertex is done (out of unvisited, pending, done) 
     * a hash map for path->cost and cost comparison
     * 
    */
    {
		PriorityQueue<Path> PQ = new PriorityQueue<Path>(
			new Comparator<Path>() {
			    public int compare(Path a, Path b) {
				    return Float.compare(a.getTrueCost(), b.getTrueCost());
			    }
            }
        );

        Map<Path, Float> pathCostMap = new HashMap<>();  // map path->cost for cost comparison
		Set<Vertex> doneVtx = new HashSet<>(); // a set of "done" vertices

        Path srcPath = new Path(src);
		PQ.add(srcPath);
		pathCostMap.put(srcPath, srcPath.getTrueCost());

		while(!PQ.isEmpty()) {
			Path spNow = PQ.poll();
            Vertex vtxToBegin = spNow.getDestination();
			doneVtx.add(vtxToBegin);
			
			if(vtxToBegin.equals(goal)) {
                System.out.println("return at .equals");
                System.out.println(spNow);
                System.out.println(spNow.getTrueCost());
				return spNow.getParentPath();
			}

			for(Vertex neighbor: getNeighbors(vtxToBegin, state)) {
				if(!doneVtx.contains(neighbor)) {
                    Path spExpanded = new Path(neighbor, 
                                                computeEdgeWeight(getDirectionToMoveTo(vtxToBegin, neighbor)), 
                                                spNow
                                                );

					if(!pathCostMap.containsKey(spExpanded)) { // expansion to an unvisited vtx
						PQ.add(spExpanded);
						pathCostMap.put(spExpanded, spExpanded.getTrueCost());

					} else {  // already have a path for src and dst; cost comparison
						if(pathCostMap.get(spExpanded) > spExpanded.getTrueCost()) {  // current optimal cost vs. latest cost
							// delete the old path from our heap and add childPath to the heap
							PQ.remove(spExpanded); // must remove it first, as the specific path is different
							PQ.add(spExpanded);
							pathCostMap.replace(spExpanded, spExpanded.getTrueCost());
						}
					}
				}
			}
		}
        System.out.println("return at new Path(src)");
		return new Path(src);
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
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

    private float computeEdgeWeight(Direction drc) 
    /*****************************************
    computeEdgeWeight() inputs a direction and returns a set of neighbors given the node vtx. 
    *****************************************/
    {
        float edgeWeight = 0f;

        switch(drc)
        {
            case EAST:
                edgeWeight = 5f;
                break;
            case WEST:
                edgeWeight = 5f;
                break;
            case NORTH: // 'UP'
                edgeWeight = 10f;
                break;
            case SOUTH:
                edgeWeight = 1f;
                break;
            case NORTHWEST:
                edgeWeight = (float)Math.sqrt(Math.pow(computeEdgeWeight(Direction.NORTH), 2) +
                                            Math.pow(computeEdgeWeight(Direction.WEST), 2));
                break;
            case NORTHEAST:
                edgeWeight = (float)Math.sqrt(Math.pow(computeEdgeWeight(Direction.NORTH), 2) +
                                            Math.pow(computeEdgeWeight(Direction.EAST), 2));
                break;
            case SOUTHWEST:
                edgeWeight = (float)Math.sqrt(Math.pow(computeEdgeWeight(Direction.SOUTH), 2) +
                                            Math.pow(computeEdgeWeight(Direction.WEST), 2));
                break;
            case SOUTHEAST:
                edgeWeight = (float)Math.sqrt(Math.pow(computeEdgeWeight(Direction.SOUTH), 2) +
                                            Math.pow(computeEdgeWeight(Direction.EAST), 2));
                break;
            default:
                System.err.println("ERROR: unknown direction=" + drc);
                System.exit(-1);
        }
        return edgeWeight;
    }

}
