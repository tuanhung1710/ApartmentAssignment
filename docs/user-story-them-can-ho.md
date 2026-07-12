# User Story – UC: Thêm căn hộ

| Mục | Nội dung |
|-----|----------|
| **ID** | US-APT-01 |
| **Epic** | Quản lý căn hộ (TV2) |
| **Use Case** | Thêm căn hộ |
| **Priority** | High |
| **Actor** | Manager, Admin |
| **Module** | Apartment Management |
| **URL** | `GET/POST /apartment?action=create` |

---

## 1. User Story

**Là** Quản lý tòa nhà (Manager) hoặc Quản trị viên (Admin),  
**tôi muốn** thêm một căn hộ mới vào hệ thống với mã căn, tòa nhà, tầng, diện tích và trạng thái,  
**để** quản lý danh mục căn hộ, gán cư dân và tính phí dịch vụ sau này.

### Giá trị nghiệp vụ
- Chuẩn hóa dữ liệu căn hộ trước khi gán cư dân / tạo phí tháng.
- Tránh trùng mã căn hộ trong cùng hệ thống.
- Chỉ người có thẩm quyền mới được tạo căn hộ.

---

## 2. Preconditions

1. Người dùng đã **đăng nhập** thành công.
2. Vai trò người dùng là **ADMIN** hoặc **MANAGER**.
3. Database SQL Server đang hoạt động, bảng `apartments` đã được tạo.
4. Hệ thống sẵn sàng nhận request (Tomcat đang chạy).

---

## 3. Main Flow (Happy Path)

| Bước | Actor | Hệ thống |
|------|--------|----------|
| 1 | Manager/Admin chọn menu **Căn hộ** | Hiển thị danh sách căn hộ (`action=list`) |
| 2 | Bấm nút **Thêm căn hộ** | Mở form tạo mới (`GET /apartment?action=create`) |
| 3 | Nhập: Mã căn, Tòa nhà, Tầng, Diện tích, Loại hình, Trạng thái, Ghi chú | Validate client-side (required, min, pattern) |
| 4 | Bấm **Lưu** | `POST /apartment` với `action=create` |
| 5 | — | Kiểm tra role ADMIN/MANAGER |
| 6 | — | Validate server-side toàn bộ field |
| 7 | — | Kiểm tra `apartment_code` chưa tồn tại |
| 8 | — | Insert bản ghi vào bảng `apartments` (`status` mặc định ACTIVE nếu để trống) |
| 9 | — | Flash success: *Thêm căn hộ thành công.* |
| 10 | — | Redirect về danh sách căn hộ (`action=list`) |
| 11 | Xem thông báo thành công + căn mới trong list | — |

---

## 4. Alternative Flow

### AF-01: Hủy thao tác
- **Tại bước 3–4**, user bấm **Hủy**.
- Hệ thống quay về danh sách căn hộ, **không** lưu dữ liệu.

### AF-02: Giữ lại dữ liệu khi lỗi validate
- **Tại bước 6**, dữ liệu không hợp lệ.
- Hệ thống hiển thị form lại kèm:
  - Thông báo lỗi cụ thể
  - Giá trị user đã nhập (không mất form)
- User sửa và submit lại → quay Main Flow bước 4.

### AF-03: Chọn status mặc định
- User không chọn **Trạng thái** (hoặc để mặc định).
- Hệ thống gán `ACTIVE`.

### AF-04: Ghi chú để trống
- User không nhập **Ghi chú**.
- Hệ thống lưu `notes = null` / rỗng — vẫn hợp lệ.

---

## 5. Exception Flow

### EF-01: Chưa đăng nhập
- User truy cập `/apartment?action=create` khi chưa login.
- **AuthFilter** redirect → `/auth?action=login`.

### EF-02: Không đủ quyền (STAFF / RESIDENT)
- User đã login nhưng role không phải ADMIN/MANAGER.
- Hệ thống forward **403** hoặc flash error + redirect dashboard/list (theo rule controller).
- Message: *Bạn không có quyền thêm căn hộ.*

### EF-03: Trùng mã căn hộ
- `apartment_code` đã tồn tại trong DB.
- Flash/error: *Mã căn hộ đã tồn tại.*
- Giữ form + dữ liệu đã nhập.

### EF-04: Lỗi database / insert thất bại
- JDBC exception, connection null, executeUpdate = 0.
- Error: *Không thể thêm căn hộ. Vui lòng thử lại.*
- Giữ form + dữ liệu đã nhập.
- Log lỗi ra console server.

### EF-05: Method / action không hợp lệ
- POST thiếu `action` hoặc action lạ.
- Trả `400` / `404` theo chuẩn controller hiện tại.

---

## 6. Postconditions

### Thành công
1. Có **1 bản ghi mới** trong bảng `apartments`.
2. `apartment_code` unique trong hệ thống.
3. `created_at` / `updated_at` được DB gán (nếu có default).
4. User thấy flash success và danh sách cập nhật.
5. Audit nghiệp vụ: căn hộ sẵn sàng để gán cư dân / tạo phí (module khác).

### Thất bại
1. **Không** có bản ghi mới.
2. User vẫn ở form tạo (hoặc bị chặn 403/login).
3. Dữ liệu form được giữ (trừ case chưa login / 403).

---

## 7. Acceptance Criteria

| # | Tiêu chí | Pass |
|---|----------|------|
| AC-01 | ADMIN/MANAGER mở được form **Thêm căn hộ** | ☐ |
| AC-02 | STAFF/RESIDENT **không** thêm được căn hộ | ☐ |
| AC-03 | Chưa login bị redirect login | ☐ |
| AC-04 | Submit đủ field hợp lệ → insert OK + redirect list + flash success | ☐ |
| AC-05 | Bỏ trống mã căn / tòa nhà / tầng / diện tích → báo lỗi, không insert | ☐ |
| AC-06 | Diện tích ≤ 0 hoặc không phải số → báo lỗi | ☐ |
| AC-07 | Tầng < 0 hoặc không phải số nguyên → báo lỗi | ☐ |
| AC-08 | `occupancyType` không thuộc OWNED/RENTED → báo lỗi | ☐ |
| AC-09 | `status` không thuộc ACTIVE/INACTIVE → báo lỗi (nếu gửi) | ☐ |
| AC-10 | Trùng `apartmentCode` → báo lỗi, không insert | ☐ |
| AC-11 | Mã căn trim khoảng trắng 2 đầu; không chấp nhận rỗng sau trim | ☐ |
| AC-12 | Ghi chú dài quá giới hạn → báo lỗi | ☐ |
| AC-13 | Lỗi DB → message thân thiện, không crash trang trắng | ☐ |
| AC-14 | Form lỗi vẫn giữ giá trị đã nhập | ☐ |
| AC-15 | Nút Hủy về list, không lưu | ☐ |

---

## 8. Business Rules

| ID | Rule |
|----|------|
| BR-01 | Chỉ **ADMIN**, **MANAGER** được **thêm** căn hộ. |
| BR-02 | `apartment_code` **duy nhất** toàn hệ thống (case-insensitive theo chuẩn DB collation; app so sánh sau trim). |
| BR-03 | Trạng thái mặc định khi tạo: **ACTIVE**. |
| BR-04 | Loại hình sử dụng (`occupancy_type`) chỉ: **OWNED** (sở hữu) hoặc **RENTED** (thuê). |
| BR-05 | Trạng thái căn (`status`) chỉ: **ACTIVE** / **INACTIVE**. |
| BR-06 | Không cho tạo căn với diện tích ≤ 0. |
| BR-07 | Tầng (`floor_number`) là số nguyên ≥ 0 (tầng trệt = 0 được phép). |
| BR-08 | STAFF chỉ xem/hỗ trợ; RESIDENT chỉ xem căn của mình — **không create**. |
| BR-09 | Mã căn sau khi tạo **không** đổi trong UC này (UC sửa là story khác). |
| BR-10 | Thêm căn hộ **không** tự gán cư dân (UC gán cư dân tách riêng). |

---

## 9. Validation Rules

| Field | Bắt buộc | Kiểu / Format | Min | Max | Rule |
|-------|----------|---------------|-----|-----|------|
| `apartmentCode` | Yes | String, trim | 1 | 20 | Chỉ chữ, số, `-`, `_`; không khoảng trắng giữa; unique |
| `building` | Yes | String, trim | 1 | 50 | Không rỗng |
| `floorNumber` | Yes | Integer | 0 | 200 | Số nguyên hợp lệ |
| `areaM2` | Yes | Decimal | 0.01 | 10000 | > 0; tối đa 2 chữ số thập phân |
| `occupancyType` | Yes | Enum | — | — | `OWNED` \| `RENTED` |
| `status` | No | Enum | — | — | `ACTIVE` \| `INACTIVE`; default `ACTIVE` |
| `notes` | No | String, trim | 0 | 500 | Tùy chọn |

