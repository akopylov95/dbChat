package client;

import javax.swing.*;
import java.awt.*;

public class JTextFieldWithHint extends JTextField{
    private String hint;

    public JTextFieldWithHint(String hint) {
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(getText().isEmpty()){
            g.setColor(Color.blue);
            g.drawString(hint, 10, 18);
        }
    }
}
