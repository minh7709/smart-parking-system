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
  Checkbox,
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
      // res.data is the ApiResponse.data (which is a Page object)
      // the actual array is in res.data.content
      const dataArray = res.data?.content || res.data || [];
      setRules(dataArray);
    } catch (error) {
      console.error("Fetch rules error:", error);
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
        ruleName: values.ruleName,
        vehicleType: values.vehicleType,
        pricingStrategy: values.pricingStrategy,
        penaltyFee: values.penaltyFee,
        isActive: true,
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
          message.error("Vui lòng thêm ít nhất 1 mốc giá cho Lũy tiến!");
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
          message.error("Cần 1 hoặc 2 mốc cho Khung giờ!");
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

      await adminApi.createPricingRule(payload);
      message.success("Lưu cấu hình thành công!");
      setIsModalVisible(false);
      form.resetFields();
      fetchRules();
    } catch (error) {
      console.error("Lưu cấu hình thất bại:", error);
      let errorMsg = error?.message || "Lỗi máy chủ!";
      if (error?.payload?.message) errorMsg = error.payload.message;
      if (error?.status === 409) {
        message.warning("Tên cấu hình đã tồn tại. Vui lòng đổi tên khác.");
      } else {
        message.error("Lỗi: " + errorMsg);
      }
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
