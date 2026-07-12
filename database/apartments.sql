-- Bảng căn hộ – UC Thêm căn hộ (US-APT-01)
-- Chạy trên SQL Server database ApartmentManagement

IF OBJECT_ID(N'dbo.apartments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.apartments (
        apartment_id    INT IDENTITY(1,1) PRIMARY KEY,
        apartment_code  NVARCHAR(20)  NOT NULL,
        building        NVARCHAR(50)  NOT NULL,
        floor_number    INT           NOT NULL,
        area_m2         DECIMAL(10,2) NOT NULL,
        occupancy_type  NVARCHAR(20)  NOT NULL, -- OWNED | RENTED
        status          NVARCHAR(20)  NOT NULL CONSTRAINT DF_apartments_status DEFAULT ('ACTIVE'), -- ACTIVE | INACTIVE
        notes           NVARCHAR(500) NULL,
        created_at      DATETIME2     NOT NULL CONSTRAINT DF_apartments_created DEFAULT (SYSUTCDATETIME()),
        updated_at      DATETIME2     NOT NULL CONSTRAINT DF_apartments_updated DEFAULT (SYSUTCDATETIME()),
        CONSTRAINT UQ_apartments_code UNIQUE (apartment_code),
        CONSTRAINT CK_apartments_occupancy CHECK (occupancy_type IN ('OWNED', 'RENTED')),
        CONSTRAINT CK_apartments_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT CK_apartments_floor CHECK (floor_number >= 0 AND floor_number <= 200),
        CONSTRAINT CK_apartments_area CHECK (area_m2 > 0 AND area_m2 <= 10000)
    );
END
GO
