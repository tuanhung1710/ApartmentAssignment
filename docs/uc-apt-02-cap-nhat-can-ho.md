# UC-APT-02 – Cập nhật căn hộ (Đặc tả chức năng)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-02 |
| **Tên** | Cập nhật căn hộ |
| **Module** | Apartment Management (TV2) |
| **Actor** | Admin, Manager |
| **Priority** | High |
| **Liên quan** | UC-APT-01 Thêm căn hộ |
| **URL** | `GET /apartment?action=edit&id={id}` · `POST /apartment` `action=update` |

---

## 1. User Story

**Là** Quản lý tòa nhà (Manager) hoặc Quản trị viên (Admin),  
**tôi muốn** cập nhật thông tin một căn hộ đã có (tòa nhà, tầng, diện tích, loại hình, trạng thái, ghi chú),  
**để** dữ liệu căn hộ luôn chính xác phục vụ gán cư dân và tính phí.

### Giá trị nghiệp vụ
- Sửa sai sót khi nhập liệu ban đầu.
- Cập nhật trạng thái ACTIVE/INACTIVE khi căn ngừng sử dụng tạm thời.
- Không làm hỏng liên kết theo `apartment_id` (mã căn **không đổi** sau khi tạo).

---

## 2. Preconditions

1. Người dùng đã đăng nhập.
2. Vai trò là **ADMIN** hoặc **MANAGER**.
3. Tồn tại ít nhất một căn hộ trong hệ thống (đã tạo qua UC-APT-01 hoặc seed).
4. Biết `apartment_id` hợp lệ cần sửa (từ danh sách).
5. Database và bảng `apartments` sẵn sàng.

---

## 3. Main Flow

| Bước | Actor | Hệ thống |
|------|--------|----------|
| 1 | Mở **Danh sách căn hộ** | `GET /apartment?action=list` |
| 2 | Bấm **Sửa** trên một dòng | `GET /apartment?action=edit&id={id}` |
| 3 | — | Kiểm tra login + role; `findById`; load form có data |
| 4 | Xem **Mã căn** (chỉ đọc), sửa các field được phép | Form edit |
| 5 | Bấm **Lưu** | `POST action=update` + `apartmentId` |
| 6 | — | Validate field; `update` DB; set `updated_at` |
| 7 | — | Flash *Cập nhật căn hộ thành công.* |
| 8 | — | Redirect về list |
| 9 | Thấy flash + dữ liệu đã đổi trên list | — |

---

## 4. Alternative Flow

### AF-01: Hủy cập nhật
- User bấm **Hủy** / Về danh sách → không lưu, về list.

### AF-02: Validate fail
- Server trả form edit kèm `errors` + giữ giá trị đã sửa.

### AF-03: Chỉ đổi một phần field
- User chỉ sửa notes hoặc status → vẫn update các field gửi lên (full form submit).

---

## 5. Exception Flow

| ID | Tình huống | Xử lý |
|----|------------|--------|
| EF-01 | Chưa login | Redirect login |
| EF-02 | Không đủ quyền (STAFF/RESIDENT) | Flash / 403 / không cho edit |
| EF-03 | Thiếu `id` hoặc `id` không phải số | Flash lỗi + redirect list |
| EF-04 | `id` không tồn tại | *Không tìm thấy căn hộ.* + redirect list |
| EF-05 | Validate field fail | Forward form + errors |
| EF-06 | Update DB fail | *Không thể cập nhật căn hộ. Vui lòng thử lại.* + giữ form |
| EF-07 | POST thiếu/sai `apartmentId` | Lỗi + không update |

---

## 6. Postconditions

### Thành công
- Bản ghi `apartments` theo `apartment_id` đã đổi các cột cho phép.
- `apartment_code` **không đổi**.
- `updated_at` được cập nhật (SYSUTCDATETIME).
- User thấy flash success và list phản ánh data mới.

### Thất bại
- DB không đổi (hoặc không commit thay đổi).
- User ở form edit kèm lỗi, hoặc bị đưa về list/login.

---

## 7. Acceptance Criteria

| # | Tiêu chí |
|---|----------|
| AC-01 | ADMIN/MANAGER mở được form sửa từ list |
| AC-02 | Form hiển thị đúng data hiện tại của căn |
| AC-03 | Mã căn **hiển thị nhưng không sửa được** |
| AC-04 | Sửa building/floor/area/occupancy/status/notes hợp lệ → lưu OK + redirect list + flash |
| AC-05 | Validate giống rule create (trừ unique code) |
| AC-06 | id không tồn tại → không crash, thông báo rõ |
| AC-07 | STAFF/RESIDENT không cập nhật được |
| AC-08 | Hủy không lưu thay đổi |
| AC-09 | F5 sau success không update 2 lần (PRG redirect) |
| AC-10 | Lỗi validate giữ lại giá trị user vừa nhập |

---

## 8. Business Rules

| ID | Rule |
|----|------|
| BR-U01 | Chỉ **ADMIN**, **MANAGER** được cập nhật căn hộ. |
| BR-U02 | **Không cho đổi `apartment_code`** sau khi tạo (ổn định mã định danh nghiệp vụ). |
| BR-U03 | Khóa cập nhật theo **`apartment_id`** (PK). |
| BR-U04 | `occupancy_type` ∈ {OWNED, RENTED}. |
| BR-U05 | `status` ∈ {ACTIVE, INACTIVE}. |
| BR-U06 | `floor_number` ∈ [0, 200]; `area_m2` ∈ (0, 10000]. |
| BR-U07 | Cập nhật **không** tự đổi quan hệ cư dân (module gán cư dân tách). |
| BR-U08 | STAFF chỉ xem list (nếu được); không edit. |

