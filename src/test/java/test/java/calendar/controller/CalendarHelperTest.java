package test.java.calendar.controller;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import calendar.controller.CalendarHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The CalendarHelperTest class provides a suite of unit tests for the CalendarHelper class.
 * It verifies that methods for parsing event strings, building recurring event commands,
 * and extracting specific event details (such as the subject, start time, and end time)
 * behave correctly under both normal and edge-case input conditions.
 */
public class CalendarHelperTest {

  private CalendarHelper helper;

  @Before
  public void setUp() {
    helper = new CalendarHelper();
    helper.selectedRecurrenceDays.clear();
    helper.recurrenceOccurrences = null;
    helper.recurrenceUntilDate = null;
  }

  @Test
  public void testParseEvents_MixedLines() {
    String input = "Header line\n- Event One\nRandom Text\n-   Event Two   \nNo prefix here";
    List<String> events = helper.parseEvents(input);
    assertEquals("Should parse two events", 2, events.size());
    assertEquals("Event One", events.get(0));
    assertEquals("Event Two", events.get(1));
  }

  @Test
  public void testParseEvents_NoMatchingLines() {
    String input = "Line one\nLine two\nLine three";
    List<String> events = helper.parseEvents(input);
    assertTrue("No events should be parsed", events.isEmpty());
  }

  @Test
  public void testBuildRecurringCreateCommand_NoRecurrenceExtras() {
    String subject = "Event";
    LocalDateTime startTime = LocalDateTime.parse("2025-04-09T08:00");
    LocalDateTime endTime = LocalDateTime.parse("2025-04-09T09:00");
    String description = "";
    String location = "";
    boolean isPublic = false;
    String cmd = helper.buildRecurringCreateCommand(subject, startTime, endTime,
            description, location, isPublic);
    String expected = "create event \"Event\" from 2025-04-09T08:00 to 2025-04-09T09:00 repeats ";
    assertEquals("Command should match when no extras are provided", expected, cmd);
  }

  @Test
  public void testBuildRecurringCreateCommand_WithDescriptionLocationPublic() {
    String subject = "Team Meeting";
    LocalDateTime startTime = LocalDateTime.parse("2025-04-09T10:00");
    LocalDateTime endTime = LocalDateTime.parse("2025-04-09T11:00");
    String description = "Discuss project status";
    String location = "Conference Room";
    boolean isPublic = true;
    String cmd = helper.buildRecurringCreateCommand(subject, startTime, endTime,
            description, location, isPublic);
    String expected = "create event \"Team Meeting\" from 2025-04-09T10:00 to " +
            "2025-04-09T11:00 repeats " +
            " description \"Discuss project status\" location \"Conference Room\" public";
    assertEquals("Command should include description, location, and public flag", expected, cmd);
  }

  @Test
  public void testBuildRecurringCreateCommand_WithRecurrenceUntilDate() {
    helper.selectedRecurrenceDays.add(DayOfWeek.MONDAY);
    helper.selectedRecurrenceDays.add(DayOfWeek.WEDNESDAY);
    LocalDateTime until = LocalDateTime.parse("2025-05-01T00:00");
    helper.recurrenceUntilDate = until;
    String subject = "Yoga Class";
    LocalDateTime startTime = LocalDateTime.parse("2025-04-10T07:00");
    LocalDateTime endTime = LocalDateTime.parse("2025-04-10T08:00");
    String description = "";
    String location = "";
    boolean isPublic = false;
    String cmd = helper.buildRecurringCreateCommand(subject, startTime, endTime,
            description, location, isPublic);
    String expected = "create event \"Yoga Class\" from 2025-04-10T07:00 to " +
            "2025-04-10T08:00 repeats MW until "
            + until.toString();
    assertEquals("Command should include recurrenceUntilDate and recurrence pattern",
            expected, cmd);
  }

  @Test
  public void testBuildRecurringCreateCommand_WithRecurrenceOccurrences() {
    helper.selectedRecurrenceDays.add(DayOfWeek.FRIDAY);
    helper.recurrenceOccurrences = 5;
    helper.recurrenceUntilDate = null;
    String subject = "Standup";
    LocalDateTime startTime = LocalDateTime.parse("2025-04-11T09:00");
    LocalDateTime endTime = LocalDateTime.parse("2025-04-11T09:30");
    String description = "Update meeting";
    String location = "Online";
    boolean isPublic = true;
    String cmd = helper.buildRecurringCreateCommand(subject, startTime, endTime,
            description, location, isPublic);
    String expected = "create event \"Standup\" from 2025-04-11T09:00 to " +
            "2025-04-11T09:30 repeats " +
            "F for 5 times description \"Update meeting\" location \"Online\" public";
    assertEquals("Command should include recurrenceOccurrences and extra details", expected, cmd);
  }

