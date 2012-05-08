package regenerate_ver01;

import java.util.ArrayList;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:    
 * @Class:	 CS490
 * @Project: Course Scheduler
 *
 *
 * @Class Description:
 *			This class constructs a Course object that contains GMU catalog
 *			information about a specific course.
 *
 *			These course objects are used as the most basic building blocks
 *			for constructing a course schedule for n semesters for a specified
 *			degree program.
 *
 *			Each course object is independent of an explicit degree program.
 *
 */
public class Course {

    //Attributes
    private String courseID;
    private boolean placementTestPresent;
    private String[] prerequisites;		
    private ArrayList<String> successors;
    private ArrayList<Course> prereqCourses;
    private ArrayList<Course> successorCourses;
    private int priority;		// higher priorities -> take this course earlier
	private int initialPriority;
    private double difficulty; 
    private int credits;
    private int semester;		// semester it is in; -1 if in none
    private boolean taken;		// is it already in the schedule?
    private String type;		// elective, core, etc..
    private boolean isAdded = false;
    private int lengthOfLongestSuccessorChain;
    private ArrayList<String> corequisites;
    private ArrayList<Course> coreqCourses;
    private ArrayList<String> coreqSuccessors;
    private ArrayList<Course> coreqSuccessorCourses;
    private int[] possSemesters;	// semesters specified in the degreeInfo file
    private boolean locked = false;
    private int maxPrereqChainLength;
    private int visited;
    public static int WHITE = 1;
    public static int GRAY = 2;
    public static int BLACK = 3;
    private boolean inSchedule;
    private int[] possibleSemesters;
    private boolean modified;
    private int maxCoreqSuccessorChainLength;

    public static int NO_SEMESTER = -1; 
    private int[] activePossSems;   // this will be changed by the solver 
    // Constructors
    /**
     * Constructor with  priority given as a parameter
     *
     * @param courseID
     * @param credits
     * @param semesters
     * @param type
     * @param priority
     * @param prerequisites
     * @param corequisites
     */
    public Course(String courseID, int credits, int placementTest, int[] semesters,
            String type, int priority, double difficulty, 
            ArrayList<String> prerequisites, ArrayList<String> corequisites) {
        this.courseID = courseID;

        if (placementTest == 0) {
            this.placementTestPresent = false;
        } else {
            this.placementTestPresent = true;
        }

        this.priority = priority;
		this.initialPriority = priority;
        this.difficulty = difficulty; 
        this.possSemesters = semesters.clone();	// array of ints so shallow clone should be okay
        this.activePossSems = new int[0];
        
        this.credits = credits;
        this.type = type;
        this.semester = this.NO_SEMESTER;
        this.inSchedule = false;

        this.prerequisites = new String[prerequisites.size()];
        prerequisites.toArray(this.prerequisites);	// array of strings so shallow clone should be okay.

        this.prereqCourses = new ArrayList<Course>();
        this.maxPrereqChainLength = 0;

        this.corequisites = new ArrayList<String>();
        for (int i = 0; i < corequisites.size(); i++) {
            this.corequisites.add(corequisites.get(i));
        }
        this.coreqCourses = new ArrayList<Course>();

        this.coreqSuccessors = new ArrayList<String>();
        this.coreqSuccessorCourses = new ArrayList<Course>();
        this.maxCoreqSuccessorChainLength = 0;

        this.successors = new ArrayList<String>();
        this.successorCourses = new ArrayList<Course>();

        this.possibleSemesters = new int[0];
        this.taken = false;

        this.visited = 0;
        this.modified = false;
    }

    
    //------------------------------------
    // Accessors & Mutators
    //------------------------------------

