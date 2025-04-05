package com.newpick4u.newsorigin.newsorigin.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.newpick4u.newsorigin.newsorigin.application.dto.NewNewsOriginDto;
import java.util.ArrayList;

public interface OriginCollectClient {

  ArrayList<NewNewsOriginDto> getOriginNewsList() throws JsonProcessingException;
}
