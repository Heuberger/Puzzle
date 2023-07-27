/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.puzzle;

import static java.lang.Math.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Carlos F. Heuberger, 2023-07-27
 *
 */
public class Infinite extends JPanel {

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            Infinite infinite = new Infinite(args);
            
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
            frame.add(infinite);
            frame.setSize(1000, 800);
            frame.validate();
            frame.setVisible(true);
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private final Size puzzleSize;
    private final Random random;
    private final int type;
    private final Color[] COLORS = new Color[] {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW.darker().darker(),
            Color.MAGENTA,
            Color.GRAY,
            Color.CYAN,
            Color.ORANGE};

    private final List<Piece> pieces;
    
    private int gx = 0;
    private int gy = 0;
    private double scale = 1.0;

    private Infinite(String... args) {
        puzzleSize = new TemplateSizeImpl(16, new Template75(), null);
        random = new Random(1);
        type = 11;
        pieces = Collections.unmodifiableList(createPieces(null));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getX() < 20) {
                    gx -= 10;
                } else if (e.getX() > getWidth()-20) {
                    gx += 10;
                }
                if (e.getY() < 20) {
                    gy -= 10;
                } else if (e.getY() > getHeight()-20) {
                    gy += 10;
                }
                repaint();
            }
        });
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int amount = e.getWheelRotation();
                scale *= pow(1.1, amount);
                if (scale > 0.91 && scale < 1.09) {
                    scale = 1.0;
                }
                repaint();
            }
        });
        
        setLayout(null);
        for (Piece piece: pieces) {
            add(piece);
        }
    }
    
    private List<Piece> createPieces(BufferedImage image) {
        List<Piece> result = new ArrayList<>();

        final int COUNT = puzzleSize.getCount();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        final int BORDER = puzzleSize.getBorderWidth();
        final int OVER = puzzleSize.getOverlap();

        final int X;
        final int Y;
        if (image == null) {
            Y = (int) Math.sqrt(COUNT);
            X = Math.round((float)COUNT / Y);
        } else {
            int width = image.getWidth() + BORDER + BORDER;
            int height = image.getHeight() + BORDER + BORDER;
            double area = (double)(width*height)/(COUNT);
            double min = Math.sqrt(area);
            X = (int) Math.round(width/min);
            Y = (int) Math.round(height/min);
            System.out.printf("Pieces: %dx%d=%d (%dx%d)%n", X, Y, X*Y, SX, SY);
            AffineTransform scale = AffineTransform.getScaleInstance(
                (double) (X*SX-BORDER-BORDER) / image.getWidth(),
                (double) (Y*SY-BORDER-BORDER) / image.getHeight());
            AffineTransformOp op = new AffineTransformOp(scale, AffineTransformOp.TYPE_BICUBIC);
            image = op.filter(image, null);
            if (BORDER > 0) {
                width = BORDER + image.getWidth() + BORDER;
                height = BORDER + image.getHeight() + BORDER;
                BufferedImage border = new BufferedImage(width, height, image.getType());
                Graphics2D gg = border.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    Color base = new Color(200, 150, 100);
                    gg.setColor(base);
                    for (int i = 0; i < BORDER; i += 2) {
                        gg.fill3DRect(i/2, i/2, width-i, height-i, true);
                    }
                    gg.drawImage(image, BORDER, BORDER, null);
                    image = border;
                } finally {
                    gg.dispose();
                }
            }
        }
        
        puzzleSize.width(X);
        puzzleSize.height(Y);
        
        switch (type) {
            case 0: {  // 5 ovals
                for (int i = 0; i < 5; i++) {
                    Piece piece = new ShapePiece(0, 0,
                        new Ellipse2D.Float(30f*i, 0f, 29f, 59f), 
                        image);
                    result.add(piece);
                }
            } break;

            case 1: {  // two pieces
                Path2D.Float div = new Path2D.Float();
                div.moveTo(100f, 000f);
                div.lineTo(100f, 040f);
                div.lineTo(110f, 040f);

                div.quadTo(120f, 040f, 120f, 030f);
                div.quadTo(140f, 050f, 120f, 070f);
                div.quadTo(120f, 060f, 110f, 060f);

                div.lineTo(100f, 060f);
                div.lineTo(100f, 100f);

                Path2D.Float path = new Path2D.Float();
                path.moveTo(000f, 000f);
                path.append(div, true);
                path.lineTo(000f, 100f);
                path.closePath();

                Piece piece1 = new ShapePiece(0, 0, path, image);
                piece1.setLocation(100, 100);
                result.add(piece1);

                path = new Path2D.Float(div);
                path.lineTo(200f, 100f);
                path.lineTo(200f, 000f);
                path.closePath();

                Piece piece2 = new ShapePiece(0, 0, path, image);
                piece2.setLocation(300, 100);
                result.add(piece2);

                piece1.addNeighbour(piece2);
            } break;

            case 2: {  // shapes
                AffineTransform translate = AffineTransform.getTranslateInstance(100f, 100f);
                Path2D.Float path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.CHORD), false);
                path.closePath();
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.OPEN), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.PIE), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float(); 
                path.append(new CubicCurve2D.Float(000f, 050f, 
                    000f, 000f, 
                    100f, 000f, 
                    100f, 050f), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new QuadCurve2D.Float(000f, 000f,
                    000f, 100f,
                    100f, 000f), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));
            } break;

            case 3: {  // one area
                Area area = new Area(new Rectangle(100, 100));
                area.add(new Area(new Ellipse2D.Float(110f, 035f, 030f, 030f)));
                area.subtract(new Area(new Rectangle(90, 40, 30, 20)));
                result.add(new ShapePiece(0, 0, area, image));
            } break;

            case 10:
            case 11:
            case 12:
            case 13:
            case 20:
            case 21:
            case 22:
            case 23:
            {  // maskS
                int rule = type % 10;
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                Piece[] list = new Piece[free];

                Polygon[][] shapes = createShapes(X, Y);
                BufferedImage[][] masks = createMasks(X, Y, shapes);

                Rectangle base = new Rectangle(OVER, OVER, SX, SY);
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i - OVER;
                        int y = SY * j - OVER;
                        Direction dir;
                        switch (random.nextInt(4)) {
                            case 1: dir = Direction.EAST; break;
                            case 2: dir = Direction.SOUTH; break;
                            case 3: dir = Direction.WEST; break;
                            default: dir = Direction.NORTH; break;
                        }                        
                        MaskPiece piece = type >= 20 ?
                                new MaskPieceDebug(x, y, dir, masks[i][j], image, x, y, rule, base, shapes[i][j]) :
                                new MaskPiece(x, y, dir, masks[i][j], image, x, y, rule);
                        piece.setName(String.format("%dx%d", j, i));
                        piece.setBackground(Color.getHSBColor((float)i/X, 0.25f+0.75f*j/Y, 0.8f));
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        piece.setLocation(SX-OVER + i*(SX+15), SY-OVER + j*(SY+15));

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        list[j1 + i1*Y] = piece;
                                        free -= 1;
                                        int x1 = SX-OVER + i1*(SX+18) + 5*(j1%2);
                                        int y1 = SY-OVER + j1*(SY+18) + 5*(i1%2);
                                        piece.setLocation(x1, y1);
                                    }
                                    r -= 1;
                                }
                            }
                        }
                    }
                }
                result.addAll(Arrays.asList(list));
            } break;

            default:
                System.err.println("unknown type " + type);
                System.exit(-1);
        }

        if (type < 10 || type > 29) {
            for (int i = 0; i < result.size(); i++) {
                Piece piece = result.get(i);
                piece.setForeground(Color.BLACK);
                piece.setBackground(COLORS[i % COLORS.length]);
            }
        }
        return result;
    }

    private Polygon[][] createShapes(int X, int Y) {
        final int BASE = puzzleSize.getBaseVariation();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        
        Polygon[][] shapes = new Polygon[X][Y];
        int[][] dx = new int[X+1][Y+1];
        int[][] dy = new int[X+1][Y+1];
        for (int j = 1; j < Y; j++) {
            dy[0][j] = rnd(0, BASE);
        }
        for (int i = 0; i < X; i++) {
            if (i+1 < X) {
                dx[i+1][0] = rnd(0, BASE);
            }
            for (int j = 0; j < Y; j++) {
                if (i+1 < X) {
                    dx[i+1][j+1] = rnd(0, BASE);
                }
                if (j+1 < Y) {
                    dy[i+1][j+1] = rnd(0, BASE);
                }
                Polygon shape = new Polygon();
                shape.addPoint(dx[i][j], dy[i][j]);
                shape.addPoint(SX+dx[i+1][j], dy[i+1][j]);
                shape.addPoint(SX+dx[i+1][j+1], SY+dy[i+1][j+1]);
                shape.addPoint(dx[i][j+1], SY+dy[i][j+1]);
                shape.addPoint(dx[i][j], dy[i][j]);
                shapes[i][j] = shape;
            }
        }
        return shapes;
    }
    
    private BufferedImage[][] createMasks(int X, int Y, Polygon[][] shapes) {
        final int BASE = puzzleSize.getBaseVariation();
        final int OVER = puzzleSize.getOverlap();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        final int PEGWIDTH = puzzleSize.getPegWidth();
        final int PEGLENGTH = puzzleSize.getPegLength();
        final int PEGRADIUS = puzzleSize.getPegRadius();
        final int PEGPOSDELTA = puzzleSize.getPegPositionDelta();
        final int PEGRADIUSDELTA = puzzleSize.getPegRadiusDelta();
        final int PEGHEIGHTDELTA = puzzleSize.getPegHeightDelta();

        BufferedImage[][] masks = new BufferedImage[X][Y];
        Color fillColor = new Color(128, 128, 128, 255);
        Color transpColor = new Color(128, 128, 128, 0);

        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                BufferedImage m = new BufferedImage(OVER+SX+OVER, OVER+SY+OVER, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gg = m.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.translate(OVER, OVER);
                    gg.setColor(fillColor);
                    gg.fill(shapes[i][j]);
                } finally {
                    gg.dispose();
                }
                masks[i][j] = m;
            }
        }
