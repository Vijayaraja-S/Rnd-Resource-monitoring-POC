package com.p3.export.operation;

import com.p3.export.formatter.BaseExporter;
import com.p3.export.formatter.ExportFormat;
import com.p3.export.formatter.JsExportHelper;
import com.p3.export.formatter.TextExportHelper;
import com.p3.export.options.ColumnInfo;
import com.p3.export.options.Options;
import com.p3.export.specifics.BlobKeySplitBean;
import com.p3.export.specifics.ColumnEntity;
import com.p3.export.utility.json.JSONArray;
import com.p3.export.utility.json.JSONObject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class JsExportEngine extends BaseExporter implements ExportEngineHandler {

  private JSONArray headerResult;
  private JSONArray dataResult;
  private long bytesize;

  JsExportEngine(Options options, String title, List<ColumnInfo> columnsInfo, String dataformat) throws Exception {
    super(options, columnsInfo, title,dataformat);

  }

  @Override
  public TextExportHelper getExporter() {
    return exportHelper;
  }

  public void iterateRows(final List<Object> currentRow) throws Exception {
    JSONArray ja = processRow(currentRow);
    dataResult.put(ja);
    bytesize += ja.toString().getBytes().length * 1.8; // 1.8 is approximation size factor
    exportHelper.flush();
    options.incrementRecordProcessed();
    generateProgressReport(title);
  }

  private JSONArray processRow(List<Object> currentRow) {
    JSONArray ja = new JSONArray();
    for (Object data : currentRow) {
      if (data == null) {
        ja.put(JSONObject.NULL);
        continue;
      }
      if (data instanceof BlobKeySplitBean) {
        BlobKeySplitBean blobKeySplitBean = (BlobKeySplitBean) data;
        final String fileName = blobKeySplitBean.getOutputFileName();
        try {
          if (options.getExportFormat() == ExportFormat.dynamic_export_html
              || options.getExportFormat() == ExportFormat.dynamic_export_html_blob_only) {
            if (blobKeySplitBean.getError() == null) {
              ja.put(
                  "<a target='_blank' href='"
                      + blobKeySplitBean.getRelativePath()
                      + "'>"
                      + fileName
                      + "</a>");
            } else {
              ja.put(
                  "<span class='blob-error-text'>"
                      + fileName
                      + "</span> <span class='cursor-pointer' title='"
                      + blobKeySplitBean.getError()
                      + "'>⚠️</span>");
            }
          } else {
            JSONObject blobJo = new JSONObject();
            blobJo.put("file", fileName);
            if (blobKeySplitBean.getError() == null) {
              blobJo.put("path", blobKeySplitBean.getRelativePath());
            } else {
              blobJo.put("message", blobKeySplitBean.getError());
            }
            ja.put(blobJo);
          }
        } catch (Exception e) {
          ja.put(fileName);
        }
      } else {
        ja.put(data);
      }
    }
    return ja;
  }

  private JSONArray processHeader(List<ColumnInfo> columnInfos) {
    JSONArray ja = new JSONArray();
    for (ColumnInfo columnInfo : columnInfos) {
      ja.put(columnInfo.getColumn());
    }
    return ja;
  }

  @Override
  public void handleDataStart() throws Exception {
    exportHelper.append(options.getFileStartAppender());
    writeHeaderOutput();
    initiateDateResult();
  }

  private void initiateDateResult() throws Exception {
    dataResult = new JSONArray();
    exportHelper.append("searchResultExport = ");
    exportHelper.flush();
  }

  private void writeHeaderOutput() throws Exception {
    headerResult = processHeader(columnsInfo);
    exportHelper.append("searchResultHeader = ");
    exportHelper.flush();
    ((JsExportHelper) (exportHelper)).write(headerResult);
    exportHelper.flush();
    exportHelper.append(";\n\n");
    exportHelper.flush();
  }

  @Override
  public void handleDataEnd() throws Exception {
    ((JsExportHelper) (exportHelper)).write(dataResult);
    exportHelper.flush();
    exportHelper.append(";");
    exportHelper.flush();
    exportHelper.append(options.getFileEndAppender());
    exportHelper.flush();
    exportHelper.close();
  }
  @Override
  public void handleDataEnd(Object reportDetails) throws Exception {
    ((JsExportHelper) (exportHelper)).write(dataResult);
    exportHelper.flush();
    exportHelper.append(";");
    exportHelper.flush();
    exportHelper.append(options.getFileEndAppender());
    exportHelper.flush();
    exportHelper.append(reportDetails.toString());
    exportHelper.flush();
    exportHelper.close();
  }

  @Override
  public void iterateRows(List<Object> values, List<String> attachementList) throws Exception {

  }
  @Override
  public void iterateRows(List<Object> values, List<String> attachementList, List<ColumnEntity> columnMetadata) throws Exception {

  }
}
