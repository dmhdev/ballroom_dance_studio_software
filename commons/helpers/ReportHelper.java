/*
 * Several common methods employed by different views and classes. Mostly used to populate JTables and JComboBoxes with data.
 */
package commons.helpers;

import static commons.helpers.ServerHelper.connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author daynehammes
 */
public class ReportHelper {

    ServerHelper server = new ServerHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /*
    Untaught Lessons Liability Report
     */
    public String ullCalculateValueOfLessonsTaken(double[] lessonsTakenCount, double[] lessonPriceByType) {

        double valueOfPrivateLessons = lessonsTakenCount[0] * lessonPriceByType[0];
        double valueOfGroupLessons = lessonsTakenCount[1] * lessonPriceByType[1];
        double valueOfPartyLessons = lessonsTakenCount[2] * lessonPriceByType[2];

        return String.format("%.2f", (valueOfPrivateLessons + valueOfGroupLessons + valueOfPartyLessons));

    }

    public String ullCalculateAmountPrePaid(double[] lessonsTakenCount, double[] lessonPriceByType, double amountPaid) {

        double valueOfPrivateLessons = lessonsTakenCount[0] * lessonPriceByType[0];
        double valueOfGroupLessons = lessonsTakenCount[1] * lessonPriceByType[1];
        double valueOfPartyLessons = lessonsTakenCount[2] * lessonPriceByType[2];
        double valueOfAllLessons = valueOfPrivateLessons + valueOfGroupLessons + valueOfPartyLessons;

        return String.format("%.2f", (amountPaid - valueOfAllLessons));

    }

