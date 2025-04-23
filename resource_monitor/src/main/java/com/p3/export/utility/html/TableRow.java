package com.p3.export.utility.html;

import com.p3.export.formatter.ExportFormat;
import java.util.ArrayList;
import java.util.List;

public class TableRow {

  private final ExportFormat exportFormat;
  private final List<TableCell> cells;

  public TableRow(final ExportFormat exportFormat) {
    this.exportFormat = exportFormat;
    cells = new ArrayList<>();
  }

  public TableRow add(final TableCell cell) {
    cells.add(cell);
    return this;
  }

  public TableCell firstCell() {
    if (cells.isEmpty()) {
      return null;
    }
    return cells.get(0);
  }

  public TableCell lastCell() {
    if (cells.isEmpty()) {
      return null;
    }
    return cells.get(cells.size() - 1);
  }

  /**
   * Converts the table row to HTML.
   *
   * @return HTML
   */
  @Override
  public String toString() {
    if (exportFormat == ExportFormat.html) {
      return toHtmlString();
    } else {
      return toPlainTextString();
    }
  }

  private String toHtmlString() {
    final StringBuilder buffer = new StringBuilder(1024);
    buffer.append("\t<tr>").append(System.lineSeparator());
    for (final TableCell cell : cells) {
      buffer.append("\t\t").append(cell).append(System.lineSeparator());
    }
    buffer.append("\t</tr>");
    return buffer.toString();
  }

  private String toPlainTextString() {
    final StringBuilder buffer = new StringBuilder(1024);
    for (int i = 0; i < cells.size(); i++) {
      final TableCell cell = cells.get(i);
      if (i > 0) {
        buffer.append(getFieldSeparator());
      }
      buffer.append(cell.toString());
    }
    return buffer.toString();
  }

  private String getFieldSeparator() {
    String fieldSeparator;
    switch (exportFormat) {
      case csv:
        fieldSeparator = ",";
        break;
      case txt:
      case tsv:
        fieldSeparator = "\t";
        break;
      case ssv:
        fieldSeparator = ";";
        break;
      default:
        fieldSeparator = "  ";
    }
    return fieldSeparator;
  }
}
