
package Generic_Block_Game;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import javazoom.jl.decoder.JavaLayerException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {

    Indexed color = TextColor.ANSI.Indexed.fromRGB(1, 1, 1);    

    Semaphore mutex;
    int TO_WAIT = 800;
    public final static int ROW = 24;
    public final static int COL = 12;
    boolean break_it = false;
    int category = 0;
    input_handler input_thread;
    Sound sound;
    Panel mainPanel;
    Panel gamePanel;
    Panel buttonPanel;
    BasicWindow window;
    Thread main_thread;
    MultiWindowTextGUI textGUI;
    private Socket socket = null;
    private DataOutputStream out;
    public boolean GameOver = false;
    private int Trashlined_rows = 0;
    private int gameId;	
    private int enemyId;
    private int playersConnected;

    public void setEnemyId(int id) {
    	while (id > playersConnected-1){
    		id--;
			System.out.println("Player: " + id + " doesn't exist, i'm trying to select player " + (id-1) + " as enemy");
		}
    	enemyId = id;
    }
	
    public void setGameId(int id) {
		gameId=id;
    }
	    
    
    public void Reset(BlockGrid_Holder[][] contentAreas, int[][] grid) {

        for (int i = 0; i < ROW; i++) {

            for (int j = 0; j < COL; j++) {

                if (grid[i][j] != 2 && grid[i][j] != -1) {

                    grid[i][j] = 0;
                    contentAreas[i][j].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));

                }
            }

        }

    }

    public void SendingTrash(int lines) throws InterruptedException, IOException {
        mutex.acquire();
	    
        /*int column = 23 - Trashlined_rows;
        int k = 0;

        int starting_index = -1;
        int ending_index = -1;

        for (int i = 0; i < ROW; i++) {

            starting_index = ending_index = -1;

            for (k = 0; k < COL; k++) {

                if (grid[i][k] == 2) {
                    if (starting_index == -1) {
                        starting_index = k;
                    } else {
                        ending_index = k;
                    }
                }

            }

            if (starting_index == -1) {
                continue;
            }

            if (ending_index == -1) {
                ending_index = starting_index;
            }

            if (starting_index - number_of_lines < 0) {
                return false;
            }

            for (int j = starting_index - number_of_lines; j <= ending_index - number_of_lines; j++) {

                Indexed temp_color = contentAreas[i][j].getColor();
                contentAreas[i][j].SetBlock(contentAreas[i][starting_index].getColor());
                contentAreas[i][starting_index].SetBlock(temp_color);
                int temp = grid[i][j];
                grid[i][j] = grid[i][starting_index];
                grid[i][starting_index] = temp;

                starting_index++;
            }

        }*/
	  
		
	int trashLines;
			
			switch(lines) {
			case 2:
				trashLines = 1;
				break;
			case 3:
				trashLines = 2;
				break;
			case 4:
				trashLines = 4;
				break;
			default:
				trashLines = 0;
			
			}
	    boolean reachedTop = false;
		for (int i=0; i<grid.length;i++) {
			for(int j=0; j<grid[i].length;j++) {
				if (grid[i][j]==2) {
					Indexed index = contentAreas[i][j].getColor();
					contentAreas[i][j].SetBlock(TextColor.Indexed
						.fromRGB(255, 255, 255));
					grid[i][j] = 0;
					if(i+trashLines<=0) {
						reachedTop = true;
					} else {
						contentAreas[i-trashLines][j].SetBlock(index);
						grid[i-trashLines][j] = 2;
					}
				} else if (grid[i][j]==1){
					Indexed index = contentAreas[i][j].getColor();
					contentAreas[i][j].SetBlock(TextColor.Indexed
							.fromRGB(255, 255, 255));
					grid[i][j] = 0;
					if(i+trashLines<=0) {
						reachedTop = true;
					} else {
						contentAreas[i-trashLines][j].SetBlock(index);
						grid[i-trashLines][j] = 1;
					}
				}
			}
		}

            for (int i = 0; i < trashLines; i++) {

                for (int j = 0; j < COL; j++) {

                    contentAreas[ROW - 1 - Trashlined_rows][j].SetBlock(TextColor.Indexed.fromRGB(27, 30, 35));
                    grid[ROW - 1 - Trashlined_rows][j] = -1;

                }

                Trashlined_rows++;

	    }
        if(reachedTop) {
	    sendGameOver();
	}
        mutex.release();
	
    }

    public void SendingEnd(int number_of_lines) throws IOException {

        if (!this.socket.isOutputShutdown()) {
           String line = String.valueOf(number_of_lines) + String.valueOf(enemyId);
	   out.writeUTF(line);
        }

    }

    public boolean validate(int[][] grid, int row1, int column1, int row2, int column2, int row3, int column3, int row4, int column4) {

        if (grid[row1][column1] == 0
                && grid[row2][column2] == 0
                && grid[row3][column3] == 0
                && grid[row4][column4] == 0) {

            return true;
        }

        return false;

    }

    public void SetValues(BlockGrid_Holder[][] contentAreas, int[][] grid, int value, Indexed indexed_color, int row1, int column1, int row2, int column2, int row3, int column3, int row4, int column4) {

        contentAreas[row1][column1].SetBlock(indexed_color);
        grid[row1][column1] = value;

        contentAreas[row2][column2].SetBlock(indexed_color);
        grid[row2][column2] = value;

        contentAreas[row3][column3].SetBlock(indexed_color);
        grid[row3][column3] = value;

        contentAreas[row4][column4].SetBlock(indexed_color);
        grid[row4][column4] = value;

    }

    public void Clear_Matched_Line(BlockGrid_Holder[][] contentAreas, int[][] grid, ArrayList<Integer> columns, int column) throws InterruptedException, IOException {

        mutex.acquire();
        boolean instruction = false;
        
        	
		// Direction -> VERTICAL
		for (int i = 0; i < COL - 2; i++) {

			for (int j = 0; j < COL; j++) {

				for (int k = 0; k < columns.size(); k++)
					contentAreas[columns.get(k)][j].blink(!instruction);
			}

			Thread.sleep(100);

			instruction = !instruction;
		}

		for (int i = 0; i < COL; i++) {

			for (int k = 0; k < columns.size(); k++) {
				contentAreas[columns.get(k)][i].SetBlock(TextColor.Indexed
						.fromRGB(255, 255, 255));
				grid[columns.get(k)][i] = 0;
			}
			Thread.sleep(80);
		}

		column--;

		boolean inserted = false;

		for (int k = 0; k < COL; k++, column--)
			for (int i = 0; i < COL && column >= 0; i++) {

				if (grid[column][i] == 0) {
					// column--;
					continue;
				}

				inserted = false;

				for (int j = column; j < ROW - 1; j++) {

					if (grid[j + 1][i] == 0) {
						continue;
					} else {

						Indexed indexer = contentAreas[column][i]
								.getColor();

						grid[column][i] = 0;
						contentAreas[column][i].SetBlock(TextColor.Indexed
								.fromRGB(255, 255, 255));

						grid[j][i] = 2;
						contentAreas[j][i].SetBlock(indexer);
						inserted = true;
						Thread.sleep(80);
						break;
					}

				}

				if (!inserted) {

					Indexed indexer = contentAreas[column][i].getColor();
					grid[column][i] = 0;
					contentAreas[column][i].SetBlock(TextColor.Indexed
							.fromRGB(255, 255, 255));
					grid[ROW - 1][i] = 2;
					contentAreas[ROW - 1][i].SetBlock(indexer);
					Thread.sleep(80);
				}

			}

        SendingEnd(columns.size());
        mutex.release();

    }

    public void Matched_Lines(BlockGrid_Holder[][] contentAreas, int[][] grid) throws InterruptedException, IOException {

        boolean isMatched = true;

        ArrayList<Integer> indexes = new ArrayList<>();
        
        
	    // Direction -> Vertical	
		int max_row = 0;
		for (int i = ROW - 1; i >= 0; i--) {

			isMatched = true;

			for (int j = COL - 1; j >= 0; j--) {

				if (grid[i][j] != 2) {
					isMatched = false;
					break;
				}

			}

			if (isMatched) {

				if (max_row < i) {
					max_row = i;
				}
				indexes.add(i);
			}
		}
		
		if (indexes.size() > 0) {
			Clear_Matched_Line(contentAreas, grid, indexes, max_row);
		}

    }

    public boolean block_generator(BlockGrid_Holder[][] contentAreas, int[][] grid) {

        Random rand = new Random();
        int block_to_appear = rand.nextInt(5);
		
		// Direction -> VERTICAL
		

		if (block_to_appear == 0) {

			color = TextColor.Indexed.fromRGB(56, 56, 56);

			int orientation = rand.nextInt(4);
			int first, second, third, fourth;

			first = second = third = fourth = 0;

			int random_int = 0;

			if (orientation == 0) {

				category = 3;

				int min = 2, max = COL - 2;
				int row = 0;
				// Right side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row, first, row + 1, second, row + 2,
						third, row + 1, fourth + 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 2,
						third, row + 1, fourth + 1);

			}

			else if (orientation == 1) {

				category = 4;

				int min = 2, max = COL - 2;
				int row = 0;
				// Left side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row, first, row + 1, second, row + 2,
						third, row + 1, fourth - 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 2,
						third, row + 1, fourth - 1);

			}

			else if (orientation == 2) {

				category = 1;
				int min = 2, max = COL - 2;
				int row = 0;
				// Up T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row, first, row, second - 1, row,
						third + 1, row + 1, fourth)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second - 1, row,
						third + 1, row + 1, fourth);
			}

			else {
				
				category = 2;
				int min = 2, max = COL - 2;
				int row = 0;
				// down T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row + 1, first, row + 1, second - 1,
						row + 1, third + 1, row, fourth)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row + 1, first, row + 1, second - 1,
						row + 1, third + 1, row, fourth);

			}

		}

		else if (block_to_appear == 1) {

			color = TextColor.Indexed.fromRGB(0, 255, 255);
			// block 4 tabs
			category = 30;
			int first, second, third, fourth;

			first = second = third = fourth = 0;

			int random_int = 0;

			int min = 2, max = COL - 2;
			int row = 0;
			random_int = (int) Math.floor(Math.random() * (max - min + 1)
					+ min);
			first = random_int % COL;
			second = first;
			third = first;
			fourth = first;

			if (!validate(grid, row, first, row, second + 1, row + 1, third,
					row + 1, fourth + 1)) {
				return false;
			}

			SetValues(contentAreas, grid, 1, color, row, first, row, second + 1, row + 1, third,
					row + 1, fourth + 1);

		}

		else if (block_to_appear == 2) {

			color = TextColor.Indexed.fromRGB(139, 0, 0);

			// Straight line

			int orientation = rand.nextInt(2);

			int first, second, third, fourth;

			first = second = third = fourth = 0;

			int random_int = 0;

			if (orientation == 0) {

				int min = 2, max = COL - 2;
				int row = 0;
				category = 14;
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row, first, row + 1, second, row + 2,
						third, row + 3, fourth)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 2,
						third, row + 3, fourth);
			}

			else {

				int min = 3, max = COL - 3;
				int row = 0;
				category = 13;
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				if (!validate(grid, row, first, row, second + 1, row,
						third + 2, row, fourth - 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second + 1, row,
						third + 2, row, fourth - 1);

			}

		}

		else if (block_to_appear == 3) {
			color = TextColor.Indexed.fromRGB(139, 0, 0);
			// L shape

			int orientation = rand.nextInt(8);

			int first, second, third, fourth;

			first = second = third = fourth = 0;

			int random_int = 1;

			if (orientation == 0) {

				color = TextColor.Indexed.fromRGB(219, 48, 130);
				// L Reverse up
				int min = 2, max = COL - 3;
				int row = 0;

				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 8;

				if (!validate(grid, row, first, row + 1, second, row,
						third + 1, row, fourth + 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row,
						third + 1, row, fourth + 2);

			}

			else if (orientation == 1) {
				color = TextColor.Indexed.fromRGB(255, 255, 0);
				int min = 3, max = COL - 2;
				int row = 0;

				// L up

				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 9;

				if (!validate(grid, row, first, row + 1, second, row,
						third - 1, row, fourth - 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row,
						third - 1, row, fourth - 2);

			}

			else if (orientation == 2) {
				color = TextColor.Indexed.fromRGB(219, 48, 130);
				int min = 2, max = COL - 2;
				int row = 0;
				// L Reverse left
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 6;

				if (!validate(grid, row, first, row + 1, second, row + 2,
						third, row + 2, fourth + 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 2,
						third, row + 2, fourth + 1);
			}

			else if (orientation == 3) {
				color = TextColor.Indexed.fromRGB(255, 255, 0);
				int min = 2, max = COL - 2;
				int row = 0;
				// L Right
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 10;

				if (!validate(grid, row, first, row + 1, second, row + 2,
						third, row + 2, fourth - 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 2,
						third, row + 2, fourth - 1);
			}

			else if (orientation == 4) {
				color = TextColor.Indexed.fromRGB(219, 48, 130);

				int min = 2, max = COL - 2;
				int row = 0;
				// L Reverse Right
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 7;

				if (!validate(grid, row, first, row, second + 1, row + 1,
						third + 1, row + 2, fourth + 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second + 1, row + 1,
						third + 1, row + 2, fourth + 1);

			}

			else if (orientation == 5) {
				color = TextColor.Indexed.fromRGB(255, 255, 0);

				int min = 2, max = COL - 2;
				int row = 0;
				// L Left
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 11;

				if (!validate(grid, row, first, row, second - 1, row + 1,
						third - 1, row + 2, fourth - 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second - 1, row + 1,
						third - 1, row + 2, fourth - 1);

			}

			else if (orientation == 6) {
				color = TextColor.Indexed.fromRGB(219, 48, 130);

				int min = 2, max = COL - 3;
				int row = 0;
				// Left Reverse Down
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 5;

				if (!validate(grid, row, first, row + 1, second, row + 1,
						third - 1, row + 1, fourth - 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 1,
						third - 1, row + 1, fourth - 2);
			}

			else {

				color = TextColor.Indexed.fromRGB(255, 255, 0);

				int min = 3, max = COL - 3;
				int row = 0;
				// L up
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 12;

				if (!validate(grid, row, first, row + 1, second, row + 1,
						third + 1, row + 1, fourth + 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 1,
						third + 1, row + 1, fourth + 2);

			}

		}

		else {

			int orientation = rand.nextInt(4);

			int first, second, third, fourth;

			first = second = third = fourth = 0;

			int random_int = 0;

			if (orientation == 0) {
				color = TextColor.Indexed.fromRGB(144, 245, 0);
				int min = 3, max = COL - 3;
				int row = 0;
				// Right side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 18;

				if (!validate(grid, row, first, row, second + 1, row + 1,
						third + 1, row + 1, fourth + 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second + 1, row + 1,
						third + 1, row + 1, fourth + 2);

			}

			else if (orientation == 1) {
				color = TextColor.Indexed.fromRGB(0, 0, 176);
				int min = 2, max = COL - 3;
				int row = 0;
				// Right side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 16;

				if (!validate(grid, row, first, row, second - 1, row + 1,
						third - 1, row + 1, fourth - 2)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row, second - 1, row + 1,
						third - 1, row + 1, fourth - 2);

			}

			else if (orientation == 2) {

				color = TextColor.Indexed.fromRGB(0, 0, 176);

				int min = 2, max = COL - 2;
				int row = 0;
				// Right side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 15;

				if (!validate(grid, row, first, row + 1, second, row + 1,
						third + 1, row + 2, fourth + 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1, second, row + 1,
						third + 1, row + 2, fourth + 1);

			}

			else {
				color = TextColor.Indexed.fromRGB(144, 245, 0);
				int min = 2, max = COL - 2;
				int row = 0;
				// Right side T
				random_int = (int) Math.floor(Math.random()
						* (max - min + 1) + min);
				first = random_int % COL;
				second = first;
				third = first;
				fourth = first;

				category = 17;

				if (!validate(grid, row, first, row + 1,
						second, row + 1, third - 1, row + 2, fourth - 1)) {
					return false;
				}

				SetValues(contentAreas, grid, 1, color, row, first, row + 1,
						second, row + 1, third - 1, row + 2, fourth - 1);

			}

		}
		
        input_thread.setCategory(category);
        input_thread.setColor(color);

        return true;
    }

    public void StartInputThread(Screen screen, BlockGrid_Holder[][] contentAreas, int[][] grid) {

        input_thread = new input_handler(screen, contentAreas, grid, mutex, color, out);
    }

    public Game(Socket socket) throws IOException {
    	this.socket = socket;
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    public void sendGameOver() throws IOException {
    	out.writeUTF("Game Over");
	}

    BlockGrid_Holder[][] contentAreas;
    int[][] grid;
    Screen screen;

    public void SetMainInterface() {

        gamePanel.removeAllComponents();
        for (int i = 0; i < ROW; i++) {
        	
        	Panel horizontal = new Panel();
			horizontal.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
			for(int j = 0; j < COL; j++) {
				horizontal.addComponent(contentAreas[i][j].getPanel());
			}
			gamePanel.addComponent(horizontal);
            if (i + 1 < ROW) {
                gamePanel.addComponent(new Panel().addComponent(new EmptySpace(TextColor.ANSI.WHITE, new TerminalSize(2, 1))));
            }

        }
    }

    public void StartGame() throws UnsupportedAudioFileException, IOException, LineUnavailableException, JavaLayerException {
        mutex = new Semaphore(1);
	    
		Terminal terminal = new DefaultTerminalFactory().setInitialTerminalSize(new TerminalSize(60,52)).createTerminal();
		screen = new TerminalScreen(terminal);

        screen.startScreen();

        textGUI = new MultiWindowTextGUI(screen);

        try {
            window = new BasicWindow("Generic Block Game");

            gamePanel = new Panel();

            mainPanel = new Panel();
            mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

            gamePanel = new Panel();
            mainPanel.addComponent(gamePanel.withBorder(Borders.singleLine("Your Game")));

            buttonPanel = new Panel();
            buttonPanel.addComponent(new Button("Opponent 1", () -> {
            	if (gameId == 0)
            		setEnemyId(1);
            	else
            		setEnemyId(0);
			}));
			buttonPanel.addComponent(new Button("Opponent 2", () -> {
				if (gameId == 1)
					setEnemyId(2);
				else
					setEnemyId(1);
			}));
			buttonPanel.addComponent(new Button("Opponent 3", () -> {
				if (gameId == 2)
					setEnemyId(3);
				else
					setEnemyId(2);
			}));
            mainPanel.addComponent(buttonPanel.withBorder(Borders.singleLine("Select who has to receive the trash line")));

            grid = new int[ROW][COL];
            contentAreas = new BlockGrid_Holder[ROW][];

            for (int i = 0; i < ROW; i++) {
                contentAreas[i] = new BlockGrid_Holder[COL];

                for (int j = 0; j < COL; j++) {

                    grid[i][j] = 0;

                    contentAreas[i][j] = new BlockGrid_Holder();

                    contentAreas[i][j].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));

                }
            }

            SetMainInterface();

            window.setComponent(mainPanel.withBorder(Borders.singleLine()));

            StartInputThread(screen, contentAreas, grid);

            main_thread = new Thread(() -> {

                while (true) {

                    try {

                        break_it = false;

                        boolean block = block_generator(contentAreas, grid);

                        if (!block) {
                           // SendingEnd("Game Over");
                            Thread.sleep(2000);

                            GameOver = true;
                            sendGameOver();
                            break;
                        }

                        while (!break_it) {

                            Thread.sleep(TO_WAIT);
                            mutex.acquire();

                            //Reset(contentAreas);
                            int shape[][] = new int[4][2];
        					int prev_shape[][] = new int[4][2];

        					for (int i = 0; i < prev_shape.length; i++) {
        						prev_shape[i][0] = -1;
        						prev_shape[i][1] = -1;
        					}

        					
    						// Direction -> VERTICAL
    						
    						for (int i = 0; i < ROW; i++) {

    							if (break_it)
    								break;

    							for (int j = 0; j < COL && !break_it; j++) {

    								if (grid[i][j] == 1) {
    									boolean match = false;
    									for (int k = 0; k < shape.length; k++) {

    										if (shape[k][0] == 0
    												&& shape[k][1] == 0) {
    											prev_shape[k][0] = i;
    											prev_shape[k][1] = j;
    											if (i + 1 < ROW
    													&& grid[i + 1][j] != 2) {
    												shape[k][0] = i + 1;
    												shape[k][1] = j;
    											} else {
    												break_it = true;
    											}
    											match = true;
    											break;
    										}
    									}

    									if (!match) {
    										break_it = true;
    									}
    								}
    							}
    						}

    						if (break_it) {
    							// do nothing

    							for (int i = 0; i < ROW; i++) {

    								for (int j = 0; j < COL; j++) {

    									if (grid[i][j] == 1)
    										grid[i][j] = 2;
    								}
    							}
    							// Reset(contentAreas, grid);

    							category = 0;
    						} else {
    							Reset(contentAreas, grid);
    							boolean condition1 = false;
    							boolean condition2 = false;
    							for (int k = 0; k < shape.length; k++) {
    								int r = shape[k][0];
    								int c = shape[k][1];
    								contentAreas[r][c].SetBlock(color);
    								if (r + 1 >= ROW) {
    									condition1 = true;
    								}
    							}
    							if (!condition1) {
    								for (int k = 0; k < shape.length; k++) {
    									int r = shape[k][0];
    									int c = shape[k][1];
    									if (grid[r + 1][c] == 2) {
    										condition2 = true;
    									}
    								}
    							}

    							if (condition1 || condition2) {
    								for (int k = 0; k < shape.length; k++) {
    									int r = shape[k][0];
    									int c = shape[k][1];
    									grid[r][c] = 2;
    								}
    								category = 0;
    								break_it = true;
    							} else {
    								for (int k = 0; k < prev_shape.length; k++) {
    									int pr = prev_shape[k][0];
    									int pc = prev_shape[k][1];
    									grid[pr][pc] = 0;
    								}
    								for (int k = 0; k < shape.length; k++) {
    									int r = shape[k][0];
    									int c = shape[k][1];
    									grid[r][c] = 1;
    								}
    							}
    						}
    						
                            mutex.release();

                            Matched_Lines(contentAreas, grid);

                        }

                        category = 0;
                        input_thread.setCategory(category);

                        System.out.println("End");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Generic_Block_Game.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //mutex.release();
                }

            });

            main_thread.start();

            textGUI.addWindowAndWait(window);

        } finally {
//            screen.stopScreen();
        }
    }

    public void Windup() throws InterruptedException, IOException {

        mutex.acquire();

        for (int i = 0; i < ROW; i++) {

            for (int j = 0; j < COL; j++) {
                grid[i][j] = -1;

                contentAreas[i][j].getPanel().removeAllComponents();
            }

        }

        contentAreas = null;

        gamePanel.removeAllComponents();

        gamePanel = null;

        screen.stopScreen();

        screen = null;

        textGUI.removeWindow(window);

        textGUI = null;

        window.setComponent(null);

        window = null;

        break_it = false;

        input_thread.StopThread();

        main_thread.stop();

        input_thread = null;

        main_thread = null;

        this.GameOver = true;

        mutex.release();
    }

    public void Pause() throws InterruptedException {

        mutex.acquire();

    }

    public void Resume() {

        mutex.release();
    }

}
