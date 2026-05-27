import { render, screen, fireEvent } from "@testing-library/react";
import ForgotPasswordForm from "../features/auth/components/ForgotPasswordForm";

describe("ForgotPasswordForm", () => {
  test("goi onSubmit khi nhan gui OTP", () => {
    const onSubmit = jest.fn((e) => e.preventDefault());
    const onPhoneChange = jest.fn();
    const onBack = jest.fn();

    render(
      <ForgotPasswordForm
        phone=""
        loading={false}
        onPhoneChange={onPhoneChange}
        onSubmit={onSubmit}
        onBack={onBack}
      />
    );

    fireEvent.change(screen.getByPlaceholderText("Nhập số điện thoại"), {
      target: { value: "0912345678" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Gửi OTP" }));

    expect(onPhoneChange).toHaveBeenCalledWith("0912345678");
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});
