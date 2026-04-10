-- =========================================================================
-- 1. KHỞI TẠO MÔI TRƯỜNG & EXTENSION (BẮT BUỘC CHO UUID)
-- =========================================================================
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Kích hoạt extension sinh UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================================
-- 2. TẠO CÁC KIỂU DỮ LIỆU ENUM 
-- =========================================================================
-- Chỉ giữ lại 2 Role thực tế cho hệ thống Desktop
CREATE TYPE user_role AS ENUM ('ADMIN', 'GUARD');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE vehicle_type_enum AS ENUM ('CAR', 'MOTO', 'BICYCLE');
CREATE TYPE sub_type AS ENUM ('MONTHLY', 'QUARTERLY', 'YEARLY');
CREATE TYPE sub_status AS ENUM ('PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED');
CREATE TYPE lane_type_enum AS ENUM ('IN', 'OUT');
CREATE TYPE lane_status AS ENUM ('ACTIVE', 'MAINTENANCE', 'DELETED');
CREATE TYPE session_status AS ENUM ('PARKED', 'COMPLETED', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');
CREATE TYPE payment_method AS ENUM ('CASH', 'ONLINE_PAYMENT');
CREATE TYPE incident_type_enum AS ENUM ('LOST_CARD', 'DAMAGE', 'SYSTEM_ERROR','WRONG_PLATE', 'OTHER');

CREATE TYPE pricing_strategy_enum AS ENUM (
    'FLAT_RATE', 'TIME_WINDOW', 'ROLLING_BLOCK', 'PROGRESSIVE', 'DAILY_CAPPED'
);

-- CREATE TYPE audit_action_enum AS ENUM (
--     'CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'MANUAL_OPEN_BARRIER', 'EXPORT_REPORT'
-- );
-- CREATE TYPE audit_table_enum AS ENUM (
--     'USERS', 'VEHICLE', 'SUBSCRIPTION', 'PARKING_SESSION', 'INVOICE', 'PRICING_RULE', 'INCIDENT', 'SYSTEM'
-- );

-- =========================================================================
-- 3. TẠO CÁC BẢNG DỮ LIỆU (100% UUID)
-- =========================================================================

-- 1. Bảng users (Tài khoản Admin, Guard - Phục vụ xác thực JWT)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Sẽ lưu mã băm BCrypt
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role user_role NOT NULL,
    status user_status DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng vehicle (Guard đăng ký vé tháng cho khách)
CREATE TABLE vehicle (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type vehicle_type_enum NOT NULL,
    brand VARCHAR(50),
    -- Lưu trực tiếp thông tin khách hàng tại đây
    customer_name VARCHAR(100),     
    customer_phone VARCHAR(20),     
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng subscription (Vé tháng)
CREATE TABLE subscription (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vehicle_id UUID NOT NULL REFERENCES vehicle(id),
    type sub_type NOT NULL,
    price BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status sub_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng lane (Làn xe / Camera)
CREATE TABLE lane (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lane_name VARCHAR(50) NOT NULL,
    lane_type lane_type_enum NOT NULL,
    ip_camera VARCHAR(100),
    status lane_status DEFAULT 'ACTIVE'
);

-- 5. Bảng pricing_rule (Động cơ tính giá)
CREATE TABLE pricing_rule (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rule_name VARCHAR(100) NOT NULL,              
    vehicle_type vehicle_type_enum NOT NULL,      
    strategy pricing_strategy_enum NOT NULL,      
    base_price BIGINT NOT NULL,                   
    start_time TIME,                              
    end_time TIME,                                
    block_minutes INT,                            
    threshold_minutes INT,                        
    threshold_price BIGINT,                       
    max_price_per_day BIGINT,                     
    progressive_config JSONB,  
    penalty_fee BIGINT NOT NULL DEFAULT 0,                   
    is_active BOOLEAN DEFAULT TRUE,               
    created_by UUID REFERENCES users(id),     
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Bảng parking_session (Lượt gửi xe)
CREATE TABLE parking_session (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entry_lane_id UUID REFERENCES lane(id),
    exit_lane_id UUID REFERENCES lane(id),
    time_in TIMESTAMP NOT NULL,
    time_out TIMESTAMP,
    plate_in_ocr VARCHAR(20),
    plate_out_ocr VARCHAR(20),
    final_plate VARCHAR(20),
    vehicle_type vehicle_type_enum NOT NULL,
    image_in_url TEXT,
    image_out_url TEXT,
    confidence_in FLOAT,
    confidence_out FLOAT,
    is_month BOOLEAN DEFAULT FALSE,
    status session_status DEFAULT 'PARKED'
);

-- 7. Bảng invoice (Hóa đơn / Thanh toán)
CREATE TABLE invoice (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES parking_session(id), 
    sub_id UUID REFERENCES subscription(id),             
    cashier_id UUID REFERENCES users(id),               
    amount BIGINT NOT NULL,                                 
    penalty_amount BIGINT DEFAULT 0,                        
    payment_time TIMESTAMP,
    payment_method VARCHAR(20),
    transaction_ref VARCHAR(50),
    status payment_status DEFAULT 'PENDING'
);

-- 8. Bảng incident (Sự cố: Mất thẻ, hư barie...)
CREATE TABLE incident (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES parking_session(id),
    reported_by UUID REFERENCES users(id),
    incident_type incident_type_enum NOT NULL, 
    description TEXT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -- 9. Bảng audit_log (Nhật ký giám sát hệ thống - UC5)
-- CREATE TABLE audit_log (
--     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
--     user_id UUID REFERENCES users(id),              
--     action_type audit_action_enum NOT NULL,             
--     target_table audit_table_enum NOT NULL,             
--     target_id VARCHAR(50),                              
--     old_value JSONB,                                    
--     new_value JSONB,                                    
--     ip_address VARCHAR(45),
--     device_info VARCHAR(255),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP      
-- );

-- 9. Bảng cấu hình gói vé (Dành cho Admin thiết lập giá)
CREATE TABLE subscription_pricing (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vehicle_type vehicle_type_enum NOT NULL, -- CAR, MOTO, BICYCLE
    duration_type sub_type NOT NULL,         -- MONTHLY, QUARTERLY, YEARLY
    price BIGINT NOT NULL,                   -- Giá tiền tương ứng
    description TEXT,                        -- Mô tả (vd: Gói tháng xe máy giá rẻ)
    is_active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Đảm bảo mỗi loại xe chỉ có 1 mức giá duy nhất cho 1 loại kỳ hạn
    UNIQUE(vehicle_type, duration_type) 
);

ALTER TABLE subscription 
ADD COLUMN config_id UUID REFERENCES subscription_config(id);
-- =========================================================================
-- 4. ĐÁNH CHỈ MỤC (INDEXING) - TỐI ƯU HIỆU SUẤT TRUY VẤN
-- =========================================================================
CREATE INDEX idx_vehicle_plate ON vehicle(license_plate);
CREATE INDEX idx_session_final_plate ON parking_session(final_plate);
CREATE INDEX idx_session_status ON parking_session(status);
CREATE INDEX idx_invoice_payment_time ON invoice(payment_time);
-- CREATE INDEX idx_audit_target ON audit_log(target_table, target_id);