/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.utilities;

import static commons.helpers.NavHelper.openPreviousView;
import commons.helpers.ServerHelper;
import static commons.helpers.ServerHelper.connection;
import static views.main.FADSApp.jarDirectory;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import views.main.FADSApp;

/**
 *
 * @author daynehammes
 */
public class CustomSQLManager extends javax.swing.JFrame {

    private ServerHelper serverHelper = new ServerHelper();
    private String sql = "";
    private int rows = 0;
    private DefaultTableModel queryOutputTableModel = new DefaultTableModel();
    private boolean successfulBackgroundThread = false;

    /**
     * Creates new form Menu
     */
    public CustomSQLManager() {

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

        // Wrap text area words
        sqlInput.setWrapStyleWord(true);

        // Set submit button
        getRootPane().setDefaultButton(runSQLBtn);

    }

    private void runSQL() {

        // Reset output windows
        sqlOutputMessage.setText("");
        queryOutputTable.setModel(new DefaultTableModel());

        try {

            // Check for invalid commands
            if (hasInvalidCommands()) {

                JOptionPane.showMessageDialog(null, "Invalid command found. Only SELECT and UPDATE statements allowed",
                        "SQL Syntax Error", JOptionPane.ERROR_MESSAGE);

            } else {

                // Check type of input
                if (queryRadioBtn.isSelected()) {

                    // Fill table with query output
                    runSQLQuery();

                } else {

                    // Run Update statement
                    runSQLUpdate();

                }
            }

        } catch (Exception e) {

            // Add error to output
            sqlOutputMessage.setText(e.toString());

            e.printStackTrace();
        }
    }

    private boolean hasInvalidCommands() {

        sql = sqlInput.getText();

        // Check for invalid commands
        int deleteCommandIndex = sql.toLowerCase().indexOf("delete");
        int insertCommandIndex = sql.toLowerCase().indexOf("insert");
        int createCommandIndex = sql.toLowerCase().indexOf("create");
        int alterCommandIndex = sql.toLowerCase().indexOf("alter");
        int dropCommandIndex = sql.toLowerCase().indexOf("drop");

        if (deleteCommandIndex == -1 && insertCommandIndex == -1 && createCommandIndex == -1 && alterCommandIndex == -1 && 
                dropCommandIndex == -1) {
            return false;
        } else {
            return true;
        }

    }

    private void runSQLQuery() throws SQLException, InterruptedException {

        // Get SQL Query string
        sql = sqlInput.getText();

        // Ensure 'select' in sql
        if (!sql.toLowerCase().contains("select")) {

            JOptionPane.showMessageDialog(null, "Did not find 'Select' in Query statement.\nFor non-queries, choose 'Update'.",
                    "SQL Syntax Error", JOptionPane.INFORMATION_MESSAGE);

        } else {

            // Ensure ';' at end of query
            if (!sql.contains(";")) {
                sql += ";";
            }

            // Run query in background
            successfulBackgroundThread = false;
            Thread thread1 = new Thread() {
                public void run() {

                    try {

                        runSQLQueryBackgroundThread(sql);
                        successfulBackgroundThread = true;

                    } catch (Exception e) {
                        sqlOutputMessage.setText(e.toString());
                        e.printStackTrace();
                    }
                }
            };

            thread1.start();
            thread1.join();

            // Set output result message
            if (successfulBackgroundThread) {
                sqlOutputMessage.setText(String.format("Success. Retrieved %d rows.", rows));

                // Set data/model to JTable
                queryOutputTable.setModel(queryOutputTableModel);

                // Remove lines from table
                queryOutputTable.setShowGrid(false);
                queryOutputTable.setIntercellSpacing(new Dimension(0, 0));
                queryOutputTable.setFillsViewportHeight(true);
            }
        }
    }

