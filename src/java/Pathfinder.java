import java.util.PriorityQueue;
import java.util.Stack;
import java.util.HashSet;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;


public class Pathfinder{
	
	public Node map[][];
	
	/**
	 *  X dimension of map
	 */
	private int mapXDim;
	
	/**
	 * Y Dimension of map
	 */
	private int mapYDim;
	
	
	//Used for testing. Not called in normal use
	public static void main(String[] args){
		
		Pathfinder p = new Pathfinder(6,7);
		p.printPrev();
		
		//Set occupied cells
		//p.updateCell(5,4,true);
		//p.updateCell(0,2,true);
		p.updateCell(0,3,true);
		//p.updateCell(4,4,true);
		//p.updateCell(4,6,true);
		//p.updateCell(4,5,true);
		
		p.printMap();
		
		System.out.println("Path: "+java.util.Arrays.toString(p.getPath(0,2,5,6)));
		//"D:" + depth + (  prev!=null?" P:(" + prev.x + "," + prev.y + ")":"") + " O:" + (occupied?'1':'0')
		p.printPath(0,2,5,6);
		
		
	}
	
	/**
	 * Creates new pathfinder with empty map with specified X and Y dimensions
	 * @param x X dimension of internal map
	 * @param y Y dimension of internal map
	 */
	public Pathfinder(int x, int y){
		
		map = new Node[x][y];
		mapXDim = x;
		mapYDim = y;
		
		for(int i=0;i<x;i++)
			for(int j=0;j<y;j++)
				map[i][j] = new Node(i,j,false);
		
	}
	
	
	public void updateCells(ParamedicEnv.RobotBayModel model) {
		ArrayList<int[]> obstaclesPos = model.getObstacleLocations();
		for (int i = 0; i < obstaclesPos.size(); i++) {
			int[] obstaclePos = obstaclesPos.get(i);
			updateCell(obstaclePos[0], obstaclePos[1], true);
		}
	}
	
	public void updateCell(int x, int y, boolean val){
		
		map[x][y].occupied = val;
		
	}
	
	/**
	 * Calls default pathfinding algorithm. Only A Star has been implemented, so always calls {@link #A_Star(int, int, int, int)}
	 * @param startx X coordinate of starting point
	 * @param starty Y coordinate of starting point
	 * @param endx X coordinate of goal
	 * @param endy Y coordinate of goal
	 * @return An array of {@link #Node} in order that should be traversed to reach goal. Last eleent is always goal unless no path found. Returns null if no path found.
	 */
	public Node[] getPath(int startx, int starty, int endx, int endy){
		
		return A_Star(startx,starty,endx,endy);
		
	}
	
	
	/**
	 * Class to store information about a cell while calculating path
	 *
	 */
	public static class Node{
		
		/**
		 * X Position in map
		 */
		public final int x;
		
		/**
		 * Y Position in map
		 */
		public final int y;
		
		/**
		 * The Manhattan distance of this cell from the starting point
		 */
		private int distance;
		
		/**
		 * After a pathfinding method has been called, if this cell is in the resulting path,
		 * this is set to the previous node in the path or null if it is the first element of path
		 */
		private Node prev;
		
		/**
		 * If this Node is in the path depth is equal to prev.depth+1. If prev is null depth is 0
		 */
		private int depth;
		
		/**
		 * True if robot cannot move through cell, false otherwise
		 */
		private boolean occupied;
		
		private Node(int x, int y, boolean occupied){
			this.x = x;
			this.y = y;
			this.occupied = occupied;
			distance = Integer.MAX_VALUE;
			prev = null;
			depth = 0;		
			
		}
		
		
		
		/**
		 * If given previous node in path, sets {@link #prev},{@link #distance} and {@link #depth} of this Node
		 * @param prev Previous node in path
		 */
		private void setPrev(Node prev){
			this.prev = prev;

			if(prev != null){
				
				distance = distanceTo(prev) + prev.distance;
				depth = prev.depth +1;
			}
			else{
				distance = 0;
				depth = 0;
			}
			
		}
		
		/**
		 * Returns absolute Manhattan distance from this node to specified node
		 */
		public int distanceTo(Node n){
			
			return Math.abs(x - n.x) + Math.abs(y - n.y);
			
		}
		

