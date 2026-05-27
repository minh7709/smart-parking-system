import { test, expect } from "@playwright/test";

test("Happy path guard check-in/check-out", async ({ page }) => {
  // Dang nhap voi tai khoan bao ve
  await page.goto("/login");
  await page.getByPlaceholder("Tên đăng nhập").fill("guard");
  await page.getByPlaceholder("Mật khẩu").fill("Guard@123");
  await page.getByRole("button", { name: "Đăng nhập" }).click();

  await expect(page).toHaveURL(/\/lane/);

  // Chon lan vao va lan ra
  await page.getByText("Làn vào").click();
  await page.locator(".ant-select-item-option").first().click();

  await page.getByText("Làn ra").click();
  await page.locator(".ant-select-item-option").first().click();

  await page.getByRole("button", { name: "Vào màn hình giám sát" }).click();
  await expect(page).toHaveURL(/\/monitor/);

  // Neu modal xac nhan check-in/check-out xuat hien thi xu ly
  const confirmIn = page.getByRole("button", { name: /Xác nhận check-in/i });
  if (await confirmIn.isVisible()) {
    await confirmIn.click();
  }

  const confirmOut = page.getByRole("button", { name: /Xác nhận check-out/i });
  if (await confirmOut.isVisible()) {
    await confirmOut.click();
  }

  await expect(page.getByText("Lịch sử")).toBeVisible();
});
