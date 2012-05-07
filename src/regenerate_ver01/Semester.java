package regenerate_ver01;



import java.util.ArrayList;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:
 * @Class:   CS490
 * @Project: Course Scheduler
 *
 *
 * @Class Description:
 *			This class constructs a semester object, which is essentially a
 *			container of a group of course objects.
 *
 *			The number of course objects that can be in each semester is
 *			bounded by the attributes maxCredits (default = 18)
 *			and minCredits (default = 12), each of which can be changed
 *			by the user.
 */
public class Semester {

    //----------------------------------------------------
    // Attributes
    //----------------------------------------------------
    private int credits;
    private ArrayList<Course> courses;
    private int semID;	// semester number
    private int maxCredits = 18;		//..allowed per semester
    private int minCredits = 12;		//.. allowed per semester

	private ArrayList<String> possibleCourses; // all courses that can be taken in this semester


    //-----------------------------------------------------
    // Constructors
    //-----------------------------------------------------
    public Semester(int semID) {
        this.semID = semID;
        this.credits = 0;
        this.courses = new ArrayList<Course>();
		this.possibleCourses = new ArrayList<String>();
    }

    
    //-----------------------------------------------------
    // Other Public Methods
    //-----------------------------------------------------
    public boolean hasMinCredits() {
		return  (this.credits>=getMinCredits());
    }

	/**
	 * counts how many courses of this requirement type are in this semester
	 * @param type
	 * @return
	 */
	public int countType(String type){
		int count = 0;

		for(Course c: this.courses){
			if(c.getType().equals(type)){
				count++;
			}
		}
		return count;
	}
    
    //-------------------------------------------------------
    // Accessors & Mutators
    //--------------------------------------------------------
    public int getCredits() {
        return credits;
    }

    /**
     * Returns a shallow clone of the arraylist of courses in this semester
     *
     * @return
     */
    public ArrayList<Course> getCourses() {
        return (ArrayList<Course>) courses.clone();
    }

    public int getSemID() {
        return semID;
    }

	// always adds course
    public void addCourse(Course course) {
        this.courses.add(course);
        this.credits += course.getCredits();
        course.setSemester(semID);
        course.setTaken(true);
        course.setAdded(true);
    }

    /**
     * removes selected course from this semester, if it exists
     *
     *
     * @param course
     * @return 
     */
    public boolean removeCourse(Course course) {
        boolean success = false;
        for (int i = 0; i < this.courses.size(); i++) {
            if (this.courses.get(i).equals(course)) {
                Course c = this.courses.remove(i);
                c.setTaken(false);
                c.setSemester(-1);

                this.credits -= c.getCredits();

                // set in schedule = false?
                success = true;
                break;
            }
        }
        return success;
    }

    /**
     * removes selected course from this semester, if it exists
     *
     *
     * @param course
     * @return 
     */
    public boolean removeCourse(String courseID) {
        boolean success = false;
        for (int i = 0; i < this.courses.size(); i++) {
            if (this.courses.get(i).getCourseID().equals(courseID)) {
                success = removeCourse(this.courses.get(i));
                break;
            }
        }
        return success;
    }

    /**
     * @return the maxCredits
     */
    public int getMaxCredits() {
        return maxCredits;
    }

    /**
     * @param maxCredits the maxCredits to set
     */
    public void setMaxCredits(int maxCredits) {
        this.maxCredits = maxCredits;
    }

    /**
     * @return the minCredits
     */
    public int getMinCredits() {
        return minCredits;
    }

    /**
     * @param minCredits the minCredits to set
     */
    public void setMinCredits(int minCredits) {
        this.minCredits = minCredits;
    }

	/**
	 * safely removes all courses from this semester (i.e. modifies credit etc.
	 * info as well )
	 */
    public void clear() {
        for (int i = 0; i < this.courses.size(); i++) {
            this.removeCourse(courses.get(i));
            i--;
        }
    }
	/**
	 * returns the course IDs of all of the courses in this semester
	 *
	 * @return
	 */
	public ArrayList<String> getCourseIDs(){
		ArrayList<String> courseIDs = new ArrayList<String>();
		for(Course c : this.courses){
			courseIDs.add(c.getCourseID());
		}
		return courseIDs;
	}

	// Add a course that can be taken in this semester
	public void addPossibleCourse(String courseID){
		this.possibleCourses.add(courseID);
	}

	// get all courses that can be taken in this semester
	public ArrayList<String> getPossibleCourses(){
		return (ArrayList<String>)this.possibleCourses.clone();
	}

	// empties the possibleCourses arraylist
	public void clearPossibleCourses(){
		this.possibleCourses = new ArrayList<String>();
	}

    //-----------------------------------------------------------
    // Descriptions & Comparisons
    //-----------------------------------------------------------

	// Compares the semester ID of this semester and another semester
    public int compareTo(Semester that) {
        int thatSemID = that.getSemID();
        if (this.semID > thatSemID) {
            return 1;
        }
        if (this.semID < thatSemID) {
            return -1;
        }
        return 0;
    }

    public String toString() {
        String returnThis = "\n----------\nSEMESTER: " + semID + " \tCREDITS: " + credits + "\n------------";

        for (int i = 0; i < courses.size(); i++) {
            returnThis += "\n\t" + courses.get(i).toString();
        }

        return returnThis;
    }

    //-------------------------------------------------
    // Clones
    //-------------------------------------------------
    /**
     * Returns a shallow clone
     **/
    @Override
    public Semester clone() {
        Semester sem = new Semester(this.semID);
        for (int i = 0; i < courses.size(); i++) {
            sem.addCourse(courses.get(i));
        }
        return sem;
    }
}
