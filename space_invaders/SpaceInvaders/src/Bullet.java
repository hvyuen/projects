import java.awt.Color;
import objectdraw.*;

public class Bullet extends ActiveObject{
	protected FilledRect shot; 
	//Keeps track of whether the bullet was shot by the ship or an alien
	protected boolean origin;
	//Determines the direction of the bullet from the origin
	protected int direction;
	protected boolean alive = true;

	DrawingCanvas c; 
	protected Alien alien; 
	protected Ship ship; 

	public Bullet(double x, double y, boolean origin, int direction, Color color, Alien a, Ship s, DrawingCanvas canvas) {
		shot = new FilledRect(x, y, 4.0, 10.0, canvas); 
		this.origin = origin;
		this.direction = direction;
		shot.setColor(color);
		alien = a;
		ship = s;
		c = canvas;
		start(); 
	}
		
	public void run() {
		while(shot.getX() >= 0 && shot.getY() >= 0 && shot.getX() <= c.getWidth() && shot.getY() <= c.getHeight()) {
			shot.move(0.0, direction); 
			
			//If a bullet overlaps with the ship, clear the bullet
			if(ship.hit(this)) {
				shot.removeFromCanvas();
				alive = false; 
				break; 
				}
			
			//If a bullet overlaps with an alien, clear the bullet
			if (alien.hit(this)) {
				ship.bulletList.remove(this); 
				shot.removeFromCanvas();
				alive = false;
				break;
				}

			pause(10); 
			}

		if (alive) {
			shot.removeFromCanvas(); 
			}
		}
	}


