package com.newpick4u.ainews.ainews.application.dto;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public record ProceedAiNewsDto(
    String originalString,
    ProceedFields proceedFields
) {

  public static ProceedAiNewsDto of(String originalString, ProceedFields proceedFields) {
    return new ProceedAiNewsDto(originalString, proceedFields);
  }

  public List<String> getTagStringListByMaxSize(int maxSize) {
    List<String> tags = this.proceedFields.tags;
    ArrayList<String> processedTagList = new ArrayList<>();
    if (CollectionUtils.isEmpty(tags)) {
      return processedTagList;
    }
    int lastSize = Math.min(tags.size(), maxSize);
    for (int i = 0; i < lastSize; i++) {
      String originalTag = tags.get(i);
      processedTagList.add(StringUtils.trimAllWhitespace(originalTag));
    }

    return processedTagList;
  }

  public record ProceedFields(
      String summary,
      List<String> tags
  ) {

    public String getTagsString(int maxTagCount) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      int count = 0;
      for (String tag : this.tags) {
        if (count >= maxTagCount) {
          break;
        }
        sb.append(StringUtils.trimAllWhitespace(tag));
        sb.append(",");
        count++;
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append("]");
      return sb.toString();
    }
  }
}
