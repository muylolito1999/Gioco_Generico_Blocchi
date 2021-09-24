
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

public class Client {
    // initialize socket and input output streams

    private Socket socket = null;
    private DataInputStream input = null;
    private static DataOutputStream out = null;
    private DataInputStream sock_in = null;
    private Game game;
    private int id;
    private int enemyId;
    private int numberOfPlayers;

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
                        setId();
                        receiveNumberOfPLayers();
                        System.out.println(numberOfPlayers); // ricorda di eliminare
			game.setGameId(id);
						
			if(id!=0) {
			    enemyId=id-1;
			} else 
			    enemyId = 1;
			game.setEnemyId(enemyId);

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
                            System.out.println(numberOfPlayers); // ricorda di eliminare
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
                    } else if (line.equals("Exit")) {
			game.Windup();
			isStarted=false;
		    } else if (line.equals("Game Over")) {
                        numberOfPlayers--;
                        System.out.println("Someone Lost. Players remaining: " + numberOfPlayers);
                    } else {
			int enemyIdOfSender = Integer.valueOf(line)%10;
			if(enemyIdOfSender==id) {
			    int clearedLines = Integer.valueOf(line)/10;
			    game.SendingTrash(clearedLines);
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
    private void setId() throws IOException {
		id = sock_in.readInt();
		System.out.println("Client ID: " + id);
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

    public void receiveNumberOfPLayers() throws IOException{
        numberOfPlayers = sock_in.readInt();
    }

    public void sendNumberOfPLayers() throws IOException {
        out.writeInt(numberOfPlayers);
    }

}
