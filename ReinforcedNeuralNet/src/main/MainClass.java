package main;

import java.util.Random;
import java.util.Scanner;

import org.newdawn.slick.Color;

import graphing.Diagram;
import neural.NeuralNet;
import neural.Neuron;

public class MainClass {
	public static final int SAMPLE_SIZE = 2000000;
	public static final float EPSILON = 0.005F;
	public static final double ALPHA = 0.5d; // 0 = slow, .5 = moderate
	public static final double ETA = 0.15d; // 0 = slow, .2 = medium, 1 = fast
	public static final int TEST_SIZE = 20;
	public static final int MIN_PASSES = 100;
	public static final int DRAW_INTERVAL = 10;
	private static Random r = new Random();
	private static float delta;
	
	public static void main(String args[]) {
		Neuron.setConstants(ALPHA, ETA);
		int[] topology = new int[]{1,10,10,10,10,1};
		NeuralNet myNet = new NeuralNet(topology);
		
		drawTarget();
		drawCurrent(myNet);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("\n\n\nWelcome, here you can view the neural net's evolution step by step.\n"
				+ "Press ENTER to advance one step\n"
				+ "Type CONTINUE to activate automated training");
		Scanner scan = new Scanner(System.in);
		while (true) {
			String in = scan.nextLine().replaceAll(" ", "").toUpperCase();
			
			if (in.equals("CONTINUE"))
				break;
			
			if (in.equals(""))
				train(myNet, 1, false, false, 1);
			
			if (in.equals("END")) {
				scan.close();
				System.exit(0);;
			}
		}
		
		train(myNet, SAMPLE_SIZE, true, true, DRAW_INTERVAL);
		
		System.out.println("\nHi again, as you could see, the neural net has approximated the function quite well.\n"
				+ "Type TEST to run an analysis\n"
				+ "Type TRAIN {num} to train the net num more times\n"
				+ "Type END to exit");
		while (true) {
			System.out.print("\nComma-seperated Input: ");
			String in = scan.nextLine().replaceAll(" ", "").toUpperCase();
			
			if (in.equals("END"))
				break;
			
			if (in.equals("TEST")) {
				test(myNet);
				continue;
			}
			
			if (in.startsWith("TRAIN")) {
				int it = Integer.parseInt(in.substring(5));
				train(myNet, it, false, true, DRAW_INTERVAL);
				continue;
			}
			
			String[] inp = in.split(",");
			
			double[] input = new double[inp.length];
			for (int i = 0; i < inp.length; i++) {
				input[i] = Double.parseDouble(inp[i]);
			}
			
			myNet.feedForward(input);
			
			double[] results = myNet.getResults();
			for (double res : results) {
				System.out.println("Result: " + res);
			}
		}
		scan.close();
		System.exit(0);
	}
	
	public static double getTarget(double[] input) {
		return Math.sinh(input[0]/2)/2.5;
	}
	public static double getRandInput() {
		return (r.nextDouble()-0.5d)*2*Math.PI;
	}
	/**
	 * @return {xMin, xMax, yMin, yMax}
	 */
	public static double[] getRanges() {
		return new double[]{-Math.PI, Math.PI, -1, 1};
	}
	
	private static void train(NeuralNet net, int sampleSize, boolean stopAccurate, boolean verbose, int drawInterval) {
		double[] totalError = new double[TEST_SIZE];
		int drawTimer = 0;
		long lastDraw = System.currentTimeMillis();
		
		for (int pass = 0; pass < sampleSize; pass++) {
			double x = getRandInput();
			double[] input = new double[]{x};
			double[] target = new double[]{getTarget(input)};
			
			if (verbose) {
				System.out.println("\nPass: " + pass);
				System.out.println("Input: {" + x + "}");
				System.out.println("Target: {" + target[0] + "}");
			}
			
			net.feedForward(input);
			if (verbose) {
				double[] results = net.getResults();
				for (double res : results) {
					System.out.println("Result: " + res);
				}
			}

			net.backProp(target);
			
			double totErr = Double.MAX_VALUE;
			if (verbose) {
				double err = net.getRecentAvgError();
				totalError = append(err, totalError);
				totErr = sum(totalError);
				System.out.println("Net recent average error: " + err);
				System.out.println("Total error: " + totErr);
			}
			
			if (totErr < EPSILON * TEST_SIZE && pass >= MIN_PASSES && stopAccurate) {
				System.out.println("\nReached satisfactory accuracy!\nNow ready to use:");
				return;
			}
		
			
			if (drawTimer <= 0) {
				drawCurrent(net);
				drawTimer = drawInterval;
				delta = System.currentTimeMillis() - lastDraw;
				lastDraw = System.currentTimeMillis();
				Diagram.speed = 1000/delta;
			}
			drawTimer--;
		}
	}
	
	private static void drawTarget () {
		double x = -Math.PI;
		double[] inputs = new double[(int) (Math.PI * 2 / 0.01d + 1)];
		double[] outputsTarget = new double[inputs.length];
		int it = 0;
		
		while (x < Math.PI) {
			double[] input = new double[]{x};
			inputs[it] = x;
			outputsTarget[it] = getTarget(input);
			it++;
			x += 0.01d;
		}

		double xRange = getRanges()[1]-getRanges()[0];
		double yRange = getRanges()[3]-getRanges()[2];
		Diagram.outputValues(inputs, outputsTarget, "x", "f(x)", getRanges()[0], getRanges()[2], xRange, yRange, Color.red, "Target");
	}
	
	private static void drawCurrent(NeuralNet net) {
		double x = getRanges()[0];
		double[] inputs = new double[(int) ((getRanges()[1]-getRanges()[0]) / 0.01d + 1)];
		double[] outputs = new double[inputs.length];
		int it = 0;
		
		while (x < getRanges()[1]) {
			double[] input = new double[]{x};
			net.feedForward(input);
			double res = net.getResults()[0];
			inputs[it] = x;
			outputs[it] = res;
			it++;
			x += 0.01d;
		}
		
		Diagram.addData(inputs, outputs, Color.blue, "Results", 0);
	}

	private static void test(NeuralNet net) {
		System.out.println("TESTING...");
		
		double x = -Math.PI;
		double totalErr = 0d;
		double[] inputs = new double[(int) (Math.PI * 2 / 0.01d + 1)];
		double[] outputs = new double[inputs.length];
		double[] outputsTarget = new double[inputs.length];
		int it = 0;
		
		while (x < Math.PI) {
			double[] input = new double[]{x};
			net.feedForward(input);
			double res = net.getResults()[0];
			double err = Math.abs(res-getTarget(input));
			System.out.println("Iteration " + it + " x=" + x + " res=" + res + " f(x)=" + Math.sin(x) + " err=" + err);
			inputs[it] = x;
			outputs[it] = res;
			outputsTarget[it] = getTarget(input);
			totalErr += err;
			it++;
			x += 0.01d;
		}
		
		System.out.println("DONE! Average Error percentage: " + totalErr/it*100 + "%");
		drawCurrent(net);
	}
	
	public static double[] append(double d, double[] arr) {
		for (int i = 0; i < arr.length - 1; i++) {
			arr[i] = arr[i+1];
		}
		arr[arr.length - 1] = d;
		return arr;
	}
	public static double sum(double[] arr) {
		double d = 0d;
		
		for (int i = 0; i < arr.length - 1; i++) {
			d += arr[i];
		}
		
		return d;
	}
}
