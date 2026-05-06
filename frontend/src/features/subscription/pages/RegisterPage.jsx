import React from "react";
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Row,
  Col,
  DatePicker,
  message,
} from "antd";
import { SaveOutlined, CarOutlined, UserOutlined } from "@ant-design/icons";
import { AppLayout } from "../../../components/Layout/AppLayout";

const { Option } = Select;

const RegisterPage = () => {
  const [form] = Form.useForm();

  const onFinish = (values) => {
    console.log("Dữ liệu gửi đi:", values);
    // Chỗ này sau này sẽ gọi API lưu vào Database
    message.success({
      content: "Đăng ký vé tháng thành công!",
      style: { marginTop: "10vh" },
    });
    form.resetFields();
  };

  return (
    <>
      <div style={{ maxWidth: 900, margin: "0 auto", paddingBottom: 40 }}>
        <h2
          style={{
            color: "#fff",
            fontSize: 24,
            marginBottom: 24,
            textAlign: "center",
          }}
        >
          Đăng Ký Vé Tháng
        </h2>

        <Form form={form} layout="vertical" onFinish={onFinish} size="large">
          <Row gutter={24}>
            {/* CỘT 1: THÔNG TIN PHƯƠNG TIỆN & KHÁCH HÀNG */}
            <Col span={12}>
              <Card
                title={
                  <span style={{ color: "#00b96b" }}>
                    <CarOutlined /> Thông tin phương tiện
                  </span>
                }
                bordered={false}
                style={{
                  background: "#141414",
                  border: "1px solid #1f1f1f",
                  borderRadius: 16,
                  height: "100%",
                }}
                styles={{ header: { borderBottom: "1px solid #1f1f1f" } }}
              >
                <Form.Item
                  label={<span style={{ color: "#aaa" }}>Biển số xe</span>}
                  name="licensePlate"
                  rules={[
                    { required: true, message: "Vui lòng nhập biển số!" },
                  ]}
                >
                  <Input
                    placeholder="VD: 51H-12345"
                    style={{
                      background: "#1a1a1a",
                      border: "1px solid #333",
                      color: "#fff",
                    }}
                  />
                </Form.Item>

                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      label={<span style={{ color: "#aaa" }}>Loại xe</span>}
                      name="vehicleType"
                      rules={[{ required: true }]}
                    >
                      <Select
                        placeholder="Chọn loại xe"
                        dropdownStyle={{ background: "#1f1f1f", color: "#fff" }}
                      >
                        <Option value="CAR">Ô tô</Option>
                        <Option value="MOTORBIKE">Xe máy</Option>
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label={
                        <span style={{ color: "#aaa" }}>Hiệu xe (Brand)</span>
                      }
                      name="brand"
                    >
                      <Input
                        placeholder="VD: Honda, Toyota..."
                        style={{
                          background: "#1a1a1a",
                          border: "1px solid #333",
                          color: "#fff",
                        }}
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <Form.Item
                  label={<span style={{ color: "#aaa" }}>Tên chủ xe</span>}
                  name="customerName"
                  rules={[{ required: true }]}
                >
                  <Input
                    prefix={<UserOutlined style={{ color: "#555" }} />}
                    placeholder="Nhập họ và tên"
                    style={{
                      background: "#1a1a1a",
                      border: "1px solid #333",
                      color: "#fff",
                    }}
                  />
                </Form.Item>

                <Form.Item
                  label={<span style={{ color: "#aaa" }}>Số điện thoại</span>}
                  name="customerPhone"
                  rules={[{ required: true }]}
                >
                  <Input
                    placeholder="Nhập SĐT liên hệ"
                    style={{
                      background: "#1a1a1a",
                      border: "1px solid #333",
                      color: "#fff",
                    }}
                  />
                </Form.Item>
              </Card>
            </Col>

            {/* CỘT 2: THÔNG TIN GÓI CƯỚC */}
            <Col span={12}>
              <Card
                title={
                  <span style={{ color: "#1677ff" }}>
                    Thông tin gói vé tháng
                  </span>
                }
                bordered={false}
                style={{
                  background: "#141414",
                  border: "1px solid #1f1f1f",
                  borderRadius: 16,
                  height: "100%",
                }}
                styles={{ header: { borderBottom: "1px solid #1f1f1f" } }}
              >
                <Form.Item
                  label={
                    <span style={{ color: "#aaa" }}>Loại gói (Sub Type)</span>
                  }
                  name="subType"
                  rules={[{ required: true }]}
                >
                  <Select
                    placeholder="Chọn gói cước"
                    dropdownStyle={{ background: "#1f1f1f", color: "#fff" }}
                  >
                    <Option value="MONTHLY_1">Gói 1 Tháng</Option>
                    <Option value="MONTHLY_3">Gói 3 Tháng</Option>
                    <Option value="YEARLY">Gói 1 Năm</Option>
                  </Select>
                </Form.Item>

                <Form.Item
                  label={<span style={{ color: "#aaa" }}>Giá tiền (VNĐ)</span>}
                  name="price"
                  rules={[{ required: true }]}
                >
                  <Input
                    type="number"
                    placeholder="VD: 150000"
                    style={{
                      background: "#1a1a1a",
                      border: "1px solid #333",
                      color: "#fff",
                    }}
                  />
                </Form.Item>

                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      label={
                        <span style={{ color: "#aaa" }}>Ngày bắt đầu</span>
                      }
                      name="startDate"
                      rules={[{ required: true }]}
                    >
                      <DatePicker
                        style={{
                          width: "100%",
                          background: "#1a1a1a",
                          border: "1px solid #333",
                        }}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      label={
                        <span style={{ color: "#aaa" }}>Ngày kết thúc</span>
                      }
                      name="endDate"
                      rules={[{ required: true }]}
                    >
                      <DatePicker
                        style={{
                          width: "100%",
                          background: "#1a1a1a",
                          border: "1px solid #333",
                        }}
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <div style={{ marginTop: 32, textAlign: "right" }}>
                  <Button
                    type="default"
                    style={{
                      marginRight: 12,
                      background: "transparent",
                      color: "#aaa",
                      border: "1px solid #333",
                    }}
                    onClick={() => form.resetFields()}
                  >
                    Nhập lại
                  </Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<SaveOutlined />}
                    style={{
                      background: "#00b96b",
                      borderColor: "#00b96b",
                      fontWeight: 600,
                    }}
                  >
                    Lưu & Đăng ký
                  </Button>
                </div>
              </Card>
            </Col>
          </Row>
        </Form>
      </div>
    </>
  );
};

export default RegisterPage;
