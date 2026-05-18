import React, { useState, useEffect, useCallback, useRef } from "react";
import {
  Card, Form, Input, Select, Button, Row, Col,
  DatePicker, Table, Space, Tag, Modal, Popconfirm, Tooltip
} from "antd";
import {
  PlusOutlined, EditOutlined, DeleteOutlined,
  SearchOutlined, SyncOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import {
  getSubscriptionsApi,
  createSubscriptionApi,
  updateSubscriptionApi,
  deleteSubscriptionApi,
} from "../api/subscriptionApi";
import { getSystemTypes } from "../../../utils/storage";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const RegisterPage = () => {
  const notify = useNotification();

  // ── Bảng dữ liệu ─────────────────────────────────────────────
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // ── Enums ─────────────────────────────────────────────────────
  const [subTypes, setSubTypes] = useState([]);
  const [subStatuses, setSubStatuses] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [vehicleTypes, setVehicleTypes] = useState([]);

  // ── Filter / search ───────────────────────────────────────────
  const [searchInput, setSearchInput] = useState("");    // giá trị đang gõ
  const [searchPlate, setSearchPlate] = useState("");    // sau debounce → trigger fetch
  const [filterType, setFilterType] = useState(null);
  const [filterStatus, setFilterStatus] = useState(null);
  const debounceTimer = useRef(null);

  // ── Modal ─────────────────────────────────────────────────────
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  // ── Load enum từ localStorage (cache khi đăng nhập) ──────────────────
  useEffect(() => {
    const types = getSystemTypes();
    setSubTypes(types.subscriptionTypes ?? []);
    setSubStatuses(types.subscriptionStatuses ?? []);
    setPaymentMethods(types.paymentMethods ?? []);
    setVehicleTypes(types.vehicleTypes ?? []);
  }, []);

  // ── Hàm fetch chính ───────────────────────────────────────────
  const fetchSubscriptions = useCallback(
    async (page = currentPage, size = pageSize, silent = false) => {
      if (!silent) setLoading(true);
      try {
        const params = {
          page: page - 1,   // Spring: 0-indexed
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

  // ── Re-fetch khi bộ lọc / trang thay đổi ─────────────────────
  useEffect(() => {
    fetchSubscriptions(currentPage, pageSize);
  }, [currentPage, pageSize, searchPlate, filterType, filterStatus]); // eslint-disable-line

  // ── Polling 3 giây (silent) ───────────────────────────────────
  useEffect(() => {
    const id = setInterval(() => fetchSubscriptions(currentPage, pageSize, true), 3000);
    return () => clearInterval(id);
  }, [fetchSubscriptions, currentPage, pageSize]);

  // ── Debounce search ──────────────────────────────────────────
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

  // ── Modal ─────────────────────────────────────────────────────
  const openCreateModal = () => {
    setEditingId(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const openEditModal = (record) => {
    setEditingId(record.id);
    form.setFieldsValue({
      licensePlate: record.licensePlate,
      vehicleType: typeof record.vehicleType === "object" ? record.vehicleType?.value : record.vehicleType,
      subType: typeof record.subType === "object" ? record.subType?.value : record.subType,
      subStatus: typeof record.subStatus === "object" ? record.subStatus?.value : record.subStatus,
      price: record.price,
      startDate: record.startDate ? dayjs(record.startDate) : null,
      endDate: record.endDate ? dayjs(record.endDate) : null,
    });
    setIsModalVisible(true);
  };

  const handleModalOk = () => {
    form.validateFields().then(async (values) => {
      try {
        const allValues = form.getFieldsValue();
        const payload = {
          licensePlate: allValues.licensePlate,
          subType: allValues.subType,
          subStatus: allValues.subStatus,
          startDate: allValues.startDate ? allValues.startDate.format("YYYY-MM-DDTHH:mm:ss") : null,
          endDate: allValues.endDate ? allValues.endDate.format("YYYY-MM-DDTHH:mm:ss") : null,
          paymentMethod: allValues.paymentMethod ?? "CASH",
        };

        if (editingId) {
          await updateSubscriptionApi(editingId, payload);
          notify.success("Cập nhật vé tháng thành công!");
        } else {
          await createSubscriptionApi(payload);
          notify.success("Đăng ký vé tháng thành công!");
        }
        setIsModalVisible(false);
        fetchSubscriptions(currentPage, pageSize);
      } catch (err) {
        notify.apiError(err, "Lỗi khi lưu vé tháng");
      }
    });
  };

  const handleDelete = async (id) => {
    try {
      await deleteSubscriptionApi(id);
      notify.success("Xóa vé tháng thành công!");
      fetchSubscriptions(currentPage, pageSize);
    } catch (error) {
      notify.apiError(error, "Lỗi khi xóa vé tháng");
    }
  };

  // ── Columns ──────────────────────────────────────────────────
  const SUB_TYPE_COLORS = { MONTHLY_1: "blue", MONTHLY_3: "cyan", YEARLY: "gold" };
  const SUB_STATUS_COLORS = { ACTIVE: "green", EXPIRED: "red", PENDING: "orange" };

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
      render: (d) => (d ? dayjs(d).format("DD/MM/YYYY") : ""),
    },
    {
      title: "Ngày kết thúc", dataIndex: "endDate", key: "endDate",
      render: (d) => (d ? dayjs(d).format("DD/MM/YYYY") : ""),
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
          <Tooltip title="Chỉnh sửa">
            <Button type="text" icon={<EditOutlined style={{ color: "#1677ff" }} />} onClick={() => openEditModal(record)} />
          </Tooltip>
          <Tooltip title="Xóa">
            <Popconfirm title="Bạn có chắc muốn xóa vé tháng này?" onConfirm={() => handleDelete(record.id)} okText="Có" cancelText="Không">
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
      {/* FILTER */}
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
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal} style={{ background: "#1677ff", borderRadius: 6, fontWeight: 500 }}>
              Tạo mới
            </Button>
          </Col>
        </Row>
      </Card>

      {/* TABLE */}
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
            showTotal: (t, r) => `${r[0]}–${r[1]} / ${t} vé`,
            onChange: (page, size) => { setCurrentPage(page); setPageSize(size); },
          }}
        />
      </Card>

      {/* MODAL */}
      <Modal
        title={editingId ? "Cập Nhật Vé Tháng" : "Đăng Ký Vé Tháng Mới"}
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={() => setIsModalVisible(false)}
        okText="Lưu lại"
        cancelText="Hủy"
        width={600}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 20 }}>
          <Row gutter={16}>
            {/* Biển số */}
            <Col span={editingId ? 12 : 24}>
              <Form.Item label="Biển số xe" name="licensePlate" rules={[{ required: true, message: "Vui lòng nhập biển số!" }]}>
                <Input placeholder="VD: 51H-12345" disabled={!!editingId} />
              </Form.Item>
            </Col>

            {/* Loại xe — chỉ hiển thị khi edit */}
            {editingId && (
              <Col span={12}>
                <Form.Item label="Loại xe" name="vehicleType" rules={[{ required: true, message: "Vui lòng chọn loại xe!" }]}>
                  <Select placeholder="Chọn loại xe" disabled>
                    {vehicleTypes.map((t) => <Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>)}
                  </Select>
                </Form.Item>
              </Col>
            )}
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Loại gói" name="subType" rules={[{ required: true, message: "Vui lòng chọn gói!" }]}>
                <Select placeholder="Chọn gói cước">
                  {subTypes.map((t) => <Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>)}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Ngày bắt đầu" name="startDate" rules={[{ required: true, message: "Vui lòng chọn ngày bắt đầu!" }]}>
                <DatePicker style={{ width: "100%" }} format="YYYY-MM-DD HH:mm:ss" showTime />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            {/* Tạo mới: chọn PTTT */}
            {!editingId && (
              <Col span={12}>
                <Form.Item label="Phương thức thanh toán" name="paymentMethod" rules={[{ required: true, message: "Vui lòng chọn thanh toán!" }]} initialValue={paymentMethods[0]?.value ?? paymentMethods[0]}>
                  <Select placeholder="Chọn PTTT">
                    {paymentMethods.map((m) => <Option key={m.value ?? m} value={m.value ?? m}>{m.label ?? m}</Option>)}
                  </Select>
                </Form.Item>
              </Col>
            )}

            {/* Cập nhật: hiển thị trạng thái, ngày kết thúc, giá tiền */}
            {editingId && (
              <>
                <Col span={12}>
                  <Form.Item label="Trạng thái" name="subStatus" rules={[{ required: true, message: "Vui lòng chọn trạng thái!" }]}>
                    <Select placeholder="Chọn trạng thái">
                      {subStatuses.map((s) => <Option key={s.value ?? s} value={s.value ?? s}>{s.label ?? s}</Option>)}
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item label="Ngày kết thúc" name="endDate" rules={[{ required: true, message: "Vui lòng chọn ngày kết thúc!" }]}>
                    <DatePicker style={{ width: "100%" }} format="YYYY-MM-DD HH:mm:ss" showTime />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item label="Giá tiền (VNĐ)" name="price">
                    <Input type="number" placeholder="VD: 150000" disabled />
                  </Form.Item>
                </Col>
              </>
            )}
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default RegisterPage;
