import java.io.IOException;

public class RobotInterface {
	public Server server;
//	public Robot robot;

	public RobotInterface(Server s) {
		server = s;
	}
	
	public void parseCommand(String command) throws IOException {
		System.out.println(command);
		String[] parts = command.split(":");
		String part1 = parts[0];
		String part2 = parts[1];
		
		if (part1.equals("MOVE")) {
			System.out.println("It is a move command");
			String[] coordinatesStr = part2.split(",");
			int x = Integer.parseInt(coordinatesStr[0]);
			int y = Integer.parseInt(coordinatesStr[1]);
			
			int[] coordinates = {x, y};
//			robot.moveTo(coordinates);
			server.sendData("ACHIEVED");
			
		} else if (part1.equals("SCAN")) {
			if (part2.equals("COLOUR")) {
				String colour = "burgandy";
//				colour = robot.scanColour()
				server.sendData(colour);
			}
			
		}
	}
	
	public void sendColour(String colour) throws IOException {
		server.sendData(colour);
	}
	
	public void sendMove(String move) throws IOException {
		server.sendData(move);
	}
	
}
