/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons.helpers;

import static commons.helpers.GenericHelper.studentFilter;
import static commons.helpers.GenericHelper.studentRow;
import static commons.helpers.ServerHelper.connection;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author daynehammes
 */
public class TableHelper {

    // Import Common Methods
    GenericHelper genericHelper = new GenericHelper();

    /*
     Student Table Methods
     */
    // Populate Students JTable
    public void populateStudentsTable(String studentType, JTable studentTable, TableRowSorter studentSorter, DefaultTableModel studentTableModel,
            JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter, JTextField searchInput) throws ClassNotFoundException, SQLException {

        try {

            // Get Students by type
            String studentDataQuery = "";
            if (studentType.equals("All")) {
                studentDataQuery = "select LName,Fname,Cell1,Cell2,Phone,StudentID,Active,DateUpdated,StudentID"
                        + " from Students order by LName asc;";
            } else {
                studentDataQuery = String.format("select LName,Fname,Cell1,Cell2,Phone,StudentID,Active,DateUpdated,StudentID"
                        + " from Students where StudentType='%s' order by LName asc;",
                        studentType);
            }
            ResultSet studentData = connection.prepareStatement(studentDataQuery).executeQuery();

            // Create JTable model
            studentTableModel = new DefaultTableModel() {

                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells uneditable
                    return false;
                }
            };

            // Create columns
            Object[] tableColumns = new Object[]{"Last Name", "First Name", "Cell 1", "Cell 2", "Phone", "Student ID", "Status", "Last Modified", "StudentID"};
            studentTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            studentSorter = new TableRowSorter<DefaultTableModel>(studentTableModel);

            while (studentData.next()) {

                // Get variables
                String lastName = studentData.getString(1);
                String firstName = studentData.getString(2);
                String cell1 = studentData.getString(3);
                String cell2 = studentData.getString(4);
                String phone = studentData.getString(5);
                String studentID = studentData.getString(6);
                String lastModified = (studentData.getString(8).substring(5, 7) + "/" + studentData.getString(8).substring(8) + "/" + studentData.getString(8).substring(0, 4));
                String currentStudentID = studentData.getString(9);

                // Get value of active/inactive column
                String status = "Inactive";
                if (studentData.getBoolean(7)) {
                    status = "Active";
                }

                // Create new table row
                String[] row = new String[]{lastName, firstName, cell1, cell2, phone, studentID, status, lastModified, currentStudentID};

                // Add row to model
                studentTableModel.addRow(row);

            }

            // Set data/model to JTable
            studentTable.setModel(studentTableModel);
            studentTable.setRowSorter(studentSorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(studentTable, 5, tableColumns.length);

            // Initialize filter listeners
            initializeStudentTableFilters(activeFilter, inactiveFilter, allFilter, searchInput, studentSorter);

            // Remove lines from table
            studentTable.setShowGrid(false);
            studentTable.setIntercellSpacing(new Dimension(0, 0));
            studentTable.setFillsViewportHeight(true);
            

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (studentTable.getRowCount() > 0) {
            studentTable.setRowSelectionInterval(studentRow, studentRow);
        }

    }

    // Initialize listeners for students table
    public void initializeStudentTableFilters(JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter,
            JTextField searchInput, TableRowSorter studentSorter) {

        // Create new document listener for search field
        DocumentListener textChangedListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }

            public void removeUpdate(DocumentEvent e) {
                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }

            public void insertUpdate(DocumentEvent e) {
                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }
        };

        // Set search field listener
        searchInput.getDocument().addDocumentListener(textChangedListener);

