import java.util.HashSet;

public class ParticleFilter{
	
	private final boolean[][] grid;
	
	private final int dimX;
	private final int dimY;
	
	public HashSet<Particle> particles;
	
	private enum Direction{XP,XN,YP,YN}
	
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
	
	public boolean addObstacle(int x, int y){
		
		if(x>=0 && x<dimX && y>=0 && y<dimY){

			grid[x][y] = true;
			
			particles.remove(new Particle(x,y,Direction.XP));
			particles.remove(new Particle(x,y,Direction.XN));
			particles.remove(new Particle(x,y,Direction.YP));
			particles.remove(new Particle(x,y,Direction.YN));
			
			return true;
			
		}else return false;
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
	
	public static void main(String[] args){
		ParticleFilter filter = new ParticleFilter(6,6);
		System.out.println(filter.particles);
		System.out.println(filter.particles.size());
		filter.addObstacle(0,0);
		System.out.println(filter.particles.size());
		filter.moveParticlesForward();
		System.out.println(filter.particles.size());
		Particle x = new Particle(1,2,Direction.XP);
		Particle y = new Particle(1,2,Direction.XP);
		filter.rotateParticles180();
		System.out.println(x.hashCode() == y.hashCode());
		System.out.println(filter.particles.contains(x));
	}
	
}