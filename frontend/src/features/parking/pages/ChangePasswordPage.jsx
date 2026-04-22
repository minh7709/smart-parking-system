import React, { useState } from "react";
import { Card, Form, Input, Button, message, Alert } from "antd";
import { LockOutlined, KeyOutlined } from "@ant-design/icons";
import { AppLayout } from "../../../components/Layout/AppLayout";
import { useNavigate } from "react-router-dom";
import { validatePassword } from "../../../utils/validators";

const ChangePasswordPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error("Mật khẩu xác nhận không khớp!");
      return;
    }

    if (!validatePassword(values.newPassword)) {
      message.error(
        "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số!",
      );
      return;
    }

    setLoading(true);

    try {
      // Gọi API đổi mật khẩu ở đây
      // const response = await changePasswordApi({
      //   oldPassword: values.oldPassword,
      //   newPassword: values.newPassword,
      // });

      // Tạm thời giả lập thành công
      setTimeout(() => {
        message.success("Đổi mật khẩu thành công!");
        form.resetFields();
        navigate("/profile");
        setLoading(false);
      }, 1000);
    } catch (error) {
      message.error(error.message || "Đổi mật khẩu thất bại!");
      setLoading(false);
    }
  };

  return (
    <>
      <Card
        title="Đổi mật khẩu"
        style={{
          maxWidth: 500,
          margin: "0 auto",
          background: "#909090",
          border: "1px solid #1f1f1f",
          borderRadius: 16,
        }}
        headStyle={{ borderBottom: "1px solid #1f1f1f", color: "#fff" }}
      >
        <Alert
          message="Yêu cầu mật khẩu"
          description="Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số."
          type="info"
          showIcon
          style={{
            marginBottom: 24,
            background: "#ff1a1a",
            border: "1px solid #1890ff",
          }}
        />

        <Form form={form} layout="vertical" onFinish={onFinish} size="large">
          <Form.Item
            name="oldPassword"
            label="Mật khẩu cũ"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu cũ!" }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Nhập mật khẩu cũ"
              style={{
                background: "#ffffff",
                border: "1px solid #333",
                color: "#fff",
              }}
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="Mật khẩu mới"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu mới!" }]}
          >
            <Input.Password
              prefix={<KeyOutlined />}
              placeholder="Nhập mật khẩu mới"
              style={{
                background: "#fcfcfc",
                border: "1px solid #333",
                color: "#fff",
              }}
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Xác nhận mật khẩu mới"
            rules={[
              { required: true, message: "Vui lòng xác nhận mật khẩu mới!" },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Xác nhận mật khẩu mới"
              style={{
                background: "#ffffff",
                border: "1px solid #333",
                color: "#fff",
              }}
            />
          </Form.Item>

          <Form.Item>
            <div
              style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}
            >
              <Button onClick={() => navigate("/profile")}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                Đổi mật khẩu
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </>
  );
};

export default ChangePasswordPage;
