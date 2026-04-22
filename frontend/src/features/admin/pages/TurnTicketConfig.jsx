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
  message,
  Space,
  Divider,
} from "antd";
import {
  PlusOutlined,
  DeleteOutlined,
  MinusCircleOutlined,
} from "@ant-design/icons";
import { adminApi } from "../api/admin.api";

const { Option } = Select;

const PricingRuleConfig = () => {
  // Đổi tên component cho tổng quát hơn
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState("FLAT_RATE");
  const [form] = Form.useForm();

  const fetchRules = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getPricingRules();
      // Không cần lọc nữa, lấy toàn bộ 5 chiến thuật hiển thị ra bảng
      setRules(res.data || []);
    } catch (error) {
      message.error("Không thể tải danh sách cấu hình");
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
        ...values,
        startTime: values.startTime ? values.startTime.toISOString() : null,
        // Ép basePrice về null nếu là Lũy tiến
        basePrice:
          values.pricingStrategy === "PROGRESSIVE" ? null : values.basePrice,
        // Chỉ gửi mảng cấu hình động nếu chọn đúng 2 chiến thuật này
        progressiveConfig: ["PROGRESSIVE", "TIME_WINDOW"].includes(
          values.pricingStrategy,
        )
          ? values.progressiveConfig
          : null,
      };

      await adminApi.createPricingRule(payload);
      message.success("Lưu cấu hình thành công!");
      setIsModalVisible(false);
      form.resetFields();
      fetchRules();
    } catch (error) {
      message.error(
        "Lỗi: " +
          (error.response?.data?.message || "Kiểm tra lại dữ liệu nhập!"),
      );
    }
  };

  const columns = [
    {
      title: "Tên quy tắc",
      dataIndex: "ruleName",
      key: "ruleName",
      fontWeight: "bold",
    },
    {
      title: "Chiến thuật",
      dataIndex: "pricingStrategy",
      render: (s) => <Tag color={s === "FLAT_RATE" ? "blue" : "cyan"}>{s}</Tag>,
    },
    {
      title: "Loại xe",
      dataIndex: "vehicleType",
      render: (t) => <Tag>{t}</Tag>,
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
      title: "Hành động",
      render: (_, record) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => adminApi.deletePricingRule(record.id).then(fetchRules)}
        />
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
        title="Thêm Cấu hình Giá Vé mới"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
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
              <Select placeholder="Chọn xe">
                <Option value="CAR">Ô tô</Option>
                <Option value="MOTORBIKE">Xe máy</Option>
                <Option value="BICYCLE">Xe đạp</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="pricingStrategy"
              label="Chiến thuật tính tiền"
              rules={[{ required: true }]}
              style={{ width: 300 }}
            >
              <Select onChange={(value) => setSelectedStrategy(value)}>
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

          <Divider orientation="left" plain>
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
                          name={[name, "timeMilestone"]}
                          label="Phút/Giờ (Mốc)"
                          rules={[{ required: true }]}
                        >
                          <InputNumber placeholder="VD: 60" />
                        </Form.Item>
                        <Form.Item
                          {...restField}
                          name={[name, "price"]}
                          label="Giá (VNĐ)"
                          rules={[{ required: true }]}
                        >
                          <InputNumber placeholder="VD: 15000" />
                        </Form.Item>
                        <MinusCircleOutlined
                          onClick={() => remove(name)}
                          style={{ color: "red" }}
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