        // Radio button listeners
        activeFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }
        });
        inactiveFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }
        });
        allFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                studentSearchFilter(activeFilter, inactiveFilter, searchInput, studentSorter);
            }
        });

        // Start Students Table Filter set to previously chosen filter
        RowFilter<DefaultTableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {

            // Set starting position of filter
            String currentFilterName = "Active";
            if (studentFilter == 1) {
                currentFilterName = "Inactive";
                activeFilter.setSelected(false);
                inactiveFilter.setSelected(true);
            } else if (studentFilter == 2) {
                currentFilterName = "";
                activeFilter.setSelected(false);
                allFilter.setSelected(true);
            }

            // Has string of current state "Active" or "Inactive" and the table row that Active/Inactive status is in
            rf = RowFilter.regexFilter(currentFilterName, 6);
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
        }
        studentSorter.setRowFilter(rf);

    }

    // Search Filter for input text
    public void studentSearchFilter(JRadioButton activeFilter, JRadioButton inactiveFilter, JTextField searchInput, TableRowSorter studentSorter) {
        RowFilter<DefaultTableModel, Object> rf = null;
        List<RowFilter<Object, Object>> rfs
                = new ArrayList<RowFilter<Object, Object>>();

        // Check for any matching pattern in columns
        try {

            // First add row filter for active/inactive state
            RowFilter activeInactiveFilter = RowFilter.regexFilter(getActiveInactiveRadioBtnState(activeFilter, inactiveFilter), 6);
            rfs.add(activeInactiveFilter);

            String text = searchInput.getText();
            String[] textArray = text.split(" ");

            for (int i = 0; i < textArray.length; i++) {

                // Add column indexes to check
                rfs.add(RowFilter.regexFilter("(?i)" + textArray[i], 0, 1, 2));
            }

            rf = RowFilter.andFilter(rfs);

        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        studentSorter.setRowFilter(rf);
    }

    /*
     Instructor Table Methods
     */
    // Populate Instructors JTable
    public void populateInstructorsTable(JTable instructorTable, DefaultTableModel instructorTableModel, TableRowSorter instructorSorter,
            JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter, JTextField searchInput) throws ClassNotFoundException, SQLException {

        try {

            // Get all active Instructors in resultSet
            ResultSet resultSet = connection.prepareStatement(
                    "select LName,FName,InstructorID,Title,Address,City,State,ZipCode,Email,BirthDate,HireDate,HomePhone,WorkPhone,Notes,FullTime,SchedulePriority,Active,DateUpdated"
                    + " from Instructors order by LName asc;").executeQuery();

            // Create JTable model
            instructorTableModel = new DefaultTableModel() {

                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells uneditable
                    return false;
                }
            };

            // Create columns
            Object[] tableColumns = new Object[]{"Last Name", "First Name", "Home Phone", "Title", "Address", "City", "State", "Zip", "Email",
                "BirthDate", "HireDate", "Instructor ID", "WorkPhone", "Notes", "Full Time", "Priority", "Active", "Last Modified"};

            instructorTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            instructorSorter = new TableRowSorter<DefaultTableModel>(instructorTableModel);

            while (resultSet.next()) {

                // Get values
                String lastName = resultSet.getString(1);
                String firstName = resultSet.getString(2);
                String instructorID = resultSet.getString(3);
                String title = resultSet.getString(4);
                String address = resultSet.getString(5);
                String city = resultSet.getString(6);
                String state = resultSet.getString(7);
                String zip = resultSet.getString(8);
                String email = resultSet.getString(9);
                String birthDate = resultSet.getString(10);
                String hireDate = resultSet.getString(11);
                String notes = resultSet.getString(14);
                String homePhone = resultSet.getString(12);
                String workPhone = resultSet.getString(13);
                String priority = resultSet.getString(16);
                String lastModified = resultSet.getString(18);
                String fullTime = resultSet.getString(15);

                // Get value of active/inactive column and set text
                String status = null;
                if (resultSet.getBoolean(17) == true) {
                    status = "Active";
                } else {
                    status = "Inactive";
                }

                // Run forloop, first create String[] object then model.addRow
                String[] row = new String[]{lastName, firstName, homePhone, title, address, city, state, zip, email, birthDate, hireDate, instructorID, workPhone, notes,
                    fullTime, priority, status, lastModified};

                instructorTableModel.addRow(row);

            }

            // Set data/model to JTable
            instructorTable.setModel(instructorTableModel);
            instructorTable.setRowSorter(instructorSorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(instructorTable, 3, tableColumns.length);

            // Initialize filter listeners
            initializeInstructorTableFilters(activeFilter, inactiveFilter, allFilter, searchInput, instructorSorter);

            // Remove lines from table
            instructorTable.setShowGrid(false);
            instructorTable.setIntercellSpacing(new Dimension(0, 0));
            instructorTable.setFillsViewportHeight(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (instructorTable.getRowCount() > 0) {
            instructorTable.setRowSelectionInterval(0, 0);
        }

    }

    // Initialize listeners for students table
    public void initializeInstructorTableFilters(JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter,
            JTextField searchInput, TableRowSorter instructorSorter) {

        // Create new document listener for search field
        DocumentListener textChangedListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }

            public void removeUpdate(DocumentEvent e) {
                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }

            public void insertUpdate(DocumentEvent e) {
                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }
        };

        // Set search field listener
        searchInput.getDocument().addDocumentListener(textChangedListener);

        // Radio button listeners
        activeFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }
        });
        inactiveFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }
        });
        allFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                instructorSearchFilter(activeFilter, inactiveFilter, searchInput, instructorSorter);
            }
        });

        // Start Instructors Table Filter set to Active
        RowFilter<DefaultTableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            // Has string of current state "Active" or "Inactive" and the table row that Active/Inactive status is in
            rf = RowFilter.regexFilter("Active", 16);
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
        }
        instructorSorter.setRowFilter(rf);

    }

    // Search Filter for input text
    public void instructorSearchFilter(JRadioButton activeFilter, JRadioButton inactiveFilter, JTextField searchInput, TableRowSorter instructorSorter) {
        RowFilter<DefaultTableModel, Object> rf = null;
        List<RowFilter<Object, Object>> rfs
                = new ArrayList<RowFilter<Object, Object>>();

        // Check for any matching pattern in columns
        try {

            // First add row filter for active/inactive state
            RowFilter activeInactiveFilter = RowFilter.regexFilter(getActiveInactiveRadioBtnState(activeFilter, inactiveFilter), 16);
            rfs.add(activeInactiveFilter);

            String text = searchInput.getText();
            String[] textArray = text.split(" ");

            for (int i = 0; i < textArray.length; i++) {

                // Add column indexes to check
                rfs.add(RowFilter.regexFilter("(?i)" + textArray[i], 0, 1, 2));
            }

            rf = RowFilter.andFilter(rfs);

        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        instructorSorter.setRowFilter(rf);
    }

    /*
     Program Table Methods
     */
    // Populate Programs JTable
    public void populateProgramsTable(JTable programTable, DefaultTableModel programTableModel, TableRowSorter programSorter,
            JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter, JTextField searchInput) throws ClassNotFoundException, SQLException {

        try {

            // Get all active students in resultSet
            ResultSet resultSet = connection.prepareStatement(
                    "select ProgramName, ProgramID, ProgramDescription, RatePrivate, RateGroup, RateParty, DefaultBonusesAwardedPrivate,"
                    + " DefaultBonusesAwardedGroup, DefaultBonusesAwardedParty, DefaultLessonsPrivate, DefaultLessonsGroup, DefaultLessonsParty,"
                    + " Active, UnlimitedLessons, ProgramGroup, DateUpdated from Programs order by ProgramName asc;").executeQuery();

            // Create JTable model
            programTableModel = new DefaultTableModel() {

                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells uneditable
                    return false;
                }
            };

            // Create columns
            Object[] tableColumns = new Object[]{"Program Name", "Program ID", "Program Description", "Private Rate", "Group Rate",
                "Party Rate", "Private Bonus", "Group Bonus", "Party Bonus", "Private Lessons", "Group Lessons", "Party Lessons", "Active", "Unlimited Lessons", "Program Group", "DateUpdated"};
            programTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            programSorter = new TableRowSorter<>(programTableModel);

            while (resultSet.next()) {

                String programName = resultSet.getString(1);
                String programID = resultSet.getString(2);
                String programDescription = resultSet.getString(3);
                String ratePrivate = resultSet.getString(4);
                String rateGroup = resultSet.getString(5);
                String rateParty = resultSet.getString(6);
                String defaultBonusesAwardedPrivate = resultSet.getString(7);
                String defaultBonusesAwardedGroup = resultSet.getString(8);
                String defaultBonusesAwardedParty = resultSet.getString(9);
                String defaultLessonsAwardedPrivate = resultSet.getString(10);
                String defaultLessonsAwardedGroup = resultSet.getString(11);
                String defaultLessonsAwardedParty = resultSet.getString(12);
                String programGroup = resultSet.getString(15);

                // Get value of active/inactive column and set text
                String activeStatus = null;
                if (resultSet.getBoolean(13)) {
                    activeStatus = "Active";
                } else {
                    activeStatus = "Inactive";
                }

                // Unlimited Lessons Status is not in same order as Headers
                String unlimitedLessonsStatus = null;
                if (resultSet.getBoolean(14)) {
                    unlimitedLessonsStatus = "True";
                } else {
                    unlimitedLessonsStatus = "False";
                }

                // Format last updated
                String lastUpdated = (resultSet.getString(16).substring(5, 7) + "/" + resultSet.getString(16).substring(8) + "/"
                        + resultSet.getString(16).substring(0, 4));

                // Run forloop, first create String[] object then model.addRow
                String[] row = new String[]{programName, programID, programDescription, ratePrivate, rateGroup, rateParty,
                    defaultBonusesAwardedPrivate, defaultBonusesAwardedGroup, defaultBonusesAwardedParty, defaultLessonsAwardedPrivate, defaultLessonsAwardedGroup,
                    defaultLessonsAwardedParty, activeStatus, unlimitedLessonsStatus, programGroup, lastUpdated};

                programTableModel.addRow(row);

            }

            // Set data/model to JTable
            programTable.setModel(programTableModel);
            programTable.setRowSorter(programSorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(programTable, 12, tableColumns.length);

            // Initialize filter listeners
            initializeProgramTableFilters(activeFilter, inactiveFilter, allFilter, searchInput, programSorter);

            // Remove lines from table
            programTable.setShowGrid(false);
            programTable.setIntercellSpacing(new Dimension(0, 0));
            programTable.setFillsViewportHeight(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (programTable.getRowCount() > 0) {
            programTable.setRowSelectionInterval(0, 0);
        }
    }

    // Initialize listeners for students table
    public void initializeProgramTableFilters(JRadioButton activeFilter, JRadioButton inactiveFilter, JRadioButton allFilter,
            JTextField searchInput, TableRowSorter programSorter) {

        // Create new document listener for search field
        DocumentListener textChangedListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }
        };

        // Set search field listener
        searchInput.getDocument().addDocumentListener(textChangedListener);

        // Radio button listeners
        activeFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }
        });
        inactiveFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }
        });
        allFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                programSearchFilter(activeFilter, inactiveFilter, searchInput, programSorter);
            }
        });

        // Start Program Table Filter set to Active
        RowFilter<DefaultTableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            // Has string of current state "Active" or "Inactive" and the table row that Active/Inactive status is in
            rf = RowFilter.regexFilter("Active", 12);
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
        }
        programSorter.setRowFilter(rf);

    }

    // Search Filter for input text
    public void programSearchFilter(JRadioButton activeFilter, JRadioButton inactiveFilter, JTextField searchInput, TableRowSorter programSorter) {
        RowFilter<DefaultTableModel, Object> rf = null;
        List<RowFilter<Object, Object>> rfs
                = new ArrayList<RowFilter<Object, Object>>();

        // Check for any matching pattern in columns
        try {

            // First add row filter for active/inactive state
            RowFilter activeInactiveFilter = RowFilter.regexFilter(getActiveInactiveRadioBtnState(activeFilter, inactiveFilter), 12);
            rfs.add(activeInactiveFilter);

            String text = searchInput.getText();
            String[] textArray = text.split(" ");

            for (int i = 0; i < textArray.length; i++) {

                // Add column indexes to check
                rfs.add(RowFilter.regexFilter("(?i)" + textArray[i], 0, 1, 2));
            }

            rf = RowFilter.andFilter(rfs);

        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        programSorter.setRowFilter(rf);
    }

    /*
     Enrollment Table Methods
     */
    // Populate Program Enrollment JTable
    public void populateEnrollmentTable(JTable enrollmentTable, TableRowSorter enrollmentSorter, DefaultTableModel enrollmentTableModel, String studentID)
            throws ClassNotFoundException, SQLException {

        // Create Uneditable Table model
        enrollmentTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        try {

            // Get all enrolled programs for student
            ResultSet resultSet = connection.prepareStatement(
                    "select EN.ProgramID,EN.PrivateLessonAttended,EN.GroupLessonAttended,EN.PartyLessonAttended,EN.PrivateLessonTotal,EN.GroupLessonTotal,EN.PartyLessonTotal,EN.ContractPaid,"
                    + "EN.ContractTotal,EN.OwesPayment,EN.PrimaryInstructorID,EN.DateCreated,EN.EnrollmentID,EN.PrivateLessonPrice,EN.GroupLessonPrice,EN.PartyLessonPrice"
                    + " from ProgramEnrollment as EN INNER JOIN Programs as PR ON PR.ProgramID=EN.ProgramID where EN.StudentID='" + studentID
                    + "' and ((EN.ContractPaid < EN.ContractTotal) or (EN.PrivateLessonAttended < EN.PrivateLessonTotal) or (EN.GroupLessonAttended < EN.GroupLessonTotal) or"
                    + " (EN.PartyLessonAttended < EN.PartyLessonTotal) or (EN.ProgramID='BNSL') or (PR.UnlimitedLessons=TRUE)) order by EN.DateCreated desc;").executeQuery();

            // Create columns
            Object[] tableColumns = new Object[]{"Enrolled", "Program", "Instructor", "Priv Tot", "Priv Rem", "Grp Tot", "Grp Rem",
                "Prty Tot", "Prty Rem", "Contract", "Paid", "Balance", "EnrollmentID", "Priv Price", "Grp Price", "Prty Price", "Priv Used", "Grp Used", "Prty Used"};
            enrollmentTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            enrollmentSorter = new TableRowSorter<DefaultTableModel>(enrollmentTableModel);

            // Set row values
            while (resultSet.next()) {

                // Get variables
                String enrollmentID = resultSet.getString(13);
                Boolean owesPayment = resultSet.getBoolean(10);
                String primaryInstructorID = addRedHTML(resultSet.getString(11), owesPayment);
                String enrollDate = addRedHTML((resultSet.getString(12).substring(5, 7) + "/"
                        + resultSet.getString(12).substring(8) + "/" + resultSet.getString(12).substring(2, 4)), owesPayment);

                // Get instructor name from id
                ResultSet instructorResult = connection.prepareStatement(
                        "select FName from Instructors where InstructorID='" + removeRedHTML(primaryInstructorID) + "';").executeQuery();

                String instructorName = "";
                if (instructorResult.next()) {
                    instructorName = addRedHTML(instructorResult.getString(1), owesPayment);
                }

                // Create black or red cells based on owesPayment boolean
                String programID = addRedHTML(resultSet.getString(1), owesPayment);
                String contractTotal = addRedHTML(resultSet.getString(9), owesPayment);
                String contractPaid = addRedHTML(resultSet.getString(8), owesPayment);
                String contractBalance = addRedHTML(String.valueOf(resultSet.getDouble(8) - resultSet.getDouble(9)), owesPayment);

                // Get lessons remaining and total for each type
                String privateLessonsUsed = addRedHTML(resultSet.getString(2), owesPayment);
                String groupLessonsUsed = addRedHTML(resultSet.getString(3), owesPayment);
                String partyLessonsUsed = addRedHTML(resultSet.getString(4), owesPayment);
                String privateLessonsTotal = addRedHTML(resultSet.getString(5), owesPayment);
                String groupLessonsTotal = addRedHTML(resultSet.getString(6), owesPayment);
                String partyLessonsTotal = addRedHTML(resultSet.getString(7), owesPayment);
                String privateLessonsRemaining = addRedHTML(String.valueOf(resultSet.getDouble(5) - resultSet.getDouble(2)), owesPayment);
                String groupLessonsRemaining = addRedHTML(String.valueOf(resultSet.getDouble(6) - resultSet.getDouble(3)), owesPayment);
                String partyLessonsRemaining = addRedHTML(String.valueOf(resultSet.getDouble(7) - resultSet.getDouble(4)), owesPayment);
                String privateLessonsPrice = addRedHTML(resultSet.getString(14), owesPayment);
                String groupLessonsPrice = addRedHTML(resultSet.getString(15), owesPayment);
                String partyLessonsPrice = addRedHTML(resultSet.getString(16), owesPayment);

                // Create enrollment table row
                String[] row = new String[]{enrollDate, programID, instructorName, privateLessonsTotal, privateLessonsRemaining, groupLessonsTotal,
                    groupLessonsRemaining, partyLessonsTotal, partyLessonsRemaining, contractTotal, contractPaid, contractBalance, enrollmentID,
                    privateLessonsPrice, groupLessonsPrice, partyLessonsPrice, privateLessonsUsed, groupLessonsUsed, partyLessonsUsed};

                // Add to table
                enrollmentTableModel.addRow(row);

            }

            // Set data/model to JTable
            enrollmentTable.setModel(enrollmentTableModel);
            enrollmentTable.setRowSorter(enrollmentSorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(enrollmentTable, 12, 19);

            // Hide grid lines on table
            enrollmentTable.setShowGrid(false);
            enrollmentTable.setIntercellSpacing(new Dimension(0, 0));
            enrollmentTable.setFillsViewportHeight(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (enrollmentTable.getRowCount() > 0) {
            enrollmentTable.setRowSelectionInterval(0, 0);
        }

    }

    /*
     Lesson Table Methods
     */
    // Populate Lessons JTable
    public void populateLessonsTable(JTable lessonTable, TableRowSorter lessonSorter, DefaultTableModel lessonTableModel, String studentID,
            int columnsShown, String tableType) throws ClassNotFoundException, SQLException, ParseException {

        // Create Uneditable Table model
        lessonTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        // Add whitespace to lessons table
        lessonTable.setShowGrid(false);
        lessonTable.setIntercellSpacing(new Dimension(0, 0));
        lessonTable.setFillsViewportHeight(true);

        try {

            ResultSet resultSet = null;
            if (tableType.equals("AttendPurchase")) {

                // Get lessons in last month or unattended
                resultSet = connection.prepareStatement(
                        "select LessonID,ProgramID,RateType,InstructorName,AppointmentDate,AppointmentTimeStart,LessonStatus,AppointmentTimeEnd,LessonCode,LessonUnits,"
                        + "Notes,PaymentStatus,LessonPrice,DateCreated,EnrollmentID from LessonSchedule where StudentID='"
                        + studentID + "' and (LessonStatus='Unattended' or AppointmentDate > dateadd('day', -30, CURRENT_DATE)) order by AppointmentDate desc;")
                        .executeQuery();
            } else if (tableType.equals("StudentDetails")) {

                // Get all lessons in resultSet
                resultSet = connection.prepareStatement(
                        "select LessonID,ProgramID,RateType,InstructorName,AppointmentDate,AppointmentTimeStart,LessonStatus,AppointmentTimeEnd,LessonCode,LessonUnits,"
                        + "Notes,PaymentStatus,LessonPrice,DateCreated,EnrollmentID from LessonSchedule where StudentID='"
                        + studentID + "' order by AppointmentDate desc;").executeQuery();
            }

            // Create columns
            Object[] tableColumns = new Object[]{"Date", "Start Time", "Instructor", "Status", "Payment Status", "Program", "Lesson Type", "Lesson ID",
                "End Time", "Lesson Code", "Lesson Units", "Notes", "Lesson Price", "Created On", "Enrollment ID"};
            lessonTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            lessonSorter = new TableRowSorter<DefaultTableModel>(lessonTableModel);

            // Set row values
            while (resultSet.next()) {

                // Get variables
                String lessonID = resultSet.getString(1);
                String programID = resultSet.getString(2);
                String rateType = resultSet.getString(3);
                String instructorName = resultSet.getString(4);
                String appointmentDate = resultSet.getString(5);
                String appointmentTimeStart = genericHelper.formatTimeToHHMM(resultSet.getString(6));
                String lessonStatus = resultSet.getString(7);
                String appointmentTimeEnd = genericHelper.formatTimeToHHMM(resultSet.getString(8));
                String lessonCode = resultSet.getString(9);
                String lessonUnits = resultSet.getString(10);
                String notes = resultSet.getString(11);
                String paymentStatus = resultSet.getString(12);
                String lessonPrice = resultSet.getString(13);
                String createdDate = resultSet.getString(14);
                String enrollmentID = resultSet.getString(15);

                // Format dates
                DateFormat dateFormatUnformatted = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dateFormatFormatted = new SimpleDateFormat("MM/dd/yy");

                Date startDateUnformatted = dateFormatUnformatted.parse(appointmentDate);
                String startDateFormatted = dateFormatFormatted.format(startDateUnformatted);
                Date createdDateUnformatted = dateFormatUnformatted.parse(createdDate);
                String createdDateFormatted = dateFormatFormatted.format(createdDateUnformatted);

                // Run forloop, first create String[] object then model.addRow
                String[] row = new String[]{startDateFormatted, appointmentTimeStart, instructorName, lessonStatus, paymentStatus, programID, rateType, lessonID,
                    appointmentTimeEnd, lessonCode, lessonUnits, notes, lessonPrice, createdDate, enrollmentID};

                lessonTableModel.addRow(row);

            }

            // Set data/model to JTable
            lessonTable.setModel(lessonTableModel);
            lessonTable.setRowSorter(lessonSorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(lessonTable, columnsShown, 15);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (lessonTable.getRowCount() > 0) {
            lessonTable.setRowSelectionInterval(0, 0);
        }

    }

    /*
     Bonus History Table Methods
     */
    // Populate Bonus HIstory JTable/*
    public void populateBonusHistoryTable(JTable bonusHistoryTable, TableRowSorter bonusHistorySorter, DefaultTableModel bonusHistoryTableModel, String studentID,
            int columnsShown) throws ClassNotFoundException, SQLException, ParseException {

        // Create Uneditable Table model
        bonusHistoryTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        // Add whitespace to table
        bonusHistoryTable.setShowGrid(false);
        bonusHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        bonusHistoryTable.setFillsViewportHeight(true);

        try {

            // Get all bonus transactions in resultSet
            ResultSet resultSet = connection.prepareStatement("select BT.TransactionID,BT.BonusType,BT.LessonType,BT.TransactionType,BT.FromEnrollmentID,"
                    + " BT.UsedOnEnrollmentID,BT.RedeemedOn,BT.DateCreated,BT.UnitsUsed,BT.ReferredStudentID from BonusTransaction as BT INNER JOIN Students as ST ON"
                    + " ST.StudentID=BT.StudentID where BT.StudentID='" + studentID + "' order by BT.TransactionID desc;").executeQuery();

            // Create columns
            Object[] tableColumns = new Object[]{"ID", "Bonus Type", "Lesson Type", "Trans. Type", "Enrollment From", "Enrollment Used", "Referred Student", "Redeemed On",
                "Created", "Units Changed"};
            bonusHistoryTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            bonusHistorySorter = new TableRowSorter<>(bonusHistoryTableModel);

            // Set row values
            while (resultSet.next()) {

                // Get variables
                String transactionID = resultSet.getString(1);
                String bonusType = resultSet.getString(2);
                String lessonType = resultSet.getString(3);
                String transactionType = resultSet.getString(4);
                String enrollmentIDFrom = resultSet.getString(5);
                String enrollmentIDUsedOn = resultSet.getString(6);
                String redeemedOnDate = resultSet.getString(7);
                String createdDate = resultSet.getString(8);
                String unitsChanged = resultSet.getString(9);
                String referredStudentID = resultSet.getString(10);

                String referredStudentName = "";
                if (genericHelper.stringNotNull(referredStudentID)) {

                    ResultSet referredStudentData = connection.prepareStatement(String.format("SELECT LName,FName FROM Students WHERE StudentID='%s';", referredStudentID)).executeQuery();

                    if (referredStudentData.next()) {
                        referredStudentName = referredStudentData.getString(1) + ", " + referredStudentData.getString(2);
                    }
                }

                // Format dates
                DateFormat dateFormatUnformatted = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dateFormatFormatted = new SimpleDateFormat("MM/dd/yy");

                String redeemedOnDateFormatted = "";
                if (redeemedOnDate != null) {
                    Date redeemedOnDateUnformatted = dateFormatUnformatted.parse(redeemedOnDate);
                    redeemedOnDateFormatted = dateFormatFormatted.format(redeemedOnDateUnformatted);
                }
                Date createdDateUnformatted = dateFormatUnformatted.parse(createdDate);
                String createdDateFormatted = dateFormatFormatted.format(createdDateUnformatted);

                // Run forloop, first create String[] object then model.addRow
                String[] row = new String[]{transactionID, bonusType, lessonType, transactionType, enrollmentIDFrom, enrollmentIDUsedOn,
                    referredStudentName, redeemedOnDateFormatted, createdDateFormatted, unitsChanged};

                bonusHistoryTableModel.addRow(row);
            }

            // Set data/model to JTable
            bonusHistoryTable.setModel(bonusHistoryTableModel);
            bonusHistoryTable.setRowSorter(bonusHistorySorter);

            // Hide unnecessary columns
            hideTrivialTableColumns(bonusHistoryTable, columnsShown, 9);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (bonusHistoryTable.getRowCount() > 0) {
            bonusHistoryTable.setRowSelectionInterval(0, 0);
        }

    }

    // Populate Bonus HIstory JTable/*
    public void populatePaymentHistoryTable(JTable paymentHistoryTable, TableRowSorter paymentHistorySorter, DefaultTableModel paymentHistoryTableModel, String studentID,
            int columnsShown) throws ClassNotFoundException, SQLException, ParseException {

        // Create Uneditable Table model
        paymentHistoryTableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells uneditable
                return false;
            }
        };

        // Add whitespace to table
        paymentHistoryTable.setShowGrid(false);
        paymentHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        paymentHistoryTable.setFillsViewportHeight(true);

        try {

            ResultSet resultSet = connection.prepareStatement("select PTN.PaymentID, PTN.PaymentType, PTN.Amount, PRG.ProgramName, PTN.DateCreated, PTN.EnrollmentID FROM"
                    + " PaymentTransaction AS PTN INNER JOIN ProgramEnrollment as EN ON PTN.EnrollmentID=EN.EnrollmentID INNER JOIN Programs AS PRG ON PRG.ProgramID=EN.ProgramID "
                    + "WHERE PTN.StudentID='" + studentID + "' ORDER BY PTN.PaymentID DESC ;").executeQuery();

            // Create columns
            Object[] tableColumns = new Object[]{"ID", "Type", "Amount", "Program Name", "Date", "Enrollment ID"};
            paymentHistoryTableModel.setColumnIdentifiers(tableColumns);

            // Set sorters
            paymentHistorySorter = new TableRowSorter<DefaultTableModel>(paymentHistoryTableModel);

            // Set row values
            while (resultSet.next()) {

                // Get variables
                String paymentID = resultSet.getString(1);
                String paymentType = resultSet.getString(2);
                String paymentAmount = resultSet.getString(3);
                String programName = resultSet.getString(4);
                String dateCreated = resultSet.getString(5);
                String enrollmentID = resultSet.getString(6);

                // Format dates
                DateFormat dateFormatUnformatted = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dateFormatFormatted = new SimpleDateFormat("MM/dd/yy");

                Date createdDateUnformatted = dateFormatUnformatted.parse(dateCreated);
                String createdDateFormatted = dateFormatFormatted.format(createdDateUnformatted);

                // Run forloop, first create String[] object then model.addRow
                String[] row = new String[]{paymentID, paymentType, paymentAmount, programName, createdDateFormatted, enrollmentID};

                paymentHistoryTableModel.addRow(row);

            }

            // Set data/model to JTable
            paymentHistoryTable.setModel(paymentHistoryTableModel);
            paymentHistoryTable.setRowSorter(paymentHistorySorter);

            // Hide unnecessary columns
            //hideTrivialTableColumns(paymentHistoryTable, columnsShown, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Select first row
        if (paymentHistoryTable.getRowCount() > 0) {
            paymentHistoryTable.setRowSelectionInterval(0, 0);
        }

    }

    /*
     Common Table Methods
     */
    // Hide unnecessary columns
    public void hideTrivialTableColumns(JTable inputTable, int showLength, int totalLength) {

        // Hide individual columns
        for (int i = showLength; i < totalLength; i++) {
            inputTable.getColumnModel().getColumn(i).setMinWidth(0);
            inputTable.getColumnModel().getColumn(i).setMaxWidth(0);
            inputTable.getColumnModel().getColumn(i).setWidth(0);
        }
    }

    // Return string of current radio active/inactive position
    public String getActiveInactiveRadioBtnState(JRadioButton activeFilter, JRadioButton inactiveFilter) {

        if (activeFilter.isSelected()) {
            return "Active";
        } else if (inactiveFilter.isSelected()) {
            return "Inactive";
        } else {
            return "";
        }
    }

    public void scrollTableToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport) table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);

        table.scrollRectToVisible(rect);

        // Scroll the area into view
        //viewport.scrollRectToVisible(rect);
    }

    // Adds red HTML to string if owesPayment is true
    public String addRedHTML(String input, boolean owesPayment) {

        String preHTMLRed = "<html><span style='color:red;'>";
        String postHTMLRed = "</span></html>";

        return (owesPayment ? (preHTMLRed + input + postHTMLRed) : input);
    }

    // Remove html markings from enrollment text
    public String removeRedHTML(String input) {
        return input.replace("<html><span style='color:red;'>", "").replace("</span></html>", "");
    }

}
