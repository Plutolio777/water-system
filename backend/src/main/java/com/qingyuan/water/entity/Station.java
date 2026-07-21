package com.qingyuan.water.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Station {
    private Long id;
    private String stationCode;
    private String stationName;
    private String riverName;
    private BigDecimal installedCapacity;
    private Integer unitCount;
    private String status;
    private String manager;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private BigDecimal currentPower;
    private BigDecimal todayGeneration;
    private BigDecimal waterLevel;
    private BigDecimal inflow;
}
