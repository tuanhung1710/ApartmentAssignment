/*
================================================================================
  Hệ thống quản lý & xử lý yêu cầu căn hộ chung cư (PRJ301)
  SQL Server - Schema
  Tham chiếu: docs/use-cases.md | docs/ke-hoach-1-tuan-5-nguoi.md
================================================================================
*/

USE master;
GO

IF DB_ID(N'ApartmentManagement') IS NOT NULL
BEGIN
    ALTER DATABASE ApartmentManagement SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ApartmentManagement;
END
GO

CREATE DATABASE ApartmentManagement;
GO

USE ApartmentManagement;
GO

/*--------------------------------------------------------------------------------
  1. USERS - tài khoản + profile
  Role: ADMIN | MANAGER | STAFF | RESIDENT
--------------------------------------------------------------------------------*/
CREATE TABLE users (
    user_id         INT IDENTITY(1,1) PRIMARY KEY,
    username        NVARCHAR(50)  NOT NULL UNIQUE,
    password        NVARCHAR(255) NOT NULL,          -- MVP: plain/hash đơn giản
    full_name       NVARCHAR(100) NOT NULL,
    email           NVARCHAR(100) NULL,
    phone           NVARCHAR(20)  NULL,
    role            NVARCHAR(20)  NOT NULL
                    CONSTRAINT CK_users_role
                    CHECK (role IN (N'ADMIN', N'MANAGER', N'STAFF', N'RESIDENT')),
    department      NVARCHAR(50)  NULL,              -- STAFF: Lễ tân / Kỹ thuật (ghi chú)
    is_active       BIT           NOT NULL
                    CONSTRAINT DF_users_is_active DEFAULT (1),
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_users_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2     NULL
);
GO

CREATE INDEX IX_users_role ON users(role);
CREATE INDEX IX_users_is_active ON users(is_active);
GO

/*--------------------------------------------------------------------------------
  2. APARTMENTS - căn hộ
  occupancy_type: OWNED (chủ ở) | RENTED (cho thuê)
--------------------------------------------------------------------------------*/
CREATE TABLE apartments (
    apartment_id    INT IDENTITY(1,1) PRIMARY KEY,
    apartment_code  NVARCHAR(20)  NOT NULL UNIQUE,   -- VD: A-1201
    building        NVARCHAR(50)  NULL,              -- MVP 1 tòa: có thể NULL / 'A'
    floor_number    INT           NULL,
    area_m2         DECIMAL(8,2)  NULL,
    occupancy_type  NVARCHAR(20)  NOT NULL
                    CONSTRAINT CK_apartments_occupancy
                    CHECK (occupancy_type IN (N'OWNED', N'RENTED')),
    status          NVARCHAR(20)  NOT NULL
                    CONSTRAINT DF_apartments_status DEFAULT (N'ACTIVE')
                    CONSTRAINT CK_apartments_status
                    CHECK (status IN (N'ACTIVE', N'INACTIVE')),
    notes           NVARCHAR(500) NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_apartments_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2     NULL
);
GO

CREATE INDEX IX_apartments_status ON apartments(status);
CREATE INDEX IX_apartments_floor ON apartments(floor_number);
GO

/*--------------------------------------------------------------------------------
  3. APARTMENT_RESIDENTS - liên kết user (chủ / người thuê) với căn hộ
  MVP: 1 Resident user ↔ 1 căn hộ active (enforce ở app; DB cho phép lịch sử)
  role_in_apartment: OWNER | TENANT_REP
--------------------------------------------------------------------------------*/
CREATE TABLE apartment_residents (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    apartment_id    INT           NOT NULL
                    CONSTRAINT FK_ar_apartment
                    REFERENCES apartments(apartment_id),
    user_id         INT           NOT NULL
                    CONSTRAINT FK_ar_user
                    REFERENCES users(user_id),
    role_in_apartment NVARCHAR(20) NOT NULL
                    CONSTRAINT CK_ar_role
                    CHECK (role_in_apartment IN (N'OWNER', N'TENANT_REP')),
    is_current      BIT           NOT NULL
                    CONSTRAINT DF_ar_is_current DEFAULT (1),
    start_date      DATE          NULL,
    end_date        DATE          NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_ar_created_at DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT UQ_ar_apartment_user_role
        UNIQUE (apartment_id, user_id, role_in_apartment, is_current)
);
GO

CREATE INDEX IX_ar_user ON apartment_residents(user_id);
CREATE INDEX IX_ar_apartment_current ON apartment_residents(apartment_id, is_current);
GO

