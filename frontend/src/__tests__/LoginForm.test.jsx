import { render, screen, fireEvent } from "@testing-library/react";
import LoginForm from "../features/auth/components/LoginForm";

describe("LoginForm", () => {
  test("goi onSubmit khi submit form", () => {
    const onSubmit = jest.fn((e) => e.preventDefault());
    const onUsernameChange = jest.fn();
    const onPasswordChange = jest.fn();
    const onRememberMeChange = jest.fn();
    const onForgotPassword = jest.fn();

    render(
      <LoginForm
        username=""
        password=""
        rememberMe={false}
        loading={false}
        onUsernameChange={onUsernameChange}
        onPasswordChange={onPasswordChange}
        onRememberMeChange={onRememberMeChange}
        onSubmit={onSubmit}
        onForgotPassword={onForgotPassword}
      />
    );

    fireEvent.change(screen.getByPlaceholderText("Tên đăng nhập"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByPlaceholderText("Mật khẩu"), {
      target: { value: "Admin@123" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Đăng nhập" }));

    expect(onSubmit).toHaveBeenCalledTimes(1);
    expect(onUsernameChange).toHaveBeenCalledWith("admin");
    expect(onPasswordChange).toHaveBeenCalledWith("Admin@123");
  });
});
