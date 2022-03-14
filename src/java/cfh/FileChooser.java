/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Carlos F. Heuberger
 * @version 2.1, 2021-04-07
 */
public class FileChooser extends JFileChooser {

    private static final long serialVersionUID = 1L;

    private final String PREF_FILE = "file";
    
    private final Preferences prefs;
    
    public FileChooser(String key) {
        String classname = Thread.currentThread().getStackTrace()[2].getClassName();
        prefs = Preferences.userRoot().node("/" + classname.replace('.', '/') + "/" + key);
        File file = new File(prefs.get(PREF_FILE, "."));
        setSelectedFile(file);
        setMultiSelectionEnabled(false);
    }

    public FileChooser addFileFilter(FileFilter filter) {
        addChoosableFileFilter(filter);
        setAcceptAllFileFilterUsed(true);
        return this;
    }
    
    public File getFileToSave(Component parent) {
        return getFileToSave(parent, null);
    }
    
    public File getFileToSave(Component parent, String defaultExtension) {
        if (showSaveDialog(getParent()) != APPROVE_OPTION) {
            return null;
        }
        File file = getSelectedFile();
        if (defaultExtension != null && file.getName().indexOf('.') == -1) {
            file = new File(file.getParentFile(), file.getName() + "." + defaultExtension);
        }
        Object[] msg = { "File already exists!", file.getAbsolutePath(), "Overwrite?" };
        if (file.exists() && showConfirmDialog(getParent(), msg, "Confirm", OK_CANCEL_OPTION) != OK_OPTION)
            return null;
        if (file.exists()) {
            File bak = new File(file.getParentFile(), file.getName() + ".bak");
            if (bak.exists()) {
                bak.delete();
            }
            file.renameTo(bak);
        }
        return file;
    }
    
    public File getFileToLoad(Component parent) {
        return getFileToLoad(parent, null);
    }
    
    public File getFileToLoad(Component parent, String defaultExtension) {
        if (showOpenDialog(parent) != APPROVE_OPTION) {
            return null;
        }
        File file = getSelectedFile();
        if (defaultExtension != null && file.getName().indexOf('.') == -1 && !file.exists()) {
            file = new File(file.getParentFile(), file.getName() + "." + defaultExtension);
        }
        return file;
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        int option = super.showOpenDialog(parent);
        if (option == APPROVE_OPTION) {
            File file = getSelectedFile();
            prefs.put(PREF_FILE, file.getAbsolutePath());
        }
        return option;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        int option = super.showSaveDialog(parent);
        if (option == APPROVE_OPTION) {
            File file = getSelectedFile();
            prefs.put(PREF_FILE, file.getAbsolutePath());
        }
        return option;
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        int option = super.showDialog(parent, approveButtonText);
        if (option == APPROVE_OPTION) {
            File file = getSelectedFile();
            prefs.put(PREF_FILE, file.getAbsolutePath());
        }
        return option;
    }
}
