package com.qingyuan.water.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class WorkOrderRequest {
    @NotNull(message = "请选择站点")
    private Long stationId;
    private Long deviceId;
    @NotBlank(message = "请输入工单标题")
    private String title;
    private String description;
    @NotBlank(message = "请输入负责人")
    private String owner;
    private String priority;
    private LocalDate dueDate;
}