  @Test
  public void testExtractSubject_Valid() {
    String input = "Launch (2025-04-10T12:00 - 2025-04-10T13:00)";
    String subject = helper.extractSubject(input);
    assertEquals("Launch", subject);
  }

  @Test
  public void testExtractSubject_NoOpeningParen() {
    String input = "NoParenthesesInfo";
    assertNull("Should return null when '(' is missing", helper.extractSubject(input));
  }

  @Test
  public void testExtractSubject_EmptyBeforeParen() {
    String input = "   (2025-04-10T12:00 - 2025-04-10T13:00)";
    assertNull("Empty subject should yield null", helper.extractSubject(input));
  }

  @Test
  public void testExtractSubject_Boundary() {
    String input = "A(2025-04-10T12:00 - 2025-04-10T13:00)";
    assertEquals("A", helper.extractSubject(input));
  }

  @Test
  public void testExtractStartTime_Valid() {
    String input = "Meeting (2025-04-09T10:00 - 2025-04-09T11:00)";
    LocalDateTime start = helper.extractStartTime(input);
    LocalDateTime expected = LocalDateTime.parse("2025-04-09T10:00");
    assertEquals("Start time should be parsed correctly", expected, start);
  }

  @Test
  public void testExtractStartTime_NoOpeningParen() {
    String input = "Meeting 2025-04-09T10:00 - 2025-04-09T11:00)";
    assertNull("Should return null when '(' is missing", helper.extractStartTime(input));
  }

  @Test
  public void testExtractStartTime_InvalidDate() {
    String input = "Meeting (invalid - 2025-04-09T11:00)";
    assertNull("Should return null if parsing fails", helper.extractStartTime(input));
  }

  @Test
  public void testExtractStartTime_Boundary() {
    String input = "(2025-04-09T10:00 - 2025-04-09T11:00)";
    LocalDateTime start = helper.extractStartTime(input);
    LocalDateTime expected = LocalDateTime.parse("2025-04-09T10:00");
    assertEquals("Boundary case: start time should be parsed correctly", expected, start);
  }


  @Test
  public void testGetEndTime_Valid() {
    String input = "Event (2025-04-09T10:00 - 2025-04-09T11:00)";
    String end = helper.getEndTime(input);
    assertEquals("2025-04-09T11:00", end);
  }

  @Test
  public void testGetEndTime_NoOpeningParen() {
    String input = "Event 2025-04-09T10:00 - 2025-04-09T11:00)";
    assertNull("Should return null when '(' is missing", helper.getEndTime(input));
  }

  @Test
  public void testExtractStartTime_InvalidDateReturnsNull() {
    String input = "Meeting (invalid - 2025-04-09T11:00)";
    assertNull("Invalid start date should yield null", helper.extractStartTime(input));
  }

  @Test
  public void testGetEndTime_MissingOpenParenReturnsNull() {
    String input = "Event 2025-04-09T10:00 - 2025-04-09T11:00)";
    assertNull("Missing '(' should yield null for getEndTime", helper.getEndTime(input));
  }

  @Test
  public void testGetEndTime_EventStringStartsWithParen() {
    String event = "(2025-04-09T10:00 - 2025-04-09T11:00)";
    String result = helper.getEndTime(event);
    assertEquals("The correct end time should be extracted", "2025-04-09T11:00", result);
  }

  @Test
  public void testExtractStartTime_NoOpenParen() {
    String input = "Meeting 2025-04-09T10:00 - 2025-04-09T11:00)";
    assertNull("Missing '(' should yield null", helper.extractStartTime(input));
  }

  @Test
  public void testGetEndTime_NoOpenParen() {
    String input = "Event 2025-04-09T10:00 - 2025-04-09T11:00)";
    assertNull("Missing '(' in getEndTime should yield null", helper.getEndTime(input));
  }

  @Test
  public void testGetEndTime_Boundary() {
    String input = "(2025-04-09T10:00 - 2025-04-09T11:00)";
    String expected = "2025-04-09T11:00";
    String actual = helper.getEndTime(input);
    assertEquals("Boundary: should correctly extract end time using openParen+1",
            expected, actual);
  }

}