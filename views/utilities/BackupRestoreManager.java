/*
 * Database Manager keeps a record of all database backups that are stored in 'root/database_backups'. Backups are stored in folders with an incremental unique identifier.
 * A list of backups is kept in 'root/database_backups/database_backups.csv'.
 */
package views.utilities;

import commons.helpers.GenericHelper;
import static commons.helpers.NavHelper.openPreviousView;
import commons.helpers.ServerHelper;
import static views.main.FADSApp.jarDirectory;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author daynehammes
 */
public class BackupRestoreManager extends javax.swing.JFrame {

    private GenericHelper genericHelper = new GenericHelper();
    private ServerHelper serverHelper = new ServerHelper();
    private DefaultTableModel backupTableModel;

    /**
     * Creates new form Menu
     */
    public BackupRestoreManager() {

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

        // Set Fields
        try {
            setFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set all fields
    private void setFields() throws ClassNotFoundException, SQLException, URISyntaxException, IOException {

        // Populate Database Backups Table
        populateBackupsTable();

    }

    // Populate Database Backups Table
    private void populateBackupsTable() throws IOException, URISyntaxException {

        // Create Backup table model
        backupTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        // Create columns
        Object[] tableColumns = new Object[]{"Backup ID", "Notes", "Created At"};
        backupTableModel.setColumnIdentifiers(tableColumns);

        // Get list of filenames in Database Backups
        ArrayList<String> backupList = getListOfBackups();

        // Populate Backup Table
        for (String backup : backupList) {

            // Get date from filename
            String formattedDate = getDateFromMillis(backup);

            // Get notes from file
            String notes = getNotesString(jarDirectory + "/database_backups/" + backup);

            // Create new row
            String[] row = new String[]{backup, notes, formattedDate};

            // Add row to model
            backupTableModel.addRow(row);

        }

        // Set data/model to JTable
        databaseBackupTable.setModel(backupTableModel);

        // Remove lines from table
        databaseBackupTable.setShowGrid(false);
        databaseBackupTable.setIntercellSpacing(new Dimension(0, 0));
        databaseBackupTable.setFillsViewportHeight(true);
    }

    // Get string from notes file
    private String getNotesString(String backupDirectory) throws IOException, FileNotFoundException {

        String notes = "";

        BufferedReader br = new BufferedReader(new FileReader(backupDirectory + "/notes.txt"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            notes = sb.toString();
        } finally {
            br.close();
        }

        return notes;

    }

    // Get list of database backup directory names
    private ArrayList<String> getListOfBackups() throws URISyntaxException {

        // Create ArrayList
        ArrayList<String> backupList = new ArrayList<String>();

        // Set directory to search for files
        File[] files = new File(jarDirectory + "/database_backups/").listFiles();
        
        if (files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    backupList.add(file.getName());
                }
            }
        }

        return backupList;

    }

    // Get Date object from milliseconds string
    private String getDateFromMillis(String millisTimestamp) {

        // Convert string to long
        long milliSeconds = Long.parseLong(millisTimestamp);

        // Create date format
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);

        // Format String
        String dateString = formatter.format(calendar.getTime());

        return dateString;

    }

    // Create New Backup
    private void createNewBackup(boolean isReverting, String notes) throws SQLException, ClassNotFoundException, IOException, URISyntaxException {

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

        // Only show alert and repaint if not reverting
        if (!isReverting) {
            // Alert success
            JOptionPane.showMessageDialog(null, "Database Backup successfully created.",
                    "Databse Backup Created", JOptionPane.INFORMATION_MESSAGE);

            // Repaint table
            populateBackupsTable();
            databaseBackupTable.repaint();
        }

    }

