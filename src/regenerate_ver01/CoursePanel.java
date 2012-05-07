package regenerate_ver01;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 * @author Beenish Jamil, Emily Vorek
 * @Class Description GUI class containing all of the input fields for a new course; this class
 * appears in the degree program creation area.
 * Facilitates access to the information about the new course; all input fields are attributes of the
 * CoursePanel to aid in the parsing of this information.
 */
public class CoursePanel extends JPanel {

    private JTextField courseIDField, numCreditsField, semestersField, priorityField, reqType;
    private static CoreqButton addCoreq;
    private static PrereqButton addPrereq;
    //set the size of the panel in advance to ensure that it displays properly
    private int panelMinHeight = 25;
    private int panelMinWidth = 800;
    private int panelMaxHeight = 26;
    private int panelMaxWidth = 2500;
    private static JComboBox prereqType, coreqType;
    private ArrayList<PrereqPanel> prereqPanels = new ArrayList<PrereqPanel>(); //all child PrereqPanels for this course
    private ArrayList<CoreqPanel> coreqPanels = new ArrayList<CoreqPanel>(); //all child CoreqPanels for this course
    private JComboBox placementTest;
    private ArrayList<String> prereqs = new ArrayList<String>(); //list of prereqs to be associated with the new course
    private ArrayList<String> coreqs = new ArrayList<String>(); //list of coreqs to be associated with the new course


    /*
     * Constructor for a new CoursePanel.  The index parameter is used to associate the PrereqButton
     * and CoreqButton with this specific panel.  The size/configuration of this panel cannot be modified
     * by any calling methods; this ensures that all fields contained in the panel display properly.
     */
    public CoursePanel(int index) {
        JLabel reqTypeLabel = new JLabel("Requirement Type: ");
        placementTest = new JComboBox();
        placementTest.addItem("Placement Test Required");
        placementTest.addItem("Placement Test Not Required");
        placementTest.setMinimumSize(new Dimension(175, 25));
        placementTest.setMaximumSize(new Dimension(176, 26));
        reqType = new JTextField();
        reqType.setMinimumSize(new Dimension(500, 20));
        reqType.setMaximumSize(new Dimension(500, 21));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setMinimumSize(new Dimension(panelMinWidth, panelMinHeight));
        this.setMaximumSize(new Dimension(panelMaxWidth, panelMaxHeight));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        JLabel courseID = new JLabel("Course ID: ");
        courseIDField = new JTextField();
        courseIDField.setMinimumSize(new Dimension(500, 20));
        courseIDField.setMaximumSize(new Dimension(500, 21));
        JLabel numCredits = new JLabel("Number of Credits: ");
        numCreditsField = new JTextField();
        numCreditsField.setMinimumSize(new Dimension(125, 20));
        numCreditsField.setMaximumSize(new Dimension(125, 21));
        JLabel semesters = new JLabel("Semester(s) it should be taken in (separated by commas): ");
        semestersField = new JTextField();
        semestersField.setMinimumSize(new Dimension(500, 20));
        semestersField.setMaximumSize(new Dimension(500, 21));
        JLabel priority = new JLabel("Priority: ");
        priorityField = new JTextField();
        priorityField.setMinimumSize(new Dimension(125, 20));
        priorityField.setMaximumSize(new Dimension(125, 21));
        addPrereq = new PrereqButton(index);
        addCoreq = new CoreqButton(index);
        prereqType = new JComboBox();
        prereqType.addItem("Credits");
        prereqType.addItem("Course");

        coreqType = new JComboBox();
        coreqType.addItem("Credits");
        coreqType.addItem("Course");
        coreqType.addItem("Requirement");

        contentPanel.add(courseID);
        contentPanel.add(courseIDField);
        contentPanel.add(numCredits);
        contentPanel.add(numCreditsField);
        contentPanel.add(semesters);
        contentPanel.add(semestersField);
        contentPanel.add(priority);
        contentPanel.add(priorityField);
        contentPanel.add(reqTypeLabel);
        contentPanel.add(reqType);
        contentPanel.add(addPrereq);
        contentPanel.add(addCoreq);
        contentPanel.add(placementTest);

        this.add(contentPanel);
    }

    public JTextField getCourseIDField() {
        return this.courseIDField;
    }

    /*
     * Accessors; do not allow any calling methods to modify the attributes of this panel
     */
    public JTextField getNumCreditsField() {
        return this.numCreditsField;
    }

    public JTextField getSemestersField() {
        return this.semestersField;
    }

    public JTextField getPriorityField() {
        return this.priorityField;
    }

    public JTextField getReqType() {
        return this.reqType;
    }

    public CoursePanel getCoursePanel(int index) {
        return new CoursePanel(index);
    }

    public String getPlacementTest() {
        return (String) placementTest.getSelectedItem();
    }

    public ArrayList<String> getPrereqs() {
        return this.prereqs;
    }

    public ArrayList<String> getCoreqs() {
        return this.coreqs;
    }

    public ArrayList<PrereqPanel> getPrereqPanels() {
        return this.prereqPanels;
    }

    public ArrayList<CoreqPanel> getCoreqPanels() {

        return this.coreqPanels;
    }

    public JPanel getPrereqPanel(int index) {
        return this.prereqPanels.get(index);
    }

    public PrereqButton getAddPrereq() {
        return addPrereq;
    }

    public CoreqButton getAddCoreq() {
        return addCoreq;
    }


    /*
     * Enlarges this panel by the correct amount so that its PrereqPanel(s) and CoreqPanel(s) display properly.
     * Do not allow the user to specify by how much the panel is enlarged.
     */
    public void expandPanel() {
        panelMinHeight = panelMinHeight * 2;
        panelMaxHeight = panelMaxHeight * 2;
        this.setMinimumSize(new Dimension(panelMinWidth, panelMinHeight));
        this.setMaximumSize(new Dimension(panelMaxWidth, panelMaxHeight));
    }

    /*
     * When a PrereqPanel is added, add it to the list of PrereqPanels associated with this CoursePanel
     * and add the name of the new prereq to another list.
     */
    public void addPrereqToList(PrereqPanel prereqPanel) {
        prereqPanels.add(prereqPanel);
        prereqs.add(prereqPanel.getPrereqName());
    }

    /*
     * When a CoreqPanel is added, add it to the list of CoreqPanels associated with this CoursePanel
     * and add the name of the new coreq to another list.
     */
    public void addCoreqToList(CoreqPanel coreqPanel) {
        coreqPanels.add(coreqPanel);
        coreqs.add(coreqPanel.getCoreqName());
    }
}
