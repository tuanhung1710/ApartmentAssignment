# ApartmentManagement (PRJ301)

Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư — NetBeans Ant · Tomcat 10 · Jakarta · SQL Server · JDBC · Lombok · JSP/JSTL/Bootstrap.

## Cấu trúc

```
ApartmentManagement/
├── lib/                          # JARs compile-time (NetBeans classpath)
├── src/java/apartmentmanagement/
│   ├── dal/DBContext.java
│   ├── model/                    # Entity Lombok
│   ├── dao/                      # *DAO extends DBContext
│   ├── filter/                   # EncodingFilter, AuthFilter
│   ├── controller/
│   │   ├── auth/                 # AuthenController, DashboardController
│   │   ├── apartment/            # TV2
│   │   ├── fee/                  # TV3
│   │   ├── request/              # TV4 + TV5
│   │   └── admin/                # TV5
│   └── util/
├── web/
│   ├── assets/css/app.css
│   ├── WEB-INF/
│   │   ├── lib/                  # runtime JARs trong WAR
│   │   ├── views/{common,auth,apartment,fee,request,admin,error}/
│   │   └── web.xml
│   └── index.jsp
└── nbproject/
```

Tài liệu: `../docs/coding-standards.md`, `../docs/use-cases.md`, `../docs/ke-hoach-1-tuan-5-nguoi.md`  
Database: `../database/schema.sql`, `../database/seed.sql`

## Chạy project

1. SQL Server: chạy `database/schema.sql` rồi `database/seed.sql`
2. Sửa connection trong `apartmentmanagement.dal.DBContext` (user/password máy bạn)
3. Mở project bằng **NetBeans** (Java Web / Tomcat 10)
4. Enable **Annotation Processing** (Lombok)
5. Run trên Tomcat 10 → `/ApartmentManagement`

## Tài khoản demo

| Username | Password | Role |
|----------|----------|------|
| admin | 123456 | ADMIN |
| manager | 123456 | MANAGER |
| staff | 123456 | STAFF |
| resident1 | 123456 | RESIDENT |
