import React, { useState, useEffect, useMemo, useRef } from 'react';
import { Table, Button, Space, Input, Modal, Form, Select, Tag, Popconfirm, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { adminApi } from '../api/admin.api';
import { useNotification } from '../../../hooks/useNotification';
import { getSystemTypes } from '../../../utils/storage';

const UserManagementPage = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();
  const notify = useNotification();
  
  const userStatuses = useMemo(() => getSystemTypes("userStatuses") ?? [], []);
  
  // States for search filters
  const [searchInput, setSearchInput] = useState('');
  const [searchPhone, setSearchPhone] = useState('');
  const [filterStatus, setFilterStatus] = useState(null);
  const debounceTimer = useRef(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const params = {};
      if (searchPhone.trim()) params.phone = searchPhone.trim();
      if (filterStatus) params.status = filterStatus;

      const response = await adminApi.getUsers(params);
      // ApiResponse trả về data là danh sách người dùng
      const _data = Array.isArray(response?.data) ? response.data : (Array.isArray(response) ? response : []);
      setData(_data);
    } catch (error) {
      console.error('Failed to fetch users:', error);
      notify.apiError(error, 'Không thể lấy danh sách người dùng');
      setData([]);
    } finally {
      setLoading(false);
    } 
  };

  useEffect(() => {
    fetchUsers();
  }, [searchPhone, filterStatus]);

  const handlePhoneChange = (e) => {
    const val = e.target.value;
    setSearchInput(val);
    clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      setSearchPhone(val);
    }, 500);
  };

  const handleResetFilters = () => {
    setSearchInput('');
    setSearchPhone('');
    setFilterStatus(null);
  };

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
      status: typeof record.status === 'object' ? record.status?.value : record.status,
      // password will be left empty on edit unless modified
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await adminApi.deleteUser(id);
      notify.success('Đã xoá người dùng');
      fetchUsers();
    } catch (error) {
      console.error('Failed to delete user:', error);
      notify.apiError(error, 'Không thể xoá người dùng');
    }
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      
      const payload = { ...values };
      delete payload.confirm;
      
      if (isEditMode) {
        if (!payload.password) {
          delete payload.password;
        }
        await adminApi.updateUser(editingId, payload);
        notify.success('Đã cập nhật thông tin người dùng');
      } else {
        await adminApi.createUser(payload);
        notify.success('Đã thêm người dùng mới');
      }
      setIsModalVisible(false);
      fetchUsers();
    } catch (error) {
      console.error('Save failed:', error);
      notify.apiError(error, 'Có lỗi xảy ra khi lưu thông tin');
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
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const val = typeof status === 'object' ? status?.value : status;
        const label = typeof status === 'object' ? status?.label : status;
        return (
          <Tag color={val === 'ACTIVE' ? 'green' : 'red'}>
            {label ?? val}
          </Tag>
        );
      },
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

      <Row className="mb-6" gutter={16} align="middle">
        <Col>
          <Input 
            placeholder="Tìm theo số điện thoại" 
            prefix={<SearchOutlined />} 
            value={searchInput}
            onChange={handlePhoneChange}
            style={{ width: 300 }}
            size="large"
            allowClear
            onClear={() => { setSearchInput(''); setSearchPhone(''); }}
          />
        </Col>
        <Col>
          <Select
            placeholder="Lọc theo trạng thái"
            value={filterStatus}
            onChange={(val) => setFilterStatus(val ?? null)}
            style={{ width: 200 }}
            size="large"
            allowClear
          >
            {userStatuses.map((s) => (
              <Select.Option key={s.value ?? s} value={s.value ?? s}>
                {s.label ?? s}
              </Select.Option>
            ))}
          </Select>
        </Col>
        <Col>
          <Button onClick={handleResetFilters} size="large">
            Làm mới
          </Button>
        </Col>
      </Row>

      <Table 
        columns={columns} 
        dataSource={data}
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

          {!isEditMode && (
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="password"
                  label="Mật khẩu"
                  rules={[
                    { required: true, message: 'Vui lòng nhập mật khẩu!' },
                    { min: 8, max: 100, message: 'Mật khẩu phải từ 8 đến 100 ký tự' }
                  ]}
                  hasFeedback
                >
                  <Input.Password />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="confirm"
                  label="Xác nhận mật khẩu"
                  dependencies={['password']}
                  hasFeedback
                  rules={[
                    { required: true, message: 'Vui lòng xác nhận mật khẩu!' },
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
          )}

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
                name="status"
                label="Trạng thái"
                rules={[{ required: true, message: 'Vui lòng chọn trạng thái!' }]}
              >
                <Select placeholder="Chọn trạng thái">
                  {userStatuses.map((s) => (
                    <Select.Option key={s.value ?? s} value={s.value ?? s}>
                      {s.label ?? s}
                    </Select.Option>
                  ))}
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