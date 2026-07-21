package com.qingyuan.water.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitoringRecord {
    private Long id;
    private Long stationId;
    private Long deviceId;
    private BigDecimal power;
    private BigDecimal voltage;
    private BigDecimal currentValue;
    private BigDecimal flowRate;
    private BigDecimal waterLevel;
    private BigDecimal waterHead;
    private BigDecimal efficiency;
    private BigDecimal bearingTemperature;
    private BigDecimal vibration;
    private LocalDateTime recordedAt;
}
