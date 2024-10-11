package src.labs.infexf.agents;

import java.util.Set;
import java.util.Stack;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;


// JAVA PROJECT IMPORTS


public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src, Vertex dst, StateView state) {
        float baseWeight = 1f; 
        float tempDist = 0;
        float minDist = 5000;
        int dstX = dst.getXCoordinate();
        int dstY = dst.getYCoordinate();

        Set<Integer> enemies = this.getOtherEnemyUnitIDs();
        if (enemies == null) 
        {
            return 1;
        }
        for (int id : enemies)
        {
    
            UnitView enemyView = state.getUnit(id);
            if (enemyView == null) continue;
            
            int enemyX = enemyView.getXPosition();
            int enemyY = enemyView.getYPosition();
            

            tempDist = (float) Math.sqrt(Math.pow(enemyX - dstX, 2) + Math.pow(enemyY - dstY, 2));
            
            minDist = Math.min(tempDist, minDist);

            if (minDist >= 6.25) 
            {
                baseWeight = 1f;
            }
            else if (minDist >= 5.5)
            {
                baseWeight = 10f;
            }
            else if (minDist >= 5)
            {
                baseWeight = 20f;
            }
            else if (minDist >= 4.5)
            {
                baseWeight = 40f;
            }
            else if (minDist >= 4)
            {
                baseWeight = 100f;
            }
            else if (minDist >= 3.5)
            {
                baseWeight = 150f;
            }
            else if (minDist >= 3)
            {
                baseWeight = 200f;
            }
            else if (minDist >= 2.5)
            {
                baseWeight = 300f;
            }
            else if (minDist >= 2)
            {
                baseWeight = 400f;
            }
            else if (minDist >= 1.5)
            {
                baseWeight = 500f;
            }
            else if (minDist >= 1)
            {
                baseWeight = 800f;
            }
            else
            {
                baseWeight = 800f;
            }
        }
        return baseWeight;
    }

    
    
    
    @Override
    public boolean shouldReplacePlan(StateView state) {
        int count = 0;
        for (Vertex v : this.getCurrentPlan())
        {
            Set<Integer> enemies = this.getOtherEnemyUnitIDs();
            if (enemies == null) 
            {
                return false;
            }
            for (int id : enemies)
            {
                UnitView enemyView = state.getUnit(id);
                if (enemyView == null) continue;

                int x = v.getXCoordinate();
                int y = v.getYCoordinate();
                int enemyX = enemyView.getXPosition();
                int enemyY = enemyView.getYPosition();

                int attackRadius = enemyView.getTemplateView().getRange();
                attackRadius += 3;

                if (Math.abs(x-enemyX) <= attackRadius && Math.abs(y-enemyY) <= attackRadius)
                {
                    return true;
                }
            }
            count ++;
            if (count >= 6){
                return false;
            }

        }
        return false;
    }
}
    
