
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

/**
 * MapGUI Class Class to create a simulation of rescue mission
 *
 */
public class MapGUI {

	/**
	 * Static colours
	 */
	private static final Color green = new Color(0, 153, 0);
	private static final Color red = new Color(204, 0, 0);
	private static final Color yellow = new Color(255, 204, 0);
	private static final Color blue = new Color(0, 0, 204);
	private static final Color darkMagenta = new Color(139, 0, 139);
	private static final Color indigo = new Color(75, 0, 130);
	private static final Color darkRed = new Color(107, 0, 0);
	private static final Color cyan = new Color(12, 195, 177);

	/**
	 * Number of rows in the map
	 */
	private static int MAP_ROWS = 6;
	
	/**
	 * Number of columns in the map
	 */
	private static int MAP_COLS = 6;

	/**
	 * 2D Array of JLabels to hold data in the cells
	 */
	private JLabel[][] labels = new JLabel[MAP_ROWS][MAP_COLS];
	
	/**
	 * 2D array of cells representing the cells in the map
	 */
	private JPanel[][] cells = new JPanel[MAP_ROWS][MAP_COLS];
	
	
	private JFrame frame;
	private MapPane mapPane;

	/**
	 * Main Method for testing the GUI
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		MapGUI mapGUI = new MapGUI();
		
		try {
			Thread.sleep(1000);

		} catch (Exception e) {
		}
		mapGUI.setCellParticles(0, 0, '\u254B');
		mapGUI.setCellParticles(1, 1, '\u253B');
		mapGUI.setCellParticles(2, 2, '\u2533');
		mapGUI.setCellParticles(3, 3, '\u2523');

	}

	/**
	 * MapGUI Constructor for the map GUI Class
	 * 
	 */
	public MapGUI() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace();
				}
				// Create a new JFrame Window with which can be closed
				frame = new JFrame("Robotics Assignment 2 - Map Output");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Create a Map Pane Layout and add it to the frame
				mapPane = new MapPane();
				frame.add(mapPane);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);

			}
		});
	}

	/**
	 * setAgentLocation
	 * Format a cell to be the agent
	 * 
	 * @param x 
	 * @param y
	 * @param c carrying variable, 1 if critical, 2 if non critical
	 */
	public void setAgentLocation(int x, int y, int c) {
		if (x == -1 || y == -1) {
			return;
		}
		
		JLabel label = labels[x][y];
		label.setText("R");
		JPanel cell = cells[x][y];
		cell.setBorder(new LineBorder(cyan, 4));

		if (c == 1) {
			cell.setBackground(darkRed);
		} else if (c == 2) {
			cell.setBackground(cyan);
		}

		if (cell.getBackground() == Color.WHITE) {
			label.setForeground(Color.DARK_GRAY);
		}
	}

	/**
	 * updateFilter()
	 * Update the map with particles
	 * 
	 * @param filterString
	 */
	public void updateFilter(String filterString) {
		String[] map = filterString.split("\n");
		
		for (int y = 0; y < map.length; y++) {
			char[] chars = map[y].toCharArray();
			for (int x = 0; x < chars.length; x++) {
				char c = chars[x];
				setCellParticles(x, map.length-1-y, c);
			}
		}
	}
	
	/**
	 * setCellParticles()
	 * Update cell to display particles
	 * @param x
	 * @param y
	 * @param c
	 */
	public void setCellParticles(int x, int y, char c) {
		JLabel label = labels[x][y];
		label.setFont(new Font("monospaced", 1, 50));
		label.setText("" + c);
		label.setForeground(Color.RED);
	}
	
	/**
	 * setCellParticles()
	 * Update cell to display particles
	 * @param x
	 * @param y
	 * @param c
	 */
	public void setCellParticles(int x, int y, String s) {
		JLabel label = labels[x][y];
		label.setFont(new Font("monospaced", 1, 50));
		label.setText(s);
	}
	
	/**
	 * addObstacle
	 * Format cell to represent obstacle
	 * @param x
	 * @param y
	 */
	public void addObstacle(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(Color.DARK_GRAY);
		label.setText("O");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	/**
	 * addHospital
	 * Format cell to represent hospital
	 * @param x
	 * @param y
	 */
	public void addHospital(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(yellow);
		label.setText("H");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	/**
	 * addVictim
	 * Format cell to represent a potential victim
	 * @param x
	 * @param y
	 */
	public void addVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(Color.LIGHT_GRAY);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	/**
	 * addCriticalVictim
	 * Format cell to represent a critical victim
	 * @param x
	 * @param y
	 */
	public void addCriticalVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(darkRed);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	/**
	 * addNonCriticalVictim
	 * Format cell to represent a non critical victim
	 * @param x
	 * @param y
	 */
	public void addNonCriticalVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(cyan);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	/**
	 * removeCellFormatting
	 * Reset the formatting of a cell
	 * @param x
	 * @param y
	 */
	public void removeCellFormatting(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(Color.WHITE);
		label.setText("");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
		cell.setBorder(new LineBorder(Color.BLACK, 2));
	}

	/**
	 * addPath
	 * Add the current path of the robot to the GUI
	 * @param path
	 */
	public void addPath(List<int[]> path) {
		for (int i = 0; i < path.size(); i++) {
			int[] pos = path.get(i);
			if (i == path.size() - 1) {
				cells[pos[0]][pos[1]].setBorder(new LineBorder(darkMagenta, 4));
			} else {
				cells[pos[0]][pos[1]].setBorder(new LineBorder(Color.magenta, 3));
			}
		}
	}
	
	/**
	 * updateMap()
	 * Iterate through each cell in the map and update its style and value
	 * 
	 * @param oL - obstacle locations
	 * @param aL - agent location
	 * @param vL - potential victim locations
	 * @param hL - hospital location
	 * @param cL - critical victim locations
	 * @param nL - non critical victim locations
	 * @param c - carrying a victim value
	 * @param path
	 */
	public void updateMap(ArrayList<int[]> oL, int[] aL, ArrayList<int[]> vL, int[] hL, ArrayList<int[]> cL,
			ArrayList<int[]> nL, int c, List<int[]> path) {

		ArrayList<int[]> obstacleLocations = oL;
		int[] agentLocation = aL;
		ArrayList<int[]> victimLocations = vL;
		int[] hospitalLocation = hL;

		ArrayList<int[]> criticalLocations = cL;
		ArrayList<int[]> nonCriticalLocations = nL;

		int carrying = c;

		// Remove Cell Formatting - can cause interface flashing but fixing this isn't a
		// priority.
		for (int x = 0; x < MAP_ROWS; x++) {
			for (int y = 0; y < MAP_COLS; y++) {
				removeCellFormatting(x, y);
			}
		}

		// Set the hospital
		addHospital(hospitalLocation[0], hospitalLocation[1]);

		// Set the obstacles
		for (int i = 0; i < obstacleLocations.size(); i++) {
			addObstacle(obstacleLocations.get(i)[0], obstacleLocations.get(i)[1]);
		}

		// Set the potential victim locations
		for (int i = 0; i < victimLocations.size(); i++) {
			addVictim(victimLocations.get(i)[0], victimLocations.get(i)[1]);
		}

		// Set the potential victim locations
		for (int i = 0; i < criticalLocations.size(); i++) {
			addCriticalVictim(criticalLocations.get(i)[0], criticalLocations.get(i)[1]);
		}

		// Set the potential victim locations
		for (int i = 0; i < nonCriticalLocations.size(); i++) {
			addNonCriticalVictim(nonCriticalLocations.get(i)[0], nonCriticalLocations.get(i)[1]);
		}

		// Set the path, if there is one.
		addPath(path);

		// Set the current agent location
		setAgentLocation(agentLocation[0], agentLocation[1], carrying);
	}

	/**
	 * updateMap()
	 * Takes the model as input and extracts the data to update the map
	 * 
	 * @param model
	 */
	public void updateMap(ParamedicEnv.RobotBayModel model) {

		// Get the locations of the required entities
		ArrayList<int[]> obstacleLocations = model.getObstacleLocations();
		int[] agentLocation = model.getAgentPosition();
		ArrayList<int[]> victimLocations = model.getPotentialVictimLocations();
		int[] hospitalLocation = model.getHospitalLocation();
		ArrayList<int[]> criticalLocations = model.getLocations(ParamedicEnv.CRITICAL);
		ArrayList<int[]> nonCriticalLocations = model.getLocations(ParamedicEnv.NONCRITICAL);

		// Represent carrying as 0, 1 or 2 - very hacky way to do it, I'm sorry - I hate Swing.
		int carrying = 0;
		if (model.carryingCritical == true) {
			carrying = 1;
		} else if (model.carryingNonCritical == true) {
			carrying = 2;
		}

		// Path is assumed to be an empty array list
		ArrayList<int[]> path = new ArrayList<int[]>();
		
		// Update the GUI
		updateMap(obstacleLocations, agentLocation, victimLocations, hospitalLocation, criticalLocations,
				nonCriticalLocations, carrying, path);
	}

	/**
	 * updateMap()
	 * Takes the model as input and extracts the data to update the map
	 * Also takes the current path of the robot to represent visually
	 * 
	 * @param model
	 * @param path
	 */
	public void updateMap(ParamedicEnv.RobotBayModel model, List<int[]> path) {

		// Get the locations of the required entities
		ArrayList<int[]> obstacleLocations = model.getObstacleLocations();
		int[] agentLocation = model.getAgentPosition();
		ArrayList<int[]> victimLocations = model.getPotentialVictimLocations();
		int[] hospitalLocation = model.getHospitalLocation();
		ArrayList<int[]> criticalLocations = model.getLocations(ParamedicEnv.CRITICAL);
		ArrayList<int[]> nonCriticalLocations = model.getLocations(ParamedicEnv.NONCRITICAL);

		
		// Represent carrying as 0, 1 or 2 - very hacky way to do it, I'm sorry - I hate Swing X2.
		int carrying = 0;
		if (model.carryingCritical == true) {
			carrying = 1;
		} else if (model.carryingNonCritical == true) {
			carrying = 2;
		}

		// Update the map with the data
		updateMap(obstacleLocations, agentLocation, victimLocations, hospitalLocation, criticalLocations,
				nonCriticalLocations, carrying, path);
	}

	public class MapPane extends JPanel {
		/**
		 * MapPane Constructor for the map pane class to construct display grid
		 * 
		 */
		public MapPane() {
			// Make a Grid Layout of the size of the arena + the walls (2)
			setLayout(new GridLayout(8, 8, 0, 0));

			// Set up a blank arena
			for (int c = 7; c >= 0; c--) {
				for (int r = 0; r < 8; r++) {

					JPanel cell = new JPanel() {
						@Override
						public Dimension getPreferredSize() {
							return new Dimension(80, 80);
						}
					};

					cell.setBorder(new LineBorder(Color.BLACK, 2));

					// If we are a border cell - make it dark gray
					if (r == 0 || c == 0 || r == 7 || c == 7) {
						cell.setBackground(Color.DARK_GRAY);
					} else {
						// Otherwise we need to put a label with the probability in the cell
						JLabel label = new JLabel();
						label.setFont(new Font("Arial", 1, 30));
						cell.add(label);
						labels[r - 1][c - 1] = label;
						cells[r - 1][c - 1] = cell;

					}

					// Add the cell to the container
					add(cell);
				}
			}

		}

	}

}