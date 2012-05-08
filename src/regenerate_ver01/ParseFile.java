package regenerate_ver01;

//TODO: ADD CHECKS ON WHETHER ALL PREREQS MENTIONED ARE IN FILE !!!!
//		Add option of a requirement being a prerequisite

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:
 * @Class:	 CS 490
 * @Project: Course Scheduling Tool
 *
 *
 * @Class Description: The static getDegreeInfo method of this class is able to
 *						parse a valid degree info file and return a Degree object
 *						containing all of the information in that file to the
 *						calling method
 */
public class ParseFile {

    // Constants :
    // symbols :
    private static String SECTION_HEAD_PREFIX = "!";
    private static String COURSE_TYPE_PREFIX = "##";
    // Understanding requirements:
    private static int REQUIREMENT_TYPE = 0;
    private static int NUM_REQUIRED = 1;		// if val[num_req] = 0, then all are required
    // Breakdown of course data
    private static int COURSEID = 0;
    private static int CREDITS = 1;
    private static int SEMESTER = 2; // what semesters can this course be in
    private static int PRIORITY = 3;
    private static int NUMCOREQS = 4;
    private static int NUMPREREQS = 5;

	// offset from the end
	private static int PLACEMENT_TEST = 0; // is a placement test present? (0 = no; 1 = yes)

    private static HashMap<String, Course> courseDictionary;

	// Understanding prerequisites:
    // data[prereq] >= 0 --> # of prerequisites
    private static int PREREQ_CREDITS = -1;		// if -1, the specified number of credit hours are required
    private static String PREREQ_SEPARATED = ","; // this means that multiple prerequisite types exist
    // understanding corequisites:
    // data[coreq] >= 0 --> # of corequisites
    private static int COREQ_CREDITS = -1;		// if -1, the specified number of credit hours are required
    private static int COREQ_REQUIREMENT = -2; // if -2, the corequisite is the completion of a requirement/course-type
    // (set of courses) rather than a single course
    private static String COREQ_SEPARATED = ","; // this means that multiple corequisite types exist
    // section headers:
    private static String REQUIREMENTS = "REQUIREMENTS";
    private static int SECNUM_REQ = 1;
    private static String OFFERED = "OFFERED";
    private static int SECNUM_OFF = 2;


