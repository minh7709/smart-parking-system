import React, { useState, useEffect } from "react";
import { Card, Table, Input, Spin, Select, Row, Col, Modal, Image } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { getParkingSessionsApi, getParkingSessionImageApi } from "../api/parkingSession.api";
import { useNotification } from "../../../hooks/useNotification";
import { countParkingSessionsApi } from "../api/parkingSession.api";
import { getVehicleTypesApi } from "../api/vehicleApi";
import { getSystemTypes } from "../../../utils/storage";

const styles = {
  card: {
    background: "#ffffff",
    backdropFilter: "blur(12px)",
    border: "1px solid #e5e7eb",
    borderRadius: 16,
    boxShadow: "0 14px 30px rgba(15, 23, 42, 0.12)",
  },
  cardHead: { borderBottom: "1px solid #e5e7eb", color: "#141414" },
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

const ParkedTable = React.forwardRef(({ refreshTrigger }, ref) => {
  const notify = useNotification();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [debouncedSearchText, setDebouncedSearchText] = useState(""); // Lưu text sau khi debounce
  const [vehicleTypeFilter, setVehicleTypeFilter] = useState(null);
  const [parkedCount, setParkedCount] = useState(0);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [imageModalOpen, setImageModalOpen] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);
  const [imageInSrc, setImageInSrc] = useState(null);
  const [imageOutSrc, setImageOutSrc] = useState(null);
  const [imageLoading, setImageLoading] = useState(false);
  const vehicleTypes = getSystemTypes('vehicleTypes') ?? [];

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
      notify.apiError(error, "Lỗi định dạng ngày tháng");
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
        page: pagination.current - 1,
        size: pagination.pageSize,
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
        setPagination((prev) => ({
          ...prev,
          total: response.data.totalElements ?? response.data.total ?? prev.total,
        }));
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
    setPagination((prev) => ({ ...prev, current: 1 }));
  }, [debouncedSearchText, vehicleTypeFilter]);

  useEffect(() => {
    fetchParkingSessions();
  }, [debouncedSearchText, vehicleTypeFilter, pagination.current, pagination.pageSize]);

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
  }, [debouncedSearchText, vehicleTypeFilter, pagination.current, pagination.pageSize]);

  const handleSearch = (value) => {
    setSearchText(value);
  };

  const handleRowClick = (record) => {
    setSelectedSession(record.fullData);
    setImageModalOpen(true);
  };

  const handleCloseImageModal = () => {
    setImageModalOpen(false);
    setSelectedSession(null);
    setImageInSrc(null);
    setImageOutSrc(null);
  };

  useEffect(() => {
    if (!imageModalOpen || !selectedSession?.id) return;

    let isActive = true;
    let inUrl = null;
    let outUrl = null;

    const fetchImages = async () => {
      setImageLoading(true);
      setImageInSrc(null);
      setImageOutSrc(null);

      try {
        if (selectedSession.status?.value === 'PARKED') {
          const inBlob = await getParkingSessionImageApi(selectedSession.id, "in");
          inUrl = URL.createObjectURL(inBlob);
          if (isActive) setImageInSrc(inUrl);
        } else {
          const [inBlob, outBlob] = await Promise.all([
            getParkingSessionImageApi(selectedSession.id, "in"),
            getParkingSessionImageApi(selectedSession.id, "out"),
          ]);
          inUrl = URL.createObjectURL(inBlob);
          outUrl = URL.createObjectURL(outBlob);
          if (isActive) {
            setImageInSrc(inUrl);
            setImageOutSrc(outUrl);
          }
        }
      } catch (error) {
        notify.apiError(error, "Lỗi tải ảnh");
      } finally {
        if (isActive) setImageLoading(false);
      }
    };

    fetchImages();

    return () => {
      isActive = false;
      if (inUrl) URL.revokeObjectURL(inUrl);
      if (outUrl) URL.revokeObjectURL(outUrl);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [imageModalOpen, selectedSession?.id, selectedSession?.status?.value]);

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
        .parked-table-search input::placeholder, .parked-table-search .ant-select-selection-placeholder { color: #808080 !important; opacity: 1 !important; display: block !important; }
        .parked-table-search .ant-select-selector { background: #fff !important; border-color: #d9d9d9 !important; }
        .parked-table-search .ant-select-selection-item, .parked-table-search .ant-select-arrow { color: #141414 !important; }
        .parked-table-wrapper :where(.ant-table, .ant-table-container, th, td) { background: #fff !important; color: #141414 !important; border-bottom: 2px solid #d1d5db !important; }
        .parked-table-wrapper th { font-weight: bold; background: #f9fafb !important; color: #1d4ed8 !important; }
        .parked-table-wrapper tr:hover>td { background: #f5f5f5 !important; }
        .parked-table-wrapper .ant-empty-description { color: #141414 !important; }
      `}</style>
      <Card
        title="Danh sách xe đang đỗ" extra={<span style={{ color: "#1677ff", fontWeight: "bold", fontSize: 16 }}>Hiện đang đỗ: {parkedCount}</span>} style={styles.card}
        styles={{
          header: styles.cardHead,
        }}
      >
        <Row gutter={12} style={{ marginBottom: 16 }}>
          <Col flex="auto" className="parked-table-search">
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
          <Col style={{ width: 150 }} className="parked-table-search">
            <Select
              placeholder={<span style={{ color: "#141414" }}>Loại xe</span>}
              allowClear
              style={{ width: "100%", ...styles.selectInCard }}
              value={vehicleTypeFilter}
              onChange={(value) => setVehicleTypeFilter(value)}
              options={vehicleTypes}
            />
          </Col>
        </Row>
        <div className="parked-table-wrapper">
          <Spin spinning={loading}>
            {/* Truyền trực tiếp state data thay vì filteredData */}
            <Table
              columns={columns}
              dataSource={data}
              pagination={{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
              }}
              onChange={(nextPagination) => {
                setPagination((prev) => ({
                  ...prev,
                  current: nextPagination.current,
                  pageSize: nextPagination.pageSize,
                }));
              }}
              onRow={(record) => ({
                onClick: () => handleRowClick(record),
              })}
            />
          </Spin>
        </div>
      </Card>

      <Modal
        title="Ảnh phiên gửi xe"
        open={imageModalOpen}
        onCancel={handleCloseImageModal}
        footer={null}
        width={900}
      >
        <Spin spinning={imageLoading}>
          <Row gutter={16}>
            <Col span={12}>
              <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Ảnh vào</div>
              {imageInSrc ? (
                <Image src={imageInSrc} alt="image-in" style={{ width: "100%" }} />
              ) : (
                <div style={{ width: "100%", height: 220, background: "#f0f0f0" }} />
              )}
            </Col>
            <Col span={12}>
              <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Ảnh ra</div>
              {imageOutSrc ? (
                <Image src={imageOutSrc} alt="image-out" style={{ width: "100%" }} />
              ) : (
                <div style={{ width: "100%", height: 220, background: "#f0f0f0" }} />
              )}
            </Col>
          </Row>
        </Spin>
      </Modal>
    </>
  );
});

ParkedTable.displayName = 'ParkedTable';

export default ParkedTable;