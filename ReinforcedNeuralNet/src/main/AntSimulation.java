package main;

import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import neural.Gene;
import neural.NeuralNet;
import util.ImmutableVector2f;

public class AntSimulation extends BasicGame{
	public static void main(String[] args) {
		try {
			AntSimulation sim = new AntSimulation();
			AppGameContainer appgc = new AppGameContainer(sim, WIDTH, HEIGHT, false);
			appgc.setAlwaysRender(true);
			appgc.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	public static Ant mutate (Ant ant) {
		return new Ant(ant.net, ant.speed, ant.position.makeVector2f(), ant.genes);
	}
	
	private static final int popSize = 2;
	private static final int foodAmount = 20;
	private static final Color BCKG_COLOR = Color.gray;
	private static final Color ANT_COLOR = Color.blue;
	private static final Color EGG_COLOR = Color.yellow;
	private static final Color FOOD_COLOR = Color.green;
	
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800;
	
	private ArrayList<Ant> ants = new ArrayList<Ant>();
	private ArrayList<ImmutableVector2f> food = new ArrayList<ImmutableVector2f>();
	private Random pseudo = new Random();
	
	public AntSimulation(String title) {
		super(title);
	}
	public AntSimulation() {
		this("Ant Simulation");
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setColor(BCKG_COLOR);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setAntiAlias(true);
		
		for (ImmutableVector2f p : food) {
			float x = p.getX();
			float y = p.getY();
			
			g.setColor(FOOD_COLOR);
			g.fillOval(x, y, 10, 10);
		}
		
		for (Ant a : ants) {
			float x = a.position.getX();
			float y = a.position.getY();
			
			if (a.isMature()) {
				float dx = a.velocity.getX() * 20;
				float dy = a.velocity.getY() * 20;
				
				g.setColor(ANT_COLOR);
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
					new Gene(0,2,0.5d,true,1),
					new Gene(1,3,0.5d,true,1),
					new Gene(1,2,0.5d,true,1),
					new Gene(1,4,0.5d,true,1),
					new Gene(4,2,0.5d,true,1),
					new Gene(0,3,0.5d,true,1)
				};
			NeuralNet net = new NeuralNet(2, 2, g);
			Vector2f pos = new Vector2f(
					(float) pseudo.nextDouble() * WIDTH,
					(float) pseudo.nextDouble() * HEIGHT
			);
			ants.add(new Ant(net, 50, pos, g));
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
		for (int m = 0; m < ants.size(); m++) {
			Ant a = ants.get(m);
			a.tick(new Vector2f(0, 1), delta/1000f);
			
			if (!a.isAlive) {
				ants.remove(a);
				
				if (ants.size() == 0)
					break;
			}
			
			if (a.layEgg) {
				a.layEgg = false;
				ants.add(mutate(a));
			}
			
			for (int i = 0; i < food.size(); i++) {
				Vector2f f = food.get(i).makeVector2f();
				f.sub(a.position.makeVector2f());
				
				if (f.length() < 10) {
					a.pickupFood(1);
					ImmutableVector2f pos = new ImmutableVector2f(
							(float) pseudo.nextDouble() * WIDTH,
							(float) pseudo.nextDouble() * HEIGHT
					);
					food.set(i, pos);
					
					if (food.size() == 0)
						break;
				}
			}
		}
	}
}
