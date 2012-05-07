package regenerate_ver01;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Beenish Jamil, Emily Vorek
 *
 * @Class Description PrereqPanel is a GUI class that is the panel containing the input fields
 * for a new prerequisite in the degree program creation area.
 * Contains an input text field for the name of the corequisite and a dropdown menu containing the possible
 * types of the prerequisite (credits or course).
 * This panel appears below the correct course when the "Add Prerequisite" button is clicked for a new course.
 */
public class PrereqPanel extends JPanel {

    public JTextField prereqField;
    public JComboBox prereqType;

    /*
     * Create and set up a new PrereqPanel that is the correct size. Do not allow the user to modify any
     * of the characteristics of the panel; the panel will be the same for any course.
     */
    public PrereqPanel() {
        this.setMinimumSize(new Dimension(800, 25));
        this.setMaximumSize(new Dimension(2600, 26));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        prereqField = new JTextField();
        prereqField.setMinimumSize(new Dimension(300, 20));
        prereqField.setMaximumSize(new Dimension(2000, 21));

        prereqType = new JComboBox();
        prereqType.addItem("Credits");
        prereqType.addItem("Course");

        prereqType.setMinimumSize(new Dimension(100, 20));
        prereqType.setMaximumSize(new Dimension(100, 21));
        JLabel prereqName = new JLabel("Name of Prerequisite: ");

        this.add(prereqName);
        this.add(prereqField);
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(prereqType);
        this.add(Box.createGlue());
    }

    /*
     * Return the name of the new prerequisite.  Do not allow access to the text field itself.
     */
    public String getPrereqName() {
        return this.prereqField.getText();
    }

    /*
     * Return the type of the new prerequisite.  Do not allow access to the dropdown itself.
     */
    public String getPrereqType() {
        return (String)this.prereqType.getSelectedItem();
    }
}
