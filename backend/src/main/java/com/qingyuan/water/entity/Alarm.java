package com.qingyuan.water.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Alarm {
    private Long id;
    private String alarmCode;
    private Long stationId;
    private String stationName;
    private Long deviceId;
    private String deviceName;
    private String level;
    private String title;
    private String currentValue;
    private String thresholdValue;
    private String status;
    private String handler;
    private LocalDateTime occurredAt;
    private LocalDateTime handledAt;
}
