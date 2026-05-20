import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  DatePicker,
  Tag,
  Space,
  Divider,
  Checkbox,
  Tooltip,
  Badge,
  Popconfirm,
} from "antd";
import { useNotification } from "../../../hooks/useNotification";
import {
  PlusOutlined,
  DeleteOutlined,
  MinusCircleOutlined,
  CheckCircleFilled,
  StopOutlined,
} from "@ant-design/icons";
import { adminApi } from "../api/admin.api";

const { Option } = Select;

const PricingRuleConfig = () => {
  // Đổi tên component cho tổng quát hơn
  const notify = useNotification();
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState("FLAT_RATE");
  const [activatingId, setActivatingId] = useState(null);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editingIsActive, setEditingIsActive] = useState(true);
  const [form] = Form.useForm();

  const fetchRules = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getPricingRules();
      // res.data is the ApiResponse.data (which is a Page object)
      // the actual array is in res.data.content
      const dataArray = res.data?.content || res.data || [];
      setRules(dataArray);
    } catch (error) {
      console.error("Fetch rules error:", error);
      notify.apiError(error, "Không thể tải danh sách cấu hình");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleFinish = async (values) => {
    try {
      const payload = {
        ruleName: values.ruleName,
        vehicleType: values.vehicleType,
        pricingStrategy: values.pricingStrategy,
        penaltyFee: values.penaltyFee,
        isActive: isEditMode ? editingIsActive : true,
        startTime: values.startTime
          ? values.startTime.format("YYYY-MM-DDTHH:mm:ss")
          : null,
      };

      // Các trường chung (không dùng cho PROGRESSIVE)
      if (values.pricingStrategy !== "PROGRESSIVE") {
        payload.basePrice = values.basePrice;
      }
      if (values.pricingStrategy === "ROLLING_BLOCK") {
        payload.blockMinutes = values.blockMinutes;
      }
      if (values.pricingStrategy === "DAILY_CAPPED") {
        payload.maxPricePerDay = values.maxPricePerDay;
      }

      // Xử lý progressiveConfig riêng
      if (values.pricingStrategy === "PROGRESSIVE") {
        const configArray = values.progressiveConfig || [];
        if (configArray.length === 0) {
          notify.error("Vui lòng thêm ít nhất 1 mốc giá cho Lũy tiến!");
          return;
        }
        // Chuyển đổi sang { timeMilestone, price }
        payload.progressiveConfig = configArray.map((item) => ({
          timeMilestone: item.fromHour * 60, // giả sử fromHour là số giờ, muốn phút thì *60
          price: item.pricePerHour,
        }));
      } else if (values.pricingStrategy === "TIME_WINDOW") {
        const configArray = values.progressiveConfig || [];
        if (configArray.length === 0 || configArray.length > 2) {
          notify.error("Cần 1 hoặc 2 mốc cho Khung giờ!");
          return;
        }
        payload.progressiveConfig = configArray.map((item) => ({
          fromHour: item.fromHour,
          toHour: item.toHour,
          pricePerHour: item.pricePerHour,
          isFixed: item.isFixed || false,
        }));
      }
      // Gọi API

      if (isEditMode) {
        await adminApi.updatePricingRule(editingId, payload);
        notify.success("Cập nhật cấu hình thành công!");
      } else {
        await adminApi.createPricingRule(payload);
        notify.success("Lưu cấu hình thành công!");
      }
      setIsModalVisible(false);
      setIsEditMode(false);
      setEditingId(null);
      form.resetFields();
      fetchRules();
    } catch (error) {
      console.error("Lưu cấu hình thất bại:", error);
      if (error?.status === 409) {
        notify.warning("Tên cấu hình đã tồn tại. Vui lòng đổi tên khác.");
      } else {
        notify.apiError(error, "Lưu cấu hình thất bại");
      }
    }
  };

  const getEnumLabel = (value) => {
    if (value && typeof value === "object") {
      return value.label ?? value.value ?? "";
    }
    return value ?? "";
  };

  const getEnumValue = (value) => {
    if (value && typeof value === "object") {
      return value.value ?? value.label ?? "";
    }
    return value ?? "";
  };

  const handleToggleActive = async (record) => {
    try {
      setActivatingId(record.id);
      if (record.isActive) {
        notify.warning(
          "Không thể tắt thủ công cấu hình đang hoạt động. Hãy kích hoạt một cấu hình khác để thay thế.",
        );
        return;
      }

      await adminApi.activatePricingRule(record.id);
      notify.success(`Đã kích hoạt "${record.ruleName}"!`);
      fetchRules();
    } catch (error) {
      console.error("Toggle active error:", error);
      notify.apiError(error, "Không thể cập nhật trạng thái");
    } finally {
      setActivatingId(null);
    }
  };

  const openEditModal = (record) => {
    setIsEditMode(true);
    setEditingId(record.id);
    setEditingIsActive(!!record.isActive);

    const pricingStrategy = getEnumValue(record.pricingStrategy);
    setSelectedStrategy(pricingStrategy || "FLAT_RATE");

    let progressiveConfig = [];
    if (pricingStrategy === "PROGRESSIVE") {
      progressiveConfig = (record.progressiveConfig || []).map((item) => ({
        fromHour: item?.timeMilestone ? item.timeMilestone / 60 : item?.fromHour,
        pricePerHour: item?.price ?? item?.pricePerHour,
      }));
    } else if (pricingStrategy === "TIME_WINDOW") {
      progressiveConfig = (record.progressiveConfig || []).map((item) => ({
        fromHour: item?.fromHour,
        toHour: item?.toHour,
        pricePerHour: item?.pricePerHour,
        isFixed: item?.isFixed ?? false,
      }));
    }

    const normalizeNumber = (value) => {
      if (value && typeof value === "object") {
        return value.value ?? value.amount ?? null;
      }
      return value ?? null;
    };

    form.setFieldsValue({
      ruleName: record.ruleName,
      vehicleType: getEnumValue(record.vehicleType),
      pricingStrategy,
      basePrice: normalizeNumber(record.basePrice),
      blockMinutes: record.blockMinutes,
      maxPricePerDay: normalizeNumber(record.maxPricePerDay),
      penaltyFee: normalizeNumber(record.penaltyFee),
      startTime: null,
      progressiveConfig,
    });
    setIsModalVisible(true);
  };

  const deletePricingRule = async (id) => {
    try {
      await adminApi.deletePricingRule(id);
      notify.success("Đã xóa cấu hình!");
      fetchRules();
    } catch (error) {
      console.error("Delete error:", error);
      notify.apiError(error, "Không thể xóa cấu hình");
    }
  };
  const columns = [
    {
      title: "Tên quy tắc",
      dataIndex: "ruleName",
      key: "ruleName",
      render: (name, record) => (
        <Space>
          <Badge status={record.isActive ? "success" : "default"} dot />
          <span style={{ fontWeight: 600 }}>{name}</span>
        </Space>
      ),
    },
    {
      title: "Chiến thuật",
      dataIndex: "pricingStrategy",
      render: (s) => {
        const label = getEnumLabel(s);
        return <Tag color={label === "FLAT_RATE" ? "blue" : "cyan"}>{label}</Tag>;
      },
    },
    {
      title: "Loại xe",
      dataIndex: "vehicleType",
      render: (t) => <Tag>{getEnumLabel(t)}</Tag>,
    },
    {
      title: "Giá/Phí phạt",
      render: (_, record) => (
        <div>
          <div>
            Gốc:{" "}
            <strong>
              {record.basePrice?.toLocaleString()
                ? `${record.basePrice.toLocaleString()}đ`
                : "Lũy tiến"}
            </strong>
          </div>
          <div style={{ fontSize: "11px", color: "#888" }}>
            Phạt: {record.penaltyFee?.toLocaleString()}đ
          </div>
        </div>
      ),
    },
    {
      title: "Trạng thái",
      dataIndex: "isActive",
      render: (isActive) =>
        isActive ? (
          <Tag color="success" icon={<CheckCircleFilled />}>
            Đang hoạt động
          </Tag>
        ) : (
          <Tag color="default" icon={<StopOutlined />}>
            Chưa kích hoạt
          </Tag>
        ),
    },
    {
      title: "Kích hoạt",
      render: (_, record) => (
        <Tooltip
          title={
            record.isActive
              ? "Nhấn để vô hiệu hóa"
              : "Nhấn để kích hoạt chiến thuật này"
          }
        >
          <Button
            type={record.isActive ? "primary" : "default"}
            loading={activatingId === record.id}
            onClick={() => handleToggleActive(record)}
            style={{
              borderColor: record.isActive ? "#16a34a" : "#dc2626",
              color: record.isActive ? "#ffffff" : "#ffffff",
              backgroundColor: record.isActive ? "#16a34a" : "#dc2626",
              fontWeight: 600,
              letterSpacing: "0.2px",
              borderRadius: 8,
              minWidth: 130,
              transition: "all 0.3s ease",
            }}
          >
            {record.isActive ? "Đang kích hoạt" : "Kích hoạt"}
          </Button>
        </Tooltip>
      ),
    },
    {
      title: "Sửa",
      render: (_, record) => (
        <Button type="text" onClick={() => openEditModal(record)}>
          Sửa
        </Button>
      ),
    },
    {
      title: "Xóa",
      render: (_, record) => (
        <Popconfirm
          title="Bạn có chắc chắn muốn xóa cấu hình này?"
          onConfirm={() => deletePricingRule(record.id)}
          okText="Có"
          cancelText="Không"
        >
          <Button type="text" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Card
        title="Quản lý Cấu hình Giá Vé"
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setIsModalVisible(true)}
          >
            Thêm cấu hình
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={rules}
          rowKey="id"
          loading={loading}
        />
      </Card>

      <Modal
        title={isEditMode ? "Cập nhật Cấu hình Giá Vé" : "Thêm Cấu hình Giá Vé mới"}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setIsEditMode(false);
          setEditingId(null);
        }}
        footer={null}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          initialValues={{ pricingStrategy: "FLAT_RATE" }}
        >
          <Form.Item
            name="ruleName"
            label="Tên cấu hình"
            rules={[{ required: true }]}
          >
            <Input placeholder="VD: Vé lượt xe máy đồng giá 5k / Vé tháng Ô tô" />
          </Form.Item>

          <Space style={{ display: "flex" }} align="baseline">
            <Form.Item
              name="vehicleType"
              label="Loại xe"
              rules={[{ required: true }]}
              style={{ width: 300 }}
            >
              <Select placeholder="Chọn xe" disabled={isEditMode}>
                <Option value="CAR">Ô tô</Option>
                <Option value="MOTOR">Xe máy</Option>
                <Option value="BICYCLE">Xe đạp</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="pricingStrategy"
              label="Chiến thuật tính tiền"
              rules={[{ required: true }]}
              style={{ width: 300 }}
            >
              <Select onChange={(value) => setSelectedStrategy(value)} disabled={isEditMode}>
                {/* ĐÃ THÊM ĐỦ 5 CHIẾN THUẬT */}
                <Option value="FLAT_RATE">Giá cố định (Flat Rate)</Option>
                <Option value="ROLLING_BLOCK">
                  Theo Block (Rolling Block)
                </Option>
                <Option value="PROGRESSIVE">Lũy tiến (Progressive)</Option>
                <Option value="DAILY_CAPPED">
                  Giới hạn ngày (Daily Capped)
                </Option>
                <Option value="TIME_WINDOW">Khung giờ (Time Window)</Option>
              </Select>
            </Form.Item>
          </Space>

          <Divider titlePlacement="left" plain>
            Thông số chi tiết
          </Divider>

          {/* Giá cơ bản (Hiển thị cho tất cả TRỪ Lũy tiến) */}
          {selectedStrategy !== "PROGRESSIVE" && (
            <Form.Item
              name="basePrice"
              label="Giá cơ bản (VNĐ)"
              rules={[{ required: true }]}
            >
              <InputNumber style={{ width: "100%" }} />
            </Form.Item>
          )}

          {/* Hiện blockMinutes nếu là Rolling Block */}
          {selectedStrategy === "ROLLING_BLOCK" && (
            <Form.Item
              name="blockMinutes"
              label="Số phút mỗi Block"
              rules={[{ required: true }]}
            >
              <InputNumber style={{ width: "100%" }} />
            </Form.Item>
          )}

          {/* Hiện maxPricePerDay nếu là Daily Capped */}
          {selectedStrategy === "DAILY_CAPPED" && (
            <Form.Item
              name="maxPricePerDay"
              label="Giá tối đa một ngày (VNĐ)"
              rules={[{ required: true }]}
            >
              <InputNumber style={{ width: "100%" }} />
            </Form.Item>
          )}

          {/* Mảng động (Dynamic List) cho PROGRESSIVE và TIME_WINDOW */}
          {["PROGRESSIVE", "TIME_WINDOW"].includes(selectedStrategy) && (
            <div
              style={{
                background: "#f5f5f5",
                padding: 16,
                borderRadius: 8,
                marginBottom: 20,
              }}
            >
              <p style={{ fontWeight: "bold" }}>
                Cấu hình các mốc giá{" "}
                {selectedStrategy === "TIME_WINDOW" ? "(Tối đa 2 mốc)" : ""}
              </p>
              <Form.List
                name="progressiveConfig"
                rules={[
                  {
                    validator: async (_, names) => {
                      if (!names || names.length < 1)
                        return Promise.reject(
                          new Error("Phải có ít nhất 1 mốc giá!"),
                        );
                      if (
                        selectedStrategy === "TIME_WINDOW" &&
                        names.length > 2
                      ) {
                        return Promise.reject(
                          new Error(
                            "Chỉ được phép tối đa 2 mốc cấu hình cho Khung giờ!",
                          ),
                        );
                      }
                    },
                  },
                ]}
              >
                {(fields, { add, remove }, { errors }) => (
                  <>
                    {fields.map(({ key, name, ...restField }) => (
                      <Space
                        key={key}
                        style={{ display: "flex", marginBottom: 8 }}
                        align="baseline"
                      >
                        <Form.Item
                          {...restField}
                          name={[name, "fromHour"]}
                          label="Từ giờ"
                          rules={[{ required: true }]}
                        >
                          <InputNumber
                            placeholder="VD: 0"
                            style={{ width: 90 }}
                          />
                        </Form.Item>

                        <Form.Item
                          {...restField}
                          name={[name, "toHour"]}
                          label="Đến giờ"
                          rules={[{ required: true }]}
                        >
                          <InputNumber
                            placeholder="VD: 6"
                            style={{ width: 90 }}
                          />
                        </Form.Item>

                        <Form.Item
                          {...restField}
                          name={[name, "pricePerHour"]}
                          label="Giá/Giờ (VNĐ)"
                          rules={[{ required: true }]}
                        >
                          <InputNumber
                            placeholder="VD: 15000"
                            style={{ width: 120 }}
                          />
                        </Form.Item>

                        <Form.Item
                          {...restField}
                          name={[name, "isFixed"]}
                          valuePropName="checked"
                          style={{ paddingTop: 30 }}
                        >
                          <Checkbox>Cố định</Checkbox>
                        </Form.Item>

                        <MinusCircleOutlined
                          onClick={() => remove(name)}
                          style={{ color: "red", marginTop: 40 }}
                        />
                      </Space>
                    ))}
                    <Form.Item>
                      <Button
                        type="dashed"
                        onClick={() => add()}
                        block
                        icon={<PlusOutlined />}
                      >
                        Thêm mốc giá
                      </Button>
                      <Form.ErrorList errors={errors} />
                    </Form.Item>
                  </>
                )}
              </Form.List>
            </div>
          )}

          <Form.Item
            name="penaltyFee"
            label="Phí phạt trễ/sai quy định (VNĐ)"
            rules={[{ required: true }]}
          >
            <InputNumber style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item
            name="startTime"
            label="Ngày bắt đầu áp dụng"
            rules={[{ required: true }]}
          >
            <DatePicker style={{ width: "100%" }} showTime />
          </Form.Item>

          <Button type="primary" htmlType="submit" block size="large">
            Lưu cấu hình
          </Button>
        </Form>
      </Modal>
    </div>
  );
};

export default PricingRuleConfig;
