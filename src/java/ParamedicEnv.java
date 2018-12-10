import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;

import java.util.Random;
import java.util.Stack;

/**
 * Paramedic Environment
 * This is quite a long file -sorry
 *
 */
public class ParamedicEnv extends Environment {

	/**
	 * Entities with the environment
	 */
	public static final int HOSPITAL = 8; // hospital code in grid model
	public static final int VICTIM = 16; // victim code in grid model
	public static final int CRITICAL = 64;
	public static final int NONCRITICAL = 128;
	
	/**
	 * X and Y size of the grid world
	 */
	public static final int GSize = 6; // The bay is a 6x6 grid

	private Logger logger = Logger.getLogger("doctor2018." + ParamedicEnv.class.getName());
	private RobotBayModel model;
	private MapGUI mapView;
	private Client client;
	private int perceptIndex = 0;

	/**
	 * Boolean indicating if we are simulating
	 */
	private boolean isSimulatorMode = false;

	/** Called before the MAS execution with the args informed in .mas2j */
	@Override
	public void init(String[] args) {
		super.init(args);
		
		// If we aren't simulating, try to connect to the client
		if (isSimulatorMode == false) {
			try {
				client = new Client();
			} catch (Exception e) {

			}
		}

		// Instantiate a Bay Model and MapGUI object
		model = new RobotBayModel();
		mapView = new MapGUI();
	}

