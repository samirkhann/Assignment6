package calendar.view;

import calendar.controller.CommandParser;
import calendar.model.CalendarManager;
import calendar.controller.CalendarHelper;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.SpinnerDateModel;

import java.awt.Insets;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The CalendarStarter class serves as the main entry point for the Calendar application.
 * It is responsible for initializing the graphical user interface,
 * setting up the interaction between the user and the underlying calendar model,
 * and coordinating with the command parser and helper components.
 */
public class CalendarStarter {

  private final JFrame frame;
  private final JPanel calendarPanel;
  private final JLabel monthLabel;
  private final JComboBox<String> calendarDropdown;
  private YearMonth currentMonth;
  private final CalendarManager calendarManager;
  private final CommandParser commandParser;
  private final CalendarHelper helper;
  private final JPanel weekHeaderPanel = new JPanel(new GridLayout(1, 7));

  private final DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Constructs and initializes the Calendar application GUI.
   * <p>
   * This constructor sets up the calendar manager, command parser, and helper, creates
   * and configures all GUI components (such as the main frame, panels, buttons, and labels),
   * and attaches the necessary event listeners to enable interaction.
   * It also initializes the calendar display and dropdown menu, then makes the main window visible.
   * </p>
   */
  public CalendarStarter() {
    calendarManager = new CalendarManager();
    commandParser = new CommandParser(calendarManager);
    helper = new CalendarHelper();

    frame = new JFrame("Calendar App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 650);
    frame.setLayout(new BorderLayout());

    currentMonth = YearMonth.now();

    JPanel topPanel = new JPanel();
    JButton prevButton = new JButton("<");
    JButton nextButton = new JButton(">");
    JButton createCalendarButton = new JButton("Create Calendar");
    JButton editCalendarButton = new JButton("Edit Calendar");
    JButton exportButton = new JButton("Export CSV");
    JButton importButton = new JButton("Import CSV");
    monthLabel = new JLabel();
    calendarDropdown = new JComboBox<>();

    commandParser.parseAndExecute("create calendar --name Default --timezone UTC");
    commandParser.parseAndExecute("use calendar --name Default");

    updateCalendarDropdown();

    topPanel.add(prevButton);
    topPanel.add(monthLabel);
    topPanel.add(nextButton);
    topPanel.add(calendarDropdown);
    topPanel.add(createCalendarButton);
    topPanel.add(editCalendarButton);
    topPanel.add(exportButton);
    topPanel.add(importButton);

    frame.add(topPanel, BorderLayout.NORTH);
    calendarPanel = new JPanel();
    frame.add(calendarPanel, BorderLayout.CENTER);

    prevButton.addActionListener(e -> changeMonth(-1));
    nextButton.addActionListener(e -> changeMonth(1));
    calendarDropdown.addActionListener(e -> changeCalendar());
    createCalendarButton.addActionListener(e -> createCalendar());
    editCalendarButton.addActionListener(e -> editCalendar());
    exportButton.addActionListener(e -> exportCalendar());
    importButton.addActionListener(e -> importCalendar());

    updateCalendar();
    frame.setVisible(true);
  }

  private void importCalendar() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select CSV File to Import");
    int userSelection = fileChooser.showOpenDialog(frame);
    if (userSelection != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File csvFile = fileChooser.getSelectedFile();

    DateTimeFormatter csvDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter csvTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      String headerLine = br.readLine();
      if (headerLine == null) {
        JOptionPane.showMessageDialog(frame, "The file is empty.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }
        String[] tokens = line.split(",");
        for (int i = 0; i < tokens.length; i++) {
          tokens[i] = tokens[i].trim();
        }
        if (tokens.length < 5) {
          System.out.println("Skipping invalid line: " + line);
          continue;
        }
        String subject = tokens[0];

        LocalDate startDate = LocalDate.parse(tokens[1], csvDateFormatter);
        LocalTime startTime = LocalTime.parse(tokens[2], csvTimeFormatter);
        LocalDateTime startDateTimeObj = LocalDateTime.of(startDate, startTime);
        String startDateTime = startDateTimeObj.format(outputFormatter);

        LocalDate endDate = LocalDate.parse(tokens[3], csvDateFormatter);
        LocalTime endTime = LocalTime.parse(tokens[4], csvTimeFormatter);
        LocalDateTime endDateTimeObj = LocalDateTime.of(endDate, endTime);
        String endDateTime = endDateTimeObj.format(outputFormatter);

        String description = tokens.length > 5 ? tokens[5] : "";
        String location = tokens.length > 6 ? tokens[6] : "";
        boolean isPublic = (tokens.length > 7) ?
                !tokens[7].equalsIgnoreCase("TRUE") : true;

        String cmd = "create event \"" + subject + "\" from " +
                startDateTime + " to " + endDateTime;
        if (!description.isEmpty()) {
          cmd += " description \"" + description + "\"";
        }
        if (!location.isEmpty()) {
          cmd += " location \"" + location + "\"";
        }
        if (isPublic) {
          cmd += " public";
        }

        String result = commandParser.parseAndExecute(cmd);
        System.out.println("Imported command: " + cmd + " => " + result);
        count++;
      }
      JOptionPane.showMessageDialog(frame, "CSV Import completed. "
                      + count + " events processed.",
              "Import Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(frame, "Error importing CSV: " + e.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateCalendarDropdown() {
    calendarDropdown.removeAllItems();
    for (String calendarName : calendarManager.getCalendarNames()) {
      calendarDropdown.addItem(calendarName);
    }
  }

  private void changeCalendar() {
    String selectedCalendar = (String) calendarDropdown.getSelectedItem();
    if (selectedCalendar != null) {
      String cmd = "use calendar --name \"" + selectedCalendar + "\"";
      String result = commandParser.parseAndExecute(cmd);
      if (result.startsWith("Error")) {
        JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
      } else {
        updateCalendar();
      }
    }
  }

  private void changeMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    updateCalendar();
  }

  private void updateCalendar() {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));

    LocalDate firstOfMonth = currentMonth.atDay(1);
    DayOfWeek firstWeekday = firstOfMonth.getDayOfWeek();
    int dayOffset = firstWeekday.getValue() % 7;

    for (int i = 0; i < dayOffset; i++) {
      calendarPanel.add(new JLabel(""));
    }

    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));
      dayButton.addActionListener(e -> showEvents(date));
      calendarPanel.add(dayButton);
    }

    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

    weekHeaderPanel.removeAll();
    String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String weekday : weekdays) {
      JLabel label = new JLabel(weekday, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 12));
      label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
      weekHeaderPanel.add(label);
    }

    JPanel calendarContainer = new JPanel(new BorderLayout());
    calendarContainer.add(weekHeaderPanel, BorderLayout.NORTH);
    calendarContainer.add(calendarPanel, BorderLayout.CENTER);

    Container contentPane = frame.getContentPane();
    BorderLayout layout = (BorderLayout) contentPane.getLayout();
    Component currentCenter = layout.getLayoutComponent(BorderLayout.CENTER);
    if (currentCenter != null) {
      contentPane.remove(currentCenter);
    }
    contentPane.add(calendarContainer, BorderLayout.CENTER);

    frame.revalidate();
    frame.repaint();
  }

  private void createCalendar() {
    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) {
      JOptionPane.showMessageDialog(frame, "Calendar name cannot be empty.",
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String quotedName = "\"" + name + "\"";

    List<String> timeZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
    Collections.sort(timeZones);
    String[] timeZonesArray = timeZones.toArray(new String[0]);
    String timezone = (String) JOptionPane.showInputDialog(
            frame, "Select timezone:", "Timezone Selection",
            JOptionPane.QUESTION_MESSAGE, null,
            timeZonesArray, timeZonesArray[0]
    );
    if (timezone == null) {
      JOptionPane.showMessageDialog(frame, "Timezone selection cancelled.",
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String cmd = "create calendar --name " + quotedName + " --timezone " + timezone;
    String result = commandParser.parseAndExecute(cmd);
    if (!result.startsWith("Error")) {
      updateCalendarDropdown();
      JOptionPane.showMessageDialog(frame, result, "Success", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void editCalendar() {
    String selectedCalendar = (String) calendarDropdown.getSelectedItem();
    if (selectedCalendar == null) {
      JOptionPane.showMessageDialog(frame, "Please select a calendar to edit.",
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String[] options = {"Name", "Timezone"};
    int choice = JOptionPane.showOptionDialog(frame, "What would you like to edit?",
            "Edit Calendar",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    if (choice == JOptionPane.CLOSED_OPTION) {
      return;
    }
    String propertyToEdit = options[choice].toLowerCase();
    String newValue;
    if (propertyToEdit.equals("timezone")) {
      List<String> timeZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
      Collections.sort(timeZones);
      String[] timeZonesArray = timeZones.toArray(new String[0]);
      newValue = (String) JOptionPane.showInputDialog(
              frame, "Select timezone:", "Timezone Selection",
              JOptionPane.QUESTION_MESSAGE, null,
              timeZonesArray, timeZonesArray[0]
      );
      if (newValue == null || newValue.trim().isEmpty()) {
        return;
      }
    } else {
      newValue = JOptionPane.showInputDialog(frame, "Enter new " + propertyToEdit + ":");
      if (newValue == null || newValue.trim().isEmpty()) {
        return;
      }
      newValue = "\"" + newValue + "\"";
    }
    String quotedCalendar = "\"" + selectedCalendar + "\"";
    String cmd = "edit calendar --name " + quotedCalendar
            + " --property " + propertyToEdit + " " + newValue;
    String result = commandParser.parseAndExecute(cmd);
    if (!result.startsWith("Error")) {
      updateCalendarDropdown();
      JOptionPane.showMessageDialog(frame, result, "Success", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void showEvents(LocalDate date) {
    String result = commandParser.parseAndExecute("print events on " + date);
    if (result.startsWith("Error")) {
      JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setPreferredSize(new Dimension(700, 450));
    JLabel headingLabel = new JLabel("Events on " + date);
    headingLabel.setFont(headingLabel.getFont().deriveFont(Font.BOLD, 16f));
    mainPanel.add(headingLabel, BorderLayout.NORTH);

    JPanel listPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    JScrollPane scrollPane = new JScrollPane(listPanel);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    List<String> eventsList = helper.parseEvents(result);
    if (eventsList.isEmpty()) {
      listPanel.add(new JLabel("No events found on this date."));
    } else {
      for (String eventLine : eventsList) {
        JLabel eventLabel = new JLabel(eventLine);
        listPanel.add(eventLabel);
        listPanel.add(new JLabel(""));
      }
    }

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton createEventButton = new JButton("Create New Event");
    createEventButton.addActionListener(e -> {
      createEvent(date);
      Window window = SwingUtilities.getWindowAncestor(mainPanel);
      if (window != null) {
        window.dispose();
      }
      showEvents(date);
    });

    JButton editButton = new JButton("Edit an Event");
    editButton.addActionListener(e -> {
      editEventOnDate(date);
      Window window = SwingUtilities.getWindowAncestor(mainPanel);
      if (window != null) {
        window.dispose();
      }
      showEvents(date);
    });

    bottomPanel.add(createEventButton);
    bottomPanel.add(editButton);
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    JOptionPane.showMessageDialog(frame, mainPanel, "Events on " + date,
            JOptionPane.PLAIN_MESSAGE);
  }

  private void editEventOnDate(LocalDate date) {
    String printResult = commandParser.parseAndExecute("print events on " + date);
    if (printResult.startsWith("Error") || printResult.contains("No events on")) {
      JOptionPane.showMessageDialog(frame, "No events found on " + date,
              "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    List<String> eventsList = helper.parseEvents(printResult);
    if (eventsList.isEmpty()) {
      JOptionPane.showMessageDialog(frame, "No events found on " + date,
              "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    String chosenLine = (String) JOptionPane.showInputDialog(
            frame, "Select an event to edit:", "Edit an Event",
            JOptionPane.QUESTION_MESSAGE, null,
            eventsList.toArray(new String[0]),
            eventsList.get(0)
    );
    if (chosenLine == null) {
      return;
    }
    editEventFromLine(chosenLine);
  }

  private void editEventFromLine(String eventLine) {
    String subject = helper.extractSubject(eventLine);
    LocalDateTime startDateTime = helper.extractStartTime(eventLine);
    if (subject == null || startDateTime == null) {
      JOptionPane.showMessageDialog(frame, "Could not parse subject or time:\n" + eventLine,
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String[] properties = {"subject", "description", "location", "time", "public"};
    String propertyToEdit = (String) JOptionPane.showInputDialog(
            frame, "Which property do you want to edit?", "Edit Event",
            JOptionPane.QUESTION_MESSAGE, null,
            properties, properties[0]
    );
    if (propertyToEdit == null) {
      return;
    }
    String msg = "Enter new value for " + propertyToEdit;
    if ("time".equals(propertyToEdit)) {
      msg += " (YYYY-MM-ddTHH:mm)";
    }
    String newValue = JOptionPane.showInputDialog(frame, msg);
    if (newValue == null || newValue.trim().isEmpty()) {
      return;
    }
    String endTimeStr = helper.getEndTime(eventLine);
    if (endTimeStr == null || endTimeStr.trim().isEmpty()) {
      JOptionPane.showMessageDialog(frame, "Could not parse end time:\n" + eventLine,
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    boolean isRecurring = calendarManager.isRecurringEvent(subject);
    if (isRecurring) {
      Object[] seriesOptions = {"Edit from this time", "Edit all occurrences", "Just This Event"};
      int choice = JOptionPane.showOptionDialog(
              frame,
              "This event is recurring. How would you like to edit it?",
              "Recurring Event",
              JOptionPane.DEFAULT_OPTION,
              JOptionPane.QUESTION_MESSAGE,
              null,
              seriesOptions,
              seriesOptions[0]
      );
      if (choice == 0) {
        String cmd = "edit events " + propertyToEdit + " \"" + subject + "\" from "
                + startDateTime.format(helper.dateTimeFormatter) + " with \"" + newValue + "\"";
        String resp = commandParser.parseAndExecute(cmd);
        if (resp.startsWith("Error")) {
          JOptionPane.showMessageDialog(frame, resp, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(frame,
                  "Recurring events (from start) edited successfully.",
                  "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        return;
      } else if (choice == 1) {
        String cmd = "edit events " + propertyToEdit + " \"" + subject + "\" \"" + newValue + "\"";
        String resp = commandParser.parseAndExecute(cmd);
        if (resp.startsWith("Error")) {
          JOptionPane.showMessageDialog(frame, resp, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(frame,
                  "All recurring events edited successfully.",
                  "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        return;
      } else if (choice == 2) {
        String cmd = "edit event " + propertyToEdit + " \"" + subject + "\" from "
                + startDateTime.format(helper.dateTimeFormatter) + " to " + endTimeStr
                + " with \"" + newValue + "\"";
        String response = commandParser.parseAndExecute(cmd);
        if (response.startsWith("Error")) {
          JOptionPane.showMessageDialog(frame, response, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(frame, "Single recurring event edited successfully.",
                  "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        return;
      }
    }
    String singleCmd = "edit event " + propertyToEdit + " \"" + subject + "\" from "
            + startDateTime.format(helper.dateTimeFormatter) + " to " + endTimeStr
            + " with \"" + newValue + "\"";
    String response = commandParser.parseAndExecute(singleCmd);
    if (response.startsWith("Error")) {
      JOptionPane.showMessageDialog(frame, response, "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(frame, "Event edited successfully.",
              "Success", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void createEvent(LocalDate date) {
    JTextField subjectField = new JTextField(20);
    JTextField descriptionField = new JTextField(20);
    JTextField locationField = new JTextField(20);
    JCheckBox isPublicCheckBox = new JCheckBox("Event is public?");
    JCheckBox allDayCheckBox = new JCheckBox("All-day event");
    JCheckBox recurringCheckBox = new JCheckBox("Repeat event?");
    JSpinner startHourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
    JSpinner startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    JSpinner endHourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
    JSpinner endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    panel.add(new JLabel("Subject:"), gbc);
    panel.add(subjectField, gbc);
    panel.add(new JLabel("Description (optional):"), gbc);
    panel.add(descriptionField, gbc);
    panel.add(new JLabel("Location (optional):"), gbc);
    panel.add(locationField, gbc);
    panel.add(isPublicCheckBox, gbc);
    panel.add(allDayCheckBox, gbc);
    panel.add(recurringCheckBox, gbc);

    JPanel timePanel = new JPanel();
    timePanel.add(new JLabel("Start Time:"));
    timePanel.add(startHourSpinner);
    timePanel.add(new JLabel(":"));
    timePanel.add(startMinuteSpinner);
    timePanel.add(new JLabel("   End Time:"));
    timePanel.add(endHourSpinner);
    timePanel.add(new JLabel(":"));
    timePanel.add(endMinuteSpinner);
    panel.add(timePanel, gbc);

    allDayCheckBox.addActionListener(e -> {
      boolean isAllDay = allDayCheckBox.isSelected();
      startHourSpinner.setEnabled(!isAllDay);
      startMinuteSpinner.setEnabled(!isAllDay);
      endHourSpinner.setEnabled(!isAllDay);
      endMinuteSpinner.setEnabled(!isAllDay);
    });
    recurringCheckBox.addActionListener(e -> {
      if (recurringCheckBox.isSelected()) {
        showRecurrenceDialog();
      } else {
        helper.selectedRecurrenceDays.clear();
        helper.recurrenceOccurrences = null;
        helper.recurrenceUntilDate = null;
      }
    });

    int result = JOptionPane.showConfirmDialog(frame, panel, "Create Event",
            JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      String subject = subjectField.getText().trim();
      String description = descriptionField.getText().trim();
      String location = locationField.getText().trim();
      boolean isPublic = isPublicCheckBox.isSelected();
      boolean isAllDay = allDayCheckBox.isSelected();
      boolean isRecurring = recurringCheckBox.isSelected();
      if (subject.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Event subject cannot be empty.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      LocalDateTime startTime;
      LocalDateTime endTime;
      if (isAllDay) {
        startTime = date.atTime(LocalTime.of(0, 0));
        endTime = date.atTime(LocalTime.of(23, 59));
      } else {
        int startHour = (int) startHourSpinner.getValue();
        int startMin = (int) startMinuteSpinner.getValue();
        int endHour = (int) endHourSpinner.getValue();
        int endMin = (int) endMinuteSpinner.getValue();
        startTime = date.atTime(startHour, startMin);
        endTime = date.atTime(endHour, endMin);
      }
      if (endTime.isBefore(startTime)) {
        JOptionPane.showMessageDialog(frame, "End time cannot be before start time.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String cmd;
      if (isRecurring && !helper.selectedRecurrenceDays.isEmpty()) {
        cmd = helper.buildRecurringCreateCommand(subject, startTime, endTime,
                description, location, isPublic);
      } else {
        cmd = "create event \"" + subject + "\" from "
                + startTime.format(dateTimeFormatter) + " to "
                + endTime.format(dateTimeFormatter);
        if (!description.isEmpty()) {
          cmd += " description \"" + description + "\"";
        }
        if (!location.isEmpty()) {
          cmd += " location \"" + location + "\"";
        }
        if (isPublic) {
          cmd += " public";
        }
      }
      String createResult = commandParser.parseAndExecute(cmd);
      if (!createResult.startsWith("Error")) {
        updateCalendar();
        JOptionPane.showMessageDialog(frame, "Event created successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(frame, createResult, "Error",
                JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void showRecurrenceDialog() {
    String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday"};
    DayOfWeek[] dayMapping = { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    };
    JCheckBox[] dayCheckboxes = new JCheckBox[7];
    JPanel daysPanel = new JPanel(new GridLayout(1, 7));
    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = new JCheckBox(dayNames[i]);
      daysPanel.add(dayCheckboxes[i]);
    }
    JRadioButton untilRadio = new JRadioButton("Until Date");
    JRadioButton timesRadio = new JRadioButton("Occurrences");
    ButtonGroup group = new ButtonGroup();
    group.add(untilRadio);
    group.add(timesRadio);
    JSpinner occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    occurrencesSpinner.setEnabled(false);
    JSpinner untilDateSpinner = new JSpinner(new SpinnerDateModel());
    untilDateSpinner.setEnabled(false);

    untilRadio.addActionListener(e -> {
      if (untilRadio.isSelected()) {
        untilDateSpinner.setEnabled(true);
        occurrencesSpinner.setEnabled(false);
      }
    });
    timesRadio.addActionListener(e -> {
      if (timesRadio.isSelected()) {
        untilDateSpinner.setEnabled(false);
        occurrencesSpinner.setEnabled(true);
      }
    });

    JPanel recurrencePanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    recurrencePanel.add(new JLabel("Repeat on:"), gbc);
    recurrencePanel.add(daysPanel, gbc);

    JPanel radioPanel = new JPanel();
    radioPanel.add(untilRadio);
    radioPanel.add(timesRadio);
    recurrencePanel.add(radioPanel, gbc);

    JPanel untilPanel = new JPanel();
    untilPanel.add(new JLabel("Until Date:"));
    untilPanel.add(untilDateSpinner);
    JPanel timesPanel = new JPanel();
    timesPanel.add(new JLabel("Occurrences:"));
    timesPanel.add(occurrencesSpinner);

    recurrencePanel.add(untilPanel, gbc);
    recurrencePanel.add(timesPanel, gbc);

    int result = JOptionPane.showConfirmDialog(frame, recurrencePanel,
            "Recurrence Details", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
      List<DayOfWeek> chosenDays = new ArrayList<>();
      for (int i = 0; i < 7; i++) {
        if (dayCheckboxes[i].isSelected()) {
          chosenDays.add(dayMapping[i]);
        }
      }
      helper.selectedRecurrenceDays = chosenDays;
      if (untilRadio.isSelected()) {
        Date untilDateRaw = (Date) untilDateSpinner.getValue();
        helper.recurrenceUntilDate = untilDateRaw.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        helper.recurrenceOccurrences = null;
      } else if (timesRadio.isSelected()) {
        helper.recurrenceOccurrences = (Integer) occurrencesSpinner.getValue();
        helper.recurrenceUntilDate = null;
      } else {
        helper.recurrenceUntilDate = null;
        helper.recurrenceOccurrences = null;
      }
    } else {
      helper.selectedRecurrenceDays.clear();
      helper.recurrenceUntilDate = null;
      helper.recurrenceOccurrences = null;
    }
  }

  private void exportCalendar() {
    String filename = JOptionPane.showInputDialog(frame, "Enter filename for export:");
    if (filename == null || filename.trim().isEmpty()) {
      JOptionPane.showMessageDialog(frame, "Filename cannot be empty.",
              "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String result = commandParser.parseAndExecute("export cal " + filename);
    if (!result.startsWith("Error")) {
      JOptionPane.showMessageDialog(frame, result, "Success",
              JOptionPane.INFORMATION_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(frame, result, "Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * The main entry point for the Calendar application.
   * <p>
   * This method schedules the creation and display of the main application window on
   * the Event Dispatch Thread. Command line arguments are currently not used.
   * </p>
   *
   * @param args    Command line arguments.
   *                We need two separate comments on the constructor and the class
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(CalendarStarter::new);
  }
}