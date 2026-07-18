USE ApartmentManagement;
GO

SET NOCOUNT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
GO

IF OBJECT_ID(N'dbo.fee_categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.fee_categories (
        category_id     INT IDENTITY(1,1) NOT NULL
                        CONSTRAINT PK_fee_categories PRIMARY KEY,
        name            NVARCHAR(100) NOT NULL
                        CONSTRAINT UQ_fc_name UNIQUE,
        description     NVARCHAR(500) NULL,
        is_active       BIT           NOT NULL
                        CONSTRAINT DF_fc_is_active DEFAULT (1),
        created_at      DATETIME2(0)  NOT NULL
                        CONSTRAINT DF_fc_created_at DEFAULT (SYSUTCDATETIME())
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fc_active_name' AND object_id = OBJECT_ID(N'dbo.fee_categories')
)
    CREATE INDEX IX_fc_active_name ON dbo.fee_categories (is_active, name);
GO

IF OBJECT_ID(N'dbo.fees', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.fees (
        fee_id          INT IDENTITY(1,1) NOT NULL
                        CONSTRAINT PK_fees PRIMARY KEY,
        category_id     INT           NOT NULL
                        CONSTRAINT FK_fees_category
                        REFERENCES dbo.fee_categories(category_id),
        title           NVARCHAR(200) NOT NULL,
        amount          DECIMAL(12,2) NOT NULL
                        CONSTRAINT CK_fees_amount CHECK (amount >= 0),
        fee_month       TINYINT       NULL
                        CONSTRAINT CK_fees_month
                        CHECK (fee_month IS NULL OR fee_month BETWEEN 1 AND 12),
        fee_year        SMALLINT      NULL
                        CONSTRAINT CK_fees_year
                        CHECK (fee_year IS NULL OR fee_year BETWEEN 2000 AND 2100),

        fee_type        NVARCHAR(20)  NOT NULL
                        CONSTRAINT DF_fees_fee_type DEFAULT (N'MONTHLY')
                        CONSTRAINT CK_fees_fee_type
                        CHECK (fee_type IN (N'MONTHLY', N'ONE_TIME')),
        status          NVARCHAR(20)  NOT NULL
                        CONSTRAINT DF_fees_status DEFAULT (N'DRAFT')
                        CONSTRAINT CK_fees_status
                        CHECK (status IN (N'DRAFT', N'ASSIGNED', N'PUBLISHED')),
        note            NVARCHAR(500) NULL,
        created_by      INT           NULL
                        CONSTRAINT FK_fees_created_by REFERENCES dbo.users(user_id),
        created_at      DATETIME2(0)  NOT NULL
                        CONSTRAINT DF_fees_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at      DATETIME2(0)  NULL
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fees_status_created' AND object_id = OBJECT_ID(N'dbo.fees')
)
    CREATE INDEX IX_fees_status_created ON dbo.fees (status, created_at DESC);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fees_category' AND object_id = OBJECT_ID(N'dbo.fees')
)
    CREATE INDEX IX_fees_category ON dbo.fees (category_id);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fees_period' AND object_id = OBJECT_ID(N'dbo.fees')
)
    CREATE INDEX IX_fees_period ON dbo.fees (fee_year, fee_month)
        WHERE fee_year IS NOT NULL;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fees_fee_type' AND object_id = OBJECT_ID(N'dbo.fees')
)
    CREATE INDEX IX_fees_fee_type ON dbo.fees (fee_type);
GO

