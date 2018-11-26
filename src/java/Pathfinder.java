import java.util.PriorityQueue;
import java.util.Stack;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Arrays;

public class Pathfinder{
	
	private final Node map[][];
	private final int mapXDim;
	private final int mapYDim;
	
	
	//Main only used for testing
	public static void main(String[] args){
		
		Pathfinder p = new Pathfinder(6,7);

		//Set occupied cells
		p.updateCell(5,4,true);
		//p.updateCell(5,3,true);
		p.updateCell(4,4,true);
		p.updateCell(4,6,true);
		//p.updateCell(4,5,true);
		
		p.printMap();
		
		System.out.println("Path: "+java.util.Arrays.toString(p.getPath(0,0,5,6)));
		//"D:" + depth + (  prev!=null?" P:(" + prev.x + "," + prev.y + ")":"") + " O:" + (occupied?'1':'0')
		p.printPath(0,0,5,6);
		
		
	}
	
	//Creates new pathfinder with empty map with specified X and Y dimensions
	public Pathfinder(int x, int y){
		
		map = new Node[x][y];
		mapXDim = x;
		mapYDim = y;
		
		for(int i=0;i<x;i++)
			for(int j=0;j<y;j++)
				map[i][j] = new Node(i,j,false);
		
	}
	

	
	
	public void updateCell(int x, int y, boolean val){
		
		map[x][y].occupied = val;
		
	}
	
	//Calls default pathfinding algorithm
	//Returns null if no path possible
	public Node[] getPath(int startx, int starty, int endx, int endy){
		
		return A_Star(startx,starty,endx,endy);
		
	}
	
	public static class Node{
		
		//Position in map
		public final int x;
		public final int y;
		
		//Manhattan distance from starting point
		private int distance;
		
		//Previous Node in path
		private Node prev;
		
		//Number of Nodes before this node in path
		private int depth;
		
		private boolean occupied;
		
		private Node(int x, int y, boolean occupied){
			this.x = x;
			this.y = y;
			this.occupied = occupied;
			distance = Integer.MAX_VALUE;
			prev = null;
			depth = 0;		
			
		}
		
		
		//If given previous node in path, sets prev, distace and depth of this Node
		private void setPrev(Node prev){
			this.prev = prev;
			//System.out.println("Cp:" + this.toString() + this.prev.toString() );
			if(prev != null){
				
				distance = distanceTo(prev) + prev.distance;
				depth = prev.depth +1;
			}
			else{
				distance = 0;
				depth = 0;
			}
			
		}
		
		//Returns absolute Manhattan distance to specified node
		public int distanceTo(Node n){
			
			return Math.abs(x - n.x) + Math.abs(y - n.y);
			
		}
		
		//Returns the value of the heuristic used for the A star algorithm (i.e. returns h(x))
		//Using Manhattan distance from end point as heuristic
		private int aStarHeuristic(int endx, int endy){
			
			return Math.abs(x - endx) + Math.abs(y - endy);
			
		}
		
		//returns the estimated cost of the path if this node is selected (i.e. returns f(x) = g(x) + h(x))
		public int estCost(int endx, int endy){ return distance + aStarHeuristic(endx,endy);}
		
		//Override equals method to use position in map
		public boolean equals(Node n){return x==n.x && y==n.y;}
	
		//returns the path to get to this Node
		public Node[] path(){
			
			Node[] path = new Node[depth+1];
			for(Node current = this; current.prev!=null; current = current.prev){
				
				path[current.depth] = current;

			}
			
			return path;
			
		}
		
		public String toString(){
			
			return "(" + x + "," + y +")";
			
		}
		
	}
	
	//Returns array of nodes representing path from start point to end point
	//Returns null if no path possible
	private Node[] A_Star(final int startx, final int starty, final int endx, final int endy){
		
		
		//Create priority queue that orders based on estimated cost
		//i.e. heuristic + distance from start
		PriorityQueue<Node> agenda = new PriorityQueue<Node>(mapXDim*mapYDim,
			new Comparator<Node>(){
				
				public int compare(Node n1, Node n2){return n1.estCost(endx,endy) - n2.estCost(endx,endy);}
				
			}
		);
		
		//Set of elements that have already been found
		HashSet<Node> closed = new HashSet<Node>();
		
		Node start = map[startx][starty];
		
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
		return null;
	}
	
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
	
	//Prints contents of each cell and highlights start point, end point and each cell in path
	//Crashes if there isn't a path
	public void printPath(int startx, int starty, int endx, int endy){
		
		HashSet<Node> path = new HashSet<Node>(Arrays.asList(getPath(startx,starty,endx,endy)));
		
		Node start = map[startx][starty];
		Node end = map[endx][endy];
		
		for(Node[] row: map){
		
			String printString = "";
			
			for(Node n: row){
				
				if(n.occupied) printString += 'X';
				else if(path.contains(n)) printString += '*';
				else if(n.equals(start)) printString += 'S';
				else if(n.equals(end)) printString += 'E';
				else printString += '.';
				
			}
			
			System.out.println(printString);
		}
		
	}
	
	
}