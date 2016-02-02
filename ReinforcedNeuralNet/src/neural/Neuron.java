package neural;

import java.util.ArrayList;

public class Neuron {
	public ArrayList<Connection> in_conn;
	public int id;
	public double value;
	
	public Neuron(int id) {
		this.id = id;
		in_conn = new ArrayList<Connection>();
	}

	public void update() {
		double sum = 0D;
		
		for (Connection in : in_conn) {
			sum += in.value;
		}
		
		if (in_conn.size() > 0) // not bias or input
			value = transfer(sum);
	}
	
	public static double transfer (double x) {
		double y = 1 / (1 + Math.pow(Math.E, -4.9 * x)) * 2 - 1;
		return y;
	}
}
