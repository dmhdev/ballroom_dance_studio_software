/*
 * Represents an individual Student/New Student Lesson.
 */
package commons.entities;

import commons.helpers.GenericHelper;
import commons.helpers.ServerHelper;
import static commons.helpers.ServerHelper.connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author daynehammes
 */
public class Lesson {

    ServerHelper serverHelper = new ServerHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private String lessonID;

    public Lesson(String lessonID) {
        this.lessonID = lessonID;
    }

    // Schedule new lesson
    // Schedule student lesson
    public String scheduleStudentLesson(String lessonType, String instructorName, String appointmentDate, String appointmentStartTime,
            String appointmentEndTime, String notes, String lessonCode, String instructorID, String programID, String enrollmentID, String studentID,
            double lessonUnits, double amountDue, String lessonStatus, String paymentStatus) throws ClassNotFoundException, SQLException {

        String insertSuccess = "Failed";

        boolean lessonTimeFree = getLessonTimeFreeForInstructor(instructorID, appointmentDate, appointmentStartTime);

        if (lessonTimeFree) {

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

            // Check if is first lesson
            Enrollment enrollment = new Enrollment(enrollmentID);
            boolean isFirstLesson = enrollment.isFirstLesson();

            // Get lesson price and last lesson boolean
            ResultSet resultSet = connection.prepareStatement(
                    "select PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,PrivateLessonTotal,GroupLessonTotal,PartyLessonTotal,"
                    + " PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice,ContractPaid from ProgramEnrollment where EnrollmentID='" + enrollmentID
                    + "';").executeQuery();
            resultSet.next();
            double lessonAttended = (resultSet.getDouble(1) + resultSet.getDouble(2) + resultSet.getDouble(3));
            double lessonTotal = (resultSet.getDouble(4) + resultSet.getDouble(5) + resultSet.getDouble(6));
            double lessonRemaining = lessonTotal - lessonAttended;
            double privateLessonPrice = resultSet.getDouble(7);
            double groupLessonPrice = resultSet.getDouble(8);
            double partyLessonPrice = resultSet.getDouble(9);
            double contractPaid = resultSet.getDouble(10);

            // Set lesson column type to get price
            double lessonPrice = 0.0;
            if (lessonType.equals("Private")) {
                lessonPrice = privateLessonPrice;
            } else if (lessonType.equals("Group")) {
                lessonPrice = groupLessonPrice;
            } else if (lessonType.equals("Party")) {
                lessonPrice = partyLessonPrice;
            }

            // Check if last lesson
            boolean lastLesson = false;
            if (lessonRemaining <= 1) {
                lastLesson = true;
            }

            // Check if owes payment
            double[] newAmountOwed = enrollment.getNewAmountOwed(lessonUnits, resultSet.getDouble(1), resultSet.getDouble(2), resultSet.getDouble(3),
                    privateLessonPrice, groupLessonPrice, partyLessonPrice, lessonType);
            boolean owesPayment = false;

            if (newAmountOwed[0] > contractPaid) {
                owesPayment = true;
            }

            // Insert lesson in LessonSchdeule
            connection.prepareStatement(String.format(
                    "insert into LessonSchedule(StudentID,EnrollmentID,ProgramID,RateType,StudentName,InstructorName,AppointmentDate,AppointmentTimeStart,"
                    + " AppointmentTimeEnd,LessonUnits,Notes,LessonCode,InstructorID,LessonPrice,InstructorPriority,LastLesson,"
                    + "LessonStatus,PaymentStatus,FirstLesson,OwesPayment,AmountDue)"
                    + " values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%f,%d,%b,'%s','%s',%b,%b,%f);",
                    studentID, enrollmentID, programID, lessonType, studentName, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                    lessonUnits, notes, lessonCode, instructorID, lessonPrice, instructorPriority, lastLesson, lessonStatus, paymentStatus, isFirstLesson, owesPayment,
                    amountDue)).execute();

            // Get inserted id and set for lesson
            String lastInsertedID = genericHelper.getLastInsertedID("LessonSchedule", "LessonID");
            this.lessonID = lastInsertedID;

            // Flag successful insert
            insertSuccess = "Success";

        } else {
            insertSuccess = "InstructorTimeConflict";
        }

        return insertSuccess;
    }

    // Check if selected time is free for instructor
    public boolean getLessonTimeFreeForInstructor(String instructorID, String appointmentDate, String appointmentStartTime) throws SQLException {

        boolean lessonTimeFree = true;

        ResultSet instructorSet = connection.prepareStatement("SELECT LessonID FROM LessonSchedule where"
                + " InstructorID='" + instructorID + "' and AppointmentDate='" + appointmentDate + "' and AppointmentTimeStart='" + appointmentStartTime + "';").executeQuery();

        if (instructorSet.next()) {
            lessonTimeFree = false;
        }

        return lessonTimeFree;
    }

