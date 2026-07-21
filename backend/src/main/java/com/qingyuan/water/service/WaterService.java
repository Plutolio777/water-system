package com.qingyuan.water.service;

import com.qingyuan.water.dto.WorkOrderRequest;
import com.qingyuan.water.entity.*;
import com.qingyuan.water.mapper.WaterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WaterService {
    private final WaterMapper waterMapper;

    public WaterService(WaterMapper waterMapper) {
        this.waterMapper = waterMapper;
    }

    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("metrics", waterMapper.findOverviewMetrics());
        result.put("stations", waterMapper.findStations());
        result.put("series", aggregateMonitoring(waterMapper.findMonitoring(null)));
        List<Alarm> alarms = waterMapper.findAlarms();
        result.put("alarms", alarms.subList(0, Math.min(4, alarms.size())));
        Map<String, Object> weather = new HashMap<String, Object>();
        weather.put("condition", "多云转阵雨");
        weather.put("temperature", 26);
        weather.put("rainfall", 12.4);
        weather.put("inflowForecast", 91.6);
        result.put("weather", weather);
        return result;
    }

    public Map<String, Object> monitoring(Long stationId) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Device> devices = waterMapper.findDevices(stationId);
        List<MonitoringRecord> records = waterMapper.findMonitoring(stationId);
        List<Map<String, Object>> readings = new ArrayList<Map<String, Object>>();
        for (Device device : devices) {
            MonitoringRecord latest = null;
            for (MonitoringRecord record : records) {
                if (device.getId().equals(record.getDeviceId())) latest = record;
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("device", device);
            row.put("latest", latest);
            readings.add(row);
        }
        result.put("readings", readings);
        result.put("series", aggregateMonitoring(records));
        return result;
    }

    private List<Map<String, Object>> aggregateMonitoring(List<MonitoringRecord> records) {
        Map<String, List<MonitoringRecord>> grouped = new LinkedHashMap<String, List<MonitoringRecord>>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (MonitoringRecord record : records) {
            String key = record.getRecordedAt().format(formatter);
            if (!grouped.containsKey(key)) grouped.put(key, new ArrayList<MonitoringRecord>());
            grouped.get(key).add(record);
        }
        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, List<MonitoringRecord>> entry : grouped.entrySet()) {
            BigDecimal power = BigDecimal.ZERO, flow = BigDecimal.ZERO;
            for (MonitoringRecord item : entry.getValue()) {
                power = power.add(value(item.getPower()));
                flow = flow.add(value(item.getFlowRate()));
            }
            int size = entry.getValue().size();
            Map<String, Object> point = new LinkedHashMap<String, Object>();
            point.put("time", entry.getKey());
            point.put("power", power.setScale(2, BigDecimal.ROUND_HALF_UP));
            point.put("flow", flow.divide(new BigDecimal(size), 2, BigDecimal.ROUND_HALF_UP));
            series.add(point);
        }
        return series;
    }

    private BigDecimal value(BigDecimal val) { return val == null ? BigDecimal.ZERO : val; }

    /**
     * 发电量统计 — 统一返回 { list: [...], metrics: {...} }
     * list 中每条记录包含 period, stationName, generation, planGeneration, rainfall, avgPower, runningHours
     */
    public Map<String, Object> generation(String period, Integer year, Long stationId) {
        // 查最近365天原始记录
        List<GenerationRecord> source = waterMapper.findGeneration(365);

        // 按 period 分组聚合
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
        for (GenerationRecord r : source) {
            if (stationId != null && !stationId.equals(r.getStationId())) continue;
            String key;
            if ("MONTH".equals(period)) {
                key = r.getRecordDate().toString().substring(0, 7); // yyyy-MM
            } else if ("YEAR".equals(period)) {
                key = r.getRecordDate().toString().substring(0, 4); // yyyy
            } else {
                key = r.getRecordDate().toString(); // yyyy-MM-dd
            }
            if (year != null && !"YEAR".equals(period)) {
                if (!r.getRecordDate().toString().startsWith(String.valueOf(year))) continue;
            }
            if (!grouped.containsKey(key)) {
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("period", key);
                row.put("stationName", stationId != null ? r.getStationName() : "全站");
                row.put("generation", BigDecimal.ZERO);
                row.put("planGeneration", BigDecimal.ZERO);
                row.put("rainfall", BigDecimal.ZERO);
                grouped.put(key, row);
            }
            Map<String, Object> row = grouped.get(key);
            row.put("generation", ((BigDecimal) row.get("generation")).add(value(r.getGeneration())));
            row.put("planGeneration", ((BigDecimal) row.get("planGeneration")).add(value(r.getPlanGeneration())));
            BigDecimal rf = (BigDecimal) row.get("rainfall");
            if (r.getRainfall() != null && r.getRainfall().compareTo(rf) > 0) row.put("rainfall", r.getRainfall());
        }

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(grouped.values());
        // 补充 avgPower (generation/runningHours estimation) 和 runningHours
        for (Map<String, Object> row : list) {
            BigDecimal gen = (BigDecimal) row.get("generation");
            // 假设每天/月/年对应运行小时数
            int hours = "MONTH".equals(period) ? 720 : "YEAR".equals(period) ? 8760 : 24;
            BigDecimal avg = gen.divide(new BigDecimal(hours), 2, BigDecimal.ROUND_HALF_UP);
            row.put("avgPower", avg);
            row.put("runningHours", hours);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("list", list);
        result.put("metrics", waterMapper.findOverviewMetrics());
        return result;
    }

    @Transactional
    public WorkOrder createWorkOrder(WorkOrderRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderCode("WO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        workOrder.setStationId(request.getStationId());
        workOrder.setDeviceId(request.getDeviceId());
        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setOwner(request.getOwner());
        workOrder.setPriority(request.getPriority());
        workOrder.setStatus("PENDING");
        workOrder.setDueDate(request.getDueDate());
        waterMapper.insertWorkOrder(workOrder);
        return workOrder;
    }
}
