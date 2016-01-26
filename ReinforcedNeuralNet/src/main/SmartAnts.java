package main;

import neural.Gene;
import neural.NeuralNet;

public class SmartAnts {
	public static void main(String args[]) {
		Gene[] g = new Gene[]{
				new Gene(0,2,0.5d,true,1),
				new Gene(1,3,0.5d,true,1),
				new Gene(1,2,0.5d,true,1),
				new Gene(1,4,0.5d,true,1),
				new Gene(4,2,0.5d,true,1),
				new Gene(0,3,0.5d,true,1)
			};
		NeuralNet net = new NeuralNet(2, 2, g);
		
		net.tick(new double[]{0, 1});
		for (double d : net.getOutput()) {
			System.out.println("Result: " + d);
		}
		
		net.tick(new double[]{0, 1});
		for (double d : net.getOutput()) {
			System.out.println("Result: " + d);
		}
		
		net.tick(new double[]{0, 1});
		for (double d : net.getOutput()) {
			System.out.println("Result: " + d);
		}
		
		net.tick(new double[]{0, 1});
		for (double d : net.getOutput()) {
			System.out.println("Result: " + d);
		}
	}
}
