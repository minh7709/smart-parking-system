import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LanePage from "../features/parking/pages/Lanepage";
import { getActiveLanesApi } from "../features/parking/api/lane.api";

jest.mock("../features/parking/api/lane.api", () => ({
  getActiveLanesApi: jest.fn(),
}));

jest.mock("../hooks/useNotification", () => ({
  useNotification: () => ({
    success: jest.fn(),
    error: jest.fn(),
    apiError: jest.fn(),
    warning: jest.fn(),
  }),
}));

jest.mock("../hooks/useLogout", () => () => jest.fn());

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => jest.fn(),
}));

describe("LanePage", () => {
  test("load danh sach lan hoat dong", async () => {
    getActiveLanesApi.mockResolvedValue({
      data: [
        { id: "1", laneName: "IN-1", laneType: { value: "IN" } },
        { id: "2", laneName: "OUT-1", laneType: { value: "OUT" } },
      ],
    });

    render(
      <MemoryRouter>
        <LanePage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("Chọn làn trước khi vào hệ thống giám sát camera")).toBeInTheDocument();
    });
  });
});
