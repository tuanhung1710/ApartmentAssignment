# ApartmentManagement (PRJ301)

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư.

**Stack:** NetBeans Ant · Tomcat 10.1 · JDK 17 · Jakarta Servlet/JSP · SQL Server · JDBC · Lombok · JSTL · Bootstrap 5 · Jakarta WebSocket

**Nhánh demo / nộp bài:** `main` (đủ TV1–TV5). Các nhánh `tv1`…`Tv5` chỉ là nhánh dev theo thành viên.

---

## 1. Chạy nhanh

### 1.1. Database (SQL Server) — đường chuẩn

Máy mới hoặc muốn reset sạch, **chỉ cần 2 file**:

1. `database/schema.sql` — DROP + CREATE DB `ApartmentManagement` + toàn bộ bảng
2. `database/seed.sql` — data demo (users, 3 tòa, 282 căn, phí, request…)

> Không cần `sql/fee-module.sql` hay `migrate-*.sql` nếu chạy lại từ đầu.
> Các file đó chỉ dùng khi **giữ DB cũ** (không drop được) — xem §6.

Sửa connection:

`src/java/apartmentmanagement/dal/DBContext.java`

```text
DB_URL  = jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true
DB_USER = sa
DB_PASSWORD = <mật khẩu SQL Server máy bạn>
```

Test nhanh: chạy `main` trong `DBContext` → in `Connected OK`.
Login demo báo sai dù đúng tài khoản → thường là **chưa kết nối SQL** / chưa seed (xem log Tomcat: `DBContext getConnection error`).

### 1.2. NetBeans + Tomcat

1. Open project bằng **NetBeans** (Java Web / Ant)
2. Tomcat **10.x** (Jakarta)
3. Bật **Annotation Processing** (Lombok)
4. Classpath: `mssql-jdbc`, `lombok`, JSTL (`lib/` + `web/WEB-INF/lib/`); WebSocket API đi kèm Tomcat
5. Run → context path **`/ApartmentManagement`**

---

## 2. Tài khoản demo

Password tất cả: **`123456`** (plain text – MVP).

| Username | Role | Ghi chú |
|----------|------|---------|
| `admin` | ADMIN | User, thông báo, building |
| `manager` | MANAGER | Căn hộ, phí, duyệt request |
| `staff` | STAFF | Kỹ thuật – việc được gán |
| `staff2` | STAFF | Lễ tân |
| `resident1` | RESIDENT | Chủ **A-0801** — case demo chính |
| `owner1` / `tenant1` | RESIDENT | Chủ / người thuê **A-0802** |
| `resident2` / `resident3` | RESIDENT | A-0901 / A-1005 |
| `resident_noapt` | RESIDENT | Chưa gán căn — empty state |
| `locked_user` | STAFF | `is_active=0` — **không login** |

**Dashboard sau seed (đối chiếu nhanh):**

| Role | Kỳ vọng card |
|------|----------------|
| ADMIN (`admin`) | Users **11**, Locked **1**, Buildings **3**, Apartments **282** |
| MANAGER | Pending **1**, Processing **2**, Draft fees **2** |
| STAFF (`staff`) | Assigned **1**, Completed week **≥1** |
| RESIDENT (`resident1`) | Căn **A-0801**, open request **1**, phí demo tháng 7/2026 |

Chi tiết VERIFY SEED: comment đầu `database/seed.sql`.

---

## 3. URL demo

Base: `http://localhost:8080/ApartmentManagement`

| URL | Mô tả |
|-----|--------|
| `/` · `/auth?action=home` | Landing public |
| `/auth?action=login` | Đăng nhập |
| `/auth?action=forgot-password` | Quên MK (OTP demo session) |
| `/dashboard` | Dashboard theo role |
| `/profile` | Hồ sơ / sửa / đổi MK |
| `/building?action=list` | CRUD tòa nhà (TV1) |
| `/apartment?action=list` | Căn hộ (TV2) |
| `/fee?action=list` · `/fee?action=my` | Phí quản trị / phí cư dân (TV3) |
| `/request?action=my` · `create` · `detail` | Yêu cầu cư dân + chat (TV4) |
| `/request?action=manage` | Duyệt / gán / tiến độ (TV5) |
| `/admin?action=users` · `announcements` | User + thông báo (TV5, ADMIN) |
| `/ws/request-chat/{requestId}` | WebSocket chat trên ticket |

