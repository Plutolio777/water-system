# 微型水能源数字管理系统

基于 Spring Boot + Vue 3 的微型水电站数字化运营管理平台。

## 技术栈
- 前端：Vue 3 + TypeScript + Vite + ECharts + Pinia
- 后端：Spring Boot 2.7 + MyBatis + JWT
- 数据库：MySQL 8.0

## 快速启动

### 1. 导入数据库
在 MySQL 8.0 中执行 database/water_system.sql

### 2. 启动后端
cd backend
mvn spring-boot:run

后端默认连接 localhost:3306/water_system，账号 root / 123456
如需修改请编辑 backend/src/main/resources/application.yml

### 3. 启动前端
npm install
npm run dev

前端运行在 http://localhost:5173

### 4. 演示账号
- 管理员：admin / admin123
- 值班员：operator / operator123

## 主要功能
- 站点总览：关键指标、实时功率曲线、天气水文、告警摘要
- 数据监测：机组实时数据（功率/流量/水位/转速等）
- 发电分析：日/月/年发电量统计与图表
- 数据报表：月度/年度报表
- 设备管理：设备台账、状态、检修周期
- 告警管理：告警事件列表、确认处置
- 运维管理：工单创建与跟踪
- 流域大屏：全屏实时态势大屏（/big-screen）
