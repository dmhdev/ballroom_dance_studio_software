/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.schedule;

import views.lesson.EditLesson;
import views.student.StudentAttendLesson;
import commons.helpers.ServerHelper;
import commons.entities.Lesson;
import commons.helpers.GenericHelper;
import static commons.helpers.NavHelper.addToNavHistory;
import static commons.helpers.ServerHelper.connection;
import static commons.helpers.NavHelper.scheduleDaysOffset;
import static commons.helpers.NavHelper.studentID;
import static views.main.FADSApp.jarDirectory;
import views.main.MainMenu;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import views.lesson.LessonInfo;

/**
 *
 * @author daynehammes
 */
public class LessonSchedule extends javax.swing.JFrame {

    private ServerHelper serverHelper = new ServerHelper();
    private GenericHelper genericHelper = new GenericHelper();
    private StudentAttendLesson attendPurchaseLesson = null;
    private String currentStudentID, currentLessonID;
    private static String[] weekDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String[] startTimes = {"8:00 AM", "8:45 AM", "9:30 AM", "10:15 AM", "11:00 AM", "11:45 AM", "12:30 PM", "1:15 PM", "2:00 PM", "2:45 PM", "3:30 PM", "4:15 PM",
        "5:00 PM", "5:45 PM", "6:30 PM", "7:15 PM", "8:00 PM", "8:45 PM", "9:30 PM", "10:15 PM", "11:00 PM", "11:45 PM"};
    private ButtonGroup lessonButtonGroup = new ButtonGroup();
    private List<JToggleButton> lessonButtonList = new ArrayList<JToggleButton>();
    private JPanel schedulePanel = null;
    private String[] formattedDate;
    private boolean dateChooserIsActive = true;

