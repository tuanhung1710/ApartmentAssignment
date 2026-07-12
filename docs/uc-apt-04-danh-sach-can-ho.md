# UC-APT-04 – Danh sách căn hộ (Thiết kế màn hình)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-04 |
| **URL** | `GET /apartment?action=list` |
| **Actor** | ADMIN, MANAGER, STAFF (xem) · Manage actions: ADMIN/MANAGER |
| **View** | `web/WEB-INF/views/apartment/list.jsp` |

---

## 1. Mục tiêu

Cho phép tra cứu, lọc, sắp xếp, phân trang danh mục căn hộ; thực hiện thao tác Thêm / Sửa / Vô hiệu / Kích hoạt / Xóa (đúng quyền).

---

## 2. Bộ lọc (Filters)

| Control | Param | Kiểu | Giá trị | Mặc định |
|---------|-------|------|---------|----------|
| Trạng thái | `status` | Select | `` (Tất cả) \| ACTIVE \| INACTIVE | Tất cả |
| Loại hình | `occupancyType` | Select | `` \| OWNED \| RENTED | Tất cả |
| Tòa nhà | `building` | Text | Chuỗi chứa (LIKE) | rỗng |
| Nút | — | Submit | **Lọc** | — |
| Nút | — | Link | **Xóa lọc** → list sạch | — |

Filter kết hợp **AND** với từ khóa tìm kiếm.

---

## 3. Tìm kiếm (Search)

| Mục | Chi tiết |
|-----|----------|
| Param | `keyword` |
| Placeholder | `Mã căn, tòa nhà, ghi chú...` |
| Phạm vi | `apartment_code`, `building`, `notes` (LIKE %keyword%) |
| Nút | **Tìm** (cùng form filter) |

---

## 4. Sort

| Cột sortable | Param `sort` | Mặc định direction |
|--------------|--------------|--------------------|
| Mã căn | `code` | asc |
| Tòa | `building` | asc |
| Tầng | `floor` | asc |
| Diện tích | `area` | asc |
| Loại hình | `occupancy` | asc |
| Trạng thái | `status` | asc |

- Param `dir` = `asc` \| `desc`
- Click header: đổi cột / đảo chiều
- **Whitelist** cột sort trong DAO (chống SQL injection)
- Default sort: `building asc`, rồi `floor`, `code` nếu không truyền sort

---

## 5. Pagination

| Mục | Chi tiết |
|-----|----------|
| Param | `page` (1-based) |
| Page size | `AppConstants.DEFAULT_PAGE_SIZE` = **10** |
| SQL | `OFFSET … FETCH NEXT …` (SQL Server) |
| UI | Prev / số trang / Next |
| Giữ | keyword, filter, sort trên URL phân trang (`c:url` + `c:param`) |
| Hiển thị | `Hiển thị x–y / tổng z căn` |

---

## 6. Hiển thị cột

| # | Cột | Ghi chú |
|---|-----|---------|
| 1 | # | STT theo trang: `(page-1)*size + index` |
| 2 | Mã căn | bold |
| 3 | Tòa | |
| 4 | Tầng | |
| 5 | Diện tích | format số + m² |
| 6 | Loại hình | badge OWNED/RENTED |
| 7 | Trạng thái | badge ACTIVE/INACTIVE |
| 8 | Ghi chú | truncate / — nếu rỗng |
| 9 | Thao tác | chỉ `canManage` |

Hàng INACTIVE: class `table-secondary`.

---

## 7. Action Button

| Button | Ai thấy | Điều kiện |
|--------|---------|-----------|
| **Thêm căn hộ** | ADMIN/MANAGER | Header |
| **Sửa** | ADMIN/MANAGER | Mọi status |
| **Vô hiệu** | ADMIN/MANAGER | status = ACTIVE |
| **Kích hoạt** | ADMIN/MANAGER | status = INACTIVE |
| **Xóa** | ADMIN/MANAGER | status = INACTIVE (+ rule UC-03) |
| STAFF | — | Chỉ xem bảng, không action manage |

---

## 8. Empty State

| Trường hợp | UI |
|------------|-----|
| DB chưa có căn **và** không filter | Icon + “Chưa có căn hộ nào” + link Thêm (nếu canManage) |
| Có filter/keyword nhưng 0 kết quả | “Không tìm thấy căn hộ phù hợp” + nút **Xóa bộ lọc** |

---

## 9. Loading State

| Cách (MVP server-render) | Chi tiết |
|--------------------------|----------|
| Overlay | `#listLoading` ẩn mặc định |
| Khi submit form lọc / click phân trang / sort | JS hiện spinner “Đang tải…” |
| Sau full page load | Overlay tắt (trang mới) |

Không dùng AJAX bắt buộc — đủ cho demo PRJ301.

---

## 10. Layout wireframe

```text
[ Tiêu đề Danh sách căn hộ          ] [ + Thêm căn hộ ]

[ Card bộ lọc ]
  Keyword [............]  Tòa [......]
  Status [v]  Occupancy [v]   [Lọc] [Xóa lọc]

[ Card bảng ]
  table headers (sort links)
  rows...
  empty state nếu cần

[ Hiển thị 1–10 / 25 ]     [<] 1 2 3 [>]

[ Loading overlay khi đang điều hướng ]
```

---

## 11. Query params ví dụ

```text
/apartment?action=list
  &keyword=A-
  &building=Tòa
  &status=ACTIVE
  &occupancyType=OWNED
  &sort=floor
  &dir=desc
  &page=2
```

---

## 12. Ảnh hưởng UC trước

| UC | Ảnh hưởng |
|----|-----------|
| 01 Create | Không đổi — redirect list vẫn `action=list` |
| 02 Update | Không đổi |
| 03 Disable/Delete | Nút action giữ trên list (trong trang kết quả) |
| List cũ | **Nâng cấp** handleList + DAO filter + UI |

---

## 13. Traceability

| Layer | File |
|-------|------|
| Design | `docs/uc-apt-04-danh-sach-can-ho.md` |
| DAO | `findWithFilters`, `countWithFilters` |
| Controller | `handleList` parse filter/sort/page |
| View | `list.jsp` |
