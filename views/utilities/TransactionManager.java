/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.utilities;

import static commons.helpers.ServerHelper.connection;
import commons.helpers.GenericHelper;
import commons.helpers.TableHelper;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author daynehammes
 */
public class TransactionManager extends javax.swing.JFrame {

    private GenericHelper genericHelper = new GenericHelper();
    private TableHelper tableHelper = new TableHelper();
    private DefaultTableModel transactionTableModel;
    private TableRowSorter transactionSorter;

    /**
     * Creates new form Menu
     */
    public TransactionManager() {

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

        // Set initial fields
        setFields();
    }

    // Sets initial fields
    private void setFields() {

        // Set current year as default transaction period
        genericHelper.setInitialDates(startDateChooser, endDateChooser);

        // Remove lines from table
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 0));
        transactionTable.setFillsViewportHeight(true);

    }

    // Update data for selected item in table
    private void updateUIFromTable() {

        // Check whether initialization or mouse click
        int row = transactionTable.convertRowIndexToModel(transactionTable.getSelectedRow());

        // Get lesson values
        String transactionID = (String) transactionTable.getModel().getValueAt(row, 0);
        String studentName = (String) transactionTable.getModel().getValueAt(row, 1);
        String studentID = (String) transactionTable.getModel().getValueAt(row, 2);
        String programID = (String) transactionTable.getModel().getValueAt(row, 3);
        String amount = (String) transactionTable.getModel().getValueAt(row, 4);
        String lessonUnits = (String) transactionTable.getModel().getValueAt(row, 5);
        String lessonType = (String) transactionTable.getModel().getValueAt(row, 6);
        String paymentType = (String) transactionTable.getModel().getValueAt(row, 7);
        String dateCreated = (String) transactionTable.getModel().getValueAt(row, 8);

        // Update ui items
        transactionIDPane.setText(transactionID);
        studentNamePane.setText(studentName);
        studentIDPane.setText(studentID);
        programIDPane.setText(programID);
        amountPane.setText(amount);
        unitsPane.setText(lessonUnits);
        paymentTypePane.setText(paymentType);
        lessonTypePane.setText(lessonType);
        dateCreatedPane.setText(dateCreated);

    }

    // Populate Students JTable
    public void populateTransactionsTable() throws ClassNotFoundException, SQLException {

        // Check input
        if (startDateChooser.getDate() == null || endDateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Please choose a start and end date.",
                    "Choose Dates", JOptionPane.WARNING_MESSAGE);
        } else {
            // Get date variables
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = new String(dateFormat.format(startDateChooser.getDate()));
            String endDate = new String(dateFormat.format(endDateChooser.getDate()));

            try {

                // Get all payments for date
                ResultSet transactionSet = connection.prepareStatement(String.format("select PaymentID,StudentID,NULL,EnrollmentID,Amount,LessonUnits,PaymentType,LessonType,DateCreated"
                        + " from PaymentTransaction where DateCreated between DATE '%s' and '%s' UNION select transaction_id,student_id,enrollment_id_from,enrollment_id_usedon,"
                        + "NULL,units_used,NULL,bonus_lesson_type,DateCreated"
                        + " from BonusTransaction where DateCreated between DATE '%s' and '%s';", startDate, endDate, startDate, endDate)).executeQuery();

                // Create JTable model
                transactionTableModel = new DefaultTableModel();

                // Create columns
                Object[] tableColumns = new Object[]{"Transaction ID", "Student Name", "Student ID", "Enrollment ID From", "Enrollment ID Used On",
                    "Amount", "Units", "Lesson Type", "Payment Type", "Date Created"};
                transactionTableModel.setColumnIdentifiers(tableColumns);

                // Set sorters
                transactionSorter = new TableRowSorter<DefaultTableModel>(transactionTableModel);

                // Add Payment data to table
                while (transactionSet.next()) {

                    // Get variables
                    String transactionID = transactionSet.getString(1);
                    String studentID = transactionSet.getString(2);
                    String enrollmentID = transactionSet.getString(3);
                    String amount = transactionSet.getString(4);
                    String lessonUnits = transactionSet.getString(5);
                    String paymentType = transactionSet.getString(6);
                    String lessonType = transactionSet.getString(7);
                    String dateCreated = transactionSet.getString(8);

                    // Check null values from Bonus table and replace
                    if (amount == null) {
                        amount = "0.00";
                    }
                    if (paymentType == null) {
                        paymentType = "Bonus";
                    }
                    if (enrollmentID == null) {
                        enrollmentID = "-10000";
                    }

                    // Get Student Name
                    ResultSet studentSet = connection.prepareStatement(String.format("select LName,FName from Students where StudentID='%s';", studentID)).executeQuery();
                    String studentName = "N/A";
                    if (studentSet.next()) {
                        studentName = studentSet.getString(1) + ", " + studentSet.getString(2);
                    }

                    // Get Program Id
                    String programID = "N/A";
                    if (paymentType.equals("Bonus")) {
                        programID = enrollmentID;
                    } else {
                        ResultSet programSet = connection.prepareStatement(String.format("select ProgramID from ProgramEnrollment where EnrollmentID='%s';", enrollmentID)).executeQuery();

                        if (programSet.next()) {
                            programID = programSet.getString(1);
                        }
                    }

                    // Set Transaction Id to identify payment or bonus
                    if (paymentType.equals("Bonus")) {
                        transactionID = "B" + transactionID;
                    } else {
                        transactionID = "P" + transactionID;
                    }

                    // Run forloop, first create String[] object then model.addRow
                    String[] row = new String[]{transactionID, studentName, studentID, programID, amount, lessonUnits, lessonType, paymentType, dateCreated};

                    transactionTableModel.addRow(row);

                }

                // Set data/model to JTable
                transactionTable.setModel(transactionTableModel);
                transactionTable.setRowSorter(transactionSorter);

                // Hide unnecessary columns
                tableHelper.hideTrivialTableColumns(transactionTable, tableColumns.length - 1, tableColumns.length - 1);

                // Initialize filter listeners
                initializeTransactionTableFilters();

                // Remove lines from table
                transactionTable.setShowGrid(false);
                transactionTable.setIntercellSpacing(new Dimension(0, 0));
                transactionTable.setFillsViewportHeight(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // Initialize filter listeners
    private void initializeTransactionTableFilters() {

        // Create new document listener
        DocumentListener textChangedListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                searchFilterAllColumns();
            }

            public void removeUpdate(DocumentEvent e) {
                searchFilterAllColumns();
            }

            public void insertUpdate(DocumentEvent e) {
                searchFilterAllColumns();
            }
        };

        // Set search field listener
        searchInput.getDocument().addDocumentListener(textChangedListener);
    }

    // Search Filter for input text
    private void searchFilterAllColumns() {
        RowFilter<DefaultTableModel, Object> rf = null;
        List<RowFilter<Object, Object>> rfs
                = new ArrayList<RowFilter<Object, Object>>();

        // Check for any matching pattern in columns
        try {

            String text = searchInput.getText();
            String[] textArray = text.split(" ");

            for (int i = 0; i < textArray.length; i++) {

                // Add column indexes to check
                rfs.add(RowFilter.regexFilter("(?i)" + textArray[i], 0, 1, 2, 3, 4, 5, 6, 7, 8));
            }

            rf = RowFilter.andFilter(rfs);

        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        transactionSorter.setRowFilter(rf);
    }

    // Delete transaction
    private void deleteTransaction() throws ClassNotFoundException, SQLException {

        // Check for row
        if (transactionTable.getSelectedRowCount() > 0) {

            // Confirm deletion
            int confirmUpdate = JOptionPane.showConfirmDialog(null, "Really delete transaction record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirmUpdate == JOptionPane.YES_OPTION) {

                // Get lesson id
                int row = transactionTable.convertRowIndexToModel(transactionTable.getSelectedRow());
                String paymentType = (String) transactionTable.getModel().getValueAt(row, 7);
                String transactionID = ((String) transactionTable.getModel().getValueAt(row, 0)).substring(1);

                try {

                    // Check type of payment
                    if (paymentType.equals("Bonus")) {

                        // Delete payment from Payments
                        connection.prepareStatement("delete from Bonuses where RecordID='" + transactionID + "';").execute();

                        // Alert success
                        JOptionPane.showMessageDialog(null, "Successfully deleted bonus with Bonus ID: " + transactionID + ".",
                                "Deleted Bonus", JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        // Delete payment from Payments
                        connection.prepareStatement("delete from Payments where PaymentID='" + transactionID + "';").execute();

                        // Alert success
                        JOptionPane.showMessageDialog(null, "Successfully deleted payment with Payment ID: " + transactionID + ".",
                                "Deleted Payment", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Repopulate Transactions table
                    try {
                        populateTransactionsTable();
                        transactionTable.repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            JOptionPane.showMessageDialog(null, "Please select a transaction record.",
                    "No Record Selected", JOptionPane.WARNING_MESSAGE);
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

        formatButtonGroup = new javax.swing.ButtonGroup();
        back_menu_button = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        studentIDLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        cityLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        unitsPane = new javax.swing.JTextField();
        programIDPane = new javax.swing.JTextField();
        studentIDPane = new javax.swing.JTextField();
        transactionIDPane = new javax.swing.JTextField();
        studentIDLabel1 = new javax.swing.JLabel();
        paymentTypePane = new javax.swing.JTextField();
        lessonTypePane = new javax.swing.JTextField();
        addressLabel1 = new javax.swing.JLabel();
        cityLabel1 = new javax.swing.JLabel();
        dateCreatedPane = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        studentNamePane = new javax.swing.JTextField();
        amountPane = new javax.swing.JTextField();
        backBtn = new javax.swing.JButton();
        mainLogo = new javax.swing.JLabel();
        mainTitleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        deleteTransaction = new javax.swing.JButton();
        startDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        retrieveTransactionsBtn = new javax.swing.JButton();
        searchInput = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        transactionTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(843, 706));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        getContentPane().add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentIDLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        studentIDLabel.setText("Transaction ID:");
        jPanel1.add(studentIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        addressLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        addressLabel.setText("Student ID:");
        jPanel1.add(addressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        cityLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cityLabel.setText("Program ID:");
        jPanel1.add(cityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Amount:");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 60, 90, 10));

        unitsPane.setEditable(false);
        unitsPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(unitsPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 80, 130, 30));

        programIDPane.setEditable(false);
        programIDPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(programIDPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 130, 30));

        studentIDPane.setEditable(false);
        studentIDPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(studentIDPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 50, 130, 30));

        transactionIDPane.setEditable(false);
        transactionIDPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(transactionIDPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 130, 30));

        studentIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        studentIDLabel1.setText("Transaction Type:");
        jPanel1.add(studentIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 60, -1, -1));

        paymentTypePane.setEditable(false);
        paymentTypePane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(paymentTypePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 50, 130, 30));

        lessonTypePane.setEditable(false);
        lessonTypePane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(lessonTypePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 20, 130, 30));

        addressLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        addressLabel1.setText("Lesson Type:");
        jPanel1.add(addressLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 30, -1, -1));

        cityLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        cityLabel1.setText("Date Created:");
        jPanel1.add(cityLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 90, -1, -1));

        dateCreatedPane.setEditable(false);
        dateCreatedPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(dateCreatedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 80, 130, 30));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Units:");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 90, 90, 10));

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Student Name:");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 30, 90, 10));

        studentNamePane.setEditable(false);
        studentNamePane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(studentNamePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 20, 130, 30));

        amountPane.setEditable(false);
        amountPane.setMargin(new java.awt.Insets(0, 2, 0, 0));
        jPanel1.add(amountPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 50, 130, 30));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 820, 140));

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });
        getContentPane().add(backBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        mainLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(mainLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 370, 90));

        mainTitleLabel.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitleLabel.setText("Transaction Manager");
        getContentPane().add(mainTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 83, 280, 30));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        deleteTransaction.setText("Delete Transaction");
        deleteTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTransactionActionPerformed(evt);
            }
        });
        jPanel2.add(deleteTransaction, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 180, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 120, 210, 50));
        getContentPane().add(startDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 160, 30));
        getContentPane().add(endDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 280, 160, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel6.setText("to");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 280, 20, 30));

        retrieveTransactionsBtn.setText("Retrieve Transactions");
        retrieveTransactionsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveTransactionsBtnActionPerformed(evt);
            }
        });
        getContentPane().add(retrieveTransactionsBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 280, -1, 30));

        searchInput.setToolTipText("Filter Students");
        getContentPane().add(searchInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, 160, 30));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel7.setText("Filter:");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 40, 30));

        transactionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Student ID", "Program ID", "Amount", "Units", "Payment Type", "Date Created"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        transactionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                transactionTableMouseReleased(evt);
            }
        });
        transactionTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                transactionTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(transactionTable);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 380, 1090, 250));

        setBounds(0, 0, 1088, 650);
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed
        // Open main menu
        MaintenanceMenu maintenanceMenu = new MaintenanceMenu();
        maintenanceMenu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // maintenance menu
        MaintenanceMenu maintenanceMenu = new MaintenanceMenu();
        maintenanceMenu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void deleteTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTransactionActionPerformed
        // Delete transaction from database
        try {
            deleteTransaction();
        } catch (Exception e) {

            // Alert failure
            JOptionPane.showMessageDialog(null, "There was a problem deleting transaction. Please try again.",
                    "Delete Transaction Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_deleteTransactionActionPerformed

    private void retrieveTransactionsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveTransactionsBtnActionPerformed
        // populate table
        try {
            populateTransactionsTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_retrieveTransactionsBtnActionPerformed

    private void transactionTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transactionTableMouseReleased

        // Update UI
        updateUIFromTable();
    }//GEN-LAST:event_transactionTableMouseReleased

    private void transactionTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_transactionTableKeyReleased

        // Update UI
        updateUIFromTable();
    }//GEN-LAST:event_transactionTableKeyReleased

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
            java.util.logging.Logger.getLogger(TransactionManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TransactionManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TransactionManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TransactionManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TransactionManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressLabel1;
    private javax.swing.JTextField amountPane;
    private javax.swing.JButton backBtn;
    private javax.swing.JButton back_menu_button;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JLabel cityLabel1;
    private javax.swing.JTextField dateCreatedPane;
    private javax.swing.JButton deleteTransaction;
    private com.toedter.calendar.JDateChooser endDateChooser;
    private javax.swing.ButtonGroup formatButtonGroup;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lessonTypePane;
    private javax.swing.JLabel mainLogo;
    private javax.swing.JLabel mainTitleLabel;
    private javax.swing.JTextField paymentTypePane;
    private javax.swing.JTextField programIDPane;
    private javax.swing.JButton retrieveTransactionsBtn;
    private javax.swing.JTextField searchInput;
    private com.toedter.calendar.JDateChooser startDateChooser;
    private javax.swing.JLabel studentIDLabel;
    private javax.swing.JLabel studentIDLabel1;
    private javax.swing.JTextField studentIDPane;
    private javax.swing.JTextField studentNamePane;
    private javax.swing.JTextField transactionIDPane;
    private javax.swing.JTable transactionTable;
    private javax.swing.JTextField unitsPane;
    // End of variables declaration//GEN-END:variables
}
