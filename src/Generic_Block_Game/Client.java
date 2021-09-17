/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic_Block_Game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;

/**
 *
 * @author LolloB
 */
public class Client {
    // initialize socket and input output streams

    private Socket socket = null;
    private DataInputStream input = null;
    private static DataOutputStream out = null;
    private DataInputStream sock_in = null;
    private Game game;

    private void isGameOver() {
        Thread thread = new Thread(() -> {

            while (!game.GameOver);

            try {
                game.Windup();
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        thread.start();
    }

    private void ReceivingEnd() {

        Thread thread = new Thread(() -> {
            boolean isStarted = false;
            boolean isPaused = false;
            String line = "";

            // keep reading until "Over" is input
            while (true) {
                try {
                    line = sock_in.readUTF();

                    if (line.length() == 0) {
                        break;
                    } else if (!isStarted && line.equals("Start")) {

                        game = new Game(this.socket);

                        isGameOver();

                        Thread main_thread = new Thread(() -> {
                            try {
                                game.StartGame();
                            } catch (UnsupportedAudioFileException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (LineUnavailableException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JavaLayerException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        });

                        main_thread.start();
                        isStarted = true;

                    } else if (line.equals("Pause")) {

                        if (!isPaused) {
                            System.out.println("Paused");
                            game.Pause();
                            isPaused = true;
                        }
                    } else if (line.equals("Restart")) {

                        isStarted = true;
                        game.Windup();

                        game = new Game(this.socket);

                        isGameOver();

                        Thread main_thread = new Thread(() -> {
                            try {
                                game.StartGame();
                            } catch (UnsupportedAudioFileException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (LineUnavailableException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JavaLayerException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        });

                        main_thread.start();

                    } else if (line.equals("Resume")) {

                        if (isPaused) {
                            game.Resume();
                            isPaused = false;
                        }
                    } else {
                        if (!line.equals("Exit")) {
                            boolean status = game.SendingEnd(line);

                            if (!status) {
                                game.Windup();
                            }
                        }
                    }

                } catch (IOException i) {
                    System.out.println(i);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // close the connection
            try {
                input.close();
                out.close();
                socket.close();
            } catch (IOException i) {
                System.out.println(i);
            }

        });

        thread.start();

    }

    // constructor to put ip address and port
    public Client(String address, int port) throws UnsupportedAudioFileException, LineUnavailableException, JavaLayerException {
        // establish a connection
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal
            input = new DataInputStream(System.in);
            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());

            sock_in = new DataInputStream(socket.getInputStream());

            ReceivingEnd();
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }

    }
}
