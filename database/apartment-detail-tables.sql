-- Bảng hỗ trợ UC-APT-05 Chi tiết căn hộ
-- Chạy trên DB ApartmentManagement (sau apartments.sql)

USE ApartmentManagement;
GO

-- Gán user ↔ căn (chủ / đại diện thuê)
IF OBJECT_ID(N'dbo.apartment_residents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.apartment_residents (
        id                 INT IDENTITY(1,1) PRIMARY KEY,
        apartment_id       INT NOT NULL,
        user_id            INT NOT NULL,
        role_in_apartment  NVARCHAR(20) NOT NULL, -- OWNER | TENANT_REP | TENANT
        is_current         BIT NOT NULL CONSTRAINT DF_ar_current DEFAULT (1),
        start_date         DATE NULL,
        end_date           DATE NULL,
        created_at         DATETIME2 NOT NULL CONSTRAINT DF_ar_created DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT CK_ar_role CHECK (role_in_apartment IN ('OWNER', 'TENANT_REP', 'TENANT'))
    );
END
GO

-- Thành viên hộ
IF OBJECT_ID(N'dbo.household_members', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.household_members (
        member_id     INT IDENTITY(1,1) PRIMARY KEY,
        apartment_id  INT NOT NULL,
        full_name     NVARCHAR(100) NOT NULL,
        relationship  NVARCHAR(50) NULL,
        phone         NVARCHAR(20) NULL,
        id_number     NVARCHAR(30) NULL,
        date_of_birth DATE NULL,
        is_active     BIT NOT NULL CONSTRAINT DF_hm_active DEFAULT (1),
        created_at    DATETIME2 NOT NULL CONSTRAINT DF_hm_created DEFAULT (SYSUTCDATETIME())
    );
END
GO

-- Lịch sử thao tác căn hộ (audit đơn giản)
IF OBJECT_ID(N'dbo.apartment_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.apartment_history (
        history_id    INT IDENTITY(1,1) PRIMARY KEY,
        apartment_id  INT NOT NULL,
        action        NVARCHAR(50) NOT NULL, -- CREATE | UPDATE | DEACTIVATE | ACTIVATE | DELETE | NOTE
        old_status    NVARCHAR(20) NULL,
        new_status    NVARCHAR(20) NULL,
        note          NVARCHAR(500) NULL,
        actor_user_id INT NULL,
        actor_name    NVARCHAR(100) NULL,
        created_at    DATETIME2 NOT NULL CONSTRAINT DF_ah_created DEFAULT (SYSUTCDATETIME())
    );
END
GO
