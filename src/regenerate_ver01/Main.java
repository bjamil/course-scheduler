package regenerate_ver01;

import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:
 * @Class:   CS490
 * @Project: Course Scheduler
 *
 *
 * @Class Description:
 *			This is the main driver class.
 *
 */
public class Main extends JApplet implements ActionListener {
    
    //TODO : Only keep valid course alternatives in drop down panels
    //TODO : Add action listeners to course dropdown menus


    //Attributes that are referenced throughout the class

    private ArrayList<CourseDropdown> courseBoxes;
    private Schedule_CLP schedule;
    private Degree degree;
    private Graphics g;
    private ArrayList<Course> courses;
    private JButton submitChanges, submitDPSize, login, generateNew, submit;
    private JTextField usernameInput, passwordInput, nameInput, sizeInput;
    private JScrollPane sp = new JScrollPane();
    private ArrayList<JCheckBox> boxList;
    private ArrayList<JPanel> mainPanels;
    private ArrayList<CoursePanel> coursePanels;
    private ArrayList<JPanel> dropdownPanels;
    private ArrayList<ReqInputPanel> reqInputList = new ArrayList<ReqInputPanel>();
    private ArrayList<String> reqTypes = new ArrayList<String>();
    private ArrayList<RequirementPanel> rPanels = new ArrayList<RequirementPanel>();
    private static JPanel invisPanel = new JPanel();
    private int coursesInProgram;
    private String degreeProgramName = "";
    private final int COURSE_PANEL_MIN_HEIGHT = 25;
    private final int COURSE_PANEL_MIN_WIDTH = 800;
    private final int COURSE_PANEL_MAX_HEIGHT = 26;
    private final int COURSE_PANEL_MAX_WIDTH = 2500;
    private JButton createProgram;
    private DegreeList dl;
    private final String P_TEST = " Placement Test";
    boolean isFailed = false;
    ArrayList<String> courseIDs;


