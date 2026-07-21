package com.qingyuan.water.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GenerationRecord {
    private Long id;
    private Long stationId;
    private String stationName;
    private LocalDate recordDate;
    private BigDecimal generation;
    private BigDecimal planGeneration;
    private BigDecimal rainfall;
    private BigDecimal onGridEnergy;
}
