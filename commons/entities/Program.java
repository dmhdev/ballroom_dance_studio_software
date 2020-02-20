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
public class Program {
    
    private String programID;

    public Program(String programID) {
        this.programID = programID;
    }
    
      public void deleteProgram() throws SQLException {

        // Delete Bonus Transactions
        ResultSet bonusResultSet = connection.prepareStatement("SELECT BT.TransactionID FROM BonusTransaction as BT INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentId=BT.FromEnrollmentID"
                + " WHERE EN.ProgramID='" + programID + "';").executeQuery();    
        while (bonusResultSet.next()) {
            System.out.println(bonusResultSet.getString(1));
            connection.prepareStatement("DELETE FROM BonusTransaction WHERE TransactionID='" + bonusResultSet.getString(1) + "';").execute();
        }
        
        // Delete Payment Transactions
        ResultSet paymentResultSet = connection.prepareStatement("SELECT PT.PaymentID FROM PaymentTransaction as PT INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentId=PT.EnrollmentID"
                + " WHERE EN.ProgramID='" + programID + "';").executeQuery();    
        while (paymentResultSet.next()) {
            
            connection.prepareStatement("DELETE FROM PaymentTransaction WHERE PaymentID='" + paymentResultSet.getString(1) + "';").execute();
        }


        // Delete Lessons
        ResultSet lessonResultSet = connection.prepareStatement("SELECT LS.LessonID FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentId=LS.EnrollmentID"
                + " WHERE EN.ProgramID='" + programID + "';").executeQuery();    
        while (lessonResultSet.next()) {
            
            connection.prepareStatement("DELETE FROM LessonSchedule WHERE LessonID='" + lessonResultSet.getString(1) + "';").execute();
        }

        // Delete Enrollments
        connection.prepareStatement("DELETE FROM ProgramEnrollment WHERE ProgramID='" + programID + "';").execute();
        
        
        // Delete Program
        connection.prepareStatement("DELETE FROM Programs WHERE ProgramID='" + programID + "';").execute();

        ResultSet deletedSet = connection.prepareStatement("select ProgramID from Programs where ProgramID='" + programID + "';").executeQuery();

        // If the set is empty it was successful
        if (deletedSet.next()) {
            JOptionPane.showMessageDialog(null, "There was a problem deleting program. Please try again.",
                    "Delete Program Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Successfully deleted program with Program ID: " + programID + ".",
                    "Deleted Program Record", JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