		/**
		 * Returns the value of the heuristic used for the A star algorithm (i.e. returns h(x))
		 * Using Manhattan distance to end point as heuristic
		 * @param endx The X coordinate of the goal we are trying to find a path to
		 * @param endy The Y coordinate of the goal we are trying to find a path to
		 * @return The heuristic value of this cell
		 */
		private int aStarHeuristic(int endx, int endy){
			
			return Math.abs(x - endx) + Math.abs(y - endy);
			
		}
		
		/**
		 * returns the estimated cost of the path if this node is selected (i.e. returns f(x) = g(x) + h(x))
		 * @param endxThe X coordinate of the goal we are trying to find a path to
		 * @param endy The Y coordinate of the goal we are trying to find a path to
		 * @return The estimated cost of this cell
		 */
		public int estCost(int endx, int endy){ 
			
			if(distance == Integer.MAX_VALUE) {
				
				return distance;
				
			}
			else {
				return distance + aStarHeuristic(endx,endy);
			}
			
		}
		
		/**
		 * Override equals method so that cells in same location in map are considered the same
		 * @param n The cell we are comparing this cell to
		 * @return True of this cell is in the same position as n, false otherwise
		 */
		public boolean equals(Node n){return x==n.x && y==n.y;}
	
		/**
		 * Returns the path to get to his node from the starting point
		 * @return An array of {@link Pathfinder#Node}, each element representing the next Node to go to to get to the goal
		 */
		public Node[] path(){
			
			Node[] path = new Node[depth];
			for(Node current = this; current.prev!=null; current = current.prev){
				path[current.depth-1] = current;

			}
			
			return path;
			
		}
		
		public String toString(){
			
			return "(" + x + "," + y +")";
			
		}
		
		/**
		 * Returns the number of walls orthogonally surrounding this cell, max 4
		 * @return  the number of walls surrounding this cell
		 */
		public int numberOccupiedNeighbors(Pathfinder p) {
			
			Node[][] map = p.map;
			Node center = map[x][y];
			int result = 0;
			
			if(x > 0 && map[x-1][y].occupied) result++;
			if(x==0)result++;
			
			if(x < p.mapXDim-1 && map[x+1][y].occupied)result++;
			if(x==p.mapXDim-1)result++;
			
			if(y > 0 && map[x][y-1].occupied)result++;
			if(y==0)result++;
			
			if(y < p.mapYDim-1 && map[x][y+1].occupied)result++;
			if(y==p.mapYDim-1)result++;
			
			return result;
		}
		
	}
	
	/**
	 * Returns array of nodes representing path from start point to end point using the A* algorithm
	 * Returns null if no path possible
	 * @param startx The starting x coordinate of the path
	 * @param starty The starting y coordinate of the path
	 * @param endx The x coordinate of the goal
	 * @param endy The y coordinate of the goal
	 * @return array of nodes representing path from start point to end point
	 */
	private Node[] A_Star(final int startx, final int starty, final int endx, final int endy){
		
		final Pathfinder p = this;
		
		//Create priority queue that orders based on estimated cost
		//i.e. heuristic + distance from start
		PriorityQueue<Node> agenda = new PriorityQueue<Node>(mapXDim*mapYDim,
			new Comparator<Node>(){
				
			
				public int compare(Node n1, Node n2){
					
					int n1Cost = n1.estCost(endx,endy);
					int n2Cost = n2.estCost(endx,endy);
						
						return n1Cost-n2Cost;


				}
				
			}
		);
		
		//Set of elements that have already been found
		HashSet<Node> closed = new HashSet<Node>();
		
		Node start = map[startx][starty];
		start.distance = 0;
		agenda.add(start);
		
		//While there are still Nodes to check
		while(!agenda.isEmpty()){
			
			//Get the next node with the minimum estimated cost
			Node next = agenda.poll();
			

			
			//If this node is the end point, finish
			if(next.x == endx && next.y == endy){

				
				return next.path();
				
				
			}
			
			closed.add(next);
			
			int x = next.x;
			int y = next.y;
			
			//Get neighbors of cell
			Stack<Node> neighbors = new Stack<Node>();
			
			if(x > 0){
				neighbors.push(map[x-1][y]);
			}
			if(x < mapXDim-1){
				neighbors.push(map[x+1][y]);
			}
			if(y > 0){
				neighbors.push(map[x][y-1]);
			}
			if(y < mapYDim-1){
				neighbors.push(map[x][y+1]);
			}
			
			//For each neighbor
			while(!neighbors.isEmpty()){
				
				Node neighbor = neighbors.pop();
				
				//Ignore it if is is occupied or has already been found
				if(neighbor.occupied || closed.contains(neighbor))continue;
				
				//If this neighbor is chosen, how far would it be from the start
				int tempDistance = next.distance + next.distanceTo(neighbor);
				
				if(!agenda.contains(neighbor)) agenda.add(neighbor);
				
				//If we have already found a better way to get to this Node, don't update
				else if(tempDistance >= neighbor.distance) continue;
				
				//If this is the best way to get to this Node, set the previous node in the path
				neighbor.setPrev(next);
				
			}
			
		}
		
		//If we empty agenda, no possible path
		//System.out.println("Path Failed");
		return null;
	}
	
