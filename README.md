# ApartmentManagement — nhánh `tv1`

> **Nhánh dev (TV1 — nền tảng).** Bản đủ module để chạy / demo / nộp bài: xem nhánh **[`main`](https://github.com/tuanhung1710/ApartmentAssignment/tree/main)**.

Phạm vi TV1: auth, profile, forgot OTP, landing, layout/filter, dashboard shell, DB schema/seed, CRUD tòa nhà (`/building`).

## Setup (theo `main`)

Trên nhánh này có thể thiếu module khác. **Khuyến nghị clone/checkout `main`** rồi:

1. Chạy `database/schema.sql` → `database/seed.sql`
2. Sửa `src/java/apartmentmanagement/dal/DBContext.java` (password SQL máy bạn)
3. NetBeans + Tomcat 10.x · Annotation Processing (Lombok) · Run → `/ApartmentManagement`

Chi tiết URL, tài khoản demo, phân công: **[README trên `main`](https://github.com/tuanhung1710/ApartmentAssignment/blob/main/README.md)**.

## Phạm vi code trên nhánh này

```
controller/auth/          AuthenController, DashboardController
controller/admin/         BuildingController
dao/                      UserDAO, BuildingDAO, DashboardStatsDAO, …
filter/                   AuthFilter, EncodingFilter
dal/DBContext.java
web/WEB-INF/views/{common,auth,dashboard,admin/building-*,error}/
database/schema.sql · seed.sql
```

**Quy ước:** chỉ sửa package/view module mình. Shell (`layout`, filter, `DBContext`) merge cẩn thận với `main`.
Cắm trang qua `contentPage` + forward `common/layout.jsp` — xem `coding-standards.md` trên `main`.

## Tài khoản demo (sau seed trên `main`)

Password: `123456` — `admin` · `manager` · `staff` · `resident1` · …

## Nộp bài

Dùng **`main`** (link GitHub + zip source + `database/schema.sql` + `seed.sql`).
Không cần export `.bak` từ SQL Server.
