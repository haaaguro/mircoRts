	package ai.Yui;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.evaluation.LanchesterEvaluationFunction;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;


public class Yui extends AbstractionLayerAI{
	UnitTypeTable nutt;
	UnitType workerType;
	UnitType baseType;
	UnitType barracksType;
	UnitType heavyType;
	UnitType rangedType;
	UnitType lightType;
	boolean workerRush = false;
	int maxHarvest = 3;
	boolean defense = true;
	boolean Troopattack = false;
	boolean top = false;
	List<Unit> troop = new LinkedList<Unit>();
	int building = 0;
	Unit base = null;
	public Yui(UnitTypeTable utt) {
		
		this(utt, new AStarPathFinding());
		// TODO Auto-generated constructor stub
	}

	public Yui(UnitTypeTable utt, AStarPathFinding aStarPathFinding) {
        super(aStarPathFinding);
        reset(utt);
	}

	@Override
	public void reset(UnitTypeTable a_utt) {
		nutt = a_utt;
	        if (nutt!=null) {
	            workerType = nutt.getUnitType("Worker");
	            baseType = nutt.getUnitType("Base");
	            barracksType = nutt.getUnitType("Barracks");
	            heavyType = nutt.getUnitType("Heavy");
	            rangedType = nutt.getUnitType("Ranged");
	            lightType = nutt.getUnitType("Light");
	        }
	}

	@Override
	public PlayerAction getAction(int player, GameState gs) throws Exception {
		 PhysicalGameState pgs = gs.getPhysicalGameState();
		 Player p = gs.getPlayer(player);
		 int size = pgs.getWidth()*pgs.getHeight();
		 
		 if(size<=256) {
			 workerRush = true;
			 maxHarvest = 1;
			 defense = false;
		 }
		 List<Unit> units = new LinkedList<Unit>();
		 List<Unit> Runits = new LinkedList<Unit>();
		 List<Unit> Lunits = new LinkedList<Unit>();
		 List<Unit> Hunits = new LinkedList<Unit>();
		 List<Unit> workers = new LinkedList<Unit>();
		 List<Unit> enemy = new LinkedList<Unit>();
		 
		 for(Unit u:pgs.getUnits()) {
	            if (u.getType().canHarvest && 
	                u.getPlayer() == player) {
	            	units.add(u);
	            } 
	            if (u.getType().canHarvest && 
		                u.getPlayer() == player) {
		                workers.add(u);
		            }
	            if (u.getType()== lightType && 
		                u.getPlayer() == player) {
		            	Lunits.add(u);
		            	troop.add(u);
		            }
	            if (u.getType()== rangedType && 
		                u.getPlayer() == player) {
		            	Runits.add(u);
		            	 troop.add(u);
		            }
	            if(u.getPlayer()!= player) {
	            		enemy.add(u);
	            }
	  /*          if (u.getType().canAttack &&!u.getType().canHarvest&&
		                u.getPlayer() == player) {
		            	troop.add(u);
		            }    */ 
	           if(u.getType()==baseType&&u.getPlayer()==player) {
	        	   base =u;
	        	   if(base.getX()<pgs.getWidth()/2) {
	        		   top = true;
	        	   }
	           }
	        }
		 
		 LanchesterEvaluationFunction lev = new LanchesterEvaluationFunction();
		 float evaluate = lev.evaluate(0, 1, gs); 
		 for(Unit u:pgs.getUnits()) {
			 	if (u.getType()==baseType && 
			 		u.getPlayer() == player && 
			 		gs.getActionAssignment(u)==null) {
			 		baseBehavior(workers,u,p,pgs,evaluate);
			 	}
         if (u.getType() == barracksType
                 && u.getPlayer() == player
                 && gs.getActionAssignment(u) == null) {
             	 barracksBehavior(u, p, pgs,Lunits,Runits);
         }
		 }
		 workersBehavior(workers,p,pgs,units);
		// if(troop.size()>0) {
			// for(Unit u:troop) attackUnitBehavior(u, p, pgs);
		// } 
		 
			 troopBahaviour(troop,p,gs,units,base);
		
		  
		return translateActions(player, gs);
	}
	   public void troopBahaviour(List<Unit> troop,Player p, GameState gs,List<Unit> units, Unit base)
	     {	 PhysicalGameState pgs = gs.getPhysicalGameState();
	     	 ResourceUsage ru = gs.getResourceUsage();
	         List<Unit> freeTroop = new LinkedList<Unit>();
	         if (troop.size()>0) {
	        	 if(!Troopattack) {
	        		 for(Unit u:troop) defendBehaviour(u, p, pgs);
	        		 }
	             while(troop.size()>0) {
	                 freeTroop.add(troop.remove(0));

	                 if (freeTroop.size() >=2) {
	                	 Troopattack = true;
	                     for (Unit u : freeTroop) attackUnitBehavior(u, p, pgs);
	                     for (Unit u : freeTroop) freeTroop.removeAll(freeTroop);
	                 }else {
	            	/* Unit target = base;
	                	 for(int i=0;i<freeTroop.size();i++) {
	                		Unit unit = freeTroop.get(i);
	                		if(!top) {
	                		move(unit, target.getX()-1-i,target.getY()); 
	                		System.out.println(target.getX());
	                		}else {
	                		move(unit, target.getX()+1+i,target.getY());  
		                	 }
	                	 }
	                */ }

	             }
	          }


	     }
	   private void defendBehaviour(Unit u, Player p, PhysicalGameState pgs)
	    {
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
	        if (closestEnemy!=null&&closestDistance<=pgs.getWidth()/4) {
	            attack(u,closestEnemy);
	        }
	    }
	private void barracksBehavior(Unit u, Player p, PhysicalGameState pgs,List<Unit> Lunits,List<Unit> Runits) {
		 if (p.getResources() >= rangedType.cost&&Runits.size()<2) {
	            train(u, rangedType);
	           
	        }
		 else if (p.getResources() >= lightType.cost) {
	            train(u, lightType);
	        }
	}

