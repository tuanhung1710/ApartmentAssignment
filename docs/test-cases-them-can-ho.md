# Test Cases – Thêm căn hộ (US-APT-01)

| Mục | Nội dung |
|-----|----------|
| **Feature** | Thêm căn hộ |
| **Module** | Apartment (TV2) |
| **URL** | `/apartment?action=create` |
| **Build/Role test** | Tomcat 10 · DB `apartments` đã tạo · user seed |
| **Tài liệu liên quan** | US, UI Form, Validation Rules |

### Tài khoản dùng test

| Username | Password | Role |
|----------|----------|------|
| manager | 123456 | MANAGER (chính) |
| admin | 123456 | ADMIN |
| staff | 123456 | STAFF (negative quyền) |
| resident1 | 123456 | RESIDENT (negative quyền) |

### Quy ước Priority

| Priority | Ý nghĩa |
|----------|---------|
| **P0** | Chặn release nếu fail |
| **P1** | Quan trọng |
| **P2** | Bổ sung / biên |

### Kết quả chạy (tester điền)

| Status | Ký hiệu |
|--------|---------|
| Pass | ✅ |
| Fail | ❌ |
| Blocked | ⛔ |
| Not run | ⬜ |

---

# A. POSITIVE TEST CASES

## TC-APT-C-001 – Thêm căn hộ thành công (full field)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-001 |
| **Type** | Positive |
| **Priority** | P0 |
| **Test Scenario** | Manager nhập đủ field hợp lệ và lưu thành công |
| **Preconditions** | Login `manager`; mã `A-1201` chưa tồn tại |
| **Test Steps** | 1. Vào menu **Căn hộ**<br>2. Bấm **Thêm căn hộ**<br>3. Nhập: Mã `A-1201`, Tòa `Tòa A`, Tầng `12`, DT `75.5`, Loại `OWNED`, Status `ACTIVE`, Notes `Căn góc`<br>4. Bấm **Lưu** |
| **Expected Result** | Redirect list; flash *Thêm căn hộ thành công.*; có dòng `A-1201` trong bảng; 1 row mới trong DB |
| **Actual Result** | |
| **Status** | ⬜ |

---

## TC-APT-C-002 – Thêm thành công với notes rỗng

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-002 |
| **Type** | Positive |
| **Priority** | P1 |
| **Test Scenario** | Ghi chú để trống vẫn lưu được |
| **Preconditions** | Login manager; mã `A-1202` chưa có |
| **Test Steps** | 1. Mở form thêm<br>2. Nhập mã `A-1202`, tòa `Tòa A`, tầng `5`, DT `60`, OWNED, ACTIVE<br>3. Notes để trống<br>4. Lưu |
| **Expected Result** | Thành công; `notes` null/rỗng trong DB |
| **Status** | ⬜ |

---

## TC-APT-C-003 – Default status ACTIVE khi để mặc định

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-003 |
| **Type** | Positive |
| **Priority** | P1 |
| **Test Scenario** | Không đổi status (mặc định ACTIVE) vẫn lưu ACTIVE |
| **Preconditions** | Login manager; mã `B-0101` chưa có |
| **Test Steps** | 1. Mở form<br>2. Nhập field bắt buộc, giữ Status = ACTIVE<br>3. Lưu |
| **Expected Result** | DB `status = ACTIVE` |
| **Status** | ⬜ |

---

## TC-APT-C-004 – Thêm căn INACTIVE hợp lệ

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-004 |
| **Type** | Positive |
| **Priority** | P1 |
| **Test Scenario** | Cho phép tạo căn trạng thái INACTIVE |
| **Preconditions** | Login manager; mã `C-0001` chưa có |
| **Test Steps** | 1. Form thêm: mã `C-0001`, tòa `Tòa C`, tầng `1`, DT `45`, RENTED, **INACTIVE**<br>2. Lưu |
| **Expected Result** | Thành công; badge/status INACTIVE trên list |
| **Status** | ⬜ |

---

## TC-APT-C-005 – Loại hình RENTED

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-005 |
| **Type** | Positive |
| **Priority** | P1 |
| **Test Scenario** | occupancyType = RENTED hợp lệ |
| **Preconditions** | Login manager; mã `R-100` chưa có |
| **Test Steps** | Nhập hợp lệ + Loại hình **RENTED** → Lưu |
| **Expected Result** | Thành công; occupancy RENTED |
| **Status** | ⬜ |

---

## TC-APT-C-006 – Floor biên 0 (tầng trệt)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-006 |
| **Type** | Positive · Boundary |
| **Priority** | P1 |
| **Test Scenario** | floorNumber = 0 được chấp nhận |
| **Preconditions** | Login manager; mã `G-000` chưa có |
| **Test Steps** | Tầng = `0`, các field khác hợp lệ → Lưu |
| **Expected Result** | Thành công; floor_number = 0 |
| **Status** | ⬜ |

