package cfh.puzzle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SizePanel {

    private static final String PREF_COUNT = "sizeCount";
    private static final String PREF_TEMPLATE = "sizeTemplate";
    
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());
    
    private JPanel panel;
    private JTextField message;
    private JTextField countField;
    private JComboBox<String> templateBox;
    private String key = null;
    
    public SizePanel() {
        initGUI();
    }

    private void initGUI() {
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
        JLabel countLabel = new JLabel("Count:");
        countLabel.setLabelFor(countField);
        
        templateBox = new JComboBox<String>();
        templateBox.addItem("40");
        templateBox.addItem("50");
        templateBox.addItem("55");
        templateBox.addItem("60");
        templateBox.addItem("65");
        templateBox.addItem("85");
        templateBox.setSelectedItem(prefs.get(PREF_TEMPLATE, "65"));
        JLabel templateLabel = new JLabel("Template:");
        templateLabel.setLabelFor(templateBox);
        
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
        hg.addComponent(message);
        hg.addGroup(hg2);
        layout.setHorizontalGroup(hg);
        
        SequentialGroup vg = layout.createSequentialGroup();
        vg.addComponent(message);
        vg.addGap(4);
        vg.addGroup(layout.createBaselineGroup(false, false)
            .addComponent(countLabel)
            .addComponent(countField));
        vg.addGap(4);
        vg.addGroup(layout.createBaselineGroup(false, false)
            .addComponent(templateLabel)
            .addComponent(templateBox));
        layout.setVerticalGroup(vg);
    }
    
    private void doMessageClicked(MouseEvent ev) {
        if (ev.getClickCount() == 2 && ev.getButton() == ev.BUTTON1 && ev.getModifiersEx() == ev.CTRL_DOWN_MASK) {
            key = JOptionPane.showInputDialog(panel, "Key?", key);
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
                continue;
            }
            if (count < 4) {
                message.setText("count must be at least 4");
                continue;
            }
            Template templ;
            String templName = (String) templateBox.getSelectedItem();
            templ = Template.get(templName);
            message.setText(null);
            prefs.put(PREF_COUNT, Integer.toString(count));
            prefs.put(PREF_TEMPLATE, templName);
            return new TemplateSizeImpl(count, templ, key);
        }
    }
}
