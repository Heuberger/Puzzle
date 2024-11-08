/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static javax.swing.JOptionPane.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import cfh.FileChooser;

/**
 * @author Carlos F. Heuberger
 */
public class GamePanel extends JPanel implements GameListener {

    static final int DELTA_SNAP = 8;

    private final Sound sound = new Sound();
    
    private final String title;
    private BufferedImage image = null;
    private BufferedImage background = null;
    
    private final int sizeX;
    private final int sizeY;

    private JMenuItem showMenuItem;
    
    private final List<Piece> pieces = new ArrayList<>();
    
    private static final Integer KEY_HISTORY = KeyEvent.VK_BACK_SPACE; 
    private final Map<Integer, Point> bookmarks = new HashMap<>();

    private static final int KEY_NO_GROUP = KeyEvent.VK_SPACE;
    private int actualGroup = KEY_NO_GROUP;
    private final Map<Integer, List<Piece>> groups = new HashMap<>();

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
                boolean shift = (ev.getModifiersEx() & ev.SHIFT_DOWN_MASK) != 0;
                boolean mark = (ev.VK_0 <= ch && ch <= ev.VK_9) || (ev.VK_A <= ch && ch <= ev.VK_Z);
                Point actual = getLocation();
                
                if (shift && ctrl && mark) {
                    actualGroup = ch;
                    groups.put(ch, pieces.stream().filter(Piece::isSelected).collect(toList()));
                    repaint();
                    sound.groupCopy();
                } else if (shift && (mark || ch == KEY_NO_GROUP)) {
                    if (actualGroup != KEY_NO_GROUP) {
                        groups.put(actualGroup, pieces.stream().filter(Piece::isSelected).collect(toList()));
                    }
                    actualGroup = ch;
                    pieces.stream().forEach(Piece::unselect);
                    groups.getOrDefault(ch, emptyList()).stream().forEach(Piece::select);
                    repaint();
                    sound.groupSelect();
                } else if (ctrl && mark) {
                    putBookmark(ch, actual);
                    sound.posMark();
                } else if (!shift && !ctrl && (mark || ch == KEY_HISTORY)) {
                    Point p = bookmarks.get(ch);
                    if (p != null) {
                        putBookmark(KEY_HISTORY, actual);
                        setLocation(p);
                        repaint();
                        sound.posSet();
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
        return unmodifiableList(pieces);
    }
    
    protected Map<Integer, Point> getBookmarks() {
        return unmodifiableMap(bookmarks);
    }
    
    protected void putBookmark(Integer key, Point point) {
        bookmarks.put(key, point);
    }

    protected JPopupMenu createPopup() {
        Action homeAction = createAction("Home", this::doHome);
        JMenuItem home = new JMenuItem(homeAction);
        getInputMap().put(KeyStroke.getKeyStroke("HOME"), homeAction.getValue(Action.NAME));
        getActionMap().put(homeAction.getValue(Action.NAME), homeAction);
        
        JMenuItem arrange = new JMenuItem(createAction("Arrange", this::doArrange));
        
        showMenuItem = new JMenuItem(createAction("Show", this::doShow));
        showMenuItem.setEnabled(image != null);
        
        JMenuItem bg = new JMenuItem(createAction("Background", this::doBackground));
        
        JMenuItem debug = new JMenuItem(createAction("Debug", this::doDebug));
        
        JPopupMenu menu = new JPopupMenu();
        menu.add(showMenuItem);
        menu.add(bg);
        menu.add(home);
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
            sound.pieceJoin();
            if (piece.getGroup().size() == pieces.size()) {
                sound.complete();
            }
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
            sound.pieceDisconnect();
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

        String info = String.format("%c: %4d selected", (char) actualGroup, pieces.stream().filter(Piece::isSelected).count());
        gg.setFont(new Font("monospaced", Font.BOLD, 16));
        FontMetrics fm = gg.getFontMetrics();
        int tw = fm.stringWidth(info);
        int th = fm.getHeight();
        gg.setColor(new Color(0, 200, 0, 100));
        gg.fillRect(-getX(), -getY(), 4+tw+4, th);
        gg.setColor(new Color(0, 0, 0, 255));
        gg.drawString(info, 4-getX(), fm.getAscent()+fm.getLeading()-getY());
    }
    
    private void doHome(ActionEvent ev) {
        Point location = getLocation();
        if (location.x != 0 || location.y != 0) {
            putBookmark(KEY_HISTORY, location);
        }
        setLocation(0, 0);
    }
    
    private void doArrange(ActionEvent ev) {
        boolean all = actualGroup == KEY_NO_GROUP && pieces.stream().noneMatch(Piece::isSelected);
        if (isCtrl(ev)) {
            int w = getParent().getWidth();
            int h = getParent().getHeight();
            int i = 0;
            int j = 0;
            int x0 = 15 - getX();
            int y0 = 15 - getY();
            synchronized (getTreeLock()) {
                for (Piece piece : pieces) {
                    if (!piece.getConnected().isEmpty())
                        continue;
                    if (!all && !piece.isSelected())
                        continue;

                    int x = x0 + i*(sizeX+20) + 5*(j%2);
                    int y = y0 + j*(sizeY+20) + 5*(i%2);
                    piece.setLocation(x, y);

                    j += 1;
                    if (y + sizeY + piece.getHeight() > h - getY()) {
                        j = 0;
                        i += 1;
                        if (!isShift(ev) && (x + sizeX + piece.getWidth() > w - getX())) {
                            x0 += 16;
                            y0 += 16;
                            i = 0;
                        }
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
                        if (!all && !piece.isSelected())
                            continue;
      
                        int pw = (piece.getWidth() + sizeX) / 2 + 2;
                        int ph = (piece.getHeight() + sizeY) / 2 + 2; 
                        if (x + pw > w - getX()) {
                            x = - getX() + delta;
                            y += ph;
                            if (!isShift(ev) && (y + ph > h - getY())) {
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
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            double scale;
            if (image.getWidth() > screen.width-20 || image.getHeight() > screen.height-100) {
                double w = (double) image.getWidth() / (screen.width-20);
                double h = (double) image.getHeight() / (screen.height-100);
                scale = Math.max(w, h);
            } else {
                scale = 1.0;
            }
            if (ev ==null || (ev.getModifiers() & ev.CTRL_MASK) == 0) {
                scale *= 2;
            }

            ShowPanel panel = new ShowPanel(scale, image);
            JDialog preview = new JDialog(SwingUtilities.windowForComponent(this));
            preview.setTitle("Preview - " + title);
            preview.setModal(false);
            preview.add(new JScrollPane(panel));
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getButton() == ev.BUTTON1) {
                        preview.setVisible(false);
                        preview.dispose();
                    }
                }
            });
            preview.pack();
            preview.setVisible(true);
        }
    }

    private void doBackground(ActionEvent ev) {
        File file = new FileChooser("background").getFileToLoad(getParent());
        if (file != null) {
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
    
    private static boolean isShift(ActionEvent ev) {
        return (ev.getModifiers() & ev.SHIFT_MASK) != 0;
    }
    
    @SuppressWarnings("serial")
    private static Action createAction(String name, Consumer<ActionEvent> function) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent ev) {
                function.accept(ev);
            }
        };
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class DragListener extends MouseAdapter {

        private int gameX = 0;
        private int gameY = 0;

        private int pressedX;
        private int pressedY;
        
        private JWindow detail = null;

        @Override
        public void mousePressed(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)) {
                pressedX = ev.getX();
                pressedY = ev.getY();
                gameX = getX();
                gameY = getY();
                putBookmark(KEY_HISTORY, getLocation());
            } else if (SwingUtilities.isMiddleMouseButton(ev)) {
                if (detail == null) {
                    detail = new JWindow(getRootFrame());
                    BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gg = img.createGraphics();
                    try {
                        Point p = getLocationOnScreen();
                        gg.scale(3, 3);
                        gg.translate(p.getX() - ev.getXOnScreen() + img.getWidth()/6, p.getY() - ev.getYOnScreen() + img.getHeight()/6);
                        paint(gg);
                    } finally {
                        gg.dispose();
                    }
                    JLabel lbl = new JLabel(new ImageIcon(img));
                    detail.add(lbl);
                    detail.pack();
                    detail.setLocation(ev.getXOnScreen()-img.getWidth()/2, ev.getYOnScreen()-img.getHeight()/2);
                    detail.setVisible(true);
                }
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
            if (SwingUtilities.isMiddleMouseButton(ev)) {
                if (detail != null) {
                    detail.dispose();
                    detail = null;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)) {
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
}
