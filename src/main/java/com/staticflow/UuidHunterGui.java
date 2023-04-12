package main.java.com.staticflow;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Custom Settings UI for this Extension. It consists of a textfield for entering the preferred number of seconds before and after the target UUID to bruteforce
 * values for.
 */
public class UuidHunterGui extends JPanel {

    public UuidHunterGui() {
        this.setLayout(new BorderLayout());

        //Textfield Setup
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance()) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text == null || text.trim().isEmpty()) { // allow empty strings
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);
        JFormattedTextField timeRangeField = new JFormattedTextField(formatter);
        timeRangeField.setColumns(2);
        timeRangeField.setText("1");
        timeRangeField.setPreferredSize(new Dimension(100,20));
        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        textFieldPanel.add(timeRangeField);
        //End Textfield Setup

        //TextField Label Setup
        JLabel rangeLabel = new JLabel("Range for UUID Scan: ");
        rangeLabel.setPreferredSize(new Dimension(200,20));
        JPanel rangeLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        rangeLabelPanel.add(rangeLabel);
        //End TextField Label Setup

        //Save Button Setup
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> ExtensionState.getInstance().setUuidScanRange(Integer.parseInt(timeRangeField.getText())));
        saveButton.setPreferredSize(new Dimension(100,20));
        JPanel saveButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        saveButtonPanel.add(saveButton);
        //End Save Button Setup

        //Main Panel Setup
        JPanel allPanel = new JPanel();
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.X_AXIS));
        allPanel.add(Box.createVerticalGlue());
        allPanel.add(rangeLabelPanel);
        allPanel.add(Box.createRigidArea(new Dimension(1, 0))); // add some vertical spacing
        allPanel.add(textFieldPanel);
        allPanel.add(Box.createRigidArea(new Dimension(1, 0))); // add some vertical spacing
        allPanel.add(saveButtonPanel);
        allPanel.add(Box.createVerticalGlue());

        this.add(allPanel,BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50)); // add padding
        this.setPreferredSize(new Dimension(500,40));
        //End Main Panel Setup
    }
}
