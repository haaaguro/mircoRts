package bot2;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;


public class bot2 extends AbstractionLayerAI{
    UnitTypeTable nutt;
    UnitType workerType;
    UnitType baseType;

    public bot2(UnitTypeTable utt) {

        this(utt, new AStarPathFinding());
        // TODO Auto-generated constructor stub
    }

    public bot2(UnitTypeTable utt, PathFinding a_pf) {
        super(a_pf);
        reset(utt);
    }

    public void reset(UnitTypeTable a_utt) {
        nutt = a_utt;
        if (nutt!=null) {
            workerType = nutt.getUnitType("Worker");
            baseType = nutt.getUnitType("Base");
        }
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        List<Unit> units = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() == player) {
                units.add(u);
            }
        }
        for(Unit u:pgs.getUnits()) {
            if (u.getType()==baseType &&
                    u.getPlayer() == player &&
                    gs.getActionAssignment(u)==null) {
                baseBehavior(units,u,p,pgs);
            }

        }
        List<Unit> workers = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) {
            if (u.getType().canHarvest &&
                    u.getPlayer() == player) {
                workers.add(u);

            }
        }
        workerBehavior(workers,p,gs);
        return translateActions(player, gs);
    }

    private void workerBehavior(List<Unit> workers, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int resourcesUsed = 0;
        List<Unit> harvestWorkers = new LinkedList<Unit>();
        // List<Unit> harvestWorkers = new LinkedList<Unit>();
        List<Unit> freeWorkers = new LinkedList<Unit>();

        harvestWorkers.addAll(workers);
        if (workers.isEmpty()) return;


        if (harvestWorkers.size()>2) {
            freeWorkers.addAll(harvestWorkers.subList(2,harvestWorkers.size()));
            harvestWorkers=harvestWorkers.subList(0,1);
        }


        for(Unit u:harvestWorkers) harvestUnitBehavior(u,p,gs);
        for(Unit u:freeWorkers) attackUnitBehavior(u, p, gs);

    }

    private void baseBehavior(List<Unit> units, Unit u,Player p, PhysicalGameState pgs) {
        if (p.getResources()>=workerType.cost&&units.size()<6) {
            train(u, workerType);
        }

    }


    public void attackUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for(Unit u2:pgs.getUnits()) {
            if (u2.getPlayer()>=0 && u2.getPlayer()!=p.getID()) {
                int d = (int) Math.sqrt(Math.pow((u2.getX() - u.getX()),2) + Math.pow((u2.getY() - u.getY()),2));
                if (closestEnemy==null || d<closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy!=null) {
            attack(u,closestEnemy);
        }
    }

    public void harvestUnitBehavior(Unit u, Player p, GameState gs){
        PhysicalGameState pgs=gs.getPhysicalGameState();
        Unit closestBase = null;
        Unit closestResource = null;
        int closestDistance = 0;
        Unit harvestWorker=u;
        if (harvestWorker!=null) {
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestResource == null || d < closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                    harvest(harvestWorker, closestResource, closestBase);
                }
            }

            for(Unit u2:pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestBase==null || d<closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                        harvest(harvestWorker, closestResource, closestBase);
                    }
                }
            }

            /*if (closestResource!=null && closestBase!=null) {
                AbstractAction aa = getAbstractAction(harvestWorker);
                if (aa instanceof Harvest) {
                    Harvest h_aa = (Harvest)aa;
                    if (h_aa.target != closestResource || h_aa.base!=closestBase) harvest(harvestWorker, closestResource, closestBase);
                } else {
                    harvest(harvestWorker, closestResource, closestBase);
                }
            }
             */



        }


    }

    public AI clone() {
        return new bot2(nutt);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return new ArrayList<>();
    }

}
