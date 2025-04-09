package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a single, non-recurring event in the calendar system.
 * Extends {@link AbstractEvent} to provide functionality specific to single events,
 * such as determining if the event is all-day or falls within a specified date range.
 */
public class SingleEvent extends AbstractEvent {

  /**
   * Constructs a single event with a specific start and end time.
   *
   * @param subject     The subject or title of the event.
   * @param startTime   The start time of the event.
   * @param endTime     The end time of the event.
   * @param description A description of the event.
   * @param location    The location where the event will take place.
   * @param isPublic    Whether the event is public or private.
   */
  public SingleEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                     String description, String location, boolean isPublic) {
    super(subject, startTime, endTime, description, location, isPublic);
  }

  /**
   * Constructs an all-day single event on a specific date.
   *
   * @param subject     The subject or title of the event.
   * @param startTime   The start time (used to determine the date for the all-day event).
   * @param description A description of the event.
   * @param location    The location where the event will take place.
   * @param isPublic    Whether the event is public or private.
   */
  public SingleEvent(String subject, LocalDateTime startTime,
                     String description, String location, boolean isPublic) {
    super(subject, startTime,
            startTime.toLocalDate().atTime(23, 59, 59),
            description, location, isPublic);
  }

  /**
   * Determines whether this single event occurs on a specific date and time.
   *
   * @param dateTime The date and time to check for occurrence.
   * @return {@code true} if this single event occurs at the specified date and time;
   * {@code false} otherwise.
   */
  @Override
  public boolean occursOn(LocalDateTime dateTime) {
    return !dateTime.isBefore(startTime) && !dateTime.isAfter(endTime);
  }

  /**
   * Determines whether this single event falls within a specified date and time range.
   *
   * @param rangeStart The start of the range to check.
   * @param rangeEnd   The end of the range to check.
   * @return {@code true} if this single event falls within the specified range;
   * @code {false} otherwise.
   */
  public boolean fallsWithinRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
    return !endTime.isBefore(rangeStart) && !startTime.isAfter(rangeEnd);
  }

  /**
   * Checks whether this single event spans an entire day (from midnight to 11:59 PM).
   *
   * @return {@code true} if this single event is an all-day event; {@code false} otherwise.
   */
  public boolean isAllDay() {
    return startTime.toLocalTime().getHour() == 0 &&
            startTime.toLocalTime().getMinute() == 0 &&
            endTime.toLocalTime().getHour() == 23 &&
            endTime.toLocalTime().getMinute() == 59;
  }

  @Override
  public boolean isRecurring() {
    return false;
  }
}