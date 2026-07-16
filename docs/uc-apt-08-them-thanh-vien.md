# UC-APT-08 – Thêm thành viên sinh sống (Đặc tả)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-08 |
| **Tên** | Thêm thành viên hộ / người sinh sống trong căn |
| **Actor** | Admin, Manager |
| **URL** | `GET/POST /apartment?action=add-member` |
| **Bảng** | `household_members` |
| **Standards** | `coding-standards.md` |

---

## 1. User Story

**Là** Admin/Manager, **tôi muốn** thêm thành viên sinh sống trong căn (họ tên, **vai trò** Chủ hộ/Thành viên, CCCD, SĐT, ngày sinh), **để** quản lý nhân khẩu gắn căn hộ.

---

## 2. Preconditions

1. Đã đăng nhập; role **ADMIN** hoặc **MANAGER**.
2. Căn hộ tồn tại.
3. Bảng `household_members` đã tạo (`apartment-detail-tables.sql`).

---

## 3. Business Rules

| ID | Rule |
|----|------|
| **BR-M01** | Chỉ ADMIN/MANAGER được thêm thành viên. |
| **BR-M02** | Thành viên gắn **1** `apartment_id` (không dùng `users` — nhập tay nhân khẩu). |
| **BR-M03** | **Vai trò** (cột DB `relationship`): bắt buộc; **chỉ 2 giá trị**: `Chủ hộ` \| `Thành viên`. Không dùng quan hệ gia đình (Vợ/Chồng, Con…). |
| **BR-M04** | **CCCD / CMND** (`id_number`): optional nhưng nếu nhập phải đúng format (9–12 chữ số). |
| **BR-M05** | **Phone**: optional; nếu nhập 9–11 chữ số (có thể bắt đầu 0). |
| **BR-M06** | **Date of Birth**: optional; nếu nhập không được **sau hôm nay**; khuyến nghị tuổi ≥ 0. |
| **BR-M07** | **Họ tên** bắt buộc, 2–100 ký tự sau trim. |
| **BR-M08** | Thành viên mới: `is_active = 1` (đang sinh sống). |
| **BR-M09** | Cho phép nhiều thành viên / căn; không giới hạn cứng MVP. |
| **BR-M10** | CCCD trùng trên **cùng căn** (active) → cảnh báo/chặn (MVP: **chặn**). |
| **BR-M11** | Ghi history căn: `ADD_MEMBER`. |
| **BR-M12** | Không xóa owner/tenant khi thêm member — bảng độc lập. |

---

## 4. Validation (field)

| Field | DB / Form | Required | Rule | Message |
|-------|-----------|----------|------|---------|
| apartmentId | hidden | Yes | >0, căn tồn tại | ID/căn không hợp lệ |
| fullName | full_name | Yes | trim, 2–100 ký tự | Vui lòng nhập họ tên / Họ tên 2–100 ký tự |
| relationship (UI: Vai trò) | relationship | Yes | enum: `Chủ hộ` \| `Thành viên` | Vui lòng chọn vai trò / Vai trò chỉ được là Chủ hộ hoặc Thành viên |
| idNumber (CCCD) | id_number | No | nếu có: `^\d{9,12}$` | CCCD/CMND phải gồm 9–12 chữ số |
| phone | phone | No | nếu có: `^0?\d{9,10}$` (9–11 số) | Số điện thoại không hợp lệ |
| dateOfBirth | date_of_birth | No | yyyy-MM-dd; ≤ today | Ngày sinh không hợp lệ / không được ở tương lai |
| Trùng CCCD cùng căn | — | — | không trùng active | CCCD đã tồn tại trên căn này |
| Quyền | — | — | ADMIN/MANAGER | Không có quyền thêm thành viên |

---

## 5. Vai trò (cột `relationship`)

| Giá trị UI / DB | Ghi chú |
|-----------------|---------|
| **Chủ hộ** | Vai trò chủ hộ; gán owner có thể sync 1 dòng này |
| **Thành viên** | Thành viên khác trong hộ |

- UI label: **Vai trò** (không gọi “Quan hệ”).
- MVP: **select cố định 2 option** — không free-text, không quan hệ gia đình.
- Validate server: reject mọi giá trị ngoài 2 option trên.

---

## 6. CCCD / Phone / DOB (tóm)

| Field | Optional | Format |
|-------|----------|--------|
| CCCD | Yes | 9–12 digits |
| Phone | Yes | 9–11 digits, có thể có 0 đầu |
| DOB | Yes | date ≤ today |

---

## 7. Acceptance Criteria

| # | Tiêu chí |
|---|----------|
| AC-01 | Manager mở form từ detail → thêm TV thành công |
| AC-02 | fullName + vai trò (Chủ hộ/Thành viên) bắt buộc |
| AC-03 | CCCD sai format → lỗi |
| AC-04 | Phone sai format → lỗi |
| AC-05 | DOB tương lai → lỗi |
| AC-06 | CCCD trùng trên cùng căn (active) → lỗi |
| AC-07 | Detail hiện thêm 1 dòng thành viên |
| AC-08 | STAFF không thêm được |
| AC-09 | Flash success + redirect detail |
| AC-10 | is_active = true mặc định |

---

## 8. Messages

| Code | Message |
|------|---------|
| S-M01 | Thêm thành viên thành công. |
| E-M01 | Bạn không có quyền thêm thành viên. |
| E-M02 | Vui lòng nhập họ tên. |
| E-M03 | Vui lòng chọn vai trò (Chủ hộ hoặc Thành viên). |
| E-M04 | CCCD/CMND phải gồm 9–12 chữ số. |
| E-M05 | Số điện thoại không hợp lệ. |
| E-M06 | Ngày sinh không hợp lệ. |
| E-M07 | Ngày sinh không được ở tương lai. |
| E-M08 | CCCD đã tồn tại trên căn này. |
| E-M09 | Không thể thêm thành viên. Vui lòng thử lại. |

---

## 9. Traceability

| Layer | File |
|-------|------|
| BA | `docs/uc-apt-08-them-thanh-vien.md` |
| DAO | `HouseholdMemberDAO.insert`, `existsActiveIdNumber` |
| Controller | `add-member` GET/POST |
| View | `add-member.jsp` + nút trên detail |
