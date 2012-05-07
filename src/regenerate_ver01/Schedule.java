package regenerate_ver01;

//NOTE: all clones of course arraylists are shallow
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:
 * @Class:   CS490
 * @Project: Course Scheduler
 *
 *
 * @Class Description:
 *		This class stores and generates a schedule with a required set of courses
 *		It allows modification of the generated schedule.
 */
public class Schedule {

    //-----------------------------------------
    // Attributes
    //-----------------------------------------
    private Map<String, Course> catalog;
    private ArrayList<Semester> semesters;
    private ArrayList<Course> required;
    private int totalTakenCredits;
    private int totalSemesters;		// total # of expected semesters
    private ArrayList<String> taken;
	private int[] semIDByNumCreds;	// semester IDs sorted by the number of credits in them 

    //-----------------------------------------
    // Constructors
    //-----------------------------------------

    //Fix generateNewSchedule method
    //Refactor methods so that courses are passed around by ID instead of by Course

	public Schedule(Map<String, Course> catalog, ArrayList<String> taken,
			ArrayList<String> selected){

		this.required = new ArrayList<Course>();
		for(String cID: selected){
			Course c = catalog.get(cID);
			if(c!= null && !c.getType().equals("CREDITS")){
				required.add(catalog.get(cID));
			}
		}

        this.semesters = new ArrayList<Semester>();

        this.totalTakenCredits = 0;
        this.totalSemesters = 8;
        this.catalog = new HashMap<String, Course>();
        this.catalog.putAll(catalog); // shallow copy of catalog

		this.semIDByNumCreds = new int[totalSemesters];
		for(int i = 0; i < totalSemesters; i++){
			this.semIDByNumCreds[i] = i+1;
		}

        // initialize semester arraylist
        for (int i = 0; i < totalSemesters; i++) {
            this.semesters.add(new Semester(i + 1));
        }

        this.taken = new ArrayList<String>();
        this.taken.addAll(taken);

        // flag taken courses as taken
        for (String cID : taken) {
            if (catalog.get(cID) != null) {
                catalog.get(cID).setTaken(true);
            } else {
                System.out.println("Error: Unknown taken course - " + cID);
            }
        }

		updatePossibleCoursesForSemesters();

        updatePriority(this.required);

    

	}

    
	//-----------------------------------------
	// Sample Schedule Generation 
	//------------------------------------------


    /**
     * Generates a new schedule based on the priorities of all the required courses
     */
    public void genNewSchedule() {
        int numTries = this.totalSemesters;
        int numTaken = 0;


        ArrayList<Course> tmp = new ArrayList<Course>();
        tmp.addAll(this.required);


        // Step 1: Update priority of all required courses
        updatePriority(tmp);

        // Step 2: sort all required courses by priority
        sortByPriority(tmp);

        
        //Step 4: loop through all semesters and add courses in until all
		//			semesters have the minimum number of credits
        for (int i = 0; i < this.totalSemesters; i++) {

            Semester currSem = this.semesters.get(i);

            for (int j = 0; j < tmp.size(); j++) {
				Course currCourse = tmp.get(j);


              if (!currSem.hasMinCredits()) {
			//	if(currSem.getCredits() + currCourse.getCredits() < currSem.getMaxCredits()){

                    if (!currCourse.isTaken()) {

                        if (prereqsMet(currCourse, i + 1) && coreqsMet(currCourse, i + 1) //                              
                                ) {
                            // add it to this semester

                            currSem.addCourse(currCourse);
							System.out.println("\t adding " + currCourse.toString() + 
									" to sem " + (i+1));

                            this.totalTakenCredits += currCourse.getCredits();

                            // remove it from tmp
                            tmp.remove(j);
                            j--;

                            numTries = this.totalSemesters;
                        }
                    } else {
                        // it's taken - remove it from tmp
                        tmp.remove(j);
                        j--;
                        numTries = this.totalSemesters;
                    }
                } else {
                    break;
                }
            }
        }


        //if courses remain, try to squeeze them in for <= max-2, max-1, max crdits somewhere
		// to minimize occurrences of over-saturated semesters
        this.sortByPriority(tmp);
        int tries = 2;
        while (tries >= 0) {
            if (tmp.size() > 0) {
                for (int i = 0; i < semesters.size(); i++) {
                    Semester currSem = this.semesters.get(i);
                    int currSemCredits = currSem.getCredits();
                    int acceptCredits = currSem.getMaxCredits() - tries;
                    int acceptIf = acceptCredits - currSemCredits;

                    if (currSemCredits > acceptCredits) { // space to add in

                        for (int j = 0; j < tmp.size(); j++) {
                            Course currCourse = tmp.get(j);
                            if (prereqsMet(currCourse, i + 1) && coreqsMet(currCourse, i + 1)
                                    && currCourse.getCredits() <= acceptIf) {
                                // squeeze it in!
                                System.out.println("Squeezing in! " + currCourse.getCourseID());
                                currSem.addCourse(currCourse);
                                this.totalTakenCredits += currCourse.getCredits();
                                tmp.remove(j);
                                j--;
                            }
                        }
                    }
                }
            } else {
                break;
            }
            tries--;
        }

        if (tmp.size() > 0) {
            System.out.println("#############\nCould not fit in: ");
            for (Course c : tmp) {
                System.out.println("\t" + c.getCourseID());
            }

			// If there are still courses remaining, stick them in somewhere and refresh schedule
			// to ensure that everything gets added into the schedule in as valid
			// a place as possible
			for(Course c: tmp){
				this.moveCourseByForce(c.getCourseID(), c.getActivePossibleSems()[0]);
			}
			this.refreshSchedule(new ArrayList<String>(), new ArrayList<String>());
        }
    }


