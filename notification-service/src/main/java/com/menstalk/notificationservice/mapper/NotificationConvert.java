package com.menstalk.notificationservice.mapper;

import com.menstalk.notificationservice.dto.NotificationResponse;
import com.menstalk.notificationservice.models.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationConvert {
    NotificationConvert INSTANCE = Mappers.getMapper(NotificationConvert.class);

    @Mapping(source = "notificationKey.notificationId", target = "notificationId")
    @Mapping(source = "notificationKey.userId", target = "userId")
    @Mapping(source = "notificationKey.createTime", target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    NotificationResponse notificationConvertToNotificationResponse(Notification notification);
}
