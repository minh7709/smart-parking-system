import React, { useState, useEffect } from "react";
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Row,
  Col,
  DatePicker,
  notification,
  Table,
  Space,
  Tag,
  Modal,
  Popconfirm,
  Tooltip
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import {
  getSubscriptionsApi,
  createSubscriptionApi,
  updateSubscriptionApi,
  deleteSubscriptionApi,
  getSubscriptionByLicensePlateApi,
} from "../api/subscriptionApi";
import axiosClient from "../../../api/axiosClient";
import API_ENDPOINTS from "../../../api/endpoints";

const { Option } = Select;  

const RegisterPage = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // States lưu cấu hình Enum từ Backend
  const [subTypes, setSubTypes] = useState([]);
  const [subStatuses, setSubStatuses] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
 
  
  // Filters
  const [searchPlate, setSearchPlate] = useState("");
  const [filterType, setFilterType] = useState(null);
  const [filterStatus, setFilterStatus] = useState(null);

  // Modal states
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  const fetchSubscriptions = async (page = 1, size = 50) => {
    setLoading(true);
    try {
      const params = { page: page - 1, size: size };
      const res = await getSubscriptionsApi(params);
      if (res.data && res.data.content) {
        setData(res.data.content);
        setTotal(res.data.totalElements);
      } else {
        setData(res?.content || []);
        setTotal(res?.totalElements || 0);
      }
    } catch (err) {
      console.error(err);
      notification.error({ message: "Lỗi", description: "Lỗi khi tải danh sách vé tháng!" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscriptions(currentPage, pageSize);
  }, [currentPage, pageSize]);

  useEffect(() => {
    const fetchAppTypes = async () => {
      try {
        const [types, statuses, payments, vehicles] = await Promise.all([
          axiosClient.get(API_ENDPOINTS.type.subscriptionTypes),
          axiosClient.get(API_ENDPOINTS.type.subscriptionStatuses),
          axiosClient.get(API_ENDPOINTS.type.paymentMethods),
          axiosClient.get(API_ENDPOINTS.type.vehicleTypes),
        ]);
        setSubTypes(types.data || types || []);
        setSubStatuses(statuses.data || statuses || []);
        setPaymentMethods(payments.data || payments || []);
        setVehicleTypes(vehicles.data || vehicles || []);
      } catch(e) {
        console.error("Lỗi khi load tham số Type", e);
      }
    };
    fetchAppTypes();
  }, []);

  const handleSearch = (value) => {
    setSearchPlate(value);
  };

  const handleResetFilters = () => {
    setSearchPlate("");
    setFilterType(null);
    setFilterStatus(null);
    setCurrentPage(1);
    fetchSubscriptions(1, pageSize);
  };

  const filteredData = data.filter((record) => {
    const matchPlate = (record.licensePlate || "").toLowerCase().includes(searchPlate.toLowerCase());
    const matchType = !filterType || record.subType === filterType;
    const matchStatus = !filterStatus || record.subStatus === filterStatus;
    return matchPlate && matchType && matchStatus;
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
      subType: record.subType,
      price: record.price,
      startDate: record.startDate ? dayjs(record.startDate) : null,
      endDate: record.endDate ? dayjs(record.endDate) : null,
      subStatus: record.subStatus,
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await deleteSubscriptionApi(id);
      notification.success({ message: "Thành công", description: "Xóa vé tháng thành công!" });
      fetchSubscriptions(currentPage, pageSize);
    } catch (error) {
      console.error(error);
      notification.error({ message: "Lỗi", description: "Lỗi khi xóa vé tháng" });
    }
  };

  const handleModalOk = () => {
    form.validateFields().then(async (values) => {
      try {
        // Lấy tất cả giá trị form hiện tại (bao gồm các trường bị disabled)
        const allValues = form.getFieldsValue();
        
        // Gọi API với đúng 5 trường chuẩn của Backend, không gửi thêm các trường thừa
        const payload = {
          licensePlate: allValues.licensePlate,
          subType: allValues.subType,
          startDate: allValues.startDate ? allValues.startDate.format("YYYY-MM-DDTHH:mm:ss") : null,
          paymentMethod: allValues.paymentMethod || "CASH" // Bắt buộc phải có vì Backend đặt @NotNull
        };

        if (editingId) {
          await updateSubscriptionApi(editingId, payload);
          notification.success({ message: "Thành công", description: "Cập nhật vé tháng thành công!" });
        } else {
          await createSubscriptionApi(payload);
          notification.success({ message: "Thành công", description: "Đăng ký vé tháng thành công!" });
        }
        setIsModalVisible(false);
        fetchSubscriptions(currentPage, pageSize);
      } catch (err) {
        console.error(err);
        const errorData = err.payload?.message;
        let errorMsg = "Có lỗi xảy ra, vui lòng thử lại!";
        
        if (errorData) {
          if (errorData.fieldErrors && errorData.fieldErrors.length > 0) {
            errorMsg = errorData.fieldErrors.map(f => f.message).join(", ");
          } else if (errorData) {
            errorMsg = errorData;
          }
        }
        
        notification.error({ message: err.payload?.errorCode || "Lỗi", description: errorMsg });
      }
    });
  };

  const columns = [
    {
      title: "Biển số xe",
      dataIndex: "licensePlate",
      key: "licensePlate",
      fontWeight: "bold",
    },
    {
      title: "Loại xe",
      dataIndex: "vehicleType",
      key: "vehicleType",
      render: (type) => {
        const labels = {
          MOTORBIKE: "Xe máy",
          CAR: "Ô tô",
          BICYCLE: "Xe đạp"
        };
        return <Tag color="blue">{type ? (labels[type] || type) : "N/A"}</Tag>;
      },
    },
    {
      title: "Loại gói",
      dataIndex: "subType",
      key: "subType",
      render: (type) => {
        const colors = {
          MONTHLY_1: "blue",
          MONTHLY_3: "cyan",
          YEARLY: "gold",
        };
        const labels = {
          MONTHLY_1: "1 Tháng",
          MONTHLY_3: "3 Tháng",
          YEARLY: "1 Năm",
        };
        return <Tag color={colors[type] || "default"}>{labels[type] || type}</Tag>;
      },
    },
    {
      title: "Giá tiền",
      dataIndex: "price",
      key: "price",
      render: (price) => `${price?.toLocaleString("vi-VN")} đ`,
    },
    {
      title: "Ngày bắt đầu",
      dataIndex: "startDate",
      key: "startDate",
      render: (date) => (date ? dayjs(date).format("DD/MM/YYYY") : ""),
    },
    {
      title: "Ngày kết thúc",
      dataIndex: "endDate",
      key: "endDate",
      render: (date) => (date ? dayjs(date).format("DD/MM/YYYY") : ""),
    },
    {
      title: "Trạng thái",
      dataIndex: "subStatus",
      key: "subStatus",
      render: (status) => {
        const colors = {
          ACTIVE: "green",
          EXPIRED: "red",
          PENDING: "orange",
        };
        return <Tag color={colors[status] || "default"}>{status}</Tag>;
      },
    },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <Space size="middle">
          <Tooltip title="Chỉnh sửa">
            <Button
              type="text"
              icon={<EditOutlined style={{ color: "#1677ff" }} />}
              onClick={() => openEditModal(record)}
            />
          </Tooltip>
          <Tooltip title="Xóa">
            <Popconfirm
              title="Bạn có chắc chắn muốn xóa vé tháng này?"
              onConfirm={() => handleDelete(record.id)}
              okText="Có"
              cancelText="Không"
            >
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
      <h2 style={{ color: "#141414", marginBottom: 24, fontSize: 24 }}>
        Quản Lý Vé Tháng
      </h2>

      {/* FILTER SECTION */}
      <Card bordered={false} style={{ marginBottom: 24, borderRadius: 12 }}>
        <Row gutter={16} align="middle">
          <Col span={8}>
            <Input.Search
              placeholder="Nhập biển số xe cần tìm..."
              value={searchPlate}
              onChange={(e) => handleSearch(e.target.value)}
              allowClear
            />
          </Col>
          <Col span={5}>
            <Select
              placeholder="Loại gói"
              style={{ width: "100%" }}
              value={filterType}
              onChange={(val) => setFilterType(val)}
              allowClear
            >
              {subTypes.map((type) => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </Col>
          <Col span={5}>
            <Select
              placeholder="Trạng thái"
              style={{ width: "100%" }}
              value={filterStatus}
              onChange={(val) => setFilterStatus(val)}
              allowClear
            >
              {subStatuses.map((status) => (
                <Option key={status} value={status}>
                  {status}
                </Option>
              ))}
            </Select>
          </Col>
          <Col span={6} style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
            <Button 
              onClick={handleResetFilters} 
              icon={<SyncOutlined />}
              style={{ borderRadius: 6 }}
            >
              Làm mới
            </Button>
            <Button 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={openCreateModal}
              style={{ background: "#1677ff", borderColor: "#1677ff", borderRadius: 6, fontWeight: 500 }}
            >
              Tạo mới
            </Button>
          </Col>
        </Row>
      </Card>

      {/* TABLE SECTION */}
      <Card bordered={false} style={{ borderRadius: 12 }}>
        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
          }}
        />
      </Card>

      {/* CREATE/EDIT MODAL */}
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
            <Col span={editingId ? 12 : 24}>
              <Form.Item
                label="Biển số xe"
                name="licensePlate"
                rules={[{ required: true, message: "Vui lòng nhập biển số!" }]}
              >
                <Input placeholder="VD: 51H-12345" disabled={!!editingId} />
              </Form.Item>
            </Col>

            {editingId && (
              <Col span={12}>
                <Form.Item
                  label="Loại xe"
                  name="vehicleType"
                  rules={[{ required: true, message: "Vui lòng chọn loại xe!" }]}
                >
                  <Select placeholder="Chọn loại xe" disabled={!!editingId}>
                    {vehicleTypes.map((type) => (
                      <Option key={type} value={type}>
                        {type === 'MOTORBIKE' ? 'Xe máy' : type === 'CAR' ? 'Ô tô' : type === 'BICYCLE' ? 'Xe đạp' : type}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            )}
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Loại gói"
                name="subType"
                rules={[{ required: true, message: "Vui lòng chọn gói!" }]}
              >
                <Select placeholder="Chọn gói cước">
                  {subTypes.map((type) => (
                    <Option key={type} value={type}>
                      {type}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                label="Ngày bắt đầu"
                name="startDate"
                rules={[{ required: true, message: "Vui lòng chọn ngày bắt đầu!" }]}
              >
                <DatePicker style={{ width: "100%" }} format="YYYY-MM-DD HH:mm:ss" showTime />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            {/* Khi tạo mới: hiển thị chọn phương thức thanh toán */}
            {!editingId && (
              <Col span={12}>
                <Form.Item
                  label="Phương thức thanh toán"
                  name="paymentMethod"
                  rules={[{ required: true, message: "Vui lòng chọn thanh toán!" }]}
                  initialValue={paymentMethods[0] || "CASH"}
                >
                  <Select placeholder="Chọn PTTT">
                    {paymentMethods.map((method) => (
                      <Option key={method} value={method}>
                        {method}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            )}

            {/* Khi cập nhật: hiển thị trạng thái và ngày kết thúc thay vì PTTT */}
            {editingId && (
              <>
                <Col span={12}>
                  <Form.Item
                    label="Trạng thái"
                    name="subStatus"
                    rules={[{ required: true, message: "Vui lòng chọn trạng thái!" }]}
                  >
                    <Select placeholder="Chọn trạng thái">
                      {subStatuses.map((status) => (
                        <Option key={status} value={status}>
                          {status}
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    label="Ngày kết thúc"
                    name="endDate"
                    rules={[{ required: true, message: "Vui lòng chọn ngày kết thúc!" }]}
                  >
                    <DatePicker style={{ width: "100%" }} format="YYYY-MM-DD HH:mm:ss" showTime />
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    label="Giá tiền (VNĐ)"
                    name="price"
                  >
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