	/**
	 * executeAction()
	 * Complete an action requested by the agent speak
	 */
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
				/**
				 * addVictim
				 * Called by paramedic to add a potential victim to the bay model
				 */
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the victim to the Grid World Model
				model.addVictim(x, y);
				logger.info("adding victim at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("addObstacle")) {
				/**
				 * addObstacle
				 * Called by paramedic to add an obstacle to the bay model
				 */
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the obstacle to the Grid World Model
				model.addObstacle(x, y);
				logger.info("adding obstacle at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("addHospital")) {
				/**
				 * addVictim
				 * Called by the paramedic to add a hospital to the bay model
				 */
				// Get x and y terms
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				// Add the hospital to the grid world model
				model.addHospital(x, y);
				logger.info("adding hospital at: " + x + "," + y);

				// Update the map view
				mapView.updateMap(model);
				

			}  else if (action.getFunctor().equals("localise")) {
				/**
				 * localise
				 * Called by the paramedic to localise and determine the agent position
				 */
				
				if (isSimulatorMode == true) {
					model.setAgPos(0, 0, 0);
					Literal location = Literal.parseLiteral("location(self, 0, 0)");
					addPercept("paramedic", location);
					return true;
				}
				
				// Run the particle filter	
				int[] location = localiseWithParticleFilter();
				
				if (location != null) {
					logger.info("Localised: X:" + location[0] + " Y:" + location[1] + "Heading: " + location[2]);
				} else {
					logger.info("Failed to localise");
				}
							
				// Set the robots odometry
				client.sendData("SET:" + location[0] + "," + location[1] + "," + location[2]);			
				logger.info("ROBOT SET:" + client.awaitData());
				
				// Set the agents location in the model and add a percept to the paramedic
				model.setAgPos(0, location[0], location[1]);
				Literal locationLiteral = Literal.parseLiteral("location(self,"+ location[0] + "," + location[1] + ")");
				addPercept("paramedic", locationLiteral);
				
				// Update map view
				mapView.updateMap(model);		
		
			} else if (action.getFunctor().equals("nextTarget")) {
				/**
				 * nextTarget
				 * Gets the closest victim to the agent and adds the agentspeak percept
				 */
				
				// Where is the agent?
				int[] agentPos = model.getAgentPosition();

				// Array store the victims
				ArrayList<int[]> victims = new ArrayList<int[]>(0);
				
				// The potential victim locations
				ArrayList<int[]> potentialVictims = model.getLocations(VICTIM);
				
				// Get the locations of any non critical victims we have found
				ArrayList<int[]> nonCriticalVictims = model.getLocations(NONCRITICAL);

				// Add the potential victims to the array
				victims.addAll(potentialVictims);
				
				// If there are no critical victims left, we can add the non criticals to be selected 
				if (model.numberOfCriticals == 0) {
					logger.info("No more critical victim - can now save non critical victims");
					victims.addAll(nonCriticalVictims);
				}

				// Get the nearest victim
				int[] nearestVictim = getNearestVictim(agentPos, victims);
				logger.info("Nearest Neighbour: " + nearestVictim[0] + ", " + nearestVictim[1]);
				
				// Add the agents percept of nearest neighbour
				Literal nearest = Literal.parseLiteral("newNearest(" + nearestVictim[0] + "," + nearestVictim[1] + "," + perceptIndex + ")");
				perceptIndex++;
				addPercept("paramedic", nearest);

			} else if (action.getFunctor().equals("moveTo")) {
				/**
				 * moveTo
				 * Send commands to robot to move to a particular cell
				 * Should have seperated into outer function, but who has time
				 */

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
				

//				boolean skipX = false;
//				boolean skipY = false;
				
				for (int i = 0; i < path.length; i++) {
					// Update the map with the new location
					System.out.println(Arrays.deepToString(formattedPath.subList(i, path.length).toArray()));
					
					mapView.updateMap(model, formattedPath.subList(i, path.length));
					
//					if (i < path.length-1) {
//						if (path[i+1].x == path[i].x && !skipY) {
//							skipX = true;
//							continue;
//						} else if (path[i+1].y == path[i].y && !skipX) {
//							skipY = true;
//							continue;
//						}
//					}
//					
//					skipX = false;
//					skipY = false;
//					
					if (isSimulatorMode == false) {
						client.sendData("MOVE:" + path[i].x + "," + path[i].y);
						String data = client.awaitData();
						logger.info(data);
					} else Thread.sleep(1000);

					int pathX = path[i].x;
					int pathY = path[i].y;
					
					// Update the agents position to the next move
					model.setAgPos(0, pathX, pathY);
					// Update the map with the new location
					mapView.updateMap(model, formattedPath.subList(i, path.length));

				}

				// Update agent position and map
				model.setAgPos(0, x, y);
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("criticals")) {
				/**
				 * criticals
				 * Called by the agentspeak to update the number of criticals value in the model
				 */

				int criticals = (int) ((NumberTerm) action.getTerm(0)).solve();
				model.numberOfCriticals = criticals;

			} else if (action.getFunctor().equals("inspectVictim")) {
				/**
				 * inspectVictim
				 * Scan the victim colour and inform paramedic
				 */
				
				// Location
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				if (isSimulatorMode == false) {
					// Ask the robot to scan the colour
					client.sendData("SCAN:COLOUR");
					String colour = client.awaitData();
					logger.info("Received Colour:" + colour + ":");

					// Set the correct percept of the colour for the location
					Literal location = null;
					if (colour.equals("white")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", white)");
					} else if (colour.equals("burgandy")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", burgandy)");
					} else if (colour.equals("cyan")) {
						location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
					}
					addPercept("paramedic", location);

					// Update the map view
					mapView.updateMap(model);
				} else {
					/**
					 * For the simulator mode, we have to hard code the values of the critical and non critical victims
					 */
					if (x == 2 && y == 3) {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", burgandy)");
						addPercept("paramedic", location);
					} else if (x == 4 && y == 5) {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
						addPercept("paramedic", location);
					} else if (x == 5 && y == 1) {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", cyan)");
						addPercept("paramedic", location);
					} else {
						Literal location = Literal.parseLiteral("colour(" + x + "," + y + ", white)");
						addPercept("paramedic", location);
					}
					mapView.updateMap(model);
					Thread.sleep(1000);
				}

			} else if (action.getFunctor().equals("noVictimAt")) {
				/**
				 * noVictimAt
				 * Called if there is no victim at the location and removes the victim from the model
				 */
				// Location
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				
				// Remove the victim from the model
				model.removeVictim(x, y);
				
				// Update the map view
				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("criticalVictimAt")) {
				/**
				 * criticalVictimAt
				 * Called if there is a critical victim at a location
				 * Removes the victim code from the location and adds a critical
				 */
				// Location
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				
				// Remove the victim and add a critical
				model.removeVictim(x, y);
				model.addCritical(x, y);
				
				// Update the map view
				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("nonCriticalVictimAt")) {
				/**
				 * nonCriticalVictimAt
				 * Called if there is a non critical at a location
				 * Removes the victim code from the location and adds a non critical
				 */
				// Location
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				
				/// Remove the victim and add a critical
				model.removeVictim(x, y);
				model.addNonCritical(x, y);
				
				// Update the model
				mapView.updateMap(model);
				Thread.sleep(500);

			} else if (action.getFunctor().equals("pickUpVictim")) {
				/**
				 * pickUpVictim
				 * Picks up a victim at the location and assigns 
				 */
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();

				int victimType = model.getVictimType(x, y);
				logger.info("Victim Type: " + victimType);
				model.remove(victimType, x, y);

				// Set the model to believe we are carrying a victim
				if (victimType == CRITICAL) {
					model.carryingCritical = true;
				} else {
					model.carryingNonCritical = true;
				}
				
				if (!isSimulatorMode) {
					// Turn off the LED on the robot 
					client.sendData("LED:HASVICTIM");
					client.awaitData();
				}

				// Update the map
				mapView.updateMap(model);
				
				if (isSimulatorMode) {
					Thread.sleep(500);
				}

			} else if (action.getFunctor().equals("putDownVictim")) {
				/**
				 * putDownVictim
				 * Update the model to put down the victim we are carrying
				 */
				model.carryingCritical = false;
				model.carryingNonCritical = false;
				
				if (!isSimulatorMode) {
					// Turn off the LED on the robot 
					client.sendData("LED:NOVICTIM");
					client.awaitData();
				}
				
				// Update the map
				mapView.updateMap(model);

			} else if (action.getFunctor().equals("allLocated")) {
				/**
				 * allLocated
				 * Removes locations of potential victims when we have found all the possible victims
				 * 
				 */
				ArrayList<int[]> potentialVictims = model.getLocations(VICTIM);
				for (int i = 0; i < potentialVictims.size(); i++) {
					int[] victim = potentialVictims.get(i);
					model.remove(VICTIM, victim[0], victim[1]);
				}

				mapView.updateMap(model);
			
			} else if (action.getFunctor().equals("finish")) {
				/**
				 * finish 
				 * Called when the search and rescue is completed
				 * Tells the robot to display finished LED
				 */
				if (!isSimulatorMode) {
					// Turn off the LED on the robot 
					client.sendData("LED:FINISHED");
					client.awaitData();
				}
				
			} else {
				logger.info("executing: " + action + ", but not implemented! Lel");
				return true;
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

	/**
	 * getNearestVictim
	 * Returns the closest victim from the agents position using the model
	 * @param agentPos
	 * @param victims
	 * @return
	 */
	public int[] getNearestVictim(int[] agentPos, ArrayList<int[]> victims) {
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
				currentNearestPos = victimLocation;
				currentShortestPath = path.length;
			}
		}
		return currentNearestPos;
	}
	
	/**
	 * placeObstacles()
	 * Uses the model to place obstacles in the particle filter
	 * @param filter
	 */
	public void placeObstacles(ParticleFilter filter) {
		
		// Get the locations of the obstacles in the arena
		ArrayList<int[]> obstacleLocations = model.getObstacleLocations();
				
		// For obstacle, add the location to the filter
		obstacleLocations.forEach((obstacle) -> {
			filter.addObstacle(obstacle[0], obstacle[1]);
		});
		
	}
	
	
	/**
	 * localiseWithParticleFilter
	 * Figure out the location of the robot using the particle filter
	 * @return location
	 * @throws IOException
	 */
	public int[] localiseWithParticleFilter() throws IOException {
		
		// Turn off the LED on the robot 
		client.sendData("LED:LOCALISING");
		client.awaitData();
		
		// Create a particle filter
		ParticleFilter filter = new ParticleFilter(GSize, GSize);
		
		placeObstacles(filter);
		
		// Update map with filter data
		mapView.updateFilter(filter.toString());
	
		// While we have more than one particle
		outer: while (filter.getNumberParticles() != 1) {
			
			if(filter.getNumberParticles() < 1) {
				filter = new ParticleFilter(GSize,GSize);
				placeObstacles(filter);
			}
			
			// Get the colour of the current position and update the particle filter
			client.sendData("SCAN:COLOUR");
			String colour = client.awaitData();
			logger.info("Cell is " + colour);
			eliminateParticlesByColour(filter, colour);
			mapView.updateFilter(filter.toString());
			
			// Array of booleans to store whether we can move in a direction
			// based upon whether there is an obstacle or wall
			boolean[] validDirection = new boolean[4];
			
			// Stack to store the index of the valid directions, excluding the cameFrom direction
			Stack<Integer> validDirections = new Stack<Integer>();
			
			// For all potential directions
			for (int i = 0; i < 4; i++) {

				// Scan in the direction and receive a range value
				client.sendData("SCAN:RANGE");
				String data = client.awaitData();
				
				logger.info("Scan Result: "+ data);
				
				// If the reading is not infinity and range greater than a quarter of a cell
				// then we know there is not an obstacle, so it is an explorable direction
				if(data != "Infinity")validDirection[i] = Double.parseDouble(data) > 0.1875;
				else validDirection[i] = true;
				
				// Update the filter with the potential obstacle of the direction
				filter.obstacleInfront(!validDirection[i]);
				
				// Update the map view
				mapView.updateFilter(filter.toString());
				
				if(filter.getNumberParticles() == 1)break outer;
				
				// Rotate 90 to scan the next direction
				client.sendData("ROTATE:90");
				client.awaitData();
				
				// Update the filter with the rotation
				filter.rotateParticlesClockwise();
				
				// Update the map view
				mapView.updateFilter(filter.toString());
				
				logger.info("Direction " + i + " is "+validDirection[i]);

			}
			
			// Set the stack to be all the directions we can move in, excluding the direction we just came from 
			for(int d = 0; d< 4; d++) {
				if(validDirection[d] && 2!=d) validDirections.push(d);
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
			else nextDirection = 2;
			
			logger.info("Next direction: " + nextDirection);
			
			// Calculate the rotation we need
			int rotation = 90*((nextDirection)%4);
			
			logger.info("Rotating " + rotation + "degrees");
			
			// If rotation does not equal 0, rotate
			if(rotation!=0) {
				client.sendData("ROTATE:" + rotation);
				client.awaitData();
			}
			
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
			
		}
		
		// Get the last particle
		int[] particle = filter.getPosition();
		
		// Turn off the LED on the robot 
		client.sendData("LED:NOVICTIM");
		client.awaitData();
		
		return particle;
	}
	
	/**
	 * eliminateParticlesByColour()
	 * Removes particles from the filter based upon the colour of the current location
	 * @param filter
	 * @param colour
	 * @throws IOException
	 */
	public void eliminateParticlesByColour(ParticleFilter filter, String colour) throws IOException {
		if (colour.equals("yellow")) {
			// If we are at yellow, we are at hospital, remove everything but the hospital particles
			for (int x = 0; x < GSize; x++) {
				for (int y = 0; y < GSize; y++) {
					if (x == y && y == 0) continue;
					filter.removeParticlesAt(x, y);
				}
			}
		} else if (colour.equals("white")) {
			// If we are at white, we can remove the location of the hospital from the filter
			int[] hospitalLocation = model.getHospitalLocation();
			filter.removeParticlesAt(hospitalLocation[0], hospitalLocation[1]);

		} else if (colour.equals("burgandy") || colour.equals("cyan")) {
			// If we are at burgandy or cyan, we can remove all locations apart from the potential victim locations
			filter.removeAllBut(model.getPotentialVictimLocations());
		}
	}
	
	/**
	 * RobotBayModel 
	 * Maintains a model of the grid world with locations
	 *
	 */
	class RobotBayModel extends GridWorldModel {
		// Flag for whether we are carrying a critical victim
		public boolean carryingCritical = false;
		
		// Flag for whether we are carrying a non critical victim
		public boolean carryingNonCritical = false;
		
		// Stores the number of current victms
		public int numberOfCriticals = 0;
		
		/**
		 * RobotBayModel
		 * Constructor which initialises with the Grid Size
		 */
		private RobotBayModel() {
			super(GSize, GSize, 1); // The third parameter is the number of agents
		}
		
		/**
		 * addAgent()
		 * Add the agent to the model
		 * @param x
		 * @param y
		 */
		void addAgent(int x, int y) {
			add(AGENT, x, y);
		}

		/**
		 * addVictim()
		 * Add a potential victim to the model
		 * @param x
		 * @param y
		 */
		void addVictim(int x, int y) {
			add(VICTIM, x, y);
		}

		/**
		 * addHospital()
		 * Add the hospital to the model
		 * @param x
		 * @param y
		 */
		void addHospital(int x, int y) {
			add(HOSPITAL, x, y);
		}

		/**
		 * addObstacle()
		 * Add a obstacle to the model
		 * @param x
		 * @param y
		 */
		void addObstacle(int x, int y) {
			add(OBSTACLE, x, y);
		}

		/**
		 * removeVictim()
		 * Remove victim from the model
		 * @param x
		 * @param y
		 */
		void removeVictim(int x, int y) {
			remove(VICTIM, x, y);
		}

		/**
		 * addCritical()
		 * Add a critical victim to the model
		 * @param x
		 * @param y
		 */
		void addCritical(int x, int y) {
			add(CRITICAL, x, y);
		}

		/**
		 * addNonCritical()
		 * Add a  non critical victim to the model
		 * @param x
		 * @param y
		 */
		void addNonCritical(int x, int y) {
			add(NONCRITICAL, x, y);
		}

		/**
		 * getAgentPosition()
		 * Get the agents position in the model
		 * @return agent position
		 */
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

		/**
		 * getObstacleLocations()
		 * Returns the locations of obstacles
		 * @return obstacle locations
		 */
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

		/**
		 * getPotentialVictimLocations()
		 * Return the locations of potential vicitms
		 * @return
		 */
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

		/**
		 * getLocations()
		 * Makes most of above functions redundant
		 * BUT - I'm commenting at home so don't want to replace them without testing
		 * 
		 * @param type
		 * @return
		 */
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

		/**
		 * getVictimType()
		 * Get the type of victim at the location
		 * @param x
		 * @param y
		 * @return
		 */
		public int getVictimType(int x, int y) {
			if (hasObject(CRITICAL, x, y) == true) {
				return CRITICAL;
			}
			if (hasObject(NONCRITICAL, x, y) == true) {
				return NONCRITICAL;
			}
			return 0;
		}

		/**
		 * getHospitalLocation()
		 * It's hard coded -- really sorry this file is so long, don't have time to separate it into classes :(
		 * @return
		 */
		public int[] getHospitalLocation() {
			int[] l = { 0, 0 };
			return l;
		}

	}
}