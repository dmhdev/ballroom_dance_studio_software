/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.program;

import commons.entities.Program;
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
public class ProgramManager extends javax.swing.JFrame {

    GenericHelper genericHelper = new GenericHelper();
    TableHelper tableHelper = new TableHelper();
    private DefaultTableModel programTableModel;
    private TableRowSorter programSorter;

    /**
     * Creates new form StudentsManager
     */
    public ProgramManager() {
        initComponents();
        // Set Window Icon
        try {
            BufferedImage favicon = ImageIO.read(getClass().getResource("/resources/icon16.png"));
            setIconImage(favicon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle("Students Manager - Dance Studios");
        setLocationRelativeTo(null);
        setResizable(true);

        // Set initial fields
        setFields();
    }

    // Sets initial fields
    private void setFields() {

        // Populate Programs JTable
        try {
            tableHelper.populateProgramsTable(programTable, programTableModel, programSorter, activeFilter, inactiveFilter, allFilter, searchInput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set first row to the UI
        try {
            updateUIFromTable(programTable, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wrap text area words
        programDescriptionPane.setWrapStyleWord(true);

    }

    // Update data for selected item in table
    private void updateUIFromTable(JTable target, boolean initializeUI) {

        // Check whether initialization or mouse click
        int row = target.convertRowIndexToModel(0);
        if (!initializeUI) {
            row = target.convertRowIndexToModel(target.getSelectedRow());
        }

        // Get program values
        String programName = (String) target.getModel().getValueAt(row, 0);
        String programID = (String) target.getModel().getValueAt(row, 1);
        String programDescription = (String) target.getModel().getValueAt(row, 2);
        String ratePrivate = (String) target.getModel().getValueAt(row, 3);
        String rateGroup = (String) target.getModel().getValueAt(row, 4);
        String rateParty = (String) target.getModel().getValueAt(row, 5);
        String bonusPrivate = (String) target.getModel().getValueAt(row, 6);
        String bonusGroup = (String) target.getModel().getValueAt(row, 7);
        String bonusParty = (String) target.getModel().getValueAt(row, 8);
        String lessonsPrivate = (String) target.getModel().getValueAt(row, 9);
        String lessonsGroup = (String) target.getModel().getValueAt(row, 10);
        String lessonsParty = (String) target.getModel().getValueAt(row, 11);
        String status = (String) target.getModel().getValueAt(row, 12);
        String unlimitedLessons = (String) target.getModel().getValueAt(row, 13);
        String programGroup = (String) target.getModel().getValueAt(row, 14);
        String lastModified = (String) target.getModel().getValueAt(row, 15);

        // Set program values in UI
        programNameLabel.setText(programName);
        programIDPane.setText(programID);
        activeInactiveLabel.setText(status);
        privateLessonPane.setText(lessonsPrivate);
        groupLessonPane.setText(lessonsGroup);
        unlimitedLessonsPane.setText(lessonsParty);
        privateBonusPane.setText(bonusPrivate);
        groupBonusPane.setText(bonusGroup);
        partyBonusPane.setText(bonusParty);
        privateRatePane.setText(ratePrivate);
        groupRatePane.setText(rateGroup);
        partyRatePane.setText(rateParty);
        programGroupPane.setText(programGroup);
        programDescriptionPane.setText(programDescription);
        lastModifiedLabel.setText("Last Modified: " + lastModified);

        // Active Bool
        if (unlimitedLessons.equals("True")) {
            unlimitedLessonsPane.setText("Yes");
        } else {
            unlimitedLessonsPane.setText("No");
        }

        // Set status label in UI
        if (status.equals("Active")) {
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
        programNameLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        activeInactiveLabel = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        programDescriptionPane = new javax.swing.JTextArea();
        programIDPane = new javax.swing.JLabel();
        instructorIDLabel = new javax.swing.JLabel();
        instructorIDLabel1 = new javax.swing.JLabel();
        privateLessonPane = new javax.swing.JLabel();
        groupLessonPane = new javax.swing.JLabel();
        instructorIDLabel2 = new javax.swing.JLabel();
        instructorIDLabel3 = new javax.swing.JLabel();
        unlimitedLessonsPane = new javax.swing.JLabel();
        instructorIDLabel5 = new javax.swing.JLabel();
        instructorIDLabel6 = new javax.swing.JLabel();
        instructorIDLabel7 = new javax.swing.JLabel();
        privateRatePane = new javax.swing.JLabel();
        groupRatePane = new javax.swing.JLabel();
        partyRatePane = new javax.swing.JLabel();
        instructorIDLabel8 = new javax.swing.JLabel();
        instructorIDLabel9 = new javax.swing.JLabel();
        instructorIDLabel10 = new javax.swing.JLabel();
        partyBonusPane = new javax.swing.JLabel();
        groupBonusPane = new javax.swing.JLabel();
        privateBonusPane = new javax.swing.JLabel();
        instructorIDLabel11 = new javax.swing.JLabel();
        programGroupPane = new javax.swing.JLabel();
        instructorIDLabel4 = new javax.swing.JLabel();
        partyLessonPane = new javax.swing.JLabel();
        backBtn = new javax.swing.JButton();
        mainTitleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        addProgramBtn = new javax.swing.JButton();
        editProgramBtn = new javax.swing.JButton();
        deleteProgramBtn = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        lastModifiedLabel = new javax.swing.JLabel();
        searchInput = new javax.swing.JTextField();
        allFilter = new javax.swing.JRadioButton();
        activeFilter = new javax.swing.JRadioButton();
        inactiveFilter = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        programTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(769, 609));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        mainLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(mainLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 380, 90));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        programNameLabel.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        programNameLabel.setForeground(java.awt.Color.blue);
        jPanel1.add(programNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 3, 400, -1));

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel4.setText("Status:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, -1, -1));

        activeInactiveLabel.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        activeInactiveLabel.setForeground(new java.awt.Color(255, 0, 0));
        activeInactiveLabel.setText("Active");
        jPanel1.add(activeInactiveLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 240, 60, -1));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("Description:");
        jPanel1.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 40, -1, -1));

        programDescriptionPane.setEditable(false);
        programDescriptionPane.setColumns(20);
        programDescriptionPane.setLineWrap(true);
        programDescriptionPane.setRows(5);
        jScrollPane2.setViewportView(programDescriptionPane);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 40, 170, 110));

        programIDPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        programIDPane.setText("filler");
        jPanel1.add(programIDPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, -1, -1));

        instructorIDLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel.setText("Program ID:");
        jPanel1.add(instructorIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        instructorIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel1.setText("Private Lessons:");
        jPanel1.add(instructorIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, -1));

        privateLessonPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateLessonPane.setText("filler");
        jPanel1.add(privateLessonPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 120, -1, -1));

        groupLessonPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupLessonPane.setText("filler");
        jPanel1.add(groupLessonPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 140, -1, -1));

        instructorIDLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel2.setText("Group Lessons:");
        jPanel1.add(instructorIDLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, -1, -1));

        instructorIDLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel3.setText("Party Lessons:");
        jPanel1.add(instructorIDLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, -1, -1));

        unlimitedLessonsPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        unlimitedLessonsPane.setText("filler");
        jPanel1.add(unlimitedLessonsPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 180, -1, -1));

        instructorIDLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel5.setText("Party Rate:");
        jPanel1.add(instructorIDLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        instructorIDLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel6.setText("Group Rate:");
        jPanel1.add(instructorIDLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));

        instructorIDLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel7.setText("Private Rate:");
        jPanel1.add(instructorIDLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, -1));

        privateRatePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateRatePane.setText("filler");
        jPanel1.add(privateRatePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, -1, -1));

        groupRatePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupRatePane.setText("filler");
        jPanel1.add(groupRatePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, -1, -1));

        partyRatePane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyRatePane.setText("filler");
        jPanel1.add(partyRatePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, -1, -1));

        instructorIDLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel8.setText("Party Bonus:");
        jPanel1.add(instructorIDLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, -1));

        instructorIDLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel9.setText("Group Bonus:");
        jPanel1.add(instructorIDLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 60, -1, -1));

        instructorIDLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel10.setText("Private Bonus:");
        jPanel1.add(instructorIDLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 40, -1, -1));

        partyBonusPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyBonusPane.setText("filler");
        jPanel1.add(partyBonusPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, -1, -1));

        groupBonusPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        groupBonusPane.setText("filler");
        jPanel1.add(groupBonusPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, -1, -1));

        privateBonusPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        privateBonusPane.setText("filler");
        jPanel1.add(privateBonusPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 40, -1, -1));

        instructorIDLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel11.setText("Program Group:");
        jPanel1.add(instructorIDLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 100, -1, -1));

        programGroupPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        programGroupPane.setText("filler");
        jPanel1.add(programGroupPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 100, -1, -1));

        instructorIDLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        instructorIDLabel4.setText("Unlimited Lessons:");
        jPanel1.add(instructorIDLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, -1, -1));

        partyLessonPane.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        partyLessonPane.setText("filler");
        jPanel1.add(partyLessonPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 160, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 680, 270));

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });
        getContentPane().add(backBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        mainTitleLabel.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitleLabel.setText("Program Manager");
        getContentPane().add(mainTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 80, 200, 40));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addProgramBtn.setText("Add Program");
        addProgramBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProgramBtnActionPerformed(evt);
            }
        });
        jPanel2.add(addProgramBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 151, 30));

        editProgramBtn.setText("Edit Program");
        editProgramBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProgramBtnActionPerformed(evt);
            }
        });
        jPanel2.add(editProgramBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 151, 30));

        deleteProgramBtn.setText("Delete Program");
        deleteProgramBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteProgramBtnActionPerformed(evt);
            }
        });
        jPanel2.add(deleteProgramBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 151, 30));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 120, 171, 140));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel6.setText("Filter:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 420, 40, 30));

        lastModifiedLabel.setText("Last Modified: 01/21/2014");
        getContentPane().add(lastModifiedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 20, -1, -1));

        searchInput.setToolTipText("Filter Students");
        getContentPane().add(searchInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 420, 160, 30));

        activeInactiveBtnGroup.add(allFilter);
        allFilter.setText("All");
        getContentPane().add(allFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 420, -1, 30));

        activeInactiveBtnGroup.add(activeFilter);
        activeFilter.setSelected(true);
        activeFilter.setText("Active");
        getContentPane().add(activeFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 420, -1, 30));

        activeInactiveBtnGroup.add(inactiveFilter);
        inactiveFilter.setText("Inactive");
        getContentPane().add(inactiveFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 420, -1, 30));

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        programTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Program Name", "Program ID", "Program Description", "Private Rate", "Group Rate", "Party Rate", "Unlimited", "Active", "Date Updated"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        programTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                programTableMouseReleased(evt);
            }
        });
        programTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                programTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(programTable);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 450, 910, 230));

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // TODO add your handling code here:
        MainMenu main_menu = new MainMenu();
        main_menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void programTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_programTableKeyReleased

        // Update UI
        JTable target = (JTable) evt.getSource();
        updateUIFromTable(target, false);

    }//GEN-LAST:event_programTableKeyReleased

    private void programTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_programTableMouseReleased

        // Update UI
        JTable target = (JTable) evt.getSource();
        updateUIFromTable(target, false);

    }//GEN-LAST:event_programTableMouseReleased

    private void editProgramBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProgramBtnActionPerformed

        // Open Edit Program Dialog
        EditProgram editProgram = new EditProgram(this, false, programIDPane.getText());
        editProgram.setLocationRelativeTo(null);
        editProgram.setVisible(true);
    }//GEN-LAST:event_editProgramBtnActionPerformed

    private void addProgramBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProgramBtnActionPerformed

        // Open Add Program Dialog
        AddProgram addProgram = new AddProgram(this, false);
        addProgram.setLocationRelativeTo(null);
        addProgram.setVisible(true);
    }//GEN-LAST:event_addProgramBtnActionPerformed

    private void deleteProgramBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteProgramBtnActionPerformed

        // Get selected student
        int row = programTable.convertRowIndexToModel(programTable.getSelectedRow());
        String currentProgramName = (String) programTable.getModel().getValueAt(row, 0);
        String currentProgramID = (String) programTable.getModel().getValueAt(row, 1);
        String dialogText = String.format("Really delete program %s?\nThis will remove the Program and all associated Enrollments, Lessons, Payments and Bonus Transactions.",
                currentProgramName);

        // Confirmation dialog to see if really wants to save
        int confirmUpdate = JOptionPane.showConfirmDialog(null, dialogText, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirmUpdate == JOptionPane.YES_OPTION) {

            // Delete student
            Program program = new Program(currentProgramID);
            try {
                program.deleteProgram();

                setFields();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }//GEN-LAST:event_deleteProgramBtnActionPerformed

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
            java.util.logging.Logger.getLogger(ProgramManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProgramManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProgramManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProgramManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                new ProgramManager().setVisible(true);
            }
        });

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeFilter;
    private javax.swing.ButtonGroup activeInactiveBtnGroup;
    private javax.swing.JLabel activeInactiveLabel;
    private javax.swing.JButton addProgramBtn;
    private javax.swing.JRadioButton allFilter;
    private javax.swing.JButton backBtn;
    private javax.swing.JButton deleteProgramBtn;
    private javax.swing.JButton editProgramBtn;
    private javax.swing.JLabel groupBonusPane;
    private javax.swing.JLabel groupLessonPane;
    private javax.swing.JLabel groupRatePane;
    private javax.swing.JRadioButton inactiveFilter;
    private javax.swing.JLabel instructorIDLabel;
    private javax.swing.JLabel instructorIDLabel1;
    private javax.swing.JLabel instructorIDLabel10;
    private javax.swing.JLabel instructorIDLabel11;
    private javax.swing.JLabel instructorIDLabel2;
    private javax.swing.JLabel instructorIDLabel3;
    private javax.swing.JLabel instructorIDLabel4;
    private javax.swing.JLabel instructorIDLabel5;
    private javax.swing.JLabel instructorIDLabel6;
    private javax.swing.JLabel instructorIDLabel7;
    private javax.swing.JLabel instructorIDLabel8;
    private javax.swing.JLabel instructorIDLabel9;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lastModifiedLabel;
    private javax.swing.JLabel mainLogo;
    private javax.swing.JLabel mainTitleLabel;
    private javax.swing.JLabel partyBonusPane;
    private javax.swing.JLabel partyLessonPane;
    private javax.swing.JLabel partyRatePane;
    private javax.swing.JLabel privateBonusPane;
    private javax.swing.JLabel privateLessonPane;
    private javax.swing.JLabel privateRatePane;
    private javax.swing.JTextArea programDescriptionPane;
    private javax.swing.JLabel programGroupPane;
    private javax.swing.JLabel programIDPane;
    private javax.swing.JLabel programNameLabel;
    private javax.swing.JTable programTable;
    private javax.swing.JTextField searchInput;
    private javax.swing.JLabel unlimitedLessonsPane;
    // End of variables declaration//GEN-END:variables
}
