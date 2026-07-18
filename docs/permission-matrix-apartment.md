# Task 14 – Permission Matrix  
## Module Apartment & Resident (UC-APT-01 … 09)

> **UC-APT-10 (Danh sách thành viên global + Export)** đã gỡ: không còn menu/sidebar `Thành viên`, không còn `action=members` / `export-members`.  
> Thành viên hộ quản lý trên **chi tiết căn** (add/edit/remove).

| Ký hiệu | Nghĩa |
|---------|--------|
| ✅ | Được phép |
| ❌ | Không |
| 👁 | Chỉ xem (read-only) |
| ⚠ | Hạn chế (chỉ data của mình) |

---

## 1. Ma trận theo 10 Use Case

| # | Use Case | Admin | Manager | Staff | Resident |
|---|----------|:-----:|:-------:|:-----:|:--------:|
| **01** | Thêm căn hộ | ✅ | ✅ | ❌ | ❌ |
| **02** | Cập nhật căn hộ | ✅ | ✅ | ❌ | ❌ |
| **03** | Vô hiệu hóa / Xóa căn | ✅ | ✅ | ❌ | ❌ |
| **04** | Danh sách căn hộ (search/filter/sort/page) | ✅ | ✅ | 👁 | ❌* |
| **05** | Chi tiết căn hộ | ✅ | ✅ | 👁 | ⚠ chỉ căn được gán |
| **06** | Gán / đổi chủ sở hữu | ✅ | ✅ | ❌ | ❌ |
| **07** | Gán người thuê | ✅ | ✅ | ❌ | ❌ |
| **08** | Thêm thành viên (trên detail căn) | ✅ | ✅ | ❌ | ❌ |
| **09** | Cập nhật / Xóa thành viên (trên detail căn) | ✅ | ✅ | ❌ | ❌ |

\* RESIDENT dùng menu “Căn hộ của tôi” (placeholder / gán current) — không vào quản lý list căn.

---

## 2. Nhóm quyền tóm tắt

| Nhóm | Admin | Manager | Staff | Resident |
|------|:-----:|:-------:|:-----:|:--------:|
| Quản lý master căn (CRUD + status) | ✅ | ✅ | ❌ | ❌ |
| Xem list/detail căn | ✅ | ✅ | ✅ | ⚠ detail của mình |
| Gán Owner / Tenant | ✅ | ✅ | ❌ | ❌ |
| Nhân khẩu (TV add/edit/remove trên detail) | ✅ | ✅ | ❌ | ❌ |
| Audit / History (xem trên detail) | ✅ | ✅ | 👁 | ⚠ nếu xem được detail |

---

## 3. Map code (enforce)

| Check | Chỗ |
|-------|-----|
| Path `/apartment` login + role map | `AuthFilter` |
| Manage (create/update/delete/assign/member write) | `canManage()` = ADMIN \| MANAGER |
| View list căn | `canViewList()` = ADMIN \| MANAGER \| STAFF |
| Detail RESIDENT | `canViewDetail()` + `isCurrentResident` |

---

## 4. Ghi chú nghiệp vụ

1. **Admin ≈ Manager** trên module căn (MVP PRJ301) — Admin thêm quyền module Users/Announcements ở sidebar.  
2. **Staff** = hỗ trợ vận hành: xem, không sửa master/gán.  
3. **Resident** = chỉ dữ liệu gắn mình; không export, không quản lý TV toàn tòa.  
4. Soft delete căn/TV: chỉ manage roles.

---

## 5. Acceptance (Permission)

| # | Tiêu chí |
|---|----------|
| P-01 | Staff vào list căn được, không thấy nút Thêm/Sửa/Gán |
| P-02 | Manager làm đủ 01–09 (trừ hard rule nghiệp vụ) |
| P-03 | Không còn menu/action danh sách thành viên global |
| P-04 | Resident detail căn không gán → 403 |
