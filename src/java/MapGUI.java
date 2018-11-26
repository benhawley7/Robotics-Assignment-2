
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;

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

	private static int MAP_ROWS = 6;
	private static int MAP_COLS = 6;

	private float[][] map;
	private int[] robotPosition;
	private int[][] pathPositions;
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

		// Test Map Updating
		float[][] exampleMap = new float[][] { { 0.9f, 0.1f, 0.2f, 0.3f, 0.4f, 1f }, { 1f, 1f, 1f, 0.8f, 1f, 1f },
				{ 1f, 1f, 0.5f, 0.7f, 1f, 1f }, { 1f, 1f, 1f, 1f, 1f, 1f }, { 1f, 0.7f, 0.6f, 0.65f, 1f, 1f },
				{ 0.8f, 0.8f, 1f, 1f, 1f, 1f }, { 1f, 1f, 1f, 0.9f, 0.97f, 0.2f }, };

		int[] robotPosition = { 0, 0 };
		int[][] pathPositions = { { 1, 1 }, { 1, 2 }, { 1, 3 } };

		MapGUI mapGUI = new MapGUI();
		try {
			Thread.sleep(5000);

		} catch (Exception e) {
		}

		exampleMap[0][0] = 0.4f;
		exampleMap[1][1] = 0.4f;
		pathPositions[0][1] = 5;
//        mapGUI.updateMap(exampleMap, robotPosition, pathPositions);

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
		cell.setBackground(blue);
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

	/**
	 * updateMap() Update the map with new values, robot and path positions
	 * 
	 * @param m  2d array of floats of probabilities
	 * @param ro array of x y position of the robot
	 * @param p  2d array of x y positions for the path
	 */
//	public void updateMap(float[][] m, int[] ro, int[][] p) {
//		map = m;
//		robotPosition = ro;
//		pathPositions = p;
//		// Update the labels in the map
//		for (int r = 0; r < 7; r++) {
//			for (int c = 0; c < 6; c++) {
//				cells[r][c].setBorder(new LineBorder(Color.BLACK, 2));
//				// I can't remember the coloured corners right now so I'm guessing
//				if (r == 0 && c == 0) {
//					// Its a coloured cell
//					cells[r][c].setBackground(blue);
//					labels[r][c].setText(String.format("%.2f", map[r][c]));
//				} else if (r == 0 && c == 5) {
//					// Its a coloured cell
//					cells[r][c].setBackground(green);
//					labels[r][c].setText(String.format("%.2f", map[r][c]));
//				} else if (r == 6 && c == 0) {
//					// Its a coloured cell
//					cells[r][c].setBackground(yellow);
//					labels[r][c].setText(String.format("%.2f", map[r][c]));
//				} else if (r == 6 && c == 5) {
//					// Its a coloured cell
//					cells[r][c].setBackground(red);
//					labels[r][c].setText(String.format("%.2f", map[r][c]));
//				} else {
//					labels[r][c].setText(String.format("%.2f", map[r][c]));
//					cells[r][c].setBackground(colorFromProbality(map[r][c]));
//					if (map[r][c] > 0.5) {
//						labels[r][c].setForeground(Color.WHITE);
//					} else {
//						labels[r][c].setForeground(Color.BLACK);
//					}
//				}
//			}
//		}
//
//		setPathBorders(pathPositions);
//		setRobotBorder(robotPosition[0], robotPosition[1]);
//
//	}
	


	public class MapPane extends JPanel {
		/**
		 * MapPane Constructor for the map pane class to construct display grid
		 * 
		 */
		public MapPane() {
			// Make a Grid Layout of the size of the arena + the walls (2)
			setLayout(new GridLayout(8, 8, 0, 0));

			int[] hospitalPos = {};
			int[][] obstaclePositions = {{}};
			int[][] victimPositions = {{}};

			for (int r = 7; r >= 0; r--) {
				for (int c = 0; c < 8; c++) {
//                	String test = "R:" + r + ", C:" + c;
//                	System.out.println(test);
					// Create a new JPanel with desired dimensions
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

//						// Is this the hospital cell?
//						if (r == hospitalPos[0] + 1 && c == hospitalPos[1] + 1) {
//							cell.setBackground(blue);
//							label.setText("H");
//							label.setFont(new Font("Arial", 1, 45));
//							label.setForeground(Color.WHITE);
//						}
//
//						for (int i = 0; i < obstaclePositions.length; i++) {
//							if (r == obstaclePositions[i][0] + 1 && c == obstaclePositions[i][1] + 1) {
//								cell.setBackground(Color.DARK_GRAY);
//								label.setText("O");
//								label.setFont(new Font("Arial", 1, 45));
//								label.setForeground(Color.WHITE);
//							}
//						}
//
//						for (int i = 0; i < victimPositions.length; i++) {
//							if (r == victimPositions[i][0] + 1 && c == victimPositions[i][1] + 1) {
//								cell.setBackground(Color.LIGHT_GRAY);
//								label.setText("V");
//								label.setFont(new Font("Arial", 1, 45));
//								label.setForeground(Color.WHITE);
//							}
//						}

						cell.add(label);
						labels[r - 1][c - 1] = label;
						cells[r - 1][c - 1] = cell;
					}

					// Add the cell to the container
					add(cell);
				}
			}
//            setPathBorders(pathPositions);
//            setRobotBorder(robotPosition[0], robotPosition[1]);
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