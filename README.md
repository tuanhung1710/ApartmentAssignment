# ApartmentManagement — nhánh `Tv5`

> **Nhánh dev (TV5 — admin + xử lý request).** Bản đủ module để chạy / demo / nộp bài: xem nhánh **[`main`](https://github.com/tuanhung1710/ApartmentAssignment/tree/main)**.

Phạm vi TV5: CRUD user + thông báo nội bộ (`/admin`); duyệt / gán / cập nhật tiến độ request (`/request?action=manage`).

## Setup (theo `main`)

Trên nhánh này có thể thiếu module khác. **Khuyến nghị clone/checkout `main`** rồi:

1. Chạy `database/schema.sql` → `database/seed.sql`
2. Sửa `src/java/apartmentmanagement/dal/DBContext.java` (password SQL máy bạn)
3. NetBeans + Tomcat 10.x · Annotation Processing (Lombok) · Run → `/ApartmentManagement`

Chi tiết URL, tài khoản demo, phân công: **[README trên `main`](https://github.com/tuanhung1710/ApartmentAssignment/blob/main/README.md)**.

## Phạm vi code trên nhánh này

```
controller/admin/         AdminController (users, announcements)
controller/request/       manage / approve / reject / assign / progress
dao/                      AnnouncementDAO, UserDAO (admin filters), RequestDAO
web/WEB-INF/views/admin/  listUser, announcement, …
web/WEB-INF/views/request/ manage-list, manage-detail
```
**Lưu ý:** path docs/database nằm ở **root repo** (\`database/\`, \`docs/\`), không phải \`../database\`. Bản ổn định đã port lên \`main\`.

**Quy ước:** chỉ sửa package/view module mình. Shell (`layout`, filter, `DBContext`) merge cẩn thận với `main`.
Cắm trang qua `contentPage` + forward `common/layout.jsp` — xem `coding-standards.md` trên `main`.

## Tài khoản demo (sau seed trên `main`)

Password: `123456` — `admin` · `manager` · `staff` · `resident1` · …

## Nộp bài

Dùng **`main`** (link GitHub + zip source + `database/schema.sql` + `seed.sql`).
Không cần export `.bak` từ SQL Server.
