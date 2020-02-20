/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.entities;

import static commons.helpers.ServerHelper.connection;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import static views.main.FADSApp.jarDirectory;

/**
 *
 * @author daynehammes
 */
public class Report {

    private DateFormat reportDateFormat;
    
    public Report() {
        
        reportDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    }

    // Run Query and Generate Report
    public void generateQueryReport(String sqlQuery, String reportFileName, String reportTitle, String[] reportHeaders,
            String currentPersonName, int columnCount, int[] dateColumns)
            throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Create Writer
        BufferedWriter writer = null;

        try {

            // Get ResultSet for query
            ResultSet resultSet = connection.prepareStatement(sqlQuery).executeQuery();

            // Create Report File
            File reportFile = createReportFile(reportFileName);

            // Instantiate Writer
            writer = new BufferedWriter(new FileWriter(reportFile));

            // Begin HTML String with Headers
            StringBuilder reportHTML = startHTMLReportSingleReport(reportFile, reportTitle, reportHeaders, currentPersonName);

            // Add Rows to HTML String
            while (resultSet.next()) {

                // Start new Row
                reportHTML.append("<tr>");

                // Add each Column to Report HTML
                for (int i = 1; i <= columnCount; i++) {
                    
                    // Check if DateCreated column and format if so
                    boolean isDateCreatedColumn = false;
                    for (int x = 0; x < dateColumns.length; x++) {
                        if (i==dateColumns[x]) {
                            isDateCreatedColumn = true;
                        }
                    }
                    
                    // Check for special report instructions
                    if (isDateCreatedColumn) {
                        String formattedDate = reportDateFormat.format(resultSet.getDate(i)).toString();
                        reportHTML.append(String.format("<td>%s</td>", formattedDate));
                    } else if ( (reportFileName.equals("StudentLessonHistory")) && (i==2) ) {
                        reportHTML.append(String.format("<td>%s</td>", resultSet.getString(i).substring(0, 5) ));
                    } else {
                        reportHTML.append(String.format("<td>%s</td>", resultSet.getString(i)));
                    }
                }

                // End new Row
                reportHTML.append("</tr>");

            }

            // Close HTML string
            reportHTML.append("</tbody></table></div></body></html>");

            // Write string to file
            writer.write(reportHTML.toString());

            // Open Report
            try {
                Desktop.getDesktop().browse(reportFile.toURI());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Close the writer
            writer.close();
        } catch (Exception e) {
        }

    }

    // Run Query and Generate Report with Aggregate Value
    public void generateAggregateQueryReport(String sqlQuery, String aggregateSQLQuery, String reportFileName, String reportTitle, String[] reportHeaders,
            String currentPersonName, int columnCount, int[] dateColumns) throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Create Writer
        BufferedWriter writer = null;

        try {

            // Get AggregateSet for query
            ResultSet aggregateSet = connection.prepareStatement(aggregateSQLQuery).executeQuery();

            // Get ResultSet for query
            ResultSet resultSet = connection.prepareStatement(sqlQuery).executeQuery();

            // Create Report File
            File reportFile = createReportFile(reportFileName);

            // Instantiate Writer
            writer = new BufferedWriter(new FileWriter(reportFile));

            // Begin HTML String with Headers
            StringBuilder reportHTML = startHTMLReportSingleReport(reportFile, reportTitle, reportHeaders, currentPersonName);

            // Get Aggregate value
            String aggregateValue = "N/A";
            if (aggregateSet.next()) {

                // Get Aggregate value
                aggregateValue = aggregateSet.getString(1);

            }

            // Add Rows to HTML String
            while (resultSet.next()) {

                // Start new Row
                reportHTML.append("<tr>");

                // Add each Column to Report HTML
                for (int i = 1; i <= columnCount; i++) {
                    
                    // Check if DateCreated column and format if so
                    boolean isDateCreatedColumn = false;
                    for (int x = 0; x < dateColumns.length; x++) {
                        if (i==dateColumns[x]) {
                            isDateCreatedColumn = true;
                        }
                    }
                    
                    // Check for special report instructions
                    if (isDateCreatedColumn) {
                        
                        String formattedDate = reportDateFormat.format(resultSet.getDate(i)).toString();
                        reportHTML.append(String.format("<td>%s</td>", formattedDate));
                        
                    } else if (i == columnCount) {

                        // Add aggregate column
                        reportHTML.append(String.format("<td>%s</td>", aggregateValue));

                    } else {

                        // Add normal column
                        reportHTML.append(String.format("<td>%s</td>", resultSet.getString(i)));

                    }

                }

                // End new Row
                reportHTML.append("</tr>");

            }

            // Close HTML string
            reportHTML.append("</tbody></table></div></body></html>");

            // Write string to file
            writer.write(reportHTML.toString());

            // Open Report
            try {
                Desktop.getDesktop().browse(reportFile.toURI());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Close the writer
            writer.close();
        } catch (Exception e) {
        }

    }

