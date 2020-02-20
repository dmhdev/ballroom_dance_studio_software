/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.lesson;

import commons.helpers.ComboBoxHelper;
import static commons.helpers.ServerHelper.connection;
import views.student.StudentDetails;
import views.student.StudentAttendLesson;
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
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import views.schedule.LessonSchedule;
import views.student.StudentAttendLesson;
import views.student.StudentDetails;
import static commons.helpers.NavHelper.studentID;
import static commons.helpers.NavHelper.studentType;

/**
 *
 * @author Akureyri
 */
public class EditLesson extends javax.swing.JDialog {

    ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private StudentAttendLesson attendPurchaseLesson;
    private LessonSchedule lessonSchedule;
    private static String lessonIDStatic, studentIDStatic, parentClassStatic, studentTypeStatic;
    private String lessonID, parentClass;
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    private static java.awt.Frame grandparentStatic;
    java.awt.Frame parent;

    /**
     * Creates new form AddStudent
     */
    public EditLesson(String lessonIDStatic, java.awt.Frame parent, String parentClassStatic) {
        super(parent, true);

        this.lessonID = lessonIDStatic;
        this.parentClass = parentClassStatic;
        this.parent = parent;

        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Edit Lesson");
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

        // Set Lesson Values
        setLessonValues();

        // Set Custom Traversal Policy
        setCustomTraversalPolicy();

        // Set submit button
        getRootPane().setDefaultButton(saveLessonBtn);

    }

    // Set Lesson Values
    private void setLessonValues() throws ClassNotFoundException, SQLException, ParseException {

        // Create arraylist of instructors and apply to combobox
        comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect, false);

