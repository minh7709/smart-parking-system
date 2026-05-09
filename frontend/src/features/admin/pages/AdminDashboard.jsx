import React from "react";
import {
  Row,
  Col,
  Card,
  Statistic,
  Table,
  Button,
  Select,
  Radio,
  DatePicker,
  Tag,
} from "antd";
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  DownloadOutlined,
} from "@ant-design/icons";

const { RangePicker } = DatePicker;

// Dữ liệu mẫu cho bảng
const tableData = [
  {
    key: "1",
    time: "14:25 22/10/2023",
    plate: "30A-123.45",
    type: "Vé Lượt",
    gate: "Cổng 1 (Vào)",
    amount: "30,000đ",
  },
  {
    key: "2",
    time: "14:20 22/10/2023",
    plate: "51G-987.65",
    type: "Vé Tháng",
    gate: "Cổng 2 (Ra)",
    amount: "0đ",
  },
  {
    key: "3",
    time: "14:15 22/10/2023",
    plate: "29D-555.22",
    type: "Vé Lượt",
    gate: "Cổng 1 (Vào)",
    amount: "50,000đ",
  },
];

const columns = [
  { title: "THỜI GIAN", dataIndex: "time", key: "time", fontWeight: "bold" },
  {
    title: "BIỂN SỐ XE",
    dataIndex: "plate",
    key: "plate",
    render: (text) => <strong>{text}</strong>,
  },
  {
    title: "LOẠI VÉ",
    dataIndex: "type",
    key: "type",
    render: (type) => (
      <Tag
        color={type === "Vé Lượt" ? "blue" : "green"}
        style={{ borderRadius: "10px" }}
      >
        {type}
      </Tag>
    ),
  },
  { title: "CỔNG", dataIndex: "gate", key: "gate" },
  {
    title: "THÀNH TIỀN",
    dataIndex: "amount",
    key: "amount",
    align: "right",
    render: (text) => <strong>{text}</strong>,
  },
];

const AdminDashboard = () => {
  return (
    <div>
      {/* BỘ LỌC */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          marginBottom: 20,
        }}
      >
        <div style={{ display: "flex", gap: "15px", alignItems: "center" }}>
          <span style={{ fontWeight: "bold", color: "#888" }}>THỜI GIAN</span>
          <Radio.Group defaultValue="ngay">
            <Radio.Button value="ngay">Ngày</Radio.Button>
            <Radio.Button value="tuan">Tuần</Radio.Button>
            <Radio.Button value="thang">Tháng</Radio.Button>
          </Radio.Group>

          <span style={{ fontWeight: "bold", color: "#888", marginLeft: 10 }}>
            CỔNG KIỂM SOÁT
          </span>
          <Select defaultValue="all" style={{ width: 120 }}>
            <Select.Option value="all">Tất cả cổng</Select.Option>
            <Select.Option value="1">Cổng 1</Select.Option>
            <Select.Option value="2">Cổng 2</Select.Option>
          </Select>
        </div>

        <div style={{ display: "flex", gap: "10px" }}>
          <RangePicker />
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            style={{ backgroundColor: "#00875a" }}
          >
            Xuất báo cáo
          </Button>
        </div>
      </div>

      {/* 4 THẺ THỐNG KÊ */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic
              title="Tổng doanh thu"
              value={1245000000}
              suffix="đ"
              style={{ color: "#000" }}
            />
            <div
              style={{ color: "#52c41a", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowUpOutlined /> 12.5% so với kỳ trước
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic title="Lưu lượng xe" value={18542} suffix="lượt" />
            <div
              style={{ color: "#52c41a", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowUpOutlined /> 8.2% so với kỳ trước
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic title="Trung bình ngày" value={41500000} suffix="đ" />
            <div
              style={{ color: "#ff4d4f", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowDownOutlined /> 2.4% so với kỳ trước
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic title="Tỷ lệ lấp đầy" value={85} suffix="%" />
            {/* Bạn có thể dùng component Progress của Antd ở đây */}
            <div
              style={{
                background: "#e0e0e0",
                height: "8px",
                borderRadius: "4px",
                marginTop: "15px",
              }}
            >
              <div
                style={{
                  background: "#0958d9",
                  width: "85%",
                  height: "100%",
                  borderRadius: "4px",
                }}
              ></div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* BIỂU ĐỒ */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={16}>
          <Card
            title="Biểu đồ doanh thu"
            style={{ borderRadius: "12px", height: "100%" }}
          >
            <p style={{ color: "#888" }}>
              Phân tích dòng tiền theo thời gian (đơn vị: triệu đồng)
            </p>
            {/* Chỗ này sau bạn dùng thẻ <LineChart> của Recharts nhúng vào */}
            <div
              style={{
                height: "250px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "#f5f5f5",
                borderRadius: "8px",
              }}
            >
              [Khu vực gắn Biểu đồ Line Chart (Nên dùng thư viện Recharts)]
            </div>
          </Card>
        </Col>
        <Col span={8}>
          <Card
            title="Lưu lượng xe"
            style={{ borderRadius: "12px", height: "100%" }}
          >
            {/* Biểu đồ cột mini ở đây */}
            <div
              style={{
                height: "180px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "#f5f5f5",
                borderRadius: "8px",
                marginBottom: "15px",
              }}
            >
              [Biểu đồ cột Bar Chart]
            </div>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                borderTop: "1px solid #f0f0f0",
                paddingTop: "10px",
              }}
            >
              <span>Bận rộn nhất:</span>
              <strong>Chủ Nhật (12:00 - 21:00)</strong>
            </div>
          </Card>
        </Col>
      </Row>

      {/* BẢNG GIAO DỊCH GẦN ĐÂY */}
      <Card
        title="Chi tiết giao dịch gần đây"
        style={{ borderRadius: "12px" }}
        extra={<Button type="link">Xem tất cả</Button>}
      >
        <Table columns={columns} dataSource={tableData} pagination={false} />
      </Card>
    </div>
  );
};

export default AdminDashboard;
