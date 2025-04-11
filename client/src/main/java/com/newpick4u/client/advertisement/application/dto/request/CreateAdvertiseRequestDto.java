package com.newpick4u.client.advertisement.application.dto.request;

import com.newpick4u.client.advertisement.domain.entity.Advertisement.AdvertisementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAdvertiseRequestDto(@NotNull UUID clientId,
                                        @NotNull UUID newsId,
                                        @NotNull @Size(min = 4, max = 50) String title,
                                        @NotNull @Size(min = 4, max = 255) String content,
                                        @NotNull AdvertisementType type,
                                        @NotNull @Size(min = 4, max = 255) String url,
                                        @NotNull Long budget,
                                        @NotNull @Size(min = 1, max = 50) Integer maxPointGrantCount) {

}
