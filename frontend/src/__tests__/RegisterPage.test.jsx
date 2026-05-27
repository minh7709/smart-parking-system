import { render, screen, waitFor } from "@testing-library/react";
import RegisterPage from "../features/parking/pages/RegisterPage";
import * as subscriptionApi from "../features/parking/api/subscriptionApi";

jest.mock("../features/parking/api/subscriptionApi", () => ({
  getSubscriptionsApi: jest.fn(),
  createSubscriptionApi: jest.fn(),
  updateSubscriptionApi: jest.fn(),
  deleteSubscriptionApi: jest.fn(),
}));

jest.mock("../hooks/useNotification", () => ({
  useNotification: () => ({
    success: jest.fn(),
    error: jest.fn(),
    apiError: jest.fn(),
    warning: jest.fn(),
  }),
}));

jest.mock("../utils/storage", () => ({
  getSystemTypes: () => [],
}));

describe("RegisterPage", () => {
  test("load danh sach ve thang", async () => {
    subscriptionApi.getSubscriptionsApi.mockResolvedValue({
      data: { content: [], totalElements: 0 },
    });

    render(<RegisterPage />);

    await waitFor(() => {
      expect(screen.getByText("Tạo mới")).toBeInTheDocument();
    });
  });
});
