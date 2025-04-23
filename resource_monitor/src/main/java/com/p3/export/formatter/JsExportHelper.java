package com.p3.export.formatter;

import com.p3.export.exceptions.ExportException;
import com.p3.export.options.Options;
import com.p3.export.utility.json.JSONArray;
import com.p3.export.utility.json.JSONException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class JsExportHelper extends PlainTextExportHelper {

  public JsExportHelper(
      final ExportFormat exportFormat, String title, String outputFolderPath, Options options)
      throws IOException {
    super(exportFormat, title, outputFolderPath, options);
  }

  public JsExportHelper(final PrintWriter out, ExportFormat exportFormat)
      throws FileNotFoundException {
    super(out, exportFormat);
  }

  public void write(final JSONArray jsonArray) throws ExportException {
    try {
      jsonArray.write(out);
    } catch (final JSONException e) {
      throw new ExportException("Could not write database", e);
    }
  }
}
