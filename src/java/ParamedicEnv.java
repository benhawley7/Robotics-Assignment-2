// Environment code for project doctor2018

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;

import jason.asSemantics.*;


public class ParamedicEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model
    // Obstacle is 32
    public static final int CRITICAL = 64;
    public static final int NONCRITICAL = 128;
    

    private Logger logger = Logger.getLogger("doctor2018."+ParamedicEnv.class.getName());
    private RobotBayModel model;
    private MapGUI mapView;
    private Client client;
    
    private boolean isSimulatorMode = false;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        
        if (isSimulatorMode == false) {
            try {
    			client = new Client();
    		} catch (Exception e) {
    		
    		}
        }

        model = new RobotBayModel();
        mapView = new MapGUI();
 
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
    	// Before we do anything, lets make sure we are connected to the robot
//    	if (isSimulatorMode == false && client.isConnected() == false) {
//    		do {
//        		try {
//    				client.connectToRobot();
//    			} catch (InterruptedException e) {
//
//    			}
//    		} while (client.isConnected() == false);
//    	}
    	
        try {
        	if (action.getFunctor().equals("addVictim")) {
        		// Get x and y terms
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                
                // Add the victim to the Grid World Model
                model.addVictim(x,y);
                logger.info("adding victim at: "+x+","+y);
                
                // Update the map view
                mapView.updateMap(model);
                
            } else if (action.getFunctor().equals("addObstacle")) {
            	// Get x and y terms
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                
                // Add the obstacle to the Grid World Model
                model.addObstacle(x,y);
                logger.info("adding obstacle at: "+x+","+y);
                
                // Update the map view
                mapView.updateMap(model);
                
            } else if (action.getFunctor().equals("addHospital")) {
            	// Get x and y terms
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                
                // Add the hospital to the grid world model
                model.addHospital(x,y);
                logger.info("adding hospital at: "+x+","+y);
                
                // Update the map view
                mapView.updateMap(model);
                
            } else if (action.getFunctor().equals("moveTo")) {
            	// Get x and y terms
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                logger.info("Moving to " + x + ", " + y);
                
                // Create a new pathfinder
                Pathfinder p = new Pathfinder(GSize, GSize);
                
                // Update the pathfinder with the obstacle locations
                ArrayList<int[]> obstaclesPos = model.getObstacleLocations();
            	for (int i = 0; i < obstaclesPos.size(); i++) {
            		int[] obstaclePos = obstaclesPos.get(i);
            		p.updateCell(obstaclePos[0], obstaclePos[1], true);
            	}
            	
            	// Get the current position of the agent
            	int[] agentPos = model.getAgentPosition();
  
            	// Get the path from the agent to the target location
            	Pathfinder.Node[] path = p.getPath(agentPos[0], agentPos[1], x, y);
            
            	// Go through the path, starts at 1 because index 0 is null for some reason
                for (int i = 1; i < path.length; i++) {

                	if (isSimulatorMode == false) {
                    	/**
                    	 * Here is where we will send the move command to the EV3
                    	 */
                		client.sendData("MOVE:" + path[i].x + "," + path[i].y);
                		String data = client.awaitData();
                		logger.info(data);
                	}
	
                	// Update the agents position to the next move
                	model.setAgPos(0, path[i].x, path[i].y);
                	
                	// Update the map with the new location
                	mapView.updateMap(model);
                	
                	// Sleep for testing purposes
                	Thread.sleep(1000);
                }
	
                model.setAgPos(0, x, y);
                mapView.updateMap(model);
            	
            } else if (action.getFunctor().equals("nextTarget")) {
            	int [] agentPos = model.getAgentPosition();
            	ArrayList<int[]> potentialVictims = model.getPotentialVictimLocations();
              	
              	// Create a pathfinder
            	Pathfinder p = new Pathfinder(GSize, GSize);
            	
            	// Update the obstacles using the model
            	p.updateCells(model);
            	
            	// Default, we say the nearest neighbour is the agents current position
            	int[] currentNearestPos = agentPos;
            	int currentShortestPath = Integer.MAX_VALUE; 
            	
            	// Find the nearest neighbour victim
            	for (int j = 0; j < potentialVictims.size(); j++) {
            		int[] victimLocation = potentialVictims.get(j);
            		Pathfinder.Node[] path = p.getPath(agentPos[0], agentPos[1], victimLocation[0], victimLocation[1]);
            		if (path.length < currentShortestPath) {
            			logger.info(victimLocation[0] + ", " + victimLocation[1] + " is closer than " + currentNearestPos[0] + ", " + currentNearestPos[1]);
            			currentNearestPos = victimLocation;
            			currentShortestPath = path.length;
            		}
            	}
            	
            	// Update the agents percept of nearest neighbour
            	Literal nearest = Literal.parseLiteral("nearest("+ currentNearestPos[0] + "," + currentNearestPos[1] + ")");
            	addPercept("paramedic", nearest);
            	
            	
            } else if (action.getFunctor().equals("inspectVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
            	/**
            	 * START TEMP TEST CODE
            	 */
            	
//                critical(2,3).
//                ~critical(4,5).
//                ~critical(5,1).
                
                if (x == 2 && y == 3) {
                	// Critical
                	model.addCritical(x, y);
                	
                    Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", burgandy)");
                    addPercept("paramedic", location);
	
                } else if (x == 4 && y == 5) {
                	// Not Critical
                	model.addNonCritical(x, y);
                    Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
                    addPercept("paramedic", location);
                } else if (x == 5 && y == 1) {
                	// Not Critical
                	model.addNonCritical(x, y);
                    Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
                    addPercept("paramedic", location);
                } else {
                	Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", white)");
                    addPercept("paramedic", location);
                }
        	
            	mapView.updateMap(model);
            	/**
            	 * END TEMP TEST CODE
            	 */
            	
            	
            	
//            	if (isSimulatorMode == false) {
//                	/**
//                	 * Here is where we will send the Colour Scan Command to the EV3
//                	 */
//            		client.sendData("SCAN:COLOUR");
//            		String colour = client.awaitData();
//            		
//            		if (colour == "White") {
//            			// No Victim
//            		} else if (colour == "Red") {
//            			// Critical Victim
//            		} else if (colour == "Cyan") {
//            			// Non Critical Victim
//            		} else {
//            			// We don't care if its any other colour
//            		}
//            	}
            	
            } else if (action.getFunctor().equals("removeVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.removeVictim(x, y);
                mapView.updateMap(model);
                
            } else if (action.getFunctor().equals("pickUpVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
            	model.removeVictim(x, y);
            	mapView.updateMap(model);
            	
            	// Update GUI to show that we are holding a victim
            	
            } else if (action.getFunctor().equals("putDownVictim")) {
            	// Reset the GUI to show we have put down a victim     
            	
            	
            } else if (action.getFunctor().equals("finishRescueMission")) {
            	// Finish the mission
            	
            } else {
                logger.info("executing: "+action+", but not implemented! Lel");
                return true;
                // Note that technically we should return false here.  But that could lead to the
                // following Jason error (for example):
                // [ParamedicEnv] executing: addObstacle(2,2), but not implemented!
                // [paramedic] Could not finish intention: intention 6: 
                //    +location(obstacle,2,2)[source(doctor)] <- ... addObstacle(X,Y) / {X=2, Y=2, D=doctor}
                // This is due to the action failing, and there being no alternative.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        informAgsEnvironmentChanged();
        return true;       
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
    
    // ======================================================================
    class RobotBayModel extends GridWorldModel {
     	
        private RobotBayModel() {
            super(GSize, GSize, 1);	// The third parameter is the number of agents
    
            setAgPos(0, 0, 0);
            Literal location = Literal.parseLiteral("location(self, 0, 0)");
            addPercept("paramedic", location);

        }
        
        public int[] getAgentPosition() {
        	int[] agentPos = {-1, -1};
          	// Find the position of the agent
        	for (int x = 0; x < GSize; x++) {
        		for (int y = 0; y < GSize; y++) {
        			if (model.hasObject(AGENT, x, y) == true) {
        				agentPos[0] = x;
        				agentPos[1] = y;
        			}
        		}
        	}
			return agentPos;
        }
        
        public ArrayList<int[]> getObstacleLocations() {
          	// Find the position of the agent
         	ArrayList<int[]> obstacles = new ArrayList<int[]>();
        	for (int x = 0; x < GSize; x++) {
        		for (int y = 0; y < GSize; y++) {
        			if (model.hasObject(OBSTACLE, x, y) == true) {
        				int[] victimPos = {x,y};
        				obstacles.add(victimPos);
        			}
        		}
        	}
        	return obstacles;
        }
        
        public ArrayList<int[]> getPotentialVictimLocations() {
        	ArrayList<int[]> victims = new ArrayList<int[]>();
        	for (int x = 0; x < GSize; x++) {
        		for (int y = 0; y < GSize; y++) {
        			if (model.hasObject(VICTIM, x, y) == true) {
        				int[] victimPos = {x,y};
        				victims.add(victimPos);
        			}
        		}
        	}
        	return victims;
        }
        
        public ArrayList<int[]> getLocations(int type) {
        	ArrayList<int[]> locations = new ArrayList<int[]>();
        	for (int x = 0; x < GSize; x++) {
        		for (int y = 0; y < GSize; y++) {
        			if (model.hasObject(type, x, y) == true) {
        				int[] victimPos = {x,y};
        				locations.add(victimPos);
        			}
        		}
        	}
        	return locations;
        }
        
        
        public int[] getHospitalLocation() {
        	int[] l = {0, 0};
        	return l;
        }
        
        void addAgent(int x, int y) {
        	add(AGENT, x, y);
        }
        
        void addVictim(int x, int y) {
            add(VICTIM, x, y);
        }
        void addHospital(int x, int y) {
            add(HOSPITAL, x, y);
        }
        void addObstacle(int x, int y) {
            add(OBSTACLE, x, y);
        }
        
        void removeVictim(int x, int y) {
        	remove(VICTIM, x, y);
        }
        
        void addCritical(int x, int y) {
        	add(CRITICAL, x, y);
        }
        
        void addNonCritical(int x, int y) {
        	add(NONCRITICAL, x, y);
        }

    }
}