	public void workersBehavior(List<Unit> workers,Player p, PhysicalGameState pgs,List<Unit> units) {
		int nbarracks = 0;
		List<Unit> harvestWorkers = new LinkedList<Unit>();
		List<Unit> freeWorkers = new LinkedList<Unit>();
		freeWorkers.addAll(workers);
		if (workers.isEmpty()) return;
		while (freeWorkers.size()>0 && harvestWorkers.size()<maxHarvest) {
			 harvestWorkers.add(freeWorkers.remove(0));
		 }
		 for (Unit u2 : pgs.getUnits()) {
	            if (u2.getType() == barracksType
	                    && u2.getPlayer() == p.getID()) {
	                nbarracks++;
	            }
	        }
		if (harvestWorkers.size()>0) {
			for(Unit harvestWorker: harvestWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            List<Integer> reservedPositions = new LinkedList<Integer>();
            for(Unit u2:pgs.getUnits()) {
                if (u2.getType().isResource) { 
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestResource==null || d<closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                    harvest(harvestWorker, closestResource, closestBase);
                }
            }
            closestDistance = 0;
            for(Unit u2:pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) { 
                    int d = Math.abs(u2.getX() - harvestWorker.getX()) + Math.abs(u2.getY() - harvestWorker.getY());
                    if (closestBase==null || d<closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                    harvest(harvestWorker, closestResource, closestBase);
                }
            }
            
            if(nbarracks==0&&!workerRush) {
            	if (p.getResources() >= barracksType.cost && !freeWorkers.isEmpty()&&freeWorkers.size()>=2) {
            		
                    Unit u = freeWorkers.remove(freeWorkers.size()-1-building);
                    int x = base.getX();
                    int y = base.getY();
                    int x1 = u.getX();
                    int y1 = u.getY();
                    if(!top) {
                    	x=x-2;
                    	y=y-building*2;
                    }else {
                    	x=x1+2+building*2;
                    	y=y1+2;
                    }
                    if(building<1) {
                    building++;
                    }
                    buildIfNotAlreadyBuilding(u,barracksType,x,y,reservedPositions,p,pgs);
                }
            }
            if(!Troopattack) {
            	 for(Unit u:freeWorkers) defendBehaviour(u, p, pgs);
            }
            if(Troopattack||workerRush) {
           for(Unit u:freeWorkers) attackUnitBehavior(u, p, pgs);
           // troop.addAll(freeWorkers);
            }
			}
        }
	}

	private void attackUnitBehavior(Unit u, Player p, PhysicalGameState pgs) {
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

	private void baseBehavior( List<Unit> workers,Unit u,Player p, PhysicalGameState pgs,float evaluate) {
		if(workerRush == false) {
		if(workers.size()<5) {
			if (p.getResources()>=workerType.cost) {
				  train(u, workerType);
			}
		} 
		}else if(workerRush==true) {
			if (p.getResources()>=workerType.cost) {
				 train(u, workerType);
			}
				
		}
	}

	public AI clone() {
		return new Yui(nutt);
	}

	@Override
	public List<ParameterSpecification> getParameters() {
		return new ArrayList<>();
	}
	
}
