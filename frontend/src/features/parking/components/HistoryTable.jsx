import React, { useState, useEffect } from "react";
import { Card, Table, Tag, Input, Spin, Select, Row, Col } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { getParkingSessionsApi } from "../api/parkingSession.api";
import { useNotification } from "../../../hooks/useNotification";

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
  const [vehicleTypeFilter, setVehicleTypeFilter] = useState(null);
  const [parkedCount, setParkedCount] = useState(0);

  useEffect(() => {
    fetchParkingSessions();
  }, []);

  useEffect(() => {
    if (refreshTrigger) {
      fetchParkingSessions();
    }
  }, [refreshTrigger]);

  React.useImperativeHandle(ref, () => ({
    refresh: fetchParkingSessions,
  }));

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        // Nếu không parse được, thử parse lại
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

  const fetchParkingSessions = async () => {
    setLoading(true);
    try {
      const response = await getParkingSessionsApi({
        page: 0,
        size: 50,
        sort: "createdAt,desc",
      });

      if (response?.data?.content) {
        if (response.data.totalElements !== undefined) {
          setParkedCount(response.data.totalElements);
        } else {
          setParkedCount(response.data.content.length);
        }

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
      notify.error("Lỗi tải lịch sử: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (value) => {
    setSearchText(value);
  };

  const filteredData = data.filter((record) => {
    const matchPlate = record.plateInOcr.toLowerCase().includes(searchText.toLowerCase());
    const matchVehicleType = !vehicleTypeFilter || record.vehicleType === vehicleTypeFilter;
    return matchPlate && matchVehicleType;
  });
  const columns = [
    { title: "Thời gian", dataIndex: "time" },
    { title: "Loại xe", dataIndex: "vehicleType" },
    { title: "Vé tháng", dataIndex: "month" },
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
        title="Lịch sử"        extra={<span style={{ color: "#1677ff", fontWeight: "bold", fontSize: 16 }}>Hiện đang đỗ: {parkedCount}</span>}        style={styles.card}
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
                input: {
                  color: "#141414",
                },
                textarea: {
                  color: "#141414",
                },
              }}
              value={searchText}
              onChange={(e) => handleSearch(e.target.value)}
            />
          </Col>
          <Col style={{ width: 150 }} className="history-table-search">
            <Select
              placeholder={<span style={{ color: "#141414" }}>Loại xe</span>}
              allowClear
              style={{
                width: "100%",
                ...styles.selectInCard,
              }}
              value={vehicleTypeFilter}
              onChange={(value) => setVehicleTypeFilter(value)}
              options={[
                { label: "MOTOR", value: "MOTOR" },
                { label: "CAR", value: "CAR" },
                { label: "BICYCLE", value: "BICYCLE" },
              ]}
            />
          </Col>
        </Row>
        <div className="history-table-wrapper">
          <Spin spinning={loading}>
            <Table columns={columns} dataSource={filteredData} pagination={false} />
          </Spin>
        </div>
      </Card>
    </>
  );
});

HistoryTable.displayName = 'HistoryTable';

export default HistoryTable;
