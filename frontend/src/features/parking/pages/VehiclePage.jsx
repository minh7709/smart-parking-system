import React, { useState, useEffect } from "react";
import { Card, Form, Input, Select, Button, Row, Col, Table, Space, Tag, Modal, Popconfirm, Tooltip } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined, SyncOutlined } from "@ant-design/icons";
import { getVehiclesApi, createVehicleApi, updateVehicleApi, deleteVehicleApi } from "../api/vehicleApi";
import axiosClient from "../../../api/axiosClient";
import API_ENDPOINTS from "../../../api/endpoints";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const VehiclePage = () => {
  const notify = useNotification();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const [vehicleTypes, setVehicleTypes] = useState([]);
  
  const [searchPlate, setSearchPlate] = useState("");
  const [filterType, setFilterType] = useState(null);

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  const fetchVehicles = async (page = 1, size = 50) => {
    setLoading(true);
    try {
      const params = { page: page - 1, size: size };
      const res = await getVehiclesApi(params);
      if (res.data && res.data.content) {
        setData(res.data.content);
        setTotal(res.data.totalElements);
      } else {
        setData(res?.content || []);
        setTotal(res?.totalElements || 0);
      }
    } catch (err) {
      console.error(err);
      notification.error({ message: "L?i", description: "L?i khi t?i danh s�ch phuong ti?n!" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles(currentPage, pageSize);
  }, [currentPage, pageSize]);

  useEffect(() => {
    const fetchTypes = async () => {
      try {
        const vehicles = await axiosClient.get(API_ENDPOINTS.type.vehicleTypes);
        setVehicleTypes(vehicles.data || vehicles || []);
      } catch (e) {
        console.error("L?i khi load Lo?i xe", e);
      }
    };
    fetchTypes();
  }, []);

  const handleSearch = (value) => {
    setSearchPlate(value);
  };

  const handleResetFilters = () => {
    setSearchPlate("");
    setFilterType(null);
    setCurrentPage(1);
    fetchVehicles(1, pageSize);
  };

  const filteredData = data.filter((record) => {
    const matchPlate = (record.licensePlate || "").toLowerCase().includes(searchPlate.toLowerCase());
    const matchVehicleType = !filterType || record.vehicleType === filterType;
    return matchPlate && matchVehicleType;
  });

  const openCreateModal = () => {
    setEditingId(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const openEditModal = (record) => {
    setEditingId(record.id);
    form.setFieldsValue({
      licensePlate: record.licensePlate,
      vehicleType: record.vehicleType,
      brand: record.brand,
      customerName: record.customerName,
      customerPhone: record.customerPhone
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await deleteVehicleApi(id);
      notify.success("Xóa phương tiện thành công!");
      fetchVehicles(currentPage, pageSize);
    } catch (error) {
      console.error(error);
      notify.apiError(error, "Lỗi khi xóa phương tiện");
    }
  };

  const handleModalOk = () => {
    form.validateFields().then(async (values) => {
      try {
        const payload = { ...values };
        if (editingId) {
          await updateVehicleApi(editingId, payload);
          notify.success("Cập nhật phương tiện thành công!");
        } else {
          await createVehicleApi(payload);
          notify.success("Thêm phương tiện mới thành công!");
        }
        setIsModalVisible(false);
        fetchVehicles(currentPage, pageSize);
      } catch (err) {
        console.error(err);
        notify.apiError(err, "Lỗi khi lưu phương tiện");
      }
    });
  };

  const columns = [
    { title: "Biển số xe", dataIndex: "licensePlate", key: "licensePlate", fontWeight: "bold" },
    {
      title: "Loại xe", dataIndex: "vehicleType", key: "vehicleType",
      render: (type) => {
        const typeLabel = typeof type === 'object' ? type?.label : type;
        return <Tag color="blue">{typeLabel || "N/A"}</Tag>;
      }
    },
    { title: "Hãng xe", dataIndex: "brand", key: "brand" },
    { title: "Chủ xe", dataIndex: "customerName", key: "customerName" },
    { title: "Số điện thoại", dataIndex: "customerPhone", key: "customerPhone" },
    {
      title: "Hành động", key: "actions",
      render: (_, record) => (
        <Space size="middle">
          <Tooltip title="Chỉnh sửa"><Button type="text" icon={<EditOutlined style={{ color: '#1677ff' }} />} onClick={() => openEditModal(record)} /></Tooltip>
          <Tooltip title="Xóa"><Popconfirm title="Xóa phương tiện này?" onConfirm={() => handleDelete(record.id)} okText="Có" cancelText="Không"><Button type="text" danger icon={<DeleteOutlined />} /></Popconfirm></Tooltip>
        </Space>
      )
    }
  ];

  return (
    <div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
      <h2 style={{ color: "#141414", marginBottom: 24, fontSize: 24 }}>Quản Lý Phương Tiện</h2>
      <Card bordered={false} style={{ marginBottom: 24, borderRadius: 12 }}>
        <Row gutter={16} align="middle">
          <Col span={8}>
            <Input.Search placeholder="Nhập biển số xe..." value={searchPlate} onChange={(e) => handleSearch(e.target.value)} allowClear />
          </Col>
          <Col span={6}>
            <Select placeholder="Loại xe" style={{ width: "100%" }} value={filterType} onChange={setFilterType} allowClear>
              {vehicleTypes.map((t) => <Option key={t.value || t} value={t.value || t}>{t.label || t}</Option>)}
            </Select>
          </Col>
          <Col span={10} style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
            <Button onClick={handleResetFilters} icon={<SyncOutlined />}>Làm mới</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal} style={{ background: "#1677ff" }}>Tạo mới</Button>
          </Col>
        </Row>
      </Card>
      <Card bordered={false} style={{ borderRadius: 12 }}>
        <Table columns={columns} dataSource={filteredData} rowKey="id" loading={loading} pagination={{ current: currentPage, pageSize: pageSize, total: total, showSizeChanger: true, onChange: (page, size) => { setCurrentPage(page); setPageSize(size); } }} />
      </Card>
      <Modal title={editingId ? "Cập nhật" : "Thêm phương tiện mới"} open={isModalVisible} onOk={handleModalOk} onCancel={() => setIsModalVisible(false)} okText="Luu l?i" cancelText="H?y" width={600}>
        <Form form={form} layout="vertical" style={{ marginTop: 20 }}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Biển số xe" name="licensePlate" rules={[{ required: true, message: "Nhập biển số!" }]}><Input disabled={!!editingId} /></Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Loại xe" name="vehicleType" rules={[{ required: true, message: "Chọn loại xe!" }]}>
                <Select>
                  {vehicleTypes.map((t) => <Option key={t.value || t} value={t.value || t}>{t.label || t}</Option>)}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Hãng xe" name="brand" rules={[{ required: true, message: "Nhập hãng xe!" }]}><Input /></Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Chủ xe" name="customerName" rules={[{ required: true, message: "Nhập tên chủ xe!" }]}><Input /></Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Số điện thoại" name="customerPhone" rules={[{ required: true, message: "Nhập số điện thoại!" }]}><Input /></Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default VehiclePage;