    private void runSQLQueryBackgroundThread(String sql) throws SQLException {

        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Reset table variables
        queryOutputTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        // Create columns list
        int columnsNumber = metaData.getColumnCount();
        Object[] tableColumns = new Object[columnsNumber];
        for (int i = 1; i <= columnsNumber; i++) {
            tableColumns[i - 1] = metaData.getColumnName(i);
        }
        queryOutputTableModel.setColumnIdentifiers(tableColumns);

        // Iterate over rows and add to table model
        rows = 0;
        while (resultSet.next()) {

            // Create row
            String[] row = new String[columnsNumber];

            for (int i = 1; i <= columnsNumber; i++) {

                // Add value to current row
                row[i - 1] = resultSet.getString(i);

            }

            // Add row to model
            queryOutputTableModel.addRow(row);

            rows++;
        }

    }

    private void runSQLUpdate() throws SQLException, ClassNotFoundException, IOException, URISyntaxException, InterruptedException {

        // Get SQL Query string
        sql = sqlInput.getText();

        // Ensure 'select' not in sql
        if (sql.substring(0, 6).toLowerCase().contains("select")) {

            JOptionPane.showMessageDialog(null, "Found 'Select' in Update statement.\nPlease choose 'Query' for database queries.",
                    "SQL Syntax Error", JOptionPane.INFORMATION_MESSAGE);

        } else {

            // Ensure ';' at end of query
            if (!sql.contains(";")) {
                sql += ";";
            }

            // Run query in background
            successfulBackgroundThread = false;
            Thread thread1 = new Thread() {
                public void run() {

                    try {

                        runSQLUpdateBackgroundThread(sql);
                        successfulBackgroundThread = true;

                    } catch (Exception e) {
                        sqlOutputMessage.setText(e.toString());
                        e.printStackTrace();
                    }
                }
            };

            thread1.start();
            thread1.join();

            // Set output result message
            if (successfulBackgroundThread) {
                sqlOutputMessage.setText(String.format("Success. Updated %d rows.", rows));
            }

        }

    }

    private void runSQLUpdateBackgroundThread(String sql) throws SQLException, ClassNotFoundException, IOException, URISyntaxException {

        // Backup Database
        createNewBackup("CustomSQLBackup");

        // Run update
        rows = connection.prepareStatement(sql).executeUpdate();

    }

    // Create New Database Backup
    private void createNewBackup(String notes) throws SQLException, ClassNotFoundException, IOException, URISyntaxException {

        // Create file list as strings
        String[] databaseFileList = {"FADSDataSettings.properties", "FADSDataSettings.script"};

        // Shut down server
        serverHelper.shutdownDatabaseConnection();

        // Get current Timestamp
        Date date = new Date();
        String timestamp = String.valueOf(new Timestamp(date.getTime()).getTime());

        // Create new folder
        new File(jarDirectory + "/database_backups/" + timestamp).mkdir();

        // Copy files from JAR Directory to new folder directory
        for (int i = 0; i < databaseFileList.length; i++) {

            // Create source/destination path objects
            Path sourceFile = Paths.get(jarDirectory + "/" + databaseFileList[i]);
            Path destinationFile = Paths.get(jarDirectory + "/database_backups/" + timestamp + "/" + databaseFileList[i]);

            // Create containing folder for copy
            File dir = new File(jarDirectory + "/database_backups/" + timestamp + "/");
            dir.mkdir();

            // Copy file to destination
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);

        }

        // Create Notes file and add notes
        PrintWriter writer = new PrintWriter(jarDirectory + "/database_backups/" + timestamp + "/" + "notes.txt", "UTF-8");
        writer.println(notes);
        writer.close();