IF OBJECT_ID(N'dbo.fee_scopes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.fee_scopes (
        scope_id        INT IDENTITY(1,1) NOT NULL
                        CONSTRAINT PK_fee_scopes PRIMARY KEY,
        fee_id          INT           NOT NULL
                        CONSTRAINT FK_fs_fee
                        REFERENCES dbo.fees(fee_id) ON DELETE CASCADE
                        CONSTRAINT UQ_fs_fee UNIQUE,
        scope_type      NVARCHAR(20)  NOT NULL
                        CONSTRAINT CK_fs_type
                        CHECK (scope_type IN (N'ALL', N'BUILDING', N'FLOOR', N'APARTMENT')),
        building        NVARCHAR(50)  NULL,
        floor_number    INT           NULL,
        apartment_id    INT           NULL
                        CONSTRAINT FK_fs_apartment REFERENCES dbo.apartments(apartment_id),
        created_at      DATETIME2(0)  NOT NULL
                        CONSTRAINT DF_fs_created_at DEFAULT (SYSUTCDATETIME()),

        CONSTRAINT CK_fs_fields CHECK (
            (scope_type = N'ALL'
                AND building IS NULL AND floor_number IS NULL AND apartment_id IS NULL)
            OR (scope_type = N'BUILDING'
                AND building IS NOT NULL AND floor_number IS NULL AND apartment_id IS NULL)
            OR (scope_type = N'FLOOR'
                AND building IS NOT NULL AND floor_number IS NOT NULL AND apartment_id IS NULL)
            OR (scope_type = N'APARTMENT'
                AND apartment_id IS NOT NULL)
        )
    );
END
GO

IF OBJECT_ID(N'dbo.fee_assignments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.fee_assignments (
        assignment_id   INT IDENTITY(1,1) NOT NULL
                        CONSTRAINT PK_fee_assignments PRIMARY KEY,
        fee_id          INT           NOT NULL
                        CONSTRAINT FK_fa_fee
                        REFERENCES dbo.fees(fee_id) ON DELETE CASCADE,
        apartment_id    INT           NOT NULL
                        CONSTRAINT FK_fa_apartment REFERENCES dbo.apartments(apartment_id),
        amount          DECIMAL(12,2) NOT NULL
                        CONSTRAINT CK_fa_amount CHECK (amount >= 0),
        status          NVARCHAR(20)  NOT NULL
                        CONSTRAINT DF_fa_status DEFAULT (N'UNPAID')
                        CONSTRAINT CK_fa_status
                        CHECK (status IN (N'UNPAID', N'PAID')),
        assigned_at     DATETIME2(0)  NOT NULL
                        CONSTRAINT DF_fa_assigned_at DEFAULT (SYSUTCDATETIME()),
        paid_at         DATETIME2(0)  NULL,
        CONSTRAINT UQ_fa_fee_apartment UNIQUE (fee_id, apartment_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fa_apartment_status' AND object_id = OBJECT_ID(N'dbo.fee_assignments')
)
    CREATE INDEX IX_fa_apartment_status ON dbo.fee_assignments (apartment_id, status);
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_fa_status' AND object_id = OBJECT_ID(N'dbo.fee_assignments')
)
    CREATE INDEX IX_fa_status ON dbo.fee_assignments (status)
        INCLUDE (amount);
GO

IF OBJECT_ID(N'dbo.payments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payments (
        payment_id      INT IDENTITY(1,1) NOT NULL
                        CONSTRAINT PK_payments PRIMARY KEY,
        assignment_id   INT           NOT NULL
                        CONSTRAINT FK_pay_assignment
                        REFERENCES dbo.fee_assignments(assignment_id) ON DELETE CASCADE,
        amount          DECIMAL(12,2) NOT NULL
                        CONSTRAINT CK_pay_amount CHECK (amount >= 0),
        paid_at         DATETIME2(0)  NOT NULL
                        CONSTRAINT DF_pay_paid_at DEFAULT (SYSUTCDATETIME()),
        note            NVARCHAR(500) NULL,
        recorded_by     INT           NULL
                        CONSTRAINT FK_pay_recorded_by REFERENCES dbo.users(user_id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_pay_assignment' AND object_id = OBJECT_ID(N'dbo.payments')
)
    CREATE INDEX IX_pay_assignment ON dbo.payments (assignment_id);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.fee_categories)
