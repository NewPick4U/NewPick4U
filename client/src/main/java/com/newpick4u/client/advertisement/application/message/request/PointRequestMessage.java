package com.newpick4u.client.advertisement.application.message.request;


import java.util.UUID;

public record PointRequestMessage(Long userId, UUID advertisementId, Integer point) {

}
