/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.program_enrollment;

import commons.entities.Enrollment;
import static commons.helpers.ServerHelper.connection;
import commons.helpers.ComboBoxHelper;
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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import commons.helpers.CustomFocusTraversalPolicy;
import views.student.StudentDetails;

/**
 *
 * @author Akureyri
 */
public class EditProgramEnrollment extends javax.swing.JDialog {

    ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    GenericHelper genericHelper = new GenericHelper();
    private static String enrollmentIDStatic;
    private String enrollmentID;
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    java.awt.Frame parent;

    /**
     * Creates new form AddStudent
     */
    public EditProgramEnrollment(java.awt.Frame parent, String enrollmentIDStatic) {
        super(parent, true);

        this.enrollmentID = enrollmentIDStatic;
        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Edit Program Enrollment");
        initComponents();

        // Set field values for student
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set field values for student
    private void setFields() throws ClassNotFoundException, SQLException, ParseException, InterruptedException {

        // Create arraylist of instructors and apply to combobox
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, primaryInstructorSelect, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, buddyInstructorSelect, true);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, buddyInstructorSelect2, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Set Program enrollment values
                    setEnrollmentValues();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        thread1.start();
        thread1.join();

        // Set Tab Traversal Policy Order
        setCustomTraversalPolicy();

        // Set Money Calculation Listeners
        setMoneyAutoCalculate();

        // Set submit button
        getRootPane().setDefaultButton(saveEnrollmentBtn);

    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(primaryInstructorSelect);
        componentArrayList.add(buddyInstructorSelect);
        componentArrayList.add(buddyInstructorSelect2);
        componentArrayList.add(privatePriceInput);
        componentArrayList.add(groupPriceInput);
        componentArrayList.add(partyPriceInput);
        componentArrayList.add(privateTotalInput);
        componentArrayList.add(privateAttendedInput);
        componentArrayList.add(groupTotalInput);
        componentArrayList.add(groupAttendedInput);
        componentArrayList.add(partyTotalInput);
        componentArrayList.add(partyAttendedInput);
        componentArrayList.add(programGroupSelect);
        componentArrayList.add(saveEnrollmentBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    // Set Program enrollment values
    private void setEnrollmentValues() throws ClassNotFoundException, SQLException, ParseException {

        try {

            // Get instructor data with id
            ResultSet resultSet = connection.prepareStatement(
                    "select ContractTotal,ContractPaid,PrimaryInstructorID,InstructorID1,InstructorID2,PrivateLessonTotal,GroupLessonTotal,PartyLessonTotal,"
                    + "PrivateLessonAttended,GroupLessonAttended,PartyLessonAttended,PrivateLessonPrice,GroupLessonPrice,PartyLessonPrice,ProgramGroup,DateCreated "
                    + "from ProgramEnrollment where EnrollmentID='"
                    + enrollmentID + "';").executeQuery();

            if (resultSet.next()) {

                // Set text fields with enrollment data
                contractTotalInput.setText(resultSet.getString(1));
                contractPaidInput.setText(resultSet.getString(2));
                privateTotalInput.setText(resultSet.getString(6));
                groupTotalInput.setText(resultSet.getString(7));
                partyTotalInput.setText(resultSet.getString(8));
                privateAttendedInput.setText(resultSet.getString(9));
                groupAttendedInput.setText(resultSet.getString(10));
                partyAttendedInput.setText(resultSet.getString(11));
                privatePriceInput.setText(resultSet.getString(12));
                groupPriceInput.setText(resultSet.getString(13));
                partyPriceInput.setText(resultSet.getString(14));
                programGroupSelect.setSelectedItem(resultSet.getString(15));

                // Set Dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date enrollmentDate = format.parse(resultSet.getString(16));
                enrollmentDateChooser.setDate(enrollmentDate);

                // Set Instructor comobobox to correct value
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, primaryInstructorSelect, resultSet.getString(3), false);
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, buddyInstructorSelect, resultSet.getString(4), true);
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, buddyInstructorSelect2, resultSet.getString(5), true);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Connects to DB and updates instructor
    public void updateEnrollmentInfo() throws ClassNotFoundException, SQLException {

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really edit enrollment?",
                "Confirm Program Enrollment Edit", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Get variables from input, checking if numeric
            double contractTotal = 0, contractPaid = 0, privateTotal = 0, groupTotal = 0, partyTotal = 0, privateAttended = 0, groupAttended = 0,
                    partyAttended = 0, privatePrice = 0, groupPrice = 0, partyPrice = 0;
            if (genericHelper.isNumericString(contractTotalInput.getText())) {
                contractTotal = Double.parseDouble(contractTotalInput.getText());
            }
            if (genericHelper.isNumericString(contractPaidInput.getText())) {
                contractPaid = Double.parseDouble(contractPaidInput.getText());
            }
            if (genericHelper.isNumericString(privateTotalInput.getText())) {
                privateTotal = Double.parseDouble(privateTotalInput.getText());
            }
            if (genericHelper.isNumericString(groupTotalInput.getText())) {
                groupTotal = Double.parseDouble(groupTotalInput.getText());
            }
            if (genericHelper.isNumericString(partyTotalInput.getText())) {
                partyTotal = Double.parseDouble(partyTotalInput.getText());
            }
            if (genericHelper.isNumericString(privateAttendedInput.getText())) {
                privateAttended = Double.parseDouble(privateAttendedInput.getText());
            }
            if (genericHelper.isNumericString(groupAttendedInput.getText())) {
                groupAttended = Double.parseDouble(groupAttendedInput.getText());
            }
            if (genericHelper.isNumericString(partyAttendedInput.getText())) {
                partyAttended = Double.parseDouble(partyAttendedInput.getText());
            }
            if (genericHelper.isNumericString(privatePriceInput.getText())) {
                privatePrice = Double.parseDouble(privatePriceInput.getText());
            }
            if (genericHelper.isNumericString(groupPriceInput.getText())) {
                groupPrice = Double.parseDouble(groupPriceInput.getText());
            }
            if (genericHelper.isNumericString(partyPriceInput.getText())) {
                partyPrice = Double.parseDouble(partyPriceInput.getText());
            }

            // Get instructor IDs
            String primaryInstructorID = comboBoxHelper.getSelectedInstructorID(primaryInstructorSelect, instructorArrayList, false);
            String buddyInstructorID = comboBoxHelper.getSelectedInstructorID(buddyInstructorSelect, instructorArrayList, true);
            String buddyInstructorID2 = comboBoxHelper.getSelectedInstructorID(buddyInstructorSelect2, instructorArrayList, true);

            // Get Program Group
            String programGroup = programGroupSelect.getSelectedItem().toString();

            // Calculate Owes Payment
            double contractOwed = ((privateAttended * privatePrice) + (groupAttended * groupPrice) + (partyAttended * partyPrice));
            boolean owesPayment = false;
            if (contractOwed > contractPaid) {
                owesPayment = true;
            }

            // Add date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String enrollmentDate = dateFormat.format(enrollmentDateChooser.getDate());

            try {

                // Get previous enrollment date
                ResultSet originalEnrollmentData = connection.prepareStatement(String.format("SELECT DateCreated FROM ProgramEnrollment WHERE EnrollmentID='%s';", enrollmentID)).executeQuery();
                if (originalEnrollmentData.next()) {

                    // Check if original enrollment date is different from updated. If so, need to update dates for enrollment, inital payment and bonuses awarded
                    String originalEnrollmentDate = originalEnrollmentData.getString(1);
                    if (originalEnrollmentDate.equals(enrollmentDate)) {

                        // Update only Program Enrollment Values, excluding date created
                        connection.prepareStatement(String.format(
                                "UPDATE ProgramEnrollment SET ProgramGroup='%s',ContractTotal=%f,ContractPaid=%f,PrivateLessonTotal=%f,GroupLessonTotal=%f,PartyLessonTotal=%f,PrivateLessonAttended=%f,"
                                + "GroupLessonAttended=%f,PartyLessonAttended=%f,PrimaryInstructorID='%s',InstructorID1='%s',InstructorID2='%s',PrivateLessonPrice='%s',GroupLessonPrice='%s',"
                                + "PartyLessonPrice='%s', OwesPayment=%b WHERE EnrollmentID='%s';",
                                programGroup, contractTotal, contractPaid, privateTotal, groupTotal, partyTotal, privateAttended, groupAttended, partyAttended, primaryInstructorID, buddyInstructorID,
                                buddyInstructorID2, privatePrice, groupPrice, partyPrice, owesPayment, enrollmentID)).execute();

                    } else {

                        // Update Program Enrollment Values, including date created
                        connection.prepareStatement(String.format(
                                "UPDATE ProgramEnrollment SET ProgramGroup='%s',ContractTotal=%f,ContractPaid=%f,PrivateLessonTotal=%f,GroupLessonTotal=%f,PartyLessonTotal=%f,PrivateLessonAttended=%f,"
                                + "GroupLessonAttended=%f,PartyLessonAttended=%f,PrimaryInstructorID='%s',InstructorID1='%s',InstructorID2='%s',PrivateLessonPrice='%s',GroupLessonPrice='%s',"
                                + "PartyLessonPrice='%s', OwesPayment=%b, DateCreated='%s', DateUpdated='%s' WHERE EnrollmentID='%s';",
                                programGroup, contractTotal, contractPaid, privateTotal, groupTotal, partyTotal, privateAttended, groupAttended, partyAttended, primaryInstructorID, buddyInstructorID,
                                buddyInstructorID2, privatePrice, groupPrice, partyPrice, owesPayment, enrollmentDate, enrollmentDate, enrollmentID)).execute();

                        // Update bonus dates created from enrollment
                        connection.prepareStatement(String.format("UPDATE BonusTransaction SET DateCreated='%s', DateUpdated='%s' WHERE FromEnrollmentID='%s';", 
                                enrollmentDate, enrollmentDate, enrollmentID)).execute();
                        
                        // Update initial payments
                        connection.prepareStatement(String.format("UPDATE PaymentTransaction SET DateCreated='%s', DateUpdated='%s' WHERE FirstPayment=TRUE AND EnrollmentID='%s';", 
                                enrollmentDate, enrollmentDate, enrollmentID)).execute();
                        
                    }

                    // Update last lesson status for all lessons in enrollment
                    Enrollment enrollment = new Enrollment(enrollmentID);
                    enrollment.updateProgramCompleted();

                    // Alert success and close
                    JOptionPane.showMessageDialog(null, "Saved all changes.",
                            "Changes Saved", JOptionPane.INFORMATION_MESSAGE);

                    // Close this and reopen 
                    if (parent.getClass().getSimpleName().equals("StudentDetails")) {
                        StudentDetails studentDetails = new StudentDetails();
                        studentDetails.setVisible(true);
                    } else {

                        // Check for parent of studentProgramEnrollment
                        StudentProgramEnrollment studentProgramEnrollment = new StudentProgramEnrollment();
                        studentProgramEnrollment.setVisible(true);
                    }
                    parent.dispose();
                    this.dispose();

                } else {

                    // Throw error if original enrollment data not found
                    Exception e = new Exception();
                    throw e;
                }

            } catch (Exception e) {

                // Alert failed insert
                JOptionPane.showMessageDialog(null, "Error during update. No changes were saved.",
                        "Error During Update", JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }

    // Set Money Auto Calculators
    private void setMoneyAutoCalculate() {

        // Create list of textfields that require listeners
        JTextField[] moneyInputFields = {privatePriceInput, groupPriceInput, partyPriceInput, privateTotalInput, groupTotalInput, partyTotalInput};

        for (int i = 0; i < moneyInputFields.length; i++) {

            // Add listeners
            moneyInputFields[i].getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateTotalPrice();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateTotalPrice();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateTotalPrice();
                }
            });
        }

    }

    // Updates total price on money input change
    private void updateTotalPrice() {

        // Get variables
        Double privatePrice = Double.parseDouble((genericHelper.isNumericString(privatePriceInput.getText())) ? privatePriceInput.getText() : "0");
        Double groupPrice = Double.parseDouble((genericHelper.isNumericString(groupPriceInput.getText())) ? groupPriceInput.getText() : "0");
        Double partyPrice = Double.parseDouble((genericHelper.isNumericString(partyPriceInput.getText())) ? partyPriceInput.getText() : "0");

        Double privateLessons = Double.parseDouble((genericHelper.isNumericString(privateTotalInput.getText())) ? privateTotalInput.getText() : "0");
        Double groupLessons = Double.parseDouble((genericHelper.isNumericString(groupTotalInput.getText())) ? groupTotalInput.getText() : "0");
        Double partyLessons = Double.parseDouble((genericHelper.isNumericString(partyTotalInput.getText())) ? partyTotalInput.getText() : "0");

        // Calculate total
        Double totalPayment = ((privatePrice * privateLessons) + (groupPrice * groupLessons) + (partyPrice * partyLessons));

        // Set textfield
        contractTotalInput.setText(String.valueOf(totalPayment));
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
        saveEnrollmentBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        buddyInstructorSelect2 = new javax.swing.JComboBox();
        buddyInstructorSelect = new javax.swing.JComboBox();
        primaryInstructorSelect = new javax.swing.JComboBox();
        enrollmentIDLabel = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        contractTotalInput = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        contractPaidInput = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        privateTotalInput = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        privateAttendedInput = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        groupAttendedInput = new javax.swing.JTextField();
        groupTotalInput = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        partyAttendedInput = new javax.swing.JTextField();
        partyTotalInput = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        partyPriceInput = new javax.swing.JTextField();
        groupPriceInput = new javax.swing.JTextField();
        privatePriceInput = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        enrollmentDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel35 = new javax.swing.JLabel();
        programGroupSelect = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edit Program Enrollment"));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        saveEnrollmentBtn.setText("Save");
        saveEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveEnrollmentBtnActionPerformed(evt);
            }
        });
        mainPanel.add(saveEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 290, 110, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 290, 100, 40));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Primary Instructor:");
        mainPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, 20));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Buddy 1:");
        mainPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, -1, 20));

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel34.setText("Buddy 2:");
        mainPanel.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 210, -1, 20));

        mainPanel.add(buddyInstructorSelect2, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 210, 150, 20));

        mainPanel.add(buddyInstructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 150, 20));

        mainPanel.add(primaryInstructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 150, 150, 20));

        enrollmentIDLabel.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        enrollmentIDLabel.setForeground(java.awt.Color.blue);
        mainPanel.add(enrollmentIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 60, 10));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Contract Total:");
        mainPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, 20));

        contractTotalInput.setEditable(false);
        mainPanel.add(contractTotalInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 90, 150, 20));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Contract Paid:");
        mainPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, 20));

        contractPaidInput.setEditable(false);
        mainPanel.add(contractPaidInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, 150, 20));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Private Total:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, -1, 20));
        mainPanel.add(privateTotalInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 60, 150, 20));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Private Attended:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, -1, 20));
        mainPanel.add(privateAttendedInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 90, 150, 20));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Group Total:");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 120, -1, 20));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Group Attended:");
        mainPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 150, -1, 20));
        mainPanel.add(groupAttendedInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 150, 150, 20));
        mainPanel.add(groupTotalInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 120, 150, 20));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Party Total:");
        mainPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 180, -1, 20));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setText("Party Attended:");
        mainPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 210, -1, 20));
        mainPanel.add(partyAttendedInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 210, 150, 20));
        mainPanel.add(partyTotalInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 180, 150, 20));

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setText("Private Price:");
        mainPanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, -1, 20));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Group Price:");
        mainPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, -1, 20));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setText("Party Price:");
        mainPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 300, -1, 20));
        mainPanel.add(partyPriceInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 300, 150, 20));
        mainPanel.add(groupPriceInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 270, 150, 20));
        mainPanel.add(privatePriceInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 240, 150, 20));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Date Enrolled:");
        mainPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 20));
        mainPanel.add(enrollmentDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 160, 30));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setText("Program Group:");
        mainPanel.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 240, -1, 20));

        programGroupSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Original", "Preview", "Extension", "ReExtension", "Renewal", "Other" }));
        mainPanel.add(programGroupSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 240, 150, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 642, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveEnrollmentBtnActionPerformed
        // Add new instructor to the database
        try {
            updateEnrollmentInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveEnrollmentBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Close this 
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
            java.util.logging.Logger.getLogger(EditProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditProgramEnrollment dialog = new EditProgramEnrollment(new javax.swing.JFrame(), enrollmentIDStatic);
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
    private javax.swing.JComboBox buddyInstructorSelect;
    private javax.swing.JComboBox buddyInstructorSelect2;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField contractPaidInput;
    private javax.swing.JTextField contractTotalInput;
    private com.toedter.calendar.JDateChooser enrollmentDateChooser;
    private javax.swing.JLabel enrollmentIDLabel;
    private javax.swing.JTextField groupAttendedInput;
    private javax.swing.JTextField groupPriceInput;
    private javax.swing.JTextField groupTotalInput;
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
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField partyAttendedInput;
    private javax.swing.JTextField partyPriceInput;
    private javax.swing.JTextField partyTotalInput;
    private javax.swing.JComboBox primaryInstructorSelect;
    private javax.swing.JTextField privateAttendedInput;
    private javax.swing.JTextField privatePriceInput;
    private javax.swing.JTextField privateTotalInput;
    private javax.swing.JComboBox programGroupSelect;
    private javax.swing.JButton saveEnrollmentBtn;
    // End of variables declaration//GEN-END:variables
}
