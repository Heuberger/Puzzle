/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import static javax.swing.JOptionPane.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import cfh.FileChooser;

/**
 * @author Carlos F. Heuberger
 */
public class Main extends GamePanel {

    private static final String VERSION;
    static {
        String version = Main.class.getPackage().getImplementationVersion();
        VERSION = "Puzzle by Carlos F. Heuberger - v" + (version==null ? "?" : version);
    }
    
    private static final int MAXX = 9000;
    private static final int MAXY = 5000;
    
    private static final FileNameExtensionFilter JIG_EXTENSION_FILTER = new FileNameExtensionFilter("Puzzle Files (*.jig)", "jig");
    private static final FileNameExtensionFilter IMG_EXTENSION_FILTER = new FileNameExtensionFilter("Images (*.jpg,*.png,*.gif,...)", ImageIO.getReaderFileSuffixes());

    private static final Color[] COLORS = new Color[] {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW.darker().darker(),
        Color.MAGENTA,
        Color.GRAY,
        Color.CYAN,
        Color.ORANGE};
    
    private static final String ALGORITHM = "DESede";

    public static void main(String[] args) {
        String arg;
        String imageName;
        BufferedImage image = null;
        int index = 0;
        String open = null;

        while (index < args.length && args[index].startsWith("-")) {
            String opt = args[index++].substring(1);
            if (opt.length() == 0) {
                index -= 1;
                break;
            }
            
            if ("help".startsWith(opt)) {
                System.out.println(""
                        + "java -jar puzzle.jar [<image> [<count>x<template> [<seed> [<type>]]\n"
                        + "java -jar puzzle.jar -open <JIG-file>\n"
                        + "    <image>     image name (from resource) or path, none = no image\n"
                        + "    <count>     piece number\n"
                        + "    <template>  40, 50, 55, 60, 65, 75, 85 = piece template\n"
                        + "    <seed>      random = random seed, else the seed number\n"
                        + "    <type>      11 = normal, 21 = debug, others for testing\n"
                        + "    <JIG-file>  Jigsaw file to open"
                        );
                return;
            } else if ("open".startsWith(opt)) {
                if (index < args.length) {
                    open = args[index++];
                } else {
                    errorMessage("missing JIG-file to open");
                    return;
                }
            } else {
                errorMessage("unrecognized option", args[index-1]);
                return;
            }
        }
        
        if (open != null) {
            startJigsaw(new File(open));
            return;
        } else if (index < args.length && args[index].length() > 0) {
            arg = args[index++];
            if (arg.equals("-") || arg.equals("none")) {
                imageName = "none";
            } else {
                URL url;
                if (arg.startsWith("http")) {
                    try {
                        url = new URL(arg);
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        errorMessage(ex, arg);
                        return;
                    }
                } else {
                    url = Main.class.getResource(arg);
                    if (url == null && arg.charAt(0) != '/') {
                        url = Main.class.getResource("resources/" + arg);
                    }
                }
                if (url == null) {
                    try {
                        image = ImageIO.read(new File(arg));
                        imageName = arg;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        errorMessage(ex, arg);
                        return;
                    }
                } else {
                    try {
                        image = ImageIO.read(url);
                        imageName = url.toString();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        errorMessage(ex, arg, url);
                        return;
                    }
                }
            }
        } else {
            File file = new FileChooser("puzzle")
                .addFileFilter(JIG_EXTENSION_FILTER)
                .addFileFilter(IMG_EXTENSION_FILTER)
                .getFileToLoad(null, JIG_EXTENSION_FILTER.getExtensions()[0]);
            if (file != null) {
                imageName = file.getAbsolutePath();
                try {
                	image = ImageIO.read(file);
                } catch (IOException ex) {
                	ex.printStackTrace();
                	errorMessage(ex, file);
                	return;
                }
                if (image == null) {
                    startJigsaw(file);
                    return;
                }
            } else {
                imageName = "no image";
            }
        }
        
        Size size;
        if (index < args.length) {
            arg = args[index++];
            int i = arg.toLowerCase().indexOf('x');
            if (i == -1) {
                errorMessage("Wrong format, expected <count>x<template>", arg);
                return;
            } 
            int count;
            String templName;
            try {
                count = Integer.parseInt(arg.substring(0, i));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                errorMessage(ex, arg);
                return;
            }
            templName = arg.substring(i+1);
            Template templ = Template.get(templName);
            size = new TemplateSizeImpl(count, templ, null);   // TODO 
        } else {
            size = null;
        }
        
        long seed;
        if (index < args.length) {
            arg = args[index++];
        	if (arg.equals("random")) {
        		seed = randomSeed();
        	} else {
        		try {
        			seed = Long.parseLong(arg);
        		} catch (NumberFormatException ex) {
        			ex.printStackTrace();
        			errorMessage(ex, arg);
        			return;
        		}
        	}
        } else {
            seed = randomSeed();
        }
        
        int type = 11;
        if (index < args.length) {
            arg = args[index++];
            try {
                type = Integer.parseInt(arg);
            } catch (NumberFormatException ex) {
                errorMessage(ex);
            }
        }
        
        if (index < args.length) {
            errorMessage("unrecognized argument, ignoring", Arrays.copyOfRange(args, index, args.length));
            index = args.length;
        }
        
        if (size == null) {
        	SizePanel sizePanel = new SizePanel();
			size = sizePanel.showAndGetSize();
        }
        
        System.out.printf("Seed: %d%n", seed);
        System.out.printf("Image: %s%n", imageName);
        System.out.printf("Size: %s%n", size);
        
        if (size != null) {
            new Main(type, image, size, seed, imageName);
        }
    }
    
