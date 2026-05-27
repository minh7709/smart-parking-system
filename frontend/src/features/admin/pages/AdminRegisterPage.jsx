import React, { useState, useEffect, useCallback, useMemo, useRef } from "react";
import {
  Card, Input, Select, Button, Row, Col,
  Table, Space, Tag, Popconfirm, Tooltip
} from "antd";
import { SearchOutlined, SyncOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import {
  getSubscriptionsApi,
  cancelAdminSubscriptionApi,
} from "../../parking/api/subscriptionApi";
import { getSystemTypes } from "../../../utils/storage";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const AdminRegisterPage = () => {
  const notify = useNotification();

  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const subTypes = useMemo(() => getSystemTypes("subscriptionTypes") ?? [], []);
  const subStatuses = useMemo(() => getSystemTypes("subscriptionStatuses") ?? [], []);

  const [searchInput, setSearchInput] = useState("");
  const [searchPlate, setSearchPlate] = useState("");
  const [filterType, setFilterType] = useState(null);
  const [filterStatus, setFilterStatus] = useState(null);
  const debounceTimer = useRef(null);

  const fetchSubscriptions = useCallback(
    async (page = currentPage, size = pageSize, silent = false) => {
      if (!silent) setLoading(true);
      try {
        const params = {
          page: page - 1,
          size,
          sort: "createdAt,desc",
        };
        if (searchPlate.trim()) params.licensePlate = searchPlate.trim();
        if (filterType) params.subType = filterType;
        if (filterStatus) params.subStatus = filterStatus;

        const res = await getSubscriptionsApi(params);
        const pageData = res?.data;
        setData(pageData?.content ?? []);
        setTotal(pageData?.totalElements ?? 0);
      } catch (err) {
        if (!silent) notify.apiError(err, "Lỗi khi tải danh sách vé tháng");
      } finally {
        if (!silent) setLoading(false);
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [currentPage, pageSize, searchPlate, filterType, filterStatus]
  );

  useEffect(() => {
    fetchSubscriptions(currentPage, pageSize);
  }, [currentPage, pageSize, searchPlate, filterType, filterStatus]); // eslint-disable-line

  useEffect(() => {
    const id = setInterval(() => fetchSubscriptions(currentPage, pageSize, true), 3000);
    return () => clearInterval(id);
  }, [fetchSubscriptions, currentPage, pageSize]);

  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearchInput(val);
    clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      setCurrentPage(1);
      setSearchPlate(val);
    }, 500);
  };

  const handleFilterType = (val) => { setFilterType(val ?? null); setCurrentPage(1); };
  const handleFilterStatus = (val) => { setFilterStatus(val ?? null); setCurrentPage(1); };

  const handleResetFilters = () => {
    setSearchInput("");
    setSearchPlate("");
    setFilterType(null);
    setFilterStatus(null);
    setCurrentPage(1);
  };
  const handleCancel = async (id) => {
    try {
      await cancelAdminSubscriptionApi(id);
      notify.success("Hủy vé tháng thành công!");
      fetchSubscriptions(currentPage, pageSize);
    } catch (error) {
      notify.apiError(error, "Lỗi khi hủy vé tháng");
    }
  };

  const SUB_TYPE_COLORS = { MONTHLY_1: "blue", MONTHLY_3: "cyan", YEARLY: "gold" };
  const SUB_STATUS_COLORS = { ACTIVE: "green", EXPIRED: "red", PENDING: "orange" };
  const getSubStatusValue = (status) => (typeof status === "object" ? status?.value : status);
  const canCancel = (status) => ["PENDING", "ACTIVE"].includes(getSubStatusValue(status));

  const columns = [
    { title: "Biển số xe", dataIndex: "licensePlate", key: "licensePlate", fontWeight: "bold" },
    {
      title: "Loại xe", dataIndex: "vehicleType", key: "vehicleType",
      render: (t) => <Tag color="blue">{typeof t === "object" ? t?.label : t}</Tag>,
    },
    {
      title: "Loại gói", dataIndex: "subType", key: "subType",
      render: (t) => {
        const val = typeof t === "object" ? t?.value : t;
        const label = typeof t === "object" ? t?.label : t;
        return <Tag color={SUB_TYPE_COLORS[val] ?? "default"}>{label ?? val}</Tag>;
      },
    },
    {
      title: "Giá tiền", dataIndex: "price", key: "price",
      render: (p) => `${p?.toLocaleString("vi-VN")} đ`,
    },
    {
      title: "Ngày bắt đầu", dataIndex: "startDate", key: "startDate",
      render: (d) => (d ? dayjs(d).format("YYYY-MM-DDTHH:mm:ss") : ""),
    },
    {
      title: "Ngày kết thúc", dataIndex: "endDate", key: "endDate",
      render: (d) => (d ? dayjs(d).format("YYYY-MM-DDTHH:mm:ss") : ""),
    },
    {
      title: "Trạng thái", dataIndex: "subStatus", key: "subStatus",
      render: (s) => {
        const val = typeof s === "object" ? s?.value : s;
        const label = typeof s === "object" ? s?.label : s;
        return <Tag color={SUB_STATUS_COLORS[val] ?? "default"}>{label ?? val}</Tag>;
      },
    },
    {
      title: "Hành động", key: "actions",
      render: (_, record) => (
        <Space size="middle">
          <Tooltip title="Hủy vé">
            <Popconfirm
              title="Bạn chắc muốn hủy vé này?"
              onConfirm={() => handleCancel(record.id)}
              okText="Hủy"
              cancelText="Không"
              disabled={!canCancel(record.subStatus)}
            >
              <Button type="text" danger disabled={!canCancel(record.subStatus)}>
                Hủy
              </Button>
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
      <Card bordered={false} style={{ marginBottom: 24, borderRadius: 12 }}>
        <Row gutter={16} align="middle">
          <Col span={7}>
            <Input
              placeholder="Nhập biển số xe cần tìm..."
              prefix={<SearchOutlined />}
              value={searchInput}
              onChange={handleSearchChange}
              allowClear
              onClear={() => { setSearchInput(""); setSearchPlate(""); setCurrentPage(1); }}
            />
          </Col>
          <Col span={5}>
            <Select placeholder="Loại gói" style={{ width: "100%" }} value={filterType} onChange={handleFilterType} allowClear>
              {subTypes.map((t) => (
                <Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
              ))}
            </Select>
          </Col>
          <Col span={5}>
            <Select placeholder="Trạng thái" style={{ width: "100%" }} value={filterStatus} onChange={handleFilterStatus} allowClear>
              {subStatuses.map((s) => (
                <Option key={s.value ?? s} value={s.value ?? s}>{s.label ?? s}</Option>
              ))}
            </Select>
          </Col>
          <Col span={7} style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
            <Button onClick={handleResetFilters} icon={<SyncOutlined />} style={{ borderRadius: 6 }}>Làm mới</Button>
          </Col>
        </Row>
      </Card>

      <Card bordered={false} style={{ borderRadius: 12 }}>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showTotal: (t, r) => `${r[0]}-${r[1]} / ${t} vé`,
            onChange: (page, size) => { setCurrentPage(page); setPageSize(size); },
          }}
        />
      </Card>
    </div>
  );
};

export default AdminRegisterPage;