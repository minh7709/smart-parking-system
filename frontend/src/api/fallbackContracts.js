import API_ENDPOINTS from './endpoints';

const nowIso = '2026-04-08T00:00:00Z';

const ok = (data, message) => ({
  success: true,
  message,
  data,
  timestamp: nowIso,
});

export const FALLBACK_API_CONTRACTS = [
  {
    key: 'auth.login',
    method: 'POST',
    path: API_ENDPOINTS.auth.login,
    requestBody: {
      username: 'guard01',
      password: 'Guard@123',
      rememberMe: true,
    },
    responseBody: ok(
      {
        accessToken: 'jwt-access-token',
        refreshToken: 'jwt-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: {
          id: 'd7342f6f-6d37-4260-a0f2-fdb522f013a9',
          username: 'guard01',
          fullName: 'Nguyen Van Guard',
          role: 'GUARD',
          status: 'ACTIVE',
          rememberMe: true,
        },
      },
      'Login successful'
    ),
  },
  {
    key: 'auth.refresh',
    method: 'POST',
    path: API_ENDPOINTS.auth.refresh,
    requestBody: {
      refreshToken: 'jwt-refresh-token',
    },
    responseBody: ok(
      {
        accessToken: 'new-jwt-access-token',
        refreshToken: 'new-jwt-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
      },
      'Refresh Token successful'
    ),
  },
  {
    key: 'auth.logout',
    method: 'POST',
    path: API_ENDPOINTS.auth.logout,
    requestBody: {
      refreshToken: 'jwt-refresh-token',
    },
    responseBody: ok(null, 'Logout successful'),
  },
  {
    key: 'auth.me',
    method: 'GET',
    path: API_ENDPOINTS.auth.me,
    requestBody: null,
    responseBody: ok(
      {
        id: 'd7342f6f-6d37-4260-a0f2-fdb522f013a9',
        username: 'guard01',
        fullName: 'Nguyen Van Guard',
        role: 'GUARD',
        status: 'ACTIVE',
      },
      'Current user info retrieved successfully'
    ),
  },
  {
    key: 'auth.forgotPassword',
    method: 'POST',
    path: API_ENDPOINTS.auth.forgotPassword,
    requestBody: {
      phone: '0912345678',
    },
    responseBody: ok('0912345678', 'Ma otp da duoc gui den so dien thoai cua ban'),
  },
  {
    key: 'auth.verifyOtp',
    method: 'POST',
    path: API_ENDPOINTS.auth.verifyOtp,
    requestBody: {
      phone: '0912345678',
      otp: '123456',
    },
    responseBody: ok('reset-password-token', 'OTP verified successfully. start resetting password'),
  },
  {
    key: 'auth.resetPassword',
    method: 'POST',
    path: API_ENDPOINTS.auth.resetPassword,
    requestBody: {
      newPassword: 'NewPass@123',
      token: 'reset-password-token',
    },
    responseBody: ok(null, 'Password reset successful'),
  },
  {
    key: 'guard.activeLanes',
    method: 'GET',
    path: API_ENDPOINTS.guard.activeLanes,
    requestBody: null,
    responseBody: ok(
      [
        {
          id: '69c77e9c-b0f9-4f26-b912-df9fcd036d5f',
          laneName: 'Lane In 01',
          laneType: 'ENTRY',
          status: 'ACTIVE',
          ipCamera: 'rtsp://192.168.1.10/live',
        },
      ],
      'Active lanes retrieved successfully'
    ),
  },
  {
    key: 'guard.parkingSession.checkIn',
    method: 'POST',
    path: API_ENDPOINTS.guard.parkingSession.checkIn,
    requestBody: {
      multipart: true,
      request: {
        entryLaneId: '69c77e9c-b0f9-4f26-b912-df9fcd036d5f',
        vehicleType: 'MOTORBIKE',
      },
      image: 'File(binary)',
    },
    responseBody: ok(
      {
        id: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
        plateInOcr: '59A11234',
        finalPlate: '59A11234',
        timeIn: nowIso,
        status: 'PENDING_CONFIRMATION',
        isMonth: false,
        vehicleType: 'MOTORBIKE',
      },
      'Check-in successful'
    ),
  },
  {
    key: 'guard.parkingSession.checkOut',
    method: 'POST',
    path: API_ENDPOINTS.guard.parkingSession.checkOut,
    requestBody: {
      multipart: true,
      request: {
        exitLaneId: '8d6ef2bd-fec0-45f6-ae85-7c663eb8e3ce',
        parkingSessionId: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
      },
      image: 'File(binary)',
    },
    responseBody: ok(
      {
        id: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
        plateOutOcr: '59A11234',
        finalPlate: '59A11234',
        timeOut: nowIso,
        status: 'PENDING_PAYMENT',
        fee: 5000,
        isMonth: false,
        vehicleType: 'MOTORBIKE',
      },
      'Check-out successful'
    ),
  },
  {
    key: 'guard.parkingSession.confirmCheckIn',
    method: 'POST',
    path: API_ENDPOINTS.guard.parkingSession.confirmCheckIn,
    requestBody: {
      entryLaneId: '69c77e9c-b0f9-4f26-b912-df9fcd036d5f',
      finalPlate: '59A11234',
      parkingSessionId: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
    },
    responseBody: ok(
      {
        id: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
        finalPlate: '59A11234',
        status: 'IN_PARKING',
      },
      'Check-in confirmed successfully'
    ),
  },
  {
    key: 'guard.parkingSession.confirmCheckOut',
    method: 'POST',
    path: API_ENDPOINTS.guard.parkingSession.confirmCheckOut,
    requestBody: {
      finalPlate: '59A11234',
      paymentMethod: 'CASH',
      parkingSessionId: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
    },
    responseBody: ok(null, 'Check-out confirmed successfully'),
  },
  {
    key: 'guard.parkingSession.reportLostCard',
    method: 'POST',
    path: API_ENDPOINTS.guard.parkingSession.reportLostCard,
    requestBody: {
      multipart: true,
      request: {
        exitLaneId: '8d6ef2bd-fec0-45f6-ae85-7c663eb8e3ce',
        description: 'Lost card at payment booth',
      },
      image: 'File(binary)',
    },
    responseBody: ok(
      {
        id: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
        status: 'LOST_CARD',
        fee: 50000,
      },
      'Incident reported successfully'
    ),
  },
  {
    key: 'guard.parkingSession.byPlate',
    method: 'GET',
    path: API_ENDPOINTS.guard.parkingSession.byPlate(':plate'),
    requestBody: null,
    responseBody: ok(
      {
        id: '64a6c64b-7814-4078-a4a5-df1f2b0c72f4',
        finalPlate: '59A11234',
        status: 'IN_PARKING',
      },
      'Parking session retrieved successfully'
    ),
  },
  {
    key: 'guard.parkingSession.list',
    method: 'GET',
    path: API_ENDPOINTS.guard.parkingSession.base,
    requestBody: {
      query: {
        status: 'IN_PARKING',
        page: 0,
        size: 20,
      },
    },
    responseBody: ok(
      {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
      'Parking sessions retrieved successfully'
    ),
  },
  {
    key: 'admin.pricingRules.create',
    method: 'POST',
    path: API_ENDPOINTS.pricingRules.base,
    requestBody: {
      ruleName: 'Bike - Time Block',
      vehicleType: 'MOTORBIKE',
      pricingStrategy: 'BLOCK',
      basePrice: 5000,
      blockMinutes: 60,
      thresholdMinutes: 30,
      thresholdPrice: 3000,
      maxPricePerDay: 50000,
      penaltyFee: 20000,
      isActive: true,
    },
    responseBody: ok(
      {
        id: 'd2b58d98-18ea-48f4-b638-67d47a0e9534',
        ruleName: 'Bike - Time Block',
        vehicleType: 'MOTORBIKE',
        pricingStrategy: 'BLOCK',
      },
      'Pricing rule created successfully'
    ),
  },
  {
    key: 'admin.pricingRules.update',
    method: 'PUT',
    path: API_ENDPOINTS.pricingRules.byId(':id'),
    requestBody: {
      ruleName: 'Bike - Time Block Updated',
      vehicleType: 'MOTORBIKE',
      pricingStrategy: 'BLOCK',
      basePrice: 6000,
      penaltyFee: 25000,
      isActive: true,
    },
    responseBody: ok(
      {
        id: ':id',
        ruleName: 'Bike - Time Block Updated',
      },
      'Pricing rule updated successfully'
    ),
  },
  {
    key: 'admin.pricingRules.detail',
    method: 'GET',
    path: API_ENDPOINTS.pricingRules.byId(':id'),
    requestBody: null,
    responseBody: ok(
      {
        id: ':id',
        ruleName: 'Bike - Time Block',
      },
      'Pricing rule fetched successfully'
    ),
  },
  {
    key: 'admin.pricingRules.list',
    method: 'GET',
    path: API_ENDPOINTS.pricingRules.base,
    requestBody: {
      query: {
        vehicleType: 'MOTORBIKE',
        page: 0,
        size: 20,
      },
    },
    responseBody: ok(
      {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
      'Pricing rules fetched successfully'
    ),
  },
  {
    key: 'admin.pricingRules.delete',
    method: 'DELETE',
    path: API_ENDPOINTS.pricingRules.byId(':id'),
    requestBody: null,
    responseBody: ok(null, 'Pricing rule deleted successfully'),
  },
  {
    key: 'admin.pricingRules.activate',
    method: 'POST',
    path: API_ENDPOINTS.pricingRules.activate(':id'),
    requestBody: null,
    responseBody: ok(
      {
        id: ':id',
        isActive: true,
      },
      'Pricing rule activated successfully'
    ),
  },
  {
    key: 'admin.pricingRules.deactivate',
    method: 'POST',
    path: API_ENDPOINTS.pricingRules.deactivate(':id'),
    requestBody: null,
    responseBody: ok(
      {
        id: ':id',
        isActive: false,
      },
      'Pricing rule deactivated successfully'
    ),
  },
  {
    key: 'admin.users.list',
    method: 'GET',
    path: API_ENDPOINTS.admin.users,
    requestBody: null,
    responseBody: [
      {
        id: 'd7342f6f-6d37-4260-a0f2-fdb522f013a9',
        username: 'admin01',
        fullName: 'System Admin',
        role: 'ADMIN',
      },
    ],
  },
  {
    key: 'admin.users.byId',
    method: 'GET',
    path: API_ENDPOINTS.admin.userById(':id'),
    requestBody: null,
    responseBody: {
      id: ':id',
      username: 'guard01',
      fullName: 'Nguyen Van Guard',
      role: 'GUARD',
    },
  },
  {
    key: 'admin.dashboard',
    method: 'GET',
    path: API_ENDPOINTS.admin.dashboard,
    requestBody: null,
    responseBody: {
      adminUser: 'admin01',
      totalUsers: 12,
      timestamp: nowIso,
      message: 'Welcome to Smart Parking Admin Dashboard',
    },
  },
  {
    key: 'admin.verifyAccess',
    method: 'GET',
    path: API_ENDPOINTS.admin.verifyAccess,
    requestBody: null,
    responseBody: {
      isAdmin: true,
      username: 'admin01',
      authorities: ['ROLE_ADMIN'],
    },
  },
  {
    key: 'type.allEnums',
    method: 'GET',
    path: '/v1/type/*',
    requestBody: null,
    responseBody: {
      laneStatuses: ['ACTIVE', 'INACTIVE'],
      laneTypes: ['ENTRY', 'EXIT'],
      vehicleTypes: ['MOTORBIKE', 'CAR'],
      sessionStatuses: ['PENDING_CONFIRMATION', 'IN_PARKING', 'PENDING_PAYMENT', 'COMPLETED'],
      paymentStatuses: ['UNPAID', 'PAID', 'FAILED'],
      paymentMethods: ['CASH', 'QR', 'CARD'],
      pricingStrategies: ['BLOCK', 'THRESHOLD', 'PROGRESSIVE'],
      incidentTypes: ['LOST_CARD'],
      userRoles: ['ADMIN', 'GUARD'],
      userStatuses: ['ACTIVE', 'INACTIVE'],
      subscriptionTypes: ['MONTHLY', 'QUARTERLY', 'YEARLY'],
      subscriptionStatuses: ['ACTIVE', 'EXPIRED'],
    },
  },
];

export const FALLBACK_API_BY_KEY = FALLBACK_API_CONTRACTS.reduce((acc, item) => {
  acc[item.key] = item;
  return acc;
}, {});

export const getFallbackContract = (key) => FALLBACK_API_BY_KEY[key] || null;
