package calendar.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calendar.model.CalendarInterface;
import calendar.service.ExportService;
import calendar.service.ExportServiceInterface;

/**
 * Parses and executes user commands for interacting with the calendar system.
 * Supports commands for creating, editing, querying, printing, exporting, and checking events.
 * Handles tokenization of input strings and delegates execution to the {@link CalendarInterface}.
 */
public class CommandParser {

  private final CalendarInterface calendar;

  private static final DateTimeFormatter DATE_TIME_FORMATTER
        = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Constructs a new {@code CommandParser} with the specified calendar interface.
   *
   * @param calendar The {@link CalendarInterface} instance to interact with the calendar system.
   */
  public CommandParser(CalendarInterface calendar) {

    this.calendar = calendar;
  }

  /**
   * Parses and executes a given command string.
   *
   * @param command The command to parse and execute.
   * @return The result of executing the command, or an error message if the command is invalid.
   */
  public String parseAndExecute(String command) {

    if (command == null || command.trim().isEmpty()) {
      return "Error: Empty command";
    }

    String[] tokens = tokenizeCommand(command);

    try {
      String action = tokens[0].toLowerCase();

      if ("triggerException".equals(tokens[0])) {
        throw new RuntimeException("Forced exception for testing");
      }

      switch (action) {
        case "create":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("calendar")) {
            return handleCreateCalendar(tokens);
          } else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("event")) {
            return handleCreateEvent(tokens);
          }
          return "Error: Invalid create command";

        case "edit":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("event")) {
            return handleEditEvent(tokens);
          } else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("events")) {
            return handleEditEvents(tokens);
          } else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("calendar")) {
            return handleEditCalendar(tokens);
          }
          return "Error: Invalid edit command";

        case "use":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("calendar")) {
            return handleUseCalendar(tokens);
          }
          return "Error: Invalid use command";

        case "copy":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("event")) {
            return handleCopyEvent(tokens);
          } else if (tokens.length > 1 && tokens[1].equalsIgnoreCase("events")) {
            return handleCopyEvents(tokens);
          }
          return "Error: Invalid copy command";

        case "print":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("events")) {
            return handlePrintEvents(tokens);
          }
          return "Error: Invalid print command";

        case "export":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("cal")) {
            return handleExportCal(tokens);
          }
          return "Error: Invalid export command";

        case "show":
          if (tokens.length > 1 && tokens[1].equalsIgnoreCase("status")) {
            return handleBusyAt(tokens);
          }
          return "Error: Invalid show command";

        case "exit":
          return "exit";

        default:
          return "Error: Unrecognized command";
      }
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private String handleCreateCalendar(String[] tokens) {

    if (tokens.length != 6 || !tokens[2].equalsIgnoreCase("--name") ||
            !tokens[4].equalsIgnoreCase("--timezone")) {
      return "Error: Invalid create calendar command";
    }

    String name = tokens[3];
    String timezone = tokens[5];

    boolean success = calendar.createCalendar(name, timezone);
    return success ? "Calendar created successfully" :
            "Failed to create calendar (duplicate name or invalid timezone)";
  }

  private String handleEditCalendar(String[] tokens) {

    if (tokens.length != 7 || !tokens[2].equalsIgnoreCase("--name") ||
            !tokens[4].equalsIgnoreCase("--property")) {
      return "Error: Invalid edit calendar command";
    }

    String name = tokens[3];
    String propertyName = tokens[5];

    String newValue = tokens[6];
    boolean success = calendar.editCalendar(name, propertyName, newValue);
    return success ? "Calendar edited successfully" :
            "Failed to edit calendar (invalid name, property, or value)";
  }

  private String handleUseCalendar(String[] tokens) {
    if (tokens.length != 4 || !tokens[2].equalsIgnoreCase("--name")) {
      return "Error: Invalid use calendar command";
    }

    String name = tokens[3];
    boolean success = calendar.useCalendar(name);
    return success ? "Switched to calendar: " + name : "Failed to switch to calendar (not found)";
  }

  private String handleCopyEvent(String[] tokens) {
    if (tokens.length != 9 || !tokens[3].equalsIgnoreCase("on") ||
            !tokens[5].equalsIgnoreCase("--target") ||
            !tokens[7].equalsIgnoreCase("to")) {
      return "Error: Invalid copy event calendar command";
    }

    try {
      String eventName = tokens[2];
      LocalDateTime originalStartTime = LocalDateTime.parse(tokens[4], DATE_TIME_FORMATTER);
      String targetCalendarName = tokens[6];
      LocalDateTime newStartTime = LocalDateTime.parse(tokens[8], DATE_TIME_FORMATTER);

      boolean success = calendar.copySingleEvent(eventName, originalStartTime,
              targetCalendarName, newStartTime);

      return success ? "Event copied successfully" :
              "Failed to copy event due to conflict or Non Existing event";

    } catch (DateTimeParseException e) {
      return "Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'HH:mm";
    }
  }

  private String handleCopyEvents(String[] tokens) {

    if (tokens[2].equalsIgnoreCase("on")) {
      return handleCopyEventsOn(tokens);
    } else if (tokens[2].equalsIgnoreCase("between")) {
      return handleCopyEventsBetween(tokens);
    } else {
      return "Error: Invalid copy events command. Expected 'on' or 'between'.";
    }

  }

  private String handleCopyEventsOn(String[] tokens) {
    if (tokens.length != 8 || !tokens[4].equalsIgnoreCase("--target") ||
            !tokens[6].equalsIgnoreCase("to")) {
      return "Error: Invalid format for copy events on command";
    }

    try {
      LocalDate originalDate = LocalDate.parse(tokens[3], DateTimeFormatter.ISO_LOCAL_DATE);
      String targetCalendarName = tokens[5];
      LocalDate newDate = LocalDate.parse(tokens[7], DateTimeFormatter.ISO_LOCAL_DATE);

      boolean success = calendar.copyEventsOn(originalDate, targetCalendarName, newDate);
      return success ? "Events copied successfully" :
              "Failed to copy events due to conflict or No existing event";

    } catch (DateTimeParseException e) {
      return "Error: Invalid date format. Expected format: YYYY-MM-DD";
    }
  }

  private String handleCopyEventsBetween(String[] tokens) {
    if (tokens.length != 10 || !tokens[4].equalsIgnoreCase("and") ||
            !tokens[6].equalsIgnoreCase("--target") ||
            !tokens[8].equalsIgnoreCase("to")) {
      return "Error: Invalid format for copy events between command.";
    }

    try {

      LocalDate rangeStart = LocalDate.parse(tokens[3], DateTimeFormatter.ISO_LOCAL_DATE);
      LocalDate rangeEnd = LocalDate.parse(tokens[5], DateTimeFormatter.ISO_LOCAL_DATE);
      String targetCalendarName = tokens[7];
      LocalDate newStartDate = LocalDate.parse(tokens[9], DateTimeFormatter.ISO_LOCAL_DATE);

      boolean success =
              calendar.copyEventsBetween(rangeStart, rangeEnd, targetCalendarName, newStartDate);

      return success ? "Events copied successfully" :
              "Failed to copy events due to conflict or No Existing event";

    } catch (DateTimeParseException e) {
      return "Error: Invalid date format. Expected format: YYYY-MM-DD";
    }
  }

  /**
   * Handles the creation of a single event based on the provided command tokens.
   *
   * @param tokens    The tokenized command string.
   * @return          A success message if the event is created successfully,
   *                  or an error message if it fails.
   */
  private String handleCreateEvent(String[] tokens) {
    boolean autoDecline = true;
    int currentIndex = 2;

    if (tokens[currentIndex].equals("--autoDecline")) {
      currentIndex++;
      if (currentIndex >= tokens.length || tokens[currentIndex].equals("from")
              || tokens[currentIndex].equals("on")) {
        return "Error: Missing event name after --autoDecline";
      }
    }

    String subject = tokens[currentIndex];
    currentIndex++;

    if (currentIndex >= tokens.length) {
      return "Error: Missing 'from' or 'on' in create event command";
    }

    LocalDateTime startDateTime = null;
    LocalDateTime endDateTime = null;
    boolean allDay = false;

    if (tokens[currentIndex].equals("on")) {
      currentIndex++;

      allDay = true;

      if (currentIndex >= tokens.length) {
        return "Error: Missing date for all-day event";
      }

      try {
        startDateTime = LocalDate.parse(tokens[currentIndex],
                DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        endDateTime = startDateTime.toLocalDate().atTime(LocalTime.of(23, 59));
        currentIndex++;
      } catch (DateTimeParseException e) {
        return "Error: Invalid date format for all-day event. Expected YYYY-MM-DD";
      }
    } else if (tokens[currentIndex].equals("from")) {
      currentIndex++;

      if (currentIndex >= tokens.length) {
        return "Error: Missing start date/time after 'from'";
      }

      try {
        startDateTime = LocalDateTime.parse(tokens[currentIndex], DATE_TIME_FORMATTER);
        currentIndex++;
      } catch (DateTimeParseException e) {
        return "Error: Invalid start date/time format. Expected YYYY-MM-DDThh:mm";
      }

      if (currentIndex >= tokens.length || !tokens[currentIndex].equals("to")) {
        return "Error: Expected 'to' after start date/time";
      }
      currentIndex++;

      if (currentIndex >= tokens.length) {
        return "Error: Missing end date/time after 'to'";
      }

      try {
        endDateTime = LocalDateTime.parse(tokens[currentIndex], DATE_TIME_FORMATTER);
        currentIndex++;
      } catch (DateTimeParseException e) {
        return "Error: Invalid end date/time format. Expected YYYY-MM-DDThh:mm";
      }
    } else {
      return "Error: Missing 'on' or 'from'";
    }

    if (startDateTime.isAfter(endDateTime)) {
      return "Error: Start date and time cannot be after end date";
    }

    if (currentIndex < tokens.length && tokens[currentIndex].equals("repeats")) {
      return handleCreateRecurringEvent(tokens, currentIndex + 1, subject,
              startDateTime, endDateTime, autoDecline, allDay);
    }

    String[] description = {null};
    String[] location = {null};
    boolean[] isPublic = {false};

    try {
      processOptionalFields(tokens, currentIndex, description, location, isPublic);
    } catch (IllegalArgumentException e) {
      return e.getMessage();
    }

    boolean success = calendar.createEvent(subject, startDateTime, endDateTime,
            description[0], location[0], isPublic[0], autoDecline);
    return success ? "Event created successfully" : "Failed to create event due to conflict";
  }

  /**
   * Handles the creation of a recurring event based on the provided command tokens.
   *
   * @param tokens          The tokenized command string.
   * @param currentIndex    The current index in the token array where recurrence details start.
   * @param subject         The subject or title of the recurring event.
   * @param startDateTime   The start time of the first occurrence of the event.
   * @param endDateTime     The end time of the first occurrence of the event.
   * @param autoDecline     Whether conflicting events should be automatically declined.
   * @return                A success message if the recurring event is created successfully,
   *                        or an error message if it fails.
   */
  private String handleCreateRecurringEvent(String[] tokens, int currentIndex, String subject,
                                            LocalDateTime startDateTime, LocalDateTime endDateTime,
                                            boolean autoDecline, boolean allDay) {
    if (currentIndex >= tokens.length) {
      return "Error: Missing recurrence pattern";
    }

    String recurrencePattern = tokens[currentIndex];
    List<String> recurrenceDays = new ArrayList<>();
    for (char c : recurrencePattern.toCharArray()) {
      recurrenceDays.add(String.valueOf(c));
    }
    currentIndex++;

    if (currentIndex >= tokens.length) {
      return "Error: Missing 'for' or 'until' after recurrence pattern";
    }

    int occurrences = -1;
    LocalDateTime untilDate = null;
    if (tokens[currentIndex].equals("for")) {
      currentIndex++;

      if (currentIndex >= tokens.length) {
        return "Error: Missing number of occurrences";
      }

      try {
        occurrences = Integer.parseInt(tokens[currentIndex]);
      } catch (NumberFormatException e) {
        return "Error: Invalid number of occurrences";
      }

      currentIndex++;

      if (currentIndex >= tokens.length  || !tokens[currentIndex].equals("times")) {
        return "Error: Times missing after the Number of occurrences";
      }

    } else if (tokens[currentIndex].equals("until")) {
      currentIndex++;

      if (currentIndex >= tokens.length) {
        return "Error: Missing until date";
      }

      try {
        if (allDay) {
          untilDate = LocalDate.parse(tokens[currentIndex], DateTimeFormatter.ISO_LOCAL_DATE)
                  .atTime(LocalTime.of(23, 59, 59));
        } else {
          untilDate = LocalDateTime.parse(tokens[currentIndex], DATE_TIME_FORMATTER);
        }
        if (untilDate.isBefore(endDateTime)) {
          return "Until date cannot be after end date";
        }
      } catch (DateTimeParseException e) {
        return "Error: Invalid date/time format for until date";
      }

    }
    currentIndex++;

    String[] description = {null};
    String[] location = {null};
    boolean[] isPublic = {false};

    try {
      processOptionalFields(tokens, currentIndex, description, location, isPublic);
    } catch (IllegalArgumentException e) {
      return e.getMessage();
    }

    boolean success = calendar.createRecurringEvent(subject, startDateTime, endDateTime,
            description[0], location[0], isPublic[0], autoDecline, recurrenceDays,
            occurrences, untilDate);
    return success ? "Recurring event created successfully" :
            "Failed to create recurring event due to conflict";
  }

  /**
   * Helper method to process optional fields such as description, location, and public status.
   *
   * @param tokens       The tokenized command string.
   * @param currentIndex The current index in the token array where optional fields start.
   * @param description  The description of the event (mutable).
   * @param location     The location of the event (mutable).
   * @param isPublic     Whether the event is public (mutable).
   */
  private void processOptionalFields(String[] tokens, int currentIndex, String[] description,
                                     String[] location, boolean[] isPublic) {
    while (currentIndex < tokens.length) {
      switch (tokens[currentIndex]) {
        case "description":
          currentIndex++;
          if (currentIndex >= tokens.length) {
            throw new IllegalArgumentException("Error: Missing description after" +
                    " 'description' keyword");
          }
          description[0] = tokens[currentIndex++];
          break;

        case "location":
          currentIndex++;
          if (currentIndex >= tokens.length) {
            throw new IllegalArgumentException("Error: Missing location after 'location' keyword");
          }
          location[0] = tokens[currentIndex++];
          break;

        case "public":
          isPublic[0] = true;
          currentIndex++;
          break;

        default:
          throw new IllegalArgumentException("Error: Unexpected token '" + tokens[currentIndex] +
                  "' in create event command");
      }
    }
  }

  /**
   * Handles editing a single event based on the provided command tokens.
   *
   * @param tokens    The tokenized command string.
   * @return A success message if the event is edited successfully, or an error message if it fails.
   */
  private String handleEditEvent(String[] tokens) {
    if (tokens.length != 10 || !tokens[4].equalsIgnoreCase("from") ||
            !tokens[6].equalsIgnoreCase("to") ||
            !tokens[8].equalsIgnoreCase("with")) {
      return "Error: Invalid edit event command.";
    }

    try {
      String propertyToEdit = tokens[2];
      String subject = tokens[3];

      LocalDateTime startDateTime = LocalDateTime.parse(tokens[5], DATE_TIME_FORMATTER);
      LocalDateTime endDateTime = LocalDateTime.parse(tokens[7], DATE_TIME_FORMATTER);

      if (startDateTime.isAfter(endDateTime)) {
        return "Error: Start date/time cannot be after end date/time.";
      }

      String newValue = tokens[9];

      boolean success = calendar.editEvent(subject, startDateTime, propertyToEdit, newValue);
      return success ? "Event edited successfully" :
              "Failed to edit event (event not found or invalid property)";
    } catch (DateTimeParseException e) {
      return "Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'hh:mm";
    }
  }

  /**
   * Handles editing multiple events (recurring or otherwise) based on the provided command tokens.
   *
   * @param tokens    The tokenized command string.
   * @return          A success message if the events are edited successfully,
   *                  or an error message if it fails.
   */
  private String handleEditEvents(String[] tokens) {
    int currentIndex = 2;

    if (currentIndex >= tokens.length) {
      return "Error: Invalid edit command";
    }

    String propertyToEdit = tokens[currentIndex];
    currentIndex++;

    if (currentIndex >= tokens.length) {
      return "Error: Missing event name";
    }

    String subject = (tokens[currentIndex]);
    currentIndex++;

    if (currentIndex >= tokens.length) {
      return "Error: Missing NewValue or From";
    }

    LocalDateTime dateTime = null;
    String newValue;
    boolean success = false;

    if (tokens[currentIndex].equalsIgnoreCase("from")) {
      currentIndex++;
      if (currentIndex >= tokens.length) {
        return "Error: Missing start date/time after 'from'";
      }

      try {
        dateTime = LocalDateTime.parse(tokens[currentIndex], DATE_TIME_FORMATTER);
        currentIndex++;
      } catch (DateTimeParseException e) {
        return "Error: Invalid date/time format in edit event command";
      }

      if (currentIndex >= tokens.length || !tokens[currentIndex].equalsIgnoreCase("with")) {
        return "Error: Expected 'with' after date/time format";
      }
      currentIndex++;

      if (currentIndex >= tokens.length) {
        return "Error: Missing NewPropertyValue after 'with'";
      }
      newValue = tokens[currentIndex];

      success = calendar.editRecurringEventDate(subject, dateTime, propertyToEdit, newValue);

    } else {
      newValue = tokens[currentIndex];
      currentIndex++;
      if (currentIndex < tokens.length) {
        return "Error: Unexpected input after new property value";
      }
      success = calendar.editRecurringEventValue(subject, propertyToEdit, newValue);
    }
    return success ? "Event edited successfully" :
            "Failed to edit event (event not found or invalid property)";
  }

  /**
   * Handles printing events based on a specific date or range from the provided command tokens.
   *
   * @param tokens    The tokenized command string.
   * @return          A formatted list of events for the specified date or range,
   *                  or an error message if no events are found or input is invalid.
   */
  private String handlePrintEvents(String[] tokens) {
    String timeIndicator = tokens[2];

    if (timeIndicator.equals("on")) {
      if (tokens.length < 4) {
        return "Error: Missing date in print events command";
      }

      LocalDate date;
      try {
        date = LocalDate.parse(tokens[3], DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (DateTimeParseException e) {
        return "Error: Invalid date format in print events command. Expected format: YYYY-MM-DD";
      }

      List<String> events = calendar.queryEventsByDate(date.atStartOfDay());

      if (events.isEmpty()) {
        return "No events on " + tokens[3];
      }

      StringBuilder result = new StringBuilder("Events on " + tokens[3] + ":\n");
      for (String event : events) {
        result.append("- ").append(event).append("\n");
      }
      return result.toString();
    } else if (timeIndicator.equals("from")) {
      if (tokens.length < 6 || !tokens[4].equals("to")) {
        return "Error: Invalid format for print events from/to command. " +
                "Expected format: 'print events from <startDateTime> to <endDateTime>'";
      }

      LocalDateTime startDate;
      LocalDateTime endDate;
      try {
        startDate = LocalDateTime.parse(tokens[3], DATE_TIME_FORMATTER);
        endDate = LocalDateTime.parse(tokens[5], DATE_TIME_FORMATTER);
      } catch (DateTimeParseException e) {
        return "Error: Invalid date/time format in print events command." +
                " Expected format: YYYY-MM-DD'T'HH:mm";
      }

      List<String> events = calendar.queryEventsByRange(startDate, endDate);

      if (events.isEmpty()) {
        return "No events from " + tokens[3] + " to " + tokens[5];
      }

      StringBuilder result = new StringBuilder("Events from " + tokens[3] +
              " to " + tokens[5] + ":\n");
      for (String event : events) {
        result.append("- ").append(event).append(" ");
      }
      return result.toString();
    }
    else {
      return "Error: Expected 'on' or 'from' after 'print events'";
    }
  }

  /**
   * Handles exporting calendar data to a CSV file based on the provided command tokens.
   *
   * @param tokens    The tokenized command string containing export details (e.g., filename).
   * @return A success message if the export is successful, or an error message if it fails.
   */
  private String handleExportCal(String[] tokens) {
    if (tokens.length < 3) {
      return "Error: Missing filename in export command";
    }

    String filename = tokens[2];

    try {
      List<String> csvLines = calendar.eventsAsCSV();
      ExportServiceInterface exportService = new ExportService();
      return exportService.exportToCSV(csvLines, filename);
    } catch (IllegalStateException e) {
      return "Error: No active calendar selected.";
    }
  }


  /**
   * Checks whether a specific date and time is busy based on the provided command tokens.
   *
   * @param tokens    The tokenized command string containing date and time.
   * @return          "Busy" if there are events at that time,
   *                  "Available" otherwise, or an error message if input is invalid.
   */
  private String handleBusyAt(String[] tokens) {
    int currentIndex = 2;

    if (!tokens[currentIndex].equalsIgnoreCase("on")) {
      return "Error: Expected 'on' after 'show status'";
    }

    currentIndex++;
    if (currentIndex >= tokens.length) {
      return "Error: Missing date/time after 'on'";
    }

    LocalDateTime dateTime;
    try {
      dateTime = LocalDateTime.parse(tokens[currentIndex], DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      return "Error: Invalid date/time format in show status command";
    }

    boolean isBusy = calendar.isBusy(dateTime);
    return isBusy ? "Busy" : "Available";
  }

  /**
   * Tokenizes a raw command string into individual components,
   * handling quoted strings as single tokens.
   *
   * @param command   The raw command string to tokenize.
   * @return An array of strings representing individual tokens from the command string.
   */
  private String[] tokenizeCommand(String command) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(command);
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else {
        tokens.add(matcher.group(2));
      }
    }

    return tokens.toArray(new String[0]);
  }
}