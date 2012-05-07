package regenerate_ver01;

import java.util.ArrayList;

/**
 *
 * @author Beenish Jamil, Emily Vorek
 * @Class Description Prepares a new course (obtained from the information entered into the degree
 * program creation area) to be written to a degree program file.
 * Handles the formatting of the information obtained from each CoursePanel in the degree program creation
 * area.
 */
public class CourseToWrite {

    private String reqType, courseID, numCredits, priority, semesters, prereqData, coreqData;
    private ArrayList<PrereqPanel> prereqs = new ArrayList<PrereqPanel>(); //associated prereqs for this course
    private ArrayList<CoreqPanel> coreqs = new ArrayList<CoreqPanel>(); //associated coreqs for this course
    private String prereqNamesStr = "";
    private String coreqNamesStr = "";
    private boolean isWritten = false; //indicates whether this course has been written to the file yet (avoids duplicate courses in the same program)
    private String placementTestRequired = "";

    /*
     * Sets up a CourseToWrite with information obtained from the degree program creation area.
     */
    public CourseToWrite(String courseID, String semesters, String reqType, String numCredits, String priority, String placementTestRequired) {
        this.courseID = courseID;
        this.reqType = reqType;
        this.numCredits = numCredits;
        this.priority = priority;
        this.semesters = semesters;
        if (placementTestRequired.equals("Placement Test Not Required")) {
            this.placementTestRequired = "0";
        } else {
            this.placementTestRequired = "1";
        }
    }

    /*
     * Set to true once this course is written to the file.
     */
    public void setWritten(boolean written) {
        this.isWritten = written;
    }

    /*
     * Has the course been written to the file?
     */
    public boolean isWritten() {
        return this.isWritten;
    }

    /*
     * Obtains the information concerning the prereqs for this course,
     * and properly formats this information according to the required syntax for the degree program file.
     */
    public void processPrereqs() {
        //allow information to be added to the strings for the name and types of the prereqs
        StringBuffer type = new StringBuffer("");
        StringBuffer names = new StringBuffer("");
        int numPrereqs = 0;

        //go through the list of prereqs associated with this course
        for (int i = 0; i < prereqs.size(); i++) {

            //if there are no prereqs for this course, set the prereqData to 0 and stop looping.
            if (prereqs.size() == 0) {
                prereqData = "0";
                break;
            } else { //there is at least one prereq for this course

                //"-1" indicates that the prereq type is the completion of a certain number of credits.  Append -1 to the proper location.
                if (prereqs.get(i).getPrereqType().equals("Credits")) {
                    //If this is the first entry in the list of prereq types, do not insert a comma.
                    if (type.toString().equals("")) {
                        type.append("-1");
                    } else { //there is something else before it, so add a comma
                        type.append(",-1");
                    }
                    //if the prereq is a course, simply increment the number of prereqs, no special syntax for this
                } else if (prereqs.get(i).getPrereqType().equals("Course")) {
                    numPrereqs += 1;
                }

                //now process the names of the prereqs, with each one separated by a space
                if (names.toString().equals("")) {
                    names = names.append(prereqs.get(i).getPrereqName());
                } else {
                    names = names.append(" ");
                    names = names.append(prereqs.get(i).getPrereqName());
                }
            }
            //done processing this part - so convert this to a string
            prereqNamesStr = names.toString();
        }

        for (int i = 0; i < prereqs.size(); i++) {
            names.append(prereqs.get(i).getPrereqName() + " ");
        }

        //if there is a special type of prereq, append the number of prereqs to this
        if (!type.toString().equals("")) {
            prereqData = type.toString() + "," + numPrereqs;
        
            //if not, the "prereq data" is just the number of prereqs
        } else {
            prereqData = numPrereqs + "";
        }
    }

    /*
     * Obtains the information concerning the coreqs for this course,
     * and properly formats this information according to the required syntax for the degree program file.
     */
    public void processCoreqs() {
        //allow information to be added to the strings for the name and types of the coreqs
        StringBuffer type = new StringBuffer("");
        StringBuffer names = new StringBuffer("");
        int numCoreqs = 0;

        //go through the list of coreqs associated with this course
        for (int i = 0; i < coreqs.size(); i++) {

            //if there are no coreqs for this course, set coreqData to 0 and stop looping.
            if (coreqs.size() == 0) {
                coreqData = "0";
                break;
            } else { //there is at least one coreq for this course

                //"-1" indicates that the coreq type is completion of a certain number of credits.  Append -1 to the proper location.
                if (coreqs.get(i).getCoreqType().equals("Credits")) {
                    //If this is the first entry in the list of prereq types, do not insert a comma.
                    if (type.toString().equals("")) {
                        type.append("-1");
                    } else { //there is something else before it, so add a comma
                        type.append(",-1");
                    }
                    //similar logic as for Credits; except "-2" means the coreq type is the fulfillment of a requirement
                } else if (coreqs.get(i).getCoreqType().equals("Requirement")) {
                    if (type.toString().equals("")) {
                        type.append("-2");
                    } else {
                        type.append(",-2");
                    }

                    //if the coreq is a course, simply increment the number of coreqs, no special syntax for this
                } else if (coreqs.get(i).getCoreqType().equals("Course")) {
                    numCoreqs += 1;
                }

                //now process the names of the coreqs, with each one separated by a space
                if (names.toString().equals("")) {
                    names = names.append(coreqs.get(i).getCoreqName());
                } else {
                    names = names.append(" ");
                    names = names.append(coreqs.get(i).getCoreqName());
                }
            }

            coreqNamesStr = names.toString();
        }

        for (int i = 0; i < coreqs.size(); i++) {
            names.append(coreqs.get(i).getCoreqName() + " ");
        }

        //if there is a special type of coreq, append the number of coreqs to this
        if (!type.toString().equals("")) {
            coreqData = type.toString() + "," + numCoreqs;
        } else {
            coreqData = numCoreqs + "";
        }
    }

    /*
     * Associates a PrereqPanel with this course to facilitate access to the prereq information.
     */
    public void addPrereq(PrereqPanel prereq) {
        this.prereqs.add(prereq);
    }

    /*
     * Associates a CoreqPanel with this course to facilitate access to the prereq information.
     */
    public void addCoreq(CoreqPanel coreq) {
        this.coreqs.add(coreq);
    }

    /*
     * Accessors
     */
    public String getReqType() {
        return this.reqType;
    }

    public String getCourseID() {
        return this.courseID;
    }

    public String getSemesters() {
        return this.semesters;
    }

    public String getNumCredits() {
        return this.numCredits;
    }

    public String getPriority() {
        return this.priority;
    }

    public String getPlacementTestRequired() {
        return this.placementTestRequired;
    }

    /*
     * The formatted Course string, to be written to the degree program file.
     */
    public String toString() {
        processPrereqs();
        processCoreqs();
        String init = this.courseID + " " + this.numCredits + " " + this.semesters + " " + this.priority + " " + coreqData + " " + prereqData + " " + coreqNamesStr + " " + prereqNamesStr + " " + this.placementTestRequired;
        return init;
    }
}