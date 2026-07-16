-- Migrate: occupancy VACANT / N/A + coupling status
-- Chạy trên SQL Server DB ApartmentManagement (bảng apartments đã tồn tại)
--
-- Rule:
--   INACTIVE → occupancy N/A
--   ACTIVE không cư dân current → VACANT
--   ACTIVE còn cư dân → giữ OWNED/RENTED (fallback OWNED nếu lạ)

-- 1) Mở CHECK occupancy (drop cũ nếu có)
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_apartments_occupancy' AND parent_object_id = OBJECT_ID(N'dbo.apartments')
)
BEGIN
    ALTER TABLE dbo.apartments DROP CONSTRAINT CK_apartments_occupancy;
END
GO

-- 2) Migrate data (trước khi gắn CHECK mới)
-- 2a) INACTIVE → N/A
UPDATE dbo.apartments
SET occupancy_type = N'N/A',
    updated_at = SYSUTCDATETIME()
WHERE status = N'INACTIVE';
GO

-- 2b) ACTIVE không có cư dân current → VACANT
UPDATE a
SET a.occupancy_type = N'VACANT',
    a.updated_at = SYSUTCDATETIME()
FROM dbo.apartments a
WHERE a.status = N'ACTIVE'
  AND NOT EXISTS (
        SELECT 1
        FROM dbo.apartment_residents r
        WHERE r.apartment_id = a.apartment_id
          AND r.is_current = 1
    )
  AND a.occupancy_type IN (N'OWNED', N'RENTED');
GO

-- 2c) ACTIVE còn cư dân nhưng occupancy lạ → OWNED
UPDATE a
SET a.occupancy_type = N'OWNED',
    a.updated_at = SYSUTCDATETIME()
FROM dbo.apartments a
WHERE a.status = N'ACTIVE'
  AND a.occupancy_type NOT IN (N'OWNED', N'RENTED', N'VACANT');
GO

-- 2d) Bất kỳ row còn occupancy ngoài 4 giá trị → map an toàn
UPDATE dbo.apartments
SET occupancy_type = CASE
        WHEN status = N'INACTIVE' THEN N'N/A'
        ELSE N'VACANT'
    END,
    updated_at = SYSUTCDATETIME()
WHERE occupancy_type NOT IN (N'OWNED', N'RENTED', N'VACANT', N'N/A');
GO

-- 3) CHECK mới
ALTER TABLE dbo.apartments
ADD CONSTRAINT CK_apartments_occupancy
CHECK (occupancy_type IN (N'OWNED', N'RENTED', N'VACANT', N'N/A'));
GO
