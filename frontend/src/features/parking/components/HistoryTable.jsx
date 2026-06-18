import React, { useEffect, useState } from "react";
import { Card, Table, Input, Spin, Row, Col, Button, Modal, Image } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import {
  getParkingSessionsByLicensePlateApi,
  getParkingSessionImageApi,
} from "../api/parkingSession.api";
import { useNotification } from "../../../hooks/useNotification";

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
};

const HistoryTable = () => {
  const notify = useNotification();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [activePlate, setActivePlate] = useState("");
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [imageModalOpen, setImageModalOpen] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);
  const [imageInSrc, setImageInSrc] = useState(null);
  const [imageOutSrc, setImageOutSrc] = useState(null);
  const [imageLoading, setImageLoading] = useState(false);

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        const timestamp = parseInt(dateString, 10);
        if (!isNaN(timestamp)) {
          const parsedDate = new Date(timestamp);
          return (
            parsedDate.toLocaleDateString("vi-VN") +
            " " +
            parsedDate.toLocaleTimeString("vi-VN")
          );
        }
        return "N/A";
      }
      return (
        date.toLocaleDateString("vi-VN") +
        " " +
        date.toLocaleTimeString("vi-VN")
      );
    } catch (error) {
      notify.apiError(error, "Lỗi định dạng ngày tháng");
      return "N/A";
    }
  };

  const fetchHistory = async (plate) => {
    const normalizedPlate = plate?.trim();
    if (!normalizedPlate) {
      notify.error("Vui lòng nhập biển số xe.");
      return;
    }

    setLoading(true);
    try {
      const response = await getParkingSessionsByLicensePlateApi(normalizedPlate, {
        page: pagination.current - 1,
        size: pagination.pageSize,
        sort: "createdAt,desc",
      });

      if (response?.data?.content) {
        const formattedData = response.data.content.map((session, index) => ({
          key: session.id || index,
          time: formatDateTime(session.timeIn),
          vehicleType: session.vehicleType || "N/A",
          month: session.month != null ? (session.month ? "Có" : "Không") : "N/A",
          plateInOcr: session.plateInOcr || "N/A",
          finalPlate: session.finalPlate || "N/A",
          plateOutOcr: session.plateOutOcr || "N/A",
          status: session.status || "N/A",
          fullData: session,
        }));
        setData(formattedData);
        setPagination((prev) => ({
          ...prev,
          total: response.data.totalElements ?? response.data.total ?? prev.total,
        }));
      } else {
        setData([]);
      }
    } catch (error) {
      notify.apiError(error, "Lỗi tải dữ liệu");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!activePlate) return;
    fetchHistory(activePlate);
  }, [activePlate, pagination.current, pagination.pageSize]);

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
        // Gọi đồng thời cả 2 API, không quan tâm status là PARKED hay gì khác
        const [inResult, outResult] = await Promise.allSettled([
          getParkingSessionImageApi(selectedSession.id, "in"),
          getParkingSessionImageApi(selectedSession.id, "out"),
        ]);

        if (isActive) {
          // 1. Nếu tải thành công ảnh VÀO thì hiển thị
          if (inResult.status === "fulfilled") {
            inUrl = URL.createObjectURL(inResult.value);
            setImageInSrc(inUrl);
          } else {
            console.warn("Không tải được ảnh vào:", inResult.reason);
          }

          // 2. Nếu tải thành công ảnh RA thì hiển thị
          if (outResult.status === "fulfilled") {
            outUrl = URL.createObjectURL(outResult.value);
            setImageOutSrc(outUrl);
          } else {
            console.warn("Không tải được ảnh ra:", outResult.reason);
          }
        }
      } catch (error) {
        // Catch ở đây chỉ bắt lỗi block code, API error đã được allSettled xử lý
        notify.apiError(error, "Lỗi hệ thống khi tải ảnh");
      } finally {
        if (isActive) setImageLoading(false);
      }
    };

    fetchImages();

    // Cleanup function: Hủy URL cũ để tránh rò rỉ bộ nhớ khi unmount hoặc chạy lại effect
    return () => {
      isActive = false;
      if (inUrl) URL.revokeObjectURL(inUrl);
      if (outUrl) URL.revokeObjectURL(outUrl);
    };
    
  // Đã xóa selectedSession?.status?.value khỏi dependency array
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [imageModalOpen, selectedSession?.id]);

  const columns = [
    { title: "Thời gian", dataIndex: "time" },
    {
      title: "Loại xe",
      dataIndex: "vehicleType",
      render: (vehicleType) => vehicleType?.label || "N/A",
    },
    { title: "Đăng ký", dataIndex: "month" },
    { title: "Biển số vào", dataIndex: "plateInOcr" },
    { title: "Biển số thực tế", dataIndex: "finalPlate" },
    { title: "Biển số ra", dataIndex: "plateOutOcr" },
    { title: "Trạng thái", dataIndex: "status", render: (status) => status?.label || "N/A" },
  ];

  return (
    <>
      <style>{`
        .history-table-search input::placeholder { color: #808080 !important; opacity: 1 !important; display: block !important; }
        .history-table-wrapper :where(.ant-table, .ant-table-container, th, td) { background: #fff !important; color: #141414 !important; border-bottom: 2px solid #d1d5db !important; }
        .history-table-wrapper th { font-weight: bold; background: #f9fafb !important; color: #1d4ed8 !important; }
        .history-table-wrapper tr:hover>td { background: #f5f5f5 !important; }
        .history-table-wrapper .ant-empty-description { color: #141414 !important; }
      `}</style>
      <Card
        title="Lịch sử phiên gửi xe"
        style={styles.card}
        styles={{
          header: styles.cardHead,
        }}
      >
        <Row gutter={12} style={{ marginBottom: 16 }}>
          <Col flex="auto" className="history-table-search">
            <Input
              placeholder="Nhập biển số đầy đủ..."
              prefix={<SearchOutlined style={{ color: "#141414" }} />}
              style={styles.searchInCard}
              styles={{
                input: { color: "#141414" },
                textarea: { color: "#141414" },
              }}
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
            />
          </Col>
          <Col>
            <Button
              type="primary"
              onClick={() => {
                const plate = searchText.trim();
                if (!plate) {
                  notify.error("Vui lòng nhập biển số xe.");
                  return;
                }
                setActivePlate(plate);
                setPagination((prev) => ({ ...prev, current: 1 }));
              }}
            >
              Tìm
            </Button>
          </Col>
        </Row>
        <div className="history-table-wrapper">
          <Spin spinning={loading}>
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
};

export default HistoryTable;