/*--------------------------------------------------------------------------------
  4. HOUSEHOLD_MEMBERS - thành viên sinh sống (không nhất thiết có account)
--------------------------------------------------------------------------------*/
CREATE TABLE household_members (
    member_id       INT IDENTITY(1,1) PRIMARY KEY,
    apartment_id    INT           NOT NULL
                    CONSTRAINT FK_hm_apartment
                    REFERENCES apartments(apartment_id),
    full_name       NVARCHAR(100) NOT NULL,
    relationship    NVARCHAR(50)  NULL,              -- Vợ/Chồng, Con, ...
    phone           NVARCHAR(20)  NULL,
    id_number       NVARCHAR(30)  NULL,              -- CCCD/CMND (tuỳ chọn)
    date_of_birth   DATE          NULL,
    is_active       BIT           NOT NULL
                    CONSTRAINT DF_hm_is_active DEFAULT (1),
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_hm_created_at DEFAULT (SYSUTCDATETIME())
);
GO

CREATE INDEX IX_hm_apartment ON household_members(apartment_id);
GO

/*--------------------------------------------------------------------------------
  5. MONTHLY_FEES - phí dịch vụ hàng tháng theo căn hộ
  service_fee + water_fee + parking_fee
--------------------------------------------------------------------------------*/
CREATE TABLE monthly_fees (
    fee_id          INT IDENTITY(1,1) PRIMARY KEY,
    apartment_id    INT           NOT NULL
                    CONSTRAINT FK_mf_apartment
                    REFERENCES apartments(apartment_id),
    fee_month       TINYINT       NOT NULL
                    CONSTRAINT CK_mf_month CHECK (fee_month BETWEEN 1 AND 12),
    fee_year        SMALLINT      NOT NULL
                    CONSTRAINT CK_mf_year CHECK (fee_year BETWEEN 2000 AND 2100),
    service_fee     DECIMAL(12,2) NOT NULL
                    CONSTRAINT DF_mf_service DEFAULT (0),
    water_fee       DECIMAL(12,2) NOT NULL
                    CONSTRAINT DF_mf_water DEFAULT (0),
    parking_fee     DECIMAL(12,2) NOT NULL
                    CONSTRAINT DF_mf_parking DEFAULT (0),
    total_amount    AS (service_fee + water_fee + parking_fee) PERSISTED,
    status          NVARCHAR(20)  NOT NULL
                    CONSTRAINT DF_mf_status DEFAULT (N'DRAFT')
                    CONSTRAINT CK_mf_status
                    CHECK (status IN (N'DRAFT', N'PUBLISHED', N'PAID', N'UNPAID')),
    -- DRAFT: đang nhập | PUBLISHED/UNPAID: đã gửi cư dân | PAID: đã thanh toán
    note            NVARCHAR(500) NULL,
    created_by      INT           NULL
                    CONSTRAINT FK_mf_created_by REFERENCES users(user_id),
    published_at    DATETIME2     NULL,
    paid_at         DATETIME2     NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_mf_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2     NULL,
    CONSTRAINT UQ_mf_apartment_period UNIQUE (apartment_id, fee_month, fee_year)
);
GO

CREATE INDEX IX_mf_period ON monthly_fees(fee_year, fee_month);
CREATE INDEX IX_mf_status ON monthly_fees(status);
GO

