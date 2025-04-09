package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single calendar containing events.
 */
public class Calendar {

  String name;
  ZoneId timezone;
  List<AbstractEvent> events;

  /**
   * Constructs a new Calendar with the given unique name.
   *
   * @param name    The unique name of this calendar.
   */
  public Calendar(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.events = new ArrayList<>();
  }

  /**
   * Renames this calendar to a new unique name.
   *
   * @param newName The new unique name for this calendar.
   */
  public void rename(String newName) {
    this.name = newName;
  }

  /**
   * Adds an event to this calendar.
   *
   * @param event The event to add.
   */
  public void addEvent(AbstractEvent event) {
    events.add(event);
  }

  /**
   * Checks whether the given event conflicts with any existing events in the calendar.
   * A conflict occurs if the new event's time overlaps with an existing event's time.
   *
   * @param newEvent    The event to check for conflicts. Must not be {@code null}.
   * @return {@code true} if there is a conflict with an existing event; {@code false} otherwise.
   */
  public boolean hasConflict(AbstractEvent newEvent) {
    for (AbstractEvent existingEvent : events) {
      if (existingEvent.conflictsWith(newEvent)) {
        return true;
      }
    }
    return false;
  }
}