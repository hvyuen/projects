/* Thought Question 1
 * Creates a pattern that repeatedly creates another pattern (a glider) moving diagonally downwards to the right indefinitely. 
 * It's significant because it creates an infinitely reproducing pattern that can affect other patterns on the board, which 
 * disproves Conway's conjecture that no pattern can grow infinitely.
 * 
 * Thought Question 2
 * Each turn, we need to check the status of each cell on the board, representing each element in the 2D array, before changing
 * them, since each change is dependent on the status of the cells around that cell. This means each time we update the array,
 * we need to create another array storing the status of each cell to update the original cells simultaneously. This takes up
 * more memory than just the statuses of the original cells. 
 * 
 * Thought Question 3
 * Pros:
 * You could create a grid of Cell objects instead of FilledRect objects, and have a FilledRect instance variable inside the
 * Cell class that's set to white in the constructor method, which would have been easier to implement. 
 * 
 * Cons:
 * Each Cell in the array would need to create an instance of FilledRect and would have a lot of methods that might not be
 * used, which is more expensive. 
 * 
 * Thought Question 4
 * A cell would only be created out of bounds if there's at least 3 adjacent cells on the edge of the board.
 * Check if there's at least 3 cells adjacent to each other on the edge of board (return it as a boolean).
 * If true, double the size of the grid. 
 * 
*/
import java.awt.Color;
import java.awt.Container;
import javax.swing.JRootPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import objectdraw.*;
public class GameOfLife extends WindowController implements KeyListener {
	
	protected static final int WINDOW_SIZE = 616;
	protected static final int BOX_SIZE = 15;
	protected Cell lastToggledCell;
	
	protected Grid grid;
	
	public void begin() {
		int yoffset = 0;
		
		/* The coordinate system of the grid is thrown off slightly by
		 * the existance of the system menu bar.  The code below figures out
		 * the hight of the menu bar. The call to resize at the end of this
		 * method takes this offset into account when making the whole grid
		 * visible. 
		 */
		Container c = this;
		while(! (c instanceof JRootPane)) {
			yoffset += (int)(c.getParent().getY());
			c = c.getParent();
		}
		grid = new Grid(WINDOW_SIZE, BOX_SIZE, canvas);
        requestFocus();
        addKeyListener(this);
        canvas.addKeyListener(this);
        lastToggledCell = null;
        resize(WINDOW_SIZE, WINDOW_SIZE + yoffset);
	}
	
	public void onMousePress(Location point) {
		/* TODO: Toggle the cell that was clicked on
		 * and keep track of what cell you just 
		 * changed. 
		 */
	      this.lastToggledCell = this.grid.toggle(point);
	   }
	
	public void onMouseDrag(Location point) {
		/* TODO: Toggle the cell under the mouse if
		 * it wasn't the last cell to be toggled. 
		 */
	      Cell cell = this.grid.getCell(point);
	      
	      if (cell != null && !cell.equals(this.lastToggledCell)) {
	         this.lastToggledCell = this.grid.toggle(point);
	      }
	}
	
    // Required by KeyListener Interface.
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    
    public void keyTyped(KeyEvent e)
    {
    	char letter = e.getKeyChar();
    	if(letter == 'g' && lastToggledCell != null) {
    		grid.gliderGun(lastToggledCell.getRow(), lastToggledCell.getCol());
    	} else if (letter == 'c') {
    		/* TODO: Clear the grid */
    		this.grid.clear();
    	}
    	else {
    		/* TODO: Toggle whether the grid is running */
    		this.grid.toggleRunning();
    	}
    }
	
    public static void main(String[] args) { 
        new GameOfLife().startController(WINDOW_SIZE, WINDOW_SIZE); 
	}

}
