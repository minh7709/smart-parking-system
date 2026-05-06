export const API_ENDPOINTS = {
	auth: {
		login: '/v1/auth/login',
		refresh: '/v1/auth/refresh',
		logout: '/v1/auth/logout',
		me: '/v1/auth/me',
		forgotPassword: '/v1/auth/forgot-password',
		verifyOtp: '/v1/auth/verify-otp',
		resetPassword: '/v1/auth/reset-password',
	},
	admin: {
		users: '/v1/admin/users',
		userById: (id) => `/v1/admin/users/${id}`,
		dashboard: '/v1/admin/dashboard',
		verifyAccess: '/v1/admin/verify-access',
	},
	pricingRules: {
		base: '/v1/admin/pricing-rules',
		byId: (id) => `/v1/admin/pricing-rules/${id}`,
		activate: (id) => `/v1/admin/pricing-rules/${id}/activate`,
		deactivate: (id) => `/v1/admin/pricing-rules/${id}/deactivate`,
	},
	guard: {
		activeLanes: '/v1/guard/active-lanes/',
		parkingSession: {
			base: '/v1/guard/parking-session',
			byPlate: (plate) => `/v1/guard/parking-session/${plate}`,
			checkIn: '/v1/guard/parking-session/check-in',
			checkOut: '/v1/guard/parking-session/check-out',
			confirmCheckIn: '/v1/guard/parking-session/confirm-check-in',
			confirmCheckOut: '/v1/guard/parking-session/confirm-check-out',
			reportLostCard: '/v1/guard/parking-session/report-incident/lost-card',
		},
	},
	type: {
		laneStatuses: '/v1/type/lane-statuses',
		laneTypes: '/v1/type/lane-types',
		vehicleTypes: '/v1/type/vehicle-types',
		sessionStatuses: '/v1/type/session-statuses',
		paymentStatuses: '/v1/type/payment-statuses',
		paymentMethods: '/v1/type/payment-methods',
		pricingStrategies: '/v1/type/pricing-strategies',
		incidentTypes: '/v1/type/incident-types',
		userRoles: '/v1/type/user-roles',
		userStatuses: '/v1/type/user-statuses',
		subscriptionTypes: '/v1/type/subscription-types',
		subscriptionStatuses: '/v1/type/subscription-statuses',
	},
};

export default API_ENDPOINTS;
