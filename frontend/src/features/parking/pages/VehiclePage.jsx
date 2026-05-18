import React, { useState, useEffect, useCallback, useRef } from "react";
import {
  Card, Form, Input, Select, Button, Row, Col,
  Table, Space, Tag, Modal, Popconfirm, Tooltip
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, SyncOutlined, SearchOutlined } from "@ant-design/icons";
import {
  getVehiclesApi,
  createVehicleApi,
  updateVehicleApi,
  deleteVehicleApi,
} from "../api/vehicleApi";
import { getSystemTypes } from "../../../utils/storage";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const VehiclePage = () => {
  const notify = useNotification();

  // ── Dữ liệu bảng ──────────────────────────────────────────────
  const [data, setData]           = useState([]);
  const [loading, setLoading]     = useState(false);
  const [total, setTotal]         = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize]   = useState(10);

  // ── Enum loại xe ──────────────────────────────────────────────
  const [vehicleTypes, setVehicleTypes] = useState([]);

  // ── Filter / search ───────────────────────────────────────────
  const [searchInput, setSearchInput] = useState("");        // giá trị người dùng đang gõ
  const [searchPlate, setSearchPlate] = useState("");        // giá trị đã debounce → trigger fetch
  const [filterType, setFilterType]   = useState(null);
  const debounceTimer = useRef(null);

  // ── Modal ─────────────────────────────────────────────────────
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingId, setEditingId]           = useState(null);
  const [form]                              = Form.useForm();

  // ── Load enum từ localStorage (sau khi đăng nhập đã cache) ──────────
  useEffect(() => {
    const { vehicleTypes: types } = getSystemTypes();
    setVehicleTypes(types ?? []);
  }, []);

  // ── Hàm fetch chính ───────────────────────────────────────────
  const fetchVehicles = useCallback(
    async (page = currentPage, size = pageSize, silent = false) => {
      if (!silent) setLoading(true);
      try {
        const params = {
          page: page - 1,   // Spring Pageable: 0-indexed
          size,
          sort: "createdAt,desc",
        };
        if (searchPlate.trim()) params.licensePlate = searchPlate.trim();
        if (filterType)         params.vehicleType  = filterType;

        const res = await getVehiclesApi(params);
        const pageData = res?.data;
        setData(pageData?.content ?? []);
        setTotal(pageData?.totalElements ?? 0);
      } catch (err) {
        if (!silent) notify.apiError(err, "Lỗi khi tải danh sách phương tiện");
      } finally {
        if (!silent) setLoading(false);
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [currentPage, pageSize, searchPlate, filterType]
  );

  // ── Re-fetch khi filter / page thay đổi ──────────────────────
  useEffect(() => {
    fetchVehicles(currentPage, pageSize);
  }, [currentPage, pageSize, searchPlate, filterType]); // eslint-disable-line

  // ── Polling 3 giây (silent) ───────────────────────────────────
  useEffect(() => {
    const id = setInterval(() => fetchVehicles(currentPage, pageSize, true), 3000);
    return () => clearInterval(id);
  }, [fetchVehicles, currentPage, pageSize]);

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

  // ── Filter loại xe ───────────────────────────────────────────
  const handleFilterType = (val) => {
    setFilterType(val ?? null);
    setCurrentPage(1);
  };

  // ── Reset bộ lọc ─────────────────────────────────────────────
  const handleResetFilters = () => {
    setSearchInput("");
    setSearchPlate("");
    setFilterType(null);
    setCurrentPage(1);
  };

  // ── Modal thêm / sửa ─────────────────────────────────────────
  const openCreateModal = () => {
    setEditingId(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const openEditModal = (record) => {
    setEditingId(record.id);
    form.setFieldsValue({
      licensePlate:  record.licensePlate,
      vehicleType:   typeof record.vehicleType === "object" ? record.vehicleType?.value : record.vehicleType,
      brand:         record.brand,
      customerName:  record.customerName,
      customerPhone: record.customerPhone,
    });
    setIsModalVisible(true);
  };

  const handleModalOk = () => {
    form.validateFields().then(async (values) => {
      try {
        if (editingId) {
          await updateVehicleApi(editingId, values);
          notify.success("Cập nhật phương tiện thành công!");
        } else {
          await createVehicleApi(values);
          notify.success("Thêm phương tiện mới thành công!");
        }
        setIsModalVisible(false);
        fetchVehicles(currentPage, pageSize);
      } catch (err) {
        notify.apiError(err, "Lỗi khi lưu phương tiện");
      }
    });
  };

  // ── Xóa ──────────────────────────────────────────────────────
  const handleDelete = async (id) => {
    try {
      await deleteVehicleApi(id);
      notify.success("Xóa phương tiện thành công!");
      fetchVehicles(currentPage, pageSize);
    } catch (error) {
      notify.apiError(error, "Lỗi khi xóa phương tiện");
    }
  };

  // ── Columns ──────────────────────────────────────────────────
  const columns = [
    { title: "Biển số xe", dataIndex: "licensePlate", key: "licensePlate", fontWeight: "bold" },
    {
      title: "Loại xe", dataIndex: "vehicleType", key: "vehicleType",
      render: (type) => {
        const label = typeof type === "object" ? type?.label : type;
        return <Tag color="blue">{label || "N/A"}</Tag>;
      },
    },
    { title: "Hãng xe",       dataIndex: "brand",         key: "brand" },
    { title: "Chủ xe",        dataIndex: "customerName",  key: "customerName" },
    { title: "Số điện thoại", dataIndex: "customerPhone", key: "customerPhone" },
    {
      title: "Hành động", key: "actions",
      render: (_, record) => (
        <Space size="middle">
          <Tooltip title="Chỉnh sửa">
            <Button type="text" icon={<EditOutlined style={{ color: "#1677ff" }} />} onClick={() => openEditModal(record)} />
          </Tooltip>
          <Tooltip title="Xóa">
            <Popconfirm title="Xóa phương tiện này?" onConfirm={() => handleDelete(record.id)} okText="Có" cancelText="Không">
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
          <Col span={8}>
            <Input
              placeholder="Tìm biển số xe..."
              prefix={<SearchOutlined />}
              value={searchInput}
              onChange={handleSearchChange}
              allowClear
              onClear={() => { setSearchInput(""); setSearchPlate(""); setCurrentPage(1); }}
            />
          </Col>
          <Col span={6}>
            <Select
              placeholder="Loại xe"
              style={{ width: "100%" }}
              value={filterType}
              onChange={handleFilterType}
              allowClear
            >
              {vehicleTypes.map((t) => (
                <Option key={t.value ?? t} value={t.value ?? t}>
                  {t.label ?? t}
                </Option>
              ))}
            </Select>
          </Col>
          <Col span={10} style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
            <Button onClick={handleResetFilters} icon={<SyncOutlined />}>Làm mới</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal} style={{ background: "#1677ff" }}>
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
            current:         currentPage,
            pageSize:        pageSize,
            total:           total,
            showSizeChanger: true,
            showTotal:       (t, r) => `${r[0]}–${r[1]} / ${t} phương tiện`,
            onChange: (page, size) => { setCurrentPage(page); setPageSize(size); },
          }}
        />
      </Card>

      {/* MODAL */}
      <Modal
        title={editingId ? "Cập nhật phương tiện" : "Thêm phương tiện mới"}
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={() => setIsModalVisible(false)}
        okText="Lưu lại"
        cancelText="Hủy"
        width={600}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 20 }}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Biển số xe" name="licensePlate" rules={[{ required: true, message: "Nhập biển số!" }]}>
                <Input disabled={!!editingId} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Loại xe" name="vehicleType" rules={[{ required: true, message: "Chọn loại xe!" }]}>
                <Select>
                  {vehicleTypes.map((t) => (
                    <Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Hãng xe" name="brand" rules={[{ required: true, message: "Nhập hãng xe!" }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Chủ xe" name="customerName" rules={[{ required: true, message: "Nhập tên chủ xe!" }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Số điện thoại" name="customerPhone" rules={[{ required: true, message: "Nhập số điện thoại!" }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default VehiclePage;