    // Delete selected lesson
    public void attendSelectedLesson() throws SQLException, ClassNotFoundException {

        try {

            // Get lesson variables to update program enrollment
            ResultSet lessonSet = connection.prepareStatement(
                    "select EnrollmentID,RateType,LessonUnits,LessonStatus from LessonSchedule where LessonID='" + lessonID + "';").executeQuery();
            lessonSet.next();

            // Get lesson variables
            String enrollmentID = lessonSet.getString(1);
            String lessonType = lessonSet.getString(2);
            double lessonUnits = lessonSet.getDouble(3);
            String lessonStatus = lessonSet.getString(4);

            // Get Enrollment Object
            Enrollment enrollment = new Enrollment(enrollmentID);

            // Get program id to check for unlimitedLessons
            ResultSet enrollmentData = connection.prepareStatement(String.format("SELECT EN.ProgramID,PR.UnlimitedLessons FROM ProgramEnrollment as EN INNER JOIN Programs as PR ON"
                    + " EN.ProgramID=PR.ProgramID WHERE EN.EnrollmentID='%s';", enrollmentID)).executeQuery();
            enrollmentData.next();
            String programID = enrollmentData.getString(1);
            boolean unlimitedLessons = enrollmentData.getBoolean(2);

            boolean attendanceOverLimit = false;
            if (!unlimitedLessons) {

                // Check if attendance exceeds program
                attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

            }

            // Check if already attended
            if (lessonStatus.equals("Attended")) {

                // Alert already attended
                JOptionPane.showMessageDialog(null, "Scheduled Lesson has already been Attended.",
                        "Lesson Already Attended", JOptionPane.INFORMATION_MESSAGE);

            } // Check if too many lessons for attendance
            else if (attendanceOverLimit) {

                // Alert exceeds program
                JOptionPane.showMessageDialog(null, "Lesson exceeds Program Enrollment for " + lessonType + " lessons.",
                        "Lesson Exceeds Limit", JOptionPane.INFORMATION_MESSAGE);

            } else {

                // Set status attended for lesson schedule
                connection.prepareStatement(String.format(
                        "UPDATE LessonSchedule SET LessonStatus='Attended' WHERE LessonID='%s';", lessonID)).execute();

                // Update program enrollment
                enrollment.updateProgramEnrollment(lessonType, lessonUnits, 0);

                // Update last lesson status for all lessons in enrollment
                enrollment.updateProgramCompleted();

                // Alert success
                JOptionPane.showMessageDialog(null, "Successfully attended lesson with LessonID: " + lessonID + ".",
                        "Attended Lesson", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was a problem attending lesson. Please try again.",
                    "Attend Lesson Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // Delete selected lesson
    public void cancelSelectedLesson() throws SQLException, ClassNotFoundException {

        // Update to Cancelled in LessonSchedule
        try {

            // Get Lesson Record from Lesson Schedule
            String lessonQuery = String.format("select LessonStatus,EnrollmentID,RateType,LessonUnits from LessonSchedule where LessonID='%s';", lessonID);
            ResultSet lessonSet = serverHelper.queryDatabase(lessonQuery);
            lessonSet.next();

            // Get variables
            String lessonStatus = lessonSet.getString(1);
            String enrollmentID = lessonSet.getString(2);
            String lessonType = lessonSet.getString(3);
            double lessonUnits = lessonSet.getDouble(4);

            // Check if already cancelled
            if (lessonStatus.equals("Cancelled")) {

                JOptionPane.showMessageDialog(null, "This lesson has already been cancelled.",
                        "Lesson Already Cancelled", JOptionPane.WARNING_MESSAGE);

            } else if (lessonStatus.equals("Attended")) {

                String lessonCancel = String.format("UPDATE LessonSchedule SET LessonStatus='Cancelled' WHERE LessonID='%s';", lessonID);
                serverHelper.updateDatabase(lessonCancel);

                // Update Program Enrollment Attended based on lesson type
                String enrollmentQuery = "";
                if (lessonType.equals("Private")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select PrivateLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET PrivateLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                } else if (lessonType.equals("Group")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select GroupLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET GroupLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                } else if (lessonType.equals("Party")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select PartyLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET PartyLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                }

                // Update OwesPayment variable
                // Get program enrollment object
                String queryString = "select ContractPaid,PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,"
                        + "PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice,OwesPayment from ProgramEnrollment where EnrollmentId='" + enrollmentID + "';";
                ResultSet enrollmentSet = serverHelper.queryDatabase(queryString);
                enrollmentSet.next();

                // Get required variables
                double amountPaid = enrollmentSet.getDouble(1);
                double privateAttended = enrollmentSet.getDouble(2);
                double groupAttended = enrollmentSet.getDouble(3);
                double partyAttended = enrollmentSet.getDouble(4);
                double privatePrice = enrollmentSet.getDouble(5);
                double groupPrice = enrollmentSet.getDouble(6);
                double partyPrice = enrollmentSet.getDouble(7);
                boolean owesPayment = enrollmentSet.getBoolean(8);

                double newAmountOwed = ((privateAttended * privatePrice) + (groupAttended * groupPrice) + (partyAttended * partyPrice));

                // Update owesPayment on Program Enrollment
                if (amountPaid < newAmountOwed) {
                    owesPayment = true;
                } else {
                    owesPayment = false;
                }

                // Update with new owespayment
                String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET OwesPayment=%b WHERE EnrollmentID='%s';", owesPayment, enrollmentID);
                serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                // Get Enrollment Object
                Enrollment enrollment = new Enrollment(enrollmentID);

                // Update last lesson status for all lessons in enrollment
                enrollment.updateProgramCompleted();

                // Alert success
                JOptionPane.showMessageDialog(null, "Successfully cancelled lesson with LessonID: " + lessonID + ".",
                        "Cancelled Lesson", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Cancel non-attended, non-cancelled lesson
                String lessonCancel = String.format("UPDATE LessonSchedule SET LessonStatus='Cancelled' WHERE LessonID='%s';", lessonID);
                serverHelper.updateDatabase(lessonCancel);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "There was a problem cancelling lesson. Please try again.",
                    "Cancel Lesson Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // Delete selected lesson
    public void deleteSelectedLesson() throws SQLException, ClassNotFoundException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really delete lesson?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Get Lesson Record from Lesson Schedule
            String lessonQuery = String.format("select LessonStatus,EnrollmentID,RateType,LessonUnits from LessonSchedule where LessonID='%s';", lessonID);
            ResultSet lessonSet = serverHelper.queryDatabase(lessonQuery);
            lessonSet.next();

            // Get variables
            String lessonStatus = lessonSet.getString(1);
            String enrollmentID = lessonSet.getString(2);
            String lessonType = lessonSet.getString(3);
            double lessonUnits = lessonSet.getDouble(4);

            if (lessonStatus.equals("Attended")) {

                // Update Program Enrollment Attended based on lesson type
                String enrollmentQuery = "";
                if (lessonType.equals("Private")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select PrivateLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET PrivateLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                } else if (lessonType.equals("Group")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select GroupLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET GroupLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                } else if (lessonType.equals("Party")) {

                    // Get attended amount from Program Enrollment
                    enrollmentQuery = String.format("select PartyLessonAttended from ProgramEnrollment where EnrollmentID='%s';", enrollmentID);
                    ResultSet enrollmentSet = serverHelper.queryDatabase(enrollmentQuery);
                    enrollmentSet.next();

                    // Calculate new Lesson Attended total
                    double newLessonAttended = enrollmentSet.getDouble(1) - lessonUnits;

                    // Update with new attendance
                    String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET PartyLessonAttended=%f WHERE EnrollmentID='%s';", newLessonAttended, enrollmentID);
                    serverHelper.updateDatabase(enrollmentAttendanceUpdate);

                }

                // Update OwesPayment variable
                // Get program enrollment object
                String queryString = "select ContractPaid,PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,"
                        + "PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice,OwesPayment from ProgramEnrollment where EnrollmentId='" + enrollmentID + "';";
                ResultSet enrollmentSet = serverHelper.queryDatabase(queryString);
                enrollmentSet.next();

                // Get required variables
                double amountPaid = enrollmentSet.getDouble(1);
                double privateAttended = enrollmentSet.getDouble(2);
                double groupAttended = enrollmentSet.getDouble(3);
                double partyAttended = enrollmentSet.getDouble(4);
                double privatePrice = enrollmentSet.getDouble(5);
                double groupPrice = enrollmentSet.getDouble(6);
                double partyPrice = enrollmentSet.getDouble(7);
                boolean owesPayment = enrollmentSet.getBoolean(8);

                double newAmountOwed = ((privateAttended * privatePrice) + (groupAttended * groupPrice) + (partyAttended * partyPrice));

                // Update owesPayment on Program Enrollment
                if (amountPaid < newAmountOwed) {
                    owesPayment = true;
                } else {
                    owesPayment = false;
                }

                // Update with new owespayment
                String enrollmentAttendanceUpdate = String.format("UPDATE ProgramEnrollment SET OwesPayment=%b WHERE EnrollmentID='%s';", owesPayment, enrollmentID);
                serverHelper.updateDatabase(enrollmentAttendanceUpdate);

            }

            // Get all active students in resultSet
            String deleteStmt = String.format("delete from LessonSchedule where LessonID='%s';", lessonID);
            serverHelper.updateDatabase(deleteStmt);

            // Check if delete successful
            String queryString = "select LessonID from LessonSchedule where LessonID='" + lessonID + "';";
            ResultSet deletedSet = serverHelper.queryDatabase(queryString);

            // If the set is empty it was successful
            if (deletedSet.next()) {
                JOptionPane.showMessageDialog(null, "There was a problem deleting lesson. Please try again.",
                        "Delete Lesson Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Successfully deleted lesson with LessonID: " + lessonID + ".",
                        "Deleted Lesson", JOptionPane.INFORMATION_MESSAGE);
            }

        }
    }

}
