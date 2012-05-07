package regenerate_ver01;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Beenish Jamil, Emily Vorek
 *
 * @Class Description CoreqPanel is a GUI class that is the panel containing the input fields
 * for a new corequisite in the degree program creation area.
 * Contains an input text field for the name of the corequisite and a dropdown menu containing the possible
 * types of the corequisite (credits, course or requirement).
 * This panel appears below the correct course when the "Add Corequisite" button is clicked for a new course.
 */
public class CoreqPanel extends JPanel {

    public JTextField coreqField;
    public JComboBox coreqType;
    /*
     * Create and set up a new CoreqPanel that is the correct size. Do not allow the user to modify any
     * of the characteristics of the panel; the panel will be the same for any course.
     */
    public CoreqPanel() {
        this.setMinimumSize(new Dimension(800, 25));
        this.setMaximumSize(new Dimension(2600, 26));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        coreqField = new JTextField();
        coreqField.setMinimumSize(new Dimension(300, 20));
        coreqField.setMaximumSize(new Dimension(2000, 21));

        coreqType = new JComboBox();
        coreqType.addItem("Credits");
        coreqType.addItem("Course");
        coreqType.addItem("Requirement");

        coreqType.setMinimumSize(new Dimension(100, 20));
        coreqType.setMaximumSize(new Dimension(100, 21));
        JLabel coreqName = new JLabel("Name of Corequisite: ");

        this.add(coreqName);
        this.add(coreqField);
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(coreqType);
        this.add(Box.createGlue());
    }

    /*
     * Return the name of the new corequisite.  Do not allow access to the text field itself.
     */
    public String getCoreqName() {
        return this.coreqField.getText();
    }

    /*
     * Return the type of the new corequisite.  Do not allow access to the dropdown itself.
     */
    public String getCoreqType() {
        return (String)this.coreqType.getSelectedItem();
    }
}