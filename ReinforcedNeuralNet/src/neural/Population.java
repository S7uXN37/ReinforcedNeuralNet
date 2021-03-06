package neural;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Population {
	private static final int specTarget = 10;
	private static final int popSize = 50;
	private float deltaT = 3.0f;
	private final float dDeltaT = 0.3f;
	public ArrayList<NeuralNet> population;
	private ArrayList<Species> species;
	
	public Population(Random r) {
		population = new ArrayList<NeuralNet>();
		
		int MIN_NODES = NeuralNet.INPUTS + NeuralNet.OUTPUTS;
		int MAX_NODES = MIN_NODES + 2;
		
		// add starting nets
		for (int i = 0; i < popSize; i++) {
			ArrayList<Gene> genes = new ArrayList<Gene>();
			
			int nodes = (int) (r.nextDouble() * (MAX_NODES - MIN_NODES + 1) + MIN_NODES);
			for (int node = 0; node < NeuralNet.INPUTS; node++) {
				int out = node + NeuralNet.INPUTS;
				double weight = r.nextDouble() - 0.5d;
				
				Gene g = new Gene(node, out, weight, true);
				genes.add(g);
			}
			
			Gene[] genome = new Gene[genes.size()];
			for (int n = 0; n < genome.length; n++) {
				genome[n] = genes.get(n);
			}
			
			NeuralNet net = new NeuralNet(genome);
			while (net.neurons.size() < nodes) {
				net.mutateAddNode(r);
				net.mutateAddConnection(r);
				net.mutateAddConnection(r);
			}
			
			population.add(net);
		}
	}
	
	private void recalculateSpecies(boolean stop) {
		for (NeuralNet member : population) {
			boolean assigned = false;
			for (NeuralNet n : population) {
				if (member.getDistanceTo(n) < deltaT && n.species != -1) {
					species.get(n.species).members.add(member);
					member.species = n.species;
					assigned = true;
					break;
				}
			}
			
			if (!assigned) {
				Species newS = new Species(member);
				species.add(newS);
				member.species = species.size() - 1;
			}
		}
		
		if (species.size() > specTarget && !stop) {
			deltaT -= dDeltaT;
			recalculateSpecies(true);
		} else if (species.size() < specTarget && !stop) {
			deltaT += dDeltaT;
			recalculateSpecies(true);
		}
	}
	
	public void nextGeneration(Random r) {
		// TODO too many bad organisms survive, maybe possible to eliminate bad species altogether
		double totFitness = 0d;
		for (Species s : species) {
			totFitness += s.getCombinedFitness() / s.members.size();
		}
		ArrayList<NeuralNet> totOffspring = new ArrayList<NeuralNet>();
		for (Species s : species) {
			int num_offspring = (int) (popSize * s.getCombinedFitness() / s.members.size() / totFitness);
			ArrayList<NeuralNet> offspring = s.reproduce(num_offspring, r);
			for (NeuralNet n : offspring) {
				totOffspring.add(n);
			}
		}
		int missingNets = popSize - totOffspring.size();
		assert species.size() > 0 : "No Species found";
		ArrayList<NeuralNet> offspring = species.get(r.nextInt(species.size())).reproduce(missingNets, r);
		for (NeuralNet n : offspring) {
			totOffspring.add(n);
		}
		
		population = totOffspring;
		
		mutate(r);
		resetSpecies();
		recalculateSpecies(false);
		
		for (NeuralNet n : population) {
			n.fitness = 0;
		}
	}
	
	private void resetSpecies() {
		species = new ArrayList<Species>();
		for (NeuralNet n : population) {
			n.species = -1;
		}
	}

	public void firstGeneration(Random r) {
		species = new ArrayList<Species>();
		
		resetSpecies();
		recalculateSpecies(false);
	}

	private void mutate(Random r) {
		for (NeuralNet n : population) {
			n.mutate(r);
		}
	}
}

class Species {
	public ArrayList<NeuralNet> members;

	public Species(NeuralNet representative) {
		members = new ArrayList<NeuralNet>();
		members.add(representative);
	}
	
	private double getAdjFitness (int index) {
		double fit = members.get(index).fitness;
		double reduc = members.size();
		return fit / reduc;
	}
	
	public double getCombinedFitness() {
		double tot = 0d;
		for (int i = 0; i < members.size(); i++) {
			tot += getAdjFitness(i);
		}
		return tot;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<NeuralNet> reproduce (int newSize, Random r) {
		ArrayList<NeuralNet> offspring = new ArrayList<NeuralNet>();
		
		if (newSize <= 0)
			return offspring;
		
		if (members.size() > 5) {
			NeuralNet champ = members.get(0);
			for (NeuralNet n : members) {
				if (n.fitness > champ.fitness) {
					champ = n;
				}
			}
			offspring.add(champ);
		}
		if (members.size() >= 2) {
			ArrayList<NeuralNet> fitParents = new ArrayList<NeuralNet>();
			// sort NeuralNets, best first
			ArrayList<NeuralNet> sorted = (ArrayList<NeuralNet>) members.clone();
			sorted.sort(new Comparator<NeuralNet>() {
				@Override
				public int compare(NeuralNet o1, NeuralNet o2) {
					if (o1.fitness == o2.fitness)
						return 0;
					else
						return o1.fitness > o2.fitness ? -1 : 1;
				}
			});
			
			// add organisms until 5 fit parents are found
			for (int i = 0; i < sorted.size() && fitParents.size() < 5; i++) {
				NeuralNet n = sorted.get(i);
				fitParents.add(n);
			}
			
			while (offspring.size() < newSize) {
				int p1 = r.nextInt(fitParents.size());
				int p2 = r.nextInt(fitParents.size() - 1);
				if (p2 >= p1) {
					p2++;
				}
				
				ArrayList<Gene> childGenes = NeuralNet.crossOver(fitParents.get(p1), fitParents.get(p2), r);
				Gene[] genome = new Gene[childGenes.size()];
				for (int n = 0; n < childGenes.size(); n++) {
					genome[n] = childGenes.get(n);
				}
				NeuralNet child = new NeuralNet(genome);
				offspring.add(child);
			}
			

			String s = "Species: ";
			for (NeuralNet n : members)
				s += ", "+n.fitness;
			s += ", fitParents: ";
			for (NeuralNet n : fitParents)
				s += ", "+n.fitness;
			System.out.println(s);
		}
		
		return offspring;
	}
}