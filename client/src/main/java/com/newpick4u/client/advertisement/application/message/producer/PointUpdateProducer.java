package com.newpick4u.client.advertisement.application.message.producer;

import com.newpick4u.client.advertisement.application.message.request.PointUpdateMessage;

public interface PointUpdateProducer {

  public void produce(PointUpdateMessage message);
}
