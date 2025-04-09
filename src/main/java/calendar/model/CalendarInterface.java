package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines the contract for a calendar management system.
 * Provides methods for creating, editing, querying, and managing events,
 * including support for recurring events and exporting data.
 */
public interface CalendarInterface {

  /**
   * Creates a new event in the calendar.
   *
   * @param subject      The subject or title of the event.
   * @param startTime    The start time of the event.
   * @param endTime      The end time of the event.
   * @param description  A description of the event.
   * @param location     The location of the event.
   * @param isPublic     Whether the event is public or private.
   * @param autoDecline  Whether conflicting events should be automatically declined.
   * @return {@code true} if the event was successfully created, {@code false} otherwise.
   */
  boolean createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                      String description, String location, boolean isPublic, boolean autoDecline);

  /**
   * Creates a new recurring event in the calendar.
   *
   * @param subject         The subject or title of the recurring event.
   * @param startTime       The start time of the first occurrence of the event.
   * @param endTime         The end time of the first occurrence of the event.
   * @param description     A description of the recurring event.
   * @param location        The location of the recurring event.
   * @param isPublic        Whether the recurring event is public or private.
   * @param autoDecline     Whether conflicting events should be automatically declined.
   * @param recurrenceDays  A list of days (e.g., "Monday", "Tuesday") on which the event recurs.
   * @param occurrences     Occurrences for the recurring event.
   *                        Ignored if {@code untilDate} is provided.
   * @param untilDate       The date until which the recurring event should repeat.
   *                        Can be {@code null}.
   * @return {@code true} if the recurring event was successfully created, {@code false} otherwise.
   */
  boolean createRecurringEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                               String description, String location, boolean isPublic,
                               boolean autoDecline, List<String> recurrenceDays,
                               int occurrences, LocalDateTime untilDate);

  /**
   * Edits an existing event in the calendar.
   *
   * @param subject         The subject or title of the event to edit.
   * @param startTime       The start time of the event to identify it uniquely.
   * @param propertyToEdit  The property to edit (e.g., "description", "location").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the event was successfully edited, {@code false} otherwise.
   */
  boolean editEvent(String subject, LocalDateTime startTime,
                    String propertyToEdit, String newValue);

  /**
   * Edits an existing recurring event in the calendar starting from a specific occurrence.
   *
   * @param subject         The subject or title of the recurring event to edit.
   * @param startFrom       The start time of the first occurrence to edit in the series.
   * @param propertyToEdit  The property to edit (e.g., "description", "location").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the recurring event was successfully edited, {@code false} otherwise.
   */
  boolean editRecurringEventDate(String subject, LocalDateTime startFrom,
                             String propertyToEdit, String newValue);

  /**
   * Edits all existing recurring event in the calendar with the same Subject.
   *
   * @param subject         The subject or title of the recurring event to edit.
   * @param propertyToEdit  The property to edit (e.g., "description", "location").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the recurring event was successfully edited, {@code false} otherwise.
   */
  boolean editRecurringEventValue(String subject, String propertyToEdit, String newValue);

  /**
   * Queries events scheduled on a specific date.
   *
   * @param date        The date for which to query events (time component may be ignored).
   * @return            A list of string representations of events occurring on that date.
   *                    Returns an empty list if no events are found.
   */
  List<String> queryEventsByDate(LocalDateTime date);

  /**
   * Queries events scheduled within a specific date and time range.
   *
   * @param startDate    The start date and time of the range to query events from (inclusive).
   * @param endDate      The end date and time of the range to query events to (inclusive).
   * @return              A list of string representations of events occurring within that range.
   *                      Returns an empty list if no events are found.
   */
  List<String> queryEventsByRange(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Checks whether there is any scheduled event at a specific date and time.
   *
   * @param dateTime    The date and time to check for conflicts or scheduled events.
   * @return {@code true} if there is an event at that date and time; {@code false} otherwise.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Checks whether an event with a given subject is part of a recurring series of events.
   *
   * @param subject   The subject or title of the event to check for recurrence status.
   * @return {@code true} if the specified event is part of a recurring series;
   * {@code false} otherwise.
   */
  boolean isRecurringEvent(String subject);

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name        The name of the calendar to be created. Must not be {@code null} or empty.
   * @param timezone    The timezone of the calendar in format.
   * @return {@code true} if the calendar was successfully created; {@code false} otherwise.
   */
  boolean createCalendar(String name, String timezone);

  /**
   * Edits a calendar's property based on its name.
   *
   * @param name            The name of the calendar to edit.
   * @param propertyName    The property to edit (e.g., "name", "timezone").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the calendar was successfully edited; {@code false} otherwise.
   */
  boolean editCalendar(String name, String propertyName, String newValue);

  /**
   * Switches the active calendar to the specified calendar by name.
   *
   * @param name    The name of the calendar to use as the active calendar.
   * @return {@code true} if the active calendar was successfully switched; {@code false} otherwise.
   */
  boolean useCalendar(String name);

  /**
   * Copies a single event from the active calendar to another specified calendar,
   * adjusting its start and end times based on the target calendar's timezone.
   *
   * @param eventName          The name of the event to copy.
   * @param originalStartTime  The original start time of the event in the active calendar.
   * @param targetCalendarName The name of the target calendar where the event will be copied.
   * @param newStartTime       The new start time of the event in the target calendar.
   * @return {@code true} if the event was successfully copied; {@code false} otherwise.
   */
  boolean copySingleEvent(String eventName, LocalDateTime originalStartTime,
                          String targetCalendarName, LocalDateTime newStartTime);


  /**
   * Copies all events occurring within a specific date range from the active calendar to,
   * another specified calendar, adjusting their start and end times based on a new starting,
   * date in the target calendar's timezone.
   *
   * @param rangeStart         The start date of the range in the active calendar.
   * @param rangeEnd           The end date of the range in the active calendar.
   * @param targetCalendarName The name of the target calendar where events will be copied.
   * @param newStartDate       The starting date in the target calendar for copied events.
   * @return {@code true} if at least one event was successfully copied; {@code false} otherwise.
   */
  boolean copyEventsBetween(LocalDate rangeStart, LocalDate rangeEnd,
                            String targetCalendarName, LocalDate newStartDate);

  /**
   * Copies all events occurring on a specific date from the active calendar to another,
   * specified calendar adjusting their start and end times based on the target date and timezone.
   *
   * @param originalDate       The date of events to copy from the active calendar.
   * @param targetCalendarName The name of the target calendar where events will be copied.
   * @param newDate            The date in the target calendar where events will be copied to.
   * @return {@code true} if at least one event was successfully copied; {@code false} otherwise.
   */
  boolean copyEventsOn(LocalDate originalDate, String targetCalendarName, LocalDate newDate);

  /**
   * Exports all events from the active calendar as CSV-formatted strings,
   * including details like subject, start and end times, description,
   * location, and privacy status.
   *
   * @return A list of CSV-formatted strings representing all events in the active calendar.
   *         Throws an exception if no active calendar is selected.
   */
  List<String> eventsAsCSV();
}