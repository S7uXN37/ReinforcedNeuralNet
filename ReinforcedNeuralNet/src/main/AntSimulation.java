package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import neural.Gene;
import util.ImmutableVector2f;
import util.Util;

public class AntSimulation extends BasicGame{
	public static void main(String[] args) {
		try {
			AntSimulation sim = new AntSimulation();
			AppGameContainer appgc = new AppGameContainer(sim, WIDTH+100, HEIGHT+100, false);
			appgc.setAlwaysRender(true);
			appgc.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	public static Ant mutate (Ant ant, float chance) {
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
	
	private static final int popSize = 50;
	private static final int foodAmount = 200;
	private static final int INIT_MUTATIONS = 5;
	private static final float MUTATION_CHANCE = 0.01f;
	private static final float SIM_SPEED = 2f;
	private static final Color BCKG_COLOR = Color.gray;
	private static final Color GOOD_ANT_COLOR = Color.blue;
	private static final Color BAD_ANT_COLOR = Color.red;
	private static final Color EGG_COLOR = Color.yellow;
	private static final Color FOOD_COLOR = Color.green;
	
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	private ArrayList<ImmutableVector2f> food = new ArrayList<ImmutableVector2f>();
	private Random pseudo = new Random();
	private float mostFoodPerSec = 0;
	
	public AntSimulation(String title) {
		super(title);
	}
	public AntSimulation() {
		this("Ant Simulation");
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setColor(BCKG_COLOR);
		g.fillRect(0, 0, WIDTH+100, HEIGHT+100);
		g.setAntiAlias(true);
		
		for (ImmutableVector2f p : food) {
			float x = p.getScreenX();
			float y = p.getScreenY();
			
			g.setColor(FOOD_COLOR);
			g.fillOval(x, y, 10, 10);
		}
		
		for (Ant a : ants) {
			float x = a.position.getScreenX();
			float y = a.position.getScreenY();
			
			if (a.isMature()) {
				ImmutableVector2f v = new ImmutableVector2f(a.velocity.normalise());
				float dx = v.getScreenX() * 20;
				float dy = v.getScreenY() * 20;
				
				g.setColor(Util.colorLerp(BAD_ANT_COLOR, GOOD_ANT_COLOR, (float) (a.foodCollected/a.aliveForSecs) / mostFoodPerSec));
				g.fillOval(x, y, 20, 20);
				g.fillOval(x+dx, y+dy, 10, 10);
			} else {
				g.setColor(EGG_COLOR);
				g.fillOval(x, y, 10, 10);
			}
		}
		
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		// spawn ants randomly
		for (int i = 0; i < popSize; i++) {
			Gene[] g = new Gene[]{
					new Gene(0,2,0.5d,true),
					new Gene(1,3,0.5d,true),
					new Gene(1,2,0.5d,true),
					new Gene(1,4,0.5d,true),
					new Gene(4,2,0.5d,true),
					new Gene(0,3,0.5d,true)
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
	public void update(GameContainer gc, int delta) throws SlickException {
		mostFoodPerSec = 0;
		for (int m = 0; m < ants.size(); m++) {
			Ant a = ants.get(m);
			
			if (!a.isAlive) {
				ants.remove(a);
				
				if (ants.size() == 0)
					break;
				
				continue;
			}
			
			if ((a.foodCollected/a.aliveForSecs) > mostFoodPerSec)
				mostFoodPerSec = (a.foodCollected/a.aliveForSecs);
			
			if (a.layEgg) {
				a.layEgg = false;
				ants.add(mutate(a, MUTATION_CHANCE));
			}
			
			ImmutableVector2f closest = null;
			float closestDistSqr = Float.MAX_VALUE;
			
			for (int i = 0; i < food.size(); i++) {
				Vector2f f = food.get(i).makeVector2f();
				f.sub(a.position.makeVector2f());
				
				if (f.length() < 10) {
					a.pickupFood(1000);
					ImmutableVector2f pos = new ImmutableVector2f(
							(float) pseudo.nextDouble() * WIDTH,
							(float) pseudo.nextDouble() * HEIGHT
					);
					food.set(i, pos);
					
					if (food.size() == 0)
						break;
				} else if (f.lengthSquared() < closestDistSqr) {
					closest = new ImmutableVector2f(f);
					closestDistSqr = f.lengthSquared();
				}
			}
			
			a.tick(closest.makeVector2f().normalise(), SIM_SPEED * delta/1000f);
		}
	}
}