	public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void lockToSemester(int semester) {
        this.semester = semester;
        this.locked = true;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setAdded(boolean added) {
        this.isAdded = added;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public int getLongestSuccessorChainLength() {
        return this.lengthOfLongestSuccessorChain;
    }
/**
     * @return the credits
     */
    public int getCredits() {
        return credits;
    }
    /**
     * @return the semester
     */
    public int getSemester() {
        return semester;
    }
    /**
     * @param semester the semester to set
     */
    public void setSemester(int semester) {
        this.semester = semester;
    }

    /**
     * @return the taken
     */
    public boolean isTaken() {
        return taken;
    }

    /**
     * @param taken the taken to set
     */
    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return float priority
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @return the courseID
     */
    public String getCourseID() {
        return courseID;
    }

    /**
     * @return the inSchedule
     */
    public boolean isInSchedule() {
        return inSchedule;
    }

    /**
     * @param inSchedule the inSchedule to set
     */
    public void setInSchedule(boolean inSchedule) {
        this.inSchedule = inSchedule;
    }
	    /**
     * @return the placementTestPresent
     */
    public boolean isPlacementTestPresent() {
        return placementTestPresent;
	}

	/**
     * @return the visited
     */
    public int getVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(int visited) {
        this.visited = visited;
    }



    /**
     * Adds a pointer to the specified course object as a prerequisite of this course
     * object.
     *
     * Add fails if pointers to all valid prerequisites already exist, or if specified
     * course object is not listed as a prerequisite of this course
     *
     * @param course
     * @return Returns 1 if add successful, 0 if not
     */
    public int addPrereqCourse(Course course) {

        int added = 0;
        int valid = 0;
        int i;

        // check if course is a valid prerequisite
        String prevCourseID = course.getCourseID();
        for (i = 0; i < this.prerequisites.length; i++) {
            if (this.prerequisites[i].equals(prevCourseID)) {
                valid = 1;
                break;
            }
        }

        // if valid, add it in to the prereqCourses array
        if (valid != 0) {
            prereqCourses.add(course);
            added = 1;
        }
        return added;
    }

    /**
     * Adds a pointer to the successor Course course
     *
     * @param course
     * @return 1 if addition successful; 0 if not
     */
    public int addSuccessorCourse(Course course) {

        // check if successor course already exists
        int added = 1;
        // if not:
        if (course == null) {
            added = 0;
        } else {
            this.successors.add(course.getCourseID());
            this.successorCourses.add(course);
        }

        return added;
    }

	/**
     * Adds a pointer to a course that this course is a corequisite of
     *
     * @param course
     * @return 1 if addition successful; 0 if not
     */
    public int addCoreqSuccessorCourse(Course course) {
        int added = 1;
        if (course == null) {
            added = 0;
        } else {
            this.coreqSuccessors.add(course.getCourseID());
            this.coreqSuccessorCourses.add(course);
        }
        return added;
    }


    /**
     * Adds a pointer to a coreq of this coures
     *
     * @param course
     * @return 1 if addition successful; 0 if not
     */
    public void addCoreqCourse(Course course) {

        int added = 1;
        if (course == null) {
            added = 0;
        } else {
            String title = course.getCourseID();
            int known = 0;
            for (int i = 0; i < this.corequisites.size(); i++) {
                if (this.corequisites.get(i).equals(title)) {
                    known = 1;
                    break;
                }
            }

            if (known == 1) {
                this.coreqCourses.add(course);
            } else {
                this.corequisites.add(course.getCourseID());
                this.coreqCourses.add(course);
            }
        }
    }
    /**
     * removes the specified corequisite -- this is needed when
	 * a requirement type is a corequisite , for example.
     *
     * @param course
     * @return 1 if addition successful; 0 if not
     */
    public void removeCoreqCourse(String courseID) {
        for (int i = 0; i < this.corequisites.size(); i++) {
            if (this.corequisites.get(i).equals(courseID)) {
                this.corequisites.remove(i);
                break;
            }
        }

        for (int i = 0; i < this.coreqCourses.size(); i++) {
            if (this.coreqCourses.get(i).getCourseID().equals(courseID)) {
                this.coreqCourses.remove(i);
                break;
            }
        }
    }

    /**
     * Updates the priority attribute of this course
     *
     */
    public void updatePriority() {
		priority = 0 ;

        priority = this.getLongestSuccessorChain();

        //check if max coreq successor chain length is longer, if so, set that as the priority
        if(this.getLongestCoreqSuccessorChainLength() > this.lengthOfLongestSuccessorChain) {
			priority = this.maxCoreqSuccessorChainLength;
		}


        // if this course has no prerequisites or successors OR coreqs, it can
        // be placed anywhere.
        // have its priority be a negative value so that we don't confuse it with
        // courses at the end of a successor chain (0 priority)
        if (this.maxPrereqChainLength == 0 && this.lengthOfLongestSuccessorChain == 0
                && this.maxCoreqSuccessorChainLength == -1) {
            priority = -1;
        }
        if (this.coreqSuccessorCourses.size() > 0) {
            priority += this.maxCoreqSuccessorChainLength + 1;
        }


        // check if one of the prerequisites is a credit type, if so, decrease
        // priority to first semester that course can be taken in based on an
        // average of 14 credit/semester schedule (arbitrary decision)
        for (int i = 0; i < this.prereqCourses.size(); i++) {

            if (this.prereqCourses.get(i).getType().equals("CREDITS")) {
                priority -= this.prereqCourses.get(i).getCredits() / 14.0;
            }
        }

        // if the possible semesters of this course have been specified, increase
        // the priority of this course to an arbitrarily high value to ensure
        // that it is always added to the schedule first in priority based additions
        if (this.possSemesters.length > 0 && this.possSemesters[0] != 0) {
            priority += 100 / this.possSemesters.length;
        }

		priority += initialPriority; 

    }

	/**
	 * Returns an array of all of the possible semesters this course can be placed
	 * in the current state of the schedule. This method bases its returned values on
	 * the relative position of this course in its prereq/coreq/successor chain .
	 *
	 * Note that this semester assumes that this course is offered in all semesters
	 *
         *  @param totalSems - the total semesters in our schedule 
	 * @return
	 */
    public int[] getPossibleSemesters(int totalSems) {
        // if possible semesters have been specified by the user OR
        // if this method was already called once, return the stored data
        if (this.possSemesters != null && this.possSemesters.length > 0
                && this.possSemesters[0] != 0) {
            return this.possSemesters.clone();
        }


        // this is the first time this method is being called; find the
        // possible semesters for a totalSems semester schedule and store & return it

        int earliest = this.getLongestPrerequisiteChain(); // the earliest you can take this course
        int latest;	// the latest semester you can take this course in


        // first check if this course is a coreq for any other course. If it is,
        // it cannot be taken in any semester later than its coreq's max semester
        int latestCoreqSem = totalSems;
        for (Course c : this.coreqSuccessorCourses) {
            if (c != null) {
                int[] pS = c.getPossibleSemesters(totalSems);
                if (pS[pS.length - 1] < latestCoreqSem) {
                    latestCoreqSem = pS[pS.length - 1]; // getting the min
                }
            }
        }
        // find out what its latest semester would be if it didnt have coreqs
        int latestRegSem = totalSems - this.getLongestSuccessorChain();


        // pick the smaller of the two as the latest that this course can be taken
        if (latestRegSem > latestCoreqSem) {
            latest = latestCoreqSem;
        } else {
            latest = latestRegSem;
        }

        int[] semesters = new int[latest - this.maxPrereqChainLength];
        this.possibleSemesters = new int[semesters.length];

        for (int i = 0; i < semesters.length; i++) {
            semesters[i] = this.maxPrereqChainLength + i + 1;
            this.possibleSemesters[i] = semesters[i];
        }

        return semesters;
    }

    /**
	 * returns an array of all of the possible semesters this course can be in
	 * based on the current state of the sample schedule as well as this course's
	 * relative position in its prereq/coreq/successor chain.
	 *
	 * Note that this method assumes that this course is offered in all semesters
	 * 
	 * @return a 2D int array A such that A[0] = earliest this course can be taken
	 *			and A[1] = latest this course can be taken
	 */
    public int[] getActivePossibleSems(int totalSems, int avgCredPerSem) {

        int[] ps = this.getPossibleSemesters(totalSems);

        int start = ps[0];
        int end = ps[ps.length - 1];

        int earlyCoreqSuccessor = this.getEarliestCoreqSuccessorSem();
        if (earlyCoreqSuccessor > end || earlyCoreqSuccessor < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            earlyCoreqSuccessor = end;
        }
        int earlySuccessor = this.getEarliestSuccessorSem() - 1;
        if (earlySuccessor > end || earlySuccessor < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            earlySuccessor = end;
        }
        int lateCoreq = this.getLatestCoreqSem();
        if (lateCoreq < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            lateCoreq = start;
        }
        int latePrereq = this.getLatestPrereqSem(avgCredPerSem) + 1;
        if (latePrereq < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            latePrereq = start;
        }

		// define the latest you can take this course
        if (earlyCoreqSuccessor < earlySuccessor) {
            end = earlyCoreqSuccessor;
        } else {
            end = earlySuccessor;
        }

		// define the earliest you can take this course
        if (lateCoreq > latePrereq) {
            start = lateCoreq;
        } else {
            start = latePrereq;
        }


        int[] active = new int[2];
        active[0] = start;
        active[1] = end;

        return active;
    }
    
    
    /**
     * returns an array of all of the possible semesters this course can be in
     * based on the current state of the sample schedule as well as this course's
     * relative position in its prereq/coreq/successor chain.
     *
     * Note that this method assumes that this course is offered in all semesters
     * 
     * 
     * @param totalSems  -- the total number of semesters in the schedule 
     * @param openSems -- boolean array of length totalSems that indicates whether or
     *                      not the course can be added to a specific semester (1 if 
     *                      it can, 2 if it can't ) .
     * 
     * @return an int array A such that contains all open semesters this course 
     *              can be placed in . The length of the int array corresponds to the
     *              number of possible semesters it can be placed in 
     *  
     */
    public int[] updateActivePossibleSems(int totalSems, boolean[] openSems, int avgCredPerSems) {
        if(this.isInSchedule() && this.isAdded()){
            int[] ps = new int[1];
            ps[0] = this.semester;
            this.activePossSems = ps; 
            return ps;
        }
        
        int[] ps = this.getPossibleSemesters(totalSems);

        int start = ps[0];
        int end = ps[ps.length - 1];

        int earlyCoreqSuccessor = this.getEarliestCoreqSuccessorSem();
        if (earlyCoreqSuccessor > end || earlyCoreqSuccessor < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            earlyCoreqSuccessor = end;
        }
        int earlySuccessor = this.getEarliestSuccessorSem() - 1;
        if (earlySuccessor > end || earlySuccessor < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            earlySuccessor = end;
        }
        int lateCoreq = this.getLatestCoreqSem();
        if (lateCoreq < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            lateCoreq = start;
        }
        int latePrereq = this.getLatestPrereqSem(avgCredPerSems) + 1;
        if (latePrereq < start) {
			// some invalid entries -- maybe some courses aren't in the schedule
            latePrereq = start;
        }

		// define the latest you can take this course
        if (earlyCoreqSuccessor < earlySuccessor) {
            end = earlyCoreqSuccessor;
        } else {
            end = earlySuccessor;
        }

		// define the earliest you can take this course
        if (lateCoreq > latePrereq) {
            start = lateCoreq;
        } else {
            start = latePrereq;
        }

        System.out.println(" ~~ ");
        System.out.println("Determining active semesters for course: " + this.courseID);
        System.out.println("earliest : " + start);
        System.out.println("latest : " + end);
        System.out.println(" ~~ ");
        
        if(end < start){
            this.activePossSems = new int[0];
            return new int[0];
        }
        int[] active = new int[end-start+1];
        for(int i =0, j=start; i< active.length; i++,j++){
            active[i] = j; 
        }

        this.activePossSems = (int[])active.clone();
        return active;
        
    }
    
    public int[] getActivePossSems(){
        return (int[]) this.activePossSems; 
    }
    
    
    /**
     * @return the successor courses
	 *
	 * Note that only the returned ArrayList is cloned. None of the courses in it
	 * are
     */
    public ArrayList<Course> getSuccessorCourses() {
        return (ArrayList<Course>) this.successorCourses.clone();
    }
    /**
     * @return the course IDs of the successors of this course
	 *
	 * Note that only the returned ArrayList is cloned. None of the courses in it
	 * are
     */
    public ArrayList<String> getSuccessors() {
        return (ArrayList<String>) this.successors.clone();
    }

	/**
     * @return the courses this course is a corequisite for
	 *
	 * Note that only the returned ArrayList is cloned. None of the courses in it
	 * are
     */
    public ArrayList<Course> getCoreqSuccessorCourses() {
        return (ArrayList<Course>) this.coreqSuccessorCourses.clone();
    }
	
	/**
     * @return the course IDs of the courses that this course is a corequisite for
	 *
	 * Note that only the returned ArrayList is cloned. None of the courses in it
	 * are
     */
    public ArrayList<String> getCoreqSuccessors() {
        return (ArrayList<String>) this.coreqSuccessors.clone();
    }
   /**
     * @return the prerequisites course IDs
     */
    public String[] getPrerequisites() {
        return prerequisites.clone();
    }

	/**
     * @return the prereqCourses
     */
    public ArrayList<Course> getPrereqCourses() {
        return (ArrayList<Course>) prereqCourses.clone();
    }

    public ArrayList<String> getCorequisites() {
        return (ArrayList<String>) this.corequisites.clone();
    }

    public ArrayList<Course> getCoreqCourses() {
        return (ArrayList<Course>) this.coreqCourses.clone();
    }

    
    //----------------------------------------------
    // Private Helper Methods
    //----------------------------------------------
    /**
     * Recursive method that counts the length of the successor/dependency chains
     * of each course's successors. It returns the length of the longest chain.
     * (i.e. it returns the length of the longest straight path .)
     *
     * @param course
     * @param lengths
     * @return length of the longest successor chain in lengths[1]
     */
    public int getLongestSuccessorChain() {
//		if(this.lengthOfLongestSuccessorChain > 0){
//			return this.lengthOfLongestSuccessorChain; // already calc'ed it
//		}

//		else{	// maybe we need to calculate it
        int length = 0;

        for (Course c : this.successorCourses) {
            if (c != null) {
                int prevLength = c.getLongestSuccessorChain();
                if (prevLength >= length) {
                    length = prevLength + 1;
                }
            }
        }


        this.lengthOfLongestSuccessorChain = length;
        return length;
//		}

    }

    public int getLongestPrerequisiteChain() {
        if (this.maxPrereqChainLength > 0) {
            return this.maxPrereqChainLength; // already calc'ed it
        } else {	// maybe we need to calculate it
            int length = 0;

            for (Course c : this.prereqCourses) {
                if (c != null) {
                    int prevLength = c.getLongestPrerequisiteChain();
                    if (prevLength >= length) {
                        length = prevLength + 1;
                    }
                }
            }
            this.maxPrereqChainLength = length;
            return length;
        }

    }

    public int getLongestCoreqSuccessorChainLength() {
        if (this.maxCoreqSuccessorChainLength > 0) {
            return this.maxCoreqSuccessorChainLength; // already calc'ed it
        } else {
            //maybe we still need to calc it
            int length = 0;
            for (Course c : this.coreqSuccessorCourses) {
                if (c != null) {
                    int prevLength = c.getLongestSuccessorChainLength();
                    if (prevLength >= length) {
                        length = prevLength + 1;
                    }
                }
            }

            this.maxCoreqSuccessorChainLength = length - 1;
            return length - 1;
        }
    }

	public int getLatestPrereqSem(int avgCredPerSem) {
        int latest = 0;

        for (Course c : this.prereqCourses) {
            if (c != null) {
                if (!c.getType().equals("CREDITS")) {
                    if (c.getSemester() > latest) {
                        latest = c.getSemester();
                    }
                } else {
                    // based on an average of 16 credits per semester schedule
                    int tmp = (int)Math.ceil((float)c.getCredits() / avgCredPerSem);
                    if (tmp > latest) {
                        latest = tmp;
                    }
                }
            }
        }
        return latest;
    }

    private int getEarliestSuccessorSem() {
        int earliest = 1000; // arbitrarily high initial value

        for (Course c : this.successorCourses) {
            if (c != null && !c.getType().equals("CREDITS")
                    && c.isInSchedule()) { // credits check is
                // a just in case check; it should never happen
                if (c.getSemester() < earliest) {
                    earliest = c.getSemester();
                }
            }
        }
        return earliest;
    }

    private int getLatestCoreqSem() {
        int latest = 0;

        for (Course c : this.coreqCourses) {
            if (c != null) {
                if (!c.getType().equals("CREDITS")) {
                    if (c.getSemester() > latest) {
                        latest = c.getSemester();
                    }
                } else {
                    // based on an average of 16 credits per semester schedule
                    int tmp = c.getCredits() / 16;
                    if (tmp > latest) {
                        latest = tmp;
                    }
                }
            }
        }
        return latest;
    }

    private int getEarliestCoreqSuccessorSem() {
        int earliest = 1000; // arbitrarily high initial value

        for (Course c : this.coreqSuccessorCourses) {
            if (c != null && !c.getType().equals("CREDITS")
                    && c.isInSchedule()) { // credits check is
                // a just in case check; it should never happen
                if (c.getSemester() < earliest) {
                    earliest = c.getSemester();
                }
            }
        }
        return earliest;
    }


    //-------------------------------------------------
    // Object Descritpions + Comparisons
    //------------------------------------------------
    public int compareID(Course that) {
        return this.getCourseID().compareTo(that.getCourseID());
    }

    /**
     * compares two courses by their priority 
     * 
     * @param that
     * @return 
     */
    public int compareTo(Course that) { /// ??? Is this a safe assumption?
        // mode of comparison = priorities
        if (this.priority > that.priority) {
            return 1;
        }
        if (this.priority < that.priority) {
            return -1;
        }

        // else equal
        return 0;
    }
    
    public boolean equals(Course that) {
        if (this.getCourseID().equalsIgnoreCase(that.getCourseID())) // same courseID = same course
        {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return courseID + "\t" + credits + " Credits\t" + priority + " Priority\t" + " Difficulty\t" + difficulty;
    }

    /**
     * Returns a detailed description of the state of the course object
     *
     * @return String of object state
     */
    public String getDetails() {
        String returnThis = this.toString() + "\n\tPrerequisites: ";

        // getting prerequisites
        if (this.getPrerequisites().length > 0) {
            for (int i = 0; i < this.prerequisites.length; i++) {
                returnThis += getPrerequisites()[i] + " ";
            }
        } else {
            returnThis += "N/A";
        }

        // getting successors
        returnThis += "\n\tSuccessors: ";
        if (this.successors.isEmpty()) {
            returnThis += "N/A";
        } else {
            for (int i = 0; i < this.successors.size(); i++) {
                returnThis += this.successors.get(i) + " ";
            }
        }

        returnThis += "\n\tCorequisites: ";
        if (this.corequisites.isEmpty()) {
            returnThis += "N/A";
        } else {
            for (int i = 0; i < this.corequisites.size(); i++) {
                returnThis += this.corequisites.get(i) + " ";
            }
        }

        return returnThis;
    }


    //-------------------------------------------------
    // Clones
    //------------------------------------------------
    /**
     * Returns a clone with the exact same state as this Course object, just without
     * the successors information and the prereq/successor pointers and semester/taken/inSchedule
     * information
     * @return
     */
    public Course deepclone() {
        ArrayList<String> prereqs = new ArrayList<String>();
        for (String s : this.prerequisites) {
            prereqs.add(s);
        }
        int pTest;
        if (this.isPlacementTestPresent() == true) {
            pTest = 1;
        } else {
            pTest = 0;
        }
        Course clone = new Course(this.courseID, this.credits, pTest, this.possSemesters.clone(),
                this.type, this.initialPriority, this.difficulty, prereqs, this.corequisites);

        return clone;
    }

    /**
     * returns a clone with the exact same state as this course object, including
     * schedule information. Pointers/pereqs/successors are not initialized in this
     * clone. the priority is not the same in this clone either.
     * @return
     */
    public Course clone() {
        ArrayList<String> prereqs = new ArrayList<String>();
        for (String s : this.prerequisites) {
            prereqs.add(s);
        }

        int pTest;
        if (this.isPlacementTestPresent() == true) {
            pTest = 1;
        } else {
            pTest = 0;
        }
        Course clone = new Course(this.courseID, this.credits, pTest, this.possSemesters.clone(),
                this.type, this.initialPriority, this.difficulty, prereqs, this.corequisites);
        clone.setInSchedule(this.inSchedule);
        if (this.locked) {
            clone.lockToSemester(this.semester);
        }
        clone.setAdded(this.isAdded);
        clone.setSemester(this.semester);
        clone.setTaken(this.taken);

        return clone;
    }

    /**
     * @return the difficulty
     */
    public double getDifficulty() {
        return difficulty;
    }

    /**
     * @param difficulty the difficulty to set
     */
    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public int[] removeFromDomain(int semester) {
        int[] activePossSems = this.getActivePossSems();
        
        ArrayList<Integer> domain = new ArrayList<Integer>();
        
        for(int i: activePossSems){
            if(i != semester){
                domain.add(i);
            }
        }
        
        this.possSemesters = new int[domain.size()];
        for(int i = 0; i < this.possSemesters.length; i++){
            this.possSemesters[i] = domain.get(i);
        }
            
        return this.getActivePossSems(); 
        
        
    }




}
