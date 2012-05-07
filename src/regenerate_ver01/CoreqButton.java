package regenerate_ver01;
import javax.swing.*;

/**
 *
 * @author Beenish Jamil, Emily Vorek

 * @Class Description:
 * This class enables an administrative user to add a corequisite for a course
 * in the degree program creation area.  When this button is clicked, input fields appear
 * under the input text fields for the correct course.
 * The correct course in the degree program creation window is identified using an index value
 * to ensure that the corequisite gets added to the correct course
 *
 */
public class CoreqButton extends JButton{
    private int index; //indicates the course in the window for which the coreq is being added

    /*
     * Sets up a CoreqButton and associates it with the proper course.
     */
    public CoreqButton(int index) {
        setText("Add Corequisite");
        this.index = index;
    }

    /*
     * Get the index indicating which course is associated with this button.
     */
    public int getIndex() {
        return this.index;
    }
}
