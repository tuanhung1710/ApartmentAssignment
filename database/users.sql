-- Bảng users + tài khoản demo (login app)
-- Chạy trên SQL Server: database ApartmentManagement
-- Password demo plaintext (PRJ301): 123456

USE ApartmentManagement;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        user_id     INT IDENTITY(1,1) PRIMARY KEY,
        username    NVARCHAR(50)  NOT NULL,
        password    NVARCHAR(100) NOT NULL,
        full_name   NVARCHAR(100) NOT NULL,
        email       NVARCHAR(100) NULL,
        phone       NVARCHAR(20)  NULL,
        role        NVARCHAR(20)  NOT NULL, -- ADMIN | MANAGER | STAFF | RESIDENT
        department  NVARCHAR(100) NULL,
        is_active   BIT NOT NULL CONSTRAINT DF_users_active DEFAULT (1),
        created_at  DATETIME2 NOT NULL CONSTRAINT DF_users_created DEFAULT (SYSUTCDATETIME()),
        updated_at  DATETIME2 NOT NULL CONSTRAINT DF_users_updated DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT UQ_users_username UNIQUE (username),
        CONSTRAINT CK_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'STAFF', 'RESIDENT'))
    );
END
GO

-- Seed demo (không insert trùng username)
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'admin')
    INSERT INTO dbo.users (username, password, full_name, email, phone, role, department, is_active)
    VALUES (N'admin', N'123456', N'Quản trị viên', N'admin@tienhung.vn', N'0901000001', N'ADMIN', N'Ban quản trị', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'manager')
    INSERT INTO dbo.users (username, password, full_name, email, phone, role, department, is_active)
    VALUES (N'manager', N'123456', N'Quản lý tòa nhà', N'manager@tienhung.vn', N'0901000002', N'MANAGER', N'Vận hành', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'staff')
    INSERT INTO dbo.users (username, password, full_name, email, phone, role, department, is_active)
    VALUES (N'staff', N'123456', N'Nhân viên kỹ thuật', N'staff@tienhung.vn', N'0901000003', N'STAFF', N'Kỹ thuật', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'resident1')
    INSERT INTO dbo.users (username, password, full_name, email, phone, role, department, is_active)
    VALUES (N'resident1', N'123456', N'Cư dân A', N'resident1@email.com', N'0901000004', N'RESIDENT', NULL, 1);
GO

-- Kiểm tra
SELECT user_id, username, password, role, is_active FROM dbo.users;
GO
