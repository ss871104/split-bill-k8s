package com.menstalk.batchservice.models;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private NotificationKey notificationKey;
    private String title;
    private String content;
    private boolean isRead;

}
