/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.student;

import commons.entities.Enrollment;
import commons.entities.Lesson;
import static commons.helpers.ServerHelper.connection;
import commons.helpers.ComboBoxHelper;
import views.program_enrollment.StudentProgramEnrollment;
import views.program_enrollment.EditProgramEnrollment;
import views.lesson.StudentScheduleLesson;
import views.lesson.EditLesson;
import commons.helpers.GenericHelper;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import static commons.helpers.NavHelper.addToNavHistory;
import static commons.helpers.NavHelper.navHistory;
import static commons.helpers.NavHelper.openPreviousView;
import static commons.helpers.NavHelper.printNavHistory;
import static commons.helpers.NavHelper.studentID;
import commons.helpers.TableHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import views.schedule.LessonSchedule;
import static commons.helpers.NavHelper.studentType;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author daynehammes
 */
public class StudentDetails extends javax.swing.JFrame {

    private StudentAttendLesson attendPurchaseLesson = null;
    private GenericHelper genericHelper = new GenericHelper();
    private TableHelper tableHelper = new TableHelper();
    private ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    private StudentProgramEnrollment studentProgramEnrollment;
    private ArrayList<String> instructorArrayList = new ArrayList<>();
    private ArrayList<String[]> referralArrayList = new ArrayList<>();
    private DefaultTableModel enrollmentTableModel, lessonTableModel, bonusHistoryTableModel, paymentHistoryTableModel;
    private TableRowSorter enrollmentSorter, lessonSorter, bonusHistorySorter, paymentHistorySorter;
    ListSelectionModel enrollmentTableSelectionModel = null;
    private ActionListener referredByTypeSelectActionListener;
    private JFormattedTextField phoneInput, cellInput, cellInput2;

