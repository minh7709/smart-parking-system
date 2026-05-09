import React, { useState, useEffect } from "react";
import { Card, Table, Tag, Input, Spin, Select, Row, Col } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { getParkingSessionsApi } from "../api/parkingSession.api";
import { useNotification } from "../../../hooks/useNotification";

const styles = {
  card: {
    background: "rgba(255,255,255,0.03)",
    backdropFilter: "blur(12px)",
    border: "1px solid rgba(255,255,255,0.08)",
    borderRadius: 16,
  },
  cardHead: { borderBottom: "1px solid rgba(255,255,255,0.05)", color: "#fff" },
  searchInCard: {
    width: "100%",
    background: "#1a1a1a",
    border: "1px solid rgba(255,255,255,0.1)",
    color: "#fff",
    borderRadius: 8,
  },
  selectInCard: {
    background: "#1a1a1a",
    borderRadius: 8,
  },
};

const HistoryTable = React.forwardRef(({ refreshTrigger }, ref) => {
  const notify = useNotification();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [vehicleTypeFilter, setVehicleTypeFilter] = useState(null);

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

  const fetchParkingSessions = async () => {
    setLoading(true);
    try {
      const response = await getParkingSessionsApi({
        page: 0,
        size: 50,
        sort: "createdAt,desc",
      });

      if (response?.data?.content) {
        const formattedData = response.data.content.map((session, index) => ({
          key: session.id || index,
          time: new Date(session.createdAt).toLocaleDateString("vi-VN") + " " + new Date(session.createdAt).toLocaleTimeString("vi-VN"),
          vehicleType: session.vehicleType || "N/A",
          month: session.month ? "Có" : "Không",
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
    <Card
      title="Lịch sử"
      style={styles.card}
      styles={{
        header: styles.cardHead,
      }}
    >
      <Row gutter={12} style={{ marginBottom: 16 }}>
        <Col flex="auto">
          <Input
            placeholder="Tìm biển số..."
            prefix={<SearchOutlined style={{ color: "rgba(255,255,255,0.4)" }} />}
            style={styles.searchInCard}
            value={searchText}
            onChange={(e) => handleSearch(e.target.value)}
          />
        </Col>
        <Col style={{ width: 150 }}>
          <Select
            placeholder="Loại xe"
            allowClear
            style={{ width: "100%", ...styles.selectInCard }}
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
      <Spin spinning={loading}>
        <Table columns={columns} dataSource={filteredData} pagination={false} />
      </Spin>
    </Card>
  );
});

HistoryTable.displayName = 'HistoryTable';

export default HistoryTable;
