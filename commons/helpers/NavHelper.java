/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.helpers;

import java.util.ArrayList;
import views.lesson.StudentScheduleLesson;
import views.main.MainMenu;
import views.program_enrollment.StudentProgramEnrollment;
import views.schedule.LessonSchedule;
import views.student.StudentAttendLesson;
import views.student.StudentDetails;
import views.student.StudentManager;
import views.utilities.MaintenanceMenu;

/**
 *
 * @author daynehammes
 */
// Remember last screen Application-wide  
public class NavHelper {

    public static String studentID = "";
    public static String studentType = "";
    public static int scheduleDaysOffset = 0;
    public static ArrayList<String> navHistory = new ArrayList<String>();

    public static void setStudentID(String sentStudentID) {

        studentID = sentStudentID;

    }

    public static void setStudentType(String sentStudentType) {

        studentType = sentStudentType;

    }

    public static void setScheduleDaysOffset(int sentScheduleDaysOffset) {

        scheduleDaysOffset = sentScheduleDaysOffset;

    }

    public static void addToNavHistory(String currentView) {

        navHistory.add(currentView);

    }

    public static void removeFromNavHistory() {

        navHistory.remove(navHistory.size() - 1);

    }

    public static void clearNavHistory() {

        navHistory.clear();

    }

    public static void printNavHistory() {

        for (String viewName : navHistory) {
            System.out.println(viewName + " ");
        }

    }

    public static String getLastViewAsString() {

        return navHistory.get(navHistory.size() - 1);

    }

    // Open previous View
    public static void openPreviousView() {
        

        if (navHistory.size() > 0) {

            // Get last view opened from List
            switch (navHistory.get(navHistory.size() - 1)) {

                case "StudentManager":
                    StudentManager studentManager = new StudentManager();
                    studentManager.setVisible(true);
                    break;

                case "NewStudentManager":
                    studentManager = new StudentManager();
                    studentManager.setVisible(true);
                    break;

                case "UnenrolledStudentManager":
                    studentManager = new StudentManager();
                    studentManager.setVisible(true);
                    break;

                case "StudentDetails":
                    StudentDetails studentDetails = new StudentDetails();
                    studentDetails.setVisible(true);
                    break;
                
                case "StudentProgramEnrollment":
                    StudentProgramEnrollment studentProgramEnrollment = new StudentProgramEnrollment();
                    studentProgramEnrollment.setVisible(true);
                    break;
                
                case "StudentScheduleLesson":
                    StudentScheduleLesson studentScheduleLesson = new StudentScheduleLesson();
                    studentScheduleLesson.setVisible(true);
                    break;
                
                case "StudentAttendPurchaseLesson":
                    StudentAttendLesson studentAttendPurchaseLesson = new StudentAttendLesson();
                    studentAttendPurchaseLesson.setVisible(true);
                    break;
                
                case "LessonSchedule":
                    LessonSchedule lessonSchedule = new LessonSchedule();
                    lessonSchedule.setVisible(true);
                    break;
                    
                case "MaintenanceMenu":
                    MaintenanceMenu maintenanceMenu = new MaintenanceMenu();
                    maintenanceMenu.setVisible(true);
                    break;
                
                default:
                    MainMenu main_menu = new MainMenu();
                    main_menu.setVisible(true);
                    break;
            }

            // Remove View from Nav History
            removeFromNavHistory();

        } else {

            // If Nav History List is empty, return to Main Menu
            setStudentID("");
            setStudentType("");
            MainMenu main_menu = new MainMenu();
            main_menu.setVisible(true);

        }

    }

}
