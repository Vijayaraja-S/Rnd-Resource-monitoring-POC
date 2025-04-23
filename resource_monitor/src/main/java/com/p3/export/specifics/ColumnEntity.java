package com.p3.export.specifics;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ColumnEntity {
  private String name;
  private Integer ordinal;
  private String type;
  private Integer typeLength;
  private Integer scale;
  @Builder.Default private Boolean index = Boolean.FALSE;
  @Builder.Default private Boolean fullText = Boolean.FALSE;
  private String indexing;
  @Builder.Default private Boolean encrypt = Boolean.FALSE;
  @Builder.Default private Boolean isBlob = Boolean.FALSE;
}
