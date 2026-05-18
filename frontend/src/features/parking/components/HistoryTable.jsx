import React, { useState, useEffect } from "react";
import { Card, Table, Tag, Input, Spin, Select, Row, Col } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { getParkingSessionsApi } from "../api/parkingSession.api";
import { useNotification } from "../../../hooks/useNotification";
import { countParkingSessionsApi } from "../api/parkingSession.api";
import { getVehicleTypesApi } from "../api/vehicleApi";

const styles = {
  card: {
    background: "#ffffff",
    backdropFilter: "blur(12px)",
    border: "1px solid #d9d9d9",
    borderRadius: 16,
  },
  cardHead: { borderBottom: "1px solid #d9d9d9", color: "#141414" },
  searchInCard: {
    width: "100%",
    background: "#ffffff",
    border: "1px solid #d9d9d9",
    color: "#141414",
    borderRadius: 8,
  },
  selectInCard: {
    background: "#ffffff",
    borderRadius: 8,
    color: "#141414",
  },
  placeholderColor: "#808080",
};

const HistoryTable = React.forwardRef(({ refreshTrigger }, ref) => {
  const notify = useNotification();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [debouncedSearchText, setDebouncedSearchText] = useState(""); // Lưu text sau khi debounce
  const [vehicleTypeFilter, setVehicleTypeFilter] = useState(null);
  const [parkedCount, setParkedCount] = useState(0);
  const [vehicleOptions, setVehicleOptions] = useState([]);

  useEffect(() => {
    const fetchVehicleTypes = async () => {
      try {
        const response = await getVehicleTypesApi();
        if (Array.isArray(response?.data)) {
          setVehicleOptions(response.data);
        }
      } catch (error) {
        console.error("Không thể tải danh sách loại xe từ Backend:", error);
      }
    };
    fetchVehicleTypes();
  }, [])

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchText(searchText);
    }, 500);
    return () => clearTimeout(handler);
  }, [searchText]);

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        const timestamp = parseInt(dateString);
        if (!isNaN(timestamp)) {
          const parsedDate = new Date(timestamp);
          return parsedDate.toLocaleDateString("vi-VN") + " " + parsedDate.toLocaleTimeString("vi-VN");
        }
        return "N/A";
      }
      return date.toLocaleDateString("vi-VN") + " " + date.toLocaleTimeString("vi-VN");
    } catch (error) {
      console.error("Error parsing date:", dateString, error);
      return "N/A";
    }
  };

  // 2. Fetch API kết hợp các query param từ filter/search
  // Thêm cờ 'silent' để không hiện vòng xoay loading khi đang auto-refresh ngầm
  const fetchParkingSessions = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      // Chuẩn bị params gọi xuống backend
      const params = {
        page: 0,
        size: 20,
        sort: "createdAt,desc",
        status: "PARKED"
      };

      // Backend (ví dụ Spring Boot) cần hỗ trợ nhận các tham số này
      if (debouncedSearchText) params.licensePlate = debouncedSearchText;
      if (vehicleTypeFilter) params.vehicleType = vehicleTypeFilter;

      const response = await getParkingSessionsApi(params);

      if (response?.data?.content) {
        const formattedData = response.data.content.map((session, index) => ({
          key: session.id || index,
          time: formatDateTime(session.timeIn),
          vehicleType: session.vehicleType || "N/A",
          month: session.month != null ? (session.month ? "Có" : "Không") : "N/A",
          plateInOcr: session.plateInOcr || "N/A",
          finalPlate: session.finalPlate || "N/A",
          fullData: session,
        }));
        setData(formattedData);
      }
    } catch (error) {
      notify.apiError(error, "Lỗi tải dữ liệu");
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const fetchTotalParkingSessions = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await countParkingSessionsApi({ status: "PARKED" });
      if (response?.data) {
        setParkedCount(response.data);
      }
    } catch (error) {
      notify.apiError(error, "Lỗi tải dữ liệu");
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    fetchTotalParkingSessions();
  }, []);

  // 3. Gọi API khi filter hoặc text search (đã debounce) thay đổi
  useEffect(() => {
    fetchParkingSessions();
  }, [debouncedSearchText, vehicleTypeFilter]);

  // Hỗ trợ refresh từ component cha
  useEffect(() => {
    if (refreshTrigger) {
      fetchTotalParkingSessions();
      fetchParkingSessions();
    }
  }, [refreshTrigger]);

  React.useImperativeHandle(ref, () => ({
    refresh: fetchParkingSessions,
  }));

  // 4. Cơ chế Polling cho Real-time (Làm mới ngầm mỗi 3 giây)
  useEffect(() => {
    const intervalId = setInterval(() => {
      fetchTotalParkingSessions(true);
      fetchParkingSessions(true); // Gửi cờ silent = true

    }, 3000);

    return () => clearInterval(intervalId); // Clear interval khi component unmount hoặc state đổi
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearchText, vehicleTypeFilter]);

  const handleSearch = (value) => {
    setSearchText(value);
  };

  const columns = [
    { title: "Thời gian", dataIndex: "time" },
    {
      title: "Loại xe",
      dataIndex: "vehicleType",
      render: (vehicleType) => vehicleType?.label || "N/A"
    },
    { title: "Đăng ký", dataIndex: "month" },
    { title: "Biển số vào", dataIndex: "plateInOcr" },
    { title: "Biển số thực tế", dataIndex: "finalPlate" },
  ];

  return (
    <>
      <style>{`
        .history-table-search input::placeholder, .history-table-search .ant-select-selection-placeholder { color: #808080 !important; opacity: 1 !important; display: block !important; }
        .history-table-search .ant-select-selector { background: #fff !important; border-color: #d9d9d9 !important; }
        .history-table-search .ant-select-selection-item, .history-table-search .ant-select-arrow { color: #141414 !important; }
        .history-table-wrapper :where(.ant-table, .ant-table-container, th, td) { background: #fff !important; color: #141414 !important; border-bottom: 1px solid #f0f0f0 !important; }
        .history-table-wrapper th { font-weight: bold; background: #fafafa !important; color: #1677ff !important; }
        .history-table-wrapper tr:hover>td { background: #f5f5f5 !important; }
        .history-table-wrapper .ant-empty-description { color: #141414 !important; }
      `}</style>
      <Card
        title="Lịch sử" extra={<span style={{ color: "#1677ff", fontWeight: "bold", fontSize: 16 }}>Hiện đang đỗ: {parkedCount}</span>} style={styles.card}
        styles={{
          header: styles.cardHead,
        }}
      >
        <Row gutter={12} style={{ marginBottom: 16 }}>
          <Col flex="auto" className="history-table-search">
            <Input
              placeholder="Tìm biển số..."
              prefix={<SearchOutlined style={{ color: "#141414" }} />}
              style={styles.searchInCard}
              styles={{
                input: { color: "#141414" },
                textarea: { color: "#141414" },
              }}
              value={searchText}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </Col>
          <Col style={{ width: 150 }} className="history-table-search">
            <Select
              placeholder={<span style={{ color: "#141414" }}>Loại xe</span>}
              allowClear
              style={{ width: "100%", ...styles.selectInCard }}
              value={vehicleTypeFilter}
              onChange={(value) => setVehicleTypeFilter(value)}
              options={vehicleOptions}
            />
          </Col>
        </Row>
        <div className="history-table-wrapper">
          <Spin spinning={loading}>
            {/* Truyền trực tiếp state data thay vì filteredData */}
            <Table columns={columns} dataSource={data} pagination={false} />
          </Spin>
        </div>
      </Card>
    </>
  );
});

HistoryTable.displayName = 'HistoryTable';

export default HistoryTable;