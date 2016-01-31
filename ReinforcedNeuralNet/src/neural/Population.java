package neural;

import java.util.ArrayList;
import java.util.Random;

public class Population {
	private static final int specTarget = 10;
	private static final int popSize = 50;
	private float deltaT = 3.0f;
	private final float dDeltaT = 0.3f;
	public ArrayList<NeuralNet> population;
	private ArrayList<Species> species;
	
	public Population() {
		// TODO add starting nets
	}
	
	private void recalculateSpecies() {
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
		
		if (species.size() > specTarget) {
			deltaT -= dDeltaT;
		} else if (species.size() < specTarget) {
			deltaT += dDeltaT;
		} else
			return;
		
		recalculateSpecies();
	}
	
	public void nextGeneration(Random r) {
		species = new ArrayList<Species>();
		double totFitness = 0d;
		for (Species s : species) {
			totFitness += s.getCombinedFitness();
		}
		ArrayList<NeuralNet> totOffspring = new ArrayList<NeuralNet>();
		for (Species s : species) {
			int num_offspring = (int) (popSize * s.getCombinedFitness() / totFitness);
			ArrayList<NeuralNet> offspring = s.reproduce(num_offspring, r);
			for (NeuralNet n : offspring) {
				totOffspring.add(n);
			}
		}
		int missingNets = popSize - totOffspring.size();
		ArrayList<NeuralNet> offspring = species.get(r.nextInt(species.size())).reproduce(missingNets, r);
		for (NeuralNet n : offspring) {
			totOffspring.add(n);
		}
		
		population = totOffspring;
		NeuralNet.nextGeneration();
		
		mutate(r);
		recalculateSpecies();
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
	
	public ArrayList<NeuralNet> reproduce (int newSize, Random r) {
		ArrayList<NeuralNet> offspring = new ArrayList<NeuralNet>();
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
			double avgFitness = getCombinedFitness() / members.size();
			for (double d = 0; fitParents.size() < 2; d += 0.1d) {
				for (NeuralNet n : members) {
					if (n.fitness > avgFitness - d) {
						fitParents.add(n);
					}
				}
			}
			
			for (int i = 0; i < newSize; i++) {
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
		}
		return offspring;
	}
}