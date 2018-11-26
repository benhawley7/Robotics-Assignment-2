// Environment code for project doctor2018

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.logging.*;

public class ParamedicEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model

    private Logger logger = Logger.getLogger("doctor2018."+ParamedicEnv.class.getName());
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
//    private RobotBayModel model;
//    private RobotBayView view;    
    
    private MapGUI mapView;

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        //addPercept(ASSyntax.parseLiteral("percept(demo)"));
//        model = new RobotBayModel();
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
//                model.addVictim(x,y);
                logger.info("adding victim at: "+x+","+y);
                mapView.addVictim(x, y);
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
//                model.addObstacle(x,y);
                logger.info("adding obstacle at: "+x+","+y);
                mapView.addObstacle(x, y);
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
//                model.addHospital(x,y);
                mapView.addHospital(x, y);
                logger.info("adding hospital at: "+x+","+y);
            } else {
                logger.info("executing: "+action+", but not implemented!");
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
//    class RobotBayModel extends GridWorldModel {
//
//        private RobotBayModel() {
//            super(GSize, GSize, 1);	// The third parameter is the number of agents
//
//            // initial location of Obstacles
//            // Note that OBSTACLE is defined in the model (value 4), as
//            // is AGENT (2), but we have to define our own code for the
//            // victim and hospital (uses bitmaps, hence powers of 2)
//        }
//        
//        void addVictim(int x, int y) {
//            add(VICTIM, x, y);
//        }
//        void addHospital(int x, int y) {
//            add(HOSPITAL, x, y);
//        }
//        void addObstacle(int x, int y) {
//            add(OBSTACLE, x, y);
//        }
//
//    }
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
