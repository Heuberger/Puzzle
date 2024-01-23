/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.puzzle;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * @author Carlos F. Heuberger, 2024-01-23
 *
 */
public class ShowPanel extends JPanel {

    private final double scale;
    private final Image image;
    
    private Point crosshair = null;
    
    public ShowPanel(double scale, Image image) {
        assert scale > 0 : scale;
        this.scale = scale;
        this.image = requireNonNull(image, "image");
        
        setPreferredSize(new Dimension((int)(image.getWidth(this)/scale), (int)(image.getHeight(this)/scale)));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                if (ev.getButton() == ev.BUTTON3) {
                    crosshair = crosshair==null ? ev.getPoint() : null;
                    repaint();
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gg = (Graphics2D) g;
        gg.scale(1/scale, 1/scale);
        gg.drawImage(image, 0, 0, this);
        
        if (crosshair != null) {
            gg.setXORMode(Color.WHITE);
            gg.scale(scale, scale);
            int x = crosshair.x;
            int y = crosshair.y;
            gg.drawLine(0, y, getWidth(), y);
            gg.drawLine(x, 0, x, getHeight());
        }
    }
}
