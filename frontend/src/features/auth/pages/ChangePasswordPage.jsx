import React, { useState } from "react";
import { Card, Form, Input, Button, Alert } from "antd";
import { LockOutlined, KeyOutlined } from "@ant-design/icons";
import { AppLayout } from "../../../components/Layout/AppLayout";
import { useNavigate } from "react-router-dom";
import { validatePassword } from "../../../utils/validators";
import { changePasswordApi } from "../../auth/api/auth.api";
import { useNotification } from "../../../hooks/useNotification";

const ChangePasswordPage = () => {
  const navigate = useNavigate();
  const notify = useNotification();
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      notify.error("Mật khẩu xác nhận không khớp!");
      return;
    }

    if (!validatePassword(values.newPassword)) {
      notify.error(
        "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số!",
        "Mật khẩu không hợp lệ"
      );
      return;
    }

    setLoading(true);

    try {
      await changePasswordApi({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });

      notify.success("Đổi mật khẩu thành công!");
      form.resetFields();
      navigate("/profile");
    } catch (error) {
      notify.apiError(error, "Đổi mật khẩu thất bại");
    } finally {
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
          background: "#ffffff",
          border: "1px solid #f0f0f0",
          borderRadius: 16,
        }}
        headStyle={{ borderBottom: "1px solid #f0f0f0", color: "#141414", fontSize: 18 }}
      >
        <Alert
          message="Yêu cầu mật khẩu"
          description="Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số."
          type="info"
          showIcon
          style={{
            marginBottom: 24,
          }}
        />

        <Form form={form} layout="vertical" onFinish={onFinish} size="large">
          <Form.Item
            name="currentPassword"
            label={<span style={{ color: "#555" }}>Mật khẩu cũ</span>}
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu cũ!" }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: "#888" }} />}
              placeholder="Nhập mật khẩu cũ"
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label={<span style={{ color: "#555" }}>Mật khẩu mới</span>}
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu mới!" }]}
          >
            <Input.Password
              prefix={<KeyOutlined style={{ color: "#888" }} />}
              placeholder="Nhập mật khẩu mới"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label={<span style={{ color: "#555" }}>Xác nhận mật khẩu mới</span>}
            rules={[
              { required: true, message: "Vui lòng xác nhận mật khẩu mới!" },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: "#888" }} />}
              placeholder="Xác nhận mật khẩu mới"
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
