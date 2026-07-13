# UC-APT-06 – Gán chủ sở hữu (Đặc tả Use Case)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-06 |
| **Tên** | Gán / Đổi chủ sở hữu căn hộ |
| **Actor** | Admin, Manager |
| **URL** | `GET /apartment?action=assign-owner&id={apartmentId}` · `POST action=assign-owner` |
| **Bảng** | `apartment_residents` (`role_in_apartment = 'OWNER'`) |
| **Standards** | `coding-standards.md` |

---

## 1. User Story (tóm)

**Là** Admin/Manager, **tôi muốn** gán (hoặc đổi) **một** chủ sở hữu hiện tại cho căn hộ, **để** biết ai là owner hợp lệ phục vụ quản lý và màn chi tiết.

---

## 2. Preconditions

1. User đã **đăng nhập**.
2. Role **ADMIN** hoặc **MANAGER**.
3. Căn hộ **tồn tại** (`apartment_id` hợp lệ).
4. Bảng `apartment_residents` + `users` đã có (chạy SQL detail + users).
5. Có ít nhất 1 user có thể làm owner (MVP: user `is_active = 1`, ưu tiên role RESIDENT; cho phép chọn user active khác nếu cần demo).

---

## 3. Business Rules

| ID | Rule |
|----|------|
| **BR-O01** | Một căn hộ có **tối đa 1 Owner hiện tại** (`role_in_apartment = 'OWNER'` AND `is_current = 1`). |
| **BR-O02** | Có thể có **nhiều Owner lịch sử** (`is_current = 0`) — không xóa cứng khi đổi owner. |
| **BR-O03** | **Đổi Owner:** đóng Owner cũ rồi gán Owner mới trong cùng thao tác (atomic về logic app: end old → insert new). |
| **BR-O04** | **Xử lý Owner cũ:** `is_current = 0`, `end_date = ngày hiệu lực đổi` (thường = hôm nay hoặc `start_date` owner mới). **Không** xóa row. |
| **BR-O05** | Owner mới: `is_current = 1`, `start_date` bắt buộc (mặc định hôm nay), `end_date = null`. |
| **BR-O06** | Chỉ ADMIN/MANAGER được gán/đổi. |
| **BR-O07** | User được gán phải **tồn tại** và **`is_active = 1`**. |
| **BR-O08** | Không gán trùng: nếu user đã là Owner hiện tại của căn → báo “đã là chủ hiện tại”. |
| **BR-O09** | Căn **INACTIVE**: vẫn cho gán (MVP) nhưng nên cảnh báo; hard rule: căn phải tồn tại. |
| **BR-O10** | Gán owner **không** tự đổi `occupancy_type` căn (OWNED/RENTED giữ nguyên — module khác). |
| **BR-O11** | Ghi **lịch sử** căn (history): action `ASSIGN_OWNER` / `CHANGE_OWNER`. |

### Một căn có bao nhiêu Owner?

| Loại | Số lượng |
|------|----------|
| Owner **hiện tại** (`is_current = 1`) | **0 hoặc 1** |
| Owner **lịch sử** (`is_current = 0`) | 0..N |

### Đổi Owner & Owner cũ

```text
Trước:  UserA  OWNER  is_current=1  start=...  end=null
Thao tác: gán UserB
Sau:
  UserA  OWNER  is_current=0  end_date=hôm nay   ← đóng, giữ lịch sử
  UserB  OWNER  is_current=1  start_date=hôm nay end=null  ← owner mới
```

---

## 4. Validation

| Field | Rule | Message |
|-------|------|---------|
| `apartmentId` | required, >0, tồn tại | ID căn hộ không hợp lệ / Không tìm thấy căn hộ |
| `userId` | required, >0, user tồn tại, is_active | Vui lòng chọn chủ sở hữu / User không hợp lệ hoặc đã khóa |
| `startDate` | optional; default hôm nay; format yyyy-MM-dd | Ngày bắt đầu không hợp lệ |
| Trùng owner hiện tại | userId ≠ current owner userId | User này đã là chủ sở hữu hiện tại của căn |
| Quyền | ADMIN/MANAGER | Bạn không có quyền gán chủ sở hữu |

---

## 5. Main Flow

1. Từ **Chi tiết căn hộ** bấm **Gán / Đổi chủ sở hữu**.  
2. GET form: hiện căn + owner hiện tại (nếu có) + select user.  
3. Chọn user (+ ngày bắt đầu tùy chọn) → **Lưu**.  
4. Server validate → nếu có owner hiện tại khác user → **end old owner**.  
5. Insert owner mới `OWNER` + `is_current=1`.  
6. Ghi history + flash success → redirect **detail**.

---

## 6. Alternative / Exception

| Case | Xử lý |
|------|--------|
| Chưa có owner → gán lần đầu | Chỉ insert mới (không end) |
| Đổi owner | End cũ + insert mới |
| Chọn đúng owner hiện tại | Lỗi validate, không ghi DB |
| STAFF/RESIDENT | Chặn |
| User bị khóa | Chặn |
| DB lỗi | Flash/error, giữ form |

---

## 7. Acceptance Criteria

| # | Tiêu chí |
|---|----------|
| AC-01 | ADMIN/MANAGER mở form gán owner từ detail |
| AC-02 | Gán owner lần đầu thành công → detail hiện 1 owner |
| AC-03 | Đổi owner → owner cũ `is_current=0` + có end_date; owner mới current |
| AC-04 | Một căn không có 2 owner `is_current=1` cùng lúc |
| AC-05 | Chọn lại đúng owner hiện tại → báo lỗi, không nhân bản |
| AC-06 | User không active / không chọn → lỗi |
| AC-07 | STAFF không gán được |
| AC-08 | Sau OK redirect detail + flash success |
| AC-09 | Lịch sử căn có bản ghi ASSIGN/CHANGE_OWNER (nếu bảng history có) |

---

## 8. Messages

| Code | Message |
|------|---------|
| S-O01 | Gán chủ sở hữu thành công. |
| S-O02 | Đổi chủ sở hữu thành công. |
| E-O01 | Bạn không có quyền gán chủ sở hữu. |
| E-O02 | Vui lòng chọn chủ sở hữu. |
| E-O03 | User không hợp lệ hoặc đã bị khóa. |
| E-O04 | User này đã là chủ sở hữu hiện tại của căn. |
| E-O05 | Không tìm thấy căn hộ. |
| E-O06 | Không thể gán chủ sở hữu. Vui lòng thử lại. |

---

## 9. Traceability

| Layer | Artifact |
|-------|----------|
| BA | `docs/uc-apt-06-gan-chu-so-huu.md` |
| DAO | `ApartmentResidentDAO` endCurrentOwner / insertOwner / findCurrentOwner |
| UserDAO | `findActiveUsers` (dropdown) |
| Controller | `handleAssignOwnerForm` / `handleAssignOwner` |
| View | `assign-owner.jsp` + link trên `detail.jsp` |

---

## 10. Ảnh hưởng UC trước

| UC | Ảnh hưởng |
|----|-----------|
| 01–05 | **Không phá**; detail hiện owner sau khi gán |
| Delete căn | Vẫn check cư dân hiện tại (gồm owner current) |