Servlet map bằng **`@WebServlet`** / **`@ServerEndpoint`** (không khai báo servlet trong `web.xml`).
Session: **`currentUser`** (`User`), không dùng key rời `userId` / `userRole`.
Chi tiết coding: [`coding-standards.md`](coding-standards.md).

### Kịch bản demo end-to-end

1. `resident1` → gửi request (`/request?action=create`) → PENDING
2. `manager` / `admin` → **Xử lý yêu cầu** → Approve → Assign `staff`
3. `staff` → việc được giao → Update progress / Complete
4. `admin` → **Người dùng** / **Thông báo**
5. `manager` → `/apartment`, `/fee` (danh mục + gán phí)

---

## 4. Module & phân công

| TV | Module | Trạng thái trên `main` |
|----|--------|-------------------------|
| **TV1** | Auth, profile, forgot OTP, landing, layout, filter, DB, dashboard shell, CRUD tòa | ✅ |
| **TV2** | Căn hộ, occupancy OWNED/RENTED/VACANT, gán chủ/thuê, thành viên hộ | ✅ |
| **TV3** | Danh mục phí, gán/công bố, resident my-fees | ✅ |
| **TV4** | Request cư dân (gửi/list/detail/hủy PENDING) + chat HTTP/WebSocket | ✅ |
| **TV5** | Admin user + thông báo; duyệt/gán/tiến độ request | ✅ |

Cắm trang module (TV2–TV5):

```java
request.setAttribute("pageTitle", "...");
request.setAttribute("contentPage", "/WEB-INF/views/.../xxx.jsp");
request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
```

Tài liệu UC/BR căn hộ: thư mục [`docs/`](docs/).

---

## 5. Cấu trúc thư mục (rút gọn)

```
ApartmentAssignment/
├── database/
│   ├── schema.sql                 # ★ SOURCE OF TRUTH — drop/create DB + bảng
│   ├── seed.sql                   # ★ Data demo
│   ├── migrate-apartment-vacant.sql          # optional / legacy
│   └── trigger-vacant-when-no-tenant.sql     # optional helper
├── sql/fee-module.sql             # optional — chỉ khi DB cũ thiếu bảng phí
├── docs/                          # UC, BR, test case (TV2…)
├── coding-standards.md
├── src/java/apartmentmanagement/
│   ├── dal/          # DBContext
│   ├── model/ dao/ filter/ util/
│   ├── controller/   # auth, admin, apartment, fee, request
│   └── websocket/    # request chat (TV4)
└── web/
    ├── assets/css/app.css
    ├── index.jsp
    └── WEB-INF/views/{common,auth,dashboard,admin,apartment,fee,request,error}/
```

---

## 6. File SQL phụ (không dùng khi setup chuẩn)

| File | Khi nào chạy |
|------|----------------|
| `sql/fee-module.sql` | DB đã có từ schema cũ, **không** drop được, thiếu bảng `fee_*` / `payments` |
| `database/migrate-apartment-vacant.sql` | DB cũ chưa có `occupancy_type = VACANT` |
| `database/trigger-vacant-when-no-tenant.sql` | Helper hết hạn hợp đồng thuê — **không** bắt buộc để demo |

**Ưu tiên luôn:** drop sạch bằng `schema.sql` + `seed.sql`.

**Occupancy (TV2):** `INACTIVE` → UI **N/A**; `ACTIVE` → OWNED / RENTED / VACANT. Create / init-floor mặc định: INACTIVE + N/A.

---

## 7. Git & nộp bài

- Nhánh chính: **`main`**. Mỗi TV làm trên nhánh riêng rồi merge/port vào `main`.
- Không commit mật khẩu SQL máy cá nhân (`DBContext`).
- `nbproject/private/` — path máy local, đã ignore khi có thể.

**Nộp:**

- Link GitHub public (nhánh **`main`**)
- Zip: source + `database/schema.sql` + `database/seed.sql` + slide + báo cáo
- **Không cần** export `.bak` từ SQL Server nếu đã có 2 script trên

---

## 8. Liên hệ nhóm

Phân công TV1–TV5 theo kế hoạch 1 tuần 5 người (xem tài liệu nhóm / `docs/`).
Demo điều phối theo kịch bản §3.
