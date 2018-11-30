
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;


/**
 * MapGUI Class Class to create GUI to display probability values on client
 *
 */
public class MapGUI {

	// Static colours
	private static final Color green = new Color(0, 153, 0);
	private static final Color red = new Color(204, 0, 0);
	private static final Color yellow = new Color(255, 204, 0);
	private static final Color blue = new Color(0, 0, 204);
	private static final Color darkMagenta = new Color(139, 0, 139);
	private static final Color indigo = new Color(75, 0, 130);

	private static final Color darkRed = new Color(107, 0, 0);
	private static final Color cyan = new Color(12, 195, 177);

	private static int MAP_ROWS = 6;
	private static int MAP_COLS = 6;

	private JLabel[][] labels = new JLabel[MAP_ROWS][MAP_COLS];
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
			Thread.sleep(5000);

		} catch (Exception e) {
		}


	}

	/**
	 * MapGUI Constructor for the map GUI Class
	 * 
	 * @param m 2d array of floats of probabilities
	 * @param r array of x y position of the robot
	 * @param p 2d array of x y positions for the path
	 */
	public MapGUI() {
//    	map = m;
//		robotPosition = r;
//		pathPositions = p;
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
				frame = new JFrame("Robotics Assignment 1 - Map Output");
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
	
	public void setAgentLocation(int x, int y, int c) {
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

	public void addObstacle(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(Color.DARK_GRAY);
		label.setText("O");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	public void addHospital(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(yellow);
		label.setText("H");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	public void addVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(Color.LIGHT_GRAY);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	public void addCriticalVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(darkRed);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}

	public void addNonCriticalVictim(int x, int y) {
		JPanel cell = cells[x][y];
		JLabel label = labels[x][y];
		cell.setBackground(cyan);
		label.setText("V");
		label.setFont(new Font("Arial", 1, 45));
		label.setForeground(Color.WHITE);
	}
	
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
	 * updateMap() Update the map with new values, robot and path positions
	 * 
	 * @param m  2d array of floats of probabilities
	 * @param ro array of x y position of the robot
	 * @param p  2d array of x y positions for the path
	 */
	public void updateMap(ArrayList<int[]> oL, int[] aL, ArrayList<int[]> vL, int[] hL, ArrayList<int[]> cL, ArrayList<int[]> nL, int c) {
		
		
		ArrayList<int[]> obstacleLocations = oL;
		int [] agentLocation = aL;
		ArrayList<int[]> victimLocations = vL;
		int [] hospitalLocation = hL;
		
		ArrayList<int[]> criticalLocations = cL;
		ArrayList<int[]> nonCriticalLocations = nL;
		
		int carrying = c;
		
		// Remove Cell Formatting  - can cause interface flashing but fixing this isn't a priority.
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
		
		// Set the current agent location
		setAgentLocation(agentLocation[0], agentLocation[1], carrying);
	}
	
	public void updateMap(ParamedicEnv.RobotBayModel model) {
		
		ArrayList<int[]> obstacleLocations = model.getObstacleLocations();
		int [] agentLocation = model.getAgentPosition();
		ArrayList<int[]> victimLocations = model.getPotentialVictimLocations();
		int [] hospitalLocation = model.getHospitalLocation();
		
		ArrayList<int[]> criticalLocations = model.getLocations(ParamedicEnv.CRITICAL);
		ArrayList<int[]> nonCriticalLocations = model.getLocations(ParamedicEnv.NONCRITICAL);
		
		int carrying = 0;
		if (model.carryingCritical == true) {
			carrying = 1;
		} else if (model.carryingNonCritical == true) {
			carrying = 2;
		}
		
		updateMap(obstacleLocations, agentLocation, victimLocations, hospitalLocation, criticalLocations, nonCriticalLocations, carrying);
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

	/**
	 * setRobotBorder() Sets the border of a cell to represent the robot position
	 * 
	 * @param x
	 * @param y
	 */
	private void setRobotBorder(int x, int y) {
		cells[x][y].setBorder(new LineBorder(Color.CYAN, 3));
	}

	/**
	 * setPathBorders() Sets a sequence of cell borders to represent the current
	 * path
	 * 
	 * @param pathPositions array of positions
	 */
	private void setPathBorders(int[][] pathPositions) {
		for (int i = 0; i < pathPositions.length; i++) {
			if (i != pathPositions.length - 1) {
				cells[pathPositions[i][0]][pathPositions[i][1]].setBorder(new LineBorder(Color.magenta, 3));
			} else {
				cells[pathPositions[i][0]][pathPositions[i][1]].setBorder(new LineBorder(darkMagenta, 4));
			}
		}
	}

	/**
	 * colorFromProbality() Returns a colour from white to grey based on input
	 * probability
	 * 
	 * @param probability
	 * @return Color
	 */
	private Color colorFromProbality(float probability) {
		return new Color((1 - (probability * 0.90f)), (1 - (probability * 0.90f)), (1 - (probability * 0.90f)));
	}

}