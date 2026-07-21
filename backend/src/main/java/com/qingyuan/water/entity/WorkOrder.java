package com.qingyuan.water.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkOrder {
    private Long id;
    private String orderCode;
    private Long stationId;
    private String stationName;
    private Long deviceId;
    private String deviceName;
    private String title;
    private String description;
    private String owner;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