    // Run Query and Generate Report
    public void generateNonQueryReportSingleReport(String reportFileName, String reportTitle, List<List<Object>> reportArrayList, String[] reportHeaders, String currentPersonName, int columnCount)
            throws ClassNotFoundException, SQLException, IOException, URISyntaxException {

        // Create Writer
        BufferedWriter writer = null;

        try {

            // Create Report File
            File reportFile = createReportFile(reportFileName);

            // Instantiate Writer
            writer = new BufferedWriter(new FileWriter(reportFile));

            // Begin HTML String with Headers
            StringBuilder reportHTML = startHTMLReportSingleReport(reportFile, reportTitle, reportHeaders, currentPersonName);

            // Add Rows to HTML String
            for (List<Object> currentReportRow : reportArrayList) {

                // Start new Row
                reportHTML.append("<tr>");

                // Add each Column to Report HTML
                for (int i = 0; i < columnCount; i++) {

                    // Add normal column
                    reportHTML.append(String.format("<td>%s</td>", (String) currentReportRow.get(i)));

                }

                // End new Row
                reportHTML.append("</tr>");

            }

            // Close HTML string
            reportHTML.append("</tbody></table></div></body></html>");

            // Write string to file
            writer.write(reportHTML.toString());

            // Open Report
            try {
                Desktop.getDesktop().browse(reportFile.toURI());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Close the writer
            writer.close();
        } catch (Exception e) {
        }

    }

    // Run Query and Generate Report
    public void generateNonQueryReportMultiReport(String reportFileName, String reportHeader, String reportSubHeader, 
            List<Map<String, Object>> reportsArrayList) throws ClassNotFoundException, SQLException, IOException, 
            URISyntaxException {

        // Example JSON structure of reportsArrayList
        /*
         [
         { 
         "title" : "", 
         "subtitle" : "",
         "headers" : [], 
         "data" : [  [], [] ] 
         },
        
         {}        
         ]
         */
        // Create Writer
        BufferedWriter writer = null;

        try {

            // Create Report File
            File reportFile = createReportFile(reportFileName);

            // Instantiate Writer
            writer = new BufferedWriter(new FileWriter(reportFile));

            // Begin HTML String with Headers
            StringBuilder reportHTML = startHTMLReportMultiReport(reportFile, reportHeader, reportSubHeader);

            // Iterate over reports
            for (Map<String, Object> reportMap : reportsArrayList) {

                // Add header for report
                reportHTML.append(String.format("<div class='row text-center studio-report-section'><h5>%s</h5></div>"
                        + "<table class='table table-striped table-bordered table-condensed'><thead><tr>",
                        reportMap.get("title")));

                // Iterate over headers and append
                List<String> currentHeaders = (ArrayList) reportMap.get("headers");

                for (String currentHeader : currentHeaders) {

                    reportHTML.append(String.format("<th>%s</th>", currentHeader));

                }

                // End headers
                reportHTML.append("</tr></thead><tbody>");

                // Iterate over data and append
                List<List<Object>> reportData = (ArrayList) reportMap.get("data");

                for (List<Object> currentReportRow : reportData) {

                    // Start new Row
                    reportHTML.append("<tr>");

                    // Add each Column to Report HTML
                    for (Object currentDataItem : currentReportRow) {

                        // Add normal column
                        reportHTML.append(String.format("<td>%s</td>", (String) currentDataItem));

                    }

                    // End new Row
                    reportHTML.append("</tr>");

                }

                reportHTML.append("</tbody></table><div class='pagebreak'> </div>");
            }

            // Close HTML string
            reportHTML.append("</div></body></html>");

            // Write string to file
            writer.write(reportHTML.toString());

            // Open Report
            try {
                Desktop.getDesktop().browse(reportFile.toURI());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Close the writer
            writer.close();
        } catch (Exception e) {
        }

    }

    public String getLessonPriceFromType(String lessonType, String[] lessonPrices) {

        if (lessonType.equals("Private")) {
            return lessonPrices[0];
        } else if (lessonType.equals("Group")) {
            return lessonPrices[1];
        } else if (lessonType.equals("Party")) {
            return lessonPrices[2];
        }

        return lessonPrices[0];

    }

    public String getWhiteSheetFrontOrBackEnd(String programGroup) {

        if (programGroup.equals("Renewal")) {
            return "B";
        } else {
            return "F";
        }
    }

    public String getWhiteSheetTypeColumn(boolean isFirstLesson, String programGroup, String studentReferralType, String studentID) throws SQLException, ParseException {

        if (isFirstLesson) {

            if (programGroup.equals("Original")) {

                // Check if former student
                String studentLessonQuery = String.format("SELECT AppointmentDate FROM LessonSchedule WHERE StudentID='%s' ORDER BY AppointmentDate DESC;", studentID);
                ResultSet studentLessonSet = connection.prepareStatement(studentLessonQuery).executeQuery();
                if (studentLessonSet.next()) {

                    // Check if appointment date is over a year ago from today
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date todaysDate = new Date();
                    Date lastAppointmentDate = dateFormat.parse(studentLessonSet.getString(1));

                    int dateDifferenceInDays = (int) ((todaysDate.getTime() - lastAppointmentDate.getTime()) / (1000 * 60 * 60 * 24));

                    if (dateDifferenceInDays > 364) {
                        return "Former Student";
                    }

                }
                return studentReferralType;
            }
            return "Deposit";
        } else {
            return "Payment";
        }
    }

    public String[] getWhiteSheetOtherInstructorNames(String enrollmentID) throws SQLException {

        String[] otherInstructorNames = {"", ""};
        String enrollmentQuery = String.format("SELECT InstructorID1, InstructorID2 FROM ProgramEnrollment WHERE EnrollmentID='%s';", enrollmentID);
        ResultSet enrollmentSet = connection.prepareStatement(enrollmentQuery).executeQuery();

        if (enrollmentSet.next()) {

            String instructorID2 = enrollmentSet.getString(1);
            String instructorID3 = enrollmentSet.getString(2);

            String instructorQuery2 = String.format("SELECT LName FROM Instructors WHERE InstructorID='%s';", instructorID2);
            String instructorQuery3 = String.format("SELECT LName FROM Instructors WHERE InstructorID='%s';", instructorID3);

            ResultSet instructorSet2 = connection.prepareStatement(instructorQuery2).executeQuery();
            ResultSet instructorSet3 = connection.prepareStatement(instructorQuery3).executeQuery();

            if (instructorSet2.next()) {
                otherInstructorNames[0] = instructorSet2.getString(1);
            }
            if (instructorSet3.next()) {
                otherInstructorNames[1] = instructorSet3.getString(1);
            }
        }

        return otherInstructorNames;
    }

    // Check if should use date
    public boolean getUseAllDates(JCheckBox studentAllDatesCheck) {

        boolean useDate = false;

        if (studentAllDatesCheck.isSelected()) {
            useDate = true;
        }

        return useDate;

    }
    
    // Check if student is under/over 18 making them Qualified/Unqualified
    public String getStudentIsQualified(Date birthDate) {
        
            String isQualified = "Q";
            
            // First check if date has been set to default
            System.out.println("birth="+birthDate.toString());
            
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18);
            Date eighteenYearDate = calendar.getTime();
            System.out.println(eighteenYearDate.toString());
            
            if (birthDate.after(eighteenYearDate)) {
                isQualified = "UQ";
            }
            
            return isQualified;
    }
    
    // Create Report File
    public File createReportFile(String reportFileName) throws URISyntaxException {

        // Create Report File Path
        String reportFilePath = String.format("%s/reports/%s.html", jarDirectory, reportFileName);

        // Create Report File
        File reportFile = new File(reportFilePath);

        return reportFile;
    }

    // Begin HTML String with Headers
    public StringBuilder startHTMLReportSingleReport(File reportFile, String reportTitle, String[] reportHeaders, String currentPersonName) throws IOException {

        // Create new StringBuilder
        StringBuilder reportHTML = new StringBuilder();

        // Get canonical path for static files
        String staticResourcesPath = String.format("file://%s/reports", jarDirectory);

        // Append Title and begin Report HTML
        reportHTML.append("<html><head><link href='" + staticResourcesPath + "/css/bootstrap.css' rel='stylesheet' media='screen'>"
                + "<link href='" + staticResourcesPath + "/css/custom.css' rel='stylesheet' media='screen'>"
                + "<link rel='stylesheet' type='text/css' media='print' href='" + staticResourcesPath + "/css/bootstrap.css'>"
                + "<link rel='stylesheet' type='text/css' media='print' href='" + staticResourcesPath + "/css/custom.css'>"
                + "<link rel='icon' type='image/png' href='" + staticResourcesPath + "/img/icon16.png'>"
                + "<title>FADS - " + reportTitle + "</title></head><body>"
                + "<div class='container-fluid'>"
                + "<div class='row text-center'><img src='" + staticResourcesPath + "/img/fadsLogo.png' />"
                + "<h3>" + reportTitle + "</h3><h5>" + currentPersonName + "</h5></div>"
                + "<table class='table table-striped table-bordered table-condensed'><thead><tr>");

        // For Windows, replace backslash with forward slash
        // Append all Headers HTML
        for (int i = 0; i < reportHeaders.length; i++) {

            reportHTML.append(String.format("<th>%s</th>", reportHeaders[i]));
        }

        // Close Headers HTML and begin Rows HTML
        reportHTML.append("</tr></thead><tbody>");

        return reportHTML;
    }

    // Begin HTML String with Headers
    public StringBuilder startHTMLReportMultiReport(File reportFile, String reportHeader, String reportSubHeader)
            throws IOException {

        // Create new StringBuilder
        StringBuilder reportHTML = new StringBuilder();

        // Get canonical path for static files
        String reportsDirPath = String.format("%s/reports", jarDirectory);

        // Append Title and begin Report HTML
        reportHTML.append(String.format("<html><head><link href='%s/css/bootstrap.css' rel='stylesheet' media='screen'>"
                + "<link href='%s/css/custom.css' rel='stylesheet' media='screen'>"
                + "<link rel='stylesheet' type='text/css' media='print' href='%s/css/bootstrap.css'>"
                + "<link rel='stylesheet' type='text/css' media='print' href='%s/css/custom.css'>"
                + "<link rel='icon' type='image/png' href='%s/img/icon16.png'>"
                + "<title>FADS - " + reportHeader + "</title></head><body>"
                + "<div class='container-fluid'><div class='row text-center'><img src='%s/img/fadsLogo.png' />"
                        + "<h3>" + reportHeader + "</h3><h5>" + reportSubHeader + "</h5></div>", reportsDirPath, reportsDirPath, reportsDirPath, reportsDirPath, reportsDirPath, reportsDirPath));

        return reportHTML;
    }

}
