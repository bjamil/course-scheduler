package regenerate_ver01;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.ArrayList;
import java.awt.Dimension;
/**
 *
 * @author Beenish Jamil, Emily Vorek
 * @Class Description A dropdown menu that enables the user to select a degree program for use
 * in schedule generation.  Hides a JFileChooser and is displayed in the initial screen.
 */
public class DegreeList extends JComboBox {
    private JFileChooser fc;
    private ArrayList<String> fileNames;

    /*
     * Creates a DegreeList; do not allow any calling methods to modify the internal details of this object.
     */
    public DegreeList() {

        //The file chooser does not ever pop up; it just facilitates access to all .schd files in the proper directory.
        fc = new JFileChooser();
        fileNames = new ArrayList<String>();
        this.setMinimumSize(new Dimension(200,25));
        this.setMaximumSize(new Dimension(201,26));

        //Ensures that only .schd files are displayed in the dropdown.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SCHD Files", "schd");
        fc.setFileFilter(filter);

        //Assumes that all .schd files are stored in the current working directory;
        //gets a list of the files with this extension.
        File[] files = fc.getFileSystemView().getFiles(new File("./"), false);

        //Display the names of these files in the dropdown menu, without the .schd extension.
        //Because of the specifics of how the degree program is created,
        //the file name is the name of the degree program plus a .schd extension.
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(".schd") && !fileNames.contains(files[i].getName())) {
                fileNames.add(files[i].getName().replace(".schd", ""));
            }
        }

        for (int i = 0; i < fileNames.size(); i++) {
            this.addItem(fileNames.get(i));
        }
    }
}