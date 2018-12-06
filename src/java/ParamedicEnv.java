
// Environment code for project doctor2018

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;

import java.util.Random;
import java.util.Stack;

public class ParamedicEnv extends Environment {

	public static final int GSize = 6; // The bay is a 6x6 grid
	public static final int HOSPITAL = 8; // hospital code in grid model
	public static final int VICTIM = 16; // victim code in grid model
	// Obstacle is 32
	public static final int CRITICAL = 64;
	public static final int NONCRITICAL = 128;

	private Logger logger = Logger.getLogger("doctor2018." + ParamedicEnv.class.getName());
	private RobotBayModel model;
	private MapGUI mapView;
	private Client client;
	private int perceptIndex = 0;

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
		if (isSimulatorMode == false) {
			if (client.isConnected() == false) {
				client.connectToRobot();
			}
		}

		try {
			if (action.getFunctor().equals("addVictim")) {
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the victim to the Grid World Model
				model.addVictim(x, y);
				logger.info("adding victim at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("localise")) {
				Literal localised = Literal.parseLiteral("localised");
//				if (isSimulatorMode == true) {
//					addPercept(localised);
//					return true;
//				}
//	
				// Create a particle filter
				ParticleFilter filter = new ParticleFilter(GSize, GSize);
				
				// Get the locations of the obstacles in the arena
				ArrayList<int[]> obstacleLocations = model.getObstacleLocations();
				
				// For obstacle, add the location to the filter
				obstacleLocations.forEach((obstacle) -> {
					filter.addObstacle(obstacle[0], obstacle[1]);
				});
				
				// Update map with filter data
				mapView.updateFilter(filter.toString());
				
				// Store the direction we just moved in
				int directionLastMoved = 0;
			
				// While we have more than one particle
				outer: while (filter.getNumberParticles() > 1) {
					
					// Get the colour of the current position and update the particle filter
					client.sendData("SCAN:COLOUR");
					String colour = client.awaitData();
					logger.info("Cell is " + colour);
					eliminateParticlesByColour(filter, colour);
					mapView.updateFilter(filter.toString());
					Thread.sleep(5000);
					
					// Array of booleans to store whether we can move in a direction
					// based upon whether there is an obstacle or wall
					boolean[] validDirection = new boolean[4];
					
					// Stack to store the index of the valid directions, excluding the cameFrom direction
					Stack<Integer> validDirections = new Stack<Integer>();
					
					// For all potential directions
					for (int i = 0; i < 4; i++) {
						
						if(filter.getNumberParticles() == 1)break outer;
						
						// Scan in the direction and receive a range value
						client.sendData("SCAN:RANGE");
						String data = client.awaitData();
						
						logger.info("Scan Result: "+data);
						Thread.sleep(3000);
						
						// If the reading is not infinity and range greater than a quarter of a cell
						// then we know there is not an obstacle, so it is an explorable direction
						if(data != "Infinity")validDirection[i] = Double.parseDouble(data) > 0.1875;
						else validDirection[i] = true;
						
						// Update the filter with the potential obstacle of the direction
						filter.obstacleInfront(!validDirection[i]);
						
						// Update the map view
						mapView.updateFilter(filter.toString());

						// Rotate 90 to scan the next direction
						client.sendData("ROTATE:90");
						client.awaitData();
						
						Thread.sleep(2000);
						
						// Update the filter with the rotation
						filter.rotateParticlesClockwise();
						
						// Update the map view
						mapView.updateFilter(filter.toString());
						
						logger.info("Direction " + i + " is "+validDirection[i]);

					}
					
					// Set the stack to be all the directions we can move in, excluding the direction we just came from 
					for(int d = 0; d< 4; d++) {
						if(validDirection[d] && (directionLastMoved+2)%4!=d) validDirections.push(d);
					}
					
					// Declare the direction we want to move to, relative to the last direction we moved in					
					int nextDirection = 2;
					
					// If we have valid directions
					if(validDirections.size()>0) {
						// Pick a random valid direction
						logger.info("Valid Directions: " + validDirections.toString());
						nextDirection = validDirections.elementAt((new Random()).nextInt(validDirections.size()));
						
					}
					// Otherwise backtrack, to explore a new path
					else nextDirection = (directionLastMoved+2)%4;
					
					logger.info("Next direction: " + nextDirection);
					
					// Calculate the rotation we need
					int rotation = 90*((nextDirection)%4);
					
					logger.info("Rotating " + rotation + "degrees");
					
					// If rotation does not equal 0, rotate
					if(rotation!=0) {
						client.sendData("ROTATE:" + rotation);
						client.awaitData();
					}
					
					Thread.sleep(2000);
					
					// Update particle filter with the direction
					for(int i = 0; i< nextDirection; i++) filter.rotateParticlesClockwise();
					
					// Update the map view
					mapView.updateFilter(filter.toString());

					// Move forward
					client.sendData("FORWARD:1");
					client.awaitData();
					
					// Move the particles forward
					filter.moveParticlesForward();
					
					// Update the map view
					mapView.updateFilter(filter.toString());
					
					directionLastMoved = nextDirection;
				}
				
				// Get the last particle
				int[] particle = filter.getPosition();
				
				if(particle != null) 
				logger.info("Yeah mate we reckon we are localised, nay chance. X:" + particle[0] + " Y:" + particle[1] + "Heading: " + particle[2]);
				else logger.info("No particles left in filter");
				// Set the robots odometry
				client.sendData("SET:" + particle[0] + "," + particle[1] + "," + particle[2]);			
				client.awaitData();
				

				
				model.setAgPos(0, particle[0], particle[1]);
				Literal location = Literal.parseLiteral("location(self, 0, 0)");
				addPercept("paramedic", location);
				
				while (true) Thread.yield();
//				return true;				
//				
			} else if (action.getFunctor().equals("addObstacle")) {
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the obstacle to the Grid World Model
				model.addObstacle(x, y);
				logger.info("adding obstacle at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("addHospital")) {
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the hospital to the grid world model
				model.addHospital(x, y);
				logger.info("adding hospital at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("moveTo")) {
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
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
				ArrayList<int[]> formattedPath = p.convertToIntArrayList(path);

				// Go through the path, starts at 1 because index 0 is null for some reason
				for (int i = 0; i < path.length; i++) {

					if (isSimulatorMode == false) {
						/**
						 * Here is where we will send the move command to the EV3
						 */
						client.sendData("MOVE:" + path[i].x + "," + path[i].y);
						String data = client.awaitData();
						logger.info(data);

					}

					int pathX = path[i].x;
					int pathY = path[i].y;
					
					
					// Update the agents position to the next move
					model.setAgPos(0, pathX, pathY);
					
//					formattedPath.remove(i);
					logger.info("" + formattedPath.size());
					
					// Update the map with the new location
					mapView.updateMap(model, formattedPath);

					// Sleep for testing purposes
					Thread.sleep(1000);
				}

				model.setAgPos(0, x, y);
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("criticals")) {

				int criticals = (int) ((NumberTerm) action.getTerm(0)).solve();
				model.numberOfCriticals = criticals;

			} else if (action.getFunctor().equals("nextTarget")) {
				int[] agentPos = model.getAgentPosition();

				ArrayList<int[]> victims = new ArrayList<int[]>(0);
				ArrayList<int[]> potentialVictims = model.getPotentialVictimLocations();
				ArrayList<int[]> nonCriticalVictims = model.getLocations(NONCRITICAL);

				logger.info("NUMBER OF CRITICALS: " + model.numberOfCriticals);

				String output = "POTENTIAL VICTIMS:";
				for (int i = 0; i < potentialVictims.size(); i++) {
					output += "[" + potentialVictims.get(i)[0] + ", " + potentialVictims.get(i)[1] + "] ";
				}
				logger.info(output);

				String output2 = "NON CRITICALS: ";
				for (int i = 0; i < nonCriticalVictims.size(); i++) {
					output2 += "[" + nonCriticalVictims.get(i)[0] + ", " + nonCriticalVictims.get(i)[1] + "] ";
				}
				logger.info(output2);

				if (model.numberOfCriticals == 0) {
					logger.info("No more criticals, can save our non criticals");
					victims.addAll(nonCriticalVictims);
				}

				victims.addAll(potentialVictims);

				String output3 = "ALL SELECTED: ";
				for (int i = 0; i < victims.size(); i++) {
					output3 += "[" + victims.get(i)[0] + ", " + victims.get(i)[1] + "] ";
				}
				logger.info(output3);

				// Create a pathfinder
				Pathfinder p = new Pathfinder(GSize, GSize);

				// Update the obstacles using the model
				p.updateCells(model);

				// Default, we say the nearest neighbour is the agents current position
				int[] currentNearestPos = agentPos;
				int currentShortestPath = Integer.MAX_VALUE;

				// Find the nearest neighbour victim
				for (int j = 0; j < victims.size(); j++) {
					int[] victimLocation = victims.get(j);
					Pathfinder.Node[] path = p.getPath(agentPos[0], agentPos[1], victimLocation[0], victimLocation[1]);
					if (path.length < currentShortestPath) {
//            			logger.info(victimLocation[0] + ", " + victimLocation[1] + " is closer than " + currentNearestPos[0] + ", " + currentNearestPos[1]);
						currentNearestPos = victimLocation;
						currentShortestPath = path.length;
					}
				}

				logger.info("NEAREST NEIGHBOUR: " + currentNearestPos[0] + ", " + currentNearestPos[1]);
				// Update the agents percept of nearest neighbour
				Literal removal = Literal.parseLiteral("nearest(X,Y)");
				Literal nearest = Literal.parseLiteral(
						"newNearest(" + currentNearestPos[0] + "," + currentNearestPos[1] + "," + perceptIndex + ")");
				perceptIndex++;
				// removePercept("paramedic",removal);
				addPercept("paramedic", nearest);

			} else if (action.getFunctor().equals("inspectVictim")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				if (isSimulatorMode == false) {
					client.sendData("SCAN:COLOUR");
					String colour = client.awaitData();
					
					logger.info("Received Colour:" + colour + ":");

					Literal location = null;
					if (colour.equals("white")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", white)");
					} else if (colour.equals("burgandy")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", burgandy)");
					} else if (colour.equals("cyan")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
					}
					addPercept("paramedic", location);

					mapView.updateMap(model);
				} else {
					/**
					 * START TEMP TEST CODE
					 */

//	                critical(2,3).
//	                ~critical(4,5).
//	                ~critical(5,1).

//	                ~critical(2,0).
//	                ~critical(2,2).
//	                critical(5,4).

//	                critical(2,5).
//	                ~critical(5,5).
//	                critical(3,1).

//	                critical(1,3).
//	                ~critical(4,3).
//	                ~critical(0,2).

					if (x == 2 && y == 0) {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", burgandy)");
						addPercept("paramedic", location);
					} else if (x == 4 && y == 3) {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
						addPercept("paramedic", location);
					} else if (x == 0 && y == 1) {
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
				}

			} else if (action.getFunctor().equals("noVictimAt")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.removeVictim(x, y);
				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("criticalVictimAt")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.removeVictim(x, y);
				model.addCritical(x, y);
				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("nonCriticalVictimAt")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.removeVictim(x, y);
				model.addNonCritical(x, y);
				mapView.updateMap(model);
				Thread.sleep(500);
			} else if (action.getFunctor().equals("pickUpVictim")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				int victimType = model.getVictimType(x, y);
				logger.info("Victim Type: " + victimType);
				model.remove(victimType, x, y);

				if (victimType == CRITICAL) {
					model.carryingCritical = true;
				} else {
					model.carryingNonCritical = true;
				}

				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("putDownVictim")) {
				// Reset the GUI to show we have put down a victim
				model.carryingCritical = false;
				model.carryingNonCritical = false;
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("allLocated")) {
				// Finish the mission
				ArrayList<int[]> potentialVictims = model.getLocations(VICTIM);
				
				for (int i = 0; i < potentialVictims.size(); i++) {
					int[] victim = potentialVictims.get(i);
					model.remove(VICTIM, victim[0], victim[1]);
				}
				mapView.updateMap(model);
			} else if (action.getFunctor().equals("finishRescueMission")) {


			} else {
				logger.info("executing: " + action + ", but not implemented! Lel");
				return true;
				// Note that technically we should return false here. But that could lead to the
				// following Jason error (for example):
				// [ParamedicEnv] executing: addObstacle(2,2), but not implemented!
				// [paramedic] Could not finish intention: intention 6:
				// +location(obstacle,2,2)[source(doctor)] <- ... addObstacle(X,Y) / {X=2, Y=2,
				// D=doctor}
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

	public void eliminateParticlesByColour(ParticleFilter filter, String colour) throws IOException {
		if (colour.equals("yellow")) {
			for (int x = 0; x < GSize; x++) {
				for (int y = 0; y < GSize; y++) {
					if (x == y && y == 0) continue;
					filter.removeParticlesAt(x, y);
				}
			}
		} else if (colour.equals("white")) {
			
			int[] hospitalLocation = model.getHospitalLocation();
			filter.removeParticlesAt(hospitalLocation[0], hospitalLocation[1]);
	
			List<int[]> possibleLocations = model.getLocations(VICTIM);
			for (int x = 0; x < possibleLocations.size(); x++) {
				int[] location = possibleLocations.get(x);
				filter.removeParticlesAt(location[0], location[1]);
			}
		} else if (colour.equals("burgandy") || colour.equals("cyan")) {
			filter.removeAllBut(model.getPotentialVictimLocations());
		}
	}
	
	// ======================================================================
	class RobotBayModel extends GridWorldModel {

		private RobotBayModel() {
			super(GSize, GSize, 1); // The third parameter is the number of agents

			setAgPos(0, 0, 0);
//			Literal location = Literal.parseLiteral("location(self, 0, 0)");
//			addPercept("paramedic", location);

		}

		public boolean carryingCritical = false;
		public boolean carryingNonCritical = false;

		public int numberOfCriticals = 0;

		public int[] getAgentPosition() {
			int[] agentPos = { -1, -1 };
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
						int[] victimPos = { x, y };
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
						int[] victimPos = { x, y };
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
						int[] victimPos = { x, y };
						locations.add(victimPos);
					}
				}
			}
			return locations;
		}

		public int getVictimType(int x, int y) {
			if (hasObject(CRITICAL, x, y) == true) {
				return CRITICAL;
			}
			if (hasObject(NONCRITICAL, x, y) == true) {
				return NONCRITICAL;
			}
			return 0;
		}

		public int[] getHospitalLocation() {
			int[] l = { 0, 0 };
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