# ApartmentManagement — nhánh `tv4`

> **Nhánh dev (TV4 — yêu cầu cư dân).** Bản đủ module để chạy / demo / nộp bài: xem nhánh **[`main`](https://github.com/tuanhung1710/ApartmentAssignment/tree/main)**.

Phạm vi TV4: request cư dân (gửi / list / chi tiết / hủy PENDING) + chat/comment (HTTP và/hoặc WebSocket tùy commit nhánh).

## Setup (theo `main`)

Trên nhánh này có thể thiếu module khác. **Khuyến nghị clone/checkout `main`** rồi:

1. Chạy `database/schema.sql` → `database/seed.sql`
2. Sửa `src/java/apartmentmanagement/dal/DBContext.java` (password SQL máy bạn)
3. NetBeans + Tomcat 10.x · Annotation Processing (Lombok) · Run → `/ApartmentManagement`

Chi tiết URL, tài khoản demo, phân công: **[README trên `main`](https://github.com/tuanhung1710/ApartmentAssignment/blob/main/README.md)**.

## Phạm vi code trên nhánh này

```
controller/request/       RequestController, RequestCommentController (nếu có)
dao/                      RequestDAO, RequestHistoryDAO, …
websocket/                (nếu có trên nhánh)
web/WEB-INF/views/request/
```
**Lưu ý:** path docs/database nằm ở **root repo** (\`database/\`, \`docs/\`), không phải \`../database\`.

**Quy ước:** chỉ sửa package/view module mình. Shell (`layout`, filter, `DBContext`) merge cẩn thận với `main`.
Cắm trang qua `contentPage` + forward `common/layout.jsp` — xem `coding-standards.md` trên `main`.

## Tài khoản demo (sau seed trên `main`)

Password: `123456` — `admin` · `manager` · `staff` · `resident1` · …

## Nộp bài

Dùng **`main`** (link GitHub + zip source + `database/schema.sql` + `seed.sql`).
Không cần export `.bak` từ SQL Server.
