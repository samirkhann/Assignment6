package calendar.controller;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import calendar.model.CalendarInterface;
import calendar.model.CalendarManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link CommandParser} class.
 * Verifies that commands are correctly parsed and executed, including:
 * - Creation of single and recurring events.
 * - Editing events and their properties.
 * - Querying, printing, and exporting events.
 * - Handling invalid or malformed commands.
 * This test class ensures that the {@link CommandParser} interacts,
 * correctly with the {@link CalendarInterface}.
 */
public class CommandParserTest {

  @Test
  public void testEmptyCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("");
    assertEquals("Error: Empty command", result);

    result = parser.parseAndExecute(null);
    assertEquals("Error: Empty command", result);
  }

  @Test
  public void testUnrecognizedCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("unknownCommand");
    assertEquals("Error: Unrecognized command", result);
  }

  @Test
  public void testParseAndExecuteInvalidCreateCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("create invalid");
    assertEquals("Error: Invalid create command", result);
  }

  @Test
  public void testParseAndExecuteInvalidEditCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("edit invalid");
    assertEquals("Error: Invalid edit command", result);
  }

  @Test
  public void testParseAndExecuteInvalidPrintCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("print invalid");
    assertEquals("Error: Invalid print command", result);
  }

  @Test
  public void testParseAndExecuteInvalidExportCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("export invalid");
    assertEquals("Error: Invalid export command", result);
  }

  @Test
  public void testParseAndExecuteInvalidStatusCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("show invalid");
    assertEquals("Error: Invalid show command", result);
  }

  @Test
  public void testParseAndExecuteUnrecognizedCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("unknownCommand");
    assertEquals("Error: Unrecognized command", result);
  }

  @Test
  public void testParseAndExecuteMissingAction() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Empty command", result);
  }

  @Test
  public void testParseAndExecuteInvalidAction() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "unknownAction";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Unrecognized command", result);
  }

  @Test
  public void testParseAndExecuteExitCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "exit";
    String result = parser.parseAndExecute(command);
    assertEquals("exit", result);
  }

  @Test
  public void testParseAndExecuteCreateCommandMissingSecondToken() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid create command", result);
  }

  @Test
  public void testParseAndExecuteEditCommandMissingSecondToken() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "edit";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid edit command", result);
  }

  @Test
  public void testParseAndExecutePrintCommandMissingSecondToken() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "print";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid print command", result);
  }

  @Test
  public void testParseAndExecuteExportCommandMissingSecondToken() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "export";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid export command", result);
  }

  @Test
  public void testParseAndExecuteBusyCommandMissingSecondToken() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "show";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid show command", result);
  }

  @Test
  public void testParseAndExecuteGeneralExceptionCatchBlock() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "triggerException";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Forced exception for testing", result);
  }

  @Test
  public void testParseAndExecuteWithException2() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "whats up";
    String result = parser.parseAndExecute(command);
    assertTrue(result.startsWith("Error"));
  }

  @Test
  public void testParseAndExecuteExceptionHandling() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "create event Weekly Meeting from invalidDate";
    String result = parser.parseAndExecute(command);

    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testCreateEvent() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);

  }

  @Test
  public void testCreateRecurringEventWithOccurrences() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M for 5";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Times missing after the Number of occurrences", result);

    String commandNew = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M for 5 times";
    String resultNew = parser.parseAndExecute(commandNew);
    assertEquals("Recurring event created successfully", resultNew);
  }

  @Test
  public void testCreateRecurringEventWithUntilDate() {
    CommandParser parser = new CommandParser(new CalendarManager());
    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M until 2025-04-10T23:59";
    String result = parser.parseAndExecute(command);
    assertEquals("Recurring event created successfully", result);
  }

  @Test
  public void testInvalidCreateEventCommands() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command1 = "create event \"Weekly Meeting\"";
    assertEquals("Error: Missing 'from' or 'on' in create event command",
            parser.parseAndExecute(command1));

    String command2 = "create event \"Weekly Meeting\" from";
    assertEquals("Error: Missing start date/time after 'from'",
            parser.parseAndExecute(command2));

    String command3 = "create event \"Weekly Meeting\" from 03-10-2025T11:00";
    assertEquals("Error: Invalid start date/time format. Expected YYYY-MM-DDThh:mm",
            parser.parseAndExecute(command3));

    String command4 = "create event \"Weekly Meeting\" from 2025-03-10T10:00";
    assertEquals("Error: Expected 'to' after start date/time",
            parser.parseAndExecute(command4));

    String command5 = "create event \"Weekly Meeting\" from 2025-03-10T10:00" +
            " to 2025-03-10T11:00 until";
    assertEquals("Error: Unexpected token 'until' in create event command",
            parser.parseAndExecute(command5));

    String command6 = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 03-10-2025T11:00";
    assertEquals("Error: Invalid end date/time format. Expected YYYY-MM-DDThh:mm",
            parser.parseAndExecute(command6));
  }

  @Test
  public void testHandleCreateEventMissingEndTime() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing end date/time after 'to'", result);
  }

  @Test
  public void testHandleCreateEventValidAllDay() {
    CommandParser parser = new CommandParser(new CalendarManager());
    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event AllDayMeeting on 2025-03-10";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);
  }

  @Test
  public void testRecurringEventWithInvalidWeekday() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 repeats Z until 2025-04-15T10:00";
    String queryResult = parser.parseAndExecute(createCommand);
    assertEquals("Error: Inappropriate day: Z", queryResult);
  }

  @Test
  public void testHandleCreateEventMissingDate() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event AllDayMeeting on";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing date for all-day event", result);
  }

  @Test
  public void testHandleCreateEventInvalidDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event AllDayMeeting on invalidDate";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid date format for all-day event. " +
            "Expected YYYY-MM-DD", result);
  }

  @Test
  public void testHandleCreateEventMissingFromOrOnKeyword() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event AllDayMeeting";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing 'from' or 'on' in create event command", result);
  }

  @Test
  public void testHandleCreateEventMissingDateTime() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event Meeting on";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing date for all-day event", result);
  }

  @Test
  public void testHandleCreateRecurringEventMissingRecurrencePattern() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing recurrence pattern", result);
  }

  @Test
  public void testHandleCreateRecurringEventMissingForOrUntil() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M ";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing 'for' or 'until' after recurrence pattern", result);
  }

  @Test
  public void testHandleCreateRecurringEventMissingNumberOfOccurrences() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M for";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing number of occurrences", result);
  }

  @Test
  public void testHandleCreateRecurringEventInvalidNumberOfOccurrences() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M for invalidNumber";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid number of occurrences", result);
  }

  @Test
  public void testHandleCreateRecurringEventMissingUntilDate() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M until";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Missing until date", result);
  }

  @Test
  public void testHandleCreateRecurringEventInvalidUntilDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M until invalidDate";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid date/time format for until date", result);
  }

  @Test
  public void testHandleCreateRecurringEventWithoutForAndUtilDate() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "create event \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 repeats M until invalidDate";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid date/time format for until date", result);
  }

  @Test
  public void testHandleCreateEventAutoDeclineAtBoundaryPosition() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "create event Meeting from 2025-03-10T10:00 --autoDecline";
    String result = parser.parseAndExecute(command);

    assertTrue(result.startsWith("Error:"));
  }

  @Test
  public void testHandleCreateEventAutoDeclineWithNoName() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String invalidCommand = "create event --autoDecline";
    String invalidResult = parser.parseAndExecute(invalidCommand);
    assertEquals("Error: Missing event name after --autoDecline", invalidResult);
  }

  @Test
  public void testHandleCreateEventWithAutoDeclineFlag() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event --autoDecline \"Weekly Meeting\" " +
            "from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result = parser.parseAndExecute(command);

    assertEquals("Event created successfully", result);
  }

  @Test
  public void testHandleCreateEventWithAutoDeclineFlagAndWithout() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event --autoDecline \"Weekly Meeting\" " +
            "from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);

    String command1 = "create event --autoDecline Meeting " +
            "from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result1 = parser.parseAndExecute(command1);
    assertEquals("Error: Conflict detected", result1);

    String command2 = "create event Meeting from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result2 = parser.parseAndExecute(command2);
    assertEquals("Error: Conflict detected", result2);
  }

  @Test
  public void testHandleCreateRecurringEventAutoDeclineValidation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event --autoDecline Meeting " +
            "from 2025-03-10T10:00 to 2025-03-10T11:00 repeats M until 2025-04-10T23:59";
    String result = parser.parseAndExecute(command);
    assertEquals("Recurring event created successfully", result);

    String testAuto = "create event --autoDecline Meeting " +
            "from 2025-03-17T10:00 to 2025-03-17T11:00 repeats M until 2025-04-10T23:59";
    String resultAuto = parser.parseAndExecute(testAuto);
    assertEquals("Error: Conflicted event: Meeting", resultAuto);

    String invalidCommand = "create event Meeting " +
            "from 2025-03-17T10:00 to 2025-03-17T11:00 repeats M until 2025-04-10T23:59";
    String invalidResult = parser.parseAndExecute(invalidCommand);
    assertEquals("Error: Conflicted event: Meeting", invalidResult);
  }

  @Test
  public void testAllDayEventAutoDeclineValidation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event --autoDecline Meeting on 2025-03-10";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);

    String testAuto = "create event --autoDecline Meeting " +
            "from 2025-03-10T01:00 to 2025-03-10T02:00 repeats M until 2025-04-10T23:59";
    String resultAuto = parser.parseAndExecute(testAuto);
    assertEquals("Error: Conflicted event: Meeting", resultAuto);

    String invalidCommand = "create event Meeting from 2025-03-10T10:00 to 2025-03-10T11:00";
    String invalidResult = parser.parseAndExecute(invalidCommand);
    assertEquals("Error: Conflict detected", invalidResult);
  }

  @Test
  public void testWithDescriptionLocationPublic() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to" +
            " 2025-03-10T11:00 description Meeting location Boston public";
    String queryResult = parser.parseAndExecute(createCommand);
    assertEquals("Event created successfully", queryResult);

    String printCommand = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to" +
            " 2025-03-10T11:00 description Meeting location Boston public";
    String printResult = parser.parseAndExecute(printCommand);
    assertEquals("Error: Conflict detected", printResult);
  }

  @Test
  public void testRecurringEventWithDescriptionLocationPublic() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to" +
            " 2025-03-10T11:00 repeats M until 2025-04-10T23:59 description" +
            " Meeting location \"Boston public\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Recurring event created successfully", queryResult);
  }

  @Test
  public void testUnexpectedToken() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String createCommand = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to" +
            " 2025-03-10T11:00 extraToken";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Unexpected token 'extraToken' in " +
            "create event command", queryResult);
  }

  @Test
  public void testEventWithDescriptionAndLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 description \"Weekly team Meeting\" location \"New York public\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Event created successfully", queryResult);
  }

  @Test
  public void testRecurringEventWithDescriptionAndLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Daily Standup\" from 2025-03-15T09:00 to " +
            "2025-03-15T09:30 repeats U until 2025-04-15T10:00 description \"Daily" +
            " team sync-up\" location \"Virtual public\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Recurring event created successfully", queryResult);
  }

  @Test
  public void testAllDayEventWithDescriptionAndLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Company Retreat\" on 2025-06-01 description" +
            " \"Annual company retreat\" location \"Boston public\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Event created successfully", queryResult);
  }

  @Test
  public void testEventWithOnlyDescription() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 description \"Weekly team meeting\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Event created successfully", queryResult);
  }

  @Test
  public void testRecurringEventWithOnlyDescription() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 repeats U until 2025-04-15T10:00 description \"Weekly team meeting\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Recurring event created successfully", queryResult);
  }

  @Test
  public void testEventWithOnlyLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 location \"New York\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Event created successfully", queryResult);
  }

  @Test
  public void testEventRecurringWithOnlyLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 repeats U until 2025-04-15T10:00 location \"New York\"";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Recurring event created successfully", queryResult);
  }

  @Test
  public void testRecurringUnexpectedToken() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to " +
            "2025-03-10T11:00 repeats U until 2025-04-15T10:00 extraToken";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Unexpected token 'extraToken' in create event command", queryResult);
  }

  @Test
  public void testEventWithEmptyLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Meeting\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 location";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Missing location after 'location' keyword", queryResult);
  }

  @Test
  public void testEventWithEmptyDescription() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 description";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Missing description after 'description' keyword", queryResult);
  }

  @Test
  public void testRecurringEventWithEmptyLocation() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Meeting\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 repeats U until 2025-04-15T10:00 location";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Missing location after 'location' keyword", queryResult);
  }

  @Test
  public void testRecurringEventWithEmptyDescription() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Team Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T10:00 repeats U until 2025-04-15T10:00 description";
    String queryResult = parser.parseAndExecute(createCommand);

    assertEquals("Error: Missing description after 'description' keyword", queryResult);
  }

  @Test
  public void testHandleEditEventMissingPropertyToEdit() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event \"Weekly Meeting\" on 2025-03-10T10:00 " +
            "description to \"Updated Description\"";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testHandleEditEventMissingDateTime() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "edit event Weekly Meeting on 2025-03-10T10:00 set " +
            "description to Updated Description";
    String result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testHandleEditEventValidCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));
    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String commandEdit = "edit event subject \"Weekly Meeting\" from " +
            "2025-03-10T10:00 to 2025-03-10T11:00 with Meeting";
    String resultEdit = parser.parseAndExecute(commandEdit);

    assertEquals("Event edited successfully", resultEdit);
  }

  @Test
  public void testHandleEditEventsValidCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));
    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String commandEdit = "edit events subject \"Weekly Meeting\" from " +
            "2025-03-10T10:00 with Meeting";
    String resultEdit = parser.parseAndExecute(commandEdit);

    assertEquals("Event edited successfully", resultEdit);
  }

  @Test
  public void testHandleEditEventsValidCommand2() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));
    String command = "create event Weekly Meeting from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String commandEdit = "edit events subject \"Weekly Meeting\" \"Meeting\" with Meeting";
    String resultEdit = parser.parseAndExecute(commandEdit);

    assertEquals("Error: Unexpected input after new property value", resultEdit);
  }

  @Test
  public void testEditEventInvalidStartDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from invalid-date to " +
            "2025-03-15T12:00 with \"Updated Project Kickoff Name\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'hh:mm", result);
  }

  @Test
  public void testEditEventInvalidEndDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 " +
            "to Invalid with \"Updated Project Kickoff Name\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'hh:mm"
            , result);
  }

  @Test
  public void testEditEventStartAfterEndDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 " +
            "to 2025-03-14T10:00 with \"Updated Project Kickoff Name\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Start date/time cannot be after end date/time."
            , result);
  }

  @Test
  public void testEditRecurringEventsStartingAtSpecificTime() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T11:00 repeats U until 2025-04-16T10:00 description \"Weekly Meeting\"";
    String resultCommand = parser.parseAndExecute(createCommand);
    assertEquals("Recurring event created successfully", resultCommand);

    String command = "edit events description \"Weekly Sync\" from 2025-03-16T09:00" +
            " with \"Updated weekly meeting\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testMissingStartDate() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingToKeyword() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 Updated Name";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingToKeyword2() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingEndDate() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 to";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingPropertyToEdit() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingEventName() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testInvalidStartDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from invalid-date to " +
            "2025-03-15T12:00 with \"Updated Project Kickoff Name\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'hh:mm", result);
  }

  @Test
  public void testInvalidEndDateFormat() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 to " +
            "invalid-date with \"Updated Project Kickoff Name\"";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'hh:mm", result);
  }

  @Test
  public void testMissingWithKeyword() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" from 2025-03-15T10:00 to " +
            "2025-03-15T12:00 Updated Project Kickoff Name";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingWithKeyword2() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit event name \"Project Kickoff\" Updated Project Kickoff Name" +
            "from 2025-03-15T10:00 to 2025-03-15T12:00";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testMissingWithKeywordAfterFrom() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events description \"Weekly Sync\" from 2025-03-13T09:00 " +
            "Updated Description";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Expected 'with' after date/time format", result);
  }

  @Test
  public void testMissingWith() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events description \"Weekly Sync\" from 2025-03-13T09:00 ";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Expected 'with' after date/time format", result);
  }

  @Test
  public void testMissingNewValueAfterWith() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events description \"Weekly Sync\" from 2025-03-13T09:00 with";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Missing NewPropertyValue after 'with'", result);
  }

  @Test
  public void testMissingPropertyToEdits() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid edit command", result);
  }

  @Test
  public void testMissingEventsName() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events name";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Missing event name", result);
  }

  @Test
  public void testMissingFromKeyword() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "edit events subject Meeting from";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Missing start date/time after 'from'", result);
  }

  @Test
  public void testInvalidDate() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String command = "edit events subject Meeting from 09-71-1222";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format in edit event command", result);
  }

  @Test
  public void testValidEditCommandWithDateRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command1 = "create event \"Weekly Sync\" from 2025-03-13T09:00 to 2025-03-13T11:00";
    parser.parseAndExecute(command1);


    String command = "edit events description \"Weekly Sync\" from 2025-03-13T09:00 with " +
            "Updated Description";
    String result = parser.parseAndExecute(command);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testValidEditCommandWithoutDateRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Team Meeting\"  from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "edit events location \"Team Meeting\" \"Conference Room A\"";
    String result = parser.parseAndExecute(command1);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testEditDateRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Sync\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "edit events time \"Weekly Sync\" \"2025-03-11T10:00\"";
    String result = parser.parseAndExecute(command1);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testEditStartDateRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "edit event time \"Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00 " +
            "with \"2025-03-10T15:00\"";
    String result = parser.parseAndExecute(command1);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testEditRecurringEvent() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Sync\" from 2025-03-14T10:00 to" +
            " 2025-03-14T11:00 repeats F until 2025-04-15T10:00 public";
    parser.parseAndExecute(command);

    String command1 = "edit events description \"Weekly Sync\" from 2025-03-14T10:00 with" +
            " Updated Description";
    String result1 = parser.parseAndExecute(command1);
    assertEquals("Event edited successfully", result1);

    String command2 = "edit events subject \"Weekly Sync\" from 2025-03-14T10:00 with Sync";
    String result2 = parser.parseAndExecute(command2);
    assertEquals("Event edited successfully", result2);

    String command5 = "edit events location \"Sync\" from 2025-03-14T10:00 with Mumbai";
    String result5 = parser.parseAndExecute(command5);
    assertEquals("Event edited successfully", result5);

    String command6 = "edit events location \"Sync\" from 2025-03-14T10:00 with Mumbai";
    String result6 = parser.parseAndExecute(command6);
    assertEquals("Event edited successfully", result6);

    String command7 = "edit events error \"Sync\" from 2025-03-14T10:00 with Mumbai";
    String result7 = parser.parseAndExecute(command7);
    assertEquals("Failed to edit event (event not found or invalid property)", result7);

    String command4 = "edit events public \"Sync\" from 2025-03-14T10:00 with private";
    String result4 = parser.parseAndExecute(command4);
    assertEquals("Event edited successfully", result4);

    String command3 = "edit events time \"Sync\" from 2025-03-14T10:00 with 2025-03-14T05:00";
    String result3 = parser.parseAndExecute(command3);
    assertEquals("Event edited successfully", result3);

  }

  @Test
  public void testInvalidProperty() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Sync\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "edit events work \"Weekly Sync\" \"Updated Description\"";
    String result = parser.parseAndExecute(command1);

    assertEquals("Failed to edit event (event not found or invalid property)", result);
  }

  @Test
  public void testMissingFromKeywordButDateRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Sync\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "edit events description \"Weekly Sync\" \"Updated Description\"";
    String result = parser.parseAndExecute(command1);

    assertEquals("Event edited successfully", result);
  }

  @Test
  public void testInvalidStartDateFormatAfterFrom() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "edit events description \"Weekly Sync\" from invalid-date with" +
            " Updated Description";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Invalid date/time format in edit event command", result);
  }

  @Test
  public void testPrintEventByRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);

    String commandPrint = "print events from 2025-03-10T10:00 to 2025-03-10T11:00";
    String resultPrint = parser.parseAndExecute(commandPrint);
    assertEquals("Events from 2025-03-10T10:00 to 2025-03-10T11:00:\n" +
            "- Weekly Meeting (2025-03-10T10:00 - 2025-03-10T11:00) (private) ", resultPrint);
  }

  @Test
  public void testPrintEventsOnSpecificDay() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand1 = "create event Meeting on 2025-03-13";
    String createCommand2 = "create event Workshop on 2025-03-14";
    assertEquals("Event created successfully",
            parser.parseAndExecute(createCommand1));
    assertEquals("Event created successfully",
            parser.parseAndExecute(createCommand2));

    String printCommand = "print events on 2025-03-13";
    String result = parser.parseAndExecute(printCommand);

    assertTrue(result.startsWith("Events on 2025-03-13:\n"));
    assertTrue(result.contains("- Meeting"));

    String printCommand2 = "print events on 2025-03-14";
    String result2 = parser.parseAndExecute(printCommand2);
    assertTrue(result2.contains("- Workshop"));
  }

  @Test
  public void testPrintEventsWithinTimeRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand1 = "create event \"Morning Meeting\" from " +
            "2025-03-13T09:00 to 2025-03-13T10:00";
    String createCommand2 = "create event \"Afternoon Workshop\" " +
            "from 2025-03-13T14:00 to 2025-03-13T16:00";
    assertEquals("Event created successfully",
            parser.parseAndExecute(createCommand1));
    assertEquals("Event created successfully",
            parser.parseAndExecute(createCommand2));

    String printCommand = "print events from 2025-03-13T08:00" +
            " to 2025-03-13T17:00";
    String result = parser.parseAndExecute(printCommand);

    assertTrue(result.startsWith("Events from 2025-03-13T08:00" +
            " to 2025-03-13T17:00:\n"));
    assertTrue(result.contains("- Morning Meeting"));
    assertTrue(result.contains("- Afternoon Workshop"));
  }

  @Test
  public void testMissingDateInOnCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String printCommand = "print events on";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("Error: Missing date in print events command", result);
  }

  @Test
  public void testInvalidDateFormatInOnCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String printCommand = "print events on invalid-date";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("Error: Invalid date format in print events command." +
            " Expected format: YYYY-MM-DD", result);
  }

  @Test
  public void testMissingToKeywordInFromToCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String printCommand = "print events from 2025-03-13T09:00";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("Error: Invalid format for print events from/to command." +
            " Expected format: 'print events from <startDateTime> to <endDateTime>'", result);
  }

  @Test
  public void testInvalidDateFormatInFromToCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String printCommand = "print events from invalid-date to invalid-date";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("Error: Invalid date/time format in print events command." +
            " Expected format: YYYY-MM-DD'T'HH:mm", result);
  }

  @Test
  public void testNoEventsFoundOnSpecificDay() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String printCommand = "print events on 2025-03-14";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("No events on 2025-03-14", result);
  }

  @Test
  public void testNoEventsFoundWithinTimeRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String printCommand = "print events from 2025-03-14T08:00 to 2025-03-14T17:00";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("No events from 2025-03-14T08:00 to 2025-03-14T17:00", result);
  }

  @Test
  public void testInvalidTimeIndicator() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String printCommand = "print events at 2025-03-13";
    String result = parser.parseAndExecute(printCommand);

    assertEquals("Error: Expected 'on' or 'from' after 'print events'", result);
  }

  @Test
  public void testExportCalendar() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    String result = parser.parseAndExecute(command);
    assertEquals("Event created successfully", result);

    String commandExport = "export cal output.csv";
    String resultExport = parser.parseAndExecute(commandExport);

    assertTrue(resultExport.contains("Calendar exported successfully"));
  }

  @Test
  public void testValidExportCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "export cal mycalendar.csv";
    String result = parser.parseAndExecute(command);

    assertEquals("Calendar exported successfully to mycalendar.csv", result);
  }

  @Test
  public void testMissingFilenameInExportCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String command = "export cal";
    String result = parser.parseAndExecute(command);

    assertEquals("Error: Missing filename in export command", result);
  }

  @Test
  public void testValidBusyCheckBusy() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event Meeting from 2025-03-13T10:00 to 2025-03-13T11:00";
    assertEquals("Event created successfully", parser.parseAndExecute(createCommand));

    String busyCommand = "show status on 2025-03-13T10:30";
    String result = parser.parseAndExecute(busyCommand);

    assertEquals("Busy", result);
  }

  @Test
  public void testValidBusyCheckAvailable() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String busyCommand = "show status on 2025-03-13T12:00";
    String result = parser.parseAndExecute(busyCommand);

    assertEquals("Available", result);
  }

  @Test
  public void testMissingOnKeywordInBusyCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String busyCommand = "show status 2025-03-13T10:30";
    String result = parser.parseAndExecute(busyCommand);

    assertEquals("Error: Expected 'on' after 'show status'", result);
  }

  @Test
  public void testMissingDateTimeInBusyCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String busyCommand = "show status on";
    String result = parser.parseAndExecute(busyCommand);

    assertEquals("Error: Missing date/time after 'on'", result);
  }

  @Test
  public void testInvalidDateTimeFormatInBusyCommand() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String busyCommand = "show status on invalid-date";
    String result = parser.parseAndExecute(busyCommand);

    assertEquals("Error: Invalid date/time format in show status command", result);
  }

  @Test
  public void testBusyAtSpecificTime() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String command = "create event \"Weekly Meeting\" from 2025-03-10T10:00 to 2025-03-10T11:00";
    parser.parseAndExecute(command);

    String command1 = "show status on 2025-03-10T10:30";

    assertTrue(parser.parseAndExecute(command1).contains("Busy"));
  }

  @Test
  public void testMissingStartDateAfterFrom() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String editCommand = "edit events description \"Weekly Sync\" from";
    String result = parser.parseAndExecute(editCommand);

    assertEquals("Error: Missing start date/time after 'from'", result);
  }

  @Test
  public void testValidEditEventsFuture() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Sync\" from 2025-03-21T09:00" +
            " to 2025-03-21T10:00 repeats F until 2025-06-01T23:59";
    assertEquals("Recurring event created successfully",
            parser.parseAndExecute(createCommand));

    String editCommand = "edit events description \"Weekly Sync\" from" +
            " 2025-03-28T09:00 with Updated Description";
    assertEquals("Event edited successfully", parser.parseAndExecute(editCommand));
  }

  @Test
  public void testValidEditEventsPast() {
    CommandParser parser = new CommandParser(new CalendarManager());

    assertEquals("Calendar created successfully",
            parser.parseAndExecute("create calendar --name Work --timezone America/New_York"));

    assertEquals("Switched to calendar: Work",
            parser.parseAndExecute("use calendar --name Work"));

    String createCommand = "create event \"Weekly Sync\" from 2025-03-21T09:00 to" +
            " 2025-03-21T10:00 repeats F until 2025-06-01T23:59";
    assertEquals("Recurring event created successfully", parser.parseAndExecute(createCommand));

    String editCommand = "edit events description \"Weekly Syc\" from 2024-01" +
            "-28T09:00 with \"Updated Description\"";
    assertEquals("Failed to edit event (event not found or invalid property)",
            parser.parseAndExecute(editCommand));
  }

  @Test
  public void testInvalidEditCommand() {

    CommandParser parser = new CommandParser(new CalendarManager());

    String editCommand = "edit event description";
    String result = parser.parseAndExecute(editCommand);

    assertEquals("Error: Invalid edit event command.", result);
  }

  @Test
  public void testInvalidEditCommand2() {

    CommandParser parser = new CommandParser(new CalendarManager());
    String tokens = "edit events location \"Meeting Daily\"";

    String result = parser.parseAndExecute(tokens);

    assertEquals("Error: Missing NewValue or From", result);
  }

  @Test
  public void testEditEndDateBeforeStartDate() {
    CommandParser parser = new CommandParser(new CalendarManager());

    String createCommand = "create event \"Weekly Sync\" from 2025-03-15T09:00 to " +
            "2025-03-15T08:00 repeats U until 2025-04-16T10:00 description Weekly Meeting";
    String resultCommand = parser.parseAndExecute(createCommand);
    assertEquals("Error: Start date and time cannot be after end date", resultCommand);
  }

  @Test
  public void testCreateCalendar() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String command = "create calendar --name Work --timezone America/New_York";
    String result = parser.parseAndExecute(command);
    assertEquals("Calendar created successfully", result);

    command = "create calendar --name Work --timezone Europe/London";
    result = parser.parseAndExecute(command);
    assertEquals("Failed to create calendar (duplicate name or invalid timezone)", result);

    command = "create calendar --name Personal --timezone Invalid/Timezone";
    result = parser.parseAndExecute(command);
    assertEquals("Failed to create calendar (duplicate name or invalid timezone)", result);

    command = "create calendar --name Personal --timezone America/New_York extraToken";
    result = parser.parseAndExecute(command);
    assertEquals("Error: Invalid create calendar command", result);
  }

  @Test
  public void testUseCalendar() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String createCommand = "create calendar --name Work --timezone America/New_York";
    parser.parseAndExecute(createCommand);

    String useCommand = "use calendar --name Work";
    String useResult = parser.parseAndExecute(useCommand);
    assertEquals("Switched to calendar: Work", useResult);

    useCommand = "use calendar --name Personal";
    useResult = parser.parseAndExecute(useCommand);
    assertEquals("Failed to switch to calendar (not found)", useResult);
  }

  @Test
  public void testCopyEvent() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");

    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-20T09:00 to" +
            " 2025-03-20T10:00");

    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    assertEquals("Error: Invalid date/time format. Expected format: YYYY-MM-DD'T'HH:mm",
            parser.parseAndExecute("copy event \"Morning Meeting\" on 2025-03-20" +
                    " --target Personal to 2025-03-21T13:00"));

    assertEquals("Error: Invalid copy event calendar command",
            parser.parseAndExecute("copy event \"Morning Meeting\" on 2025-03-20T09:00" +
                    " --target Personal to 2025-03-21T13:00 inalid"));

    String copyCommand = "copy event \"Morning Meeting\" on 2025-03-20T09:00 --target " +
            "Personal to 2025-03-21T13:00";
    String copyResult = parser.parseAndExecute(copyCommand);

    assertEquals("Event copied successfully", copyResult);
  }

  @Test
  public void testEditEvent() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");

    String createEventCommand = "create event \"Morning Meeting\" from 2025-03-20T09:00 " +
            "to 2025-03-20T10:00";
    String createResult = parser.parseAndExecute(createEventCommand);

    assertEquals("Event created successfully", createResult);

    String editCommand = "edit calendar --name Work --property name Business";
    String editResult = parser.parseAndExecute(editCommand);
    assertEquals("Calendar edited successfully", editResult);

    String editCommand2 = "edit calendar --name Business --property timezone Europe/London";
    String editResult2 = parser.parseAndExecute(editCommand2);
    assertEquals("Calendar edited successfully", editResult2);

    String printCommand = "print events from 2025-03-20T00:00 to 2025-03-20T23:59";
    String printResult = parser.parseAndExecute(printCommand);
    assertEquals("Events from 2025-03-20T00:00 to 2025-03-20T23:59:\n" +
            "- Morning Meeting (2025-03-20T13:00 - 2025-03-20T14:00) (private) ", printResult);
  }

  @Test
  public void testCopyEventsOn() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-20T09:00 " +
            "to 2025-03-20T10:00");
    parser.parseAndExecute("create event \"Afternoon Meeting\" from 2025-03-20T14:00 " +
            "to 2025-03-20T15:00");

    String incompleteCommand = "copy events with";
    String incompleteResult = parser.parseAndExecute(incompleteCommand);
    assertEquals("Error: Invalid copy events command. Expected 'on' or 'between'.",
            incompleteResult);

    String copyCommandTime = "copy events on 2025-03-19T12:00 --target Personal to 2025-03-25";
    String resultTime = parser.parseAndExecute(copyCommandTime);
    assertEquals("Error: Invalid date format. Expected format: YYYY-MM-DD", resultTime);

    String invalidCommand = "copy events on 2025-03-19 --target Personal to 2025-03-25 Invalid";
    String invalidResult = parser.parseAndExecute(invalidCommand);
    assertEquals("Error: Invalid format for copy events on command", invalidResult);

    String copyCommand = "copy events on 2025-03-20 --target Personal to 2025-03-21";
    String result = parser.parseAndExecute(copyCommand);
    assertEquals("Events copied successfully", result);

    parser.parseAndExecute("use calendar --name Personal");

    String printCommand = "print events from 2025-03-21T00:00 to 2025-03-21T23:59";
    String printResult = parser.parseAndExecute(printCommand);

    assertEquals("Events from 2025-03-21T00:00 to 2025-03-21T23:59:\n" +
            "- Morning Meeting (2025-03-21T13:00 - 2025-03-21T14:00) (private) " +
            "- Afternoon Meeting (2025-03-21T18:00 - 2025-03-21T19:00) (private) ", printResult);
  }

  @Test
  public void testCopyEventsBetween() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-19T09:00" +
            " to 2025-03-19T10:00");
    parser.parseAndExecute("create event \"Afternoon Meeting\" from 2025-03-20T14:00" +
            " to 2025-03-20T15:00");

    String copyCommandTime = "copy events between 2025-03-19T12:00 and invalid --target Personal " +
            "to 2025-03-25";
    String resultTime = parser.parseAndExecute(copyCommandTime);
    assertEquals("Error: Invalid date format. Expected format: YYYY-MM-DD", resultTime);

    String invalidCommand = "copy events between 2025-03-19 and 2025-03-20 --target Personal to " +
            "2025-03-25 Invalid";
    String invalidResult = parser.parseAndExecute(invalidCommand);
    assertEquals("Error: Invalid format for copy events between command.", invalidResult);

    String copyCommand = "copy events between 2025-03-19 and 2025-03-20 --target Personal to" +
            " 2025-03-25";
    String result = parser.parseAndExecute(copyCommand);
    assertEquals("Events copied successfully", result);

    parser.parseAndExecute("use calendar --name Personal");

    String printCommandDay1 = "print events from 2025-03-25T00:00 to 2025-03-25T23:59";
    String printResultDay1 = parser.parseAndExecute(printCommandDay1);
    assertEquals("Events from 2025-03-25T00:00 to 2025-03-25T23:59:\n" +
            "- Morning Meeting (2025-03-25T13:00 - 2025-03-25T14:00) (private) " +
            "- Afternoon Meeting (2025-03-25T18:00 - " +
            "2025-03-25T19:00) (private) ", printResultDay1);
  }

  @Test
  public void testUseAndCopy() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    assertEquals("Error: Invalid use command",
            parser.parseAndExecute("use"));

    assertEquals("Error: Invalid copy command",
            parser.parseAndExecute("copy"));
  }

  @Test
  public void testInvalidCommand() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    assertEquals("Error: Invalid edit calendar command",
            parser.parseAndExecute("edit calendar --name \"Meeting\" " +
                    "--property timezone Europe/London New_York\""));

    assertEquals("Error: Invalid use calendar command",
            parser.parseAndExecute("use calendar --name Work --timezone Europe/London"));

  }

  @Test
  public void testExportToCSV() throws IOException {
    CalendarManager calendarManager = new CalendarManager();
    CommandParser parser = new CommandParser(calendarManager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");

    assertEquals("Event created successfully", parser.parseAndExecute("create event" +
            " \"Regular Meeting\" from 2025-03-15T14:00 to 2025-03-15T16:00 description " +
            "\"Regular Description\" location \"Office\" public"));
    assertEquals("Event created successfully", parser.parseAndExecute("create event" +
            " \"Private Event\" from 2025-03-17T10:00 to 2025-03-17T11:00 " +
            "description \"Private Description\"" +
            " location \"Secret Location\""));
    assertEquals("Recurring event created successfully", parser.parseAndExecute("create event" +
            " \"Weekly Meeting\" from 2025-03-20T10:00 to 2025-03-20T11:00 repeats M" +
            " for 2 times description" +
            " \"Recurring Description\" location \"Conference Room\" public"));

    File tempFile = File.createTempFile("calendar_export_", ".csv");
    String filePath = tempFile.getAbsolutePath();

    String exportResult = parser.parseAndExecute("export cal " + filePath);
    assertEquals("Calendar exported successfully to " + filePath, exportResult);

    List<String> fileLines = Files.readAllLines(Paths.get(filePath));

    assertTrue(fileLines.get(0).startsWith("Subject,Start Date,Start Time"));

    assertEquals(5, fileLines.size());

    boolean foundRegular = false;
    boolean foundPrivate = false;
    boolean foundRecurring = false;

    for (String line : fileLines) {
      if (line.contains("Regular Meeting")) {
        foundRegular = true;
      }
      if (line.contains("Private Event") && line.contains("TRUE")) {
        foundPrivate = true;
      }
      if (line.contains("Weekly Meeting")) {
        foundRecurring = true;
      }
    }

    assertTrue(foundRegular);
    assertTrue(foundPrivate);
    assertTrue(foundRecurring);
    tempFile.delete();
  }

  @Test
  public void testExportToCSVIOException() {
    CalendarManager calendarManager = new CalendarManager();
    CommandParser parser = new CommandParser(calendarManager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");

    parser.parseAndExecute("create event Morning Meeting from 2025-03-20T09:00 to " +
            "2025-03-20T10:00 description Regular Description location Office public");

    String invalidFilePath = "/invalid_path/calendar_export.csv";
    String result = parser.parseAndExecute("export cal " + invalidFilePath);

    assertEquals("Error: Failed to export calendar due to I/O error.", result);
  }

  @Test
  public void testCreateEventWithoutCalendarSelected() {

    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "create event Meeting from 2025-04-01T10:00 to 2025-04-01T11:00 description " +
            "WeeklySync location Zoom public";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testCreateRecurringEventWithoutCalendarSelected() {

    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);

    newManager.createCalendar("TempCal", "America/New_York");
    String command = "create event Standup from 2025-04-01T09:00 to " +
            "2025-04-01T09:30 repeats MTWRF" +
            " for 5 times description DailyStandup public";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testEditEventWithoutCalendarSelected() {
    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "edit event description Meeting from 2025-04-01T10:00 to 2025-04-01T11:00 " +
            "with Updated";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testEditEventsWithoutCalendarSelected() {

    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "edit events location Meeting from 2025-04-01T10:00 with NewRoom";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testPrintEventsWithoutCalendarSelected() {
    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "print events on 2025-04-01";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testExportCalendarWithoutCalendarSelected() {
    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "export cal export.csv";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testShowStatusWithoutCalendarSelected() {
    CalendarManager newManager = new CalendarManager();
    CommandParser parser = new CommandParser(newManager);
    newManager.createCalendar("TempCal", "America/New_York");
    String command = "show status on 2025-04-01T10:00";
    String result = parser.parseAndExecute(command);
    assertTrue(result.contains("Error: No active calendar selected."));
  }

  @Test
  public void testCopyConflict() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Business --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-20T09:00 to " +
            "2025-03-20T10:00");

    parser.parseAndExecute("use calendar --name Business");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-20T09:00 to" +
            " 2025-03-20T10:00");
    String copyCommand = "copy event \"Morning Meeting\" on 2025-03-20T09:00 --target Work to" +
            " 2025-03-20T09:00";
    String copyResult = parser.parseAndExecute(copyCommand);
    assertEquals("Failed to copy event due to conflict or Non Existing event", copyResult);
  }

  @Test
  public void testInvalidCommandSyntax() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("create calendar --name Work timezone" +
            " America/New_York");
    assertEquals("Error: Invalid create calendar command", result);
  }

  @Test
  public void testTimezoneEdgeCases() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "Pacific/Kiritimati");
    manager.useCalendar("Work");

    manager.createEvent("Event in Kiritimati", LocalDateTime.of(2025, 3, 26, 10, 0),
            LocalDateTime.of(2025, 3, 26, 11, 0),
            "Description", "Location", true, false);

    manager.editCalendar("Work", "timezone", "Pacific/Niue");

    List<String> events = manager.queryEventsByDate(LocalDateTime.of(
            2025, 3, 25, 9, 0));
    assertTrue(events.get(0).contains("Event in Kiritimati"));
  }

  @Test
  public void testRecurringEventNoDaysSpecified() {
    CommandParser parser = new CommandParser(new CalendarManager());
    String result = parser.parseAndExecute("create event \"Weekly Meeting\"" +
            " from 2025-03-10T10:00 to 2025-03-10T11:00 repeats for 5 times");
    assertEquals("Error: Unexpected token 'times' in create event command", result);
  }

  @Test
  public void testExportEmptyCalendar() throws IOException {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("EmptyCal", "America/New_York");
    manager.useCalendar("EmptyCal");

    File tempFile = File.createTempFile("empty_calendar_", ".csv");
    String filePath = tempFile.getAbsolutePath();

    String exportResult = new CommandParser(manager).parseAndExecute("export cal " + filePath);
    assertEquals("Calendar exported successfully to " + filePath, exportResult);

    List<String> fileLines = Files.readAllLines(Paths.get(filePath));
    assertEquals(1, fileLines.size());
    assertTrue(fileLines.get(0).startsWith("Subject,Start Date,Start Time"));
  }

  @Test
  public void testCopyEventsAcrossTimezones() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    manager.createEvent("Meeting", LocalDateTime.of(2025, 3, 26, 10, 0),
            LocalDateTime.of(2025, 3, 26, 11, 0),
            "Description", "Location", true, false);

    manager.createCalendar("Personal", "Asia/Tokyo");

    boolean success = manager.copySingleEvent("Meeting",
            LocalDateTime.of(2025, 3, 26, 10, 0), "Personal",
            LocalDateTime.of(2025, 3, 27, 9, 0));

    assertTrue(success);
  }

  @Test
  public void testOverlappingRecurringEvents() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    boolean result1 = manager.createRecurringEvent("Morning Sync",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "Description", "Location", true,
            true,
            List.of("M", "W"), 4,
            null);
    assertTrue(result1);

    assertThrows(IllegalStateException.class, () -> {
      manager.createRecurringEvent("Team Meeting",
              LocalDateTime.of(2025, 3, 10, 9, 30),
              LocalDateTime.of(2025, 3, 10, 10, 30),
              "Description", "Location", true,
              true,
              List.of("M"), 4,
              null);
    });
  }

  @Test
  public void testQueryEventsOutsideRange() {
    CommandParser parser = new CommandParser(new CalendarManager());

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");
    String result = parser.parseAndExecute("print events from " +
            "1900-01-01T00:00 to 1900-12-31T23:59");

    assertEquals("No events from 1900-01-01T00:00 to 1900-12-31T23:59", result);
  }

  @Test
  public void testLeapYearDates() {
    CommandParser parser = new CommandParser(new CalendarManager());

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");
    String resultCreate = parser.parseAndExecute(
            "create event LeapDayEvent on 2024-02-29"
    );
    assertEquals("Event created successfully", resultCreate);
    String resultQuery = parser.parseAndExecute(
            "print events on 2024-02-29"
    );
    assertTrue(resultQuery.contains("- LeapDayEvent"));
  }

  @Test
  public void testUntilDate() {
    CommandParser parser = new CommandParser(new CalendarManager());

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");
    String resultCreate = parser.parseAndExecute(
            "create event \"Event Work 3\" from 2025-03-25T01:00 to 2025-03-25T02:00" +
                    " repeats T until 2024-04-08T23:59"
    );
    assertEquals("Until date cannot be after end date", resultCreate);
  }

  @Test
  public void testCopyOnTheSameZone() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone America/New_York");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-19T09:00 to " +
            "2025-03-19T10:00");
    parser.parseAndExecute("create event \"Afternoon Meeting\" from 2025-03-20T14:00 to " +
            "2025-03-20T15:00");

    String copyCommand = "copy events between 2025-03-19 " +
            "and 2025-03-20 --target Personal to 2025-03-25";
    String result = parser.parseAndExecute(copyCommand);
    assertEquals("Events copied successfully", result);

    parser.parseAndExecute("use calendar --name Personal");

    String printCommandDay1 = "print events from 2025-03-25T00:00 to 2025-03-25T23:59";
    String printResultDay1 = parser.parseAndExecute(printCommandDay1);
    assertEquals("Events from 2025-03-25T00:00 to 2025-03-25T23:59:\n" +
            "- Morning Meeting (2025-03-25T09:00 - 2025-03-25T10:00) (private) " +
            "- Afternoon Meeting (2025-03-25T14:00 " +
            "- 2025-03-25T15:00) (private) ", printResultDay1);
  }

  @Test
  public void testCopyOnInvalidCalendar() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-19T09:00 to " +
            "2025-03-19T10:00");
    parser.parseAndExecute("create event \"Afternoon Meeting\" from 2025-03-20T14:00 " +
            "to 2025-03-20T15:00");

    String copyCommand = "copy events between 2025-03-19 and 2025-03-20 " +
            "--target Personal to 2025-03-25";
    String result = parser.parseAndExecute(copyCommand);
    assertEquals("Error: No active calendar or target calender found", result);
  }

  @Test
  public void testEditCalendar() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-19T09:00 to " +
            "2025-03-19T10:00");
    parser.parseAndExecute("create event \"Afternoon Meeting\" from 2025-03-20T14:00" +
            " to 2025-03-20T15:00");

    parser.parseAndExecute("edit calendar --name Work --property timezone Europe/London");
    String printCommandDay1 = "print events from 2025-03-19T00:00 to 2025-03-20T23:59";
    String printResultDay1 = parser.parseAndExecute(printCommandDay1);
    assertEquals("Events from 2025-03-19T00:00 to 2025-03-20T23:59:\n" +
            "- Morning Meeting (2025-03-19T13:00 - 2025-03-19T14:00) (private) -" +
            " Afternoon Meeting (2025-03-20T18:00 - 2025-03-20T19:00) (private) ", printResultDay1);

  }

  @Test
  public void testCopySingleEventToNonexistentCalendar() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Work");

    parser.parseAndExecute("create event \"Meeting\" from 2025-03-20T09:00 to " +
            "2025-03-20T10:00");

    String result = parser.parseAndExecute("copy event \"Meeting\" on 2025-03-20T09:00" +
            " --target NonexistentCalendar to 2025-03-21T10:00");
    assertEquals("Error: No active calendar or target calender found", result);
  }

  @Test
  public void testCopyEventsOnDayWithNoEvents() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");

    String result = parser.parseAndExecute("copy events on 2025-03-20 --target Personal to" +
            " 2025-03-21");
    assertEquals("Failed to copy events due to conflict or No existing event", result);
  }

  @Test
  public void testCopyEventsBetweenDatesWithNoOverlap() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Event Outside Range\" from 2025-03-15T10:00 to" +
            " 2025-03-15T11:00");

    String result = parser.parseAndExecute("copy events between 2025-03-20 and 2025-03-21" +
            " --target Personal to 2025-03-25");
    assertEquals("Failed to copy events due to conflict or No Existing event", result);
  }

  @Test
  public void testCopyAllEventsOnDayAcrossTimezones() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Asia/Tokyo");

    parser.parseAndExecute("use calendar --name Work");
    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-20T09:00 to " +
            "2025-03-20T10:00");

    String result = parser.parseAndExecute("copy events on 2025-03-20 --target Personal " +
            "to 2025-03-21");

    assertEquals("Events copied successfully", result);

    parser.parseAndExecute("use calendar --name Personal");
    String printResult = parser.parseAndExecute("print events on 2025-03-21");

    assertTrue(printResult.contains("- Morning Meeting (2025-03-21T22:00 - 2025-03-21T23:00)"));
  }

  @Test
  public void testCopyEventsNonExistentEvent() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Personal");
    String result = parser.parseAndExecute("copy event Lecture on 2025-04-01T10:00 " +
            "--target Personal to 2025-05-01T09:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEventsBetweenDatesAcrossTimezones() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Work --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Personal --timezone Europe/London");

    parser.parseAndExecute("use calendar --name Work");

    parser.parseAndExecute("create event \"Morning Meeting\" from 2025-03-19T09:00 to" +
            " 2025-03-19T10:00");

    parser.parseAndExecute("use calendar --name Work");
    String result = parser.parseAndExecute("copy event \"Morning Meeting\" on 2025-03-19T09:00 " +
            "--target Personal to 2025-05-01T09:00");

    assertEquals("Event copied successfully", result);
  }

  @Test
  public void testCopyEvent_conflictInTargetCalendar_viaParser() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Source");

    parser.parseAndExecute("create event Lecture from 2025-04-01T10:00 to 2025-04-01T11:00 " +
            "description Planning location Room101 public");
    parser.parseAndExecute("use calendar --name Target");
    parser.parseAndExecute("create event Blocker from 2025-05-01T09:00 to 2025-05-01T10:00" +
            " description Conflict location RoomB public");
    parser.parseAndExecute("use calendar --name Source");
    String result = parser.parseAndExecute("copy event Lecture on 2025-04-01T10:00 " +
            "--target Target to 2025-05-01T09:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEvent_successToA_butFailsToB_viaParser() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone America/New_York");
    parser.parseAndExecute("create calendar --name CalendarA --timezone America/New_York");
    parser.parseAndExecute("create calendar --name CalendarB --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Source");
    parser.parseAndExecute("create event Sync from 2025-06-10T14:00 to 2025-06-10T15:00 " +
            "description WeeklySync location Virtual public");
    parser.parseAndExecute("use calendar --name CalendarB");
    parser.parseAndExecute("create event Blocked from 2025-07-01T09:00 to 2025-07-01T09:30" +
            " description Busy location RoomZ public");

    parser.parseAndExecute("use calendar --name Source");
    String resultA = parser.parseAndExecute("copy event Sync on 2025-06-10T14:00" +
            " --target CalendarA to 2025-07-01T09:00");
    String resultB = parser.parseAndExecute("copy event Sync on 2025-06-10T14:00" +
            " --target CalendarB to 2025-07-01T09:00");
    assertEquals("Event copied successfully", resultA);
    assertEquals("Failed to copy event due to conflict or Non Existing event", resultB);
  }

  @Test
  public void testCopyAcrossThreeCalendars_conflictChain_viaParser() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);
    parser.parseAndExecute("create calendar --name A --timezone America/New_York");
    parser.parseAndExecute("create calendar --name B --timezone America/New_York");
    parser.parseAndExecute("create calendar --name C --timezone America/New_York");
    parser.parseAndExecute("use calendar --name A");
    parser.parseAndExecute("create event AEvent from 2025-10-01T10:00 to 2025-10-01T11:00" +
            " description FirstLink location Zoom public");
    parser.parseAndExecute("use calendar --name B");
    parser.parseAndExecute("create event BEvent from 2025-10-02T10:00 to 2025-10-02T11:00" +
            " description MiddleLink location Zoom public");
    parser.parseAndExecute("use calendar --name C");

    parser.parseAndExecute("create event CEvent from 2025-10-03T10:00 to 2025-10-03T11:00 " +
            "description EndLink location Zoom public");
    parser.parseAndExecute("use calendar --name A");
    parser.parseAndExecute("copy event AEvent on 2025-10-01T10:00 --target B to 2025-10-02T10:00");
    parser.parseAndExecute("use calendar --name B");
    String result = parser.parseAndExecute("copy event AEvent on 2025-10-02T10:00 " +
            "--target C to 2025-10-03T10:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEvent_sameNameDifferentCalendars_noFalseConflict_viaParser() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Alpha --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Beta --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Alpha");
    parser.parseAndExecute("create event Planning from 2025-09-15T13:00 to 2025-09-15T14:00" +
            " description AlphaPlan location RoomA public");
    parser.parseAndExecute("use calendar --name Beta");
    parser.parseAndExecute("create event Planning from 2025-09-15T15:00 to 2025-09-15T16:00" +
            " description BetaPlan location RoomB public");
    parser.parseAndExecute("use calendar --name Alpha");
    String result = parser.parseAndExecute("copy event Planning on 2025-09-15T13:00 " +
            "--target Beta to 2025-09-15T13:00");
    assertEquals("Event copied successfully", result);
  }

  @Test
  public void testRecurringCopyFailsDueToTargetSingleEventConflict_viaParser() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Main --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");

    parser.parseAndExecute("use calendar --name Main");
    parser.parseAndExecute("create event Standup from 2025-04-01T09:00 to 2025-04-01T10:00 " +
            "repeats MTWRF for 5 times description DailyStandup location Zoom public");
    parser.parseAndExecute("use calendar --name Target");
    parser.parseAndExecute("create event Conflict from 2025-04-02T09:15 to 2025-04-02T10:00 " +
            "description Overlap location RoomX public");
    parser.parseAndExecute("use calendar --name Main");
    String result = parser.parseAndExecute("copy event Standup on 2025-04-01T09:00 --target" +
            " Target to 2025-04-02T09:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testRecurringCopyFailsIfAnySingleInstanceConflicts() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Source");
    parser.parseAndExecute("create event Series from 2025-05-05T08:00 to 2025-05-05T09:00 " +
            "repeats MTWR for 4 times description RecurringEvent location A1 public");
    parser.parseAndExecute("use calendar --name Target");
    parser.parseAndExecute("create event Clash from 2025-05-07T08:30 to 2025-05-07T09:30 " +
            "description SingleConflict location Z9 public");
    parser.parseAndExecute("use calendar --name Source");
    String result = parser.parseAndExecute("copy event Series on 2025-05-05T08:00 --target" +
            " Target to 2025-05-07T08:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyFailsDueToConflictAfterTimezoneConversion() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone Asia/Tokyo");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Source");

    parser.parseAndExecute("create event Workshop from 2025-06-10T23:00 to 2025-06-11T00:00 " +
            "description NightSession location Tokyo public");
    parser.parseAndExecute("use calendar --name Target");
    parser.parseAndExecute("create event MorningMeeting from 2025-06-10T10:00 to " +
            "2025-06-10T11:00 description ConflictHere location NYC public");
    parser.parseAndExecute("use calendar --name Source");

    String result = parser.parseAndExecute("copy event Workshop on 2025-06-10T23:00" +
            " --target Target to 2025-06-10T10:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEventsBetweenFailsDueToOneConflict() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Original --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Original");

    parser.parseAndExecute("create event Day1 from 2025-03-01T09:00 to 2025-03-01T10:00 " +
            "description One location Here public");
    parser.parseAndExecute("create event Day2 from 2025-03-02T09:00 to 2025-03-02T10:00 " +
            "description Two location Here public");
    parser.parseAndExecute("use calendar --name Target");

    parser.parseAndExecute("create event Clash from 2025-04-02T09:00 to 2025-04-02T10:00 " +
            "description Conflict location Room1 public");
    parser.parseAndExecute("use calendar --name Original");
    String result = parser.parseAndExecute("copy events between 2025-03-01 and 2025-03-02 " +
            "--target Target to 2025-04-02");

    assertEquals("Failed to copy events due to conflict or No Existing event", result);
  }

  @Test
  public void testCopyEventFitsBetweenWithoutConflict() {

    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Src --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Dst --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Src");

    parser.parseAndExecute("create event GapFiller from 2025-07-01T12:00 to 2025-07-01T12:30 " +
            "description TightSpot location Lobby public");
    parser.parseAndExecute("use calendar --name Dst");

    parser.parseAndExecute("create event Early from 2025-07-01T11:00 to 2025-07-01T12:00" +
            " description Before location A1 public");
    parser.parseAndExecute("create event Late from 2025-07-01T12:30 to 2025-07-01T13:30 " +
            "description After location A2 public");
    parser.parseAndExecute("use calendar --name Src");
    String result = parser.parseAndExecute("copy event GapFiller on 2025-07-01T12:00 " +
            "--target Dst to 2025-08-01T12:00");
    assertEquals("Event copied successfully", result);
  }

  @Test
  public void testEditCalendar_invalidTimezoneFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Global --timezone America/New_York");
    String result = parser.parseAndExecute(
            "edit calendar --name Global --property timezone Mars/Colony");

    assertEquals("Failed to edit calendar (invalid name, property, or value)", result);
  }

  @Test
  public void testEditCalendar_renameToExistingFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name A --timezone America/New_York");
    parser.parseAndExecute("create calendar --name B --timezone Europe/London");

    String result = parser.parseAndExecute("edit calendar --name A --property name B");

    assertEquals("Failed to edit calendar (invalid name, property, or value)", result);
  }

  @Test
  public void testCopyEventWithinSameCalendar_successWhenNoConflict() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name MyCal --timezone America/New_York");
    parser.parseAndExecute("use calendar --name MyCal");
    parser.parseAndExecute(
            "create event Solo from 2025-08-01T10:00 to 2025-08-01T11:00 description" +
                    " Original location Zoom public");

    String result = parser.parseAndExecute(
            "copy event Solo on 2025-08-01T10:00 --target MyCal to 2025-08-01T12:00");

    assertEquals("Event copied successfully", result);
  }

  @Test
  public void testCopyEventWithinSameCalendar_conflictFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name MyCal --timezone America/New_York");
    parser.parseAndExecute("use calendar --name MyCal");
    parser.parseAndExecute(
            "create event Solo from 2025-08-01T10:00 to 2025-08-01T11:00 description" +
                    " Original location Zoom public");

    String result = parser.parseAndExecute(
            "copy event Solo on 2025-08-01T10:00 --target MyCal to 2025-08-01T10:30");

    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEvent_targetCalendarNotFound() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone America/New_York");
    parser.parseAndExecute("use calendar --name Source");
    parser.parseAndExecute(
            "create event Alpha from 2025-10-01T10:00 to 2025-10-01T11:00 description" +
                    " TestEvent location Room public");

    String result = parser.parseAndExecute(
            "copy event Alpha on 2025-10-01T10:00 --target MissingCal to 2025-10-02T10:00");

    assertEquals("Error: No active calendar or target calender found", result);
  }

  @Test
  public void testCreateCalendar_timezoneMissingRegionFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String result = parser.parseAndExecute("create calendar --name NoRegion --timezone New_York");
    assertEquals("Failed to create calendar (duplicate name or invalid timezone)", result);
  }

  @Test
  public void testCreateCalendar_timezoneCaseSensitiveFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String result = parser.parseAndExecute(
            "create calendar --name LowercaseZone --timezone america/new_york");
    assertEquals("Failed to create calendar (duplicate name or invalid timezone)", result);
  }

  @Test
  public void testCopyEvent_eventWithInvalidOriginalTimeFailsGracefully() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name Source --timezone America/New_York");
    parser.parseAndExecute("create calendar --name Target --timezone America/New_York");

    parser.parseAndExecute("use calendar --name Source");

    // No event actually exists at this time
    String result = parser.parseAndExecute(
            "copy event GhostEvent on 2025-01-01T10:00 --target Target to 2025-01-01T11:00");
    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testCopyEvent_sameTimeAsOriginalFailsDueToExactConflict() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name SoloCal --timezone America/New_York");
    parser.parseAndExecute("use calendar --name SoloCal");
    parser.parseAndExecute(
            "create event Duplicate from 2025-08-01T10:00 to 2025-08-01T11:00" +
                    " description Self location Zoom public");

    String result = parser.parseAndExecute(
            "copy event Duplicate on 2025-08-01T10:00 --target SoloCal to 2025-08-01T10:00");

    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testUseCalendar_notFoundFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String result = parser.parseAndExecute("use calendar --name NotReal");
    assertEquals("Failed to switch to calendar (not found)", result);
  }

  @Test
  public void testEditCalendar_unknownPropertyFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name ToEdit --timezone America/New_York");

    String result = parser.parseAndExecute("edit calendar --name ToEdit --property color blue");
    assertEquals("Failed to edit calendar (invalid name, property, or value)", result);
  }

  @Test
  public void testCopyEventRecurring_conflictFails() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    parser.parseAndExecute("create calendar --name MyCal1 --timezone America/New_York");
    parser.parseAndExecute("create calendar --name MyCal2 --timezone America/New_York");
    parser.parseAndExecute("use calendar --name MyCal1");
    parser.parseAndExecute(
            "create event \"Event Work 2\" from " +
                    "2025-03-25T11:00 to 2025-03-25T12:00 repeats T for 3 times");

    parser.parseAndExecute("use calendar --name MyCal2");
    parser.parseAndExecute(
            "create event \"Event Work 2\" from " +
                    "2025-03-25T11:00 to 2025-03-25T12:00 repeats T for 3 times");

    parser.parseAndExecute("use calendar --name MyCal1");
    String result = parser.parseAndExecute(
            "copy event \"Event Work 2\" on 2025-04-01T11:00 " +
                    "--target MyCal2 to 2025-04-01T11:00");

    assertEquals("Failed to copy event due to conflict or Non Existing event", result);
  }

  @Test
  public void testEditRecurringEventDate_NoUpdatesWhenFromTimeAfterOccurrences() {
    CalendarManager manager = new CalendarManager();
    CommandParser parser = new CommandParser(manager);

    String createCalendarResult = parser.parseAndExecute("create calendar --name Work" +
            " --timezone UTC");
    assertEquals("Calendar created successfully", createCalendarResult);
    String useCalendarResult = parser.parseAndExecute("use calendar --name Work");
    assertEquals("Switched to calendar: Work", useCalendarResult);

    String createCommand = "create event \"Weekly Sync\" from 2025-03-21T09:00 to " +
            "2025-03-21T10:00 " +
            "repeats F until 2025-04-01T23:59";
    String createResult = parser.parseAndExecute(createCommand);
    assertEquals("Recurring event created successfully", createResult);

    String editCommand = "edit events description \"Weekly Sync\" from 2025-08-15T09:00 with" +
            " \"New Description\"";
    String editResult = parser.parseAndExecute(editCommand);
    assertEquals("Failed to edit event (event not found or invalid property)", editResult);
  }
}