    //Starts up the applet and displays the initial screen.
    public Main() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                chooseDegreeProgram();
            }
        });
    }

    //Since this is an applet, the init() method, not the main() method, actually starts the program.
    public void init() {
        new Main();
    }

    /*
     * Shows an error message detailing the exact problem.  Can be called from anywhere in the program.
     * invisPanel is simply a dummy/unused panel that is used as a parent for the dialog box.
     */
    public static void showAlert(String s) {
        JOptionPane.showMessageDialog(invisPanel, s);
    }

    /*
     * Displays the initial screen, which prompts the user to select a degree program from a dropdown menu.
     */
    public void chooseDegreeProgram() {
        dl = new DegreeList(); //dropdown menu containing a list of degree programs

        //configure and set up the window
        JPanel panel = new JPanel();

        JLabel label = new JLabel("Please select a degree program: ");

        panel.setMinimumSize(new Dimension(200, 75));
        panel.setMaximumSize(new Dimension(200, 76));

        JPanel buttonPanel = new JPanel();
        login = new JButton("Log In");
        login.addActionListener(this);

        JButton getDegreeProgram = new JButton("Select Degree Program");
        getDegreeProgram.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(label).addComponent(getDegreeProgram)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(login).addComponent(dl, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(60, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(label).addComponent(dl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(getDegreeProgram).addComponent(login)).addContainerGap(228, Short.MAX_VALUE)));

        //use a JScrollPane to enable a scrollbar and keep information from getting cut off
        sp.setViewportView(panel);
        setContentPane(sp);
    }

    /*
     * Displays the checklist that enables the user to select the courses that they have taken
     * and, when applicable, wish to take.
     */
    public void showOrderedCheckList(String fileName) {
        g = getContentPane().getGraphics();
//        ArrayList<JPanel> panelList = new ArrayList<JPanel>();

        //create a Degree object so that the degree program information from the file can be accessed
        this.degree = ParseFile.getDegreeInfo(fileName);
        Map<String, Course> catalog = degree.getCourseDictionary();
        courses = new ArrayList<Course>();
        courses.addAll(catalog.values());
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        boxList = new ArrayList<JCheckBox>();

		// sort all courses by requirement type
        ArrayList<ArrayList<Course>> orderedCat = degree.getOrderedCatalog();
        Map<String, Integer> typeNum = degree.getReqTypeNums();

//        ArrayList<JPanel> panels = new ArrayList<JPanel>();

        for (ArrayList<Course> req : orderedCat) {
            JPanel panel = new JPanel();
            GridLayout layout = new GridLayout();
            panel.setLayout(layout);
            layout.setColumns(1);
            layout.setRows(req.size() + 2);
            String type = req.get(0).getType();

            if (type != null && !type.equals("CREDITS")) {
				// create a panel for this requirement and add it to the gui
                int numReq = typeNum.get(req.get(0).getType());
                RequirementPanel rp = new RequirementPanel(type, numReq, req);

                rPanels.add(rp);
                main.add(Box.createRigidArea(new Dimension(0, 20)));
                main.add(rp.getPanel());
            }
        }

        //make sure the Submit Changes button is displayed in the proper place
        submitChanges = new JButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        submitChanges.setText("Submit Changes");
        submitChanges.addActionListener(this);
        buttonPanel.add(submitChanges);
        buttonPanel.add(Box.createHorizontalStrut(10));
        main.add(buttonPanel);

        sp.setViewportView(main);
        setContentPane(sp);
    }

    /*
     * Responsible for creating a new initial schedule given a list of taken courses,
     * as well as a list of courses that the user wishes to take.
     */
    public void configure(ArrayList<String> taken, ArrayList<String> selected) {
        //Step 0: Ensure that a degree program has been initialized
		//		  (this should be a redundant check)
		if(degree == null){
			System.out.println("Error: Degree program not initialized!");
		}else{
			//step 1: Get set of catalog that the schedule will use
			//		  All pointers in this catalog are initialized and
			//		  all pointers point to items within the catalog ONLY
			Map<String, Course> catalog = degree.getCourseDictionary();

			
			// step 2: Initialize a new schedule and generate a sample schedule
			//		   that includes selected courses and excludes taken courses
			this.schedule = new Schedule_CLP(catalog, taken, selected);
			schedule.genNewSchedule();

			// step 3: display generated schedule
			displayGui(courses, schedule);
		}
    }

    
    /*
	 * TODO : THIS IS NOT NECESSARY -- Only include alternatives in drop downs !!
	 *
     * Sets up a dropdown menu containing a list of all the courses in the degree program,
     * which allows the user to modify the schedule.
     */
    private CourseDropdown createDropDown(ArrayList<Course> courseList) {
        CourseDropdown box = new CourseDropdown();
        box.getMoveButton().addActionListener(this);
        box.setMinimumSize(new Dimension(100, 20));
        box.setMaximumSize(new Dimension(101, 21));
        for (Course c : courseList) {
            box.addItem(c.getCourseID());
        }
        box.setEnabled(false);
        return box;
    }

    /*
     * Initial panel that is displayed after the user logs in to the degree program creation area.
     * Prompts the user for the name of the degree program and the number of courses in the program.
     * The name of the degree program plus a .schd extension will become the name of the degree
     * program file, and the number of courses in the program determines
     * the number of sets of input text fields (each corresponding to a new course)
     * that will be displayed in the degree program creation area.
     */
    public void showEntryPanel() {

        //create and set up the panel
        JPanel input = new JPanel();
        JLabel programName = new JLabel("Please enter a name for the degree program: ");
        JLabel howMany = new JLabel("How many courses are in this degree program?: ");

        nameInput = new JTextField();
        sizeInput = new JTextField();

        submitDPSize = new JButton("Submit");
        submitDPSize.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(input);
        input.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addGroup(layout.createSequentialGroup().addComponent(programName).addGap(26, 26, 26).addComponent(nameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 103,
                javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(howMany).addGap(18, 18, 18).addComponent(sizeInput)).addComponent(submitDPSize)).addContainerGap(56, Short.MAX_VALUE)));


        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(programName).addComponent(nameInput, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(howMany).addComponent(sizeInput, javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(submitDPSize).addContainerGap(209, Short.MAX_VALUE)));

        sp.setViewportView(input);
    }

    /*
     * Sets up an individual CoursePanel to be displayed in the degree program creation area.
     * One CoursePanel corresponds to one new course to be added to the degree program file.
     */
    public CoursePanel createCoursePanel(int index) {
        CoursePanel coursePanel = new CoursePanel(index);
        coursePanel.setMinimumSize(new Dimension(COURSE_PANEL_MIN_WIDTH, COURSE_PANEL_MIN_HEIGHT));
        coursePanel.setMaximumSize(new Dimension(COURSE_PANEL_MAX_WIDTH, COURSE_PANEL_MAX_HEIGHT));
        coursePanel.getAddCoreq().addActionListener(this);
        coursePanel.getAddPrereq().addActionListener(this);
        return coursePanel;
    }

    /*
     * Displays the main degree program creation screen, which contains the correct number of CoursePanels,
     * each of which corresponds to a new course in the degree program.
     * programName will become the degree program's file name.
     * size is the number of courses in the program.
     */
    public void createDegreeProgram(String programName, int size) {
        coursePanels = new ArrayList<CoursePanel>();

        //add the correct number of CoursePanels to the panel, and associate the correct index with each
        for (int i = 0; i < size; i++) {
            coursePanels.add(createCoursePanel(i));
        }

        //configure and set up the window
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        for (int i = 0; i < coursePanels.size(); i++) {
            main.add(coursePanels.get(i));
            main.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        createProgram = new JButton("Next");
        createProgram.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setMinimumSize(new Dimension(COURSE_PANEL_MIN_WIDTH, COURSE_PANEL_MIN_HEIGHT));
        buttonPanel.setMaximumSize(new Dimension(COURSE_PANEL_MAX_WIDTH, COURSE_PANEL_MAX_HEIGHT));
        buttonPanel.add(createProgram);
        buttonPanel.add(Box.createGlue());

        main.add(buttonPanel);
        //guidance on how to use this
        JTextArea instructions = new JTextArea("Instructions: \n 1. Enter the course ID of the selected"
                + "course, with no spaces between the prefix and the course number(for example, \"MATH101\"."
                + "\n2.  Enter the number of credits the course has.  "
                + "\n3.  Enter the semester(s) the course should be taken in, with multiple semesters"
                + " separated by a comma.  If unknown, enter 0."
                + "\n4.  Enter the priority of the course; 1 is highest, 9 is lowest.  If unknown, enter 0."
                + "\n5.  Add any prerequisites that the course has.  If the prerequisite is the completion"
                + " of a certain number of credits, select \"Credits\" from the menu, and enter a number "
                + "\n in the text field.  If it is a course, select \"Course\" from the menu, and enter a course"
                + "ID in the field."
                + "\n6.  Add any corequisites that the course has.  If the corequisite is the completion"
                + "of a certain number of credits, select \"Credits\" from the menu, and enter a number "
                + "\nin the text field.  If it is a course, select \"Course\" from the menu, and enter a course"
                + "ID in the field.  \nIf it is the fulfillment of a particular requirement, select"
                + "\"Requirement\" from the menu, and enter the name of the requirement in the text field."
                + "\n7.  Indicate whether or not a placement test is required for this course using the menu."
                + "\n8.  Click \"Next\".");

        main.add(instructions);

        sp.setViewportView(main);
    }

    /*
     * Properly displays the generated schedule, with all courses grouped by semester.
	 *
	 * Note that this hard codes 8 semesters into the schedule
     */
    public void displayGui(ArrayList<Course> courseList, Schedule_CLP schedule) {

        //configure the main panel and all component panels, which correspond to a semester
        g = getContentPane().getGraphics();
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(5, 2));
        mainPanels = new ArrayList<JPanel>();
        dropdownPanels = new ArrayList<JPanel>();

        JPanel panel1 = new JPanel();
        JLabel semester1 = new JLabel("SEMESTER: 1           TOTAL CREDITS: " + schedule.getSemesters().get(0).getCredits());
        JPanel sem1Panel = new JPanel();
        sem1Panel.setLayout(new BoxLayout(sem1Panel, BoxLayout.X_AXIS));
        sem1Panel.add(semester1);
        sem1Panel.add(Box.createHorizontalGlue());
        panel1.add(sem1Panel);
        dropdownPanels.add(panel1);
        mainPanels.add(dropdownPanels.get(0));

        JPanel panel2 = new JPanel();
        JLabel semester2 = new JLabel("SEMESTER: 2           TOTAL CREDITS: " + schedule.getSemesters().get(1).getCredits());
        JPanel sem2Panel = new JPanel();
        sem2Panel.setLayout(new BoxLayout(sem2Panel, BoxLayout.X_AXIS));
        sem2Panel.add(semester2);
        sem2Panel.add(Box.createHorizontalGlue());
        panel2.add(sem2Panel);
        dropdownPanels.add(panel2);
        mainPanels.add(dropdownPanels.get(1));

        JPanel panel3 = new JPanel();
        JLabel semester3 = new JLabel("SEMESTER: 3           TOTAL CREDITS: " + schedule.getSemesters().get(2).getCredits());
        JPanel sem3Panel = new JPanel();
        sem3Panel.setLayout(new BoxLayout(sem3Panel, BoxLayout.X_AXIS));
        sem3Panel.add(semester3);
        sem3Panel.add(Box.createHorizontalGlue());
        panel3.add(sem3Panel);
        dropdownPanels.add(panel3);
        mainPanels.add(dropdownPanels.get(2));

        JPanel panel4 = new JPanel();
        JLabel semester4 = new JLabel("SEMESTER: 4           TOTAL CREDITS: " + schedule.getSemesters().get(3).getCredits());
        JPanel sem4Panel = new JPanel();
        sem4Panel.setLayout(new BoxLayout(sem4Panel, BoxLayout.X_AXIS));
        sem4Panel.add(semester4);
        sem4Panel.add(Box.createHorizontalGlue());
        panel4.add(sem4Panel);
        dropdownPanels.add(panel4);
        mainPanels.add(dropdownPanels.get(3));

        JPanel panel5 = new JPanel();
        JLabel semester5 = new JLabel("SEMESTER: 5           TOTAL CREDITS: " + schedule.getSemesters().get(4).getCredits());
        JPanel sem5Panel = new JPanel();
        sem5Panel.setLayout(new BoxLayout(sem5Panel, BoxLayout.X_AXIS));
        sem5Panel.add(semester5);
        sem5Panel.add(Box.createHorizontalGlue());
        panel5.add(sem5Panel);
        dropdownPanels.add(panel5);
        mainPanels.add(dropdownPanels.get(4));

        JPanel panel6 = new JPanel();
        JLabel semester6 = new JLabel("SEMESTER: 6           TOTAL CREDITS: " + schedule.getSemesters().get(5).getCredits());
        JPanel sem6Panel = new JPanel();
        sem6Panel.setLayout(new BoxLayout(sem6Panel, BoxLayout.X_AXIS));
        sem6Panel.add(semester6);
        sem6Panel.add(Box.createHorizontalGlue());
        panel6.add(sem6Panel);
        dropdownPanels.add(panel6);
        mainPanels.add(dropdownPanels.get(5));

        JPanel panel7 = new JPanel();
        JLabel semester7 = new JLabel("SEMESTER: 7           TOTAL CREDITS: " + schedule.getSemesters().get(6).getCredits());
        JPanel sem7Panel = new JPanel();
        sem7Panel.setLayout(new BoxLayout(sem7Panel, BoxLayout.X_AXIS));
        sem7Panel.add(semester7);
        sem7Panel.add(Box.createHorizontalGlue());
        panel7.add(sem7Panel);
        dropdownPanels.add(panel7);
        mainPanels.add(dropdownPanels.get(6));

        JPanel panel8 = new JPanel();
        JLabel semester8 = new JLabel("SEMESTER: 8           TOTAL CREDITS: " + schedule.getSemesters().get(7).getCredits());
        JPanel sem8Panel = new JPanel();
        sem8Panel.setLayout(new BoxLayout(sem8Panel, BoxLayout.X_AXIS));
        sem8Panel.add(semester8);
        sem8Panel.add(Box.createHorizontalGlue());
        panel8.add(sem8Panel);
        dropdownPanels.add(panel8);
        mainPanels.add(dropdownPanels.get(7));

        //set the layout of all component/semester panels
        for (JPanel p : mainPanels) {
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        }

        for (JPanel p : dropdownPanels) {
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        }

        //all of the CourseDropdowns associated with a semester
        courseBoxes = new ArrayList<CourseDropdown>();

        for (int i = 0; i < 8; i++) { //semesters

            //go through all of the courses in this semester and set the selected item of each
            //CourseDropdown in this semester panel to the correct course
            ArrayList<JPanel> panelList = new ArrayList<JPanel>();
            for (Course course : schedule.getSemesters().get(i).getCourses()) { //all courses in sem
                CourseDropdown box = createDropDown(courseList);
                courseBoxes.add(box);

                for (int j = 0; j <= schedule.getSemesters().get(i).getCourses().size(); j++) { //
                    panelList.add(new JPanel());
                }
                for (JPanel p : panelList) {
                    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                    box.setSelectedItem(course.getCourseID());
                    box.setSelectedCourse(course.getCourseID());
                    box.setSemester(schedule.getSemesters().get(i));
                    p.add(box);
                    p.add(Box.createHorizontalStrut(10));
                    p.add(box.getMoveButton());
                    p.add(box.getLockBox());
                    p.add(Box.createHorizontalGlue());
                }

                //add the CourseDropdown panels to the main panel
                for (JPanel p : panelList) {
                    dropdownPanels.get(i).add(p);
                }
            }

            if (schedule.getSemesters().get(i).getCredits() < 18) {
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            }
            panel.add(mainPanels.get(i));
        }

        //finish configuring the panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
//        generateNew = new JButton("Generate New Schedule");
        JButton exit = new JButton("Exit");
        exit.addActionListener(this);
//        generateNew.addActionListener(this);
//        buttonPanel.add(generateNew);
//        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
//        buttonPanel.add(Box.createGlue());

//        panel.add(buttonPanel);

        //dummy panel that is not actually used; only used by the JDialogBox that displays error messages
        panel.add(invisPanel);

        sp.setViewportView(panel);
    }

    /*
     * Redraws all components in the applet window.
     */
    public void paint(Graphics g) {
        getContentPane().paintAll(g);
    }

    /*
     * Event listener for all applicable items in this class.
     */
    public void actionPerformed(ActionEvent e) {

        //the following attributes are used by multiple functionalities

        ArrayList<String> progCourses = new ArrayList<String>();
        ArrayList<String> removed = new ArrayList<String>();
        HashMap<String, Integer> reqTypeCount = new HashMap<String, Integer>();
//        ArrayList<String> courseIDs = new ArrayList<String>();


        //displays the degree program checklist for the proper degree program
        if (((JButton) e.getSource()).getText().equals("Select Degree Program")) {
            showOrderedCheckList(dl.getSelectedItem() + ".schd");
        }

        //takes the user back to the initial screen
        if (((JButton) e.getSource()).getText().equals("Exit")) {
            chooseDegreeProgram();
        }

        //in the degree program creation area, this displays the screen
        //asking the user how many courses of each requirement type need to be taken
        if (((JButton) e.getSource()).getText().equals("Next")) {
            System.out.println("You clicked Next");
            JPanel panel = new JPanel();

            //for all new courses in the schedule, get the requirement type of each
            for (CoursePanel cp : coursePanels) {
                String reqType = cp.getReqType().getText();
                if (!reqTypes.contains(reqType)) { //make sure no duplicate requirement types are in the list
                    reqTypes.add(reqType);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                    //list of all newly created courses; used for cross-checking to ensure that a prereq
                    //or coreq course actually exists in the program
                    courseIDs.add(cp.getCourseIDField().getText());
                }
            }

            //go through all courses in the new program and ensure that all prereqs/coreqs actually exist
            for (String courseID : courseIDs) {
                for (int i = 0; i < coursePanels.size(); i++) {
                    for (int j = 0; j < coursePanels.get(i).getPrereqs().size(); j++) {

                        //the prereq/coreq course does not exist, so show an error
                        if (!courseIDs.contains(coursePanels.get(i).getPrereqPanels().get(j).getPrereqName()) && coursePanels.get(i).getPrereqPanels().get(j).getPrereqType().equals("Course")) {
                            showAlert("You have not added " + coursePanels.get(i).getPrereqPanels().get(j).getPrereqName() + " to the degree program.");
                            isFailed = true;
                            System.out.println("failed");
                            break;
                        } else {
                            System.out.println("passed");
                            isFailed = false;
                        }
                    }
                }
                if (isFailed) {
                    break;
                }
            }

            //performs similar crosschecking for coreq courses
            for (String courseID : courseIDs) {
                for (int i = 0; i < coursePanels.size(); i++) {
                    for (int j = 0; j < coursePanels.get(i).getCoreqs().size(); j++) {

                        //the prereq/coreq course does not exist, so show an error
                        if (!courseIDs.contains(coursePanels.get(i).getCoreqPanels().get(j).getCoreqName()) && coursePanels.get(i).getCoreqPanels().get(j).getCoreqType().equals("Course")) {
                            showAlert("You have not added " + coursePanels.get(i).getCoreqPanels().get(j).getCoreqName() + " to the degree program.");
                            isFailed = true;
                            break;
                        } else {
                            isFailed = false;
                        }
                    }
                }
                if (isFailed) {
                    break;
                }
            }

            //sets up the input fields so that the user can indicate how many courses of each requirement type are required

            for (String req : reqTypes) {
                ReqInputPanel rp = new ReqInputPanel(req);
                panel.add(rp);
                reqInputList.add(rp);
            }

            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            if (!isFailed) { //do not display this screen if there is an error with the degree program
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                buttonPanel.setMinimumSize(new Dimension(800, 25));
                buttonPanel.setMaximumSize(new Dimension(2600, 26));
                JButton finish = new JButton("Finish");
                finish.addActionListener(this);
                buttonPanel.add(finish);
                buttonPanel.add(Box.createGlue());
                panel.add(buttonPanel);


                sp.setViewportView(panel);
            }
        }

        //sets up the degree program file from the information obtained from the user
        if (((JButton) e.getSource()).getText().equals("Finish")) {
            File f = new File(degreeProgramName + ".schd");

            //sets up the file, and formats the information properly
            try {
                f.createNewFile();
                FileWriter fw = new FileWriter(f);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(degreeProgramName);
                bw.newLine();
                bw.write("! REQUIREMENTS");
                bw.newLine();

                //contains a formatted list of all the courses to be put into the degree program
                ArrayList<CourseToWrite> coursesToWrite = new ArrayList<CourseToWrite>();
                String progTitle = degreeProgramName;
                for (CoursePanel cp : coursePanels) {
                    String reqType = cp.getReqType().getText(); //req type of the course

                    for (ReqInputPanel rp : reqInputList) {
                        System.out.println("in rp");
                        String req = rp.getLabel();
                        String count = rp.getReqCount();

                        //indicates how many courses of this requirement type are required
                        if (count.trim().equalsIgnoreCase("All")) {
                            //0 means all courses are required
                            reqTypeCount.put(req, 0);
                        } else {
                            //otherwise put in the count of required courses
                            if (count.equals("")) {
                                reqTypeCount.put(req, 0);
                            } else {
                                reqTypeCount.put(req, Integer.parseInt(count));
                            }

                            }
                        }
                    

                    //attributes of the course to write to the file
                    String courseID = cp.getCourseIDField().getText();
                    String numCredits = cp.getNumCreditsField().getText();
                    String priority = cp.getPriorityField().getText();
                    String placementTestRequired = cp.getPlacementTest();
                    progCourses.add(courseID);
                    String semesters = cp.getSemestersField().getText();

                    //create a CourseToWrite information so that this information will be properly formatted
                    CourseToWrite cw = new CourseToWrite(courseID, semesters, reqType, numCredits, priority, placementTestRequired);

                    //add all specified prereqs to this course
                    for (int i = 0; i < cp.getPrereqPanels().size(); i++) {
                        cw.addPrereq(cp.getPrereqPanels().get(i));
                    }

                    //add all specified coreqs to this course
                    for (int i = 0; i < cp.getCoreqPanels().size(); i++) {
                        cw.addCoreq(cp.getCoreqPanels().get(i));
                    }
                    coursesToWrite.add(cw);
                }

                //writes each requirement type along with its count to the file
                for (String rt : reqTypes) {
                    bw.write(rt + " " + reqTypeCount.get(rt));
                    bw.newLine();
                }


                bw.write("! OFFERED");
                bw.newLine();

                //sort the courses by requirement type
                for (int i = 0; i < reqTypes.size(); i++) {
                    String reqType = reqTypes.get(i);
                    bw.write("## " + reqType + " 0"); //minimum priority is 0
                    bw.newLine();
                    for (int j = 0; j < coursesToWrite.size(); j++) {
                        if (reqType.equals(coursesToWrite.get(j).getReqType())
                                && !coursesToWrite.get(j).isWritten()) {

                            //write this course to the file (if it hasn't been already) under its requirement type
                            bw.write(coursesToWrite.get(j).toString());
                            bw.newLine();
                            coursesToWrite.get(j).setWritten(true);
                        }
                    }
                }
                //we are done-get rid of the FileWriter
                bw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            chooseDegreeProgram();
        }

        //adds a PrereqPanel to the correct CoursePanel if a PrereqButton has been clicked
        if (e.getSource() instanceof PrereqButton) {

            //figure out which panel is associated with this prereq button
            CoursePanel cp = coursePanels.get(((PrereqButton) e.getSource()).getIndex());
            cp.add(Box.createVerticalGlue());
            PrereqPanel prereqPanel = new PrereqPanel();

            //enlarge the CoursePanel so the PrereqPanel can be added
            cp.expandPanel();
            cp.add(prereqPanel);
            cp.add(Box.createHorizontalGlue());
            cp.addPrereqToList(prereqPanel);

            getContentPane().paintAll(getContentPane().getGraphics());
        }

        //adds a coreq to the correct Course Panel if the Add Coreq button has been clicked
        if (e.getSource() instanceof CoreqButton) {

            //figure out which panel is associated with this coreq button
            CoursePanel cp = coursePanels.get(((CoreqButton) e.getSource()).getIndex());
            cp.add(Box.createVerticalGlue());
            CoreqPanel coreqPanel = new CoreqPanel();

            //enlarge the CoursePanel so the CoreqPanel can be added
            cp.expandPanel();
            cp.add(coreqPanel);
            cp.addCoreqToList(coreqPanel);
            getContentPane().paintAll(getContentPane().getGraphics());
        }

        //get the information about the degree program name and the number of courses in this degree program
        //so that we know how many CoursePanels are needed
        if (e.getSource().equals(submitDPSize)) {
            degreeProgramName = nameInput.getText();
            coursesInProgram = Integer.parseInt(sizeInput.getText());
            createDegreeProgram(degreeProgramName, coursesInProgram);

        }

        //user wants to create a new degree program - required to log in first
        if (e.getSource().equals(login)) {
            JPanel loginPanel = new JPanel();

            //for later use - the list of all courseIDs contained in the degree program
            courseIDs = new ArrayList<String>();
            //for the purposes of this app, username will be "test" and password will be "test"
            JLabel usernameLabel = new JLabel("Username: ");
            JLabel passwordLabel = new JLabel("Password: ");
            usernameInput = new JTextField();
            passwordInput = new JPasswordField();

            //panel setup/configuration
            submit = new JButton("Log In!");
            submit.addActionListener(this);
            submit.setMinimumSize(new Dimension(125, 20));
            submit.setMaximumSize(new Dimension(126, 21));

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(loginPanel);
            loginPanel.setLayout(layout);
            layout.setAutoCreateGaps(false);
            layout.setAutoCreateContainerGaps(false);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup().addComponent(usernameLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(usernameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 65,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup()
                    .addComponent(passwordLabel).addPreferredGap(javax.swing.LayoutStyle
                    .ComponentPlacement.UNRELATED).addComponent(passwordInput)).addComponent(submit))
                    .addContainerGap(266, Short.MAX_VALUE)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup().addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel).addComponent(usernameInput,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle
                    .ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout
                    .Alignment.LEADING).addComponent(passwordLabel).addComponent(passwordInput,
                    javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addComponent(submit)
                    .addContainerGap(202, Short.MAX_VALUE)));


            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.add(submit);
            buttonPanel.add(Box.createGlue());
            loginPanel.add(buttonPanel);

            sp.setViewportView(loginPanel);
        }

        //get login credentials from the user and see if they match
        if (e.getSource().equals(submit)) {
            String username = "test";
            String password = "test";
            String usernameEntered = usernameInput.getText();
            String passwordEntered = passwordInput.getText();

            //if the correct username/password were entered, prompt the user for the name of
            //the degree program and the number of courses in the degree program
            if (usernameEntered.equals(username) && passwordEntered.equals(password)) {
                showEntryPanel();
            }
        }

        // move course!!!
        if (e.getSource() instanceof MoveButton) {
            MoveButton moveButton = (MoveButton) e.getSource();
            String courseID = moveButton.getCourseID();
            int[] possibilities = schedule.getPossibleSemesters(courseID);
            Object[] p = new Object[possibilities.length];
            for (int i = 0; i < possibilities.length; i++) {
                p[i] = (Object) possibilities[i];
            }

            // get new semester
            Integer newSem = (Integer) JOptionPane.showInputDialog(
                    this,
                    "Please select a new semester for "
                    + courseID + " out\n "
                    + "of the following possible semesters\n "
                    + "for this course: ",
                    "Customized Dialog",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    p,
                    "" + moveButton.getSemester());
            

			// if cancel isn't pressed, then attempt to move the course
			if(newSem != null){
				ArrayList modified = new ArrayList<String>();
				ArrayList locked = new ArrayList<String>();
				modified.add(courseID);
				for (CourseDropdown cd : this.courseBoxes) {
					if (cd.getLockBox().isSelected()) {
						locked.add(cd.getSelectedCourse());
					}
				}

				if (schedule.isMovePossible(courseID, newSem, locked)) {
					schedule.moveCourse(courseID, newSem);
					if(!schedule.refreshSchedule(locked, modified)){
						JOptionPane.showMessageDialog(this, "Sorry, a valid schedule "
								+ "couldn't be found. ");
					}
					displayGui(courses, schedule);
				} else {
					System.out.println("NOT POSSIBLE");
					JOptionPane.showMessageDialog(this, "Sorry, this move is not possible\n"
							+ "There might be too many locked courses in\n"
							+ "the destination semester");
				}
			}
		}

        if (e.getSource().equals(submitChanges)) {
            System.out.println("---------------------\nReading in checklist information\n");
            ArrayList<Course> notTaken = new ArrayList<Course>();
            ArrayList<String> taken = new ArrayList<String>();
            ArrayList<String> selected = new ArrayList<String>();

            for (RequirementPanel rp : this.rPanels) {
                if (rp.isValid()) {
                    for (String cID : rp.getTakenCourses()) {
                        taken.add(cID);
                    }
                    selected.addAll(rp.getSelectedCourses());

                } else {
                    JOptionPane.showMessageDialog(this, "Please select at least "
                            + rp.getNumReq() + " " + rp.getType()
                            + " \n courses that you have taken or \nwish to take.");
                    return;
                }
            }

            configure(taken, selected);
        }
		/*
		 * Ignore the next part for now. Add this button and action listener again
		 * when alternative courses are added to course drop downs. This ensures that
		 * required courses are never removed from the schedule by the user accidentally.
		 *
		*/