---

## TC-APT-C-007 – Floor biên 200

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-007 |
| **Type** | Positive · Boundary |
| **Priority** | P1 |
| **Test Scenario** | floorNumber = 200 được chấp nhận |
| **Preconditions** | Login manager; mã `G-200` chưa có |
| **Test Steps** | Tầng = `200` + field hợp lệ → Lưu |
| **Expected Result** | Thành công |
| **Status** | ⬜ |

---

## TC-APT-C-008 – Area biên 15

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-008 |
| **Type** | Positive · Boundary |
| **Priority** | P1 |
| **Test Scenario** | areaM2 = 15 hợp lệ (tối thiểu) |
| **Preconditions** | Login manager; mã `S-001` chưa có |
| **Test Steps** | DT = `15` + field hợp lệ → Lưu |
| **Expected Result** | Thành công |
| **Status** | ⬜ |

---

## TC-APT-C-009 – Area biên 10000

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-009 |
| **Type** | Positive · Boundary |
| **Priority** | P1 |
| **Test Scenario** | areaM2 = 10000 hợp lệ |
| **Preconditions** | Login manager; mã `S-MAX` chưa có |
| **Test Steps** | DT = `10000` → Lưu |
| **Expected Result** | Thành công |
| **Status** | ⬜ |

---

## TC-APT-C-010 – Admin cũng thêm được

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-010 |
| **Type** | Positive |
| **Priority** | P1 |
| **Test Scenario** | Role ADMIN tạo căn hộ OK |
| **Preconditions** | Login `admin`; mã `ADM-01` chưa có |
| **Test Steps** | Thêm căn hợp lệ → Lưu |
| **Expected Result** | Thành công như manager |
| **Status** | ⬜ |

---

## TC-APT-C-011 – Mã căn format hợp lệ đặc biệt

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-011 |
| **Type** | Positive |
| **Priority** | P2 |
| **Test Scenario** | Mã có `_` và `-` hợp lệ |
| **Preconditions** | Login manager; mã `A_12-01` chưa có |
| **Test Steps** | apartmentCode = `A_12-01` + field hợp lệ → Lưu |
| **Expected Result** | Thành công |
| **Status** | ⬜ |

---

## TC-APT-C-012 – Hủy form không lưu

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-012 |
| **Type** | Positive (hành vi Hủy) |
| **Priority** | P1 |
| **Test Scenario** | Bấm Hủy không tạo bản ghi |
| **Preconditions** | Login manager; đếm số căn trước |
| **Test Steps** | 1. Mở form thêm<br>2. Gõ vài field<br>3. Bấm **Hủy** |
| **Expected Result** | Về list; số bản ghi DB không tăng |
| **Status** | ⬜ |

---

## TC-APT-C-013 – Mở form từ nút list

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-013 |
| **Type** | Positive · UI |
| **Priority** | P2 |
| **Test Scenario** | Nút Thêm căn hộ điều hướng đúng form |
| **Preconditions** | Login manager |
| **Test Steps** | List → bấm **Thêm căn hộ** |
| **Expected Result** | URL `action=create`; thấy form đủ field |
| **Status** | ⬜ |

---

# B. NEGATIVE TEST CASES

## TC-APT-C-101 – Thiếu mã căn hộ

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-101 |
| **Type** | Negative |
| **Priority** | P0 |
| **Test Scenario** | Bắt buộc apartmentCode |
| **Preconditions** | Login manager |
| **Test Steps** | Để trống Mã căn; điền field khác hợp lệ → Lưu |
| **Expected Result** | Không insert; lỗi *Vui lòng nhập mã căn hộ.*; form giữ data đã nhập |
| **Status** | ⬜ |

---

## TC-APT-C-102 – Mã căn sai format

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-102 |
| **Type** | Negative · Data Format |
| **Priority** | P0 |
| **Test Scenario** | Ký tự đặc biệt / space trong mã |
| **Preconditions** | Login manager |
| **Test Steps** | Nhập mã `A 1201` hoặc `A@01` → Lưu |
| **Expected Result** | Lỗi format mã; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-103 – Mã căn trùng (Duplicate)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-103 |
| **Type** | Negative · Duplicate |
| **Priority** | P0 |
| **Test Scenario** | Không cho trùng apartment_code |
| **Preconditions** | Đã có căn `A-1201` (chạy TC-001 trước) |
| **Test Steps** | Thêm lại mã `A-1201` với field khác hợp lệ → Lưu |
| **Expected Result** | *Mã căn hộ đã tồn tại.*; không thêm row mới |
| **Status** | ⬜ |

---

