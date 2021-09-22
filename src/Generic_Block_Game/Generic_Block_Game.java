/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic_Block_Game;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;

/**
 *
 * @author LolloB
 */
public class Generic_Block_Game {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedAudioFileException, LineUnavailableException, JavaLayerException {

        Client client = new Client("127.0.0.1", 5000);
        int enemyId = client.getId()-1;
        if(client.getId()==0) {
            enemyId = 1;
        }
        client.setEnemyId(enemyId);

    }

}
