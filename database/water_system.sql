-- 微型水能源数字管理系统 MySQL 8.0 初始化脚本
-- 默认账号：admin / admin123，值班员：operator / operator123
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS water_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE water_system;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS work_order;
DROP TABLE IF EXISTS alarm;
DROP TABLE IF EXISTS generation_record;
DROP TABLE IF EXISTS monitoring_record;
DROP TABLE IF EXISTS device;
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(40) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(40) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'OPERATOR',
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE station (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  station_code VARCHAR(30) NOT NULL UNIQUE,
  station_name VARCHAR(80) NOT NULL,
  river_name VARCHAR(80),
  installed_capacity DECIMAL(10,2) NOT NULL,
  unit_count INT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
  manager VARCHAR(40),
  longitude DECIMAL(10,6),
  latitude DECIMAL(10,6),
  address VARCHAR(200),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  station_id BIGINT NOT NULL,
  device_code VARCHAR(30) NOT NULL UNIQUE,
  device_name VARCHAR(100) NOT NULL,
  device_type VARCHAR(30) NOT NULL,
  model VARCHAR(80),
  status VARCHAR(20) NOT NULL,
  rated_power DECIMAL(10,2) DEFAULT 0,
  current_power DECIMAL(10,2) DEFAULT 0,
  running_hours INT DEFAULT 0,
  next_maintenance_date DATE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_device_station FOREIGN KEY (station_id) REFERENCES station(id)
) ENGINE=InnoDB;

CREATE TABLE monitoring_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  station_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  power DECIMAL(10,2),
  voltage DECIMAL(10,2),
  current_value DECIMAL(10,2),
  flow_rate DECIMAL(10,2),
  water_level DECIMAL(10,2),
  water_head DECIMAL(10,2),
  efficiency DECIMAL(6,2),
  bearing_temperature DECIMAL(6,2),
  vibration DECIMAL(6,2),
  recorded_at DATETIME NOT NULL,
  INDEX idx_monitor_station_time(station_id, recorded_at),
  CONSTRAINT fk_monitor_station FOREIGN KEY (station_id) REFERENCES station(id),
  CONSTRAINT fk_monitor_device FOREIGN KEY (device_id) REFERENCES device(id)
) ENGINE=InnoDB;

CREATE TABLE generation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  station_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  generation DECIMAL(12,2) NOT NULL,
  plan_generation DECIMAL(12,2) NOT NULL,
  rainfall DECIMAL(8,2) DEFAULT 0,
  on_grid_energy DECIMAL(12,2) DEFAULT 0,
  UNIQUE KEY uk_station_date(station_id, record_date),
  CONSTRAINT fk_generation_station FOREIGN KEY (station_id) REFERENCES station(id)
) ENGINE=InnoDB;

CREATE TABLE alarm (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  alarm_code VARCHAR(40) NOT NULL UNIQUE,
  station_id BIGINT NOT NULL,
  device_id BIGINT,
  level VARCHAR(20) NOT NULL,
  title VARCHAR(120) NOT NULL,
  current_value VARCHAR(40),
  threshold_value VARCHAR(40),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  handler VARCHAR(40),
  occurred_at DATETIME NOT NULL,
  handled_at DATETIME,
  CONSTRAINT fk_alarm_station FOREIGN KEY (station_id) REFERENCES station(id),
  CONSTRAINT fk_alarm_device FOREIGN KEY (device_id) REFERENCES device(id)
) ENGINE=InnoDB;

CREATE TABLE work_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_code VARCHAR(40) NOT NULL UNIQUE,
  station_id BIGINT NOT NULL,
  device_id BIGINT,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  owner VARCHAR(40) NOT NULL,
  priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  due_date DATE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_station FOREIGN KEY (station_id) REFERENCES station(id),
  CONSTRAINT fk_order_device FOREIGN KEY (device_id) REFERENCES device(id)
) ENGINE=InnoDB;

INSERT INTO sys_user(username,password,real_name,phone,role) VALUES
('admin','$2b$10$YSFcYlFlsR3YN6H7Ijh0Y.eW2WedpiKRAcLohC/e8jdIChirMrwDm','系统管理员','13800000000','ADMIN'),
('operator','$2b$10$dAqSIwHUGZsI64WCFiMvFOP4JAhgq0KdjTG7KI4WpSWT.AYXUnwVK','水电值班员','13900000000','OPERATOR');

INSERT INTO station(station_code,station_name,river_name,installed_capacity,unit_count,status,manager,longitude,latitude,address) VALUES
('QS-01','青石一级电站','清源河上游',12.50,2,'RUNNING','周启明',104.042000,30.682000,'四川省清源县青石镇上游段'),
('QS-02','云岭梯级电站','云岭支流',8.80,1,'RUNNING','陈川',104.118000,30.744000,'四川省清源县云岭乡东坡段'),
('QS-03','峡口生态电站','清源河中游',15.20,1,'ATTENTION','林夏',104.176000,30.611000,'四川省清源县峡口村生态段');