        // Restart server
        serverHelper.initiateDatabaseConnection();
    }

    // Get JAR Path
    private String getJARPath() throws URISyntaxException {

        // Get code source directory
        CodeSource codeSource = FADSApp.class.getProtectionDomain().getCodeSource();

        // Get Main JAR as File and create Directory Path
        File mainJarFile = new File(codeSource.getLocation().toURI().getPath());
        String mainJarDirectory = mainJarFile.getParentFile().getParentFile().getPath();

        return mainJarDirectory;
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
        sqlButtonGroup = new javax.swing.ButtonGroup();
        back_menu_button = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        studentIDLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sqlInput = new javax.swing.JTextArea();
        mainLogo = new javax.swing.JLabel();
        mainTitleLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        runSQLBtn = new javax.swing.JButton();
        queryRadioBtn = new javax.swing.JRadioButton();
        updateRadioBtn = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        queryOutputTable = new javax.swing.JTable();
        studentIDLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        sqlOutputMessage = new javax.swing.JTextArea();
        studentIDLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(900, 330));
        setMinimumSize(new java.awt.Dimension(900, 330));
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
        studentIDLabel.setText("SQL:");
        jPanel1.add(studentIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        sqlInput.setColumns(20);
        sqlInput.setRows(5);
        sqlInput.setTabSize(4);
        jScrollPane1.setViewportView(sqlInput);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 14, 500, 90));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, 600, 130));

        mainLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(mainLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, 360, 90));

        mainTitleLabel.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        mainTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainTitleLabel.setText("Run Custom SQL");
        getContentPane().add(mainTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 93, 280, 30));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jLabel1.setText("*Database Backup created on Update statements");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 140, -1, -1));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        runSQLBtn.setText("Run SQL");
        runSQLBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runSQLBtnActionPerformed(evt);
            }
        });
        jPanel3.add(runSQLBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 130, -1));

        sqlButtonGroup.add(queryRadioBtn);
        queryRadioBtn.setSelected(true);
        queryRadioBtn.setText("Query");
        jPanel3.add(queryRadioBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        sqlButtonGroup.add(updateRadioBtn);
        updateRadioBtn.setText("Update");
        jPanel3.add(updateRadioBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 160, 170, 130));

        queryOutputTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(queryOutputTable);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 430, 920, 280));

        studentIDLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        studentIDLabel1.setText("Output:");
        getContentPane().add(studentIDLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, -1, -1));

        sqlOutputMessage.setEditable(false);
        sqlOutputMessage.setColumns(65);
        sqlOutputMessage.setRows(3);
        jScrollPane2.setViewportView(sqlOutputMessage);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 320, 800, 60));

        studentIDLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        studentIDLabel2.setText("Result:");
        getContentPane().add(studentIDLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, -1, -1));

        setBounds(0, 0, 914, 725);
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed
        // Open main menu
        openPreviousView();
        this.dispose();
    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void runSQLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runSQLBtnActionPerformed

        runSQLBtn.setEnabled(false);

        try {
            runSQL();

        } catch (Exception e) {

            // Alert failure
            JOptionPane.showMessageDialog(null, "There was a problem running SQL. Please try again.",
                    "SQL Syntax Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        runSQLBtn.setEnabled(true);
    }//GEN-LAST:event_runSQLBtnActionPerformed

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
            java.util.logging.Logger.getLogger(CustomSQLManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CustomSQLManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CustomSQLManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CustomSQLManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomSQLManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back_menu_button;
    private javax.swing.ButtonGroup formatButtonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel mainLogo;
    private javax.swing.JLabel mainTitleLabel;
    private javax.swing.JTable queryOutputTable;
    private javax.swing.JRadioButton queryRadioBtn;
    private javax.swing.JButton runSQLBtn;
    private javax.swing.ButtonGroup sqlButtonGroup;
    private javax.swing.JTextArea sqlInput;
    private javax.swing.JTextArea sqlOutputMessage;
    private javax.swing.JLabel studentIDLabel;
    private javax.swing.JLabel studentIDLabel1;
    private javax.swing.JLabel studentIDLabel2;
    private javax.swing.JRadioButton updateRadioBtn;
    // End of variables declaration//GEN-END:variables
}
