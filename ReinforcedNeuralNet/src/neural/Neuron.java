package neural;

import java.util.ArrayList;

public class Neuron {
	public ArrayList<Connection> in_conn;
	public double value;
	
	public Neuron() {
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
		return Math.tanh(x);
	}
}
