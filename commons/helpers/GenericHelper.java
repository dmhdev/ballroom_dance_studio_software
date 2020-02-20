/*
 * Several common methods employed by different views and classes. Mostly used to populate JTables and JComboBoxes with data.
 */
package commons.helpers;

import com.toedter.calendar.JDateChooser;
import commons.entities.Enrollment;
import static commons.helpers.ServerHelper.connection;
import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author daynehammes
 */
public class GenericHelper {

    public static int studentRow = 0;
    public static int studentFilter = 0;        // 0 = Active, 1 = Inactive, 2 = All
    ServerHelper server = new ServerHelper();

    // Set Student Name Title
    public void setStudentName(JLabel studentNameField, String studentID) throws SQLException, ClassNotFoundException {

        // Get Student Details from Database
        try {
            ResultSet studentSet = connection.prepareStatement("select FName,LName from Students WHERE StudentID='" + studentID + "';").executeQuery();
            studentSet.next();

            // Set Student Values
            String lastName = studentSet.getString(2);
            String firstName = studentSet.getString(1);
            studentNameField.setText(lastName + ", " + firstName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    // Set current year as default transaction period
    public void setInitialDates(JDateChooser startDateChooser, JDateChooser endDateChooser) {

        // Set second date as today
        Date currentDate = new Date();
        endDateChooser.setDate(currentDate);

        // Get beginning of year date
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();

        // Set beginning date as 01/01/Current Year
        startDateChooser.setDate(startDate);

    }

    // Enable or Disable all buttons in JFrame
    public void toggleAllButtonsEnabled(JFrame mainFrame, Boolean shouldEnable) {

        // Get list of all components
        List<Component> allComponents = getAllComponents(mainFrame);

        // Iterate and enable/disable all buttons
        for (int i = 0; i < allComponents.size(); i++) {
            if (allComponents.get(i) instanceof JButton) {
                JButton currentBtn = (JButton) allComponents.get(i);

                // Enable or disable
                if (shouldEnable) {
                    currentBtn.setEnabled(true);
                } else {
                    currentBtn.setEnabled(false);
                }

            }

        }

    }

    // Return list of all components in JFrame
    public List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    // Check if a string is numeric
    public boolean isNumericString(String input) {
        try {
            double d = Double.parseDouble(input);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Remove html markings from enrollment text
    public String removeRedHTML(String input) {

        return input.replace("<html><span style='color:red;'>", "").replace("</span></html>", "");
    }

    // Check if a string is numeric
    public static boolean isNumericDouble(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Formats time input from HH:MM:SS or H:MM:SS to HH:MM
    public String formatTimeToHHMM(String inputTime) {

        // Cut off seconds from time string
        int lastColon = inputTime.lastIndexOf(":");
        inputTime = inputTime.substring(0, lastColon);

        // Check if single digit in hour position
        if (inputTime.length() < 5) {
            return "0" + inputTime;
        } else {
            return inputTime;
        }

    }

    // Check if valid date
    public boolean isValidDateFormat(String date) {

        String DATE_FORMAT = "yyyy-MM-dd";
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Takes in a string and returns either the string or "N/A"
    public String notAvailableFactory(String input) {

        if (input.length() > 0) {
            return input;
        } else {
            return "N/A";
        }
    }

    // Get ResultSet from Database
    public ResultSet getResultSet(String query) throws SQLException, ClassNotFoundException {

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
    public void executeSQL(String updateSQL) throws SQLException, ClassNotFoundException {

        try {

            // Perform update operation
            connection.prepareStatement(updateSQL).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Get id of last inserted sql object in table
    public String getLastInsertedID(String tableName, String columnName) throws SQLException, ClassNotFoundException {

        ResultSet insertedSet = getResultSet(String.format("SELECT TOP 1 %s FROM %s ORDER BY DateUpdated DESC;", columnName, tableName));

        if (insertedSet.next()) {
            return insertedSet.getString(1);
        }
        return null;

    }

    public void updateReferrerPendingBonuses(double referrerBonusAmount, String referrerStudentID, String referredStudentID) throws SQLException {

        // Get old bonus amounts
        ResultSet referrerBonusData = connection.prepareStatement(
                "select InstructorID,InstructorID2,InstructorID3 from Students where StudentID='" + referrerStudentID + "';").executeQuery();
        
        if (referrerBonusData.next()) {

            // Get number of instructors for student (for reporting later)
            String referringInstructorID = referrerBonusData.getString(1);
            String referringInstructorID2 = referrerBonusData.getString(2);
            String referringInstructorID3 = referrerBonusData.getString(3);

            // Record bonus transaction
            connection.prepareStatement(String.format("Insert into BonusTransaction (StudentID,TransactionType,BonusType,LessonType,UnitsUsed,ReferredStudentID,ReferringStudentCurrentInstructorID,"
                    + "ReferringStudentCurrentInstructorID2,ReferringStudentCurrentInstructorID3)"
                    + " Values('%s','StudentReferred','Pending','Private',%f,'%s','%s','%s','%s');",
                    referrerStudentID, referrerBonusAmount, referredStudentID, referringInstructorID, referringInstructorID2, referringInstructorID3)).execute();

        }

    }

    public void deletePaymentTransaction(String paymentID, String enrollmentID, double paymentTotal) throws SQLException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really delete payment transaction?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Update program enrollment payment
            Enrollment enrollment = new Enrollment(enrollmentID);
            enrollment.updateProgramEnrollment("", 0, paymentTotal);

            // Delete payment
            connection.prepareStatement("DELETE FROM PUBLIC.PaymentTransaction WHERE PaymentID='" + paymentID + "';").execute();

            // Confirm success
            JOptionPane.showMessageDialog(null, "Successfully deleted Payment Transaction with Transaction ID: " + paymentID + ".",
                    "Deleted Payment Transaction", JOptionPane.INFORMATION_MESSAGE);

        }

    }

    public void deleteBonusTransaction(String transactionID) throws SQLException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really delete bonus transaction?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Get Student Details from Database
            connection.prepareStatement("DELETE FROM PUBLIC.BonusTransaction WHERE TransactionID='" + transactionID + "';").execute();

            // Confirm success
            JOptionPane.showMessageDialog(null, "Successfully deleted Bonus Transaction with Transaction ID: " + transactionID + ".",
                    "Deleted Bonus Transaction", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    public void redeemPendingBonus(String transactionID, String transactionType) throws SQLException {

        // Confirmation dialog
        int confirmRedemption = JOptionPane.showConfirmDialog(null, "Really redeem referral bonus?",
                "Confirm Redeem Bonus", JOptionPane.YES_NO_OPTION);
        if (confirmRedemption == JOptionPane.YES_OPTION) {

            // Get data from bonus transaction; If it was a Manual Update, there will be no referred student id
            ResultSet pendingBonusData;
            if (transactionType.equals("ManualUpdate")) {

                pendingBonusData = connection.prepareStatement(String.format("SELECT StudentID,LessonType,UnitsUsed FROM BonusTransaction WHERE TransactionID='%s';",
                        transactionID)).executeQuery();
            } else {

                pendingBonusData = connection.prepareStatement(String.format("SELECT BT.StudentID,BT.LessonType,BT.UnitsUsed,BT.ReferredStudentID,ST.InstructorID,ST.InstructorID2,"
                        + "ST.InstructorID3 FROM BonusTransaction as BT INNER JOIN Students as ST ON BT.ReferredStudentID=ST.StudentID WHERE BT.TransactionID='%s';",
                        transactionID)).executeQuery();
            }

            if (pendingBonusData.next()) {

                // Get current date
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String redeemedOnDate = dateFormat.format(date);

                // Get data
                String studentID = pendingBonusData.getString(1);
                String lessonType = pendingBonusData.getString(2);
                double unitsUsed = pendingBonusData.getDouble(3);

                // Get referred student data if transaction type is a referral
                if (transactionType.equals("StudentReferred")) {

                    String referredStudentID = pendingBonusData.getString(4);
                    String referringStudentInstructorID = pendingBonusData.getString(5);
                    String referringStudentInstructorID2 = pendingBonusData.getString(6);
                    String referringStudentInstructorID3 = pendingBonusData.getString(7);

                    // Record redemption of bonus as new transaction for Student Referred
                    connection.prepareStatement(String.format("Insert into BonusTransaction (StudentID,TransactionType,BonusType,LessonType,UnitsUsed,RedeemedOn,"
                            + "ReferredStudentID,ReferringStudentInstructorID,ReferringStudentInstructorID2,ReferringStudentInstructorID3)"
                            + " Values('%s','RedeemedPending','Real','%s',%f,'%s','%s','%s','%s','%s');",
                            studentID, lessonType, unitsUsed, redeemedOnDate, referredStudentID, referringStudentInstructorID, referringStudentInstructorID2, referringStudentInstructorID3)).execute();
                } else {

                    // Record redemption of bonus as new transaction for Manual Update
                    connection.prepareStatement(String.format("Insert into BonusTransaction (StudentID,TransactionType,BonusType,LessonType,UnitsUsed,RedeemedOn)"
                            + " Values('%s','RedeemedPending','Real','%s',%f,'%s');",
                            studentID, lessonType, unitsUsed, redeemedOnDate)).execute();

                }

                // Update original transaction with date bonus was redeemed on
                connection.prepareStatement(String.format(
                        "UPDATE BonusTransaction SET RedeemedOn='%s' WHERE TransactionID='%s';", redeemedOnDate, transactionID))
                        .execute();

                // Confirm success
                JOptionPane.showMessageDialog(null, "Successfully redeemed referral bonus.",
                        "Redeemed Referral Bonus", JOptionPane.INFORMATION_MESSAGE);

            } else {

                // Show error: unable to find original bonus transaction
                JOptionPane.showMessageDialog(null, "Original bonus could not be found, cannot redeem bonus.",
                        "Redeem Bonus Error", JOptionPane.ERROR_MESSAGE);

            }

        }

    }

    public void createPaymentTransaction(String studentID, String paymentType, double paymentTotal,
            double lessonUnits, String enrollmentID, String lessonType, String paymentDate) throws SQLException {

        // Check if this is the first payment on enrollment
        String query = String.format("SELECT ContractPaid FROM ProgramEnrollment WHERE EnrollmentID='%s' ;", enrollmentID);
        ResultSet resultSet = connection.prepareStatement(query).executeQuery();

        boolean isFirstPayment = false;
        if (resultSet.next()) {
            double contractPaid = resultSet.getDouble(1);
            if (contractPaid == paymentTotal) {
                isFirstPayment = true;
            }
        }

        // Add Payment Record
        connection.prepareStatement(String.format("insert into PaymentTransaction(StudentID,PaymentType,Amount,LessonUnits,EnrollmentID,LessonType,FirstPayment,DateCreated,DateUpdated) Values "
                + "('%s','%s',%f,%f,'%s','%s',%b,'%s','%s');", studentID, paymentType, paymentTotal, lessonUnits, enrollmentID, lessonType, isFirstPayment, paymentDate, paymentDate))
                .execute();

    }

    public void createBonusTransactionFromEnrollment(String studentID, String lessonType, double unitsChanged, String enrollmentID, String enrollmentDate) throws SQLException {

        // Record bonus transaction
        connection.prepareStatement(String.format("Insert into BonusTransaction (StudentID,TransactionType,BonusType,LessonType,UnitsUsed,FromEnrollmentID,DateCreated,DateUpdated)"
                + " Values('%s','ProgramEnrolled','Real','%s',%f,'%s','%s','%s');",
                studentID, lessonType, unitsChanged, enrollmentID, enrollmentDate, enrollmentDate)).execute();

    }

    public void createBonusTransactionFromAttendance(String studentID, String lessonID, String lessonType, double unitsChanged, String bonusDate, String enrollmentID)
            throws SQLException {

        // Record bonus transaction
        connection.prepareStatement(String.format("Insert into BonusTransaction (StudentID,TransactionType,BonusType,LessonType,UnitsUsed,DateCreated,UsedOnEnrollmentID)"
                + " Values('%s','LessonAttended','Real','%s',%f,'%s','%s');",
                studentID, lessonType, (unitsChanged * -1), bonusDate, enrollmentID)).execute();

    }

    public boolean stringNotNull(String input) {

        boolean stringNotNull = false;

        if ((input != null) && !(input.equals("None")) && !(input.equals("null"))) {

            String trimmedInput = input.trim();

            if (!(trimmedInput.equals("")) && trimmedInput.length() > 0) {
                stringNotNull = true;
            }

        }
        return stringNotNull;
    }

    public double roundDecimalToTwoDecimalPlaces(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String roundToIntegerOrDecimalAsString(double value) {

        int intValue = 0;

        // Check if double is valid integer
        if ((value == Math.floor(value)) && !Double.isInfinite(value)) {
            return Integer.toString((int) value);
        } else {
            return Double.toString(roundDecimalToTwoDecimalPlaces(value));
        }

    }

    // Count and return student bonus amount
    public double[] getStudentBonusAmounts(String studentID) throws SQLException {

        double[] bonusAmounts = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        // Get all bonuses for student
        ResultSet bonusTransactions = connection.prepareStatement(String.format("select BonusType, LessonType, UnitsUsed, RedeemedOn"
                + " FROM BonusTransaction WHERE StudentID='%s';", studentID)).executeQuery();

        while (bonusTransactions.next()) {

            // Get variables
            String bonusLessonType = bonusTransactions.getString(1) + bonusTransactions.getString(2);
            double unitsUsed = bonusTransactions.getDouble(3);
            String redeemedOn = bonusTransactions.getString(4);

            // Do not count transactions that have been redeemed
            if (!((bonusLessonType.contains("Pending")) && (stringNotNull(redeemedOn)))) {

                // Update amount based on type
                if (bonusLessonType.equals("RealPrivate")) {

                    bonusAmounts[0] = bonusAmounts[0] + unitsUsed;

                } else if (bonusLessonType.equals("RealGroup")) {

                    bonusAmounts[1] = bonusAmounts[1] + unitsUsed;

                } else if (bonusLessonType.equals("RealParty")) {

                    bonusAmounts[2] = bonusAmounts[2] + unitsUsed;

                } else if (bonusLessonType.equals("PendingPrivate")) {

                    bonusAmounts[3] = bonusAmounts[3] + unitsUsed;

                } else if (bonusLessonType.equals("PendingGroup")) {

                    bonusAmounts[4] = bonusAmounts[4] + unitsUsed;

                } else if (bonusLessonType.equals("PendingParty")) {

                    bonusAmounts[5] = bonusAmounts[5] + unitsUsed;

                }
            }
        }

        return bonusAmounts;
    }
}
