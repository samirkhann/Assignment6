package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a generic event in the calendar.
 * This abstract class serves as a base for different types of events, providing common properties,
 * and methods such as conflict checking and string representation.
 */
public abstract class AbstractEvent {

  String subject;
  LocalDateTime startTime;
  LocalDateTime endTime;
  String description;
  String location;
  boolean isPublic;

  /**
   * Constructs an instance of an event with the specified details.
   *
   * @param subject     The subject or title of the event.
   * @param startTime   The start time of the event.
   * @param endTime     The end time of the event.
   * @param description A description of the event (optional, defaults to an empty string if null).
   * @param location    The location of the event (optional, defaults to an empty string if null).
   * @param isPublic    Whether the event is public or private.
   */
  public AbstractEvent(String subject, LocalDateTime startTime, LocalDateTime endTime,
                       String description, String location, boolean isPublic) {
    this.subject = subject;
    this.startTime = startTime;
    this.endTime = endTime;
    this.description = description != null ? description : "";
    this.location = location != null ? location : "";
    this.isPublic = isPublic;
  }

  /**
   * Checks if this event conflicts with another event.
   * Two events conflict if their time periods overlap.
   *
   * @param other The other event to check for a conflict with.
   * @return {@code true} if there is a conflict; {@code false} otherwise.
   */
  public boolean conflictsWith(AbstractEvent other) {
    return !(this.endTime.isBefore(other.startTime) ||
            this.startTime.isAfter(other.endTime));
  }

  /**
   * Determines whether this event occurs on a specific date.
   * This method must be implemented by subclasses to define how they handle date occurrences.
   *
   * @param date The date to check for occurrence.
   * @return {@code true} if the event occurs on the given date; {@code false} otherwise.
   */
  public abstract boolean occursOn(LocalDateTime date);

  /**
   * Returns a string representation of the event, including its subject, time range,
   * location (if provided), description (if provided), and visibility status (public or private).
   *
   * @return A formatted string describing the event.
   */
  @Override
  public String toString() {
    return subject + " (" + startTime + " - " + endTime + ")" +
            (location != null && !location.isEmpty() ? " at " + location : "") +
            (description != null && !description.isEmpty() ? ", Info: " + description : "") +
            (isPublic ? " (public)" : " (private)");
  }

  /**
   * Determines if this event is recurring.
   *
   * @return {@code true} if this event is recurring; {@code false} otherwise.
   */
  public abstract boolean isRecurring();
}