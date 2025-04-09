package calendar.service;

import java.util.List;

/**
 * Interface for exporting data to a file.
 */
public interface ExportServiceInterface {

  /**
   * Exports the given data to a file at the specified file path.
   *
   * @param csvLines The data to be written to the file.
   * @param filePath The file path where the data should be saved.
   * @return A string indicating success or failure.
   */
  String exportToCSV(List<String> csvLines, String filePath);
}