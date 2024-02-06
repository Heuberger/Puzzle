/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Carlos F. Heuberger
 */
public class SizePanel {

    private static final String PREF_COUNT = "sizeCount";
    private static final String PREF_TEMPLATE = "sizeTemplate";
    
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());
    
    private final String name;
    private final BufferedImage image;
    
    private JPanel panel;
    private JLabel imageName;
    private JTextField message;
    private JTextField countField;
    private JComboBox<String> templateBox;
    private JTextField sizeField;
    private char[] key = null;
    
    public SizePanel(String name, BufferedImage image) {
        this.name = name.replaceFirst(".*(/|\\\\)", "");
        this.image = image;
        initGUI();
    }

    private void initGUI() {
        imageName = new JLabel(name);
        imageName.setToolTipText(String.format("size: %d x %d", image.getWidth(), image.getHeight()));

        message = new JTextField();
        message.setEditable(false);
        message.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doMessageClicked(e);
            }
        });
        
        countField = new JTextField(10);
        countField.setText(prefs.get(PREF_COUNT, "200"));
        countField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent ev) {
                doSizeChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent ev) {
                doSizeChanged();
            }
            @Override
            public void changedUpdate(DocumentEvent ev) {
                doSizeChanged();
            }
        });
        JLabel countLabel = new JLabel("Count:");
        countLabel.setLabelFor(countField);
        
        templateBox = new JComboBox<>();
        templateBox.addItem("40");
        templateBox.addItem("50");
        templateBox.addItem("55");
        templateBox.addItem("60");
        templateBox.addItem("65");
        templateBox.addItem("75");
        templateBox.addItem("85");
        templateBox.setSelectedItem(prefs.get(PREF_TEMPLATE, "65"));
        templateBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                doSizeChanged();
            }
        });
        JLabel templateLabel = new JLabel("Template:");
        templateLabel.setLabelFor(templateBox);
        
        sizeField = new JTextField();
        sizeField.setEditable(false);
        sizeField.setToolTipText(String.format("image size: %d x %d", image.getWidth(), image.getHeight()));
        
        panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        
        layout.setAutoCreateContainerGaps(true);
        
        SequentialGroup hg2 = layout.createSequentialGroup();
        hg2.addGroup(layout.createParallelGroup()
            .addComponent(countLabel)
            .addComponent(templateLabel));
        hg2.addGap(4);
        hg2.addGroup(layout.createParallelGroup()
            .addComponent(countField)
            .addComponent(templateBox));
        ParallelGroup hg = layout.createParallelGroup();
        hg.addComponent(imageName);
        hg.addComponent(message);
        hg.addGroup(hg2);
        hg.addComponent(sizeField);
        layout.setHorizontalGroup(hg);
        
        SequentialGroup vg = layout.createSequentialGroup();
        vg.addComponent(imageName);
        vg.addGap(16);
        vg.addComponent(message);
        vg.addGap(4);
        vg.addGroup(layout.createBaselineGroup(false, false)
            .addComponent(countLabel)
            .addComponent(countField));
        vg.addGap(4);
        vg.addGroup(layout.createBaselineGroup(false, false)
            .addComponent(templateLabel)
            .addComponent(templateBox));
        vg.addGap(16);
        vg.addComponent(sizeField);
        layout.setVerticalGroup(vg);
        
        doSizeChanged();
    }
    
    private void doSizeChanged() {
        try {
            int count = Integer.parseInt(countField.getText());
            Template templ = Template.get((String) templateBox.getSelectedItem());
            Dimension d = Main.dimension(image, count, templ.getBorderWidth());
            sizeField.setText(String.format("%d x %d = %d pieces", d.width, d.height, d.width*d.height));
            sizeField.setToolTipText(String.format("%.2f x %.2f", (float)image.getWidth()/d.width, (float)image.getHeight()/d.height));
        } catch (IllegalArgumentException ex) {
            sizeField.setText("");
        }
    }
    
    private void doMessageClicked(MouseEvent ev) {
        if (ev.getClickCount() == 2 && ev.getButton() == ev.BUTTON1 && ev.getModifiersEx() == ev.CTRL_DOWN_MASK) {
            JPasswordField field = new JPasswordField(16);
            JOptionPane.showMessageDialog(panel, field, "Password?", JOptionPane.QUESTION_MESSAGE);
            key = field.getPassword();
        }
    }
    
    public Size showAndGetSize() {
        while (true) {
            int opt = JOptionPane.showConfirmDialog(null, panel, "Size", JOptionPane.OK_CANCEL_OPTION);
            if (opt != JOptionPane.OK_OPTION)
                return null;
            int count;
            try {
                count = Integer.parseInt(countField.getText());
            } catch (NumberFormatException ex) {
                message.setText("invalid integer: " + countField.getText());
                message.setForeground(Color.RED);
                continue;
            }
            if (count < 2) {
                message.setText("count must be at least 2");
                message.setForeground(Color.RED);
                continue;
            }
            Template templ;
            String templName = (String) templateBox.getSelectedItem();
            templ = Template.get(templName);
            message.setText(null);
            message.setForeground(null);
            prefs.put(PREF_COUNT, Integer.toString(count));
            prefs.put(PREF_TEMPLATE, templName);
            return new TemplateSizeImpl(count, templ, key);
        }
    }
}
