# ApartmentManagement (PRJ301) — TV1 + TV2 + Fee + TV4 + TV5 (Admin)

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư.

**Stack:** NetBeans Ant · Tomcat 10.1 · JDK 17 · Jakarta Servlet/JSP · SQL Server · JDBC · Lombok · JSTL · Bootstrap 5 · Jakarta WebSocket

**Merge:** platform TV1 (auth, building, dashboard, schema/seed) + module căn hộ TV2 (UC-APT-01…09, occupancy VACANT/N/A, history) + module phí TV3 (categories, assignments, resident my-fees) + module yêu cầu cư dân TV4 (gửi / list / chi tiết / hủy PENDING + chat realtime) + **TV5 Admin** (CRUD user + thông báo nội bộ).

---

## 1. Cấu trúc

```
ApartmentAssignment/
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
│   ├── dao/                    # User, Building, Apartment*, Fee*, Request*, AnnouncementDAO (TV5), …
│   ├── filter/
│   ├── controller/
│   │   ├── auth/               # Authen + Dashboard
│   │   ├── admin/              # Building CRUD (TV1) + AdminController user/announcement (TV5)
│   │   ├── apartment/          # TV2 full module
│   │   ├── fee/                # TV3 fee module
│   │   └── request/            # TV4 resident request (+ comment API)
│   ├── websocket/              # TV4 request chat (WS hub/endpoint)
│   └── util/                   # FlashUtil, DateTimeUtil, HtmlSanitizer, …
└── web/WEB-INF/views/
    ├── common/                 # layout, sidebar, pagination, flash
    ├── auth/ apartment/ fee/ dashboard/ error/
    ├── admin/                  # building-* (TV1) + listUser/addUser/… + announcement (TV5)
    └── request/                # my-list, create, detail, _comments
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

Bảng liên quan TV4 (đã có trong `schema.sql`): `requests`, `request_history`, `system_settings`.

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
2. NetBeans → Open project  
3. Tomcat **10.x** (Jakarta), bật Annotation Processing (Lombok)  
4. Classpath: `mssql-jdbc`, `lombok`, JSTL (thường trong `lib/` và `web/WEB-INF/lib/`); WebSocket API đi kèm Tomcat  
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
| `/request?action=my` | Danh sách yêu cầu của cư dân (TV4) |
| `/request?action=create` | Form gửi yêu cầu mới (TV4) |
| `/request?action=detail&id=` | Chi tiết + lịch sử + chat (TV4) |
| `/request?action=cancel&id=` | Hủy yêu cầu PENDING (TV4) |
| `/request-comment` | API list/add comment (fallback HTTP) |
| `/ws/request-chat/{requestId}` | WebSocket chat realtime trên ticket |
| `/admin?action=users` | **TV5** Danh sách user (lọc + phân trang) — ADMIN |
| `/admin?action=add` | **TV5** Thêm user |
| `/admin?action=detail&id=` | **TV5** Chi tiết user |
| `/admin?action=edit&id=` | **TV5** Sửa user (role/dept/status) |
| `/admin?action=lock\|unlock&id=` | **TV5** Khóa / mở khóa user |
| `/admin?action=reset-password&id=` | **TV5** Reset MK về `DEFAULT_PASSWORD` (`123456`) |
| `/admin?action=announcements` | **TV5** Danh sách thông báo nội bộ |
| `/admin?action=add-announcement` | **TV5** Thêm thông báo |
| `/admin?action=edit-announcement&id=` | **TV5** Sửa thông báo |
| `/admin?action=delete-announcement&id=` | **TV5** Xóa thông báo |
| `/request?action=manage` | **TV5** Xử lý yêu cầu (Manager/Admin xem all · Staff việc được giao) |
| `/request` POST `approve` / `reject` / `assign` | **TV5** Duyệt / từ chối / gán staff (Manager/Admin) |
| `/request` POST `update-progress` / `complete` | **TV5** Cập nhật tiến độ / hoàn thành (Staff được gán) |

Servlet map bằng **`@WebServlet`** / **`@ServerEndpoint`** (không khai báo servlet trong `web.xml` — xem `coding-standards.md`).

Session: **`currentUser`** (`User` object), không dùng key rời `userId` / `userRole`.

---

## 4. Tài khoản demo (seed)

Password tất cả: **`123456`** (plain text – MVP đồ án).

| Username | Role | Ghi chú |
|----------|------|---------|
| admin | ADMIN | Users, thông báo |
| manager | MANAGER | Căn hộ, phí, duyệt request (TV5) |
| staff | STAFF | Kỹ thuật |
| staff2 | STAFF | Lễ tân |
| resident1 | RESIDENT | Chủ **A-0801** — test TV4 |
| owner1 | RESIDENT | Chủ A-0802 (cho thuê) |
| tenant1 | RESIDENT | Người thuê A-0802 |
| resident2 / resident3 | RESIDENT | A-0901 / A-1005 |
| resident_noapt | RESIDENT | Chưa gán căn — không gửi được request |
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
| Fee (categories, assign, publish, my-fees) | ✅ TV3 |
| Request cư dân (gửi / list / detail / hủy PENDING) | ✅ TV4 |
| Request chat/comment (HTTP + WebSocket) | ✅ TV4 |
| **Admin CRUD user** (list/add/edit/detail/lock/unlock/reset MK) | ✅ **TV5** |
| **Admin CRUD thông báo nội bộ** (list/add/edit/delete + publish) | ✅ **TV5** |
| **Duyệt / gán / cập nhật tiến độ request** | ✅ **TV5** (`/request?action=manage`) |

---

## 6. TV4 — Yêu cầu cư dân (tóm tắt)

**Phạm vi hiện tại (RESIDENT):**

| Action | Method | Mô tả |
|--------|--------|--------|
| `my` / `list` | GET | Danh sách yêu cầu của user (lọc status/type/keyword, phân trang) |
| `create` | GET/POST | Form + submit: REPAIR / PARKING / MOVE_IN / MOVE_OUT / OTHER |
| `detail` | GET | Chi tiết ticket, lịch sử trạng thái, khung chat |
| `cancel` | GET/POST | Hủy khi `status = PENDING` |

**Điều kiện:** user phải là `RESIDENT` và đang được gán căn (`apartment_residents.is_current = 1`).  
`resident_noapt` sẽ bị chặn khi tạo request.

**Chat / comment trên ticket:**

- UI: `web/WEB-INF/views/request/_comments.jsp` (Quill rich text + timeline)
- Realtime: `ws://…/ws/request-chat/{requestId}` (`RequestChatEndpoint` + `RequestChatHub`)
- Fallback HTTP: `/request-comment?action=list|add`
- Nội dung HTML được sanitize (`HtmlSanitizer`); lịch sử lưu trong `request_history` (comment = `old_status == new_status`)

