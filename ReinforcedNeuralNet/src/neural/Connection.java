package neural;

import java.util.Random;

public class Connection {
	public double weight;
	public double deltaWeight;
	
	public Connection() {
		weight = (new Random()).nextDouble()-0.5d;
	}
}
