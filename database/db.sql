-- =========================================================================
-- 1. XÓA BẢNG VÀ ENUM CŨ NẾU CÓ (Hỗ trợ chạy lại script nhiều lần không lỗi)
-- =========================================================================
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- =========================================================================
-- 2. TẠO CÁC KIỂU DỮ LIỆU ENUM (Chuẩn hóa toàn bộ hệ thống)
-- =========================================================================
CREATE TYPE user_role AS ENUM ('ADMIN', 'GUARD', 'CUSTOMER');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE vehicle_type_enum AS ENUM ('CAR', 'MOTO', 'BICYCLE');
CREATE TYPE sub_type AS ENUM ('MONTHLY', 'QUARTERLY', 'YEARLY');
CREATE TYPE sub_status AS ENUM ('PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED');
CREATE TYPE lane_type_enum AS ENUM ('IN', 'OUT');
CREATE TYPE lane_status AS ENUM ('ACTIVE', 'MAINTENANCE');
CREATE TYPE session_status AS ENUM ('PARKED', 'COMPLETED', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');
CREATE TYPE incident_type_enum AS ENUM ('LOST_CARD', 'DAMAGE', 'SYSTEM_ERROR', 'OTHER');

-- Enum cho Hệ thống tính giá
CREATE TYPE pricing_strategy_enum AS ENUM (
    'FLAT_RATE',      -- Tính theo lượt
    'TIME_WINDOW',    -- Tính theo khung giờ (Ngày/Đêm)
    'ROLLING_BLOCK',  -- Tính theo Block (Mỗi 4 tiếng)
    'PROGRESSIVE',    -- Tính bậc thang (Càng lâu càng đắt)
    'DAILY_CAPPED'    -- Tính theo giờ nhưng có giá trần ngày
);

-- Enum cho Audit Log
CREATE TYPE audit_action_enum AS ENUM (
    'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 
    'MANUAL_OPEN_BARRIER', 'EXPORT_REPORT'
);
CREATE TYPE audit_table_enum AS ENUM (
    'USERS', 'VEHICLE', 'SUBSCRIPTION', 'PARKING_SESSION', 
    'INVOICE', 'PRICING_RULE', 'INCIDENT', 'SYSTEM'
);

-- =========================================================================
-- 3. TẠO CÁC BẢNG DỮ LIỆU (Thứ tự từ không phụ thuộc đến có phụ thuộc)
-- =========================================================================

-- 1. Bảng users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role user_role NOT NULL,
    status user_status DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng vehicle
CREATE TABLE vehicle (
    vehicle_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type vehicle_type_enum NOT NULL,
    brand VARCHAR(50),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng subscription (Vé tháng)
CREATE TABLE subscription (
    sub_id SERIAL PRIMARY KEY,
    vehicle_id INT NOT NULL REFERENCES vehicle(vehicle_id),
    type sub_type NOT NULL,
    price BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status sub_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng lane (Làn xe / Camera)
CREATE TABLE lane (
    lane_id SERIAL PRIMARY KEY,
    lane_name VARCHAR(50) NOT NULL,
    lane_type lane_type_enum NOT NULL,
    ip_camera VARCHAR(100),
    status lane_status DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE
);

-- 5. Bảng pricing_rule (Động cơ tính giá - ĐÃ THÊM THUỘC TÍNH MỚI)
CREATE TABLE pricing_rule (
    rule_id SERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,              
    vehicle_type vehicle_type_enum NOT NULL,      
    strategy pricing_strategy_enum NOT NULL,      
    
    base_price BIGINT NOT NULL,                   -- Giá cơ sở 
    start_time TIME,                              -- Giờ bắt đầu (cho TIME_WINDOW)
    end_time TIME,                                -- Giờ kết thúc (cho TIME_WINDOW)
    block_minutes INT,                            -- Số phút 1 block (cho ROLLING_BLOCK)
    
    -- GIẢI QUYẾT BÀI TOÁN NGOẠI LỆ (Gửi > 12h)
    threshold_minutes INT,                        -- Mốc thời gian ngoại lệ (VD: 720)
    threshold_price BIGINT,                       -- Giá áp dụng khi vượt mốc (VD: 10000)
    
    max_price_per_day BIGINT,                     -- Giá trần 1 ngày (cho DAILY_CAPPED)
    progressive_config JSONB,                     -- Cấu hình bậc thang
    
    is_active BOOLEAN DEFAULT TRUE,               
    created_by INT REFERENCES users(user_id),     
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Bảng parking_session (Lượt gửi xe)
CREATE TABLE parking_session (
    session_id BIGSERIAL PRIMARY KEY,
    entry_lane_id INT REFERENCES lane(lane_id),
    exit_lane_id INT REFERENCES lane(lane_id),
    time_in TIMESTAMP NOT NULL,
    time_out TIMESTAMP,
    plate_in_ocr VARCHAR(20),
    plate_out_ocr VARCHAR(20),
    final_plate VARCHAR(20),
    image_in_url TEXT,
    image_out_url TEXT,
    confidence_in FLOAT,
    confidence_out FLOAT,
    is_month BOOLEAN DEFAULT FALSE,
    status session_status DEFAULT 'PARKED'
);

-- 7. Bảng invoice (Hóa đơn / Thanh toán)
CREATE TABLE invoice (
    invoice_id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES parking_session(session_id), 
    sub_id INT REFERENCES subscription(sub_id),             
    cashier_id INT REFERENCES users(user_id),               
    amount BIGINT NOT NULL,                                 
    penalty_amount BIGINT DEFAULT 0,                        
    payment_time TIMESTAMP,
    payment_method VARCHAR(20),
    transaction_ref VARCHAR(50),
    status payment_status DEFAULT 'PENDING'
);

-- 8. Bảng incident (Sự cố: Mất thẻ, hư barie...)
CREATE TABLE incident (
    incident_id SERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES parking_session(session_id),
    reported_by INT REFERENCES users(user_id),
    incident_type incident_type_enum NOT NULL, 
    description TEXT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Bảng audit_log (Nhật ký giám sát hệ thống)
CREATE TABLE audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),              
    action_type audit_action_enum NOT NULL,             
    target_table audit_table_enum NOT NULL,             
    target_id VARCHAR(50),                              
    old_value JSONB,                                    
    new_value JSONB,                                    
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP      
);

-- =========================================================================
-- 4. ĐÁNH CHỈ MỤC (INDEXING) - TỐI ƯU HIỆU SUẤT TRUY VẤN
-- =========================================================================
CREATE INDEX idx_vehicle_plate ON vehicle(license_plate);
CREATE INDEX idx_session_final_plate ON parking_session(final_plate);
CREATE INDEX idx_session_status ON parking_session(status);
CREATE INDEX idx_invoice_payment_time ON invoice(payment_time);
CREATE INDEX idx_audit_target ON audit_log(target_table, target_id);