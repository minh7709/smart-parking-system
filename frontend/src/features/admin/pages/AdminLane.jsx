import React, { useEffect, useState } from "react";
import { Button, Card, Col, Form, Input, Modal, Popconfirm, Row, Select, Space, Table, Tag } from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { adminApi } from "../api/admin.api";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const AdminLane = () => {
	const notify = useNotification();
	const [data, setData] = useState([]);
	const [loading, setLoading] = useState(false);
	const [laneTypes, setLaneTypes] = useState([]);
	const [laneStatuses, setLaneStatuses] = useState([]);

	const [isModalVisible, setIsModalVisible] = useState(false);
	const [isEditMode, setIsEditMode] = useState(false);
	const [editingId, setEditingId] = useState(null);
	const [form] = Form.useForm();

	const fetchLanes = async () => {
		try {
			setLoading(true);
			const response = await adminApi.getLanes();
			const items = Array.isArray(response?.data) ? response.data : (Array.isArray(response) ? response : []);
			setData(items);
		} catch (error) {
			notify.apiError(error, "Lỗi khi tải danh sách làn");
			setData([]);
		} finally {
			setLoading(false);
		}
	};

	const fetchLaneTypes = async () => {
		try {
			const response = await adminApi.getLaneTypes();
			const items = Array.isArray(response?.data) ? response.data : [];
			setLaneTypes(items);
		} catch (error) {
			notify.apiError(error, "Lỗi khi tải loại làn");
			setLaneTypes([]);
		}
	};

	const fetchLaneStatuses = async () => {
		try {
			const response = await adminApi.getLaneStatuses();
			const items = Array.isArray(response?.data) ? response.data : [];
			setLaneStatuses(items);
		} catch (error) {
			notify.apiError(error, "Lỗi khi tải trạng thái làn");
			setLaneStatuses([]);
		}
	};

	useEffect(() => {
		fetchLanes();
		fetchLaneTypes();
		fetchLaneStatuses();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	const handleAdd = () => {
		setIsEditMode(false);
		setEditingId(null);
		form.resetFields();
		setIsModalVisible(true);
	};

	const handleEdit = (record) => {
		setIsEditMode(true);
		setEditingId(record.id);
		form.setFieldsValue({
			laneName: record.laneName,
			laneType: record.laneType,
			status: record.status,
			ipCamera: record.ipCamera,
		});
		setIsModalVisible(true);
	};

	const handleDelete = async (id) => {
		try {
			await adminApi.deleteLane(id);
			notify.success("Xóa làn thành công");
			fetchLanes();
		} catch (error) {
			notify.apiError(error, "Lỗi khi xóa làn");
		}
	};

	const handleSave = async () => {
		try {
			const values = await form.validateFields();
			if (isEditMode) {
				await adminApi.updateLane(editingId, values);
				notify.success("Cập nhật làn thành công");
			} else {
				await adminApi.createLane(values);
				notify.success("Tạo làn thành công");
			}
			setIsModalVisible(false);
			fetchLanes();
		} catch (error) {
			notify.apiError(error, "Lỗi khi lưu làn");
		}
	};

	const statusColor = (status) => {
		if (status === "ACTIVE") return "green";
		if (status === "INACTIVE") return "red";
		return "default";
	};

	const columns = [
		{ title: "Tên làn", dataIndex: "laneName", key: "laneName" },
		{
			title: "Loại làn",
			dataIndex: "laneType",
			key: "laneType",
			render: (laneType) => {
				const label = typeof laneType === "object" ? laneType?.label : laneType;
				return label ?? "";
			},
		},
		{
			title: "Trạng thái",
			dataIndex: "status",
			key: "status",
			render: (status) => {
				const value = typeof status === "object" ? status?.value : status;
				const label = typeof status === "object" ? status?.label : status;
				return <Tag color={statusColor(value)}>{label ?? value}</Tag>;
			},
		},
		{ title: "IP camera", dataIndex: "ipCamera", key: "ipCamera" },
		{
			title: "Hành động",
			key: "actions",
			render: (_, record) => (
				<Space size="middle">
					<Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
					<Popconfirm
						title="Bạn chắc muốn xóa làn này?"
						onConfirm={() => handleDelete(record.id)}
						okText="Xóa"
						cancelText="Hủy"
					>
						<Button type="text" danger icon={<DeleteOutlined />} />
					</Popconfirm>
				</Space>
			),
		},
	];

	return (
		<div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
			<Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
				<Col>
					<h2 style={{ margin: 0 }}>Quản lý làn</h2>
				</Col>
				<Col>
					<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
						Thêm làn
					</Button>
				</Col>
			</Row>

			<Card bordered={false} style={{ borderRadius: 12 }}>
				<Table
					columns={columns}
					dataSource={data}
					rowKey="id"
					loading={loading}
				/>
			</Card>

			<Modal
				title={isEditMode ? "Cập nhật làn" : "Tạo làn"}
				open={isModalVisible}
				onOk={handleSave}
				onCancel={() => setIsModalVisible(false)}
				okText="Lưu"
				cancelText="Hủy"
			>
				<Form form={form} layout="vertical">
					<Form.Item
						name="laneName"
						label="Tên làn"
						rules={[{ required: true, message: "Vui lòng nhập tên làn" }]}
					>
						<Input />
					</Form.Item>

					<Form.Item
						name="laneType"
						label="Loại làn"
						rules={[{ required: true, message: "Vui lòng chọn loại làn" }]}
					>
						<Select placeholder="Chọn loại làn">
							{laneTypes.map((t) => (
								<Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
							))}
						</Select>
					</Form.Item>

					<Form.Item
						name="status"
						label="Trạng thái"
						rules={[{ required: true, message: "Vui lòng chọn trạng thái" }]}
					>
						<Select placeholder="Chọn trạng thái">
							{laneStatuses.map((s) => (
								<Option key={s.value ?? s} value={s.value ?? s}>{s.label ?? s}</Option>
							))}
						</Select>
					</Form.Item>

					<Form.Item
						name="ipCamera"
						label="Địa chỉ IP camera"
						rules={[{ required: true, message: "Vui lòng nhập IP camera" }]}
					>
						<Input />
					</Form.Item>
				</Form>
			</Modal>
		</div>
	);
};

export default AdminLane;