    /**
     * Creates new form Menu
     *
     *
     *
     */
    public StudentDetails() {

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

        // Set fields with student id
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setFields() throws SQLException, ClassNotFoundException, ParseException, InterruptedException {

        // Format and add phone inputs
        MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");
        phoneInput = new JFormattedTextField(phoneFormatter);
        cellInput = new JFormattedTextField(phoneFormatter);
        cellInput2 = new JFormattedTextField(phoneFormatter);
        detailsPanel.add(phoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 120, 190, 30));
        detailsPanel.add(cellInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 190, 30));
        detailsPanel.add(cellInput2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 190, 30));

        // Set initial enrollment table values
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                enrollmentTableSelectionModel = enrollmentTable.getSelectionModel();

                // Add table list selection listener to enrollment model
                enrollmentTableSelectionModel.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {

                        // Update Used/Total and Components
                        new EnrollmentUIUpdater().execute();

                    }

                });

            }
        };

        Thread thread2 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 7, "StudentDetails");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        Thread thread3 = new Thread() {
            public void run() {

                // Create arraylist of instructors and apply to combobox
                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect2, true);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect3, true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread4 = new Thread() {
            public void run() {

                try {

                    tableHelper.populatePaymentHistoryTable(paymentHistoryTable, paymentHistorySorter, paymentHistoryTableModel, studentID, 5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Create traversal policy
        Thread thread5 = new Thread() {
            public void run() {

                setCustomTraversalPolicy();
            }
        };

        // Set initial bonus history table values
        Thread thread6 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateBonusHistoryTable(bonusHistoryTable, bonusHistorySorter, bonusHistoryTableModel, studentID, 11);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                enrollmentTableSelectionModel = enrollmentTable.getSelectionModel();

                // Add table list selection listener to enrollment model
                enrollmentTableSelectionModel.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {

                        // Update Used/Total and Components
                        new EnrollmentUIUpdater().execute();

                    }

                });

            }
        };

        // Create component lists and traversal policy
        Thread thread7 = new Thread() {
            public void run() {

                try {

                    comboBoxHelper.populateStudentReferralListAndComboBox(referralArrayList, studentReferrerSelect);
                    comboBoxHelper.populateReferralTypeComboBox(referredBySelect);
                    comboBoxHelper.setComboBoxStudentReferrerAndType(referralArrayList, referredBySelect, studentReferrerSelect, studentID);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Set bonus amounts
        Thread thread8 = new Thread() {
            public void run() {

                try {

                    setBonusAmounts();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Start population threads
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();

        // Wait for finish
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();
        thread6.join();
        thread7.join();
        thread8.join();

        // Set editable text field boxes with student data
        setStudentTextFields();

        // Start event listener for referral select
        if (referredBySelect.getSelectedItem().toString().equals("Student")) {
            studentReferrerSelect.setEnabled(true);
        }
        referredByTypeSelectActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Check if Referral Type is Guest
                if (referredBySelect.getSelectedItem().toString().equals("Student")) {
                    studentReferrerSelect.setEnabled(true);
                } else {
                    studentReferrerSelect.setEnabled(false);
                }
            }
        };
        referredBySelect.addActionListener(referredByTypeSelectActionListener);

    }

    private void setBonusAmounts() {

        try {

            // Get bonus amounts
            double[] bonusAmounts = genericHelper.getStudentBonusAmounts(studentID);

            // Set fields
            privateBonusInput.setText(String.format("%.2f", bonusAmounts[0]));
            groupBonusInput.setText(String.format("%.2f", bonusAmounts[1]));
            partyBonusInput.setText(String.format("%.2f", bonusAmounts[2]));
            pendingPrivateBonusInput.setText(String.format("%.2f", bonusAmounts[3]));
            pendingGroupBonusInput.setText(String.format("%.2f", bonusAmounts[4]));
            pendingPartyBonusInput.setText(String.format("%.2f", bonusAmounts[5]));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(firstNameInput);
        componentArrayList.add(lastNameInput);
        componentArrayList.add(studentIDInput);
        componentArrayList.add(phoneInput);
        componentArrayList.add(cellInput);
        componentArrayList.add(cellInput2);
        componentArrayList.add(addressInput);
        componentArrayList.add(cityInput);
        componentArrayList.add(stateInput);
        componentArrayList.add(zipInput);
        componentArrayList.add(emailInput);
        componentArrayList.add(emailInput2);
        componentArrayList.add(referredBySelect);
        componentArrayList.add(birthDateChooser);
        componentArrayList.add(birthDateChooser2);
        componentArrayList.add(instructorSelect);
        componentArrayList.add(instructorSelect2);
        componentArrayList.add(instructorSelect3);
        componentArrayList.add(studentTypeSelect);
        componentArrayList.add(mailingListCheck);
        componentArrayList.add(activeCheck);
        componentArrayList.add(notesInput);
        componentArrayList.add(updateDetailsBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        detailsPanel.setFocusTraversalPolicyProvider(true);
        detailsPanel.setFocusTraversalPolicy(policy);

    }

    // Set editable text field boxes with student data
    private void setStudentTextFields() throws SQLException, ClassNotFoundException {

        try {

            // Get student data with id
            ResultSet resultSet = connection.prepareStatement(
                    "select FName,LName,StudentID,Phone,Cell1,Cell2,Address,City,State,ZipCode,Email,Email2,BirthDate,BirthDate2,InstructorID,InstructorID2,InstructorID3,MailingList,Active,StudentType,Notes,"
                    + "ReferrerID from Students where StudentID='" + studentID + "';").executeQuery();

            if (resultSet.next()) {

                // Get variables
                String firstName = resultSet.getString(1);
                String lastName = resultSet.getString(2);
                String studentID = resultSet.getString(3);
                String phone = resultSet.getString(4);
                String cell = resultSet.getString(5);
                String cell2 = resultSet.getString(6);
                String address = resultSet.getString(7);
                String city = resultSet.getString(8);
                String state = resultSet.getString(9);
                String zip = resultSet.getString(10);
                String email = resultSet.getString(11);
                String email2 = resultSet.getString(12);
                String instructorID = resultSet.getString(15);
                String instructorID2 = resultSet.getString(16);
                String instructorID3 = resultSet.getString(17);
                String studentType = resultSet.getString(20);
                String notes = resultSet.getString(21);
                String referrerID = resultSet.getString(22);

                // Set Dates
                String birthDateStr = resultSet.getString(13);
                String birthDateStr2 = resultSet.getString(14);
                if ((birthDateStr != null) && (birthDateStr.length() > 0)) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date birthDate = format.parse(birthDateStr);
                    birthDateChooser.setDate(birthDate);
                }
                if ((birthDateStr2 != null) && (birthDateStr2.length() > 0)) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date birthDate = format.parse(birthDateStr2);
                    birthDateChooser2.setDate(birthDate);
                }
                System.out.println("sdetsdate="+birthDateStr);
                // Mailing List Bool
                if (resultSet.getBoolean(18)) {
                    mailingListCheck.setSelected(true);
                }
                // Active Bool
                if (resultSet.getBoolean(19)) {
                    activeCheck.setSelected(true);
                }

                // Set Student Type
                studentTypeSelect.setSelectedItem(studentType);

                // Set Instructor and Referral comobobox to correct value
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, instructorSelect, instructorID, false);
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, instructorSelect2, instructorID2, true);
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, instructorSelect3, instructorID3, true);
                comboBoxHelper.setComboBoxReferralNone(referralArrayList, studentReferrerSelect, referrerID);

                // Set text fields with student data
                firstNameInput.setText(firstName);
                lastNameInput.setText(lastName);
                studentIDInput.setText(studentID);
                addressInput.setText(address);
                cityInput.setText(city);
                stateInput.setText(state);
                zipInput.setText(zip);
                emailInput.setText(email);
                emailInput2.setText(email2);
                notesInput.setText(notes);

                // Set phone inputs only if not empty to leave mask
                if (phone.length() > 0) {
                    phoneInput.setText(phone);
                }
                if (cell.length() > 0) {
                    cellInput.setText(cell);
                }
                if (cell2.length() > 0) {
                    cellInput2.setText(cell2);
                }

                // Set Student Name
                this.studentNameField.setText(lastName + ", " + firstName);

                // Update enrollment for first row
                new EnrollmentUIUpdater().execute();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Update Student information
    private void updateStudentInfo() throws ClassNotFoundException, SQLException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really save changes?", "Confirm Edit", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Ensure required fields are not blank
            if (firstNameInput.getText().equals("")
                    || lastNameInput.getText().equals("")
                    || studentIDInput.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nFirst Name\nLast Name\n" + studentType + " ID",
                        "Fill required Fields", JOptionPane.WARNING_MESSAGE);

            } else {

                // Get required variables
                String firstName = StringEscapeUtils.escapeSql(firstNameInput.getText());
                String lastName = StringEscapeUtils.escapeSql(lastNameInput.getText());
                String updatedStudentID = StringEscapeUtils.escapeSql(studentIDInput.getText());
                String address = StringEscapeUtils.escapeSql(addressInput.getText());
                String city = StringEscapeUtils.escapeSql(cityInput.getText());
                String state = StringEscapeUtils.escapeSql(stateInput.getText());
                String zip = StringEscapeUtils.escapeSql(zipInput.getText());
                String phone = StringEscapeUtils.escapeSql(phoneInput.getText());
                String cell = StringEscapeUtils.escapeSql(cellInput.getText());
                String cell2 = StringEscapeUtils.escapeSql(cellInput2.getText());
                String email = StringEscapeUtils.escapeSql(emailInput.getText());
                String email2 = StringEscapeUtils.escapeSql(emailInput2.getText());
                String notes = StringEscapeUtils.escapeSql(notesInput.getText());
                String mailingList = "FALSE";
                String active = "FALSE";
                String guest = "FALSE";
                if (mailingListCheck.isSelected()) {
                    mailingList = "TRUE";
                }
                if (activeCheck.isSelected()) {
                    active = "TRUE";
                }

                // Set phone numbers to blank if no numbers (avoids leaving "() -" as the text)
                if (!phone.matches(".*\\d.*")) {
                    // Doesn't contain number
                    phone = "";
                }
                if (!cell.matches(".*\\d.*")) {
                    // Doesn't contain number
                    cell = "";
                }
                if (!cell2.matches(".*\\d.*")) {
                    // Doesn't contain number
                    cell2 = "";
                }

                // Get Dates
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String birthDate = "", birthDate2 = "";
                try {
                    birthDate = new String(dateFormat.format(birthDateChooser.getDate()));
                } catch (Exception e) {
                    birthDate = "0001-01-01";
                }
                try {
                    birthDate2 = new String(dateFormat.format(birthDateChooser2.getDate()));
                } catch (Exception e) {
                    birthDate2 = "0001-01-01";
                }

                // Get Student Type
                String newStudentType = (String) studentTypeSelect.getSelectedItem();

                // Get instructor and referrer from select lists
                String instructorID = comboBoxHelper.getSelectedInstructorID(instructorSelect, instructorArrayList, false);
                String instructorID2 = comboBoxHelper.getSelectedInstructorID(instructorSelect2, instructorArrayList, true);
                String instructorID3 = comboBoxHelper.getSelectedInstructorID(instructorSelect3, instructorArrayList, true);

                // Get Referral Type and Student Referrer if applicable
                String referralType = referredBySelect.getSelectedItem().toString();
                String referrerID = "";
                if (studentReferrerSelect.isEnabled()) {
                    referrerID = referralArrayList.get(studentReferrerSelect.getSelectedIndex())[0];

                }

                // Update Referral type and promoted on dates
                ResultSet studentSet = genericHelper.getResultSet(String.format("select ReferralType, PromotedToNewStudent, PromotedToStudent, RegisteredAsUnenrolledStudent from Students where StudentID='%s';",
                        studentID));

                String registeredAsUnenrolledStudent = "0001-01-01", promotedToNewStudent = "0001-01-01", promotedToStudent = "0001-01-01";
                double referrerBonusAmount = 0.0;
                if (studentSet.next()) {

                    // Check if this is a new referral entry (Previous referral type was not a referral) and if so, prompt for number of referals to award
                    String oldReferralType = studentSet.getString(1);
                    if (!(oldReferralType.equals("Student")) && (referralType.equals("Student"))) {

                        // Prompt user for number of bonuses to award
                        String referrerBonusAmountText = "NA";
                        int i = 0;
                        while (!genericHelper.isNumericString(referrerBonusAmountText)) {
                            if (i == 0) {
                                referrerBonusAmountText = JOptionPane.showInputDialog("You have chosen a Student Referrer as Referral Type.\nPlease input the number of Bonuses to award Student Referrer: ");
                            } else {
                                referrerBonusAmountText = JOptionPane.showInputDialog("You have chosen a Student Referrer as Referral Type.\nPlease input the number of Bonuses to award Student Referrer: "
                                        + "\nPlease enter a valid number.");
                            }
                            i++;
                        }
                        referrerBonusAmount = Double.parseDouble(referrerBonusAmountText);
                        genericHelper.updateReferrerPendingBonuses(referrerBonusAmount, referrerID, studentID);

                    }

                    // Check if student is promoted
                    promotedToNewStudent = studentSet.getString(2);
                    promotedToStudent = studentSet.getString(3);
                    registeredAsUnenrolledStudent = studentSet.getString(4);

                    if (!newStudentType.equals(studentType)) {

                        Date date = new Date();

                        if (newStudentType.equals("Unenrolled Student")) {

                            registeredAsUnenrolledStudent = dateFormat.format(date);

                        } else if (newStudentType.equals("New Student")) {

                            promotedToNewStudent = dateFormat.format(date);

                        } else if (newStudentType.equals("Student")) {

                            promotedToStudent = dateFormat.format(date);

                        }

                    }

                } // End bonus updates

                try {

                    // Check if student id exist (and get old bonus values)
                    ResultSet studentIDSet = connection.prepareStatement(
                            "select StudentID from Students where StudentID='"
                            + updatedStudentID + "';").executeQuery();

                    // If not, create new student
                    if ((studentIDSet.next()) && (!updatedStudentID.equals(studentID))) {

                        // Notify already student with ID
                        JOptionPane.showMessageDialog(null, "The New Student ID already exists. Please choose a different ID.",
                                "Duplicate ID", JOptionPane.WARNING_MESSAGE);
                    } else {

                        // Update Student Record
                        String insertStatement = String.format(
                                "UPDATE Students SET FName='%s',LName='%s',StudentID='%s',Address='%s',City='%s',State='%s',ZipCode='%s',Phone='%s',"
                                + "Cell1='%s',Cell2='%s',Email='%s',Email2='%s',Notes='%s',MailingList=%b,Active=%b,InstructorID='%s',InstructorID2='%s',"
                                + "InstructorID3='%s',BirthDate='%s',BirthDate2='%s', ReferralType='%s', ReferrerID='%s', StudentType='%s', PromotedToNewStudent='%s',"
                                + " PromotedToStudent='%s', RegisteredAsUnenrolledStudent='%s' WHERE StudentID='%s';",
                                firstName, lastName, updatedStudentID, address, city, state, zip, phone, cell, cell2, email, email2, notes,
                                Boolean.parseBoolean(mailingList),
                                Boolean.parseBoolean(active), instructorID, instructorID2, instructorID3, birthDate, birthDate2, referralType, referrerID, newStudentType, promotedToNewStudent, promotedToStudent,
                                registeredAsUnenrolledStudent, studentID);

                        // Prepare and execute insert statement
                        connection.prepareStatement(insertStatement).execute();

                        // Cascade student ID changes to other tables
                        if (!updatedStudentID.equals(studentID)) {

                            // Update all Program Enrollment
                            String updateProgramEnrollment = String.format("UPDATE ProgramEnrollment SET StudentID='%s' where StudentID='%s';", updatedStudentID, studentID);
                            connection.prepareStatement(updateProgramEnrollment).execute();

                            // Update all Lesson Schedule
                            String updateLessonSchedule = String.format("UPDATE LessonSchedule SET StudentID='%s', StudentName='%s' where StudentID='%s';", updatedStudentID,
                                    String.format("%s %s", firstName, lastName), studentID);
                            connection.prepareStatement(updateLessonSchedule).execute();

                            // Update all Payments
                            String updatePayments = String.format("UPDATE PaymentTransaction SET StudentID='%s' where StudentID='%s';", updatedStudentID, studentID);
                            connection.prepareStatement(updatePayments).execute();

                            // Update all Bonuses
                            String updateBonuses = String.format("UPDATE BonusTransaction SET StudentID='%s' where StudentID='%s';", updatedStudentID, studentID);
                            connection.prepareStatement(updateBonuses).execute();

                        }

                        // Alert success and close
                        JOptionPane.showMessageDialog(null, "Saved all changes.",
                                "Changes Saved", JOptionPane.INFORMATION_MESSAGE);

                        // Repaint bonus transaction table
                        try {
                            tableHelper.populateBonusHistoryTable(bonusHistoryTable, bonusHistorySorter, bonusHistoryTableModel, studentID, 11);
                            bonusHistoryTable.repaint();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }

                } catch (Exception e) {

                    // Alert failed insert
                    JOptionPane.showMessageDialog(null, "Error during update. No changes were saved.",
                            "Error During Update", JOptionPane.ERROR_MESSAGE);

                    e.printStackTrace();
                }

            }

        }

    }

    public class EnrollmentUIUpdater extends SwingWorker<Object, Object> {

        private String privateLessonTotal, groupLessonTotal, partyLessonTotal, privateLessonUsed, groupLessonUsed, partyLessonUsed;

        public EnrollmentUIUpdater() {
        }

        @Override
        protected Integer doInBackground() throws Exception {

            // If initalizing load, set UI from first row
            int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

            // Get enrollment values
            privateLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 3));
            groupLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 5));
            partyLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 7));
            privateLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 16));
            groupLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 17));
            partyLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 18));

            return 1;
        }

        @Override
        protected void done() {

            // Update total and used lesson panes
            privateTotalPane.setText(privateLessonTotal);
            groupTotalPane.setText(groupLessonTotal);
            partyTotalPane.setText(partyLessonTotal);
            privateUsedPane.setText(privateLessonUsed);
            groupUsedPane.setText(groupLessonUsed);
            partyUsedPane.setText(partyLessonUsed);

        }

    }

    // Connects to DB and adds student
    public void addReferralType() throws ClassNotFoundException, SQLException {

        // Verify input is filled in
        if (addReferralTypeInput.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nNew Referral Type",
                    "Fill required Fields", JOptionPane.WARNING_MESSAGE);
        } else {

            // Get variables from input
            String referralTypeName = StringEscapeUtils.escapeSql(addReferralTypeInput.getText());

            try {

                // First check if referral type has been used before
                ResultSet resultSet = connection.prepareStatement(
                        "select * from ReferralType where ReferralType='" + referralTypeName + "';").executeQuery();

                // If not, create new Program
                if (!resultSet.next()) {

                    // Insert new Referral Type in Database
                    String insertStatement = String.format(
                            "Insert into ReferralType(ReferralType) Values('%s');", referralTypeName);

                    // Prepare and execute insert statement
                    connection.prepareStatement(insertStatement).execute();

                    // Alert success and close
                    JOptionPane.showMessageDialog(null, "Successfully added new Referral Type: " + referralTypeName,
                            "Added New Referral Type", JOptionPane.INFORMATION_MESSAGE);

                    // Close this dialog and update referral list
                    Thread updateReferrersComboBox = new Thread() {
                        public void run() {

                            try {

                                referredBySelect.removeActionListener(referredByTypeSelectActionListener);
                                comboBoxHelper.populateReferralTypeComboBox(referredBySelect);
                                referredBySelect.addActionListener(referredByTypeSelectActionListener);
                                comboBoxHelper.setComboBoxStudentReferrerAndType(referralArrayList, referredBySelect, studentReferrerSelect, studentID);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    updateReferrersComboBox.start();

                    AddReferralTypeDialog.dispose();
                    addReferralTypeInput.setText("");

                } else {

                    // Notify already Program with ID
                    JOptionPane.showMessageDialog(null, "That Referral Type already exists. Please choose a different Referral Type.",
                            "Duplicate Referral Type", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        AddReferralTypeDialog = new javax.swing.JDialog();
        mainPanel1 = new javax.swing.JPanel();
        addReferralTypeLabel = new javax.swing.JLabel();
        addReferralTypeBtn = new javax.swing.JButton();
        addReferralTypeCancelBtn = new javax.swing.JButton();
        addReferralTypeInput = new javax.swing.JTextField();
        topLogo = new javax.swing.JLabel();
        mainTitle = new javax.swing.JLabel();
        backBtn = new javax.swing.JButton();
        detailsPanel = new javax.swing.JPanel();
        studentNameField = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        firstNameInput = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        lastNameInput = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        studentIDInput = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        addressInput = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        cityInput = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        stateInput = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        zipInput = new javax.swing.JTextField();
        emailInput = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        birthDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel23 = new javax.swing.JLabel();
        instructorSelect = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        activeCheck = new javax.swing.JCheckBox();
        mailingListCheck = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        BonusPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        privateBonusInput = new javax.swing.JTextField();
        groupBonusInput = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        partyBonusInput = new javax.swing.JTextField();
        updateDetailsBtn = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        birthDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel32 = new javax.swing.JLabel();
        emailInput2 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        PendingBonusPanel = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        pendingPrivateBonusInput = new javax.swing.JTextField();
        pendingGroupBonusInput = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        pendingPartyBonusInput = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        referredBySelect = new javax.swing.JComboBox();
        studentReferrerSelect = new javax.swing.JComboBox();
        newReferralTypeBtn = new javax.swing.JButton();
        studentTypeSelect = new javax.swing.JComboBox();
        jLabel36 = new javax.swing.JLabel();
        instructorSelect2 = new javax.swing.JComboBox();
        jLabel37 = new javax.swing.JLabel();
        instructorSelect3 = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        notesInput = new javax.swing.JTextArea();
        lessonScheduleBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        StudentHistoryTabbedPanel = new javax.swing.JTabbedPane();
        ProgramEnrollmentPane = new javax.swing.JPanel();
        instructorIDLabel = new javax.swing.JLabel();
        instructorIDLabel1 = new javax.swing.JLabel();
        privateUsedPane = new javax.swing.JLabel();
        privateTotalPane = new javax.swing.JLabel();
        instructorIDLabel3 = new javax.swing.JLabel();
        instructorIDLabel2 = new javax.swing.JLabel();
        groupUsedPane = new javax.swing.JLabel();
        groupTotalPane = new javax.swing.JLabel();
        instructorIDLabel5 = new javax.swing.JLabel();
        instructorIDLabel4 = new javax.swing.JLabel();
        partyTotalPane = new javax.swing.JLabel();
        partyUsedPane = new javax.swing.JLabel();
        newEnrollmentBtn = new javax.swing.JButton();
        editEnrollmentBtn = new javax.swing.JButton();
        deleteEnrollmentBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        enrollmentTable = new javax.swing.JTable();
        LessonHistoryPane = new javax.swing.JPanel();
        newLessonBtn = new javax.swing.JButton();
        editLessonBtn = new javax.swing.JButton();
        deleteLessonBtn = new javax.swing.JButton();
        cancelLessonBtn = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lessonTable = new javax.swing.JTable();
        PaymentHistoryPanel = new javax.swing.JPanel();
        deletePaymentBtn = new javax.swing.JButton();
        newPaymentBtn = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        paymentHistoryTableScrollPane = new javax.swing.JScrollPane();
        paymentHistoryTable = new javax.swing.JTable();
        BonusHistoryPanel = new javax.swing.JPanel();
        removeBonusBtn = new javax.swing.JButton();
        redeemBonusBtn = new javax.swing.JButton();
        addBonusBtn = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        bonusHistoryTableScrollPane = new javax.swing.JScrollPane();
        bonusHistoryTable = new javax.swing.JTable();

        AddReferralTypeDialog.setSize(new java.awt.Dimension(400, 170));
        AddReferralTypeDialog.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mainPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Add Referral Type"));
        mainPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addReferralTypeLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        addReferralTypeLabel.setText("New Referral Type:");
        mainPanel1.add(addReferralTypeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        addReferralTypeBtn.setText("Add Referral Type");
        addReferralTypeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReferralTypeBtnActionPerformed(evt);
            }
        });
        mainPanel1.add(addReferralTypeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, 170, 40));

        addReferralTypeCancelBtn.setText("Cancel");
        addReferralTypeCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addReferralTypeCancelBtnActionPerformed(evt);
            }
        });
        mainPanel1.add(addReferralTypeCancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 100, 100, 40));
        mainPanel1.add(addReferralTypeInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 220, 30));

        AddReferralTypeDialog.getContentPane().add(mainPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 160));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(784, 521));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 0, 360, 100));

        mainTitle.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitle.setText("Student Details");
        getContentPane().add(mainTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 83, 290, 40));

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });
        getContentPane().add(backBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        detailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        detailsPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentNameField.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        studentNameField.setForeground(java.awt.Color.blue);
        studentNameField.setText("Example, Name");
        detailsPanel.add(studentNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 510, -1));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("First Name:");
        detailsPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, 10));
        detailsPanel.add(firstNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 190, 30));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Last Name:");
        detailsPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, 10));
        detailsPanel.add(lastNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 190, 30));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Student ID:");
        detailsPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, 10));
        detailsPanel.add(studentIDInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 90, 190, 30));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Phone:");
        detailsPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, -1, 10));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Cell 1:");
        detailsPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, -1, 10));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Address:");
        detailsPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 40, -1, -1));
        detailsPanel.add(addressInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, 190, 30));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("City:");
        detailsPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 70, -1, 10));
        detailsPanel.add(cityInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 60, 190, 30));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("State:");
        detailsPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 100, -1, 10));
        detailsPanel.add(stateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 90, 70, 30));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Zip:");
        detailsPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 100, 30, 10));
        detailsPanel.add(zipInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 90, 80, 30));
        detailsPanel.add(emailInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 190, 30));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Email 1:");
        detailsPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 130, 50, 10));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Birthday 1:");
        detailsPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 30, -1, 10));
        detailsPanel.add(birthDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 20, 160, -1));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Instructor:");
        detailsPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 90, -1, 10));

        detailsPanel.add(instructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 80, 160, 30));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Mailing List:");
        detailsPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 210, -1, 10));

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setText("Active:");
        detailsPanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 240, -1, 10));
        detailsPanel.add(activeCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 230, -1, 30));
        detailsPanel.add(mailingListCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 200, -1, 30));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Student Type:");
        detailsPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 180, -1, 10));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setText("Notes:");
        detailsPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 270, 40, 10));

        BonusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bonus"));
        BonusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setText("Private");
        BonusPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 28, -1, 20));

        jLabel13.setText("Group");
        BonusPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, -1, -1));

        privateBonusInput.setEditable(false);
        BonusPanel.add(privateBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, 40, -1));

        groupBonusInput.setEditable(false);
        BonusPanel.add(groupBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 30, 40, -1));

        jLabel29.setText("Party");
        BonusPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(211, 30, 40, -1));

        partyBonusInput.setEditable(false);
        BonusPanel.add(partyBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 30, 40, -1));

        detailsPanel.add(BonusPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 340, 300, 60));

        updateDetailsBtn.setText("Update");
        updateDetailsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDetailsBtnActionPerformed(evt);
            }
        });
        detailsPanel.add(updateDetailsBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 370, 130, 40));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Cell 2:");
        detailsPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, 10));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("Birthday 2:");
        detailsPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 60, -1, 10));
        detailsPanel.add(birthDateChooser2, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 50, 160, -1));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Email 2:");
        detailsPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 160, 50, 10));
        detailsPanel.add(emailInput2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 150, 190, 30));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Referrer:");
        detailsPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 220, 70, 10));

        PendingBonusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pending Bonus"));
        PendingBonusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel14.setText("Private");
        PendingBonusPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 28, -1, 20));

        jLabel16.setText("Group");
        PendingBonusPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, -1, -1));

        pendingPrivateBonusInput.setEditable(false);
        PendingBonusPanel.add(pendingPrivateBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, 40, -1));

        pendingGroupBonusInput.setEditable(false);
        pendingGroupBonusInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pendingGroupBonusInputActionPerformed(evt);
            }
        });
        PendingBonusPanel.add(pendingGroupBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 30, 40, -1));

        jLabel34.setText("Party");
        PendingBonusPanel.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(211, 30, 40, -1));

        pendingPartyBonusInput.setEditable(false);
        PendingBonusPanel.add(pendingPartyBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 30, 40, -1));

        detailsPanel.add(PendingBonusPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 340, 300, 60));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setText("Referred By:");
        detailsPanel.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 190, 80, 10));

        referredBySelect.setEditable(true);
        detailsPanel.add(referredBySelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 180, 190, 30));

        studentReferrerSelect.setEnabled(false);
        detailsPanel.add(studentReferrerSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 210, 190, 30));

        newReferralTypeBtn.setText("+");
        newReferralTypeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newReferralTypeBtnActionPerformed(evt);
            }
        });
        detailsPanel.add(newReferralTypeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 180, 30, 30));

        studentTypeSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Student", "New Student", "Unenrolled Student" }));
        detailsPanel.add(studentTypeSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 170, 160, 30));

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel36.setText("Buddy Instructor:");
        detailsPanel.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 120, -1, 10));

        detailsPanel.add(instructorSelect2, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 110, 160, 30));

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel37.setText("Buddy Instructor 2:");
        detailsPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 150, -1, 10));

        detailsPanel.add(instructorSelect3, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 140, 160, 30));

        notesInput.setColumns(20);
        notesInput.setRows(5);
        notesInput.setTabSize(4);
        jScrollPane1.setViewportView(notesInput);

        detailsPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 260, 260, 90));

        getContentPane().add(detailsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 1020, 420));

        lessonScheduleBtn.setText("View Schedule");
        lessonScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lessonScheduleBtnActionPerformed(evt);
            }
        });
        getContentPane().add(lessonScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 70, 160, 40));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel3.setLayout(new java.awt.BorderLayout());

        ProgramEnrollmentPane.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        instructorIDLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel.setText("Private Total:");
        ProgramEnrollmentPane.add(instructorIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, 20));

        instructorIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel1.setText("Private Used:");
        ProgramEnrollmentPane.add(instructorIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 20));

        privateUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateUsedPane.setText("0.0");
        ProgramEnrollmentPane.add(privateUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 40, 20));

        privateTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateTotalPane.setText("0.0");
        ProgramEnrollmentPane.add(privateTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, 40, 20));

        instructorIDLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel3.setText("Group Total:");
        ProgramEnrollmentPane.add(instructorIDLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, -1, 20));

        instructorIDLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel2.setText("Group Used:");
        ProgramEnrollmentPane.add(instructorIDLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 30, -1, 20));

        groupUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupUsedPane.setText("0.0");
        ProgramEnrollmentPane.add(groupUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 40, 20));

        groupTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupTotalPane.setText("0.0");
        ProgramEnrollmentPane.add(groupTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 10, 40, 20));

        instructorIDLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel5.setText("Party Total:");
        ProgramEnrollmentPane.add(instructorIDLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, -1, 20));

        instructorIDLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel4.setText("Party Used:");
        ProgramEnrollmentPane.add(instructorIDLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, -1, 20));

        partyTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyTotalPane.setText("0.0");
        ProgramEnrollmentPane.add(partyTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 10, 40, 20));

        partyUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyUsedPane.setText("0.0");
        ProgramEnrollmentPane.add(partyUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, 40, 20));

        newEnrollmentBtn.setText("New");
        newEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newEnrollmentBtnActionPerformed(evt);
            }
        });
        ProgramEnrollmentPane.add(newEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 90, 30));

        editEnrollmentBtn.setText("Edit");
        editEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editEnrollmentBtnActionPerformed(evt);
            }
        });
        ProgramEnrollmentPane.add(editEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 20, 90, 30));

        deleteEnrollmentBtn.setText("Delete");
        deleteEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEnrollmentBtnActionPerformed(evt);
            }
        });
        ProgramEnrollmentPane.add(deleteEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, 90, 30));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        enrollmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Date", "Program", "Lesson Total", "Lesson Used", "Paid", "Contract"
            }
        ));
        enrollmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                enrollmentTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(enrollmentTable);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel1, java.awt.BorderLayout.CENTER);

        ProgramEnrollmentPane.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 260));

        StudentHistoryTabbedPanel.addTab("Program Enrollment", ProgramEnrollmentPane);

        LessonHistoryPane.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        newLessonBtn.setText("New");
        newLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLessonBtnActionPerformed(evt);
            }
        });
        LessonHistoryPane.add(newLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 20, 90, 30));

        editLessonBtn.setText("Edit");
        editLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLessonBtnActionPerformed(evt);
            }
        });
        LessonHistoryPane.add(editLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 20, 90, 30));

        deleteLessonBtn.setText("Delete");
        deleteLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLessonBtnActionPerformed(evt);
            }
        });
        LessonHistoryPane.add(deleteLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 20, 90, 30));

        cancelLessonBtn.setText("Cancel");
        cancelLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelLessonBtnActionPerformed(evt);
            }
        });
        LessonHistoryPane.add(cancelLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 20, 90, 30));

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        lessonTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "Start Time", "Lesson Type", "Instructor", "Status"
            }
        ));
        jScrollPane3.setViewportView(lessonTable);

        jPanel4.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jPanel5.add(jPanel4, java.awt.BorderLayout.CENTER);

        LessonHistoryPane.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 260));

        StudentHistoryTabbedPanel.addTab("Lesson History", LessonHistoryPane);

        PaymentHistoryPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        deletePaymentBtn.setText("Remove Payment");
        deletePaymentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePaymentBtnActionPerformed(evt);
            }
        });
        PaymentHistoryPanel.add(deletePaymentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 20, 140, 30));

        newPaymentBtn.setText("New Payment");
        newPaymentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPaymentBtnActionPerformed(evt);
            }
        });
        PaymentHistoryPanel.add(newPaymentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 20, 140, 30));

        jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.BorderLayout());

        paymentHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "Start Time", "Lesson Type", "Instructor", "Status"
            }
        ));
        paymentHistoryTableScrollPane.setViewportView(paymentHistoryTable);

        jPanel6.add(paymentHistoryTableScrollPane, java.awt.BorderLayout.CENTER);

        jPanel7.add(jPanel6, java.awt.BorderLayout.CENTER);

        PaymentHistoryPanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 260));

        StudentHistoryTabbedPanel.addTab("Payment History", PaymentHistoryPanel);

        BonusHistoryPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        removeBonusBtn.setText("Remove Bonus");
        removeBonusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBonusBtnActionPerformed(evt);
            }
        });
        BonusHistoryPanel.add(removeBonusBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 20, 140, 30));

        redeemBonusBtn.setText("Redeem Bonus");
        redeemBonusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redeemBonusBtnActionPerformed(evt);
            }
        });
        BonusHistoryPanel.add(redeemBonusBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 20, 140, 30));

        addBonusBtn.setText("Add Bonus");
        addBonusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBonusBtnActionPerformed(evt);
            }
        });
        BonusHistoryPanel.add(addBonusBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 20, 140, 30));

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.BorderLayout());

        bonusHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "Start Time", "Lesson Type", "Instructor", "Status"
            }
        ));
        bonusHistoryTableScrollPane.setViewportView(bonusHistoryTable);

        jPanel8.add(bonusHistoryTableScrollPane, java.awt.BorderLayout.CENTER);

        jPanel9.add(jPanel8, java.awt.BorderLayout.CENTER);

        BonusHistoryPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1030, 260));

        StudentHistoryTabbedPanel.addTab("Bonus History", BonusHistoryPanel);

        jPanel3.add(StudentHistoryTabbedPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 550, 1050, 350));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed

        // Open Student Manager
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                openPreviousView();
                StudentDetails.this.dispose();
            }
        };
        thr.start();

    }//GEN-LAST:event_backBtnActionPerformed

    private void updateDetailsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDetailsBtnActionPerformed

        // Update student details
        try {
            updateStudentInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_updateDetailsBtnActionPerformed

    private void newLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLessonBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentDetails");

        // Open Schedule Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                StudentScheduleLesson studentAddLesson = new StudentScheduleLesson();
                studentAddLesson.setVisible(true);
                StudentDetails.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_newLessonBtnActionPerformed

    private void newEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newEnrollmentBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentDetails");

        // Open Program Enrollment
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                studentProgramEnrollment = new StudentProgramEnrollment();
                studentProgramEnrollment.setVisible(true);
                StudentDetails.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_newEnrollmentBtnActionPerformed

    private void deleteLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLessonBtnActionPerformed

        // Get selected program
        int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());
        String currentLessonID = (String) lessonTable.getModel().getValueAt(row, 7);

        // Delete selected lesson
        try {
            Lesson lesson = new Lesson(currentLessonID);
            lesson.deleteSelectedLesson();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Repaint lessons table
        try {
            tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 7, "StudentDetails");
            lessonTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }//GEN-LAST:event_deleteLessonBtnActionPerformed

    private void cancelLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelLessonBtnActionPerformed

        // Get selected lesson
        int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());
        String currentLessonID = (String) lessonTable.getModel().getValueAt(row, 7);

        // Cancel selected lesson
        try {
            Lesson lesson = new Lesson(currentLessonID);
            lesson.cancelSelectedLesson();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Repaint lessons table
        try {
            tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 7, "StudentDetails");
            enrollmentTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }//GEN-LAST:event_cancelLessonBtnActionPerformed

    private void deleteEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEnrollmentBtnActionPerformed

        // Get selected program
        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        String enrollmentID = (String) enrollmentTable.getModel().getValueAt(row, 12);

        // Delete selected lesson
        try {
            Enrollment enrollment = new Enrollment(enrollmentID);
            enrollment.deleteSelectedEnrollment();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
            tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 7, "StudentDetails");
            tableHelper.populatePaymentHistoryTable(paymentHistoryTable, paymentHistorySorter, paymentHistoryTableModel, studentID, 5);
            tableHelper.populateBonusHistoryTable(bonusHistoryTable, bonusHistorySorter, bonusHistoryTableModel, studentID, 11);
            enrollmentTable.repaint();
            lessonTable.repaint();
            paymentHistoryTable.repaint();
            bonusHistoryTable.repaint();

            setBonusAmounts();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }//GEN-LAST:event_deleteEnrollmentBtnActionPerformed

    private void editLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLessonBtnActionPerformed

        // Get Current Lesson ID
        int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());
        String currentLessonID = (String) lessonTable.getModel().getValueAt(row, 7);

        // Open Edit Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                EditLesson editLesson = new EditLesson(currentLessonID, StudentDetails.this, "StudentDetails");
                editLesson.setLocationRelativeTo(null);
                editLesson.setVisible(true);
            }
        };
        thr.start();

    }//GEN-LAST:event_editLessonBtnActionPerformed

    private void editEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEnrollmentBtnActionPerformed

        // Get selected program
        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        String enrollmentID = (String) enrollmentTable.getModel().getValueAt(row, 12);

        // Open Edit Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                EditProgramEnrollment editProgramEnrollment = new EditProgramEnrollment(StudentDetails.this, enrollmentID);
                editProgramEnrollment.setLocationRelativeTo(null);
                editProgramEnrollment.setVisible(true);
            }
        };
        thr.start();

    }//GEN-LAST:event_editEnrollmentBtnActionPerformed

    private void enrollmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enrollmentTableMouseClicked

        // Update only Used/Total
        new EnrollmentUIUpdater().execute();
    }//GEN-LAST:event_enrollmentTableMouseClicked

    private void pendingGroupBonusInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pendingGroupBonusInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pendingGroupBonusInputActionPerformed

    private void removeBonusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBonusBtnActionPerformed

        // Get selected bonus
        int row = bonusHistoryTable.convertRowIndexToModel(bonusHistoryTable.getSelectedRow());
        String transactionID = (String) bonusHistoryTable.getModel().getValueAt(row, 0);

        // Delete selected lesson
        try {
            genericHelper.deleteBonusTransaction(transactionID);

        } catch (Exception e) {
            e.printStackTrace();

            // Alert Error
            JOptionPane.showMessageDialog(null, "There was a problem deleting Bonus Transaction. Please try again.",
                    "Delete Bonus Transaction Error", JOptionPane.ERROR_MESSAGE);
        }

        try {

            // Reset bonus table
            tableHelper.populateBonusHistoryTable(bonusHistoryTable, bonusHistorySorter, bonusHistoryTableModel, studentID, 11);
            bonusHistoryTable.repaint();

            // Reset bonus amounts
            setBonusAmounts();

        } catch (Exception e1) {
            e1.printStackTrace();
        }


    }//GEN-LAST:event_removeBonusBtnActionPerformed

    private void redeemBonusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redeemBonusBtnActionPerformed

        // Get selected bonus
        int row = bonusHistoryTable.convertRowIndexToModel(bonusHistoryTable.getSelectedRow());
        String transactionID = (String) bonusHistoryTable.getModel().getValueAt(row, 0);
        String bonusType = (String) bonusHistoryTable.getModel().getValueAt(row, 1);
        String transactionType = (String) bonusHistoryTable.getModel().getValueAt(row, 3);
        String redeemedOn = (String) bonusHistoryTable.getModel().getValueAt(row, 7);
        double unitsUsed = Double.parseDouble((String) bonusHistoryTable.getModel().getValueAt(row, 9));

        // Verify that valid bonus is selected for redemption
        if (!(bonusType.contains("Pending"))) {

            // Alert bonus does not contain Pending
            JOptionPane.showMessageDialog(null, "You have selected an invalid bonus record for redemption.\nPlease only redeem bonus records with 'Pending' in the Bonus Type field.",
                    "Redeem Bonus Error", JOptionPane.ERROR_MESSAGE);

        } else if (unitsUsed <= 0) {

            // Alert bonus does not contain a positive units used
            JOptionPane.showMessageDialog(null, "You have selected an invalid bonus record for redemption.\nPlease only redeem bonus records with a positive value in the Units Used field.",
                    "Redeem Bonus Error", JOptionPane.ERROR_MESSAGE);

        } else if (genericHelper.stringNotNull(redeemedOn)) {

            // Alert bonus has already been redeemed
            JOptionPane.showMessageDialog(null, "You have selected an invalid bonus record for redemption.\nBonus record has already been redeemed.",
                    "Redeem Bonus Error", JOptionPane.ERROR_MESSAGE);

        } else {

            // Redeem bonus
            try {

                genericHelper.redeemPendingBonus(transactionID, transactionType);

            } catch (Exception e) {
                e.printStackTrace();

                // Alert Error
                JOptionPane.showMessageDialog(null, "There was a problem redeeming Bonus. Please try again.",
                        "Redeem Bonus Error", JOptionPane.ERROR_MESSAGE);
            }

            try {

                // Reset bonus table
                tableHelper.populateBonusHistoryTable(bonusHistoryTable, bonusHistorySorter, bonusHistoryTableModel, studentID, 9);
                bonusHistoryTable.repaint();

                // Reset bonus amounts
                setBonusAmounts();

            } catch (Exception e1) {
                e1.printStackTrace();

                JOptionPane.showMessageDialog(null, "Could not refresh bonus amounts. Please refresh page.",
                        "Refresh Error", JOptionPane.ERROR_MESSAGE);
            }

        }

    }//GEN-LAST:event_redeemBonusBtnActionPerformed

    private void newReferralTypeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newReferralTypeBtnActionPerformed

        // Open Edit Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                AddReferralTypeDialog.setLocationRelativeTo(null);
                AddReferralTypeDialog.setVisible(true);

            }
        };
        thr.start();
    }//GEN-LAST:event_newReferralTypeBtnActionPerformed

    private void addReferralTypeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addReferralTypeBtnActionPerformed
        // Add new Program to the database
        try {
            addReferralType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_addReferralTypeBtnActionPerformed

    private void addReferralTypeCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addReferralTypeCancelBtnActionPerformed
        // Close dialog
        AddReferralTypeDialog.dispose();
    }//GEN-LAST:event_addReferralTypeCancelBtnActionPerformed

    private void deletePaymentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePaymentBtnActionPerformed

        // Get selected bonus
        int row = paymentHistoryTable.convertRowIndexToModel(paymentHistoryTable.getSelectedRow());
        String paymentID = (String) paymentHistoryTable.getModel().getValueAt(row, 0);
        String enrollmentID = (String) paymentHistoryTable.getModel().getValueAt(row, 5);
        double paymentAmount = Double.parseDouble((String) paymentHistoryTable.getModel().getValueAt(row, 2)) * -1.0;

        // Delete selected payment
        try {
            genericHelper.deletePaymentTransaction(paymentID, enrollmentID, paymentAmount);

        } catch (Exception e) {
            e.printStackTrace();

            // Alert Error
            JOptionPane.showMessageDialog(null, "There was a problem deleting Payment Transaction. Please try again.",
                    "Delete Payment Transaction Error", JOptionPane.ERROR_MESSAGE);
        }

        try {
            tableHelper.populatePaymentHistoryTable(paymentHistoryTable, paymentHistorySorter, paymentHistoryTableModel, studentID, 5);

            tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);

            paymentHistoryTable.repaint();
            enrollmentTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }//GEN-LAST:event_deletePaymentBtnActionPerformed

    private void lessonScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lessonScheduleBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentDetails");

        // Open Program Enrollment
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                LessonSchedule lessonSchedule = new LessonSchedule();
                lessonSchedule.setVisible(true);
                StudentDetails.this.dispose();
            }
        };
        thr.start();

    }//GEN-LAST:event_lessonScheduleBtnActionPerformed

    private void newPaymentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPaymentBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentDetails");

        // Open Attend Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                attendPurchaseLesson = new StudentAttendLesson();
                attendPurchaseLesson.setVisible(true);
                StudentDetails.this.dispose();
            }
        };
        thr.start();

    }//GEN-LAST:event_newPaymentBtnActionPerformed

    private void addBonusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBonusBtnActionPerformed

        // Open Add Bonus Dialog
        Thread thr = new Thread() {
            public void run() {
                AddBonus add_bonus = new AddBonus(StudentDetails.this);
                add_bonus.setLocationRelativeTo(null);
                add_bonus.setVisible(true);
            }
        };
        thr.start();
    }//GEN-LAST:event_addBonusBtnActionPerformed

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
            java.util.logging.Logger.getLogger(StudentDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentDetails.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentDetails().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog AddReferralTypeDialog;
    private javax.swing.JPanel BonusHistoryPanel;
    private javax.swing.JPanel BonusPanel;
    private javax.swing.JPanel LessonHistoryPane;
    private javax.swing.JPanel PaymentHistoryPanel;
    private javax.swing.JPanel PendingBonusPanel;
    private javax.swing.JPanel ProgramEnrollmentPane;
    private javax.swing.JTabbedPane StudentHistoryTabbedPanel;
    private javax.swing.JCheckBox activeCheck;
    private javax.swing.JButton addBonusBtn;
    private javax.swing.JButton addReferralTypeBtn;
    private javax.swing.JButton addReferralTypeCancelBtn;
    private javax.swing.JTextField addReferralTypeInput;
    private javax.swing.JLabel addReferralTypeLabel;
    private javax.swing.JTextField addressInput;
    private javax.swing.JButton backBtn;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private com.toedter.calendar.JDateChooser birthDateChooser2;
    private javax.swing.JTable bonusHistoryTable;
    private javax.swing.JScrollPane bonusHistoryTableScrollPane;
    private javax.swing.JButton cancelLessonBtn;
    private javax.swing.JTextField cityInput;
    private javax.swing.JButton deleteEnrollmentBtn;
    private javax.swing.JButton deleteLessonBtn;
    private javax.swing.JButton deletePaymentBtn;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JButton editEnrollmentBtn;
    private javax.swing.JButton editLessonBtn;
    private javax.swing.JTextField emailInput;
    private javax.swing.JTextField emailInput2;
    private javax.swing.JTable enrollmentTable;
    private javax.swing.JTextField firstNameInput;
    private javax.swing.JTextField groupBonusInput;
    private javax.swing.JLabel groupTotalPane;
    private javax.swing.JLabel groupUsedPane;
    private javax.swing.JLabel instructorIDLabel;
    private javax.swing.JLabel instructorIDLabel1;
    private javax.swing.JLabel instructorIDLabel2;
    private javax.swing.JLabel instructorIDLabel3;
    private javax.swing.JLabel instructorIDLabel4;
    private javax.swing.JLabel instructorIDLabel5;
    private javax.swing.JComboBox instructorSelect;
    private javax.swing.JComboBox instructorSelect2;
    private javax.swing.JComboBox instructorSelect3;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField lastNameInput;
    private javax.swing.JButton lessonScheduleBtn;
    private javax.swing.JTable lessonTable;
    private javax.swing.JCheckBox mailingListCheck;
    private javax.swing.JPanel mainPanel1;
    private javax.swing.JLabel mainTitle;
    private javax.swing.JButton newEnrollmentBtn;
    private javax.swing.JButton newLessonBtn;
    private javax.swing.JButton newPaymentBtn;
    private javax.swing.JButton newReferralTypeBtn;
    private javax.swing.JTextArea notesInput;
    private javax.swing.JTextField partyBonusInput;
    private javax.swing.JLabel partyTotalPane;
    private javax.swing.JLabel partyUsedPane;
    private javax.swing.JTable paymentHistoryTable;
    private javax.swing.JScrollPane paymentHistoryTableScrollPane;
    private javax.swing.JTextField pendingGroupBonusInput;
    private javax.swing.JTextField pendingPartyBonusInput;
    private javax.swing.JTextField pendingPrivateBonusInput;
    private javax.swing.JTextField privateBonusInput;
    private javax.swing.JLabel privateTotalPane;
    private javax.swing.JLabel privateUsedPane;
    private javax.swing.JButton redeemBonusBtn;
    private javax.swing.JComboBox referredBySelect;
    private javax.swing.JButton removeBonusBtn;
    private javax.swing.JTextField stateInput;
    private javax.swing.JTextField studentIDInput;
    private javax.swing.JLabel studentNameField;
    private javax.swing.JComboBox studentReferrerSelect;
    private javax.swing.JComboBox studentTypeSelect;
    private javax.swing.JLabel topLogo;
    private javax.swing.JButton updateDetailsBtn;
    private javax.swing.JTextField zipInput;
    // End of variables declaration//GEN-END:variables
}