	/**
	 * Returns the set of {@link Pathfinder#Node} that are reachable from the given position
	 * @param x X coordinate of given position
	 * @param y Y coordinate of given position
	 * @return {@link HashSet} of Nodes that are reachable
	 */ 
	public HashSet<Node> reachable(int x, int y) {
		
		HashSet<Node> reached= new HashSet<Node>();
		Stack<Node> agenda = new Stack<Node>();
		agenda.push(map[x][y]);
		
		while(!agenda.isEmpty()) {
			
			Node next = agenda.pop();
			if(!reached.contains(next)) {
				
				reached.add(next);
					
				if (!next.occupied){
					if(next.x > 0){
						agenda.push(map[next.x-1][next.y]);
					}
					if(next.x < mapXDim-1){
						agenda.push(map[next.x+1][next.y]);
					}
					if(next.y > 0){
						agenda.push(map[next.x][next.y-1]);
					}
					if(next.y < mapYDim-1){
						agenda.push(map[next.x][next.y+1]);
					}
				}
			}
			
			
		}
		
		
		return reached;
	}
	
	
	//Used for testing. Not called in normal use
	//prints the contents of each cell
	public void printMap(){
		
		for(Node[] row: map){
		
			String printString = "";
			
			for(Node n: row){
				
				if(n.occupied) printString += 'X';
				else printString += '.';
				
			}
			
			System.out.println(printString);
		}
	}
	
	//Used for testing. Not called in normal use
	//Prints contents of each cell and highlights start point, end point and each cell in path
	public void printPath(int startx, int starty, int endx, int endy){
		
		HashSet<Node> path;
		
		try {
			path = new HashSet<Node>(Arrays.asList(getPath(startx,starty,endx,endy)));
		}
		catch(NullPointerException e) {
			System.out.println("No Path Found");
			return;
			
		}
		Node start = map[startx][starty];
		Node end = map[endx][endy];
		
		for(Node[] row: map){
		
			String printString = "";
			
			for(Node n: row){
				
				if(n.occupied) printString += 'X';
				else if(n.equals(end)&&path.contains(n)) printString += 'E';
				else if(path.contains(n)) printString += '*';
				else if(n.equals(start)) printString += 'S';
				else if(n.equals(end)) printString += 'e';
				else printString += '.';
				
			}
			
			System.out.println(printString);
		}
		
	}
	
	//Used for testing. Not called in normal use
	//For each cell, print Node.prev.
	//If Node.prev is null, print its own position
	public void printPrev(){
		
		for(Node[] row: map){
		
			String printString = "";
			
			for(Node n: row){
				
				/*if(n.prev == null) printString += String.format("(%02d,%02d)", n.x,n.y);
				else printString += "("+ n.prev.x + "," + n.prev.y + ")";*/
				if(n.prev==null)printString+='X';
				else if(n.x-n.prev.x<0)printString+='v';
				else if(n.x-n.prev.x>0)printString+='^';
				else if(n.y-n.prev.y<0)printString+='>';
				else if(n.y-n.prev.y>0)printString+='<';
				
			}
			
			System.out.println(printString);
		}
		System.out.println("\n\n");
		
		
	}
	
	//Used for testing. Not called in normal use
	public void printDistance() {
		
		
		for(Node[] row: map) {
			for(Node node: row) {
				
				System.out.printf("%02d ", node.depth);
				
			}
			System.out.println();
		}
		
	}
	
}