BEGIN
    INSERT INTO dbo.fee_categories (name, description, is_active)
    VALUES
    (N'Phí quản lý', N'Phí quản lý vận hành chung cư', 1),
    (N'Phí gửi xe',  N'Phí gửi xe máy / ô tô', 1),
    (N'Phí bảo trì', N'Quỹ bảo trì định kỳ', 1),
    (N'Phí điện',    N'Phí điện công cộng / căn hộ', 1),
    (N'Phí nước',    N'Phí nước sinh hoạt', 1),
    (N'Phí dịch vụ', N'Các dịch vụ khác', 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.fees)
BEGIN
    DECLARE @catMgmt INT = (SELECT category_id FROM dbo.fee_categories WHERE name = N'Phí quản lý');
    DECLARE @catPark INT = (SELECT category_id FROM dbo.fee_categories WHERE name = N'Phí gửi xe');
    DECLARE @catMain INT = (SELECT category_id FROM dbo.fee_categories WHERE name = N'Phí bảo trì');
    DECLARE @createdBy INT = (
        SELECT TOP (1) user_id FROM dbo.users WHERE role IN (N'MANAGER', N'ADMIN') ORDER BY user_id
    );

    DECLARE @feeDraft INT, @feeAssigned INT, @feePublished INT;

    INSERT INTO dbo.fees (category_id, title, amount, fee_month, fee_year, fee_type, status, note, created_by)
    VALUES (@catMgmt, N'Phí quản lý tháng 8/2026', 500000, 8, 2026, N'MONTHLY', N'DRAFT',
            N'Nháp — chưa gán căn', @createdBy);
    SET @feeDraft = SCOPE_IDENTITY();

    INSERT INTO dbo.fees (category_id, title, amount, fee_month, fee_year, fee_type, status, note, created_by)
    VALUES (@catPark, N'Phí gửi xe tháng 8/2026', 200000, 8, 2026, N'MONTHLY', N'ASSIGNED',
            N'Đã gán, chưa công bố', @createdBy);
    SET @feeAssigned = SCOPE_IDENTITY();

    INSERT INTO dbo.fees (category_id, title, amount, fee_month, fee_year, fee_type, status, note, created_by)
    VALUES (@catMain, N'Phí bảo trì quý 3/2026', 300000, 7, 2026, N'ONE_TIME', N'PUBLISHED',
            N'Tầng 5 — đã công bố', @createdBy);
    SET @feePublished = SCOPE_IDENTITY();

    INSERT INTO dbo.fee_scopes (fee_id, scope_type, building, floor_number, apartment_id)
    VALUES
    (@feeDraft,     N'BUILDING', N'A', NULL, NULL),
    (@feeAssigned,  N'ALL',      NULL, NULL, NULL),
    (@feePublished, N'FLOOR',    N'A', 5,    NULL);

    INSERT INTO dbo.fee_assignments (fee_id, apartment_id, amount, status, assigned_at, paid_at)
    SELECT @feeAssigned, apartment_id, 200000,
           CASE WHEN apartment_id = (
                    SELECT MIN(apartment_id) FROM dbo.apartments WHERE status = N'ACTIVE'
                )
                THEN N'PAID' ELSE N'UNPAID' END,
           SYSUTCDATETIME(),
           CASE WHEN apartment_id = (
                    SELECT MIN(apartment_id) FROM dbo.apartments WHERE status = N'ACTIVE'
                )
                THEN SYSUTCDATETIME() ELSE NULL END
    FROM dbo.apartments
    WHERE status = N'ACTIVE';

    INSERT INTO dbo.fee_assignments (fee_id, apartment_id, amount, status)
    SELECT @feePublished, apartment_id, 300000, N'UNPAID'
    FROM dbo.apartments
    WHERE status = N'ACTIVE' AND building = N'A' AND floor_number = 5;

    INSERT INTO dbo.payments (assignment_id, amount, paid_at, note, recorded_by)
    SELECT assignment_id, amount, paid_at, N'Thu demo', @createdBy
    FROM dbo.fee_assignments
    WHERE status = N'PAID' AND paid_at IS NOT NULL;
END
GO

PRINT N'Fee module schema + seed OK.';
GO
