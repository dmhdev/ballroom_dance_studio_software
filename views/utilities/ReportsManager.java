/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.utilities;

import views.main.MainMenu;
import commons.helpers.GenericHelper;
import commons.entities.Report;
import commons.helpers.ReportHelper;
import static commons.helpers.ServerHelper.connection;
import commons.helpers.TableHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author daynehammes
 */
public class ReportsManager extends javax.swing.JFrame {

    private GenericHelper genericHelper = new GenericHelper();
    private ReportHelper reportHelper = new ReportHelper();
    private Report report = new Report();
    private TableHelper tableHelper = new TableHelper();
    private DefaultTableModel studentTableModel, instructorTableModel;
    private TableRowSorter studentSorter, instructorSorter;
    private ButtonGroup studentBtnGroup, instructorBtnGroup;
    private DateFormat sqlDateFormat, reportDateFormat;

    /**
     * Creates new form Menu
     */
    public ReportsManager() {

        initComponents();
        // Set Window Icon
        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Dance Studios - Management System");
        setLocationRelativeTo(null);

        sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        reportDateFormat = new SimpleDateFormat("MM-dd-yyyy");

        // Set Fields
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set all fields
    private void setFields() throws ClassNotFoundException, SQLException, InterruptedException {

        // Populate Students JTable
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateStudentsTable("All", studentTable, studentSorter, studentTableModel, studentActiveFilter, studentInactiveFilter, studentAllFilter, studentSearchInput);
                    studentBtnGroup = new ButtonGroup();
                    studentBtnGroup.add(studentActiveFilter);
                    studentBtnGroup.add(studentInactiveFilter);
                    studentBtnGroup.add(studentAllFilter);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // Populate Instructors JTable
        Thread thread2 = new Thread() {
            public void run() {
                try {
                    tableHelper.populateInstructorsTable(instructorTable, instructorTableModel, instructorSorter, instructorActiveFilter, instructorInactiveFilter,
                            instructorAllFilter, instructorSearchInput);
                    instructorBtnGroup = new ButtonGroup();
                    instructorBtnGroup.add(instructorActiveFilter);
                    instructorBtnGroup.add(instructorInactiveFilter);
                    instructorBtnGroup.add(instructorAllFilter);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // Start threads
        thread1.start();
        thread2.start();

        // Join threads
        thread1.join();
        thread2.join();

        // Set initial dates
        genericHelper.setInitialDates(studentStartDateChooser, studentEndDateChooser);
        genericHelper.setInitialDates(instructorStartDateChooser, instructorEndDateChooser);
        genericHelper.setInitialDates(studioStartDateChooser, studioEndDateChooser);

    }

    // Disable all buttons while generating report
    private void disableButtons() {
        generateStudentReportBtn.setEnabled(false);
        generateInstructorReportBtn.setEnabled(false);
        generateStudioReportBtn.setEnabled(false);
    }

    // Enable all buttons after generating report
    private void enableButtons() {
        generateStudentReportBtn.setEnabled(true);
        generateInstructorReportBtn.setEnabled(true);
        generateStudioReportBtn.setEnabled(true);
    }

    // Create student birthdays panel
    private void studentLessonHistoryReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Student ID
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        String currentStudentID = (String) studentTable.getModel().getValueAt(row, 8);
        String currentStudentName = ((String) studentTable.getModel().getValueAt(row, 1) + " " + (String) studentTable.getModel().getValueAt(row, 0));

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(studentAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select LS.AppointmentDate,LS.AppointmentTimeStart,PR.ProgramName,LS.RateType,"
                    + "LS.InstructorName,LS.LessonStatus,LS.PaymentStatus from LessonSchedule"
                    + " as LS INNER JOIN ProgramEnrollment as EN on EN.EnrollmentID=LS.EnrollmentID"
                    + " INNER JOIN Programs as PR on PR.ProgramID=EN.ProgramID WHERE LS.DateCreated"
                    + " between DATE '%s' and '%s' and LS.StudentID='%s' order by LS.RateType,EN.ProgramID,"
                    + "LS.AppointmentDate asc;", startDate, endDate, currentStudentID);
        } else {
            sqlQuery = String.format("select LS.AppointmentDate,LS.AppointmentTimeStart,PR.ProgramName,LS.RateType,"
                    + "LS.InstructorName,LS.LessonStatus,LS.PaymentStatus from LessonSchedule"
                    + " as LS INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID INNER JOIN"
                    + " Programs as PR on PR.ProgramID=EN.ProgramID WHERE LS.StudentID='%s' order by LS.RateType,"
                    + "EN.ProgramID,LS.AppointmentDate asc;",
                    currentStudentID);
        }

        // Create File Name
        String reportFileName = "StudentLessonHistory";

        // Create Row Headers
        String[] reportHeaders = {"Date", "Time", "Program", "Lesson Type", "Instructor", "Lesson Status", "Payment Status"};

        // Date columns
        int[] dateColumns = {1};

        // Generate Report
        report.generateQueryReport(sqlQuery, reportFileName, "Student Lesson History", reportHeaders, currentStudentName,
                reportHeaders.length, dateColumns);

    }

    // Create student birthdays panel
    private void studentEnrollmentHistoryReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Student ID
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        String currentStudentID = (String) studentTable.getModel().getValueAt(row, 8);
        String currentStudentName = ((String) studentTable.getModel().getValueAt(row, 1) + " " + (String) studentTable.getModel().getValueAt(row, 0));

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(studentAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select EN.DateCreated,EN.ProgramID,EN.ContractTotal,EN.ContractPaid,INS.LName"
                    + " FROM ProgramEnrollment"
                    + " AS EN INNER JOIN Instructors AS INS ON INS.InstructorID="
                    + "EN.PrimaryInstructorID WHERE EN.DateCreated BETWEEN DATE '%s' and '%s' and EN.StudentID='%s' ORDER BY"
                    + " EN.DateCreated,EN.ProgramID,INS.LName,INS.FName ASC;", startDate, endDate, currentStudentID);
        } else {
            sqlQuery = String.format("select EN.DateCreated,EN.ProgramID,EN.ContractTotal,EN.ContractPaid,INS.LName"
                    + " FROM ProgramEnrollment AS EN INNER JOIN Instructors AS INS ON INS.InstructorID=EN.PrimaryInstructorID"
                    + " WHERE EN.StudentID='%s' ORDER BY EN.DateCreated,EN.ProgramID,INS.LName,INS.Fname ASC;", currentStudentID);
        }

        // Create File Name
        String reportFileName = "StudentEnrollmentHistory";

        // Create Row Headers
        String[] reportHeaders = {"Date Created", "Program ID", "Contract Total", "Contract Paid", "Instructor"};

        // Date columns
        int[] dateColumns = {1};

        // Generate Report
        report.generateQueryReport(sqlQuery, reportFileName, "Student Enrollment History", reportHeaders, currentStudentName,
                reportHeaders.length, dateColumns);

    }

    // Create Student Birthdays Report
    private void studentBirthdaysReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Student ID
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        String currentStudentName = ((String) studentTable.getModel().getValueAt(row, 1) + " " + (String) studentTable.getModel().getValueAt(row, 0));

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(studentAllDatesCheck);

