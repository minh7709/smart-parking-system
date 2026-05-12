import axiosClient from '../../../api/axiosClient';
import ENDPOINTS from '../../../api/endpoints';

export const getActiveLanesApi = () => axiosClient.get(ENDPOINTS.guard.activeLanes);