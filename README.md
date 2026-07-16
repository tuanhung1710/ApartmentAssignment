# ApartmentManagement (PRJ301) — TV1 + TV2 unified

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư.

**Stack:** NetBeans Ant · Tomcat 10.1 · JDK 17 · Jakarta Servlet/JSP · SQL Server · JDBC · Lombok · JSTL · Bootstrap 5

**Merge:** platform TV1 (auth, building, dashboard, schema/seed) + module căn hộ TV2 (UC-APT-01…10, occupancy VACANT/N/A, history, members).

---

## 1. Cấu trúc

```
tv2/
├── database/
│   ├── schema.sql              # ★ SOURCE OF TRUTH — drop/create DB + bảng (TV1+TV2)
│   ├── seed.sql                # ★ Data demo đầy đủ (282 căn, fees, requests…)
│   ├── migrate-*.sql           # Nâng cấp DB cũ (không drop)
│   └── apartments.sql / users.sql / …  # script legacy TV2 (tham khảo)
├── docs/                       # UC/BR module căn hộ (TV2)
├── coding-standards.md
├── src/java/apartmentmanagement/
│   ├── dal/DBContext.java
│   ├── model/
│   ├── dao/                    # User, Building, Apartment*, DashboardStats, …
│   ├── filter/
│   ├── controller/
│   │   ├── auth/               # Authen + Dashboard
│   │   ├── admin/              # Building CRUD
│   │   └── apartment/          # TV2 full module
│   └── util/
└── web/WEB-INF/views/
    ├── common/ auth/ admin/ apartment/ dashboard/ error/
```

---

## 2. Database (SQL Server)

### Máy mới / reset sạch

1. Chạy **`database/schema.sql`** (DROP + CREATE DB `ApartmentManagement`)
2. Chạy **`database/seed.sql`**

### DB TV2 cũ (đã có apartments, chưa buildings)

1. `database/migrate-apartment-occupancy-vacant-na.sql` (nếu CHECK occupancy còn OWNED/RENTED)
2. `database/migrate-reconcile-occupancy.sql`
3. `database/migrate-add-buildings-and-building-id.sql`

### Connection

`src/java/apartmentmanagement/dal/DBContext.java`

```text
jdbc:sqlserver://localhost:1433;databaseName=ApartmentManagement;encrypt=true;trustServerCertificate=true
user: sa
password: <máy bạn>
```

### Occupancy (TV2 rules)

| status | occupancy |
|--------|-----------|
| INACTIVE | **N/A** only |
| ACTIVE | OWNED / RENTED / **VACANT** |

Create / init-floor mặc định: **INACTIVE + N/A**.

---

## 3. Chạy app

1. SQL Server + schema/seed
2. NetBeans → Open project `tv2`
3. Tomcat **10.x** (Jakarta), bật Annotation Processing (Lombok)
4. Run → context **`/ApartmentManagement`**

### URL chính

| URL | Mô tả |
|-----|--------|
| `/` hoặc `/auth?action=home` | Trang chủ public |
| `/auth?action=login` | Đăng nhập |
| `/auth?action=forgot-password` | Quên MK (OTP session demo) |
| `/dashboard` | Dashboard theo role |
| `/building?action=list` | CRUD tòa nhà |
| `/apartment?action=list` | Danh sách căn hộ (TV2) |
| `/apartment?action=members` | Thành viên hộ |
| `/profile` | Hồ sơ |

---

## 4. Tài khoản demo (seed)

Password tất cả: **`123456`**

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

Dashboard expected (sau seed): xem comment đầu `database/seed.sql`.

---

## 5. Module đã có / chưa có

| Module | Trạng thái |
|--------|------------|
| Auth + profile + forgot OTP + landing | ✅ TV1 |
| Building CRUD | ✅ TV1 |
| Dashboard stats theo role | ✅ TV1 |
| Apartment UC-01…10 | ✅ TV2 |
| Fee / Request controllers | ❌ placeholder (schema+seed sẵn, sidebar link) |

---

## 6. Git / nhánh

Nhánh làm việc: **`tv2`**. Mỗi người chỉ sửa package module mình; shell (`layout`, filter, `DBContext`) merge cẩn thận.
