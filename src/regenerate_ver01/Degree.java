package regenerate_ver01;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @Author:  Beenish Jamil, Emily Vorek
 * @Date:
 * @Class:	 CS490
 * @Project: Course Scheduler
 *
 *
 * @Class Description:
 *		This class creates Degree objects that store information about
 *		the requirements of degrees. It requires information on the types of
 *		courses required by the degree as well as the number of courses needed
 *		for each of those requirements. It also requires a catalog of all
 *		courses offered by the institution that are relavent to this degree as
 *		a parameter of its constructor.
 *
 *		A degree object can take in a schedule object as its parameter and
 *		identify whether that schedule fulfills this degree's requirements or not. 
 */
public class Degree {

    //----------------------------------------------------
    // Attributes
    //----------------------------------------------------
    private String title;
    private ArrayList<Course> catalog;
    private ArrayList<ArrayList<Course>> orderedCatalog;
    private Map<String, Integer> typeNumMap; // types of requirements = key; number required = val
    private Map<String, Integer> typePosMap; // types of courses offered = key; position in orderedCatalog = val
    private Map<String, Course> courseDictionary;

    //------------------------------------------------
    // Constructors
    //-------------------------------------------------
    public Degree(String title, Map<String, Integer> typeNumReq,
            ArrayList<Course> catalog, Map<String, Course> courseDictionary) {

        this.title = title;

        //TODO : FIX Shallow clones
        this.catalog = (ArrayList<Course>) catalog.clone();
        this.typeNumMap = new HashMap<String, Integer>();
        this.typeNumMap.putAll(typeNumReq);
        this.orderedCatalog = new ArrayList<ArrayList<Course>>();
        this.typePosMap = new HashMap<String, Integer>();
        // initialize ordered catalog

        this.courseDictionary = new HashMap<String, Course>();
        this.courseDictionary.putAll(courseDictionary);

//        this.orderedCatalog = this.getOrderedCatalog(catalog);
		sortCatalogByType();		// gets ordered catalog
        
		//initializating prerequisite / successor pointers
        addPrereqSuccessorPointers(catalog);

		//initializing corequisite pointers
		addCoreqPointers(this.courseDictionary);

		// updating the priority of all courses
		this.updatePriority(catalog);

    }


 

    //--------------------------------------------------
    // Other Public Methods
    //--------------------------------------------------
    
    public int getNumberOfCourses() {
        return this.catalog.size();
    }


    /**
	 * returns a course dictionary (i.e. 'catalog) for this degree. All pointers
	 * in this catalog are initialized. All pointers point to courses within the
	 * catalog only.
	 *
	 * @return course dictionary
	 */
	public Map<String, Course> getCourseDictionary() {

        Map<String, Course> courseDict = new HashMap<String, Course>();

        for (Course c : this.courseDictionary.values()) {
            Course newCourse = c.clone();
            courseDict.put(newCourse.getCourseID(), newCourse);

        }
        ArrayList<Course> cat = new ArrayList<Course>();
        cat.addAll(courseDict.values());
        addPrereqSuccessorPointers(cat);
		addCoreqPointers(courseDict);
		updatePriority(cat);

        return courseDict;
    }

    /**
     * Checks if all of the requirements of the degree have been met by the
     * provided schedule -- i.e. whether enough courses have been taken to
	 * satisfy each requirement type
     *
     * @param schedule
     * @return boolean value True if requirements met, false if not
     */
    public boolean requirementsMet(Schedule schedule) {

        // initialize a new hashmap to keep count of the number of taken
        // courses of each requirement type
        Map<String, Integer> taken = new HashMap<String, Integer>();

        // get all of the taken courses in the schedule
		ArrayList<String> takenCourses = schedule.getTakenCourses();

        // loop through all of the courses in the schedule and keep a tally of
        // each course type taken in the taken map
		for(String cID: takenCourses){
			Course c = this.courseDictionary.get(cID);
			if(c != null ){
				String type = c.getType();

				if(taken.containsKey(type)){
					taken.put(type, taken.get(type) + 1);
				}
				else{
					taken.put(type, 1);
				}
			}
			else{
				System.out.println("Taken course " + cID + " is not in Degree's catalog");
			}
		}

        // compare taken course numbers with required course numbers
        Iterator iter = this.typeNumMap.keySet().iterator();

        while (iter.hasNext()) {
            String type = (String) iter.next();
            int numReq = this.typeNumMap.get(type);

            // are all required?
            if (numReq == 0) {
                // check if the amount taken and the amount required are the same for this type
                if (taken.get(type) != this.orderedCatalog.get(this.typePosMap.get(type)).size()) {
                    System.out.println("\nRequirement Not Met: " + type);
					System.out.println("Required: " + this.orderedCatalog.get(this.typePosMap.get(type)).size() + " \tTaken : " + taken.get(type));
                    return false;
                }
            } else { // some required -- check if enough taken
                if (taken.get(type) != null){
					if(taken.get(type) < this.typeNumMap.get(type)) {
						// you can take more, just not less
						System.out.println("\nRequirement Not Met: " + type);
						System.out.println("Required: " + this.typeNumMap.get(type) + " \tTaken : " + taken.get(type));
						return false;
					}
                }
				else{
					System.out.println(" \nNo courses of type " + type + " were found in schedule" );
					return false; 
				}
            }
        }
        return true;
    }