        // Set date values 
        String startMonth = null, endMonth = null;
        if (!useAllDates) {
            try {

                // Get Start and End dates
                Date startDate = studentStartDateChooser.getDate();
                Date endDate = studentEndDateChooser.getDate();

                // Create Calendar instance
                Calendar cal = Calendar.getInstance();

                // Extract month and convert to string
                cal.setTime(startDate);
                startMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);
                cal.setTime(endDate);
                endMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select StudentID,LName,FName,BirthDate,DateCreated from Students where MONTH(BirthDate) >= %s and MONTH(BirthDate) <= %s"
                    + " or MONTH(BirthDate2) >= %s and MONTH(BirthDate2) <= %s order by LName desc;",
                    startMonth, endMonth, startMonth, endMonth);
        } else {
            sqlQuery = "select StudentID,LName,FName,BirthDate,DateCreated from Students order by LName desc;";
        }

        // Create File Name
        String reportFileName = "StudentBirthdays";

        // Create Row Headers
        String[] reportHeaders = {"Student ID", "Last Name", "First Name", "Birth Date", "Date Created"};

        // Date columns
        int[] dateColumns = {4, 5};

        // Generate Report
        report.generateQueryReport(sqlQuery, reportFileName, "Student Birthdays", reportHeaders, currentStudentName,
                reportHeaders.length, dateColumns);

    }

    // Create student birthdays panel
    private void studentPaymentsDue() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Create Query String
        String sqlQuery = "select EN.StudentID,ST.LName,ST.FName,EN.ProgramID,EN.ContractPaid,EN.ContractTotal"
                + " FROM ProgramEnrollment as EN INNER JOIN Students as ST ON ST.StudentID=EN.StudentID WHERE EN.OwesPayment=TRUE ;";

        // Create File Name
        String reportFileName = "StudentPaymentsDue";

        // Create Row Headers
        String[] reportHeaders = {"Student ID", "Last Name", "First Name", "Program ID", "Contract Paid", "Contract Total"};

        // Date columns
        int[] dateColumns = {};

        // Generate Report
        report.generateQueryReport(sqlQuery, reportFileName, "Student Payments Due", reportHeaders, "",
                reportHeaders.length, dateColumns);

    }

    // Create student birthdays panel
    private void studentPaymentHistoryReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Student ID
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        String currentStudentID = (String) studentTable.getModel().getValueAt(row, 8);
        String currentStudentName = ((String) studentTable.getModel().getValueAt(row, 1) + " " + (String) studentTable.getModel().getValueAt(row, 0));

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(studentAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select DateCreated,LessonType,LessonUnits,Amount,PaymentType from PaymentTransaction"
                    + " where DateCreated between DATE '%s' and '%s' and StudentID='%s' order by DateCreated,LessonType asc;", startDate, endDate, currentStudentID);
        } else {
            sqlQuery = String.format("select DateCreated,LessonType,LessonUnits,Amount,PaymentType from PaymentTransaction"
                    + " where StudentID='%s' order by DateCreated,LessonType asc;", currentStudentID);
        }

        // Create File Name
        String reportFileName = "StudentPaymentHistory";

        // Create Row Headers
        String[] reportHeaders = {"Transaction Date", "Lesson/Deposit", "Lesson Units", "Amount", "Payment Type"};

        // Date columns
        int[] dateColumns = {1};

        // Generate Report
        report.generateQueryReport(sqlQuery, reportFileName, "Student Payment History", reportHeaders, currentStudentName,
                reportHeaders.length, dateColumns);

    }

    // Create student birthdays panel
    private void studentReferralsReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Student ID
        int row = studentTable.convertRowIndexToModel(studentTable.getSelectedRow());
        String currentStudentID = (String) studentTable.getModel().getValueAt(row, 8);
        String currentStudentName = ((String) studentTable.getModel().getValueAt(row, 1) + " " + (String) studentTable.getModel().getValueAt(row, 0));

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(studentAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = String.format("SELECT BT.DateCreated,ST.LName,ST.FName,ST.BirthDate,BT.UnitsUsed FROM Students AS ST"
                + " INNER JOIN BonusTransaction AS BT ON BT.ReferredStudentID=ST.StudentID WHERE ST.ReferrerID='%s'"
                + " AND BT.TransactionType='StudentReferred' ORDER BY"
                + " ST.DateCreated,ST.LName ASC;", currentStudentID);
        ResultSet referralsResultSet = connection.prepareStatement(sqlQuery).executeQuery();

        // Create Arraylist for Non-Query Report
        List<List<Object>> reportArrayList = new ArrayList<>();
        while (referralsResultSet.next()) {

            String referralDate = referralsResultSet.getString(1);
            String lastName = referralsResultSet.getString(2);
            String firstName = referralsResultSet.getString(3);
            String qualified = "-";//report.getStudentIsQualified(referralsResultSet.getDate(4));
            String bonusLessons = referralsResultSet.getString(5);

            List<Object> currentRowData = new ArrayList();
            currentRowData.add(0, referralDate);
            currentRowData.add(1, lastName);
            currentRowData.add(2, firstName);
            currentRowData.add(3, qualified);
            currentRowData.add(4, bonusLessons);
            reportArrayList.add(currentRowData);

        }

        // Create Row Headers
        String[] reportHeaders = {"Date Referred", "Last Name", "First Name", "Q/UQ", "BLS"};

        // Date columns
        int[] dateColumns = {1};

        // Generate Report
        report.generateNonQueryReportSingleReport("StudentReferrals", String.format("Student Referrals by %s", currentStudentName),
                reportArrayList, reportHeaders, "", reportHeaders.length);

    }

    // Create Student Birthdays Report
    private void grossIncomeReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(incomeAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select PT.PaymentID,ST.LName,ST.FName,PT.Amount,PT.LessonUnits,PT.PaymentType,PT.LessonType,PT.DateCreated"
                    + " from PaymentTransaction as PT INNER JOIN Students as ST on ST.StudentID=PT.StudentID WHERE PT.DateCreated between "
                    + "DATE '%s' and '%s' ORDER BY PT.DateCreated,ST.LName,ST.FName,PT.PaymentType,PT.LessonType DESC;", startDate, endDate);
        } else {
            sqlQuery = "select PT.PaymentID,ST.LName,ST.FName,PT.Amount,PT.LessonUnits,PT.PaymentType,PT.LessonType,PT.DateCreated"
                    + " from PaymentTransaction as PT INNER JOIN Students as ST on ST.StudentID=PT.StudentID"
                    + " ORDER BY PT.DateCreated,ST.LName,ST.FName,PT.PaymentType,PT.LessonType DESC;";
        }

        // Create Aggregate Query String
        String aggregateSQLQuery = "";
        if (!useAllDates) {
            aggregateSQLQuery = String.format("select SUM(Amount) as GrossIncome from PaymentTransaction where DateCreated between DATE '%s' and '%s';", startDate, endDate);
        } else {
            aggregateSQLQuery = "select SUM(Amount) as GrossIncome from PaymentTransaction";
        }

        // Create File Name
        String reportFileName = "GrossIncome";

        // Create Row Headers
        String[] reportHeaders = {"Payment ID", "Last Name", "First Name", "Amount", "Lesson Units", "Payment Type", "Lesson Type", "Date Created", "Gross Income"};

        // Date columns
        int[] dateColumns = {8};

        // Generate Aggregate Report
        report.generateAggregateQueryReport(sqlQuery, aggregateSQLQuery, reportFileName, "Gross Income", reportHeaders, "",
                reportHeaders.length, dateColumns);

    }

    // Create Student Birthdays Report
    private void unpaidEnrollmentIncome() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Check if should use date
        boolean useAllDates = report.getUseAllDates(incomeAllDatesCheck);

        // Set date values 
        String startDate = null, endDate = null;
        if (!useAllDates) {
            try {

                startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
                endDate = sqlDateFormat.format(studentEndDateChooser.getDate());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Create Query String
        String sqlQuery = "";
        if (!useAllDates) {
            sqlQuery = String.format("select EN.enrollmentID,ST.LName,ST.FName,PR.ProgramName,EN.ContractPaid,EN.ContractTotal,"
                    + "EN.DateCreated from ProgramEnrollment"
                    + " AS EN INNER JOIN Students AS ST ON ST.StudentID=EN.StudentID INNER JOIN Programs as PR ON"
                    + " PR.ProgramID=EN.ProgramID WHERE EN.DateCreated between DATE '%s' and '%s' order by EN.DateCreated,ST.LName,ST.Fname DESC;", startDate, endDate);
        } else {
            sqlQuery = "select EN.EnrollmentID,ST.Lname,ST.FName,PR.ProgramName,EN.ContractPaid,EN.ContractTotal,EN.DateCreated"
                    + " from ProgramEnrollment AS EN INNER JOIN Students AS ST ON ST.StudentID=EN.StudentID INNER JOIN Programs as PR ON"
                    + " PR.ProgramID=EN.ProgramID"
                    + " ORDER BY EN.DateCreated,ST.Lname,ST.Fname DESC;";
        }

        // Create Aggregate Query String
        String aggregateSQLQuery = "";
        if (!useAllDates) {
            aggregateSQLQuery = String.format("select (SUM(ContractTotal) - SUM(ContractPaid)) as UnpaidEnrollment from ProgramEnrollment"
                    + " where DateCreated between DATE '%s' and '%s';", startDate, endDate);
        } else {
            aggregateSQLQuery = "select (SUM(ContractTotal) - SUM(ContractPaid)) as UnpaidEnrollment from ProgramEnrollment;";
        }

        // Create File Name
        String reportFileName = "UnpaidEnrollmentIncome";

        // Create Row Headers
        String[] reportHeaders = {"Enrollment ID", "Student LName", "Student FName", "Program", "Contract Paid", "Contract Total", "Date Created", "Total Unpaid Income"};

        // Date columns
        int[] dateColumns = {6};

        // Generate Aggregate Report
        report.generateAggregateQueryReport(sqlQuery, aggregateSQLQuery, reportFileName, "Unpaid Enrollment Income", reportHeaders,
                "", reportHeaders.length, dateColumns);

    }

    private void dailyCashSheetLessonsTaughtReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();
        String appointmentDate = "", studentID = "", studentName = "", instructorID = "", instructorID2 = "", instructorID3 = "", instructorName = "",
                lessonType = "", lessonPrice = "", programID = "", programGroup = "", frontOrBackEnd = "", lessonTotals = "", bonusTotals = "", contractTotal = "", typeColumn = "",
                studentReferralType = "", enrollmentID = "";
        String prevAppointmentDate = "", prevStudentID = "", prevInstructorID = "";
        String[] lessonPrices = new String[3];
        boolean isFirstLesson = false;

        // Get selected dates
        String startDate = sqlDateFormat.format(studioStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studioEndDateChooser.getDate());

        // Create Query String and execute
        String sqlQuery = String.format("SELECT LS.AppointmentDate,ST.StudentID,ST.FName,ST.LName,LS.InstructorID,LS.InstructorID2,LS.InstructorID3,INS.FName,INS.LName,LS.RateType,EN.PrivateLessonPrice,EN.GroupLessonPrice,EN.PartyLessonPrice,"
                + "  LS.ProgramID, EN.ProgramGroup, EN.PrivateLessonTotal, EN.GroupLessonTotal, EN.PartyLessonTotal, EN.BonusesAwardedPrivate, EN.BonusesAwardedGroup, EN.BonusesAwardedParty,"
                + " EN.ContractTotal, LS.FirstLesson, ST.ReferralType, LS.EnrollmentID"
                + " FROM LessonSchedule as LS INNER JOIN Students as ST ON LS.StudentID=ST.StudentID INNER JOIN Instructors as INS ON INS.InstructorID=LS.InstructorID INNER JOIN ProgramEnrollment"
                + "  as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.AppointmentDate between DATE '%s' and '%s' AND LS.LessonStatus='Attended'"
                + " AND EN.ProgramGroup!='Other' ORDER BY LS.AppointmentDate,ST.LName,ST.FName,LS.InstructorID DESC;", startDate, endDate);
        ResultSet lessonResultSet = connection.prepareStatement(sqlQuery).executeQuery();

        // Loop over all lessons for period
        int i = 0;
        while (lessonResultSet.next()) {

            // Get variables
            appointmentDate = reportDateFormat.format(lessonResultSet.getDate(1)).toString();
            studentID = lessonResultSet.getString(2);
            studentName = lessonResultSet.getString(4) + ", " + lessonResultSet.getString(3);
            instructorID = lessonResultSet.getString(5);
            instructorID2 = lessonResultSet.getString(6);
            instructorID3 = lessonResultSet.getString(7);
            instructorName = lessonResultSet.getString(9);
            lessonType = lessonResultSet.getString(10);
            lessonPrices[0] = lessonResultSet.getString(11);
            lessonPrices[1] = lessonResultSet.getString(12);
            lessonPrices[2] = lessonResultSet.getString(13);
            lessonPrice = report.getLessonPriceFromType(lessonType, lessonPrices);
            programID = lessonResultSet.getString(14);
            programGroup = lessonResultSet.getString(15);
            frontOrBackEnd = report.getWhiteSheetFrontOrBackEnd(programGroup);
            lessonTotals = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(16)),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(17)),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(18)));
            bonusTotals = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(19)),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(20)),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonResultSet.getDouble(21)));
            contractTotal = lessonResultSet.getString(22);
            isFirstLesson = lessonResultSet.getBoolean(23);
            studentReferralType = lessonResultSet.getString(24);
            typeColumn = report.getWhiteSheetTypeColumn(isFirstLesson, programGroup, studentReferralType, studentID);
            enrollmentID = lessonResultSet.getString(25);

            // If first lesson, show all instructors for new enrollment, if not, put line through lessons/sales columns
            if (isFirstLesson) {

                String[] otherInstructorNames = report.getWhiteSheetOtherInstructorNames(enrollmentID);

                if (otherInstructorNames[0].length() > 0) {
                    instructorName = String.format("<strong>%s</strong>, %s", instructorName, otherInstructorNames[0]);
                }
                if (otherInstructorNames[1].length() > 0) {
                    instructorName = String.format("%s, %s", instructorName, otherInstructorNames[1]);
                }

            } else {
                lessonTotals = "-";
                bonusTotals = "-";
                contractTotal = "-";
            }

            // Check if it is the same day, student, and teacher as previous
            if ((appointmentDate.equals(prevAppointmentDate)) && (studentID.equals(prevStudentID)) && (instructorID.equals(prevInstructorID))) {

                // Add new values to previous values in arraylist
                lessonPrice = String.valueOf(genericHelper.roundDecimalToTwoDecimalPlaces(Double.parseDouble((String) reportArrayList.get(i - 1).get(9)) + Double.parseDouble(lessonPrice)));
                reportArrayList.get(i - 1).set(9, lessonPrice);
            } else {

                // Add variables to arraylist
                List<Object> currentRowData = new ArrayList();
                currentRowData.add(0, appointmentDate);
                currentRowData.add(1, studentName);
                currentRowData.add(2, instructorName);
                currentRowData.add(3, frontOrBackEnd);
                currentRowData.add(4, lessonTotals);
                currentRowData.add(5, bonusTotals);
                currentRowData.add(6, contractTotal);
                currentRowData.add(7, programGroup);
                currentRowData.add(8, typeColumn);
                currentRowData.add(9, lessonPrice);
                reportArrayList.add(currentRowData);

                // Only incremement row counter if new row added
                i++;

            }

            // Set prev variables
            prevAppointmentDate = appointmentDate;
            prevStudentID = studentID;
            prevInstructorID = instructorID;

        }

        // Calculate aggregate value
        double lessonPriceTotalUnits = 0.0;
        for (List<Object> currentRowData : reportArrayList) {

            lessonPriceTotalUnits = genericHelper.roundDecimalToTwoDecimalPlaces(lessonPriceTotalUnits + Double.parseDouble((String) currentRowData.get(9)));
            currentRowData.add(10, String.valueOf(lessonPriceTotalUnits));

        }

        // Create Row Headers
        String[] reportHeaders = {"Date", "Name", "Teacher", "F/B End", "Lessons", "BL/NCH", "Sales", "Program Group", "Type", "Value", "Total"};

        // Generate Report
        report.generateNonQueryReportSingleReport("DailyCashSheetLessonsTaught", "Daily Cash Sheet - Lessons Taught", reportArrayList, reportHeaders, "", reportHeaders.length);

    }

    private void weeklyCashSheetMoneyInReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare variables
        List<List<Object>> reportArrayList = new ArrayList<>();
        String appointmentDate = "", studentID = "", studentName = "", instructorID = "", instructorID2 = "", instructorID3 = "", instructorName = "",
                paymentType = "", paymentAmount = "", programGroup = "", frontOrBackEnd = "", lessonTotals = "", bonusTotals = "", contractTotal = "", typeColumn = "",
                referralType = "", enrollmentID = "", privateLessonPrice = "", groupLessonPrice = "", partyLessonPrice = "", paymentTotal = "0.0";
        double privateLessonTotal = 0.0, groupLessonTotal = 0.0, partyLessonTotal = 0.0, privateBonusTotal = 0.0, groupBonusTotal = 0.0, partyBonusTotal = 0.0;
        boolean isFirstPayment = false;

        // Get selected dates
        String startDate = sqlDateFormat.format(studioStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studioEndDateChooser.getDate());

        // Create Query String and execute
        String sqlQuery = String.format("SELECT PT.DateCreated,ST.StudentID,ST.LName,ST.FName,EN.PrimaryInstructorID,EN.InstructorID1,EN.InstructorID2,EN.ProgramGroup,"
                + " EN.PrivateLessonTotal,EN.GroupLessonTotal,EN.PartyLessonTotal,EN.BonusesAwardedPrivate,EN.BonusesAwardedGroup,EN.BonusesAwardedParty,"
                + " EN.PrivateLessonPrice,EN.GroupLessonPrice, EN.PartyLessonPrice,EN.ContractTotal,PT.FirstPayment,ST.ReferralType,PT.PaymentType,PT.Amount,INS.LName,INS.FName,EN.EnrollmentID"
                + " FROM PaymentTransaction as PT INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=PT.EnrollmentID INNER JOIN Students as ST ON ST.StudentID=PT.StudentID"
                + " INNER JOIN Instructors as INS ON INS.InstructorID=EN.PrimaryInstructorID WHERE PT.DateCreated BETWEEN DATE '%s' AND '%s' ;", startDate, endDate);
        ResultSet weeklyCashSheetSet = connection.prepareStatement(sqlQuery).executeQuery();

        // Loop over all lessons for period
        while (weeklyCashSheetSet.next()) {

            // Get variables
            appointmentDate = reportDateFormat.format(weeklyCashSheetSet.getDate(1)).toString();
            studentID = weeklyCashSheetSet.getString(2);
            studentName = weeklyCashSheetSet.getString(3) + ", " + weeklyCashSheetSet.getString(4);
            instructorID = weeklyCashSheetSet.getString(5);
            instructorID2 = weeklyCashSheetSet.getString(6);
            instructorID3 = weeklyCashSheetSet.getString(7);
            programGroup = weeklyCashSheetSet.getString(8);
            privateLessonTotal = weeklyCashSheetSet.getDouble(9);
            groupLessonTotal = weeklyCashSheetSet.getDouble(10);
            partyLessonTotal = weeklyCashSheetSet.getDouble(11);
            privateBonusTotal = weeklyCashSheetSet.getDouble(12);
            groupBonusTotal = weeklyCashSheetSet.getDouble(13);
            partyBonusTotal = weeklyCashSheetSet.getDouble(14);
            privateLessonPrice = weeklyCashSheetSet.getString(15);
            groupLessonPrice = weeklyCashSheetSet.getString(16);
            partyLessonPrice = weeklyCashSheetSet.getString(17);
            contractTotal = weeklyCashSheetSet.getString(18);
            isFirstPayment = weeklyCashSheetSet.getBoolean(19);
            referralType = weeklyCashSheetSet.getString(20);
            paymentType = weeklyCashSheetSet.getString(21);
            paymentAmount = weeklyCashSheetSet.getString(22);
            instructorName = weeklyCashSheetSet.getString(23) + ", " + weeklyCashSheetSet.getString(24);
            enrollmentID = weeklyCashSheetSet.getString(25);

            typeColumn = report.getWhiteSheetTypeColumn(isFirstPayment, programGroup, referralType, studentID);
            frontOrBackEnd = report.getWhiteSheetFrontOrBackEnd(programGroup);

            // If first payment, show all instructors for new enrollment, if not, put line through lessons/sales columns
            if (isFirstPayment) {

                // Add other instructor names
                String[] otherInstructorNames = report.getWhiteSheetOtherInstructorNames(enrollmentID);

                if (otherInstructorNames[0].length() > 0) {
                    instructorName = String.format("<strong>%s</strong>/%s", weeklyCashSheetSet.getString(23), otherInstructorNames[0]);
                }
                if (otherInstructorNames[1].length() > 0) {
                    instructorName = String.format("%s/%s", instructorName, otherInstructorNames[1]);
                }

                // Add up totals for lessons and bonuses
                lessonTotals = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(privateLessonTotal),
                        genericHelper.roundToIntegerOrDecimalAsString(groupLessonTotal),
                        genericHelper.roundToIntegerOrDecimalAsString(partyLessonTotal));
                bonusTotals = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(privateBonusTotal),
                        genericHelper.roundToIntegerOrDecimalAsString(groupBonusTotal),
                        genericHelper.roundToIntegerOrDecimalAsString(partyBonusTotal));

            } else {
                lessonTotals = "-";
                bonusTotals = "-";
                contractTotal = "-";
            }

            // Add payment amount to current payment total
            paymentTotal = String.valueOf(genericHelper.roundDecimalToTwoDecimalPlaces(Double.parseDouble(paymentAmount) + Double.parseDouble(paymentTotal)));

            // Create Data Row
            List<Object> dataRow = new ArrayList();
            dataRow.add(appointmentDate);
            dataRow.add(studentName);
            dataRow.add(instructorName);
            dataRow.add(frontOrBackEnd);
            dataRow.add(lessonTotals);
            dataRow.add(bonusTotals);
            dataRow.add(contractTotal);
            dataRow.add(programGroup);
            dataRow.add(typeColumn);
            dataRow.add(paymentType);
            dataRow.add(paymentAmount);
            dataRow.add(paymentTotal);
            reportArrayList.add(dataRow);

        }

        // Create Row Headers
        String[] reportHeaders = {"Date", "Name", "Teacher", "F/B End", "Lessons", "Bonuses", "Sales", "Program Group", "Type", "Method", "Paid", "Total"};

        // Create report title
        String reportSubHeader = String.format("%s to %s", reportDateFormat.format(studioStartDateChooser.getDate()).toString(),
                reportDateFormat.format(studioEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("WeeklyCashSheetMoneyIn", "Weekly Cash Sheet - Money In", reportArrayList, reportHeaders, reportSubHeader,
                reportHeaders.length);

    }

    private void studioAndTeacherRegionalReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        List<Map<String, Object>> reportsArrayList = new ArrayList();

        // Get selected dates
        String startDate = sqlDateFormat.format(studioStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studioEndDateChooser.getDate());

        // Add studio reports to array list
        reportsArrayList.add(getStudioOriginalsSubReport(startDate, endDate));
        reportsArrayList.add(getStudioSalesBreakdownSubReport(startDate, endDate));
        reportsArrayList.add(getStudioCashReceiptsSubReport(startDate, endDate));
        reportsArrayList.add(getStudioLessonBreakdownSubReport(startDate, endDate));

        // Add active teacher reports to array list
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String instructorsQuery = "SELECT InstructorID, LName, FName FROM Instructors ORDER BY LName, FName DESC";
        ResultSet instructorsSet = stmt.executeQuery(instructorsQuery);

        reportsArrayList.add(getInstructorLessonCashBreakdownSubReport(instructorsSet, startDate, endDate));
        instructorsSet.beforeFirst();
        reportsArrayList.add(getInstructorSalesProgramGroupSubReport(instructorsSet, startDate, endDate));

        // Create report title
        String reportSubHeader = String.format("%s to %s", reportDateFormat.format(studioStartDateChooser.getDate()).toString(),
                reportDateFormat.format(studioEndDateChooser.getDate()).toString());

        report.generateNonQueryReportMultiReport("StudioReport", "IBDS Studio Report",
                reportSubHeader, reportsArrayList);

    }

    private Map<String, Object> getStudioOriginalsSubReport(String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map<String, Object> studioOriginalsReferralCounts = new HashMap();
        Map studioOriginalsReportMap = new HashMap();
        List<String> studioOriginalsReportHeaders = new ArrayList();
        List<List<Object>> studioOriginalsReportData = new ArrayList();

        // Create query for new unenrolled students during period (Tries), get PromotedToNewStudent and PromotedToStudent fields to check if successful (Sales)
        String studioOriginalsQuery = String.format("SELECT ReferralType,PromotedToNewStudent,PromotedToStudent FROM Students WHERE DateCreated between DATE '%s' and '%s' ORDER BY DateCreated DESC",
                startDate, endDate);
        ResultSet studioOriginalsSet = connection.prepareStatement(studioOriginalsQuery).executeQuery();

        while (studioOriginalsSet.next()) {

            // Get variables
            String currentReferralType = studioOriginalsSet.getString(1);
            String promotedToNewStudent = studioOriginalsSet.getString(2);
            String promotedToStudent = studioOriginalsSet.getString(3);

            // Check if referral type exists, if not create
            List<Integer> currentReferralTypeValues = (ArrayList) studioOriginalsReferralCounts.get(currentReferralType);
            if (currentReferralTypeValues == null) {
                currentReferralTypeValues = new ArrayList();
                currentReferralTypeValues.add(0);
                currentReferralTypeValues.add(0);
                studioOriginalsReferralCounts.put(currentReferralType, currentReferralTypeValues);

            }

            // Get  tries and sales count, and add 1 to tries
            int currentTries = currentReferralTypeValues.get(0) + 1;
            int currentSales = currentReferralTypeValues.get(1);

            // Get number of sales and add to count if exists
            if (!promotedToNewStudent.equals("0001-01-01") || !promotedToStudent.equals("0001-01-01")) {
                currentSales += 1;
            }

            // Update referral type values
            currentReferralTypeValues.set(0, currentTries);
            currentReferralTypeValues.set(1, currentSales);

        }

        // Add headers
        studioOriginalsReportHeaders.add("Referral Type");
        studioOriginalsReportHeaders.add("Tries");
        studioOriginalsReportHeaders.add("Sales");

        // Add data from referral counts hashmap
        for (Map.Entry<String, Object> entry : studioOriginalsReferralCounts.entrySet()) {

            // Get key and values
            String currentReferralType = entry.getKey();
            List<Integer> currentReferralTypeValues = (ArrayList) entry.getValue();
            int currentTries = currentReferralTypeValues.get(0);
            int currentSales = currentReferralTypeValues.get(1);

            // Create new row and add to data list
            List<Object> dataRow = new ArrayList();
            dataRow.add(currentReferralType);
            dataRow.add(String.valueOf(currentTries));
            dataRow.add(String.valueOf(currentSales));
            studioOriginalsReportData.add(dataRow);

        }

        // Add key/values and Original report to Reports ArrayList
        studioOriginalsReportMap.put("title", "Originals");
        studioOriginalsReportMap.put("headers", studioOriginalsReportHeaders);
        studioOriginalsReportMap.put("data", studioOriginalsReportData);

        return studioOriginalsReportMap;
    }

    private Map<String, Object> getStudioSalesBreakdownSubReport(String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map<String, Object> studioSalesBreakdownCountsAndAmounts = new HashMap();
        Map studioSalesBreakdownReportMap = new HashMap();
        List<String> studioSalesBreakdownReportHeaders = new ArrayList();
        List<List<Object>> studioSalesBreakdownReportData = new ArrayList();

        // Initialize Referral Type Objects
        String[] referralTypesList = {"Original", "Guests", "Preliminary", "Extension", "ReExtension", "Renewal", "Misc"};
        for (int i = 0; i < referralTypesList.length; i++) {
            List<Double> currentReferralTypeValues = new ArrayList();
            currentReferralTypeValues.add(0, 0.0);
            currentReferralTypeValues.add(1, 0.0);
            currentReferralTypeValues.add(2, 0.0);
            studioSalesBreakdownCountsAndAmounts.put(referralTypesList[i], currentReferralTypeValues);
        }

        /*
         Sales and Amount Columns
         */
        // Create query for student count and enrollment contract values that had a first lesson in new enrollment that week (Sales), ordered by ReferralType 
        String studioSalesBreakdownSoldAndAmountQuery = String.format("SELECT EN.ProgramGroup, ST.ReferralType, EN.ContractTotal FROM LessonSchedule as LS INNER JOIN Students as ST ON LS.StudentID=ST.StudentID"
                + " INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.DateCreated between DATE '%s' and '%s' AND LS.FirstLesson=TRUE AND LS.LessonStatus='Attended'"
                + " ORDER BY LS.StudentID DESC", startDate, endDate);
        ResultSet studioSalesBreakdownSoldAndAmountSet = connection.prepareStatement(studioSalesBreakdownSoldAndAmountQuery).executeQuery();

        while (studioSalesBreakdownSoldAndAmountSet.next()) {

            // Get variables
            String currentProgramGroup = studioSalesBreakdownSoldAndAmountSet.getString(1);
            String currentReferralType = studioSalesBreakdownSoldAndAmountSet.getString(2);
            Double currentSaleAmount = genericHelper.roundDecimalToTwoDecimalPlaces(studioSalesBreakdownSoldAndAmountSet.getDouble(3));

            // If Original Program Group, check whether referred as a Guest or Other
            if (currentProgramGroup.equals("Original") && currentReferralType.equals("Student")) {

                currentProgramGroup = "Guests";

            }

            // Update student count and amount for referral type
            List<Double> currentReferralTypeValues = (ArrayList) studioSalesBreakdownCountsAndAmounts.get(currentProgramGroup);
            currentReferralTypeValues.set(1, currentReferralTypeValues.get(1) + 1.0);
            currentReferralTypeValues.set(2, currentReferralTypeValues.get(2) + currentSaleAmount);

        }

        /*
         Try Column
         */
        // Create query for student originals and guests try count, all scheduled first lessons in the Original program group
        String studioSalesBreakdownTryOriginalsQuery = String.format("SELECT ST.ReferralType FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON"
                + " LS.EnrollmentID=EN.EnrollmentID INNER JOIN Students as ST ON ST.StudentID=LS.StudentID WHERE LS.DateCreated between DATE '%s' and '%s' AND"
                + " LS.FirstLesson=TRUE AND EN.ProgramGroup='Original';", startDate, endDate);
        ResultSet studioSalesBreakdownTryOriginalsSet = connection.prepareStatement(studioSalesBreakdownTryOriginalsQuery).executeQuery();

        while (studioSalesBreakdownTryOriginalsSet.next()) {

            // Get variable
            String currentReferralType = studioSalesBreakdownTryOriginalsSet.getString(1);

            // Check if Original is from Guest or another source
            String currentProgramGroup = "Original";
            if (currentReferralType.equals("Student")) {
                currentProgramGroup = "Guests";
            }

            // Update student try count for referral type
            List<Double> currentReferralTypeValues = (ArrayList) studioSalesBreakdownCountsAndAmounts.get(currentProgramGroup);
            currentReferralTypeValues.set(0, currentReferralTypeValues.get(0) + 1.0);

        }

        // Create query for student pre, ext and reext, all attended last lessons in the previous enrollment type (orig>pre, pre>ext, ext>reext)
        String studioSalesBreakdownTryOthersQuery = String.format("SELECT EN.ProgramGroup FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID"
                + " WHERE EN.IsCompleted=TRUE AND LS.LessonStatus='Attended' AND LS.DateCreated between DATE '%s' and '%s' ORDER BY LS.StudentID DESC", startDate, endDate);
        ResultSet studioSalesBreakdownTryOthersSet = connection.prepareStatement(studioSalesBreakdownTryOthersQuery).executeQuery();

        while (studioSalesBreakdownTryOthersSet.next()) {

            // Get variable
            String previousProgramGroup = studioSalesBreakdownTryOthersSet.getString(1);

            // Set current program group from previous
            String currentProgramGroup = "Misc";
            if (previousProgramGroup.equals("Original")) {
                currentProgramGroup = "Preliminary";
            } else if (previousProgramGroup.equals("Preliminary")) {
                currentProgramGroup = "Extension";
            } else if (previousProgramGroup.equals("Extension")) {
                currentProgramGroup = "ReExtension";
            }

            // Update student try count for referral type
            List<Double> currentReferralTypeValues = (ArrayList) studioSalesBreakdownCountsAndAmounts.get(currentProgramGroup);
            currentReferralTypeValues.set(0, currentReferralTypeValues.get(0) + 1.0);

        }

        // Set Renewal tries to the same as sales
        List<Double> currentReferralTypeValues = (ArrayList) studioSalesBreakdownCountsAndAmounts.get("Renewal");
        currentReferralTypeValues.set(0, currentReferralTypeValues.get(1));

        /*
         Format Data
         */
        // Set order of referral values
        Map<String, Integer> referralTypeOrder = new HashMap();
        referralTypeOrder.put("Original", 0);
        referralTypeOrder.put("Guests", 1);
        referralTypeOrder.put("Preliminary", 2);
        referralTypeOrder.put("Extension", 3);
        referralTypeOrder.put("ReExtension", 4);
        referralTypeOrder.put("Renewal", 5);
        referralTypeOrder.put("Misc", 6);

        // Prefill indexes
        for (int i = 0; i <= 6; i++) {
            studioSalesBreakdownReportData.add(i, null);
        }

        for (Map.Entry<String, Object> entry : studioSalesBreakdownCountsAndAmounts.entrySet()) {

            // Get referral types and corresponding try, sold, amount values
            String currentReferralType = entry.getKey();
            currentReferralTypeValues = (ArrayList) entry.getValue();
            double currentTries = genericHelper.roundDecimalToTwoDecimalPlaces(currentReferralTypeValues.get(0));
            double currentSales = genericHelper.roundDecimalToTwoDecimalPlaces(currentReferralTypeValues.get(1));
            double currentAmount = genericHelper.roundDecimalToTwoDecimalPlaces(currentReferralTypeValues.get(2));

            // Create new row and add to data list at correct position for referral value, after prefilling indexes
            List<Object> dataRow = new ArrayList();
            dataRow.add(currentReferralType);
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(currentTries));
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(currentSales));
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(currentAmount));
            studioSalesBreakdownReportData.set(referralTypeOrder.get(currentReferralType), dataRow);

        }

        // Add headers
        studioSalesBreakdownReportHeaders.add("Referral Type");
        studioSalesBreakdownReportHeaders.add("Tries");
        studioSalesBreakdownReportHeaders.add("Sales");
        studioSalesBreakdownReportHeaders.add("Amount");

        // Add key/values and Original report to Reports ArrayList
        studioSalesBreakdownReportMap.put("title", "Sales Breakdown");
        studioSalesBreakdownReportMap.put("headers", studioSalesBreakdownReportHeaders);
        studioSalesBreakdownReportMap.put("data", studioSalesBreakdownReportData);

        return studioSalesBreakdownReportMap;
    }

    private Map<String, Object> getStudioCashReceiptsSubReport(String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map studioCashReceiptsReportMap = new HashMap();
        List<String> studioCashReceiptsReportHeaders = new ArrayList();
        List<List<Object>> studioCashReceiptsReportData = new ArrayList();

        // Prepare variables
        double lessonPrice = 0.0, lessonPriceTotal = 0.0, enrollmentPaymentTotal = 0.0, enrollmentPaymentsTotal = 0.0;

        // Create query for value of all lessons actually taught during period
        String studioCashReceiptsLessonsTaughtValueQuery = String.format("SELECT LS.RateType, EN.PrivateLessonPrice, EN.GroupLessonPrice, EN.PartyLessonPrice FROM LessonSchedule as LS INNER JOIN"
                + " ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.AppointmentDate between DATE '%s' and '%s' AND LS.LessonStatus='Attended';",
                startDate, endDate);
        ResultSet studioCashReceiptsLessonsTaughtValueSet = connection.prepareStatement(studioCashReceiptsLessonsTaughtValueQuery).executeQuery();

        while (studioCashReceiptsLessonsTaughtValueSet.next()) {

            // Get lesson price from type
            String lessonType = studioCashReceiptsLessonsTaughtValueSet.getString(1);
            String[] lessonPrices = {studioCashReceiptsLessonsTaughtValueSet.getString(2), studioCashReceiptsLessonsTaughtValueSet.getString(3), studioCashReceiptsLessonsTaughtValueSet.getString(4)};
            lessonPrice = genericHelper.roundDecimalToTwoDecimalPlaces(Double.parseDouble(report.getLessonPriceFromType(lessonType, lessonPrices)));

            // Add to lesson price total
            lessonPriceTotal += lessonPrice;

        }

        // Create query for value of all money actually paid during period
        String studioCashReceiptsEnrollmentsSalesQuery = String.format("SELECT PT.Amount FROM PaymentTransaction AS PT WHERE PT.DateCreated"
                + " BETWEEN DATE '%s' and '%s';",
                startDate, endDate);
        ResultSet studioCashReceiptsEnrollmentSalesSet = connection.prepareStatement(studioCashReceiptsEnrollmentsSalesQuery).executeQuery();

        while (studioCashReceiptsEnrollmentSalesSet.next()) {

            // Get enrollment contract total
            enrollmentPaymentTotal = genericHelper.roundDecimalToTwoDecimalPlaces(studioCashReceiptsEnrollmentSalesSet.getDouble(1));

            // Add contract to total contracts count
            enrollmentPaymentsTotal += enrollmentPaymentTotal;

        }

        // Create Data Rows
        List<Object> dataRow = new ArrayList();
        dataRow.add("Lesson Cash Total");
        dataRow.add(String.valueOf(lessonPriceTotal));
        List<Object> dataRow2 = new ArrayList();
        dataRow2.add("Enrollment Sales Total");
        dataRow2.add(String.valueOf(enrollmentPaymentsTotal));
        studioCashReceiptsReportData.add(dataRow);
        studioCashReceiptsReportData.add(dataRow2);

        // Add headers
        studioCashReceiptsReportHeaders.add("Type");
        studioCashReceiptsReportHeaders.add("Amount");

        // Add key/values and Original report to Reports ArrayList
        studioCashReceiptsReportMap.put("title", "Cash Receipts");
        studioCashReceiptsReportMap.put("headers", studioCashReceiptsReportHeaders);
        studioCashReceiptsReportMap.put("data", studioCashReceiptsReportData);

        return studioCashReceiptsReportMap;
    }

    private Map<String, Object> getStudioLessonBreakdownSubReport(String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map<String, Object> studioLessonBreakdownCounts = new HashMap();
        Map studioLessonBreakdownReportMap = new HashMap();
        List<String> studioLessonBreakdownReportHeaders = new ArrayList();
        List<List<Object>> studioLessonBreakdownReportData = new ArrayList();

        // Prepare variables
        String lessonType = "", programGroup = "";
        boolean firstLesson = false, paidWithBonus = false;
        double privateLessonsEnrolled = 0.0, privateLessonsAttended = 0.0, groupLessonsEnrolled = 0.0, groupLessonsAttended = 0.0, partyLessonsEnrolled = 0.0, partyLessonsAttended = 0.0,
                bonusLessonsEnrolled = 0.0, bonusLessonsAttended = 0.0, otherLessonsEnrolled = 0.0, otherLessonsAttended = 0.0, lessonUnits = 0.0, lessonEnrollmentTotal = 0.0;

        // Create query for value of all lessons taught during period
        String studioLessonBreakdownQuery = String.format("SELECT LS.RateType, LS.FirstLesson, LS.PaidWithBonus, LS.LessonUnits, EN.ProgramGroup, EN.PrivateLessonTotal, EN.GroupLessonTotal,"
                + "EN.PartyLessonTotal FROM LessonSchedule as LS INNER JOIN"
                + " ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.AppointmentDate between DATE '%s' and '%s' AND LS.LessonStatus='Attended';",
                startDate, endDate);
        ResultSet studioLessonBreakdownSet = connection.prepareStatement(studioLessonBreakdownQuery).executeQuery();

        while (studioLessonBreakdownSet.next()) {

            // Get variables
            lessonType = studioLessonBreakdownSet.getString(1);
            firstLesson = studioLessonBreakdownSet.getBoolean(2);
            paidWithBonus = studioLessonBreakdownSet.getBoolean(3);
            lessonUnits = genericHelper.roundDecimalToTwoDecimalPlaces(studioLessonBreakdownSet.getDouble(4));
            programGroup = studioLessonBreakdownSet.getString(5);

            // Increment Lessons Enrolled counters
            if (firstLesson) {

                // Get variables
                lessonEnrollmentTotal = genericHelper.roundDecimalToTwoDecimalPlaces(studioLessonBreakdownSet.getDouble(6) + studioLessonBreakdownSet.getDouble(7) + studioLessonBreakdownSet.getDouble(8));

                if (paidWithBonus) {

                    bonusLessonsEnrolled += lessonEnrollmentTotal;

                } else if (programGroup.equals("Other")) {

                    otherLessonsEnrolled += lessonEnrollmentTotal;

                } else {

                    if (lessonType.equals("Private")) {

                        privateLessonsEnrolled += lessonEnrollmentTotal;

                    } else if (lessonType.equals("Group")) {

                        groupLessonsEnrolled += lessonEnrollmentTotal;

                    } else if (lessonType.equals("Party")) {

                        partyLessonsEnrolled += lessonEnrollmentTotal;

                    }

                }

            }

            // Increment Lesson Attended counters
            if (paidWithBonus) {

                bonusLessonsAttended += lessonUnits;

            } else if (programGroup.equals("Other")) {

                otherLessonsAttended += lessonUnits;

            } else {

                if (lessonType.equals("Private")) {

                    privateLessonsAttended += lessonUnits;

                } else if (lessonType.equals("Group")) {

                    groupLessonsAttended += lessonUnits;

                } else if (lessonType.equals("Party")) {

                    partyLessonsAttended += lessonUnits;

                }

            }

        }

        // Create Data Rows
        String[] lessonTypes = {"Private", "Group", "Party", "Bonus", "Other"};

        for (int i = 0; i < lessonTypes.length; i++) {

            List<Object> dataRow = new ArrayList();
            dataRow.add(lessonTypes[i]);

            if (lessonTypes[i].equals("Private")) {

                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(privateLessonsEnrolled));
                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(privateLessonsAttended));

            } else if (lessonTypes[i].equals("Group")) {

                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(groupLessonsEnrolled));
                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(groupLessonsAttended));

            } else if (lessonTypes[i].equals("Party")) {

                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(partyLessonsEnrolled));
                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(partyLessonsAttended));

            } else if (lessonTypes[i].equals("Bonus")) {

                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(bonusLessonsEnrolled));
                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(bonusLessonsAttended));

            } else if (lessonTypes[i].equals("Other")) {

                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(otherLessonsEnrolled));
                dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(otherLessonsAttended));

            }

            studioLessonBreakdownReportData.add(dataRow);

        }

        // Add headers
        studioLessonBreakdownReportHeaders.add("Type");
        studioLessonBreakdownReportHeaders.add("Enrolled");
        studioLessonBreakdownReportHeaders.add("Taught");

        // Add key/values and Original report to Reports ArrayList
        studioLessonBreakdownReportMap.put("title", "Lesson Breakdown");
        studioLessonBreakdownReportMap.put("headers", studioLessonBreakdownReportHeaders);
        studioLessonBreakdownReportMap.put("data", studioLessonBreakdownReportData);

        return studioLessonBreakdownReportMap;
    }

    private Map<String, Object> getInstructorLessonCashBreakdownSubReport(ResultSet instructorsSet, String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map instructorLessonCashBreakdownReportMap = new HashMap();
        List<String> instructorLessonCashBreakdownReportHeaders = new ArrayList();
        List<List<Object>> instructorLessonCashBreakdownReportData = new ArrayList();

        while (instructorsSet.next()) {

            String instructorID = instructorsSet.getString(1);
            String lastName = instructorsSet.getString(2);
            String firstName = instructorsSet.getString(3);

            // Prepare variables
            double lessonsTaught = 0.0, lessonsCashValue = 0.0, privateLessonsEnrolled = 0.0, groupLessonsEnrolled = 0.0, partyLessonsEnrolled = 0.0, bonusesEnrolledAndTaught = 0.0,
                    privateLessonTotal = 0.0, groupLessonTotal = 0.0, partyLessonTotal = 0.0, lessonPrice = 0.0, bonusesAwardedTotal = 0.0, bonusReferringStudentInstructorCount = 1.0,
                    lessonUnits = 0.0;
            String lessonType = "", bonusReferringStudentInstructorID = "None", bonusReferringStudentInstructorID2 = "None", bonusReferringStudentInstructorID3 = "None";
            String[] lessonPrices = new String[3];
            boolean firstLesson = false;

            // Create query for value of all lessons taught during period
            String instructorLessonCashBreakdownLessonsQuery = String.format("SELECT LS.RateType, EN.PrivateLessonPrice, EN.GroupLessonPrice, EN.PartyLessonPrice,"
                    + " LS.FirstLesson, EN.PrivateLessonTotal, EN.GroupLessonTotal, EN.PartyLessonTotal, EN.BonusesAwardedPrivate, EN.BonusesAwardedGroup, EN.BonusesAwardedParty, LS.LessonUnits "
                    + " FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.AppointmentDate between"
                    + " DATE '%s' and '%s' AND LS.LessonStatus='Attended' AND LS.InstructorID='%s';",
                    startDate, endDate, instructorID);
            ResultSet instructorLessonCashBreakdownLessonsSet = connection.prepareStatement(instructorLessonCashBreakdownLessonsQuery).executeQuery();

            while (instructorLessonCashBreakdownLessonsSet.next()) {

                // Get variables
                lessonType = instructorLessonCashBreakdownLessonsSet.getString(1);
                lessonPrices[0] = instructorLessonCashBreakdownLessonsSet.getString(2);
                lessonPrices[1] = instructorLessonCashBreakdownLessonsSet.getString(3);
                lessonPrices[2] = instructorLessonCashBreakdownLessonsSet.getString(4);
                lessonPrice = genericHelper.roundDecimalToTwoDecimalPlaces(Double.parseDouble(report.getLessonPriceFromType(lessonType, lessonPrices)));
                firstLesson = instructorLessonCashBreakdownLessonsSet.getBoolean(5);
                privateLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownLessonsSet.getDouble(6));
                groupLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownLessonsSet.getDouble(7));
                partyLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownLessonsSet.getDouble(8));
                lessonUnits = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownLessonsSet.getDouble(12));

                // Increment lessons taught and total cash value
                lessonsTaught += lessonUnits;
                lessonsCashValue += lessonPrice;

                // Check if new enrollment and increment enrolled lessons
                if (firstLesson) {

                    privateLessonsEnrolled += privateLessonTotal;
                    groupLessonsEnrolled += groupLessonTotal;
                    partyLessonsEnrolled += partyLessonTotal;

                    bonusesAwardedTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownLessonsSet.getDouble(9) + instructorLessonCashBreakdownLessonsSet.getDouble(10)
                            + instructorLessonCashBreakdownLessonsSet.getDouble(11));

                    bonusesEnrolledAndTaught += bonusesAwardedTotal;

                }

            }

            // Create query for bonus transactions during period
            String instructorLessonCashBreakdownBonusesQuery = String.format("SELECT ReferringStudentCurrentInstructorID,ReferringStudentCurrentInstructorID2,"
                    + "ReferringStudentCurrentInstructorID3,UnitsUsed,"
                    + "TransactionType FROM BonusTransaction WHERE RedeemedOn BETWEEN DATE '%s' and '%s' AND TransactionType='RedeemedPending' AND"
                    + " (ReferringStudentCurrentInstructorID='%s' OR ReferringStudentCurrentInstructorID2='%s' OR ReferringStudentCurrentInstructorID3='%s');",
                    startDate, endDate, instructorID, instructorID, instructorID);
            ResultSet instructorLessonCashBreakdownBonusesSet = connection.prepareStatement(instructorLessonCashBreakdownBonusesQuery).executeQuery();

            while (instructorLessonCashBreakdownBonusesSet.next()) {

                // Get student instructors at time of original referral
                bonusReferringStudentInstructorID = instructorLessonCashBreakdownBonusesSet.getString(1);
                bonusReferringStudentInstructorID2 = instructorLessonCashBreakdownBonusesSet.getString(2);
                bonusReferringStudentInstructorID3 = instructorLessonCashBreakdownBonusesSet.getString(3);

                // Get count of instructors
                if ((bonusReferringStudentInstructorID != null) && !(bonusReferringStudentInstructorID.equals("None")) && !(bonusReferringStudentInstructorID.equals("null"))
                        && !(bonusReferringStudentInstructorID.equals("")) && !(bonusReferringStudentInstructorID.equals(instructorID))) {
                    bonusReferringStudentInstructorCount += 1.0;
                }
                if ((bonusReferringStudentInstructorID2 != null) && !(bonusReferringStudentInstructorID2.equals("None")) && !(bonusReferringStudentInstructorID2.equals("null"))
                        && !(bonusReferringStudentInstructorID2.equals("")) && !(bonusReferringStudentInstructorID2.equals(instructorID))) {
                    bonusReferringStudentInstructorCount += 1.0;
                }
                if ((bonusReferringStudentInstructorID3 != null) && !(bonusReferringStudentInstructorID3.equals("None")) && !(bonusReferringStudentInstructorID3.equals("null"))
                        && !(bonusReferringStudentInstructorID3.equals("")) && !(bonusReferringStudentInstructorID3.equals(instructorID))) {
                    bonusReferringStudentInstructorCount += 1.0;
                }

                // Add units redeemed to bonus award total, divided by number of instructors
                bonusesAwardedTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorLessonCashBreakdownBonusesSet.getDouble(4) / bonusReferringStudentInstructorCount);

                // Increment bonus counter
                bonusesEnrolledAndTaught += bonusesAwardedTotal;
            }

            // Create Data Rows
            List<Object> dataRow = new ArrayList();
            dataRow.add(lastName + ", " + firstName);
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(lessonsTaught));
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(lessonsCashValue));
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(privateLessonsEnrolled) + "/" + genericHelper.roundToIntegerOrDecimalAsString(groupLessonsEnrolled) + "/" + genericHelper.roundToIntegerOrDecimalAsString(partyLessonsEnrolled));
            dataRow.add(genericHelper.roundToIntegerOrDecimalAsString(bonusesEnrolledAndTaught));
            instructorLessonCashBreakdownReportData.add(dataRow);
        }

        // Add headers
        instructorLessonCashBreakdownReportHeaders.add("Teacher");
        instructorLessonCashBreakdownReportHeaders.add("Lessons Taught");
        instructorLessonCashBreakdownReportHeaders.add("Lesson Cash");
        instructorLessonCashBreakdownReportHeaders.add("Lessons Enrolled");
        instructorLessonCashBreakdownReportHeaders.add("Bonuses Enrolled/Redeemed");

        // Add key/values and Original report to Reports ArrayList
        instructorLessonCashBreakdownReportMap.put("title", "Instructor Lesson/Cash Breakdown");
        instructorLessonCashBreakdownReportMap.put("headers", instructorLessonCashBreakdownReportHeaders);
        instructorLessonCashBreakdownReportMap.put("data", instructorLessonCashBreakdownReportData);

        return instructorLessonCashBreakdownReportMap;
    }

    private Map<String, Object> getInstructorSalesProgramGroupSubReport(ResultSet instructorsSet, String startDate, String endDate) throws SQLException {

        // Prepare report objects
        Map<String, Object> instructorSalesProgramGroupCountsAndAmounts = new HashMap();
        Map instructorSalesProgramGroupReportMap = new HashMap();
        List<String> instructorSalesProgramGroupReportHeaders = new ArrayList();
        List<List<Object>> instructorSalesProgramGroupReportData = new ArrayList();

        String[] programGroupFullNames = {"Original", "Preliminary", "Extension", "ReExtension", "Renewal"};
        String[] programGroupShortNames = {"Orig", "Pre", "Ext", "ReExt", "Ren"};

        while (instructorsSet.next()) {

            String instructorID = instructorsSet.getString(1);
            String lastName = instructorsSet.getString(2);
            String firstName = instructorsSet.getString(3);

            // Prepare variables
            double privateLessonsEnrolled = 0.0, groupLessonsEnrolled = 0.0, partyLessonsEnrolled = 0.0, currentPrivateLessonsEnrolled = 0.0, currentGroupLessonsEnrolled = 0.0,
                    currentPartyLessonsEnrolled = 0.0, contractTotal = 0.0, currentContractTotal = 0.0, numberOfEnrollments = 0.0, currentNumberOfEnrollments = 0.0;
            int currentNumberOfInstructors = 1;
            String programGroup = "", primaryInstructorID = "", instructorID1 = "", instructorID2 = "";

            // Prepopulate values in data counts map
            for (int i = 0; i < programGroupFullNames.length; i++) {

                instructorSalesProgramGroupCountsAndAmounts.put(String.format("private%sLessonsEnrolled", programGroupFullNames[i]), 0.00);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("group%sLessonsEnrolled", programGroupFullNames[i]), 0.00);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("party%sLessonsEnrolled", programGroupFullNames[i]), 0.00);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("%sContractTotal", programGroupFullNames[i]), 0.00);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("new%sEnrollments", programGroupFullNames[i]), 0.00);
            }

            // Create query for number of new enrollments, number of lessons in new enrollments, cash value of new enrollments, divided by teacher
            String instructorSalesProgramGroupQuery = String.format("SELECT EN.ContractTotal,"
                    + " EN.PrivateLessonTotal, EN.GroupLessonTotal, EN.PartyLessonTotal, EN.PrimaryInstructorID, EN.InstructorID1, EN.InstructorID2, EN.ProgramGroup"
                    + " FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON EN.EnrollmentID=LS.EnrollmentID WHERE LS.AppointmentDate between"
                    + " DATE '%s' and '%s' AND LS.LessonStatus='Attended' AND LS.FirstLesson=TRUE AND EN.ProgramGroup!='Other' AND (EN.PrimaryInstructorID='%s' OR EN.InstructorID1='%s' "
                    + " OR EN.InstructorID2='%s') ;",
                    startDate, endDate, instructorID, instructorID, instructorID);
            ResultSet instructorSalesProgramGroupSet = connection.prepareStatement(instructorSalesProgramGroupQuery).executeQuery();

            while (instructorSalesProgramGroupSet.next()) {

                // Get variables
                currentContractTotal = genericHelper.roundDecimalToTwoDecimalPlaces(instructorSalesProgramGroupSet.getDouble(1));
                currentPrivateLessonsEnrolled = genericHelper.roundDecimalToTwoDecimalPlaces(instructorSalesProgramGroupSet.getDouble(2));
                currentGroupLessonsEnrolled = genericHelper.roundDecimalToTwoDecimalPlaces(instructorSalesProgramGroupSet.getDouble(3));
                currentPartyLessonsEnrolled = genericHelper.roundDecimalToTwoDecimalPlaces(instructorSalesProgramGroupSet.getDouble(4));
                primaryInstructorID = instructorSalesProgramGroupSet.getString(5);
                instructorID1 = instructorSalesProgramGroupSet.getString(6);
                instructorID2 = instructorSalesProgramGroupSet.getString(7);
                programGroup = instructorSalesProgramGroupSet.getString(8);

                // Get number of instructors
                if ((primaryInstructorID != null) && !(primaryInstructorID.equals("None")) && !(primaryInstructorID.equals("null")) && !(primaryInstructorID.equals("")) && !(primaryInstructorID.equals(instructorID))) {
                    currentNumberOfInstructors += 1;
                }
                if ((instructorID1 != null) && !(instructorID1.equals("None")) && !(instructorID1.equals("null")) && !(instructorID1.equals("")) && !(instructorID1.equals(instructorID))) {
                    currentNumberOfInstructors += 1;
                }
                if ((instructorID2 != null) && !(instructorID2.equals("None")) && !(instructorID2.equals("null")) && !(instructorID2.equals("")) && !(instructorID2.equals(instructorID))) {
                    currentNumberOfInstructors += 1;
                }

                // Update values by number of instructors
                currentPrivateLessonsEnrolled = currentPrivateLessonsEnrolled / currentNumberOfInstructors;
                currentGroupLessonsEnrolled = currentGroupLessonsEnrolled / currentNumberOfInstructors;
                currentPartyLessonsEnrolled = currentPartyLessonsEnrolled / currentNumberOfInstructors;
                currentContractTotal = currentContractTotal / currentNumberOfInstructors;
                currentNumberOfEnrollments = (1.0 / currentNumberOfInstructors);
                privateLessonsEnrolled += currentPrivateLessonsEnrolled;
                groupLessonsEnrolled += currentGroupLessonsEnrolled;
                partyLessonsEnrolled += currentPartyLessonsEnrolled;
                contractTotal += currentContractTotal;
                numberOfEnrollments += currentNumberOfEnrollments;

                // Go through program group names and update variables
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("private%sLessonsEnrolled", programGroup), privateLessonsEnrolled);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("group%sLessonsEnrolled", programGroup), groupLessonsEnrolled);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("party%sLessonsEnrolled", programGroup), partyLessonsEnrolled);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("%sContractTotal", programGroup), contractTotal);
                instructorSalesProgramGroupCountsAndAmounts.put(String.format("new%sEnrollments", programGroup), numberOfEnrollments);

                // Reset variables
                currentNumberOfInstructors = 1;

            }

            // Create Data Object Format
            List<Object> dataRow = new ArrayList();
            dataRow.add(lastName + ", " + firstName);
            for (int i = 0; i < programGroupFullNames.length; i++) {

                // Create Data Rows
                dataRow.add(String.format("%.2f/%.2f/%.2f", instructorSalesProgramGroupCountsAndAmounts.get(String.format("private%sLessonsEnrolled", programGroupFullNames[i])),
                        instructorSalesProgramGroupCountsAndAmounts.get(String.format("group%sLessonsEnrolled", programGroupFullNames[i])),
                        instructorSalesProgramGroupCountsAndAmounts.get(String.format("party%sLessonsEnrolled", programGroupFullNames[i]))));
                dataRow.add(String.format("%.2f", instructorSalesProgramGroupCountsAndAmounts.get(String.format("new%sEnrollments", programGroupFullNames[i]))));
                dataRow.add(String.format("%.2f", instructorSalesProgramGroupCountsAndAmounts.get(String.format("%sContractTotal", programGroupFullNames[i]))));

            }
            instructorSalesProgramGroupReportData.add(dataRow);
        }

        // Add headers
        instructorSalesProgramGroupReportHeaders.add("Teacher");
        for (int i = 0; i < programGroupShortNames.length; i++) {

            instructorSalesProgramGroupReportHeaders.add(String.format("Lessons Enrolled (%s)", programGroupShortNames[i]));
            instructorSalesProgramGroupReportHeaders.add(String.format("Num. Enrollments (%s)", programGroupShortNames[i]));
            instructorSalesProgramGroupReportHeaders.add(String.format("Cash of Sales (%s)", programGroupShortNames[i]));
        }

        // Add key/values and Original report to Reports ArrayList
        instructorSalesProgramGroupReportMap.put("title", "Instructor Sales/Program Group Breakdown");
        instructorSalesProgramGroupReportMap.put("headers", instructorSalesProgramGroupReportHeaders);
        instructorSalesProgramGroupReportMap.put("data", instructorSalesProgramGroupReportData);

        return instructorSalesProgramGroupReportMap;
    }

    private void newStudentReportUPSReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        List<Map<String, Object>> reportsArrayList = new ArrayList();

        // Get selected dates
        String startDate = sqlDateFormat.format(studioStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studioEndDateChooser.getDate());

        // Get instructor id
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String currentInstructorName = ((String) instructorTable.getModel().getValueAt(row, 1) + " " + (String) instructorTable.getModel().getValueAt(row, 0));

        // Add subreports to report
        reportsArrayList.add(getNewStudentSubReport(currentInstructorID, startDate, endDate, "New Students"));
        reportsArrayList.add(getNewStudentSubReport(currentInstructorID, startDate, endDate, "Guests"));

        // Create report title
        String reportSubHeader = String.format("%s: %s to %s", currentInstructorName,
                reportDateFormat.format(instructorStartDateChooser.getDate()).toString(),
                reportDateFormat.format(instructorEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportMultiReport("NewStudentReportUPSReport", "New Student Report/UPS",
                reportSubHeader, reportsArrayList);

    }

    private Map<String, Object> getNewStudentSubReport(String instructorID, String startDate, String endDate, String reportType)
            throws SQLException {

        // Prepare report objects
        Map newStudentSubReportMap = new HashMap();
        List<String> newStudentSubReportHeaders = new ArrayList();
        List<List<Object>> newStudentSubReportData = new ArrayList();

        // Generate report for either Guests or New Students
        String newStudentReportQuery = "";
        if (reportType.equals("Guests")) {
            newStudentReportQuery = String.format("SELECT ST.DateCreated,ST.StudentID,ST.LName,ST.FName,ST.ReferralType,ST.Phone,"
                    + "ST.BirthDate,ST.ReferralType FROM Students as ST WHERE ST.InstructorID='%s' AND ST.DateCreated"
                    + " BETWEEN DATE '%s' and '%s' AND ST.ReferralType='Student'"
                    + " ORDER BY ST.DateCreated ASC;",
                    instructorID, startDate, endDate);
        } else {
            newStudentReportQuery = String.format("SELECT ST.DateCreated,ST.StudentID,ST.LName,St.FName,ST.ReferralType,ST.Phone,"
                    + "ST.BirthDate,ST.ReferralType FROM Students as ST WHERE ST.InstructorID='%s' AND ST.DateCreated"
                    + " BETWEEN DATE '%s' and '%s' AND ST.ReferralType!='Student'"
                    + " ORDER BY ST.DateCreated ASC;",
                    instructorID, startDate, endDate);
        }

        ResultSet newStudentReportResultset = connection.prepareStatement(newStudentReportQuery).executeQuery();

        // Add Data
        while (newStudentReportResultset.next()) {

            // Prepare Data
            String dateCreated = newStudentReportResultset.getString(1);
            String currentStudentID = newStudentReportResultset.getString(2);
            String studentName = String.format("%s, %s", newStudentReportResultset.getString(4),
                    newStudentReportResultset.getString(3));
            String referralSource = newStudentReportResultset.getString(5);
            String phone = newStudentReportResultset.getString(6);
            String[] startingLessonDates = reportHelper.nsupsGetStartingLessonsForStudent(currentStudentID);
            String secondEnrollmentData = reportHelper.nsupsGetStudentSecondEnrollment(currentStudentID);

            String qualified = "-", referrerBonuses = "-";
            if (reportType.equals("Guests")) {
                qualified = report.getStudentIsQualified(newStudentReportResultset.getDate(7));

                if (newStudentReportResultset.getString(8).equals("Student")) {
                    referrerBonuses = reportHelper.nsupsGetReferrerBonuses(currentStudentID);
                }
            }

            // Add to Data List
            List<Object> dataRow = new ArrayList();
            dataRow.add(dateCreated);
            dataRow.add(studentName);
            dataRow.add(referralSource);
            dataRow.add(phone);
            if (reportType.equals("Guests")) {
                dataRow.add(qualified);
                dataRow.add(referrerBonuses);
            }
            dataRow.add(startingLessonDates[0]);
            dataRow.add(startingLessonDates[1]);
            dataRow.add(startingLessonDates[2]);
            dataRow.add(secondEnrollmentData);
            newStudentSubReportData.add(dataRow);

        }

        // Add headers
        newStudentSubReportHeaders.add("Date");
        newStudentSubReportHeaders.add("Student Name");
        newStudentSubReportHeaders.add("Source");
        newStudentSubReportHeaders.add("Phone");
        if (reportType.equals("Guests")) {
            newStudentSubReportHeaders.add("Q/UQ");
            newStudentSubReportHeaders.add("Referrer Bonuses");
        }
        newStudentSubReportHeaders.add("1st Lesson");
        newStudentSubReportHeaders.add("2nd Lesson");
        newStudentSubReportHeaders.add("Grp/Party");
        newStudentSubReportHeaders.add("Result");

        // Add key/values and Original report to Reports ArrayList
        newStudentSubReportMap.put("title", String.format("%s Report", reportType));
        newStudentSubReportMap.put("headers", newStudentSubReportHeaders);
        newStudentSubReportMap.put("data", newStudentSubReportData);

        return newStudentSubReportMap;
    }

    // Percentage of students who progressed to a higher program group from original program group
    private void studentProgressionPercentagesStudioReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare utility data structures
        Map<String, Boolean> checkedStudentIDCurrentYear = new HashMap();
        Map<String, Object> programGroupProgressionCountsCurrentYear = new HashMap();
        Map<String, Boolean> checkedStudentIDPreviousYear = new HashMap();
        Map<String, Object> programGroupProgressionCountsPreviousYear = new HashMap();

        // Start progression names array
        String[] progressionNames = {"origToPre", "origToExt", "origToReext", "origToRen", "preToExt", "preToReext", "preToRen", "extToReext", "extToRen", "reextToRen"};
        String[] formattedProgressionNames = {"Orig>Pre", "Orig>Ext", "Orig>ReExt", "Orig>Ren", "Pre>Ext", "Pre>ReExt", "Pre>Ren", "Ext>ReExt", "Ext>Ren", "ReExt>Ren"};

        List<Double> startingValues = new ArrayList();
        startingValues.add(0.0);
        startingValues.add(0.0);
        for (int i = 0; i < progressionNames.length; i++) {
            programGroupProgressionCountsCurrentYear.put(progressionNames[i], startingValues);
        }
        for (int i = 0; i < progressionNames.length; i++) {
            programGroupProgressionCountsPreviousYear.put(progressionNames[i], startingValues);
        }

        // Get selected year and previous year
        String[] yearsInReport = reportHelper.sppxGetYearsInReport(studioStartDateChooser.getDate());

        // Create query
        String previousYearQuery = String.format("SELECT LS.StudentID,EN.ProgramGroup,EN.DateCreated FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON LS.EnrollmentID=EN.EnrollmentID"
                + " WHERE EN.DateCreated BETWEEN DATE '%s' and '%s' AND EN.IsCompleted=TRUE ORDER BY LS.AppointmentDate DESC;",
                yearsInReport[0], yearsInReport[1]);
        ResultSet previousYearResultSet = connection.prepareStatement(previousYearQuery).executeQuery();
        String currentYearQuery = String.format("SELECT LS.StudentID,EN.ProgramGroup,EN.DateCreated FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON LS.EnrollmentID=EN.EnrollmentID"
                + " WHERE EN.DateCreated BETWEEN DATE '%s' and '%s' AND EN.IsCompleted=TRUE ORDER BY LS.AppointmentDate DESC;",
                yearsInReport[2], yearsInReport[3]);
        ResultSet currentYearResultSet = connection.prepareStatement(currentYearQuery).executeQuery();

        while (previousYearResultSet.next()) {

            String studentID = previousYearResultSet.getString(1);
            String triedProgramGroup = previousYearResultSet.getString(2);
            String dateCreated = previousYearResultSet.getString(3);

            // If student has not been checked yet, and do not check renewals and other program groups because they are at the end/outside of progression
            if ((checkedStudentIDPreviousYear.get(studentID + triedProgramGroup) == null) && !(triedProgramGroup.equals("Renewal")) && !(triedProgramGroup.equals("Other"))) {

                // Add one to program group starting value
                reportHelper.sppxIncrementProgramGroupTriesCount(triedProgramGroup, programGroupProgressionCountsPreviousYear);

                // Query for all student enrollments created after tried enrollment
                String studentProgramGroupQuery = String.format("SELECT EN.ProgramGroup FROM ProgramEnrollment as EN"
                        + " WHERE EN.StudentID='%s' AND EN.DateCreated >= DATE '%s' AND EN.ProgramGroup != 'Other' ORDER BY EN.DateCreated DESC;",
                        studentID, dateCreated);
                ResultSet studentProgramGroupSet = connection.prepareStatement(studentProgramGroupQuery).executeQuery();

                // Check whether there was a new enrollment after the tried program enrollment
                while (studentProgramGroupSet.next()) {

                    String endProgramGroup = studentProgramGroupSet.getString(1);
                    reportHelper.sppxIncrementProgramGroupEndCount(triedProgramGroup, endProgramGroup, programGroupProgressionCountsPreviousYear);
                }

            }

            // Add student id to checked students map
            checkedStudentIDPreviousYear.put(studentID + triedProgramGroup, true);

        }

        while (currentYearResultSet.next()) {

            String studentID = currentYearResultSet.getString(1);
            String triedProgramGroup = currentYearResultSet.getString(2);
            String dateCreated = currentYearResultSet.getString(3);

            // If student has not been checked yet, and do not check renewals and other program groups because they are at the end/outside of progression
            if ((checkedStudentIDCurrentYear.get(studentID + triedProgramGroup) == null) && !(triedProgramGroup.equals("Renewal")) && !(triedProgramGroup.equals("Other"))) {

                // Add one to program group starting value
                reportHelper.sppxIncrementProgramGroupTriesCount(triedProgramGroup, programGroupProgressionCountsCurrentYear);

                // Query for all student enrollments created after tried enrollment
                String studentProgramGroupQuery = String.format("SELECT EN.ProgramGroup FROM ProgramEnrollment as EN"
                        + " WHERE EN.StudentID='%s' AND EN.DateCreated >= DATE '%s' AND EN.ProgramGroup != 'Other' ORDER BY EN.DateCreated DESC;",
                        studentID, dateCreated);
                ResultSet studentProgramGroupSet = connection.prepareStatement(studentProgramGroupQuery).executeQuery();

                // Check whether there was a new enrollment after the tried program enrollment
                while (studentProgramGroupSet.next()) {

                    String endProgramGroup = studentProgramGroupSet.getString(1);
                    reportHelper.sppxIncrementProgramGroupEndCount(triedProgramGroup, endProgramGroup, programGroupProgressionCountsCurrentYear);
                }

            }

            // Add student id to checked students map
            checkedStudentIDCurrentYear.put(studentID + triedProgramGroup, true);

        }

        // Add data to report array list
        for (int i = 0; i < progressionNames.length; i++) {
            List<Double> currentProgressionValues = (ArrayList) programGroupProgressionCountsCurrentYear.get(progressionNames[i]);
            List<Double> previousProgressionValues = (ArrayList) programGroupProgressionCountsPreviousYear.get(progressionNames[i]);

            // Calculate current year progression percentage
            double currentProgressionValuePercentage = 0.0;
            if (currentProgressionValues.get(0) != 0.0) {
                currentProgressionValuePercentage = (currentProgressionValues.get(1) / currentProgressionValues.get(0)) * 100;

            }
            // Calculate previous year progression percentage
            double previousProgressionValuePercentage = 0.0;
            if (previousProgressionValues.get(0) != 0.0) {
                previousProgressionValuePercentage = (previousProgressionValues.get(1) / previousProgressionValues.get(0)) * 100;

            }

            // Calculate percent increase or decrease
            double percentIncreaseOrDecrease = reportHelper.sppxCalculatePercentIncreaseOrDecrease(currentProgressionValuePercentage, previousProgressionValuePercentage);

            List<Object> currentRowData = new ArrayList();
            currentRowData.add(String.format("%s", formattedProgressionNames[i]));

            // Add Previous Year data
            currentRowData.add(String.format("%s%% (%s/%s)", genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValuePercentage),
                    genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValues.get(1)),
                    genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValues.get(0))));

            // Add Current Year data
            currentRowData.add(String.format("%s%% (%s/%s)", genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValuePercentage),
                    genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValues.get(1)),
                    genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValues.get(0))));

            // Add increase or decrease data
            currentRowData.add(String.format("%s%%", genericHelper.roundToIntegerOrDecimalAsString(percentIncreaseOrDecrease)));

            reportArrayList.add(currentRowData);
        }

        // Create Row Headers
        //String[] reportHeaders = {"Orig>Pre", "Orig>Ext", "Orig>ReExt", "Orig>Ren", "Pre>Ext", "Pre>ReExt", "Pre>Ren", "Ext>ReExt", "Ext>Ren", "ReExt>Ren"};
        String[] reportHeaders = {"Progression", "Previous Year", "Current Year", "Difference +/-"};

        String reportSubHeader = String.format("%s/%s", yearsInReport[0].substring(0, 4), yearsInReport[2].substring(0, 4));

        // Generate Report
        report.generateNonQueryReportSingleReport("StudentProgressionPercentagesStudio", "Student Progression Percentages - Studio",
                reportArrayList, reportHeaders, reportSubHeader, reportHeaders.length);

    }

    // Percentage of students who progressed to a higher program group from original program group
    private void studentProgressionPercentagesInstructorReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Get selected Instructor ID
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String instructorName = String.format("%s, %s", (String) instructorTable.getModel().getValueAt(row, 0),
                (String) instructorTable.getModel().getValueAt(row, 1));
        String instructorID = (String) instructorTable.getModel().getValueAt(row, 11);

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare utility data structures
        Map<String, Boolean> checkedStudentIDCurrentYear = new HashMap();
        Map<String, Object> programGroupProgressionCountsCurrentYear = new HashMap();
        Map<String, Boolean> checkedStudentIDPreviousYear = new HashMap();
        Map<String, Object> programGroupProgressionCountsPreviousYear = new HashMap();

        // Start progression names array
        String[] progressionNames = {"origToPre", "origToExt", "origToReext", "origToRen", "preToExt", "preToReext", "preToRen", "extToReext", "extToRen", "reextToRen"};
        String[] formattedProgressionNames = {"Orig>Pre", "Orig>Ext", "Orig>ReExt", "Orig>Ren", "Pre>Ext", "Pre>ReExt", "Pre>Ren", "Ext>ReExt", "Ext>Ren", "ReExt>Ren"};

        List<Double> startingValues = new ArrayList();
        startingValues.add(0.0);
        startingValues.add(0.0);
        for (int i = 0; i < progressionNames.length; i++) {
            programGroupProgressionCountsCurrentYear.put(progressionNames[i], startingValues);
        }
        for (int i = 0; i < progressionNames.length; i++) {
            programGroupProgressionCountsPreviousYear.put(progressionNames[i], startingValues);
        }

        // Get selected year and previous year
        String[] yearsInReport = reportHelper.sppxGetYearsInReport(studioStartDateChooser.getDate());

        // Create query
        String previousYearQuery = String.format("SELECT LS.StudentID,EN.ProgramGroup,EN.DateCreated FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON LS.EnrollmentID=EN.EnrollmentID"
                + " WHERE EN.DateCreated BETWEEN DATE '%s' and '%s' AND EN.IsCompleted=TRUE AND EN.PrimaryInstructorID='%s' ORDER BY LS.AppointmentDate DESC;",
                yearsInReport[0], yearsInReport[1], instructorID);
        ResultSet previousYearResultSet = connection.prepareStatement(previousYearQuery).executeQuery();
        String currentYearQuery = String.format("SELECT LS.StudentID,EN.ProgramGroup,EN.DateCreated FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON LS.EnrollmentID=EN.EnrollmentID"
                + " WHERE EN.DateCreated BETWEEN DATE '%s' and '%s' AND EN.IsCompleted=TRUE AND EN.PrimaryInstructorID='%s' ORDER BY LS.AppointmentDate DESC;",
                yearsInReport[2], yearsInReport[3], instructorID);
        ResultSet currentYearResultSet = connection.prepareStatement(currentYearQuery).executeQuery();

        while (previousYearResultSet.next()) {

            String studentID = previousYearResultSet.getString(1);
            String triedProgramGroup = previousYearResultSet.getString(2);
            String dateCreated = previousYearResultSet.getString(3);

            // If student has not been checked yet, and do not check renewals and other program groups because they are at the end/outside of progression
            if ((checkedStudentIDPreviousYear.get(studentID + triedProgramGroup) == null) && !(triedProgramGroup.equals("Renewal")) && !(triedProgramGroup.equals("Other"))) {

                // Add one to program group starting value
                reportHelper.sppxIncrementProgramGroupTriesCount(triedProgramGroup, programGroupProgressionCountsPreviousYear);

                // Query for all student enrollments created after tried enrollment
                String studentProgramGroupQuery = String.format("SELECT EN.ProgramGroup FROM ProgramEnrollment as EN"
                        + " WHERE EN.StudentID='%s' AND EN.DateCreated >= DATE '%s' AND EN.ProgramGroup != 'Other' ORDER BY EN.DateCreated DESC;",
                        studentID, dateCreated);
                ResultSet studentProgramGroupSet = connection.prepareStatement(studentProgramGroupQuery).executeQuery();

                // Check whether there was a new enrollment after the tried program enrollment
                while (studentProgramGroupSet.next()) {

                    String endProgramGroup = studentProgramGroupSet.getString(1);
                    reportHelper.sppxIncrementProgramGroupEndCount(triedProgramGroup, endProgramGroup, programGroupProgressionCountsPreviousYear);
                }

            }

            // Add student id to checked students map
            checkedStudentIDPreviousYear.put(studentID + triedProgramGroup, true);

        }

        while (currentYearResultSet.next()) {

            String studentID = currentYearResultSet.getString(1);
            String triedProgramGroup = currentYearResultSet.getString(2);
            String dateCreated = currentYearResultSet.getString(3);

            // If student has not been checked yet, and do not check renewals and other program groups because they are at the end/outside of progression
            if ((checkedStudentIDCurrentYear.get(studentID + triedProgramGroup) == null) && !(triedProgramGroup.equals("Renewal")) && !(triedProgramGroup.equals("Other"))) {

                // Add one to program group starting value
                reportHelper.sppxIncrementProgramGroupTriesCount(triedProgramGroup, programGroupProgressionCountsCurrentYear);

                // Query for all student enrollments created after tried enrollment
                String studentProgramGroupQuery = String.format("SELECT EN.ProgramGroup FROM ProgramEnrollment as EN"
                        + " WHERE EN.StudentID='%s' AND EN.DateCreated >= DATE '%s' AND EN.ProgramGroup != 'Other' ORDER BY EN.DateCreated DESC;",
                        studentID, dateCreated);
                ResultSet studentProgramGroupSet = connection.prepareStatement(studentProgramGroupQuery).executeQuery();

                // Check whether there was a new enrollment after the tried program enrollment
                while (studentProgramGroupSet.next()) {

                    String endProgramGroup = studentProgramGroupSet.getString(1);
                    reportHelper.sppxIncrementProgramGroupEndCount(triedProgramGroup, endProgramGroup, programGroupProgressionCountsCurrentYear);
                }

            }

            // Add student id to checked students map
            checkedStudentIDCurrentYear.put(studentID + triedProgramGroup, true);

        }

        // Add data to report array list
        for (int i = 0; i < progressionNames.length; i++) {
            List<Double> currentProgressionValues = (ArrayList) programGroupProgressionCountsCurrentYear.get(progressionNames[i]);
            List<Double> previousProgressionValues = (ArrayList) programGroupProgressionCountsPreviousYear.get(progressionNames[i]);

            // Calculate current year progression percentage
            double currentProgressionValuePercentage = 0.0;
            if (currentProgressionValues.get(0) != 0.0) {
                currentProgressionValuePercentage = (currentProgressionValues.get(1) / currentProgressionValues.get(0)) * 100;

            }
            // Calculate previous year progression percentage
            double previousProgressionValuePercentage = 0.0;
            if (previousProgressionValues.get(0) != 0.0) {
                previousProgressionValuePercentage = (previousProgressionValues.get(1) / previousProgressionValues.get(0)) * 100;

            }

            // Calculate percent increase or decrease
            double percentIncreaseOrDecrease = reportHelper.sppxCalculatePercentIncreaseOrDecrease(currentProgressionValuePercentage, previousProgressionValuePercentage);

            List<Object> currentRowData = new ArrayList();
            currentRowData.add(String.format("%s", formattedProgressionNames[i]));

            // Add Previous Year data
            currentRowData.add(String.format("%s%% (%s/%s)", genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValuePercentage),
                    genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValues.get(1)),
                    genericHelper.roundToIntegerOrDecimalAsString(previousProgressionValues.get(0))));

            // Add Current Year data
            currentRowData.add(String.format("%s%% (%s/%s)", genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValuePercentage),
                    genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValues.get(1)),
                    genericHelper.roundToIntegerOrDecimalAsString(currentProgressionValues.get(0))));

            // Add increase or decrease data
            currentRowData.add(String.format("%s%%", genericHelper.roundToIntegerOrDecimalAsString(percentIncreaseOrDecrease)));

            reportArrayList.add(currentRowData);
        }

        // Create Row Headers
        String[] reportHeaders = {"Progression", "Previous Year", "Current Year", "Difference +/-"};

        String reportSubHeader = String.format("%s<br />%s/%s", instructorName, yearsInReport[0].substring(0, 4), yearsInReport[2].substring(0, 4));

        // Generate Report
        report.generateNonQueryReportSingleReport("StudentProgressionPercentagesInstructor", "Student Progression Percentages - Instructor",
                reportArrayList, reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void instructorSaleIncomeReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare variables
        double valueOfLessonsEnrolled = 0.0, numberOfEnrollments = 0.0, numberOfPrivateLessons = 0.0,
                numberOfGroupLessons = 0.0, numberOfPartyLessons = 0.0;

        // Get selected Instructor
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String currentInstructorName = ((String) instructorTable.getModel().getValueAt(row, 1) + " " + (String) instructorTable.getModel().getValueAt(row, 0));

        // Get selected dates
        String startDate = sqlDateFormat.format(instructorStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(instructorEndDateChooser.getDate());

        // Prepare query
        String query = String.format("SELECT EN.ContractTotal,EN.PrimaryInstructorID,EN.InstructorID1,EN.InstructorID2,"
                + "EN.PrivateLessonTotal,EN.GroupLessonTotal,EN.PartyLessonTotal FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN"
                + " ON LS.EnrollmentID=EN.EnrollmentId WHERE LS.AppointmentDate BETWEEN DATE '%s' AND '%s' AND LS.FirstLesson=TRUE AND LS.LessonStatus='Attended' AND"
                + " (EN.PrimaryInstructorID='%s' OR EN.InstructorID1='%s' OR EN.InstructorID2='%s') ;",
                startDate, endDate, currentInstructorID, currentInstructorID, currentInstructorID);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        while (resultSet.next()) {
            double currentNumberOfInstructors = 1.0;
            double contractTotal = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(1));
            String primaryInstructorID = resultSet.getString(2);
            String instructorID1 = resultSet.getString(3);
            String instructorID2 = resultSet.getString(4);

            // Get number of instructors
            if ((primaryInstructorID != null) && !(primaryInstructorID.equals("None")) && !(primaryInstructorID.equals("null")) && !(primaryInstructorID.equals("")) && !(primaryInstructorID.equals(currentInstructorID))) {
                currentNumberOfInstructors += 1;
            }
            if ((instructorID1 != null) && !(instructorID1.equals("None")) && !(instructorID1.equals("null")) && !(instructorID1.equals("")) && !(instructorID1.equals(currentInstructorID))) {
                currentNumberOfInstructors += 1;
            }
            if ((instructorID2 != null) && !(instructorID2.equals("None")) && !(instructorID2.equals("null")) && !(instructorID2.equals("")) && !(instructorID2.equals(currentInstructorID))) {
                currentNumberOfInstructors += 1;
            }

            // Calculate contract total
            contractTotal = contractTotal / currentNumberOfInstructors;

            // Calculate Number of Enrollments
            double currentNumberOfEnrollments = genericHelper.roundDecimalToTwoDecimalPlaces(1.0 / currentNumberOfInstructors);

            // Calculate number of lessons
            double currentPrivateLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(5) / currentNumberOfInstructors);
            double currentGroupLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(6) / currentNumberOfInstructors);
            double currentPartyLessonTotal = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(7) / currentNumberOfInstructors);

            // Add to total values
            valueOfLessonsEnrolled += contractTotal;
            numberOfEnrollments += currentNumberOfEnrollments;
            numberOfPrivateLessons += currentPrivateLessonTotal;
            numberOfGroupLessons += currentGroupLessonTotal;
            numberOfPartyLessons += currentPartyLessonTotal;

        }

        // Create Data Row
        List<Object> dataRow = new ArrayList();
        dataRow.add(String.format("%.2f", numberOfEnrollments));
        dataRow.add(String.format("%.2f/%.2f/%.2f", numberOfPrivateLessons, numberOfGroupLessons, numberOfPartyLessons));
        dataRow.add(String.format("$%.2f", valueOfLessonsEnrolled));
        reportArrayList.add(dataRow);

        // Create Row Headers
        String[] reportHeaders = {"Num. Enrollments", "Total Lessons Enrolled", "Value of Lessons Enrolled"};

        // Create subheader
        String reportSubHeader = String.format("%s<br />%s to %s", currentInstructorName,
                reportDateFormat.format(instructorStartDateChooser.getDate()).toString(),
                reportDateFormat.format(instructorEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("InstructorSaleIncome", "Instructor Sale Income", reportArrayList, reportHeaders,
                reportSubHeader, reportHeaders.length);

    }

    private void instructorLessonsTaughtReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare variables
        double privateLessonsTaught = 0.0, totalLessonIncome = 0.0;

        // Get selected Instructor
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String currentInstructorName = ((String) instructorTable.getModel().getValueAt(row, 1) + " " + (String) instructorTable.getModel().getValueAt(row, 0));

        // Get selected dates
        String startDate = sqlDateFormat.format(instructorStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(instructorEndDateChooser.getDate());

        // Prepare query
        String query = String.format("SELECT LS.LessonUnits, LS.RateType, EN.PrivateLessonPrice FROM LessonSchedule as LS INNER JOIN ProgramEnrollment as EN"
                + " ON LS.EnrollmentID=EN.EnrollmentId WHERE LS.AppointmentDate BETWEEN DATE '%s' AND '%s' AND LS.LessonStatus='Attended' AND LS.InstructorID='%s' ;",
                startDate, endDate, currentInstructorID);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        while (resultSet.next()) {

            double lessonUnits = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(1));
            String lessonType = resultSet.getString(2);
            double privateLessonPrice = genericHelper.roundDecimalToTwoDecimalPlaces(resultSet.getDouble(3));

            if (lessonType.equals("Private")) {
                privateLessonsTaught = genericHelper.roundDecimalToTwoDecimalPlaces(privateLessonsTaught + lessonUnits);
                totalLessonIncome = genericHelper.roundDecimalToTwoDecimalPlaces(totalLessonIncome + privateLessonPrice);
            }

        }

        // Create Data Row
        List<Object> dataRow = new ArrayList();
        dataRow.add(String.format("%.2f", privateLessonsTaught));
        dataRow.add(String.format("$%.2f", totalLessonIncome));
        reportArrayList.add(dataRow);

        // Create Row Headers
        String[] reportHeaders = {"Num. Lessons Taught", "Value of Lessons Taught"};

        // Create subheader
        String reportSubHeader = String.format("%s<br />%s to %s", currentInstructorName,
                reportDateFormat.format(instructorStartDateChooser.getDate()).toString(),
                reportDateFormat.format(instructorEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("InstructorLessonsTaught", "Instructor Lessons Taught", reportArrayList,
                reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void instructorActiveStudentsReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Instructor
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String currentInstructorName = ((String) instructorTable.getModel().getValueAt(row, 1) + " " + (String) instructorTable.getModel().getValueAt(row, 0));

        // Get Students with at least 2 enrollments (Active)
        List<List<Object>> studentsWithTwoEnrollments = reportHelper.iasGetStudentsWithTwoEnrollments(currentInstructorID);

        // Create Row Headers
        String[] reportHeaders = {"Date Started", "Name", "Address", "Phone", "Last Lesson"};

        // Create subheader
        String reportSubHeader = currentInstructorName;

        // Generate Report
        report.generateNonQueryReportSingleReport("InstructorActiveStudents", "Instructor Active Students", studentsWithTwoEnrollments,
                reportHeaders, reportSubHeader, reportHeaders.length);
    }

    private void instructorInactiveStudentsReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Get selected Instructor
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String currentInstructorName = ((String) instructorTable.getModel().getValueAt(row, 1) + " " + (String) instructorTable.getModel().getValueAt(row, 0));

        // Get Students with at least 2 enrollments (Active)
        List<List<Object>> studentsWithTwoEnrollments = reportHelper.iisGetInactiveStudents(currentInstructorID);

        // Create Row Headers
        String[] reportHeaders = {"Date Started", "Name", "Address", "Phone", "Last Lesson"};

        // Create subheader
        String reportSubHeader = currentInstructorName;

        // Generate Report
        report.generateNonQueryReportSingleReport("InstructorInactiveStudents", "Instructor Inactive Students", studentsWithTwoEnrollments,
                reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void marketingBreakdownReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare variables
        Map<String, Object> referralTypeConversions = new HashMap();

        // Get selected dates
        String startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studentEndDateChooser.getDate());

        // Prepare query
        String query = String.format("SELECT ST.ReferralType, ST.StudentID FROM Students as ST INNER JOIN ProgramEnrollment as EN"
                + " ON ST.StudentID=EN.StudentID WHERE EN.DateCreated BETWEEN DATE '%s' AND '%s' AND EN.ProgramGroup='Original' ORDER BY ST.ReferralType DESC ;",
                startDate, endDate);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        while (resultSet.next()) {

            String referralType = resultSet.getString(1);
            String studentID = resultSet.getString(2);

            // Add key if not exists
            if (referralTypeConversions.get(referralType) == null) {
                List<Double> referralTypeValues = new ArrayList();
                referralTypeValues.add(0.0);
                referralTypeValues.add(0.0);
                referralTypeConversions.put(referralType, referralTypeValues);
            }

            // Increment value for originals in referral type map
            List<Double> currentReferralTypeValues = (ArrayList) referralTypeConversions.get(referralType);
            currentReferralTypeValues.set(0, genericHelper.roundDecimalToTwoDecimalPlaces(currentReferralTypeValues.get(0) + 1.0));

            // Check if student has purchased a Preliminary
            String preQuery = String.format("SELECT * FROM ProgramEnrollment WHERE StudentID='%s' AND ProgramGroup='Preliminary' ;", studentID);
            ResultSet preResultSet = connection.prepareStatement(preQuery).executeQuery();

            // Increment conversions if present
            if (preResultSet.next()) {
                currentReferralTypeValues.set(1, currentReferralTypeValues.get(1) + 1.0);
            }

            // Replace values for key
            referralTypeConversions.replace(referralType, currentReferralTypeValues);

        }

        // Add data to report array list
        for (Map.Entry<String, Object> entry : referralTypeConversions.entrySet()) {

            String referralType = entry.getKey();
            List<Double> currentValues = (ArrayList) entry.getValue();

            // Create Data Row
            List<Object> dataRow = new ArrayList();
            dataRow.add(referralType);
            dataRow.add(String.format("%.2f", currentValues.get(0)));
            dataRow.add(String.format("%.2f", currentValues.get(1)));
            dataRow.add(String.format("%.2f%%", ((currentValues.get(1) / currentValues.get(0)) * 100)));
            reportArrayList.add(dataRow);

        }

        // Create Row Headers
        String[] reportHeaders = {"Referral Type", "New Originals", "Purchased Preliminary", "Conversion Rate"};

        // Create subheader
        String reportSubHeader = String.format("%s to %s",
                reportDateFormat.format(studentStartDateChooser.getDate()).toString(),
                reportDateFormat.format(studentEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("MarketingBreakdown", "Marketing Breakdown", reportArrayList, reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void studentEnrollmentProgramGroupsBreakdownReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Prepare variables
        Map<String, Double> enrollmentTypeValues = new HashMap();
        String[] programGroups = {"Original", "Preliminary", "Extension", "Reextension", "Renewal", "Other"};
        for (int i = 0; i < programGroups.length; i++) {
            enrollmentTypeValues.put(programGroups[i], 0.0);
        }
        double enrollmentTotals = 0.0;

        // Get selected dates
        String startDate = sqlDateFormat.format(studentStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studentEndDateChooser.getDate());

        // Prepare query
        String query = String.format("SELECT ProgramGroup FROM ProgramEnrollment WHERE DateCreated BETWEEN DATE '%s' AND '%s' ORDER BY ProgramGroup DESC ;",
                startDate, endDate);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        while (resultSet.next()) {

            String programGroup = resultSet.getString(1);

            // Increment enrollments for program group
            double currentEnrollmentTotalForType = genericHelper.roundDecimalToTwoDecimalPlaces(enrollmentTypeValues.get(programGroup));
            currentEnrollmentTotalForType += 1.0;
            enrollmentTypeValues.replace(programGroup, currentEnrollmentTotalForType);

            // Incrememnt total enrollments
            enrollmentTotals += 1.0;

        }

        // Order entries based on array order
        for (int i = 0; i < programGroups.length; i++) {

            // Add data to report array list
            for (Map.Entry<String, Double> entry : enrollmentTypeValues.entrySet()) {

                String programGroup = entry.getKey();

                // Add current entry if equals current program group in array
                if (programGroup.equals(programGroups[i])) {

                    double currentValue = genericHelper.roundDecimalToTwoDecimalPlaces(entry.getValue());

                    // Create Data Row
                    List<Object> dataRow = new ArrayList();
                    dataRow.add(programGroup);
                    dataRow.add(String.format("%.0f", currentValue));
                    //dataRow.add(String.format("%.2f%%", ((currentValue / enrollmentTotals) * 100)));
                    reportArrayList.add(dataRow);
                }

            }
        }
        // Create Row Headers
        String[] reportHeaders = {"Program Group", "New Enrollments"};

        // Create subheader
        String reportSubHeader = String.format("%s to %s",
                reportDateFormat.format(instructorStartDateChooser.getDate()).toString(),
                reportDateFormat.format(instructorEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("StudentEnrollmentProgramGroupsBreakdown", "Student Enrollment Program Groups Breakdown",
                reportArrayList, reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void untaughtLessonLiabilityReport() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        // Prepare report array list
        List<List<Object>> reportArrayList = new ArrayList<>();

        // Get selected dates
        String startDate = sqlDateFormat.format(studioStartDateChooser.getDate());
        String endDate = sqlDateFormat.format(studioEndDateChooser.getDate());

        // Prepare query
        String query = String.format("SELECT ST.FName,ST.LName,EN.ProgramID,EN.PrivateLessonTotal,EN.GroupLessonTotal,EN.PartyLessontotal,"
                + "EN.PrivateLessonPrice,EN.GroupLessonPrice,EN.PartyLessonPrice,EN.ContractTotal,EN.PrivateLessonAttended,EN.GroupLessonAttended,"
                + "EN.PartyLessonAttended,EN.ContractPaid FROM Students as ST INNER JOIN ProgramEnrollment as EN"
                + " ON ST.StudentID=EN.StudentID WHERE EN.DateCreated BETWEEN DATE '%s' AND '%s' ORDER BY ST.LName DESC ;",
                startDate, endDate);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        while (resultSet.next()) {

            // Format data from DB
            String studentName = String.format("%s, %s", resultSet.getString(2), resultSet.getString(1));
            String programID = resultSet.getString(3);
            String totalLessonsEnrolled = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(4)),
                    genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(5)), genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(6)));
            String lessonPrices = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(7)),
                    genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(8)), genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(9)));
            String contractValue = resultSet.getString(10);
            String lessonsTaken = String.format("%s/%s/%s", genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(11)),
                    genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(12)), genericHelper.roundToIntegerOrDecimalAsString(resultSet.getDouble(13)));
            double[] lessonsTakenCount = {resultSet.getDouble(11), resultSet.getDouble(12), resultSet.getDouble(13)};
            double[] lessonPriceByType = {resultSet.getDouble(7), resultSet.getDouble(8), resultSet.getDouble(9)};
            String valueOfLessonsTaken = reportHelper.ullCalculateValueOfLessonsTaken(lessonsTakenCount, lessonPriceByType);
            String amountPaid = resultSet.getString(14);
            String prePaid = reportHelper.ullCalculateAmountPrePaid(lessonsTakenCount, lessonPriceByType, resultSet.getDouble(14));
            String valueRemaining = String.format("%.2f", (Double.parseDouble(contractValue) - Double.parseDouble(valueOfLessonsTaken)));

            // Create Data Row
            List<Object> dataRow = new ArrayList();
            dataRow.add(studentName);
            dataRow.add(programID);
            dataRow.add(totalLessonsEnrolled);
            dataRow.add(lessonPrices);
            dataRow.add(contractValue);
            dataRow.add(lessonsTaken);
            dataRow.add(valueOfLessonsTaken);
            dataRow.add(amountPaid);
            dataRow.add(prePaid);
            dataRow.add(valueRemaining);
            reportArrayList.add(dataRow);

        }

        // Create Row Headers
        // PPL: Price per lesson; Behind: Value of Lessons taken but not paid for; Value Remaining: value of remaining untaught lessons;
        String[] reportHeaders = {"Student Name", "Program ID", "# Lessons Enrolled", "PPL", "Contract Value", "Lessons Taken",
            "Value of Lessons Taken", "Amount Paid", "Pre-Paid", "Value Remaining"};

        // Create subheader
        String reportSubHeader = String.format("%s to %s",
                reportDateFormat.format(studentStartDateChooser.getDate()).toString(),
                reportDateFormat.format(studentEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("UntaughtLessonLiability", "Untaught Lesson Liability", reportArrayList, reportHeaders, reportSubHeader, reportHeaders.length);

    }

    private void studioLessonSummary() throws ClassNotFoundException, SQLException, IOException, URISyntaxException, ParseException {

        List<List<Object>> reportArrayList = new ArrayList();
        List<List<String>> dataPrepList = new ArrayList();
        double lessonsEnrolledCountTotal = 0.0;
        double lessonsEnrolledValueTotal = 0.0;
        double lessonsTaughtCountTotal = 0.0;
        double lessonsTaughtValueTotal = 0.0;

        // Get weeks between dates
        LinkedHashMap<Integer, ArrayList<Date>> weekAndDatesMap = reportHelper.slsGetWeeksBetweenDates(studioStartDateChooser.getDate(), studioEndDateChooser.getDate());

        // Iterate over weeks
        for (Map.Entry<Integer, ArrayList<Date>> weekEntry : weekAndDatesMap.entrySet()) {

            double lessonsEnrolledCount = 0.0;
            double lessonsEnrolledValue = 0.0;
            double lessonsTaughtCount = 0.0;
            double lessonsTaughtValue = 0.0;
            int week = weekEntry.getKey();
            ArrayList<Date> weekStartAndEndDates = weekEntry.getValue();

            ResultSet lessonsEnrolledResults = connection.prepareStatement(String.format(
                    "SELECT PrivateLessonTotal,GroupLessonTotal,PartyLessonTotal,PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice"
                    + " FROM ProgramEnrollment WHERE DateCreated BETWEEN DATE '%s' AND '%s';",
                    sqlDateFormat.format(weekStartAndEndDates.get(0)), sqlDateFormat.format(weekStartAndEndDates.get(1)))).executeQuery();

            while (lessonsEnrolledResults.next()) {

                lessonsEnrolledCount += (lessonsEnrolledResults.getDouble(1) + lessonsEnrolledResults.getDouble(2) + lessonsEnrolledResults.getDouble(3));
                lessonsEnrolledValue += (lessonsEnrolledResults.getDouble(4) + lessonsEnrolledResults.getDouble(5) + lessonsEnrolledResults.getDouble(6));

            }

            ResultSet lessonsTaughtResults = connection.prepareStatement(String.format(
                    "SELECT LS.RateType,EN.PrivateLessonPrice,EN.GroupLessonPrice,EN.PartyLessonPrice"
                    + " FROM LessonSchedule as LS INNER JOIN ProgramEnrollment AS EN ON EN.EnrollmentID=LS.EnrollmentID"
                    + " WHERE LS.AppointmentDate BETWEEN DATE '%s' AND '%s' AND LS.LessonStatus='Attended';",
                    sqlDateFormat.format(weekStartAndEndDates.get(0)), sqlDateFormat.format(weekStartAndEndDates.get(1)))).executeQuery();

            while (lessonsTaughtResults.next()) {

                lessonsTaughtCount += 1;
                double lessonsTaughtPrice = reportHelper.slsGetLessonTaughtPrice(lessonsTaughtResults);
                lessonsTaughtValue += lessonsTaughtPrice;
            }

            // Add data to data prep list
            List<String> dataPrepListEntry = new ArrayList();
            dataPrepListEntry.add(String.valueOf(week));
            dataPrepListEntry.add(sqlDateFormat.format(weekStartAndEndDates.get(1)));
            dataPrepListEntry.add(String.format("%s / $%s", genericHelper.roundToIntegerOrDecimalAsString(lessonsEnrolledCount),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonsEnrolledValue)));
            dataPrepListEntry.add(String.format("%s / $%s", genericHelper.roundToIntegerOrDecimalAsString(lessonsTaughtCount),
                    genericHelper.roundToIntegerOrDecimalAsString(lessonsTaughtValue)));
            dataPrepList.add(dataPrepListEntry);

            // Add weekly total to overall total
            lessonsEnrolledCountTotal += lessonsEnrolledCount;
            lessonsEnrolledValueTotal += lessonsEnrolledValue;
            lessonsTaughtCountTotal += lessonsTaughtCount;
            lessonsTaughtValueTotal += lessonsTaughtValue;

        }

        // Add data to report array list
        for (List weekEntry : dataPrepList) {

            // Create Data Row
            List<Object> dataRow = new ArrayList();
            dataRow.add(weekEntry.get(0));
            dataRow.add(weekEntry.get(1));
            dataRow.add(weekEntry.get(2));
            dataRow.add(weekEntry.get(3));
            reportArrayList.add(dataRow);

        }

        // Create Totals Row
        List<Object> dataRow = new ArrayList();
        dataRow.add("Total:");
        dataRow.add("");
        dataRow.add(String.format("%s / $%s", genericHelper.roundToIntegerOrDecimalAsString(lessonsEnrolledCountTotal),
                genericHelper.roundToIntegerOrDecimalAsString(lessonsEnrolledValueTotal)));
        dataRow.add(String.format("%s / $%s", genericHelper.roundToIntegerOrDecimalAsString(lessonsTaughtCountTotal),
                genericHelper.roundToIntegerOrDecimalAsString(lessonsTaughtValueTotal)));
        reportArrayList.add(dataRow);

        // Create Row Headers
        String[] reportHeaders = {"Week", "Date Ending", "Lessons Enrolled # / Value", "Lessons Taught # / Value"};

        // Create subheader
        String reportSubHeader = String.format("%s to %s",
                reportDateFormat.format(studioStartDateChooser.getDate()).toString(),
                reportDateFormat.format(studioEndDateChooser.getDate()).toString());

        // Generate Report
        report.generateNonQueryReportSingleReport("StudioLessonSummary", "Studio Lesson Summary", reportArrayList,
                reportHeaders, reportSubHeader, reportHeaders.length);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formatButtonGroup = new javax.swing.ButtonGroup();
        topLogo = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        back_menu_button = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        studentReportsPanel = new javax.swing.JPanel();
        generateStudentReportBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        studentTable = new javax.swing.JTable();
        studentSearchInput = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        studentActiveFilter = new javax.swing.JRadioButton();
        studentInactiveFilter = new javax.swing.JRadioButton();
        studentAllFilter = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        studentReportTypeList = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        endDateChooserLabel = new javax.swing.JLabel();
        startDateChooserLabel = new javax.swing.JLabel();
        studentStartDateChooser = new com.toedter.calendar.JDateChooser();
        studentEndDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooserLabel1 = new javax.swing.JLabel();
        studentAllDatesCheck = new javax.swing.JCheckBox();
        instructorReportsPanel = new javax.swing.JPanel();
        generateInstructorReportBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        instructorTable = new javax.swing.JTable();
        instructorSearchInput = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        instructorActiveFilter = new javax.swing.JRadioButton();
        instructorInactiveFilter = new javax.swing.JRadioButton();
        instructorAllFilter = new javax.swing.JRadioButton();
        jPanel7 = new javax.swing.JPanel();
        endDateChooserLabel2 = new javax.swing.JLabel();
        startDateChooserLabel1 = new javax.swing.JLabel();
        instructorStartDateChooser = new com.toedter.calendar.JDateChooser();
        instructorEndDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooserLabel3 = new javax.swing.JLabel();
        instructorAllDatesCheck = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        instructorReportTypeList = new javax.swing.JList();
        incomeReportsPanel = new javax.swing.JPanel();
        generateStudioReportBtn = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        endDateChooserLabel4 = new javax.swing.JLabel();
        startDateChooserLabel2 = new javax.swing.JLabel();
        studioStartDateChooser = new com.toedter.calendar.JDateChooser();
        studioEndDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooserLabel5 = new javax.swing.JLabel();
        incomeAllDatesCheck = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        incomeReportTypeList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(784, 528));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 0, 360, 90));

        title.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Reports Menu");
        getContentPane().add(title, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 83, 290, 30));

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        getContentPane().add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        studentReportsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        generateStudentReportBtn.setText("Generate Report");
        generateStudentReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateStudentReportBtnActionPerformed(evt);
            }
        });
        studentReportsPanel.add(generateStudentReportBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 480, 180, 40));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Student"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Last Name", "First Name", "StudentID", "Status", "Address", "City", "State", "Zip", "Email", "HomePhone", "WorkPhone", "Instructor", "Mailing List", "Notes", "BonusPrivate", "BonusGroup"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                studentTableMouseReleased(evt);
            }
        });
        studentTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                studentTableKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(studentTable);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 890, 190));

        studentSearchInput.setToolTipText("Filter Students");
        jPanel1.add(studentSearchInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 160, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel6.setText("Filter:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 40, 30));

        studentActiveFilter.setSelected(true);
        studentActiveFilter.setText("Active");
        jPanel1.add(studentActiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 30, -1, 30));

        studentInactiveFilter.setText("Inactive");
        jPanel1.add(studentInactiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, -1, 30));

        studentAllFilter.setText("All");
        jPanel1.add(studentAllFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, -1, 30));

        studentReportsPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 910, 280));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Report Type"));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentReportTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Student Lesson History", "Student Enrollment History", "Student Birthdays", "Student Payments Due", "Student Payment History", "Student Referrals", "Marketing Breakdown", "Enrollment Program Groups Breakdown" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(studentReportTypeList);

        jPanel5.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 400, 110));

        studentReportsPanel.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 160));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Date Range"));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        endDateChooserLabel.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel.setText("All Dates:");
        jPanel6.add(endDateChooserLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, 30));

        startDateChooserLabel.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        startDateChooserLabel.setText("Start Date:");
        jPanel6.add(startDateChooserLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, 30));
        jPanel6.add(studentStartDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 150, 30));
        jPanel6.add(studentEndDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 150, 30));

        endDateChooserLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel1.setText("End Date:");
        jPanel6.add(endDateChooserLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 30));

        studentAllDatesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentAllDatesCheckActionPerformed(evt);
            }
        });
        jPanel6.add(studentAllDatesCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 93, -1, 30));

        studentReportsPanel.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 400, 160));

        jTabbedPane1.addTab("Student Reports", studentReportsPanel);

        instructorReportsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        generateInstructorReportBtn.setText("Generate Report");
        generateInstructorReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateInstructorReportBtnActionPerformed(evt);
            }
        });
        instructorReportsPanel.add(generateInstructorReportBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 480, 180, 40));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Instructor"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        instructorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Last Name", "First Name", "StudentID", "Status", "Address", "City", "State", "Zip", "Email", "HomePhone", "WorkPhone", "Instructor", "Mailing List", "Notes", "BonusPrivate", "BonusGroup"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        instructorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                instructorTableMouseReleased(evt);
            }
        });
        instructorTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                instructorTableKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(instructorTable);

        jPanel2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 890, 190));

        instructorSearchInput.setToolTipText("Filter Students");
        jPanel2.add(instructorSearchInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 160, 30));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel7.setText("Filter:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 40, 30));

        instructorActiveFilter.setSelected(true);
        instructorActiveFilter.setText("Active");
        jPanel2.add(instructorActiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 30, -1, 30));

        instructorInactiveFilter.setText("Inactive");
        jPanel2.add(instructorInactiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, -1, 30));

        instructorAllFilter.setText("All");
        jPanel2.add(instructorAllFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, -1, 30));

        instructorReportsPanel.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 910, 280));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Date Range"));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        endDateChooserLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel2.setText("All Dates:");
        jPanel7.add(endDateChooserLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, 30));

        startDateChooserLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        startDateChooserLabel1.setText("Start Date:");
        jPanel7.add(startDateChooserLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, 30));
        jPanel7.add(instructorStartDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 150, 30));
        jPanel7.add(instructorEndDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 150, 30));

        endDateChooserLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel3.setText("End Date:");
        jPanel7.add(endDateChooserLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 30));

        instructorAllDatesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instructorAllDatesCheckActionPerformed(evt);
            }
        });
        jPanel7.add(instructorAllDatesCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 93, -1, 30));

        instructorReportsPanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 400, 160));

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Report Type"));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        instructorReportTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Instructor Sale Income", "Instructor Lessons Taught", "Student Progression Percentages - Instructor", "New Student Report/UPS", "Instructor Active Students", "Instructor Inactive Students" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(instructorReportTypeList);

        jPanel8.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 400, 110));

        instructorReportsPanel.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 160));

        jTabbedPane1.addTab("Instructor Reports", instructorReportsPanel);

        incomeReportsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        generateStudioReportBtn.setText("Generate Report");
        generateStudioReportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateStudioReportBtnActionPerformed(evt);
            }
        });
        incomeReportsPanel.add(generateStudioReportBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 220, 180, 40));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Date Range"));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        endDateChooserLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel4.setText("All Dates:");
        jPanel9.add(endDateChooserLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, 30));

        startDateChooserLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        startDateChooserLabel2.setText("Start Date:");
        jPanel9.add(startDateChooserLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, 30));
        jPanel9.add(studioStartDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 150, 30));
        jPanel9.add(studioEndDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 150, 30));

        endDateChooserLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        endDateChooserLabel5.setText("End Date:");
        jPanel9.add(endDateChooserLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 30));

        incomeAllDatesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incomeAllDatesCheckActionPerformed(evt);
            }
        });
        jPanel9.add(incomeAllDatesCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 93, -1, 30));

        incomeReportsPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 400, 160));

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Report Type"));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        incomeReportTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Studio Lesson Summary", "Unpaid Enrollment Income", "Untaught Lesson Liability", "Daily Cash Sheet - Lessons Taught", "Weekly Cash Sheet - Money In", "Student Progression Percentages - Studio", "Studio Report" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane7.setViewportView(incomeReportTypeList);

        jPanel10.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 400, 110));

        incomeReportsPanel.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 160));

        jTabbedPane1.addTab("Studio Reports", incomeReportsPanel);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 950, 590));

        setBounds(0, 0, 973, 744);
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed
        // Open main menu
        MainMenu main_menu = new MainMenu();
        main_menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void studentTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_studentTableMouseReleased

    }//GEN-LAST:event_studentTableMouseReleased

    private void studentTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentTableKeyReleased


    }//GEN-LAST:event_studentTableKeyReleased

    private void generateStudentReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateStudentReportBtnActionPerformed

        // Get selected report
        String selectedReport = (String) studentReportTypeList.getSelectedValue();

        // Disable buttons
        disableButtons();
        // Run method for report
        if (selectedReport == null) {

            // Alert no report type selected
            JOptionPane.showMessageDialog(null, "Please select a Report Type from the List.",
                    "No Report Type Selected", JOptionPane.INFORMATION_MESSAGE);

            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Lesson History")) {

            // Student Lesson History
            Thread studentLessonHistoryReportThread = new Thread() {
                public void run() {
                    try {
                        studentLessonHistoryReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentLessonHistoryReportThread.start();
                studentLessonHistoryReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Enrollment History")) {

            // Student Enrollment History
            Thread studentEnrollmentHistoryReportThread = new Thread() {
                public void run() {
                    try {
                        studentEnrollmentHistoryReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentEnrollmentHistoryReportThread.start();
                studentEnrollmentHistoryReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Birthdays")) {

            // Student Birthdays
            Thread studentBirthdaysReportThread = new Thread() {
                public void run() {
                    try {
                        studentBirthdaysReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentBirthdaysReportThread.start();
                studentBirthdaysReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Payments Due")) {

            // Student Payments Due
            Thread studentPaymentsDueThread = new Thread() {
                public void run() {
                    try {
                        studentPaymentsDue();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentPaymentsDueThread.start();
                studentPaymentsDueThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Payment History")) {

            // Student Payment History
            Thread studentPaymentHistoryReportThread = new Thread() {
                public void run() {
                    try {
                        studentPaymentHistoryReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentPaymentHistoryReportThread.start();
                studentPaymentHistoryReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Referrals")) {

            // Student Referrals
            Thread studentReferralsReportThread = new Thread() {
                public void run() {
                    try {
                        studentReferralsReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentReferralsReportThread.start();
                studentReferralsReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Marketing Breakdown")) {

            // Student Referrals
            Thread marketingBreakdownThread = new Thread() {
                public void run() {
                    try {
                        marketingBreakdownReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                marketingBreakdownThread.start();
                marketingBreakdownThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Enrollment Program Groups Breakdown")) {

            // Student Referrals
            Thread studentEnrollmentProgramGroupsBreakdownReportThread = new Thread() {
                public void run() {
                    try {
                        studentEnrollmentProgramGroupsBreakdownReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentEnrollmentProgramGroupsBreakdownReportThread.start();
                studentEnrollmentProgramGroupsBreakdownReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        }

    }//GEN-LAST:event_generateStudentReportBtnActionPerformed

    private void studentAllDatesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentAllDatesCheckActionPerformed

        // Disable or Enable Date fields
        if (studentAllDatesCheck.isSelected()) {
            studentStartDateChooser.setEnabled(false);
            studentEndDateChooser.setEnabled(false);
        } else {
            studentStartDateChooser.setEnabled(true);
            studentEndDateChooser.setEnabled(true);
        }

    }//GEN-LAST:event_studentAllDatesCheckActionPerformed

    private void generateInstructorReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateInstructorReportBtnActionPerformed

        // Get selected report
        String selectedReport = (String) instructorReportTypeList.getSelectedValue();

        // Disable buttons
        disableButtons();

        // Run method for report
        if (selectedReport == null) {

            // Alert no report type selected
            JOptionPane.showMessageDialog(null, "Please select a Report Type from the List.",
                    "No Report Type Selected", JOptionPane.INFORMATION_MESSAGE);

            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Instructor Sale Income")) {

            // Instructor New Students
            Thread instructorSaleIncomeReportThread = new Thread() {
                public void run() {
                    try {
                        instructorSaleIncomeReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                instructorSaleIncomeReportThread.start();
                instructorSaleIncomeReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Instructor Lessons Taught")) {

            // Instructor New Students
            Thread instructorLessonsTaughtReportThread = new Thread() {
                public void run() {
                    try {
                        instructorLessonsTaughtReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                instructorLessonsTaughtReportThread.start();
                instructorLessonsTaughtReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Progression Percentages - Instructor")) {

            // Student Referrals
            Thread studentProgressionPercentagesInstructorThread = new Thread() {
                public void run() {
                    try {
                        studentProgressionPercentagesInstructorReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentProgressionPercentagesInstructorThread.start();
                studentProgressionPercentagesInstructorThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("New Student Report/UPS")) {

            // Student Referrals
            Thread newStudentUPSReportThread = new Thread() {
                public void run() {
                    try {
                        newStudentReportUPSReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                newStudentUPSReportThread.start();
                newStudentUPSReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Instructor Active Students")) {

            // Student Referrals
            Thread instructorActiveStudentsReportThread = new Thread() {
                public void run() {
                    try {
                        instructorActiveStudentsReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                instructorActiveStudentsReportThread.start();
                instructorActiveStudentsReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Instructor Inactive Students")) {

            // Student Referrals
            Thread instructorInactiveStudentsReportThread = new Thread() {
                public void run() {
                    try {
                        instructorInactiveStudentsReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                instructorInactiveStudentsReportThread.start();
                instructorInactiveStudentsReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        }

    }//GEN-LAST:event_generateInstructorReportBtnActionPerformed

    private void instructorTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_instructorTableMouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_instructorTableMouseReleased

    private void instructorTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_instructorTableKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_instructorTableKeyReleased

    private void instructorAllDatesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instructorAllDatesCheckActionPerformed

        // Disable or Enable Date fields
        if (instructorAllDatesCheck.isSelected()) {
            instructorStartDateChooser.setEnabled(false);
            instructorEndDateChooser.setEnabled(false);
        } else {
            instructorStartDateChooser.setEnabled(true);
            instructorEndDateChooser.setEnabled(true);
        }

    }//GEN-LAST:event_instructorAllDatesCheckActionPerformed

    private void generateStudioReportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateStudioReportBtnActionPerformed

        // Get selected report
        String selectedReport = (String) incomeReportTypeList.getSelectedValue();

        // Disable buttons
        disableButtons();

        // Run method for report
        if (selectedReport == null) {

            // Alert no report type selected
            JOptionPane.showMessageDialog(null, "Please select a Report Type from the List.",
                    "No Report Type Selected", JOptionPane.INFORMATION_MESSAGE);

            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Gross Income")) {

            // Gross Income
            Thread grossIncomeReportThread = new Thread() {
                public void run() {
                    try {
                        grossIncomeReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                grossIncomeReportThread.start();
                grossIncomeReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Student Progression Percentages - Studio")) {

            // Student Referrals
            Thread studentProgressionPercentagesStudioThread = new Thread() {
                public void run() {
                    try {
                        studentProgressionPercentagesStudioReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studentProgressionPercentagesStudioThread.start();
                studentProgressionPercentagesStudioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Untaught Lesson Liability")) {

            // Unpaid Enrollment Income
            Thread untaughtLessonLiabilityThread = new Thread() {
                public void run() {
                    try {
                        untaughtLessonLiabilityReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                untaughtLessonLiabilityThread.start();
                untaughtLessonLiabilityThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Unpaid Enrollment Income")) {

            // Unpaid Enrollment Income
            Thread unpaidEnrollmentIncomeThread = new Thread() {
                public void run() {
                    try {
                        unpaidEnrollmentIncome();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                unpaidEnrollmentIncomeThread.start();
                unpaidEnrollmentIncomeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Daily Cash Sheet - Lessons Taught")) {

            // Unpaid Enrollment Income
            Thread weeklyCashTaughtInLessonsThread = new Thread() {
                public void run() {
                    try {
                        dailyCashSheetLessonsTaughtReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                weeklyCashTaughtInLessonsThread.start();
                weeklyCashTaughtInLessonsThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Weekly Cash Sheet - Money In")) {

            // Unpaid Enrollment Income
            Thread dailyCashSheetReportThread = new Thread() {
                public void run() {
                    try {
                        weeklyCashSheetMoneyInReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                dailyCashSheetReportThread.start();
                dailyCashSheetReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Studio Lesson Summary")) {

            // Unpaid Enrollment Income
            Thread studioLessonSummaryThread = new Thread() {
                public void run() {
                    try {
                        studioLessonSummary();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                studioLessonSummaryThread.start();
                studioLessonSummaryThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();

        } else if (selectedReport.equals("Studio Report")) {

            // Unpaid Enrollment Income
            Thread regionalReportThread = new Thread() {
                public void run() {
                    // Create arraylist of instructors and apply to combobox
                    try {
                        studioAndTeacherRegionalReport();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                regionalReportThread.start();
                regionalReportThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Enable buttons
            enableButtons();
        }

    }//GEN-LAST:event_generateStudioReportBtnActionPerformed

    private void incomeAllDatesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incomeAllDatesCheckActionPerformed

        // Disable or Enable Date fields
        if (incomeAllDatesCheck.isSelected()) {
            studioStartDateChooser.setEnabled(false);
            studioEndDateChooser.setEnabled(false);
        } else {
            studioStartDateChooser.setEnabled(true);
            studioEndDateChooser.setEnabled(true);
        }
    }//GEN-LAST:event_incomeAllDatesCheckActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ReportsManager.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ReportsManager.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ReportsManager.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ReportsManager.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReportsManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back_menu_button;
    private javax.swing.JLabel endDateChooserLabel;
    private javax.swing.JLabel endDateChooserLabel1;
    private javax.swing.JLabel endDateChooserLabel2;
    private javax.swing.JLabel endDateChooserLabel3;
    private javax.swing.JLabel endDateChooserLabel4;
    private javax.swing.JLabel endDateChooserLabel5;
    private javax.swing.ButtonGroup formatButtonGroup;
    private javax.swing.JButton generateInstructorReportBtn;
    private javax.swing.JButton generateStudentReportBtn;
    private javax.swing.JButton generateStudioReportBtn;
    private javax.swing.JCheckBox incomeAllDatesCheck;
    private javax.swing.JList incomeReportTypeList;
    private javax.swing.JPanel incomeReportsPanel;
    private javax.swing.JRadioButton instructorActiveFilter;
    private javax.swing.JCheckBox instructorAllDatesCheck;
    private javax.swing.JRadioButton instructorAllFilter;
    private com.toedter.calendar.JDateChooser instructorEndDateChooser;
    private javax.swing.JRadioButton instructorInactiveFilter;
    private javax.swing.JList instructorReportTypeList;
    private javax.swing.JPanel instructorReportsPanel;
    private javax.swing.JTextField instructorSearchInput;
    private com.toedter.calendar.JDateChooser instructorStartDateChooser;
    private javax.swing.JTable instructorTable;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel startDateChooserLabel;
    private javax.swing.JLabel startDateChooserLabel1;
    private javax.swing.JLabel startDateChooserLabel2;
    private javax.swing.JRadioButton studentActiveFilter;
    private javax.swing.JCheckBox studentAllDatesCheck;
    private javax.swing.JRadioButton studentAllFilter;
    private com.toedter.calendar.JDateChooser studentEndDateChooser;
    private javax.swing.JRadioButton studentInactiveFilter;
    private javax.swing.JList studentReportTypeList;
    private javax.swing.JPanel studentReportsPanel;
    private javax.swing.JTextField studentSearchInput;
    private com.toedter.calendar.JDateChooser studentStartDateChooser;
    private javax.swing.JTable studentTable;
    private com.toedter.calendar.JDateChooser studioEndDateChooser;
    private com.toedter.calendar.JDateChooser studioStartDateChooser;
    private javax.swing.JLabel title;
    private javax.swing.JLabel topLogo;
    // End of variables declaration//GEN-END:variables
}
