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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import commons.helpers.CustomFocusTraversalPolicy;
import static commons.helpers.NavHelper.addToNavHistory;
import static commons.helpers.NavHelper.openPreviousView;
import commons.helpers.TableHelper;
import views.schedule.LessonSchedule;
import static commons.helpers.NavHelper.studentID;

/**
 * This class allows Students to Enroll in a new Program, marking the total cost
 * of the program, the type of program and the amount of lessons included.
 */
public class StudentProgramEnrollment extends javax.swing.JFrame implements ListSelectionListener {

    private GenericHelper genericHelper = new GenericHelper();
    private ComboBoxHelper comboBoxHelper = new ComboBoxHelper();
    private TableHelper tableHelper = new TableHelper();
    private String currentProgramID;
    private List<String[]> programArrayList = new ArrayList<String[]>();
    private ArrayList<String> instructorArrayList = new ArrayList<String>();
    private DefaultTableModel enrollmentTableModel;
    private TableRowSorter enrollmentSorter;

    /**
     * Creates new form Menu
     */
    public StudentProgramEnrollment() {


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

        // Create arraylist of instructors and apply to combobox
        Thread thread1 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, primaryInstructorSelect, false);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, buddyInstructorSelect, true);
                    comboBoxHelper.populateInstructorListAndComboBox(instructorArrayList, buddyInstructorSelect2, true);

                    comboBoxHelper.setStudentDefaultTeachers(instructorArrayList, primaryInstructorSelect, buddyInstructorSelect, buddyInstructorSelect2, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // Set Student's Name
        Thread thread2 = new Thread() {
            public void run() {

                try {
                    genericHelper.setStudentName(studentNameField, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // Populate Program List
        Thread thread3 = new Thread() {
            public void run() {

                try {
                    comboBoxHelper.populateProgramList(programArrayList, programList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set initial selected program
                try {
                    currentProgramID = programArrayList.get(0)[0];
                    programList.setSelectedIndex(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Set Program List's Selection Listener
                programList.addListSelectionListener(StudentProgramEnrollment.this);

            }
        };

        // Set initial enrollment table values
        Thread thread4 = new Thread() {
            public void run() {

                try {
                    tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Set custom traversal policy
        Thread thread5 = new Thread() {
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
        thread5.start();

        // Wait for finish
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        thread5.join();

        // Set current date for new program enrollment datechooser
        Date date = new Date();
        enrollmentDateChooser.setDate(date);

        // Set Money Calculation Listeners
        setMoneyAutoCalculate();

        // Inital program rate ui
        updateProgramRateDisplay();

        // Set submit button
        getRootPane().setDefaultButton(createEnrollmentBtn);

        // Add program list change listener
        programList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {

                    // Update Rate Panes
                    try {
                        updateProgramRateDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        // Set JList as selected focus
        programList.requestFocus();

    }

    // Set Tab Traversal Policy Order
    private void setCustomTraversalPolicy() {

        // Create ordered list of components
        ArrayList<Component> componentArrayList = new ArrayList<Component>();
        componentArrayList.add(programList);
        componentArrayList.add(programGroupSelect);
        componentArrayList.add(privateLessonPrice);
        componentArrayList.add(groupLessonPrice);
        componentArrayList.add(partyLessonPrice);
        componentArrayList.add(primaryInstructorSelect);
        componentArrayList.add(buddyInstructorSelect);
        componentArrayList.add(buddyInstructorSelect2);
        componentArrayList.add(enrollmentDateChooser);
        componentArrayList.add(privateLessonsAdd);
        componentArrayList.add(groupLessonsAdd);
        componentArrayList.add(partyLessonsAdd);
        componentArrayList.add(privateBonusAdd);
        componentArrayList.add(groupBonusAdd);
        componentArrayList.add(partyBonusAdd);
        componentArrayList.add(initialPaymentInput);
        componentArrayList.add(purchaseTypeSelect);
        componentArrayList.add(createEnrollmentBtn);
        componentArrayList.add(cancelBtn);

        // Create new custom traversal policy and apply to panel
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(componentArrayList);
        enrollmentPanel.setFocusTraversalPolicyProvider(true);
        enrollmentPanel.setFocusTraversalPolicy(policy);

    }

    // Set Money Auto Calculators
    private void setMoneyAutoCalculate() {

        // Create list of textfields that require listeners
        JTextField[] moneyInputFields = {privateLessonPrice, groupLessonPrice, partyLessonPrice, privateLessonsAdd, groupLessonsAdd, partyLessonsAdd};

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
        Double privatePrice = Double.parseDouble((genericHelper.isNumericString(privateLessonPrice.getText())) ? privateLessonPrice.getText() : "0");
        Double groupPrice = Double.parseDouble((genericHelper.isNumericString(groupLessonPrice.getText())) ? groupLessonPrice.getText() : "0");
        Double partyPrice = Double.parseDouble((genericHelper.isNumericString(partyLessonPrice.getText())) ? partyLessonPrice.getText() : "0");

        Double privateLessons = Double.parseDouble((genericHelper.isNumericString(privateLessonsAdd.getText())) ? privateLessonsAdd.getText() : "0");
        Double groupLessons = Double.parseDouble((genericHelper.isNumericString(groupLessonsAdd.getText())) ? groupLessonsAdd.getText() : "0");
        Double partyLessons = Double.parseDouble((genericHelper.isNumericString(partyLessonsAdd.getText())) ? partyLessonsAdd.getText() : "0");

        // Calculate total
        Double totalPayment = ((privatePrice * privateLessons) + (groupPrice * groupLessons) + (partyPrice * partyLessons));

        // Set textfield
        totalPriceInput.setText(String.format("%.2f", totalPayment));
    }

    // Value Change Listener for Program List
    public void valueChanged(ListSelectionEvent evt) {

        // If list value changed
        if (evt.getValueIsAdjusting()) {

            // Update Current Program ID
            currentProgramID = programArrayList.get(programList.getSelectedIndex())[0];

            // Update Rate Panes
            try {
                updateProgramRateDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // Update displayed rates for program
    private void updateProgramRateDisplay() {

        privateLessonPrice.setText(programArrayList.get(programList.getSelectedIndex())[2]);
        groupLessonPrice.setText(programArrayList.get(programList.getSelectedIndex())[3]);
        partyLessonPrice.setText(programArrayList.get(programList.getSelectedIndex())[4]);
        privateBonusAdd.setText(programArrayList.get(programList.getSelectedIndex())[5]);
        groupBonusAdd.setText(programArrayList.get(programList.getSelectedIndex())[6]);
        partyBonusAdd.setText(programArrayList.get(programList.getSelectedIndex())[7]);
        privateLessonsAdd.setText(programArrayList.get(programList.getSelectedIndex())[8]);
        groupLessonsAdd.setText(programArrayList.get(programList.getSelectedIndex())[9]);
        partyLessonsAdd.setText(programArrayList.get(programList.getSelectedIndex())[10]);
        programGroupSelect.setSelectedItem(programArrayList.get(programList.getSelectedIndex())[11]);
    }

    // Create enrollment for student
    private void createEnrollmentForStudent() throws SQLException, ClassNotFoundException {

        if (currentProgramID != null) {

            try {

                // Get enrollment date
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String enrollmentDate = dateFormat.format(enrollmentDateChooser.getDate());

                // Ready variables
                Double privatePrice = Double.parseDouble((genericHelper.isNumericString(privateLessonPrice.getText())) ? privateLessonPrice.getText() : "0");
                Double groupPrice = Double.parseDouble((genericHelper.isNumericString(groupLessonPrice.getText())) ? groupLessonPrice.getText() : "0");
                Double partyPrice = Double.parseDouble((genericHelper.isNumericString(partyLessonPrice.getText())) ? partyLessonPrice.getText() : "0");

                Double privateLessons = Double.parseDouble((genericHelper.isNumericString(privateLessonsAdd.getText())) ? privateLessonsAdd.getText() : "0");
                Double groupLessons = Double.parseDouble((genericHelper.isNumericString(groupLessonsAdd.getText())) ? groupLessonsAdd.getText() : "0");
                Double partyLessons = Double.parseDouble((genericHelper.isNumericString(partyLessonsAdd.getText())) ? partyLessonsAdd.getText() : "0");
                Double privateBonus = Double.parseDouble((genericHelper.isNumericString(privateBonusAdd.getText())) ? privateBonusAdd.getText() : "0");
                Double groupBonus = Double.parseDouble((genericHelper.isNumericString(groupBonusAdd.getText())) ? groupBonusAdd.getText() : "0");
                Double partyBonus = Double.parseDouble((genericHelper.isNumericString(partyBonusAdd.getText())) ? partyBonusAdd.getText() : "0");

                Double initialPayment = Double.parseDouble((genericHelper.isNumericString(initialPaymentInput.getText())) ? initialPaymentInput.getText() : "0");
                Double contractTotal = ((privatePrice * privateLessons) + (groupPrice * groupLessons) + (partyPrice * partyLessons));

                // Get instructors from select lists
                String primaryInstructorID = comboBoxHelper.getSelectedInstructorID(primaryInstructorSelect, instructorArrayList, false);
                String buddyInstructorID = comboBoxHelper.getSelectedInstructorID(buddyInstructorSelect, instructorArrayList, true);
                String buddyInstructorID2 = comboBoxHelper.getSelectedInstructorID(buddyInstructorSelect2, instructorArrayList, true);

                // Get Program Group
                String programGroup = programGroupSelect.getSelectedItem().toString();

                // Create new entry in program enrollment
                connection.prepareStatement(String.format("insert into ProgramEnrollment(ProgramID,ProgramGroup,StudentID,PrivateLessonTotal,PrivateLessonPrice,"
                        + "GroupLessonTotal,GroupLessonPrice,PartyLessonTotal,PartyLessonPrice,BonusesAwardedPrivate,BonusesAwardedGroup,BonusesAwardedParty,"
                        + "ContractTotal,ContractPaid,PrimaryInstructorID,InstructorID1,InstructorID2,DateCreated) Values "
                        + "('%s','%s','%s',%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,'%s','%s','%s','%s');", currentProgramID, programGroup, studentID, privateLessons, privatePrice, groupLessons, groupPrice, partyLessons, partyPrice,
                        privateBonus, groupBonus, partyBonus, contractTotal, initialPayment, primaryInstructorID, buddyInstructorID, buddyInstructorID2, enrollmentDate)).execute();

                // Get most recent entered into program enrollment
                ResultSet enrollmentSet = genericHelper.getResultSet("SELECT TOP 1 EnrollmentID FROM ProgramEnrollment ORDER BY EnrollmentID DESC");
                enrollmentSet.next();
                String enrollmentID = enrollmentSet.getString(1);

                // Add Payment Record
                if (initialPayment > 0 && genericHelper.stringNotNull(enrollmentID)) {

                    // Get payment type
                    String paymentType = (String) purchaseTypeSelect.getSelectedItem();

                    // Set Payment Date
                    Date date = new Date();
                    String paymentDate = enrollmentDate;

                    genericHelper.createPaymentTransaction(studentID, paymentType, initialPayment, 0.0, enrollmentID, "Deposit", paymentDate);
                }

                // Create Bonus transaction for each type, if bonuses not set to 0
                if (privateBonus > 0) {
                    genericHelper.createBonusTransactionFromEnrollment(studentID, "Private", privateBonus, enrollmentID, enrollmentDate);
                }
                if (groupBonus > 0) {
                    genericHelper.createBonusTransactionFromEnrollment(studentID, "Group", groupBonus, enrollmentID, enrollmentDate);
                }
                if (partyBonus > 0) {
                    genericHelper.createBonusTransactionFromEnrollment(studentID, "Party", partyBonus, enrollmentID, enrollmentDate);
                }

                // Alert that there is already an active contract and prompt choice
                JOptionPane.showMessageDialog(null, "Successfully Added new Program Enrollment for Student.",
                        "Added New Enrollment", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Repopulate and repaint enrollment table
            try {
                tableHelper.populateEnrollmentTable(enrollmentTable, enrollmentSorter, enrollmentTableModel, studentID);
                enrollmentTable.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            // Alert no Programs created
            JOptionPane.showMessageDialog(null, "No Programs have been created for Program Enrollment.",
                    "No Programs Available", JOptionPane.WARNING_MESSAGE);
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
        enrollmentPanel = new javax.swing.JPanel();
        studentNameField = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        programList = new javax.swing.JList();
        jLabel14 = new javax.swing.JLabel();
        createEnrollmentBtn = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        privateLessonPrice = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        partyLessonPrice = new javax.swing.JTextField();
        groupLessonPrice = new javax.swing.JTextField();
        totalPriceInput = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        partyLessonsAdd = new javax.swing.JTextField();
        groupLessonsAdd = new javax.swing.JTextField();
        privateLessonsAdd = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        initialPaymentInput = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        primaryInstructorSelect = new javax.swing.JComboBox();
        jLabel33 = new javax.swing.JLabel();
        buddyInstructorSelect = new javax.swing.JComboBox();
        jLabel34 = new javax.swing.JLabel();
        buddyInstructorSelect2 = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        purchaseTypeSelect = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        partyBonusAdd = new javax.swing.JTextField();
        groupBonusAdd = new javax.swing.JTextField();
        privateBonusAdd = new javax.swing.JTextField();
        cancelBtn = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        programGroupSelect = new javax.swing.JComboBox();
        jLabel38 = new javax.swing.JLabel();
        enrollmentDateChooser = new com.toedter.calendar.JDateChooser();
        editEnrollmentBtn = new javax.swing.JButton();
        lessonScheduleBtn = new javax.swing.JButton();
        deleteEnrollmentBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        enrollmentTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(784, 521));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 370, 90));

        mainTitle.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitle.setText("Program Enrollment");
        getContentPane().add(mainTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 83, 290, 30));

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        getContentPane().add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        enrollmentPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        enrollmentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentNameField.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        studentNameField.setForeground(java.awt.Color.blue);
        studentNameField.setText("Example, Name");
        enrollmentPanel.add(studentNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 470, -1));

        programList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Program List" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(programList);

        enrollmentPanel.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, 230, 150));

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Program Type:");
        enrollmentPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, 20));

        createEnrollmentBtn.setText("Create Enrollment Plan");
        createEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createEnrollmentBtnActionPerformed(evt);
            }
        });
        enrollmentPanel.add(createEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 390, 200, 40));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Instructors:");
        enrollmentPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 150, -1, 20));

        privateLessonPrice.setText("0");
        enrollmentPanel.add(privateLessonPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 70, 170, 20));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Private:");
        enrollmentPanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 70, -1, 20));

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel28.setText("Group:");
        enrollmentPanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, -1, 20));

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setText("Party:");
        enrollmentPanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, -1, 20));

        partyLessonPrice.setText("0");
        enrollmentPanel.add(partyLessonPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 110, 170, 20));

        groupLessonPrice.setText("0");
        enrollmentPanel.add(groupLessonPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 90, 170, 20));

        totalPriceInput.setEditable(false);
        totalPriceInput.setText("0");
        enrollmentPanel.add(totalPriceInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 260, 160, 20));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("Total Price:");
        enrollmentPanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 260, -1, 20));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Lesson Units to Add:");
        enrollmentPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 40, -1, 20));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel19.setText("Private:");
        enrollmentPanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 70, -1, 20));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Group:");
        enrollmentPanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 90, -1, 20));

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setText("Party:");
        enrollmentPanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 110, -1, 20));

        partyLessonsAdd.setText("0");
        enrollmentPanel.add(partyLessonsAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 110, 170, 20));

        groupLessonsAdd.setText("0");
        enrollmentPanel.add(groupLessonsAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 90, 170, 20));

        privateLessonsAdd.setText("0");
        enrollmentPanel.add(privateLessonsAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 70, 170, 20));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Initial Payment:");
        enrollmentPanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 290, -1, 20));

        initialPaymentInput.setText("0");
        enrollmentPanel.add(initialPaymentInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 290, 160, 20));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Primary:");
        enrollmentPanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 180, -1, 20));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setText("Price per Lesson:");
        enrollmentPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 40, -1, 20));

        enrollmentPanel.add(primaryInstructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 180, 160, 20));

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setText("Buddy 1:");
        enrollmentPanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 210, -1, 20));

        enrollmentPanel.add(buddyInstructorSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 210, 160, 20));

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel34.setText("Buddy 2:");
        enrollmentPanel.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 240, -1, 20));

        enrollmentPanel.add(buddyInstructorSelect2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 240, 160, 20));

        jLabel21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel21.setText("Purchase Type:");
        enrollmentPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 320, -1, 20));

        purchaseTypeSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Master Card", "Visa", "Discover", "American Express", "Cash", "Check" }));
        enrollmentPanel.add(purchaseTypeSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 320, 170, -1));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("Bonus Units to Add:");
        enrollmentPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 140, -1, 20));

        jLabel22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel22.setText("Private:");
        enrollmentPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 170, -1, 20));

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel36.setText("Group:");
        enrollmentPanel.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 190, -1, 20));

        jLabel37.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel37.setText("Party:");
        enrollmentPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 210, -1, 20));

        partyBonusAdd.setText("0");
        enrollmentPanel.add(partyBonusAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 210, 170, 20));

        groupBonusAdd.setText("0");
        enrollmentPanel.add(groupBonusAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 190, 170, 20));

        privateBonusAdd.setText("0");
        enrollmentPanel.add(privateBonusAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 170, 170, 20));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        enrollmentPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 390, 100, 40));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setText("Program Group:");
        enrollmentPanel.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, -1, 20));

        programGroupSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Original", "Preliminary", "Extension", "ReExtension", "Renewal", "Other" }));
        enrollmentPanel.add(programGroupSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 270, 160, 20));

        jLabel38.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel38.setText("Date Enrolled:");
        enrollmentPanel.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 280, -1, 20));
        enrollmentPanel.add(enrollmentDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 290, 160, 30));

        getContentPane().add(enrollmentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 910, 450));

        editEnrollmentBtn.setText("Edit Enrollment");
        editEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editEnrollmentBtnActionPerformed(evt);
            }
        });
        getContentPane().add(editEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 590, 150, 30));

        lessonScheduleBtn.setText("View Schedule");
        lessonScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lessonScheduleBtnActionPerformed(evt);
            }
        });
        getContentPane().add(lessonScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 70, 160, 40));

        deleteEnrollmentBtn.setText("Delete Enrollment");
        deleteEnrollmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEnrollmentBtnActionPerformed(evt);
            }
        });
        getContentPane().add(deleteEnrollmentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 590, 150, 30));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel3.setLayout(new java.awt.BorderLayout());

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
        jScrollPane2.setViewportView(enrollmentTable);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 930, 160));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed

        // Open Previous View
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                openPreviousView();
                StudentProgramEnrollment.this.dispose();
            }
        };
        thr.start();


    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void createEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createEnrollmentBtnActionPerformed

        // Create new enrollment for student
        try {
            createEnrollmentForStudent();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }//GEN-LAST:event_createEnrollmentBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed

        // Open Previous View
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                openPreviousView();
                StudentProgramEnrollment.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void editEnrollmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEnrollmentBtnActionPerformed
        // Get selected program
        int row = enrollmentTable.convertRowIndexToModel(enrollmentTable.getSelectedRow());
        String enrollmentID = (String) enrollmentTable.getModel().getValueAt(row, 12);

        // Open Edit Lesson Dialog
        Thread thr = new Thread() {
            public void run() {

                EditProgramEnrollment editProgramEnrollment = new EditProgramEnrollment(StudentProgramEnrollment.this, enrollmentID);
                editProgramEnrollment.setLocationRelativeTo(null);
                editProgramEnrollment.setVisible(true);
            }
        };
        thr.start();
    }//GEN-LAST:event_editEnrollmentBtnActionPerformed

    private void lessonScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lessonScheduleBtnActionPerformed

        // Add current view to Nav History
        addToNavHistory("StudentProgramEnrollment");
        
        // Open Program Enrollment
        this.setEnabled(false);
        Thread thr = new Thread() {
            public void run() {

                LessonSchedule lessonSchedule = new LessonSchedule();
                lessonSchedule.setVisible(true);
                StudentProgramEnrollment.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_lessonScheduleBtnActionPerformed

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
            enrollmentTable.repaint();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }//GEN-LAST:event_deleteEnrollmentBtnActionPerformed

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
            java.util.logging.Logger.getLogger(StudentProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentProgramEnrollment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentProgramEnrollment().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back_menu_button;
    private javax.swing.JComboBox buddyInstructorSelect;
    private javax.swing.JComboBox buddyInstructorSelect2;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton createEnrollmentBtn;
    private javax.swing.JButton deleteEnrollmentBtn;
    private javax.swing.JButton editEnrollmentBtn;
    private com.toedter.calendar.JDateChooser enrollmentDateChooser;
    private javax.swing.JPanel enrollmentPanel;
    private javax.swing.JTable enrollmentTable;
    private javax.swing.JTextField groupBonusAdd;
    private javax.swing.JTextField groupLessonPrice;
    private javax.swing.JTextField groupLessonsAdd;
    private javax.swing.JTextField initialPaymentInput;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
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
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton lessonScheduleBtn;
    private javax.swing.JLabel mainTitle;
    private javax.swing.JTextField partyBonusAdd;
    private javax.swing.JTextField partyLessonPrice;
    private javax.swing.JTextField partyLessonsAdd;
    private javax.swing.JComboBox primaryInstructorSelect;
    private javax.swing.JTextField privateBonusAdd;
    private javax.swing.JTextField privateLessonPrice;
    private javax.swing.JTextField privateLessonsAdd;
    private javax.swing.JComboBox programGroupSelect;
    private javax.swing.JList programList;
    private javax.swing.JComboBox purchaseTypeSelect;
    private javax.swing.JLabel studentNameField;
    private javax.swing.JLabel topLogo;
    private javax.swing.JTextField totalPriceInput;
    // End of variables declaration//GEN-END:variables
}
