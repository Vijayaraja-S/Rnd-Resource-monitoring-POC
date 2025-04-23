package com.p3.export.specifics;

import java.io.File;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BlobKeySplitBean {
  private String key;
  private String name;
  private String url;
  private String region;
  private String type;
  private String bucketName;
  private String endpoint;
  private String accessKey;
  private String secretKey;

  private UUID tableId;
  private String columnId;
  private String fileId;

  private String relativePath;
  private String srcFilePath;
  private String destFilePath;

  @Builder.Default private String error = null;

  @Builder.Default private String uuid = UUID.randomUUID().toString().substring(0, 4);

  public String getOutputFileName() {
    return uuid + "-" + name;
  }

  public String generateDestFilePath() {
    return getColumnId() + File.separator + getOutputFileName();
  }

  public void generateDestFilePath(String exportPath) {
    destFilePath = exportPath + File.separator + generateDestFilePath();
  }

  public String generateSrcFilePath() {
    return getColumnId() + "/" + fileId;
  }

  public void generateSrcFilePath(String basePath) {
    srcFilePath = basePath + "/" + generateSrcFilePath();
  }

  public void setValues() {
    String[] split = key.split(":");
    tableId = UUID.fromString(split[0]);
    columnId = split[1];
    fileId = split[2];
    generateRelativePath();
  }

  private void generateRelativePath() {
    relativePath =
        "."
            + File.separator
            + "Blobs"
            + File.separator
            + getColumnId()
            + File.separator
            + getOutputFileName();
  }

  public void generatePathValues(String baseLocation, String exportPath) {
    generateSrcFilePath(baseLocation);
    generateDestFilePath(exportPath);
  }
}