### Regex gợi ý
- `apartmentCode`: `^[A-Za-z0-9][A-Za-z0-9_-]{0,19}$`
- `areaM2`: số thập phân dương, tối đa 2 số lẻ

---

## 10. Error Messages

| Code | Điều kiện | Message (VI) |
|------|-----------|--------------|
| E001 | Thiếu quyền | Bạn không có quyền thêm căn hộ. |
| E002 | Chưa login | (Redirect login – không cần message form) |
| E003 | Thiếu mã căn | Vui lòng nhập mã căn hộ. |
| E004 | Mã căn sai format | Mã căn hộ chỉ gồm chữ, số, gạch ngang hoặc gạch dưới (tối đa 20 ký tự). |
| E005 | Trùng mã căn | Mã căn hộ đã tồn tại. |
| E006 | Thiếu tòa nhà | Vui lòng nhập tòa nhà. |
| E007 | Tòa nhà quá dài | Tên tòa nhà tối đa 50 ký tự. |
| E008 | Thiếu / sai tầng | Tầng phải là số nguyên từ 0 đến 200. |
| E009 | Thiếu / sai diện tích | Diện tích phải là số lớn hơn 0 (tối đa 10.000 m²). |
| E010 | Sai loại hình | Loại hình sử dụng không hợp lệ (OWNED / RENTED). |
| E011 | Sai trạng thái | Trạng thái không hợp lệ (ACTIVE / INACTIVE). |
| E012 | Ghi chú quá dài | Ghi chú tối đa 500 ký tự. |
| E013 | Insert fail / DB | Không thể thêm căn hộ. Vui lòng thử lại. |
| S001 | Thành công | Thêm căn hộ thành công. |

---

## 11. UI / UX Notes

- Form nằm trong layout chung (sidebar + header + flash).
- Nút primary: **Lưu**; secondary: **Hủy**.
- Field bắt buộc đánh dấu `*`.
- Hiển thị lỗi field-level (nếu có map) hoặc flash/alert tổng.
- Select cho `occupancyType`, `status`.
- Responsive Bootstrap 5.

---

## 12. Data Mapping

| UI Field | Form name | Model field | DB column |
|----------|-----------|-------------|-----------|
| Mã căn hộ | `apartmentCode` | `apartmentCode` | `apartment_code` |
| Tòa nhà | `building` | `building` | `building` |
| Tầng | `floorNumber` | `floorNumber` | `floor_number` |
| Diện tích (m²) | `areaM2` | `areaM2` | `area_m2` |
| Loại hình | `occupancyType` | `occupancyType` | `occupancy_type` |
| Trạng thái | `status` | `status` | `status` |
| Ghi chú | `notes` | `notes` | `notes` |

### DDL tham chiếu (SQL Server)

```sql
CREATE TABLE apartments (
    apartment_id    INT IDENTITY(1,1) PRIMARY KEY,
    apartment_code  NVARCHAR(20)  NOT NULL UNIQUE,
    building        NVARCHAR(50)  NOT NULL,
    floor_number    INT           NOT NULL,
    area_m2         DECIMAL(10,2) NOT NULL,
    occupancy_type  NVARCHAR(20)  NOT NULL, -- OWNED | RENTED
    status          NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | INACTIVE
    notes           NVARCHAR(500) NULL,
    created_at      DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME()
);
```

---

## 13. Out of Scope (UC này không làm)

- Sửa / xóa / khóa căn hộ
- Gán chủ hộ / người thuê
- Import Excel
- Phân trang/filter nâng cao (có thể có list tối thiểu để quay về sau create)
- Upload ảnh căn hộ

---

## 14. Traceability (Implementation)

| Layer | Artifact |
|-------|----------|
| BA | `docs/user-story-them-can-ho.md` |
| UI Form | `docs/ui-form-them-can-ho.md` |
| Validation Rules | `docs/validation-rules-them-can-ho.md` |
| Test Cases | `docs/test-cases-them-can-ho.md` |
| Model | `Apartment.java` |
| DAO | `ApartmentDAO.java` |
| Controller | `ApartmentController.java` |
| View | `web/WEB-INF/views/apartment/form.jsp`, `list.jsp` |
| Auth | `AuthFilter` path `/apartment` + check role create trong controller |
| SQL | `database/apartments.sql` (tạo bảng nếu chưa có) |
