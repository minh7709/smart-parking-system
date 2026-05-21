import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, Row, Select, Space, Spin, Tag, Typography } from 'antd';
import { getActiveLanesApi } from '../api/lane.api';
import { saveLaneSelection } from '../../../utils/storage';
import useLogout from '../../../hooks/useLogout';
import styles from './Lanepage.module.css';
import { useNotification } from '../../../hooks/useNotification';

const { Title, Text } = Typography;

const LanePage = () => {
	const notify = useNotification();
	const navigate = useNavigate();
	const handleLogout = useLogout();
	const [loading, setLoading] = useState(true);
	const [lanes, setLanes] = useState([]);
	const [checkInLaneId, setCheckInLaneId] = useState(undefined);
	const [checkOutLaneId, setCheckOutLaneId] = useState(undefined);

	useEffect(() => {
		const fetchLanes = async () => {
			setLoading(true);

			try {
				const response = await getActiveLanesApi();
				setLanes(Array.isArray(response?.data) ? response.data : []);
			} catch (err) {
				notify.apiError(err, 'Lỗi khi tải danh sách làn');
			} finally {
				setLoading(false);
			}
		};

		fetchLanes();
	}, []);

	const inLanes = useMemo(
		() => lanes.filter((lane) => String(lane.laneType?.value || '').toUpperCase() === 'IN'),
		[lanes],
	);

	const outLanes = useMemo(
		() => lanes.filter((lane) => String(lane.laneType?.value || '').toUpperCase() === 'OUT'),
		[lanes],
	);

	const selectedCheckInLane = useMemo(
		() => inLanes.find((lane) => lane.id === checkInLaneId),
		[inLanes, checkInLaneId],
	);

	const selectedCheckOutLane = useMemo(
		() => outLanes.find((lane) => lane.id === checkOutLaneId),
		[outLanes, checkOutLaneId],
	);

	const handleContinue = () => {
		if (!selectedCheckInLane || !selectedCheckOutLane) {
			notify.error("Vui lòng chọn làn vào và làn ra");
			return;
		}

		saveLaneSelection({
			checkInLane: selectedCheckInLane,
			checkOutLane: selectedCheckOutLane,
		});

		navigate('/monitor', {
			state: {
				checkInLane: selectedCheckInLane,
				checkOutLane: selectedCheckOutLane,
			},
		});
	};

	return (
		<div className={styles['lane-page']}>
			<div className={`${styles['lane-page__glow']} ${styles['lane-page__glow--left']}`} />
			<div className={`${styles['lane-page__glow']} ${styles['lane-page__glow--right']}`} />

			<Card className={styles['lane-page__card']} variant="borderless">
				<Space orientation="vertical" size={24} style={{ width: '100%' }}>
					<div className={styles['lane-page__header']}>
						<Tag color="gold" style={{ marginBottom: 12 }}>
							BẢNG ĐIỀU KHIỂN BẢO VỆ
						</Tag>
						<Title level={2} style={{ margin: 0, color: '#f8f5ef' }}>
							Chọn làn trước khi vào hệ thống giám sát camera
						</Title>
						<Text style={{ color: '#d7d2c7' }}>
							Hãy gắn làn vào và làn ra để hệ thống camera hoạt động.
						</Text>
					</div>

					<Spin spinning={loading} description="Đang tải làn...">
						<Row gutter={[16, 16]}>
							<Col xs={24} md={12}>
								<Card className={`${styles['lane-page__selector']} ${styles['lane-page__selector--in']}`} variant="borderless">
									<Text strong style={{ color: '#efe8d9' }}>
										Làn vào
									</Text>
									<Select
										style={{ fontSize: '20px' }}
										value={checkInLaneId}
										onChange={setCheckInLaneId}
										className={styles['lane-page__select']}
										popupClassName={styles['lane-page__dropdown']}
											placeholder="Chọn làn vào"
										options={inLanes.map((lane) => ({
											value: lane.id,
												label: `${lane.laneName} - ${lane.ipCamera || 'Không có IP camera'}`,
										}))}
									/>
								</Card>
							</Col>

							<Col xs={24} md={12}>
								<Card className={`${styles['lane-page__selector']} ${styles['lane-page__selector--out']}`} variant="borderless">
									<Text strong style={{ color: '#efe8d9' }}>
										Làn ra
									</Text>
									<Select
										style={{ fontSize: '20px' }}
										value={checkOutLaneId}
										onChange={setCheckOutLaneId}
										className={styles['lane-page__select']}
										popupClassName={styles['lane-page__dropdown']}
											placeholder="Chọn làn ra"
										options={outLanes.map((lane) => ({
											value: lane.id,
												label: `${lane.laneName} - ${lane.ipCamera || 'Không có IP camera'}`,
										}))}
									/>
								</Card>
							</Col>
						</Row>
					</Spin>

					<div className={styles['lane-page__summary']}>
						<Text style={{ color: '#efe8d9' }}>
							Làn vào: <strong>{selectedCheckInLane?.laneName || 'Chưa chọn'}</strong>
						</Text>
						<Text style={{ color: '#efe8d9' }}>
							Làn ra: <strong>{selectedCheckOutLane?.laneName || 'Chưa chọn'}</strong>
						</Text>
					</div>

					<Space className={styles['lane-page__actions']}>
						<Button type="default" onClick={() => handleLogout()}>
							Quay lại đăng nhập
						</Button>
						<Button
							className={styles['lane-page__monitor-btn']}
							type="primary"
							onClick={handleContinue}
							disabled={loading || !selectedCheckInLane || !selectedCheckOutLane}
						>
							Vào màn hình giám sát
						</Button>
					</Space>
				</Space>
			</Card>
		</div>
	);
};


export default LanePage;
