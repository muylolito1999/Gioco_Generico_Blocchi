/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic_Block_Game;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import javazoom.jl.decoder.JavaLayerException;

/**
 *
 * @author LolloB
 */
public class Game {

    Indexed color = TextColor.ANSI.Indexed.fromRGB(1, 1, 1);

    public void Reset(BlockGrid_Holder[][] contentAreas, int[][] grid) {

        for (int i = 0; i < 12; i++) {

            for (int j = 0; j < 24; j++) {

                if (grid[i][j] != 2 && grid[i][j] != -1) {

                    grid[i][j] = 0;
                    contentAreas[i][j].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));

                }
            }

        }

    }

    Semaphore mutex;
    int TO_WAIT = 500;
    boolean break_it = false;
    int category = 0;
    input_handler input_thread;
    Sound sound;
    Panel mainPanel;
    BasicWindow window;
    Thread main_thread;
    MultiWindowTextGUI textGUI;
    private Socket socket = null;
    private DataOutputStream out;
    public boolean GameOver = false;
    private int Trashlined_rows = 0;

    public void Update_Comments() {

    }

    public boolean SendingEnd(String line) throws InterruptedException {
        mutex.acquire();

        int number_of_lines = Integer.valueOf(line);

        //int column = 23 - Trashlined_rows;
        int k = 0;

        int starting_index = -1;
        int ending_index = -1;

        for (int i = 0; i < 12; i++) {

            starting_index = ending_index = -1;

            for (k = 0; k < 24; k++) {

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

        }

        for (int i = 0; i < number_of_lines; i++) {

            for (int j = 0; j < 12; j++) {

                contentAreas[j][23 - Trashlined_rows].SetBlock(TextColor.Indexed.fromRGB(27, 30, 35));
                grid[j][23 - Trashlined_rows] = -1;

            }

            Trashlined_rows++;

        }

        mutex.release();
        return true;
    }

    public void SendingEnd(int number_of_lines) throws IOException {

        if (!this.socket.isOutputShutdown()) {
            out.writeUTF(String.valueOf(number_of_lines));
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
        for (int i = 0; i < 10; i++) {

            for (int j = 0; j < 12; j++) {

                for (int k = 0; k < columns.size(); k++) {
                    contentAreas[j][columns.get(k)].blink(!instruction);
                }
            }

            Thread.sleep(100);

            instruction = !instruction;
        }

        for (int i = 0; i < 12; i++) {

            for (int k = 0; k < columns.size(); k++) {
                contentAreas[i][columns.get(k)].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));
                grid[i][columns.get(k)] = 0;
            }
            Thread.sleep(80);
        }

        column--;
        int point = column;

        boolean inserted = false;

        for (int k = 0; k < 12; k++, column--) {
            for (int i = 0; i < 12 && column >= 0; i++) {

                if (grid[i][column] == 0) {
//                column--;
                    continue;
                }

                inserted = false;

                for (int j = column; j < 23; j++) {

                    if (grid[i][j + 1] == 0) {
                        continue;
                    } else {

                        Indexed indexer = contentAreas[i][column].getColor();

                        grid[i][column] = 0;
                        contentAreas[i][column].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));

                        grid[i][j] = 2;
                        contentAreas[i][j].SetBlock(indexer);
                        inserted = true;
                        Thread.sleep(80);
                        break;
                    }

                }

                if (!inserted) {

                    Indexed indexer = contentAreas[i][column].getColor();
                    grid[i][column] = 0;
                    contentAreas[i][column].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));
                    grid[i][23] = 2;
                    contentAreas[i][23].SetBlock(indexer);
                    Thread.sleep(80);
                }

            }
        }

        SendingEnd(columns.size());
        mutex.release();

    }

    public void Matched_Lines(BlockGrid_Holder[][] contentAreas, int[][] grid) throws InterruptedException, IOException {

        boolean isMatched = true;

        ArrayList<Integer> indexes = new ArrayList<>();
        int max_column = 0;

        for (int i = 23; i >= 0; i--) {

            isMatched = true;

            for (int j = 11; j >= 0; j--) {

                if (grid[j][i] != 2) {
                    isMatched = false;
                    break;
                }

            }

            if (isMatched) {

                if (max_column < i) {
                    max_column = i;
                }
                indexes.add(i);
            }
        }

        if (indexes.size() > 0) {
            Clear_Matched_Line(contentAreas, grid, indexes, max_column);
        }

    }

    public boolean block_generator(BlockGrid_Holder[][] contentAreas, int[][] grid) {

        Random rand = new Random();
        int block_to_appear = rand.nextInt(5);

        if (block_to_appear == 0) {

            color = TextColor.Indexed.fromRGB(56, 56, 56);

            int orientation = rand.nextInt(4);
            int first, second, third, fourth;

            first = second = third = fourth = 0;

            int random_int = 0;

            if (orientation == 0) {

                category = 1;

                int min = 2, max = 10;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth + 1, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth + 1, column + 1);

            } else if (orientation == 1) {

                category = 2;

                int min = 2, max = 10;
                int column = 0;
                //Left side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth - 1, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth - 1, column + 1);

            } else if (orientation == 2) {

                category = 3;
                int min = 2, max = 10;
                int column = 0;
                //Up T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column,
                        second - 1, column,
                        third + 1, column,
                        fourth, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second - 1, column,
                        third + 1, column,
                        fourth, column + 1);
            } else {

                //down T
                category = 4;
                int min = 2, max = 10;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column + 1,
                        second - 1, column + 1,
                        third + 1, column + 1,
                        fourth, column)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column + 1,
                        second - 1, column + 1,
                        third + 1, column + 1,
                        fourth, column);

            }

        } else if (block_to_appear == 1) {

            color = TextColor.Indexed.fromRGB(0, 255, 255);
            //block 4 tabs
            category = 30;
            int first, second, third, fourth;

            first = second = third = fourth = 0;

            int random_int = 0;

            int min = 2, max = 10;
            int column = 0;
            random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
            first = random_int % 12;
            second = first;
            third = first;
            fourth = first;

            if (!validate(grid, first, column,
                    second + 1, column,
                    third, column + 1,
                    fourth + 1, column + 1)) {
                return false;
            }

            SetValues(contentAreas, grid, 1, color, first, column,
                    second + 1, column,
                    third, column + 1,
                    fourth + 1, column + 1);

        } else if (block_to_appear == 2) {

            color = TextColor.Indexed.fromRGB(139, 0, 0);

            //Straight line
            int orientation = rand.nextInt(2);

            int first, second, third, fourth;

            first = second = third = fourth = 0;

            int random_int = 0;

            if (orientation == 0) {

                int min = 2, max = 10;
                int column = 0;
                category = 13;
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth, column + 3)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth, column + 3);
            } else {

                int min = 3, max = 9;
                int column = 0;
                category = 14;
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                if (!validate(grid, first, column,
                        second + 1, column,
                        third + 2, column,
                        fourth - 1, column)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second + 1, column,
                        third + 2, column,
                        fourth - 1, column);

            }

        } else if (block_to_appear == 3) {
            color = TextColor.Indexed.fromRGB(139, 0, 0);
            //L shape

            int orientation = rand.nextInt(8);

            int first, second, third, fourth;

            first = second = third = fourth = 0;

            int random_int = 1;

            if (orientation == 0) {

                color = TextColor.Indexed.fromRGB(219, 48, 130);
                //L Reverse up
                int min = 2, max = 9;
                int column = 0;

                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 11;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third + 1, column,
                        fourth + 2, column)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third + 1, column,
                        fourth + 2, column);

            } else if (orientation == 1) {
                color = TextColor.Indexed.fromRGB(255, 255, 0);
                int min = 3, max = 10;
                int column = 0;

                //L up
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 6;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third - 1, column,
                        fourth - 2, column)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third - 1, column,
                        fourth - 2, column);

            } else if (orientation == 2) {
                color = TextColor.Indexed.fromRGB(219, 48, 130);
                int min = 2, max = 10;
                int column = 0;
                //L Reverse left
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 9;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth + 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth + 1, column + 2);
            } else if (orientation == 3) {
                color = TextColor.Indexed.fromRGB(255, 255, 0);
                int min = 2, max = 10;
                int column = 0;
                //L Right
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 5;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth - 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third, column + 2,
                        fourth - 1, column + 2);
            } else if (orientation == 4) {
                color = TextColor.Indexed.fromRGB(219, 48, 130);

                int min = 2, max = 10;
                int column = 0;
                //L Reverse Right
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 12;

                if (!validate(grid, first, column,
                        second + 1, column,
                        third + 1, column + 1,
                        fourth + 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second + 1, column,
                        third + 1, column + 1,
                        fourth + 1, column + 2);

            } else if (orientation == 5) {
                color = TextColor.Indexed.fromRGB(255, 255, 0);

                int min = 2, max = 10;
                int column = 0;
                //L Left
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 8;

                if (!validate(grid, first, column,
                        second - 1, column,
                        third - 1, column + 1,
                        fourth - 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second - 1, column,
                        third - 1, column + 1,
                        fourth - 1, column + 2);

            } else if (orientation == 6) {
                color = TextColor.Indexed.fromRGB(219, 48, 130);

                int min = 2, max = 9;
                int column = 0;
                //Left Reverse Down
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 10;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third - 1, column + 1,
                        fourth - 2, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third - 1, column + 1,
                        fourth - 2, column + 1);
            } else {

                color = TextColor.Indexed.fromRGB(255, 255, 0);

                int min = 3, max = 9;
                int column = 0;
                //L up
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 7;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third + 1, column + 1,
                        fourth + 2, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third + 1, column + 1,
                        fourth + 2, column + 1);

            }

        } else {

            int orientation = rand.nextInt(4);

            int first, second, third, fourth;

            first = second = third = fourth = 0;

            int random_int = 0;

            if (orientation == 0) {
                color = TextColor.Indexed.fromRGB(144, 245, 0);
                int min = 3, max = 9;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 15;

                if (!validate(grid, first, column,
                        second + 1, column,
                        third + 1, column + 1,
                        fourth + 2, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second + 1, column,
                        third + 1, column + 1,
                        fourth + 2, column + 1);

            } else if (orientation == 1) {
                color = TextColor.Indexed.fromRGB(0, 0, 176);
                int min = 2, max = 9;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 17;

                if (!validate(grid, first, column,
                        second - 1, column,
                        third - 1, column + 1,
                        fourth - 2, column + 1)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second - 1, column,
                        third - 1, column + 1,
                        fourth - 2, column + 1);

            } else if (orientation == 2) {

                color = TextColor.Indexed.fromRGB(0, 0, 176);

                int min = 2, max = 10;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 18;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third + 1, column + 1,
                        fourth + 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third + 1, column + 1,
                        fourth + 1, column + 2);

            } else {
                color = TextColor.Indexed.fromRGB(144, 245, 0);
                int min = 2, max = 10;
                int column = 0;
                //Right side T
                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                first = random_int % 12;
                second = first;
                third = first;
                fourth = first;

                category = 16;

                if (!validate(grid, first, column,
                        second, column + 1,
                        third - 1, column + 1,
                        fourth - 1, column + 2)) {
                    return false;
                }

                SetValues(contentAreas, grid, 1, color, first, column,
                        second, column + 1,
                        third - 1, column + 1,
                        fourth - 1, column + 2);

            }

        }

        input_thread.setCategory(category);
        input_thread.setColor(color);

        return true;
    }

    public void StartInputThread(Screen screen, BlockGrid_Holder[][] contentAreas, int[][] grid) {

        input_thread = new input_handler(screen, contentAreas, grid, mutex, color);
    }

    public Game(Socket socket) throws IOException {
        this.socket = socket;
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    BlockGrid_Holder[][] contentAreas;
    int[][] grid;
    Screen screen;

    public void SetMainInterface() {

        mainPanel.removeAllComponents();
        for (int i = 0; i < 12; i++) {

            mainPanel.addComponent(Panels.horizontal(contentAreas[i][0].getPanel(), contentAreas[i][1].getPanel(),
                    contentAreas[i][2].getPanel(), contentAreas[i][3].getPanel(),
                    contentAreas[i][4].getPanel(), contentAreas[i][5].getPanel(),
                    contentAreas[i][6].getPanel(), contentAreas[i][7].getPanel(),
                    contentAreas[i][8].getPanel(), contentAreas[i][9].getPanel(),
                    contentAreas[i][10].getPanel(), contentAreas[i][11].getPanel(),
                    contentAreas[i][12].getPanel(), contentAreas[i][13].getPanel(),
                    contentAreas[i][14].getPanel(), contentAreas[i][15].getPanel(),
                    contentAreas[i][16].getPanel(), contentAreas[i][17].getPanel(),
                    contentAreas[i][18].getPanel(), contentAreas[i][19].getPanel(),
                    contentAreas[i][20].getPanel(), contentAreas[i][21].getPanel(),
                    contentAreas[i][22].getPanel(), contentAreas[i][23].getPanel()
            ));

            if (i + 1 < 12) {
                mainPanel.addComponent(new Panel().addComponent(new EmptySpace(TextColor.ANSI.WHITE, new TerminalSize(2, 1))));
            }

        }
    }

    public void StartGame() throws UnsupportedAudioFileException, IOException, LineUnavailableException, JavaLayerException {

        mutex = new Semaphore(1);

        screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        textGUI = new MultiWindowTextGUI(screen);

        try {
            window = new BasicWindow("Generic Block Game");

            mainPanel = new Panel();

            grid = new int[12][24];
            contentAreas = new BlockGrid_Holder[12][];

            for (int i = 0; i < 12; i++) {
                contentAreas[i] = new BlockGrid_Holder[24];

                for (int j = 0; j < 24; j++) {

                    grid[i][j] = 0;

                    contentAreas[i][j] = new BlockGrid_Holder();

                    contentAreas[i][j].SetBlock(TextColor.Indexed.fromRGB(255, 255, 255));

                }
            }

            SetMainInterface();

            window.setComponent(mainPanel);

            StartInputThread(screen, contentAreas, grid);

            main_thread = new Thread(() -> {

                while (true) {

                    try {

                        break_it = false;

                        boolean block = block_generator(contentAreas, grid);

                        if (!block) {
                            SendingEnd("Game Over");
                            Thread.sleep(2000);

                            GameOver = true;
                            break;
                        }

                        while (!break_it) {

                            Thread.sleep(TO_WAIT);
                            mutex.acquire();

                            //Reset(contentAreas);
                            int first_row, second_row, third_row, fourth_row;
                            int first_column, second_column, third_column, fourth_column;

                            int prev_first_row, prev_second_row, prev_third_row, prev_fourth_row;
                            int prev_first_column, prev_second_column, prev_third_column, prev_fourth_column;

                            first_row = second_row = third_row = fourth_row = 0;

                            first_column = second_column = third_column = fourth_column = 0;

                            prev_first_row = prev_second_row = prev_third_row = prev_fourth_row = -1;

                            prev_first_column = prev_second_column = prev_third_column = prev_fourth_column = -1;

                            for (int i = 0; i < 12; i++) {

                                if (break_it) {
                                    break;
                                }

                                for (int j = 0; j < 24 && !break_it; j++) {

                                    if (grid[i][j] == 1) {

                                        if (first_row == 0 && first_column == 0) {

                                            prev_first_row = i;
                                            prev_first_column = j;

                                            if (j + 1 < 24 && grid[i][j + 1] != 2 && grid[i][j + 1] != -1) {

                                                first_row = i;
                                                first_column = j + 1;

                                                // grid[i][j] = 0;
                                            } else {
                                                break_it = true;
                                            }
                                        } else if (second_row == 0 && second_column == 0) {

                                            prev_second_row = i;
                                            prev_second_column = j;

                                            if (j + 1 < 24 && grid[i][j + 1] != 2 && grid[i][j + 1] != -1) {

                                                second_row = i;
                                                second_column = j + 1;

                                                //grid[i][j] = 0;
                                            } else {
                                                break_it = true;
                                            }

                                        } else if (third_row == 0 && third_column == 0) {

                                            prev_third_row = i;
                                            prev_third_column = j;

                                            if (j + 1 < 24 && grid[i][j + 1] != 2 && grid[i][j + 1] != -1) {

                                                third_row = i;
                                                third_column = j + 1;

                                                //grid[i][j] = 0;
                                            } else {
                                                break_it = true;
                                            }

                                        } else if (fourth_row == 0 && fourth_column == 0) {

                                            prev_fourth_row = i;
                                            prev_fourth_column = j;

                                            if (j + 1 < 24 && grid[i][j + 1] != 2 && grid[i][j + 1] != -1) {
                                                fourth_row = i;
                                                fourth_column = j + 1;

                                                //grid[i][j] = 0;
                                            } else {
                                                break_it = true;
                                            }

                                        } else {
                                            break_it = true;
                                        }

                                    }

                                }
                            }

                            if (break_it) {
                                //do nothing    

                                for (int i = 0; i < 12; i++) {

                                    for (int j = 0; j < 24; j++) {

                                        if (grid[i][j] == 1) {
                                            grid[i][j] = 2;
                                        }
                                    }
                                }
                                //Reset(contentAreas, grid);

                                category = 0;
                                input_thread.setCategory(category);
                            } else {
                                Reset(contentAreas, grid);

                                contentAreas[first_row][first_column].SetBlock(color);
                                contentAreas[second_row][second_column].SetBlock(color);
                                contentAreas[third_row][third_column].SetBlock(color);
                                contentAreas[fourth_row][fourth_column].SetBlock(color);

                                if (first_column + 1 >= 24 || second_column + 1 >= 24 || third_column + 1 >= 24 || fourth_column + 1 >= 24) {
                                    grid[first_row][first_column] = 2;
                                    grid[second_row][second_column] = 2;
                                    grid[third_row][third_column] = 2;
                                    grid[fourth_row][fourth_column] = 2;
                                    category = 0;
                                    break_it = true;
                                } else if (grid[first_row][first_column + 1] == 2
                                        || grid[second_row][second_column + 1] == 2
                                        || grid[third_row][third_column + 1] == 2
                                        || grid[fourth_row][fourth_column + 1] == 2) {

                                    grid[first_row][first_column] = 2;
                                    grid[second_row][second_column] = 2;
                                    grid[third_row][third_column] = 2;
                                    grid[fourth_row][fourth_column] = 2;
                                    category = 0;
                                    break_it = true;
                                } else {

                                    grid[prev_first_row][prev_first_column] = 0;
                                    grid[prev_second_row][prev_second_column] = 0;
                                    grid[prev_third_row][prev_third_column] = 0;
                                    grid[prev_fourth_row][prev_fourth_column] = 0;

                                    grid[first_row][first_column] = 1;
                                    grid[second_row][second_column] = 1;
                                    grid[third_row][third_column] = 1;
                                    grid[fourth_row][fourth_column] = 1;
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

        for (int i = 0; i < 12; i++) {

            for (int j = 0; j < 24; j++) {
                grid[i][j] = -1;

                contentAreas[i][j].getPanel().removeAllComponents();
            }

        }

        contentAreas = null;

        mainPanel.removeAllComponents();

        mainPanel = null;

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