    protected static void errorMessage(Object... msg) {
        showMessageDialog(null, msg, "Error", ERROR_MESSAGE);
    }

	private static long randomSeed() {
		long seed = 8006678197202707420L ^ System.nanoTime();
		seed %= 100000;
		return seed;
	}
	
	private static void startJigsaw(File file) {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            int magic = input.readInt();
            char[] key;
            if (magic == MAGIC2) {
                JPasswordField field = new JPasswordField(16);
                if (JOptionPane.showConfirmDialog(null, field, "Input", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
                key = field.getPassword();
            } else {
                key =null;
            }
            if (magic == MAGIC || magic == MAGIC2) {
                int type = input.readInt();
                long seed = input.readLong();
                Size size = Size.read(input, key);
                BufferedImage image = decodeImage(input, key, seed);
                Main test = new Main(type, image, size, seed, file.getAbsolutePath());
                test.load(input, key);
            } else {
                errorMessage("unable to load ", file.getAbsolutePath());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            errorMessage(ex, "reading from", file.getAbsolutePath());
        }
	}


    private static final int MAGIC = 0x55F0_0105;
    private static final int MAGIC2 = 0x55F0_0106;
	
    private final Size puzzleSize;
    private final int type;
    private final long seed;
    private final BufferedImage image;
    
    private final Random random;

    private JFrame frame;

    private Main(int type, BufferedImage image, Size size, long seed, String title) {
        super(title, size.getSizeX(), size.getSizeY());

        this.puzzleSize = size;
        this.type = type;
        this.seed = seed;
        this.image = image;
        
        random = new Random(seed);

        for (Piece piece : createPieces(image)) {
            add(piece);
        }
        
        setLayout(null);
        setOpaque(false);
        setSize(MAXX, MAXY);
        
        frame = new JFrame(String.format("%s - %s - %s (%dx%d)", VERSION, title, size.toString(), size.width(), size.height()));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        URL url = Main.class.getResource("resources/icon.png");
        if (url == null) {
            System.err.println("unable to load \"resources/icon.png\"");
        } else {
            try {
                Image icon = ImageIO.read(url);
                frame.setIconImage(icon);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                if (showConfirmDialog(frame, "Close ?", "Confirm", YES_NO_OPTION) == YES_OPTION) {
                    frame.dispose();
                }
            }
        });
        frame.setLayout(null);
        frame.add(this);
        frame.setSize(1024, 800);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        doShow(null);
    }
    
    private void doSave(ActionEvent ev) {
        File file = new FileChooser("puzzle")
            .addFileFilter(JIG_EXTENSION_FILTER)
            .getFileToSave(getParent(), JIG_EXTENSION_FILTER.getExtensions()[0]);
        if (file != null) {
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
                output.writeInt(puzzleSize.getKey()==null ? MAGIC : MAGIC2);
                output.writeInt(type);
                output.writeLong(seed);
                puzzleSize.write(output);
                encodeImage(image, output, puzzleSize.getKey(), seed);
                save(output, puzzleSize);
            } catch (Exception ex) {
                ex.printStackTrace();
                error(ex.getClass().getSimpleName(), "Exception saving to", file.getAbsolutePath());
            }
        }
    }

