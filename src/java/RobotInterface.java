import java.io.IOException;

public class RobotInterface {
	
	protected final Logger logger = new Logger(getClass());

	public Server server;
	public Robot robot;

	public RobotInterface(Server s, Robot robot) {
		server = s;
		this.robot = robot;
	}
	
	public void parseCommand(String command) throws IOException {
		logger.info(command);
		String[] parts = command.split(":");
		String part1 = parts[0];
		String part2 = parts[1];
		
		if (part1.equals("MOVE")) {
			logger.debug("It is a move command");
			String[] coordinatesStr = part2.split(",");
			int y = Integer.parseInt(coordinatesStr[0]);
			int x = Integer.parseInt(coordinatesStr[1]);
			
			robot.getPilot().travelTo(new Vector2(x, y).div(4));
			server.sendData("ACHIEVED");
			
		} else if (part1.equals("SCAN")) {
			if (part2.equals("COLOUR")) {
				String color = robot.getLeftColorModel().getColor().orElse("White").toLowerCase();
				server.sendData(color);
			}
			
		}
	}
	
	public void sendColour(String colour) throws IOException {
		server.sendData(colour);
	}
	
	public void sendMove(String move) throws IOException {
		server.sendData(move);
	}
	
	
	public void sendIR(String ir) throws IOException {
		server.sendData(ir);
	}