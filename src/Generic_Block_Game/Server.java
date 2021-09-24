package Generic_Block_Game;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;

public class Server {
    
    private ServerSocket server;
    private int port;
    private ArrayList<Socket> sockets;
    private ArrayList<Socket> pending_sockets;
    private Sound sound;

    public Server(int port) throws IOException {
        this.port = port;
        sockets = new ArrayList<Socket>();
        pending_sockets = new ArrayList<Socket>();
        Initialize();

    }

    public void Initialize() throws IOException {

        server = new ServerSocket(port);
        System.out.println("Server started");

        Server_Instructions_Broadcaster();

        Client_handler();

    }

    public void Broadcast_Message(String line) {

        for (int i = 0; i < sockets.size(); i++) {

            if (sockets.get(i).isConnected() && !sockets.get(i).isOutputShutdown()) {
                DataOutputStream out = null;
                try {
                    out = new DataOutputStream(sockets.get(i).getOutputStream());
                    out.writeUTF(line);

                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    public void Server_Instructions_Broadcaster() {

        Thread thread = new Thread(() -> {

            boolean isStarted = false;
            boolean isPaused = false;

            Scanner input = new Scanner(System.in);

            String line = "";

            while (true) {

                line = input.next();

                System.out.println(line);

                if (line.equals("Start")) {

                    if (!isStarted) {

                        if (sockets.size() >= 2) {
                            try {
                                sound = new Sound();
                            } catch (UnsupportedAudioFileException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (LineUnavailableException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JavaLayerException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            Broadcast_Message(line);
                            isStarted = true;
			    sendId();	
                        } else {
                            System.out.println("At least 2 players should arrive for the game to start");
                        }
                    } else {
                        System.out.println("Game has Already Started");
                    }

                } else if (line.equals("Pause")) {

                    if (isStarted) {

                        if (!isPaused) {
                            sound.StopSound();
                            isPaused = true;
                            Broadcast_Message(line);
                        } else {
                            System.out.println("Game is Already paused");
                        }

                    } else {
                        System.out.println("Game is not Started yet");
                    }

                } else if (line.equals("Resume")) {

                    if (isStarted) {

                        if (isPaused) {

                            try {
                                sound = new Sound();
                            } catch (UnsupportedAudioFileException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (LineUnavailableException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JavaLayerException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            isPaused = false;
                            Broadcast_Message(line);
                        } else {
                            System.out.println("Game is not paused, so cant resume!");
                        }
                    }

                } else if (line.equals("Restart")) {

                    isStarted = true;
                    isPaused = false;
                    sound.StopSound();
                    try {
                        sound = new Sound();
                    } catch (UnsupportedAudioFileException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (LineUnavailableException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JavaLayerException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Broadcast_Message(line);

                } else if (line.equals("Exit")) {
		    Broadcast_Message(line);
  		    isStarted=false;
		}else {
                    System.out.println("Invalid Command!");
                }
            }

        });

        thread.start();

    }

    public void Server_Thread(Socket new_Socket) throws IOException {

        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new_Socket.getInputStream()));

        Thread thread = new Thread(() -> {

            String line = "";

            while (new_Socket.isConnected() && !line.equals("Exit")) {
                try {

                    System.out.println("Receive here on Top");
                    if (!new_Socket.isInputShutdown()) {

                        line = in.readUTF();

                        System.out.println(line);

                        if (line.length() == 0) {
                            break;
                        }

                        for (int i = 0; i < sockets.size(); i++) {

                            if (sockets.get(i) != new_Socket) {

                                if (sockets.get(i).isConnected() && !sockets.get(i).isOutputShutdown()) {
                                    System.out.println("Sent");
                                    DataOutputStream out = new DataOutputStream(sockets.get(i).getOutputStream());
                                    out.writeUTF(line);
                                }
                                //send to all connected clients
                            }
                        }

                        System.out.println(line);
                    }

                } catch (IOException i) {
                    System.out.println(i);
                }
            }

            try {
                new_Socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        thread.start();

    }
	
    public void sendId() {
	for(int i=0;i<sockets.size();i++) {
	    if (sockets.get(i).isConnected() && !sockets.get(i).isOutputShutdown()) {
		DataOutputStream out = null;
		try {
		    out = new DataOutputStream(sockets.get(i).getOutputStream());
		    out.writeInt(i);

		} catch (IOException ex) {
		Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
		}

	     }
	}
     }
	

    public void Client_handler() throws IOException {

        System.out.println("Waiting for a client ...");

        while (true) {

            Socket socket = server.accept();
            System.out.println("Client accepted");

            if (sockets.size() >= 0 && sockets.size() <= 4) {
                sockets.add(socket);
            } else {
                pending_sockets.add(socket);
            }

            Server_Thread(socket);

        }

    }

    public static void main(String args[]) throws IOException {
        Server server = new Server(5000);
    }

}
