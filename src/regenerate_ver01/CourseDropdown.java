package regenerate_ver01;

/**
 *
 * @author Beenish Jamil, Emily Vorek
 *
 * @Class Description GUI class representing the dropdown menus in the schedule that hold each course
 * and allow the user to select different courses from a list.  Has an associated semester and attributes
 * indicating the currently selected course as well as the first course that was initially selected/displayed
 * (necessary for making schedule modifications).
 */
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.*;
import java.awt.Dimension;

public class CourseDropdown extends JComboBox {

    private Semester semester;
    private String selectedCourse;
    private Course firstCourse;
    private MoveButton move; //button that enables the user to move the course to a different semester
    private JCheckBox lockBox;  //indicates that the course has been locked to a semester

    /*
     * Default constructor
     */
    public CourseDropdown() {
        this(null, null);
    }
    
    /*
     * Creates a CourseDropdown that is associated with a particular semester (1-8)
     * and initial selected/displayed course (to ensure that the schedule displays accurately
     * and properly)
     */
    public CourseDropdown(Semester semester, String selectedCourse) {
        this.semester = semester;
        this.selectedCourse = selectedCourse;

        //associate a MoveButton with this CourseDropdown
        if (semester != null) {
            move = new MoveButton(selectedCourse, semester.getSemID());
        } else {
            move = new MoveButton(selectedCourse, 0);
        }

        lockBox = new JCheckBox();
        lockBox.setText("Lock course in this semester");
    }
    /*
     * Second constructor; only sets the semester for the CourseDropdown
     */
    public CourseDropdown(Semester semester) {
        this.semester = semester;
    }

    /*
     * Basic accessors
     */

    public JButton getMoveButton() {
        return this.move;
    }

    public JCheckBox getLockBox() {
        return this.lockBox;
    }

    public Course getFirstCourse() {
        return this.firstCourse;
    }

    public Semester getSemester() {
        return this.semester;
    }

    public String getSelectedCourse() {
        return this.selectedCourse;
    }

    /*
     * Mutators
     */

    public void setFirstCourse(Course firstCourse) {
        this.firstCourse = firstCourse;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
        move.setSemester(semester.getSemID());
    }

    /*
     * Associates the CourseDropdown and its MoveButton with a specific course.
     */
    public void setSelectedCourse(String course) {
        this.selectedCourse = course;
        move.setCourseID(course);
    }
}
