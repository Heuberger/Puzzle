package cfh;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

/**
 * @version 1.0, 01.12.2011
 */
public class FileChooser extends JFileChooser {

    private static final long serialVersionUID = 4258094342413678764L;

    private final String PREF_DIR = "directory";
    
    private final Preferences prefs;
    
    public FileChooser() {
        String classname = Thread.currentThread().getStackTrace()[2].getClassName();
        prefs = Preferences.userRoot().node("/" + classname.replace('.', '/'));
        String dir = prefs.get(PREF_DIR, ".");
        setCurrentDirectory(new File(dir));
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        int option = super.showOpenDialog(parent);
        if (option == APPROVE_OPTION) {
            String dir = getCurrentDirectory().getAbsolutePath();
            prefs.put(PREF_DIR, dir);
        }
        return option;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        int option = super.showSaveDialog(parent);
        if (option == APPROVE_OPTION) {
            String dir = getCurrentDirectory().getAbsolutePath();
            prefs.put(PREF_DIR, dir);
        }
        return option;
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        int option = super.showDialog(parent, approveButtonText);
        if (option == APPROVE_OPTION) {
            String dir = getCurrentDirectory().getAbsolutePath();
            prefs.put(PREF_DIR, dir);
        }
        return option;
    }
    
}

