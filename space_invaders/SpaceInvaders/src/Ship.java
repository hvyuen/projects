import objectdraw.*;
import java.awt.Image;
import java.awt.Color;
import java.util.ArrayList; 


public class Ship extends ActiveObject {
	protected static final int SHIP_WIDTH = 50;
	protected static final int SHIP_HEIGHT = 50;
	protected static final int LEFT = -5;
	protected static final int RIGHT = 5;
	
	protected VisibleImage ship; 
	protected DrawingCanvas c; 
	protected Alien alien;
	private SpaceInvaders controller; 
	
	//keeps track of all the bullets from the ship on the screen
	protected ArrayList<Bullet> bulletList;
	//the ship's direction
	protected int direction; 
	
	public Ship(Image i, int WIDTH, int HEIGHT, Alien a, DrawingCanvas canvas, SpaceInvaders controller) {
		ship = new VisibleImage(i, canvas.getWidth() / 2 - SHIP_WIDTH / 2, canvas.getHeight() / 2 + 250, SHIP_WIDTH, SHIP_HEIGHT, canvas);
		bulletList = new ArrayList<Bullet>(); 
		this.direction = 0;
		
		this.alien = a;
		this.c = canvas;
		this.controller = controller;

		start();
		}
	
	public void left() {
		ship.move(LEFT,0); 
		}
	
	public void right() {
		ship.move(RIGHT,0);
		}
	
	public void direction(int i) {
		direction = i;
		}
	
	//Adds each shot from the ship to the ArrayList to keep track
	public void shoot() {
		Bullet shipShot = new Bullet(ship.getX() + SHIP_WIDTH/2, ship.getY(), true, -3, Color.WHITE, alien, this, c);
		bulletList.add(shipShot); 
		}
	
	//Checks if the ship has been hit by an enemy bullet
	public boolean hit(Bullet bullet) {
		if(!bullet.origin && ship.overlaps(bullet.shot)) {
			ship.removeFromCanvas();
			controller.gameOver(false);
			return true; 
			}
		return false;
		}
	
	//Returns the ship's Y-coordinate (used in the Alien class to determine a game over)
	public double getY() {
		return this.ship.getY();
		}
	
	//Clears the ship from the screen
	public void clear() {
		try {
			ship.removeFromCanvas(); 
			}
		catch(IllegalStateException e){
			
		}
	}
	
	public void run() {
		pause(500); 
		while(true) {
			if(direction < 0 && ship.getX() > 0) {
				left();
				}
			else if (direction > 0 && ship.getX() < this.c.getWidth() - SHIP_WIDTH) {
				right(); 
				}
			pause(15); 
			}	
		}
	}