## TC-APT-C-104 – Thiếu tòa nhà

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-104 |
| **Type** | Negative |
| **Priority** | P0 |
| **Test Scenario** | building required |
| **Preconditions** | Login manager |
| **Test Steps** | Để trống Tòa nhà → Lưu |
| **Expected Result** | *Vui lòng nhập tòa nhà.*; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-105 – Tòa nhà quá 50 ký tự

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-105 |
| **Type** | Negative · Boundary |
| **Priority** | P2 |
| **Test Scenario** | building length > 50 |
| **Preconditions** | Login manager |
| **Test Steps** | Nhập building 51+ ký tự → Lưu |
| **Expected Result** | *Tên tòa nhà tối đa 50 ký tự.* |
| **Status** | ⬜ |

---

## TC-APT-C-106 – Floor rỗng / không phải số

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-106 |
| **Type** | Negative · Data Format |
| **Priority** | P0 |
| **Test Scenario** | floorNumber invalid format |
| **Preconditions** | Login manager |
| **Test Steps** | Tầng để trống hoặc nhập `abc` → Lưu |
| **Expected Result** | *Tầng phải là số nguyên từ 0 đến 200.* |
| **Status** | ⬜ |

---

## TC-APT-C-107 – Floor < 0 (boundary)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-107 |
| **Type** | Negative · Boundary |
| **Priority** | P0 |
| **Test Scenario** | floorNumber = -1 |
| **Preconditions** | Login manager |
| **Test Steps** | Tầng = `-1` → Lưu |
| **Expected Result** | Lỗi tầng; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-108 – Floor > 200 (boundary)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-108 |
| **Type** | Negative · Boundary |
| **Priority** | P0 |
| **Test Scenario** | floorNumber = 201 |
| **Preconditions** | Login manager |
| **Test Steps** | Tầng = `201` → Lưu |
| **Expected Result** | Lỗi tầng; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-109 – Area = 0 (boundary)

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-109 |
| **Type** | Negative · Boundary |
| **Priority** | P0 |
| **Test Scenario** | areaM2 = 0 không hợp lệ |
| **Preconditions** | Login manager |
| **Test Steps** | DT = `0` → Lưu |
| **Expected Result** | *Diện tích phải từ 15 m² trở lên...*; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-110 – Area âm

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-110 |
| **Type** | Negative · Boundary |
| **Priority** | P1 |
| **Test Scenario** | areaM2 < 0 |
| **Preconditions** | Login manager |
| **Test Steps** | DT = `-10` → Lưu |
| **Expected Result** | Lỗi diện tích |
| **Status** | ⬜ |

---

## TC-APT-C-111 – Area > 10000

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-111 |
| **Type** | Negative · Boundary |
| **Priority** | P1 |
| **Test Scenario** | areaM2 = 10000.01 |
| **Preconditions** | Login manager |
| **Test Steps** | DT = `10000.01` → Lưu |
| **Expected Result** | Lỗi diện tích |
| **Status** | ⬜ |

---

## TC-APT-C-112 – Area không phải số

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-112 |
| **Type** | Negative · Data Format |
| **Priority** | P1 |
| **Test Scenario** | area format invalid |
| **Preconditions** | Login manager |
| **Test Steps** | DT = `abc` (nếu bypass HTML) → Lưu |
| **Expected Result** | Lỗi diện tích server-side |
| **Status** | ⬜ |

---

## TC-APT-C-113 – Owner Type / occupancy không hợp lệ

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-113 |
| **Type** | Negative |
| **Priority** | P1 |
| **Test Scenario** | occupancyType ngoài OWNED/RENTED |
| **Preconditions** | Login manager; có thể dùng DevTools sửa value select |
| **Test Steps** | Gửi `occupancyType=LEASE` → Lưu |
| **Expected Result** | *Loại hình sử dụng không hợp lệ (OWNED / RENTED).* |
| **Status** | ⬜ |

---

## TC-APT-C-114 – Status không hợp lệ

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-114 |
| **Type** | Negative |
| **Priority** | P1 |
| **Test Scenario** | status ngoài ACTIVE/INACTIVE |
| **Preconditions** | Login manager |
| **Test Steps** | Gửi `status=DELETED` → Lưu |
| **Expected Result** | *Trạng thái không hợp lệ (ACTIVE / INACTIVE).* |
| **Status** | ⬜ |

---

## TC-APT-C-115 – Notes > 500 ký tự

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-115 |
| **Type** | Negative · Boundary |
| **Priority** | P2 |
| **Test Scenario** | notes vượt max |
| **Preconditions** | Login manager |
| **Test Steps** | Notes 501 ký tự → Lưu |
| **Expected Result** | *Ghi chú tối đa 500 ký tự.* |
| **Status** | ⬜ |

---