    private void save(ObjectOutputStream output, Size size) throws IOException {
        encodeImage(getBackgroundImage(), output, size.getKey(), seed);
        output.writeObject(getBackground());
        output.writeObject(getBookmarks());
        
        List<Piece> pieces = getPieces();
        pieces.stream().forEach(p -> p.id = -1);
        output.writeInt(pieces.size());
        int i = 0;
        for (Piece piece : pieces) {
            piece.id = i++;
            output.writeInt(piece.getX());
            output.writeInt(piece.getY());
            output.writeInt(getComponentZOrder(piece));
            output.writeObject(piece.getDir());
            int[] connections = piece.getConnected().stream().mapToInt(p -> p.id).filter(id -> id != -1).toArray();
            output.writeObject(connections);
        }
    }
    
    private void load(ObjectInputStream input, char[] key) throws IOException {
        setBackgroundImage(decodeImage(input, key, seed));
        Map<?, ?> marks;
        try {
            setBackground((Color) input.readObject());
            marks = (Map<?, ?>) input.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException("reading background color", ex);
        }
        for (Entry<?, ?> entry : marks.entrySet()) {
            putBookmark((Integer)entry.getKey() , (Point)entry.getValue());
        }

        List<Piece> pieces = getPieces();
        int count = input.readInt();
        if (count != pieces.size())
            throw new IOException("wrong number of pieces");
        for (Piece piece : pieces) {
            int x = input.readInt();
            int y = input.readInt();
            int z = input.readInt();
            Direction dir;
            int[] connections;
            try {
                dir = (Direction) input.readObject();
                connections = (int[]) input.readObject();
            } catch (ClassNotFoundException ex) {
                throw new IOException("reading piece " + piece, ex);
            }
            piece.setLocation(x, y);
            setComponentZOrder(piece, z);
            piece.setDir(dir);
            for (int id : connections) {
                piece.connect(pieces.get(id));
            }
        }
        repaint();
    }
    
    @Override
    protected JPopupMenu createPopup() {
        JPopupMenu popup = super.createPopup();
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(this::doSave);
        popup.add(save);
        return popup;
    }
    
    private List<Piece> createPieces(BufferedImage image) {
        List<Piece> result = new ArrayList<Piece>();
        BufferedImage mask1 = loadImage("resources/mask1.png");
        BufferedImage mask2 = loadImage("resources/mask75.png");

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
        
        setImage(image);

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

            case 4: {  // 4 mask 1
                Piece[] quad = new Piece[4];
                for (int i = 0; i/4 < 4; i++) {
                    int x =  50 + (i%2)*50 + (i%4/2)*50 + (i/8)*250;
                    int y = 100 - (i%2)*50 + (i%8/2)*50 + (i%8/4)*150;
                    int ix = 850 + (i%2)*50 + (i%4/2)*50;
                    int iy = 600 - (i%2)*50 + (i%4/2)*50;
                    MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask1, image, ix, iy, i / 4);
                    piece.setLocation(x, y);
                    result.add(piece);
                    quad[i % 4] = piece;
                    if (i % 4 == 3) {
                        quad[0].addNeighbour(quad[1]);
                        quad[0].addNeighbour(quad[2]);
                        quad[1].addNeighbour(quad[3]);
                        quad[2].addNeighbour(quad[3]);
                    }
                }
            } break;

