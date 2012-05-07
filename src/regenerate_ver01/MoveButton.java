package regenerate_ver01;

import javax.swing.*;
import java.awt.Dimension;

/**
 *
 * @author Emily
 */
public class MoveButton extends JButton {

    private JPanel panel;
    private int panelIndex;
    private String courseID;
    private int semester;

    public MoveButton(String courseID, int semester) {
        setMinimumSize(new Dimension(125, 20));
        setMaximumSize(new Dimension(126, 21));
        setText("Move Course");
        this.courseID = courseID;
        this.semester = semester;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    public void setPanelIndex(int panelIndex) {
        this.panelIndex = panelIndex;
    }

    public JPanel getPanel() {
        return this.panel;
    }

    public int getPanelIndex() {
        return this.panelIndex;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getSemester() {
        return this.semester;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }
}