/*--------------------------------------------------------------------------------
  6. REQUESTS - yêu cầu từ cư dân
  request_type: REPAIR | PARKING | MOVE_IN | MOVE_OUT | OTHER
  status: PENDING | APPROVED | REJECTED | ASSIGNED | IN_PROGRESS | COMPLETED | CANCELLED
--------------------------------------------------------------------------------*/
CREATE TABLE requests (
    request_id      INT IDENTITY(1,1) PRIMARY KEY,
    apartment_id    INT           NOT NULL
                    CONSTRAINT FK_req_apartment
                    REFERENCES apartments(apartment_id),
    created_by      INT           NOT NULL
                    CONSTRAINT FK_req_created_by
                    REFERENCES users(user_id),
    request_type    NVARCHAR(20)  NOT NULL
                    CONSTRAINT CK_req_type
                    CHECK (request_type IN (
                        N'REPAIR', N'PARKING', N'MOVE_IN', N'MOVE_OUT', N'OTHER'
                    )),
    title           NVARCHAR(200) NOT NULL,
    description     NVARCHAR(2000) NULL,

    -- REPAIR
    location_detail NVARCHAR(200) NULL,              -- vị trí hỏng (bếp, WC, ...)
    urgency         NVARCHAR(20)  NULL
                    CONSTRAINT CK_req_urgency
                    CHECK (urgency IS NULL OR urgency IN (N'LOW', N'MEDIUM', N'HIGH')),

    -- PARKING
    vehicle_type    NVARCHAR(50)  NULL,              -- Xe máy / Ô tô
    plate_number    NVARCHAR(20)  NULL,

    -- MOVE_IN / MOVE_OUT
    scheduled_at    DATETIME2     NULL,              -- giờ đăng ký chuyển đồ
    move_note       NVARCHAR(500) NULL,

    status          NVARCHAR(20)  NOT NULL
                    CONSTRAINT DF_req_status DEFAULT (N'PENDING')
                    CONSTRAINT CK_req_status
                    CHECK (status IN (
                        N'PENDING', N'APPROVED', N'REJECTED',
                        N'ASSIGNED', N'IN_PROGRESS', N'COMPLETED', N'CANCELLED'
                    )),
    reject_reason   NVARCHAR(500) NULL,
    assigned_to     INT           NULL
                    CONSTRAINT FK_req_assigned_to
                    REFERENCES users(user_id),
    approved_by     INT           NULL
                    CONSTRAINT FK_req_approved_by
                    REFERENCES users(user_id),
    approved_at     DATETIME2     NULL,
    completed_at    DATETIME2     NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_req_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2     NULL
);
GO

CREATE INDEX IX_req_status ON requests(status);
CREATE INDEX IX_req_type ON requests(request_type);
CREATE INDEX IX_req_created_by ON requests(created_by);
CREATE INDEX IX_req_assigned_to ON requests(assigned_to);
CREATE INDEX IX_req_apartment ON requests(apartment_id);
GO

/*--------------------------------------------------------------------------------
  7. REQUEST_HISTORY - lịch sử xử lý / cập nhật tiến độ
--------------------------------------------------------------------------------*/
CREATE TABLE request_history (
    history_id      INT IDENTITY(1,1) PRIMARY KEY,
    request_id      INT           NOT NULL
                    CONSTRAINT FK_rh_request
                    REFERENCES requests(request_id),
    changed_by      INT           NULL
                    CONSTRAINT FK_rh_user
                    REFERENCES users(user_id),
    old_status      NVARCHAR(20)  NULL,
    new_status      NVARCHAR(20)  NOT NULL,
    note            NVARCHAR(1000) NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_rh_created_at DEFAULT (SYSUTCDATETIME())
);
GO

CREATE INDEX IX_rh_request ON request_history(request_id);
GO

/*--------------------------------------------------------------------------------
  8. ANNOUNCEMENTS - thông báo / quy định (giờ chuyển đồ, nội quy, ...)
  Admin (hoặc Manager) đăng; mọi role đã login có thể đọc
--------------------------------------------------------------------------------*/
CREATE TABLE announcements (
    announcement_id INT IDENTITY(1,1) PRIMARY KEY,
    title           NVARCHAR(200) NOT NULL,
    content         NVARCHAR(MAX) NOT NULL,
    category        NVARCHAR(50)  NULL
                    CONSTRAINT DF_ann_category DEFAULT (N'GENERAL'),
                    -- GENERAL | MOVE_RULE | FEE | MAINTENANCE
    is_published    BIT           NOT NULL
                    CONSTRAINT DF_ann_published DEFAULT (1),
    created_by      INT           NULL
                    CONSTRAINT FK_ann_created_by
                    REFERENCES users(user_id),
    published_at    DATETIME2     NULL,
    created_at      DATETIME2     NOT NULL
                    CONSTRAINT DF_ann_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at      DATETIME2     NULL
);
GO

/*--------------------------------------------------------------------------------
  9. SYSTEM_SETTINGS - cấu hình đơn giản (khung giờ chuyển đồ, ...)
  key-value để Admin/Manager sửa không cần deploy lại
--------------------------------------------------------------------------------*/
CREATE TABLE system_settings (
    setting_key     NVARCHAR(100) NOT NULL PRIMARY KEY,
    setting_value   NVARCHAR(500) NOT NULL,
    description     NVARCHAR(300) NULL,
    updated_by      INT           NULL
                    CONSTRAINT FK_ss_updated_by REFERENCES users(user_id),
    updated_at      DATETIME2     NULL
);
GO

PRINT N'Schema ApartmentManagement created successfully.';
GO
