/*
 * This class allows for attending lessons both scheduled and unscheduled while using payment, bonus or none. It allows for processing payments alone as well.
 * The Recent Lessons list will contain all lessons in the past month, as well as any unattended lessons.
 * Input fields are disabled by default unless 'Custom Lesson' Check Box has been selected. With custom lessons, you must select a corresponding Program Enrollment.
 */
package views.student;

import static commons.helpers.ServerHelper.connection;
import views.program_enrollment.StudentProgramEnrollment;
import views.lesson.EditLesson;
import commons.helpers.GenericHelper;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import commons.entities.Enrollment;
import commons.entities.Lesson;
import commons.helpers.ComboBoxHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang.StringEscapeUtils;
import commons.helpers.CustomFocusTraversalPolicy;
import static commons.helpers.NavHelper.addToNavHistory;
import views.schedule.LessonSchedule;
import commons.helpers.TableHelper;
import static commons.helpers.NavHelper.studentID;
import static commons.helpers.NavHelper.studentType;
import static commons.helpers.NavHelper.getLastViewAsString;
import static commons.helpers.NavHelper.openPreviousView;
import java.text.ParseException;

/**
 *
 * @author daynehammes
 */
public class StudentAttendLesson extends javax.swing.JFrame {

    private GenericHelper genericHelper = new GenericHelper();
    ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    TableHelper tableHelper = new TableHelper();
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    private DefaultTableModel enrollmentTableModel, lessonTableModel;
    private TableRowSorter enrollmentSorter, lessonSorter;
    ArrayList<JComboBox> allComboBoxes = null;
    ArrayList<JTextField> allTextFields = null;
    ArrayList<JDateChooser> allDateChoosers = null;
    ListSelectionModel enrollmentTableSelectionModel = null;
    ListSelectionModel lessonTableSelectionModel = null;
    private Color highlighted = new Color(255, 255, 153);
    private Color unhighlighted = new Color(255, 255, 255);
    private boolean initializingUI = true;
    private boolean tableListenersActive = true;

