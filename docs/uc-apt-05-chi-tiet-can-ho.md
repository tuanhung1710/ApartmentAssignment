# UC-APT-05 – Chi tiết căn hộ (Thiết kế màn hình)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-05 |
| **URL** | `GET /apartment?action=detail&id={id}` |
| **View** | `web/WEB-INF/views/apartment/detail.jsp` |
| **Standards** | `coding-standards.md` — Jakarta, 1 controller, JSP WEB-INF, DAO extends DBContext |

---

## 1. Mục tiêu

Xem **toàn bộ thông tin một căn hộ** trên một màn: thông tin cơ bản, chủ / người thuê, thành viên hộ, lịch sử thay đổi, và các nút thao tác **theo role**.

---

## 2. Thông tin cơ bản

| Field | Nguồn |
|-------|--------|
| Mã căn | `apartment_code` |
| Tòa nhà | `building` |
| Tầng | `floor_number` |
| Diện tích | `area_m2` |
| Loại hình | `occupancy_type` (OWNED/RENTED) |
| Trạng thái | `status` (ACTIVE/INACTIVE) |
| Ghi chú | `notes` |
| Ngày tạo | `created_at` |
| Cập nhật lần cuối | `updated_at` |

UI: card “Thông tin căn hộ” dạng description list / grid.

---

## 3. Chủ sở hữu (Owner)

| Nguồn | `apartment_residents` WHERE `role_in_apartment = 'OWNER'` (ưu tiên `is_current = 1`) |
|-------|-------------------------------------------------------------------------------------|
| Hiển thị | Họ tên, username, SĐT/email (nếu join users), từ ngày `start_date` |
| Empty | “Chưa gán chủ sở hữu” |

---

## 4. Người thuê (Tenant)

| Nguồn | `apartment_residents` WHERE `role_in_apartment IN ('TENANT_REP','TENANT')` |
|-------|---------------------------------------------------------------------------|
| Hiển thị | Tương tự owner |
| Empty | “Chưa gán người thuê” |

> OWNED có thể chỉ có OWNER; RENTED thường có TENANT_REP. UI luôn hiện cả 2 khối.

---

## 5. Thành viên hộ (Household members)

| Nguồn | `household_members` WHERE `apartment_id = ?` |
|-------|-----------------------------------------------|
| Cột | Họ tên, quan hệ, SĐT, CCCD/CMND, ngày sinh, active |
| Empty | “Chưa có thành viên hộ” |

---

## 6. Lịch sử

| Loại | Nguồn MVP |
|------|-----------|
| Tạo căn | Dòng hệ thống từ `created_at` |
| Cập nhật gần nhất | `updated_at` |
| Đổi trạng thái / thao tác | Bảng `apartment_history` (nếu có) — action, note, actor, time |
| Empty history table | Vẫn hiện tối thiểu 1–2 dòng create/update từ apartment |

---

## 7. Các nút thao tác

| Nút | Điều kiện hiển thị (role + state) |
|-----|-----------------------------------|
| **Về danh sách** | Mọi role được xem detail |
| **Sửa** | ADMIN, MANAGER |
| **Vô hiệu hóa** | ADMIN/MANAGER + status ACTIVE |
| **Kích hoạt** | ADMIN/MANAGER + status INACTIVE |
| **Xóa** | ADMIN/MANAGER + status INACTIVE |
| **Thêm / Gán cư dân** | ADMIN/MANAGER (link placeholder nếu module gán chưa xong) |

STAFF: **chỉ xem**, không manage.  
RESIDENT: chỉ xem căn **mình được gán** (nếu không thuộc căn → 403).

---

## 8. Quyền theo Role

| Role | Xem detail | Manage (sửa/disable/xóa) | Ghi chú |
|------|------------|---------------------------|---------|
| ADMIN | ✅ mọi căn | ✅ | Full |
| MANAGER | ✅ mọi căn | ✅ | Full |
| STAFF | ✅ mọi căn | ❌ | Hỗ trợ vận hành |
| RESIDENT | ✅ căn được gán (current) | ❌ | `action=detail` check membership |
| Chưa login | ❌ | ❌ | Redirect login |
| id không tồn tại | — | — | Flash + redirect list/dashboard |

---

## 9. Layout đề xuất

```text
[ Header: Căn A-1201 · ACTIVE ]     [Sửa] [Vô hiệu] [Về list]

[ Card: Thông tin cơ bản ........ ]

[ Col 6: Chủ sở hữu ] [ Col 6: Người thuê ]

[ Card: Thành viên hộ (table) ]

[ Card: Lịch sử (timeline/table) ]
```

Empty state từng block: text muted + icon.

---

## 10. Validation / Exception

| Case | Xử lý |
|------|--------|
| id thiếu / không số | Flash lỗi → list |
| Không tìm thấy căn | Flash → list |
| RESIDENT không thuộc căn | 403 |
| Bảng residents/members chưa có | List rỗng, không crash |

---

## 11. Traceability

| Layer | File |
|-------|------|
| Design | `docs/uc-apt-05-chi-tiet-can-ho.md` |
| SQL phụ | `database/apartment-detail-tables.sql` |
| DAO | `ApartmentResidentDAO`, `HouseholdMemberDAO`, `ApartmentHistoryDAO` + `ApartmentDAO.findById` |
| Controller | `handleDetail` · `action=detail` |
| View | `detail.jsp` · link từ `list.jsp` |

---

## 12. Ảnh hưởng UC trước

| UC | Ảnh hưởng |
|----|-----------|
| 01–04 | **Không xóa**; list thêm link **Chi tiết** |
| Create/Update/Disable | Nút trên detail gọi lại action đã có |
