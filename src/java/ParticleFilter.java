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
	
	private final boolean[][] grid;
	
	private final int dimX;
	private final int dimY;
	
	public HashSet<Particle> particles;
	
	private enum Direction{YP, XP, YN, XN}
//	private enum Direction{XP,XN,YP,YN}
//	
	private static class Particle{
		int x;
		int y;
		Direction direction;
		
		public Particle(int x, int y, Direction d){
			this.x=x;
			this.y=y;
			this.direction = d;	
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof Particle){
				Particle e = (Particle)o;
				return x==e.x && y==e.y && direction.equals(e.direction);
			} else return false;
		}
		
		@Override
		public String toString(){return "("+x+","+y+","+direction.toString()+")";}
		
		@Override
		public int hashCode(){return toString().hashCode();}
	}
	
	public boolean inGrid(int x, int y){
		
		return x >= 0 && x < dimX && y >=0 && y < dimY;
		
	}
	
	public ParticleFilter(int dimX, int dimY){
		
		this.dimX  = dimX;
		this.dimY = dimY;
		grid = new boolean[dimX][dimY];
		
		particles = new HashSet<Particle>(dimX*dimY*4);
		
		
		for(int x = 0; x<dimX; x++){
			for(int y = 0; y<dimY; y++){
				
				particles.add(new Particle(x,y,Direction.XP));
				particles.add(new Particle(x,y,Direction.XN));
				particles.add(new Particle(x,y,Direction.YP));
				particles.add(new Particle(x,y,Direction.YN));
				
			}			
		}	
	}
	
	public int getNumberParticles(){
		
		return particles.size();
		
	}
	
	public int[] getPosition(){
		
		if(getNumberParticles() != 1) return null;
		
		Particle p = (Particle)(particles.toArray()[0]);
		
		return new int[]{p.x, p.y, p.direction.ordinal()};
		
	}
	
	public boolean addObstacle(int x, int y){
		
		if(inGrid(x,y)){

			grid[x][y] = true;
			
			removeParticlesAt(x,y);
			
			return true;
			
		}else return false;
	}
	
	public void removeAllBut(List<int[]> keepCells) {
		for (int x = 0; x < dimX; x++)
			for (int y = 0; y < dimY; y++) {
				boolean skip = false;
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
	
	public boolean removeParticlesAt(int x, int y){
		
		if(inGrid(x,y)){
			particles.remove(new Particle(x,y,Direction.XP));
			particles.remove(new Particle(x,y,Direction.XN));
			particles.remove(new Particle(x,y,Direction.YP));
			particles.remove(new Particle(x,y,Direction.YN));
			
			return true;
			
		}else return false;
	}
	
	public void obstacleInfront(boolean isInfront){
		
		Stack<Particle> invalidParticles = new Stack<Particle>();
		
		for(Particle p : particles){
			
			int targetX = -1;
			int targetY = -1;
			
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
			
			if(inGrid(targetX,targetY)){
				
				boolean target = grid[targetX][targetY];
				if(target != isInfront) invalidParticles.push(p);
				
			} else {
				
				if(!isInfront) invalidParticles.push(p);
				
			}
			
		}
		for(Particle ip: invalidParticles){
			
			particles.remove(ip);
			
		}
		
		
	}
	
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
	
	@Override
	public String toString(){
		
		String printString = "";
		
		for(int y = dimY-1; y >= 0; y--){
			for(int x = 0; x < dimX; x++){
				
				Particle XP = new Particle(x,y,Direction.XP);
				Particle XN = new Particle(x,y,Direction.XN);
				Particle YP = new Particle(x,y,Direction.YP);
				Particle YN = new Particle(x,y,Direction.YN);
				
				boolean xp = particles.contains(XP);
				boolean xn = particles.contains(XN);
				boolean yp = particles.contains(YP);
				boolean yn = particles.contains(YN);
				
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