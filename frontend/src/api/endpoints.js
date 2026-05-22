export const API_ENDPOINTS = {
	auth: {
		login: "/v1/auth/login",
		refresh: "/v1/auth/refresh",
		logout: "/v1/auth/logout",
		me: "/v1/auth/me",
		forgotPassword: "/v1/auth/forgot-password",
		verifyOtp: "/v1/auth/verify-otp",
		resetPassword: "/v1/auth/reset-password",
		changePassword: "/v1/auth/change-password",
	},

	guard: {
		activeLanes: "/v1/guard/active-lanes/",

		parkingSession: {
			base: "/v1/guard/parking-session",
			checkIn: "/v1/guard/parking-session/check-in",
			cancelCheckIn: "/v1/guard/parking-session/cancel-check-in",
			checkOut: "/v1/guard/parking-session/check-out",
			confirmCheckIn: "/v1/guard/parking-session/confirm-check-in",
			confirmCheckOut: "/v1/guard/parking-session/confirm-check-out",
			reportLostCard: "/v1/guard/parking-session/report-incident/lost-card",
			reportIncident: "/v1/guard/parking-session/report-incident",
			countParking: "/v1/guard/parking-session/count",
			byLicensePlate: (licensePlate) => `/v1/guard/parking-session/${licensePlate}`,
			imageById: (id, type) => `/v1/guard/parking-session/${id}/image?type=${type}`,
			imageByUrl: (imageUrl) => `/v1/guard/parking-session/image/by-url?imageUrl=${encodeURIComponent(imageUrl)}`
		},

		subscriptions: {
			base: "/v1/guard/subscriptions/",
			byId: (id) => `/v1/guard/subscriptions/${id}`,
			byVehicleId: (vehicleId) => `/v1/guard/subscriptions/vehicle/${vehicleId}`,
			byLicensePlate: (licensePlate) =>
				`/v1/guard/subscriptions/license-plate/${licensePlate}`,
		},
	},

	vehicles: {
		base: "/v1/vehicles/",
		byId: (id) => `/v1/vehicles/${id}`,
		byLicensePlate: (licensePlate) => `/v1/vehicles/license-plate/${licensePlate}`,
	},

	admin: {
		users: {
			base: "/v1/admin/users",
			byId: (id) => `/v1/admin/users/${id}`,
		},

		subscriptions: {
			base: "/v1/admin/subscriptions",
			cancel: (id) => `/v1/admin/subscriptions/${id}/cancel`,
			byId: (id) => `/v1/admin/subscriptions/${id}`,
			byVehicleId: (vehicleId) => `/v1/admin/subscriptions/vehicle/${vehicleId}`,
		},

		statistics: {
			summary: "/v1/admin/statistics/summary",
			summaryExport: "/v1/admin/statistics/summary/export",
			trafficTimeline: "/v1/admin/statistics/traffic/timeline",
			trafficLanes: "/v1/admin/statistics/traffic/lanes",
			revenueTimeline: "/v1/admin/statistics/revenue/timeline",
			revenueBreakdown: "/v1/admin/statistics/revenue/breakdown",
			revenuePenalties: "/v1/admin/statistics/revenue/penalties",
			invoices: "/v1/admin/statistics/invoices",
		},

		incidents: {
			base: "/v1/admin/incidents",
			evidence: "/v1/admin/incidents/evidence",
		},

		lanes: {
			base: "/v1/admin/lanes",
			byId: (id) => `/v1/admin/lanes/${id}`,
		},

		pricingRules: {
			base: "/v1/admin/pricing-rules",
			byId: (id) => `/v1/admin/pricing-rules/${id}`,
			activate: (id) => `/v1/admin/pricing-rules/${id}/activate`,
			deactivate: (id) => `/v1/admin/pricing-rules/${id}/deactivate`,
		},

		subscriptionPricing: {
			base: "/v1/admin/pricing-subscription",
			byId: (id) => `/v1/admin/pricing-subscription/${id}`,
			activate: (id) => `/v1/admin/pricing-subscription/${id}/activate`,
		},
	},

	type: {
		laneStatuses: "/v1/type/lane-statuses",
		laneTypes: "/v1/type/lane-types",
		vehicleTypes: "/v1/type/vehicle-types",
		sessionStatuses: "/v1/type/session-statuses",
		paymentStatuses: "/v1/type/payment-statuses",
		paymentMethods: "/v1/type/payment-methods",
		pricingStrategies: "/v1/type/pricing-strategies",
		incidentTypes: "/v1/type/incident-types",
		userRoles: "/v1/type/user-roles",
		userStatuses: "/v1/type/user-statuses",
		subscriptionTypes: "/v1/type/subscription-types",
		subscriptionStatuses: "/v1/type/subscription-statuses",
	},
};

export default API_ENDPOINTS;