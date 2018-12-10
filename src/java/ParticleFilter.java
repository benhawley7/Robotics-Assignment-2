import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ParticleFilter{
	
	/**
	 * Each element of grid is true if an obstacle is there, false otherwise
	 */
	private final boolean[][] grid;
	
	/**
	 * The X dimension of the grid
	 */
	private final int dimX;
	
	/**
	 * The Y dimension of the grid
	 */
	private final int dimY;
	
	/**
	 * The set of particles in the filter
	 */
	public HashSet<Particle> particles;
	
	/**
	 * An enumeration of the four cardinal directions, positive Y, positive X, negative Y, negative X
	 */
	private enum Direction{YP, XP, YN, XN}
	
	/**
	 * A class to store the information on each particle in the filter
	 */
	private static class Particle{
		
		/**
		 * The X coordinate of the particle in the grid
		 */
		int x;
		
		/**
		 * The Y coordinate of the particle in the grid
		 */
		int y;
		
		/**
		 * The cardinal direction the particle is facing
		 */
		Direction direction;
		
		/**
		 * 
		 * @param x The x coordinate of the particle in the grid
		 * @param y The y coordinate of the particle in the grid
		 * @param d The cardinal direction the particle is facing
		 */
		public Particle(int x, int y, Direction d){
			this.x=x;
			this.y=y;
			this.direction = d;	
		}
		
		/**
		 * Compares to particles
		 * If two particles are in the same space on the grid and facing the same direction, this return true, false otherwise
		 * @param o The particle this particle is compared with
		 */
		@Override
		public boolean equals(Object o){
			if(o instanceof Particle){
				Particle e = (Particle)o;
				return x==e.x && y==e.y && direction.equals(e.direction);
			} else return false;
		}
		
		/**
		 * This method is used to generate a hashcode for this object so that when two particles are
		 * the same, they have the same hascode
		 */
		@Override
		public String toString(){return "("+x+","+y+","+direction.toString()+")";}
		
		/**
		 * Returns the hashcode of this particle
		 */
		@Override
		public int hashCode(){return toString().hashCode();}
	}
	
	/**
	 * Checks if the given coordinate is within {@link #grid}
	 * @param x The x coordinate to check
	 * @param y The y coordinate to check
	 * @return True if the coordinate is in the grid, false otherwise
	 */
	public boolean inGrid(int x, int y){
		
		return x >= 0 && x < dimX && y >=0 && y < dimY;
		
	}
	
	/**
	 * Constructs a new {@link ParticleFilter} with given dimensions.
	 * Each space in the filter contains 4 particles, one facing each of the cardinal directions.
	 * @param dimX The x dimension of the particle filter
	 * @param dimY The y dimension of the particle filter
	 */
	public ParticleFilter(int dimX, int dimY){
		
		this.dimX  = dimX;
		this.dimY = dimY;
		grid = new boolean[dimX][dimY];
		
		particles = new HashSet<Particle>(dimX*dimY*4);
		
		//For every cell:
		for(int x = 0; x<dimX; x++){
			for(int y = 0; y<dimY; y++){
				
				//Create a new particle for each direction and add it to the set of particles
				particles.add(new Particle(x,y,Direction.XP));
				particles.add(new Particle(x,y,Direction.XN));
				particles.add(new Particle(x,y,Direction.YP));
				particles.add(new Particle(x,y,Direction.YN));
				
			}			
		}	
	}
	
	/**
	 * 
	 * @return The number of particles remaining in the filter
	 */
	public int getNumberParticles(){
		
		return particles.size();
		
	}
	
	/**
	 * Returns the position and direction of the final particle. 
	 * @return An array containing {x coordinate, y coordinate, direction}, or null if there is not exactly one particle left
	 */
	public int[] getPosition(){
		
		if(getNumberParticles() != 1) return null;
		
		Particle p = (Particle)(particles.toArray()[0]);
		
		return new int[]{p.x, p.y, p.direction.ordinal()};
		
	}
	
	/**
	 * Removes all particles at a specified cell
	 * @param x The x coordinate of the cell
	 * @param y The y coordinate of the cell
	 * 
	 * @return True if the specified cell is in bounds, false otherwise
	 */
	public boolean addObstacle(int x, int y){
		
		if(inGrid(x,y)){

			grid[x][y] = true;
			
			removeParticlesAt(x,y);
			
			return true;
			
		}else return false;
	}
	
	/**
	 * Removes all particles not in the list of specified cells
	 * @param keepCells A list of the specified cells, each one an array containing {x coordinate, y coordinate}
	 */
	public void removeAllBut(List<int[]> keepCells) {
		
		//For each cell:
		for (int x = 0; x < dimX; x++)
			for (int y = 0; y < dimY; y++) {
				
				boolean skip = false;
				
				//For each element of keepCells check if it is the cell we are currently
				//trying to clear and, if so, don't clear this cell
				for (int[] potVictim : keepCells) {
					if (potVictim[0] == x && potVictim[1] == y) {
						skip = true;
						break;
					}
				}
				if (skip) continue;
				
				removeParticlesAt(x, y);
			}
	}
	
	/**
	 * Remove all particles a given cell
	 * @param x The x coordinate of the cell
	 * @param y The y coordinate of the cell
	 * @return True if the cell is in the grid, false otherwise
	 */
	public boolean removeParticlesAt(int x, int y){
		
		
		if(inGrid(x,y)){
			
			//Try to remove each of the 4 possible particles that could be in this cell
			particles.remove(new Particle(x,y,Direction.XP));
			particles.remove(new Particle(x,y,Direction.XN));
			particles.remove(new Particle(x,y,Direction.YP));
			particles.remove(new Particle(x,y,Direction.YN));
			
			return true;
			
		}else return false;
	}
	
	/**
	 * Remove all particles from the filter depending on whether they have an obstacle
	 * or wall in front of them
	 * @param isInfront If true, remove all particles that don't have a particle in front of them, if false remove all that do
	 */
	public void obstacleInfront(boolean isInfront){
		
		//Stack to store all the particles that sould be removed
		Stack<Particle> invalidParticles = new Stack<Particle>();
		
		//Iterate through each particle:
		for(Particle p : particles){
			
			int targetX = -1;
			int targetY = -1;
			
			//Determine which cell is infront of the particle
			if(p.direction.equals(Direction.XP)){
				
				targetX = p.x+1;
				targetY = p.y;
				
			}else if(p.direction.equals(Direction.YN)){
				
				targetX = p.x;
				targetY = p.y-1;
				
			}else if(p.direction.equals(Direction.XN)){
				
				targetX = p.x-1;
				targetY = p.y;
				
			}else if(p.direction.equals(Direction.YP)){
				
				targetX = p.x;
				targetY = p.y+1;
				
			}
			
			//Check if the cell in front is a wall or obstacle
			if(inGrid(targetX,targetY)){
				
				boolean target = grid[targetX][targetY];
				if(target != isInfront) invalidParticles.push(p);
				
			} else {
				
				if(!isInfront) invalidParticles.push(p);
				
			}
			
		}
		
		//Remove each invalid particle
		for(Particle ip: invalidParticles){
			
			particles.remove(ip);
			
		}
		
		
	}
	
	/**
	 * Rotates each of the particles in the filter 90 degrees clockwise
	 */
	public void rotateParticlesClockwise(){
		
		for(Particle p: particles){
			
			if(p.direction.equals(Direction.XP)){
				
				p.direction = Direction.YN;
				
			}else if(p.direction.equals(Direction.YN)){
				
				p.direction = Direction.XN;
				
			}else if(p.direction.equals(Direction.XN)){
				
				p.direction = Direction.YP;
				
			}else if(p.direction.equals(Direction.YP)){
				
				p.direction = Direction.XP;
				
			}
		}
		
		particles = new HashSet<Particle>(particles);
	}
	
	/**
	 * Rotates each of the particles in the filter 90 degrees anti clockwise
	 */
	public void rotateParticlesAntiClockwise(){
		
		for(Particle p: particles){
			
			if(p.direction.equals(Direction.XP)){
				
				p.direction = Direction.YP;
				
			}else if(p.direction.equals(Direction.YN)){
				
				p.direction = Direction.XP;
				
			}else if(p.direction.equals(Direction.XN)){
				
				p.direction = Direction.YN;
				
			}else if(p.direction.equals(Direction.YP)){
				
				p.direction = Direction.XN;
				
			}
		}
		
		particles = new HashSet<Particle>(particles);
	}
	
	/**
	 * Rotates each of the particles in the filter 180 degrees clockwise
	 */
	public void rotateParticles180(){
		
		for(Particle p: particles){
			
			if(p.direction.equals(Direction.XP)){
				
				p.direction = Direction.XN;
				
			}else if(p.direction.equals(Direction.YN)){
				
				p.direction = Direction.YP;
				
			}else if(p.direction.equals(Direction.XN)){
				
				p.direction = Direction.XP;
				
			}else if(p.direction.equals(Direction.YP)){
				
				p.direction = Direction.YN;
				
			}
		}
		
		particles = new HashSet<Particle>(particles);
	}
	
	/**
	 * Moves each particle one square forward in the direction the particle is facing. 
	 * If the particle would move into an obstacle or leave the grid, the particle is removed from the filter
	 */
	public void moveParticlesForward(){
		
		HashSet<Particle> h = new HashSet<Particle>(particles.size());
		
		for(Particle p : particles){
			
			if(p.direction.equals(Direction.XP)){
				
				p.x+=1;
				
			}else if(p.direction.equals(Direction.YN)){
				
				p.y-=1;
				
			}else if(p.direction.equals(Direction.XN)){
				
				p.x-=1;
				
			}else if(p.direction.equals(Direction.YP)){
				
				p.y+=1;
				
			}
			
			if(p.x>=0 && p.x<dimX && p.y>=0 && p.y<dimY && !grid[p.x][p.y]){
				
				h.add(p);
				
			}
		}
		
		particles = h;
		
	}
	
	/**
	 * Returns a string used to display the contents of the particle filter
	 */
	@Override
	public String toString(){
		
		String printString = "";
		
		//For each cell in the grid
		for(int y = dimY-1; y >= 0; y--){
			for(int x = 0; x < dimX; x++){
				
				Particle XP = new Particle(x,y,Direction.XP);
				Particle XN = new Particle(x,y,Direction.XN);
				Particle YP = new Particle(x,y,Direction.YP);
				Particle YN = new Particle(x,y,Direction.YN);
				
				//Check is there is a particle facing each of the four cardinal directions
				boolean xp = particles.contains(XP);
				boolean xn = particles.contains(XN);
				boolean yp = particles.contains(YP);
				boolean yn = particles.contains(YN);
				
				
				//Add the relevant character to the string depending on what particles are in the cell
				if(xp && xn && yp && yn){
					
					printString += '\u254B';
					
				}else if(xp && xn && yp){
					
					printString += '\u253B';
					
				}else if(xp && xn && yn){
					
					printString += '\u2533';
					
				}else if(xp && yp && yn){
					
					printString += '\u2523';
					
				}else if(xn && yp && yn){
					
					printString += '\u252B';
					
				}else if(xp && xn){
					
					printString += '\u2501';
					
				}else if(yp && yn){
					
					printString += '\u2503';
					
				}else if(xp && yp){
					
					printString += '\u2517';
					
				}else if(xp && yn){
					
					printString += '\u250F';
					
				}else if(xn && yp){
					
					printString += '\u251B';
					
				}else if(xn && yn){
					
					printString += '\u2513';
					
				}else if(xp){
					
					printString += '\u257A';
					
				}else if(xn){
					
					printString += '\u2578';
					
				}else if(yp){
					
					printString += '\u2579';
					
					
				}else if(yn){
					
					printString += '\u257B';
					
				}else{
					
					printString += ' ';
					
				}
				
			}

			printString += '\n';
			
		}
		
		return printString;
		
	}
	
	/**
	 * This method is used for debugging and not called in normal use
	 * @param args Unused
	 */
	public static void main(String[] args){
		ParticleFilter filter = new ParticleFilter(6,6);
		filter.addObstacle(2,2);
		filter.addObstacle(3,2);
		filter.addObstacle(4,3);
		filter.addObstacle(5,0);
		
		ArrayList<int[]> list  = new ArrayList<int[]>();
		list.add(new int[] {0,0});
		
		filter.removeAllBut(list);
		
		filter.obstacleInfront(false);
		
		
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(400, 400));
		
		JTextArea textArea = new JTextArea(400,400);
		textArea.setFont(new Font("monospaced", Font.PLAIN, 35));
		textArea.setText(filter.toString());
		frame.add(textArea);
		frame.pack();
		frame.setVisible(true);
		
		Scanner input = new Scanner(System.in);
		
		while(true){
			
			String c = input.next();
			switch(c){
				
				case "w":
					filter.moveParticlesForward();
					break;
				case "s":
					filter.rotateParticles180();
					break;
				case "a":
					filter.rotateParticlesAntiClockwise();
					break;
				case "d":
					filter.rotateParticlesClockwise();
					break;
				case "q":
					System.exit(0);
					break;
			}
			
			textArea.setText(filter.toString());
			
		}
		
	}
	
}