    //----------------------------------------------
    // Accessors And Mutators
    //----------------------------------------------
    /**
     * returns the required number of courses for each requirement type
     * @return
     */
    public Map<String, Integer> getReqTypeNums() {
        Map<String, Integer> reqTypeNums = new HashMap<String, Integer>();
        reqTypeNums.putAll(this.typeNumMap);
        return reqTypeNums;
    }

    /**
     * Returns the position of each requirement type array in the ordered catalog
     * @return
     */
    public Map<String, Integer> getReqTypePos() {
        Map<String, Integer> reqTypeNums = new HashMap<String, Integer>();
        reqTypeNums.putAll(this.typePosMap);
        return reqTypeNums;
    }

    /**
     * returns a shallow clone of the catalog ordered/sorted/clustered by requirement type
     * @return
     */
    public ArrayList<ArrayList<Course>> getOrderedCatalog() {
        ArrayList<ArrayList<Course>> tmp = new ArrayList<ArrayList<Course>>();

        for (ArrayList<Course> courses : this.orderedCatalog) {
            tmp.add((ArrayList<Course>) courses.clone());	// shallow clone
        }
        return tmp;
    }

    //----------------------------------------------
    // Private Helper Methods
    //----------------------------------------------
    /**
     * Returns an ordered catalog, a multidimensional arraylist of courses in
     * the catalog , sorted by the type of course (core, elective, etc. )
     *
     * Each position in the arraylist is occupied by another arraylist of course
     * objects, each with a unique course type .
	 *
     */
    private ArrayList<ArrayList<Course>> sortCatalogByType(ArrayList<Course> cat) {
        ArrayList<ArrayList<Course>> orderedCat = new ArrayList<ArrayList<Course>>();
		Map<String, Integer> typePos = new HashMap<String, Integer>();
		
		for (int i = 0; i < cat.size(); i++) {
            Course currCourse = cat.get(i);
            String type = currCourse.getType();

            // check if curr course type is in our ordered catalog
            if (typePos.containsKey(type)) {
                //this type is in our course catalog, add course object in
                orderedCat.get(typePos.get(type)).add(currCourse);
            } else { // curr course type is not in ordered catalog, create a new arraylist for it

                typePos.put(type, orderedCat.size());
                ArrayList<Course> tmp = new ArrayList<Course>();
                tmp.add(currCourse);
                orderedCat.add(tmp);
//				System.out.println("Adding new type: " + type + " new size : " + orderedCat.size());
            }
        }
		return orderedCat;
    }
	private void sortCatalogByType(){

		if(this.typePosMap == null){
			this.typePosMap = new HashMap<String, Integer>();
		}
		if(this.orderedCatalog == null){
			this.orderedCatalog = new ArrayList<ArrayList<Course>>();
		}

		for (int i = 0; i < this.catalog.size(); i++) {

            Course currCourse = this.catalog.get(i);
            String type = currCourse.getType();

            // check if curr course type is in our ordered catalog
            if (this.typePosMap.containsKey(type)) {
                //this type is in our course catalog, add course object in
                this.orderedCatalog.get(this.typePosMap.get(type)).add(currCourse);
            } else { // curr course type is not in ordered catalog, create a new arraylist for it

                this.typePosMap.put(type, this.orderedCatalog.size());
                ArrayList<Course> tmp = new ArrayList<Course>();
                tmp.add(currCourse);
                this.orderedCatalog.add(tmp);
            }
        }

	}


	private void updatePriority(ArrayList<Course> courses) {
		for (int i = 0; i < courses.size(); i++) {
			courses.get(i).updatePriority();
		}
	}

	//.......................
	// Pointer Manipulation
	//.......................

