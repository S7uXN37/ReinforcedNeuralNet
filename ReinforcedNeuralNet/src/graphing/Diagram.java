package graphing;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

public class Diagram extends BasicGame
{
	AppGameContainer appgc;
	Line[] lines;
	ArrayList<Graph> channels;
	Text[] texts;
	private double xAxisOffset;
	private double yAxisOffset;
	private double xRange;
	private double yRange;
	public static boolean isOpen = false;
	
	public Diagram(String gamename, Line[] lines, Text[] texts, double xAxisOffset, double yAxisOffset, double xRange, double yRange)
	{
		super(gamename);
		this.lines = lines;
		this.texts = texts;
		this.xAxisOffset = xAxisOffset;
		this.yAxisOffset = yAxisOffset;
		this.xRange = xRange;
		this.yRange = yRange;
		channels = new ArrayList<Graph>();
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		
	}

	@Override
	public void update(GameContainer gc, int i) throws SlickException {}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		for(Line l:lines){
			g.setColor(l.c);
			g.drawLine(l.p1.getX(), l.p1.getY(), l.p2.getX(), l.p2.getY());
		}
		for(Text t:texts){
			g.setColor(t.c);
			g.drawString(t.text, t.p.getX(), t.p.getY());
		}
		for (Graph gr : channels) {
			if (gr == null)
				continue;
			
			for(Line l:gr.lines){
				g.setColor(l.c);
				g.drawLine(l.p1.getX(), l.p1.getY(), l.p2.getX(), l.p2.getY());
			}
			for(Text t:gr.texts){
				g.setColor(t.c);
				g.drawString(t.text, t.p.getX(), t.p.getY());
			}
		}
	}

	public void start()
	{
		try
		{
			appgc = new AppGameContainer(this);
			appgc.setDisplayMode(1200, 800, false);
			appgc.setShowFPS(false);
			appgc.setAlwaysRender(true);
			appgc.start();
		}
		catch (SlickException ex)
		{
			Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IllegalStateException ex)
		{
			// don't know how to avoid this one when closing
		}
	}
	
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_ESCAPE) {
			Log.info("Closing...");
			isOpen = false;
			appgc.destroy();
			System.exit(0);
		}
	}

	
	private static final Point ORIGIN = new Point(50,50);
	private static final double xAxisLength = 1200 - ORIGIN.x*2;
	private static final double yAxisLength = 800 - ORIGIN.y*2;
	private static final int ARROW_ADJUSTMENT = 10;
	private static final int X_AXIS_LABEL_OFFSET = 10;
	private static final int Y_AXIS_LABEL_OFFSET = 50;
	private static final Color DIAGRAM_COLOR = Color.white;
	private static final Color TEXT_COLOR = Color.gray;
	
	private static Diagram diagram;
	
	public static boolean setup(
			String xAxisName, String yAxisName,
			double xAxisOffset, double yAxisOffset, double xRange, double yRange, String title) throws InstanceAlreadyExistsException
	{
		Log.setVerbose(false);
		Line[] lines = new Line[6];
		
		// Set up diagram background and labels
		// axis:
		Line xAxis = new Line(ORIGIN, new Point(ORIGIN.x+xAxisLength, ORIGIN.y), Color.white);
		Line yAxis = new Line(ORIGIN, new Point(ORIGIN.x, ORIGIN.y+yAxisLength), Color.white);
		Line arrX1 = new Line(xAxis.p2, new Point(xAxis.p2.x - ARROW_ADJUSTMENT, xAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrX2 = new Line(xAxis.p2, new Point(xAxis.p2.x - ARROW_ADJUSTMENT, xAxis.p2.y + ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrY1 = new Line(yAxis.p2, new Point(yAxis.p2.x - ARROW_ADJUSTMENT, yAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		Line arrY2 = new Line(yAxis.p2, new Point(yAxis.p2.x + ARROW_ADJUSTMENT, yAxis.p2.y - ARROW_ADJUSTMENT), DIAGRAM_COLOR);
		
		lines[0] = xAxis;
		lines[1] = yAxis;
		lines[2] = arrX1;
		lines[3] = arrX2;
		lines[4] = arrY1;
		lines[5] = arrY2;
		
		// axis labels
		Text[] t = new Text[8];
		t[0] = new Text( new Point( ORIGIN.x-30, yAxis.p2.y +20 ) , yAxisName, TEXT_COLOR);
		t[1] = new Text( new Point( xAxis.p2.x, ORIGIN.y+20) , xAxisName, TEXT_COLOR);
		// axis reference values X:
		t[2] = new Text( new Point( (ORIGIN.x+xAxis.p2.x)/2, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)((xRange/2+xAxisOffset)*100) /100f, TEXT_COLOR);
		t[3] = new Text( new Point( ORIGIN.x, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)(xAxisOffset*100) /100f, TEXT_COLOR);
		t[4] = new Text( new Point( xAxis.p2.x, ORIGIN.y-X_AXIS_LABEL_OFFSET) , ""+(int)((xAxisOffset+xRange)*100) /100f, TEXT_COLOR);
		// axis reference values Y:
		t[5] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, (ORIGIN.y+yAxis.p2.y)/2) , ""+(int)((yRange/2+yAxisOffset)*100) /100f, TEXT_COLOR);
		t[6] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, ORIGIN.y) , ""+(int)(yAxisOffset*100) /100f, TEXT_COLOR);
		t[7] = new Text( new Point( ORIGIN.x-Y_AXIS_LABEL_OFFSET, yAxis.p2.y) , ""+(int)((yAxisOffset+yRange)*100) /100f, TEXT_COLOR);
		
		// create instance and start
		if(diagram!=null){
			throw new InstanceAlreadyExistsException();
		}
		diagram = new Diagram(title, lines, t, xAxisOffset, yAxisOffset, xRange, yRange);
		Thread renderLoop = new Thread(new Runnable(){
			@Override
			public void run() {
				diagram.start();
			}
		}, "RenderThread");
		renderLoop.start();
		isOpen = true;
		return true;
	}
	
	public static void addData(double[] xAxisValues, double[] yAxisValues, Color color, String graphName, int channel) {
		Line[] newLines = createLinesFromData(xAxisValues, yAxisValues, diagram.xAxisOffset, diagram.yAxisOffset, diagram.xRange, diagram.yRange, color);
		
		Text[] newTexts = new Text[1];
		for(int i=0;i<newTexts.length ;i++) {
			newTexts[i] = new Text( new Point(3*(ORIGIN.x + diagram.lines[diagram.lines.length-1].p2.x)/4, ORIGIN.y-2*X_AXIS_LABEL_OFFSET), graphName, color);
		}
		
		Graph g = new Graph(newLines, newTexts);
		
		while (diagram.channels.size() <= channel)
			diagram.channels.add(null);
		
		diagram.channels.set(channel, g);
	}

	/**
	 * @param inputs array of input values
	 * @param outputs array of corresponding outputs
	 * @param xAxisOffset offset of the whole x-axis (for graphs not starting at 0)
	 * @param yAxisOffset offset of the whole y-axis (for graphs not starting at 0)
	 * @param color color of the graph
	 * @return
	 */
	private static Line[] createLinesFromData(double[] inputs,
			double[] outputs, double xAxisOffset, double yAxisOffset,
			double xRange, double yRange, Color color) {
		Point[] vals = new Point[inputs.length];
		
		for(int i=0;i<vals.length ;i++){
			vals[i] = new Point( ORIGIN.x + (xAxisLength/xRange)*(inputs[i]-xAxisOffset),
					ORIGIN.y + (yAxisLength/yRange)*(outputs[i]-yAxisOffset));
		}
		
		Line[] connectingLines = new Line[vals.length-1];
		for(int i=0;i<connectingLines.length ;i++) {
			connectingLines[i] = new Line(vals[i],vals[i+1], color);
		}
		
		return connectingLines;
	}
	
//	private static double arrayToRange (double[] arr) {
//		double smX = arr[0];
//		double laX = arr[0];
//		for (double d : arr) {
//			if (d < smX)
//				smX = d;
//			if (d > laX)
//				laX = d;
//		}
//		return laX - smX;
//	}
}

class Graph {
	Line[] lines;
	Text[] texts;
	
	public Graph (Line[] l, Text[] t) {
		lines = l;
		texts = t;
	}
}