/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic_Block_Game;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LolloB
 */
public class input_handler {

    private Screen screen;
    private Thread input_handler;
    private BlockGrid_Holder[][] contentAreas;
    private int[][] grid;
    private Semaphore mutex;
    private int category;
    TextColor.Indexed color;
	public final static int CLOCKWISE = 0;
	public final static int ANTI_CLOCKWISE = 1;

    public input_handler(Screen screen, BlockGrid_Holder[][] contentAreas, int[][] grid, Semaphore mutex, TextColor.Indexed color) {
        this.screen = screen;
        this.contentAreas = contentAreas;
        this.grid = grid;
        this.mutex = mutex;
        this.category = 0;
        this.color = color;

        Start_Input_Handler_Thread();

    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setColor(Indexed color) {
        this.color = color;
    }

    public void StopThread() {
        this.input_handler.stop();
    }
    
    public void setValues(BlockGrid_Holder[][] contentAreas, int[][] grid,
			int value, Indexed indexed_color, int block[][]) {
		for (int i = 0; i < block.length; i++) {
			int row = block[i][0];
			int col = block[i][1];
			contentAreas[row][col].SetBlock(indexed_color);
			grid[row][col] = value;
		}

	}
    
    public boolean IsValid(int block[][]) {
		for (int i = 0; i < block.length; i++) {
			if (block[i][0] < 0 || block[i][0] >= Game.ROW || block[i][1] < 0
					|| block[i][1] >= Game.COL) {
				return false;
			}
		}
		return true;
	}
    
    private boolean validateMove(int[][] movement_block, int[][] blockChanges) {
		boolean validBlock = IsValid(movement_block);
		System.out.println("validBlock : " + validBlock);
		if (!validBlock) {
			return false;
		}
		
		for (int i = 0; i < blockChanges.length; i++) {
			int index = blockChanges[i][0];
			int row = movement_block[index][0];
			int col = movement_block[index][1];
			if (grid[row][col] != 0) {
				System.out.println("Cannot Move: " + row + ", " + col);
				return false;
			}
		}
		return true;
	}

	private void moveShape(int[][] block, int[][] movement_block,
			int[][] blockChanges) {
		for (int i = 0; i < movement_block.length; i++) {
			movement_block[i][0] = block[i][0];
			movement_block[i][1] = block[i][1];
			for (int j = 0; j < blockChanges.length; j++) {
				if (blockChanges[j][0] == i) {
					movement_block[i][0] = block[i][0] + blockChanges[j][1];
					movement_block[i][1] = block[i][1] + blockChanges[j][2];
				}
			}
		}
	}

    private void rotate(BlockGrid_Holder[][] contentAreas, int[][] grid, int movement) throws InterruptedException {

        //movement = 0 -> clockwise
        //movement = 1 -> anti-clockwise
        mutex.acquire();

        if (category == 0) {
            mutex.release();
            return;
        }

        int block[][] = new int[4][2];
		int movement_block[][] = new int[4][2];
		int blockChanges[][] = new int[4][3];
		int newCategory = 0;
		for (int i = 0; i < Game.ROW; i++) {
			for (int j = 0; j < Game.COL; j++) {
				if (grid[i][j] == 1) {
					for (int k = 0; k < block.length; k++) {
						if (block[k][0] == 0 && block[k][1] == 0) {
							block[k][0] = i;
							block[k][1] = j;
							break;
						}
					}
				}

			}
		}

		if (category == 1) {
			//Right T
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 2, -1, -1 } };
				newCategory = 4;
			} else {
				blockChanges = new int[][] { { 0, -1, 1 } };
				newCategory = 3;
			}
		}

		else if (category == 2) {
			//Left T
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 1, 1, 1 } };
				newCategory = 3;
			} else {
				blockChanges = new int[][] { { 3, 1, -1 } };
				newCategory = 4;
			}
		}

		else if (category == 3) {
			//Up T
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 1, -1 } };
				newCategory = 1;
			} else {
				blockChanges = new int[][] { { 3, -1, -1 } };
				newCategory = 2;
			}
		}

		else if (category == 4) {
			//reverse T
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 3, -1, 1 } };
				newCategory = 2;
			} else {
				blockChanges = new int[][] { { 0, 1, 1 } };
				newCategory = 1;
			}
		}

		else if (category == 5) {
			// Right L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 0, -1 }, { 1, 1, 1 },
						{ 3, 1, 0 } };
				newCategory = 6;
			} else {
				blockChanges = new int[][] { { 0, -1, -1 }, { 1, -2, 0 },
						{ 3, -1, -1 } };
				newCategory = 7;
			}
		}

		else if (category == 6) {
			// up L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 1, 1 }, { 2, -1, -1 },
						{ 3, 0, -2 } };
				newCategory = 8;
			} else {
				blockChanges = new int[][] { { 0, 2, -1 }, { 1, 0, 1 } };
				newCategory = 5;
			}
		}

		else if (category == 7) {
			// down L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 0, 2 }, { 1, 1, 1 },
						{ 3, -1, -1 } };
				newCategory = 5;
			} else {
				blockChanges = new int[][] { { 0, 1, 0 }, { 1, 1, 1 },
						{ 3, 0, -1 } };
				newCategory = 8;
			}
		}

		else if (category == 8) {
			// Left L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, -1, 1 }, { 2, 1, -1 },
						{ 3, -2, 0 } };
				newCategory = 7;
			} else {
				blockChanges = new int[][] { { 0, 1, 2 }, { 2, -1, -1 },
						{ 3, 0, 1 } };
				newCategory = 6;
			}
		}

		else if (category == 9) {
			// Reverse left L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 1, 0 }, { 2, -1, -1 },
						{ 3, 0, -1 } };
				newCategory = 10;
			} else {
				blockChanges = new int[][] { { 0, -1, 1 }, { 2, -1, 0 },
						{ 3, 0, -1 } };
				newCategory = 11;
			}
		}

		else if (category == 10) {
			// Reverse Down L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 2, 1 }, { 1, 0, -1 } };
				newCategory = 12;
			} else {
				blockChanges = new int[][] { { 0, 1, 1 }, { 2, -1, 0 },
						{ 3, 0, 1 } };
				newCategory = 9;
			}
		}

		else if (category == 11) {
			// Reverse up L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 2, -1, -1 }, { 3, -1, 1 } };
				newCategory = 9;
			} else {
				blockChanges = new int[][] { { 0, 0, -1 }, { 1, 1, 0 },
						{ 3, -1, -1 } };
				newCategory = 12;
			}
		}

		else if (category == 12) {
			// Reverse Right L
			
			if (movement == CLOCKWISE) {
				blockChanges = new int[][] { { 0, 0, 1 }, { 1, 1, 1 },
						{ 3, -1, 0 } };
				newCategory = 11;
			} else {
				blockChanges = new int[][] { { 0, 0, 1 }, { 1, 1, 0 },
						{ 3, 1, -1 } };
				newCategory = 10;
			}
		}

		else if (category == 13) {
			// I (vertical)
			
			blockChanges = new int[][] { { 0, 1, 1 }, { 2, -1, -1 },
					{ 3, -2, -2 } };
			newCategory = 14;
		}

		else if (category == 14) {
			// I (horizontal)
			
			blockChanges = new int[][] { { 0, 2, 2 }, { 1, 1, 1 },
					{ 3, -1, -1 } };
			newCategory = 13;
		}

		else if (category == 15) {
			// 4 (Horizontal)
			
			blockChanges = new int[][] { { 0, 0, 1 }, { 3, -2, 1 } };
			newCategory = 16;
		}

		else if (category == 16) {
			// 4 (Vertical)
			
			blockChanges = new int[][] { { 0, 0, -1 }, { 1, 2, -1 } };
			newCategory = 15;
		}

		else if (category == 17) {
			// 4 Reverse (Horizontal)
			
			blockChanges = new int[][] { { 0, 0, -1 }, { 3, -2, -1 } };
			newCategory = 18;
		}

		else if (category == 18) {
			// 4 Reverse (Vertical)
			
			blockChanges = new int[][] { { 0, 0, 2 }, { 1, 2, 0 } };
			newCategory = 17;
		}

		if (newCategory > 0) {
			try {
				moveShape(block, movement_block, blockChanges);
				boolean validGrid = validateMove(movement_block, blockChanges);
				System.out.println("validGrid: " + validGrid);
				if (validGrid) {
					setValues(contentAreas, grid, 0,
							TextColor.Indexed.fromRGB(255, 255, 255), block);
					setValues(contentAreas, grid, 1, color, movement_block);
					category = newCategory;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        mutex.release();

    }

    public void object_mover(BlockGrid_Holder[][] contentAreas, int[][] grid, int move) {

        int block[][] = new int[4][2];
		int movement_block[][] = new int[4][2];
		int blockChanges[][] = new int[4][3];
        boolean cant = false;

        try {
            mutex.acquire();

            if (category == 0) {
                mutex.release();
                return;
            }

            System.out.println("Mutex Acquired");
			
				
			// Direction -> VERICAL
			for (int i = 0; i < Game.ROW && !cant; i++) {
				for (int j = 0; j < Game.COL && !cant; j++) {
					if (grid[i][j] == 0 || grid[i][j] == 2)
						continue;

					if (move == 1 && j + move < Game.COL && grid[i][j] == 1
							&& grid[i][j + move] != 2) {

						for (int k = 0; k < block.length; k++) {
							if (block[k][0] == 0 && block[k][1] == 0) {
								block[k][0] = i;
								block[k][1] = j;
								break;
							}
						}
					}

					else if (move == -1 && j + move >= 0 && grid[i][j] == 1
							&& grid[i][j + move] != 2) {
						for (int k = 0; k < block.length; k++) {
							if (block[k][0] == 0 && block[k][1] == 0) {
								block[k][0] = i;
								block[k][1] = j;
								break;
							}
						}
					}

					else if (move == 2 && i < Game.ROW - 1 && grid[i][j] == 1
							&& grid[i + 1][j] != 2) {
						for (int k = 0; k < block.length; k++) {
							if (block[k][0] == 0 && block[k][1] == 0) {
								block[k][0] = i;
								block[k][1] = j;
								break;
							}
						}
					}

					else {

						cant = true;
					}
				}
			}

			if (!cant) {

				System.out.println("Values Changed");

				setValues(contentAreas, grid, 0,
						TextColor.Indexed.fromRGB(255, 255, 255), block);

				if (move == 2) {
					blockChanges = new int[][] { { 0, 1, 0 }, { 1, 1, 0 },
							{ 2, 1, 0 }, { 3, 1, 0 } };
					moveShape(block, movement_block, blockChanges);
					setValues(contentAreas, grid, 1, color, movement_block);

				} else {
					blockChanges = new int[][] { { 0, 0, move },
							{ 1, 0, move }, { 2, 0, move }, { 3, 0, move } };
					moveShape(block, movement_block, blockChanges);
					setValues(contentAreas, grid, 1, color, movement_block);
				}

			}

            System.out.println("Mutex Released");
            mutex.release();

        } catch (InterruptedException e) {

        }

    }

    public void Start_Input_Handler_Thread() {

        input_handler = new Thread(() -> {

            while (true) {
                KeyStroke keyStroke;
                try {
                    keyStroke = screen.pollInput();

                    if (keyStroke == null) {
                        keyStroke = screen.readInput();
                    }
                    if (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF) {
                        break;
                    }

                    if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
                    	// Direction -> VERTICAL
                    	object_mover(contentAreas, grid, -1);
                        System.out.println();
                    } else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
                    	// Direction -> VERTICAL
                    	object_mover(contentAreas, grid, 1);
                    } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
                        try {
                            rotate(contentAreas, grid, CLOCKWISE);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Generic_Block_Game.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else if (keyStroke.getKeyType() == KeyType.Enter) {
                        try {
                            rotate(contentAreas, grid, ANTI_CLOCKWISE);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Generic_Block_Game.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {

                        object_mover(contentAreas, grid, 2);

                    }
                    System.out.println("Read KeyStroke: " + keyStroke + "\n");

                } catch (IOException ex) {
                    Logger.getLogger(Generic_Block_Game.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            System.out.println("Input thread Ended");

        });

        input_handler.start();

    }

}
