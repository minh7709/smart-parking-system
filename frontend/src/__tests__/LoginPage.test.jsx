import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "../features/auth/pages/LoginPage";
import * as authApi from "../features/auth/api/auth.api";
import * as storage from "../utils/storage";

jest.mock("../features/auth/api/auth.api");

jest.mock("../utils/storage", () => ({
  saveAuthToLocalStorage: jest.fn(),
  fetchAllSystemTypesApi: jest.fn().mockResolvedValue({}),
  saveSystemTypes: jest.fn(),
}));

jest.mock("../hooks/useNotification", () => ({
  useNotification: () => ({
    success: jest.fn(),
    error: jest.fn(),
    apiError: jest.fn(),
    warning: jest.fn(),
  }),
}));

jest.mock("../context/AuthContext", () => ({
  useAuth: () => ({ setUser: jest.fn() }),
}));

const navigateMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => navigateMock,
}));

describe("LoginPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("dang nhap thanh cong va chuyen trang", async () => {
    authApi.loginApi.mockResolvedValue({
      success: true,
      data: {
        accessToken: "token",
        refreshToken: "refresh",
        user: { role: "ADMIN" },
      },
    });

    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Tên đăng nhập"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByPlaceholderText("Mật khẩu"), {
      target: { value: "Admin@123" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Đăng nhập" }));

    await waitFor(() => {
      expect(authApi.loginApi).toHaveBeenCalledTimes(1);
      expect(storage.saveAuthToLocalStorage).toHaveBeenCalled();
      expect(navigateMock).toHaveBeenCalledWith("/admin/dashboard");
    });
  });

  test("khong cho login khi thieu thong tin", async () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole("button", { name: "Đăng nhập" }));

    expect(authApi.loginApi).not.toHaveBeenCalled();
  });
});
