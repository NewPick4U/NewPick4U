package com.newpick4u.ainews.ainews.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AiExceptionEventHandleService {

  void processAiNews(String originalMessage);

  void saveAndSendTaskByListener(String aiNewsString) throws JsonProcessingException;
}
