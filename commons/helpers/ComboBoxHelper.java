/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.helpers;

import static commons.helpers.ServerHelper.connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author daynehammes
 */
public class ComboBoxHelper {

    /*
     Program List Methods
     */
    // Create Program List
    public void populateProgramList(List<String[]> programArrayList, JList programList) throws SQLException, ClassNotFoundException {

        try {

            // Get program data set
            ResultSet programSet = connection.prepareStatement("select ProgramID,ProgramName,RatePrivate,RateGroup,RateParty,DefaultBonusesAwardedPrivate,"
                    + " DefaultBonusesAwardedGroup, DefaultBonusesAwardedParty, DefaultLessonsPrivate,DefaultLessonsGroup,DefaultLessonsParty,ProgramGroup from Programs where Active=TRUE;").executeQuery();

            // Iterate through programs
            DefaultListModel programListModel = new DefaultListModel();
            while (programSet.next()) {

                // Set program variables
                String programID = programSet.getString(1);
                String programName = programSet.getString(2);
                String programRatePrivate = programSet.getString(3);
                String programRateGroup = programSet.getString(4);
                String programRateParty = programSet.getString(5);
                String programBonusPrivate = programSet.getString(6);
                String programBonusGroup = programSet.getString(7);
                String programBonusParty = programSet.getString(8);
                String programLessonsPrivate = programSet.getString(9);
                String programLessonsGroup = programSet.getString(10);
                String programLessonsParty = programSet.getString(11);
                String programGroup = programSet.getString(12);

                // Create details array and add to programArrayList
                String[] currentProgramDetails = new String[]{programID, programName, programRatePrivate, programRateGroup, programRateParty,
                    programBonusPrivate, programBonusGroup, programBonusParty, programLessonsPrivate, programLessonsGroup, programLessonsParty, programGroup};
                programArrayList.add(currentProgramDetails);

                // Add programName to Program List
                programListModel.addElement(String.format("%s - %s", programID, programName));
            }

            // Set Program List Model and apply Selection Listener
            programList.setModel(programListModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     Instructor ComboBox Methods
     */
    // Create instructors list
    public void populateInstructorListAndComboBox(ArrayList<String> instructorArrayList, JComboBox instructorSelect, boolean addNoneOption) throws SQLException, ClassNotFoundException {

        try {

            // Check if 'None' should be added to beginning of list
            if (addNoneOption) {
                instructorSelect.addItem("None");
            }

            // Get instructor data set
            ResultSet instructorSet = connection.prepareStatement(
                    "select InstructorID,FName from Instructors where Active=TRUE ORDER BY SchedulePriority ASC ;").executeQuery();

            // Iterate over instructor set
            while (instructorSet.next()) {

                // Add items to arraylist and select box
                String currentInstructor = instructorSet.getString(1);
                instructorArrayList.add(currentInstructor);
                instructorSelect.addItem(instructorSet.getString(2));

            }

            // Set selectbox to first instructor
            instructorSelect.setSelectedIndex(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Get student's teachers and assign to combo box
    public void setStudentDefaultTeachers(ArrayList<String> instructorArrayList, JComboBox instructorSelect, JComboBox buddyInstructorSelect, JComboBox buddyInstructorSelect2, String studentID) {

        // Get student's default instructor
        try {

            // Get student data set
            ResultSet studentSet = connection.prepareStatement(
                    "select InstructorID,InstructorID2,InstructorID3 from Students where StudentID='" + studentID + "';").executeQuery();

            // Iterate over student set
            while (studentSet.next()) {

                // Set instructor by id
                String instructorID = studentSet.getString(1);
                String instructorID2 = studentSet.getString(2);
                String instructorID3 = studentSet.getString(3);
                setSelectedComboBoxInstructor(instructorArrayList, instructorSelect, instructorID, false);
                setSelectedComboBoxInstructor(instructorArrayList, buddyInstructorSelect, instructorID2, true);
                setSelectedComboBoxInstructor(instructorArrayList, buddyInstructorSelect2, instructorID3, true);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Assign select box to correct instructor list value
    public void setSelectedComboBoxInstructor(ArrayList<String> instructorArrayList, JComboBox instructorSelect, String instructorID, boolean comboBoxHasNoneOption) {

        // Iterate over values in instructorArrayList
        for (int i = 0; i < instructorArrayList.size(); i++) {

            // Look for instructorID match
            if (instructorID.equals(instructorArrayList.get(i))) {

                // Set selected index in instructor combobox
                if (comboBoxHasNoneOption) {
                    instructorSelect.setSelectedIndex(i + 1);
                } else {
                    instructorSelect.setSelectedIndex(i);
                }
                break;

            }
        }
    }

    // Get selected instructor index
    public String getSelectedInstructorID(JComboBox instructorSelect, ArrayList<String> instructorArrayList, boolean hasNoneValue) {

        int selectedIndex = instructorSelect.getSelectedIndex();

        // Check for None selection
        if (hasNoneValue) {
            if (selectedIndex == 0) {
                return null;
            } else {
                return instructorArrayList.get(selectedIndex - 1);
            }
        }

        return instructorArrayList.get(selectedIndex);

    }

    // Return Instructor Name from Combo Box
    public String getSelectedInstructorName(JComboBox instructorSelect, ArrayList<String> instructorArrayList) {

        // Get selected index
        int selectedIndex = instructorSelect.getSelectedIndex();

        // Check if 'None' is an option in ComboBox
        if (instructorSelect.getItemAt(0).toString().equals("None")) {

            // If so, check if None is selected
            if (selectedIndex == 0) {
                return "None";
            } else {
                return instructorArrayList.get(selectedIndex - 1);
            }
        } else {
            return instructorArrayList.get(instructorSelect.getSelectedIndex());
        }

    }

    /*
     Referral ComboBox Methods
     */
    // Create Referral Types List from Database
    public void populateReferralTypeComboBox(JComboBox referralTypeSelect) {

        try {

            referralTypeSelect.removeAllItems();
            referralTypeSelect.addItem("None");
            referralTypeSelect.addItem("Student");

            // Get list of referral types
            ResultSet referralTypeResults = connection.prepareStatement(
                    "select ReferralType from ReferralType order by ReferralType asc;").executeQuery();

            // Iterate over referral types results
            while (referralTypeResults.next()) {

                // Add referral type to jcombobox
                referralTypeSelect.addItem(referralTypeResults.getString(1));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Create Student ReferralList list from Database
    public void populateStudentReferralListAndComboBox(ArrayList<String[]> referralArrayList, JComboBox referralSelect) throws SQLException, ClassNotFoundException {

        // add filler for first item
        String[] fillerStudent = {"None", "None", "None"};
        referralArrayList.add(fillerStudent);
        referralSelect.addItem("None");

        try {

            // Get list of students
            ResultSet studentSet = connection.prepareStatement(
                    "select StudentID,FName,LName from Students order by LName asc;").executeQuery();

            // Iterate over student set
            while (studentSet.next()) {

                // add items to arraylist and selectbox
                String[] currentStudent = {studentSet.getString(1), studentSet.getString(2), studentSet.getString(3)};
                referralArrayList.add(currentStudent);

                referralSelect.addItem(studentSet.getString(3) + ", " + studentSet.getString(2));

            }

            // Set selectbox to student's instructor
            referralSelect.setSelectedIndex(0);

            // Setup auto complete for referral
            AutoCompleteDecorator.decorate(referralSelect);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setComboBoxStudentReferrerAndType(ArrayList<String[]> studentReferralArrayList, JComboBox referredBySelect, JComboBox studentReferrerSelect,
            String studentID) throws SQLException {

        // Get referrer id from student id
        ResultSet studentSet = connection.prepareStatement("SELECT ReferralType,ReferrerID from Students WHERE StudentID='" + studentID + "';").executeQuery();

        if (studentSet.next()) {

            String referralType = studentSet.getString(1);
            String referrerStudentID = studentSet.getString(2);

            if (referralType != null && !referralType.equals("None")) {
                for (int x = 0; x < referredBySelect.getItemCount(); x++) {

                    // Check for referral type match
                    if (referralType.equals(referredBySelect.getItemAt(x))) {
                        referredBySelect.setSelectedIndex(x);
                        break;
                    }
                }

                // Check for student referrer match
                if (referralType.equals("Student")) {

                    for (int x = 0; x < studentReferralArrayList.size(); x++) {

                        if (studentReferralArrayList.get(x)[0].equals(referrerStudentID)) {
                            studentReferrerSelect.setSelectedIndex(x);
                            break;
                        }
                    }
                }

            }
        }
    }

    // Assign select box to correct referral list value
    public void setComboBoxReferralNone(ArrayList<String[]> referralArrayList, JComboBox referralSelect, String referrerID) {

        // Iterate over values in instructorArrayList
        for (int i = 0; i < referralArrayList.size(); i++) {

            // Look for instructorID match
            if (referrerID.equals(referralArrayList.get(i)[0])) {

                // Set selected index in instructor combobox
                referralSelect.setSelectedIndex(i);
                break;

            }
        }
    }

    // Get selected referral combobox with None item
    public String getSelectedComboBoxReferrerIndexNone(JComboBox referralSelect, ArrayList<String[]> referralArrayList) {

        // Get selected index
        int selectedIndex = referralSelect.getSelectedIndex();

        // Check if 'None' is selected
        if (selectedIndex == 0) {
            return "None";
        } else {
            return referralArrayList.get(selectedIndex - 1)[0];
        }
    }

    /*
     Time ComboBox Methods
     */
    // Update End Time ComboBox to next 45 minute
    public void updateEndTime(JComboBox startTimeHourSelect, JComboBox startTimeMinuteSelect, JComboBox endTimeHourSelect, JComboBox endTimeMinuteSelect) throws ParseException {

        // Get current start time
        String startTime = startTimeHourSelect.getSelectedItem().toString() + ":" + startTimeMinuteSelect.getSelectedItem().toString();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date startDateTime = dateFormat.parse(startTime);

        // Calculate end time
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDateTime);
        cal.add(Calendar.MINUTE, 45);
        String endTimeHour = dateFormat.format(cal.getTime()).substring(0, 2);
        String endTimeMinute = dateFormat.format(cal.getTime()).substring(3);

        // Set end time comboboxes
        endTimeHourSelect.setSelectedItem(endTimeHour);
        endTimeMinuteSelect.setSelectedItem(endTimeMinute);

    }

}
