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
    private int first_movement_row;
    private int second_movement_row;
    private int third_movement_row;
    private int fourth_movement_row;
    private int first_movement_column;
    private int second_movement_column;
    private int third_movement_column;
    private int fourth_movement_column;

    public input_handler(Screen screen, BlockGrid_Holder[][] contentAreas, int[][] grid, Semaphore mutex, TextColor.Indexed color) {
        this.screen = screen;
        this.contentAreas = contentAreas;
        this.grid = grid;
        this.mutex = mutex;
        this.category = 0;
        this.color = color;

        this.first_movement_row = this.first_movement_column = 0;
        this.second_movement_row = this.second_movement_column = 0;
        this.third_movement_row = this.third_movement_column = 0;
        this.fourth_movement_row = this.fourth_movement_column = 0;

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

    private void SetValues(BlockGrid_Holder[][] contentAreas, int[][] grid, int value, TextColor.Indexed indexed_color, int row1, int column1, int row2, int column2, int row3, int column3, int row4, int column4) {

        contentAreas[row1][column1].SetBlock(indexed_color);
        grid[row1][column1] = value;

        contentAreas[row2][column2].SetBlock(indexed_color);
        grid[row2][column2] = value;

        contentAreas[row3][column3].SetBlock(indexed_color);
        grid[row3][column3] = value;

        contentAreas[row4][column4].SetBlock(indexed_color);
        grid[row4][column4] = value;

    }

    private boolean IsValid(int first_row, int first_column, int second_row, int second_column, int third_row, int third_column, int fourth_row, int fourth_column) {
        boolean lowerbound = (first_row >= 0 && first_column >= 0
                && second_row >= 0 && second_column >= 0
                && third_row >= 0 && third_column >= 0
                && fourth_row >= 0 && fourth_column >= 0);
        boolean upperbound = (first_row < 12 && first_column < 24
                && second_row < 12 && second_column < 24
                && third_row < 12 && third_column < 24
                && fourth_row < 12 && fourth_column < 24);

        return lowerbound && upperbound;
    }

    private boolean assign(int first_row, int first_column, int second_row, int second_column, int third_row, int third_column, int fourth_row, int fourth_column) {
        first_movement_row = first_row;
        first_movement_column = first_column;

        second_movement_row = second_row;
        second_movement_column = second_column;

        third_movement_row = third_row;
        third_movement_column = third_column;

        fourth_movement_row = fourth_row;
        fourth_movement_column = fourth_column;

        return IsValid(first_movement_row, first_movement_column,
                second_movement_row, second_movement_column,
                third_movement_row, third_movement_column,
                fourth_movement_row, fourth_movement_column);
    }

    private void rotate(BlockGrid_Holder[][] contentAreas, int[][] grid, int movement) throws InterruptedException {

        //movement = 0 -> clockwise
        //movement = 1 -> anti-clockwise
        mutex.acquire();

        if (category == 0) {
            mutex.release();
            return;
        }

        int first_row, first_column, second_row, second_column, third_row, third_column, fourth_row, fourth_column;

        first_row = first_column = second_row = second_column = third_row = third_column = fourth_row = fourth_column = 0;
        for (int i = 0; i < 12; i++) {

            for (int j = 0; j < 24; j++) {

                if (grid[i][j] == 1) {

                    if (first_row == 0 && first_column == 0) {
                        first_row = i;
                        first_column = j;
                    } else if (second_row == 0 && second_column == 0) {
                        second_row = i;
                        second_column = j;
                    } else if (third_row == 0 && third_column == 0) {
                        third_row = i;
                        third_column = j;
                    } else if (fourth_row == 0 && fourth_column == 0) {
                        fourth_row = i;
                        fourth_column = j;
                    }

                }

            }
        }

        if (category == 1) {
            //Right T

            if (movement == 0) {

                boolean valid = assign(first_row, first_column, second_row, second_column, third_row - 1, third_column - 1, fourth_row, fourth_column);

                if (valid && grid[third_movement_row][third_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 4;
                }

            } else {

                boolean valid = assign(first_row - 1, first_column + 1, second_row, second_column, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 3;

                }

            }

        } else if (category == 2) {
            //Left T

            if (movement == 0) {

                boolean valid = assign(first_row, first_column, second_row + 1, second_column + 1, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[second_movement_row][second_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);
                    category = 3;
                }

            } else {

                boolean valid = assign(first_row, first_column, second_row, second_column, third_row, third_column, fourth_row + 1, fourth_column - 1);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);
                    category = 4;
                }

            }

        } else if (category == 3) {
            //Up T

            if (movement == 0) {

                boolean valid = assign(first_row + 1, first_column - 1, second_row, second_column, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 1;
                }
            } else {

                boolean valid = assign(first_row, first_column, second_row, second_column, third_row, third_column, fourth_row - 1, fourth_column - 1);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 2;
                }

            }
        } else if (category == 4) {
            //reverse T

            if (movement == 0) {

                boolean valid = assign(first_row, first_column, second_row, second_column, third_row, third_column, fourth_row - 1, fourth_column + 1);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 2;
                }
            } else {

                boolean valid = assign(first_row + 1, first_column + 1, second_row, second_column, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 1;
                }

            }
        } else if (category == 5) {
            //Right L

            if (movement == 0) {

                boolean valid = assign(first_row, first_column - 1, second_row + 1, second_column + 1, third_row, third_column, fourth_row + 1, fourth_column);

                if (valid && grid[second_movement_row][second_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);
                    category = 6;
                }
            } else {

                boolean valid = assign(first_row - 1, first_column - 1, second_row - 2, second_column, third_row, third_column, fourth_row - 1, fourth_column - 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0
                        && grid[second_movement_row][second_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 7;
                }

            }
        } else if (category == 6) {
            //up L

            if (movement == 0) {

                boolean valid = assign(first_row + 1, first_column + 1, second_row, second_column, third_row - 1, third_column - 1, fourth_row, fourth_column - 2);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 8;
                }
            } else {

                boolean valid = assign(first_row + 2, first_column - 1, second_row, second_column + 1, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[second_movement_row][second_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);
                    category = 5;
                }

            }
        } else if (category == 7) {
            //down L
            if (movement == 0) {

                boolean valid = assign(first_row, first_column + 2, second_row + 1, second_column + 1, third_row, third_column, fourth_row - 1, fourth_column - 1);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0
                        && grid[second_movement_row][second_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 5;
                }
            } else {

                boolean valid = assign(first_row + 1, first_column, second_row + 1, second_column + 1, third_row, third_column, fourth_row, fourth_column - 1);

                if (valid && grid[second_movement_row][second_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 8;
                }

            }
        } else if (category == 8) {
            //Left L

            if (movement == 0) {

                boolean valid = assign(first_row - 1, first_column + 1, second_row, second_column, third_row + 1, third_column - 1, fourth_row - 2, fourth_column);

                if (valid && grid[fourth_movement_row][fourth_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 7;
                }
            } else {

                boolean valid = assign(first_row + 1, first_column + 2, second_row, second_column, third_row - 1, third_column - 1, fourth_row, fourth_column + 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 6;
                }

            }
        } else if (category == 9) {
            //Reverse left L

            if (movement == 0) {

                boolean valid = assign(first_row + 1, first_column, second_row, second_column, third_row - 1, third_column - 1, fourth_row, fourth_column - 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 10;
                }
            } else {

                boolean valid = assign(first_row - 1, first_column + 1, second_row, second_column, third_row - 1, third_column, fourth_row, fourth_column - 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);
                    category = 11;
                }

            }
        } else if (category == 10) {
            //Reverse Down L
            if (movement == 0) {

                boolean valid = assign(first_row + 2, first_column + 1, second_row, second_column - 1, third_row, third_column, fourth_row, fourth_column);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[second_movement_row][second_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 12;
                }
            } else {

                boolean valid = assign(first_row + 1, first_column + 1, second_row, second_column, third_row - 1, third_column, fourth_row, fourth_column + 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[third_movement_row][third_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 9;
                }

            }

        } else if (category == 11) {
            //Reverse up L

            if (movement == 0) {

                boolean valid = assign(first_row, first_column, second_row, second_column, third_row - 1, third_column - 1, fourth_row - 1, fourth_column + 1);

                if (valid && grid[third_movement_row][third_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 9;
                }
            } else {

                boolean valid = assign(first_row, first_column - 1, second_row + 1, second_column, third_row, third_column, fourth_row - 1, fourth_column - 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[second_movement_row][second_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 12;
                }

            }
        } else if (category == 12) {
            //Reverse Right L

            if (movement == 0) {

                boolean valid = assign(first_row, first_column + 1, second_row + 1, second_column + 1, third_row, third_column, fourth_row - 1, fourth_column);

                if (valid && grid[second_movement_row][second_movement_column] == 0
                        && grid[first_movement_row][first_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 11;
                }
            } else {

                boolean valid = assign(first_row, first_column + 1, second_row + 1, second_column, third_row, third_column, fourth_row + 1, fourth_column - 1);

                if (valid && grid[first_movement_row][first_movement_column] == 0
                        && grid[second_movement_row][second_movement_column] == 0
                        && grid[fourth_movement_row][fourth_movement_column] == 0) {

                    SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                            second_row, second_column,
                            third_row, third_column,
                            fourth_row, fourth_column);

                    SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                            second_movement_row, second_movement_column,
                            third_movement_row, third_movement_column,
                            fourth_movement_row, fourth_movement_column);

                    category = 10;
                }

            }
        } else if (category == 13) {
            //I (vertical)

            boolean valid = assign(first_row + 1, first_column + 1, second_row, second_column, third_row - 1, third_column - 1, fourth_row - 2, fourth_column - 2);

            if (valid && grid[fourth_movement_row][fourth_movement_column] == 0
                    && grid[third_movement_row][third_movement_column] == 0
                    && grid[first_movement_row][first_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);

                category = 14;
            }
        } else if (category == 14) {
            //I (horizontal)

            boolean valid = assign(first_row + 2, first_column + 2, second_row + 1, second_column + 1, third_row, third_column, fourth_row - 1, fourth_column - 1);

            if (valid && grid[fourth_movement_row][fourth_movement_column] == 0
                    && grid[second_movement_row][second_movement_column] == 0
                    && grid[first_movement_row][first_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);

                category = 13;
            }

        } else if (category == 15) {
            //4 (Horizontal)

            boolean valid = assign(first_row, first_column + 1, second_row, second_column, third_row, third_column, fourth_row - 2, fourth_column + 1);

            if (valid && grid[first_movement_row][first_movement_column] == 0
                    && grid[fourth_movement_row][fourth_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);

                category = 16;
            }
        } else if (category == 16) {
            //4 (Vertical)

            boolean valid = assign(first_row, first_column - 1, second_row + 2, second_column - 1, third_row, third_column, fourth_row, fourth_column);

            if (valid && grid[second_movement_row][second_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);
                category = 15;
            }

        } else if (category == 17) {
            //4 Reverse (Horizontal)

            boolean valid = assign(first_row, first_column - 1, second_row, second_column, third_row, third_column, fourth_row - 2, fourth_column - 1);

            if (valid && grid[first_movement_row][first_movement_column] == 0
                    && grid[fourth_movement_row][fourth_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);
                category = 18;
            }
        } else if (category == 18) {
            //4 Reverse (Vertical)

            boolean valid = assign(first_row, first_column + 2, second_row + 2, second_column, third_row, third_column, fourth_row, fourth_column);

            if (valid && grid[first_movement_row][first_movement_column] == 0
                    && grid[second_movement_row][second_movement_column] == 0) {

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                SetValues(contentAreas, grid, 1, color, first_movement_row, first_movement_column,
                        second_movement_row, second_movement_column,
                        third_movement_row, third_movement_column,
                        fourth_movement_row, fourth_movement_column);

                category = 17;
            }

        }

        mutex.release();

    }

    public void object_mover(BlockGrid_Holder[][] contentAreas, int[][] grid, int move) {

        int first_row, first_column, second_row, second_column, third_row, third_column, fourth_row, fourth_column;

        first_row = first_column = second_row = second_column = third_row = third_column = fourth_row = fourth_column = 0;

        boolean cant = false;

        try {
            mutex.acquire();

            if (category == 0) {
                mutex.release();
                return;
            }

            System.out.println("Mutex Acquired");

            for (int i = 0; i < 12 && !cant; i++) {
                for (int j = 0; j < 24 && !cant; j++) {

                    if (grid[i][j] == 0 || grid[i][j] == 2 || grid[i][j] == -1) {
                        continue;
                    }

                    if (move == 1 && i + move < 12 && grid[i][j] == 1 && grid[i + move][j] != 2) {

                        if (first_row == 0 && first_column == 0) {

                            first_row = i;
                            first_column = j;
                        } else if (second_row == 0 && second_column == 0) {

                            second_row = i;
                            second_column = j;
                        } else if (third_row == 0 && third_column == 0) {
                            third_row = i;
                            third_column = j;
                        } else if (fourth_row == 0 && fourth_column == 0) {

                            fourth_row = i;
                            fourth_column = j;

                        }

                    } else if (move == -1 && i + move >= 0 && grid[i][j] == 1 && grid[i + move][j] != 2) {
                        if (first_row == 0 && first_column == 0) {

                            first_row = i;
                            first_column = j;
                        } else if (second_row == 0 && second_column == 0) {

                            second_row = i;
                            second_column = j;
                        } else if (third_row == 0 && third_column == 0) {
                            third_row = i;
                            third_column = j;
                        } else if (fourth_row == 0 && fourth_column == 0) {

                            fourth_row = i;
                            fourth_column = j;

                        }

                    } else if (move == 2 && j < 23 && grid[i][j] == 1 && grid[i][j + 1] != 2 && grid[i][j + 1] != -1) {

                        if (first_row == 0 && first_column == 0) {

                            first_row = i;
                            first_column = j;
                        } else if (second_row == 0 && second_column == 0) {

                            second_row = i;
                            second_column = j;
                        } else if (third_row == 0 && third_column == 0) {
                            third_row = i;
                            third_column = j;
                        } else if (fourth_row == 0 && fourth_column == 0) {

                            fourth_row = i;
                            fourth_column = j;

                        }

                    } else {

                        cant = true;
                    }
                }
            }

            if (!cant) {

                System.out.println("Values Changed");

                SetValues(contentAreas, grid, 0, TextColor.Indexed.fromRGB(255, 255, 255), first_row, first_column,
                        second_row, second_column,
                        third_row, third_column,
                        fourth_row, fourth_column);

                if (move == 2) {

                    SetValues(contentAreas, grid, 1, color, first_row, first_column + 1,
                            second_row, second_column + 1,
                            third_row, third_column + 1,
                            fourth_row, fourth_column + 1);

                } else {
                    SetValues(contentAreas, grid, 1, color, first_row + move, first_column,
                            second_row + move, second_column,
                            third_row + move, third_column,
                            fourth_row + move, fourth_column);
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
                        object_mover(contentAreas, grid, 1);
                        System.out.println();
                    } else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
                        object_mover(contentAreas, grid, -1);
                    } else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
                        try {
                            rotate(contentAreas, grid, 0);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Generic_Block_Game.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else if (keyStroke.getKeyType() == KeyType.Enter) {
                        try {
                            rotate(contentAreas, grid, 1);
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
