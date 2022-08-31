import java.awt.Image;
import java.awt.Color;
import java.util.Random;
import objectdraw.*;

public class Alien extends ActiveObject {
	protected static final int ALIEN_LENGTH = 40;
	protected static final int LEFT = -60;
	protected static final int RIGHT = 60;
	
	//The direction the aliens are moving
	protected boolean right = true;
	//How many aliens the player has killed
	protected int killCounter = 0;
	//Keeps track of how many aliens are left in each column
	protected int[] alienCounter = {5,5,5,5,5,5,5,5,5};
	
	protected VisibleImage[][] alien = new VisibleImage[6][9];
	protected SpaceInvaders controller;
	protected Ship ship; 
	protected DrawingCanvas canvas; 
	protected boolean running; 
	protected Random r = new Random();

	public Alien(Image[] aliens, Ship ship, SpaceInvaders controller, DrawingCanvas canvas) {
		for (int i = 0; i < alien.length; i++) {
			for (int j = 0; j < alien[0].length; j++) {
				alien[i][j] = new VisibleImage(aliens[i], j * 60, i * 60, ALIEN_LENGTH, ALIEN_LENGTH, canvas);
				}
			}
		this.ship = ship; 
		this.controller = controller;
		this.canvas = canvas; 
		running = true; 
		start();
	}

	public void move() {
		for (int i = 0; i < alien.length; i++) {
			for (int j = 0; j < alien[0].length; j++) {
				if(right) alien[i][j].move(60, 0);
				else alien[i][j].move(-60, 0);
			}
		}
	}

	public void down() {
		for (int i = 0; i < alien.length; i++) {
			for (int j = 0; j < alien[0].length; j++) {
				alien[i][j].move(0, 60);
			}
		}
		
	}

	public void shoot() {
		//Selects a random column
		int i = r.nextInt(9); 
		while(alienCounter[i] <= 0) {
			i = r.nextInt(9);
			}

		VisibleImage shooter = alien[alienCounter[i]][i]; 
		new Bullet(shooter.getX() + ALIEN_LENGTH/2, shooter.getY() + ALIEN_LENGTH, false, 3, Color.YELLOW, this, ship, canvas);
		}
	
	//Checks if an alien has been hit by a bullet
	public boolean hit(Bullet bullet) {
		for (int i = 0; i < alien.length; i++) {
			for (int j = 0; j < alien[0].length; j++) {
				if (bullet.origin && alien[i][j].overlaps(bullet.shot)) {
					try {
						alien[i][j].removeFromCanvas();
						alienCounter[j]--;
						killCounter++;
						return true;
					}
					catch (IllegalStateException e) {
						return true;
					}
					
				}
			}
		}
		return false;
	}
	
	//The y-coordinate of the lowest alien
	public double getY() {
		int temp = 0;
		double max = 0;
		for (int i = 0; i < alienCounter.length; i++) {
			if (alienCounter[i] > temp) temp = alienCounter[i];
			max = alien[temp][i].getY() + ALIEN_LENGTH;
			}
		return max;
		}
	
	//Checks if there are any aliens remaining. If not, the game ends and the player wins
	public boolean win() {
		for(int i = 0; i < alien[0].length; ++i) {
				if(alienCounter[i] >= 0) {
					return false; 
					}
			}
		return true;
		}
	
	//Returns the score
	public int getScore() {
		return killCounter * 10; 
		}
	
	//Clears the aliens
	public void clear() {
		for (int i = 0; i < alien.length; i++) {
			for (int j = 0; j < alien[0].length; j++) {
				try {
					alien[i][j].removeFromCanvas();
				}
				catch(IllegalStateException e) {
					
				}
			}
		}
		running = false; 
	}
	
	public void run() {
		//Move counter
		int move = 0;
		//Decreases over time to speed up the aliens' movement
		int pace = 1000;
		pause(1000); 

		while(running) {
			//When the move counter is less than 4, the aliens keep moving in the designated direction
			if(move < 4) {
				move();
				move++;
			} else {
				//When the move counter reaches 4, the aliens move down, swap direction and become faster
				down();
				move = 0;
				right = !right;
				pace -= 100;
				//If the aliens reach the same level as the player, the player loses and the game ends
				if (this.getY() >= this.ship.getY()) {
					clear();
					controller.gameOver(false);
					break;
				}
			}	
			pause(pace);
			shoot();
			if(win()) {
				controller.gameOver(true); 
			}
		}
	}
}