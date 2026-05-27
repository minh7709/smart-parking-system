import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ChangePasswordPage from "../features/auth/pages/ChangePasswordPage";
import * as authApi from "../features/auth/api/auth.api";

jest.mock("../features/auth/api/auth.api", () => ({
  changePasswordApi: jest.fn().mockResolvedValue({}),
}));

jest.mock("../hooks/useNotification", () => ({
  useNotification: () => ({
    success: jest.fn(),
    error: jest.fn(),
    apiError: jest.fn(),
    warning: jest.fn(),
  }),
}));

const navigateMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => navigateMock,
}));

describe("ChangePasswordPage", () => {
  test("doi mat khau thanh cong", async () => {
    render(<ChangePasswordPage />);

    fireEvent.change(screen.getByPlaceholderText("Nhập mật khẩu cũ"), {
      target: { value: "OldPass123" },
    });
    fireEvent.change(screen.getByPlaceholderText("Nhập mật khẩu mới"), {
      target: { value: "NewPass123" },
    });
    fireEvent.change(screen.getByPlaceholderText("Xác nhận mật khẩu mới"), {
      target: { value: "NewPass123" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Đổi mật khẩu" }));

    await waitFor(() => {
      expect(authApi.changePasswordApi).toHaveBeenCalledTimes(1);
      expect(navigateMock).toHaveBeenCalledWith("/profile");
    });
  });
});
