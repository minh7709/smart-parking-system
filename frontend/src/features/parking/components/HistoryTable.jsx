import React from "react";
import { Card, Table, Tag, Input } from "antd";
import { SearchOutlined } from "@ant-design/icons";

const styles = {
  card: {
    background: "rgba(255,255,255,0.03)",
    backdropFilter: "blur(12px)",
    border: "1px solid rgba(255,255,255,0.08)",
    borderRadius: 16,
  },
  cardHead: { borderBottom: "1px solid rgba(255,255,255,0.05)", color: "#fff" },
  searchInCard: {
    width: 220,
    background: "#1a1a1a",
    border: "1px solid rgba(255,255,255,0.1)",
    color: "#fff",
    borderRadius: 8,
  },
};

const HistoryTable = () => {
  const columns = [
    { title: "Thời gian", dataIndex: "time" },
    { title: "Biển số", dataIndex: "plate" },
    { title: "Sự kiện", dataIndex: "event" },
    {
      title: "Trạng thái",
      dataIndex: "status",
      render: (status) => {
        if (status === "Chưa thanh toán")
          return <Tag color="black">{status}</Tag>;
        if (status === "Đã thanh toán")
          return <Tag color="black">{status}</Tag>;
        return <Tag color="black">{status}</Tag>;
      },
    },
  ];

  const data = [
    {
      key: 1,
      time: "22:42:15",
      plate: "ABC-8842",
      event: "IN",
      status: "Chưa thanh toán",
    },
    {
      key: 2,
      time: "22:38:04",
      plate: "XYZ-1092",
      event: "OUT",
      status: "Đã thanh toán",
    },
    {
      key: 3,
      time: "22:35:58",
      plate: "LMN-4400",
      event: "IN",
      status: "Chưa thanh toán",
    },
    {
      key: 4,
      time: "22:21:12",
      plate: "UKN-0000",
      event: "FAIL",
      status: "Lỗi xác thực",
    },
  ];

  return (
    <Card
      title="Lịch sử"
      style={styles.card}
      styles={{
        header: styles.cardHead,
      }}
      // ĐƯA Ô SEARCH VÀO ĐÂY
      extra={
        <Input
          placeholder="Tìm biển số..."
          prefix={<SearchOutlined style={{ color: "rgba(255,255,255,0.4)" }} />}
          style={styles.searchInCard}
        />
      }
    >
      <Table columns={columns} dataSource={data} pagination={false} />
    </Card>
  );
};

export default HistoryTable;
