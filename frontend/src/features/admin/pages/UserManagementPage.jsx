import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Modal, Form, Select, Tag, Popconfirm, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin.api';
import { useNotification } from '../../../hooks/useNotification';

const UserManagementPage = () => {
  const [data, setData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();
  const { showSuccess, showError } = useNotification();
  
  // States for search filters
  const [searchFullName, setSearchFullName] = useState('');
  const [searchPhone, setSearchPhone] = useState('');

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await adminApi.getUsers();
      // Đảm bảo _data luôn là một mảng
      const _data = Array.isArray(response?.content) ? response.content : (Array.isArray(response) ? response : []);
      setData(_data);
      setFilteredData(_data);
    } catch (error) {
      console.error('Failed to fetch users:', error);
      showError('Lỗi', 'Không thể lấy danh sách người dùng');
      // Đặt lại state thành mảng rỗng khi có lỗi
      setData([]);
      setFilteredData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  useEffect(() => {
    // Local filtering when search fields change
    let result = [...data];
    if (searchFullName) {
      result = result.filter(item => 
        item.fullName && item.fullName.toLowerCase().includes(searchFullName.toLowerCase())
      );
    }
    if (searchPhone) {
      result = result.filter(item => 
        item.phone && item.phone.includes(searchPhone)
      );
    }
    setFilteredData(result);
  }, [searchFullName, searchPhone, data]);

  const handleAdd = () => {
    setIsEditMode(false);
    setEditingId(null);
    form.resetFields();
    
    // Set default values
    form.setFieldsValue({
      role: 'GUARD',
      status: 'ACTIVE'
    });
    
    setIsModalVisible(true);
  };

  const handleEdit = (record) => {
    setIsEditMode(true);
    setEditingId(record.id);
    form.setFieldsValue({
      username: record.username,
      fullName: record.fullName,
      phone: record.phone,
      role: record.role,
      status: record.status,
      // password will be left empty on edit unless modified
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await adminApi.deleteUser(id);
      showSuccess('Thành công', 'Đã xoá người dùng');
      fetchUsers();
    } catch (error) {
      console.error('Failed to delete user:', error);
      showError('Lỗi', 'Không thể xoá người dùng');
    }
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      
      const payload = { ...values };
      
      if (isEditMode) {
        // If password is empty in edit mode, remove it so backend doesn't overwrite with empty
        if (!payload.password) {
          delete payload.password;
        }
        await adminApi.updateUser(editingId, payload);
        showSuccess('Thành công', 'Đã cập nhật thông tin người dùng');
      } else {
        await adminApi.createUser(payload);
        showSuccess('Thành công', 'Đã thêm người dùng mới');
      }
      setIsModalVisible(false);
      fetchUsers();
    } catch (error) {
      console.error('Save failed:', error);
      
      // Parse backend errors
      if (error.response?.data?.fieldErrors) {
        const parsedErrors = error.response.data.fieldErrors.map(err => ({
          name: err.field,
          errors: [err.message]
        }));
        form.setFields(parsedErrors);
      } else {
        showError('Lỗi', error.response?.data?.message || 'Có lỗi xảy ra khi lưu thông tin');
      }
    }
  };

  const columns = [
    {
      title: 'Tài khoản',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: 'Họ và tên',
      dataIndex: 'fullName',
      key: 'fullName',
    },
    {
      title: 'Số điện thoại',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      render: (role) => (
        <Tag color={role === 'ADMIN' ? 'red' : 'blue'}>
          {role === 'ADMIN' ? 'Quản trị viên' : 'Bảo vệ'}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={status === 'ACTIVE' ? 'green' : 'red'}>
          {status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
        </Tag>
      ),
    },
    {
      title: 'Thao tác',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="Bạn có chắc chắn muốn xoá người dùng này?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xoá"
            cancelText="Huỷ"
          >
            <Button type="primary" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <Row justify="space-between" align="middle" className="mb-6">
        <Col>
          <h1 className="text-2xl font-bold">Quản lý nhân sự</h1>
        </Col>
        <Col>
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={handleAdd}
            size="large"
          >
            Thêm nhân sự mới
          </Button>
        </Col>
      </Row>

      <Row className="mb-6" gutter={16}>
        <Col>
          <Input 
            placeholder="Tìm theo họ & tên" 
            prefix={<SearchOutlined />} 
            value={searchFullName}
            onChange={(e) => setSearchFullName(e.target.value)}
            style={{ width: 300 }}
            size="large"
          />
        </Col>
        <Col>
          <Input 
            placeholder="Tìm theo số điện thoại" 
            prefix={<SearchOutlined />} 
            value={searchPhone}
            onChange={(e) => setSearchPhone(e.target.value)}
            style={{ width: 300 }}
            size="large"
          />
        </Col>
      </Row>

      <Table 
        columns={columns} 
        dataSource={filteredData}
        rowKey="id"
        loading={loading}
        pagination={{
            showSizeChanger: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} của ${total} mục`
        }}
      />

      <Modal
        title={isEditMode ? "Chỉnh sửa nhân sự" : "Thêm nhân sự mới"}
        open={isModalVisible}
        onOk={handleSave}
        onCancel={() => setIsModalVisible(false)}
        okText="Lưu"
        cancelText="Huỷ"
      >
        <Form
          form={form}
          layout="vertical"
          name="user_form"
        >
          <Form.Item
            name="username"
            label="Tài khoản"
            rules={[
              { required: true, message: 'Vui lòng nhập tài khoản!' },
              { min: 3, max: 50, message: 'Tài khoản phải từ 3 đến 50 ký tự' }
            ]}
          >
            <Input disabled={isEditMode} />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="password"
                label="Mật khẩu"
                rules={[
                  { required: !isEditMode, message: 'Vui lòng nhập mật khẩu!' },
                  { min: 8, max: 100, message: 'Mật khẩu phải từ 8 đến 100 ký tự' }
                ]}
                hasFeedback
              >
                <Input.Password placeholder={isEditMode ? "Để trống nếu không đổi" : ""} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="confirm"
                label="Xác nhận mật khẩu"
                dependencies={['password']}
                hasFeedback
                rules={[
                  { required: !isEditMode, message: 'Vui lòng xác nhận mật khẩu!' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('password') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
                    },
                  }),
                ]}
              >
                <Input.Password placeholder="Nhập lại mật khẩu" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="fullName"
            label="Họ và tên"
            rules={[
              { required: true, message: 'Vui lòng nhập họ và tên!' },
              { max: 100, message: 'Họ và tên tối đa 100 ký tự' }
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="phone"
            label="Số điện thoại"
            rules={[
              { required: true, message: 'Vui lòng nhập số điện thoại!' },
              { pattern: /^\d{10,11}$/, message: 'Số điện thoại phải là 10 hoặc 11 chữ số' }
            ]}
          >
            <Input />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="role"
                label="Vai trò"
                rules={[{ required: true, message: 'Vui lòng chọn vai trò!' }]}
              >
                <Select>
                  <Select.Option value="GUARD">Bảo vệ</Select.Option>
                  <Select.Option value="ADMIN">Quản trị viên</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="status"
                label="Trạng thái"
                rules={[{ required: true, message: 'Vui lòng chọn trạng thái!' }]}
              >
                <Select>
                  <Select.Option value="ACTIVE">Hoạt động</Select.Option>
                  <Select.Option value="INACTIVE">Không hoạt động</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagementPage;