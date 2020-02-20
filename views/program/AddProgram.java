/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.program;

import commons.helpers.GenericHelper;
import static commons.helpers.ServerHelper.connection;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import commons.helpers.ValidationHelper;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Akureyri
 */
public class AddProgram extends javax.swing.JDialog {

    GenericHelper genericHelper = new GenericHelper();
    ValidationHelper validationHelper = new ValidationHelper();
    java.awt.Frame parent;

    /**
     * Creates new form AddStudent
     */
    public AddProgram(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Add New Program");
        initComponents();

        // Set Fields
        setFields();

    }

    // Set Fields
    private void setFields() {

        // Set Custom Traversal Policy
        setCustomTraversalPolicy();

        // Set submit button
        getRootPane().setDefaultButton(addProgramBtn);

    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(programNameInput);
        componentArrayList.add(programIDInput);
        componentArrayList.add(privateRateInput);
        componentArrayList.add(groupRateInput);
        componentArrayList.add(partyRateInput);
        componentArrayList.add(privateBonusInput);
        componentArrayList.add(groupBonusInput);
        componentArrayList.add(partyBonusInput);
        componentArrayList.add(unlimitedLessonsCheck);
        componentArrayList.add(privateLessonsInput);
        componentArrayList.add(groupLessonsInput);
        componentArrayList.add(partyLessonsInput);
        componentArrayList.add(programGroupSelect);
        componentArrayList.add(programDescriptionInput);
        componentArrayList.add(addProgramBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    // Connects to DB and adds student
    public void addProgramToDatabase() throws ClassNotFoundException, SQLException {

        // Verify input is filled in
        if (programNameInput.getText().equals("")
                || programIDInput.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill in required fields:\n\nProgram Name\nProgram ID",
                    "Fill required Fields", JOptionPane.WARNING_MESSAGE);
        } else if (allInputsValid()) {

            // Get variables from input
            String programName = StringEscapeUtils.escapeSql(programNameInput.getText());
            String programID = StringEscapeUtils.escapeSql(programIDInput.getText());
            String privateRateStr = StringEscapeUtils.escapeSql(privateRateInput.getText());
            String groupRateStr = StringEscapeUtils.escapeSql(groupRateInput.getText());
            String partyRateStr = StringEscapeUtils.escapeSql(partyRateInput.getText());
            String privateBonusStr = StringEscapeUtils.escapeSql(privateBonusInput.getText());
            String groupBonusStr = StringEscapeUtils.escapeSql(groupBonusInput.getText());
            String partyBonusStr = StringEscapeUtils.escapeSql(partyBonusInput.getText());
            String privateLessonsStr = StringEscapeUtils.escapeSql(privateLessonsInput.getText());
            String groupLessonsStr = StringEscapeUtils.escapeSql(groupLessonsInput.getText());
            String partyLessonsStr = StringEscapeUtils.escapeSql(partyLessonsInput.getText());
            String programGroup = programGroupSelect.getSelectedItem().toString();
            String programDescription = StringEscapeUtils.escapeSql(programDescriptionInput.getText());
            String unlimitedLessons = "FALSE";

            if (unlimitedLessonsCheck.isSelected()) {
                unlimitedLessons = "TRUE";
            }

            // Check if rates are numeric, if not leave at 0
            double privateRate = 0, groupRate = 0, partyRate = 0,
                    privateBonus = 0, groupBonus = 0, partyBonus = 0,
                    privateLessons = 0, groupLessons = 0, partyLessons = 0;
            if (genericHelper.isNumericString(privateRateStr)) {
                privateRate = Double.parseDouble(privateRateStr);
            }
            if (genericHelper.isNumericString(groupRateStr)) {
                groupRate = Double.parseDouble(groupRateStr);
            }
            if (genericHelper.isNumericString(partyRateStr)) {
                partyRate = Double.parseDouble(partyRateStr);
            }
            if (genericHelper.isNumericString(privateBonusStr)) {
                privateBonus = Double.parseDouble(privateBonusStr);
            }
            if (genericHelper.isNumericString(groupBonusStr)) {
                groupBonus = Double.parseDouble(groupBonusStr);
            }
            if (genericHelper.isNumericString(partyBonusStr)) {
                partyBonus = Double.parseDouble(partyBonusStr);
            }
            if (genericHelper.isNumericString(privateLessonsStr)) {
                privateLessons = Double.parseDouble(privateLessonsStr);
            }
            if (genericHelper.isNumericString(groupLessonsStr)) {
                groupLessons = Double.parseDouble(groupLessonsStr);
            }
            if (genericHelper.isNumericString(partyLessonsStr)) {
                partyLessons = Double.parseDouble(partyLessonsStr);
            }

            try {

                // First check if program id has been used before
                ResultSet resultSet = connection.prepareStatement(
                        "select * from Programs where ProgramID='" + programID + "';").executeQuery();

                // If not, create new Program
                if (!resultSet.next()) {

                    // Insert new Program in Database
                    String insertStatement = String.format(
                            "Insert into Programs(ProgramID,ProgramName,ProgramDescription,ProgramGroup,RatePrivate,RateGroup,RateParty,DefaultBonusesAwardedPrivate,"
                            + " DefaultBonusesAwardedGroup, DefaultBonusesAwardedParty, DefaultLessonsPrivate, DefaultLessonsGroup, DefaultLessonsParty, UnlimitedLessons)"
                            + " Values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s',%b);",
                            programID, programName, programDescription, programGroup, privateRate, groupRate, partyRate, privateBonus, groupBonus, partyBonus, 
                            privateLessons, groupLessons, partyLessons, Boolean.parseBoolean(unlimitedLessons) );

                    // Prepare and execute insert statement
                    connection.prepareStatement(insertStatement).execute();

                    // Alert success and close
                    JOptionPane.showMessageDialog(null, "Successfully added new program: " + programName,
                            "Added New Program", JOptionPane.INFORMATION_MESSAGE);

                } else {

                    // Notify already Program with ID
                    JOptionPane.showMessageDialog(null, "That Program ID already exists. Please choose a different ID.",
                            "Duplicate Program ID", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Close this and reopen program manager
            ProgramManager programManager = new ProgramManager();
            programManager.setVisible(true);
            parent.dispose();
            this.dispose();

        }
    }

    public boolean allInputsValid() {
        
        boolean programIDIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(programIDInput.getText()), 32);
        boolean programNameIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(programNameInput.getText()), 64);
        boolean programDescIsValid = validationHelper.isValidVarChar(StringEscapeUtils.escapeSql(programDescriptionInput.getText()), 512);
        boolean privateRateIsValid = validationHelper.isValidNumeric(privateRateInput.getText());
        boolean groupRateIsValid = validationHelper.isValidNumeric(groupRateInput.getText());
        boolean partyRateIsValid = validationHelper.isValidNumeric(partyRateInput.getText());
        boolean privateBonusIsValid = validationHelper.isValidNumeric(privateBonusInput.getText());
        boolean groupBonusIsValid = validationHelper.isValidNumeric(groupBonusInput.getText());
        boolean partyBonusIsValid = validationHelper.isValidNumeric(partyBonusInput.getText());
        boolean privateLessonsIsValid = validationHelper.isValidNumeric(privateLessonsInput.getText());
        boolean groupLessonsIsValid = validationHelper.isValidNumeric(groupLessonsInput.getText());
        boolean partyLessonsIsValid = validationHelper.isValidNumeric(partyLessonsInput.getText());
        
        if (!programIDIsValid) {
            JOptionPane.showMessageDialog(null, "Program ID Max Length is 32 Characters",
                            "Invalid Program ID", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!programNameIsValid) {
            JOptionPane.showMessageDialog(null, "Program Name Max Length is 64 Characters",
                            "Invalid Program Name", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!programDescIsValid) {
            JOptionPane.showMessageDialog(null, "Program Description Max Length is 512 Characters",
                            "Invalid Program Description", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!privateRateIsValid) {
            JOptionPane.showMessageDialog(null, "Private Rate is not a valid number.",
                            "Invalid Private Rate", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!groupRateIsValid) {
            JOptionPane.showMessageDialog(null, "Group Rate is not a valid number.",
                            "Invalid Group Rate", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!partyRateIsValid) {
            JOptionPane.showMessageDialog(null, "Party Rate is not a valid number.",
                            "Invalid Party Rate", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!privateBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Private Bonus is not a valid number.",
                            "Invalid Private Rate", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!groupBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Group Bonus is not a valid number.",
                            "Invalid Group Rate", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!partyBonusIsValid) {
            JOptionPane.showMessageDialog(null, "Party Bonus is not a valid number.",
                            "Invalid Party Bonus", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!privateLessonsIsValid) {
            JOptionPane.showMessageDialog(null, "Private Lessons is not a valid number.",
                            "Invalid Private Lessons", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!groupLessonsIsValid) {
            JOptionPane.showMessageDialog(null, "Group Lessons is not a valid number.",
                            "Invalid Group Lessons", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!partyLessonsIsValid) {
            JOptionPane.showMessageDialog(null, "Party Lessons is not a valid number.",
                            "Invalid Party Lessons", JOptionPane.WARNING_MESSAGE);
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
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        addProgramBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        programIDInput = new javax.swing.JTextField();
        privateRateInput = new javax.swing.JTextField();
        groupRateInput = new javax.swing.JTextField();
        partyRateInput = new javax.swing.JTextField();
        programNameInput = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        programDescriptionInput = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        partyBonusInput = new javax.swing.JTextField();
        groupBonusInput = new javax.swing.JTextField();
        privateBonusInput = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        partyLessonsInput = new javax.swing.JTextField();
        groupLessonsInput = new javax.swing.JTextField();
        privateLessonsInput = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        programGroupSelect = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        unlimitedLessonsCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Add Program"));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Private Rate:");
        mainPanel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 10));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Group Rate:");
        mainPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 10));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("Party Rate:");
        mainPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 10));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Program Name:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Program ID:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 10));

        addProgramBtn.setText("Add Program");
        addProgramBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProgramBtnActionPerformed(evt);
            }
        });
        mainPanel.add(addProgramBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 300, 140, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 300, 100, 40));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Description:");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 150, 70, 10));
        mainPanel.add(programIDInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, 190, 30));
        mainPanel.add(privateRateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 190, 30));
        mainPanel.add(groupRateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 110, 190, 30));
        mainPanel.add(partyRateInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 140, 190, 30));
        mainPanel.add(programNameInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 190, 30));

        programDescriptionInput.setColumns(20);
        programDescriptionInput.setLineWrap(true);
        programDescriptionInput.setRows(5);
        programDescriptionInput.setTabSize(4);
        jScrollPane2.setViewportView(programDescriptionInput);

        mainPanel.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 150, 190, 110));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Private Bonus:");
        mainPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 10));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setText("Group Bonus:");
        mainPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 10));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText("Party Bonus:");
        mainPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, 10));
        mainPanel.add(partyBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 230, 190, 30));
        mainPanel.add(groupBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 200, 190, 30));
        mainPanel.add(privateBonusInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 170, 190, 30));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Private Lessons:");
        mainPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 30, -1, 10));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Group Lessons:");
        mainPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 60, -1, 10));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Party Lessons:");
        mainPanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 90, -1, 10));
        mainPanel.add(partyLessonsInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, 190, 30));
        mainPanel.add(groupLessonsInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 50, 190, 30));
        mainPanel.add(privateLessonsInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 190, 30));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Program Group:");
        mainPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 120, -1, 10));

        programGroupSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Original", "Preliminary", "Extension", "ReExtension", "Renewal", "Other" }));
        mainPanel.add(programGroupSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 110, 190, 30));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Unlimited Lessons:");
        mainPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 110, 20));
        mainPanel.add(unlimitedLessonsCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 270, -1, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addProgramBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProgramBtnActionPerformed
        // Add new Program to the database
        try {
            addProgramToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_addProgramBtnActionPerformed

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
            java.util.logging.Logger.getLogger(AddProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddProgram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddProgram dialog = new AddProgram(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton addProgramBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField groupBonusInput;
    private javax.swing.JTextField groupLessonsInput;
    private javax.swing.JTextField groupRateInput;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField partyBonusInput;
    private javax.swing.JTextField partyLessonsInput;
    private javax.swing.JTextField partyRateInput;
    private javax.swing.JTextField privateBonusInput;
    private javax.swing.JTextField privateLessonsInput;
    private javax.swing.JTextField privateRateInput;
    private javax.swing.JTextArea programDescriptionInput;
    private javax.swing.JComboBox programGroupSelect;
    private javax.swing.JTextField programIDInput;
    private javax.swing.JTextField programNameInput;
    private javax.swing.JCheckBox unlimitedLessonsCheck;
    // End of variables declaration//GEN-END:variables
}
