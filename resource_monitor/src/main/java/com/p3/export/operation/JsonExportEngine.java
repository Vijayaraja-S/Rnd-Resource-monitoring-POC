package com.p3.export.operation;

import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.JsonExportHelper;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.BlobKeySplitBean;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.utility.json.JSONArray;
import com.p3.export.utility.json.JSONException;
import com.p3.export.utility.json.JSONObject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class JsonExportEngine extends BaseExporter implements ExportEngineHandler {

  private JSONArray result;
  private long bytesize;

  JsonExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat) throws Exception {
    super(options, columnsInfo, title, dataformat);
  }

  @Override
  public TextExportHelper getExporter() {
    return exportHelper;
  }

  public void iterateRows(final List<Object> currentRow) throws Exception {
    JSONObject ja = processRow(currentRow);
    result.put(ja);
    bytesize += ja.toString().getBytes().length * 1.8; // 1.8 is approximation size factor
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  private JSONObject processRow(List<Object> currentRow) throws JSONException {
    JSONObject jo = new JSONObject();
    for (ColumnInfo columns : columnsInfo) {
      final Object value = currentRow.get(columns.getOrdinalPosition() - 1);
      if (value == null) {
        jo.put(columns.getColumn(), JSONObject.NULL);
        continue;
      }
      if (value instanceof BlobKeySplitBean) {
        BlobKeySplitBean blobKeySplitBean = (BlobKeySplitBean) value;
        JSONObject blobJo = new JSONObject();
        blobJo.put("file", blobKeySplitBean.getOutputFileName());
        if (blobKeySplitBean.getError() == null) {
          blobJo.put("path", blobKeySplitBean.getRelativePath());
        } else {
          blobJo.put("message", blobKeySplitBean.getError());
        }
        jo.put(columns.getColumn(), blobJo);
      } else {
        jo.put(columns.getColumn(), value);
      }
    }
    return jo;
  }

  @Override
  public void handleDataStart() throws Exception {
    result = new JSONArray();
    exportHelper.append(options.getFileStartAppender());
  }

  @Override
  public void handleDataEnd() throws Exception {
    ((JsonExportHelper) (exportHelper)).write(result);
    exportHelper.flush();
    exportHelper.append(options.getFileEndAppender());
    exportHelper.flush();
    exportHelper.close();
  }

  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {}

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList) throws Exception {

  }

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
