/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.instructor;

import static commons.helpers.ServerHelper.connection;
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
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.text.MaskFormatter;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import commons.helpers.ValidationHelper;

/**
 *
 * @author Akureyri
 */
public class EditInstructor extends javax.swing.JDialog {

    private static String instructorIDStatic;
    private String instructorID;
    java.awt.Frame parent;
    private JFormattedTextField homePhoneInput, workPhoneInput;
    private ValidationHelper validationHelper = new ValidationHelper();

    /**
     * Creates new form AddStudent
     */
    public EditInstructor(java.awt.Frame parent, boolean modal, String instructorIDStatic) {
        super(parent, modal);

        this.instructorID = instructorIDStatic;
        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Edit Instructor");
        initComponents();

        // Set field values for student
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set field values for student
    private void setFields() throws ClassNotFoundException, SQLException, ParseException {

        // Format and add phone inputs
        MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");

        homePhoneInput = new JFormattedTextField(phoneFormatter);
        workPhoneInput = new JFormattedTextField(phoneFormatter);
        mainPanel.add(homePhoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 190, 30));
        mainPanel.add(workPhoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 50, 190, 30));

        // Fill in Instructor Values
        setInstructorValues();

        // Set Custom Traversal Policy
        setCustomTraversalPolicy();

        // Set submit button
        getRootPane().setDefaultButton(saveInstructorBtn);

    }

    // Fill in Instructor Values
    private void setInstructorValues() throws SQLException, ClassNotFoundException, ParseException {

        try {

            // Get instructor data with id
            ResultSet resultSet = connection.prepareStatement(
                    "select FName,LName,InstructorID,Address,City,State,ZipCode,HomePhone,WorkPhone,Email,Title,Notes,BirthDate,HireDate,FullTime,Active,SchedulePriority"
                    + " from Instructors where InstructorID='" + instructorID + "';").executeQuery();

            if (resultSet.next()) {

                // Set text fields with instructor data
                firstNameInput.setText(resultSet.getString(1));
                lastNameInput.setText(resultSet.getString(2));
                instructorIDInput.setText(resultSet.getString(3));
                addressInput.setText(resultSet.getString(4));
                cityInput.setText(resultSet.getString(5));
                stateInput.setText(resultSet.getString(6));
                zipInput.setText(resultSet.getString(7));
                homePhoneInput.setText(resultSet.getString(8));
                workPhoneInput.setText(resultSet.getString(9));
                emailInput.setText(resultSet.getString(10));
                titleInput.setText(resultSet.getString(11));
                notesInput.setText(resultSet.getString(12));
                prioritySelect.setSelectedItem(resultSet.getString(17));

                // Set phone inputs only if not empty to leave mask
                if (resultSet.getString(8).length() > 0) {
                    homePhoneInput.setText(resultSet.getString(8));
                }
                if (resultSet.getString(9).length() > 0) {
                    workPhoneInput.setText(resultSet.getString(9));
                }

                // Set Dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date birthDate = format.parse(resultSet.getString(13));
                Date hireDate = format.parse(resultSet.getString(14));
                birthDateChooser.setDate(birthDate);
                hireDateChooser.setDate(hireDate);

                // Full Time Bool
                if (resultSet.getBoolean(15)) {
                    fullTimeCheck.setSelected(true);
                }

                // Active Bool
                if (resultSet.getBoolean(16)) {
                    activeCheck.setSelected(true);
                }

            }

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
        componentArrayList.add(instructorIDInput);
        componentArrayList.add(addressInput);
        componentArrayList.add(cityInput);
        componentArrayList.add(stateInput);
        componentArrayList.add(zipInput);
        componentArrayList.add(birthDateChooser);
        componentArrayList.add(hireDateChooser);
        componentArrayList.add(homePhoneInput);
        componentArrayList.add(workPhoneInput);
        componentArrayList.add(emailInput);
        componentArrayList.add(titleInput);
        componentArrayList.add(prioritySelect);
        componentArrayList.add(fullTimeCheck);
        componentArrayList.add(activeCheck);
        componentArrayList.add(notesInput);
        componentArrayList.add(saveInstructorBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    // Connects to DB and updates instructor
    public void updateInstructorInfo() throws ClassNotFoundException, SQLException {

        if (allInputsValid()) {
            // Confirmation dialog to see if really wants to save
            int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really save changes?", "Confirm Edit", JOptionPane.YES_NO_OPTION);
            if (confirmUpdate == JOptionPane.YES_OPTION) {

                // Verify input is filled in
                if (firstNameInput.getText().equals("")
                        || lastNameInput.getText().equals("")
                        || instructorIDInput.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nFirst Name\nLast Name\nInstructor ID",
                            "Fill required Fields", JOptionPane.WARNING_MESSAGE);
                } else {

                    // Get variables from input
                    String firstName = StringEscapeUtils.escapeSql(firstNameInput.getText());
                    String lastName = StringEscapeUtils.escapeSql(lastNameInput.getText());
                    String updatedInstructorID = StringEscapeUtils.escapeSql(instructorIDInput.getText());
                    String address = StringEscapeUtils.escapeSql(addressInput.getText());
                    String city = StringEscapeUtils.escapeSql(cityInput.getText());
                    String state = StringEscapeUtils.escapeSql(stateInput.getText());
                    String zip = StringEscapeUtils.escapeSql(zipInput.getText());
                    String homePhone = StringEscapeUtils.escapeSql(homePhoneInput.getText());
                    String workPhone = StringEscapeUtils.escapeSql(workPhoneInput.getText());
                    String email = StringEscapeUtils.escapeSql(emailInput.getText());
                    String notes = StringEscapeUtils.escapeSql(notesInput.getText());
                    String title = StringEscapeUtils.escapeSql(titleInput.getText());
                    int priority = Integer.parseInt(prioritySelect.getSelectedItem().toString());
                    String fullTime = "FALSE";
                    String active = "FALSE";
                    if (fullTimeCheck.isSelected()) {
                        fullTime = "TRUE";
                    }
                    if (activeCheck.isSelected()) {
                        active = "TRUE";
                    }

                    // Add dates
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String birthDate, hireDate;
                    try {
                        birthDate = new String(dateFormat.format(birthDateChooser.getDate()));
                    } catch (Exception e) {
                        birthDate = "2000-01-01";
                    }
                    try {
                        hireDate = new String(dateFormat.format(hireDateChooser.getDate()));
                    } catch (Exception e) {
                        hireDate = "2000-01-01";
                    }

                    // Set phone numbers to blank if no numbers (avoids leaving "() -" as the text)
                    if (!homePhone.matches(".*\\d.*")) {
                        // Doesn't contain number
                        homePhone = "";
                    }
                    if (!workPhone.matches(".*\\d.*")) {
                        // Doesn't contain number
                        workPhone = "";
                    }

                    try {

                        // First check if instructor id has been used before
                        ResultSet resultSet = connection.prepareStatement(
                                "select * from Instructors where InstructorID='" + updatedInstructorID + "';").executeQuery();

                        // If not, create new instructor
                        if ((resultSet.next()) && (!updatedInstructorID.equals(instructorID))) {

                            // Notify already instructor with ID
                            JOptionPane.showMessageDialog(null, "The New Instructor ID already exists. Please choose a different ID.",
                                    "Duplicate Instructor ID", JOptionPane.WARNING_MESSAGE);

                        } else {

                            // Insert new Instructor in Database
                            String insertStatement = String.format(
                                    "UPDATE Instructors SET FName='%s',LName='%s',InstructorID='%s',Address='%s',City='%s',State='%s',ZipCode='%s',HomePhone='%s',"
                                    + "WorkPhone='%s',Email='%s',Notes='%s',Title='%s',SchedulePriority=%d,BirthDate='%s',HireDate='%s',FullTime=%b,Active=%b WHERE InstructorID='%s';",
                                    firstName, lastName, updatedInstructorID, address, city, state, zip, homePhone, workPhone, email, notes, title, priority,
                                    birthDate, hireDate, Boolean.parseBoolean(fullTime), Boolean.parseBoolean(active), instructorID);

                            // Prepare and execute insert statement
                            connection.prepareStatement(insertStatement).execute();

                            // Update Lesson Schedule Instructor Priority
                            connection.prepareStatement(String.format(
                                    "UPDATE LessonSchedule SET InstructorPriority=%d WHERE InstructorID='%s';", priority, instructorID)).execute();

                            // Cascade instructor ID changes to other tables
                            if (!updatedInstructorID.equals(instructorID)) {

                                // Update students table
                                String updateStudents = String.format("UPDATE Students SET InstructorID='%s' where InstructorID='%s';", updatedInstructorID, instructorID);
                                connection.prepareStatement(updateStudents).execute();

                                // Update all Program Enrollment Primary Instructor
                                String updateProgramEnrollmentPrimary = String.format("UPDATE ProgramEnrollment SET PrimaryInstructorID='%s'"
                                        + " where PrimaryInstructorID='%s';", updatedInstructorID, instructorID);
                                connection.prepareStatement(updateProgramEnrollmentPrimary).execute();

                                // Update all Program Enrollment Primary Instructor
                                String updateProgramEnrollmentBuddy1 = String.format("UPDATE ProgramEnrollment SET InstructorID1='%s'"
                                        + " where InstructorID1='%s';", updatedInstructorID, instructorID);
                                connection.prepareStatement(updateProgramEnrollmentBuddy1).execute();

                                // Update all Program Enrollment Primary Instructor
                                String updateProgramEnrollmentBuddy2 = String.format("UPDATE ProgramEnrollment SET InstructorID2='%s'"
                                        + " where InstructorID2='%s';", updatedInstructorID, instructorID);
                                connection.prepareStatement(updateProgramEnrollmentBuddy2).execute();

                                // Update all Lesson Schedule
                                String updateLessonSchedule = String.format("UPDATE LessonSchedule SET InstructorID='%s', InstructorName='%s' where InstructorID='%s';", firstName,
                                        updatedInstructorID, instructorID);
                                connection.prepareStatement(updateLessonSchedule).execute();

                            }

                            // Alert success and close
                            JOptionPane.showMessageDialog(null, "Saved all changes.",
                                    "Changes Saved", JOptionPane.INFORMATION_MESSAGE);

                            // Close this and reopen instructor manager
                            InstructorManager instructorManager = new InstructorManager();
                            instructorManager.setVisible(true);
                            parent.dispose();
                            this.dispose();

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
    }
    
    public boolean allInputsValid() {
        
        boolean instructorIDIsValid = validationHelper.isValidVarChar(instructorIDInput.getText(), 32);
        boolean instructorFNameIsValid = validationHelper.isValidVarChar(firstNameInput.getText(), 32);
        boolean instructorLNameIsValid = validationHelper.isValidVarChar(lastNameInput.getText(), 32);
        boolean addressStreetIsValid = validationHelper.isValidVarChar(addressInput.getText(), 64);
        boolean addressCityIsValid = validationHelper.isValidVarChar(cityInput.getText(), 32);
        boolean addressStateIsValid = validationHelper.isValidVarChar(stateInput.getText(), 16);
        boolean addressZipIsValid = validationHelper.isValidVarChar(zipInput.getText(), 16);
        boolean homePhoneIsValid = validationHelper.isValidVarChar(homePhoneInput.getText(), 32);
        boolean workPhoneIsValid = validationHelper.isValidVarChar(workPhoneInput.getText(), 32);
        boolean emailIsValid = validationHelper.isValidVarChar(emailInput.getText(), 128);
        boolean notesIsValid = validationHelper.isValidVarChar(notesInput.getText(), 2048);
        boolean titleIsValid = validationHelper.isValidVarChar(titleInput.getText(), 32);
        
        if (!instructorIDIsValid) {
            JOptionPane.showMessageDialog(null, "Instructor ID Max Length is 32 Characters",
                            "Invalid Instructor ID", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!instructorFNameIsValid) {
            JOptionPane.showMessageDialog(null, "First Name Max Length is 32 Characters",
                            "Invalid First Name", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!instructorLNameIsValid) {
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
        } else if (!homePhoneIsValid) {
            JOptionPane.showMessageDialog(null, "Home Phone Max Length is 32 Characters",
                            "Invalid Home Phone", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!workPhoneIsValid) {
            JOptionPane.showMessageDialog(null, "Work Phone Max Length is 32 Characters",
                            "Invalid Work Phone", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!emailIsValid) {
            JOptionPane.showMessageDialog(null, "Email Max Length is 128 Characters",
                            "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!notesIsValid) {
            JOptionPane.showMessageDialog(null, "Notes Max Length is 2048 Characters",
                            "Invalid Notes", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!titleIsValid) {
            JOptionPane.showMessageDialog(null, "Title Max Length is 32 Characters",
                            "Invalid Title", JOptionPane.WARNING_MESSAGE);
            return false;
        } 
        
        return true;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        saveInstructorBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        prioritySelect = new javax.swing.JComboBox();
        fullTimeCheck = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        zipInput = new javax.swing.JTextField();
        notesInput = new javax.swing.JTextField();
        lastNameInput = new javax.swing.JTextField();
        instructorIDInput = new javax.swing.JTextField();
        addressInput = new javax.swing.JTextField();
        cityInput = new javax.swing.JTextField();
        stateInput = new javax.swing.JTextField();
        firstNameInput = new javax.swing.JTextField();
        titleInput = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        emailInput = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        hireDateChooser = new com.toedter.calendar.JDateChooser();
        birthDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel27 = new javax.swing.JLabel();
        activeCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edit Instructor"));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Instructor ID:");
        mainPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 10));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Address:");
        mainPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 10));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("City:");
        mainPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 10));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Hire Date:");
        mainPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, 10));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Zip:");
        mainPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 180, -1, 10));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Home Phone:");
        mainPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 30, -1, 10));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Work Phone:");
        mainPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 60, -1, 10));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Title:");
        mainPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 120, 40, 10));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Priority:");
        mainPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 150, -1, 10));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Full-Time:");
        mainPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 180, -1, 10));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("First Name:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Last Name:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 10));

        saveInstructorBtn.setText("Save");
        saveInstructorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveInstructorBtnActionPerformed(evt);
            }
        });
        mainPanel.add(saveInstructorBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 290, 110, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 290, 100, 40));

        prioritySelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25" }));
        mainPanel.add(prioritySelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 147, 160, 20));
        mainPanel.add(fullTimeCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 170, -1, 30));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Notes:");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 240, 40, 10));
        mainPanel.add(zipInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 80, 30));
        mainPanel.add(notesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 230, 190, 30));
        mainPanel.add(lastNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 50, 190, 30));
        mainPanel.add(instructorIDInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 190, 30));
        mainPanel.add(addressInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 190, 30));
        mainPanel.add(cityInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 190, 30));
        mainPanel.add(stateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 70, 30));
        mainPanel.add(firstNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 20, 190, 30));
        mainPanel.add(titleInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 110, 190, 30));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Email:");
        mainPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 90, 40, 10));
        mainPanel.add(emailInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 80, 190, 30));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("State:");
        mainPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 10));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setText("Birth Date:");
        mainPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 10));
        mainPanel.add(hireDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 230, 190, -1));
        mainPanel.add(birthDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 200, 190, -1));

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setText("Active:");
        mainPanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 210, -1, 10));
        mainPanel.add(activeCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 200, -1, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 601, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveInstructorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveInstructorBtnActionPerformed
        // Add new instructor to the database
        try {
            updateInstructorInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveInstructorBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Close this and reopen instructor manager
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

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
            java.util.logging.Logger.getLogger(EditInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditInstructor dialog = new EditInstructor(new javax.swing.JFrame(), true, instructorIDStatic);
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
    private javax.swing.JCheckBox activeCheck;
    private javax.swing.JTextField addressInput;
    private com.toedter.calendar.JDateChooser birthDateChooser;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField cityInput;
    private javax.swing.JTextField emailInput;
    private javax.swing.JTextField firstNameInput;
    private javax.swing.JCheckBox fullTimeCheck;
    private com.toedter.calendar.JDateChooser hireDateChooser;
    private javax.swing.JTextField instructorIDInput;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField lastNameInput;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField notesInput;
    private javax.swing.JComboBox prioritySelect;
    private javax.swing.JButton saveInstructorBtn;
    private javax.swing.JTextField stateInput;
    private javax.swing.JTextField titleInput;
    private javax.swing.JTextField zipInput;
    // End of variables declaration//GEN-END:variables
}