            case 5: {  // 4 mask 2
                Piece quad[] = new Piece[4];
                for (int i = 0; i/4 < 4; i++) {
                    int x = 50 + (i%2)*100 + (i/8)*250;
                    int y = 50 + (i%8/2)*100 + (i%8/4)*50;
                    int ix = 450 + (i%2)*100;
                    int iy = 300 + (i%4/2)*100;
                    MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask2, image, ix, iy, i / 4);
                    piece.setLocation(x, y);
                    result.add(piece);
                    quad[i % 4] = piece;
                    if (i % 4 == 3) {
                        quad[0].addNeighbour(quad[1]);
                        quad[0].addNeighbour(quad[2]);
                        quad[1].addNeighbour(quad[3]);
                        quad[2].addNeighbour(quad[3]);
                    }
                }
            } break;

            case 60:
            case 61:
            case 62:
            case 63:
            {  // rectangular/overlapped - correct orientation
                final int rule = type - 60;
                if (image == null) 
                    break;
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i;
                        int y = SY * j;
                        MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask2, image, x, y, rule);
                        result.add(piece);
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        free--;
                                        piece.setLocation(50 + i1*(SX+5), 50 + j1*(SY+5));
                                    }
                                    r--;
                                }
                            }
                        }
                        //                        piece.setLocation(50+x, 50+y);  //DEBUG
                    }
                }
            } break;
            
            case 70:
            case 71:
            case 72:
            case 73: 
            {    // rectangular overlapped rotated
                final int rule = type - 70;
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i;
                        int y = SY * j;
                        Direction dir;
                        switch (random.nextInt(5)) {
                            case 1: dir = Direction.EAST; break;
                            case 2: dir = Direction.SOUTH; break;
                            case 3: dir = Direction.WEST; break;
                            default: dir = Direction.NORTH; break;
                        }
                        MaskPiece piece = new MaskPiece(x, y, dir, mask2, image, x, y, rule);
                        result.add(piece);
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        free--;
                                        piece.setLocation(50 + i1*(SX+5), 50 + j1*(SY+5));
                                    }
                                    r--;
                                }
                            }
                        }
                    }
                }
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

    private BufferedImage loadImage(String filename) {
        URL url = getClass().getResource(filename);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException ex) {
                ex.printStackTrace();
                error(ex.getClass().getSimpleName(), "Exception loading image", url);
            }
        } else {
            error("Error", "Unable to load", filename);
        }
        return null;
    }
    
    private static void encodeImage(BufferedImage image, ObjectOutputStream output, char[] key, long seed) throws IOException {
        if (image == null) {
            output.writeInt(0);
        } else {
            byte[] data;
            try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                ImageIO.write(image, "PNG", result);
                data = result.toByteArray();
            }
            if (key != null && key.length > 0) {
                try {
                    DESedeKeySpec keySpec = new DESedeKeySpec(spec(key, seed));
                    SecretKey secret = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);
                    Cipher cipher = Cipher.getInstance(ALGORITHM);
                    cipher.init(cipher.ENCRYPT_MODE, secret);
                    data = cipher.doFinal(data);
                } catch (Exception ex) {
                    throw new IOException("Unable to encrypt", ex);
                }
            }
            output.writeInt(data.length);
            output.write(data);
        }
    }
    
    private static BufferedImage decodeImage(ObjectInputStream input, char[] key, long seed) throws IOException {
        int length = input.readInt();
        if (length == 0)
            return null;
        byte[] data = new byte[length];
        input.readFully(data);
        if (key != null && key.length > 0) {
            try {
                DESedeKeySpec keySpec = new DESedeKeySpec(spec(key, seed));
                SecretKey secret = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(cipher.DECRYPT_MODE, secret);
                data = cipher.doFinal(data);
            } catch (Exception ex) {
                throw new IOException("Unable to read", ex);
            }
        }
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            return ImageIO.read(stream);
        }
    }
    
    private static byte[] spec(char[] key, long seed) {
        StringBuilder builder = new StringBuilder(new String(key));
        long s = seed;
        while (builder.length() < 24) {
            builder.append(s);
            s += s;
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
