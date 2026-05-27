import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import ProfilePage from "../features/auth/pages/ProfilePage";
import { message } from "antd";

const navigateMock = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => navigateMock,
}));

jest.spyOn(message, "error").mockImplementation(() => {});

describe("ProfilePage", () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  test("hien thi thong tin nguoi dung", () => {
    localStorage.setItem(
      "user",
      JSON.stringify({
        fullName: "Nguyen Van A",
        username: "admin",
        phone: "0912345678",
        role: "ADMIN",
        status: "ACTIVE",
      })
    );

    render(
      <MemoryRouter>
        <ProfilePage />
      </MemoryRouter>
    );

    expect(screen.getByText("Nguyen Van A")).toBeInTheDocument();
    expect(screen.getByText("admin")).toBeInTheDocument();
  });
});