## TC-APT-C-116 – STAFF không được thêm

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-116 |
| **Type** | Negative · Security |
| **Priority** | P0 |
| **Test Scenario** | Role STAFF bị chặn create |
| **Preconditions** | Login `staff` |
| **Test Steps** | 1. Vào list căn hộ (nếu được)<br>2. Kiểm tra không có nút Thêm<br>3. Gõ URL `/apartment?action=create` |
| **Expected Result** | Không mở form tạo thành công; thông báo không có quyền / redirect |
| **Status** | ⬜ |

---

## TC-APT-C-117 – RESIDENT không được thêm

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-117 |
| **Type** | Negative · Security |
| **Priority** | P0 |
| **Test Scenario** | RESIDENT không create |
| **Preconditions** | Login `resident1` |
| **Test Steps** | Truy cập `/apartment?action=create` hoặc POST create |
| **Expected Result** | Bị chặn (403 / redirect / không quyền) |
| **Status** | ⬜ |

---

## TC-APT-C-118 – Chưa login

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-118 |
| **Type** | Negative · Security |
| **Priority** | P0 |
| **Test Scenario** | Anonymous không vào form |
| **Preconditions** | Logout / session hết |
| **Test Steps** | Mở `/apartment?action=create` |
| **Expected Result** | Redirect `/auth?action=login` |
| **Status** | ⬜ |

---

## TC-APT-C-119 – Mã căn chỉ space

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-119 |
| **Type** | Negative |
| **Priority** | P1 |
| **Test Scenario** | trim làm rỗng |
| **Preconditions** | Login manager |
| **Test Steps** | Mã = `   ` → Lưu |
| **Expected Result** | *Vui lòng nhập mã căn hộ.* |
| **Status** | ⬜ |

---

## TC-APT-C-120 – Mã dài > 20

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-120 |
| **Type** | Negative · Boundary |
| **Priority** | P1 |
| **Test Scenario** | apartmentCode length 21 |
| **Preconditions** | Login manager |
| **Test Steps** | Mã 21 ký tự hợp lệ pattern từng phần → Lưu |
| **Expected Result** | Lỗi format/max; không insert |
| **Status** | ⬜ |

---

## TC-APT-C-121 – Giữ form khi multi-error

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-121 |
| **Type** | Negative · UI |
| **Priority** | P1 |
| **Test Scenario** | Nhiều field sai vẫn giữ giá trị đúng đã gõ |
| **Preconditions** | Login manager |
| **Test Steps** | Nhập tòa `Tòa Z`, để trống mã + tầng sai → Lưu |
| **Expected Result** | Hiện ≥1 lỗi; input Tòa vẫn là `Tòa Z` |
| **Status** | ⬜ |

---

## TC-APT-C-122 – F5 sau success không insert 2 lần

| Hạng mục | Nội dung |
|----------|----------|
| **Test ID** | TC-APT-C-122 |
| **Type** | Positive/Negative regression |
| **Priority** | P1 |
| **Test Scenario** | PRG: redirect sau create |
| **Preconditions** | Login manager |
| **Test Steps** | 1. Create thành công về list<br>2. F5 trang list |
| **Expected Result** | Không phát sinh thêm row trùng; URL là list GET |
| **Status** | ⬜ |

---

# C. Ma trận cover Validation Rule

| Rule / nhóm | Test IDs |
|-------------|----------|
| Apartment Code required/format/max | 101, 102, 119, 120, 011 |
| Floor format/boundary | 006, 007, 106, 107, 108 |
| Area format/boundary | 008, 009, 109–112 |
| Status | 003, 004, 114 |
| Owner Type (occupancy) | 001, 005, 113 |
| Duplicate | 103 |
| Data Format | 102, 106, 112 |
| Boundary Value | 006–009, 105, 107–111, 115, 120 |
| AuthZ | 010, 116–118 |
| Happy path | 001–005, 010 |
| UI/Hủy/PRG | 012, 013, 121, 122 |

---

# D. Smoke test tối thiểu (5 phút)

Chạy lần lượt:

1. **TC-APT-C-001** (happy)  
2. **TC-APT-C-101** (required)  
3. **TC-APT-C-103** (duplicate)  
4. **TC-APT-C-109** (area 0)  
5. **TC-APT-C-116** (staff)  

Pass 5 case → build smoke OK cho UC Thêm căn hộ.

---

# E. Template ghi nhận bug

```text
Bug ID:
Liên quan Test ID:
Mô tả:
Steps:
Expected:
Actual:
Severity: Critical / Major / Minor
Screenshot/log:
```

---

**File code đối chiếu khi fail test**

- `ApartmentController.java` → `handleCreate`, `validateCreate`, `bindForm`
- `ApartmentDAO.java` → `existsByCode`, `insert`
- `form.jsp` / `list.jsp`
