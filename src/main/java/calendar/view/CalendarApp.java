package calendar.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import calendar.model.CalendarManager;
import calendar.model.CalendarInterface;
import calendar.controller.CommandParser;

/**
 * The main entry point for the Calendar application.
 * Supports two modes of operation:
 * - Interactive mode: Allows users to input commands via the console.
 * - Headless mode: Processes commands from a file and executes them sequentially.
 * This class handles command-line arguments to determine the mode of operation.
 * And delegates command parsing and execution to the {CommandParser}.
 */
public class CalendarApp {

  /**
   * The main method of the application. It parses command-line arguments to determine,
   * the mode of operation and invokes the appropriate method to run the application.
   *
   * @param args Command-line arguments. Expected format:
   *             <ul>
   *               <li>--mode interactive</li>
   *               <li>--mode headless filename</li>
   *             </ul>
   */
  public static void main(String[] args) {
    CalendarInterface calendar = new CalendarManager();
    CommandParser parser = new CommandParser(calendar);

    if (args.length < 2) {
      System.out.println("Usage: java calendar.CalendarApp --mode [interactive|headless filename]");
      return;
    }

    if (!args[0].equals("--mode")) {
      System.out.println("Error: First argument must be --mode");
      return;
    }

    String mode = args[1].toLowerCase();

    if (mode.equals("interactive")) {
      runInteractiveMode(parser);
    }
    else if (mode.equals("headless")) {
      if (args.length < 3) {
        System.out.println("Error: Missing filename for headless mode");
        System.out.println("Usage: java calendar.CalendarApp --mode headless filename");
        return;
      }
      runHeadlessMode(parser, args[2]);
    }
    else {
      System.out.println("Error: Mode must be either 'interactive' or 'headless'");
      System.out.println("Usage: java calendar.CalendarApp --mode [interactive|headless filename]");
    }
  }

  /**
   * Runs the application in interactive mode.
   * In this mode, users can type commands directly into the console,
   * and the application will execute them immediately.
   *
   * @param parser The instance used to parse and execute commands.
   */
  private static void runInteractiveMode(CommandParser parser) {
    System.out.println("Calendar Application");
    System.out.println("Type 'exit' to quit");

    try (Scanner scanner = new Scanner(System.in)) {
      while (true) {
        System.out.print("> ");
        String command = scanner.nextLine();

        String result = parser.parseAndExecute(command);
        System.out.println(result);

        if (result.equals("exit")) {
          break;
        }
      }
    }
  }

  /**
   * Runs the application in headless mode.
   * In this mode, commands are read from a file and executed sequentially.
   *
   * @param parser The instance used to parse and execute commands.
   * @param filename The name of the file containing commands to be executed.
   */
  private static void runHeadlessMode(CommandParser parser, String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      int lineNumber = 0;

      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (line.trim().isEmpty()) {
          continue;
        }

        String result = parser.parseAndExecute(line);

        if (result.startsWith("Error:")) {
          System.out.println("Error on line " + lineNumber + ": " + result);
          return;
        }

        if (result.equals("exit")) {
          System.out.println("Exiting after processing " + lineNumber + " lines");
          break;
        }

        System.out.println(result);
      }
      System.out.println("Finished processing file");
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }
}