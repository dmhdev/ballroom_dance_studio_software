/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.main;

import commons.helpers.NavHelper;
import commons.helpers.ServerHelper;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import javax.swing.UIManager;

/**
 * 2
 *
 * @author Akureyri
 */
public class FADSApp {

    static ServerHelper server = new ServerHelper();
    static NavHelper navHelper = new NavHelper();
    public static String jarDirectory = "";
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Hello
        try {
            /*for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
             if ("Nimbus".equals(info.getName())) {
             UIManager.setLookAndFeel(info.getClassName());
             break;
             }
             }*/
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // If Nimbus is not available, set the GUI to another look and feel.
            System.out.println("Nimbus L&F not available");
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e1) {
                System.out.println("Error with L&F");
            }
        }

        // Start or Build Database
        try {
            server.initiateDatabaseConnection();
        } catch (Exception e) {
            System.out.println("There was an error starting database.");
            e.printStackTrace();
        }

        // Always close database on program close
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {

                // Shutdown database
                try {
                    server.shutdownDatabaseConnection();
                } catch (Exception e) {
                    System.out.println("There was an error stopping database.");
                    e.printStackTrace();
                }
            }
        }, "Shutdown-thread"));

        // Set static path to main jar file
        try {
            CodeSource codeSource = FADSApp.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            jarDirectory = jarFile.getParentFile().getPath();
        } catch (URISyntaxException e) {

            try {
                File jarFile = new File(FADSApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                jarDirectory = jarFile.toString();
            } catch (URISyntaxException e1) {
                e.printStackTrace();
            }
        }
        
        // Add check for using Netbeans rather than standalone program
        if (jarDirectory.contains("/fadsapp/build")) {
            int lastSlash = jarDirectory.lastIndexOf("/");
            jarDirectory = jarDirectory.substring(0,lastSlash);
        }
        
        // Replace all forward slashes to make work on all OSs
        jarDirectory = jarDirectory.replace("\\", "/");
        
        
        

        // Start Main Menu
        MainMenu main_menu = new MainMenu();
        main_menu.setVisible(true);

    }


    /*
                
         
                
     // Connect to database
     ServerConnection server = new ServerConnection();
     server.startServer();
     Connection connection = null;
     try {

     // Instantiate connection
     connection = server.createConnection();

     // Get all active students in resultSet
     ResultSet resultSet = connection.prepareStatement(
     "select * from Students where Guest=FALSE;").executeQuery();

            

     } finally {

     // Close connection
     if (connection != null) {
     server.closeConnection();
     }

     // Close server
     if (server != null) {
     server.closeServer();
     }
     }
                
                
                
                
              
                
                
                
     // Drop All Data
     connection.prepareStatement("DROP SCHEMA PUBLIC CASCADE;")
     .execute();
                
     // Drop Table
     connection.prepareStatement("DROP TABLE PUBLIC.ProgramEnrollment;")
     .execute();
                
     // Insert into Instructors
     connection.prepareStatement(
     "insert into Instructors(InstructorID,FName,LName,FullTime,Active) Values "+
     "('3484000202','Jackie','Thrasher',TRUE,TRUE);")
     .execute();           
                
     // Insert into Programs
     connection.prepareStatement(
     "insert into Programs(ProgramID,ProgramName,RatePrivate,RateGroup,RateParty,Unlimited,Active) Values "+
     "('EX2','ExampleTwo',80.0,60.0,50.0,TRUE,TRUE);")
     .execute(); 
                
     // Insert into LessonSchedule
     connection.prepareStatement(
     "insert into LessonSchedule(StudentName,InstructorName,StudentID,ProgramID,RateType,AppointmentDate,AppointmentTimeStart,AppointmentTimeEnd,Status,PaymentStatus,LessonUnits,PaidWithBonus,InstructorID) "+
     "values ('Dayne Hammes','Jackie Thrasher','250612615','EX2','Private','2014-08-04','13:30:00','14:30:00','Unattended','Unpaid',1.0,False,'123456789');")
     .execute();
                
     // Update example
     connection.prepareStatement("UPDATE Students SET Guest=TRUE WHERE FName='Dayne';").execute();
                
     // Delete example
     connection.prepareStatement("DELETE FROM PUBLIC.LessonSchedule WHERE StudentID='345667899';").execute();
                
     // Alter example
     connection.prepareStatement("ALTER TABLE PUBLIC.Bonuses ADD UpdateType VARCHAR(32);").execute();
                
     // Get current date
     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
     Date date = new Date();
     String lastModified = dateFormat.format(date); 
                     
                
     // Add arbitrary properties to components
     JButton.putClientProperty("key", "value");
     JButton.getClientProperty("key");
     */
}
