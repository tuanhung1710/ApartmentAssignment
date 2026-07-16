-- Reconcile occupancy theo status + cư dân + TV hộ
-- BẮT BUỘC: mở CHECK trước (nếu chưa chạy migrate-apartment-occupancy-vacant-na.sql)

IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_apartments_occupancy' AND parent_object_id = OBJECT_ID(N'dbo.apartments')
)
    ALTER TABLE dbo.apartments DROP CONSTRAINT CK_apartments_occupancy;
GO

ALTER TABLE dbo.apartments
ADD CONSTRAINT CK_apartments_occupancy
CHECK (occupancy_type IN (N'OWNED', N'RENTED', N'VACANT', N'N/A'));
GO

-- 1) INACTIVE → N/A
UPDATE apartments
SET occupancy_type = N'N/A', updated_at = SYSUTCDATETIME()
WHERE status = N'INACTIVE' AND ISNULL(occupancy_type, N'') <> N'N/A';
GO

-- 2) ACTIVE + TENANT → RENTED (giữ OWNER = chủ nhà nếu có)
UPDATE a SET a.occupancy_type = N'RENTED', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND EXISTS (SELECT 1 FROM apartment_residents r
              WHERE r.apartment_id = a.apartment_id AND r.is_current = 1
                AND r.role_in_apartment IN (N'TENANT_REP', N'TENANT'))
  AND ISNULL(a.occupancy_type, N'') <> N'RENTED';
GO

-- 3) ACTIVE + OWNER only (không tenant) → OWNED
UPDATE a SET a.occupancy_type = N'OWNED', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND EXISTS (SELECT 1 FROM apartment_residents r
              WHERE r.apartment_id = a.apartment_id AND r.is_current = 1
                AND r.role_in_apartment = N'OWNER')
  AND NOT EXISTS (SELECT 1 FROM apartment_residents r
                  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1
                    AND r.role_in_apartment IN (N'TENANT_REP', N'TENANT'))
  AND ISNULL(a.occupancy_type, N'') <> N'OWNED';
GO

-- 4) ACTIVE + TV hộ, không role → OWNED
UPDATE a SET a.occupancy_type = N'OWNED', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM apartment_residents r
                  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1)
  AND EXISTS (SELECT 1 FROM household_members hm
              WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1)
  AND ISNULL(a.occupancy_type, N'') <> N'OWNED';
GO

-- 5) ACTIVE trống (0 role + 0 TV) → VACANT
UPDATE a SET a.occupancy_type = N'VACANT', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM apartment_residents r
                  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1)
  AND NOT EXISTS (SELECT 1 FROM household_members hm
                  WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1)
  AND ISNULL(a.occupancy_type, N'') <> N'VACANT';
GO

