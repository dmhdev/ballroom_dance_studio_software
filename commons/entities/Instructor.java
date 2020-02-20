/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.entities;

import static commons.helpers.ServerHelper.connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author daynehammes
 */
public class Instructor {
    
    private String instructorID;

    public Instructor(String instructorID) {
        this.instructorID = instructorID;
    }
    
      public void deleteInstructor() throws SQLException {

        // Delete Bonus Transactions
        connection.prepareStatement("UPDATE BonusTransaction SET ReferringStudentCurrentInstructorID='None' WHERE ReferringStudentCurrentInstructorID='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE BonusTransaction SET ReferringStudentCurrentInstructorID2='None' WHERE ReferringStudentCurrentInstructorID2='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE BonusTransaction SET ReferringStudentCurrentInstructorID3='None' WHERE ReferringStudentCurrentInstructorID3='" + instructorID + "';").execute();


        // Delete Lessons
        connection.prepareStatement("UPDATE LessonSchedule SET InstructorID='None' WHERE InstructorID='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE LessonSchedule SET InstructorID2='None' WHERE InstructorID2='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE LessonSchedule SET InstructorID3='None' WHERE InstructorID3='" + instructorID + "';").execute();

        // Delete Enrollments
        connection.prepareStatement("UPDATE ProgramEnrollment SET PrimaryInstructorID='None' WHERE PrimaryInstructorID='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE ProgramEnrollment SET InstructorID1='None' WHERE InstructorID1='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE ProgramEnrollment SET InstructorID2='None' WHERE InstructorID2='" + instructorID + "';").execute();

        // Delete Student Instructor
        connection.prepareStatement("UPDATE Students SET InstructorID='None' WHERE InstructorID='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE Students SET InstructorID2='None' WHERE InstructorID2='" + instructorID + "';").execute();
        connection.prepareStatement("UPDATE Students SET InstructorID3='None' WHERE InstructorID3='" + instructorID + "';").execute();
        
        
        // Delete Instructor
        connection.prepareStatement("DELETE FROM Instructors WHERE InstructorID='" + instructorID + "';").execute();

        ResultSet deletedSet = connection.prepareStatement("select InstructorID from Instructors where InstructorID='" + instructorID + "';").executeQuery();

        // If the set is empty it was successful
        if (deletedSet.next()) {
            JOptionPane.showMessageDialog(null, "There was a problem deleting instructor. Please try again.",
                    "Delete Instructor Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Successfully deleted instructor with Instructor ID: " + instructorID + ".",
                    "Deleted Instructor Record", JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
