package com.p3.export.iosource;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OutputWriter extends Writer {

  private final String description;
  private final Writer writer;
  private final boolean shouldCloseWriter;
  private boolean isClosed;

  public OutputWriter(
      final String description, final Writer writer, final boolean shouldCloseWriter) {
    this.description = requireNonNull(description, "No description provided");
    this.writer = requireNonNull(writer, "No writer provided");
    this.shouldCloseWriter = shouldCloseWriter;
  }

  @Override
  public String toString() {
    return description;
  }

  @Override
  protected void finalize() throws Throwable {
    if (!isClosed) {
      throw new IllegalStateException(
          String.format("Output writer \"%s\" was not closed", description));
    }
    super.finalize();
  }

  @Override
  public void write(final int c) throws IOException {
    ensureOpen();
    writer.write(c);
  }

  @Override
  public void write(final char[] cbuf) throws IOException {
    ensureOpen();
    writer.write(cbuf);
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) throws IOException {
    ensureOpen();
    writer.write(cbuf, off, len);
  }

  @Override
  public void write(final String str) throws IOException {
    ensureOpen();
    writer.write(str);
  }

  @Override
  public void write(final String str, final int off, final int len) throws IOException {
    ensureOpen();
    writer.write(str, off, len);
  }

  @Override
  public Writer append(final CharSequence csq) throws IOException {
    ensureOpen();
    return writer.append(csq);
  }

  @Override
  public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
    ensureOpen();
    return writer.append(csq, start, end);
  }

  @Override
  public Writer append(final char c) throws IOException {
    ensureOpen();
    return writer.append(c);
  }

  @Override
  public void flush() throws IOException {
    ensureOpen();
    writer.flush();
  }

  @Override
  public void close() throws IOException {
    flush();

    if (shouldCloseWriter) {
      log.info("Closing output writer");
      writer.close();
    } else {
      log.info("Not closing output writer, since output is to an externally provided writer");
    }
    isClosed = true;
  }

  /** Checks to make sure that the stream has not been closed. */
  private void ensureOpen() throws IOException {
    if (isClosed) {
      throw new IOException(String.format("Output writer \"%s\" is not open", description));
    }
  }
}
