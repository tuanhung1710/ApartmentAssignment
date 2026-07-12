# ApartmentManagement (PRJ301)

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư.

**Stack:** NetBeans Ant · Tomcat 10.1 · JDK 17 · Jakarta Servlet/JSP · SQL Server · JDBC · Lombok · JSTL · Bootstrap 5 CDN

Tài liệu nhóm: use case, kế hoạch 1 tuần 5 người, coding standards (trong repo / file chia sẻ nhóm).

---

## 1. Cấu trúc project

```
ApartmentAssignment_tv1/
├── database/
│   ├── schema.sql              # Tạo DB + bảng (TV1)
│   └── seed.sql                # Data demo (TV1)
├── coding-standards.md         # Quy ước code bắt buộc
├── lib/                        # JAR compile (NetBeans)
├── src/java/apartmentmanagement/
│   ├── dal/DBContext.java      # JDBC (TV1)
│   ├── model/                  # Entity Lombok (chung)
│   ├── dao/
│   │   ├── UserDAO.java        # Auth / user (TV1)
│   │   └── DashboardStatsDAO.java  # Count shell dashboard (TV1)
│   ├── filter/                 # EncodingFilter, AuthFilter (TV1)
│   ├── controller/
│   │   ├── auth/               # AuthenController, DashboardController (TV1)
│   │   ├── apartment/          # TV2
│   │   ├── fee/                # TV3
│   │   ├── request/            # TV4 + TV5
│   │   └── admin/              # TV5
│   └── util/                   # FlashUtil, AppConstants (TV1)
├── web/
│   ├── assets/css/app.css
│   ├── index.jsp               # Redirect → login
│   └── WEB-INF/
│       ├── web.xml
│       ├── lib/                # JAR runtime
│       └── views/
│           ├── common/         # layout, header, sidebar, footer, flash (TV1)
│           ├── auth/           # login, profile, đổi MK (TV1)
│           ├── dashboard/      # dashboard theo role (TV1 shell)
│           ├── error/          # 403, 404 (TV1)
│           ├── apartment/      # TV2
│           ├── fee/            # TV3
│           ├── request/        # TV4–TV5
│           └── admin/          # TV5
└── nbproject/
```

**Quy tắc tránh đụng code:** mỗi người chỉ sửa package/view module mình.  
**Chỉ TV1** sửa `layout.jsp` / `sidebar.jsp` / filter / `DBContext`.  
TV2–TV5 cắm trang bằng:

```java
request.setAttribute("pageTitle", "...");
request.setAttribute("contentPage", "/WEB-INF/views/.../xxx.jsp");
request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp").forward(request, response);
```

---

## 2. Chạy project (máy local)

### 2.1. Database (SQL Server)

1. Mở **SQL Server Management Studio** (hoặc `sqlcmd`).
2. Chạy lần lượt:
   - `database/schema.sql` — tạo DB `ApartmentManagement` + bảng  
   - `database/seed.sql` — user demo, căn hộ, phí, request mẫu  
3. Đổi connection trong:

`src/java/apartmentmanagement/dal/DBContext.java`

```text
DB_URL  = jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true
DB_USER = sa
DB_PASSWORD = <mật khẩu SQL Server máy bạn>   ← mỗi máy khác nhau, không commit password production
```

Có thể test nhanh bằng `main` trong `DBContext` (in “Connected OK”).

**Lỗi login “Sai tài khoản…” dù nhập đúng demo?**  
Thường là **chưa kết nối được SQL** (sai password `sa` trong `DBContext`, SQL chưa chạy, chưa seed).  
App sẽ báo rõ nếu mất kết nối CSDL. Xem luôn log Tomcat dòng `DBContext getConnection error`.

### 2.2. NetBeans + Tomcat

1. Mở project bằng **NetBeans** (Java Web / Ant).
2. Tomcat **10.x** (Jakarta).
3. Bật **Annotation Processing** (Lombok).
4. Đảm bảo classpath có: `mssql-jdbc`, `lombok`, JSTL (thường trong `lib/` và `web/WEB-INF/lib/`).
5. Run → context path mặc định: **`/ApartmentManagement`**

### 2.3. URL chính (TV1)

| URL | Mô tả |
|-----|--------|
| `/` hoặc `/auth?action=login` | Đăng nhập |
| `/auth?action=logout` | Đăng xuất |
| `/dashboard` | Dashboard theo role (sau login) |
| `/profile` | Xem hồ sơ |
| `/profile?action=edit-profile` | Sửa hồ sơ |
| `/profile?action=change-password` | Đổi mật khẩu |

Servlet map bằng **`@WebServlet`** (không khai báo servlet trong `web.xml` — xem `coding-standards.md`).

Session: **`currentUser`** (`User` object), không dùng các key rời `userId` / `userRole`.

---

## 3. Tài khoản demo (seed)

Mật khẩu tất cả: **`123456`** (plain text – MVP đồ án).

| Username | Role | Ghi chú demo |
|----------|------|----------------|
| `admin` | ADMIN | User, thông báo |
| `manager` | MANAGER | Căn hộ, phí, duyệt request |
| `staff` | STAFF | Kỹ thuật – việc được gán |
| `staff2` | STAFF | Lễ tân |
| `resident1` | RESIDENT | Chủ căn A-0801 |
| `owner1` | RESIDENT | Chủ căn A-0802 (case thuê) |
| `tenant1` | RESIDENT | Người thuê đại diện A-0802 |
| `resident2` | RESIDENT | Chủ A-0901 |
| `resident3` | RESIDENT | Chủ A-1005 |

---

## 4. Phân công (tóm tắt kế hoạch 1 tuần)

| TV | Module | Deliverable chính |
|----|--------|-------------------|
| **TV1** | Nền tảng + Auth + Layout + Profile + Dashboard shell + DB | `DBContext`, filter, `AuthenController`, `UserDAO`, layout, 403/404, `database/*` |
| **TV2** | Căn hộ & thành viên | CRUD căn hộ, gán chủ/thuê, thành viên |
| **TV3** | Phí tháng | Tạo/công bố phí, Resident xem, đánh dấu TT |
| **TV4** | Yêu cầu (Resident) | Gửi / list / chi tiết / hủy PENDING |
| **TV5** | Xử lý request + Admin | Duyệt/gán/tiến độ; CRUD user; thông báo |

**Cắt scope (không làm tuần này):** upload file, email, quên MK, thanh toán online, chart, đăng ký public.

---

## 5. Checklist TV1 (Definition of Done – phần nền)

- [x] `schema.sql` + `seed.sql` trong repo  
- [x] JDBC `DBContext` + login/logout 4 role  
- [x] AuthFilter + EncodingFilter UTF-8  
- [x] Layout + menu theo role + flash message  
- [x] Profile xem + sửa (full name, phone, email)  
- [x] Đổi mật khẩu  
- [x] Dashboard shell theo role (card text/số, không chart)  
- [x] Trang 403 / 404  
- [ ] Merge/support TV2–TV5 khi họ push module (ngày 3–5)

---

## 6. Git (gợi ý nhóm)

- Branch theo module: `feature/auth`, `feature/apartment`, …  
- Không commit mật khẩu máy cá nhân (đổi local trong `DBContext`, không push password production).  
- `nbproject/private/` nên ignore trên máy mỗi người nếu có path tuyệt đối.

---

## 7. Liên hệ / nộp bài

- Zip: code + `database/*.sql` + slide + báo cáo + **link GitHub public**  
- Demo end-to-end theo kịch bản kế hoạch ngày 4–6 (TV1 điều phối).