**Package chính:**

```
controller/request/   RequestController, RequestCommentController
dao/                  RequestDAO, RequestHistoryDAO, SystemSettingDAO
                      (+ ApartmentResidentDAO.findCurrentByUserId)
websocket/            RequestChatEndpoint, RequestChatHub, RequestChatService, ChatHandshakeConfigurator
util/                 DateTimeUtil, HtmlSanitizer
views/request/        my-list.jsp, create.jsp, detail.jsp, _comments.jsp
```

**Chưa thuộc TV4:** duyệt/từ chối, gán staff, đổi trạng thái (thuộc **§7b TV5 process** bên dưới).

---

## 7. TV5 — Admin user & thông báo (đã port lên `main`)

Phần **quản trị hệ thống** của TV5: CRUD tài khoản + thông báo nội bộ.

**Quyền:** chỉ **ADMIN** (`AuthFilter` rule `/admin` + `AdminController.requireAdmin`).  
Sidebar: **Người dùng** · **Thông báo**.

### 7.1. User management (`/admin`)

| Action | Method | Mô tả |
|--------|--------|--------|
| `users` / `list` | GET | Lọc keyword (username/full name), role, status + phân trang |
| `add` | GET/POST | Tạo user (username unique, password, full name, email, phone, role, dept) |
| `detail` / `view` | GET | Chi tiết user |
| `edit` | GET/POST | Sửa full name, role, department, status (email/phone readonly — user tự sửa profile) |
| `lock` / `unlock` | GET/POST | Bật/tắt `is_active` |
| `reset-password` | GET/POST | Reset MK về `AppConstants.DEFAULT_PASSWORD` (`123456`) |

**Bảo vệ:** không khóa tài khoản role **ADMIN** và không khóa chính user đang đăng nhập.

**DAO bổ sung:** `UserDAO.existsByUsername`, `updateByAdmin`, `findWithFilters`, `countWithFilters`, `findActiveStaff`.

### 7.2. Announcement management (`/admin`)

| Action | Method | Mô tả |
|--------|--------|--------|
| `announcements` | GET | List + lọc published/draft + phân trang |
| `add-announcement` | GET/POST | Tạo thông báo (title, content, category, is_published) |
| `edit-announcement` | GET/POST | Sửa thông báo |
| `delete-announcement` | GET/POST | Xóa thông báo |

**DAO:** `AnnouncementDAO` (CRUD admin).  
Landing public vẫn dùng `PublicAnnouncementDAO.findPublished` (chỉ đọc bản đã publish).

