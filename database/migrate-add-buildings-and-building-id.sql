/*
  Migrate (DB đã có TV2 apartments, chưa có buildings / building_id)
  Chạy trên SQL Server: database ApartmentManagement

  1) Tạo bảng buildings nếu thiếu
  2) Seed 3 tòa A/B/C nếu rỗng
  3) Thêm apartments.building_id + backfill theo building code
  4) FK + index
  5) (Gợi ý) chạy thêm migrate-apartment-occupancy-vacant-na.sql nếu CHECK occupancy còn cũ
*/

USE ApartmentManagement;
GO

/* 1) buildings */
IF OBJECT_ID(N'dbo.buildings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.buildings (
        building_id     INT IDENTITY(1,1) PRIMARY KEY,
        building_code   NVARCHAR(20)  NOT NULL UNIQUE,
        building_name   NVARCHAR(100) NOT NULL,
        address         NVARCHAR(300) NULL,
        total_floors    INT NULL
            CONSTRAINT CK_buildings_floors
            CHECK (total_floors IS NULL OR total_floors BETWEEN 1 AND 200),
        description     NVARCHAR(1000) NULL,
        status          NVARCHAR(20)  NOT NULL
            CONSTRAINT DF_buildings_status DEFAULT (N'ACTIVE')
            CONSTRAINT CK_buildings_status CHECK (status IN (N'ACTIVE', N'INACTIVE')),
        created_at      DATETIME2 NOT NULL
            CONSTRAINT DF_buildings_created_at DEFAULT (SYSUTCDATETIME()),
        updated_at      DATETIME2 NULL
    );
    PRINT N'Created table buildings';
END
GO

/* 2) seed tòa mặc định nếu trống */
IF NOT EXISTS (SELECT 1 FROM dbo.buildings)
BEGIN
    INSERT INTO dbo.buildings (building_code, building_name, address, total_floors, description, status)
    VALUES
    (N'A', N'Tòa A – Sky View', N'88 Nguyễn Huệ, Quận 1, TP.HCM', 20, N'Migrate seed', N'ACTIVE'),
    (N'B', N'Tòa B – Garden', N'88 Nguyễn Huệ, Quận 1, TP.HCM (block B)', 15, N'Migrate seed', N'ACTIVE'),
    (N'C', N'Tòa C – Riverside', N'90 Nguyễn Huệ, Quận 1, TP.HCM', 12, N'Migrate seed', N'ACTIVE');
    PRINT N'Seeded buildings A/B/C';
END
GO

/* 3) apartments.building_id */
IF COL_LENGTH(N'dbo.apartments', N'building_id') IS NULL
BEGIN
    ALTER TABLE dbo.apartments ADD building_id INT NULL;
    PRINT N'Added apartments.building_id';
END
GO

/* 4) backfill: match building_code exact, hoặc token đầu của building string */
UPDATE a
SET a.building_id = b.building_id
FROM dbo.apartments a
INNER JOIN dbo.buildings b ON (
    a.building = b.building_code
    OR LEFT(LTRIM(a.building), 1) = b.building_code
)
WHERE a.building_id IS NULL;
GO

/* 5) FK + index (bỏ qua nếu đã có) */
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_apartments_building' AND parent_object_id = OBJECT_ID(N'dbo.apartments')
)
BEGIN
    ALTER TABLE dbo.apartments
    ADD CONSTRAINT FK_apartments_building
        FOREIGN KEY (building_id) REFERENCES dbo.buildings(building_id);
    PRINT N'Added FK_apartments_building';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_apartments_building_id' AND object_id = OBJECT_ID(N'dbo.apartments')
)
BEGIN
    CREATE INDEX IX_apartments_building_id ON dbo.apartments(building_id);
END
GO

PRINT N'migrate-add-buildings-and-building-id done.';
GO
