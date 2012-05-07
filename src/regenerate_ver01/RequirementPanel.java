/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package regenerate_ver01;

import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @Author:  Beenish
 * @Date:
 * @Class: 
 * @Project: 
 *
 *
 * @Class Description: 
 */
public class RequirementPanel{
	private String type;
	private int numReq;
	private ArrayList<Course> courses;
	
	private JPanel panel; 
	private ArrayList<CoursePanel> cPanels;

	public RequirementPanel(String type, int numReq, ArrayList<Course> courses){
		this.type = type;
		this.numReq = numReq;
		this.courses = new ArrayList<Course>();
		for(Course c: courses){
			this.courses.add(c.clone());
		}
		this.cPanels = new ArrayList<CoursePanel>();

//		this.panel = new JPanel();
		this.panel = this.getPanel();

	}

	public final JPanel getPanel(){
		JPanel panel = new JPanel();
		GridLayout layout = new GridLayout();
		panel.setLayout(layout);
		layout.setColumns(1);
		layout.setRows(courses.size() + 2);
		if(numReq == 0){
			JLabel reqType = new JLabel(type + "\t\t-- All Courses Required");
			panel.add(reqType);


			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout());
			p2.add(new JLabel("Course ID"));
			p2.add(new JLabel("Taken"));
			p2.add(new JLabel("To Be Taken"));
			p2.add(new JLabel("Placement Test Completed"));

			panel.add(p2);


			for(Course c : courses){

				CoursePanel cp = new CoursePanel(c.getCourseID(), true, c.isPlacementTestPresent());
				panel.add(cp.getCoursePanel());
				cPanels.add(cp);
			}



		}else{
			JLabel reqType = new JLabel(type + "\t\t-- Please Choose " + numReq + " Courses:");
			panel.add(reqType);

			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout());
			p2.add(new JLabel("Course ID"));
			p2.add(new JLabel("Taken"));
			p2.add(new JLabel("To Be Taken"));
			p2.add(new JLabel("Placement Test Completed"));

			panel.add(p2);



			for(Course c : courses){
				CoursePanel cp = new CoursePanel(c.getCourseID(), false, c.isPlacementTestPresent());
				panel.add(cp.getCoursePanel());
				cPanels.add(cp);
			}


		}

		return panel;
	}

	public ArrayList<String> getTakenCourses(){
		ArrayList<String> taken = new ArrayList<String>();
		for(CoursePanel cp : cPanels){
			if(cp.isTaken()){
				taken.add(cp.getCourseID());
			}
		}
		return taken; 
	}

	public boolean isValid(){
		// checks if a valid number and choice of courses have been taken;

		if(numReq == 0 ){
			return true; // radio buttons - can't be false
		}else{
			// checkboxes ... 

			int count = 0;

			for(CoursePanel cp : cPanels){
				// valid
				if(cp.isTaken() || cp.isToBeTaken()){
					count++;
				}

				// invalid selection: both checkboxes are selected- must choose
				// at most one
				if(cp.isTaken() && cp.isToBeTaken()){
					count--; 
				}
			}

			if(count >= numReq){
				return true;
			}else{
				return false; 
			}
		}


	}

	public ArrayList<String> getSelectedCourses(){
		ArrayList<String> selected = new ArrayList<String>();
		for(CoursePanel cp : cPanels){
			if(cp.isToBeTaken()){
				selected.add(cp.getCourseID());
			}
		}
		return selected;
	}
	public String getType(){
		return type;
	}
	public int getNumReq(){
		return numReq; 
	}

	private class CoursePanel{
		private String courseID;
		private boolean required;
		private boolean testPresent;

		private AbstractButton taken;
		private AbstractButton toTake;
		private AbstractButton test;


		public CoursePanel(String courseID, boolean required, boolean testPresent){
			this.courseID = courseID;
			this.required = required;
			this.testPresent = testPresent;

			if(required){
				this.taken = new JRadioButton();
				this.toTake = new JRadioButton();
				this.test = new JRadioButton();

				this.toTake.setSelected(true);
				
				ButtonGroup g = new ButtonGroup();
				g.add(taken);
				g.add(toTake);
				g.add(test);

				if(!testPresent){
					this.test.setEnabled(false);
				}

			}else{
				this.taken = new JCheckBox();
				this.toTake = new JCheckBox();
				this.test = new JCheckBox();

				if(!testPresent){
					this.test.setEnabled(false);
				}
			}
		}

		public JPanel getCoursePanel(){
			JPanel panel = new JPanel();

			panel.setLayout(new GridLayout());
			panel.add(new JLabel(courseID));
			panel.add(taken);
			panel.add(toTake);
			panel.add(test);


			return panel;
		}

		public boolean isTaken(){
			if(taken.isSelected() || test.isSelected()){
				return true;
			}else{
				return false; 
			}
		}

		public boolean isToBeTaken(){
			return toTake.isSelected();
		}

		public String getCourseID(){
			return courseID;
		}

	}


}
