package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recurring event in the calendar system.
 * A recurring event can repeat on specific days of the week, for a fixed number of occurrences,
 * or until a specified date.
 * This class extends {@link AbstractEvent} and provides additional functionality,
 * for managing recurrence patterns.
 */
public class RecurringEvent extends AbstractEvent {

  List<DayOfWeek> recurrencePattern;
  int occurrences;
  LocalDateTime untilDate;

  /**
   * Constructs a recurring event with a recurrence pattern, maximum occurrences,
   * and an optional end date.
   *
   * @param subject          The subject or title of the event.
   * @param startTime        The start time of the first occurrence of the event.
   * @param endTime          The end time of the first occurrence of the event.
   * @param description      A description of the event.
   * @param location         The location where the event will take place.
   * @param isPublic         Whether the event is public or private.
   * @param recurrencePattern A list of {@link DayOfWeek} specifying the days,
   *                          on which the event recurs.
   * @param occurrences      The maximum number of occurrences for the event.
   *                         Use -1 for unlimited occurrences.
   * @param untilDate        The date until which the event should recur. Can be {@code null}.
   */
  public RecurringEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                        String description, String location, boolean isPublic,
                        List<DayOfWeek> recurrencePattern, int occurrences,
                        LocalDateTime untilDate) {
    super(subject, startTime, endTime, description, location, isPublic);
    this.recurrencePattern = recurrencePattern;
    this.occurrences = occurrences;
    this.untilDate = untilDate;
  }

  /**
   * Constructs a recurring event with a recurrence pattern and maximum occurrences.
   *
   * @param subject          The subject or title of the event.
   * @param startTime        The start time of the first occurrence of the event.
   * @param endTime          The end time of the first occurrence of the event.
   * @param description      A description of the event.
   * @param location         The location where the event will take place.
   * @param isPublic         Whether the event is public or private.
   * @param recurrencePattern A list of {@link DayOfWeek} specifying,
   *                         the days on which the event recurs.
   * @param occurrences      The maximum number of occurrences for the event.
   *                         Use -1 for unlimited occurrences.
   */
  public RecurringEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                        String description, String location, boolean isPublic,
                        List<DayOfWeek> recurrencePattern, int occurrences) {
    super(subject, startTime, endTime, description, location, isPublic);
    this.recurrencePattern = recurrencePattern;
    this.occurrences = occurrences;
    this.untilDate = null;
  }

  /**
   * Constructs a recurring event with a recurrence pattern and an end date.
   *
   * @param subject          The subject or title of the event.
   * @param startTime        The start time of the first occurrence of the event.
   * @param endTime          The end time of the first occurrence of the event.
   * @param description      A description of the event.
   * @param location         The location where the event will take place.
   * @param isPublic         Whether the event is public or private.
   * @param recurrencePattern A list of {@link DayOfWeek} specifying,
   *                         the days on which the event recurs.
   * @param untilDate        The date until which the event should recur. Cannot be {@code null}.
   */
  public RecurringEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                        String description, String location, boolean isPublic,
                        List<DayOfWeek> recurrencePattern, LocalDateTime untilDate) {
    super(subject, startTime, endTime, description, location, isPublic);
    this.recurrencePattern = recurrencePattern;
    this.occurrences = -1;
    this.untilDate = untilDate;
  }

  /**
   * Checks whether this recurring event occurs on a specific date and time.
   *
   * @param dateTime The date and time to check for occurrence.
   * @return {@code true} if this recurring event occurs at that date and time;
   * {@code false} otherwise.
   */
  @Override
  public boolean occursOn(LocalDateTime dateTime) {
    if (dateTime.isBefore(startTime)) {
      return false;
    }

    return !dateTime.isAfter(endTime);
  }

  /**
   * Generates all individual occurrences of this recurring event based on its recurrence pattern,
   * maximum number of occurrences, and/or end date.
   *
   * @return    A list of {@link AbstractEvent} instances representing,
   *            each occurrence of this recurring event.
   */
  public List<AbstractEvent> generateOccurrences() {
    List<AbstractEvent> events = new ArrayList<>();

    LocalDateTime currentDate = startTime;
    int generatedCount = 0;

    while (true) {
      if (occurrences >= 0 && generatedCount >= occurrences) {
        break;
      }

      if (untilDate != null && currentDate.isAfter(untilDate)) {
        break;
      }

      if (recurrencePattern.contains(currentDate.getDayOfWeek())) {
        AbstractEvent event = new RecurringEvent(
                subject,
                currentDate,
                currentDate.plusHours(ChronoUnit.HOURS.between(startTime, endTime)),
                description,
                location,
                isPublic,
                recurrencePattern,
                -1,
                untilDate
        );

        events.add(event);
        generatedCount++;
      }

      currentDate = currentDate.plusDays(1);
    }
    return events;
  }

  @Override
  public boolean isRecurring() {
    return true;
  }
}