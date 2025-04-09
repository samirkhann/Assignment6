package calendar.controller;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for processing calendar event data.
 * <p>
 * This class provides helper methods for extracting components from calendar event
 * description strings (such as the subject, start time, and end time) and for building
 * command strings for creating recurring events. Its methods are used by the GUI and
 * controller to process and format event data consistently.
 * </p>
 */
public class CalendarHelper {
  public final DateTimeFormatter dateTimeFormatter =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public List<DayOfWeek> selectedRecurrenceDays = new ArrayList<>();
  public Integer recurrenceOccurrences = null;
  public LocalDateTime recurrenceUntilDate = null;

  /**
   * Parses a command result (assumed to be a multi-line string with lines starting with "- ")
   * and returns a list containing each event's details.
   */
  public List<String> parseEvents(String cmdResult) {
    List<String> lines = new ArrayList<>();
    String[] rawLines = cmdResult.split("\n");
    for (String line : rawLines) {
      line = line.trim();
      if (line.startsWith("- ")) {
        lines.add(line.substring(2).trim());
      }
    }
    return lines;
  }

  /**
   * Extracts the subject from an event description line.
   * Expected format: "Subject (startTime - endTime)"
   */
  public String extractSubject(String line) {
    int openParen = line.indexOf("(");
    if (openParen < 1) {
      return null;
    }
    String subject = line.substring(0, openParen).trim();
    return subject.isEmpty() ? null : subject;
  }

  /**
   * Extracts the start time from an event description line.
   * Expected format: "Subject (startTime - endTime)"
   */
  public LocalDateTime extractStartTime(String line) {
    int openParen = line.indexOf("(");
    if (openParen < 0) {
      return null;
    }
    int closeParen = line.indexOf(")", openParen);
    String times = line.substring(openParen + 1, closeParen).trim();
    int dashIdx = times.indexOf(" - ");
    String startStr = times.substring(0, dashIdx).trim();
    try {
      return LocalDateTime.parse(startStr);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Extracts the end time (as a string) from an event description line.
   * Expected format: "Subject (startTime - endTime)"
   */
  public String getEndTime(String eventString) {
    int openParen = eventString.indexOf("(");
    if (openParen < 0) {
      return null;
    }
    int closeParen = eventString.indexOf(")", openParen);
    String timeRange = eventString.substring(openParen + 1, closeParen).trim();
    int dashIdx = timeRange.indexOf(" - ");
    String endStr = timeRange.substring(dashIdx + 3).trim();
    return endStr.isEmpty() ? null : endStr;
  }

  /**
   * Builds the command string for creating a recurring event.
   */
  public String buildRecurringCreateCommand(String subject, LocalDateTime startTime,
                                            LocalDateTime endTime, String description,
                                            String location, boolean isPublic) {
    StringBuilder pattern = new StringBuilder();
    for (DayOfWeek day : selectedRecurrenceDays) {
      switch (day) {
        case MONDAY:
          pattern.append("M");
          break;
        case TUESDAY:
          pattern.append("T");
          break;
        case WEDNESDAY:
          pattern.append("W");
          break;
        case THURSDAY:
          pattern.append("R");
          break;
        case FRIDAY:
          pattern.append("F");
          break;
        case SATURDAY:
          pattern.append("S");
          break;
        case SUNDAY:
          pattern.append("U");
          break;
      }
    }
    String cmd = "create event \"" + subject + "\" from "
            + startTime.format(dateTimeFormatter) + " to "
            + endTime.format(dateTimeFormatter) + " repeats " + pattern;
    if (recurrenceUntilDate != null) {
      cmd += " until " + recurrenceUntilDate;
    } else if (recurrenceOccurrences != null) {
      cmd += " for " + recurrenceOccurrences + " times";
    }
    if (!description.isEmpty()) {
      cmd += " description \"" + description + "\"";
    }
    if (!location.isEmpty()) {
      cmd += " location \"" + location + "\"";
    }
    if (isPublic) {
      cmd += " public";
    }
    return cmd;
  }
}