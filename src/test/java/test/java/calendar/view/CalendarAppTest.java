package calendar.view;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the {@link CalendarApp} class.
 * Verifies that the application behaves as expected in both modes:
 * - Interactive mode: Commands are entered via the console.
 * - Headless mode: Commands are read from a file and executed sequentially.
 * This test class ensures that:
 * - Command-line arguments are parsed correctly.
 * - The application can switch between interactive and headless modes seamlessly.
 * - Errors (e.g., invalid arguments or file not found) are handled gracefully.
 */
public class CalendarAppTest {

  @Test
  public void testMissingArguments() {
    String[] args = {};
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertEquals("Usage: java calendar.CalendarApp " +
            "--mode [interactive|headless filename]", output);
  }

  @Test
  public void testInvalidFirstArgument() {
    String[] args = {"--invalidMode", "interactive"};
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertEquals("Error: First argument must be --mode", output);
  }

  @Test
  public void testMissingMode() {
    String[] args = {"--mode"};
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertEquals("Usage: java calendar.CalendarApp " +
            "--mode [interactive|headless filename]", output);
  }

  @Test
  public void testInvalidMode() {
    String[] args = {"--mode", "invalidMode"};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("Error: Mode must be either 'interactive' or 'headless'"));
    assertTrue(output.contains("Usage: java calendar.CalendarApp " +
            "--mode [interactive|headless filename]"));
  }

  @Test
  public void testInteractiveMode() {
    String[] args = {"--mode", "interactive"};

    InputStream input = new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(input);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("Calendar Application"));
  }

  @Test
  public void testHeadlessModeMissingFilename() {
    String[] args = {"--mode", "headless"};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("Error: Missing filename for headless mode"));
    assertTrue(output.contains("Usage: java calendar.CalendarApp --mode headless filename"));
  }


  @Test
  public void testHeadlessModeValidFilename() throws IOException {
    File tempFile = File.createTempFile("testFile", ".txt");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
      writer.write("create calendar --name Work --timezone America/New_York\n");
      writer.write("use calendar --name Work\n");
      writer.write("create event Meeting from 2025-03-10T10:00 to 2025-03-10T11:00\n");
      writer.write("exit\n");
      writer.flush();
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();

    assertTrue(output.contains("Event created successfully"));
  }

  @Test
  public void testHeadlessModeInvalidCommandInFile() throws IOException {
    File tempFile = File.createTempFile("testFile", ".txt");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
      writer.write("invalid command\n");
      writer.flush();
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();

    assertTrue(output.contains("Error on line 1"));
  }

  @Test
  public void testHeadlessModeNonexistentFile() {
    String[] args = {"--mode", "headless", "nonexistent.txt"};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();

    assertTrue(output.contains("Error reading file"));
  }

  @Test
  public void testInteractiveModeWelcomeMessage() {
    String[] args = {"--mode", "interactive"};

    InputStream input = new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(input);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("Calendar Application"));
    assertTrue(output.contains("Type 'exit' to quit"));
  }

  @Test
  public void testInteractiveModePrompt() {
    String[] args = {"--mode", "interactive"};

    InputStream input = new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(input);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("> "));
  }

  @Test
  public void testHeadlessModeValidFile() throws IOException {
    File tempFile = File.createTempFile("testFile", ".txt");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

      writer.write("create calendar --name Work --timezone America/New_York\n");
      writer.write("use calendar --name Work\n");
      writer.write("create event Meeting from 2025-03-10T10:00 to 2025-03-10T11:00\n");
      writer.write("exit\n");
      writer.flush();
    }

    String[] args = {"--mode", "headless", tempFile.getAbsolutePath()};

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();

    assertTrue(output.contains("Event created successfully"));
  }

  @Test
  public void testInteractiveModeScannerClose() {
    String[] args = {"--mode", "interactive"};

    InputStream input = new ByteArrayInputStream("exit\n".getBytes());
    System.setIn(input);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    CalendarApp.main(args);

    String output = outputStream.toString().trim();
    assertTrue(output.contains("Calendar Application"));
    assertTrue(output.contains("Type 'exit' to quit"));
    assertTrue(output.contains("exit"));
  }
}