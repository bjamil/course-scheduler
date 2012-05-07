package regenerate_ver01;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Beenish Jamil, Emily Vorek
 * @Class Description Used in the degree program creation area.  Prompts the user for the number of courses
 * of a particular requirement type that are required to be taken.
 */
public class ReqInputPanel extends JPanel {
    private JTextField reqCountInput = new JTextField();
    private JLabel reqCountLabel;
    private String label;

    /*
     * Create a ReqInputPanel; "label" is a particular requirement type
     */
    public ReqInputPanel(String label) {
        this.label = label;
        reqCountLabel = new JLabel("Number of courses needed to fulfill " + label + " requirement ('All' or number):");
        this.setMinimumSize(new Dimension(800, 25));
        this.setMaximumSize(new Dimension(2600, 26));
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        reqCountInput.setMinimumSize(new Dimension(300, 20));
        reqCountInput.setMaximumSize(new Dimension(2000, 21));

        this.add(reqCountLabel);
        this.add(reqCountInput);
        this.add(Box.createRigidArea(new Dimension(10,0)));
        this.add(Box.createGlue());
    }

    /*
     * Accessors
     */
    public String getReqCount() {
        return reqCountInput.getText();
    }

    public String getLabel() {
        return this.label;
    }
}
