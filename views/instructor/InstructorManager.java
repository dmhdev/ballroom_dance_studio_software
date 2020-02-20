/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.instructor;

import commons.entities.Instructor;
import views.main.MainMenu;
import commons.helpers.GenericHelper;
import commons.helpers.TableHelper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Akureyri
 */
public class InstructorManager extends javax.swing.JFrame {

    GenericHelper genericHelper = new GenericHelper();
    TableHelper tableHelper = new TableHelper();
    private DefaultTableModel instructorTableModel;
    private TableRowSorter instructorSorter;

    /**
     * Creates new form InstructorManager
     */
    public InstructorManager() {
        initComponents();
        // Set Window Icon
        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Instructor Manager - Dance Studios");
        setLocationRelativeTo(null);
        setResizable(true);

        // Set initial fields
        setFields();
    }

    // Sets initial fields
    private void setFields() {

        // Populate Instructors JTable
        try {
            tableHelper.populateInstructorsTable(instructorTable, instructorTableModel, instructorSorter, activeFilter, inactiveFilter, allFilter, searchInput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set first row to the UI
        try {
            updateUIFromTable(instructorTable, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Wrap text area words
        notesPane.setWrapStyleWord(true);

    }

    // Update data for selected item in table
    private void updateUIFromTable(JTable target, boolean initializeUI) {

        // Check whether initialization or mouse click
        int row = target.convertRowIndexToModel(0);
        if (!initializeUI) {
            row = target.convertRowIndexToModel(target.getSelectedRow());
        }

        // Get student values
        String lastName = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 0));
        String firstName = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 1));
        String instructorID = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 11));
        String status = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 16));
        String address = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 4));
        String city = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 5));
        String state = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 6));
        String zip = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 7));
        String email = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 8));
        String homePhone = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 2));
        String workPhone = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 12));
        String notes = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 13));
        String title = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 3));
        String hireDate = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 10));
        String priority = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 15));
        String lastModified = genericHelper.notAvailableFactory((String) target.getModel().getValueAt(row, 17));

        // Set student values in UI
        instructorNameLabel.setText(lastName + ", " + firstName);
        instructorIDPane.setText(instructorID);
        activeInactiveLabel.setText(status);
        addressPane.setText(address);
        cityPane.setText(city);
        statePane.setText(state);
        zipPane.setText(zip);
        emailPane.setText(email);
        homePhonePane.setText(homePhone);
        workPhonePane.setText(workPhone);
        titlePane.setText(title);
        priorityPane.setText(priority);
        hireDatePane.setText(hireDate);
        notesPane.setText(notes);
        lastModifiedLabel.setText("Last Modified: " + lastModified);

        // Set status label in UI
        if (status == "Active") {
            activeInactiveLabel.setText(status);
            activeInactiveLabel.setForeground(Color.RED);
        } else {
            activeInactiveLabel.setText(status);
            activeInactiveLabel.setForeground(Color.BLUE);
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

        activeInactiveBtnGroup = new javax.swing.ButtonGroup();
        mainLogo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        instructorNameLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        activeInactiveLabel = new javax.swing.JLabel();
        instructorIDPane = new javax.swing.JLabel();
        instructorIDLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        addressPane = new javax.swing.JLabel();
        addressLabel1 = new javax.swing.JLabel();
        cityPane = new javax.swing.JLabel();
        addressLabel2 = new javax.swing.JLabel();
        statePane = new javax.swing.JLabel();
        addressLabel3 = new javax.swing.JLabel();
        zipPane = new javax.swing.JLabel();
        addressLabel4 = new javax.swing.JLabel();
        emailPane = new javax.swing.JLabel();
        addressLabel5 = new javax.swing.JLabel();
        hireDatePane = new javax.swing.JLabel();
        instructorIDLabel1 = new javax.swing.JLabel();
        homePhonePane = new javax.swing.JLabel();
        workPhonePane = new javax.swing.JLabel();
        addressLabel6 = new javax.swing.JLabel();
        addressLabel7 = new javax.swing.JLabel();
        titlePane = new javax.swing.JLabel();
        priorityPane = new javax.swing.JLabel();
        addressLabel8 = new javax.swing.JLabel();
        addressLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        notesPane = new javax.swing.JTextArea();
        backBtn = new javax.swing.JButton();
        mainTitleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        addInstructorBtn = new javax.swing.JButton();
        editInstructorBtn = new javax.swing.JButton();
        deleteInstructorBtn = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        lastModifiedLabel = new javax.swing.JLabel();
        searchInput = new javax.swing.JTextField();
        allFilter = new javax.swing.JRadioButton();
        activeFilter = new javax.swing.JRadioButton();
        inactiveFilter = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        instructorTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(840, 598));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mainLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(mainLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 360, 90));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        instructorNameLabel.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        instructorNameLabel.setForeground(java.awt.Color.blue);
        jPanel1.add(instructorNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 3, 210, -1));

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel4.setText("Status:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 200, -1, -1));

        activeInactiveLabel.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        activeInactiveLabel.setForeground(new java.awt.Color(255, 0, 0));
        activeInactiveLabel.setText("Active");
        jPanel1.add(activeInactiveLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 200, 60, -1));

        instructorIDPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(instructorIDPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, -1, -1));

        instructorIDLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel.setText("Instructor ID:");
        jPanel1.add(instructorIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        addressLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel.setText("Address:");
        jPanel1.add(addressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        addressPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(addressPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, -1, -1));

        addressLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel1.setText("City:");
        jPanel1.add(addressLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        cityPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(cityPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, -1, -1));

        addressLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel2.setText("State:");
        jPanel1.add(addressLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        statePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(statePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 100, -1, -1));

        addressLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel3.setText("Zip:");
        jPanel1.add(addressLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        zipPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(zipPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 120, -1, -1));

        addressLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel4.setText("Email:");
        jPanel1.add(addressLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        emailPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(emailPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 140, -1, -1));

        addressLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel5.setText("Hire Date:");
        jPanel1.add(addressLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, -1));

        hireDatePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(hireDatePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 160, -1, -1));

        instructorIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel1.setText("Home Phone:");
        jPanel1.add(instructorIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 40, -1, -1));

        homePhonePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(homePhonePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, -1, -1));

        workPhonePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(workPhonePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 60, -1, -1));

        addressLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel6.setText("Work Phone:");
        jPanel1.add(addressLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 60, -1, -1));

        addressLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel7.setText("Title:");
        jPanel1.add(addressLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 80, -1, -1));

        titlePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(titlePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 80, -1, -1));

        priorityPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jPanel1.add(priorityPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 100, -1, -1));

        addressLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel8.setText("Priority:");
        jPanel1.add(addressLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 100, -1, -1));

        addressLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addressLabel9.setText("Notes:");
        jPanel1.add(addressLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 120, -1, -1));

        notesPane.setEditable(false);
        notesPane.setColumns(20);
        notesPane.setLineWrap(true);
        notesPane.setRows(2);
        jScrollPane3.setViewportView(notesPane);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 120, 220, 70));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, 230));

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });
        getContentPane().add(backBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        mainTitleLabel.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitleLabel.setText("Instructor Manager");
        getContentPane().add(mainTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 83, 200, 30));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addInstructorBtn.setText("Add Instructor");
        addInstructorBtn.setMaximumSize(new java.awt.Dimension(134, 29));
        addInstructorBtn.setMinimumSize(new java.awt.Dimension(134, 29));
        addInstructorBtn.setPreferredSize(new java.awt.Dimension(134, 29));
        addInstructorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInstructorBtnActionPerformed(evt);
            }
        });
        jPanel2.add(addInstructorBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 151, 30));

        editInstructorBtn.setText("Edit Instructor");
        editInstructorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editInstructorBtnActionPerformed(evt);
            }
        });
        jPanel2.add(editInstructorBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 151, 30));

        deleteInstructorBtn.setText("Delete Instructor");
        deleteInstructorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteInstructorBtnActionPerformed(evt);
            }
        });
        jPanel2.add(deleteInstructorBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 151, 30));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 120, 171, 140));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel6.setText("Filter:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 360, 40, 30));

        lastModifiedLabel.setText("Last Modified: 01/21/2014");
        getContentPane().add(lastModifiedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 10, -1, -1));

        searchInput.setToolTipText("Filter Students");
        getContentPane().add(searchInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, 160, 30));

        activeInactiveBtnGroup.add(allFilter);
        allFilter.setText("All");
        getContentPane().add(allFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 360, -1, 30));

        activeInactiveBtnGroup.add(activeFilter);
        activeFilter.setSelected(true);
        activeFilter.setText("Active");
        getContentPane().add(activeFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 360, -1, 30));

        activeInactiveBtnGroup.add(inactiveFilter);
        inactiveFilter.setText("Inactive");
        getContentPane().add(inactiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 360, -1, 30));

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        instructorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Last Name", "First Name", "Instructor ID", "Title", "Address", "City", "State", "Zip", "Email", "Birth Date", "Hire Date", "HomePhone", "WorkPhone", "Notes", "Full Time", "Rank", "Active", "Last Modified"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        instructorTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                instructorTableMouseReleased(evt);
            }
        });
        instructorTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                instructorTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(instructorTable);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 400, 880, 270));

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed

        // Disable all Buttons
        genericHelper.toggleAllButtonsEnabled(InstructorManager.this, false);

        // Open Main Menu
        Thread thr = new Thread() {
            public void run() {

                MainMenu main_menu = new MainMenu();
                main_menu.setVisible(true);
                InstructorManager.this.dispose();
            }
        };
        thr.start();
    }//GEN-LAST:event_backBtnActionPerformed

    private void instructorTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_instructorTableKeyReleased

        // Update UI
        JTable target = (JTable) evt.getSource();
        updateUIFromTable(target, false);

    }//GEN-LAST:event_instructorTableKeyReleased

    private void instructorTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_instructorTableMouseReleased

        // Update UI
        JTable target = (JTable) evt.getSource();
        updateUIFromTable(target, false);

    }//GEN-LAST:event_instructorTableMouseReleased

    private void editInstructorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editInstructorBtnActionPerformed

        // Open Edit Instructor Dialog
        EditInstructor editInstructor = new EditInstructor(this, false, instructorIDPane.getText());
        editInstructor.setLocationRelativeTo(null);
        editInstructor.setVisible(true);
    }//GEN-LAST:event_editInstructorBtnActionPerformed

    private void addInstructorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInstructorBtnActionPerformed

        // Open Add Instructor Dialog
        AddInstructor addInstructor = new AddInstructor(this, false);
        addInstructor.setLocationRelativeTo(null);
        addInstructor.setVisible(true);
    }//GEN-LAST:event_addInstructorBtnActionPerformed

    private void deleteInstructorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteInstructorBtnActionPerformed
     
         // Get selected student
        int row = instructorTable.convertRowIndexToModel(instructorTable.getSelectedRow());
        String currentInstructorID = (String) instructorTable.getModel().getValueAt(row, 11);
        String instructorLastName = (String) instructorTable.getModel().getValueAt(row, 0);
        String instructorFirstName = (String) instructorTable.getModel().getValueAt(row, 1);
        String dialogText = String.format("Really delete instructor %s %s?\nThis will remove the Instructor from all associated Enrollments, Lessons, Payments and Bonus Transactions.",
                instructorFirstName, instructorLastName);

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, dialogText, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Delete student
            Instructor instructor = new Instructor(currentInstructorID);
            try {
                instructor.deleteInstructor();
                
                setFields();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        
    }//GEN-LAST:event_deleteInstructorBtnActionPerformed

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
            java.util.logging.Logger.getLogger(InstructorManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InstructorManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InstructorManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InstructorManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                new InstructorManager().setVisible(true);
            }
        });

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeFilter;
    private javax.swing.ButtonGroup activeInactiveBtnGroup;
    private javax.swing.JLabel activeInactiveLabel;
    private javax.swing.JButton addInstructorBtn;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressLabel1;
    private javax.swing.JLabel addressLabel2;
    private javax.swing.JLabel addressLabel3;
    private javax.swing.JLabel addressLabel4;
    private javax.swing.JLabel addressLabel5;
    private javax.swing.JLabel addressLabel6;
    private javax.swing.JLabel addressLabel7;
    private javax.swing.JLabel addressLabel8;
    private javax.swing.JLabel addressLabel9;
    private javax.swing.JLabel addressPane;
    private javax.swing.JRadioButton allFilter;
    private javax.swing.JButton backBtn;
    private javax.swing.JLabel cityPane;
    private javax.swing.JButton deleteInstructorBtn;
    private javax.swing.JButton editInstructorBtn;
    private javax.swing.JLabel emailPane;
    private javax.swing.JLabel hireDatePane;
    private javax.swing.JLabel homePhonePane;
    private javax.swing.JRadioButton inactiveFilter;
    private javax.swing.JLabel instructorIDLabel;
    private javax.swing.JLabel instructorIDLabel1;
    private javax.swing.JLabel instructorIDPane;
    private javax.swing.JLabel instructorNameLabel;
    private javax.swing.JTable instructorTable;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lastModifiedLabel;
    private javax.swing.JLabel mainLogo;
    private javax.swing.JLabel mainTitleLabel;
    private javax.swing.JTextArea notesPane;
    private javax.swing.JLabel priorityPane;
    private javax.swing.JTextField searchInput;
    private javax.swing.JLabel statePane;
    private javax.swing.JLabel titlePane;
    private javax.swing.JLabel workPhonePane;
    private javax.swing.JLabel zipPane;
    // End of variables declaration//GEN-END:variables
}
