# UC-APT-10 – Danh sách thành viên (Thiết kế màn hình)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-10 |
| **URL** | `GET /apartment?action=members` · Export `action=export-members` |
| **Actor** | ADMIN, MANAGER (full) · STAFF (xem) · RESIDENT (không) |
| **View** | `web/WEB-INF/views/apartment/members.jsp` |
| **Standards** | `coding-standards.md` |

---

## 1. Mục tiêu

Tra cứu toàn bộ **thành viên sinh sống** (`household_members`) theo căn, quan hệ, trạng thái; phân trang; xuất Excel (CSV).

---

## 2. Search

| Param | Phạm vi |
|-------|---------|
| `keyword` | full_name, phone, id_number (CCCD), apartment_code (LIKE) |

---

## 3. Filter

| Control | Param | Giá trị |
|---------|-------|---------|
| Quan hệ (Relationship) | `relationship` | rỗng = tất cả · hoặc 1 giá trị (Chủ hộ, Con…) |
| Trạng thái (Status) | `status` | `` \| `ACTIVE` (`is_active=1`) \| `INACTIVE` (`is_active=0`) |
| Tòa / mã căn (optional) | `building` | LIKE building |

---

## 4. Relationship & Status (cột)

| Cột | Nguồn |
|-----|--------|
| Họ tên | full_name |
| Quan hệ | relationship |
| CCCD | id_number |
| SĐT | phone |
| DOB | date_of_birth |
| Căn | apartment_code + building (JOIN) |
| Status | Active / Off |
| Thao tác | Sửa / Gỡ (canManage) |

---

## 5. Pagination

- `page` 1-based, `pageSize = 10`
- SQL Server `OFFSET/FETCH`
- Giữ keyword + filter trên URL

---

## 6. Export Excel

| Mục | Chi tiết |
|-----|----------|
| Action | `GET export-members` (cùng filter, **không** phân trang — max hợp lý / all filtered) |
| Format MVP | **CSV UTF-8 BOM** (mở được bằng Excel) — không cần Apache POI |
| Permission | ADMIN, MANAGER (STAFF xem list nhưng **không export** — BR) |
| Cột export | memberId, fullName, relationship, phone, idNumber, dob, apartmentCode, building, status |

---

## 7. Permission

| Role | Xem list | Search/Filter | Export | Sửa/Gỡ TV |
|------|----------|---------------|--------|-----------|
| ADMIN | ✅ | ✅ | ✅ | ✅ |
| MANAGER | ✅ | ✅ | ✅ | ✅ |
| STAFF | ✅ | ✅ | ❌ | ❌ |
| RESIDENT | ❌ | ❌ | ❌ | ❌ |

---

## 8. Empty / Loading

- Empty: không có TV / không khớp filter  
- Loading: overlay khi submit filter (giống list căn)

---

## 9. Traceability

| Layer | File |
|-------|------|
| Design | `docs/uc-apt-10-danh-sach-thanh-vien.md` |
| DAO | `findMembersWithFilters`, `countMembersWithFilters` |
| Controller | `handleMembers`, `handleExportMembers` |
| View | `members.jsp` + sidebar link |
