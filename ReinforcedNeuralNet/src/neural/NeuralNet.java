package neural;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class NeuralNet {
	private Gene[] genes;
	
	private int[] inputInd;
	private int[] outputInd;
	
	private HashMap<Integer, Neuron> neurons;
	private ArrayList<Connection> connections;
	
	public NeuralNet(int inputs, int outputs, Gene[] genome) {
		genes = genome;
		ArrayList<Integer> neur = new ArrayList<Integer>();
		
		inputInd = new int[inputs];
		for (int n = 0; n < inputs; n++) {
			inputInd[n] = n;
			neur.add(n);
		}
		outputInd = new int[outputs];
		for (int n = 0; n < outputs; n++) {
			outputInd[n] = n + inputs;
			neur.add(n+inputs);
		}
		
		// create neurons according to genes
		for (Gene g : genes) {
			if (!g.active)
				continue;
			if (!neur.contains(g.in))
				neur.add(g.in);
			if (!neur.contains(g.out))
				neur.add(g.out);
		}
		
		neurons = new HashMap<Integer, Neuron>();
		for (int nID : neur) {
			neurons.put(nID, new Neuron());
			System.out.println("New Neuron: " + nID);
		}
		
		// create edges
		ArrayList<Gene> conn = new ArrayList<Gene>();
		for (Gene g : genes) {
			if (!g.active)
				continue;
			conn.add(g);
		}
		
		connections = new ArrayList<Connection>();
		for (int i = 0; i < conn.size(); i++) {
			Gene g = conn.get(i);
			Neuron t1 = neurons.get(g.in);
			Neuron t2 = neurons.get(g.out);
			
			connections.add(new Connection(t1, t2, g.weight));
			System.out.println("New Connection: " + g.in + "-" + g.out + ", " + g.weight);
		}
		
		// add connections to neurons
		for (Connection c : connections) {
			c.target.in_conn.add(c);
		}
	}
	
	public void tick(final double[] input) {
		// put input into first neurons
		for (int i = 0; i < input.length; i++) {
			neurons.get(inputInd[i]).value = input[i];
		}
		// update all neurons
		for (Neuron n : neurons.values()) {
			n.update();
		}
		// update all connections
		for (Connection c : connections) {
			c.update();
		}
	}
	
	public double[] getOutput() {
		// get out neurons
		// arrange values in array
		double[] out = new double[outputInd.length];
		for (int i = 0; i < outputInd.length; i++) {
			out[i] = neurons.get(outputInd[i]).value;
		}
		return out;
	}
}
