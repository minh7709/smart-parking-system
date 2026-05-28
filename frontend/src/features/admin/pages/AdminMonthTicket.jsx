import React, { useEffect, useMemo, useState } from "react";
import {
	Button,
	Card,
	Col,
	Form,
	Input,
	InputNumber,
	Modal,
	Popconfirm,
	Row,
	Select,
	Space,
	Table,
	Tag,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { adminApi } from "../api/admin.api";
import { useNotification } from "../../../hooks/useNotification";
import { getSystemTypes } from "../../../utils/storage";

const { Option } = Select;

const AdminMonthTicket = () => {
	const notify = useNotification();
	const [data, setData] = useState([]);
	const [loading, setLoading] = useState(false);
	const [total, setTotal] = useState(0);
	const [currentPage, setCurrentPage] = useState(1);
	const [pageSize, setPageSize] = useState(10);
	const vehicleTypes = useMemo(() => getSystemTypes("vehicleTypes") ?? [], []);
	const subscriptionTypes = useMemo(() => getSystemTypes("subscriptionTypes") ?? [], []);
	const [filterVehicleType, setFilterVehicleType] = useState(null);

	const [isModalVisible, setIsModalVisible] = useState(false);
	const [isEditMode, setIsEditMode] = useState(false);
	const [editingId, setEditingId] = useState(null);
	const [form] = Form.useForm();

	const getEnumLabel = (value) => {
		if (value && typeof value === "object") {
			return value.label ?? value.value ?? "";
		}
		return value ?? "";
	};

	const fetchSubscriptionPricings = async (page = currentPage, size = pageSize) => {
		try {
			setLoading(true);
			const params = {
				page: page - 1,
				size,
				sort: "active,desc",
			};
			if (filterVehicleType) params.vehicleType = filterVehicleType;

			const res = await adminApi.getSubscriptionPricings(params);
			const pageData = res?.data;
			setData(pageData?.content ?? []);
			setTotal(pageData?.totalElements ?? 0);
		} catch (error) {
			notify.apiError(error, "Không thể tải danh sách cấu hình giá");
			setData([]);
		} finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		fetchSubscriptionPricings();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	useEffect(() => {
		fetchSubscriptionPricings(currentPage, pageSize);
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [currentPage, pageSize, filterVehicleType]);

	const handleAdd = () => {
		setIsEditMode(false);
		setEditingId(null);
		form.resetFields();
		form.setFieldsValue({ active: true });
		setIsModalVisible(true);
	};

	const handleEdit = (record) => {
		setIsEditMode(true);
		setEditingId(record.id);
		form.setFieldsValue({
			pricingName: record.pricingName,
			vehicleType: typeof record.vehicleType === "object" ? record.vehicleType?.value : record.vehicleType,
			durationType: typeof record.durationType === "object" ? record.durationType?.value : record.durationType,
			price: record.price,
			description: record.description,
			active: record.active,
		});
		setIsModalVisible(true);
	};

	const handleSave = async () => {
		try {
			const values = await form.validateFields();
			if (isEditMode) {
				await adminApi.updateSubscriptionPricing(editingId, values);
				notify.success("Cập nhật cấu hình giá gói đăng ký thành công");
			} else {
				await adminApi.createSubscriptionPricing(values);
				notify.success("Tạo cấu hình giá gói đăng ký thành công");
			}
			setIsModalVisible(false);
			fetchSubscriptionPricings(currentPage, pageSize);
		} catch (error) {
			notify.apiError(error, "Không thể lưu cấu hình giá gói đăng ký");
		}
	};

	const handleDelete = async (id) => {
		try {
			await adminApi.deleteSubscriptionPricing(id);
			notify.success("Đã xóa cấu hình giá gói đăng ký");
			fetchSubscriptionPricings(currentPage, pageSize);
		} catch (error) {
			notify.apiError(error, "Không thể xóa cấu hình giá gói đăng ký");
		}
	};

	const handleActivate = async (record) => {
		try {
			if (record.active) {
				notify.warning("Cấu hình này đang hoạt động");
				return;
			}
			await adminApi.activateSubscriptionPricing(record.id);
			notify.success("Đã kích hoạt cấu hình giá gói đăng ký");
			fetchSubscriptionPricings(currentPage, pageSize);
		} catch (error) {
			notify.apiError(error, "Không thể kích hoạt cấu hình giá gói đăng ký");
		}
	};

	const columns = [
		{ title: "Tên gói", dataIndex: "pricingName", key: "pricingName" },
		{
			title: "Loại xe",
			dataIndex: "vehicleType",
			key: "vehicleType",
			render: (t) => <Tag>{getEnumLabel(t)}</Tag>,
		},
		{
			title: "Thời hạn",
			dataIndex: "durationType",
			key: "durationType",
			render: (t) => <Tag>{getEnumLabel(t)}</Tag>,
		},
		{
			title: "Giá (VND)",
			dataIndex: "price",
			key: "price",
			render: (p) => (p?.toLocaleString ? `${p.toLocaleString()}d` : p),
		},
		{
			title: "Trạng thái",
			dataIndex: "active",
			key: "active",
			render: (active) => (
				active ? <Tag color="green">Đang hoạt động</Tag> : <Tag color="default">Không hoạt động</Tag>
			),
		},
		{
			title: "Người tạo",
			dataIndex: "createdBy",
			render: (createdBy) => createdBy ?? "N/A"
		},
		{
			title: "Kích hoạt",
			key: "activate",
			render: (_, record) => (
				<Button
					type={record.active ? "primary" : "default"}
					onClick={() => handleActivate(record)}
				>
					{record.active ? "Đang kích hoạt" : "Kích hoạt"}
				</Button>
			),
		},
		{
			title: "Thao tác",
			key: "actions",
			render: (_, record) => (
				<Space size="middle">
					<Button type="text" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
					<Popconfirm
						title="Bạn chắc chắn muốn xóa cấu hình giá gói đăng ký này?"
						onConfirm={() => handleDelete(record.id)}
						okText="Xóa"
						cancelText="Hủy"
						disabled={record.active}
					>
						<Button type="text" danger icon={<DeleteOutlined />} disabled={record.active} />
					</Popconfirm>
				</Space>
			),
		},
	];

	return (
		<div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
			<Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
				<Col>
					<h2 style={{ margin: 0 }}>Cấu hình giá gói đăng ký</h2>
				</Col>
				<Col>
					<Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
						Thêm cấu hình  giá
					</Button>
				</Col>
			</Row>

			<Card bordered={false} style={{ marginBottom: 16, borderRadius: 12 }}>
				<Row gutter={16} align="middle">
					<Col span={6}>
						<Select
							placeholder="Lọc theo loại xe"
							style={{ width: "100%" }}
							value={filterVehicleType}
							onChange={(val) => { setFilterVehicleType(val ?? null); setCurrentPage(1); }}
							allowClear
						>
							{vehicleTypes.map((t) => (
								<Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
							))}
						</Select>
					</Col>
				</Row>
			</Card>

			<Card bordered={false} style={{ borderRadius: 12 }}>
				<Table
					columns={columns}
					dataSource={data}
					rowKey="id"
					loading={loading}
					pagination={{
						current: currentPage,
						pageSize,
						total,
						showSizeChanger: true,
						showTotal: (t, r) => `${r[0]}-${r[1]} / ${t}`,
						onChange: (page, size) => { setCurrentPage(page); setPageSize(size); },
					}}
				/>
			</Card>

			<Modal
				title={isEditMode ? "Cập nhật cấu hình giá gói đăng ký" : "Thêm cấu hình giá gói đăng ký"}
				open={isModalVisible}
				onOk={handleSave}
				onCancel={() => setIsModalVisible(false)}
				okText="Lưu"
				cancelText="Hủy"
			>
				<Form form={form} layout="vertical">
					<Form.Item
						name="pricingName"
						label="Tên gói"
						rules={[{ required: true, message: "Vui lòng nhập tên gói" }]}
					>
						<Input />
					</Form.Item>

					<Form.Item
						name="vehicleType"
						label="Loại xe"
						rules={[{ required: true, message: "Vui lòng chọn loại xe" }]}
					>
						<Select placeholder="Chọn loại xe" disabled={isEditMode}>
							{vehicleTypes.map((t) => (
								<Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
							))}
						</Select>
					</Form.Item>

					<Form.Item
						name="durationType"
						label="Thời hạn"
						rules={[{ required: true, message: "Vui lòng chọn thời hạn" }]}
					>
						<Select placeholder="Chọn thời hạn" disabled={isEditMode}>
							{subscriptionTypes.map((t) => (
								<Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
							))}
						</Select>
					</Form.Item>

					<Form.Item
						name="price"
						label="Giá (VND)"
						rules={[{ required: true, message: "Vui lòng nhập giá" }]}
					>
						<InputNumber style={{ width: "100%" }} />
					</Form.Item>

					<Form.Item name="description" label="Mô tả">
						<Input.TextArea rows={3} />
					</Form.Item>

					<Form.Item
						name="active"
						label="Trạng thái"
						rules={[{ required: true, message: "Vui lòng chọn trạng thái" }]}
					>
						<Select>
							<Option value={true}>Hoạt động</Option>
							<Option value={false}>Không hoạt động</Option>
						</Select>
					</Form.Item>
				</Form>
			</Modal>
		</div>
	);
};

export default AdminMonthTicket;
