package com.qingyuan.water.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Device {
    private Long id;
    private Long stationId;
    private String stationName;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String model;
    private String status;
    private BigDecimal ratedPower;
    private BigDecimal currentPower;
    private Integer runningHours;
    private LocalDate nextMaintenanceDate;
}
