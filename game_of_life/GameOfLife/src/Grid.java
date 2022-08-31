import java.awt.Color;
import objectdraw.*;

public class Grid extends ActiveObject {

	protected FilledRect[][] grid;
	protected FramedRect[][] borders;
	protected int box_size;
	protected boolean running;
	protected static final int XOFFSET=8;
	protected static final int YOFFSET=8;
	
	public Grid(int window_size, int box_size, DrawingCanvas canvas) {
		this.box_size = box_size;
		grid = new FilledRect[(window_size-2*YOFFSET)/box_size][(window_size-2*XOFFSET)/box_size];
		borders = new FramedRect[(window_size-2*YOFFSET)/box_size][(window_size-2*XOFFSET)/box_size];
		for(int row=0; row<grid.length; row++) {
			for(int col=0; col<grid[0].length; col++) {
				grid[row][col] = new FilledRect(col*box_size+XOFFSET, row*box_size+YOFFSET, box_size, box_size, canvas);
				grid[row][col].setColor(Color.WHITE);
				borders[row][col] = new FramedRect(col*box_size+XOFFSET, row*box_size+YOFFSET, box_size, box_size, canvas);
			}
		}
		running = false;
    	this.start();
	}
	
	/* TODO: Update this method to return the cell in which the
	 * given point resides. 
	 */
	public Cell getCell(Location point) {
	      double x = point.getX();
	      double y = point.getY();
	      if (x >= 8.0 && y >= 8.0 && x <= (double)(this.box_size * this.grid[0].length + 8) && y <= (double)(this.box_size * this.grid.length + 8)) {
	         int row = (int)((point.getY() - 8.0) / (double)this.box_size);
	    	 int col = (int)((point.getX() - 8.0) / (double)this.box_size);
	         return new Cell(row, col);
	      } else {
	         return null;
	      }
	}
	
	/* TODO: Update this method to make a black cell white or a 
	 * white cell black.  Also return the cell that you toggled. 
	 */
	public Cell toggle(Location point) {
	      Cell cell = this.getCell(point);
	      this.toggle(cell.getRow(), cell.getCol());
	      return cell;
	   }
	
	/* TODO: Given a row and column in the grid, switch the
	 * color of the cell at that position. 
	 */
	public void toggle(int row, int col) {
	      if (row >= 0 && col >= 0 && row < this.grid.length && col < this.grid[0].length) {
	          if (this.grid[row][col].getColor() == Color.WHITE) {
	             this.grid[row][col].setColor(Color.BLACK);
	          } else {
	             this.grid[row][col].setColor(Color.WHITE);
	          }
	       }
	    }
	
	public void toggleRunning() {
		running = !running;
	}
	
	/* TODO: Return true if the cell at the given row and col is alive.
	 * NB: row and col may be values that are outside the grid. 
	 * Cells outside the grid are not alive. 
	 */
	protected boolean isAlive(int row, int col) {
	    if (row >= 0 && col >= 0 && row < this.grid.length && col < this.grid.length) {
	          return this.grid[row][col].getColor() == Color.BLACK;
	       } else {
	          return false;
	       }
	    }
	
	/* TODO: Return the number of alive cells that are adjacent
	 * to the given row and col. 
	 */
	protected int liveNeighbors(int row, int col) {
	      int neighbor = 0;

	      for(int cellsRow = -1; cellsRow < 2; cellsRow++) {
	         for(int cellsColumn = -1; cellsColumn < 2; cellsColumn++) {
	            if ((cellsRow != 0 || cellsColumn != 0) && this.isAlive(row + cellsRow, col + cellsColumn)) {
	               neighbor++;
	            }
	         }
	      }
	      return neighbor;
	   }
	
	
	/* TODO: Set all of the cells in the grid to WHITE/off/dead
	 */
	public void clear() {
	      for(int row = 0; row < this.grid.length; row++) {
	          for(int col = 0; col < this.grid[row].length; col++) {
	             this.grid[row][col].setColor(Color.WHITE);
	          }
	       }

	    }
	
	/* TODO: Set a given cell to BLACK/alive/on if it is within
	 * the grid. 
	 */
	private void on(int row, int col) {
	      if (row >= 0 && row < this.grid.length && col >= 0 && col < this.grid[0].length) {
	          this.grid[row][col].setColor(Color.BLACK);
	       }

	    }
	
	/* Mystery method.  Figure out when it gets used and why it's 
	 * interesting.  
	 */
	public void gliderGun(int row, int col) {
		on(row,col);
		on(row,col+1);
		on(row+1,col);
		on(row+1,col+1);
		on(row,col+10);
		on(row+1,col+10);
		on(row+2,col+10);
		on(row+3,col+11);
		on(row-1,col+11);
		on(row-2,col+12);
		on(row-2,col+13);
		on(row+4,col+12);
		on(row+4,col+13);
		on(row+1,col+14);
		on(row-1,col+15);
		on(row+3,col+15);
		on(row,col+16);
		on(row+1,col+16);
		on(row+2,col+16);
		on(row+1,col+17);
		on(row,col+20);
		on(row,col+21);
		on(row-1,col+20);
		on(row-1,col+21);
		on(row-2,col+20);
		on(row-2,col+21);
		on(row-3,col+22);
		on(row+1,col+22);
		on(row-3,col+24);
		on(row+1,col+24);
		on(row-4,col+24);
		on(row+2,col+24);
		on(row-1,col+34);
		on(row-2,col+34);
		on(row-1,col+35);
		on(row-2,col+35);

	}
	
	public void run() {
		while(true) {
			if(running) {
				// TODO:  Insert the logic to play the Game of Life
				boolean[][] runGrid = new boolean[this.grid.length][this.grid.length];
	            int row;
	            int col;
	            
	            for(row = 0; row < this.grid.length; row++) {
	               for(col = 0; col < this.grid[0].length; col++) {
	                  int neighbors = this.liveNeighbors(row, col);
	                  if (this.isAlive(row, col)) {
	                     if (neighbors < 2 || neighbors > 3) {
	                        runGrid[row][col] = true;
	                     }
	                  } else if (neighbors == 3) {
	                     runGrid[row][col] = true;
	                  }
	               }
	            }

	            for(row = 0; row < this.grid.length; row++) {
	               for(col = 0; col < this.grid[0].length; col++) {
	                  if (runGrid[row][col]) {
	                     this.toggle(row, col);
	                  }
	               }
	            }
			}
			pause(100);	
		}
	}
}
