package com.p3.export.utility.html;

/** XML escapes entities. */
public final class Entities {

  /**
   * XML escapes the characters in some text.
   *
   * @param text Text to escape.
   * @return XML-escaped text
   */
  public static String escapeForXMLAttribute(final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (int i = 0; i < text.length(); ++i) {
      final char ch = text.charAt(i);
      switch (ch) {
        case 34:
          buffer.append("&quot;");
          break;
        case 62:
          buffer.append("&gt;");
          break;
        case 38:
          buffer.append("&amp;");
          break;
        case 60:
          buffer.append("&lt;");
          break;
        case 39:
          buffer.append("&apos;");
          break;
        default:
          buffer.append(ch);
          break;
      }
    }
    return buffer.toString();
  }

  /**
   * XML escapes the characters in some text.
   *
   * @param text Text to escape.
   * @return XML-escaped text
   */
  public static String escapeForXMLElement(final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (int i = 0; i < text.length(); ++i) {
      final char ch = text.charAt(i);
      switch (ch) {
        case 62:
          buffer.append("&gt;");
          break;
        case 38:
          buffer.append("&amp;");
          break;
        case 60:
          buffer.append("&lt;");
          break;
        default:
          buffer.append(ch);
          break;
      }
    }
    return buffer.toString();
  }

  public static String escapeForLineBreaks(final String text) {
    if (text.equals("\n")) {
      return text;
    }
    if (text.contains("\n")) {
      return text.replace("\n", "<br/>");
    }
    return text;
  }
}
