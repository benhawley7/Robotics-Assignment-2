// Environment code for project doctor2018

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.logging.*;


public class ParamedicEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model
//    public static final int OBSTACLE = 32;

    private Logger logger = Logger.getLogger("doctor2018."+ParamedicEnv.class.getName());
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
    private RobotBayModel model;
//    private RobotBayView view;    
    
    private MapGUI mapView;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        //addPercept(ASSyntax.parseLiteral("percept(demo)"));
        model = new RobotBayModel();
//        view  = new RobotBayView(model);
//        model.setView(view);
        

        mapView = new MapGUI();
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        try {
        	if (action.getFunctor().equals("addVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addVictim(x,y);
                logger.info("adding victim at: "+x+","+y);
                mapView.addVictim(x, y);
                
            } else if (action.getFunctor().equals("setAgentLocation")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addAgent(x,y);
                logger.info("adding agent at: "+x+","+y);
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addObstacle(x,y);
                logger.info("adding obstacle at: "+x+","+y);
                mapView.addObstacle(x, y);
                
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addHospital(x,y);
                mapView.addHospital(x, y);
                logger.info("adding hospital at: "+x+","+y);
                
            } else if (action.getFunctor().equals("nextTarget")) {
            	int [] agentPos = model.getAgentPosition();
            	logger.info("Agent Position: " + agentPos[0] + " " + agentPos[1]);
            	ArrayList<int[]> potentialVictims = model.getPotentialVictimLocations();
            	ArrayList<int[]> obstaclesPos = model.getObstacleLocations();
              	logger.info("Potential Victims" + potentialVictims.get(0)[0] + ", " + potentialVictims.get(0)[1]);
            	Pathfinder p = new Pathfinder(GSize, GSize);
            	
            	// Josh Loves i lol
            	for (int i = 0; i < obstaclesPos.size(); i++) {
            		int[] obstaclePos = obstaclesPos.get(i);
            		p.updateCell(obstaclePos[0], obstaclePos[1], true);
            	}
            	    
            	int[] currentNearestPos = agentPos;
            	int currentShortestPath = Integer.MAX_VALUE; 
            	for (int j = 0; j < potentialVictims.size(); j++) {
            		int[] victimLocation = potentialVictims.get(j);
            		Pathfinder.Node[] path = p.getPath(agentPos[0], agentPos[1], victimLocation[0], victimLocation[1]);
            		if (path.length < currentShortestPath) {
            			logger.info(victimLocation[0] + ", " + victimLocation[1] + " is closer than " + currentNearestPos[0] + ", " + currentNearestPos[1]);
            			currentNearestPos = victimLocation;
            			currentShortestPath = path.length;
            		}
            	}
            	
            	logger.info("Nearest Neighbour is" + currentNearestPos[0] + "," + currentNearestPos[1]);
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

            // initial location of Obstacles
            // Note that OBSTACLE is defined in the model (value 4), as
            // is AGENT (2), but we have to define our own code for the
            // victim and hospital (uses bitmaps, hence powers of 2)
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

    }
//    
//    // ======================================================================
//    // This is a simple rendering of the map from the actions of the paramedic
//    // when getting details of the victim and obstacle locations
//    // You should not feel that you should use this code, but it can be used to
//    // visualise the bay layout, especially in the early parts of your solution.
//    // However, you should implement your own code to visualise the map.
//    class RobotBayView extends GridWorldView {
//
//        public RobotBayView(RobotBayModel model) {
//            super(model, "COMP329 6x6 Robot Bay", 300);
//            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
//            setVisible(true);
//            repaint();
//        }
//        
//        /** draw application objects */
//        @Override
//        public void draw(Graphics g, int x, int y, int object) {
//            switch (object) {
//            case ParamedicEnv.VICTIM:
//                drawVictim(g, x, y);
//                break;
//            case ParamedicEnv.HOSPITAL:
//                drawHospital(g, x, y);
//                break;
//           }
//        }
//        
//        public void drawVictim(Graphics g, int x, int y) {
//            //super.drawObstacle(g, x, y);
//            g.setColor(Color.black);
//            drawString(g, x, y, defaultFont, "V");
//        }
//
//        public void drawHospital(Graphics g, int x, int y) {
//            //super.drawObstacle(g, x, y);
//            g.setColor(Color.blue);
//            drawString(g, x, y, defaultFont, "H");
//        }
//    }
//    // ======================================================================
}