**Bảng:** `announcements` (đã có trong `database/schema.sql`).

### 7.3. Package / view chính

```
controller/admin/     AdminController.java          (@WebServlet /admin)
                      BuildingController.java       (TV1 — /building, không đụng /admin)
dao/                  AnnouncementDAO.java
                      UserDAO.java                  (+ method admin)
util/                 AppConstants.DEFAULT_PASSWORD
views/admin/          listUser.jsp, addUser.jsp, editUser.jsp, userDetail.jsp
                      listAnnouncement.jsp, addAnnouncement.jsp, editAnnouncement.jsp
                      building-*.jsp                (TV1 — giữ nguyên)
```

**Tích hợp shell:** forward qua `common/layout.jsp` + flash + compact `pagination.jsp` (giống building/fee).

**Test nhanh (sau seed):** login `admin` / `123456` → menu **Người dùng** / **Thông báo**.

---

## 7b. TV5 — Xử lý request (duyệt / gán / tiến độ)

Sidebar **Xử lý yêu cầu** → `/request?action=manage` (không còn placeholder).

### Luồng trạng thái

```
PENDING ──approve──► APPROVED ──assign──► ASSIGNED ──► IN_PROGRESS ──► COMPLETED
   │                     │
   └── reject ──► REJECTED     (Resident vẫn hủy được khi PENDING → CANCELLED)
```

| Role | Quyền |
|------|--------|
| **MANAGER / ADMIN** | List all · detail · approve · reject · assign staff |
| **STAFF** | List **chỉ việc được gán** · detail · update-progress · complete |
| **RESIDENT** | Giữ TV4: my / create / detail(own) / cancel PENDING |

### Actions

| Action | Method | Ai | Điều kiện |
|--------|--------|----|-----------|
| `manage` / `list` (staff+) | GET | Processor | Staff tự filter `assigned_to = me` |
| `detail` | GET | Theo ACL | Resident = owner · Staff = assigned · Mgr/Admin = all |
| `approve` | POST | Mgr/Admin | `status = PENDING` |
| `reject` | POST | Mgr/Admin | `status = PENDING` + `rejectReason` bắt buộc |
| `assign` | POST | Mgr/Admin | `status = APPROVED` + `staffId` STAFF active |
| `update-progress` | POST | Staff assigned | ASSIGNED→IN_PROGRESS/COMPLETED · IN_PROGRESS→… |
| `complete` | POST | Staff assigned | ASSIGNED hoặc IN_PROGRESS → COMPLETED |

Mỗi lần đổi status ghi `request_history` (`[APPROVE]`, `[REJECT]`, `[ASSIGN]`, …).

### Package / view

```
controller/request/   RequestController  (+ manage/approve/reject/assign/progress/complete)
dao/                  RequestDAO         (+ findWithFilters, approve/reject/assign/…)
                      UserDAO.findActiveStaff
views/request/        manage-list.jsp, manage-detail.jsp   (processor)
                      my-list.jsp, create.jsp, detail.jsp  (resident – TV4)
```

**Test nhanh:**  
1. `resident1` gửi request → PENDING  
2. `manager` / `admin` → **Xử lý yêu cầu** → Approve → Assign `staff`  
3. `staff` → việc được giao → Update progress / Complete  

---

## 8. Phân công (tóm tắt)

| TV | Module | Deliverable chính | Trên `main` |
|----|--------|-------------------|-------------|
| **TV1** | Nền tảng + Auth + Layout + Profile + Dashboard + DB + CRUD tòa | `DBContext`, filter, auth, building, layout | ✅ |
| **TV2** | Căn hộ & thành viên | CRUD căn, gán chủ/thuê, thành viên, occupancy | ✅ |
| **TV3** | Phí tháng | Tạo/công bố phí, Resident xem, đánh dấu TT | ✅ |
| **TV4** | Yêu cầu (Resident) | Gửi / list / chi tiết / hủy PENDING + chat | ✅ |
| **TV5** | Admin + xử lý request | CRUD user + thông báo; duyệt/gán/tiến độ request | ✅ |

---

## 9. Git / nhánh

Nhánh chính: **`main`**. Module từng TV merge/port vào `main` sau khi ổn định.

- Mỗi người chỉ sửa package module mình; shell (`layout`, filter, `DBContext`) merge cẩn thận.  
- **TV5 Admin** được **port chọn lọc** lên `main` (không merge cả nhánh feature cũ để tránh đè building/apartment/fee/request).  
- Không commit mật khẩu máy cá nhân (đổi local trong `DBContext`).  
- `nbproject/private/` đã ignore (path tuyệt đối theo máy).
