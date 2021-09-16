/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic_Block_Game;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Panel;

/**
 *
 * @author LolloB
 */
public class BlockGrid_Holder {

    private Panel panel;
    private Indexed indexed_color;

    public BlockGrid_Holder() {
        panel = new Panel();
        indexed_color = null;
    }

    private void EmptyBlock() {

        if (panel != null) {
            indexed_color = TextColor.Indexed.fromRGB(255, 255, 255);
            panel.removeAllComponents();

        }

    }

    public void SetBlock(Indexed color) {
        if (panel != null) {
            EmptyBlock();
            indexed_color = color;
            if (color.toColor().getRed() == 255
                    && color.toColor().getBlue() == 255
                    && color.toColor().getGreen() == 255) {
                panel.addComponent(new EmptySpace(TextColor.ANSI.WHITE, new TerminalSize(1, 1)));
            } else {
                panel.addComponent(new EmptySpace(indexed_color, new TerminalSize(1, 1)));
            }

        }
    }

    public Indexed getColor() {
        return indexed_color;
    }

    public Panel getPanel() {
        return panel;
    }

    public void blink(boolean instruction) {

        if (instruction) {
            panel.removeAllComponents();
            panel.addComponent(new EmptySpace(TextColor.ANSI.WHITE, new TerminalSize(1, 1)));
        } else {
            panel.removeAllComponents();
            panel.addComponent(new EmptySpace(indexed_color, new TerminalSize(1, 1)));
        }

    }

}

