import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, Row, Select, Space, Spin, Tag, Typography } from 'antd';
import { getActiveLanesApi } from '../api/lane.api';
import { saveLaneSelection } from '../../../utils/storage';
import useLogout from '../../../hooks/useLogout';
import './Lanepage.css';

const { Title, Text } = Typography;

const Lanepage = () => {
	const navigate = useNavigate();
	const handleLogout = useLogout();
	const [loading, setLoading] = useState(true);
	const [lanes, setLanes] = useState([]);
	const [error, setError] = useState('');
	const [checkInLaneId, setCheckInLaneId] = useState(undefined);
	const [checkOutLaneId, setCheckOutLaneId] = useState(undefined);

	useEffect(() => {
		const fetchLanes = async () => {
			setLoading(true);
			setError('');

			try {
				const response = await getActiveLanesApi();
				setLanes(Array.isArray(response?.data) ? response.data : []);
			} catch (err) {
				console.error('Get active lanes error:', err);
				setError(err.message || 'Khong tai duoc danh sach lane.');
			} finally {
				setLoading(false);
			}
		};

		fetchLanes();
	}, []);

	const inLanes = useMemo(
		() => lanes.filter((lane) => String(lane.laneType || '').toUpperCase() === 'IN'),
		[lanes],
	);

	const outLanes = useMemo(
		() => lanes.filter((lane) => String(lane.laneType || '').toUpperCase() === 'OUT'),
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
			setError('Vui long chon du lane check-in va lane check-out.');
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
		<div className="lane-page">
			<div className="lane-page__glow lane-page__glow--left" />
			<div className="lane-page__glow lane-page__glow--right" />

			<Card className="lane-page__card" variant="borderless">
				<Space orientation="vertical" size={24} style={{ width: '100%' }}>
					<div className="lane-page__header">
						<Tag color="gold" style={{ marginBottom: 12 }}>
							GUARD CONSOLE
						</Tag>
						<Title level={2} style={{ margin: 0, color: '#f8f5ef' }}>
							Chọn lane trước khi vào hệ thống giám sát Camera
						</Title>
						<Text style={{ color: '#d7d2c7' }}>
							Hãy gắn lane vào và lane ra để hệ thống camera hoạt động.
						</Text>
					</div>

					{error && <Alert type="error" message={error} showIcon />}

					<Spin spinning={loading} description="Dang tai lane...">
						<Row gutter={[16, 16]}>
							<Col xs={24} md={12}>
								<Card className="lane-page__selector lane-page__selector--in" variant="borderless">
									<Text strong style={{ color: '#efe8d9' }}>
										Lane Check-In
									</Text>
									<Select
                                        style={{ fontSize: '20px' }}
										value={checkInLaneId}
										onChange={setCheckInLaneId}
										className="lane-page__select"
										popupClassName="lane-page__dropdown"
										placeholder="Chọn lane check-in"
										options={inLanes.map((lane) => ({
											value: lane.id,
											label: `${lane.laneName} - ${lane.ipCamera || 'No Camera IP'}`,
										}))}
									/>
								</Card>
							</Col>

							<Col xs={24} md={12}>
								<Card className="lane-page__selector lane-page__selector--out" variant="borderless">
									<Text strong style={{ color: '#efe8d9' }}>
										Lane Check-Out 
									</Text>
									<Select
                                        style={{fontSize: '20px'}}
										value={checkOutLaneId}
										onChange={setCheckOutLaneId}
										className="lane-page__select"
										popupClassName="lane-page__dropdown"
										placeholder="Chọn lane check-out"
										options={outLanes.map((lane) => ({
											value: lane.id,
											label: `${lane.laneName} - ${lane.ipCamera || 'No Camera IP'}`,
										}))}
									/>
								</Card>
							</Col>
						</Row>
					</Spin>

					<div className="lane-page__summary">
						<Text style={{ color: '#efe8d9' }}>
							Lane vào: <strong>{selectedCheckInLane?.laneName || 'Chưa chọn'}</strong>
						</Text>
						<Text style={{ color: '#efe8d9' }}>
							Lane ra: <strong>{selectedCheckOutLane?.laneName || 'Chưa chọn'}</strong>
						</Text>
					</div>

					<Space className="lane-page__actions">
						<Button type="default" onClick={() => handleLogout()}>
							Quay lại đăng nhập
						</Button>
						<Button
							className="lane-page__monitor-btn"
							type="primary"
							onClick={handleContinue}
							disabled={loading || !selectedCheckInLane || !selectedCheckOutLane}
						>
							Vào màn hình monitor
						</Button>
					</Space>
				</Space>
			</Card>
		</div>
	);
};


export default Lanepage;
