package src.labs.openLoop.agents;


// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;                                        // how we tell sepia what each unit will do
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;                                          // base class for an Agent in sepia
import edu.cwru.sepia.environment.model.history.History.HistoryView;        // history of the game so far
import edu.cwru.sepia.environment.model.state.ResourceNode;                 // tree or gold
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;    // the "state" of that resource
import edu.cwru.sepia.environment.model.state.ResourceType;                 // what kind of resource units are carrying
import edu.cwru.sepia.environment.model.state.State.StateView;              // current state of the game
import edu.cwru.sepia.environment.model.state.Unit.UnitView;                // current state of a unit
import edu.cwru.sepia.util.Direction;                                       // directions for moving in the map


import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.type.PrimitiveType;


// JAVA PROJECT IMPORTS



public class OpenLoopAgent
    extends Agent
{

	private Integer myUnitId;               // id of the unit we control (used to lookop UnitView from state)
	private Integer enemyUnitId;            // id of the unit our opponent controls (used to lookup UnitView from state)
    private Integer goldResourceNodeId;     // id of one gold deposit in game (used to lookup ResourceView from state)


    /**
     * The constructor for this type. The arguments (including the player number: id of the team we are controlling)
     * are contained within the game's xml file that we are running. We can also add extra arguments to the game's xml
     * config for this agent and those will be included in args.
     */
	public OpenLoopAgent(int playerNum, String[] args)
	{
		super(playerNum); // make sure to call parent type (Agent)'s constructor!

        this.myUnitId = null;
        this.enemyUnitId = null;
        this.goldResourceNodeId = null;
		System.out.println("Constructed OpenLoopAgent");
	}

    /////////////////////////////// GETTERS AND SETTERS (this is Java after all) ///////////////////////////////
	public final Integer getMyUnitId() { return this.myUnitId; }
	public final Integer getEnemyUnitId() { return this.enemyUnitId; }
    public final Integer getGoldResourceNodeId() { return this.goldResourceNodeId; }

    private void setMyUnitId(Integer i) { this.myUnitId = i; }
    private void setEnemyUnitId(Integer i) { this.enemyUnitId = i; }
    private void setGoldResourceNodeId(Integer i) { this.goldResourceNodeId = i; }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Agents in Sepia have five abstract methods that we must override. The first three are the most important:
     *    - initialStep
     *    - middleStep
     *    - terminalStep
     * When a new game is started, the Agent objects are created using their constructors. However, the game itself
     * has not been started yet, so if an Agent wishes to keep track of units/resources in the game, that info is
     * not available to it yet. The first turn of the game, each agent's initialStep method is called by sepia
     * automatically and provided the *initial* state of the game. So, this method is often used as a
     * secondary constructor to "discover" the ids of units we control, the number of players in the game, etc.
     *
     * This method produces a mapping from the ids of units we control to the actions those units will take this turn.
     * Note we are only allowed to map units that we *control* (their ids technically) to actions, we are not allowed
     * to try to control units that aren't on our team!
     */
	@Override
	public Map<Integer, Action> initialStep(StateView state,
                                            HistoryView history)
	{

		// discover friendly units
        Set<Integer> myUnitIds = new HashSet<Integer>();
		for(Integer unitID : state.getUnitIds(this.getPlayerNumber())) // for each unit on my team
        {
            myUnitIds.add(unitID);
        }

        // check that we only have a single unit
        if(myUnitIds.size() != 1)
        {
            System.err.println("[ERROR] OpenLoopAgent.initialStep: DummyAgent should control only 1 unit");
			System.exit(-1);
        }

		// check that all units are of the correct type...in this game there is only "melee" or "footman" units
        for(Integer unitID : myUnitIds)
        {
		    if(!state.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("footman"))
		    {
			    System.err.println("[ERROR] OpenLoopAgent.initialStep: DummyAgent should control only footman units");
			    System.exit(-1);
		    }
        }

		// check that there is another player and get their player number (i.e. the id of the enemy team)
		Integer[] playerNumbers = state.getPlayerNumbers();
		if(playerNumbers.length != 2)
		{
			System.err.println("ERROR: Should only be two players in the game");
			System.exit(1);
		}
		Integer enemyPlayerNumber = null;
		if(playerNumbers[0] != this.getPlayerNumber())
		{
			enemyPlayerNumber = playerNumbers[0];
		} else
		{
			enemyPlayerNumber = playerNumbers[1];
		}

		// get the units controlled by the other player...similar strategy to how we discovered our (friendly) units
		Set<Integer> enemyUnitIds = new HashSet<Integer>();
		for(Integer unitID : state.getUnitIds(enemyPlayerNumber))
        {
            enemyUnitIds.add(unitID);
        }

        // in this game each team should have 1 unit
        if(enemyUnitIds.size() != 1)
        {
            System.err.println("[ERROR] OpenLoopAgent.initialStep: Enemy should control only 1 unit");
			System.exit(-1);
        }


		// check that the enemy only controlls "melee" or "footman" units
        for(Integer unitID : enemyUnitIds)
        {
		    if(!state.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("footman"))
		    {
			    System.err.println("[ERROR] OpenLoopAgent.initialStep: Enemy should only control footman units");
			    System.exit(-1);
		    }
        }

        // TODO: discover the id of the gold resource! Check out the documentation for StateView:
        // http://engr.case.edu/ray_soumya/Sepia/html/javadoc/edu/cwru/sepia/environment/model/state/State-StateView.html
        // get all resource ids, i.e. in this case, we should have only one resource id, for a gold mine
        Set<Integer> ResourceNodeIds = new HashSet<Integer>();
		for(Integer ResourceNodeId : state.getAllResourceIds()) // for each unit on my team
        {
            ResourceNodeIds.add(ResourceNodeId);
        }

        // check that we only have a single resource id
        if(ResourceNodeIds.size() != 1)
        {
            System.err.println("[ERROR] OpenLoopAgent.initialStep: Resource should only have one id, for a gold mine");
			System.exit(-1);
        }
        Integer goldResourceNodeId = ResourceNodeIds.iterator().next();
        System.out.println("got goldResourceNodeId");

        // set our fields
        this.setMyUnitId(myUnitIds.iterator().next());
        this.setEnemyUnitId(enemyUnitIds.iterator().next());
        this.setGoldResourceNodeId(goldResourceNodeId);
        System.out.println("myUnitIds: "+getMyUnitId());
        System.out.println("enemyUnitIds: "+getEnemyUnitId());
        System.out.println("goldResourceNodeId: "+getGoldResourceNodeId());

        // ask middlestep what actions each unit should do. Unless we need the units to do something very specific
        // on the first turn of the game, we typically put all "action logic" in middlestep and call it from here.
		return this.middleStep(state, history);
	}

    /**
     * If Agent::initialStep is called on the first turn of the game, then "middlestep" is what is called for every
     * other turn of the game. This method is called automatically by Sepia for you, but you are also free to
     * call it whenever you want (as we did in Agent::initialStep). As mentioned previously, Agent::middleStep
     * traditionally contains all "action logic" inside of it UNLESS we need units to do something very specific
     * on the first turn of the game.
     * 
     * The agent first moves to a square adjacent to the gold deposit, 
     * and once they are adjacent to the gold, issue primitive gather actions to gather all of the gold from the deposit.
     * 
     * When all of the gold is gone, the agent then moves to a square adjacent to the enemy unit, 
     * issues primitive move actions to follow a predetermined path to get by the enemy,
     * and when they are adjacent to the enemy, issue primitive attack actions to attack the enemy unit.
     */
	@Override
	public Map<Integer, Action> middleStep(StateView state,
                                           HistoryView history)
    {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();
        Action action = null;
        System.out.println("Action starts!");

        // TODO: your code to give your unit actions for this turn goes here!
        if(state.getResourceNode(this.goldResourceNodeId) != null){  // the gold mine is there to collect
            if(state.canSee(1, 1) && !state.isUnitAt(0, 2)){ // gold is NOT adjacent
                System.out.println("the gold is NOT adjacent");
                action = Action.createPrimitiveMove(this.myUnitId, Direction.NORTH);
                System.out.println("Move one unit to the north");
            } else {
                System.out.println("the gold IS adjacent");
                action = Action.createPrimitiveGather(this.myUnitId, Direction.NORTHEAST);
                System.out.println("Collect the gold");
            }
        } else if(state.getUnit(this.enemyUnitId) != null){  // gold is gone, time to slay enemy
            if(state.isUnitAt(0, 2) || state.isUnitAt(0, 1)){ // player unit not hit the north wall
                action = Action.createPrimitiveMove(this.myUnitId, Direction.NORTH);
                System.out.println("Move one unit to the north");
            } else if(!state.isUnitAt(6, 0)){//(!state.canSee(7, 0)) { // switch moving direction
                action = Action.createPrimitiveMove(this.myUnitId, Direction.EAST);
                System.out.println("Move one unit to the east");
            } else {
                action = Action.createPrimitiveAttack(this.myUnitId, this.enemyUnitId);
                System.out.println("Attack enemy");
            }
        } 
        
        actions.put(this.myUnitId, action);
        System.out.println("Action put");
        return actions;
	}

    /**
     * Finally, when the game ends, Sepia will call this method automatically for you. This method is traditionally
     * used as kind of a "post-mortem" analysis of the game (i.e. what was the outcome, how well did we do, etc.)
     * Since the game has finished (we are given the last state of the game as well as the complete history), there
     * are no actions to issue, so this method returns nothing.
     */
    @Override
	public void terminalStep(StateView state,
                             HistoryView history)
    {
        System.out.println("Gold collected and enemy slayed!");
    }

    /**
     * The following two methods aren't really used by us much in this class. These methods are used to load/save
     * the Agent (for instance if our Agent "learned" during the game we might want to save the model, etc.). Until the
     * very end of this class we will ignore these two methods.
     */
    @Override
	public void loadPlayerData(InputStream is) {}

	@Override
	public void savePlayerData(OutputStream os) {}

}
