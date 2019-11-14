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
	boolean harvestflag = true;
	List<Unit> troop = new LinkedList<Unit>();
	int building = 0;
	Unit base = null;
	Unit enemybase = null;
	int ResourceUsed=0;
	int attacker=2;
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
		 List<Unit> Resource = new LinkedList<Unit>();
		 List<Unit> enemy = new LinkedList<Unit>();
		 List<Unit> enemyworkers = new LinkedList<Unit>();
		 List<Unit> enemyRanged = new LinkedList<Unit>();
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
	            if(u.getPlayer()!=player&&u.getType()==baseType) {
	            	enemybase = u;
	            }
	            if(u.getPlayer()!=player&&u.getType()==workerType) {
	            	enemyworkers.add(u);
	            }
	            if(u.getPlayer()!=player&&u.getType()==rangedType) {
	            	enemyRanged.add(u);
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
	           if(u.getPlayer()==-1) {
	        	   if(!top&&base!=null) {
	        		   if(u.getX()<base.getX()) {
	        			   Resource.add(u);
	        		   }
	        	   }else if(top&&base!=null) {
	        		   if(u.getX()>base.getX()) {
	        			   Resource.add(u);
	        		   }
	        	   }
	           }
	        }
		 if(Resource.size()==0&&ResourceUsed>10) {
			
			 maxHarvest=0;
		 }
		// LanchesterEvaluationFunction lev = new LanchesterEvaluationFunction();
		 float evaluate = 0;// lev.evaluate(0, 1, gs); 
		 for(Unit u:pgs.getUnits()) {
			 	if (u.getType()==baseType && 
			 		u.getPlayer() == player && 
			 		gs.getActionAssignment(u)==null) {
			 		baseBehavior(workers,u,p,pgs,enemyworkers);
			 	}
         if (u.getType() == barracksType
                 && u.getPlayer() == player
                 && gs.getActionAssignment(u) == null) {
             	 barracksBehavior(u, p, pgs,Lunits,Runits,enemyRanged);
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
	                 if(maxHarvest>0) {
	                 if (freeTroop.size() >=2) {
	                	 Troopattack = true;
	                     for (Unit u : freeTroop) attackUnitBehavior(u, p,pgs);
	                     for (Unit u : freeTroop) freeTroop.removeAll(freeTroop);
	                 }
	                 }else {
	                	 for (Unit u : freeTroop) attackUnitBehavior(u, p, pgs);
	                     for (Unit u : freeTroop) freeTroop.removeAll(freeTroop);
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
	        if(!top) {
	        	if(closestEnemy!=null&&closestDistance<=pgs.getWidth()/3) {
		            attack(u,closestEnemy);
	        }
	        }
	    }
	private void barracksBehavior(Unit u, Player p, PhysicalGameState pgs,List<Unit> Lunits,List<Unit> Runits, List<Unit> enemyRanged) {
		 if (p.getResources() >= rangedType.cost&&Runits.size()<2) {
	            train(u, rangedType);
	            ResourceUsed=ResourceUsed+2;
	           
	        }
		 else if (p.getResources() >= lightType.cost) {
	            train(u, lightType);
	            ResourceUsed=ResourceUsed+2;
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
            if(!top) {
            if(!workerRush&&building<2) {
            	if (p.getResources() >= barracksType.cost && freeWorkers.size()>=2) {
                    Unit u = freeWorkers.remove(freeWorkers.size()-1-building);
                    int x = base.getX();
                    int y = base.getY();
                    int x1 = u.getX();
                    int y1 = u.getY();
                    if(!top) {
                    	x=x-1+building;
                    	y=y+1+building*2;
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
            }else {
            	 if(nbarracks==0&&!workerRush) {
                 	if (p.getResources() >= barracksType.cost && freeWorkers.size()>=2) {
                         Unit u = freeWorkers.remove(freeWorkers.size()-1-building);
                         int x = base.getX();
                         int y = base.getY();
                         int x1 = u.getX();
                         int y1 = u.getY();
                         if(!top) {
                         	x=x-1+building;
                         	y=y+1+building*2;
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
            }
            if(!Troopattack) {
            	 for(Unit u:freeWorkers) defendBehaviour(u, p, pgs);
            }
            if(Troopattack||workerRush) {
            	if(!workerRush&&attacker>0&&ResourceUsed>20) {
            	Unit worker = freeWorkers.remove(0);
            	attackWorkers(worker,p,pgs,enemybase);
            	attacker--;
            	}
           for(Unit u:freeWorkers) attackUnitBehavior(u, p,pgs);
           //
            }
            if(maxHarvest == 0) {
            	troop.addAll(harvestWorkers);
            }
			}
        }
	}

	private void attackWorkers(Unit worker, Player p, PhysicalGameState pgs, Unit enemybase) {
		int x = worker.getX();
		int y = worker.getY();
		int targetX = enemybase.getX();
		int targetY = enemybase.getY();
		if(x<targetX) {
			move(worker,targetX,y);
		}else{
			move(worker,x,targetY);
		}
	}

	private void attackUnitBehavior(Unit u, Player p,PhysicalGameState pgs) {
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

	private void baseBehavior( List<Unit> workers,Unit u,Player p, PhysicalGameState pgs,List<Unit> enemyworkers) {
		int maxworkers = 5;
		if(workerRush == false) {
			if(enemyworkers.size()>=6) {
				maxworkers = enemyworkers.size();
			}
			if(enemyworkers.size()>8){
				maxworkers= 8;
			}
			if (p.getResources()>=workerType.cost&&workers.size()<maxworkers) {
				  train(u, workerType);
				  ResourceUsed++;
			} 
		}else if(workerRush==true) {
			if (p.getResources()>=workerType.cost) {
				 train(u, workerType);
				 ResourceUsed++;
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
