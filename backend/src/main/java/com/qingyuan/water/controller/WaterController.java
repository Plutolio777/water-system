package com.qingyuan.water.controller;

import com.qingyuan.water.common.ApiResponse;
import com.qingyuan.water.dto.WorkOrderRequest;
import com.qingyuan.water.entity.*;
import com.qingyuan.water.mapper.WaterMapper;
import com.qingyuan.water.service.WaterService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
public class WaterController {
    private final WaterService waterService;
    private final WaterMapper waterMapper;

    public WaterController(WaterService waterService, WaterMapper waterMapper) {
        this.waterService = waterService;
        this.waterMapper = waterMapper;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() { return ApiResponse.ok("running"); }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() { return ApiResponse.ok(waterService.overview()); }

    @GetMapping("/stations")
    public ApiResponse<List<Station>> stations() { return ApiResponse.ok(waterMapper.findStations()); }

    @GetMapping("/monitoring")
    public ApiResponse<Map<String, Object>> monitoring(@RequestParam(required = false) Long stationId) {
        return ApiResponse.ok(waterService.monitoring(stationId));
    }

    @GetMapping("/generation")
    public ApiResponse<Map<String, Object>> generation(
            @RequestParam(required = false, defaultValue = "DAY") String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long stationId) {
        return ApiResponse.ok(waterService.generation(period, year, stationId));
    }

    @GetMapping("/reports")
    public ApiResponse<Map<String, Object>> reports() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("monthly", waterMapper.findMonthlyGeneration());
        result.put("ranking", waterMapper.findStationRanking());
        return ApiResponse.ok(result);
    }

    @GetMapping("/devices")
    public ApiResponse<List<Device>> devices(@RequestParam(required = false) Long stationId) {
        return ApiResponse.ok(waterMapper.findDevices(stationId));
    }

    @GetMapping("/alarms")
    public ApiResponse<List<Alarm>> alarms() { return ApiResponse.ok(waterMapper.findAlarms()); }

    @PatchMapping("/alarms/{id}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long id, HttpServletRequest request) {
        waterMapper.resolveAlarm(id, String.valueOf(request.getAttribute("realName")));
        return ApiResponse.ok("告警已确认", null);
    }

    @GetMapping("/work-orders")
    public ApiResponse<List<WorkOrder>> workOrders() { return ApiResponse.ok(waterMapper.findWorkOrders()); }

    @PostMapping("/work-orders")
    public ApiResponse<WorkOrder> createWorkOrder(@Valid @RequestBody WorkOrderRequest request) {
        return ApiResponse.ok("工单创建成功", waterService.createWorkOrder(request));
    }
}
