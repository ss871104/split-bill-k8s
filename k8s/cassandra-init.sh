#!/bin/bash

CQL_COMMANDS=$(cat <<-END
CREATE KEYSPACE IF NOT EXISTS notification_service
    WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
USE notification_service;
CREATE TABLE IF NOT EXISTS notification(
    userId         bigint,
    notificationId uuid,
    title          text,
    content        text,
    isRead         boolean,
    createTime     timestamp,
    PRIMARY KEY (userId, createTime, notificationId)
) WITH CLUSTERING ORDER BY (createTime DESC);
END
)

kubectl exec cassandra-0 --namespace db -- bash -c "echo \"$CQL_COMMANDS\" | cqlsh"