---

## 9. Validation Rules (Update)

| Field | Required | Rule | Message |
|-------|----------|------|---------|
| `apartmentId` | Yes | Integer > 0, tồn tại DB | Không tìm thấy căn hộ. / ID không hợp lệ. |
| `apartmentCode` | — | **Không nhận sửa** (bỏ qua input nếu client cố gửi) | — |
| `building` | Yes | trim, 1–50 ký tự | Vui lòng nhập tòa nhà. / tối đa 50 ký tự |
| `floorNumber` | Yes | Integer 0–200 | Tầng phải là số nguyên từ 0 đến 200. |
| `areaM2` | Yes | Decimal 0.01–10000, scale 2 | Diện tích phải là số lớn hơn 0 (tối đa 10.000 m²). |
| `occupancyType` | Yes | OWNED \| RENTED | Loại hình sử dụng không hợp lệ… |
| `status` | Yes* | ACTIVE \| INACTIVE (*default ACTIVE nếu rỗng) | Trạng thái không hợp lệ… |
| `notes` | No | ≤ 500 | Ghi chú tối đa 500 ký tự. |

**Không áp dụng** duplicate check mã căn khi update (mã không đổi).

---

## 10. Error / Success Messages

| Code | Message |
|------|---------|
| S-U01 | Cập nhật căn hộ thành công. |
| E-U01 | Bạn không có quyền cập nhật căn hộ. |
| E-U02 | ID căn hộ không hợp lệ. |
| E-U03 | Không tìm thấy căn hộ. |
| E-U04 | (các message validate field – giống create) |
| E-U05 | Không thể cập nhật căn hộ. Vui lòng thử lại. |

---

## 11. UI tóm tắt

- Tái sử dụng `form.jsp` với `formMode = edit`.
- Tiêu đề: **Cập nhật căn hộ**.
- Hidden: `action=update`, `apartmentId`.
- `apartmentCode`: `readonly` / disabled + hiển thị.
- List: cột **Thao tác** → nút **Sửa** (chỉ `canCreate` = ADMIN/MANAGER).

---

## 12. Test Cases (UC-APT-02)

### Positive

| Test ID | Scenario | Steps | Expected | Priority |
|---------|----------|-------|----------|----------|
| TC-APT-U-001 | Cập nhật thành công full field | Login manager → list → Sửa 1 căn → đổi tòa/tầng/DT/notes → Lưu | Flash success; list/DB đổi đúng; mã căn giữ nguyên | P0 |
| TC-APT-U-002 | Đổi status ACTIVE→INACTIVE | Sửa → chọn INACTIVE → Lưu | status = INACTIVE trên list | P1 |
| TC-APT-U-003 | Đổi occupancy OWNED→RENTED | Sửa → RENTED → Lưu | occupancy cập nhật | P1 |
| TC-APT-U-004 | Chỉ sửa notes | Đổi notes → Lưu | notes mới; field khác giữ | P1 |
| TC-APT-U-005 | Admin cập nhật được | Login admin → Sửa → Lưu | Thành công | P1 |
| TC-APT-U-006 | Hủy không lưu | Sửa → đổi field → Hủy | DB không đổi | P1 |
| TC-APT-U-007 | Mã căn không đổi được | Form edit: mã readonly; cố tình không submit code mới | DB `apartment_code` giữ nguyên | P0 |

### Negative

| Test ID | Scenario | Steps | Expected | Priority |
|---------|----------|-------|----------|----------|
| TC-APT-U-101 | id không tồn tại | `/apartment?action=edit&id=999999` | Flash *Không tìm thấy căn hộ.* + list | P0 |
| TC-APT-U-102 | id không hợp lệ | `edit&id=abc` | Lỗi ID + list | P1 |
| TC-APT-U-103 | Thiếu building | Xóa tòa → Lưu | Lỗi validate; không update | P0 |
| TC-APT-U-104 | Floor ngoài biên | Tầng = -1 hoặc 201 | Lỗi tầng | P0 |
| TC-APT-U-105 | Area = 0 | DT = 0 | Lỗi diện tích | P0 |
| TC-APT-U-106 | STAFF không sửa | Login staff → list (nếu có) / URL edit | Không có nút Sửa / bị chặn | P0 |
| TC-APT-U-107 | RESIDENT không sửa | URL edit | Bị chặn | P0 |
| TC-APT-U-108 | Chưa login | URL edit | Redirect login | P0 |
| TC-APT-U-109 | Notes > 500 | Notes 501 ký tự | Lỗi notes | P2 |
| TC-APT-U-110 | F5 sau success | Update OK → F5 list | Không update lặp | P1 |

### Smoke update (3 phút)

1. TC-APT-U-001  
2. TC-APT-U-103  
3. TC-APT-U-101  
4. TC-APT-U-106  

---

## 13. Traceability (Implementation)

| Layer | Artifact |
|-------|----------|
| Đặc tả | `docs/uc-apt-02-cap-nhat-can-ho.md` |
| DAO | `ApartmentDAO.update`, `findById` |
| Controller | `handleEditForm`, `handleUpdate` |
| View | `form.jsp` (mode edit), `list.jsp` (nút Sửa) |

---

## 14. Out of Scope

- Đổi mã căn hộ  
- Xóa cứng căn hộ  
- Gán/gỡ cư dân  
- Lịch sử audit chi tiết (ai sửa lúc nào) ngoài `updated_at`  