    /**
     * Creates new form Menu
     */
    public StudentAttendLesson() {

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

        // Set fields
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Adjust screen size if screen too small
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.height < 800) {
            this.setSize(1195, 670);
        }

    }

    /**
     * Initialize JFrame Methods
     */
    public void setFields() throws SQLException, ClassNotFoundException, InterruptedException {

        // Set Student's Name
        genericHelper.setStudentName(studentNameField, studentID);

        // Set Tooltip for Custom Lesson Check
        scheduledLessonCheckbox.setToolTipText("Create Custom Lesson to post lessons and payments from this screen.");

        // Set initial enrollment table values
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set inital total/used lesson fields
                int row = enrollmentTable.convertRowIndexToModel(0);
                String privateLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 3));
                String groupLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 5));
                String partyLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 7));
                String privateLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 16));
                String groupLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 17));
                String partyLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 18));
                privateTotalPane.setText(privateLessonTotal);
                groupTotalPane.setText(groupLessonTotal);
                partyTotalPane.setText(partyLessonTotal);
                privateUsedPane.setText(privateLessonUsed);
                groupUsedPane.setText(groupLessonUsed);
                partyUsedPane.setText(partyLessonUsed);

                // Add table selection listener to enrollment table
                enrollmentTableSelectionModel = enrollmentTable.getSelectionModel();
                enrollmentTableSelectionModel.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {

                        if (!e.getValueIsAdjusting() && tableListenersActive) {

                            // Update UI
                            updateUI();

                        }
                    }

                });

            }
        };

        // Populate Lessons table
        Thread thread2 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Add table selection listener to lesson table
                lessonTableSelectionModel = lessonTable.getSelectionModel();
                lessonTableSelectionModel.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {

                        if (!e.getValueIsAdjusting() && tableListenersActive) {

                            // Select Schedule Lesson Checkbox if not selected
                            if (!scheduledLessonCheckbox.isSelected()) {
                                scheduledLessonCheckbox.setSelected(true);
                            }

                            // Update UI
                            updateUI();
                        }

                    }

                });

            }
        };

        // Populate instructor comboboxes
        Thread thread3 = new Thread() {
            public void run() {
                // Create arraylist of instructors and apply to combobox
                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelectPA, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelectBA, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelectA, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, instructorSelectNC, false);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Create component lists and traversal policy
        Thread thread4 = new Thread() {
            public void run() {

                initializeComponentLists();
                setCustomTraversalPolicy();
            }
        };

        // Create component lists and traversal policy
        Thread thread5 = new Thread() {
            public void run() {

                // Set Student's Bonus Fields
                try {
                    setStudentBonusFields();
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

        // Wait for finish
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();

        // Set initial dates for date choosers
        Date date = new Date();
        appointmentDateChooserPA.setDate(date);
        appointmentDateChooserBA.setDate(date);
        appointmentDateChooserA.setDate(date);
        appointmentDateChooserNC.setDate(date);
        paymentDateChooserPA.setDate(date);
        bonusDateChooserBA.setDate(date);
        paymentDateChooserP.setDate(date);

        // Update new student unit inputs
        if (studentType.equals("New Student")) {
            lessonUnitInputPA.setText("0.5");
            lessonUnitInputBA.setText("0.5");
            lessonUnitInputA.setText("0.5");
            lessonUnitInputNC.setText("0.5");
        }

        // Update UI
        updateUI();
    }

    // Initialize Component ArrayLists for Enabling/Disabling
    public void initializeComponentLists() {

        // Combo Boxes
        allComboBoxes = new ArrayList<JComboBox>();
        allComboBoxes.add(instructorSelectPA);
        allComboBoxes.add(lessonTypeSelectPA);
        allComboBoxes.add(startTimeHourSelectPA);
        allComboBoxes.add(startTimeMinuteSelectPA);
        allComboBoxes.add(endTimeHourSelectPA);
        allComboBoxes.add(endTimeMinuteSelectPA);
        allComboBoxes.add(purchaseTypeSelectPA);
        allComboBoxes.add(instructorSelectBA);
        allComboBoxes.add(lessonTypeSelectBA);
        allComboBoxes.add(startTimeHourSelectBA);
        allComboBoxes.add(startTimeMinuteSelectBA);
        allComboBoxes.add(endTimeHourSelectBA);
        allComboBoxes.add(endTimeMinuteSelectBA);
        allComboBoxes.add(instructorSelectA);
        allComboBoxes.add(lessonTypeSelectA);
        allComboBoxes.add(startTimeHourSelectA);
        allComboBoxes.add(startTimeMinuteSelectA);
        allComboBoxes.add(endTimeHourSelectA);
        allComboBoxes.add(endTimeMinuteSelectA);
        allComboBoxes.add(lessonTypeSelectP);
        allComboBoxes.add(purchaseTypeSelectP);
        allComboBoxes.add(instructorSelectNC);
        allComboBoxes.add(lessonTypeSelectNC);
        allComboBoxes.add(startTimeHourSelectNC);
        allComboBoxes.add(startTimeMinuteSelectNC);
        allComboBoxes.add(endTimeHourSelectNC);
        allComboBoxes.add(endTimeMinuteSelectNC);

        // Text Fields
        allTextFields = new ArrayList<JTextField>();
        allTextFields.add(lessonUnitInputPA);
        allTextFields.add(codeInputPA);
        allTextFields.add(notesInputPA);
        allTextFields.add(purchaseTotalInputPA);
        allTextFields.add(lessonUnitInputBA);
        allTextFields.add(codeInputBA);
        allTextFields.add(notesInputBA);
        allTextFields.add(lessonUnitInputA);
        allTextFields.add(codeInputA);
        allTextFields.add(notesInputA);
        allTextFields.add(purchaseTotalInputP);
        allTextFields.add(lessonUnitInputNC);
        allTextFields.add(codeInputNC);
        allTextFields.add(notesInputNC);

        // Date Choosers
        allDateChoosers = new ArrayList<JDateChooser>();
        allDateChoosers.add(appointmentDateChooserPA);
        allDateChoosers.add(paymentDateChooserPA);
        allDateChoosers.add(appointmentDateChooserBA);
        allDateChoosers.add(bonusDateChooserBA);
        allDateChoosers.add(appointmentDateChooserA);
        allDateChoosers.add(paymentDateChooserP);
        allDateChoosers.add(appointmentDateChooserNC);
    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(instructorSelectPA);
        componentArrayList.add(lessonTypeSelectPA);
        componentArrayList.add(lessonUnitInputPA);
        componentArrayList.add(appointmentDateChooserPA);
        componentArrayList.add(paymentDateChooserPA);
        componentArrayList.add(startTimeHourSelectPA);
        componentArrayList.add(startTimeMinuteSelectPA);
        componentArrayList.add(endTimeHourSelectPA);
        componentArrayList.add(endTimeMinuteSelectPA);
        componentArrayList.add(codeInputPA);
        componentArrayList.add(notesInputPA);
        componentArrayList.add(purchaseTotalInputPA);
        componentArrayList.add(purchaseTypeSelectPA);
        componentArrayList.add(attendBtnPA);

        componentArrayList.add(instructorSelectBA);
        componentArrayList.add(lessonTypeSelectBA);
        componentArrayList.add(lessonUnitInputBA);
        componentArrayList.add(appointmentDateChooserBA);
        componentArrayList.add(bonusDateChooserBA);
        componentArrayList.add(startTimeHourSelectBA);
        componentArrayList.add(startTimeMinuteSelectBA);
        componentArrayList.add(endTimeHourSelectBA);
        componentArrayList.add(endTimeMinuteSelectBA);
        componentArrayList.add(codeInputBA);
        componentArrayList.add(notesInputBA);
        componentArrayList.add(attendBtnBA);

        componentArrayList.add(instructorSelectA);
        componentArrayList.add(lessonTypeSelectA);
        componentArrayList.add(lessonUnitInputA);
        componentArrayList.add(appointmentDateChooserA);
        componentArrayList.add(startTimeHourSelectA);
        componentArrayList.add(startTimeMinuteSelectA);
        componentArrayList.add(endTimeHourSelectA);
        componentArrayList.add(endTimeMinuteSelectA);
        componentArrayList.add(codeInputA);
        componentArrayList.add(notesInputA);
        componentArrayList.add(attendBtnA);

        componentArrayList.add(lessonTypeSelectP);
        componentArrayList.add(purchaseTotalInputP);
        componentArrayList.add(purchaseTypeSelectP);
        componentArrayList.add(paymentDateChooserP);
        componentArrayList.add(attendBtnP);

        componentArrayList.add(instructorSelectNC);
        componentArrayList.add(lessonTypeSelectNC);
        componentArrayList.add(lessonUnitInputNC);
        componentArrayList.add(appointmentDateChooserNC);
        componentArrayList.add(startTimeHourSelectNC);
        componentArrayList.add(startTimeMinuteSelectNC);
        componentArrayList.add(endTimeHourSelectNC);
        componentArrayList.add(endTimeMinuteSelectNC);
        componentArrayList.add(codeInputNC);
        componentArrayList.add(notesInputNC);
        componentArrayList.add(attendBtnNC);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        attendPurchaseLessonTabbedPane.setFocusTraversalPolicyProvider(true);
        attendPurchaseLessonTabbedPane.setFocusTraversalPolicy(policy);

    }

    /**
     * Handler Methods
     */
    // Update UI
    private void updateUI() {

        // Enable or Disable Scheduled Lesson Checkbox
        if (lessonTable.getRowCount() > 0) {
            scheduledLessonCheckbox.setEnabled(true);
        } else {
            scheduledLessonCheckbox.setEnabled(false);
            scheduledLessonCheckbox.setSelected(false);
        }

        // Run appropriate updateUI method
        if (initializingUI && getLastViewAsString().equals("StudentDetails")) {

            initializingUI = false;
            scheduledLessonCheckbox.setSelected(false);
            updateUIFromEnrollmentTable();

        } else if (scheduledLessonCheckbox.isSelected()) {

            updateUIFromLessonTable();

        } else if (!scheduledLessonCheckbox.isSelected()) {

            updateUIFromEnrollmentTable();

        }

    }

    // Update UI from Enrollment Table
    private void updateUIFromEnrollmentTable() {

        // Highlight Enrollment table
        enrollmentTable.setBackground(highlighted);
        lessonTable.setBackground(unhighlighted);

        // Enable input fields
        enableOrDisableInputs();

        // Get row index
        int row = 0;
        try {
            row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        } catch (Exception e) {
        }

        // Get enrollment values
        String programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
        String enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
        String instructorName = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 2));
        String privateLessonPrice = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 13));
        String groupLessonPrice = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 14));
        String partyLessonPrice = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 15));
        String privateLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 3));
        String groupLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 5));
        String partyLessonTotal = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 7));
        String privateLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 16));
        String groupLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 17));
        String partyLessonUsed = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 18));

        // Find instructor combobox index by matching with instuctorName
        int lessonInstructorIndex = 0;
        for (int i = 0; i < instructorSelectPA.getItemCount(); i++) {

            // Check if value is equal to instructorName
            if (instructorSelectPA.getItemAt(i).equals(instructorName)) {
                lessonInstructorIndex = i;
                break;
            }
        }

        // Update Purchase/Attend values
        programPanePA.setText(programID);
        programPaneBA.setText(programID);
        programPaneA.setText(programID);
        programPaneNC.setText(programID);

        // Update Price values
        lessonPricePrivatePA.setText(privateLessonPrice);
        lessonPriceGroupPA.setText(groupLessonPrice);
        lessonPricePartyPA.setText(partyLessonPrice);

        // Update selected instructor
        instructorSelectPA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectBA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectNC.setSelectedIndex(lessonInstructorIndex);

        // Update Pay Only Price Values
        lessonPricePrivateP.setText(privateLessonPrice);
        lessonPriceGroupP.setText(groupLessonPrice);
        lessonPricePartyP.setText(partyLessonPrice);

        // Update Program Pane for Pay Only
        programPaneP.setText(programID);

        // Update total and used lesson panes
        privateTotalPane.setText(privateLessonTotal);
        groupTotalPane.setText(groupLessonTotal);
        partyTotalPane.setText(partyLessonTotal);
        privateUsedPane.setText(privateLessonUsed);
        groupUsedPane.setText(groupLessonUsed);
        partyUsedPane.setText(partyLessonUsed);

    }

    // Update UI from Lesson Table
    private void updateUIFromLessonTable() {

        // Highlight Enrollment table
        lessonTable.setBackground(highlighted);
        enrollmentTable.setBackground(unhighlighted);

        // Disable inputs
        enableOrDisableInputs();

        // Get row index
        int row = 0;
        try {
            row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());
        } catch (Exception e) {
        }

        // Get lesson values
        String lessonDate = (String) lessonTable.getModel().getValueAt(row, 0);
        String appointmentTimeStart = (String) lessonTable.getModel().getValueAt(row, 1);
        String instructorName = (String) lessonTable.getModel().getValueAt(row, 2);
        String programID = (String) lessonTable.getModel().getValueAt(row, 5);
        String rateType = (String) lessonTable.getModel().getValueAt(row, 6);
        String appointmentTimeEnd = (String) lessonTable.getModel().getValueAt(row, 8);
        String lessonCode = (String) lessonTable.getModel().getValueAt(row, 9);
        String lessonUnits = (String) lessonTable.getModel().getValueAt(row, 10);
        String notes = (String) lessonTable.getModel().getValueAt(row, 11);
        String lessonPrice = (String) lessonTable.getModel().getValueAt(row, 12);

        // Find instructor combobox index by matching with instuctorName
        int lessonInstructorIndex = 0;
        for (int i = 0; i < instructorSelectPA.getItemCount(); i++) {

            // Check if value is equal to instructorName
            if (instructorSelectPA.getItemAt(i).equals(instructorName)) {
                lessonInstructorIndex = i;
                break;
            }
        }

        // Create Date object from string
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
            Date lessonDateObject = dateFormat.parse(lessonDate);

            // Update dates
            appointmentDateChooserPA.setDate(lessonDateObject);
            appointmentDateChooserBA.setDate(lessonDateObject);
            appointmentDateChooserA.setDate(lessonDateObject);
            appointmentDateChooserNC.setDate(lessonDateObject);
        } catch (ParseException e) {
            System.out.println(e);
        }

        // Update selected instructor
        instructorSelectPA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectBA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectA.setSelectedIndex(lessonInstructorIndex);
        instructorSelectNC.setSelectedIndex(lessonInstructorIndex);

        // Split time fields
        String appointmentTimeStartHour = appointmentTimeStart.substring(0, 2);
        String appointmentTimeStartMinute = appointmentTimeStart.substring(3, 5);
        String appointmentTimeEndHour = appointmentTimeEnd.substring(0, 2);
        String appointmentTimeEndMinute = appointmentTimeEnd.substring(3, 5);

        // Set time fields
        startTimeHourSelectPA.setSelectedItem(appointmentTimeStartHour);
        startTimeMinuteSelectPA.setSelectedItem(appointmentTimeStartMinute);
        endTimeHourSelectPA.setSelectedItem(appointmentTimeEndHour);
        endTimeMinuteSelectPA.setSelectedItem(appointmentTimeEndMinute);
        startTimeHourSelectBA.setSelectedItem(appointmentTimeStartHour);
        startTimeMinuteSelectBA.setSelectedItem(appointmentTimeStartMinute);
        endTimeHourSelectBA.setSelectedItem(appointmentTimeEndHour);
        endTimeMinuteSelectBA.setSelectedItem(appointmentTimeEndMinute);
        startTimeHourSelectA.setSelectedItem(appointmentTimeStartHour);
        startTimeMinuteSelectA.setSelectedItem(appointmentTimeStartMinute);
        endTimeHourSelectA.setSelectedItem(appointmentTimeEndHour);
        endTimeMinuteSelectA.setSelectedItem(appointmentTimeEndMinute);
        startTimeHourSelectNC.setSelectedItem(appointmentTimeStartHour);
        startTimeMinuteSelectNC.setSelectedItem(appointmentTimeStartMinute);
        endTimeHourSelectNC.setSelectedItem(appointmentTimeEndHour);
        endTimeMinuteSelectNC.setSelectedItem(appointmentTimeEndMinute);

        // Update Program ID Panes
        programPanePA.setText(programID);
        programPaneBA.setText(programID);
        programPaneA.setText(programID);
        programPaneP.setText(programID);
        programPaneNC.setText(programID);

        // Update Lesson Price values
        if (rateType.equals("Private")) {
            lessonPricePrivatePA.setText(lessonPrice);
            lessonPricePrivateP.setText(lessonPrice);

        } else if (rateType.equals("Group")) {
            lessonPriceGroupPA.setText(lessonPrice);
            lessonPriceGroupP.setText(lessonPrice);
        } else if (rateType.equals("Party")) {
            lessonPricePartyPA.setText(lessonPrice);
            lessonPricePartyP.setText(lessonPrice);
        } else {
            lessonPricePrivatePA.setText("0.00");
            lessonPriceGroupPA.setText("0.00");
            lessonPricePartyPA.setText("0.00");
            lessonPricePrivateP.setText("0.00");
            lessonPriceGroupP.setText("0.00");
            lessonPricePartyP.setText("0.00");
        }

        // Set Lesson Type Combobox
        lessonTypeSelectPA.setSelectedItem(rateType);
        lessonTypeSelectBA.setSelectedItem(rateType);
        lessonTypeSelectA.setSelectedItem(rateType);
        lessonTypeSelectP.setSelectedItem(rateType);
        lessonTypeSelectNC.setSelectedItem(rateType);

        // Update Lesson Units Panes
        lessonUnitInputPA.setText(lessonUnits);
        lessonUnitInputBA.setText(lessonUnits);
        lessonUnitInputA.setText(lessonUnits);
        lessonUnitInputNC.setText(lessonUnits);

        // Update Lesson Code Panes
        codeInputPA.setText(lessonCode);
        codeInputBA.setText(lessonCode);
        codeInputA.setText(lessonCode);
        codeInputNC.setText(lessonCode);

        // Update Lesson Notes Panes
        notesInputPA.setText(notes);
        notesInputBA.setText(notes);
        notesInputA.setText(notes);
        notesInputNC.setText(notes);
        

    }
    
    
    
    // Handle Enabled/Disabled fields for Custom Lesson Checkbox
    private void enableOrDisableInputs() {

        // Check if Scheduled Lesson Selected
        if (scheduledLessonCheckbox.isSelected()) {

            // Disable combo boxes 
            for (JComboBox comboBox : allComboBoxes) {

                if (allComboBoxes.indexOf(comboBox) != 6 && allComboBoxes.indexOf(comboBox) != 20) {
                    ComboBoxEditor comboBoxEditor = comboBox.getEditor();

                    JTextField comboBoxInput = (JTextField) comboBoxEditor.getEditorComponent();
                    comboBoxInput.setDisabledTextColor(UIManager.getColor("ComboBox.foreground"));
                    comboBoxInput.setBackground(UIManager.getColor("ComboBox.background"));

                    comboBox.setEnabled(false);
                    comboBox.setEditable(true);
                }

            }

            // Disable Text Fields
            for (JTextField textField : allTextFields) {

                // Disable all datechoosers except payment date inputs
                if (allTextFields.indexOf(textField) != 3 && allTextFields.indexOf(textField) != 10) {
                    textField.setEnabled(false);
                }
            }

            // Disable Date Choosers
            for (JDateChooser dateChooser : allDateChoosers) {

                // Disable all datechoosers except payment date inputs
                if (allDateChoosers.indexOf(dateChooser) != 1 && allDateChoosers.indexOf(dateChooser) != 5) {
                    JTextFieldDateEditor dateChooserEditor = (JTextFieldDateEditor) dateChooser.getDateEditor();
                    dateChooserEditor.setDisabledTextColor(Color.BLACK);
                    dateChooser.setEnabled(false);
                }
            }
            
            // Disable Lesson Totals
            privateTotalPane.setText("");
            groupTotalPane.setText("");
            partyTotalPane.setText("");
            privateUsedPane.setText("");
            groupUsedPane.setText("");
            partyUsedPane.setText("");

        } else {

            // Enable combo boxes
            for (JComboBox comboBox : allComboBoxes) {
                comboBox.setEnabled(true);
                comboBox.setEditable(false);
            }

            // Enable Text Fields
            for (JTextField textField : allTextFields) {
                textField.setEnabled(true);
            }

            // Enable Date Choosers
            for (JDateChooser dateChooser : allDateChoosers) {

                // Get current date
                Date date = new Date();
                dateChooser.setDate(date);
                dateChooser.setEnabled(true);
            }

        }
    }

    // Set Student's Bonus Fields
    private void setStudentBonusFields() throws SQLException, ClassNotFoundException {

        try {

            // Get bonus counts
            double[] bonusCounts = genericHelper.getStudentBonusAmounts(studentID);

            // Set Bonus Values on Text Fields
            availableBonusesPrivateBA.setText(String.valueOf(bonusCounts[0]));
            availableBonusesGroupBA.setText(String.valueOf(bonusCounts[1]));
            availableBonusesPartyBA.setText(String.valueOf(bonusCounts[2]));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    // Update Lesson Schedule Attended and Paid
    private void updateLessonSchedule(String lessonID, String attendType, boolean updateLessonStatus) throws SQLException {

        // Update Lesson Record
        if (attendType.equals("Purchase")) {

            // Check if update Lesson Status
            if (updateLessonStatus) {
                connection.prepareStatement(String.format("UPDATE LessonSchedule SET LessonStatus='Attended', PaymentStatus='Paid' WHERE LessonID='%s';", lessonID)).execute();

            } else {
                // Only update Payment Status
                connection.prepareStatement(String.format("UPDATE LessonSchedule SET PaymentStatus='Paid' WHERE LessonID='%s';", lessonID)).execute();

            }

        } else if (attendType.equals("Bonus")) {

            // Check if update Lesson Status
            if (updateLessonStatus) {

                connection.prepareStatement(String.format("UPDATE LessonSchedule SET LessonStatus='Attended', PaymentStatus='Bonus' WHERE LessonID='%s';", lessonID)).execute();
            } else {
                // Only update Payment Status
                connection.prepareStatement(String.format("UPDATE LessonSchedule SET PaymentStatus='Bonus' WHERE LessonID='%s';", lessonID)).execute();

            }
        } else if (attendType.equals("No Charge")) {

            // Check if update Lesson Status
            if (updateLessonStatus) {

                connection.prepareStatement(String.format("UPDATE LessonSchedule SET LessonStatus='Attended', PaymentStatus='No Charge' WHERE LessonID='%s';", lessonID)).execute();
            } else {
                // Only update Payment Status
                connection.prepareStatement(String.format("UPDATE LessonSchedule SET PaymentStatus='No Charge' WHERE LessonID='%s';", lessonID)).execute();

            }
        }
    }

    // Get Last Lesson Inserted for Student
    private String getLastLessonInsertForStudent(String studentID) throws SQLException {

        // Create Lesson Set
        ResultSet lessonSet = connection.prepareStatement(
                "select LessonID from LessonSchedule where StudentID='" + studentID + "' order by DateCreated asc;").executeQuery();
        lessonSet.next();

        // Lesson Id will be the most recent created lesson for that student
        String lessonID = lessonSet.getString(1);

        return lessonID;
    }

    // Delete lesson from Lessons Table
    private void deleteLessonFromLessonsTable() {
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

        // Stop table action listeners during table update
        tableListenersActive = false;

        // Repaint lessons table
        try {
            tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
            lessonTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // Repaint enrollment table
        try {
            tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
            enrollmentTable.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Restart table listeners
        tableListenersActive = true;

        // Update UI
        updateUI();

    }

    // Cancel lesson from Lessons Table
    private void cancelLessonFromLessonsTable() {
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

        // Stop table action listeners during table update
        tableListenersActive = false;
        
        // Repaint lessons table
        try {
            tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
            lessonTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // Repaint enrollment table
        try {
            tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Restart table listeners
        tableListenersActive = true;

        // Update UI
        updateUI();
    }

    // Validates inputs for Custom Lesson
    private String[] validateCustomLessonInputs(String processType) {

        // Create statuses array
        String[] validStatus = new String[3];

        // Purchase Attend inputs
        if (processType.equals("Attend/Purchase")) {

            // Validate lesson unit input
            if (!genericHelper.isNumericString(lessonUnitInputPA.getText())) {

                // Numeric lesson unit input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Lesson Units Input";
                validStatus[2] = "Lesson Units input is not Numeric.";

            } // Validate payment input
            else if (!genericHelper.isNumericString(purchaseTotalInputPA.getText())) {

                // Numeric payment total input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Payment Input";
                validStatus[2] = "Payment Total is not Numeric.";

            } // Validate Program Enrollment Selected
            else if (enrollmentTable.getSelectedRowCount() <= 0) {

                // Program Enrollment selection failed
                validStatus[0] = "Failed";
                validStatus[1] = "Must Choose Enrollment Plan";
                validStatus[2] = "Please choose a Program Enrollment from the table.";

            } else {

                // Validate date input
                try {
                    // Date Format for format check
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.format(appointmentDateChooserPA.getDate());

                    // Passed all validation
                    validStatus[0] = "NoErrors";
                    validStatus[1] = "";
                    validStatus[2] = "";

                } catch (Exception e) {

                    // Date input failed
                    validStatus[0] = "Failed";
                    validStatus[1] = "Invalid Lesson Date";
                    validStatus[2] = "Lesson Date is invalid for 'yyyy-MM-dd'.";

                }

            }

        } // Bonus Attend inputs
        else if (processType.equals("Attend/Bonus")) {

            // Validate lesson unit input
            if (!genericHelper.isNumericString(lessonUnitInputBA.getText())) {

                // Numeric lesson unit input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Lesson Units Input";
                validStatus[2] = "Lesson Units input is not Numeric.";

            }// Validate Program Enrollment Selected
            else if (enrollmentTable.getSelectedRowCount() <= 0) {

                // Program Enrollment selection failed
                validStatus[0] = "Failed";
                validStatus[1] = "Must Choose Enrollment Plan";
                validStatus[2] = "Please choose a Program Enrollment from the table.";

            } else {

                // Validate date input
                try {
                    // Date Format for format check
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.format(appointmentDateChooserBA.getDate());

                    // Passed all validation
                    validStatus[0] = "NoErrors";
                    validStatus[1] = "";
                    validStatus[2] = "";

                } catch (Exception e) {

                    // Date input failed
                    validStatus[0] = "Failed";
                    validStatus[1] = "Invalid Lesson Date";
                    validStatus[2] = "Lesson Date is invalid for 'yyyy-MM-dd'.";

                }

            }

        } // Attend Only inputs
        else if (processType.equals("Attend")) {

            // Validate lesson unit input
            if (!genericHelper.isNumericString(lessonUnitInputA.getText())) {

                // Numeric lesson unit input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Lesson Units Input";
                validStatus[2] = "Lesson Units input is not Numeric.";

            } // Validate Program Enrollment Selected
            else if (enrollmentTable.getSelectedRowCount() <= 0) {

                // Program Enrollment selection failed
                validStatus[0] = "Failed";
                validStatus[1] = "Must Choose Enrollment Plan";
                validStatus[2] = "Please choose a Program Enrollment from the table.";

            } else {

                // Validate date input
                try {
                    // Date Format for format check
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.format(appointmentDateChooserA.getDate());

                    // Passed all validation
                    validStatus[0] = "NoErrors";
                    validStatus[1] = "";
                    validStatus[2] = "";

                } catch (Exception e) {

                    // Date input failed
                    validStatus[0] = "Failed";
                    validStatus[1] = "Invalid Lesson Date";
                    validStatus[2] = "Lesson Date is invalid for 'yyyy-MM-dd'.";

                }

            }

        } // Attend Only inputs
        else if (processType.equals("NoCharge")) {

            // Validate lesson unit input
            if (!genericHelper.isNumericString(lessonUnitInputNC.getText())) {

                // Numeric lesson unit input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Lesson Units Input";
                validStatus[2] = "Lesson Units input is not Numeric.";

            } // Validate Program Enrollment Selected
            else if (enrollmentTable.getSelectedRowCount() <= 0) {

                // Program Enrollment selection failed
                validStatus[0] = "Failed";
                validStatus[1] = "Must Choose Enrollment Plan";
                validStatus[2] = "Please choose a Program Enrollment from the table.";

            } else {

                // Validate date input
                try {
                    // Date Format for format check
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.format(appointmentDateChooserNC.getDate());

                    // Passed all validation
                    validStatus[0] = "NoErrors";
                    validStatus[1] = "";
                    validStatus[2] = "";

                } catch (Exception e) {

                    // Date input failed
                    validStatus[0] = "Failed";
                    validStatus[1] = "Invalid Lesson Date";
                    validStatus[2] = "Lesson Date is invalid for 'yyyy-MM-dd'.";

                }

            }

        } // Payment Only
        else if (processType.equals("Purchase")) {

            // Validate payment input
            if (!genericHelper.isNumericString(purchaseTotalInputP.getText())) {

                // Numeric payment total input failed
                validStatus[0] = "Failed";
                validStatus[1] = "Invalid Payment Input";
                validStatus[2] = "Payment Total is not Numeric.";

            } // Validate Program Enrollment Selected
            else if (enrollmentTable.getSelectedRowCount() <= 0) {

                // Program Enrollment selection failed
                validStatus[0] = "Failed";
                validStatus[1] = "Must Choose Enrollment Plan";
                validStatus[2] = "Please choose a Program Enrollment from the table.";

            } else {

                // Passed all validation
                validStatus[0] = "NoErrors";
                validStatus[1] = "";
                validStatus[2] = "";

            }

        }

        return validStatus;
    }

    /**
     * Payment and Lesson Methods
     */
    // Process Attending and Purchasing Lesson
    public class AttendPurchaseLesson extends SwingWorker<Object, Object> {

        private String processStatus, statusTitle, statusMessage, lessonID, enrollmentID, paymentType, lessonType, programID,
                instructorName, appointmentDate, appointmentStartTime, appointmentEndTime, notes, lessonCode, instructorID, lessonStatus;
        private double lessonUnits, paymentTotal;

        public AttendPurchaseLesson() {

        }

        @Override
        protected Integer doInBackground() throws Exception {

            // Disable buttons
            attendBtnPA.setEnabled(false);
            attendBtnBA.setEnabled(false);
            attendBtnA.setEnabled(false);
            attendBtnP.setEnabled(false);
            attendBtnNC.setEnabled(false);

            try {

                // Is Custom Lesson
                if (!scheduledLessonCheckbox.isSelected()) {

                    // Validate lesson Inputs
                    String[] validStatus = validateCustomLessonInputs("Attend/Purchase");
                    processStatus = validStatus[0];
                    statusTitle = validStatus[1];
                    statusMessage = validStatus[2];

                    if (processStatus.equals("NoErrors")) {

                        // Get selected Program Enrollment
                        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

                        // Get attendance and payment variables
                        programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                        enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
                        lessonType = lessonTypeSelectPA.getSelectedItem().toString();
                        lessonUnits = Double.parseDouble(lessonUnitInputPA.getText());
                        paymentTotal = Double.parseDouble(purchaseTotalInputPA.getText());
                        paymentType = purchaseTypeSelectPA.getSelectedItem().toString();
                        instructorName = instructorSelectPA.getSelectedItem().toString();
                        notes = StringEscapeUtils.escapeSql(notesInputPA.getText());
                        lessonCode = StringEscapeUtils.escapeSql(codeInputPA.getText());
                        instructorID = instructorArrayList.get(instructorSelectPA.getSelectedIndex());
                        appointmentStartTime = startTimeHourSelectPA.getSelectedItem().toString() + ":" + startTimeMinuteSelectPA.getSelectedItem().toString() + ":00";
                        appointmentEndTime = endTimeHourSelectPA.getSelectedItem().toString() + ":" + endTimeMinuteSelectPA.getSelectedItem().toString() + ":00";

                        // Get appointment date
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        appointmentDate = dateFormat.format(appointmentDateChooserPA.getDate());

                        // Check if attendance exceeds program
                        Enrollment enrollment = new Enrollment(enrollmentID);
                        boolean attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

                        if (attendanceOverLimit) {

                            // Numeric payment input failed
                            processStatus = "Failed";
                            statusTitle = "Lesson Exceeds Limit";
                            statusMessage = "Lesson exceeds Program Enrollment for " + lessonType + " lessons.";

                        } else {

                            // Get payment record date
                            String paymentDate = "";
                            try {
                                paymentDate = new String(dateFormat.format(paymentDateChooserPA.getDate()));
                            } catch (Exception e) {
                                Date date = new Date();
                                paymentDate = dateFormat.format(date);
                            }

                            // Add Lesson to Lesson Schedule
                            Lesson lesson = new Lesson(null);
                            processStatus = lesson.scheduleStudentLesson(lessonType, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                                    notes, lessonCode, instructorID, programID, enrollmentID, studentID, lessonUnits, 0.0, "Attended", "Paid");

                            // Check result of lesson creation
                            if (processStatus.equals("Success")) {

                                // Add Payment Record if greater than 0
                                if (paymentTotal > 0) {
                                    genericHelper.createPaymentTransaction(studentID, paymentType, paymentTotal, lessonUnits, enrollmentID, lessonType, paymentDate);
                                }
                                
                                // Update Program Enrollment Attended, Paid and Owed
                                enrollment.updateProgramEnrollment(lessonType, lessonUnits, paymentTotal);
                                enrollment.updateProgramCompleted();

                                // Set Success messages
                                statusTitle = "Added Payment and Attended";
                                statusMessage = "Successfully attended custom lesson and added payment.";

                            } else if (processStatus.equals("InstructorTimeConflict")) {

                                // Alert Instructor Time Conflict
                                processStatus = "Failed";
                                statusTitle = "Instructor Time Conflict";
                                statusMessage = "Instructor Already has a Lesson scheduled for that time.\nPlease choose another time slot.";

                            } else {

                                // Alert Instructor Time Conflict
                                processStatus = "Failed";
                                statusTitle = "Error Scheduling Lesson";
                                statusMessage = "Error while scheduling lesson. No changes were saved.";

                            }

                        }
                    }

                } // Scheduled Lesson
                else {

                    // Ensure Payment Total is numeric
                    if (!genericHelper.isNumericString(purchaseTotalInputPA.getText())) {

                        // Numeric payment input failed
                        processStatus = "Failed";
                        statusTitle = "Invalid Payment Input";
                        statusMessage = "Payment Total is not Numeric.";

                    } else {

                        // Get selected lesson
                        int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());

                        // Get attendance and payment variables
                        lessonStatus = (String) lessonTable.getModel().getValueAt(row, 3);
                        lessonID = (String) lessonTable.getModel().getValueAt(row, 7);
                        enrollmentID = (String) lessonTable.getModel().getValueAt(row, 14);
                        programID = (String) lessonTable.getModel().getValueAt(row, 5);
                        lessonUnits = Double.parseDouble((String) lessonTable.getModel().getValueAt(row, 10));
                        lessonType = (String) lessonTable.getModel().getValueAt(row, 6);
                        paymentTotal = Double.parseDouble(purchaseTotalInputPA.getText());
                        paymentType = purchaseTypeSelectPA.getSelectedItem().toString();

                        // Check if attendance exceeds program
                        Enrollment enrollment = new Enrollment(enrollmentID);
                        boolean attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

                        if (attendanceOverLimit) {

                            // Numeric payment input failed
                            processStatus = "Failed";
                            statusTitle = "Lesson Exceeds Limit";
                            statusMessage = "Lesson exceeds Program Enrollment for " + lessonType + " lessons.";

                        } // Check if lesson is already attended
                        else if (lessonStatus.equals("Attended")) {

                            // Scheduled Lesson already Attended
                            processStatus = "Failed";
                            statusTitle = "Lesson Already Attended";
                            statusMessage = "Scheduled Lesson has already been Attended.";

                        } else {

                            // Get payment record date
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String paymentDate = "";
                            try {
                                paymentDate = new String(dateFormat.format(paymentDateChooserPA.getDate()));
                            } catch (Exception e) {
                                Date date = new Date();
                                paymentDate = dateFormat.format(date);
                            }

                            // Add Payment Record
                            if (paymentTotal > 0) {
                                genericHelper.createPaymentTransaction(studentID, paymentType, paymentTotal, lessonUnits, enrollmentID, lessonType, paymentDate);
                            }

                            // Update Lesson Schedule Status to Attended and Paid
                            updateLessonSchedule(lessonID, "Purchase", true);

                            // Update Program Enrollment Attended, Paid and Owed
                            enrollment.updateProgramEnrollment(lessonType, lessonUnits, paymentTotal);
                            enrollment.updateProgramCompleted();

                            // Process Success
                            processStatus = "Success";
                            statusTitle = "Added Payment and Attended";
                            statusMessage = "Successfully attended scheduled lesson and added payment.";
                        }

                    }

                }

            } catch (Exception e) {

                // Generic Failure
                processStatus = "Failed";
                statusTitle = "An Error Occurred";
                statusMessage = "Error during processing. Error: " + e.getMessage();

                e.printStackTrace();

            }

            return 1;
        }

        @Override
        protected void done() {

            // Enable buttons
            attendBtnPA.setEnabled(true);
            attendBtnBA.setEnabled(true);
            attendBtnA.setEnabled(true);
            attendBtnP.setEnabled(true);
            attendBtnNC.setEnabled(true);

            // Check process status
            if (processStatus.equals("Failed")) {

                // Alert generic failed insert
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.WARNING_MESSAGE);

            } else {

                // Alert success
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.INFORMATION_MESSAGE);

                // Repaint enrollment table
                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                    enrollmentTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Repaint lessons table
                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
                    lessonTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

    // Process Attending and Purchasing Lesson
    public class AttendBonusLesson extends SwingWorker<Object, Object> {

        private String processStatus, statusTitle, statusMessage, lessonID, enrollmentID, lessonType, programID,
                instructorName, appointmentDate, appointmentStartTime, appointmentEndTime, notes, lessonCode, instructorID, lessonStatus;
        private double lessonUnits, oldBonusAvail, newBonusAvail;

        public AttendBonusLesson() {

        }

        @Override
        protected Integer doInBackground() throws Exception {

            // Disable buttons
            attendBtnPA.setEnabled(false);
            attendBtnBA.setEnabled(false);
            attendBtnA.setEnabled(false);
            attendBtnP.setEnabled(false);
            attendBtnNC.setEnabled(false);

            try {

                // Is Custom Lesson
                if (!scheduledLessonCheckbox.isSelected()) {

                    // Get Lesson Type and Lesson Units
                    lessonType = lessonTypeSelectBA.getSelectedItem().toString();
                    lessonUnits = Double.parseDouble(lessonUnitInputBA.getText());

                    // Get Bonus Available for Lesson Type
                    if (lessonType.equals("Private")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesPrivateBA.getText());

                    } else if (lessonType.equals("Group")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesGroupBA.getText());

                    } else if (lessonType.equals("Party")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesPartyBA.getText());

                    }

                    // Get new bonus avail
                    newBonusAvail = oldBonusAvail - lessonUnits;

                    // Ensure enough Bonus to cover lesson
                    if (newBonusAvail >= 0) {

                        // Validate lesson Inputs
                        String[] validStatus = validateCustomLessonInputs("Attend/Bonus");
                        processStatus = validStatus[0];
                        statusTitle = validStatus[1];
                        statusMessage = validStatus[2];

                        if (processStatus.equals("NoErrors")) {

                            // Get selected Program Enrollment
                            int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

                            // Get attendance and payment variables
                            programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                            enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
                            instructorName = instructorSelectBA.getSelectedItem().toString();
                            notes = StringEscapeUtils.escapeSql(notesInputBA.getText());
                            lessonCode = StringEscapeUtils.escapeSql(codeInputBA.getText());
                            instructorID = instructorArrayList.get(instructorSelectBA.getSelectedIndex());
                            appointmentStartTime = startTimeHourSelectBA.getSelectedItem().toString() + ":" + startTimeMinuteSelectBA.getSelectedItem().toString() + ":00";
                            appointmentEndTime = endTimeHourSelectBA.getSelectedItem().toString() + ":" + endTimeMinuteSelectBA.getSelectedItem().toString() + ":00";

                            // Get appointment date
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            appointmentDate = dateFormat.format(appointmentDateChooserBA.getDate());

                            // Add Lesson to Lesson Schedule
                            Lesson lesson = new Lesson(null);
                            processStatus = lesson.scheduleStudentLesson(lessonType, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                                    notes, lessonCode, instructorID, programID, enrollmentID, studentID, lessonUnits, 0.0, "Attended", "Bonus");

                            // Check result of lesson creation
                            if (processStatus.equals("Success")) {

                                // Get Lesson ID from insert
                                lessonID = getLastLessonInsertForStudent(studentID);

                                // Get bonus record date
                                String bonusDate = "";
                                try {
                                    bonusDate = dateFormat.format(bonusDateChooserBA.getDate());
                                } catch (Exception e) {
                                    Date date = new Date();
                                    bonusDate = dateFormat.format(date);
                                }

                                // Add Bonus Record
                                genericHelper.createBonusTransactionFromAttendance(studentID, lessonID, lessonType, lessonUnits, bonusDate, enrollmentID);

                                // Process Success
                                statusTitle = "Used Bonus and Attended";
                                statusMessage = "Successfully attended custom lesson with bonus.";

                            } else if (processStatus.equals("InstructorTimeConflict")) {

                                // Alert Instructor Time Conflict
                                processStatus = "Failed";
                                statusTitle = "Instructor Time Conflict";
                                statusMessage = "Instructor Already has a Lesson scheduled for that time.\nPlease choose another time slot.";

                            } else {

                                // Alert Generic Error
                                processStatus = "Failed";
                                statusTitle = "Error Scheduling Lesson";
                                statusMessage = "Error while scheduling lesson. No changes were saved.";

                            }

                        }

                    } else {
                        // Not enough bonus to cover
                        processStatus = "Failed";
                        statusTitle = "Not Enough Bonus";
                        statusMessage = "Not Enough " + lessonType + " Bonus to cover Lesson.";
                    }
                } // Scheduled Lesson
                else {

                    // Get selected lesson
                    int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());

                    // Get attendance variables
                    lessonStatus = (String) lessonTable.getModel().getValueAt(row, 3);
                    lessonID = (String) lessonTable.getModel().getValueAt(row, 7);
                    enrollmentID = (String) lessonTable.getModel().getValueAt(row, 14);
                    programID = (String) lessonTable.getModel().getValueAt(row, 5);
                    lessonUnits = Double.parseDouble((String) lessonTable.getModel().getValueAt(row, 10));
                    lessonType = (String) lessonTable.getModel().getValueAt(row, 6);

                    // Get Bonus Available for Lesson Type
                    if (lessonType.equals("Private")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesPrivateBA.getText());

                    } else if (lessonType.equals("Group")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesGroupBA.getText());

                    } else if (lessonType.equals("Party")) {

                        oldBonusAvail = Double.parseDouble(availableBonusesPartyBA.getText());

                    }

                    // Get new bonus avail
                    newBonusAvail = oldBonusAvail - lessonUnits;

                    // Ensure enough Bonus to cover lesson
                    if (newBonusAvail < 0) {

                        // Not enough bonus to cover
                        processStatus = "Failed";
                        statusTitle = "Not Enough Bonus";
                        statusMessage = "Not Enough " + lessonType + " Bonus to cover Lesson.";

                    } // Check if lesson is already attended
                    else if (lessonStatus.equals("Attended")) {

                        // Scheduled Lesson already Attended
                        processStatus = "Failed";
                        statusTitle = "Lesson Already Attended";
                        statusMessage = "Scheduled Lesson has already been Attended.";

                    } else {

                        // Get bonus record date
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String bonusDate = "";
                        try {
                            bonusDate = new String(dateFormat.format(bonusDateChooserBA.getDate()));
                        } catch (Exception e) {
                            Date date = new Date();
                            bonusDate = dateFormat.format(date);
                        }

                        // Add Bonus Record
                        genericHelper.createBonusTransactionFromAttendance(studentID, lessonID, lessonType, lessonUnits, bonusDate, enrollmentID);

                        // Update Lesson Schedule Status to Attended and Paid
                        updateLessonSchedule(lessonID, "Bonus", true);

                        // Process Success
                        processStatus = "Success";
                        statusTitle = "Used Bonus and Attended";
                        statusMessage = "Successfully attended scheduled lesson using bonus.";

                    }

                }

            } catch (Exception e) {

                // Generic Failure
                processStatus = "Failed";
                statusTitle = "An Error Occurred";
                statusMessage = "Error during processing. Error: " + e.getMessage();

                e.printStackTrace();

            }

            return 1;
        }

        @Override
        protected void done() {

            // Enable buttons
            attendBtnPA.setEnabled(true);
            attendBtnBA.setEnabled(true);
            attendBtnA.setEnabled(true);
            attendBtnP.setEnabled(true);
            attendBtnNC.setEnabled(true);

            // Check process status
            if (processStatus.equals("Failed")) {

                // Alert generic failed insert
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.WARNING_MESSAGE);

            } else {

                // Alert success
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.INFORMATION_MESSAGE);

                // Repaint enrollment table
                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                    enrollmentTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Repaint lessons table
                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
                    lessonTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Update UI
                updateUI();

                // Reload Bonuses
                try {
                    setStudentBonusFields();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

    // Process Attending and Purchasing Lesson
    public class AttendOnly extends SwingWorker<Object, Object> {

        private String processStatus, statusTitle, statusMessage, lessonID, enrollmentID, lessonType, programID,
                instructorName, appointmentDate, appointmentStartTime, appointmentEndTime, notes, lessonCode, instructorID, lessonStatus;
        private double lessonUnits;

        public AttendOnly() {

        }

        @Override
        protected Integer doInBackground() throws Exception {

            // Disable buttons
            attendBtnPA.setEnabled(false);
            attendBtnBA.setEnabled(false);
            attendBtnA.setEnabled(false);
            attendBtnP.setEnabled(false);
            attendBtnNC.setEnabled(false);

            try {

                // Check if Scheduled Lesson or Custom Lesson
                if (!scheduledLessonCheckbox.isSelected()) {

                    // Validate lesson Inputs
                    String[] validStatus = validateCustomLessonInputs("Attend");
                    processStatus = validStatus[0];
                    statusTitle = validStatus[1];
                    statusMessage = validStatus[2];

                    if (processStatus.equals("NoErrors")) {

                        // Get selected Program Enrollment
                        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

                        // Get attendance and payment variables
                        programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                        enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
                        lessonType = lessonTypeSelectA.getSelectedItem().toString();
                        lessonUnits = Double.parseDouble(lessonUnitInputA.getText());
                        instructorName = instructorSelectA.getSelectedItem().toString();
                        notes = StringEscapeUtils.escapeSql(notesInputA.getText());
                        lessonCode = StringEscapeUtils.escapeSql(codeInputA.getText());
                        instructorID = instructorArrayList.get(instructorSelectA.getSelectedIndex());
                        appointmentStartTime = startTimeHourSelectA.getSelectedItem().toString() + ":" + startTimeMinuteSelectA.getSelectedItem().toString() + ":00";
                        appointmentEndTime = endTimeHourSelectA.getSelectedItem().toString() + ":" + endTimeMinuteSelectA.getSelectedItem().toString() + ":00";

                        // Get appointment date
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        appointmentDate = new String(dateFormat.format(appointmentDateChooserA.getDate()));

                        // Check if attendance exceeds program
                        Enrollment enrollment = new Enrollment(enrollmentID);
                        boolean attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

                        if (attendanceOverLimit) {

                            // Numeric payment input failed
                            processStatus = "Failed";
                            statusTitle = "Lesson Exceeds Limit";
                            statusMessage = "Lesson exceeds Program Enrollment for " + lessonType + " lessons.";

                        } else {

                            // Add Lesson to Lesson Schedule
                            Lesson lesson = new Lesson(null);
                            processStatus = lesson.scheduleStudentLesson(lessonType, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                                    notes, lessonCode, instructorID, programID, enrollmentID, studentID, lessonUnits, 0.0, "Attended", "Paid");

                            // Check result of lesson creation
                            if (processStatus.equals("Success")) {

                                // Update Program Enrollment Attended, Paid and Owed
                                enrollment.updateProgramEnrollment(lessonType, lessonUnits, 0);
                                enrollment.updateProgramCompleted();

                                // Process Success
                                statusTitle = "Attended Lesson";
                                statusMessage = "Successfully attended custom lesson.";

                            } else if (processStatus.equals("InstructorTimeConflict")) {

                                // Alert Instructor Time Conflict
                                processStatus = "Failed";
                                statusTitle = "Instructor Time Conflict";
                                statusMessage = "Instructor Already has a Lesson scheduled for that time.\nPlease choose another time slot.";

                            } else {

                                // Alert Instructor Time Conflict
                                processStatus = "Failed";
                                statusTitle = "Error Scheduling Lesson";
                                statusMessage = "Error while scheduling lesson. No changes were saved.";

                            }

                        }
                    }

                } // Scheduled Lesson
                else {

                    // Get selected lesson
                    int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());

                    // Get attendance and payment variables
                    lessonStatus = (String) lessonTable.getModel().getValueAt(row, 3);
                    lessonID = (String) lessonTable.getModel().getValueAt(row, 7);
                    enrollmentID = (String) lessonTable.getModel().getValueAt(row, 14);
                    programID = (String) lessonTable.getModel().getValueAt(row, 5);
                    lessonUnits = Double.parseDouble((String) lessonTable.getModel().getValueAt(row, 10));
                    lessonType = (String) lessonTable.getModel().getValueAt(row, 6);

                    // Check if attendance exceeds program
                    Enrollment enrollment = new Enrollment(enrollmentID);
                    boolean attendanceOverLimit = enrollment.programAttendanceLimitReached(lessonType, lessonUnits, programID);

                    if (attendanceOverLimit) {

                        // Numeric payment input failed
                        processStatus = "Failed";
                        statusTitle = "Lesson Exceeds Limit";
                        statusMessage = "Lesson exceeds Program Enrollment for " + lessonType + " lessons.";

                    } // Check if lesson is already attended
                    else if (lessonStatus.equals("Attended")) {

                        // Scheduled Lesson already Attended
                        processStatus = "Failed";
                        statusTitle = "Lesson Already Attended";
                        statusMessage = "Scheduled Lesson has already been Attended.";

                    } else {
                        System.out.println("In attendonly");
                        // Update Lesson Schedule Status to Attended and Paid
                        updateLessonSchedule(lessonID, "Purchase", true);

                        // Update Program Enrollment Attended, Paid and Owed
                        enrollment.updateProgramEnrollment(lessonType, lessonUnits, 0);
                        enrollment.updateProgramCompleted();

                        // Process Success
                        processStatus = "Success";
                        statusTitle = "Attended Lesson";
                        statusMessage = "Successfully attended scheduled lesson.";
                    }

                }

            } catch (Exception e) {

                // Generic Failure
                processStatus = "Failed";
                statusTitle = "An Error Occurred";
                statusMessage = "Error during processing. Error: " + e.getMessage();

                e.printStackTrace();

            }

            return 1;
        }

        @Override
        protected void done() {

            // Enable buttons
            attendBtnPA.setEnabled(true);
            attendBtnBA.setEnabled(true);
            attendBtnA.setEnabled(true);
            attendBtnP.setEnabled(true);
            attendBtnNC.setEnabled(true);

            // Check process status
            if (processStatus.equals("Failed")) {

                // Alert generic failed insert
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.WARNING_MESSAGE);

            } else {

                // Alert success
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.INFORMATION_MESSAGE);

                // Repaint enrollment table
                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                    enrollmentTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Repaint lessons table
                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
                    lessonTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

    // Process Attending and Purchasing Lesson
    public class NoChargeLesson extends SwingWorker<Object, Object> {

        private String processStatus, statusTitle, statusMessage, lessonID, enrollmentID, lessonType, programID,
                instructorName, appointmentDate, appointmentStartTime, appointmentEndTime, notes, lessonCode, instructorID, lessonStatus;
        private double lessonUnits;

        public NoChargeLesson() {

        }

        @Override
        protected Integer doInBackground() throws Exception {

            // Disable buttons
            attendBtnPA.setEnabled(false);
            attendBtnBA.setEnabled(false);
            attendBtnA.setEnabled(false);
            attendBtnP.setEnabled(false);
            attendBtnNC.setEnabled(false);

            try {

                // Check if Scheduled Lesson or Custom Lesson
                if (!scheduledLessonCheckbox.isSelected()) {

                    // Validate lesson Inputs
                    String[] validStatus = validateCustomLessonInputs("NoCharge");
                    processStatus = validStatus[0];
                    statusTitle = validStatus[1];
                    statusMessage = validStatus[2];

                    if (processStatus.equals("NoErrors")) {

                        // Get selected Program Enrollment
                        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

                        // Get attendance and payment variables
                        programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                        enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
                        lessonType = lessonTypeSelectNC.getSelectedItem().toString();
                        lessonUnits = Double.parseDouble(lessonUnitInputNC.getText());
                        instructorName = instructorSelectNC.getSelectedItem().toString();
                        notes = StringEscapeUtils.escapeSql(notesInputNC.getText());
                        lessonCode = StringEscapeUtils.escapeSql(codeInputNC.getText());
                        instructorID = instructorArrayList.get(instructorSelectNC.getSelectedIndex());
                        appointmentStartTime = startTimeHourSelectNC.getSelectedItem().toString() + ":" + startTimeMinuteSelectNC.getSelectedItem().toString() + ":00";
                        appointmentEndTime = endTimeHourSelectNC.getSelectedItem().toString() + ":" + endTimeMinuteSelectNC.getSelectedItem().toString() + ":00";

                        // Get appointment date
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        appointmentDate = new String(dateFormat.format(appointmentDateChooserNC.getDate()));

                        // Add Lesson to Lesson Schedule
                        Lesson lesson = new Lesson(null);
                        processStatus = lesson.scheduleStudentLesson(lessonType, instructorName, appointmentDate, appointmentStartTime, appointmentEndTime,
                                notes, lessonCode, instructorID, programID, enrollmentID, studentID, lessonUnits, 0.0, "Attended", "No Charge");

                        // Check result of lesson creation
                        if (processStatus.equals("Success")) {

                            // Process Success
                            statusTitle = "Attended Lesson";
                            statusMessage = "Successfully attended custom lesson.";

                        } else if (processStatus.equals("InstructorTimeConflict")) {

                            // Alert Instructor Time Conflict
                            processStatus = "Failed";
                            statusTitle = "Instructor Time Conflict";
                            statusMessage = "Instructor Already has a Lesson scheduled for that time.\nPlease choose another time slot.";

                        } else {

                            // Alert Instructor Time Conflict
                            processStatus = "Failed";
                            statusTitle = "Error Scheduling Lesson";
                            statusMessage = "Error while scheduling lesson. No changes were saved.";

                        }

                    }

                } // Scheduled Lesson
                else {

                    // Get selected lesson
                    int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());

                    // Get attendance and payment variables
                    lessonStatus = (String) lessonTable.getModel().getValueAt(row, 3);
                    lessonID = (String) lessonTable.getModel().getValueAt(row, 7);
                    enrollmentID = (String) lessonTable.getModel().getValueAt(row, 14);
                    programID = (String) lessonTable.getModel().getValueAt(row, 5);
                    lessonUnits = Double.parseDouble((String) lessonTable.getModel().getValueAt(row, 10));
                    lessonType = (String) lessonTable.getModel().getValueAt(row, 6);

                    // Check if lesson is already attended
                    if (lessonStatus.equals("Attended")) {

                        // Scheduled Lesson already Attended
                        processStatus = "Failed";
                        statusTitle = "Lesson Already Attended";
                        statusMessage = "Scheduled Lesson has already been Attended.";

                    } else {

                        // Update Lesson Schedule Status to Attended and No Charge
                        updateLessonSchedule(lessonID, "No Charge", true);

                        // Process Success
                        processStatus = "Success";
                        statusTitle = "Attended No Charge Lesson";
                        statusMessage = "Successfully attended No Charge scheduled lesson.";
                    }

                }

            } catch (Exception e) {

                // Generic Failure
                processStatus = "Failed";
                statusTitle = "An Error Occurred";
                statusMessage = "Error during processing. Error: " + e.getMessage();

                e.printStackTrace();

            }

            return 1;
        }

        @Override
        protected void done() {

            // Enable buttons
            attendBtnPA.setEnabled(true);
            attendBtnBA.setEnabled(true);
            attendBtnA.setEnabled(true);
            attendBtnP.setEnabled(true);
            attendBtnNC.setEnabled(true);

            // Check process status
            if (processStatus.equals("Failed")) {

                // Alert generic failed insert
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.WARNING_MESSAGE);

            } else {

                // Alert success
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.INFORMATION_MESSAGE);

                // Repaint enrollment table
                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                    enrollmentTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Repaint lessons table
                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
                    lessonTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }

    // Process Attending and Purchasing Lesson
    public class PurchaseOnly extends SwingWorker<Object, Object> {

        private String processStatus, statusTitle, statusMessage, lessonID, enrollmentID, paymentType, lessonType, programID,
                instructorName, appointmentDate, appointmentStartTime, appointmentEndTime, notes, lessonCode, instructorID;
        private double lessonUnits, paymentTotal;

        public PurchaseOnly() {

        }

        @Override
        protected Integer doInBackground() throws Exception {

            // Disable buttons
            attendBtnPA.setEnabled(false);
            attendBtnBA.setEnabled(false);
            attendBtnA.setEnabled(false);
            attendBtnP.setEnabled(false);
            attendBtnNC.setEnabled(false);

            try {

                // Ensure Payment Total is numeric
                if (genericHelper.isNumericString(purchaseTotalInputP.getText())) {

                    // Is Custom Lesson
                    if (!scheduledLessonCheckbox.isSelected()) {

                        // Validate lesson Inputs
                        String[] validStatus = validateCustomLessonInputs("Purchase");
                        processStatus = validStatus[0];
                        statusTitle = validStatus[1];
                        statusMessage = validStatus[2];

                        if (processStatus.equals("NoErrors")) {

                            // Get selected Program Enrollment
                            int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());

                            // Get attendance and payment variables
                            programID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 1));
                            enrollmentID = genericHelper.removeRedHTML((String) enrollmentTable.getModel().getValueAt(row, 12));
                            lessonType = lessonTypeSelectP.getSelectedItem().toString();
                            paymentTotal = Double.parseDouble(purchaseTotalInputP.getText());
                            paymentType = purchaseTypeSelectP.getSelectedItem().toString();

                            // Get payment record date
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String paymentDate = "";
                            try {
                                paymentDate = new String(dateFormat.format(paymentDateChooserP.getDate()));
                            } catch (Exception e) {
                                Date date = new Date();
                                paymentDate = dateFormat.format(date);
                            }

                            // Ensure payment input is greater than 0
                            if (paymentTotal > 0) {

                                // Add Payment Record
                                genericHelper.createPaymentTransaction(studentID, paymentType, paymentTotal, 0, enrollmentID, lessonType, paymentDate);

                                // Update Program Enrollment Attended, Paid and Owed
                                Enrollment enrollment = new Enrollment(enrollmentID);
                                enrollment.updateProgramEnrollment(lessonType, 0, paymentTotal);

                                // Process Success
                                processStatus = "Success";
                                statusTitle = "Added Payment";
                                statusMessage = "Successfully added payment.";
                            } else {
                                // Number is not greater than 0
                                processStatus = "Failed";
                                statusTitle = "Invalid Payment Input";
                                statusMessage = "Payment Total must be greater than 0.";
                            }
                        }

                    } // Scheduled Lesson
                    else {

                        try {
                            // Get selected lesson
                            int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());

                            // Get attendance and payment variables
                            lessonID = (String) lessonTable.getModel().getValueAt(row, 7);
                            enrollmentID = (String) lessonTable.getModel().getValueAt(row, 14);
                            programID = (String) lessonTable.getModel().getValueAt(row, 5);
                            lessonType = (String) lessonTable.getModel().getValueAt(row, 6);
                            paymentTotal = Double.parseDouble(purchaseTotalInputP.getText());
                            paymentType = purchaseTypeSelectP.getSelectedItem().toString();

                            // Get payment record date
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String paymentDate = "";
                            try {
                                paymentDate = new String(dateFormat.format(paymentDateChooserP.getDate()));
                            } catch (Exception e) {
                                Date date = new Date();
                                paymentDate = dateFormat.format(date);
                            }

                            // Add Payment Record
                            genericHelper.createPaymentTransaction(studentID, paymentType, paymentTotal, 0, enrollmentID, lessonType, paymentDate);

                            // Update Lesson Schedule Status to Attended and Paid
                            updateLessonSchedule(lessonID, "Purchase", false);

                            // Update Program Enrollment Attended, Paid and Owed
                            Enrollment enrollment = new Enrollment(enrollmentID);
                            enrollment.updateProgramEnrollment(lessonType, 0, paymentTotal);

                            // Process Success
                            processStatus = "Success";
                            statusTitle = "Added Payment";
                            statusMessage = "Successfully added payment.";
                        } catch (Exception e) {

                            // If there are no lessons, alert user they need to create a 'Custom Lesson'
                            processStatus = "Failed";
                            statusTitle = "An Error Occurred";
                            statusMessage = "If no Lesson is selected, please check 'Custom Lesson'";
                        }

                    }
                } else {
                    // Is not numeric string
                    processStatus = "Failed";
                    statusTitle = "Invalid Payment Input";
                    statusMessage = "Payment input is not a valid number.";
                }

            } catch (Exception e) {

                // Generic Failure
                processStatus = "Failed";
                statusTitle = "An Error Occurred";
                statusMessage = "Error during processing. Error: " + e.getMessage();

                e.printStackTrace();

            }

            return 1;
        }

        @Override
        protected void done() {

            // Enable buttons
            attendBtnPA.setEnabled(true);
            attendBtnBA.setEnabled(true);
            attendBtnA.setEnabled(true);
            attendBtnP.setEnabled(true);
            attendBtnNC.setEnabled(true);

            // Check process status
            if (processStatus.equals("Failed")) {

                // Alert generic failed insert
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.WARNING_MESSAGE);

            } else {

                // Alert success
                JOptionPane.showMessageDialog(null, statusMessage, statusTitle, JOptionPane.INFORMATION_MESSAGE);

                // Repaint enrollment table
                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                    enrollmentTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Repaint lessons table
                try {
                    tableHelper.populateLessonsTable(lessonTable, lessonSorter, lessonTableModel, studentID, 4, "AttendPurchase");
                    lessonTable.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

        contentScrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        topLogo = new javax.swing.JLabel();
        mainTitle = new javax.swing.JLabel();
        back_menu_button = new javax.swing.JButton();
        upperPanel = new javax.swing.JPanel();
        studentNameField = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lessonTable = new javax.swing.JTable();
        editLessonBtn = new javax.swing.JButton();
        attendPurchaseLessonTabbedPane = new javax.swing.JTabbedPane();
        payAttendPanel = new javax.swing.JPanel();
        attendBtnPA = new javax.swing.JButton();
        purchaseTotalInputPA = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        purchaseTypeSelectPA = new javax.swing.JComboBox();
        jPanel14 = new javax.swing.JPanel();
        lessonPricePrivatePA = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        lessonPriceGroupPA = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        lessonPricePartyPA = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        instructorSelectPA = new javax.swing.JComboBox();
        programPanePA = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        lessonTypeSelectPA = new javax.swing.JComboBox();
        lessonUnitInputPA = new javax.swing.JTextField();
        appointmentDateChooserPA = new com.toedter.calendar.JDateChooser();
        jLabel34 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        startTimeHourSelectPA = new javax.swing.JComboBox();
        endTimeHourSelectPA = new javax.swing.JComboBox();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        startTimeMinuteSelectPA = new javax.swing.JComboBox();
        endTimeMinuteSelectPA = new javax.swing.JComboBox();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        notesInputPA = new javax.swing.JTextField();
        codeInputPA = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        paymentDateChooserPA = new com.toedter.calendar.JDateChooser();
        jPanel4 = new javax.swing.JPanel();
        attendBtnBA = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        availableBonusesPrivateBA = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        availableBonusesGroupBA = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        availableBonusesPartyBA = new javax.swing.JTextField();
        appointmentDateChooserBA = new com.toedter.calendar.JDateChooser();
        jLabel14 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        codeInputBA = new javax.swing.JTextField();
        endTimeHourSelectBA = new javax.swing.JComboBox();
        startTimeHourSelectBA = new javax.swing.JComboBox();
        jLabel26 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        endTimeMinuteSelectBA = new javax.swing.JComboBox();
        startTimeMinuteSelectBA = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        lessonUnitInputBA = new javax.swing.JTextField();
        lessonTypeSelectBA = new javax.swing.JComboBox();
        programPaneBA = new javax.swing.JTextField();
        instructorSelectBA = new javax.swing.JComboBox();
        jLabel62 = new javax.swing.JLabel();
        notesInputBA = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        bonusDateChooserBA = new com.toedter.calendar.JDateChooser();
        jPanel5 = new javax.swing.JPanel();
        attendBtnA = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        instructorSelectA = new javax.swing.JComboBox();
        programPaneA = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        codeInputA = new javax.swing.JTextField();
        notesInputA = new javax.swing.JTextField();
        endTimeHourSelectA = new javax.swing.JComboBox();
        startTimeHourSelectA = new javax.swing.JComboBox();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        endTimeMinuteSelectA = new javax.swing.JComboBox();
        startTimeMinuteSelectA = new javax.swing.JComboBox();
        appointmentDateChooserA = new com.toedter.calendar.JDateChooser();
        lessonUnitInputA = new javax.swing.JTextField();
        lessonTypeSelectA = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        attendBtnP = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        lessonPricePrivateP = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        lessonPriceGroupP = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        lessonPricePartyP = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        purchaseTypeSelectP = new javax.swing.JComboBox();
        purchaseTotalInputP = new javax.swing.JTextField();
        lessonTypeSelectP = new javax.swing.JComboBox();
        programPaneP = new javax.swing.JTextField();
        paymentDateChooserP = new com.toedter.calendar.JDateChooser();
        jLabel18 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        codeInputNC = new javax.swing.JTextField();
        notesInputNC = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        startTimeMinuteSelectNC = new javax.swing.JComboBox();
        endTimeMinuteSelectNC = new javax.swing.JComboBox();
        endTimeHourSelectNC = new javax.swing.JComboBox();
        startTimeHourSelectNC = new javax.swing.JComboBox();
        appointmentDateChooserNC = new com.toedter.calendar.JDateChooser();
        lessonUnitInputNC = new javax.swing.JTextField();
        lessonTypeSelectNC = new javax.swing.JComboBox();
        programPaneNC = new javax.swing.JTextField();
        instructorSelectNC = new javax.swing.JComboBox();
        attendBtnNC = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        scheduledLessonCheckbox = new javax.swing.JCheckBox();
        deleteLessonBtn = new javax.swing.JButton();
        cancelLessonBtn = new javax.swing.JButton();
        privateTotalPane = new javax.swing.JLabel();
        instructorIDLabel = new javax.swing.JLabel();
        instructorIDLabel1 = new javax.swing.JLabel();
        privateUsedPane = new javax.swing.JLabel();
        instructorIDLabel2 = new javax.swing.JLabel();
        instructorIDLabel3 = new javax.swing.JLabel();
        groupTotalPane = new javax.swing.JLabel();
        groupUsedPane = new javax.swing.JLabel();
        instructorIDLabel4 = new javax.swing.JLabel();
        partyTotalPane = new javax.swing.JLabel();
        partyUsedPane = new javax.swing.JLabel();
        instructorIDLabel5 = new javax.swing.JLabel();
        lessonScheduleBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        enrollmentTableScrollPane = new javax.swing.JScrollPane();
        enrollmentTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(784, 521));

        contentScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        contentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        contentPanel.add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 0, 360, 90));

        mainTitle.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitle.setText("Attend Lessons");
        contentPanel.add(mainTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 83, 290, 30));

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        contentPanel.add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        upperPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        upperPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentNameField.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        studentNameField.setForeground(java.awt.Color.blue);
        studentNameField.setText("Example, Name");
        upperPanel.add(studentNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 450, -1));

        lessonTable.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        lessonTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Time", "Instructor"
            }
        ));
        jScrollPane1.setViewportView(lessonTable);

        upperPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 260, 270));

        editLessonBtn.setText("Edit Lesson");
        editLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLessonBtnActionPerformed(evt);
            }
        });
        upperPanel.add(editLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 360, 200, -1));

        payAttendPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        attendBtnPA.setText("Pay and Attend");
        attendBtnPA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnPAActionPerformed(evt);
            }
        });
        payAttendPanel.add(attendBtnPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 270, 170, 40));

        purchaseTotalInputPA.setText("0");
        payAttendPanel.add(purchaseTotalInputPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 180, 220, 30));

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setText("Purchase Total:");
        payAttendPanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 190, -1, 10));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Purchase Type:");
        payAttendPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 220, -1, 20));

        purchaseTypeSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Master Card", "Visa", "Discover", "American Express", "Cash", "Check" }));
        payAttendPanel.add(purchaseTypeSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 220, 220, -1));

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Lesson Price"));
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lessonPricePrivatePA.setEditable(false);
        lessonPricePrivatePA.setText("0");
        lessonPricePrivatePA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel14.add(lessonPricePrivatePA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 220, 20));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel38.setText("Private:");
        jPanel14.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 20));

        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel43.setText("Group:");
        jPanel14.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 20));

        lessonPriceGroupPA.setEditable(false);
        lessonPriceGroupPA.setText("0");
        lessonPriceGroupPA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel14.add(lessonPriceGroupPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 220, 20));

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel46.setText("Party:");
        jPanel14.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 20));

        lessonPricePartyPA.setEditable(false);
        lessonPricePartyPA.setText("0");
        lessonPricePartyPA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel14.add(lessonPricePartyPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 220, 20));

        payAttendPanel.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, 320, 130));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("Instructor:");
        payAttendPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, 10));

        payAttendPanel.add(instructorSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 260, 30));

        programPanePA.setEditable(false);
        programPanePA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        payAttendPanel.add(programPanePA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, 260, 30));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setText("Program:");
        payAttendPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, 10));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Lesson Units:");
        payAttendPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, 10));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Lesson Type:");
        payAttendPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 10));

        lessonTypeSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        payAttendPanel.add(lessonTypeSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, 260, 30));

        lessonUnitInputPA.setText("1.0");
        lessonUnitInputPA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        payAttendPanel.add(lessonUnitInputPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, 260, 30));
        payAttendPanel.add(appointmentDateChooserPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 130, 260, 30));

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel34.setText("Appointment Date:");
        payAttendPanel.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, 10));

        jLabel63.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel63.setText("Start Time:");
        payAttendPanel.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, 20));

        jLabel64.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel64.setText("End Time:");
        payAttendPanel.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        startTimeHourSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelectPA.setSelectedIndex(6);
        startTimeHourSelectPA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectPAActionPerformed(evt);
            }
        });
        payAttendPanel.add(startTimeHourSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 200, 70, 20));

        endTimeHourSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelectPA.setSelectedIndex(6);
        payAttendPanel.add(endTimeHourSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 230, 70, 20));

        jLabel65.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel65.setText(":");
        payAttendPanel.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 230, 10, 20));

        jLabel66.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel66.setText(":");
        payAttendPanel.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, 10, 20));

        startTimeMinuteSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelectPA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectPAActionPerformed(evt);
            }
        });
        payAttendPanel.add(startTimeMinuteSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 200, 80, 20));

        endTimeMinuteSelectPA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        endTimeMinuteSelectPA.setSelectedIndex(9);
        payAttendPanel.add(endTimeMinuteSelectPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 230, 80, 20));

        jLabel67.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel67.setText("Code:");
        payAttendPanel.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, -1, 10));

        jLabel68.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel68.setText("Notes:");
        payAttendPanel.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, -1, 10));

        notesInputPA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        payAttendPanel.add(notesInputPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 290, 260, 30));

        codeInputPA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        payAttendPanel.add(codeInputPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 260, 260, 30));

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel37.setText("Payment Date:");
        payAttendPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, 10));
        payAttendPanel.add(paymentDateChooserPA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, 260, 30));

        attendPurchaseLessonTabbedPane.addTab("Pay & Attend", payAttendPanel);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        attendBtnBA.setText("Use Bonus and Attend");
        attendBtnBA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnBAActionPerformed(evt);
            }
        });
        jPanel4.add(attendBtnBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 260, 190, 40));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Available Bonuses"));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        availableBonusesPrivateBA.setEditable(false);
        availableBonusesPrivateBA.setText("0");
        jPanel9.add(availableBonusesPrivateBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 220, 20));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Private:");
        jPanel9.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 20));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setText("Group:");
        jPanel9.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 20));

        availableBonusesGroupBA.setEditable(false);
        availableBonusesGroupBA.setText("0");
        jPanel9.add(availableBonusesGroupBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 220, 20));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Party:");
        jPanel9.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 20));

        availableBonusesPartyBA.setEditable(false);
        availableBonusesPartyBA.setText("0");
        jPanel9.add(availableBonusesPartyBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 220, 20));

        jPanel4.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, 320, 130));
        jPanel4.add(appointmentDateChooserBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 130, 260, 30));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Appointment Date:");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, 10));

        jLabel50.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel50.setText("Start Time:");
        jPanel4.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, 20));

        jLabel51.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel51.setText("End Time:");
        jPanel4.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        jLabel42.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel42.setText("Code:");
        jPanel4.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, -1, 10));

        codeInputBA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel4.add(codeInputBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 260, 260, 30));

        endTimeHourSelectBA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelectBA.setSelectedIndex(6);
        jPanel4.add(endTimeHourSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 230, 70, 20));

        startTimeHourSelectBA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelectBA.setSelectedIndex(6);
        startTimeHourSelectBA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectBAActionPerformed(evt);
            }
        });
        jPanel4.add(startTimeHourSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 200, 70, 20));

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText(":");
        jPanel4.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, 10, 20));

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText(":");
        jPanel4.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 230, 10, 20));

        endTimeMinuteSelectBA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        endTimeMinuteSelectBA.setSelectedIndex(9);
        jPanel4.add(endTimeMinuteSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 230, 80, 20));

        startTimeMinuteSelectBA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelectBA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectBAActionPerformed(evt);
            }
        });
        jPanel4.add(startTimeMinuteSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 200, 80, 20));

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel23.setText("Instructor:");
        jPanel4.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, 10));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Program:");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, 10));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Lesson Type:");
        jPanel4.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 10));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Lesson Units:");
        jPanel4.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, 10));

        lessonUnitInputBA.setText("1.0");
        lessonUnitInputBA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel4.add(lessonUnitInputBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, 260, 30));

        lessonTypeSelectBA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        jPanel4.add(lessonTypeSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, 260, 30));

        programPaneBA.setEditable(false);
        programPaneBA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel4.add(programPaneBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, 260, 30));

        jPanel4.add(instructorSelectBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 260, 30));

        jLabel62.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel62.setText("Notes:");
        jPanel4.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, -1, 10));

        notesInputBA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel4.add(notesInputBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 290, 260, 30));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Bonus Date:");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, 10));
        jPanel4.add(bonusDateChooserBA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 160, 260, 30));

        attendPurchaseLessonTabbedPane.addTab("Use Bonus & Attend", jPanel4);

        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        attendBtnA.setText("Attend Only");
        attendBtnA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnAActionPerformed(evt);
            }
        });
        jPanel5.add(attendBtnA, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 260, 160, 40));

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setText("Instructor:");
        jPanel5.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jPanel5.add(instructorSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 260, 30));

        programPaneA.setEditable(false);
        programPaneA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel5.add(programPaneA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, 260, 30));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("Program:");
        jPanel5.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 10));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Lesson Type:");
        jPanel5.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 10));

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel53.setText("Lesson Units:");
        jPanel5.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 10));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Appointment Date:");
        jPanel5.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 10));

        jLabel54.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel54.setText("Start Time:");
        jPanel5.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 20));

        jLabel55.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel55.setText("End Time:");
        jPanel5.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 20));

        jLabel69.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel69.setText("Notes:");
        jPanel5.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, 10));

        jLabel56.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel56.setText("Code:");
        jPanel5.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, 10));

        codeInputA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel5.add(codeInputA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 240, 260, 30));

        notesInputA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel5.add(notesInputA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, 260, 30));

        endTimeHourSelectA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelectA.setSelectedIndex(6);
        jPanel5.add(endTimeHourSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 210, 70, 20));

        startTimeHourSelectA.setMaximumRowCount(14);
        startTimeHourSelectA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelectA.setSelectedIndex(6);
        startTimeHourSelectA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectAActionPerformed(evt);
            }
        });
        jPanel5.add(startTimeHourSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 180, 70, 20));

        jLabel70.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel70.setText(":");
        jPanel5.add(jLabel70, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 180, 10, 20));

        jLabel71.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel71.setText(":");
        jPanel5.add(jLabel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 210, 10, 20));

        endTimeMinuteSelectA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        endTimeMinuteSelectA.setSelectedIndex(9);
        jPanel5.add(endTimeMinuteSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 210, 80, 20));

        startTimeMinuteSelectA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelectA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectAActionPerformed(evt);
            }
        });
        jPanel5.add(startTimeMinuteSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 180, 80, 20));
        jPanel5.add(appointmentDateChooserA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 260, 30));

        lessonUnitInputA.setText("1.0");
        lessonUnitInputA.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel5.add(lessonUnitInputA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 260, 30));

        lessonTypeSelectA.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        jPanel5.add(lessonTypeSelectA, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 260, 30));

        attendPurchaseLessonTabbedPane.addTab("Attend Only", jPanel5);

        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        attendBtnP.setText("Pay Only");
        attendBtnP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnPActionPerformed(evt);
            }
        });
        jPanel6.add(attendBtnP, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 260, 160, 40));

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Lesson Price"));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lessonPricePrivateP.setEditable(false);
        lessonPricePrivateP.setText("0");
        jPanel15.add(lessonPricePrivateP, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 220, 20));

        jLabel47.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel47.setText("Private:");
        jPanel15.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 20));

        jLabel48.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel48.setText("Group:");
        jPanel15.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 20));

        lessonPriceGroupP.setEditable(false);
        lessonPriceGroupP.setText("0");
        jPanel15.add(lessonPriceGroupP, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 220, 20));

        jLabel49.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel49.setText("Party:");
        jPanel15.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 20));

        lessonPricePartyP.setEditable(false);
        lessonPricePartyP.setText("0");
        jPanel15.add(lessonPricePartyP, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 220, 20));

        jPanel6.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, 320, 130));

        jLabel39.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel39.setText("Program:");
        jPanel6.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, 10));

        jLabel40.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel40.setText("Payment On:");
        jPanel6.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 10));

        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel44.setText("Purchase Total:");
        jPanel6.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, 10));

        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel45.setText("Purchase Type:");
        jPanel6.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, 10));

        purchaseTypeSelectP.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Master Card", "Visa", "Discover", "American Express", "Cash", "Check" }));
        jPanel6.add(purchaseTypeSelectP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 110, 260, 30));

        purchaseTotalInputP.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel6.add(purchaseTotalInputP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 260, 30));

        lessonTypeSelectP.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Enrollment", "Private", "Group", "Party" }));
        jPanel6.add(lessonTypeSelectP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 260, 30));

        programPaneP.setEditable(false);
        programPaneP.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel6.add(programPaneP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 260, 30));
        jPanel6.add(paymentDateChooserP, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 140, 260, 30));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Payment Date:");
        jPanel6.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, 10));

        attendPurchaseLessonTabbedPane.addTab("Pay Only", jPanel6);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setText("Instructor:");
        jPanel1.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, 10));

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText("Program:");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, 10));

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel36.setText("Lesson Type:");
        jPanel1.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, 10));

        jLabel57.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel57.setText("Lesson Units:");
        jPanel1.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, 10));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Appointment Date:");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, 10));

        jLabel58.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel58.setText("Start Time:");
        jPanel1.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, 20));

        jLabel59.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel59.setText("End Time:");
        jPanel1.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, -1, 20));

        jLabel60.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel60.setText("Code:");
        jPanel1.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, 10));

        jLabel72.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel72.setText("Notes:");
        jPanel1.add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, -1, 10));

        codeInputNC.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel1.add(codeInputNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 240, 260, 30));

        notesInputNC.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel1.add(notesInputNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, 260, 30));

        jLabel73.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel73.setText(":");
        jPanel1.add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 210, 10, 20));

        jLabel74.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel74.setText(":");
        jPanel1.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 180, 10, 20));

        startTimeMinuteSelectNC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startTimeMinuteSelectNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeMinuteSelectNCActionPerformed(evt);
            }
        });
        jPanel1.add(startTimeMinuteSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 180, 80, 20));

        endTimeMinuteSelectNC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        endTimeMinuteSelectNC.setSelectedIndex(9);
        jPanel1.add(endTimeMinuteSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 210, 80, 20));

        endTimeHourSelectNC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        endTimeHourSelectNC.setSelectedIndex(6);
        jPanel1.add(endTimeHourSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 210, 70, 20));

        startTimeHourSelectNC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        startTimeHourSelectNC.setSelectedIndex(6);
        startTimeHourSelectNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeHourSelectNCActionPerformed(evt);
            }
        });
        jPanel1.add(startTimeHourSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 180, 70, 20));
        jPanel1.add(appointmentDateChooserNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 260, 30));

        lessonUnitInputNC.setText("1.0");
        lessonUnitInputNC.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel1.add(lessonUnitInputNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 260, 30));

        lessonTypeSelectNC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Private", "Group", "Party" }));
        jPanel1.add(lessonTypeSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 260, 30));

        programPaneNC.setEditable(false);
        programPaneNC.setDisabledTextColor(new java.awt.Color(51, 51, 51));
        jPanel1.add(programPaneNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, 260, 30));

        jPanel1.add(instructorSelectNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 260, 30));

        attendBtnNC.setText("Attend No Charge");
        attendBtnNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnNCActionPerformed(evt);
            }
        });
        jPanel1.add(attendBtnNC, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 260, 180, 40));

        attendPurchaseLessonTabbedPane.addTab("No Charge Lesson", jPanel1);

        upperPanel.add(attendPurchaseLessonTabbedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 50, 860, 370));

        jLabel24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel24.setText("Recent Lessons:");
        upperPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, -1, 10));

        scheduledLessonCheckbox.setSelected(true);
        scheduledLessonCheckbox.setText("Scheduled Lesson");
        scheduledLessonCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scheduledLessonCheckboxActionPerformed(evt);
            }
        });
        upperPanel.add(scheduledLessonCheckbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 450, -1, -1));

        deleteLessonBtn.setText("Delete Lesson");
        deleteLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLessonBtnActionPerformed(evt);
            }
        });
        upperPanel.add(deleteLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 420, 200, -1));

        cancelLessonBtn.setText("Cancel Lesson");
        cancelLessonBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelLessonBtnActionPerformed(evt);
            }
        });
        upperPanel.add(cancelLessonBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 390, 200, -1));

        privateTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateTotalPane.setText("0.0");
        upperPanel.add(privateTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 430, 40, 20));

        instructorIDLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel.setText("Private Total:");
        upperPanel.add(instructorIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 430, -1, 20));

        instructorIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel1.setText("Private Used:");
        upperPanel.add(instructorIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 450, -1, 20));

        privateUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateUsedPane.setText("0.0");
        upperPanel.add(privateUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 450, 40, 20));

        instructorIDLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel2.setText("Group Used:");
        upperPanel.add(instructorIDLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 450, -1, 20));

        instructorIDLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel3.setText("Group Total:");
        upperPanel.add(instructorIDLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 430, -1, 20));

        groupTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupTotalPane.setText("0.0");
        upperPanel.add(groupTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 430, 40, 20));

        groupUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupUsedPane.setText("0.0");
        upperPanel.add(groupUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 450, 40, 20));

        instructorIDLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel4.setText("Party Used:");
        upperPanel.add(instructorIDLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 450, -1, 20));

        partyTotalPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyTotalPane.setText("0.0");
        upperPanel.add(partyTotalPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 430, 40, 20));

        partyUsedPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyUsedPane.setText("0.0");
        upperPanel.add(partyUsedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 450, 40, 20));

        instructorIDLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel5.setText("Party Total:");
        upperPanel.add(instructorIDLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 430, -1, 20));

        contentPanel.add(upperPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 1170, 490));

        lessonScheduleBtn.setText("View Schedule");
        lessonScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lessonScheduleBtnActionPerformed(evt);
            }
        });
        contentPanel.add(lessonScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 60, 160, 40));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

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
        enrollmentTableScrollPane.setViewportView(enrollmentTable);

        jPanel2.add(enrollmentTableScrollPane, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        contentPanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 610, 1190, 150));

        contentScrollPane.setViewportView(contentPanel);

        getContentPane().add(contentScrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed

        // Open previous view
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                openPreviousView();
                StudentAttendLesson.this.dispose();
            }
        };
        thr.start();

    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void editLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLessonBtnActionPerformed
        // Get Current Lesson ID
        int row = lessonTable.convertRowIndexToModel(lessonTable.getSelectedRow());
        String currentLessonID = (String) lessonTable.getModel().getValueAt(row, 7);

        // Open Edit Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                EditLesson editLesson = new EditLesson(currentLessonID, StudentAttendLesson.this, "StudentAttendPurchaseLesson");
                editLesson.setLocationRelativeTo(null);
                editLesson.setVisible(true);
            }
        };
        thr.start();

    }//GEN-LAST:event_editLessonBtnActionPerformed

    private void attendBtnPAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnPAActionPerformed

        // Record Payment and Attend Lesson
        try {
            new AttendPurchaseLesson().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_attendBtnPAActionPerformed

    private void attendBtnBAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnBAActionPerformed

        // Record Bonus and Attend Lesson
        try {
            new AttendBonusLesson().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }//GEN-LAST:event_attendBtnBAActionPerformed

    private void attendBtnAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnAActionPerformed
        // Attend Lesson
        try {
            new AttendOnly().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_attendBtnAActionPerformed

    private void attendBtnPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnPActionPerformed
        // Create Payment
        try {
            new PurchaseOnly().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_attendBtnPActionPerformed

    private void deleteLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLessonBtnActionPerformed

        // Delete lesson from Lessons Table
        deleteLessonFromLessonsTable();


    }//GEN-LAST:event_deleteLessonBtnActionPerformed

    private void cancelLessonBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelLessonBtnActionPerformed

        // Cancel lesson from Lessons Table
        cancelLessonFromLessonsTable();
    }//GEN-LAST:event_cancelLessonBtnActionPerformed

    private void scheduledLessonCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduledLessonCheckboxActionPerformed

        // Update UI
        updateUI();

    }//GEN-LAST:event_scheduledLessonCheckboxActionPerformed

    private void enrollmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enrollmentTableMouseClicked

        if (!scheduledLessonCheckbox.isSelected()) {
            // Update UI
            updateUI();
        }

    }//GEN-LAST:event_enrollmentTableMouseClicked

    private void attendBtnNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnNCActionPerformed

        // Create No Charge Attendance
        try {
            new NoChargeLesson().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_attendBtnNCActionPerformed

    private void startTimeHourSelectPAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeHourSelectPAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectPA, startTimeMinuteSelectPA, endTimeHourSelectPA, endTimeMinuteSelectPA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeHourSelectPAActionPerformed

    private void startTimeMinuteSelectPAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeMinuteSelectPAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectPA, startTimeMinuteSelectPA, endTimeHourSelectPA, endTimeMinuteSelectPA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeMinuteSelectPAActionPerformed

    private void startTimeHourSelectBAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeHourSelectBAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectBA, startTimeMinuteSelectBA, endTimeHourSelectBA, endTimeMinuteSelectBA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeHourSelectBAActionPerformed

    private void startTimeMinuteSelectBAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeMinuteSelectBAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectBA, startTimeMinuteSelectBA, endTimeHourSelectBA, endTimeMinuteSelectBA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeMinuteSelectBAActionPerformed

    private void startTimeHourSelectAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeHourSelectAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectA, startTimeMinuteSelectA, endTimeHourSelectA, endTimeMinuteSelectA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeHourSelectAActionPerformed

    private void startTimeMinuteSelectAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeMinuteSelectAActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectA, startTimeMinuteSelectA, endTimeHourSelectA, endTimeMinuteSelectA);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeMinuteSelectAActionPerformed

    private void startTimeHourSelectNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeHourSelectNCActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectNC, startTimeMinuteSelectNC, endTimeHourSelectNC, endTimeMinuteSelectNC);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeHourSelectNCActionPerformed

    private void startTimeMinuteSelectNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startTimeMinuteSelectNCActionPerformed

        // Change end time to next 45 minute
        try {
            comboBoxHelper.updateEndTime(startTimeHourSelectNC, startTimeMinuteSelectNC, endTimeHourSelectNC, endTimeMinuteSelectNC);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_startTimeMinuteSelectNCActionPerformed

    private void lessonScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lessonScheduleBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentAttendPurchaseLesson");

        // Open Lesson Schedule
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                LessonSchedule lessonSchedule = new LessonSchedule();
                lessonSchedule.setVisible(true);
                StudentAttendLesson.this.dispose();
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
            java.util.logging.Logger.getLogger(StudentAttendLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentAttendLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentAttendLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentAttendLesson.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentAttendLesson().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser appointmentDateChooserA;
    private com.toedter.calendar.JDateChooser appointmentDateChooserBA;
    private com.toedter.calendar.JDateChooser appointmentDateChooserNC;
    private com.toedter.calendar.JDateChooser appointmentDateChooserPA;
    private javax.swing.JButton attendBtnA;
    private javax.swing.JButton attendBtnBA;
    private javax.swing.JButton attendBtnNC;
    private javax.swing.JButton attendBtnP;
    private javax.swing.JButton attendBtnPA;
    private javax.swing.JTabbedPane attendPurchaseLessonTabbedPane;
    private javax.swing.JTextField availableBonusesGroupBA;
    private javax.swing.JTextField availableBonusesPartyBA;
    private javax.swing.JTextField availableBonusesPrivateBA;
    private javax.swing.JButton back_menu_button;
    private com.toedter.calendar.JDateChooser bonusDateChooserBA;
    private javax.swing.JButton cancelLessonBtn;
    private javax.swing.JTextField codeInputA;
    private javax.swing.JTextField codeInputBA;
    private javax.swing.JTextField codeInputNC;
    private javax.swing.JTextField codeInputPA;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JButton deleteLessonBtn;
    private javax.swing.JButton editLessonBtn;
    private javax.swing.JComboBox endTimeHourSelectA;
    private javax.swing.JComboBox endTimeHourSelectBA;
    private javax.swing.JComboBox endTimeHourSelectNC;
    private javax.swing.JComboBox endTimeHourSelectPA;
    private javax.swing.JComboBox endTimeMinuteSelectA;
    private javax.swing.JComboBox endTimeMinuteSelectBA;
    private javax.swing.JComboBox endTimeMinuteSelectNC;
    private javax.swing.JComboBox endTimeMinuteSelectPA;
    private javax.swing.JTable enrollmentTable;
    private javax.swing.JScrollPane enrollmentTableScrollPane;
    private javax.swing.JLabel groupTotalPane;
    private javax.swing.JLabel groupUsedPane;
    private javax.swing.JLabel instructorIDLabel;
    private javax.swing.JLabel instructorIDLabel1;
    private javax.swing.JLabel instructorIDLabel2;
    private javax.swing.JLabel instructorIDLabel3;
    private javax.swing.JLabel instructorIDLabel4;
    private javax.swing.JLabel instructorIDLabel5;
    private javax.swing.JComboBox instructorSelectA;
    private javax.swing.JComboBox instructorSelectBA;
    private javax.swing.JComboBox instructorSelectNC;
    private javax.swing.JComboBox instructorSelectPA;
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
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lessonPriceGroupP;
    private javax.swing.JTextField lessonPriceGroupPA;
    private javax.swing.JTextField lessonPricePartyP;
    private javax.swing.JTextField lessonPricePartyPA;
    private javax.swing.JTextField lessonPricePrivateP;
    private javax.swing.JTextField lessonPricePrivatePA;
    private javax.swing.JButton lessonScheduleBtn;
    private javax.swing.JTable lessonTable;
    private javax.swing.JComboBox lessonTypeSelectA;
    private javax.swing.JComboBox lessonTypeSelectBA;
    private javax.swing.JComboBox lessonTypeSelectNC;
    private javax.swing.JComboBox lessonTypeSelectP;
    private javax.swing.JComboBox lessonTypeSelectPA;
    private javax.swing.JTextField lessonUnitInputA;
    private javax.swing.JTextField lessonUnitInputBA;
    private javax.swing.JTextField lessonUnitInputNC;
    private javax.swing.JTextField lessonUnitInputPA;
    private javax.swing.JLabel mainTitle;
    private javax.swing.JTextField notesInputA;
    private javax.swing.JTextField notesInputBA;
    private javax.swing.JTextField notesInputNC;
    private javax.swing.JTextField notesInputPA;
    private javax.swing.JLabel partyTotalPane;
    private javax.swing.JLabel partyUsedPane;
    private javax.swing.JPanel payAttendPanel;
    private com.toedter.calendar.JDateChooser paymentDateChooserP;
    private com.toedter.calendar.JDateChooser paymentDateChooserPA;
    private javax.swing.JLabel privateTotalPane;
    private javax.swing.JLabel privateUsedPane;
    private javax.swing.JTextField programPaneA;
    private javax.swing.JTextField programPaneBA;
    private javax.swing.JTextField programPaneNC;
    private javax.swing.JTextField programPaneP;
    private javax.swing.JTextField programPanePA;
    private javax.swing.JTextField purchaseTotalInputP;
    private javax.swing.JTextField purchaseTotalInputPA;
    private javax.swing.JComboBox purchaseTypeSelectP;
    private javax.swing.JComboBox purchaseTypeSelectPA;
    private javax.swing.JCheckBox scheduledLessonCheckbox;
    private javax.swing.JComboBox startTimeHourSelectA;
    private javax.swing.JComboBox startTimeHourSelectBA;
    private javax.swing.JComboBox startTimeHourSelectNC;
    private javax.swing.JComboBox startTimeHourSelectPA;
    private javax.swing.JComboBox startTimeMinuteSelectA;
    private javax.swing.JComboBox startTimeMinuteSelectBA;
    private javax.swing.JComboBox startTimeMinuteSelectNC;
    private javax.swing.JComboBox startTimeMinuteSelectPA;
    private javax.swing.JLabel studentNameField;
    private javax.swing.JLabel topLogo;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
