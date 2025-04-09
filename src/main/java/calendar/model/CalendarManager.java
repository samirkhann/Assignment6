package calendar.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the {@link CalendarInterface} to manage events in a calendar system.
 * Provides functionality for creating, editing, querying, exporting, and checking events,
 * including support for recurring events and conflict resolution.
 */
public class CalendarManager implements CalendarInterface {

  /** A list to store all events in the calendar. */
  Map<String, Calendar> calendars;
  Calendar activeCalendar;

  /**
   * Constructs a new instance of {@code CalendarManager}.
   * Initializes an empty list of events.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
    this.activeCalendar = null;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name        The name of the calendar to be created. Must not be {@code null} or empty.
   * @param timezone    The timezone of the calendar in {@link ZoneId} format.
   * @return {@code true} if the calendar was successfully created; {@code false} otherwise.
   */
  public boolean createCalendar(String name, String timezone) {

    if (name == null || name.trim().isEmpty()) {
      return false;
    }

    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(timezone);
    } catch (Exception e) {
      return false;
    }

    if (calendars.containsKey(name)) {
      return false;
    }
    calendars.put(name, new Calendar(name, zoneId));
    return true;
  }

  /**
   * Edits a calendar's property based on its name.
   *
   * @param calendarName        The name of the calendar to edit.
   * @param propertyName        The property to edit (e.g., "name", "timezone").
   * @param newPropertyValue    The new value for the specified property.
   * @return {@code true} if the calendar was successfully edited; {@code false} otherwise.
   */
  public boolean editCalendar(String calendarName, String propertyName, String newPropertyValue) {
    Calendar calendar = calendars.get(calendarName);
    if (calendar == null) {
      return false;
    }

    switch (propertyName.toLowerCase()) {
      case "name":
        if (calendars.containsKey(newPropertyValue)) {
          return false;
        }
        calendars.remove(calendarName);
        calendar.rename(newPropertyValue);
        calendars.put(newPropertyValue, calendar);
        return true;

      case "timezone":
        try {
          ZoneId newZone = ZoneId.of(newPropertyValue);
          ZoneId oldZone = calendar.timezone;

          for (AbstractEvent event : calendar.events) {
            ZonedDateTime startInOldZone = event.startTime.atZone(oldZone);
            ZonedDateTime endInOldZone = event.endTime.atZone(oldZone);

            event.startTime = startInOldZone.withZoneSameInstant(newZone).toLocalDateTime();
            event.endTime = endInOldZone.withZoneSameInstant(newZone).toLocalDateTime();
          }

          calendar.timezone = newZone;
          return true;
        } catch (Exception e) {
          return false;
        }

      default:
        return false;
    }
  }

  /**
   * Switches the active calendar to the specified calendar by name.
   *
   * @param name    The name of the calendar to use as the active calendar.
   * @return {@code true} if the active calendar was successfully switched; {@code false} otherwise.
   */
  public boolean useCalendar(String name) {
    if (!calendars.containsKey(name)) {
      return false;
    }
    activeCalendar = calendars.get(name);
    return true;
  }

  /**
   * Copies a single event from the active calendar to another specified calendar,
   * adjusting its start and end times based on the target calendar's timezone.
   *
   * @param eventName             The name of the event to copy.
   * @param originalStartTime     The original start time of the event in the active calendar.
   * @param targetCalendarName    The name of the target calendar where the event will be copied.
   * @param newStartTime          The new start time of the event in the target calendar.
   * @return {@code true} if the event was successfully copied; {@code false} otherwise.
   */
  public boolean copySingleEvent(String eventName, LocalDateTime originalStartTime,
                                 String targetCalendarName, LocalDateTime newStartTime) {

    Calendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null || activeCalendar == null) {
      throw new IllegalArgumentException("No active calendar or target calender found");
    }

    AbstractEvent eventToCopy = null;
    for (AbstractEvent event : activeCalendar.events) {
      if (event.subject.equals(eventName) && event.startTime.equals(originalStartTime)) {
        eventToCopy = event;
        break;
      }
    }

    if (eventToCopy == null) {
      return false;
    }

    ZoneId sourceZone = activeCalendar.timezone;
    ZoneId targetZone = targetCalendar.timezone;

    ZonedDateTime startInTargetZone = newStartTime.atZone(targetZone);
    Duration duration = Duration.between(eventToCopy.startTime.atZone(sourceZone),
            eventToCopy.endTime.atZone(sourceZone));
    LocalDateTime newEndTime = startInTargetZone.plus(duration).toLocalDateTime();

    AbstractEvent copiedEvent = new SingleEvent(eventToCopy.subject, newStartTime, newEndTime,
            eventToCopy.description, eventToCopy.location, eventToCopy.isPublic);

    if (targetCalendar.hasConflict(copiedEvent)) {
      return false;
    }

    targetCalendar.addEvent(copiedEvent);
    return true;
  }

  /**
   * Copies all events occurring on a specific date from the active calendar to another specified
   * calendar, adjusting their start and end times based on the target date and timezone.
   *
   * @param date               The date of events to copy from the active calendar.
   * @param targetCalendarName The name of the target calendar where events will be copied.
   * @param newDate            The date in the target calendar where events will be copied to.
   * @return {@code true} if at least one event was successfully copied; {@code false} otherwise.
   */
  public boolean copyEventsOn(LocalDate date, String targetCalendarName, LocalDate newDate) {

    Calendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null || activeCalendar == null) {
      throw new IllegalArgumentException("No active calendar or target calender found");
    }

    ZoneId sourceZone = activeCalendar.timezone;
    ZoneId targetZone = targetCalendar.timezone;

    boolean copiedAtLeastOneEvent = false;

    for (AbstractEvent event : activeCalendar.events) {
      ZonedDateTime eventStart = event.startTime.atZone(sourceZone);
      ZonedDateTime eventEnd = event.endTime.atZone(sourceZone);

      ZonedDateTime sourceDayStart = date.atStartOfDay(sourceZone);
      ZonedDateTime sourceDayEnd = date.atTime(23, 59, 59).atZone(sourceZone);
      boolean occursOnDate = !(eventEnd.isBefore(sourceDayStart)
              || eventStart.isAfter(sourceDayEnd));

      if (occursOnDate) {
        ZonedDateTime newStartInTargetZone = eventStart.withZoneSameInstant(targetZone)
                .withYear(newDate.getYear())
                .withMonth(newDate.getMonthValue())
                .withDayOfMonth(newDate.getDayOfMonth());
        ZonedDateTime newEndInTargetZone =
                newStartInTargetZone.plus(Duration.between(eventStart, eventEnd));

        LocalDateTime newStartTime = newStartInTargetZone.toLocalDateTime();
        LocalDateTime newEndTime = newEndInTargetZone.toLocalDateTime();

        AbstractEvent copiedEvent = new SingleEvent(event.subject, newStartTime, newEndTime,
                event.description, event.location, event.isPublic);

        if (!targetCalendar.hasConflict(copiedEvent)) {
          targetCalendar.addEvent(copiedEvent);
          copiedAtLeastOneEvent = true;
        }
      }
    }

    return copiedAtLeastOneEvent;
  }

  /**
   * Copies all events occurring within a specific date range from the active,
   * calendar to another specified calendar, adjusting their start and end times,
   * based on a new starting date in the target calendar's timezone.
   *
   * @param rangeStart         The start date of the range in the active calendar.
   * @param rangeEnd           The end date of the range in the active calendar.
   * @param targetCalendarName The name of the target calendar where events will be copied.
   * @param newStartDate       The starting date in the target calendar for copied events.
   * @return {@code true} if at least one event was successfully copied; {@code false} otherwise.
   */
  public boolean copyEventsBetween(LocalDate rangeStart, LocalDate rangeEnd,
                                   String targetCalendarName, LocalDate newStartDate) {
    Calendar targetCalendar = calendars.get(targetCalendarName);
    if (targetCalendar == null || activeCalendar == null) {
      throw new IllegalArgumentException("No active calendar or target calender found");
    }

    ZoneId sourceZone = activeCalendar.timezone;
    ZoneId targetZone = targetCalendar.timezone;

    ZonedDateTime sourceRangeStart = rangeStart.atStartOfDay(sourceZone);
    ZonedDateTime sourceRangeEnd = rangeEnd.atTime(23, 59, 59).atZone(sourceZone);
    ZonedDateTime targetRangeStart = newStartDate.atStartOfDay(targetZone);

    boolean copiedAtLeastOneEvent = false;

    for (AbstractEvent event : activeCalendar.events) {
      ZonedDateTime eventStart = event.startTime.atZone(sourceZone);
      ZonedDateTime eventEnd = event.endTime.atZone(sourceZone);

      boolean occursInRange = !(eventEnd.isBefore(sourceRangeStart)
              || eventStart.isAfter(sourceRangeEnd));

      if (occursInRange) {

        ZonedDateTime newStartInTargetZone = eventStart.withZoneSameInstant(targetZone)
                .withYear(newStartDate.getYear())
                .withMonth(newStartDate.getMonthValue())
                .withDayOfMonth(newStartDate.getDayOfMonth());
        ZonedDateTime newEndInTargetZone =
                newStartInTargetZone.plus(Duration.between(eventStart, eventEnd));

        LocalDateTime newStartTime = newStartInTargetZone.toLocalDateTime();
        LocalDateTime newEndTime = newEndInTargetZone.toLocalDateTime();

        AbstractEvent copiedEvent = new SingleEvent(event.subject, newStartTime, newEndTime,
                event.description, event.location, event.isPublic);

        if (!targetCalendar.hasConflict(copiedEvent)) {
          targetCalendar.addEvent(copiedEvent);
          copiedAtLeastOneEvent = true;
        }
      }
    }

    return copiedAtLeastOneEvent;
  }

  /**
   * Creates a single event in the calendar.
   *
   * @param subject      The subject or title of the event.
   * @param startTime    The start time of the event.
   * @param endTime      The end time of the event.
   * @param description  A description of the event.
   * @param location     The location of the event.
   * @param isPublic     Whether the event is public or private.
   * @param autoDecline  Whether conflicting events should be automatically declined.
   * @return {@code true} if the event was successfully created;
   * {@code false} if conflicts exist and auto-decline is enabled.
   */
  @Override
  public boolean createEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                             String description, String location,
                             boolean isPublic, boolean autoDecline) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    SingleEvent newEvent = new SingleEvent(subject, startTime,
            endTime, description, location, isPublic);

    if (autoDecline && activeCalendar.hasConflict(newEvent)) {
      throw new IllegalArgumentException("Conflict detected");
    }

    activeCalendar.addEvent(newEvent);
    return true;
  }

  /**
   * Creates a recurring event in the calendar.
   *
   * @param subject         The subject or title of the recurring event.
   * @param startTime       The start time of the first occurrence of the event.
   * @param endTime         The end time of the first occurrence of the event.
   * @param description     A description of the recurring event.
   * @param location        The location of the recurring event.
   * @param isPublic        Whether the recurring event is public or private.
   * @param autoDecline     Whether conflicting events should be automatically declined.
   * @param recurrenceDayStrings A list of strings representing recurrence days (e.g., "M", "T").
   * @param occurrences     The number of occurrences for the recurring event.
   *                        Ignored if {@code untilDate} is provided.
   * @param untilDate       The date until which the recurring event should repeat.
   *                        Can be {@code null}.
   * @return                {@code true} if the recurring event was successfully created;
   *                        {@code false} if conflicts exist and auto-decline is enabled.
   */
  @Override
  public boolean createRecurringEvent(String subject, LocalDateTime startTime,
                                      LocalDateTime endTime, String description,
                                      String location, boolean isPublic, boolean autoDecline,
                                      List<String> recurrenceDayStrings,
                                      int occurrences, LocalDateTime untilDate) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    List<DayOfWeek> recurrenceDays = new ArrayList<>();
    for (String day : recurrenceDayStrings) {
      switch (day.toUpperCase()) {
        case "M":
          recurrenceDays.add(DayOfWeek.MONDAY);
          break;
        case "T":
          recurrenceDays.add(DayOfWeek.TUESDAY);
          break;
        case "W":
          recurrenceDays.add(DayOfWeek.WEDNESDAY);
          break;
        case "R":
          recurrenceDays.add(DayOfWeek.THURSDAY);
          break;
        case "F":
          recurrenceDays.add(DayOfWeek.FRIDAY);
          break;
        case "S":
          recurrenceDays.add(DayOfWeek.SATURDAY);
          break;
        case "U":
          recurrenceDays.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalStateException("Inappropriate day: " + day);
      }
    }

    RecurringEvent recurringEvent;

    if (untilDate != null) {
      recurringEvent = new RecurringEvent(subject, startTime, endTime, description, location,
              isPublic, recurrenceDays, untilDate);
    } else {
      recurringEvent = new RecurringEvent(subject, startTime, endTime, description, location,
              isPublic, recurrenceDays, occurrences);
    }

    List<AbstractEvent> instances = recurringEvent.generateOccurrences();

    if (autoDecline) {
      for (AbstractEvent instance : instances) {
        if (activeCalendar.hasConflict(instance)) {
          throw new IllegalStateException("Conflicted event: " + instance.subject);
        }
      }
    }

    for (AbstractEvent instance : instances) {
      activeCalendar.addEvent(instance);
    }

    return true;
  }

  /**
   * Edits a single event's property based on its subject and start time.
   *
   * @param subject         The subject or title of the event to edit.
   * @param startTime       The start time of the event to identify it uniquely.
   * @param propertyToEdit  The property to edit (e.g., "description", "location", "time").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the event was successfully edited; {@code false} otherwise.
   */
  @Override
  public boolean editEvent(String subject, LocalDateTime startTime,
                           String propertyToEdit, String newValue) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    for (AbstractEvent event : activeCalendar.events) {
      if (event.subject.equals(subject) && event.startTime.equals(startTime)) {
        switch (propertyToEdit.toLowerCase()) {
          case "subject":
            event.subject = newValue;
            return true;
          case "description":
            event.description = newValue;
            return true;
          case "location":
            event.location = newValue;
            return true;
          case "time":
            LocalDateTime newStartTime = LocalDateTime.parse(newValue);
            LocalDateTime newEndTime = newStartTime.plusMinutes(
                    Duration.between(event.startTime, event.endTime).toMinutes());
            event.startTime = newStartTime;
            event.endTime = newEndTime;
            return true;
          case "public":
            event.isPublic = Boolean.parseBoolean(newValue);
            return true;
          default:
            return false;
        }
      }
    }
    return false;
  }

  /**
   * Edits a recurring event's property based on its subject and start time.
   *
   * @param subject         The subject or title of the event to edit.
   * @param startFrom       The start time of the event to identify it uniquely.
   * @param propertyToEdit  The property to edit (e.g., "description", "location", "time").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the event was successfully edited; {@code false} otherwise.
   */
  @Override
  public boolean editRecurringEventDate(String subject, LocalDateTime startFrom,
                                    String propertyToEdit, String newValue) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    boolean updated = false;

    for (AbstractEvent event : activeCalendar.events) {
      if (event.subject.equals(subject)) {
        if (event.startTime.equals(startFrom) || event.startTime.isAfter(startFrom)) {
          switch (propertyToEdit.toLowerCase()) {
            case "subject":
              event.subject = newValue;
              updated = true;
              break;
            case "description":
              event.description = newValue;
              updated = true;
              break;
            case "location":
              event.location = newValue;
              updated = true;
              break;
            case "time":
              try {
                String input = newValue.trim();
                if (input.contains("T")) {
                  input = input.substring(input.indexOf("T") + 1);
                }
                LocalTime newTime = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDate eventDate = event.startTime.toLocalDate();
                LocalDateTime newStartTime = LocalDateTime.of(eventDate, newTime);
                long durationMinutes = Duration.between(event.startTime, event.endTime).toMinutes();
                event.startTime = newStartTime;
                event.endTime = newStartTime.plusMinutes(durationMinutes);
                updated = true;
              } catch (Exception e) {
                // Optionally handle parsing error here.
              }
              break;
            case "public":
              event.isPublic = Boolean.parseBoolean(newValue);
              updated = true;
              break;
            default:
              break;
          }
        }
      }
    }
    return updated;
  }

  /**
   * Edits a recurring event's property based on its subject.
   *
   * @param subject         The subject or title of the event to edit.
   * @param propertyToEdit  The property to edit (e.g., "description", "location", "time").
   * @param newValue        The new value for the specified property.
   * @return {@code true} if the event was successfully edited; {@code false} otherwise.
   */
  @Override
  public boolean editRecurringEventValue(String subject, String propertyToEdit, String newValue) {
    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    boolean updated = false;

    for (AbstractEvent event : activeCalendar.events) {
      if (event.subject.equals(subject)) {
        switch (propertyToEdit.toLowerCase()) {
          case "subject":
            event.subject = newValue;
            updated = true;
            break;
          case "description":
            event.description = newValue;
            updated = true;
            break;
          case "location":
            event.location = newValue;
            updated = true;
            break;
          case "time":
            try {
              String input = newValue.trim();
              if (input.contains("T")) {
                input = input.substring(input.indexOf("T") + 1);
              }
              LocalTime newTime = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm"));
              LocalDate eventDate = event.startTime.toLocalDate();
              LocalDateTime newStartTime = LocalDateTime.of(eventDate, newTime);
              long durationMinutes = Duration.between(event.startTime, event.endTime).toMinutes();
              event.startTime = newStartTime;
              event.endTime = newStartTime.plusMinutes(durationMinutes);
              updated = true;
            } catch (Exception e) {
              // Optionally handle parsing error here.
            }
            break;
          case "public":
            event.isPublic = Boolean.parseBoolean(newValue);
            updated = true;
            break;
          default:
            break;
        }
      }
    }
    return updated;
  }

  /**
   * Queries all events occurring on a specific date and time.
   *
   * @param     dateTime The date and time to query for events.
   * @return    A list of string representations of events occurring at that date and time.
   *            Returns an empty list if no events are found.
   */
  @Override
  public List<String> queryEventsByDate(LocalDateTime dateTime) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    List<String> results = new ArrayList<>();
    LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);

    for (AbstractEvent event : activeCalendar.events) {
      if (!event.endTime.isBefore(startOfDay) && !event.startTime.isAfter(endOfDay)) {
        results.add(event.toString());
      }
    }
    return results;
  }

  /**
   * Queries all events occurring in a specific range.
   *
   * @param     startDate The start time of the range.
   * @param     endDate The end time of the range.
   * @return    A list of string representations of events occurring it that range of date and time.
   *            Returns an empty list if no events are found.
   */
  @Override
  public List<String> queryEventsByRange(LocalDateTime startDate, LocalDateTime endDate) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    List<String> results = new ArrayList<>();

    for (AbstractEvent event : activeCalendar.events) {
      if (!event.endTime.isBefore(startDate) && !event.startTime.isAfter(endDate)) {
        results.add(event.toString());
      }
    }
    return results;
  }

  /**
   * Returns a boolean value telling if the time is busy or not.
   *
   * @param dateTime    The time of which availability we want to check.
   * @return @return {@code true} if the event was occurring in that time;
   * {@code false} otherwise.
   */
  @Override
  public boolean isBusy(LocalDateTime dateTime) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    for (AbstractEvent event : activeCalendar.events) {
      if (event.occursOn(dateTime)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Exports all events from the active calendar as CSV-formatted strings,
   * including details like subject, start and end times, description,
   * location, and privacy status.
   *
   * @return A list of CSV-formatted strings representing all events in the active calendar.
   *         Throws an exception if no active calendar is selected.
   */
  public List<String> eventsAsCSV() {
    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    List<String> csvLines = new ArrayList<>();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    csvLines.add("Subject,Start Date,Start Time,End Date,End Time,Description,Location,Private");

    for (AbstractEvent event : activeCalendar.events) {
      String subject = event.subject;
      String startDate = event.startTime.format(dateFormatter);
      String startTime = event.startTime.format(timeFormatter);
      String endDate = event.endTime.format(dateFormatter);
      String endTime = event.endTime.format(timeFormatter);

      String description = event.description;
      String location = event.location;
      String isPrivate = event.isPublic ? "FALSE" : "TRUE";

      csvLines.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
              subject, startDate, startTime, endDate, endTime,
              description, location, isPrivate));
    }

    return csvLines;
  }

  /**
   * Checks whether an event with the given subject is part of a recurring series of events.
   *
   * @param subject     The subject or title of the event to check for recurrence status.
   * @return {@code true} if the specified event is part of a recurring series;
   * {@code false} otherwise.
   */
  public boolean isRecurringEvent(String subject) {

    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    for (AbstractEvent event : activeCalendar.events) {
      if (event.subject.equals(subject) && event.isRecurring()) {
        return true;
      }
    }
    return false;
  }

  public List<String> getCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }
}