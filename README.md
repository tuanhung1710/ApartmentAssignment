# ApartmentManagement (PRJ301) — TV1 + TV2 + Fee

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư.

**Stack:** NetBeans Ant · Tomcat 10.1 · JDK 17 · Jakarta Servlet/JSP · SQL Server · JDBC · Lombok · JSTL · Bootstrap 5

**Merge:** platform TV1 (auth, building, dashboard, schema/seed) + module căn hộ TV2 (UC-APT-01…09, occupancy VACANT/N/A, history) + module phí TV3 (categories, assignments, resident my-fees).

---

## 1. Cấu trúc

```
tv2/
├── database/
│   ├── schema.sql              # ★ SOURCE OF TRUTH — drop/create DB + bảng
│   └── seed.sql                # ★ Data demo (users, buildings, 282 căn, owner/tenant, fees…)
├── sql/
│   └── fee-module.sql          # Bổ sung bảng phí (nếu schema cũ thiếu)
├── docs/                       # UC/BR module căn hộ (TV2)
├── coding-standards.md
├── src/java/apartmentmanagement/
│   ├── dal/DBContext.java
│   ├── model/
│   ├── dao/                    # User, Building, Apartment*, Fee*, DashboardStats, …
│   ├── filter/
│   ├── controller/
│   │   ├── auth/               # Authen + Dashboard
│   │   ├── admin/              # Building CRUD
│   │   ├── apartment/          # TV2 full module
│   │   ├── fee/                # TV3 fee module
│   │   └── request/            # TV4–TV5 (placeholder)
│   └── util/
└── web/WEB-INF/views/
    ├── common/                 # layout, sidebar, pagination, flash
    ├── auth/ admin/ apartment/ fee/ dashboard/ error/ request/
```

**Quy tắc tránh đụng code:** mỗi người chỉ sửa package/view module mình.  
TV2–TV5 cắm trang bằng:

```java
request.setAttribute("pageTitle", "...");
request.setAttribute("contentPage", "/WEB-INF/views/.../xxx.jsp");
request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
```

---

## 2. Database (SQL Server)

### Máy mới / reset sạch

1. **`database/schema.sql`** — DROP + CREATE DB `ApartmentManagement` + toàn bộ bảng  
2. **`database/seed.sql`** — data demo  
3. (Nếu cần) **`sql/fee-module.sql`** — bổ sung bảng phí khi DB cũ chưa có

DB lệch / cũ: **chạy lại schema + seed** (reset sạch) thay vì migrate lẻ.

### Connection

`src/java/apartmentmanagement/dal/DBContext.java`

```text
DB_URL  = jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true
DB_USER = sa
DB_PASSWORD = <mật khẩu SQL Server máy bạn>   ← mỗi máy khác nhau, không commit password production
```

Có thể test nhanh bằng `main` trong `DBContext` (in “Connected OK”).

**Lỗi login “Sai tài khoản…” dù nhập đúng demo?**  
Thường là **chưa kết nối được SQL** (sai password `sa` trong `DBContext`, SQL chưa chạy, chưa seed).  
App sẽ báo rõ nếu mất kết nối CSDL. Xem log Tomcat dòng `DBContext getConnection error`.

### Occupancy (TV2 rules)

| status   | occupancy                    |
|----------|------------------------------|
| INACTIVE | **N/A** only                 |
| ACTIVE   | OWNED / RENTED / **VACANT**  |

Create / init-floor mặc định: **INACTIVE + N/A**.

---

## 3. Chạy app

1. SQL Server + schema/seed  
2. NetBeans → Open project `tv2`  
3. Tomcat **10.x** (Jakarta), bật Annotation Processing (Lombok)  
4. Classpath: `mssql-jdbc`, `lombok`, JSTL (thường trong `lib/` và `web/WEB-INF/lib/`)  
5. Run → context **`/ApartmentManagement`**

### URL chính

| URL | Mô tả |
|-----|--------|
| `/` hoặc `/auth?action=home` | Trang chủ public |
| `/auth?action=login` | Đăng nhập |
| `/auth?action=forgot-password` | Quên MK (OTP session demo) |
| `/auth?action=logout` | Đăng xuất → trang chủ |
| `/dashboard` | Dashboard theo role |
| `/profile` | Hồ sơ / sửa / đổi MK |
| `/building?action=list` | CRUD tòa nhà (ADMIN/MANAGER ghi · STAFF xem) |
| `/apartment?action=list` | Danh sách căn hộ (TV2) |
| `/apartment?action=detail&id=` | Chi tiết căn (owner/thuê + thành viên hộ) |
| `/fee?action=list` | Danh sách phí (TV3 – admin/manager/staff) |
| `/fee?action=categories` | Danh mục phí |
| `/fee?action=my` | Phí của tôi (RESIDENT) |
| `/request?action=…` | Yêu cầu (TV4–TV5, placeholder) |

Servlet map bằng **`@WebServlet`** (không khai báo servlet trong `web.xml` — xem `coding-standards.md`).

Session: **`currentUser`** (`User` object), không dùng key rời `userId` / `userRole`.

---

## 4. Tài khoản demo (seed)

Password tất cả: **`123456`** (plain text – MVP đồ án).

| Username | Role | Ghi chú |
|----------|------|---------|
| admin | ADMIN | Users, thông báo |
| manager | MANAGER | Căn hộ, phí, duyệt request |
| staff | STAFF | Kỹ thuật |
| staff2 | STAFF | Lễ tân |
| resident1 | RESIDENT | Chủ **A-0801** |
| owner1 | RESIDENT | Chủ A-0802 (cho thuê) |
| tenant1 | RESIDENT | Người thuê A-0802 |
| resident2 / resident3 | RESIDENT | A-0901 / A-1005 |
| resident_noapt | RESIDENT | Chưa gán căn |
| locked_user | STAFF | `is_active=0` — không login |

Dashboard expected (sau seed): xem comment đầu `database/seed.sql` / block VERIFY SEED.

---

## 5. Module đã có / chưa có

| Module | Trạng thái |
|--------|------------|
| Auth + profile + forgot OTP + landing | ✅ TV1 |
| Building CRUD | ✅ TV1 |
| Dashboard stats theo role | ✅ TV1 |
| Apartment UC-01…10 | ✅ TV2 |
| Fee (categories, assign, publish, my-fees) | ✅ TV3 (từ main) |
| Request controllers | ❌ placeholder (schema+seed sẵn, sidebar link) |

---

## 6. Phân công (tóm tắt)

| TV | Module | Deliverable chính |
|----|--------|-------------------|
| **TV1** | Nền tảng + Auth + Layout + Profile + Dashboard + DB + CRUD tòa | `DBContext`, filter, auth, building, layout |
| **TV2** | Căn hộ & thành viên | CRUD căn, gán chủ/thuê, thành viên, occupancy |
| **TV3** | Phí tháng | Tạo/công bố phí, Resident xem, đánh dấu TT |
| **TV4** | Yêu cầu (Resident) | Gửi / list / chi tiết / hủy PENDING |
| **TV5** | Xử lý request + Admin | Duyệt/gán/tiến độ; CRUD user; thông báo |

---

## 7. Git / nhánh

Nhánh làm việc: **`tv2`**. Mỗi người chỉ sửa package module mình; shell (`layout`, filter, `DBContext`) merge cẩn thận.

- Không commit mật khẩu máy cá nhân (đổi local trong `DBContext`).  
- `nbproject/private/` đã ignore (path tuyệt đối theo máy).
