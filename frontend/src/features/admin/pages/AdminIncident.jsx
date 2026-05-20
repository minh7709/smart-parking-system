import React, { useEffect, useRef, useState } from "react";
import { Card, Table, Row, Col, Select, Button, Modal, Spin } from "antd";
import dayjs from "dayjs";
import { adminApi } from "../api/admin.api";
import { useNotification } from "../../../hooks/useNotification";

const { Option } = Select;

const AdminIncident = () => {
	const notify = useNotification();
	const notifyRef = useRef(notify);

	const [data, setData] = useState([]);
	const [loading, setLoading] = useState(false);
	const [total, setTotal] = useState(0);
	const [currentPage, setCurrentPage] = useState(1);
	const [pageSize, setPageSize] = useState(10);
	const [incidentTypes, setIncidentTypes] = useState([]);
	const [filterType, setFilterType] = useState(null);
	const [refreshTick, setRefreshTick] = useState(0);

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [evidenceUrl, setEvidenceUrl] = useState(null);
	const [evidenceLoading, setEvidenceLoading] = useState(false);

	useEffect(() => {
		notifyRef.current = notify;
	}, [notify]);

	useEffect(() => {
		const fetchIncidentTypes = async () => {
			try {
				const res = await adminApi.getIncidentTypes();
				setIncidentTypes(res?.data ?? []);
			} catch (error) {
				notifyRef.current.apiError(error, "Lỗi khi tải danh sách loại sự cố");
			}
		};

		fetchIncidentTypes();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	useEffect(() => {
		const fetchIncidents = async (page = currentPage, size = pageSize) => {
			setLoading(true);
			try {
				const params = {
					page: page - 1,
					size,
					sort: "reportedAt,desc",
				};
				if (filterType) params.incidentType = filterType;

				const res = await adminApi.getIncidents(params);
				const pageData = res?.data;
				setData(pageData?.content ?? []);
				setTotal(pageData?.totalElements ?? 0);
			} catch (error) {
				notifyRef.current.apiError(error, "Lỗi khi tải danh sách sự cố");
			} finally {
				setLoading(false);
			}
		};

		fetchIncidents(currentPage, pageSize);
	}, [currentPage, pageSize, filterType, refreshTick]);

	useEffect(() => {
		return () => {
			if (evidenceUrl) URL.revokeObjectURL(evidenceUrl);
		};
	}, [evidenceUrl]);

	const handleRowClick = async (record) => {
		if (evidenceUrl) {
			URL.revokeObjectURL(evidenceUrl);
			setEvidenceUrl(null);
		}
		setIsModalOpen(true);
		setEvidenceLoading(true);
		try {
			const blob = await adminApi.getIncidentEvidence(record.id);
			const url = URL.createObjectURL(blob);
			setEvidenceUrl(url);
		} catch (error) {
			notify.apiError(error, "Lỗi khi tải ảnh sự cố");
		} finally {
			setEvidenceLoading(false);
		}
	};

	const handleCloseModal = () => {
		setIsModalOpen(false);
		if (evidenceUrl) {
			URL.revokeObjectURL(evidenceUrl);
			setEvidenceUrl(null);
		}
	};

	const handleRefresh = () => {
		setFilterType(null);
		setCurrentPage(1);
		setRefreshTick((prev) => prev + 1);
	};

	const columns = [
		{ title: "Thời gian", dataIndex: "reportedAt", key: "reportedAt", render: (d) => (d ? dayjs(d).format("DD/MM/YYYY HH:mm") : "") },
		{ title: "Loại sự cố", dataIndex: "incidentType", key: "incidentType", render: (t) => t?.label || "N/A"},
		{ title: "Người báo cáo", dataIndex: "reporterName", key: "reporterName" },
		{ title: "Mô tả", dataIndex: "description", key: "description" },
	];

	return (
		<div style={{ padding: "0 24px", maxWidth: 1200, margin: "0 auto" }}>
			<Card bordered={false} style={{ marginBottom: 24, borderRadius: 12 }}>
				<Row gutter={16} align="middle">
					<Col span={6}>
						<Select
							placeholder="Loại sự cố"
							style={{ width: "100%" }}
							value={filterType}
							onChange={(val) => { setFilterType(val ?? null); setCurrentPage(1); }}
							allowClear
						>
							{incidentTypes.map((t) => (
								<Option key={t.value ?? t} value={t.value ?? t}>{t.label ?? t}</Option>
							))}
						</Select>
					</Col>
					<Col span={18} style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
						<Button onClick={handleRefresh}>
							Làm mới
						</Button>
					</Col>
				</Row>
			</Card>

			<Card bordered={false} style={{ borderRadius: 12 }}>
				<Table
					columns={columns}
					dataSource={data}
					rowKey="id"
					loading={loading}
					onRow={(record) => ({ onClick: () => handleRowClick(record) })}
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
				title="Anh su co"
				open={isModalOpen}
				onCancel={handleCloseModal}
				footer={null}
				width={800}
			>
				{evidenceLoading && (
					<div style={{ display: "flex", justifyContent: "center", padding: 24 }}>
						<Spin />
					</div>
				)}
				{!evidenceLoading && evidenceUrl && (
					<img
						src={evidenceUrl}
						alt="incident-evidence"
						style={{ width: "100%", borderRadius: 8 }}
					/>
				)}
				{!evidenceLoading && !evidenceUrl && (
					<div style={{ textAlign: "center", padding: 24 }}>Khong co anh su co.</div>
				)}
			</Modal>
		</div>
	);
};

export default AdminIncident;
