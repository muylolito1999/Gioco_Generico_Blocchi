package Generic_Block_Game;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;

public class Generic_Block_Game {

    public static void main(String[] args) throws UnsupportedAudioFileException, LineUnavailableException, JavaLayerException {

        Client client = new Client("127.0.0.1", 5000);

    }

}
