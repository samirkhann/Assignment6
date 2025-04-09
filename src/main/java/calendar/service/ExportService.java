package calendar.service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Service class responsible for exporting data to a CSV file.
 */
public class ExportService implements ExportServiceInterface {

  /**
   * Exports the given data to a CSV file at the specified file path.
   *
   * @param csvLines    The data to be written to the CSV file.
   * @param filePath    The file path where the CSV file should be saved.
   * @return A string indicating success or failure.
   */
  public String exportToCSV(List<String> csvLines, String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
      for (String line : csvLines) {
        writer.write(line + "\n");
      }
      return "Calendar exported successfully to " + filePath;
    } catch (IOException e) {
      return "Error: Failed to export calendar due to I/O error.";
    }
  }
}