    public static Degree getDegreeInfo(String filename) {
        String title = "";
        ArrayList<String> reqNames = new ArrayList<String>();
        ArrayList<Integer> reqNums = new ArrayList<Integer>();
        ArrayList required = new ArrayList();
        ArrayList catalog = new ArrayList();
        ArrayList<Course> courses = new ArrayList<Course>();
        int type = 1; // requirements
        String reqType = "UNKNOWN";		// requirement type
        String courseType = "";
        Map<String, Integer> typeNumMap = new HashMap<String, Integer>();
        int sectionPriority = 0;
        int coursePriority = 0;
        int difficulty = 0;
        courseDictionary = new HashMap<String, Course>(100);
        Random rand = new Random();
        rand.setSeed(0);
        
        try {
            Scanner scan = new Scanner(new File(filename));
            int section = 1;

            // line 0 = title of degree
            title = scan.nextLine();

            while (scan.hasNext()) {


                String[] info = (scan.nextLine()).split(" ");

                if (info[0].equals(SECTION_HEAD_PREFIX)) {
                    // check which section head it is
                    if (info[1].equals(REQUIREMENTS)) {
                        section = SECNUM_REQ;
                    } else {	 // equals offered
                        section = SECNUM_OFF;
                    }
                } else {

                    if (section == SECNUM_REQ) {
                        // this is a requirement
                        reqType = info[REQUIREMENT_TYPE];
                        int numReq = Integer.parseInt(info[NUM_REQUIRED]);

                        reqNames.add(reqType);
                        reqNums.add(numReq);

                        typeNumMap.put(reqType, numReq);
                    } else {
                        // this is a course:
                        if (info[0].equals(COURSE_TYPE_PREFIX)) {
                            // course heading
                            courseType = info[1];
                            sectionPriority = Integer.parseInt(info[2]);
                        } else {
                            // this is course data

                            // This is very inefficient
                            String courseID = info[COURSEID];
                            int credits = Integer.parseInt(info[CREDITS]);

							int pTest = Integer.parseInt(info[info.length -1 - PLACEMENT_TEST]);

                            // must it be taken in a specific semester ?
                            String[] semesterInfo = (info[SEMESTER]).split(",");

                            int[] semesters = new int[semesterInfo.length];
                            for (int i = 0; i < semesterInfo.length; i++) {
                                semesters[i] = Integer.parseInt(semesterInfo[i]);
                            }

                            coursePriority = Integer.parseInt(info[PRIORITY]);

                            // decipher coreqs

                            String[] coreqInfo = (info[NUMCOREQS]).split(",");



                            int coreqTypes = coreqInfo.length;
                            int totalCoreqs = 0;
                            ArrayList<String> coreqs = new ArrayList<String>();

                            for (int i = 0; i < coreqTypes; i++) {
                                int numCoreq = Integer.parseInt(coreqInfo[i]);

                                if (numCoreq == COREQ_CREDITS) {
                                    totalCoreqs += 1;
                                    // this is a credit hour coreq

                                    // add a dummy course for this coreq
                                    int coreqCredits = Integer.parseInt(info[NUMPREREQS + 1 + i]);
                                    String coreqCourseID = "CREDITS" + info[NUMPREREQS + 1 + i];
                                    String coreqType = "CREDITS";
                                    ArrayList<String> coreqPrereq = new ArrayList<String>();
                                    ArrayList<String> coreqCoreq = new ArrayList<String>();
                                    int[] coreqSem = new int[0];

                                    Course course = new Course(coreqCourseID, coreqCredits, 0, coreqSem, coreqType, 0,rand.nextFloat(), coreqPrereq, coreqCoreq);
                                    courses.add(course);
                                    coreqs.add(coreqCourseID);
                                } else {
                                    if (numCoreq == COREQ_REQUIREMENT) {
                                        // a requirement is its coreq
                                        // -- for now, just add the requirement title to coreqs
                                        coreqs.add(info[NUMPREREQS + 1 + i]);
                                        totalCoreqs += 1;
                                    } else {		// it's a course
                                        totalCoreqs += numCoreq;
                                        for (int j = 0; j < numCoreq; j++) {
                                            coreqs.add(info[NUMPREREQS + 1 + i + j]);
//										System.out.println("Adding coreq for coures " + courseID + " : " + info[NUMPREREQS + 1 + i + j]) ;
                                        }
                                    }
                                }
                            }

                            // decipher prereqs
                            String[] prereqInfo = (info[NUMPREREQS]).split(",");

                            int prereqTypes = prereqInfo.length;
                            ArrayList<String> prereqs = new ArrayList<String>();

                            for (int i = 0; i < prereqTypes; i++) {
                                int numPrereq = Integer.parseInt(prereqInfo[i]);
                                if (numPrereq == PREREQ_CREDITS) {
                                    // this is a credit hour prerequisite

                                    // add a dummy course for this prerequisite
                                    int prereqCredits = Integer.parseInt(info[NUMPREREQS + 1 + totalCoreqs + i]);
                                    String prereqCourseID = "CREDITS" + info[NUMPREREQS + 1 + totalCoreqs + i];
                                    String prereqType = "CREDITS";
                                    ArrayList<String> prereqPrereq = new ArrayList<String>();
                                    ArrayList<String> coreqPrereq = new ArrayList<String>();
                                    int[] prereqSem = new int[0];
                                    Course course = new Course(prereqCourseID, prereqCredits, 0, prereqSem, prereqType, 0,rand.nextFloat(), prereqPrereq, coreqPrereq);
                                    courses.add(course);
                                    prereqs.add(prereqCourseID);
                                } else {		// for now, this means numPrereq >= 0

                                    for (int j = 0; j < numPrereq; j++) {
                                        prereqs.add(info[NUMPREREQS + 1 + totalCoreqs + i + j]);
//										System.out.println("Adding prereq " + info[NUMPREREQS + 1 + totalCoreqs + i + j] + " for course: " + courseID);
                                    }
                                }
                            }
                            Course course = new Course(courseID, credits, pTest, semesters, courseType, coursePriority + sectionPriority, rand.nextFloat(), prereqs, coreqs);
                            courseDictionary.put(courseID, course);

                            courses.add(course);
                        }
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        Degree degree = new Degree(title, typeNumMap, courses, courseDictionary);
        return degree;
    }
}