//			else if (e.getSource().equals(generateNew)) {
//            ArrayList<String> modifiedUnlocked = new ArrayList<String>();
//            HashMap<String, Integer> locked = new HashMap<String, Integer>();
//            ArrayList<String> lockedIDs = new ArrayList<String>();
//            ArrayList<String> swapped = new ArrayList<String>();
//            ArrayList<String> alreadyBeenSwapped = new ArrayList<String>();
//            ArrayList<String> unchanged = new ArrayList<String>();
//            System.out.println("--------------------\nGenerating new schedule\n");
//
//            for (CourseDropdown dropDown : courseBoxes) {
//                //has a change been made to the dropdown?
//                if (!dropDown.getSelectedItem().equals(dropDown.getSelectedCourse())) {
//                    swapped.add((String) dropDown.getSelectedItem());
//                    swapped.add(dropDown.getSelectedCourse());
//                    dropDown.setSelectedCourse((String) dropDown.getSelectedItem());
//                    //course IDs of locked courses
//                    if (dropDown.getLockBox().isSelected()) {
//                        lockedIDs.add((String) dropDown.getSelectedItem());
//                    }
//                }
//
//
//                //if dropDown.getSelectedItem() is the same as the selected course, the dropdown was not modified
//                if (dropDown.getSelectedItem().equals(dropDown.getSelectedCourse())) {
//                    unchanged.add(dropDown.getSelectedCourse());
//                }
//            }
//
//            //make sure the courses have not been already swapped; first, move locked courses
//            for (int i = 0; i < swapped.size(); i += 2) {
//                if (!alreadyBeenSwapped.contains(swapped.get(i)) && !alreadyBeenSwapped.contains(swapped.get(i + 1))) {
//                    if (lockedIDs.contains(swapped.get(i))) {
//                        schedule.forceSwap(swapped.get(i), swapped.get(i + 1));
//                        alreadyBeenSwapped.add(swapped.get(i));
//                        alreadyBeenSwapped.add(swapped.get(i + 1));
//                    } else {
//                        //if the schedule is impossible, modify these courses--these are the courses selected by the
//                        //user, but unlocked
//                        modifiedUnlocked.add(swapped.get(i));
//                        modifiedUnlocked.add(swapped.get(i + 1));
//                    }
//                }
//            }
//
//            //deal with unlocked courses later
//            for (int i = 0; i < swapped.size(); i += 2) {
//                if (!alreadyBeenSwapped.contains(swapped.get(i)) && !alreadyBeenSwapped.contains(swapped.get(i + 1))) {
//                    if (!lockedIDs.contains(swapped.get(i))) {
//                        schedule.forceSwap(swapped.get(i), swapped.get(i + 1));
//                        alreadyBeenSwapped.add(swapped.get(i));
//                        alreadyBeenSwapped.add(swapped.get(i + 1));
//                    }
//                }
//            }
//
//            System.out.println("\n......\nREFRESHING Schedule");
//            boolean success = schedule.refreshSchedule(lockedIDs, modifiedUnlocked);
//            displayGui(courses, schedule);
//
//            if (!removed.isEmpty()) {
//                schedule.removeCourses(removed);
//            }
//
////            System.out.println(schedule.toString());
//            System.out.println("Requirements met? " + degree.requirementsMet(schedule));
//
//            displayGui(courses, schedule);
//        }
    }
}
