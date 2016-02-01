package neural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NeuralNet {
	public static final int INPUTS = 2;
	public static final int OUTPUTS = 2;
	
	private ArrayList<Gene> genes;
	
	private int[] inputInd;
	private int[] outputInd;
	
	protected HashMap<Integer, Neuron> neurons;
	private HashMap<Integer, ArrayList<Integer>> unconnectedNodes;
	private HashMap<Integer, Gene> innovGeneMap;
	private ArrayList<Connection> connections;
	
	private int highestNeuronInd = -1;
	
	public int species = -1;
	public double fitness;
	
	public NeuralNet(Gene[] genome) {
		genes = new ArrayList<Gene>();
		
		for (Gene g : genome)
			genes.add(g);
		
		ArrayList<Integer> neur = new ArrayList<Integer>();
		
		// set up two lists, inputInd and outputInd to keep references to which neurons are in the input and output layer respectively
		inputInd = new int[INPUTS];
		for (int n = 0; n < INPUTS; n++) {
			highestNeuronInd++;
			inputInd[n] = highestNeuronInd;
			neur.add(highestNeuronInd);
		}
		outputInd = new int[OUTPUTS];
		for (int n = 0; n < OUTPUTS; n++) {
			highestNeuronInd++;
			outputInd[n] = highestNeuronInd;
			neur.add(highestNeuronInd);
		}
		
		// create neurons according to genes, for each gene ensure both the in & out neurons are in neur
		for (Gene g : genes) {
			if (!g.active)
				continue;
			if (!neur.contains(g.in))
				neur.add(g.in);
			if (!neur.contains(g.out))
				neur.add(g.out);
		}
		
		// setup a HashMap neurons to hold references to them
		neurons = new HashMap<Integer, Neuron>();
		for (int nID : neur) {
			neurons.put(nID, new Neuron(nID));
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
		}
		
		// create dictionary to hold all UNconnected nodes for every node
		computeNodeConnectionMap();
		
		computeInnovGeneMap();
	}
	
	private void computeNodeConnectionMap() {
		unconnectedNodes = new HashMap<Integer, ArrayList<Integer>>();
		
		for (int id : neurons.keySet()) {
			ArrayList<Integer> unconnected = new ArrayList<Integer>();
			for (int idOther : neurons.keySet()) {
				if (idOther != id) {
					boolean foundConn = false;
					for (Connection c : connections) {
						if (c.originID == id && c.targetID == idOther) {
							foundConn = true;
							break;
						} else if (c.originID == idOther && c.targetID == id) {
							foundConn = true;
							break;
						}
					}
					
					if (!foundConn) {
						unconnected.add(idOther);
					}
				}
			}
			
			unconnectedNodes.put(id, unconnected);
		}
	}
	
	private void computeInnovGeneMap () {
		innovGeneMap = new HashMap<Integer, Gene>();
		for (Gene g : genes) {
			innovGeneMap.put(g.innov, g);
		}
	}
	
	public void tick(final double[] input) {
		// put input into first neurons
		for (int i = 0; i < input.length; i++) {
			neurons.get(inputInd[i]).value = input[i];
		}
		// update all connections
		for (Connection c : connections) {
			c.update();
		}
		// update all neurons
		for (Neuron n : neurons.values()) {
			n.update();
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
	
	public void mutate(Random r) {
		// mutate connections
		if (r.nextDouble() < 0.8d) {
			for (Gene g : genes) {
				if (r.nextDouble() < 0.9d) {
					// uniform perturbation = add uniformly-distributed random value
					double newWeight = g.weight + r.nextDouble() * 0.2 - 0.1d;
					newWeight = Math.min(1d, newWeight);
					newWeight = Math.max(-1d, newWeight);
					g.weight = newWeight;
				} else {
					// new random value
					g.weight = r.nextDouble() * 2 - 1d;
				}
			}
		}
		// add mutations
		if (r.nextDouble() < 0.01d) {
			mutateAddConnection(r);
		} else if (r.nextDouble() < 0.1d) {
			mutateAddNode(r);
		}
		
		computeInnovGeneMap();
		computeNodeConnectionMap();
	}
	
	protected void mutateAddConnection(Random r) {
		Object[] keys = neurons.keySet().toArray();
		int origin = (int) keys[r.nextInt(keys.length)];
		
		ArrayList<Integer> unconnected = unconnectedNodes.get(origin);
		if (unconnected.size() == 0) {
			boolean found = false;
			for (int i = 0; i < keys.length; i++) {
				unconnected = unconnectedNodes.get((int) keys[i]);
				if (unconnected.size() > 0) {
					found = true;
					break;
				}
			}
			if (!found) {
				return;
			}
		}
		int unconnInd = r.nextInt(unconnected.size());
		int target = unconnected.get(unconnInd);
		
		Gene newConnection = new Gene(origin, target, r.nextDouble()-0.5d, true);
		genes.add(newConnection);
		
		connections.add(new Connection(neurons.get(origin), neurons.get(target), newConnection.weight));
		
		computeNodeConnectionMap();
		computeInnovGeneMap();
	}
	
	protected void mutateAddNode(Random r) {
		Connection toSplit = connections.get(r.nextInt(connections.size()));
		highestNeuronInd++;
		int newID = highestNeuronInd;
		neurons.put(newID, new Neuron(newID));
		
		Gene newGene1 = new Gene(toSplit.originID, newID, 1d, true);
		Gene newGene2 = new Gene(newID, toSplit.targetID, toSplit.weight, true);

		for (Gene g : genes) {
			if (g.in == toSplit.originID && g.out == toSplit.targetID) {
				g.active = false;
				break;
			}
		}
		
		genes.add(newGene1);
		genes.add(newGene2);
		
		int connToRemove = -1;
		for (int i = 0; i < connections.size(); i++) {
			Connection c = connections.get(i);
			if (c.originID == toSplit.originID && c.targetID == toSplit.targetID) {
				connToRemove = i;
				break;
			}
		}
		connections.remove(connToRemove);
		
		connections.add(new Connection(neurons.get(newGene1.in), neurons.get(newGene1.out), toSplit.weight));
		connections.add(new Connection(neurons.get(newGene2.in), neurons.get(newGene2.out), toSplit.weight));
		
		computeNodeConnectionMap();
		computeInnovGeneMap();
	}
	
	public static ArrayList<Gene> crossOver(NeuralNet n1, NeuralNet n2, Random r) {
		// TODO definitely review:
		// still have things like 4-4
		n1.computeInnovGeneMap();
		n2.computeInnovGeneMap();
		HashMap<Integer, Gene> geneMap1 = n1.innovGeneMap;
		HashMap<Integer, Gene> geneMap2 = n2.innovGeneMap;
		
		double f1 = n1.fitness;
		double f2 = n2.fitness;
		
		ArrayList<Gene> newGenes = new ArrayList<Gene>();
		ArrayList<Integer> availInnov = new ArrayList<Integer>();
		for (int innov : geneMap1.keySet()) {
			availInnov.add(innov);
		}
		for (int innov : geneMap2.keySet()) {
			if (!availInnov.contains(innov))
				availInnov.add(innov);
		}
		
		for (int innov : availInnov) {
			Gene dominant = null;
			boolean disabled = false;
			if (geneMap1.containsKey(innov) && geneMap2.containsKey(innov)) {
				// choose dominant gene randomly
				boolean disabledInEither = (!geneMap1.get(innov).active) || (!geneMap2.get(innov).active);
				if (r.nextDouble() < 0.5) {
					dominant = geneMap1.get(innov);
				} else {
					dominant = geneMap2.get(innov);
				}
				
				// 75%: disabled if disabled in either
				if (disabledInEither && r.nextDouble() < 0.75d) {
					dominant.active = false;
					disabled = true;
				}
				
				// 40%: inherit average
				if (r.nextDouble() < 0.4d) {
					dominant.weight = (geneMap1.get(innov).weight + geneMap2.get(innov).weight) / 2d;
				}
			} else {
				// only choose if from better parent, if equal choose randomly
				if (geneMap1.containsKey(innov) && f1 > f2) {
					dominant = geneMap1.get(innov);
				} else if (geneMap2.containsKey(innov) && f2 > f1) {
					dominant = geneMap2.get(innov);
				} else if (f1 == f2) {
					if (r.nextDouble() < 0.5) {
						if (geneMap1.containsKey(innov)) {
							dominant = geneMap1.get(innov);
						} else  if (geneMap2.containsKey(innov)) {
							dominant = geneMap2.get(innov);
						}
					}
				}
			}
			
			if (dominant != null) {
				if (!disabled && !dominant.active && r.nextDouble() < 0.25d)
					dominant.active = true;
				
				newGenes.add(dominant);
			}
		}
		
		return newGenes;
	}
	
	public double getDistanceTo (NeuralNet other) {
		int nonMatching = 0;
		int matching = 0;
		double totWeightDiff = 0;
		
		ArrayList<Integer> innovLookedAt = new ArrayList<Integer>();
		for (int innov : innovGeneMap.keySet()) {
			innovLookedAt.add(innov);
			
			if (other.innovGeneMap.containsKey(innov)) {
				double w1 = innovGeneMap.get(innov).weight;
				double w2 = other.innovGeneMap.get(innov).weight;
				totWeightDiff += Math.abs(w1 - w2);
				matching++;
			} else {
				nonMatching++;
			}
		}
		for (int innov : other.innovGeneMap.keySet()) {
			if (innovLookedAt.contains(innov))
				continue;
			
			if (!other.innovGeneMap.containsKey(innov)) {
				nonMatching++;
			}
		}
		
		return 2 * nonMatching + 2 * (totWeightDiff / (double) matching);
	}
}