    /**
     * Creates new form Menu
     */
    public LessonSchedule() {

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

        // Start date change listener
        lessonDateChooser.getDateEditor().addPropertyChangeListener(
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (("date".equals(e.getPropertyName())) && (dateChooserIsActive)) {

                    Date dateFromDateChooser = lessonDateChooser.getDate();
                    updateScheduleByDate(dateFromDateChooser);
                }
            }
        });

        // Set Values from Database
        try {
            populateSchedule();
            schedulePanel.repaint();        // Necessary for Lesson Edit return
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Set Values from Database
    private void populateSchedule() throws ClassNotFoundException, SQLException, IOException, ParseException {

        try {

            // Create date and panel lists for update
            formattedDate = getFormattedDate();

            // Set Datechooser date
            dateChooserIsActive = false;
            Date dateWithOffset = new SimpleDateFormat("yyyy-MM-dd").parse(formattedDate[0]);
            lessonDateChooser.setDate(dateWithOffset);
            dateChooserIsActive = true;

            // Get LessonSchedule list for current day
            Statement stmt = connection.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet lessonSet = stmt.executeQuery(
                    "SELECT LS.LessonID,LS.StudentID,LS.EnrollmentID,LS.InstructorID,LS.InstructorName,LS.AppointmentTimeStart,LS.AppointmentTimeEnd,LS.StudentName,LS.LessonCode,LS.InstructorPriority,"
                    + "LS.LessonStatus,LS.LastLesson,LS.OwesPayment,LS.AmountDue,PR.UnlimitedLessons from LessonSchedule as LS INNER JOIN ProgramEnrollment as EN ON "
                    + "EN.EnrollmentID=LS.EnrollmentID INNER JOIN Programs as PR ON PR.ProgramID=EN.ProgramID WHERE LS.AppointmentDate='" + formattedDate[0]
                    + "' AND LS.LessonStatus!='OldDatabase' ORDER BY LS.InstructorPriority,LS.InstructorName,LS.AppointmentTimeStart DESC;");

            // Initialize Schedule layout and create Start Time column
            schedulePanel = createScheduleLayout();

            // Populate lesson list
            String previousInstructor = null;
            int currentColumn = 2;
            int previousRow = 0;        // Rows correspond with startTimes array length + 1 (+1 accounts for header row)

            while (lessonSet.next()) {

                // Get variables
                String lessonID = lessonSet.getString(1);
                String studentID = lessonSet.getString(2);
                String enrollmentID = lessonSet.getString(3);
                String currentInstructor = lessonSet.getString(4);
                String instructorName = lessonSet.getString(5);
                String appointmentStartTime = lessonSet.getString(6);
                String appointmentEndTime = lessonSet.getString(7);
                String studentName = lessonSet.getString(8);
                String lessonCode = lessonSet.getString(9);
                int instructorPriority = lessonSet.getInt(10);
                String lessonStatus = lessonSet.getString(11);
                Boolean lastLesson = lessonSet.getBoolean(12);
                Boolean owesPayment = lessonSet.getBoolean(13);
                String amountDue = lessonSet.getString(14);
                boolean unlimitedLessons = lessonSet.getBoolean(15);

                // Jackie wanted lessonCode to include amount due
                if (genericHelper.stringNotNull(amountDue)) {

                    if (genericHelper.stringNotNull(lessonCode)) {
                        lessonCode = String.format("%s / $%s", lessonCode, amountDue);
                    } else {
                        lessonCode = String.format("$%s", amountDue);
                    }
                }

                // Check if instructor is different from previous instructor
                if ((!currentInstructor.equals(previousInstructor)) || (previousInstructor == null)) {

                    // Increment current column
                    currentColumn += 3;

                    // Reset row to first
                    previousRow = 0;

                    // Add Instructor Name as new Header
                    addInstructorHeader(instructorName, currentColumn);

                    // Add Lesson Button to row
                    previousRow = addLessonButton(lessonID, enrollmentID, studentName, appointmentStartTime, appointmentEndTime,
                            lessonCode, studentID, lastLesson, lessonStatus, owesPayment, currentColumn, previousRow, unlimitedLessons);

                } else {

                    // Add the next Lesson Button
                    previousRow = addLessonButton(lessonID, enrollmentID, studentName, appointmentStartTime, appointmentEndTime,
                            lessonCode, studentID, lastLesson, lessonStatus, owesPayment, currentColumn, previousRow, unlimitedLessons);

                }

                // Set previous instructor to current instructor
                previousInstructor = currentInstructor;
            }

            // Set Date header
            dateHeader.setText(formattedDate[1]);

            // Show schedule panel
            schedulePanel.setBackground(new Color(234, 234, 234));
            schedulePanel.setVisible(true);
            scheduleScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateScheduleByDate(Date chosenDate) {

        // Get days offset from current day
        Date todaysDate = new Date();
        
        // Update current offset, reset toggle buttons and update jpanels
        scheduleDaysOffset = genericHelper.daysBetween(todaysDate, chosenDate);
        //System.out.println("today: " + todaysDate.toString());
        //System.out.println("chosen: " + chosenDate.toString());
        //System.out.println("offset: " + scheduleDaysOffset);

        if (scheduleDaysOffset >= 0) {
            scheduleDaysOffset += 1;
        }

        currentLessonID = null;
        currentStudentID = null;
        lessonButtonList.clear();
        try {
            populateSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Add Lesson Button to Row
    private int addLessonButton(String lessonID, String enrollmentID, String studentName, String appointmentStartTime, String appointmentEndTime,
            String lessonCode, String studentID, Boolean lastLesson, String lessonStatus, boolean owesPayment, int currentColumn, int previousRow, boolean unlimitedLessons)
            throws SQLException, ParseException {

        // Create formatted time string for lesson button text
        String buttonFormattedTime = createButtonFormattedTime(appointmentStartTime, appointmentEndTime);

        // Find which row to insert button in
        String rowStartTime, rowEndTime;
        for (int i = 0; i <= startTimes.length - 1; i++) {

            // Get current time rows
            rowStartTime = startTimes[i];
            if (i == startTimes.length - 1) {
                rowEndTime = "12:30 AM";
            } else {
                rowEndTime = startTimes[i + 1];
            }

            // Check if appointment time falls within the current row's range
            if (currentStartTimeInTimeSlot(appointmentStartTime, rowStartTime, rowEndTime)) {
                // Add JButton if a button has not been added for this row
                if (!(previousRow == i + 1)) {

                    // Create lesson button
                    JToggleButton currentBtn = createSingleLessonBtn(owesPayment, lastLesson, lessonStatus, studentName, lessonCode, buttonFormattedTime, lessonID, studentID, unlimitedLessons);

                    // Set visible and add to lesson button group and schedule panel
                    currentBtn.setVisible(true);
                    lessonButtonGroup.add(currentBtn);
                    lessonButtonList.add(currentBtn);
                    schedulePanel.add(currentBtn, String.format("cell %d %d 3 1, align center, gapleft 30", currentColumn, i + 1));

                }

                // Set previous row and end loop
                previousRow = i + 1;
                break;

            }

        }

        // Return marker for this row
        return previousRow;

    }

    // Format time for button
    private String createButtonFormattedTime(String appointmentStartTime, String appointmentEndTime) throws ParseException {

        // Create time parser
        SimpleDateFormat appointmentTimeParse = new SimpleDateFormat("hh:mm");

        // Create time objects for appointment start and end times
        Date appointmentStartTimeObject = appointmentTimeParse.parse(appointmentStartTime);
        Date appointmentEndTimeObject = appointmentTimeParse.parse(appointmentEndTime);

        // Create time format for button
        SimpleDateFormat buttonTimeFormat = new SimpleDateFormat("hh:mm");

        // Create button string
        String buttonTime = buttonTimeFormat.format(appointmentStartTimeObject) + " - " + buttonTimeFormat.format(appointmentEndTimeObject);

        return buttonTime;
    }

    // Check if start time falls within current row
    private boolean currentStartTimeInTimeSlot(String appointmentStartTime, String rowStartTime, String rowEndTime) throws ParseException {

        boolean inTimeSlot = false;

        // Create time parsers
        SimpleDateFormat scheduleTimeParser = new SimpleDateFormat("h:mm a");
        SimpleDateFormat appointmentTimeParse = new SimpleDateFormat("hh:mm");

        // 12:00 requires an AM/PM designation
        SimpleDateFormat appointmentTimeParse12 = new SimpleDateFormat("hh:mm a");

        // Create time objects
        Date appointmentStartTimeObject = appointmentTimeParse.parse(appointmentStartTime);
        if (appointmentStartTime.substring(0, 2).equals("12")) {
            appointmentStartTime = appointmentStartTime.substring(0, 5) + " PM";
            appointmentStartTimeObject = appointmentTimeParse12.parse(appointmentStartTime);
        }

        Date rowStartTimeObject = scheduleTimeParser.parse(rowStartTime);
        Date rowEndTimeObject = scheduleTimeParser.parse(rowEndTime);

        // Test if start time is in row
        if ((appointmentStartTimeObject.after(rowStartTimeObject) && appointmentStartTimeObject.before(rowEndTimeObject))
                || appointmentStartTimeObject.equals(rowStartTimeObject)) {
            inTimeSlot = true;
        }

        return inTimeSlot;
    }

    // Add Instructor Name as new Header
    private void addInstructorHeader(String instructorName, int currentColumn) {

        // Create Header Label and add to current column
        JLabel instructorHeader = new JLabel(instructorName);
        instructorHeader.setHorizontalAlignment(SwingConstants.CENTER);
        instructorHeader.setFont(new Font("Serif", Font.BOLD, 16));
        instructorHeader.setVisible(true);
        // MigLayout - "cell column row width height"
        schedulePanel.add(instructorHeader, String.format("cell %d 0 3 1, align center, gapleft 30", currentColumn));

    }

    // Create schedule layout and add start times column
    private JPanel createScheduleLayout() {

        // Initialize Schedule JPanel
        JPanel schedulePanel = new JPanel(new MigLayout());
        schedulePanel.setBackground(Color.white);
        scheduleScrollPane.setViewportView(schedulePanel);

        // Create Start Times Header
        JLabel timeHeader = new JLabel("Start Time");
        timeHeader.setHorizontalAlignment(SwingConstants.CENTER);
        timeHeader.setFont(new Font("Serif", Font.BOLD, 16));
        timeHeader.setVisible(true);
        // column row width height
        schedulePanel.add(timeHeader, "cell 0 0 2 1, align center");

        // Create Start Times Rows
        for (int i = 0; i < startTimes.length; i++) {
            JLabel timeLabel = new JLabel(startTimes[i]);
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setVisible(true);
            schedulePanel.add(timeLabel, "cell 0 " + (i + 1) + " 2 1, align center, gapright 30");
        }

        return schedulePanel;

    }

    // Creates a new multiple lesson button
    private JToggleButton createSingleLessonBtn(Boolean owesPayment, Boolean lastLesson, String lessonStatus,
            String studentName, String lessonCode, String formattedTime, String lessonID, String studentID, boolean unlimitedLessons) {

        JToggleButton currentBtn = new JToggleButton();

        // Choose and set correct button icon
        ImageIcon[] buttonIcons = chooseButtonIcon(owesPayment, lastLesson, lessonStatus, unlimitedLessons);
        currentBtn.setIcon(buttonIcons[0]);
        currentBtn.setSelectedIcon(buttonIcons[1]);

        // Set background transparent
        currentBtn.setOpaque(false);
        currentBtn.setContentAreaFilled(false);
        currentBtn.setBorderPainted(false);

        // Set text and move to center
        currentBtn.setVerticalTextPosition(SwingConstants.CENTER);
        currentBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        currentBtn.setHorizontalAlignment(SwingConstants.CENTER);
        currentBtn.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
        currentBtn.setText("<html><center>" + formattedTime + "<br />"
                + studentName + "<br /><strong>" + lessonCode + "</strong></center></html>");

        // Attach lesson id to button
        currentBtn.putClientProperty("lessonID", lessonID);
        currentBtn.putClientProperty("studentID", studentID);

        return currentBtn;
    }

    // Choose correct icon for button background
    private ImageIcon[] chooseButtonIcon(boolean owesPayment, boolean lastLesson, String lessonStatus, boolean unlimitedLessons) {

        ImageIcon[] buttonIcons = new ImageIcon[2];

        if (!lastLesson && !owesPayment) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button.png"));
            }

        } else if (lastLesson && !owesPayment && unlimitedLessons) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button.png"));
            }
        } else if (lastLesson && owesPayment && unlimitedLessons) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button.png"));
            }

        } else if (lastLesson && !owesPayment && !unlimitedLessons) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_star_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_star_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button_star.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star.png"));
            }

        } else if (!lastLesson && owesPayment) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button.png"));
            }

        } else if (lastLesson && owesPayment) {

            if (lessonStatus.equals("Attended")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_star_attended.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star_attended.png"));
            } else if (lessonStatus.equals("Cancelled")) {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_star_cancelled.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star_cancelled.png"));
            } else {
                buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/owes_money_button_star.png"));
                buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button_star.png"));
            }

        } else {

            // Default Case
            buttonIcons[0] = new ImageIcon(getClass().getResource("/resources/normal_button.png"));
            buttonIcons[1] = new ImageIcon(getClass().getResource("/resources/selected_button.png"));
        }

        return buttonIcons;
    }

    // Get list of dates for current screen
    private String[] getFormattedDate() {

        String[] formattedDate = new String[2];

        // Create Calendar on date
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        // set offset and add strings to array
        calendar.add(Calendar.DATE, scheduleDaysOffset);

        // Create offset and database date string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate[0] = dateFormat.format(calendar.getTime());

        // Create column header
        String offsetDay = weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        String offsetDate = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String offsetMonth = months[calendar.get(Calendar.MONTH)];
        formattedDate[1] = offsetDay + ", " + offsetMonth + " " + offsetDate;

        return formattedDate;
    }

    // Generate HTML representation of schedule
    private void preparePrintSchedule() throws URISyntaxException, IOException, SQLException, ClassNotFoundException, ParseException, FileNotFoundException, PrintException {

        // Create String time rows array
        StringBuilder[] stringBuilderRows = new StringBuilder[startTimes.length + 1];

        // Start header's stringbuilder
        StringBuilder headerRowBuilder = new StringBuilder("<tr><th>Start Time</th>");
        stringBuilderRows[0] = headerRowBuilder;

        // Start each row with <tr> HTML tag
        for (int i = 1; i < stringBuilderRows.length; i++) {

            // Create new StringBuilder
            StringBuilder currentRowBuilder = new StringBuilder(String.format("<tr><td>%s</td>", startTimes[i - 1]));
            stringBuilderRows[i] = currentRowBuilder;
        }

        // Get lesson resultSet
        String query = String.format("select InstructorID,InstructorName,StudentName,AppointmentTimeStart,AppointmentTimeEnd,LessonCode,InstructorPriority"
                + " from LessonSchedule where AppointmentDate='%s' AND LessonStatus!='Cancelled' order by InstructorPriority,InstructorName DESC,"
                + " AppointmentTimeStart ASC;", formattedDate[0]);
        ResultSet lessonSet = serverHelper.queryDatabase(query);

        // Create HTML from lessonSet
        String previousInstructor = "";
        int currentRow = 1;
        int currentColumn = 0;

        while (lessonSet.next()) {

            // Set variables
            String instructorID = lessonSet.getString(1);
            String instructorName = lessonSet.getString(2);
            String studentName = lessonSet.getString(3);
            String appointmentTimeStart = lessonSet.getString(4);
            String appointmentTimeEnd = lessonSet.getString(5);
            String lessonCode = lessonSet.getString(6);
            String formattedLessonTime = createButtonFormattedTime(appointmentTimeStart, appointmentTimeEnd);

            // Check if Instructor is same as last
            if (!instructorID.equals(previousInstructor)) {

                // Fill in empty cells for the rest of previous column, if not printing the beginning of first column
                if (currentColumn > 0) {
                    for (int i = currentRow; i < stringBuilderRows.length; i++) {
                        // Add empty cell
                        stringBuilderRows[i].append("<td></td>");
                    }
                }

                // Add heading for new column
                stringBuilderRows[0].append(String.format("<th>%s</th>", instructorName));
                currentRow = 1;

            }

            // Iterate through time rows and find matching
            String rowStartTime, rowEndTime;
            for (int i = currentRow; i < stringBuilderRows.length; i++) {

                // Set current row times
                rowStartTime = startTimes[i - 1];

                if (i - 1 == 21) {
                    rowEndTime = startTimes[i - 1];
                } else {
                    rowEndTime = startTimes[i];
                }

                // Check if start time in this time slot
                if (currentStartTimeInTimeSlot(appointmentTimeStart, rowStartTime, rowEndTime)) {

                    // Add lesson details to cell
                    if ((lessonCode != null) && !(lessonCode.equals("None")) && !(lessonCode.equals("null")) && !(lessonCode.equals(""))) {
                        stringBuilderRows[i].append(String.format("<td>%s<br />%s<br />%s</td>", formattedLessonTime, studentName, lessonCode));

                    } else {
                        stringBuilderRows[i].append(String.format("<td>%s<br />%s</td>", formattedLessonTime, studentName));
                    }

                    // Save current row and break
                    currentRow = i + 1;
                    break;

                } else {

                    // Add empty cell
                    stringBuilderRows[i].append("<td></td>");
                }
            }

            previousInstructor = instructorID;
            currentColumn++;
        }

        // Fill in last column's remaining cells
        for (int i = currentRow; i < stringBuilderRows.length; i++) {
            // Add empty cell
            stringBuilderRows[i].append("<td></td>");
        }

        // End each row with </tr> HTML tag
        for (int i = 0; i < stringBuilderRows.length; i++) {
            stringBuilderRows[i].append("</tr>");
        }

        // Join all rows in single HTML string
        StringBuilder tableHTML = new StringBuilder("<table class='table table-striped table-bordered table-condensed'><thead>");
        tableHTML.append(stringBuilderRows[0].toString());
        tableHTML.append("</thead><tbody>");
        for (int i = 1; i < stringBuilderRows.length; i++) {

            // Append row to html string builder
            tableHTML.append(stringBuilderRows[i].toString());
        }
        tableHTML.append("</tbody></table>");

        // Send table data to be printed 
        printSchedule(tableHTML);

    }

    // Save and print HTML document
    private void printSchedule(StringBuilder tableHTML) throws FileNotFoundException, PrintException, URISyntaxException, IOException {

        // Create Writer
        BufferedWriter writer = null;

        // Create Report File
        File printFile = createPrintFile();

        // Finish HTML Data
        StringBuilder finalHTML = finishPrintHTML(tableHTML);

        // Instantiate Writer
        writer = new BufferedWriter(new FileWriter(printFile));

        // Write string to file
        writer.write(finalHTML.toString());
        writer.close();

        // Open Report
        try {
            Desktop.getDesktop().browse(printFile.toURI());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    // Add Header and Footer to table HTML
    private StringBuilder finishPrintHTML(StringBuilder tableHTML) throws IOException {

        StringBuilder finalHTML = new StringBuilder();

        // Get canonical path for static files
        String staticResourcePath = String.format("file://%s/reports", jarDirectory);

        // Append Title and begin Report HTML
        finalHTML.append("<html><head><link href='" + staticResourcePath + "/css/bootstrap.css' rel='stylesheet' media='screen'>"
                + "<link href='" + staticResourcePath + "/css/custom.css' rel='stylesheet' media='screen'>"
                + "<link rel='icon' type='image/png' href='" + staticResourcePath + "/img/icon16.png'>"
                + "<title>FADS - Lesson Schedule</title><style>th,td{text-align:center;}</style></head><body>"
                + "<div class='container'>"
                + "<div class='row text-center'><img src='" + staticResourcePath + "/img/fadsLogo.png' />"
                + "<h3>Lesson Schedule</h3></div>");

        finalHTML.append(tableHTML.toString());
        finalHTML.append("</body></html>");

        return finalHTML;
    }

    // Create Report File
    private File createPrintFile() throws URISyntaxException {

        // Create Report File Path
        String reportFilePath = getPrintFilePath();

        // Create Report File
        File reportFile = new File(reportFilePath);

        return reportFile;
    }

    // Get path to print file
    private String getPrintFilePath() throws URISyntaxException {

        // Create Report File Path
        String reportFilePath = String.format("%s/reports/%s.html", jarDirectory, "LessonSchedule");

        return reportFilePath;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scheduleScrollPane = new javax.swing.JScrollPane();
        scheduleDaysPanelWrap = new javax.swing.JPanel();
        scheduleDaysPanel = new javax.swing.JPanel();
        dateHeader = new javax.swing.JLabel();
        backwardScheduleBtn = new javax.swing.JButton();
        forwardScheduleBtn = new javax.swing.JButton();
        quickAttendBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        attendBtn = new javax.swing.JButton();
        lessonDateChooser = new com.toedter.calendar.JDateChooser();
        topLogo = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        printBtn = new javax.swing.JButton();
        back_menu_button = new javax.swing.JButton();
        infoBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(905, 679));

        scheduleScrollPane.setViewportView(null);
        getContentPane().add(scheduleScrollPane, java.awt.BorderLayout.CENTER);

        scheduleDaysPanelWrap.setLayout(new java.awt.GridBagLayout());

        scheduleDaysPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scheduleDaysPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        dateHeader.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        dateHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateHeader.setText("Tuesday, Aug 5");
        scheduleDaysPanel.add(dateHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 140, 280, -1));

        backwardScheduleBtn.setText("< Previous Day");
        backwardScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backwardScheduleBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(backwardScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 150, -1));

        forwardScheduleBtn.setText("Next Day >");
        forwardScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardScheduleBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(forwardScheduleBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 180, 150, -1));

        quickAttendBtn.setText("Quick Attend");
        quickAttendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickAttendBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(quickAttendBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 180, 140, 30));

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(cancelBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 180, 110, 30));

        editBtn.setText("Edit");
        editBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(editBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 180, 110, 30));

        attendBtn.setText("Attend");
        attendBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(attendBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 180, 110, 30));
        scheduleDaysPanel.add(lessonDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 140, 20));

        topLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/banner-slim.png"))); // NOI18N
        scheduleDaysPanel.add(topLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, 370, 90));

        title.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Lesson Schedule");
        scheduleDaysPanel.add(title, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 93, 190, 30));

        printBtn.setText("Print Schedule");
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(printBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 20, 160, -1));

        back_menu_button.setText("Back");
        back_menu_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_menu_buttonActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(back_menu_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 100, -1));

        infoBtn.setText("Info");
        infoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoBtnActionPerformed(evt);
            }
        });
        scheduleDaysPanel.add(infoBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 180, 110, 30));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.ipady = 15;
        scheduleDaysPanelWrap.add(scheduleDaysPanel, gridBagConstraints);

        getContentPane().add(scheduleDaysPanelWrap, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void back_menu_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_menu_buttonActionPerformed

        MainMenu main_menu = new MainMenu();
        main_menu.setVisible(true);
        this.dispose();


    }//GEN-LAST:event_back_menu_buttonActionPerformed

    private void forwardScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardScheduleBtnActionPerformed
        // Update current offset, reset toggle buttons and update jpanels
        scheduleDaysOffset += 1;
        currentLessonID = null;
        currentStudentID = null;
        lessonButtonList.clear();
        try {
            populateSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_forwardScheduleBtnActionPerformed

    private void backwardScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardScheduleBtnActionPerformed
        // Update current offset, reset toggle buttons and update jpanels
        scheduleDaysOffset -= 1;
        currentLessonID = null;
        currentStudentID = null;
        lessonButtonList.clear();
        try {
            populateSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_backwardScheduleBtnActionPerformed

    private void quickAttendBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickAttendBtnActionPerformed

        // Loop through toggle button group, find selected button
        currentLessonID = null;
        currentStudentID = null;
        for (int i = 0; i < lessonButtonList.size(); i++) {
            if (lessonButtonList.get(i).isSelected()) {
                // Get lesson id and break
                currentLessonID = (String) lessonButtonList.get(i).getClientProperty("lessonID");
                break;
            }
        }

        // Check if a lesson has been selected
        if (currentLessonID != null) {

            // Attend lesson by id
            try {
                Lesson lesson = new Lesson(currentLessonID);
                lesson.attendSelectedLesson();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Repaint lesson schedule
            lessonButtonList.clear();
            try {
                populateSchedule();
                schedulePanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            // Alert no lesson chosen
            JOptionPane.showMessageDialog(null, "Please choose a Lesson from the Schedule.",
                    "No Lesson Chosen", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_quickAttendBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed

        // Loop through toggle button group, find selected button
        currentLessonID = null;
        currentStudentID = null;
        for (int i = 0; i < lessonButtonList.size(); i++) {
            if (lessonButtonList.get(i).isSelected()) {
                // Get ids and break
                currentLessonID = (String) lessonButtonList.get(i).getClientProperty("lessonID");
                break;
            }
        }

        // Check if a lesson has been selected
        if (currentLessonID != null) {

            // Cancel lesson by id
            try {
                Lesson lesson = new Lesson(currentLessonID);
                lesson.cancelSelectedLesson();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Repaint lesson schedule
            lessonButtonList.clear();
            try {
                populateSchedule();
                schedulePanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            // Alert no lesson chosen
            JOptionPane.showMessageDialog(null, "Please choose a Lesson from the Schedule.",
                    "No Lesson Chosen", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_cancelBtnActionPerformed

    private void editBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBtnActionPerformed
        // Loop through toggle button group, find selected button
        currentLessonID = null;
        currentStudentID = null;
        for (int i = 0; i < lessonButtonList.size(); i++) {
            if (lessonButtonList.get(i).isSelected()) {
                // Get ids and break
                currentStudentID = (String) lessonButtonList.get(i).getClientProperty("studentID");
                currentLessonID = (String) lessonButtonList.get(i).getClientProperty("lessonID");
                break;
            }
        }

        // Check if a lesson has been selected
        if (currentLessonID != null && currentStudentID != null) {

            // Open Edit Lesson Dialog
            Thread thr = new Thread() {
                public void run() {

                    EditLesson editLesson = new EditLesson(currentLessonID, LessonSchedule.this, String.valueOf(scheduleDaysOffset));
                    editLesson.setLocationRelativeTo(null);
                    editLesson.setVisible(true);
                }
            };
            thr.start();

        } else {

            // Alert no lesson chosen
            JOptionPane.showMessageDialog(null, "Please choose a Lesson from the Schedule.",
                    "No Lesson Chosen", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_editBtnActionPerformed

    private void attendBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendBtnActionPerformed

        // Loop through toggle button group, find selected button
        currentLessonID = null;
        currentStudentID = null;
        for (int i = 0; i < lessonButtonList.size(); i++) {
            if (lessonButtonList.get(i).isSelected()) {
                // Get student id and break
                currentStudentID = (String) lessonButtonList.get(i).getClientProperty("studentID");
                break;
            }
        }

        // Check if a lesson has been selected
        if (currentStudentID != null) {

            // Add current view to Nav History
            addToNavHistory("LessonSchedule");

            // Set student id equal to chosen lesson
            studentID = currentStudentID;

            // Open attend/purchase view
            Thread thr = new Thread() {
                public void run() {

                    attendPurchaseLesson = new StudentAttendLesson();
                    attendPurchaseLesson.setVisible(true);
                    LessonSchedule.this.dispose();
                }
            };
            thr.start();
        } else {

            // Alert no lesson chosen
            JOptionPane.showMessageDialog(null, "Please choose a Lesson from the Schedule.",
                    "No Lesson Chosen", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_attendBtnActionPerformed

    private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed

        // Print out Schedule
        try {
            preparePrintSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_printBtnActionPerformed

    private void infoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBtnActionPerformed
        // Loop through toggle button group, find selected button
        currentLessonID = null;
        currentStudentID = null;
        for (int i = 0; i < lessonButtonList.size(); i++) {
            if (lessonButtonList.get(i).isSelected()) {
                // Get ids and break
                currentStudentID = (String) lessonButtonList.get(i).getClientProperty("studentID");
                currentLessonID = (String) lessonButtonList.get(i).getClientProperty("lessonID");
                break;
            }
        }

        // Check if a lesson has been selected
        if (currentLessonID != null && currentStudentID != null) {

            // Open Edit Lesson Dialog
            Thread thr = new Thread() {
                public void run() {

                    LessonInfo lessonInfo = new LessonInfo(LessonSchedule.this, LessonSchedule.this, false, currentLessonID, currentStudentID, String.valueOf(scheduleDaysOffset), "None");
                    lessonInfo.setLocationRelativeTo(null);
                    lessonInfo.setVisible(true);
                }
            };
            thr.start();

        } else {

            // Alert no lesson chosen
            JOptionPane.showMessageDialog(null, "Please choose a Lesson from the Schedule.",
                    "No Lesson Chosen", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_infoBtnActionPerformed

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
            java.util.logging.Logger.getLogger(LessonSchedule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LessonSchedule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LessonSchedule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LessonSchedule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LessonSchedule().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attendBtn;
    private javax.swing.JButton back_menu_button;
    private javax.swing.JButton backwardScheduleBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel dateHeader;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton forwardScheduleBtn;
    private javax.swing.JButton infoBtn;
    private com.toedter.calendar.JDateChooser lessonDateChooser;
    private javax.swing.JButton printBtn;
    private javax.swing.JButton quickAttendBtn;
    private javax.swing.JPanel scheduleDaysPanel;
    private javax.swing.JPanel scheduleDaysPanelWrap;
    private javax.swing.JScrollPane scheduleScrollPane;
    private javax.swing.JLabel title;
    private javax.swing.JLabel topLogo;
    // End of variables declaration//GEN-END:variables
}
