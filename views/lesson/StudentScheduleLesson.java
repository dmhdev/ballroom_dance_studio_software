/*
 * This class allows for scheduling a student lesson. The lesson will be recorded in the LessonSchedule table. Lessons are shown under Student Details and on the Schedule.
 */
package views.lesson;

import commons.entities.Enrollment;
import commons.entities.Lesson;
import commons.helpers.ComboBoxHelper;
import commons.helpers.GenericHelper;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import static commons.helpers.NavHelper.addToNavHistory;
import static commons.helpers.NavHelper.navHistory;
import static commons.helpers.NavHelper.openPreviousView;
import static commons.helpers.NavHelper.studentID;
import commons.helpers.TableHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTable;
import views.schedule.LessonSchedule;
import views.student.StudentManager;
import static commons.helpers.NavHelper.studentType;

/**
 *
 * @author daynehammes
 */
public class StudentScheduleLesson extends javax.swing.JFrame {

    private ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    private TableHelper tableHelper = new TableHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    private DefaultTableModel enrollmentTableModel;
    private TableRowSorter enrollmentSorter;
    private ListSelectionModel enrollmentTableSelectionModel = null;

    /**
     * Creates new form Menu
     */
    public StudentScheduleLesson() {

        System.out.println("Student id: " + studentID);
        System.out.println("Last view: " + navHistory.get(navHistory.size() - 1) + navHistory.size());

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

    public void setFields() throws SQLException, ClassNotFoundException, InterruptedException {

        // Set Student's Name
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    genericHelper.setStudentName(studentNameField, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Create arraylist of instructors and apply to combobox
        Thread thread2 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelect, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // Set initial enrollment table values
        Thread thread3 = new Thread() {
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

                        // Choose correct instructor for enrollment
                        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
                        String instructorName = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 2));
                        instructorSelect.setSelectedItem(instructorName);
                    }

                });
            }
        };

        // Set Tab Traversal Policy Order
        Thread thread4 = new Thread() {
            public void run() {

                try {
                    setCustomTraversalPolicy();
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

        // Update new student unit inputs
        if (studentType.equals("New Student")) {
            lessonUnitInput.setText("0.5");
        }

        // Set table to update payment due input when selected
        updateUIFromTable(enrollmentTable);
        enrollmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {

                updateUIFromTable(enrollmentTable);
            }
        });

        // Listen for change in rate type to update lesson price payment due
        lessonTypeSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePaymentDueOnLessonTypeChange();
            }
        });

        // Choose correct instructor for enrollment
        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        String instructorName = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 2));
        instructorSelect.setSelectedItem(instructorName);

        // Set current date for datechooser
        Date date = new Date();
        lessonDateChooser.setDate(date);

        // Set submit button
        getRootPane().setDefaultButton(scheduleLessonBtn);

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
        componentArrayList.add(scheduleLessonBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        scheduleLessonPanel.setFocusTraversalPolicyProvider(true);
        scheduleLessonPanel.setFocusTraversalPolicy(policy);

    }

    private void updatePaymentDueOnLessonTypeChange() {

        // Get selected lesson type
        String lessonType = (String) lessonTypeSelect.getSelectedItem();

        // Get selected enrollment row
        int enrollmentSelectedRow = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

        // Check type of lesson
        if (lessonType.equals("Private")) {

            // Get amount due value
            String amountDue = String.valueOf(Double.parseDouble(genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(enrollmentSelectedRow, 13))));

            // Set amount due 
            amountDueInput.setText(amountDue);

        } else if (lessonType.equals("Group")) {
            // Get amount due value
            String amountDue = String.valueOf(Double.parseDouble(genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(enrollmentSelectedRow, 14))));

            // Set amount due 
            amountDueInput.setText(amountDue);
        } else if (lessonType.equals("Party")) {
            // Get amount due value
            String amountDue = String.valueOf(Double.parseDouble(genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(enrollmentSelectedRow, 15))));

            // Set amount due 
            amountDueInput.setText(amountDue);
        }

    }

    private void updateUIFromTable(JTable target) {

        // Get selected row
        int enrollmentSelectedRow = target.convertRowIndexToModel(target.getSelectedRow());

        // Get amount due value
        String amountDue = String.valueOf(Double.parseDouble(genericHelper.removeRedHTML((String) target.getModel().getValueAt(enrollmentSelectedRow, 13))));

        // Set amount due 
        amountDueInput.setText(amountDue);
    }

    // Schedule student lesson
    private void scheduleStudentLesson() throws ClassNotFoundException, SQLException {

        // Get Variables
        String lessonType = lessonTypeSelect.getSelectedItem().toString();
        String instructorName = instructorSelect.getSelectedItem().toString();
        String appointmentStartTime = startTimeHourSelect.getSelectedItem().toString() + ":" + startTimeMinuteSelect.getSelectedItem().toString() + ":00";
        String appointmentEndTime = endTimeHourSelect.getSelectedItem().toString() + ":" + endTimeMinuteSelect.getSelectedItem().toString() + ":00";
        String notes = StringEscapeUtils.escapeSql(notesInput.getText());
        String lessonCode = StringEscapeUtils.escapeSql(codeInput.getText());
        String instructorID = comboBoxHelper.getSelectedInstructorID(instructorSelect, instructorArrayList, false);

        // Get amount due
        double amountDue = 0.0;
        if (genericHelper.isNumericString(amountDueInput.getText())) {
            amountDue = Double.parseDouble(amountDueInput.getText());
        }

        // Get lesson units
        double lessonUnits = 0;
        if (genericHelper.isNumericString(lessonUnitInput.getText())) {
            lessonUnits = Double.parseDouble(lessonUnitInput.getText());

            // Get appointment date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String appointmentDate = new String(dateFormat.format(lessonDateChooser.getDate()));

            try {

                // Get Program ID from table. Throws NullPointerException if not found
                int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
                String programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                String enrollmentID = (String) enrollmentTable.getModel().getValueAt(row, 12);

                // Check if first lesson, then if attendance exceeds program
                Enrollment enrollment = new Enrollment(enrollmentID);
                boolean attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

                if (attendanceOverLimit) {

                    // Alert exceeds program
                    JOptionPane.showMessageDialog(null, "Lesson exceeds Program Enrollment for " + lessonType + " lessons.",
                            "Lesson Exceeds Limit", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    Lesson lesson = new Lesson(null);
                    String processStatus = lesson.scheduleStudentLesson(lessonType, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                            notes, lessonCode, instructorID, programID, enrollmentID, studentID, lessonUnits, amountDue, "Unattended", "Unpaid");

                    // Check insert status
                    if (processStatus.equals("Success")) {

                        // Alert success
                        JOptionPane.showMessageDialog(null, "Successfully scheduled lesson.", "Lesson Scheduled", JOptionPane.INFORMATION_MESSAGE);

                    } else if (processStatus.equals("InstructorTimeConflict")) {

                        // Alert generic failed insert
                        JOptionPane.showMessageDialog(null, "Instructor Already has a Lesson scheduled for that time.\nPlease choose another time slot.",
                                "Error Scheduling Lesson", JOptionPane.ERROR_MESSAGE);
                    } else {

                        // Alert generic failed insert
                        JOptionPane.showMessageDialog(null, "Error while scheduling lesson. No changes were saved.",
                                "Error Scheduling Lesson", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // Alert enrollment id error
                JOptionPane.showMessageDialog(null, "Please select Program Enrollment from Table.",
                        "No Program Enrollment Selected", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // Alert non-numeric lesson units
            JOptionPane.showMessageDialog(null, "Lesson Units are not Numeric.",
                    "Non-Numeric Lesson Units", JOptionPane.WARNING_MESSAGE);
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

        topLogo = new javax.swing.JLabel();
        mainTitle = new javax.swing.JLabel();
        back_menu_button = new javax.swing.JButton();
        scheduleLessonPanel = new javax.swing.JPanel();
        studentNameField = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        lessonDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        instructorSelect = new javax.swing.JComboBox();
        scheduleLessonBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        lessonTypeSelect = new javax.swing.JComboBox();
        jLabel31 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        startTimeMinuteSelect = new javax.swing.JComboBox();
        lessonUnitInput = new javax.swing.JTextField();
        startTimeHourSelect = new javax.swing.JComboBox();
        endTimeHourSelect = new javax.swing.JComboBox();
        endTimeMinuteSelect = new javax.swing.JComboBox();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        codeInput = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        notesInput = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        amountDueInput = new javax.swing.JTextField();
        lessonScheduleBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        enrollmentTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(729, 488));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, 370, 90));

        mainTitle.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitle.setText("Schedule Lessons");
        getContentPane().add(mainTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 83, 290, 30));

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        getContentPane().add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        scheduleLessonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scheduleLessonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentNameField.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        studentNameField.setForeground(java.awt.Color.blue);
        scheduleLessonPanel.add(studentNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 510, -1));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Date:");
        scheduleLessonPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, 20));
        scheduleLessonPanel.add(lessonDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 40, 200, 30));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText(":");
        scheduleLessonPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 80, 10, 20));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Instructor:");
        scheduleLessonPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 100, -1, 20));

        scheduleLessonPanel.add(instructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 100, 160, -1));

        scheduleLessonBtn.setText("Schedule Lesson");
        scheduleLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scheduleLessonBtnActionPerformed(evt);
            }
        });
        scheduleLessonPanel.add(scheduleLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 180, 160, 40));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        scheduleLessonPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 180, 100, 40));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Lesson Type:");
        scheduleLessonPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 40, -1, 20));

        lessonTypeSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        scheduleLessonPanel.add(lessonTypeSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 40, 160, -1));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("End Time:");
        scheduleLessonPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, -1, 20));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Lesson Units:");
        scheduleLessonPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 70, -1, 20));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Lesson Code:");
        scheduleLessonPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 140, -1, 20));

        startTimeMinuteSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectActionPerformed(evt);
            }
        });
        scheduleLessonPanel.add(startTimeMinuteSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, 80, 20));

        lessonUnitInput.setText("1.0");
        scheduleLessonPanel.add(lessonUnitInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 70, 160, 20));

        startTimeHourSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelect.setSelectedIndex(6);
        startTimeHourSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectActionPerformed(evt);
            }
        });
        scheduleLessonPanel.add(startTimeHourSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, 70, 20));

        endTimeHourSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelect.setSelectedIndex(6);
        scheduleLessonPanel.add(endTimeHourSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 70, 20));

        endTimeMinuteSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        endTimeMinuteSelect.setSelectedIndex(9);
        scheduleLessonPanel.add(endTimeMinuteSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 110, 80, 20));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Start Time:");
        scheduleLessonPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, -1, 20));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText(":");
        scheduleLessonPanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 110, 10, 20));
        scheduleLessonPanel.add(codeInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 140, 200, 20));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Notes:");
        scheduleLessonPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, -1, 20));
        scheduleLessonPanel.add(notesInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 200, 20));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Payment Due:");
        scheduleLessonPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 200, -1, 20));
        scheduleLessonPanel.add(amountDueInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 200, 200, 20));

        getContentPane().add(scheduleLessonPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 740, 240));

        lessonScheduleBtn.setText("View Schedule");
        lessonScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lessonScheduleBtnActionPerformed(evt);
            }
        });
        getContentPane().add(lessonScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 60, 150, 40));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        enrollmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
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

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 360, 760, 140));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed

        // Open Student Manager
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                openPreviousView();
                StudentScheduleLesson.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void scheduleLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduleLessonBtnActionPerformed
        // Schedule lesson
        try {
            scheduleStudentLesson();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_scheduleLessonBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // Open Student Manager
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                StudentManager studentManager = new StudentManager();
                studentManager.setVisible(true);
                StudentScheduleLesson.this.dispose();
            }
        };
        thr.start();
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

    private void enrollmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enrollmentTableMouseClicked

        // Choose correct instructor for enrollment
        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        String instructorName = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 2));
        instructorSelect.setSelectedItem(instructorName);

    }//GEN-LAST:event_enrollmentTableMouseClicked

    private void lessonScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lessonScheduleBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentScheduleLesson");

        // Open Program Enrollment
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                LessonSchedule lessonSchedule = new LessonSchedule();
                lessonSchedule.setVisible(true);
                StudentScheduleLesson.this.dispose();
            }
        };
        thr.start();

    }//GEN-LAST:event_lessonScheduleBtnActionPerformed

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
            java.util.logging.Logger.getLogger(StudentScheduleLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentScheduleLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentScheduleLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentScheduleLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentScheduleLesson().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField amountDueInput;
    private javax.swing.JButton back_menu_button;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextField codeInput;
    private javax.swing.JComboBox endTimeHourSelect;
    private javax.swing.JComboBox endTimeMinuteSelect;
    private javax.swing.JTable enrollmentTable;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private com.toedter.calendar.JDateChooser lessonDateChooser;
    private javax.swing.JButton lessonScheduleBtn;
    private javax.swing.JComboBox lessonTypeSelect;
    private javax.swing.JTextField lessonUnitInput;
    private javax.swing.JLabel mainTitle;
    private javax.swing.JTextField notesInput;
    private javax.swing.JButton scheduleLessonBtn;
    private javax.swing.JPanel scheduleLessonPanel;
    private javax.swing.JComboBox startTimeHourSelect;
    private javax.swing.JComboBox startTimeMinuteSelect;
    private javax.swing.JLabel studentNameField;
    private javax.swing.JLabel topLogo;
    // End of variables declaration//GEN-END:variables
}
