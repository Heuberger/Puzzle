package cfh.puzzle;

import static javax.swing.JOptionPane.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import cfh.FileChooser;

public class GamePanel extends JPanel implements GameListener {

    static final int DELTA_SNAP = 8;

    private final String title;
    private BufferedImage image = null;
    private BufferedImage background = null;
    
    private final int sizeX;
    private final int sizeY;

    private JMenuItem showMenuItem;
    
    private JDialog preview = null;
    
    private final List<Piece> pieces = new ArrayList<>();
    
    private final Map<Integer, Point> bookmarks = new HashMap<>();
    private static final Integer KEY_HISTORY = KeyEvent.VK_SPACE; 
    

    public GamePanel(String title, int sx, int sy) {
        if (sx < 1) throw new IllegalArgumentException("negative sx: " + sx);
        if (sy < 1) throw new IllegalArgumentException("negative sy: " + sy);
        this.title = title;
        sizeX = sx;
        sizeY = sy;
        
        setComponentPopupMenu(createPopup());

        DragListener dragListener = new DragListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ev) {
                int ch = ev.getExtendedKeyCode();
                boolean ctrl = (ev.getModifiersEx() & ev.CTRL_DOWN_MASK) != 0;
                if ((ev.VK_0 <= ch && ch <= ev.VK_9) 
                        || (ev.VK_A <= ch && ch <= ev.VK_Z)
                        || ch == KEY_HISTORY) {
                    Point actual = getLocation();
                    if (ctrl && ch != KEY_HISTORY) {
                        putBookmark(ch, actual);
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        Point p = bookmarks.get(ch);
                        if (p != null) {
                            putBookmark(KEY_HISTORY, actual);
                            setLocation(p);
                            repaint();
                        }
                    }
                }
            }
        });
        setFocusable(true);
    }
    
    protected void setImage(BufferedImage img) {
        image = img;
        showMenuItem.setEnabled(image != null);
    }
    
    protected BufferedImage getImage() {
        return image;
    }

    protected void setBackgroundImage(BufferedImage img) {
        background = img;
        repaint();
    }
    
    protected BufferedImage getBackgroundImage() {
        return background;
    }
    
    protected List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }
    
    protected Map<Integer, Point> getBookmarks() {
        return Collections.unmodifiableMap(bookmarks);
    }
    
    protected void putBookmark(Integer key, Point point) {
        bookmarks.put(key, point);
    }

    protected JPopupMenu createPopup() {
        JMenuItem home = new JMenuItem("Home");
        home.addActionListener(this::doHome);
        
        JMenuItem arrange = new JMenuItem("Arrange");
        arrange.addActionListener(this::doArrange);
        
        showMenuItem = new JMenuItem("Show");
        showMenuItem.addActionListener(this::doShow);
        showMenuItem.setEnabled(image != null);
        
        JMenuItem bg = new JMenuItem("Background");
        bg.addActionListener(this::doBackground);
        
        JMenuItem debug = new JMenuItem("Debug");
        debug.addActionListener(this::doDebug);
        
        JPopupMenu menu = new JPopupMenu();
        menu.add(home);
        menu.add(showMenuItem);
        menu.addSeparator();
        menu.add(bg);
        menu.addSeparator();
        menu.add(arrange);
        menu.addSeparator();
        menu.add(debug);
        
        return menu;
    }

    public Component add(Piece piece) {
        piece.addGameListener(this);
        pieces.add(piece);
        return super.add(piece);
    }

    @Override
    public void pieceSelected(Piece piece) {
        Container parent = piece.getParent();
        parent.setComponentZOrder(piece, 0);
        piece.repaint();
        for (Piece p  : piece.getGroup()) {
            parent.setComponentZOrder(p, 0);
            p.repaint();
        }
    }

    @Override
    public void pieceMoved(Piece piece, int x, int y) {
        Piece next = null;
        double min = DELTA_SNAP + 1;
        Point myOffset = piece.getTileOffset();
        Set<Piece> group = piece.getGroup();
        for (Piece p : group) {
            for (Piece neighbour : p.getNeighbours()) {
                if (!group.contains(neighbour) && piece.getDir() == neighbour.getDir()) {
                    double dst = myOffset.distance(neighbour.getTileOffset());
                    if (dst < min) {
                        min = dst;
                        next = neighbour;
                    }
                }
            }
        }

        if (next != null) {
            Point offset = next.getTileOffset();
            int dx = offset.x - myOffset.x;
            int dy = offset.y - myOffset.y;
            for (Piece p : group) {
                p.setLocation(p.getX()+dx, p.getY()+dy);
            }
            piece.connect(next);
        }
    }

    @Override
    public void pieceDisconnect(Piece piece) {
        final Set<Piece> connected = piece.getConnected();
        if (!connected.isEmpty()) {
            for (Piece p : connected) {
                piece.disconnect(p);
                // disconnect non-neighbours
            }
            piece.setLocation(piece.getX()+2*DELTA_SNAP, piece.getY()+2*DELTA_SNAP);
            pieceSelected(piece);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        
        Graphics2D gg = (Graphics2D) g;
        
        if (background != null) {
            for (int x = 0; x < w; x += background.getWidth(this)) {
                for (int y = 0; y < h; y += background.getHeight(this)) {
                    gg.drawImage(background, x, y, this);
                }
            }
        } else {
            gg.setColor(getBackground());
            gg.fillRect(0, 0, getWidth(), getHeight());
        }

        gg.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x < w; x += sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.setColor(Color.BLUE);
        for (int x = 0; x < w; x += 5*sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 5*sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.setColor(Color.BLACK);
        for (int x = 0; x < w; x += 10*sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 10*sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.fillRect(0, 0, 2, h);
        gg.fillRect(0, 0, w, 2);
        gg.fillRect(w-2, 0, 2, h);
        gg.fillRect(0, h-2, w, 2);
    }
    
    private void doHome(ActionEvent ev) {
        Point location = getLocation();
        if (location.x != 0 || location.y != 0) {
            putBookmark(KEY_HISTORY, location);
        }
        setLocation(0, 0);
    }
    
    private void doArrange(ActionEvent ev) {
        if (isCtrl(ev)) {
            int w = getParent().getWidth();
            int h = getParent().getHeight();
            int i = 0;
            int j = 0;
            int x0 = 15 - getX();
            int y0 = 15 - getY();
            for (Piece piece : pieces) {
                if (!piece.getConnected().isEmpty())
                    continue;

                int x = x0 + i*(sizeX+20) + 5*(j%2);
                int y = y0 + j*(sizeY+20) + 5*(i%2);
                piece.setLocation(x, y);

                j += 1;
                if (y + sizeY + piece.getHeight() > h - getY()) {
                    j = 0;
                    i += 1;
                    if (x + sizeX + piece.getWidth() > w - getX()) {
                        x0 += 16;
                        y0 += 16;
                        i = 0;
                    }
                }
            }
        } else {
            int w = getParent().getWidth();
            int h = getParent().getHeight();
            int x = - getX();
            int y = - getY();
            int delta = 0;
            synchronized (getTreeLock()) {
                for (int i = 0; i < getComponentCount(); i++) {
                    Component comp = getComponent(i);
                    if (comp instanceof Piece) {
                        Piece piece = (Piece) comp;
                        if (!piece.getConnected().isEmpty())
                            continue;
                        
                        int pw = (piece.getWidth() + sizeX) / 2;
                        int ph = (piece.getHeight() + sizeY) / 2; 
                        if (x + pw > w - getX()) {
                            x = - getX() + delta;
                            y += ph;
                            if (y + ph > h - getY()) {
                                delta += 16;
                                x += 16;
                                y = - getY() + delta;
                            }
                        }
                        piece.setLocation(x, y);
                        x += pw;
                    } else {
                        continue;
                    }
                }
            }
        }
    }
    
    protected void doShow(ActionEvent ev) {
        if (image != null) {
            if (preview == null) {
                ImageIcon icon = new ImageIcon(image);
                JLabel msg = new JLabel(icon);
                preview = new JDialog(SwingUtilities.windowForComponent(this));
                preview.setTitle("Preview - " + title);
                preview.setModal(false);
                preview.add(msg);
                msg.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent ev) {
                        preview.setVisible(false);
                    }
                });
            }
            preview.pack();
            preview.setVisible(true);
        }
    }

    private void doBackground(ActionEvent ev) {
        FileChooser chooser = new FileChooser();
        if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    setBackgroundImage(img);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    error(ex.getClass().getSimpleName(), "Exception reading background from", file.getAbsolutePath());
                }
            } else {
                switch (file.getName()) {
                    case "":
                    case "empty":
                    case "none":
                        setBackground(null);
                        setBackgroundImage(null);
                        break;
                    case "c":
                    case "col":
                    case "color":
                    case "solid":
                        Color color = JColorChooser.showDialog(getParent(), "Background", getBackground());
                        if (color != null) {
                            setBackground(color);
                            setBackgroundImage(null);
                        }
                        break;
                    default:
                        error("Error", "not a file", file);
                        break;
                }
            }
        }
    }
    
    private void doDebug(ActionEvent ev) {
        System.out.println();
        for (Piece piece : pieces) {
            if (piece.getConnected().isEmpty())
                continue;
            System.out.printf("%s:", piece.getName());
            for (Piece connected : piece.getConnected()) {
                System.out.printf(" %s", connected.getName());
            }
            System.out.println();
        }
    }
    
    protected void error(String title, Object... msg) {
        showMessageDialog(getParent(), msg, title, ERROR_MESSAGE);
    }
    
    private static boolean isCtrl(ActionEvent ev) {
        return (ev.getModifiers() & ev.CTRL_MASK) != 0;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class DragListener extends MouseAdapter {

        private int gameX = 0;
        private int gameY = 0;

        private int pressedX;
        private int pressedY;

        @Override
        public void mousePressed(MouseEvent ev) {
            if (!SwingUtilities.isLeftMouseButton(ev)) {
                return;
            }
            pressedX = ev.getX();
            pressedY = ev.getY();
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            if (!SwingUtilities.isLeftMouseButton(ev)) {
                return;
            }
            gameX += ev.getX() - pressedX;
            gameY += ev.getY() - pressedY;
            if (gameX < getParent().getWidth() - getWidth()) {
                gameX = getParent().getWidth() - getWidth();
            }
            if (gameX > 0) {
                gameX = 0;
            }
            if (gameY < getParent().getHeight() - getHeight()) {
                gameY = getParent().getHeight() - getHeight();
            }
            if (gameY > 0) {
                gameY = 0;
            }
            setLocation(gameX, gameY);
        }
    }
}
