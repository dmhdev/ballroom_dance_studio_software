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
public class AddInstructor extends javax.swing.JDialog {

    java.awt.Frame parent;
    private JFormattedTextField homePhoneInput, workPhoneInput;
    private ValidationHelper validationHelper = new ValidationHelper();

    /**
     * Creates new form AddStudent
     */
    public AddInstructor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Add New Instructor");
        initComponents();

        // Set Fields
        try {
            setFields();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setFields() throws ParseException {

        // Format and add phone inputs
        MaskFormatter phoneFormatter = new MaskFormatter("(###) ###-####");

        homePhoneInput = new JFormattedTextField(phoneFormatter);
        workPhoneInput = new JFormattedTextField(phoneFormatter);
        mainPanel.add(homePhoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 190, 30));
        mainPanel.add(workPhoneInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 50, 190, 30));

        // Set Custom Traversal Policy
        setCustomTraversalPolicy();

        // Set submit button
        getRootPane().setDefaultButton(saveInstructorBtn);

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
        componentArrayList.add(notesInput);
        componentArrayList.add(saveInstructorBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    // Connects to DB and adds student
    public void addInstructorToDatabase() throws ClassNotFoundException, SQLException {

        // Verify input is filled in
        if (firstNameInput.getText().equals("")
                || lastNameInput.getText().equals("")
                || instructorIDInput.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nFirst Name\nLast Name\nInstructor ID",
                    "Fill required Fields", JOptionPane.WARNING_MESSAGE);
        } else if (allInputsValid()) {

            // Get variables from input
            String firstName = StringEscapeUtils.escapeSql(firstNameInput.getText());
            String lastName = StringEscapeUtils.escapeSql(lastNameInput.getText());
            String instructorID = StringEscapeUtils.escapeSql(instructorIDInput.getText());
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
            if (fullTimeCheck.isSelected()) {
                fullTime = "TRUE";
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

            // Add dates
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String birthDate, hireDate;
            try {
                birthDate = new String(dateFormat.format(birthDateChooser.getDate()));
            } catch (Exception e) {
                birthDate = "2020-01-01";
            }
            try {
                hireDate = new String(dateFormat.format(hireDateChooser.getDate()));
            } catch (Exception e) {
                hireDate = "2020-01-01";
            }

            try {

                // First check if instructor id has been used before
                ResultSet resultSet = connection.prepareStatement(
                        "select * from Instructors where InstructorID='" + instructorID + "';").executeQuery();

                // If not, create new instructor
                if (!resultSet.next()) {


                        // Insert new Instructor in Database
                        String insertStatement = String.format(
                                "Insert into Instructors(InstructorID,LName,FName,Address,City,State,ZipCode,"
                                + "Title,BirthDate,HireDate,HomePhone,WorkPhone,Email,Notes,FullTime,SchedulePriority,Active)"
                                + " Values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%b,%d,TRUE);",
                                instructorID, lastName, firstName, address, city, state, zip, title, birthDate, hireDate, homePhone, 
                                workPhone, email, notes, fullTime, priority);

                        // Prepare and execute insert statement
                        connection.prepareStatement(insertStatement).execute();

                        // Alert success and close
                        JOptionPane.showMessageDialog(null, "Successfully added new instructor: " + firstName + " " + lastName,
                                "Added New Instructor", JOptionPane.INFORMATION_MESSAGE);
                        
                    
                } else {

                    // Notify already instructor with ID
                    JOptionPane.showMessageDialog(null, "That Instructor ID already exists. Please choose a different ID.",
                            "Duplicate Instructor ID", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Close this and reopen instructor manager
            InstructorManager instructorManager = new InstructorManager();
            instructorManager.setVisible(true);
            parent.dispose();
            this.dispose();

        } 
    }

    public boolean allInputsValid() {
        
        boolean instructorIDIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(instructorIDInput.getText()), 32);
        boolean instructorFNameIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(firstNameInput.getText()), 32);
        boolean instructorLNameIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(lastNameInput.getText()), 32);
        boolean addressStreetIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(addressInput.getText()), 64);
        boolean addressCityIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(cityInput.getText()), 32);
        boolean addressStateIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(stateInput.getText()), 16);
        boolean addressZipIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(zipInput.getText()), 16);
        boolean homePhoneIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(homePhoneInput.getText()), 32);
        boolean workPhoneIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(workPhoneInput.getText()), 32);
        boolean emailIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(emailInput.getText()), 128);
        boolean notesIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(notesInput.getText()), 2048);
        boolean titleIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(titleInput.getText()), 32);
        
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Add New Instructor"));
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
        mainPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 176, -1, -1));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("First Name:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Last Name:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 10));

        saveInstructorBtn.setText("Add Instructor");
        saveInstructorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveInstructorBtnActionPerformed(evt);
            }
        });
        mainPanel.add(saveInstructorBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 260, 150, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 260, 110, 40));

        prioritySelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25" }));
        mainPanel.add(prioritySelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 147, 160, 20));
        mainPanel.add(fullTimeCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 170, -1, 30));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Notes:");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 210, 40, 10));
        mainPanel.add(zipInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 170, 80, 30));
        mainPanel.add(notesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 200, 190, 30));
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
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveInstructorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveInstructorBtnActionPerformed
        // Add new instructor to the database
        try {
            addInstructorToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveInstructorBtnActionPerformed

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
            java.util.logging.Logger.getLogger(AddInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddInstructor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddInstructor dialog = new AddInstructor(new javax.swing.JFrame(), true);
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
