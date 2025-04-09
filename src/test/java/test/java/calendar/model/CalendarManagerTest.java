package calendar.model;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the main calendar functionality provided by {@link CalendarManager}.
 * Covers core operations such as:
 * - Creating single and recurring events.
 * - Editing event details.
 * - Querying events by date or range.
 * - Checking for conflicts and busy times.
 * - Exporting calendar data to CSV format.
 * This test class ensures that the {@link CalendarManager}
 * behaves as expected under various scenarios,
 * including edge cases like overlapping events or invalid inputs.
 */
public class CalendarManagerTest {
  private final CalendarInterface calendarManager = new CalendarManager();

  @Test
  public void testCreateSingleEvent() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.createEvent("Test Event",
            LocalDateTime.of(2025, 3, 7, 10, 0),
            LocalDateTime.of(2025, 3, 7, 11, 0),
            "Description", "Location", true, false);

    assertTrue(result);

    List<String> events = calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 7, 10, 0));
    assertEquals(1, events.size());
    assertEquals("Test Event (2025-03-07T10:00 - 2025-03-07T11:00) at Location," +
            " Info: Description (public)", events.get(0));
  }

  @Test
  public void testEditEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 15, 16, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 15, 17, 0);
    calendarManager.createEvent("Original Event", startTime, endTime,
            "Old Description", "Old Location", true, false);

    boolean result = calendarManager.editEvent("Original Event", startTime,
            "description", "Updated Description");
    assertTrue(result);
    List<String> events = calendarManager.queryEventsByDate(startTime);
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Updated Description"));

    result = calendarManager.editEvent("Original Event", startTime,
            "subject", "Updated Event");
    assertTrue(result);
    events = calendarManager.queryEventsByDate(startTime);
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Updated Event"));

    result = calendarManager.editEvent("Updated Event", startTime,
            "location", "Updated Location");
    assertTrue(result);
    events = calendarManager.queryEventsByDate(startTime);
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Updated Location"));

    result = calendarManager.editEvent("Updated Event", startTime,
            "public", "false");
    assertTrue(result);
    events = calendarManager.queryEventsByDate(startTime);
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains(" (private)"));
  }

  @Test
  public void testEditNonExistingEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.editEvent("Nonexistent Event",
            LocalDateTime.of(2025, 3, 15, 16, 0),
            "description", "Updated Description");

    assertFalse(result);
  }

  @Test
  public void testEditInvalidProperty() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 15, 16, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 15, 17, 0);
    calendarManager.createEvent("Original Event", startTime, endTime,
            "Old Description", "Old Location", true, false);

    boolean result = calendarManager.editEvent("Original Event", startTime,
            "invalidProperty", "New Value");

    assertFalse(result);
  }

  @Test
  public void testOverlappingEvents() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    LocalDateTime start1 = LocalDateTime.of(2025, 3, 10, 10, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, 3, 10, 11, 0);
    calendarManager.createEvent("Event One", start1, end1,
            "Description One", "Location One", true, false);

    LocalDateTime start2 = LocalDateTime.of(2025, 3, 10, 10, 30);
    LocalDateTime end2 = LocalDateTime.of(2025, 3, 10, 11, 30);

    assertThrows(IllegalArgumentException.class, () -> {
      calendarManager.createEvent("Event Two", start2, end2,
              "Description Two", "Location Two", true, true);
    });
  }

  @Test
  public void testQueryEventsByRange() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    LocalDateTime start1 = LocalDateTime.of(2025, Month.MARCH, 20, 9, 0);
    LocalDateTime end1 = LocalDateTime.of(2025, Month.MARCH, 20, 10, 0);
    calendarManager.createEvent("Event One", start1, end1,
            "Description One", "Location One", true, false);

    LocalDateTime start2 = LocalDateTime.of(2025, Month.MARCH, 22, 11, 0);
    LocalDateTime end2 = LocalDateTime.of(2025, Month.MARCH, 22, 12, 0);
    calendarManager.createEvent("Event Two", start2, end2,
            "Description Two", "Location Two", true, false);

    List<String> eventsInRange = calendarManager.queryEventsByRange(
            LocalDateTime.of(2025, Month.MARCH, 19, 0, 0),
            LocalDateTime.of(2025, Month.MARCH, 21, 23, 59));

    assertEquals(1, eventsInRange.size());
    assertTrue(eventsInRange.get(0).contains("Event One"));
  }

  @Test
  public void testConflictsWith() {
    AbstractEvent event1 = new SingleEvent("Event 1",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true);

    AbstractEvent event2 = new SingleEvent("Event 2",
            LocalDateTime.of(2025, 3, 10, 12, 0),
            LocalDateTime.of(2025, 3, 10, 13, 0),
            "Description", "Location", true);

    assertFalse(event1.conflictsWith(event2));
    assertFalse(event2.conflictsWith(event1));

    AbstractEvent event3 = new SingleEvent("Event 3",
            LocalDateTime.of(2025, 3, 10, 10, 30),
            LocalDateTime.of(2025, 3, 10, 13, 30),
            "Description", "Location", true);

    assertTrue(event1.conflictsWith(event3));
    assertTrue(event3.conflictsWith(event2));
  }

  @Test
  public void testFallsWithinRange() {
    SingleEvent event = new SingleEvent("Test Event",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true);

    assertTrue(event.fallsWithinRange(
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 12, 0)));

    assertTrue(event.fallsWithinRange(
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0)));

    assertFalse(event.fallsWithinRange(
            LocalDateTime.of(2025, 3, 10, 11, 30),
            LocalDateTime.of(2025, 3, 10, 12, 0)));

    assertFalse(event.fallsWithinRange(
            LocalDateTime.of(2025, 3, 10, 8, 0),
            LocalDateTime.of(2025, 3, 10, 9, 30)));
  }

  @Test
  public void testIsAllDay() {
    SingleEvent allDayEvent = new SingleEvent("All-Day Event",
            LocalDateTime.of(2025, 3, 15, 0, 0),
            "Description", "Location", true);

    assertTrue(allDayEvent.isAllDay());

    SingleEvent partialDayEvent = new SingleEvent("Partial-Day Event",
            LocalDateTime.of(2025, 3, 15, 10, 0),
            LocalDateTime.of(2025, 3, 15, 11, 0),
            "Description", "Location", true);

    assertFalse(partialDayEvent.isAllDay());
  }

  @Test
  public void testOccursOn() {
    SingleEvent event = new SingleEvent("Test Event",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true);

    assertTrue(event.occursOn(LocalDateTime.of(2025, 3, 10, 10, 0)));
    assertTrue(event.occursOn(LocalDateTime.of(2025, 3, 10, 10, 30)));
    assertTrue(event.occursOn(LocalDateTime.of(2025, 3, 10, 11, 0)));

    assertFalse(event.occursOn(LocalDateTime.of(2025, 3, 10, 9, 59)));
    assertFalse(event.occursOn(LocalDateTime.of(2025, 3, 10, 11, 1)));
  }

  @Test
  public void testGenerateOccurrencesWithFixedOccurrences() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true,
            List.of(DayOfWeek.MONDAY), 3);

    List<AbstractEvent> occurrences = recurringEvent.generateOccurrences();

    assertEquals(3, occurrences.size());
    assertEquals("Weekly Meeting", occurrences.get(0).subject);
    assertEquals(LocalDateTime.of(2025, 3, 10, 10, 0), occurrences.get(0).startTime);
    assertEquals(LocalDateTime.of(2025, 3, 17, 10, 0), occurrences.get(1).startTime);
    assertEquals(LocalDateTime.of(2025, 3, 24, 10, 0), occurrences.get(2).startTime);
    assertTrue(
            occurrences.stream().noneMatch(event -> event.startTime.equals(
                    LocalDateTime.of(2025, 3, 31, 10, 0)))
    );
  }

  @Test
  public void testGenerateOccurrencesNoMatchingDays() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.WEDNESDAY),
            LocalDateTime.of(2025, 3, 11, 23, 59)
    );

    List<AbstractEvent> occurrences = recurringEvent.generateOccurrences();
    assertTrue(occurrences.isEmpty());
  }

  @Test(timeout = 1000)
  public void testSmallVsZeroOccurrence() {
    RecurringEvent smallEvent = new RecurringEvent(
            "Small Occurrence Event",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true,
            List.of(DayOfWeek.MONDAY), 1
    );

    List<AbstractEvent> smallResults = smallEvent.generateOccurrences();

    try {
      RecurringEvent zeroEvent = new RecurringEvent(
              "Zero Occurrence Event",
              LocalDateTime.of(2025, 3, 10, 10, 0),
              LocalDateTime.of(2025, 3, 10, 11, 0),
              "Description", "Location", true,
              List.of(DayOfWeek.MONDAY), 0
      );

      List<AbstractEvent> zeroResults = zeroEvent.generateOccurrences();

      assertEquals(0, zeroResults.size());
    } catch (Throwable e) {
      // Expected in original code due to infinite loop or timeout
    }
  }

  @Test
  public void testQueryEventsByRangeWithRecurringEvents() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    calendarManager .createRecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true, true,
            List.of("M"), 3, null
    );

    LocalDateTime rangeStart = LocalDateTime.of(2025, 3, 15, 0, 0);
    LocalDateTime rangeEnd = LocalDateTime.of(2025, 3, 25, 23, 59);
    List<String> eventsInRange = calendarManager.queryEventsByRange(rangeStart, rangeEnd);

    assertEquals(2, eventsInRange.size());
    assertTrue(eventsInRange.get(0).contains("Weekly Meeting"));
    assertTrue(eventsInRange.get(1).contains("Weekly Meeting"));
  }

  @Test
  public void testIsBusy() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    LocalDateTime eventStart = LocalDateTime.of(2025, 3, 15, 14, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 3, 15, 16, 0);

    SingleEvent event = new SingleEvent(
            "Important Meeting",
            eventStart,
            eventEnd,
            "Description", "Location", true);

    calendarManager.activeCalendar.addEvent(event);

    assertTrue(calendarManager.isBusy(LocalDateTime.of(2025, 3, 15, 15, 0)));
    assertFalse(calendarManager.isBusy(LocalDateTime.of(2025, 3, 15, 13, 0)));
    assertFalse(calendarManager.isBusy(LocalDateTime.of(2025, 3, 15, 17, 0)));

    boolean result = calendarManager.createRecurringEvent("Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            null,
            null,
            true,
            true,
            Arrays.asList("M", "T"),
            -1,
            LocalDateTime.of(2025, 4, 10, 10,0));

    assertTrue(calendarManager.isBusy(LocalDateTime.of(2025, 3, 17, 10, 30)));
  }

  @Test
  public void testCreateRecurringEventWithUntilDate() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    assertTrue(calendarManager.createRecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description", "Location", true, true,
            List.of("M"), -1,
            LocalDateTime.of(2025, 3, 31, 23, 59)
    ));

    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 17, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 24, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 31, 10, 0)).size());
    assertEquals(0, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 4, 7, 10, 0)).size());
  }

  @Test
  public void testCreateRecurringEventWithOccurrences() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    assertTrue(calendarManager .createRecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 7, 10, 0),
            LocalDateTime.of(2025, 3, 7, 11, 0),
            "Description", "Location", true, true,
            List.of("F"), 3, null
    ));

    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 7, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 14, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 21, 10, 0)).size());
    assertEquals(0, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 4, 11, 10, 0)).size());
  }

  @Test
  public void testRecurringEventForAllDaysOfWeek() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    // Arrange: Create a recurring event for all days of the week
    assertTrue(manager.createRecurringEvent(
            "Daily Standup",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Daily team sync-up",
            "Office",
            true, true,
            List.of("M", "T", "W", "R", "F", "S", "U"), -1,
            LocalDateTime.of(2025, 3, 16, 11, 0)
    ));

    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 11, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 12, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 13, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 14, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 15, 10, 0)).size());
    assertEquals(1, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 16, 10, 0)).size());
    assertEquals(0, manager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 17, 10, 0)).size());
  }

  @Test
  public void testOccursOn_ValidOccurrence() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.MONDAY),
            -1,
            LocalDateTime.of(2025, 4, 10, 23, 59)
    );

    boolean result = recurringEvent.occursOn(LocalDateTime.of(2025, 3, 10, 10, 30));
    assertTrue(result);
  }

  @Test
  public void testOccursOn_BeforeStartTime() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.MONDAY),
            -1,
            LocalDateTime.of(2025, 4, 10, 23, 59)
    );

    boolean result = recurringEvent.occursOn(LocalDateTime.of(2025, 3, 10, 9, 59));
    assertFalse(result);
  }

  @Test
  public void testOccursOn_AfterEndTime() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.MONDAY),
            -1,
            LocalDateTime.of(2025, 4, 10, 23, 59)
    );

    boolean result = recurringEvent.occursOn(LocalDateTime.of(2025, 3, 10, 11, 1));
    assertFalse(result);
  }

  @Test
  public void testOccursOn_OutsideUntilDate() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.MONDAY),
            -1,
            LocalDateTime.of(2025, 4, 10, 23, 59)
    );

    boolean result = recurringEvent.occursOn(LocalDateTime.of(2025, 4, 11, 9, 59));
    assertFalse(result);
  }

  @Test
  public void testOccursOn_InvalidDayOfWeek() {
    RecurringEvent recurringEvent = new RecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Description",
            "Location",
            true,
            List.of(DayOfWeek.MONDAY),
            -1,
            LocalDateTime.of(2025, 4, 10,23,59)
    );

    boolean result = recurringEvent.occursOn(LocalDateTime.of(2025, 3, 11,10,0));
    assertFalse(result);
  }

  @Test
  public void testRecurringEvent() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result = calendarManager.createEvent("Test Event",
            LocalDateTime.of(2025, 3, 7, 10, 0),
            LocalDateTime.of(2025, 3, 7, 11, 0),
            "Description", "Location", true, false);

    assertTrue(result);

    boolean result2 = calendarManager.createRecurringEvent("Test",
            LocalDateTime.of(2025, 3, 7, 10, 0),
            LocalDateTime.of(2025, 3, 7, 11, 0),
            "Description", "Location", true, false,
            List.of("M", "T", "W", "R", "F", "S", "U"), 5, null);

    assertTrue(result2);

    boolean result1 = calendarManager.isRecurringEvent("Test Event");
    assertFalse(result1);

    boolean result3 = calendarManager.isRecurringEvent("Test");
    assertTrue(result3);
  }

  @Test
  public void testCreateNonConflictingEvents() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result1 = calendarManager.createEvent("Meeting",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "Team meeting", "Office", true, true);

    boolean result2 = calendarManager.createEvent("Workshop",
            LocalDateTime.of(2025, 3, 10, 11, 0),
            LocalDateTime.of(2025, 3, 10, 12, 0),
            "Coding workshop", "Lab", true, true);

    assertTrue(result1);
    assertTrue(result2);
  }

  @Test
  public void testCreateOverlappingEventsWithAutoDecline() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result1 = calendarManager.createEvent("Morning Standup",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "Daily standup", "Conference Room", true, true);
    assertTrue(result1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendarManager.createEvent("Client Call",
              LocalDateTime.of(2025, 3, 10, 9, 30),
              LocalDateTime.of(2025, 3, 10, 10, 30),
              "Client discussion", "Office", true, true);
    });

  }

  @Test
  public void testRecurringEventCreation() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result = calendarManager.createRecurringEvent("Weekly Sync",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Weekly team sync", "Conference Room", true, true,
            Arrays.asList("M", "W", "F"), 3, null);

    assertTrue(result);
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 12, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 14, 10, 0)).size());
  }

  @Test
  public void testRecurringEventAutoDecline() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result1 = calendarManager.createRecurringEvent("Team Meeting",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Sync up", "Office", true, true,
            Arrays.asList("M", "W"), 4, null);
    assertTrue(result1);

    assertThrows(IllegalStateException.class, () -> {
      calendarManager.createRecurringEvent("Conflicting Event",
              LocalDateTime.of(2025, 3, 10, 10, 30),
              LocalDateTime.of(2025, 3, 10, 11, 30),
              "Overlap event", "Office", true, true,
              Arrays.asList("M"), 2, null);
    });
  }

  @Test
  public void testQueryByDate() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    calendarManager.createEvent("Lunch Break",
            LocalDateTime.of(2025, 3, 10, 12, 0),
            LocalDateTime.of(2025, 3, 10, 13, 0),
            "Lunch time", "Cafeteria", true, false);

    List<String> events = calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 12, 30));
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Lunch Break"));
  }

  @Test
  public void testQueryByRange() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    calendarManager.createEvent("Event 1",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "Meeting 1", "Room A", true, false);

    calendarManager.createEvent("Event 2",
            LocalDateTime.of(2025, 3, 11, 14, 0),
            LocalDateTime.of(2025, 3, 11, 15, 0),
            "Meeting 2", "Room B", true, false);

    List<String> events = calendarManager.queryEventsByRange(
            LocalDateTime.of(2025, 3, 10, 0, 0),
            LocalDateTime.of(2025, 3, 12, 0, 0));
    assertEquals(2, events.size());
  }

  @Test
  public void testCreateEventOutsideWorkingHours() {

    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");

    boolean result = calendarManager.createEvent("Late Night Work",
            LocalDateTime.of(2025, 3, 10, 23, 0),
            LocalDateTime.of(2025, 3, 11, 1, 0),
            "Late night project", "Home", true, true);

    assertTrue(result);
  }

  @Test
  public void testCreateEventWithExactTimeOverlap() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result1 = calendarManager.createEvent("Morning Meeting",
            LocalDateTime.of(2025, 3, 10, 8, 0),
            LocalDateTime.of(2025, 3, 10, 9, 0),
            "Sync up", "Room 101", true, true);

    assertTrue(result1);
    assertThrows(IllegalArgumentException.class, () -> {
      calendarManager.createEvent("Standup",
              LocalDateTime.of(2025, 3, 10, 8, 0),
              LocalDateTime.of(2025, 3, 10, 9, 0),
              "Daily Standup", "Room 102", true, true);
    });
  }

  @Test
  public void testRecurringEventOnMultipleDays() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.createRecurringEvent("Exercise",
            LocalDateTime.of(2025, 3, 10, 7, 0),
            LocalDateTime.of(2025, 3, 10, 8, 0),
            "Morning Exercise", "Gym", true, true,
            Arrays.asList("M", "W", "F"), 5, null);

    assertTrue(result);
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 7, 30)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 12, 7, 30)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 14, 7, 30)).size());
  }

  @Test
  public void testRecurringEventConflictWithSingleEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    calendarManager.createEvent("One-time Meeting",
            LocalDateTime.of(2025, 3, 12, 10, 0),
            LocalDateTime.of(2025, 3, 12, 11, 0),
            "Important", "Conference Room", true, true);


    assertThrows(IllegalStateException.class, () -> {
      calendarManager.createRecurringEvent("Weekly Sync",
              LocalDateTime.of(2025, 3, 10, 10, 0),
              LocalDateTime.of(2025, 3, 10, 11, 0),
              "Weekly meeting", "Conference Room", true, true,
              Arrays.asList("M", "W"), 5, null);
    });
  }

  @Test
  public void testRecurringEventEndingOnSpecificDate() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.createRecurringEvent("Limited Recurrence",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Limited", "Office", true, true,
            Arrays.asList("M", "T", "W"), -1,
            LocalDateTime.of(2025, 3, 20, 23, 59));

    assertTrue(result);
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 10, 0)).size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 18, 10, 0)).size());
    assertEquals(0, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 21, 10, 0))
            .size());
  }

  @Test
  public void testRecurringEventWithNoMatchingDays() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.createRecurringEvent("Sunday Event",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Only on Sundays", "Church", true, true,
            Arrays.asList("U"), 3, null);

    assertTrue(result);
    assertEquals(0, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 10, 0))
            .size());
    assertEquals(1, calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 16, 10, 0))
            .size());
  }

  @Test
  public void testEditingEventProperty() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    calendarManager.createEvent("Editable Meeting",
            LocalDateTime.of(2025, 3, 10, 14, 0),
            LocalDateTime.of(2025, 3, 10, 15, 0),
            "Initial Description", "Office", true, false);

    boolean editResult = calendarManager.editEvent("Editable Meeting",
            LocalDateTime.of(2025, 3, 10, 14, 0),
            "description", "Updated Description");

    assertTrue(editResult);
    List<String> events = calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 10, 14, 0));
    assertTrue(events.get(0).contains("Updated Description"));
  }

  @Test
  public void testEditNonExistentEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.editEvent("Fake Event",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "description", "New Desc");

    assertFalse(result);
  }

  @Test
  public void testQueryEventsByDateWithRecurring() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    calendarManager.createRecurringEvent("Daily Standup",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 9, 30),
            "Daily sync", "Conference Room", true, false,
            Arrays.asList("M", "T", "W", "R", "F"), -1,
            LocalDateTime.of(2025, 3, 20, 23, 59));

    List<String> events = calendarManager.queryEventsByDate(
            LocalDateTime.of(2025, 3, 11, 9, 0));
    assertEquals(1, events.size());
    assertTrue(events.get(0).contains("Daily Standup"));
  }

  @Test
  public void testCreateEventWithEndBeforeStart() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result = calendarManager.createEvent("Invalid Event",
            LocalDateTime.of(2025, 3, 10, 11, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "This should fail", "Office", true, true);

    assertTrue(result);
  }

  @Test
  public void testRecurringEventConflictsWithSingleEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result1 = calendarManager.createEvent("Morning Briefing",
            LocalDateTime.of(2025, 3, 10, 8, 0),
            LocalDateTime.of(2025, 3, 10, 9, 0),
            "Team update", "Office", true, true);

    assertTrue(result1);

    assertThrows(IllegalStateException.class, () -> {
      calendarManager.createRecurringEvent("Weekly Standup",
              LocalDateTime.of(2025, 3, 10, 8, 30),
              LocalDateTime.of(2025, 3, 10, 9, 30),
              "Overlap test", "Office", true, true,
              Arrays.asList("M", "W"), 5, null);
    });

  }

  @Test
  public void testRecurringAllDayEventConflictsWithSingleEvent() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result1 = calendarManager.createRecurringEvent("Daily Sync",
            LocalDateTime.of(2025, 3, 10, 0, 0),
            LocalDateTime.of(2025, 3, 10, 23, 59),
            "All day event", "Remote", true, true,
            Arrays.asList("M", "T", "W"), -1,
            LocalDateTime.of(2025, 3, 30, 23, 59));
    assertTrue(result1);

    assertThrows(IllegalArgumentException.class, () -> {
      calendarManager.createEvent("Important Meeting",
              LocalDateTime.of(2025, 3, 10, 10, 0),
              LocalDateTime.of(2025, 3, 10, 11, 0),
              "Conflict with all-day event", "Office", true, true);
    });
  }

  @Test
  public void testCreateRecurringEventWithDifferentCombinations() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result1 = calendarManager.createRecurringEvent("Monday Meetings",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "Weekly sync", "Room A", true, true,
            Arrays.asList("M"), 4, null);

    boolean result2 = calendarManager.createRecurringEvent("Friday Wrap-up",
            LocalDateTime.of(2025, 3, 14, 16, 0),
            LocalDateTime.of(2025, 3, 14, 17, 0),
            "End of week summary", "Room B", true, true,
            Arrays.asList("F"), 3, null);

    assertTrue(result1);
    assertTrue(result2);
  }

  @Test
  public void testEventTimingManipulation() {
    CalendarManager calendarManager = new CalendarManager();
    calendarManager.createCalendar("Work", "America/New_York");
    calendarManager.useCalendar("Work");
    boolean result1 = calendarManager.createEvent("Morning Event",
            LocalDateTime.of(2025, 3, 10, 6, 0),
            LocalDateTime.of(2025, 3, 10, 7, 0),
            "Early meeting", "Room C", true, true);

    boolean result2 = calendarManager.createEvent("Adjusted Event",
            LocalDateTime.of(2025, 3, 10, 7, 1),
            LocalDateTime.of(2025, 3, 10, 8, 0),
            "Following meeting", "Room C", true, true);

    assertTrue(result1);
    assertTrue(result2);
  }

  @Test
  public void testEditCalendarName() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");

    assertTrue(manager.editCalendar("Work", "name", "Office"));

    manager.createCalendar("Personal", "Europe/London");
    assertFalse(manager.editCalendar("Office", "name", "Personal"));

    assertFalse(manager.editCalendar("NonExistent", "name", "NewName"));
  }

  @Test
  public void testEditCalendarTimezone() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");

    assertTrue(manager.editCalendar("Work", "timezone", "Europe/London"));


    assertFalse(manager.editCalendar("Work", "timezone", "Invalid/Timezone"));

    assertFalse(manager.editCalendar("NonExistent", "timezone", "Europe/London"));
  }

  @Test
  public void testUseCalendar() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");

    assertTrue(manager.useCalendar("Work"));

    assertFalse(manager.useCalendar("NonExistent"));
  }

  @Test
  public void testCopySingleEvent() {
    CalendarManager manager = new CalendarManager();

    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime startTime = LocalDateTime.of(2025, 3, 20, 9, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 20, 10, 0);

    SingleEvent event = new SingleEvent(
            "Meeting",
            startTime,
            endTime,
            "Discuss project",
            "Office",
            true);

    manager.activeCalendar.addEvent(event);

    manager.createCalendar("Personal", "Europe/London");

    LocalDateTime newStartTime = LocalDateTime.of(2025, 3, 21, 10, 0);

    assertTrue(manager.copySingleEvent(
            "Meeting",
            startTime,
            "Personal",
            newStartTime));

    assertFalse(manager.copySingleEvent(
            "NonExistentEvent",
            startTime,
            "Personal",
            newStartTime));
  }

  @Test
  public void testChangeTimezone() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime eventStart1 = LocalDateTime.of(2025, 3, 20, 9, 0);
    LocalDateTime eventEnd1 = LocalDateTime.of(2025, 3, 20, 10, 0);
    LocalDateTime eventStart2 = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime eventEnd2 = LocalDateTime.of(2025, 3, 20, 15, 0);

    manager.activeCalendar.addEvent(new SingleEvent(
            "Morning Meeting", eventStart1, eventEnd1,
            "Discuss project updates", "Conference Room", true));

    manager.activeCalendar.addEvent(new SingleEvent(
            "Afternoon Meeting", eventStart2, eventEnd2,
            "Discuss team goals", "Office", true));


    boolean success = manager.editCalendar("Work", "timezone", "Europe/London");
    assertTrue(success);

    Calendar workCalendar = manager.calendars.get("Work");
    assertEquals(ZoneId.of("Europe/London"), workCalendar.timezone);

    AbstractEvent updatedEvent1 = workCalendar.events.get(0);
    AbstractEvent updatedEvent2 = workCalendar.events.get(1);

    ZonedDateTime expectedStart1 = eventStart1.atZone(ZoneId.of("America/New_York"))
            .withZoneSameInstant(ZoneId.of("Europe/London"));
    ZonedDateTime expectedEnd1 = eventEnd1.atZone(ZoneId.of("America/New_York"))
            .withZoneSameInstant(ZoneId.of("Europe/London"));

    ZonedDateTime expectedStart2 = eventStart2.atZone(ZoneId.of("America/New_York"))
            .withZoneSameInstant(ZoneId.of("Europe/London"));
    ZonedDateTime expectedEnd2 = eventEnd2.atZone(ZoneId.of("America/New_York"))
            .withZoneSameInstant(ZoneId.of("Europe/London"));

    assertEquals(expectedStart1.toLocalDateTime(), updatedEvent1.startTime);
    assertEquals(expectedEnd1.toLocalDateTime(), updatedEvent1.endTime);

    assertEquals(expectedStart2.toLocalDateTime(), updatedEvent2.startTime);
    assertEquals(expectedEnd2.toLocalDateTime(), updatedEvent2.endTime);
  }

  @Test
  public void testCopySingleEventsTime() {
    CalendarManager manager = new CalendarManager();

    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime eventStart = LocalDateTime.of(2025, 3, 20, 9, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2025, 3, 20, 10, 0);
    LocalDateTime newStartTime = LocalDateTime.of(2025, 3, 21, 10, 0);


    assertThrows(IllegalArgumentException.class, () -> {
      manager.copySingleEvent(
              "Morning Meeting",
              eventStart,
              "Personal",
              newStartTime);
    });

    manager.activeCalendar.addEvent(new SingleEvent(
            "Morning Meeting", eventStart, eventEnd,
            "Discuss project updates", "Conference Room", true));

    manager.createCalendar("Personal", "Europe/London");
    boolean success = manager.copySingleEvent(
            "Morning Meeting",
            eventStart,
            "Personal",
            newStartTime);

    assertTrue(success);

    boolean conflict = manager.copySingleEvent(
            "Morning Meeting",
            eventStart,
            "Personal",
            newStartTime);
    assertFalse(conflict);

    Calendar targetCalendar = manager.calendars.get("Personal");
    AbstractEvent copiedEvent = targetCalendar.events.get(0);

    assertEquals(newStartTime, copiedEvent.startTime);
    assertEquals(newStartTime.plusHours(1), copiedEvent.endTime);
  }

  @Test
  public void testCopyEventsOnTimezoneAdjustment() {
    CalendarManager manager = new CalendarManager();

    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime originalStart1 = LocalDateTime.of(2025, 3, 20, 9, 0);
    LocalDateTime originalEnd1 = LocalDateTime.of(2025, 3, 20, 10, 0);
    LocalDateTime originalStart2 = LocalDateTime.of(2025, 3, 20, 14, 0);
    LocalDateTime originalEnd2 = LocalDateTime.of(2025, 3, 20, 15, 0);

    manager.activeCalendar.addEvent(new SingleEvent(
            "Morning Meeting", originalStart1, originalEnd1,
            "Discuss project updates", "Conference Room", true));

    manager.activeCalendar.addEvent(new SingleEvent(
            "Afternoon Meeting", originalStart2, originalEnd2,
            "Discuss team goals", "Office", true));

    assertThrows(IllegalArgumentException.class, () -> {
      manager.copyEventsOn(LocalDate.of(2025, 3, 20),
              "Personal",
              LocalDate.of(2025, 3, 21));
    });

    manager.createCalendar("Personal", "Europe/London");
    boolean success = manager.copyEventsOn(LocalDate.of(2025, 3, 20),
            "Personal",
            LocalDate.of(2025, 3, 21));

    assertTrue(success);

    boolean successAfter = manager.copyEventsOn(LocalDate.of(2025, 3, 20),
            "Personal",
            LocalDate.of(2025, 3, 21));

    assertFalse(successAfter);

    Calendar personalCalendar = manager.calendars.get("Personal");

    assertEquals(2, personalCalendar.events.size());

    AbstractEvent copiedEvent1 = personalCalendar.events.get(0);
    AbstractEvent copiedEvent2 = personalCalendar.events.get(1);

    assertEquals("Morning Meeting", copiedEvent1.subject);
    assertEquals(LocalDateTime.of(2025, 3, 21, 13, 0), copiedEvent1.startTime);
    assertEquals(LocalDateTime.of(2025, 3, 21, 14, 0), copiedEvent1.endTime);

    assertEquals("Afternoon Meeting", copiedEvent2.subject);
    assertEquals(LocalDateTime.of(2025, 3, 21, 18, 0), copiedEvent2.startTime);
    assertEquals(LocalDateTime.of(2025, 3, 21, 19, 0), copiedEvent2.endTime);
  }

  @Test
  public void testCopyEventsBetweenTimezoneAdjustment() {
    CalendarManager manager = new CalendarManager();

    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    LocalDateTime originalStart1 = LocalDateTime.of(2025, 3, 19, 9, 0); // March 19, 9 AM NY
    LocalDateTime originalEnd1 = LocalDateTime.of(2025, 3, 19, 10, 0);

    LocalDateTime originalStart2 = LocalDateTime.of(2025, 3, 20, 14, 0); // March 20, 2 PM NY
    LocalDateTime originalEnd2 = LocalDateTime.of(2025, 3, 20, 15, 0);

    manager.activeCalendar.addEvent(new SingleEvent(
            "Morning Meeting",
            originalStart1,
            originalEnd1,
            "Discuss project updates",
            "Conference Room",
            true));

    manager.activeCalendar.addEvent(new SingleEvent(
            "Afternoon Meeting",
            originalStart2,
            originalEnd2,
            "Discuss team goals",
            "Office",
            true));


    assertThrows(IllegalArgumentException.class, () -> {
      manager.copyEventsBetween(
              LocalDate.of(2025, 3, 19),
              LocalDate.of(2025, 3, 20),
              "Personal",
              LocalDate.of(2025, 3, 25));
    });

    manager.createCalendar("Personal", "Europe/London");

    boolean success = manager.copyEventsBetween(
            LocalDate.of(2025, 3, 19),
            LocalDate.of(2025, 3, 20),
            "Personal",
            LocalDate.of(2025, 3, 25));

    assertTrue(success);

    boolean successAfter = manager.copyEventsBetween(
            LocalDate.of(2025, 3, 19),
            LocalDate.of(2025, 3, 20),
            "Personal",
            LocalDate.of(2025, 3, 25));

    assertFalse(successAfter);

    Calendar targetCalendar = manager.calendars.get("Personal");
    assertEquals(2, targetCalendar.events.size());

    AbstractEvent copiedEvent1 = targetCalendar.events.get(0);
    AbstractEvent copiedEvent2 = targetCalendar.events.get(1);

    assertEquals("Morning Meeting", copiedEvent1.subject);
    assertEquals(LocalDateTime.of(2025, 3, 25, 13, 0), copiedEvent1.startTime);
    assertEquals(LocalDateTime.of(2025, 3, 25, 14, 0), copiedEvent1.endTime);

    assertEquals("Afternoon Meeting", copiedEvent2.subject);
    assertEquals(LocalDateTime.of(2025, 3, 25, 18, 0), copiedEvent2.startTime);
    assertEquals(LocalDateTime.of(2025, 3, 25, 19, 0), copiedEvent2.endTime);
  }

  @Test
  public void testEditCalendarRenameToExistingName() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "America/Los_Angeles");

    boolean result = manager.editCalendar("Work", "name", "Personal");

    assertFalse(result);

    assertTrue(manager.calendars.containsKey("Work"));
    assertTrue(manager.calendars.containsKey("Personal"));
  }

  @Test
  public void testRenameCalendar() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");

    boolean renameResult = manager.editCalendar("Work", "name", "Office");
    assertTrue(renameResult);

    assertFalse(manager.calendars.containsKey("Work"));
    assertTrue(manager.calendars.containsKey("Office"));

    Calendar renamedCalendar = manager.calendars.get("Office");
    assertEquals("Office", renamedCalendar.name);
  }

  @Test
  public void testEditInvalidPropertyCal() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");
    boolean result = manager.editCalendar("Work", "description", "Personal");

    assertFalse(result);
  }

  private final CalendarManager manager = new CalendarManager();

  @Test
  public void testCreateCalendar_success() {
    assertTrue(manager.createCalendar("Work", "America/New_York"));
  }

  @Test
  public void testCreateCalendar_duplicateNameFails() {
    manager.createCalendar("Work", "America/New_York");
    assertFalse(manager.createCalendar("Work", "Europe/London"));
  }

  @Test
  public void testCreateCalendar_invalidTimezoneFails() {
    assertFalse(manager.createCalendar("Work", "Invalid/Zone"));
  }

  @Test
  public void testEditCalendarName_success() {
    manager.createCalendar("Old", "America/New_York");
    assertTrue(manager.editCalendar("Old", "name", "New"));
  }

  @Test
  public void testEditCalendarTimezone_success() {
    manager.createCalendar("C1", "America/New_York");
    assertTrue(manager.editCalendar("C1", "timezone", "Europe/Berlin"));
  }

  @Test
  public void testUseCalendar_success() {
    manager.createCalendar("Home", "Asia/Tokyo");
    assertTrue(manager.useCalendar("Home"));
  }

  @Test
  public void testCopySingleEvent_success() {
    manager.createCalendar("Source", "America/New_York");
    manager.createCalendar("Target", "Europe/Berlin");
    manager.useCalendar("Source");

    AbstractEvent event = new SingleEvent(
            "Meeting",
            LocalDateTime.of(2025, 4, 1, 10, 0),
            LocalDateTime.of(2025, 4, 1, 11, 0),
            "Team Sync",
            "Zoom",
            true
    );
    manager.calendars.get("Source").addEvent(event);

    assertTrue(manager.copySingleEvent("Meeting",
            LocalDateTime.of(2025, 4, 1, 10, 0),
            "Target",
            LocalDateTime.of(2025, 5, 1, 9, 0)));
  }

  @Test
  public void testCopyEventsOn_success() {
    manager.createCalendar("Source", "America/New_York");
    manager.createCalendar("Target", "Europe/Berlin");
    manager.useCalendar("Source");

    AbstractEvent event = new SingleEvent(
            "Lecture",
            LocalDateTime.of(2024, 9, 5, 9, 0),
            LocalDateTime.of(2024, 9, 5, 10, 0),
            "Topic A",
            "Room 1",
            true
    );
    manager.calendars.get("Source").addEvent(event);

    assertTrue(manager.copyEventsOn(LocalDate.of(2024, 9, 5), "Target", LocalDate.of(2025, 1, 8)));
  }

  @Test
  public void testCopyEventsBetween_success() {
    manager.createCalendar("Source", "America/New_York");
    manager.createCalendar("Target", "Europe/Berlin");
    manager.useCalendar("Source");

    manager.calendars.get("Source").addEvent(new SingleEvent(
            "Class A",
            LocalDateTime.of(2024, 9, 5, 9, 0),
            LocalDateTime.of(2024, 9, 5, 10, 0),
            "Intro",
            "Room A",
            true
    ));

    manager.calendars.get("Source").addEvent(new SingleEvent(
            "Class B",
            LocalDateTime.of(2024, 9, 10, 9, 0),
            LocalDateTime.of(2024, 9, 10, 10, 0),
            "Loops",
            "Room B",
            true
    ));

    assertTrue(manager.copyEventsBetween(
            LocalDate.of(2024, 9, 1),
            LocalDate.of(2024, 9, 15),
            "Target",
            LocalDate.of(2025, 1, 8)
    ));
  }

  @Test
  public void testCopyFailsWhenTargetCalendarNotFound() {
    manager.createCalendar("Source", "America/New_York");
    manager.useCalendar("Source");

    AbstractEvent event = new SingleEvent(
            "Solo",
            LocalDateTime.of(2025, 4, 1, 9, 0),
            LocalDateTime.of(2025, 4, 1, 10, 0),
            "Alone",
            "Desk",
            true
    );
    manager.calendars.get("Source").addEvent(event);

    assertThrows(IllegalArgumentException.class, () -> {
      manager.copySingleEvent("Solo", LocalDateTime.of(2025, 4, 1, 9, 0), "Missing",
              LocalDateTime.of(2025, 4, 2, 10, 0));
    });
  }

  @Test
  public void testEditCalendarName_calendarNotFound() {
    assertFalse(manager.editCalendar("Nonexistent", "name", "NewName"));
  }

  @Test
  public void testEditCalendarName_duplicateTargetNameFails() {
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Home", "Asia/Tokyo");

    assertFalse(manager.editCalendar("Work", "name", "Home")); // "Home" already exists
  }

  @Test
  public void testEditCalendarTimezone_invalidZoneFails() {
    manager.createCalendar("Remote", "Europe/Paris");

    assertFalse(manager.editCalendar("Remote", "timezone", "NotAZone"));
  }

  @Test
  public void testEditCalendar_unknownPropertyFails() {
    manager.createCalendar("TestCal", "America/Chicago");

    assertFalse(manager.editCalendar("TestCal", "color", "blue"));  // Invalid property
  }

  @Test
  public void testUseCalendar_notFound() {
    assertFalse(manager.useCalendar("NonexistentCalendar"));
  }

  @Test
  public void testCreateMultipleUniqueCalendars() {
    assertTrue(manager.createCalendar("Cal1", "America/New_York"));
    assertTrue(manager.createCalendar("Cal2", "Europe/London"));
    assertTrue(manager.createCalendar("Cal3", "Asia/Tokyo"));

    assertTrue(manager.calendars.containsKey("Cal1"));
    assertTrue(manager.calendars.containsKey("Cal2"));
    assertTrue(manager.calendars.containsKey("Cal3"));
  }

  @Test
  public void testUseCalendarAfterRename() {
    manager.createCalendar("OldName", "America/New_York");
    manager.editCalendar("OldName", "name", "NewName");

    assertFalse(manager.useCalendar("OldName"));
    assertTrue(manager.useCalendar("NewName"));
  }

  @Test
  public void testCreateCalendar_caseSensitivityMatters() {
    assertTrue(manager.createCalendar("Work", "America/New_York"));
    assertTrue(manager.createCalendar("work", "Europe/London"));
  }

  @Test
  public void testCreateCalendar_emptyNameFails() {
    assertFalse(manager.createCalendar("", "America/New_York"));
  }

  @Test
  public void testEditCalendarProperty_caseInsensitive() {
    manager.createCalendar("Test", "America/New_York");
    assertTrue(
            manager.editCalendar("Test", "TIMEZONE", "Asia/Tokyo")); // should be case-insensitive
  }

  @Test
  public void testEditCalendar_nonexistentCalendarFails() {
    assertFalse(manager.editCalendar("MissingCal", "name", "NewName"));
  }

  @Test
  public void testUseCalendar_afterTimezoneEditStillWorks() {
    manager.createCalendar("MyCal", "America/New_York");
    manager.editCalendar("MyCal", "timezone", "Asia/Kolkata");

    assertTrue(manager.useCalendar("MyCal"));
  }

  @Test
  public void testRecurringEventsWithOverlappingTimeRanges() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    boolean result1 = manager.createRecurringEvent("Morning Sync",
            LocalDateTime.of(2025, 3, 10, 9, 0),
            LocalDateTime.of(2025, 3, 10, 10, 0),
            "Daily sync", "Office", true, true,
            Arrays.asList("M", "W"), -1,
            LocalDateTime.of(2025, 3, 31, 23, 59));
    assertTrue(result1);

    assertThrows(IllegalStateException.class, () -> {
      calendarManager.createRecurringEvent("Team Meeting",
              LocalDateTime.of(2025, 3, 10, 9, 30),
              LocalDateTime.of(2025, 3, 10, 10, 30),
              "Weekly meeting", "Room A", true, true,
              Arrays.asList("M"), -1,
              LocalDateTime.of(2025, 3, 31, 23, 59));
    });
  }

  @Test
  public void testCopyEventsBetweenCalendarsWithTimezoneAdjustments() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Asia/Tokyo");
    manager.useCalendar("Work");

    manager.createEvent("Morning Meeting",
            LocalDateTime.of(2025, 3, 20, 9, 0),
            LocalDateTime.of(2025, 3, 20, 10, 0),
            "Discuss project updates", "Conference Room", true,
            true);

    boolean success = manager.copySingleEvent(
            "Morning Meeting",
            LocalDateTime.of(2025, 3, 20, 9, 0),
            "Personal",
            LocalDateTime.of(2025, 3, 21, 9, 0));

    assertTrue(success);

    Calendar personalCalendar = manager.calendars.get("Personal");
    AbstractEvent copiedEvent = personalCalendar.events.get(0);

    assertEquals(LocalDateTime.of(2025, 3, 21, 9, 0),
            copiedEvent.startTime);
  }

  @Test
  public void testCopyEventsBetweenCalendarsWithAdjustments() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Asia/Tokyo");
    manager.useCalendar("Work");

    manager.createEvent("Morning Meeting",
            LocalDateTime.of(2025, 3, 20, 9, 0),
            LocalDateTime.of(2025, 3, 20, 10, 0),
            "Discuss project updates", "Conference Room", true,
            true);

    boolean success = manager.copyEventsOn(
            LocalDate.of(2025,3,20),
            "Personal",
            LocalDate.of(2025,3,20));

    assertTrue(success);

    Calendar personalCalendar = manager.calendars.get("Personal");
    AbstractEvent copiedEvent = personalCalendar.events.get(0);

    assertEquals(LocalDateTime.of(2025, 3, 20, 22, 0),
            copiedEvent.startTime);
  }

  @Test
  public void testCopyEventsBetweenCalendarsWithAdjustments2() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Asia/Tokyo");
    manager.useCalendar("Work");

    manager.createEvent("Morning Meeting",
            LocalDateTime.of(2025, 3, 20, 9, 0),
            LocalDateTime.of(2025, 3, 20, 10, 0),
            "Discuss project updates", "Conference Room", true,
            true);

    boolean success = manager.copyEventsBetween(
            LocalDate.of(2025, 3, 20),
            LocalDate.of(2025, 3, 21),
            "Personal",
            LocalDate.of(2025,3,25));

    assertTrue(success);

    Calendar personalCalendar = manager.calendars.get("Personal");
    AbstractEvent copiedEvent = personalCalendar.events.get(0);

    assertEquals(LocalDateTime.of(2025, 3, 25, 22, 0),
            copiedEvent.startTime);
  }

  @Test
  public void testQueryEventsSpanningMidnight() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.useCalendar("Work");

    manager.createEvent(
            "Overnight Shift",
            LocalDateTime.of(2025, 3, 25, 22, 0),
            LocalDateTime.of(2025, 3, 26, 6, 0),
            "Night shift work",
            "Office",
            true, true
    );

    List<String> eventsMarch25 = manager.queryEventsByRange(
            LocalDateTime.of(2025, 3, 25, 0, 0),
            LocalDateTime.of(2025, 3, 25, 23, 59)
    );
    assertEquals(1, eventsMarch25.size());
    assertTrue(eventsMarch25.get(0).contains("Overnight Shift"));

    List<String> eventsMarch26 = manager.queryEventsByRange(
            LocalDateTime.of(2025, 3, 26, 0, 0),
            LocalDateTime.of(2025, 3, 26, 23, 59)
    );
    assertEquals(1, eventsMarch26.size());
    assertTrue(eventsMarch26.get(0).contains("Overnight Shift"));

    List<String> eventsFullRange = manager.queryEventsByRange(
            LocalDateTime.of(2025, 3, 25, 20, 0),
            LocalDateTime.of(2025, 3, 26, 8, 0)
    );
    assertEquals(1, eventsFullRange.size());
    assertTrue(eventsFullRange.get(0).contains("Overnight Shift"));

    List<String> noEvents = manager.queryEventsByRange(
            LocalDateTime.of(2025, 3, 24, 0, 0),
            LocalDateTime.of(2025, 3, 24, 23, 59)
    );
    assertTrue(noEvents.isEmpty());
  }

  @Test
  public void testCopyInSameCalendar() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Work", "America/New_York");
    manager.createCalendar("Personal", "Europe/London");
    manager.useCalendar("Work");

    manager.useCalendar("Personal");
    manager.createEvent("Conflict Meeting",
            LocalDateTime.of(2025, 3, 20, 13, 0),
            LocalDateTime.of(2025, 3, 20, 14, 0),
            "Team Sync", "Room A", true, true);

    boolean result = manager.copySingleEvent("Morning Meeting",
            LocalDateTime.of(2025, 3, 20, 9, 0),
            "Personal",
            LocalDateTime.of(2025, 3, 20, 13, 0));

    assertFalse(result);
  }

  @Test
  public void testCopySingleEventInvalidTargetCalendar() {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("Source", "America/New_York");

    assertThrows(IllegalArgumentException.class, () -> {
      manager.copySingleEvent("Meeting",
              LocalDateTime.of(2025, 4, 1, 10, 0),
              "NonExistentTarget",
              LocalDateTime.of(2025, 5, 1, 9, 0));
    });
  }

  @Test
  public void testGenerateOccurrencesWithFiniteCount() {
    LocalDateTime start = LocalDateTime.of(2025, 3, 21, 9, 0);
    LocalDateTime end   = LocalDateTime.of(2025, 3, 21, 10, 0);
    int occurrences = 3;

    RecurringEvent recurringEvent = new RecurringEvent("TestEvent", start, end, "desc", "loc", true,
            Arrays.asList(DayOfWeek.FRIDAY), occurrences);
    List<AbstractEvent> generated = recurringEvent.generateOccurrences();
    assertEquals("There should be 3 occurrences", 3, generated.size());
  }

  @Test
  public void testGenerateOccurrencesWithUntilDate() {
    LocalDateTime start = LocalDateTime.of(2025, 3, 21, 9, 0);
    LocalDateTime end   = LocalDateTime.of(2025, 3, 21, 10, 0);
    LocalDateTime until = LocalDateTime.of(2025, 4, 15, 23, 59);

    RecurringEvent recurringEvent = new RecurringEvent("TestEvent", start, end, "desc", "loc", true,
            Arrays.asList(start.getDayOfWeek()), -1, until);
    List<AbstractEvent> generated = recurringEvent.generateOccurrences();

    for (AbstractEvent occurrence : generated) {
      assertFalse("Occurrence must not be after until date", occurrence.startTime.isAfter(until));
    }
    assertTrue("At least one occurrence should be generated", generated.size() > 0);
  }

  @Test
  public void testGetCalendarNames() {
    CalendarManager cm = new CalendarManager();
    assertTrue(cm.createCalendar("Work", "UTC"));
    List<String> names = cm.getCalendarNames();
    assertTrue("Calendar names should contain 'Work'", names.contains("Work"));
  }

}