package com.newpick4u.ainews.ainews.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.newpick4u.ainews.ainews.application.dto.ProceedAiNewsDto;

public interface AiClient {

  void initRemainAvailableRequestCountPerMin();

  ProceedAiNewsDto processByAiApi(String newsBody) throws JsonProcessingException;
}