    /**
     * For all of the courses in the provided Course ArrayList, this method
     * creates pointers to their required prerequisite and successors
     * courses, if they exist in our catalog.
     *
     * The required prerequisite and successor information is taken from
     * the prerequisites and successors attributes .
     *
     * @param courses
     */
    private void addPrereqSuccessorPointers(ArrayList<Course> courses) {

        for (int i = 0; i < courses.size(); i++) {
            // get prereq info for each course
            Course currCourse = courses.get(i);
            String[] prereqs = currCourse.getPrerequisites();

           // System.out.println("Adding prereq for course " + courses.get(i).getCourseID());
            for (int j = 0; j < prereqs.length; j++) {	// loop through prerequisites of currCourse
                // if prereq exists, add a pointer to each prereq
                int k = 0;
                int added = 0;
                int addedS = 0;

				if(!prereqs[j].startsWith("CREDITS")){

					//System.out.println("Trying to add " + prereqs[j]);
					while (k < courses.size() && added == 0) {
					 // loop through all courses to find prereq course

						//System.out.println("TEST :)" + " k = " + k);
						Course prevCourse = courses.get(k);


						if (prevCourse.getCourseID().equals(prereqs[j])) {
							added = currCourse.addPrereqCourse(prevCourse);	// add pter from curr course to prev course
							addedS = prevCourse.addSuccessorCourse(currCourse); // add pter from prev course to curr course
						}
						k++;

					}

					if (added == 1 && addedS == 1) {} else {
						System.out.println("\tCould not find prerequisite course " + prereqs[j] + " for course " + currCourse.getCourseID());
					}
				}
            }
        }

//        System.out.println("Leaving this method!");
    }


	private void addCoreqPointers(Map<String, Course> catMap){
		ArrayList<Course> courses = new ArrayList<Course>();
		courses.addAll(catMap.values());
		ArrayList<ArrayList<Course>> orderedCat = this.sortCatalogByType(courses);
		Map<String, Integer> typePos = new HashMap<String, Integer>();
		for(int i = 0; i < orderedCat.size(); i++){
			typePos.put(orderedCat.get(i).get(0).getType(), i);
		}
		for(int i = 0 ; i < courses.size() ; i++){
			// get coreq info for each course
			Course currCourse = courses.get(i);
			ArrayList<String> coreqs = currCourse.getCorequisites();

			for(int j  = 0; j < coreqs.size() ; j ++){
				// loop through all coreqs
				// if a coreq exists, add a pointer to it
				String courseID = coreqs.get(j);

				Course coreqCourse = catMap.get(courseID);

				if(coreqCourse == null){
					// it is a requirement -- assuming all courses in that
					//							requirement are corequisites to this course
					int num = this.typeNumMap.get(courseID);
					if(num != 0){
						System.out.println("Error -- don't know what to do with " +
								"corequisite "  + courseID + " for course "
								+ currCourse.getCourseID());
						break;	// move on to the next requirement type
					}

					int pos = typePos.get(courseID);
					ArrayList< Course > requirement = orderedCat.get(pos);
					for(int k = 0; k < requirement.size(); k++ ){
						Course coreq = requirement.get(k);
						currCourse.addCoreqCourse(coreq);

						int addedS = coreq.addCoreqSuccessorCourse(currCourse);
						if(addedS == 0){
							System.out.println("Error -- Could not add successor course "
									+ currCourse.getCourseID() + " to " + requirement.get(k).getCourseID());
						}
					}
					currCourse.removeCoreqCourse(courseID); // it's not a real course, so remove it
				}
				else{
					// it's a single course, add it  -- unless it's a credits dummy course
					if(!courseID.equals("CREDITS")){
//												System.out.println("Adding coreq " + courseID);
						currCourse.addCoreqCourse(coreqCourse);
						coreqCourse.addCoreqSuccessorCourse(currCourse);
					}
				}
			}
		}
	}



    //--------------------------------------------------
    // Descriptions & Comparisons
    //--------------------------------------------------
    /**
     * Returns a string representation of the input catalog
     *
     * @return
     */
    public String catalogToString() {
        String returnThis = this.title + "\n";

        for (int i = 0; i < this.orderedCatalog.size(); i++) {
            ArrayList<Course> tmp = this.orderedCatalog.get(i);
            returnThis += "\nType: " + tmp.get(0).getType();
            for (int j = 0; j < tmp.size(); j++) {
                returnThis += "\n\t" + tmp.get(j).toString();

            }
        }
        return returnThis;
    }
}
