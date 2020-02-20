/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author daynehammes
 */
public class ServerHelper {
    
    // Application-wide 
    public static org.hsqldb.Server hsqlServer = null;
    public static Connection connection = null;      // 0 = Active, 1 = Inactive, 2 = All

    // Instantiate database connection
    public static void initiateDatabaseConnection() {

        try {
            hsqlServer = new org.hsqldb.Server();
            hsqlServer.setLogWriter(null);
            hsqlServer.setSilent(true);

            // Data will be stored in files testdb.properties and testdb.script
            hsqlServer.setDatabaseName(0, "FADSData1");
            hsqlServer.setDatabasePath(0, "file:FADSDataSettings");

            // Start Database
            hsqlServer.start();

            // Connect to Database
            Class.forName("org.hsqldb.jdbcDriver");

            // Default user of the HSQLDB is 'sa' with an empty password
            connection = DriverManager.getConnection(
                    "jdbc:hsqldb:hsql://localhost/FADSData1", "sa", "");
          
            
            // Attempt opening table, will fail and create tables if db doesn't exist     
            ResultSet trialSet = connection.prepareStatement("SELECT * from Students;").executeQuery();
            
            int i = 0;
            while (trialSet.next()) {
                System.out.println(trialSet.getString(1) + " " + trialSet.getString(2)  + " " + trialSet.getString(3)  + " " + trialSet.getString(4)  + " " + trialSet.getString(5)  + " " + trialSet.getString(6) 
                 + " " + trialSet.getString(7)  + " " + trialSet.getString(8)  + " " + trialSet.getString(9)  + " " + trialSet.getString(10)  + " " + trialSet.getString(11)  + " " + trialSet.getString(12)  + " " + trialSet.getString(13)
                 + " " + trialSet.getString(14)  + " " + trialSet.getString(15)  + " " + trialSet.getString(16));
                i++;
            }
            System.out.println(i + " Rows");
            
            
            
           // Alter column
            /*connection.prepareStatement("ALTER TABLE PUBLIC.BonusTransaction ALTER COLUMN ReferringStudentCurrentnstructorID3"
                    + " RENAME TO ReferringStudentCurrentInstructorID3;").execute();*/
            
            
            
           // Drop and recrete database
            /*connection.prepareStatement("DROP SCHEMA PUBLIC CASCADE;").execute();
            Exception e = new Exception();
            throw e;*/
            
           

        } catch (Exception e) {
            e.printStackTrace();
            // Database has not been built yet
            try {

                addTables();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

    }

    public static void shutdownDatabaseConnection() throws SQLException, ClassNotFoundException {

        try {

            // Shut down database
            connection.prepareStatement("SHUTDOWN").executeUpdate();

            // Nullify server variables
            hsqlServer = null;
            connection = null;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Add tables to database
    public static void addTables() throws SQLException {

        // Create Students
        connection.prepareStatement(
                "create table Students ( StudentID VARCHAR(32) NOT NULL, FName VARCHAR(32), LName VARCHAR(32), Address VARCHAR(64)," + // 4
                " City VARCHAR(32), State VARCHAR(16), ZipCode VARCHAR(16), Phone VARCHAR(128), Cell1 VARCHAR(128), Cell2 VARCHAR(128), PromotedToNewStudent DATE, PromotedToStudent DATE," + // 9
                " RegisteredAsUnenrolledStudent DATE, Notes VARCHAR(2048), MailingList BOOLEAN, Email VARCHAR(128), Email2 VARCHAR(128)," + // 14
                " Active BOOLEAN DEFAULT TRUE NOT NULL, InstructorID VARCHAR(32), InstructorID2 VARCHAR(32), InstructorID3 VARCHAR(32), ReferralType VARCHAR(32), ReferrerID VARCHAR(32)," + // 19
                " StudentType VARCHAR(32) DEFAULT 'New Student', BirthDate DATE, BirthDate2 DATE," + // 29
                " DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE, PRIMARY KEY(StudentID));")
                .execute();

        // Create Instructors
        connection.prepareStatement(
                "create table Instructors ( InstructorID VARCHAR(32) NOT NULL, FName VARCHAR(32), LName VARCHAR(32), Title VARCHAR(32)," + // 4
                " BirthDate DATE, HireDate DATE, Address VARCHAR(64), City VARCHAR(32), State VARCHAR(16), ZipCode VARCHAR(16)," + // 10
                " Email VARCHAR(128), HomePhone VARCHAR(32), WorkPhone VARCHAR(32), Notes VARCHAR(2048), FullTime BOOLEAN, SchedulePriority INTEGER DEFAULT 10," + // 16
                " Active BOOLEAN DEFAULT TRUE NOT NULL, DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE, PRIMARY KEY(InstructorID));")
                .execute();

        // Create Programs ProgramName, ProgramID, ProgramDescription, RatePrivate, RateGroup, RateParty, DefaultBonusesAwardedPrivate, DefaultBonusesAwardedGroup, DefaultBonusesAwardedParty, Unlimited, Active, DateUpdated
        connection.prepareStatement(
                "create table Programs ( ProgramID VARCHAR(32) NOT NULL, ProgramName VARCHAR(64), ProgramDescription VARCHAR(512), ProgramGroup VARCHAR(32) NOT NULL," + // 3
                " RatePrivate NUMERIC(20,2) DEFAULT 0, RateGroup NUMERIC(20,2) DEFAULT 0, RateParty NUMERIC(20,2) DEFAULT 0, " + // 7
                " DefaultLessonsPrivate NUMERIC(20,2) DEFAULT 0, DefaultLessonsGroup NUMERIC(20,2) DEFAULT 0, DefaultLessonsParty NUMERIC(20,2) DEFAULT 0," + // 10
                " DefaultBonusesAwardedPrivate NUMERIC(20,2) DEFAULT 0, DefaultBonusesAwardedGroup NUMERIC(20,2) DEFAULT 0, DefaultBonusesAwardedParty NUMERIC(20,2) DEFAULT 0," + // 13
                " Active BOOLEAN DEFAULT TRUE NOT NULL, UnlimitedLessons BOOLEAN DEFAULT FALSE NOT NULL," +
                " DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE, PRIMARY KEY(ProgramID));")
                .execute();

        // Create Program Enrollment/Payment Plan
        connection.prepareStatement(
                "create table ProgramEnrollment ( EnrollmentID INTEGER IDENTITY NOT NULL, ProgramID VARCHAR(32) NOT NULL, ProgramGroup VARCHAR(32) NOT NULL," + // 2
                " StudentID VARCHAR(32) NOT NULL, PrimaryInstructorID VARCHAR(32), InstructorID1 VARCHAR(32), InstructorID2 VARCHAR(32)," + // 5
                " ContractTotal NUMERIC(20,2) DEFAULT 0, ContractPaid NUMERIC(20,2) DEFAULT 0, PrivateLessonTotal NUMERIC(20,2) DEFAULT 0, PrivateLessonAttended NUMERIC(20,2) DEFAULT 0," + // 8
                " PrivateLessonPrice NUMERIC(20,2) DEFAULT 0, GroupLessonTotal NUMERIC(20,2) DEFAULT 0, GroupLessonAttended NUMERIC(20,2) DEFAULT 0," + // 11
                " GroupLessonPrice NUMERIC(20,2) DEFAULT 0, PartyLessonTotal NUMERIC(20,2) DEFAULT 0, PartyLessonAttended NUMERIC(20,2) DEFAULT 0, PartyLessonPrice NUMERIC(20,2) DEFAULT 0," + // 14
                " BonusesAwardedPrivate NUMERIC(20,2) DEFAULT 0, BonusesAwardedGroup NUMERIC(20,2) DEFAULT 0, BonusesAwardedParty NUMERIC(20,2) DEFAULT 0," + // 17
                " OwesPayment BOOLEAN DEFAULT FALSE, IsCompleted BOOLEAN DEFAULT FALSE, DateCompleted DATE, DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE);")
                .execute();

        // Create LessonSchedule
        connection.prepareStatement(
                "create table LessonSchedule ( LessonID INTEGER IDENTITY NOT NULL, StudentID VARCHAR(32), EnrollmentID VARCHAR(32)," + // 3
                " ProgramID VARCHAR(32), RateType VARCHAR(32) DEFAULT 'Private' NOT NULL, StudentName VARCHAR(128), InstructorName VARCHAR(128)," + // 7
                " AppointmentDate DATE, AppointmentTimeStart TIME, AppointmentTimeEnd TIME, LessonStatus VARCHAR(32) DEFAULT 'Unattended'," + // 11
                " PaymentStatus VARCHAR(32) DEFAULT 'Unpaid', LessonUnits NUMERIC(20,2) DEFAULT 1.0, PaidWithBonus BOOLEAN, Notes VARCHAR(2048)," + // 15
                " LessonCode VARCHAR(128) DEFAULT '', InstructorID VARCHAR(32), InstructorID2 VARCHAR(32), InstructorID3 VARCHAR(32), LessonPrice NUMERIC(20,2)," + // 20
                " InstructorPriority INTEGER DEFAULT 10, AmountDue NUMERIC(20,2) DEFAULT 0, OwesPayment BOOLEAN DEFAULT FALSE, FirstLesson BOOLEAN DEFAULT FALSE, LastLesson BOOLEAN DEFAULT FALSE, "
                        + "DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE);")
                .execute();

        // Create Payment Transactions
        connection.prepareStatement(
                "create table PaymentTransaction ( PaymentID INTEGER IDENTITY NOT NULL, StudentID VARCHAR(32) NOT NULL, PaymentType VARCHAR(32) DEFAULT 'N/A'," + // 3
                " Amount NUMERIC(20,2) NOT NULL, LessonUnits NUMERIC(20,2), ProgramID VARCHAR(32), EnrollmentID VARCHAR(50), LessonType VARCHAR(32) DEFAULT 'Private'," + // 7
                " FirstPayment BOOLEAN DEFAULT FALSE, Notes VARCHAR(2048), DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE);")
                .execute();
        
        // Create BonusTransaction
        connection.prepareStatement(
                "create table BonusTransaction ( TransactionID INTEGER IDENTITY NOT NULL, StudentID VARCHAR(32), TransactionType VARCHAR(32), BonusType VARCHAR(32) DEFAULT 'Regular' NOT NULL," + // 4
                " LessonType VARCHAR(32) DEFAULT 'Private' NOT NULL, UnitsUsed NUMERIC(20,2) NOT NULL," + // 8
                " FromEnrollmentID VARCHAR(64), UsedOnEnrollmentID VARCHAR(64), LessonID VARCHAR(64), RedeemedOn DATE, ReferredStudentID VARCHAR(32), " + // 12
                " ReferringStudentCurrentInstructorID VARCHAR(32), ReferringStudentCurrentInstructorID2 VARCHAR(32)," +
                " ReferringStudentCurrentInstructorID3 VARCHAR(32), DateCreated DATE DEFAULT CURRENT_DATE, DateUpdated DATE DEFAULT CURRENT_DATE);")
                .execute();
        
        // Create Referral Type Table
        connection.prepareStatement(
                "create table ReferralType ( RecordID INTEGER IDENTITY NOT NULL, ReferralType VARCHAR(256), DateCreated DATE DEFAULT CURRENT_DATE,"
                + " DateUpdated DATE DEFAULT CURRENT_DATE);")
                .execute();
        
        
        // Bonus definitions
        // TransactionType = ManualUpdate, ProgramEnrolled, StudentReferred, LessonAttended, RedeemedPending
        // BonusType = Pending, Real
        // LessonType = Private, Group, Party
        // FromEnrollmentID = if added with a program enrollment, which enrollment
        // UsedOnEnrollmentID = if attending lesson, which enrollment used on
        // Optional: enrollment_id, referring_student_id, referred_student_id, lesson_units, referring_student_instructor_count
        // ReferringStudentCurrentInstructorID is for reporting, dividing the credit of redeemed bonuses. It is possible to change instructor so there should be a record of this.
        
    }

    // Get ResultSet from Database
    public ResultSet queryDatabase(String query) throws SQLException, ClassNotFoundException {

        ResultSet resultSet = null;

        try {

            // Get all active students in resultSet
            resultSet = connection.prepareStatement(query).executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultSet;

    }

    // Execute sql expression in Database
    public void updateDatabase(String updateSQL) throws SQLException, ClassNotFoundException {

        try {

            // Perform update operation
            connection.prepareStatement(updateSQL).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Returns the number of rows in a SQL ResultSet
    public int getNumberRows(ResultSet resultSet) throws SQLException {

        int numRows = 0;
        if (resultSet.next()) {
            resultSet.last();
            numRows = resultSet.getRow();
        }
        resultSet.beforeFirst();

        return numRows;
    }

    
}
