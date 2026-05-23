import React, { useEffect, useMemo, useState } from "react";
import {
  Row,
  Col,
  Card,
  Statistic,
  Table,
  Select,
  Radio,
  DatePicker,
  Alert,
  Spin,
} from "antd";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import axiosClient from "../../../api/axiosClient";
import API_ENDPOINTS from "../../../api/endpoints";

const RANGE_TO_UNIT = {
  ngay: "day",
  tuan: "week",
  thang: "month",
};

const RANGE_TO_INTERVAL = {
  ngay: "HOUR",
  tuan: "DAY",
  thang: "WEEK",
};

const formatDateTime = (value) =>
  value ? value.format("YYYY-MM-DDTHH:mm:ss") : null;

const safeNumber = (value) => (Number.isFinite(value) ? value : 0);

const formatCurrency = (value) =>
  new Intl.NumberFormat("vi-VN").format(safeNumber(value));

const formatDateTimeDisplay = (value) =>
  value ? dayjs(value).format("DD/MM/YYYY HH:mm") : "-";

const CHART_COLORS = ["#1677ff", "#52c41a", "#faad14", "#ff4d4f"];

const renderCurrencyTooltip = (value) => `${formatCurrency(value)} đ`;

const AdminDashboard = () => {
  const [rangeType, setRangeType] = useState("ngay");
  const [dateRange, setDateRange] = useState([
    dayjs().startOf("day"),
    dayjs().endOf("day"),
  ]);
  const [selectedLane, setSelectedLane] = useState("all");

  const [summary, setSummary] = useState(null);
  const [trafficTimeline, setTrafficTimeline] = useState([]);
  const [revenueTimeline, setRevenueTimeline] = useState([]);
  const [revenueBreakdown, setRevenueBreakdown] = useState(null);
  const [penalties, setPenalties] = useState(null);
  const [laneUtilization, setLaneUtilization] = useState([]);
  const [invoices, setInvoices] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const interval = RANGE_TO_INTERVAL[rangeType];

  const params = useMemo(() => {
    const [start, end] = dateRange || [];
    return {
      startDate: formatDateTime(start),
      endDate: formatDateTime(end),
    };
  }, [dateRange]);

  useEffect(() => {
    const fetchStatistics = async () => {
      if (!params.startDate || !params.endDate) {
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const [
          summaryRes,
          trafficTimelineRes,
          trafficLanesRes,
          revenueTimelineRes,
          revenueBreakdownRes,
          penaltiesRes,
          invoicesRes,
        ] = await Promise.all([
          axiosClient.get(API_ENDPOINTS.admin.statistics.summary, { params }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.trafficTimeline, {
            params: { ...params, interval },
          }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.trafficLanes, { params }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.revenueTimeline, {
            params: { ...params, interval },
          }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.revenueBreakdown, {
            params,
          }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.revenuePenalties, {
            params,
          }),
          axiosClient.get(API_ENDPOINTS.admin.statistics.invoices, {
            params,
          }),
        ]);

        setSummary(summaryRes?.data || null);
        setTrafficTimeline(trafficTimelineRes?.data || []);
        setLaneUtilization(trafficLanesRes?.data || []);
        setRevenueTimeline(revenueTimelineRes?.data || []);
        setRevenueBreakdown(revenueBreakdownRes?.data || null);
        setPenalties(penaltiesRes?.data || null);
        setInvoices(invoicesRes?.data?.content || []);
        console.log("Fetched statistics:", {
          summary: summaryRes?.data,
          trafficTimeline: trafficTimelineRes?.data,
          trafficLanes: trafficLanesRes?.data,
          revenueTimeline: revenueTimelineRes?.data,
          revenueBreakdown: revenueBreakdownRes?.data,
          penalties: penaltiesRes?.data,
          invoices: invoicesRes?.data?.content,
        });
      } catch (err) {
        setError(err?.message || "Không thể tải dữ liệu thống kê");
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
  }, [params, interval]);

  const handleRangeTypeChange = (event) => {
    const value = event.target.value;
    setRangeType(value);

    const unit = RANGE_TO_UNIT[value];
    setDateRange([dayjs().startOf(unit), dayjs().endOf(unit)]);
  };

  const laneOptions = useMemo(() => {
    const names = laneUtilization
      .map((lane) => lane?.laneName)
      .filter(Boolean);
    return ["all", ...new Set(names)];
  }, [laneUtilization]);

  const filteredLaneUtilization = useMemo(() => {
    if (selectedLane === "all") {
      return laneUtilization;
    }
    return laneUtilization.filter((lane) => lane.laneName === selectedLane);
  }, [laneUtilization, selectedLane]);

  const busiestTraffic = useMemo(() => {
    if (!trafficTimeline.length) {
      return null;
    }
    return trafficTimeline.reduce((current, item) => {
      const currentTotal =
        safeNumber(current?.regularCount) + safeNumber(current?.monthlyCount);
      const nextTotal =
        safeNumber(item?.regularCount) + safeNumber(item?.monthlyCount);
      return nextTotal > currentTotal ? item : current;
    }, trafficTimeline[0]);
  }, [trafficTimeline]);

  const revenueTimelineData = useMemo(
    () =>
      revenueTimeline.map((item, index) => ({
        key: `${item?.timestamp || index}`,
        timestamp: item?.timestamp,
        totalRevenue: item?.totalRevenue,
      })),
    [revenueTimeline]
  );

  const trafficTimelineData = useMemo(
    () =>
      trafficTimeline.map((item, index) => ({
        key: `${item?.timestamp || index}`,
        timestamp: item?.timestamp,
        regularCount: item?.regularCount,
        monthlyCount: item?.monthlyCount,
        totalCount:
          safeNumber(item?.regularCount) + safeNumber(item?.monthlyCount),
      })),
    [trafficTimeline]
  );

  const laneTableData = useMemo(
    () =>
      filteredLaneUtilization.map((lane, index) => ({
        key: `${lane?.laneName || index}`,
        laneName: lane?.laneName,
        entryCount: lane?.entryCount,
        exitCount: lane?.exitCount,
      })),
    [filteredLaneUtilization]
  );

  const invoiceTableData = useMemo(
    () =>
      invoices.map((invoice, index) => ({
        key: `${invoice?.id || index}`,
        id: invoice?.id,
        invoiceType: invoice?.invoiceType,
        totalAmount: invoice?.totalAmount,
        paymentMethod: invoice?.paymentMethod,
        status: invoice?.status,
        cashierName: invoice?.cashierName,
        paymentTime: invoice?.paymentTime,
      })),
    [invoices]
  );

  const laneColumns = [
    { title: "CỔNG", dataIndex: "laneName", key: "laneName" },
    {
      title: "LƯỢT VÀO",
      dataIndex: "entryCount",
      key: "entryCount",
      align: "right",
    },
    {
      title: "LƯỢT RA",
      dataIndex: "exitCount",
      key: "exitCount",
      align: "right",
    },
  ];

  const invoiceColumns = [
    {
      title: "MÃ HÓA ĐƠN",
      dataIndex: "id",
      key: "id",
      render: (value) => value || "-",
    },
    {
      title: "LOẠI HÓA ĐƠN",
      dataIndex: "invoiceType",
      key: "invoiceType",
      render: (t) => t?.label || "-",
    },
    {
      title: "THANH TOÁN",
      dataIndex: "totalAmount",
      key: "totalAmount",
      align: "right",
      render: (value) => `${formatCurrency(value)} đ`,
    },
    {
      title: "PHƯƠNG THỨC",
      dataIndex: "paymentMethod",
      key: "paymentMethod",
      render: (t) => t?.label || "-",
    },
    {
      title: "TRẠNG THÁI",
      dataIndex: "status",
      key: "status",
      render: (t) => t?.label || "-",
    },
    {
      title: "THU NGÂN",
      dataIndex: "cashierName",
      key: "cashierName",
      render: (value) => value || "-",
    },
    {
      title: "THỜI GIAN",
      dataIndex: "paymentTime",
      key: "paymentTime",
      render: (value) => formatDateTimeDisplay(value),
    },
  ];
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
          <Radio.Group value={rangeType} onChange={handleRangeTypeChange}>
            <Radio.Button value="ngay">Ngày</Radio.Button>
            <Radio.Button value="tuan">Tuần</Radio.Button>
            <Radio.Button value="thang">Tháng</Radio.Button>
          </Radio.Group>
        </div>
      </div>

      {error && (
        <Alert
          type="error"
          message="Không thể tải dữ liệu thống kê"
          description={error}
          showIcon
          style={{ marginBottom: 20 }}
        />
      )}

      {/* 4 THẺ THỐNG KÊ */}
      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic
              title="Tổng doanh thu"
              value={safeNumber(summary?.totalRevenue)}
              suffix="đ"
              formatter={(value) => formatCurrency(value)}
            />
            <div
              style={{ color: "#52c41a", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowUpOutlined /> Theo khoảng đã chọn
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic
              title="Lưu lượng xe"
              value={safeNumber(summary?.totalSessions)}
              suffix="lượt"
            />
            <div
              style={{ color: "#52c41a", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowUpOutlined /> Theo khoảng đã chọn
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic
              title="Xe đang đỗ"
              value={safeNumber(summary?.parkedCount)}
              suffix="xe"
            />
            <div
              style={{ color: "#ff4d4f", marginTop: "10px", fontSize: "12px" }}
            >
              <ArrowDownOutlined /> Tình trạng hiện tại
            </div>
          </Card>
        </Col>
        <Col span={6}>
          <Card style={{ borderRadius: "12px" }}>
            <Statistic
              title="Đăng ký đang hoạt động"
              value={safeNumber(summary?.activeSubscriptions)}
              suffix="gói"
            />
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
                  width: "100%",
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
              Phân tích dòng tiền theo thời gian (đơn vị: đồng)
            </p>
            <Spin spinning={loading}>
              <div style={{ width: "100%", height: 280 }}>
                <ResponsiveContainer>
                  <LineChart data={revenueTimelineData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                    <XAxis dataKey="timestamp" />
                    <YAxis tickFormatter={(value) => formatCurrency(value)} />
                    <Tooltip formatter={(value) => renderCurrencyTooltip(value)} />
                    <Line type="monotone" dataKey="totalRevenue" stroke={CHART_COLORS[0]} strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Spin>
          </Card>
        </Col>
        <Col span={8}>
          <Card
            title="Lưu lượng xe"
            style={{ borderRadius: "12px", height: "100%" }}
          >
            <p style={{ color: "#888" }}>
              Tổng lượt vào ra theo vé lượt và vé vip
            </p>
            <Spin spinning={loading}>
              <div style={{ width: "100%", height: 280 }}>
                <ResponsiveContainer>
                  <LineChart data={trafficTimelineData} margin={{ top: 10, right: 12, left: 0, bottom: 0 }}>
                    <XAxis dataKey="timestamp" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="regularCount" name="Vé lượt" stroke={CHART_COLORS[1]} strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="monthlyCount" name="Vé vip" stroke={CHART_COLORS[2]} strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Spin>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                borderTop: "1px solid #f0f0f0",
                paddingTop: "10px",
              }}
            >
              <span>Bận rộn nhất:</span>
              <strong>
                {busiestTraffic?.timestamp || "Chưa có dữ liệu"}
              </strong>
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: 20 }}>
        <Col span={12}>
          <Card title="Phân bổ doanh thu" style={{ borderRadius: "12px" }}>
            <Row gutter={12}>
              <Col span={12}>
                <Statistic
                  title="Tiền mặt"
                  value={safeNumber(revenueBreakdown?.cashRevenue)}
                  suffix="đ"
                  formatter={(value) => formatCurrency(value)}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="Online"
                  value={safeNumber(revenueBreakdown?.onlinePaymentRevenue)}
                  suffix="đ"
                  formatter={(value) => formatCurrency(value)}
                />
              </Col>
              <Col span={24} style={{ marginTop: 16 }}>
                <div style={{ width: "100%", height: 220 }}>
                  <ResponsiveContainer>
                    <PieChart>
                      <Pie
                        data={[
                          { name: "Tiền mặt", value: safeNumber(revenueBreakdown?.cashRevenue) },
                          { name: "Online", value: safeNumber(revenueBreakdown?.onlinePaymentRevenue) },
                        ]}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={80}
                        label
                      >
                        <Cell fill={CHART_COLORS[1]} />
                        <Cell fill={CHART_COLORS[0]} />
                      </Pie>
                      <Tooltip formatter={(value) => renderCurrencyTooltip(value)} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </Col>
              <Col span={12} style={{ marginTop: 16 }}>
                <Statistic
                  title="Vé lượt"
                  value={safeNumber(revenueBreakdown?.sessionRevenue)}
                  suffix="đ"
                  formatter={(value) => formatCurrency(value)}
                />
              </Col>
              <Col span={12} style={{ marginTop: 16 }}>
                <Statistic
                  title="Vé vip"
                  value={safeNumber(revenueBreakdown?.subscriptionRevenue)}
                  suffix="đ"
                  formatter={(value) => formatCurrency(value)}
                />
              </Col>
            </Row>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="Phí phạt" style={{ borderRadius: "12px" }}>
            <Statistic
              title="Tổng phí phạt"
              value={safeNumber(penalties?.totalPenalty)}
              suffix="đ"
              formatter={(value) => formatCurrency(value)}
            />
          </Card>
        </Col>
      </Row>
      
      {/* BẢNG GIAO DỊCH GẦN ĐÂY */}
      <Card
        title="Lưu lượng theo cổng"
        style={{ borderRadius: "12px" }}
      >
        <span style={{ fontWeight: "bold", color: "#888", marginLeft: 10, marginRight: 20 }}>
            CỔNG KIỂM SOÁT
          </span>
        <Select
            value={selectedLane}
            style={{ width: 160 }}
            onChange={setSelectedLane}
          >
            {laneOptions.map((laneName) => (
              <Select.Option key={laneName} value={laneName}>
                {laneName === "all" ? "Tất cả cổng" : laneName}
              </Select.Option>
            ))}
          </Select>
        <Spin spinning={loading}>
          <Table columns={laneColumns} dataSource={laneTableData} pagination={false} />
        </Spin>
      </Card>

      <Card
        title="Hóa đơn"
        style={{ borderRadius: "12px", marginTop: 20 }}
      >
        <Spin spinning={loading}>
          <Table
            columns={invoiceColumns}
            dataSource={invoiceTableData}
            pagination={{ pageSize: 8 }}
            scroll={{ x: 900 }}
          />
        </Spin>
      </Card>
    </div>
  );
};

export default AdminDashboard;