        // Connect to database and get Instructor data
        try {

            // Get lesson data with id
            ResultSet resultSet = connection.prepareStatement(
                    "select AppointmentDate,AppointmentTimeStart,AppointmentTimeEnd,Notes,RateType,LessonUnits,InstructorID,LessonCode,AmountDue,LessonStatus"
                    + " from LessonSchedule where LessonID='" + lessonID + "';").executeQuery();

            if (resultSet.next()) {

                // Get variables
                String appointmentDate = resultSet.getString(1);
                String appointmentTimeStart = genericHelper.formatTimeToHHMM(resultSet.getString(2));
                String appointmentTimeEnd = genericHelper.formatTimeToHHMM(resultSet.getString(3));
                String notes = resultSet.getString(4);
                String rateType = resultSet.getString(5);
                String lessonUnits = String.valueOf(resultSet.getDouble(6));
                String instructorID = resultSet.getString(7);
                String lessonCode = resultSet.getString(8);
                String amountDue = resultSet.getString(9);
                String lessonStatus = resultSet.getString(10);
                

                // Set text fields with instructor data
                notesInput.setText(notes);
                codeInput.setText(lessonCode);
                amountDueInput.setText(amountDue);
                lessonUnitInput.setText(lessonUnits);
                lessonTypeSelect.setSelectedItem(rateType);

                // Set Dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date lessonDate = format.parse(appointmentDate);
                lessonDateChooser.setDate(lessonDate);

                // Split time fields
                String appointmentTimeStartHour = appointmentTimeStart.substring(0, 2);
                String appointmentTimeStartMinute = appointmentTimeStart.substring(3);
                String appointmentTimeEndHour = appointmentTimeEnd.substring(0, 2);
                String appointmentTimeEndMinute = appointmentTimeEnd.substring(3);

                // Set time fields
                startTimeHourSelect.setSelectedItem(appointmentTimeStartHour);
                startTimeMinuteSelect.setSelectedItem(appointmentTimeStartMinute);
                endTimeHourSelect.setSelectedItem(appointmentTimeEndHour);
                endTimeMinuteSelect.setSelectedItem(appointmentTimeEndMinute);

                // Set Instructor comobobox to correct value
                comboBoxHelper.setSelectedComboBoxInstructor(instructorArrayList, instructorSelect, instructorID, false);

                // Set lesson id name
                lessonIDLabel.setText("Lesson ID: " + lessonID);
                
                // Disable change of units and lesson type if already attended
                if (lessonStatus.equals("Attended")) {
                    lessonUnitInput.setEditable(false);
                    lessonTypeSelect.setEnabled(false);
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
        componentArrayList.add(lessonDateChooser);
        componentArrayList.add(startTimeHourSelect);
        componentArrayList.add(startTimeMinuteSelect);
        componentArrayList.add(endTimeHourSelect);
        componentArrayList.add(endTimeMinuteSelect);
        componentArrayList.add(codeInput);
        componentArrayList.add(notesInput);
        componentArrayList.add(amountDueInput);
        componentArrayList.add(lessonTypeSelect);
        componentArrayList.add(lessonUnitInput);
        componentArrayList.add(instructorSelect);
        componentArrayList.add(saveLessonBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        mainPanel.setFocusTraversalPolicyProvider(true);
        mainPanel.setFocusTraversalPolicy(policy);

    }

    // Connects to DB and updates lesson
    public void updatedLessonInfo() throws ClassNotFoundException, SQLException {

        // Get variables from input
        String lessonType = lessonTypeSelect.getSelectedItem().toString();
        String notes = StringEscapeUtils.escapeSql(notesInput.getText());
        String lessonCode = StringEscapeUtils.escapeSql(codeInput.getText());
        String appointmentStartTime = startTimeHourSelect.getSelectedItem().toString() + ":" + startTimeMinuteSelect.getSelectedItem().toString() + ":00";
        String appointmentEndTime = endTimeHourSelect.getSelectedItem().toString() + ":" + endTimeMinuteSelect.getSelectedItem().toString() + ":00";

        // Check if valid amount due
        double amountDue = 0.0;
        if (genericHelper.isNumericString(amountDueInput.getText())) {
            amountDue = Double.parseDouble(amountDueInput.getText());
        }

        // Check if valid lesson units
        double units = 0;
        if (genericHelper.isNumericString(lessonUnitInput.getText())) {
            units = Double.parseDouble(lessonUnitInput.getText());
        } else {
            JOptionPane.showMessageDialog(null, "Lesson Unit input is not numeric. Setting units to 0.",
                    "Lesson Units Not Numeric", JOptionPane.WARNING_MESSAGE);
        }

        // Add date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String lessonDate = dateFormat.format(lessonDateChooser.getDate());

        // Get instructor ID and Name
        String instructorName = instructorSelect.getSelectedItem().toString();
        String instructorID = comboBoxHelper.getSelectedInstructorID(instructorSelect, instructorArrayList, false);

        try {

            // Get Instructor Priority
            ResultSet instructorSet = connection.prepareStatement(String.format("select SchedulePriority from Instructors where InstructorID='%s';", instructorID)).executeQuery();
            instructorSet.next();
            int instructorPriority = instructorSet.getInt(1);

            // Edit Lesson Schedule in Database
            String insertStatement = String.format(
                    "UPDATE LessonSchedule SET RateType='%s',Notes='%s',LessonUnits=%f,AppointmentTimeStart='%s',AppointmentTimeEnd='%s',AppointmentDate='%s',"
                    + "InstructorName='%s',InstructorID='%s', LessonCode='%s', InstructorPriority='%s', AmountDue=%f WHERE LessonID='%s';",
                    lessonType, notes, units, appointmentStartTime, appointmentEndTime, lessonDate, instructorName, instructorID, lessonCode, instructorPriority, amountDue, lessonID);

            // Prepare and execute insert statement
            connection.prepareStatement(insertStatement).execute();

            // Alert success and close
            JOptionPane.showMessageDialog(null, "Saved all changes.",
                    "Changes Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {

            // Alert failed insert
            JOptionPane.showMessageDialog(null, "Error during update. No changes were saved.",
                    "Error During Update", JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
        
        // Close this and reopen parent
        if (parent.getClass().getName() == "views.student.StudentDetails") {

            StudentDetails studentDetails = new StudentDetails();
            studentDetails.setVisible(true);

        } else if (parent.getClass().getName() == "views.student.StudentAttendLesson") {

            attendPurchaseLesson = new StudentAttendLesson();
            attendPurchaseLesson.setVisible(true);

        } else {

            // Came from Lesson Schedule
            lessonSchedule = new LessonSchedule();
            lessonSchedule.setVisible(true);

        }
        this.dispose();
        parent.dispose();

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
        saveLessonBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        lessonDateChooser = new com.toedter.calendar.JDateChooser();
        startTimeHourSelect = new javax.swing.JComboBox();
        jLabel26 = new javax.swing.JLabel();
        startTimeMinuteSelect = new javax.swing.JComboBox();
        endTimeMinuteSelect = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        endTimeHourSelect = new javax.swing.JComboBox();
        jLabel31 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        codeInput = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        lessonTypeSelect = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        lessonUnitInput = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        instructorSelect = new javax.swing.JComboBox();
        lessonIDLabel = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        notesInput = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        amountDueInput = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edit Lesson"));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        saveLessonBtn.setText("Save");
        saveLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveLessonBtnActionPerformed(evt);
            }
        });
        mainPanel.add(saveLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 210, 110, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        mainPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 210, 100, 40));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Date:");
        mainPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 20));
        mainPanel.add(lessonDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 50, 200, 30));

        startTimeHourSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelect.setSelectedIndex(6);
        startTimeHourSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectActionPerformed(evt);
            }
        });
        mainPanel.add(startTimeHourSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 90, 70, 20));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText(":");
        mainPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 90, 10, 20));

        startTimeMinuteSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectActionPerformed(evt);
            }
        });
        mainPanel.add(startTimeMinuteSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 90, 80, 20));

        endTimeMinuteSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        mainPanel.add(endTimeMinuteSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 120, 80, 20));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText(":");
        mainPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 120, 10, 20));

        endTimeHourSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelect.setSelectedIndex(6);
        mainPanel.add(endTimeHourSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 120, 70, 20));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("End Time:");
        mainPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 20));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Start Time:");
        mainPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 20));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Lesson Code:");
        mainPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 20));
        mainPanel.add(codeInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 150, 200, 20));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Lesson Type:");
        mainPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, -1, 20));

        lessonTypeSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        mainPanel.add(lessonTypeSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 50, 160, -1));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Lesson Units:");
        mainPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, -1, 20));

        lessonUnitInput.setText("1.0");
        mainPanel.add(lessonUnitInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 80, 160, 20));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Instructor:");
        mainPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, -1, 20));

        mainPanel.add(instructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 110, 160, -1));

        lessonIDLabel.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        lessonIDLabel.setForeground(java.awt.Color.blue);
        mainPanel.add(lessonIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 210, 20));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Notes:");
        mainPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 20));
        mainPanel.add(notesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, 200, 20));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Amount Due:");
        mainPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 20));
        mainPanel.add(amountDueInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 210, 200, 20));

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
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveLessonBtnActionPerformed
        // Add new instructor to the database
        try {
            updatedLessonInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveLessonBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Close this 
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void startTimeHourSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeHourSelectActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelect, startTimeMinuteSelect, endTimeHourSelect, endTimeMinuteSelect);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeHourSelectActionPerformed

    private void startTimeMinuteSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeMinuteSelectActionPerformed
        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelect, startTimeMinuteSelect, endTimeHourSelect, endTimeMinuteSelect);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeMinuteSelectActionPerformed

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
            java.util.logging.Logger.getLogger(EditLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EditLesson dialog = new EditLesson(lessonIDStatic, new javax.swing.JFrame(), parentClassStatic);
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
    private javax.swing.JTextField amountDueInput;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField codeInput;
    private javax.swing.JComboBox endTimeHourSelect;
    private javax.swing.JComboBox endTimeMinuteSelect;
    private javax.swing.JComboBox instructorSelect;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private com.toedter.calendar.JDateChooser lessonDateChooser;
    private javax.swing.JLabel lessonIDLabel;
    private javax.swing.JComboBox lessonTypeSelect;
    private javax.swing.JTextField lessonUnitInput;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField notesInput;
    private javax.swing.JButton saveLessonBtn;
    private javax.swing.JComboBox startTimeHourSelect;
    private javax.swing.JComboBox startTimeMinuteSelect;
    // End of variables declaration//GEN-END:variables
}
