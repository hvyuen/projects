import objectdraw.*;
import java.awt.*;
import java.awt.event.*;
/* Thought Questions
 * 
 * 1) Having a high score feature probably meant people would consistently try to beat the highest scores on the leaderboard,
 *    especially amongst friends (like the Flappy Bird craze a few years ago). Simple gameplay meant it was easy for anyone to
 *    pick up, but hard to master. 
 * 
 * 2) Make a Base class with a healthCount variable and the base as a VisibleImage. When a bullet overlaps with the base, 
 *    decrease healthCount. Use the coordinates of the overlap to determine where the damage on the base is to represent it 
 *    visually.
 * 
 * 3) Make a UFO class, where UFOs are instantiated randomly to move across the screen. When a bullet overlaps with the UFO, clear 
 *    it. If the UFO reaches the edge of the screen, clear all the bases. The clear bases method would be in the Base class.
 */


public class SpaceInvaders extends WindowController implements KeyListener {
	// Constants for the window
	private static final int HEIGHT= 800;
	private static final int WIDTH = 800;
	
	// remember whether a key is currently depressed
	protected boolean keyDown;
	
	protected Ship ship;
	protected Alien alien;
	protected boolean running;
	protected int score;
	
	protected FilledRect background;
	protected Text startText;
	protected Text restartText;

	public void begin() {
		this.background = new FilledRect(0, 0, WIDTH, HEIGHT, canvas);
		this.background.setColor(Color.WHITE);
		
		startText = new Text("Click to Start the Game.", WIDTH/2, HEIGHT/2, canvas);
		startText.setFontSize(36);
		startText.moveTo((canvas.getWidth() / 2) - startText.getWidth()/2, (canvas.getHeight() / 2) - startText.getHeight()/2);

		restartText = new Text("Click to play again.", WIDTH/4+120, HEIGHT/2 + 100, canvas);
		restartText.setFontSize(24);
		restartText.setColor(Color.RED);
		restartText.moveTo((canvas.getWidth() / 2) - restartText.getWidth()/2, (canvas.getHeight() * 2)/3);
		restartText.hide();
		
		running = false;
		requestFocus();
		addKeyListener(this);
		canvas.addKeyListener(this);
		}
	
	//ends the game
	public void gameOver(boolean win){
		running = false;
		ship.clear();
		alien.clear(); 
		
		if (win) {
			startText.setText("Congratulations, you won! " + "540 points.");
			startText.moveTo((canvas.getWidth() / 2) - startText.getWidth()/2, (canvas.getHeight() / 2) - startText.getHeight()/2);
			startText.setColor(Color.GREEN);
			startText.show();
			restartText.setColor(Color.GREEN);
			restartText.show();
			} else {
			startText.setText("Game over! Your score: " + alien.getScore() + "/540 points.");
			startText.moveTo((canvas.getWidth() / 2) - startText.getWidth()/2, (canvas.getHeight() / 2) - startText.getHeight()/2);
			startText.setColor(Color.RED);
			startText.show();
			restartText.setColor(Color.RED);
			restartText.show();
			}
			score = 0; 
	}
	

	public void onMouseClick(Location l) {
		if (!running) {
			running = true;	
			
			startText.hide();
			restartText.hide();
			background.setColor(Color.BLACK);
			
			//creates the alien enemies
			Image[] aliens = {getImage("invader1.png"), getImage("invader2.png"), getImage("invader3.png"), getImage("invader4.png"), getImage("invader5.png"), getImage("invader6.png")};
			alien = new Alien(aliens, ship, this, canvas);
			//creates the ship
			Image shipImage = getImage("ship.png");
			ship = new Ship(shipImage, WIDTH/2, HEIGHT/2, alien, canvas, this);
			
			alien.ship = ship;
			}
		}
	
	// Handle the arrow keys by telling the ship to go in the direction of the arrow.
	public void keyTyped(KeyEvent e){	
		if (running) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
				ship.shoot();
			}
			else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				ship.direction = -1;
				} 
			else if ( e.getKeyCode() == KeyEvent.VK_RIGHT) {
				ship.direction = 1; 
				}
			}
		}

	// Remember that the key is no longer down.
	public void keyReleased(KeyEvent e){
		if(running) {
			keyDown = false; 
			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
				ship.direction = 0;
				}
			}
		}
	// Handle the pressing of the key the same as typing.
	public void keyPressed(KeyEvent e){
		if(running) {
			if (!keyDown){
				keyTyped(e);
				}
			keyDown = true;
			}
		}
	
    public static void main(String[] args) { 
        new SpaceInvaders().startController(WIDTH, HEIGHT); 
        }
    
}