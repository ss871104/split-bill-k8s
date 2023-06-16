package com.menstalk.notificationservice.repositories;

import com.menstalk.notificationservice.models.Notification;
import com.menstalk.notificationservice.models.NotificationKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends CassandraRepository<Notification, NotificationKey> {
    List<Notification> findAllByNotificationKeyUserId(Long userId);
    @Query("SELECT * FROM notification WHERE userId = ?0 ORDER BY createTime DESC LIMIT 100")
    List<Notification> findByNotificationKeyUserIdLimit30(Long userId);
}