INSERT INTO device(station_id,device_code,device_name,device_type,model,status,rated_power,current_power,running_hours,next_maintenance_date) VALUES
(1,'U-101','1号水轮发电机组','GENERATOR','HLA575C-WJ-84','RUNNING',6.25,5.42,6824,DATE_ADD(CURDATE(),INTERVAL 22 DAY)),
(1,'U-102','2号水轮发电机组','GENERATOR','HLA575C-WJ-84','RUNNING',6.25,4.86,6410,DATE_ADD(CURDATE(),INTERVAL 44 DAY)),
(2,'U-201','1号轴流转桨机组','GENERATOR','ZZ560-LH-120','RUNNING',8.80,6.31,7316,DATE_ADD(CURDATE(),INTERVAL 38 DAY)),
(3,'U-301','1号混流式机组','GENERATOR','HLD54-LJ-140','WARNING',15.20,4.18,8102,DATE_ADD(CURDATE(),INTERVAL 4 DAY)),
(3,'G-301','进水闸门控制器','GATE','QY-GATE-8','ONLINE',0,0,12802,DATE_ADD(CURDATE(),INTERVAL 86 DAY));

INSERT INTO alarm(alarm_code,station_id,device_id,level,title,current_value,threshold_value,status,handler,occurred_at,handled_at) VALUES
('ALM-TODAY-03',3,4,'HIGH','轴承温度偏高','78.6 °C','75 °C','ACTIVE',NULL,DATE_SUB(NOW(),INTERVAL 18 MINUTE),NULL),
('ALM-TODAY-02',1,2,'MEDIUM','尾水位短时波动','521.8 m','±0.8 m','ACTIVE',NULL,DATE_SUB(NOW(),INTERVAL 104 MINUTE),NULL),
('ALM-TODAY-01',2,3,'LOW','振动幅值接近预警值','2.3 mm/s','2.5 mm/s','RESOLVED','陈川',DATE_SUB(NOW(),INTERVAL 4 HOUR),DATE_SUB(NOW(),INTERVAL 3 HOUR)),
('ALM-YESTERDAY-04',1,1,'MEDIUM','有功功率波动','±6.8%','±6.0%','RESOLVED','周启明',DATE_SUB(NOW(),INTERVAL 12 HOUR),DATE_SUB(NOW(),INTERVAL 11 HOUR));

INSERT INTO work_order(order_code,station_id,device_id,title,description,owner,priority,status,due_date,created_at) VALUES
('WO-DEMO-001',3,4,'检查导轴承冷却回路','核查冷却水压、滤网及测温探头状态','刘工','HIGH','PROCESSING',DATE_ADD(CURDATE(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 6 HOUR)),
('WO-DEMO-002',1,2,'2号机组月度巡检','完成振动、温度与润滑油位例行检查','张工','MEDIUM','PENDING',DATE_ADD(CURDATE(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)),
('WO-DEMO-003',3,5,'闸门控制柜除尘紧固','清理控制柜并复核接线端子','王工','LOW','COMPLETED',DATE_SUB(CURDATE(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 9 DAY));

-- 生成最近 365 天发电演示数据和最近 24 小时监测数据
DROP PROCEDURE IF EXISTS seed_demo_data;
DELIMITER $$
CREATE PROCEDURE seed_demo_data()
BEGIN
  DECLARE day_offset INT DEFAULT 364;
  DECLARE hour_offset INT DEFAULT 23;
  DECLARE sid INT;
  DECLARE did INT;
  WHILE day_offset >= 0 DO
    SET sid = 1;
    WHILE sid <= 3 DO
      INSERT INTO generation_record(station_id,record_date,generation,plan_generation,rainfall,on_grid_energy)
      VALUES(sid,DATE_SUB(CURDATE(),INTERVAL day_offset DAY),
        ROUND(80 + sid*34 + 26*SIN(day_offset*0.19+sid) + MOD(day_offset*sid,17),2),
        105 + sid*32,ROUND(GREATEST(0,8+10*SIN(day_offset*0.31)),2),
        ROUND((80 + sid*34 + 26*SIN(day_offset*0.19+sid) + MOD(day_offset*sid,17))*0.965,2));
      SET sid = sid + 1;
    END WHILE;
    SET day_offset = day_offset - 1;
  END WHILE;
  WHILE hour_offset >= 0 DO
    SET did = 1;
    WHILE did <= 4 DO
      INSERT INTO monitoring_record(station_id,device_id,power,voltage,current_value,flow_rate,water_level,water_head,efficiency,bearing_temperature,vibration,recorded_at)
      VALUES(IF(did<=2,1,IF(did=3,2,3)),did,
        ROUND(3.8+did*0.65+0.7*SIN(hour_offset*0.5+did),2),ROUND(10.45+0.08*SIN(hour_offset+did),2),
        ROUND(210+did*38+12*SIN(hour_offset*0.4),2),ROUND(58+did*7+6*SIN(hour_offset*0.35),2),
        ROUND(528-did*1.8+0.4*SIN(hour_offset),2),ROUND(45+did*0.8+0.5*SIN(hour_offset),2),
        ROUND(89+did+1.2*SIN(hour_offset*0.4),2),ROUND(52+did*4.8+2*SIN(hour_offset),2),
        ROUND(0.9+did*0.3+0.15*SIN(hour_offset),2),DATE_SUB(DATE_FORMAT(NOW(),'%Y-%m-%d %H:00:00'),INTERVAL hour_offset HOUR));
      SET did = did + 1;
    END WHILE;
    SET hour_offset = hour_offset - 1;
  END WHILE;
END$$
DELIMITER ;
CALL seed_demo_data();
DROP PROCEDURE seed_demo_data;