    // Revert to Backup
    private void revertToBackup() throws SQLException, ClassNotFoundException, IOException, URISyntaxException {

        // Check if row is selected
        if (databaseBackupTable.getSelectedRowCount() <= 0) {

            // Alert success
            JOptionPane.showMessageDialog(null, "Please select a database record from the table.",
                    "Select Database Record", JOptionPane.WARNING_MESSAGE);

        } else {

            // Get selected directory name from table
            int row = databaseBackupTable.convertRowIndexToModel(databaseBackupTable.getSelectedRow());
            String backupDirectoryName = (String) databaseBackupTable.getModel().getValueAt(row, 0);

            // Confirm delete
            int confirmUpdate = JOptionPane.showConfirmDialog(null, "Are you sure you want to revert database to backup " + backupDirectoryName + "?",
                    "Confirm Database Revert", JOptionPane.YES_NO_OPTION);
            if (confirmUpdate == JOptionPane.YES_OPTION) {

                // Create backup for current database
                createNewBackup(true, "Auto Created");

                // Create file list as strings
                String[] databaseFileList = {"FADSDataSettings.properties", "FADSDataSettings.script"};

                // Shut down server
                serverHelper.shutdownDatabaseConnection();

                // Copy files from backup directory to JAR directory, replacing
                for (int i = 0; i < databaseFileList.length; i++) {

                    // Create source/destination path objects
                    Path sourceFile = Paths.get(jarDirectory + "/database_backups/" + backupDirectoryName + "/" + databaseFileList[i]);
                    Path destinationFile = Paths.get(jarDirectory + "/" + databaseFileList[i]);

                    // Copy file to destination
                    Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);

                }

                // Restart server
                serverHelper.initiateDatabaseConnection();

                // Alert success
                JOptionPane.showMessageDialog(null, "Database has been reverted to backup " + backupDirectoryName + ".",
                        "Databse Reverted", JOptionPane.INFORMATION_MESSAGE);

                // Repaint table
                populateBackupsTable();
                databaseBackupTable.repaint();
            }
        }
    }

    // Delete selected backup
    private void deleteBackup() throws IOException, URISyntaxException {

        // Check if row is selected
        if (databaseBackupTable.getSelectedRowCount() <= 0) {

            // Alert success
            JOptionPane.showMessageDialog(null, "Please select a database record from the table.",
                    "Select Database Record", JOptionPane.WARNING_MESSAGE);

        } else {

            // Get selected directory name from table
            int row = databaseBackupTable.convertRowIndexToModel(databaseBackupTable.getSelectedRow());
            String backupDirectoryName = (String) databaseBackupTable.getModel().getValueAt(row, 0);

            // Confirm delete
            int confirmUpdate = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete backup " + backupDirectoryName + "?",
                    "Confirm Backup Delete", JOptionPane.YES_NO_OPTION);
            if (confirmUpdate == JOptionPane.YES_OPTION) {

                // Create directory File
                File backupDirectoryFile = new File(jarDirectory + "/database_backups/" + backupDirectoryName);

                // Delete files in directory, then the directory itself
                String[] directoryFiles = backupDirectoryFile.list();
                for (String directoryFile : directoryFiles) {
                    File currentFile = new File(backupDirectoryFile.getPath(), directoryFile);
                    currentFile.delete();
                }
                backupDirectoryFile.delete();

            }

            // Repaint table
            populateBackupsTable();
            databaseBackupTable.repaint();

        }
    }

    

    // Disable or Enable all buttons
    private void toggleButtons(boolean enable) {

        createBackupBtn.setEnabled(enable);
        revertBackupBtn.setEnabled(enable);
        deleteBackupBtn.setEnabled(enable);
        backBtn.setEnabled(enable);

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
        topLogo = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        backBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        reportTypeLabel = new javax.swing.JLabel();
        deleteBackupBtn = new javax.swing.JButton();
        createBackupBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        databaseBackupTable = new javax.swing.JTable();
        revertBackupBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(784, 528));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        getContentPane().add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 370, 90));

        title.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Database Manager");
        getContentPane().add(title, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 83, 290, 30));

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });
        getContentPane().add(backBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        reportTypeLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        reportTypeLabel.setText("Database Backups:");
        jPanel1.add(reportTypeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, 20));

        deleteBackupBtn.setText("Delete Backup");
        deleteBackupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBackupBtnActionPerformed(evt);
            }
        });
        jPanel1.add(deleteBackupBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 150, 180, 40));

        createBackupBtn.setText("Create Backup");
        createBackupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBackupBtnActionPerformed(evt);
            }
        });
        jPanel1.add(createBackupBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 50, 180, 40));

        databaseBackupTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Type", "Notes"
            }
        ));
        jScrollPane2.setViewportView(databaseBackupTable);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 560, 160));

        revertBackupBtn.setText("Revert to Backup");
        revertBackupBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertBackupBtnActionPerformed(evt);
            }
        });
        jPanel1.add(revertBackupBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 100, 180, 40));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 830, 230));

        setBounds(0, 0, 870, 401);
    }// </editor-fold>//GEN-END:initComponents

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        // Open Previous View
        openPreviousView();
        this.dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void deleteBackupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBackupBtnActionPerformed

        // Delete Backup
        toggleButtons(false);
        Thread thr = new Thread() {
            public void run() {

                try {
                    deleteBackup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                toggleButtons(true);
            }
        };
        thr.start();


    }//GEN-LAST:event_deleteBackupBtnActionPerformed

    private void createBackupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBackupBtnActionPerformed

        // Create New Backup
        toggleButtons(false);
        Thread thr = new Thread() {
            public void run() {

                try {
                    createNewBackup(false, "User Created");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                toggleButtons(true);
            }
        };
        thr.start();


    }//GEN-LAST:event_createBackupBtnActionPerformed

    private void revertBackupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertBackupBtnActionPerformed

        // Revert to Backup
        toggleButtons(false);
        Thread thr = new Thread() {
            public void run() {

                try {
                    revertToBackup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                toggleButtons(true);
            }
        };
        thr.start();

    }//GEN-LAST:event_revertBackupBtnActionPerformed

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
            java.util.logging.Logger.getLogger(BackupRestoreManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BackupRestoreManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BackupRestoreManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BackupRestoreManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BackupRestoreManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JButton createBackupBtn;
    private javax.swing.JTable databaseBackupTable;
    private javax.swing.JButton deleteBackupBtn;
    private javax.swing.ButtonGroup formatButtonGroup;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel reportTypeLabel;
    private javax.swing.JButton revertBackupBtn;
    private javax.swing.JLabel title;
    private javax.swing.JLabel topLogo;
    // End of variables declaration//GEN-END:variables
}
