import { render, screen, fireEvent } from "@testing-library/react";
import ResetPasswordForm from "../features/auth/components/ResetPasswordForm";

describe("ResetPasswordForm", () => {
  test("goi onSubmit khi xac nhan", () => {
    const onSubmit = jest.fn((e) => e.preventDefault());
    const onNewPasswordChange = jest.fn();
    const onBack = jest.fn();

    render(
      <ResetPasswordForm
        newPassword=""
        loading={false}
        onNewPasswordChange={onNewPasswordChange}
        onSubmit={onSubmit}
        onBack={onBack}
      />
    );

    fireEvent.change(
      screen.getByPlaceholderText(
        "Mật khẩu mới (tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số)"
      ),
      { target: { value: "NewPass123" } }
    );
    fireEvent.click(screen.getByRole("button", { name: "Xác nhận" }));

    expect(onNewPasswordChange).toHaveBeenCalledWith("NewPass123");
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});
