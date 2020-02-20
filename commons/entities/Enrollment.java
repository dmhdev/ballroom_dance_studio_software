/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.entities;

import static commons.helpers.ServerHelper.connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author daynehammes
 */
public class Enrollment {

    private String enrollmentID;

    public Enrollment(String enrollmentID) {
        this.enrollmentID = enrollmentID;
    }

    // Delete selected lesson
    public void deleteSelectedEnrollment() throws SQLException, ClassNotFoundException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really delete enrollment? This will delete all associated Lessons, Payments and Bonuses.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            try {

                // Delete from Program Enrollment
                connection.prepareStatement("delete from ProgramEnrollment where EnrollmentID='" + enrollmentID + "';").execute();

                // Delete from Lesson Schedule
                connection.prepareStatement("delete from LessonSchedule where EnrollmentID='" + enrollmentID + "';").execute();

                // Delete from Payment Transactions
                connection.prepareStatement("delete from PaymentTransaction where EnrollmentID='" + enrollmentID + "';").execute();

                // Delete from Bonus Transactions
                connection.prepareStatement("delete from BonusTransaction where FromEnrollmentID='" + enrollmentID + "' OR UsedOnEnrollmentID='" + enrollmentID + "' ;").execute();

                ResultSet deletedSet = connection.prepareStatement("select EnrollmentID from ProgramEnrollment where EnrollmentID='" + enrollmentID + "';").executeQuery();

                // If the set is empty it was successful
                if (deletedSet.next()) {
                    JOptionPane.showMessageDialog(null, "There was a problem deleting enrollment. Please try again.",
                            "Delete Enrollment Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Successfully deleted enrollment with EnrollmentID: " + enrollmentID + ".",
                            "Deleted Enrollment Record", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // Update Program Enrollment Attended, Paid and Owed
    public void updateProgramEnrollment(String lessonType, double lessonUnits, double paymentTotal) throws SQLException {

        // Get program enrollment object
        ResultSet enrollmentSet = connection.prepareStatement("select ContractPaid,PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,"
                + "PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice,OwesPayment from ProgramEnrollment where EnrollmentId='" + enrollmentID + "';").executeQuery();
        enrollmentSet.next();

        // Get required variables
        double previousAmountPaid = enrollmentSet.getDouble(1);
        double privateAttended = enrollmentSet.getDouble(2);
        double groupAttended = enrollmentSet.getDouble(3);
        double partyAttended = enrollmentSet.getDouble(4);
        double privatePrice = enrollmentSet.getDouble(5);
        double groupPrice = enrollmentSet.getDouble(6);
        double partyPrice = enrollmentSet.getDouble(7);
        boolean owesPayment = enrollmentSet.getBoolean(8);

        // Calculate new amount paid
        double newAmountPaid = previousAmountPaid + paymentTotal;

        double[] newAmounts = new double[4];

        // Calculate new amounts owed and new lessons attended
        if ((lessonType != null) && !(lessonType.equals("null")) && !(lessonType.equals("None")) && !(lessonType.equals(""))) {
            newAmounts = getNewAmountOwed(lessonUnits, privateAttended, groupAttended, partyAttended, privatePrice, groupPrice, partyPrice, lessonType);
            double newAmountOwed = newAmounts[0];
            double newPrivateAttended = newAmounts[1];
            double newGroupAttended = newAmounts[2];
            double newPartyAttended = newAmounts[3];

            // Update owesPayment on Program Enrollment
            if (newAmountPaid < newAmountOwed) {
                owesPayment = true;
            } else {
                owesPayment = false;
            }

            // Update new values in database
            connection.prepareStatement(String.format("UPDATE ProgramEnrollment SET ContractPaid=%f,PrivateLessonAttended=%f,GroupLessonAttended=%f,"
                    + "PartyLessonAttended=%f,OwesPayment=%b WHERE EnrollmentID='%s';",
                    newAmountPaid, newPrivateAttended, newGroupAttended, newPartyAttended, owesPayment, enrollmentID)).execute();

        } else {

            // Only update amount owed and paid
            double newAmountOwed = (privatePrice * privateAttended) + (groupPrice * groupAttended) + (partyPrice * partyAttended);

            if (newAmountPaid < newAmountOwed) {
                owesPayment = true;
            } else {
                owesPayment = false;
            }

            // Update new values in database
            connection.prepareStatement(String.format("UPDATE ProgramEnrollment SET ContractPaid=%f,OwesPayment=%b WHERE EnrollmentID='%s';",
                    newAmountPaid, owesPayment, enrollmentID)).execute();
        }
    }

    // Update Last Lesson status
    public void updateProgramCompleted() throws SQLException {

        // Get private lesson variables
        ResultSet resultSet = connection.prepareStatement(
                "select PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,PrivateLessonTotal,GroupLessonTotal,PartyLessonTotal"
                + " from ProgramEnrollment where EnrollmentID='" + enrollmentID + "';").executeQuery();
        resultSet.next();

        double lessonAttended = (resultSet.getDouble(1) + resultSet.getDouble(2) + resultSet.getDouble(3));
        double lessonTotal = (resultSet.getDouble(4) + resultSet.getDouble(5) + resultSet.getDouble(6));
        double lessonRemaining = lessonTotal - lessonAttended;

        // Check if last lesson
        boolean programCompleted = false;
        if (lessonRemaining <= 1) {
            programCompleted = true;
        }

        if (programCompleted) {

            // Get date of last lesson
            DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateProgramCompleted = getDateOfLastLesson();
            
            // Update all lessons with enrollment - used previous to program completed variable addition and it breaks things if removed
            connection.prepareStatement(String.format("UPDATE LessonSchedule SET LastLesson='%s' where EnrollmentID='%s';", programCompleted, enrollmentID)).execute();
            
            // Update program completed status
            connection.prepareStatement(String.format("UPDATE ProgramEnrollment SET IsCompleted='%s', DateCompleted='%s' where EnrollmentID='%s';", programCompleted, sqlDateFormat.format(dateProgramCompleted), enrollmentID)).execute();
        }

    }
    
    private Date getDateOfLastLesson() throws SQLException {
        
        List<Date> datesList = new ArrayList();
        ResultSet enrollmentLessonsSet = connection.prepareStatement(String.format("SELECT AppointmentDate FROM LessonSchedule WHERE EnrollmentID='%s'"
                + " ORDER BY AppointmentDATE ASC;", enrollmentID)).executeQuery();
        
        while (enrollmentLessonsSet.next()) {
            
            datesList.add(enrollmentLessonsSet.getDate(1));
            
        }
        
        return Collections.max(datesList);
    }

    // Check if Lesson attendance exceeds program enrollment
    public boolean programAttendanceLimitReached(String lessonType, double lessonUnits, String programID) throws SQLException {

        boolean exceedsLimit = false;

        // Get enrollment variables to confirm attendance unit count
        ResultSet enrollmentSet = connection.prepareStatement(
                "select EN.PrivateLessonAttended,EN.GroupLessonAttended,EN.PartyLessonAttended,EN.PrivateLessonTotal,EN.GroupLessonTotal,EN.PartyLessonTotal,PR.UnlimitedLessons "
                + "from ProgramEnrollment as EN INNER JOIN Programs as PR ON EN.ProgramID=PR.ProgramID where EN.EnrollmentID='" + enrollmentID + "';").executeQuery();
        enrollmentSet.next();

        // Get enrollment variables
        double privateLessonsRemaining = enrollmentSet.getDouble(4) - enrollmentSet.getDouble(1);
        double groupLessonsRemaining = enrollmentSet.getDouble(5) - enrollmentSet.getDouble(2);
        double partyLessonsRemaining = enrollmentSet.getDouble(6) - enrollmentSet.getDouble(3);
        boolean unlimitedLessons = enrollmentSet.getBoolean(7);

        // If unlimited lessons, always return false
        if (unlimitedLessons) {
            return exceedsLimit;
        }

        // Check lesson type and amounts
        if (lessonType.equals("Private")) {

            if (lessonUnits > privateLessonsRemaining) {
                exceedsLimit = true;
            }

        } else if (lessonType.equals("Group")) {

            if (lessonUnits > groupLessonsRemaining) {
                exceedsLimit = true;
            }

        } else if (lessonType.equals("Party")) {

            if (lessonUnits > partyLessonsRemaining) {
                exceedsLimit = true;
            }

        }

        return exceedsLimit;

    }

    // For scheduled lessons, checks if the new lesson is the first in that enrollment
    public boolean isFirstLesson() throws SQLException {

        // Get enrollment variables to confirm attendance unit count
        ResultSet enrollmentSet = connection.prepareStatement(
                "select LessonID from LessonSchedule where EnrollmentID='" + enrollmentID + "';").executeQuery();

        if (enrollmentSet.next()) {
            return false;
        }
        return true;
    }

    // Calculate new amount owed after processing
    public double[] getNewAmountOwed(double lessonUnits, double privateAttended, double groupAttended, double partyAttended,
            double privatePrice, double groupPrice, double partyPrice, String lessonType) {

        double newAmounts[] = new double[4];
        double newAmountOwed = 0.0;

        // Calculate new total amount owed
        if (lessonType.equals("Private")) {

            // Create new amount of private lessons attended
            privateAttended = privateAttended + lessonUnits;

        } else if (lessonType.equals("Group")) {

            // Create new amount of group lessons attended
            groupAttended = groupAttended + lessonUnits;

        } else {

            // Create new amount of party lessons attended
            partyAttended = partyAttended + lessonUnits;

        }

        // Calculate amount owed
        newAmountOwed = (privatePrice * privateAttended) + (groupPrice * groupAttended) + (partyPrice * partyAttended);

        // Add values to new Amounts array
        newAmounts[0] = newAmountOwed;
        newAmounts[1] = privateAttended;
        newAmounts[2] = groupAttended;
        newAmounts[3] = partyAttended;

        return newAmounts;

    }

}