    private boolean coreqsMet(Course course, int currSemester) {
        if (course == null) {
            System.out.println("Not a valid course");
            return false;

        }

        ArrayList<String> corequisites = course.getCorequisites();

        for (int i = 0; i < corequisites.size(); i++) {
            Course cc = this.catalog.get(corequisites.get(i));

            if (cc == null) {
                System.out.println("coreq for course " + course.getCourseID() + " does not exist");
                return false;
            }

            int thatSem = cc.getSemester();

			if(thatSem < 1){
				return false;
			}

            // Is it a dummy course?
            if (cc.getType().equals("CREDITS")) {

                int prevCredits = 0;
				for (int j = 0; j <= currSemester; j++) {
                    prevCredits += this.semesters.get(j).getCredits();
                }

				//credits up until this sem count


                // check if enough credits have been taken for coreq
                if (cc.getCredits() > prevCredits) {
                    return false;
                }

            } else {
                // It's a real course
                if (cc.isTaken() && thatSem > currSemester) {	// assuming thatSem != -1 if isTaken = true
                    System.out.println("\n\nCorequisites for course " + course.getCourseID() + " not met in semester " + currSemester);
                    System.out.println("\tCorequisite: " + cc.toString() + "\t taken semester : " + cc.getSemester());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true of the prerequisites for the specified course have been met
     * and if it can be taken in the specified semester or not.
     *
     * @param course
     * @param currSemester
     * @return met
     */
    private boolean prereqsMet(Course course, int currSemester) {

        if (course == null) {
            System.out.println("Not a valid course");
            return false;

        }
        ArrayList<Course> prereqCourses = course.getPrereqCourses();


        for (int i = 0; i < prereqCourses.size(); i++) {

            Course prereq = prereqCourses.get(i);

            if (prereq == null) {
                System.out.println("prereq for course " + course.getCourseID() + " does not exist");
                return false;
            }

            int thatSem = prereq.getSemester();


            // Is it a dummy course?
            if (prereq.getType().equals("CREDITS")) {

                int prevCredits = 0;
                for (int j = 0; j < currSemester - 1; j++) {
                    prevCredits += this.semesters.get(j).getCredits();
                }
                // credits up until last semester count

                // check if enough credits have been taken by previous semester
                if (prereq.getCredits() > prevCredits) {
                    return false;
                }

            } else {
                // It's a real course
                if (!prereq.isTaken() || thatSem >= currSemester) {	// assuming thatSem != -1 if isTaken = true
                    System.out.println("\n\nPrerequisites for course " + course.getCourseID() + " not met in semester " + currSemester);
                    System.out.println("\tPrerequisite: " + prereq.toString() + "\t taken semester : " + prereq.getSemester());
                    return false;
                }
            }
        }
        return true;
    }

	//------------------------------------------
	// Schedule Regeneration
	//------------------------------------------
	

	/**
	 * refreshes the schedule based on the modified courses
	 * i.e.:
	 *		- it moves modified courses into their new semesters and rearranges those
	 *		courses' prerequisites and successors in the schedule if there is a need
	 *		- this method tries to keep the schedule as close to the original schedule
	 *			as possible
	 *
	 *		- this method will try to move the modified courses as little as possible, if at all
	 *		- if rearranged courses are invalid (e.g. user manually moves cs112 and
	 *			cs211 to the same semester), this method will move the prereq backwards in the schedule,
	 *			if possible, else the successor forward.
	 * @param modified
	 * @return possible -- are these modificatins possible?
	 */
	public boolean refreshSchedule(ArrayList<String> locked, ArrayList<String> modified){
		this.updateActiveCourses(); // just to be on the safe side
		ArrayList<String> ac = this.getActiveCourses();
		ArrayList<Course> activeCourses = new ArrayList<Course>();
		for(String cID : ac ){
			Course c= this.catalog.get(cID);
			if(c!= null){
				activeCourses.add(c);
				// refresh locked and modified flags of all courses
				c.setLocked(false);
				c.setModified(false);
			}
		}
		// 1. clear out all the semesters, but retain information on which semester
		// a course belonged to
		this.clearSemesters();


		// 2. set all courses as unvisited (for graph traversal algorithms)
		for(Course c: this.catalog.values()){
			c.setVisited(Course.WHITE);
		}

		// 3.a. add in all of the locked courses, assuming that course.semester
		//      is the semester that the course was locked to
		System.out.println("\nLocked Courses: ");
		ArrayList<Course> lockedCourses = new ArrayList<Course>();
		for(String courseID : locked){
			System.out.println("\t" + courseID);
			Course c = this.catalog.get(courseID);
			if(c != null){
				// ensure that the semester the course was locked to is a valid
				// semester for the locked course
				int[] pS = c.getPossibleSemesters();
				int semID = c.getSemester();
				if(pS[0] <= semID && semID <= pS[pS.length-1]){
					this.moveCourseByForce(courseID, c.getSemester());
					// set visited to started visit
					c.setVisited(Course.GRAY);

					// add it in the arraylist to avoid future lookup
					lockedCourses.add(c);

					// flag it as locked so that it isn't moved from this semester
					c.setLocked(true);
				}else{
					// this is not a valid semester for the locked course. this
					// schedule is not possible
					System.out.println("ERROR: CANNOT LOCK " + courseID + " TO SEMESTER " + semID);
					return false;
				}
			}else{
				System.out.println("Error: Could not find course " + courseID + " in the catalog");
			}
		}

		System.out.println("\n\nSchedule with locked courses added: \n" + this.toString() + "\n\n");


		// 4. insert modified courses into the now empty semesters, assuming that
		//		course.semester is the semester that course was moved to
		System.out.println("\nModified courses: ");
		ArrayList<Course> modifiedCourses = new ArrayList<Course>();
		for(String courseID : modified){
			System.out.println("\t" + courseID);
			Course c = this.catalog.get(courseID);
			if(c != null){
				this.moveCourseByForce(courseID, c.getSemester());
				// set visited to started visit
				c.setVisited(Course.GRAY);

				// add it in the arrayList to avoid future lookup
				modifiedCourses.add(c);

				// flag it as modified to minimize its unnecessary movement
				// from the selected semester
				c.setModified(true);
			}
			else{
				System.out.println("Error: Could not find course " + courseID + " in the catalog");
			}
		}
		System.out.println("\n\nSchedule with modified courses: \n" + this.toString() + "\n\n");

		//5. Insert the prerequisites of the modified courses , trying to remain as close
		//    to the original schedule as possible
		ArrayList<Course> reAdd = new ArrayList<Course>();
		boolean success = true;


		// 6. Insert the chains for locked courses, trying to remain as close to
		//		the original schedule as possible
		System.out.println("Adding locked course chains");
		for(Course c: lockedCourses){
			System.out.println("\t-"+c.getCourseID());
			success = this.addSuccessorTrees(c, c.getSemester(), false);
			if(!success){
//				reAdd.add(c);
				System.out.println("\t!! Failed");
				// adding in the course chain of this locked course was impossible.
				// The schedule is impossible with these locked course spec.s
				System.out.println("COULD NOT ADD LOCKED COURSE " + c.getCourseID() + "'s CHAIN");
			}
		}

		System.out.println("\n\nSchedule with Locked course chains added: \n" + this.toString() + "\n\n");


		// 7. Insert the successors of the modified courses (that were originally in
		//		the schedule) , trying to remain as close to the original schedule as possible
		System.out.println("Adding modified course chains");
		for(Course c: modifiedCourses){
			System.out.println("\t-"+c.getCourseID());
			success = this.addSuccessorTrees(c, c.getSemester(), false);
			if(!success){
//				reAdd.add(c);
				System.out.println("\t!! Failed");
				// adding this course to the semester the user specified is
				// impossible. mark this course as unvisited so that it gets
				// added in normally with the rest of the courses in the
				// schedule
				c.setVisited(Course.WHITE);
			}
		}

		System.out.println("\n\nSchedule with modified course chains added: \n" + this.toString() + "\n\n");


		// 8. Insert the rest of the courses into the schedule, trying to remain as
		//		close to the original schedule as possible
		System.out.println("\nAdding all active courses! \n");
		this.sortByPriority(activeCourses);
		for(Course c: activeCourses){
			if(c.getVisited() == Course.WHITE){
				System.out.println("\t>" + c.getCourseID());
				success = addSuccessorTrees(c, c.getSemester(), false);
			}
			if(! success){
				// failed to add c to its original semester, try again, wherever possible
				int[] possibleSems = c.getPossibleSemesters();
				int end = possibleSems[possibleSems.length-1];
//				for(int i = possibleSems[possibleSems.length-1]; i >=possibleSems[0] ; i--){

				// try adding it to the semester with the least credits first
				for(int i : this.semIDByNumCreds){

					// checking if i is a possible semester
					if(possibleSems[0] <= i && i <= end){
						success = addSuccessorTrees(c, i, false);
						if(success){
							break;
						}
					}
				}
			}
			if(!success){
				// failed again, put it in the readd pile
				System.out.println("!! Failed to add " + c.getCourseID());
				reAdd.add(c);
			}
		}

		System.out.println("\n\nSchedule after all active courses are added: \n" + this.toString() + "\n");

		System.out.println("Courses that need to be readded: ");
		for(Course c : reAdd){
			System.out.println("\t" + c.getCourseID());
		}

		// 9. Re-add courses that need readding , wherever possible
		System.out.println("\nReadding courses: ");
		ArrayList<Course> reAdd2 = new ArrayList<Course>();
		this.sortByPriority(reAdd);
		for(Course c: reAdd){
			System.out.println("--" + c.getCourseID());
			int[] possibleSems = c.getPossibleSemesters();
			int start = possibleSems[0];
			int end = possibleSems[possibleSems.length -1];
//			for(int i : possibleSems){
			for(int i: this.semIDByNumCreds){

				if(start <= i && i <= end){
					success = addSuccessorTrees(c, i, true);
					if(success){
						break;
					}
				}
			}
			if(!success){
				System.out.println("\t!! Failed to add " + c.getCourseID());
				reAdd2.add(c);
			}
		}


				System.out.println("\n\nSchedule after reAdd 1: \n" + this.toString() + "\n");

		System.out.println("\nCourses that need to be readded again : ");
		for(Course c: reAdd2){
			System.out.println("\t" + c.getCourseID());
		}

		System.out.println("Semester IDs sorted by number of credits: \n") ;
		for(int i : this.semIDByNumCreds){
			System.out.print("  " + i);
		}

		if(semesters.get(this.semIDByNumCreds[0]-1).getCredits()
				< semesters.get(this.semIDByNumCreds[0]-1).getMinCredits()){

			System.out.println("Balancing semesters");
			balanceSems();
			System.out.println("\n\nSchedule after balance : \n" + this.toString() + "\n");
		}


		// reset
		for(Course c: this.catalog.values()){
			c.setModified(false);
			c.setLocked(false);
		}
		return success;
	}

	/**
	 * adds all of the courses required to take this course to the schedule
	 *
	 * if those courses are already in the schedule and are in semesters before
	 * root, then they are kept in those semesters. Else, they are moved back (if
	 * pushBack is true)
	 *
	 * @param root
	 */
	private boolean addPrereqTree(Course root, int semID, boolean pushBack){
		// initialization
		boolean success = true;
		// 1. set visited to visiting
		root.setVisited(Course.GRAY);

		// 2.a. add the root to the specified semester, if it is a possible move
		int[] possSems = root.getPossibleSemesters() ;
		if(semID< 0 || semID > totalSemesters){
			return false;
		}
		if(semID < possSems[0] || semID > possSems[possSems.length -1]){
			System.out.println("!! Course " + root.getCourseID() + " not possible in "
					+ "semester " + semID);
			// remove the course from this semester in case it has already been
			// inserted in it
			this.semesters.get(semID-1).removeCourse(root.getCourseID());
			this.updateSemIDByCredits();
			return false; // putting this course into that semester will make an
							// impossible schedule
		}

		moveCourseByForce(root.getCourseID(), semID);
		System.out.println("added " + root.getCourseID() + " to semester " + semID);

		//2.b. check if this entry was valid
		Semester s = this.semesters.get(semID-1);

			// is semester too full?
		if(s.getCredits() > s.getMaxCredits()){
			System.out.println("!! Could not add " + root.getCourseID() + " to semester"
					+ " " + semID);
			System.out.println("\t--Semester too full");

			// remove course from this semester
			s.removeCourse(root.getCourseID());
			return false;	// add failed
		}

		// 3. add in all of the prerequisites that haven't been added yet.
		for(Course prereq : root.getPrereqCourses()){
			if(prereq.getVisited() == Course.WHITE){
				// try to add the prereq to its original semester, if possible
				int prereqSem = prereq.getSemester();
				success = false;
				if(prereqSem < semID){
					success = addPrereqTree(prereq, prereqSem, pushBack);
				}

				// if not, or if add was not possible, try to add it to any semester before this one .
				if(!success) {
					if(semID == 1){
						// this should always be false if possibleSemesters is correct
						System.out.println("ERROR: Incorrect Possible Semesters range definition for course " + root.getCourseID());
						return false;
					}

					int[] pS =prereq.getPossibleSemesters();
					int end = semID-1;
					int start = pS[0];

//					for(int i = semID -1 ; i > 0; i--){

					// try adding in the prereq to the semester w/ the least
					// number of credits first
					for(int i : this.semIDByNumCreds){

						// make sure that the semester is within the possible
						// semester range
						if(start <= i && i <= end){
							success = addPrereqTree(prereq, i, pushBack);
							if(success){
								break;	// stop once it has been added in one semester
							}
						}
					}
				}
				// if even one prereq cannot be added, generating a valid schedule is
				// impossible with this course arrangement.
				if(!success){
					System.out.println("!! Could not add prereq " + prereq.getCourseID()
							+ "for course " + root.getCourseID());
					return false;
				}
			}
			// make sure that it was in a semester before this one; if not,
			// re-insert it to one that is, if pushback is true and prereq is not
			// locked
			else if(prereq.getSemester() >= semID){
				if(!prereq.isLocked() && pushBack){
					if(semID == 1){
												// this should always be false if possibleSemesters is correct
						System.out.println("ERROR: Incorrect Possible Semesters range definition for course " + root.getCourseID());
						return false;
					}
					int[] pS =prereq.getPossibleSemesters();
					int end = semID-1;
					int start = pS[0];


					// try adding in the prereq to the semester w/ the least
					// number of credits first
					for(int i : this.semIDByNumCreds){

						// make sure that the semester is within the possible
						// semester range
						if(start <= i && i <= end){
							success = addPrereqTree(prereq, i, pushBack);
							if(success){
								break;	// stop once it has been added in one semester
							}
						}
					}

					if(!success){
						System.out.println("!! Could not add prereq " + prereq.getCourseID()
								+ " for course " + root.getCourseID());
						return false;
					}
				}else{
					// root and prereq are in the same semester OR prereq is in
					// a semester after root's semester and pushing visited
					// prereq back is not allowed (or prereq is locked), therefore
					// a schedule with these settings is impossible
					System.out.println("!! could not add " + root.getCourseID()
							+ " to the same or earlier semester as its prereq "
							+ prereq.getCourseID() + " (sem " + prereq.getSemester()
							+ " )" );
					return false;
				}
			}
		}

		// 4. we're finished with this root: mark it
		root.setVisited(Course.BLACK);

		return success;
	}


	/**
	 * add the all of the course trees of the courses that depend on the root course
	 * in semesters after the root course's semester
	 *
	 * @param root
	 * @return
	 */
	private boolean addSuccessorTrees(Course root, int semID, boolean moveModified){
		// initialization
		boolean success = true;

		// 1. add the root to the specified semester, with its prereq tree,
		//		if it is a possible move
		int[] possSems = root.getPossibleSemesters() ;
		if((semID < possSems[0] || semID > possSems[possSems.length -1]) && semID > 0){
			System.out.println("!! Course " + root.getCourseID() + " not possible in "
					+ "semester " + semID);
			// remove the course from this semester in case it has already been
			// inserted in it
			this.semesters.get(semID-1).removeCourse(root.getCourseID());
			this.updateSemIDByCredits();
			return false; // putting this course into that semester will make an
							// impossible schedule
		}

		success = this.addPrereqTree(root, semID, false);

		if(! success){
			System.out.println("! Could not add prereq Tree for course " + root.getCourseID());
			return false;
		}


		// 2. add in the coreqs for this root, if possible
		success = this.addCoreqTrees(root, semID, moveModified);
		if(! success){
			System.out.println("! Could not add coreq-prereq Trees for course " + root.getCourseID());
			return false;
		}


		// 3. if successful, add in the successors for this course as well , trying
		//		to add them to their original semesters as much as possible
		//		Note: Only add in successors that are in the schedule
		if(root.getSuccessors().size() > 0){
			System.out.println("Adding successors for: " + root.getCourseID());
		}
		for(Course successor: root.getSuccessorCourses()){
			System.out.println("\t" + successor.getCourseID());
			if(successor.isInSchedule()){
				// trying to add it to its original semester
				success = false;
				if(successor.getSemester() > semID){
					success = addSuccessorTrees(successor, successor.getSemester(),
							moveModified);
				}else{
					// check if this successor can be moved at all or not
					if(successor.isLocked() || (successor.isModified() && !moveModified)){
						// it can't; this schedule is impossible
						System.out.println("!!! " + root.getCourseID()
								+ " successor course is locked to a semester ( + "
								+ successor.getSemester() + ") <= root's semester"
								+ " ( " + semID +  " )");
						return false;
					}
				}


				// if it couldn't be added in its original semester, try adding it
				// anywhere after root's semester
				if(! success ){
					int[] pS = successor.getPossibleSemesters();
					int start = semID+1;
					int end = pS[pS.length -1];
//					for(int i = semID+1; i <= 8; i++){

					// try adding it into the semesters w/ the least credits first
					for(int i : this.semIDByNumCreds){
						// check if this semester is among the possible semesters
						// for this course
						if(start <= i && i <= end){
							success = addSuccessorTrees(successor, i, moveModified);
							if( success ){
								break;		// added it to a semester!
							}
						}
					}
				}

				if(! success){
					System.out.println("!! Could not add successor for "+ root.getCourseID() + " : " + successor.getCourseID());
					System.out.println("\t\tStart semester: " + semID);
					return false; // couldn't add in successor - schedule is impossible
								// with these course settings
				}
			} else{
				System.out.println("Successor course " + successor.getCourseID() +
						" is not in schedule");
			}
		}

		return success;
	}


	/**
	 * adds all of the coreqs and their prereq trees for the root course
	 *
	 * @param root
	 * @param semID
	 * @return boolean success - true if add successful , else false
	 */
	private boolean addCoreqTrees(Course root, int semID, boolean moveModified){
		boolean success = true;
		System.out.println("\nAdding in coreqs for " + root.getCourseID());
		// 1. get all of the coreqs
		ArrayList<Course> coreqCourses = root.getCoreqCourses();

		// 2. add in each coreq's prereq tree, trying to remain as close to
		//		original schedule as possible
		for(Course coreq: coreqCourses){
			System.out.println("\t~" + coreq.getCourseID());
			if(coreq.getVisited() == Course.WHITE || coreq.getSemester() > root.getSemester()){
				System.out.println("\t\tCoreq Semester: " + coreq.getSemester());
				System.out.println("\t\tRoot Semester: " + semID);
				success = false;
				// try to add it to its original semester if it comes before or
				// is the same as root's semester
				if(coreq.getSemester() <= root.getSemester()
						&& coreq.getSemester() >  0){
					System.out.println("\t\tAdding coreq to its original semester! ");
					success = this.addPrereqTree(coreq, coreq.getSemester(), true);
				}else{
					// check if we have permission to move the coreeq
					// (either it's locked OR it's modified and we can't move
					// modified courses) if we don't , then the schedule is
					// impossible (b/c coreq sem > root sem)
					if(coreq.isLocked() || (coreq.isModified() && !moveModified)){
						System.out.println("!!! " + root.getCourseID() + " coreq "
								+ coreq.getCourseID() + " is locked to a semester ( "
								+ coreq.getSemester() + " )"
								+ "after root's semester ( " + semID + " )");
						return false;
					}
				}


				if(! success ){
					// add into original semester failed
					System.out.println("\t\tAdding coreq to a diff semester! ");
					int[] possibleSems = coreq.getPossibleSemesters();
					int start = possibleSems[0];
					int end = semID;
//					for(int i = semID; i >= possibleSems[possibleSems.length -1] ; i--){
					for(int i : this.semIDByNumCreds){
						if(start <= i && i <= end){
							success = addPrereqTree(coreq, i, true);
							if(success){
								break;
							}
						}
					}
				}
				if(! success){
					System.out.println("!! Could not add " + root.getCourseID()
								+ "'s coreq: " + coreq.getCourseID());
					return false;
				}
			}
		}
		return success;
	}

	private void updateActiveCourses(){
		ArrayList<String> active = this.getActiveCourses();

		// add in missing required courses too
		for(Course c: this.catalog.values()){
			c.setInSchedule(false);
		}

		for(String cID : active){
			Course c = this.catalog.get(cID);
			if(c!= null){
				c.setInSchedule(true);
			} else{
				System.out.println("Error: Could not find active course " + cID + " in catalog");
			}
		}
	}


	/**
	 * Tries to ensure that each semester meets the minimum credit requirement.
	 * Tries to keep the modified schedule as similar to the original schedule
	 * as possible in its rearrangements
	 *
	 * @return
	 */
	private boolean balanceSems(){
		boolean success = true;
		// check all possible courses for each semester and plug in the
		// ones that do NOT decrease the course's original semester's credit
		// count below th min limit by their removal
		System.out.println("===== \n Trying with min movements! \n===");
		for(int i =1; i <= this.totalSemesters; i++){
			System.out.println("\n---------");
			if(! this.fillInWithMinMovements(i)){
				success = false;
			}
		}
		if(! success){
			// if the first method did not succeed, try finding a solution by
			// shifting courses backwards (towards sem 1)
			System.out.println("===== \n Trying in fwd dir! \n===");
			for(int i =1; i <= this.totalSemesters; i++){
				System.out.println("\n---------");
				if(! this.fillToMinBwd(i)){
					success = false;
					break; // probably need to fill it in in the reverse direction
				}
			}
			if(! success ){
				// try again by shifting courses forwards (towards sem 8)
				System.out.println("===== \n Trying in reverse dir! \n===");
				for(int i = this.totalSemesters; i > 0; i--){
					System.out.println("\n---------");
					if(! this.fillToMinFwd(i)){
						success = false;
						break;
					}
				}
			}
		}
		return success;
	}

	/**
	 * shifts courses forwards one semester at a time
	 * this is a recursive helper method for balanceSems()
	 *
	 * @param semID
	 * @return
	 */
	private boolean fillToMinFwd(int semID){
				Semester s = this.semesters.get(semID -1);
			System.out.println("Balancing sem " + semID);
			System.out.println("Credits: " + s.getCredits());

		// base case
		if(s.getCredits() >= s.getMinCredits()){
			System.out.println("Req met!");
			return true;
		}else{
			// recursion
			System.out.println("Req not met!");
			boolean success = false;
			// try to find at least one course in the semesters after this one
			// that fills this semester up


			// probably sems towards the end don't have enough credits


			for(int i= semID-1 ; i >0; i-- ){
				System.out.println("\tSource sem: " + i);
				ArrayList<Course> courses = this.semesters.get(i-1).getCourses();

				// loop through courses in new source semester
				for(int j = 0; j < courses.size(); j++){
					Course c = courses.get(j);

					System.out.println("\tExamine course: " + c.getCourseID());
					// check if this course is moveable
					if(!c.isLocked() && !c.isModified()){
						System.out.println("\tmoveable!");
						// it's moveable!

						// check if this semester is possible for it
						int[] possSems = c.getActivePossibleSems();
						System.out.println("\tPossible sem range: "
								+ possSems[0] + " - "
								+ possSems[possSems.length-1]);

						if(possSems[0] <= semID && semID <= possSems[possSems.length -1]){
							System.out.println("\tCourse possible in this semester!");
							// move it!!
							this.moveCourseByForce(c.getCourseID(), semID);
							System.out.println("\tNew sem " + semID + "\n" + s.toString());
							// see if this move produces a possible schedule
							// (i.e. all requirements are met)

							System.out.println("\t\tChecking other sem combinations! (if needed) ");
							for(int k =semID; k >0 ; k--){

								success = this.fillToMinFwd(k);
								if(!success){
									// this move does not produce a viable
									// schedule
									System.out.println("FAILED");
									// restore original schedule state
									this.moveCourseByForce(c.getCourseID(), i);
									break;

								} else if (k==this.totalSemesters){
									//it's possible! - don't need to look more
									System.out.println("SUCCESS");
									return true;
								}
							}
						}else{
							System.out.println("\tCourse not possible in this semester");
						}
					}else{
						System.out.println("\tnot moveable!");
					}
				}
			}


			System.out.println("Success? : " + success);
			return success;
		}
	}
	/**
	 * shifts courses in this semester backwards one semester at a time
	 * this is a recursive helper method for
	 * balance sem.s
	 * @param semID
	 * @return
	 */
	private boolean fillToMinBwd(int semID){
		Semester s = this.semesters.get(semID -1);
			System.out.println("Balancing sem " + semID);
			System.out.println("Credits: " + s.getCredits());

		// base case
		if(s.getCredits() >= s.getMinCredits()){
			System.out.println("Req met!");
			return true;
		}else{
			// recursion
			System.out.println("Req not met!");
			boolean success = false;
			// try to find at least one course in the semesters after this one
			// that fills this semester up

			// loop through semesters after current on
			for(int i= semID+1 ; i <= this.totalSemesters; i++ ){
				System.out.println("\tSource sem: " + i);
				ArrayList<Course> courses = this.semesters.get(i-1).getCourses();

				// loop through courses in new source semester
				for(int j = 0; j < courses.size(); j++){
					Course c = courses.get(j);

					System.out.println("\tExamine course: " + c.getCourseID());
					// check if this course is moveable
					if(!c.isLocked() && !c.isModified()){
						System.out.println("\tmoveable!");
						// it's moveable!

						// check if this semester is possible for it
						int[] possSems = c.getActivePossibleSems();
						System.out.println("\tPossible sem range: "
								+ possSems[0] + " - "
								+ possSems[possSems.length-1]);

						if(possSems[0] <= semID && semID <= possSems[possSems.length -1]){
							System.out.println("\tCourse possible in this semester!");
							// move it!!
							this.moveCourseByForce(c.getCourseID(), semID);
							System.out.println("\tNew sem " + semID + "\n" + s.toString());
							// see if this move produces a possible schedule
							// (i.e. all requirements are met)

							System.out.println("\t\tChecking other sem combinations! (if needed) ");
							for(int k =semID; k <= this.totalSemesters; k++){

								success = this.fillToMinBwd(k);
								if(!success){
									// this move does not produce a viable
									// schedule
									System.out.println("FAILED");
									// restore original schedule state
									this.moveCourseByForce(c.getCourseID(), i);
									break;

								} else if (k==this.totalSemesters){
									//it's possible! - don't need to look more
									System.out.println("SUCCESS");
									return true;
								}
							}
						}else{
							System.out.println("\tCourse not possible in this semester");
						}
					}else{
						System.out.println("\tnot moveable!");
					}
				}

			}

			System.out.println("Success? : " + success);

			return success;
		}
	}

	/**
	 * Tries to fill this semester to the min credits requirements by moving a couple
	 * of valid courses for this semester into it.
	 * the courses moved do not decreaes the course count of their original semester
	 * to below the min credits requirment.
	 *
	 * always produces viable schedules if it succeeds (returns true)
	 * @param semID
	 * @return
	 */
	private boolean fillInWithMinMovements(int semID){

		boolean success = false;

		System.out.println("Semester " + semID);

		Semester s = this.semesters.get(semID -1);

		if(s.getCredits() >= s.getMinCredits()){
			System.out.println("reqs met!");
			return true; // don't waste time on the rest of this method
		}
		System.out.println("Possible Courses: ") ;
		System.out.println(s.getPossibleCourses().toString());

		for(String cID : s.getPossibleCourses()){
			Course c = this.catalog.get(cID);
			System.out.println("\nExamining " + cID);
			System.out.println("\tSem " + c.getSemester());

			if(c.isInSchedule() && !c.isLocked() && !c.isModified()){
				System.out.println("moveable");
				int[] possSems = c.getActivePossibleSems();

				if(semID >= possSems[0] && possSems[possSems.length-1] >= semID){
					System.out.println("Possible in this sem");
					// it's possible in this semester

					// check if its removal reduces the number of credits of the
					// course's prev semester to a value below the min
					Semester oldS = this.semesters.get(c.getSemester()-1);
					System.out.println("Old sem creds: "+ oldS.getCredits());
					if(oldS.getCredits() - c.getCredits() >= oldS.getMinCredits()){
						System.out.println("moving!");
						moveCourseByForce(c.getCourseID(), semID);

						if(s.getCredits() >= s.getMinCredits()){
							// you can stop now
							success = true;
							break;
						}
					}else{
						System.out.println("old sem will have too few creds!");
					}
				}else{
					System.out.println("not possible in this sem");
				}
			}else{
				System.out.println("not moveable");
			}
		}

		return success;
	}

	
	//-----------------------------------------
    // Other Public Methods
    //-----------------------------------------
    /**
     * move specified course to specified semester if the move is valid
     * @param courseID
     * @param semID
     */
    public boolean moveCourse(String courseID, int semID) {
       boolean success = false;
		Course c = this.catalog.get(courseID);

        Semester s = this.semesters.get(semID - 1);

        // is it in schedule?
        if (c.isInSchedule()) {
            // remove it from previous semester and add it to this one
            if (c.getSemester() != semID && prereqsMet(c, c.getSemester())) {
                // just to be sure , check this as well
                if (c.getSemester() > -1) {
                    this.semesters.get(c.getSemester() - 1).removeCourse(c.getCourseID());
                    s.addCourse(c);
                    c.setAdded(true);
					success = true;
                } else {	// it wasn't in any semester before this, add it in
                    s.addCourse(c);
                    c.setAdded(true);
					success = true;
                }

				this.updateSemIDByCredits();
            }
        } else {
            // add it to schedule and to semester
            s.addCourse(c);
            c.setAdded(true);
            c.setInSchedule(true);
            this.totalTakenCredits += c.getCredits();
			this.updateSemIDByCredits();
			success = true;
        }

		return success;
    }


    /**
     * returns an arraylist of the course IDs of all taken courses
     * @return
     */
    public ArrayList<String> getTakenCourses() {
        ArrayList<String> takenC = new ArrayList<String>();
        for (Course c : this.catalog.values()) {
            if (c.isTaken()) {
                takenC.add(c.getCourseID());
            }
        }
        return takenC;
    }


	public int[] getPossibleSemesters(String courseID){
		return this.catalog.get(courseID).getPossibleSemesters();
	}

	/**
	 *  determines if a single course move is possible based on locked courses
	 *	in the schedule .
	 * a move is considered possible if the semester selected is possible for it
	 * and there is space in the new semester for that course
	 * in the case that there isn't enough space, the move is valid if at least
	 * one course can be moved from it to make room for thew new course
	 */
	public boolean isMovePossible(String courseID, int newSem , ArrayList<String> locked){
		boolean possible = false;
		Course c = catalog.get(courseID);
		if(c!= null && !c.getType().equals("CREDITS")){
			int[] possSems = c.getPossibleSemesters();

			// range check -- is the semester even possible?
			for(int i : possSems){
				if( i == newSem){
					possible = true;
					break;
				}
			}
			if(possible){
				// locked courses check
				possible = false;

				// mark all locked courses as  locked
				for(String cID : locked){
					if(this.catalog.get(cID) != null){
						this.catalog.get(cID).setLocked((true));
					}
				}

				// count locked courses credits in this sem
				int lockedCredits = 0;
				Semester s = this.semesters.get(newSem -1);
				for(Course course : s.getCourses()){
					if(course.isLocked()){
						lockedCredits+= course.getCredits();
					}
				}
				if(lockedCredits + c.getCredits() <= s.getMaxCredits()){
					possible = true;
				}// else false
			}
		}
		return possible ;
	}

	 //-----------------------------------------
    // Accessors and Mutators
    //-----------------------------------------
    /**
     * Returns a shallow clone of all of the semesters in this schedule
     * @return
     */
    public ArrayList<Semester> getSemesters() {
        return (ArrayList<Semester>) this.semesters.clone();
    }

    public void setTotalSemesters(int numSemesters) {
        this.totalSemesters = numSemesters;
    }

    public int getTotalSemesters() {
        return this.totalSemesters;
    }

    public String[] getRequiredCourseIDs() {
        String[] requiredIDs = new String[this.required.size()];

        for (int i = 0; i < requiredIDs.length; i++) {
            requiredIDs[i] = this.required.get(i).getCourseID();
        }
        return requiredIDs;
    }

    public ArrayList<String> getActiveCourses() {
        ArrayList<String> inSchedule = new ArrayList<String>();

        for (Semester s : this.semesters) {
            inSchedule.addAll(s.getCourseIDs());
        }

        return inSchedule;
    }

    public boolean isCourseLocked(String courseID) {
        return this.catalog.get(courseID).isLocked();
    }




	//------------------------------------------
	// Private Helper Methods
	//------------------------------------------

	/**
	 * clear semesters of all data
	 */
    private void clearSemesters() {
        this.semesters.clear();
        for (int i = 0; i < this.totalSemesters; i++) {
            this.semesters.add(new Semester(i + 1));
        }
		this.updatePossibleCoursesForSemesters();
    }

    /**
     * move course without doing any validity checks on prereqs
     * or coreqs or semester balance etc.
	 *
	 * only checks if the semID specified is in the valid range of
	 * semesters. if it isn't, no move is performed
	 *
     * @param courseID
     * @param semID
     */
    private void moveCourseByForce(String courseID, int semID){

		Course c = this.catalog.get(courseID);
	
		if(semID > 0 && semID <= this.semesters.size()){
			Semester s = this.semesters.get(semID - 1);

			if(c.getSemester() > 0){
				// remove it from old semester!
				Semester prev = this.semesters.get(c.getSemester() - 1);
				prev.removeCourse(courseID);
			}
			else{
				this.totalTakenCredits += c.getCredits();
			 }

			s.addCourse(c);
			c.setAdded(true);
			c.setInSchedule(true);

			updateSemIDByCredits();
		}else{
			System.out.println("Error: Trying to move course "+ courseID + " to "
					+ "invalid semester " + semID);
		}
	}

	/**
	 * updates the int[] semIDByCredits attribute such that all semester IDs in 
	 * it are in ascending order based on the number of taken credits in the 
	 * corresponding semesters
	 */
	private void updateSemIDByCredits(){
		for(int i = 0; i < this.totalSemesters; i++){
			for(int j = i+1; j < this.totalSemesters; j++){
				int semID1 = this.semIDByNumCreds[i];
				int semID2 = this.semIDByNumCreds[j];
				int sem1Creds = this.semesters.get(semID1-1).getCredits();
				int sem2Creds = this.semesters.get(semID2-1).getCredits();

				if(sem1Creds  > sem2Creds){ // swap!
					this.semIDByNumCreds[i] = semID2;
					this.semIDByNumCreds[j] = semID1; 
				}
			}
		}
	}
 
    /**
     * update the priorities of all the courses in the provided courses arraylist
     *
     * @param courses
     */
    private void updatePriority(ArrayList<Course> courses) {
        for (int i = 0; i < courses.size(); i++) {
            courses.get(i).updatePriority();
        }
    }

	private void updatePossibleCoursesForSemesters(){
		for(Semester s: this.semesters){
			s.clearPossibleCourses();
		}
		for(Course c: this.required){
			if( c!= null && !c.getType().equals("CREDITS")){
				int[] poss = c.getPossibleSemesters();
				for(int i : poss){
					this.semesters.get(i-1).addPossibleCourse(c.getCourseID());
				}
			}
		}
	}
	/**
	 * sort the provided courses arraylist by priority
	 *
	 * @param courses
	 */
    private void sortByPriority(ArrayList<Course> courses) {
        //TODO : Use a different/more efficient sort technique
		Course[] coursesArray = new Course[courses.size()];
		courses.toArray(coursesArray);

		// sort
		for(int i = 0; i < coursesArray.length; i++){
			for(int j = i+1; j < coursesArray.length; j++){
				int comparison = coursesArray[i].compareTo(coursesArray[j]);
				if(comparison < 0){
					Course c1 = coursesArray[i];
					coursesArray[i] = coursesArray[j];
					coursesArray[j] = c1;


                }else if(comparison == 0){
					// tie breaker = number of possible semesters ; the one
					// that can be added in fewer semesters should be added
					// in first 
					if(coursesArray[i].getPossibleSemesters().length
							> coursesArray[j].getPossibleSemesters().length){
						Course c1 = coursesArray[i];
						coursesArray[i] = coursesArray[j];
						coursesArray[j] = c1;

					}
				}
            }
        }

		courses.clear();
		for(Course c: coursesArray){
			courses.add(c);
		}
    }

  
    
    //-----------------------------------------
    // Descriptions & Comparisons
    //-----------------------------------------
    @Override
    public String toString() {
        String returnThis = "Schedule: \tTotal Credits: " + this.totalTakenCredits + "\n";

        for (int i = 0; i < semesters.size(); i++) {
            returnThis += semesters.get(i).toString();
        }
        return returnThis;
    }

	//---------------------------------
	// New
	//---------------------------------

	public void findSchedules(){
		for(Semester s: semesters){
			fillSemester(s);
			if(s.getSemID() == this.totalSemesters){
				saveSchedule(s)
			}
		}
	}


}
