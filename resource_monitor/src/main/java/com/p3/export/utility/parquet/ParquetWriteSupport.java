package com.p3.export.utility.parquet;

import static com.p3.export.utility.others.Utility.isBlank;

import com.p3.export.specifics.BlobKeySplitBean;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.io.ParquetEncodingException;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;

@Slf4j
public class ParquetWriteSupport extends WriteSupport<List<Object>> {
  private final String dateFormat;
  MessageType schema;
  RecordConsumer recordConsumer;
  List<ColumnDescriptor> columnDescriptors;

  public ParquetWriteSupport(MessageType schema,String dataformat) {
    this.schema = schema;
    this.columnDescriptors = schema.getColumns();
    this.dateFormat = dataformat;
  }

  @Override
  public WriteContext init(Configuration configuration) {
    return new WriteContext(schema, new HashMap<>());
  }

  @Override
  public void prepareForWrite(RecordConsumer recordConsumer) {
    this.recordConsumer = recordConsumer;
  }

  @Override
  public void write(List<Object> record) {
    recordConsumer.startMessage();
    for (int i = 0; i < columnDescriptors.size(); i++) {
      try {
        if (record.get(i) == null) continue;
        Object value = record.get(i);
        recordConsumer.startField(columnDescriptors.get(i).getPath()[0], i);
        switch (columnDescriptors.get(i).getPrimitiveType().getPrimitiveTypeName()) {
          case BOOLEAN:
            recordConsumer.addBoolean(Boolean.parseBoolean(value.toString()));
            break;
          case FLOAT:
            recordConsumer.addFloat(Float.parseFloat(value.toString()));
            break;
          case DOUBLE:
            recordConsumer.addDouble(Double.parseDouble(value.toString()));
            break;
          case INT32:
            if (columnDescriptors.get(i).getPrimitiveType().getOriginalType() != null
                && columnDescriptors
                    .get(i)
                    .getPrimitiveType()
                    .getOriginalType()
                    .equals(OriginalType.DATE))
              value =
                  value.toString().contains(" ")
                      ? String.valueOf(
                          LocalDate.parse(
                                  value.toString().substring(0, value.toString().indexOf(" ")))
                              .toEpochDay())
                      : String.valueOf(LocalDate.parse(value.toString()).toEpochDay());
            recordConsumer.addInteger(Integer.parseInt(value.toString()));
            break;
          case INT64:
            final ColumnDescriptor columnDescriptor1 = columnDescriptors.get(i);
            final PrimitiveType primitiveType1 = columnDescriptor1.getPrimitiveType();
            final OriginalType originalType1 = primitiveType1.getOriginalType();
            if (originalType1 != null) {
              final ColumnDescriptor columnDescriptor = columnDescriptors.get(i);
              final PrimitiveType primitiveType = columnDescriptor.getPrimitiveType();
              final OriginalType originalType = primitiveType.getOriginalType();
              if (originalType.equals(OriginalType.TIMESTAMP_MILLIS)) {
                value = getTimestampObjectValue(value);
              }
            }
            recordConsumer.addLong(Long.parseLong(value.toString()));
            break;
          case INT96:
            recordConsumer.addLong(Long.parseLong(value.toString()));
            break;
          case BINARY:
          case FIXED_LEN_BYTE_ARRAY:
            if (value instanceof BlobKeySplitBean) {
              final BlobKeySplitBean blobKeySplitbean = (BlobKeySplitBean) value;
              if (isBlank(blobKeySplitbean.getError())) {
                recordConsumer.addBinary(null);
              } else {
                String data =
                    blobKeySplitbean
                            .getRelativePath()
                            .substring(blobKeySplitbean.getRelativePath().lastIndexOf("/") + 1)
                        + "::"
                        + blobKeySplitbean.getOutputFileName();
                recordConsumer.addBinary(Binary.fromString(data));
              }
            } else {
              recordConsumer.addBinary(Binary.fromString(value.toString()));
            }
            break;
          default:
            throw new ParquetEncodingException();
        }
        recordConsumer.endField(columnDescriptors.get(i).getPath()[0], i);
      } catch (Exception e) {
        log.error(
            "Error at column : " + columnDescriptors.get(i) + " . column value : " + record.get(i),
            e);
        throw e;
      }
    }
    recordConsumer.endMessage();
  }
  /**
   * Timestamp value
   *
   * @param value
   * @return
   */
  private Object getTimestampObjectValue(Object value) {
    try {
      SimpleDateFormat df = new SimpleDateFormat(removeMiscCharacters(dateFormat));
      Date parsedDate = df.parse(removeMiscCharacters(value.toString()));
      Timestamp timestamp = new Timestamp(parsedDate.getTime());
      value = String.valueOf(timestamp.getTime());
    } catch (Exception e) {
      throw new InputMismatchException("Date parsing error please check date format: " + value +", parsed value :"+removeMiscCharacters(value.toString()));
    }
    return value;
  }
  public String removeMiscCharacters(String str) {
    return str.replaceAll("T", " ").replace("Z", " ").trim();
  }
}