    /*
    Student Progression Percentages - Studio Report
     */
    public String[] sppxGetYearsInReport(Date selectedDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String selectedYearBeginning = String.valueOf(sqlDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        String selectedYearEnd = String.valueOf(sqlDateFormat.format(calendar.getTime()));

        calendar.add(Calendar.YEAR, -1);
        String previousYearEnd = String.valueOf(sqlDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String previousYearBeginning = String.valueOf(sqlDateFormat.format(calendar.getTime()));

        String[] yearsInReport = {previousYearBeginning, previousYearEnd, selectedYearBeginning, selectedYearEnd};

        return yearsInReport;

    }

    public Double sppxCalculatePercentIncreaseOrDecrease(Double currentYear, Double previousYear) {

        if (currentYear > previousYear) {

            if (previousYear == 0.0) {
                return (currentYear);
            } else {

                double increase = currentYear - previousYear;
                return ((increase / previousYear) * 100);
            }

        } else {

            if (previousYear == 0.0) {
                return (currentYear * -1);
            } else {

                double decrease = previousYear - currentYear;
                return ((decrease / previousYear) * -100);
            }

        }

    }

    // Adds one to count of all starting from program group
    public void sppxIncrementProgramGroupTriesCount(String programGroup, Map<String, Object> programGroupProgressionCounts) {

        List<Double> currentValues = new ArrayList();

        // Check for correct starting group
        if (programGroup.equals("Original")) {

            // Increment all starting values for original
            currentValues = (ArrayList) programGroupProgressionCounts.get("origToPre");
            List<Double> updatedValues = new ArrayList();
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("origToPre", updatedValues);
            currentValues = (ArrayList) programGroupProgressionCounts.get("origToExt");
            List<Double> updatedValues2 = new ArrayList();
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("origToExt", updatedValues2);
            currentValues = (ArrayList) programGroupProgressionCounts.get("origToReext");
            List<Double> updatedValues3 = new ArrayList();
            updatedValues3.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues3.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("origToReext", updatedValues3);
            currentValues = (ArrayList) programGroupProgressionCounts.get("origToRen");
            List<Double> updatedValues4 = new ArrayList();
            updatedValues4.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues4.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("origToRen", updatedValues4);

        } else if (programGroup.equals("Preliminary")) {

            currentValues = (ArrayList) programGroupProgressionCounts.get("preToExt");
            List<Double> updatedValues = new ArrayList();
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("preToExt", updatedValues);
            currentValues = (ArrayList) programGroupProgressionCounts.get("preToReext");
            List<Double> updatedValues2 = new ArrayList();
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("preToReext", updatedValues2);
            currentValues = (ArrayList) programGroupProgressionCounts.get("preToRen");
            List<Double> updatedValues3 = new ArrayList();
            updatedValues3.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues3.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("preToRen", updatedValues3);

        } else if (programGroup.equals("Extension")) {

            currentValues = (ArrayList) programGroupProgressionCounts.get("extToReext");
            List<Double> updatedValues = new ArrayList();
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("extToReext", updatedValues);
            currentValues = (ArrayList) programGroupProgressionCounts.get("extToRen");
            List<Double> updatedValues2 = new ArrayList();
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues2.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("extToRen", updatedValues2);

        } else if (programGroup.equals("ReExtension")) {

            currentValues = (ArrayList) programGroupProgressionCounts.get("reextToRen");
            List<Double> updatedValues = new ArrayList();
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0) + 1));
            updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1)));
            programGroupProgressionCounts.replace("reextToRen", updatedValues);

        }

    }

    // Adds one to count of all starting from program group
    public void sppxIncrementProgramGroupEndCount(String startProgramGroup, String endProgramGroup, Map<String, Object> programGroupProgressionCounts) {

        List<Double> currentValues = new ArrayList();

        // Check for correct starting group
        if (startProgramGroup.equals("Original")) {

            // Check for correct end group
            if (endProgramGroup.equals("Preliminary")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("origToPre");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("origToPre", updatedValues);
            } else if (endProgramGroup.equals("Extension")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("origToExt");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("origToExt", updatedValues);
            } else if (endProgramGroup.equals("ReExtension")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("origToReext");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("origToReext", updatedValues);
            } else if (endProgramGroup.equals("Renewal")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("origToRen");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("origToRen", updatedValues);
            }

        } else if (startProgramGroup.equals("Preliminary")) {

            if (endProgramGroup.equals("Extension")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("preToExt");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("preToExt", updatedValues);
            } else if (endProgramGroup.equals("ReExtension")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("preToReext");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("preToReext", updatedValues);
            } else if (endProgramGroup.equals("Renewal")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("preToRen");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("preToRen", updatedValues);
            }

        } else if (startProgramGroup.equals("Extension")) {

            if (endProgramGroup.equals("ReExtension")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("extToReext");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("extToReext", updatedValues);
            } else if (endProgramGroup.equals("Renewal")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("extToRen");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("extToRen", updatedValues);
            }

        } else if (startProgramGroup.equals("ReExtension")) {
            if (endProgramGroup.equals("Renewal")) {
                currentValues = (ArrayList) programGroupProgressionCounts.get("reextToRen");
                List<Double> updatedValues = new ArrayList();
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(0)));
                updatedValues.add(genericHelper.roundDecimalToTwoDecimalPlaces(currentValues.get(1) + 1));
                programGroupProgressionCounts.replace("reextToRen", updatedValues);
            }
        }
    }

    /*
    Studio Lesson Summary Report
     */
    public LinkedHashMap slsGetWeeksBetweenDates(Date startDate, Date endDate) {

        LinkedHashMap<Integer, ArrayList<Date>> weekAndDatesMap = new LinkedHashMap<>();

        if (endDate.before(startDate)) {
            return slsGetWeeksBetweenDates(endDate, startDate);
        }
        startDate = slsResetTime(startDate);
        endDate = slsResetTime(endDate);

        Calendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        int weekNumber = 1;
        while (cal.getTime().before(endDate)) {

            // Get week start date
            Date weekStartDate = cal.getTime();
            // Set day to Saturday
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            // Get week end date
            Date weekEndDate = cal.getTime();
            // Add 1 to day of week
            cal.add(Calendar.DAY_OF_WEEK, 1);

            // Test if weekEndDate falls after last selected date
            if (cal.getTime().after(endDate)) {
                weekEndDate = endDate;
            }

            // Add variables to hashmap
            ArrayList<Date> weekDates = new ArrayList<Date>();
            weekDates.add(weekStartDate);
            weekDates.add(weekEndDate);
            weekAndDatesMap.put(weekNumber, weekDates);

            // add another week
            weekNumber++;
        }

        return weekAndDatesMap;

    }

    public Date slsResetTime(Date d) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public double slsGetLessonTaughtPrice(ResultSet lessonsTaughtResults) throws SQLException {

        String lessonType = lessonsTaughtResults.getString(1);
        if (lessonType.equals("Private")) {
            return lessonsTaughtResults.getDouble(2);
        } else if (lessonType.equals("Group")) {
            return lessonsTaughtResults.getDouble(3);
        } else {
            return lessonsTaughtResults.getDouble(4);
        }
    }

    /*
    New Student Report/UPS
     */
    public String[] nsupsGetStartingLessonsForStudent(String studentID) throws SQLException {

        String firstPrivateLessonDate = "-", secondPrivateLessonDate = "-", firstGroupPartyLessonDate = "-";

        // Get dates of first 2 private lessons for student
        ResultSet privateLessonsResultset = connection.prepareStatement(String.format("SELECT AppointmentDate FROM LessonSchedule"
                + " WHERE StudentID='%s' AND RateType='Private' ORDER BY AppointmentDate ASC;", studentID)).executeQuery();

        int i = 0;
        while (privateLessonsResultset.next()) {

            if (i == 0) {
                firstPrivateLessonDate = privateLessonsResultset.getString(1);
            } else if (i == 1) {
                secondPrivateLessonDate = privateLessonsResultset.getString(1);
            } else {
                break;
            }
            i++;
        }

        // Get date of first group or party lesson for student
        ResultSet groupPartyLessonResultset = connection.prepareStatement(String.format("SELECT AppointmentDate FROM LessonSchedule"
                + " WHERE StudentID='%s' AND RateType!='Private' ORDER BY AppointmentDate ASC;", studentID)).executeQuery();

        while (groupPartyLessonResultset.next()) {
            firstGroupPartyLessonDate = groupPartyLessonResultset.getString(1);
            break;
        }

        String[] startingLessonDates = {firstPrivateLessonDate, secondPrivateLessonDate, firstGroupPartyLessonDate};
        return startingLessonDates;
    }

    public String nsupsGetStudentSecondEnrollment(String studentID) throws SQLException {

        String secondEnrollmentData = "-";

        ResultSet studentEnrollmentResultSet = connection.prepareStatement(String.format("SELECT PR.ProgramID,EN.ContractTotal,"
                + "EN.DateCreated FROM ProgramEnrollment AS EN INNER JOIN Programs AS PR ON PR.ProgramID=EN.ProgramID"
                + " WHERE EN.StudentID='%s' ORDER BY EN.DateCreated ASC;", studentID)).executeQuery();

        int i = 0;
        while (studentEnrollmentResultSet.next()) {
            if (i == 1) {
                secondEnrollmentData = String.format("%s/$%s/%s", studentEnrollmentResultSet.getString(1),
                        studentEnrollmentResultSet.getString(2), studentEnrollmentResultSet.getString(3));
                break;
            }
            i++;
        }

        return secondEnrollmentData;
    }

    public String nsupsGetReferrerBonuses(String studentID) throws SQLException {

        String referrerBonuses = "-";
        ResultSet referrerBonusesResultset = connection.prepareStatement(String.format("SELECT UnitsUsed FROM BonusTransaction"
                + " WHERE ReferredStudentID='%s';", studentID)).executeQuery();

        while (referrerBonusesResultset.next()) {
            referrerBonuses = referrerBonusesResultset.getString(1);
        }
        return referrerBonuses;
    }

    /*
    Instructor Active Students
     */
    public List<List<Object>> iasGetStudentsWithTwoEnrollments(String instructorID) throws SQLException {

        List<String> studentIDsWithTwoEnrollments = new ArrayList<>();
        List<List<Object>> studentsWithTwoEnrollments = new ArrayList<>();

        // Get all Students for Instructor with at least 2 Enrollments
        ResultSet instructorStudentsResultset = connection.prepareStatement(String.format("SELECT ST.StudentID FROM Students AS ST"
                + " INNER JOIN ProgramEnrollment AS EN ON EN.StudentID=ST.StudentID"
                + " WHERE EN.PrimaryInstructorID='%s' AND ST.Active=TRUE ORDER BY ST.StudentID ASC;", instructorID)).executeQuery();

        String lastStudentID = "", secondLastStudentID = "";
        while (instructorStudentsResultset.next()) {

            String currentStudentID = instructorStudentsResultset.getString(1);
            if ((lastStudentID.equals(currentStudentID)) && (!secondLastStudentID.equals(currentStudentID))) {
                studentIDsWithTwoEnrollments.add(currentStudentID);

            }
            secondLastStudentID = lastStudentID;
            lastStudentID = currentStudentID;
        }

        // Get Data for each Student
        for (String currentStudentID : studentIDsWithTwoEnrollments) {

            ResultSet studentDataResultset = connection.prepareStatement(String.format("SELECT LS.AppointmentDate,ST.FName,ST.LName,"
                    + "ST.Address,ST.City,ST.State,ST.ZipCode,ST.Phone FROM Students AS ST"
                    + " INNER JOIN LessonSchedule AS LS ON LS.StudentID=ST.StudentID"
                    + " WHERE ST.StudentID='%s' ORDER BY LS.AppointmentDate ASC;", currentStudentID)).executeQuery();

            int i = 0;
            String firstlessonDate = "-", studentName = "-", address = "-", phone = "-", lastLessonDate = "-";
            while (studentDataResultset.next()) {

                if (i == 0) {
                    firstlessonDate = studentDataResultset.getString(1);
                    studentName = String.format("%s, %s", studentDataResultset.getString(3), studentDataResultset.getString(2));
                    address = String.format("%s %s %s %s", studentDataResultset.getString(4), studentDataResultset.getString(5),
                            studentDataResultset.getString(6),studentDataResultset.getString(7));
                    phone = studentDataResultset.getString(8);
                }
                lastLessonDate = studentDataResultset.getString(1);
                i++;
            }

            // Add Data to List
            List<Object> singleStudentData = new ArrayList<>();
            singleStudentData.add(firstlessonDate);
            singleStudentData.add(studentName);
            singleStudentData.add(address);
            singleStudentData.add(phone);
            singleStudentData.add(lastLessonDate);
            studentsWithTwoEnrollments.add(singleStudentData);

        }

        return studentsWithTwoEnrollments;
    }
    
    /*
    Instructor Inactive Students
     */
    public List<List<Object>> iisGetInactiveStudents(String instructorID) throws SQLException {

        List<String> inactiveStudentIDs = new ArrayList<>();
        List<List<Object>> inactiveStudents = new ArrayList<>();

        // Get all Students for Instructor that are inactive
        ResultSet instructorStudentsResultset = connection.prepareStatement(String.format("SELECT ST.StudentID FROM Students AS ST"
                + " WHERE ST.InstructorID='%s' AND ST.Active=FALSE ORDER BY ST.StudentID ASC;", instructorID)).executeQuery();

        while (instructorStudentsResultset.next()) {
            inactiveStudentIDs.add(instructorStudentsResultset.getString(1));
        }

        // Get Data for each Student
        for (String currentStudentID : inactiveStudentIDs) {

            ResultSet studentDataResultset = connection.prepareStatement(String.format("SELECT LS.AppointmentDate,ST.FName,ST.LName,"
                    + "ST.Address,ST.Phone FROM Students AS ST"
                    + " INNER JOIN LessonSchedule AS LS ON LS.StudentID=ST.StudentID"
                    + " WHERE ST.StudentID='%s' ORDER BY LS.AppointmentDate ASC;", currentStudentID)).executeQuery();

            int i = 0;
            String firstlessonDate = "-", studentName = "-", address = "-", phone = "-", lastLessonDate = "-";
            while (studentDataResultset.next()) {

                if (i == 0) {
                    firstlessonDate = studentDataResultset.getString(1);
                    studentName = String.format("%s, %s", studentDataResultset.getString(3), studentDataResultset.getString(2));
                    address = String.format("%s %s %s %s", studentDataResultset.getString(4), studentDataResultset.getString(5),
                            studentDataResultset.getString(6),studentDataResultset.getString(7));
                    phone = studentDataResultset.getString(8);
                }
                lastLessonDate = studentDataResultset.getString(1);
                i++;
            }

            // Add Data to List
            List<Object> singleStudentData = new ArrayList<>();
            singleStudentData.add(firstlessonDate);
            singleStudentData.add(studentName);
            singleStudentData.add(address);
            singleStudentData.add(phone);
            singleStudentData.add(lastLessonDate);
            inactiveStudents.add(singleStudentData);

        }

        return inactiveStudents;
    }

}
