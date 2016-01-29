package main;

import java.awt.Font;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.management.InstanceAlreadyExistsException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Vector2f;

import graphing.Diagram;
import neural.Gene;
import util.ImmutableVector2f;
import util.Util;

public class AntSimulation extends BasicGame {
	public boolean isRunning = true;

	public static void main(String[] args) {
		try {
			AntSimulation sim = new AntSimulation();
			AppGameContainer appgc = new AppGameContainer(sim, WIDTH+100, HEIGHT+100, false);
			appgc.setAlwaysRender(true);
			appgc.setShowFPS(false);
			appgc.setForceExit(false);
			appgc.start();
			sim.OnClose();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	public static Ant mutate (Ant ant, float chance) {
		if (ant == null)
			return null;
		
		ArrayList<Gene> newGenes = new ArrayList<Gene>();
		Random r = new Random();
		if (r.nextDouble() < chance) {
			// find nodes
			ArrayList<Integer> nodes = new ArrayList<Integer>();
			for (Gene l : ant.genes) {
				if (!nodes.contains(l.in))
					nodes.add(l.in);
				if (!nodes.contains(l.out))
					nodes.add(l.out);
			}
			
			if (r.nextDouble() < 0.5d || ant.genes.length == (int) (nodes.size()*(nodes.size()-1)/2) ) { // add connection
				// map all connections
				HashMap<Integer, ArrayList<Integer>> conn = new HashMap<Integer, ArrayList<Integer>>();
				for (Gene l : ant.genes) {
					int i1 = Math.min(l.in, l.out);
					int i2 = Math.max(l.in, l.out);
					if (conn.keySet().contains(i1)) {
						conn.get(i1).add(i2);
					} else {
						ArrayList<Integer> al = new ArrayList<Integer>();
						al.add(i2);
						conn.put(i1, al);
					}
				}
				
				// find all unconnected nodes
				HashMap<Integer, ArrayList<Integer>> notConn = new HashMap<Integer, ArrayList<Integer>>();
				for (Integer key : conn.keySet()) {
					ArrayList<Integer> nc = new ArrayList<Integer>();
					for (int n : nodes) {
						if (!conn.get(key).contains(n) && n!=key) {
							nc.add(key);
						}
					}
					if (nc.size() > 0)
						notConn.put(key, nc);
				}
				
				// choose a random out node, select a random target node
				Object[] outNodes = notConn.keySet().toArray();
				if (outNodes.length > 0) {
					int out = (int) outNodes[r.nextInt(outNodes.length)];
					int targ = (int) notConn.get(out).toArray()[r.nextInt(notConn.get(out).size())];
					
					Gene g2 = new Gene(out, targ, r.nextDouble()/2f - 0.25, true);
					newGenes.add(g2);
				}
			} else { // add node
				int newNode = 0;
				while (nodes.contains(newNode))
					newNode++;
				
				int randInd = r.nextInt(ant.genes.length);
				Gene g = ant.genes[randInd];
				Gene n1 = new Gene(g.in, newNode, g.weight, g.active);
				Gene n2 = new Gene(newNode, g.out, g.weight, g.active);
				newGenes.add(n1);
				newGenes.add(n2);
				ant.genes[randInd].active = false;
			}
		}
		
		for (Gene g : ant.genes) {
			if (g.active) {
				if (r.nextDouble() < MUTATION_CHANCE) {
					g.weight += (r.nextDouble() - 0.5d) * chance * chance;
				}
				newGenes.add(g);
			}
		}
		
		float newSpeed = (float) (ant.speed + (r.nextDouble() - 0.5d) * chance * chance);
		Gene[] genes = new Gene[newGenes.size()];
		for (int i = 0; i < genes.length; i++) {
			genes[i] = newGenes.get(i);
		}
		
		return new Ant(genes, newSpeed, ant.position.makeVector2f());
	}
	
	public void OnClose() {
		try {
			Diagram.setup("Gen", "Food", 0, 0, gen+2, 40000, "Ants");
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		}
		double[] xAxis = new double[passedGenerations.size()];
		for (int i = 0; i < xAxis.length; i++) {
			xAxis[i] = passedGenerations.get(i);
		}
		double[] yMax = new double[maxFoodPerGen.size()];
		for (int i = 0; i < xAxis.length; i++) {
			yMax[i] = maxFoodPerGen.get(i);
		}
		double[] yMin = new double[minFoodPerGen.size()];
		for (int i = 0; i < xAxis.length; i++) {
			yMin[i] = minFoodPerGen.get(i);
		}
		Diagram.addData(xAxis, yMax, Color.blue, "Max Food", 0);
		Diagram.addData(xAxis, yMin, Color.red, "Min Food", 1);
	}
	
	private static final int popSize = 20;
	private static final int foodAmount = 200;
	private static final int INIT_MUTATIONS = 2;
	private static final float MUTATION_CHANCE = 0.0f;
	private static final float SIM_SPEED = 10f;
	private static final float GEN_LENGTH = 20f;
	private static final Color BCKG_COLOR = Color.gray;
	private static final Color TEXT_COLOR = Color.white;
	private static final Color GOOD_ANT_COLOR = Color.blue;
	private static final Color BAD_ANT_COLOR = Color.red;
	private static final Color FOOD_COLOR = Color.green;
	
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	private ArrayList<ImmutableVector2f> food = new ArrayList<ImmutableVector2f>();
	private Random pseudo = new Random();
	private int maxFood = 0;
	private int minFood = 0;
	private ArrayList<Integer> passedGenerations = new ArrayList<Integer>();
	private ArrayList<Integer> maxFoodPerGen = new ArrayList<Integer>();
	private ArrayList<Integer> minFoodPerGen = new ArrayList<Integer>();
	private float timeUntilNewGen = GEN_LENGTH;
	private TrueTypeFont ttf;
	private int gen = 1;
	
	public AntSimulation(String title) {
		super(title);
	}
	public AntSimulation() {
		this("Ant Simulation");
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		ttf = new TrueTypeFont(new Font("Courier New", Font.BOLD, 20), true);
		gc.getInput().addKeyListener(new InputListener(this));
		gc.getInput().addMouseListener(new InputListener(this));
		
		// spawn ants randomly
		for (int i = 0; i < popSize; i++) {
			Gene[] g = new Gene[]{
					new Gene(0,4,0.5d,true),
					new Gene(1,5,0.5d,true),
					new Gene(2,4,0.5d,true),
					new Gene(3,5,0.5d,true),
					new Gene(1,6,0.5d,true),
					new Gene(6,4,0.5d,true),
					new Gene(2,6,0.5d,true),
					new Gene(6,5,0.5d,true)
				};
			Vector2f pos = new Vector2f(
					(float) pseudo.nextDouble() * WIDTH,
					(float) pseudo.nextDouble() * HEIGHT
			);
			Ant a = new Ant (g, 50, pos);
			for (int m = 0; m < INIT_MUTATIONS; m++) {
				a = mutate(a, 1f);
			}
			ants.add(a);
		}
		
		// spawn food randomly
		for (int i = 0; i < foodAmount; i++) {
			ImmutableVector2f pos = new ImmutableVector2f(
					(float) pseudo.nextDouble() * WIDTH,
					(float) pseudo.nextDouble() * HEIGHT
			);
			food.add(pos);
		}
	}
	
	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setColor(BCKG_COLOR);
		g.fillRect(0, 0, WIDTH+100, HEIGHT+100);
		g.setAntiAlias(true);
		
		// draw food
		for (ImmutableVector2f p : food) {
			float x = p.getScreenX();
			float y = p.getScreenY();
			
			g.setColor(FOOD_COLOR);
			g.fillOval(x, y, 10, 10);
		}
		
		int infoY = 10;
		// draw ants
		for (Ant a : ants) {
			if (a == null)
				continue;
			float x = a.position.getScreenX();
			float y = a.position.getScreenY();
			
			ImmutableVector2f v = new ImmutableVector2f(a.headPosition);
			float hx = v.getScreenX();
			float hy = v.getScreenY();
			
			g.setColor(Util.colorLerp(BAD_ANT_COLOR, GOOD_ANT_COLOR, (float) a.foodCollected / maxFood));
			g.fillOval(x, y, Ant.bodyRadius*2, Ant.bodyRadius*2);
			g.fillOval(hx, hy, Ant.headRadius*2, Ant.headRadius*2);
		}
		
		// draw FPS
		g.setColor(TEXT_COLOR);
		g.setFont(ttf);
		g.drawString("FPS: " + gc.getFPS(), 10, infoY);
		
		// draw generation
		g.drawString("GENERATION: " + gen, 150, infoY);
		
		// draw generation progress bar
		g.setAntiAlias(false);
		float perc = (GEN_LENGTH - timeUntilNewGen) / GEN_LENGTH;
		int startX = 350;
		int width = 500;
		int height = 20;
		int border = 5;
		g.setColor(Color.darkGray);
		g.fillRect(startX - border, infoY - border, width + 2*border, height + 2*border);
		g.setColor(Color.lightGray);
		g.fillRect(startX, infoY, width, height);
		g.setColor(Color.green);
		g.fillRect(startX, infoY, perc * width, height);
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		if (!isRunning)
			return;
			
		if (timeUntilNewGen <= 0) {
			nextGeneration();
			timeUntilNewGen = GEN_LENGTH;
		}
		timeUntilNewGen -= delta*SIM_SPEED/1000f;
		
		maxFood = Integer.MIN_VALUE;
		minFood = Integer.MAX_VALUE;
		for (int i = 0; i < ants.size(); i++) {
			Ant a = ants.get(i);
			if (a == null) {
				ants.remove(i);
				continue;
			}
			if (a.foodCollected > maxFood)
				maxFood = a.foodCollected;
			else if (a.foodCollected < minFood)
				minFood = a.foodCollected;
			
			ImmutableVector2f closestFood = null;
			float closestFoodDistSqr = Float.MAX_VALUE;
			ImmutableVector2f closestAnt = null;
			float closestAntDistSqr = Float.MAX_VALUE;
			
			for (int j = 0; j < ants.size(); j++) {
				Ant a2 = ants.get(j);
				if (i != j) {
					ImmutableVector2f toAnt = a2.position.sub(a.position);
					if (toAnt.length() < Ant.bodyRadius*2) {
						a.position = a.position.sub(Ant.bodyRadius*2 - toAnt.length());
					}
					if (toAnt.lengthSquared() < closestAntDistSqr) {
						closestAnt = toAnt;
						closestAntDistSqr = toAnt.lengthSquared();
					}
				}
			}
			
			for (int k = 0; k < food.size(); k++) {
				Vector2f toBody = food.get(k).makeVector2f();
				toBody.sub(a.position.makeVector2f());
				Vector2f toHead = food.get(k).makeVector2f();
				toHead.sub(a.headPosition.makeVector2f());
				
				if (toBody.length() < Ant.bodyRadius || toHead.length() < Ant.headRadius) {
					a.pickupFood(1000);
					ImmutableVector2f pos = new ImmutableVector2f(
							(float) pseudo.nextDouble() * WIDTH,
							(float) pseudo.nextDouble() * HEIGHT
					);
					food.set(k, pos);
					
					if (food.size() == 0)
						break;
				} else if (toBody.lengthSquared() < closestFoodDistSqr) {
					closestFood = new ImmutableVector2f(toBody);
					closestFoodDistSqr = toBody.lengthSquared();
				}
			}
			
			a.tick(closestFood.makeVector2f().normalise(), closestAnt.makeVector2f().normalise(),
					SIM_SPEED * delta/1000f);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void nextGeneration () {
		System.out.println("Generation " + gen + " ended. MaxFood="+maxFood + " minFood=" + minFood);
		passedGenerations.add(gen);
		maxFoodPerGen.add(maxFood);
		minFoodPerGen.add(minFood);
		
		// sort ants by foodCollected
		ants.sort(null);
		Object[] sa = ants.toArray();
		Ant[] sorted = new Ant[sa.length];
		for (int i = 0; i < sa.length; i++) {
			sorted[i] = (Ant) sa[i];
		}
		// count unfit ants
		int popForDel = 0;
		int reqFood = 2000;
		for (Ant a : ants) {
			if (a.foodCollected < reqFood || a.foodCollected < maxFood - 3000)
				popForDel++;
		}
		// add better-than-average ants to list
		ArrayList<Ant> newAnts = new ArrayList<Ant>();
		for (int i = 0; i < sorted.length-popForDel; i++) {
			newAnts.add(sorted[sorted.length-1-i]);
		}
		// make a list for reproducing ants, best ant has more entries, such that one ant can replace the entire population
		ArrayList<Ant> rep = new ArrayList<Ant>();
		for (int i = 0; i < newAnts.size(); i++) {
			for (int m = 0; m < popForDel-i; m++) {
				rep.add(newAnts.get(i));
			}
		}
		// shuffle list
		for (int i = 0; i < rep.size(); i++) {
			Random r = new Random();
			int j = i + r.nextInt(rep.size() - i);
			Ant jA = rep.get(j);
			rep.set(j, rep.get(i));
			rep.set(i, jA);
		}
		// make list to dequeue
		ArrayDeque<Ant> reprod = new ArrayDeque<Ant>();
		for (Ant a : rep) {
			reprod.add(a);
		}
		// add more slightly mutated ants until desired population size is reached
		while (newAnts.size() < popSize) {
			newAnts.add( mutate(reprod.poll(), MUTATION_CHANCE) );
		}
		// overwrite existing ants
		ants = (ArrayList<Ant>) newAnts.clone();
		for (Ant a : ants) {
			if (a == null)
				continue;
			Vector2f pos = new Vector2f(
					(float) pseudo.nextDouble() * WIDTH,
					(float) pseudo.nextDouble() * HEIGHT
			);
			a.position = new ImmutableVector2f(pos);
			a.foodCollected = 0;
		}
		
		gen++;
	}
}