// if (true) return masks;        

        BufferedImage tmp;

        tmp = new BufferedImage(SX, BASE+OVER, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < X; i++) {
            for (int j = 1; j < Y; j++) {
                Graphics2D gg = tmp.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.setColor(transpColor);
                    gg.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
                    gg.setColor(fillColor);
                    int extra = random.nextInt(100);
                    if (extra < 3) {
                        int x = rnd(SX/2, 2*PEGPOSDELTA);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGRADIUSDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        gg.fillOval(x-w, BASE-h, w+w, h+h);
                    } else {
                        int x = rnd(SX/2, PEGPOSDELTA);
                        gg.fillRect(x-PEGWIDTH, 0, PEGWIDTH+PEGWIDTH, BASE+10);
                        int w = rnd(PEGRADIUS+2, PEGRADIUSDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        gg.fillOval(x-w, BASE+rnd(PEGLENGTH+1,1), w+w, h);
                    }
                } finally {
                    gg.dispose();
                }
                Graphics2D g0 = masks[i][j-1].createGraphics();
                Graphics2D g1 = masks[i][j].createGraphics();
                try {
                    if (random.nextInt(100) < (20+60*((i+j)%2))) {
                        g0.setComposite(AlphaComposite.SrcOver);
                        g1.setComposite(AlphaComposite.DstOut);
                        g0.drawImage(tmp, OVER, OVER+SY-BASE, this);
                        g1.drawImage(tmp, OVER, OVER-BASE, this);
                    } else {
                        g0.setComposite(AlphaComposite.DstOut);
                        g0.scale(1.0, -1.0);
                        g1.setComposite(AlphaComposite.SrcOver);
                        g1.scale(1.0, -1.0);
                        g0.drawImage(tmp, OVER, -OVER-SY-BASE, this);
                        g1.drawImage(tmp, OVER, -OVER-BASE, this);
                    }
                } finally {
                    g1.dispose();
                    g0.dispose();
                }
            }
        }
        tmp = new BufferedImage(BASE+OVER, SY, BufferedImage.TYPE_INT_ARGB);
        for (int i = 1; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                Graphics2D gg = tmp.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.setColor(transpColor);
                    gg.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
                    gg.setColor(fillColor);
                    int extra = random.nextInt(100);
                    if (extra < 3) {
                        int y = rnd(SY/2, 2*PEGPOSDELTA);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGRADIUSDELTA);
                        gg.fillOval(BASE-w, y-h, w+w, h+h);
                    } else if (extra < 999) {
                        int y = rnd(SY/2, PEGPOSDELTA);
                        gg.fillRect(0, y-PEGWIDTH, BASE+10, PEGWIDTH+PEGWIDTH);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        int h = rnd(PEGRADIUS+2, PEGRADIUSDELTA);
                        gg.fillOval(BASE+rnd(PEGLENGTH+1,1), y-h, w, h+h);
                    }
                } finally {
                    gg.dispose();
                }
                Graphics2D g0 = masks[i-1][j].createGraphics();
                Graphics2D g1 = masks[i][j].createGraphics();
                try {
                    if (random.nextInt(100) > (20+60*((i+j)%2))) {
                        g0.setComposite(AlphaComposite.SrcOver);
                        g1.setComposite(AlphaComposite.DstOut);
                        g0.drawImage(tmp, OVER+SX-BASE, OVER, this);
                        g1.drawImage(tmp, OVER-BASE, OVER, this);
                    } else {
                        g0.setComposite(AlphaComposite.DstOut);
                        g0.scale(-1.0, 1.0);
                        g1.setComposite(AlphaComposite.SrcOver);
                        g1.scale(-1.0, 1.0);
                        g0.drawImage(tmp, -OVER-SX-BASE, OVER, this);
                        g1.drawImage(tmp, -OVER-BASE, OVER, this);
                    }
                } finally {
                    g1.dispose();
                    g0.dispose();
                }
            }
        }

        final int EDGECOLOR = puzzleSize.getEdgeColorChange();
        long time = System.nanoTime();
        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                BufferedImage mask = masks[i][j];
                for (int x = 1; x < mask.getWidth()-1; x++) {
                    for (int y = 1; y < mask.getHeight()-1; y++) {
                        int rgb = mask.getRGB(x, y);
                        if (rgb != 0) {
                            if (mask.getRGB(x-1, y) == 0 || mask.getRGB(x, y-1) == 0) {
                                mask.setRGB(x, y, rgb - EDGECOLOR);
                            } else if (mask.getRGB(x+1, y) == 0 || mask.getRGB(x, y+1) == 0) {
                                mask.setRGB(x, y, rgb + EDGECOLOR);
                            }
                        }
                    }
                }
            }
        }
        time -= System.nanoTime();
        System.out.printf("time: %.3f ms%n", time / -1e6);

        return masks;
    }

    private int rnd(int mean, int delta) {
        int value;
        if (delta < 0) {
            value = mean + random.nextInt(-delta-delta+1) - -delta;
        } else if (delta % 2 == 0) {
            value = mean + 2*(random.nextInt(delta/2+delta/2+1) - delta/2);
        } else {
            value = mean + random.nextInt(delta+delta+1) - delta;
        }
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int w = getWidth();
        int h = getHeight();
        int sizeX = puzzleSize.getSizeX();
        int sizeY = puzzleSize.getSizeY();
        
        Graphics2D gg = (Graphics2D) g.create();
        try {
            gg.scale(scale, scale);
            
            gg.setColor(getBackground());
            gg.fillRect(0, 0, getWidth(), getHeight());

            gg.setColor(Color.LIGHT_GRAY);
            for (int x = -gx%sizeX; x < w; x += sizeX) {
                gg.drawLine(x, 0, x, h);
            }
            for (int y = -gy%sizeY; y < h; y += sizeY) {
                gg.drawLine(0, y, w, y);
            }

            gg.setColor(Color.BLUE);
            for (int x = -gx%(5*sizeX); x < w; x += 5*sizeX) {
                gg.drawLine(x, 0, x, h);
            }
            for (int y = -gy%(5*sizeY); y < h; y += 5*sizeY) {
                gg.drawLine(0, y, w, y);
            }

            gg.setColor(Color.BLACK);
            for (int x = -gx%(10*sizeX); x < w; x += 10*sizeX) {
                gg.drawLine(x, 0, x, h);
            }
            for (int y = -gy%(10*sizeY); y < h; y += 10*sizeY) {
                gg.drawLine(0, y, w, y);
            }

            gg.fillRect(0, 0, 2, h);
            gg.fillRect(0, 0, w, 2);
            gg.fillRect(w-2, 0, 2, h);
            gg.fillRect(0, h-2, w, 2);

            String info = String.format("testing - %-4d,%4d - %.2f", gx, gy, scale);
            gg.setFont(new Font("monospaced", Font.BOLD, 16));
            FontMetrics fm = gg.getFontMetrics();
            int tw = fm.stringWidth(info);
            int th = fm.getHeight();
            gg.setColor(new Color(0, 200, 0, 100));
            gg.fillRect(-getX(), -getY(), 4+tw+4, th);
            gg.setColor(new Color(0, 0, 0, 255));
            gg.drawString(info, 4-getX(), fm.getAscent()+fm.getLeading()-getY());
        } finally {
            gg.dispose();
        }
    }
    
    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D gg = (Graphics2D) g.create();
        try {
            gg.translate(-gx, -gy);
            gg.scale(scale, scale);
            super.paintChildren(gg);
        } finally {
            gg.dispose();
        }
    }
}
