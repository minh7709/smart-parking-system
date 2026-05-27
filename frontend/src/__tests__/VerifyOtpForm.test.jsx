import { render, screen, fireEvent } from "@testing-library/react";
import VerifyOtpForm from "../features/auth/components/VerifyOtpForm";

describe("VerifyOtpForm", () => {
  test("chi cho nhap otp so", () => {
    const onSubmit = jest.fn((e) => e.preventDefault());
    const onOtpChange = jest.fn();
    const onBack = jest.fn();

    render(
      <VerifyOtpForm
        otp=""
        loading={false}
        onOtpChange={onOtpChange}
        onSubmit={onSubmit}
        onBack={onBack}
      />
    );

    const otpInput = screen.getByPlaceholderText("Nhập OTP 6 chữ số");
    fireEvent.change(otpInput, { target: { value: "12ab" } });
    fireEvent.change(otpInput, { target: { value: "123456" } });

    expect(onOtpChange).toHaveBeenCalledWith("123456");
  });
});
