package com.p3.export.utility.html;

import com.p3.export.formatter.ExportFormat;
import com.p3.export.utility.others.Color;

public class TableHeaderCell extends TableCell {
  public TableHeaderCell(
      final String text,
      final boolean escapeText,
      final int characterWidth,
      final Alignment align,
      final boolean emphasizeText,
      final String styleClass,
      final Color bgColor,
      final int colSpan,
      final ExportFormat exportFormat) {
    super(
        text,
        escapeText,
        characterWidth,
        align,
        emphasizeText,
        styleClass,
        bgColor,
        colSpan,
        exportFormat);
    if (colSpan > 1) {
      addAttribute("colspan", String.valueOf(colSpan));
    }
  }

  @Override
  protected String getTag() {
    return "th";
  }
}
