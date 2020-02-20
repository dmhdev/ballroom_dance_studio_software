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
public class Student {

    private String studentID;

    public Student(String studentID) {
        this.studentID = studentID;
    }

    // Schedule student lesson
    public String scheduleStudentLesson(String lessonType, String instructorName, String appointmentDate, String appointmentStartTime,
            String appointmentEndTime, String notes, String lessonCode, String instructorID, String programID, String enrollmentID, String studentID,
            double lessonUnits, String lessonStatus, String paymentStatus) throws ClassNotFoundException, SQLException {

        String insertSuccess = "Failed";

        // Get instructor priorityResultSet
        ResultSet instructorPrioritySet = connection.prepareStatement(
                "select SchedulePriority from Instructors where InstructorID='" + instructorID + "';").executeQuery();
        instructorPrioritySet.next();
        int instructorPriority = instructorPrioritySet.getInt(1);

        // Get Student Name
        ResultSet studentNameSet = connection.prepareStatement(
                "select FName,LName from Students where StudentID='" + studentID + "';").executeQuery();
        studentNameSet.next();
        String studentName = studentNameSet.getString(1) + " " + studentNameSet.getString(2);

        // Set lesson column type to get price
        String lessonTypeColumn = "";
        if (lessonType.equals("Private")) {
            lessonTypeColumn = "PrivateLessonPrice";
        } else if (lessonType.equals("Group")) {
            lessonTypeColumn = "GroupLessonPrice";
        } else if (lessonType.equals("Party")) {
            lessonTypeColumn = "PartyLessonPrice";
        }

        // Get lesson price and last lesson boolean
        ResultSet resultSet = connection.prepareStatement(
                "select PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,PrivateLessonTotal,GroupLessonTotal,PartyLessonTotal,"
                + lessonTypeColumn + " from ProgramEnrollment where EnrollmentID='" + enrollmentID
                + "';").executeQuery();
        resultSet.next();
        double lessonAttended = (resultSet.getDouble(1) + resultSet.getDouble(2) + resultSet.getDouble(3));
        double lessonTotal = (resultSet.getDouble(4) + resultSet.getDouble(5) + resultSet.getDouble(6));
        double lessonRemaining = lessonTotal - lessonAttended;
        double lessonPrice = resultSet.getDouble(7);

        // Check if last lesson
        boolean lastLesson = false;
        if (lessonRemaining <= 1) {
            lastLesson = true;
        }

        // Insert lesson in LessonSchdeule
        connection.prepareStatement(String.format(
                "insert into LessonSchedule(StudentID,EnrollmentID,ProgramID,RateType,StudentName,InstructorName,AppointmentDate,AppointmentTimeStart,"
                + " AppointmentTimeEnd,LessonUnits,Notes,LessonCode,InstructorID,LessonPrice,InstructorPriority,LastLesson,LessonStatus,PaymentStatus)"
                + " values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%f,%d,%b,'%s','%s');",
                studentID, enrollmentID, programID, lessonType, studentName, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                lessonUnits, notes, lessonCode, instructorID, lessonPrice, instructorPriority, lastLesson, lessonStatus, paymentStatus)).execute();

        // Flag successful inster
        insertSuccess = "Success";

        return insertSuccess;

    }

    // Process Payment Record
    public void createPaymentRecord(String studentID, String paymentType, double paymentTotal,
            double lessonUnits, String enrollmentID, String lessonType, String paymentDate) throws SQLException {

        // Add Payment Record
        connection.prepareStatement(String.format("insert into Payments(StudentID,PaymentType,Amount,LessonUnits,EnrollmentID,LessonType,DateCreated) Values "
                + "('%s','%s',%f,%f,'%s','%s','%s');", studentID, paymentType, paymentTotal, lessonUnits, enrollmentID, lessonType, paymentDate))
                .execute();

    }

    // Process Bonus Record
    public void createBonusRecord(String studentID, String lessonID, String programID, String lessonType, double lessonUnits, double oldBonusAvail,
            double newBonusAvail, String bonusDate) throws SQLException {

        // Get bonus column for Students table update
        String lessonTypeBonusColumn = "BonusPrivateBalance";
        if (lessonType.equals("Group")) {
            lessonTypeBonusColumn = "BonusGroupBalance";
        } else if (lessonType.equals("Party")) {
            lessonTypeBonusColumn = "BonusPartyBalance";
        }

        // Add Bonus Record
        connection.prepareStatement(String.format("insert into Bonuses(StudentID,LessonID,ProgramID,UpdateType,LessonType,LessonUnits,OldBonusAvail,NewBonusAvail,DateCreated)"
                + " Values ('%s','%s','%s','BonusAttend','%s',%f,%f,%f,'%s');", studentID, lessonID, programID, lessonType, lessonUnits, oldBonusAvail, newBonusAvail, bonusDate))
                .execute();

        // Update amount of bonus in Students Record
        connection.prepareStatement(String.format("UPDATE Students SET %s=%f WHERE StudentID='%s';", lessonTypeBonusColumn, newBonusAvail, studentID)).execute();
    }

    public void deleteStudent() throws SQLException {

        // Delete Bonus Transactions
        connection.prepareStatement("delete from BonusTransaction where StudentID='" + studentID + "';").execute();

        // Delete Payment Transactions
        connection.prepareStatement("delete from PaymentTransaction where StudentID='" + studentID + "';").execute();

        // Delete Lessons
        connection.prepareStatement("delete from LessonSchedule where StudentID='" + studentID + "';").execute();

        // Delete Enrollments
        connection.prepareStatement("delete from ProgramEnrollment where StudentID='" + studentID + "';").execute();

        // Delete Student
        connection.prepareStatement("delete from Students where StudentID='" + studentID + "';").execute();

        ResultSet deletedSet = connection.prepareStatement("select StudentID from Students where StudentID='" + studentID + "';").executeQuery();

        // If the set is empty it was successful
        if (deletedSet.next()) {
            JOptionPane.showMessageDialog(null, "There was a problem deleting student. Please try again.",
                    "Delete Student Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Successfully deleted student with Student ID: " + studentID + ".",
                    "Deleted Student Record", JOptionPane.INFORMATION_MESSAGE);
        }

    }
    
}
