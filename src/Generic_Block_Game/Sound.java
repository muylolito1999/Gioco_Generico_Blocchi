package Generic_Block_Game;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Sound {

    File soundFile;
    AudioInputStream audioIn;
    Clip clip;
    boolean stop;
    Player player;
    Thread thread;

    public Sound() throws UnsupportedAudioFileException, IOException, LineUnavailableException, JavaLayerException {

//        audioIn = AudioSystem.getAudioInputStream(url);
//        clip = AudioSystem.getClip();
//        clip.open(audioIn);
//        stop = true;
//        StartSound();
        FileInputStream fis = new FileInputStream("Stratosphere.mp3");
        BufferedInputStream bis = new BufferedInputStream(fis);
        player = new Player(bis);

        StartSound();
    }

    public void StopSound() {

        if (thread.isAlive()) {
            stop = false;
            player.close();
            thread.stop();

        }

    }

    public void StartSound() {

        thread = new Thread(() -> {

            try {
                player.play();

                while (stop) {

                    if (player.isComplete()) {
                        player.play();
                    }
                }
            } catch (JavaLayerException ex) {
                Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        thread.start();

    }

}
