/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.student;

import commons.helpers.GenericHelper;
import commons.helpers.ComboBoxHelper;
import static commons.helpers.ServerHelper.connection;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import java.util.Date;
import static commons.helpers.NavHelper.studentType;
import commons.helpers.ValidationHelper;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Akureyri
 */
public class AddStudent extends javax.swing.JDialog {

    private ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    private ValidationHelper validationHelper = new ValidationHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    private ArrayList<String[]> referralArrayList = new ArrayList<String[]>();
    private ActionListener referredByTypeSelectActionListener;
    java.awt.Frame parent;
    private JFormattedTextField phoneInput, cellInput, cellInput2;

    /**
     * Creates new form AddStudent
     */
    public AddStudent(java.awt.Frame parent) {
        super(parent, true);

        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Add " + studentType);
        initComponents();

        // Initialize fields
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Initialize fields
    private void setFields() throws ClassNotFoundException, SQLException, ParseException, InterruptedException {

        // Format and add phone inputs
        MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");
        phoneInput = new JFormattedTextField(phoneFormatter);
        cellInput = new JFormattedTextField(phoneFormatter);
        cellInput2 = new JFormattedTextField(phoneFormatter);
        mainPanel.add(phoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 290, 190, 30));
        mainPanel.add(cellInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 190, 30));
        mainPanel.add(cellInput2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 50, 190, 30));

        // Create arraylist of referrers and apply to combobox
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateStudentReferralListAndComboBox(referralArrayList, studentReferrerSelect);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        Thread thread2 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect2, true);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect3, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Create component lists and traversal policy
        Thread thread3 = new Thread() {
            public void run() {

                setCustomTraversalPolicy();
            }
        };

        // Create component lists and traversal policy
        Thread thread4 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateReferralTypeComboBox(referredBySelect);
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

        // Wait for finish
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        // Set panel title for New Student
        setPanelTitle();

        // Start event listener for referral select
        referredByTypeSelectActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                // Check if Referral Type is Guest
                if (referredBySelect.getSelectedItem().toString().equals("Student")) {
                    studentReferrerSelect.setEnabled(true);
                    referrerBonusesInput.setEditable(true);
                } else {
                    studentReferrerSelect.setEnabled(false);
                    referrerBonusesInput.setEditable(false);
                }
            }
        };
        referredBySelect.addActionListener(referredByTypeSelectActionListener);

        // Set submit button
        getRootPane().setDefaultButton(saveStudentBtn);

    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(firstNameInput);
        componentArrayList.add(lastNameInput);
        componentArrayList.add(studentIDInput);
        componentArrayList.add(addressInput);
        componentArrayList.add(cityInput);
        componentArrayList.add(stateInput);
        componentArrayList.add(zipInput);
        componentArrayList.add(birthDateChooser);
        componentArrayList.add(birthDateChooser2);
        componentArrayList.add(notesInput);
        componentArrayList.add(phoneInput);
        componentArrayList.add(privateBonusInput);
        componentArrayList.add(groupBonusInput);
        componentArrayList.add(partyBonusInput);
        componentArrayList.add(cellInput);
        componentArrayList.add(cellInput2);
        componentArrayList.add(emailInput);
        componentArrayList.add(emailInput2);
        componentArrayList.add(instructorSelect);
        componentArrayList.add(instructorSelect2);
        componentArrayList.add(instructorSelect3);
        componentArrayList.add(referredBySelect);
        componentArrayList.add(referrerBonusesInput);
        componentArrayList.add(mailingListCheck);
        componentArrayList.add(saveStudentBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    private void setPanelTitle() {

        // Check if guest or student and set titled border and button text
        Border titledBorder;
        if (studentType.equals("New Student")) {
            titledBorder = BorderFactory.createTitledBorder("Add New Student");
            saveStudentBtn.setText("Add New Student");
        } else {
            titledBorder = BorderFactory.createTitledBorder("Add Student");
        }
        mainPanel.setBorder(titledBorder);
    }

    // Connect to DB and create student
    private void addStudentToDatabase() throws ClassNotFoundException, SQLException {

        if (firstNameInput.getText().equals("")
                || lastNameInput.getText().equals("")
                || studentIDInput.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nFirst Name\nLast Name\nStudent ID",
                    "Fill required Fields", JOptionPane.WARNING_MESSAGE);
        } else if (allInputsValid()) {

            // Get variables from input
            String lastName = StringEscapeUtils.escapeSql(lastNameInput.getText());
            String firstName = StringEscapeUtils.escapeSql(firstNameInput.getText());
            String studentID = StringEscapeUtils.escapeSql(studentIDInput.getText());
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
            String bonusPrivateText = StringEscapeUtils.escapeSql(privateBonusInput.getText());
            String bonusGroupText = StringEscapeUtils.escapeSql(groupBonusInput.getText());
            String bonusPartyText = StringEscapeUtils.escapeSql(partyBonusInput.getText());
            String mailingList = "FALSE";
            if (mailingListCheck.isSelected()) {
                mailingList = "TRUE";
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

            // Get instructor from select list
            String instructorID = comboBoxHelper.getSelectedInstructorName(instructorSelect, instructorArrayList);
            String instructorID2 = comboBoxHelper.getSelectedInstructorName(instructorSelect2, instructorArrayList);
            String instructorID3 = comboBoxHelper.getSelectedInstructorName(instructorSelect3, instructorArrayList);

            // Get Referral Type and Student Referrer if applicable
            String referralType = referredBySelect.getSelectedItem().toString();
            String referrerID = "";
            if (studentReferrerSelect.isEnabled()) {
                referrerID = referralArrayList.get(studentReferrerSelect.getSelectedIndex())[0];
            }

            // Check if bonus inputs were empty
            double bonusPrivate = 0.0, bonusGroup = 0.0, bonusParty = 0.0;
            if (genericHelper.isNumericString(bonusPrivateText)) {
                bonusPrivate = Double.parseDouble(bonusPrivateText);
            }
            if (genericHelper.isNumericString(bonusGroupText)) {
                bonusGroup = Double.parseDouble(bonusGroupText);
            }
            if (genericHelper.isNumericString(bonusPartyText)) {
                bonusParty = Double.parseDouble(bonusPartyText);
            }

            // Add dates
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String birthDate;
            try {
                birthDate = dateFormat.format(birthDateChooser.getDate());
            } catch (Exception e) {
                birthDate = "0001-01-01";
            }

            // Check student type for promoted_to fields
            String registeredAsUnenrolledStudent = "0001-01-01", promotedToNewStudent = "0001-01-01", promotedToStudent = "0001-01-01";
            Date date = new Date();
            if (studentType.equals("Unenrolled Student")) {
                registeredAsUnenrolledStudent = dateFormat.format(date);
            } else if (studentType.equals("New Student")) {
                promotedToNewStudent = dateFormat.format(date);
            } else if (studentType.equals("Student")) {
                promotedToStudent = dateFormat.format(date);

            }

            try {
                // Create new student
                String createStudentData = String.format("Insert into Students(StudentID,LName,FName,Address,City,State,ZipCode,Phone,"
                        + "Cell1,Cell2,Email,Email2,Notes,InstructorID,InstructorID2,InstructorID3,ReferralType,ReferrerID,BirthDate,MailingList,"
                        + "StudentType,PromotedToNewStudent,PromotedToStudent,RegisteredAsUnenrolledStudent,Active) Values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%b,'%s',"
                        + "'%s','%s','%s',TRUE);",
                        studentID, lastName, firstName, address, city, state, zip, phone, cell, cell2, email, email2, notes, instructorID, instructorID2, instructorID3,
                        referralType, referrerID, birthDate, Boolean.parseBoolean(mailingList), studentType, promotedToNewStudent, promotedToStudent,
                        registeredAsUnenrolledStudent);
                connection.prepareStatement(createStudentData).execute();

                // Create bonus transaction if any are larger than 0
                if (bonusPrivate > 0) {

                    String bonusTransactionData = String.format("Insert into BonusTransaction(StudentID,UnitsUsed,TransactionType,BonusType,LessonType) "
                            + "Values('%s',%.2f,'ManualUpdate','Real','Private');",
                            studentID, bonusPrivate);
                    connection.prepareStatement(bonusTransactionData).execute();

                }
                if (bonusGroup > 0) {

                    String bonusTransactionData = String.format("Insert into BonusTransaction(StudentID,UnitsUsed,TransactionType,BonusType,LessonType) "
                            + "Values('%s',%.2f,'ManualUpdate','Real','Group');",
                            studentID, bonusGroup);
                    connection.prepareStatement(bonusTransactionData).execute();

                }
                if (bonusParty > 0) {

                    String bonusTransactionData = String.format("Insert into BonusTransaction(StudentID,UnitsUsed,TransactionType,BonusType,LessonType) "
                            + "Values('%s',%.2f,'ManualUpdate','Real','Party');",
                            studentID, bonusParty);
                    connection.prepareStatement(bonusTransactionData).execute();

                }

                // Create pending bonus transaction for Referrer if student referral selected
                if (studentReferrerSelect.isEnabled()) {

                    double referrerBonusAmount = 0.0;
                    if (genericHelper.isNumericString(referrerBonusesInput.getText())) {
                        referrerBonusAmount = Double.parseDouble(referrerBonusesInput.getText());
                    }

                    if (referrerBonusAmount > 0) {

                        genericHelper.updateReferrerPendingBonuses(referrerBonusAmount, referrerID, studentID);

                    }

                }

                // Alert success and close
                JOptionPane.showMessageDialog(null, "Saved new " + studentType + ": " + firstName + " " + lastName,
                        "Saved New Student", JOptionPane.INFORMATION_MESSAGE);

                // Close this and reopen student manager
                StudentManager studentManager = new StudentManager();
                studentManager.setVisible(true);
                parent.dispose();
                this.dispose();

            } catch (SQLIntegrityConstraintViolationException e) {
                // Catch duplicate Student ID
                JOptionPane.showMessageDialog(null, studentType + " exists with this ID.", studentType + " already Exists", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e1) {

                JOptionPane.showMessageDialog(null, "Error adding new " + studentType + ".", "Insert Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }

        }
    }

    public boolean allInputsValid() {

        // Verify referrer and bonus amount if Student referrer chosen
        if (studentReferrerSelect.isEnabled()) {

            try {
                String referrerID = referralArrayList.get(studentReferrerSelect.getSelectedIndex())[0];
                if (referrerID.equals("None")) {
                    JOptionPane.showMessageDialog(null, "Invalid Student selected for Referral Type.",
                            "Invalid Student Referrer", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(null, "Invalid Student selected for Referral Type.",
                        "Invalid Student Referrer", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            boolean referrerBonusesIsValid = validationHelper.isValidNumeric(referrerBonusesInput.getText());
            if (!referrerBonusesIsValid) {
                JOptionPane.showMessageDialog(null, "Referrer Bonuses is not a valid number.",
                        "Invalid Referrer Bonuses", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        boolean studentIDIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(studentIDInput.getText()), 32);
        boolean studentFNameIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(firstNameInput.getText()), 32);
        boolean studentLNameIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(lastNameInput.getText()), 32);
        boolean addressStreetIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(addressInput.getText()), 64);
        boolean addressCityIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(cityInput.getText()), 32);
        boolean addressStateIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(stateInput.getText()), 16);
        boolean addressZipIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(zipInput.getText()), 16);
        boolean phoneIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(phoneInput.getText()), 128);
        boolean cellIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(cellInput.getText()), 128);
        boolean cell2IsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(cellInput2.getText()), 128);
        boolean emailIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(emailInput.getText()), 128);
        boolean email2IsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(emailInput2.getText()), 128);
        boolean notesIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(notesInput.getText()), 2048);
        boolean privateBonusIsValid = validationHelper.isValidNumeric(privateBonusInput.getText());
        boolean groupBonusIsValid = validationHelper.isValidNumeric(groupBonusInput.getText());
        boolean partyBonusIsValid = validationHelper.isValidNumeric(partyBonusInput.getText());

        if (!studentIDIsValid) {
            JOptionPane.showMessageDialog(null, "Student ID Max Length is 32 Characters",
                            "Invalid Student ID", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!studentFNameIsValid) {
            JOptionPane.showMessageDialog(null, "First Name Max Length is 32 Characters",
                            "Invalid First Name", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!studentLNameIsValid) {
            JOptionPane.showMessageDialog(null, "Last Name Max Length is 32 Characters",
                            "Invalid Last Name", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!addressStreetIsValid) {
            JOptionPane.showMessageDialog(null, "Street Address Max Length is 64 Characters",
                            "Invalid Street Address", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!addressCityIsValid) {
            JOptionPane.showMessageDialog(null, "City Max Length is 32 Characters",
                            "Invalid City", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!addressStateIsValid) {
            JOptionPane.showMessageDialog(null, "State Max Length is 16 Characters",
                            "Invalid State", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!addressZipIsValid) {
            JOptionPane.showMessageDialog(null, "Zipcode Max Length is 16 Characters",
                            "Invalid Zipcode", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!phoneIsValid) {
            JOptionPane.showMessageDialog(null, "Phone Max Length is 128 Characters",
                            "Invalid Phone", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!cellIsValid) {
            JOptionPane.showMessageDialog(null, "Cell Max Length is 128 Characters",
                            "Invalid Cell", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!cell2IsValid) {
            JOptionPane.showMessageDialog(null, "Cell 2 Max Length is 128 Characters",
                            "Invalid Cell 2", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!emailIsValid) {
            JOptionPane.showMessageDialog(null, "Email Max Length is 128 Characters",
                            "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!email2IsValid) {
            JOptionPane.showMessageDialog(null, "Email 2 Max Length is 128 Characters",
                            "Invalid Email 2", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!notesIsValid) {
            JOptionPane.showMessageDialog(null, "Notes Max Length is 2048 Characters",
                            "Invalid Notes", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!privateBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Private Bonus is not a valid number.",
                        "Invalid Private Bonus", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!groupBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Group Bonus is not a valid number.",
                        "Invalid Group Bonus", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!partyBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Party Bonus is not a valid number.",
                        "Invalid Party Bonus", JOptionPane.WARNING_MESSAGE);
            return false;
        } 

        return true;
    }

    // Connects to DB and adds student
    public void addReferralType() throws ClassNotFoundException, SQLException {

        // Verify input is filled in
        if (addReferralTypeInput.getText().equals("")) {

            AddReferralTypeDialog.dispose();
            addReferralTypeInput.setText("");
        } else {

            // Get variables from input
            String referralTypeName = StringEscapeUtils.escapeSql(addReferralTypeInput.getText());

            try {

                // First check if referral type has been used before
                ResultSet resultSet = connection.prepareStatement(
                        "select * from ReferralType where ReferralType='" + referralTypeName + "';").executeQuery();

                // If not, create new Referral Type
                if (resultSet.next()) {

                    // Notify already Program with ID
                    JOptionPane.showMessageDialog(null, "That Referral Type already exists. Please choose a different Referral Type.",
                            "Duplicate Referral Type", JOptionPane.WARNING_MESSAGE);

                } else if (referralTypeName.length() > 256) {
                    JOptionPane.showMessageDialog(null, "Referral Type Name Max Length is 256 Characters.",
                            "Invalid Referral Type", JOptionPane.WARNING_MESSAGE);
                } else {

                    // Insert new Referral Type in Database
                    String insertStatement = String.format(
                            "Insert into ReferralType(ReferralType) Values('%s');", referralTypeName);

                    // Prepare and execute insert statement
                    connection.prepareStatement(insertStatement).execute();

                    // Alert success and close
                    Thread successAlert = new Thread() {
                        public void run() {

                            JOptionPane.showMessageDialog(null, "Successfully added new Referral Type: " + referralTypeName,
                                    "Added New Referral Type", JOptionPane.INFORMATION_MESSAGE);

                        }
                    };
                    successAlert.start();

                    // Close this dialog and update referral list
                    referredBySelect.removeActionListener(referredByTypeSelectActionListener);
                    comboBoxHelper.populateReferralTypeComboBox(referredBySelect);
                    referredBySelect.addActionListener(referredByTypeSelectActionListener);

                    AddReferralTypeDialog.dispose();
                    addReferralTypeInput.setText("");
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
        mainPanel = new javax.swing.JPanel();
        saveStudentBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        firstNameInput = new javax.swing.JTextField();
        lastNameInput = new javax.swing.JTextField();
        studentIDInput = new javax.swing.JTextField();
        addressInput = new javax.swing.JTextField();
        cityInput = new javax.swing.JTextField();
        stateInput = new javax.swing.JTextField();
        zipInput = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        notesInput = new javax.swing.JTextField();
        mailingListCheck = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        emailInput = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        partyBonusInput = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        privateBonusInput = new javax.swing.JTextField();
        groupBonusInput = new javax.swing.JTextField();
        instructorSelect = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        referredBySelect = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        birthDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel26 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        birthDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel29 = new javax.swing.JLabel();
        emailInput2 = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        studentReferrerSelect = new javax.swing.JComboBox();
        newReferralTypeBtn = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        referrerBonusesInput = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        instructorSelect2 = new javax.swing.JComboBox();
        jLabel33 = new javax.swing.JLabel();
        instructorSelect3 = new javax.swing.JComboBox();

        AddReferralTypeDialog.setAlwaysOnTop(true);
        AddReferralTypeDialog.setSize(new java.awt.Dimension(400, 170));
        AddReferralTypeDialog.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mainPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Add Referral Type"));
        mainPanel1.setSize(new java.awt.Dimension(370, 140));
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

        AddReferralTypeDialog.getContentPane().add(mainPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 170));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        saveStudentBtn.setText("Add Student");
        saveStudentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveStudentBtnActionPerformed(evt);
            }
        });
        mainPanel.add(saveStudentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 420, 150, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 420, 100, 40));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("State:");
        mainPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 10));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("City:");
        mainPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 10));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Address:");
        mainPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Last Name:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("First Name:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Student ID:");
        mainPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 10));
        mainPanel.add(firstNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 20, 190, 30));
        mainPanel.add(lastNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 50, 190, 30));
        mainPanel.add(studentIDInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 190, 30));
        mainPanel.add(addressInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 190, 30));
        mainPanel.add(cityInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 190, 30));
        mainPanel.add(stateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 70, 30));
        mainPanel.add(zipInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 80, 30));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Zip:");
        mainPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, 30, 10));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Notes:");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 40, 10));
        mainPanel.add(notesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 260, 190, 30));
        mainPanel.add(mailingListCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 320, -1, 30));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Mailing List:");
        mainPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 330, -1, 10));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Instructor:");
        mainPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 150, -1, 10));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Email 1:");
        mainPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 90, 60, 10));
        mainPanel.add(emailInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, 190, 30));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Bonus:");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 2, 45, -1));

        jLabel11.setText("Group");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 20, -1, 30));

        jLabel13.setText("Private");
        jPanel3.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 26, -1, 20));

        partyBonusInput.setText("0");
        jPanel3.add(partyBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 20, 32, 30));

        jLabel27.setText("Party");
        jPanel3.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 20, -1, 30));

        privateBonusInput.setText("0");
        jPanel3.add(privateBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 32, 30));

        groupBonusInput.setText("0");
        jPanel3.add(groupBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, 32, 30));

        mainPanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 380, 290, 70));

        mainPanel.add(instructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 140, 190, 30));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Cell 1:");
        mainPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 30, -1, 10));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Phone:");
        mainPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, -1, 10));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Referred By:");
        mainPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 240, -1, 10));

        mainPanel.add(referredBySelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 230, 190, 30));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Birthday 1:");
        mainPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 10));
        mainPanel.add(birthDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 200, 190, 30));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setText("Cell 2:");
        mainPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 60, -1, 10));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Birthday 2:");
        mainPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, 10));
        mainPanel.add(birthDateChooser2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 230, 190, 30));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setText("Email 2:");
        mainPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 120, 50, 10));
        mainPanel.add(emailInput2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 110, 190, 30));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Referrer:");
        mainPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 270, -1, 10));

        studentReferrerSelect.setEditable(true);
        studentReferrerSelect.setEnabled(false);
        mainPanel.add(studentReferrerSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 260, 190, 30));

        newReferralTypeBtn.setText("+");
        newReferralTypeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newReferralTypeBtnActionPerformed(evt);
            }
        });
        mainPanel.add(newReferralTypeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 230, 30, 30));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("Referrer Bonuses:");
        mainPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 300, 110, 10));

        referrerBonusesInput.setEditable(false);
        referrerBonusesInput.setText("0.0");
        mainPanel.add(referrerBonusesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 290, 190, 30));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Buddy Instructor:");
        mainPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 180, -1, 10));

        mainPanel.add(instructorSelect2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 170, 190, 30));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Buddy Instructor 2:");
        mainPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 210, -1, 10));

        mainPanel.add(instructorSelect3, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 200, 190, 30));

        getContentPane().add(mainPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 670, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveStudentBtnActionPerformed
        // Add new student to the database
        try {
            addStudentToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_saveStudentBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Close this
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void newReferralTypeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newReferralTypeBtnActionPerformed

        AddReferralTypeDialog.setModal(true);
        AddReferralTypeDialog.setLocationRelativeTo(AddStudent.this);
        AddReferralTypeDialog.setVisible(true);


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
            java.util.logging.Logger.getLogger(AddStudent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddStudent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddStudent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddStudent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddStudent dialog = new AddStudent(new javax.swing.JFrame());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog AddReferralTypeDialog;
    private javax.swing.JButton addReferralTypeBtn;
    private javax.swing.JButton addReferralTypeCancelBtn;
    private javax.swing.JTextField addReferralTypeInput;
    private javax.swing.JLabel addReferralTypeLabel;
    private javax.swing.JTextField addressInput;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private com.toedter.calendar.JDateChooser birthDateChooser2;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField cityInput;
    private javax.swing.JTextField emailInput;
    private javax.swing.JTextField emailInput2;
    private javax.swing.JTextField firstNameInput;
    private javax.swing.JTextField groupBonusInput;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField lastNameInput;
    private javax.swing.JCheckBox mailingListCheck;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel mainPanel1;
    private javax.swing.JButton newReferralTypeBtn;
    private javax.swing.JTextField notesInput;
    private javax.swing.JTextField partyBonusInput;
    private javax.swing.JTextField privateBonusInput;
    private javax.swing.JComboBox referredBySelect;
    private javax.swing.JTextField referrerBonusesInput;
    private javax.swing.JButton saveStudentBtn;
    private javax.swing.JTextField stateInput;
    private javax.swing.JTextField studentIDInput;
    private javax.swing.JComboBox studentReferrerSelect;
    private javax.swing.JTextField zipInput;
    // End of variables declaration//GEN-END:variables
}
