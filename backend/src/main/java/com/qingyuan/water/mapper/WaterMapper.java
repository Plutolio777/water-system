package com.qingyuan.water.mapper;

import com.qingyuan.water.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface WaterMapper {
    List<Station> findStations();
    List<Device> findDevices(@Param("stationId") Long stationId);
    List<MonitoringRecord> findMonitoring(@Param("stationId") Long stationId);
    List<GenerationRecord> findGeneration(@Param("days") Integer days);
    List<Map<String, Object>> findMonthlyGeneration();
    List<Map<String, Object>> findStationRanking();
    List<Alarm> findAlarms();
    int resolveAlarm(@Param("id") Long id, @Param("handler") String handler);
    List<WorkOrder> findWorkOrders();
    int insertWorkOrder(WorkOrder workOrder);
    Map<String, Object> findOverviewMetrics();
}
