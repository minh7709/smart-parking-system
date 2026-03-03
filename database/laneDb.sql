-- ==============================================================================
-- 1. BẢNG ĐỒNG BỘ TỪ SERVER XUỐNG (READ-ONLY hoặc ĐỒNG BỘ 2 CHIỀU)
-- ==============================================================================

-- Phục vụ đăng nhập Guard tại trạm (Lấy từ Server về)
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL,
    status TEXT DEFAULT 'ACTIVE'
);

-- Phục vụ UC7: Tính tiền tự động (Lấy rule từ Server về)
CREATE TABLE pricing_rule (
    id TEXT PRIMARY KEY,
    rule_name TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    strategy TEXT NOT NULL,
    base_price INTEGER NOT NULL,
    progressive_config TEXT, -- Lưu JSON dưới dạng chuỗi Text
    is_active INTEGER DEFAULT 1
);

-- ==============================================================================
-- 2. BẢNG VẬN HÀNH CHÍNH (THAO TÁC LOCAL & ĐẨY LÊN SERVER)
-- ==============================================================================

-- Phục vụ UC10: Đăng ký Phương tiện (Đồng bộ 2 chiều)
CREATE TABLE vehicle (
    id TEXT PRIMARY KEY,
    license_plate TEXT NOT NULL UNIQUE,
    vehicle_type TEXT NOT NULL,
    brand TEXT,
    customer_name TEXT,
    customer_phone TEXT,
    sync_status INTEGER DEFAULT 0,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Phục vụ UC11: Bán & Gia hạn Vé tháng (Đồng bộ 2 chiều)
CREATE TABLE subscription (
    id TEXT PRIMARY KEY,
    vehicle_id TEXT NOT NULL,
    type TEXT NOT NULL, -- 'MONTHLY', 'QUARTERLY', 'YEARLY'
    price INTEGER NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    sync_status INTEGER DEFAULT 0,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(vehicle_id) REFERENCES vehicle(id)
);

-- Phục vụ UC6 (Xe vào), UC7 (Xe ra), UC9 (Tra cứu)
CREATE TABLE parking_session (
    id TEXT PRIMARY KEY,
    entry_lane_id TEXT,
    exit_lane_id TEXT,
    time_in TEXT NOT NULL,
    time_out TEXT,
    plate_in_ocr TEXT,
    plate_out_ocr TEXT,
    final_plate TEXT, -- UC6: Bảo vệ gõ lại biển số đúng nếu AI sai
    image_in_url TEXT,
    image_out_url TEXT,
    confidence_in REAL,
    confidence_out REAL,
    is_month INTEGER DEFAULT 0,
    status TEXT DEFAULT 'PARKED', -- 'PARKED', 'COMPLETED', 'CANCELLED'
    sync_status INTEGER DEFAULT 0,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Phục vụ UC7: Thanh toán và xuất hóa đơn
CREATE TABLE invoice (
    id TEXT PRIMARY KEY,
    session_id TEXT,
    sub_id TEXT,
    cashier_id TEXT,
    amount INTEGER NOT NULL,
    penalty_amount INTEGER DEFAULT 0,
    payment_time TEXT,
    payment_method TEXT, -- 'CASH', 'ONLINE_PAYMENT'
    transaction_ref TEXT,
    status TEXT DEFAULT 'PENDING',
    sync_status INTEGER DEFAULT 0,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(session_id) REFERENCES parking_session(id),
    FOREIGN KEY(sub_id) REFERENCES subscription(id)
);

-- Phục vụ UC8: Xử lý sự cố & Ngoại lệ
CREATE TABLE incident (
    id TEXT PRIMARY KEY,
    session_id TEXT,
    reported_by TEXT,
    incident_type TEXT NOT NULL, -- 'LOST_CARD', 'DAMAGE', 'SYSTEM_ERROR', 'OTHER'
    description TEXT,
    reported_at TEXT DEFAULT CURRENT_TIMESTAMP,
    sync_status INTEGER DEFAULT 0,
    FOREIGN KEY(session_id) REFERENCES parking_session(id),
    FOREIGN KEY(reported_by) REFERENCES users(id)
);