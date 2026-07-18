/*
================================================================================
  Migration (optional) — thêm VACANT vào apartments.occupancy_type
  Chỉ chạy nếu DB đã seed theo schema cũ (OWNED | RENTED).

  Nếu bạn re-run schema.sql + seed.sql từ đầu thì KHÔNG cần file này.
================================================================================
*/
USE ApartmentManagement;
GO

/* 1) Nới CHECK occupancy_type: OWNED | RENTED | VACANT */
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = N'CK_apartments_occupancy' AND parent_object_id = OBJECT_ID(N'apartments')
)
BEGIN
    ALTER TABLE apartments DROP CONSTRAINT CK_apartments_occupancy;
END
GO

ALTER TABLE apartments
    ADD CONSTRAINT CK_apartments_occupancy
    CHECK (occupancy_type IN (N'OWNED', N'RENTED', N'VACANT'));
GO

/* 2) Index hỗ trợ filter loại hình */
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_apartments_occupancy' AND object_id = OBJECT_ID(N'apartments')
)
BEGIN
    CREATE INDEX IX_apartments_occupancy ON apartments(occupancy_type);
END
GO

/*
  3) Gán VACANT deterministic cho một phần căn ACTIVE bulk
     (unit cuối mã căn % 3 = 0, không đụng 5 căn demo id 1–5)
*/
UPDATE a
SET
    occupancy_type = N'VACANT',
    notes = CASE
                WHEN a.notes IS NULL OR LTRIM(RTRIM(a.notes)) = N''
                    THEN N'Sẵn sàng bàn giao – chưa có cư dân chuyển vào'
                ELSE a.notes
            END,
    updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND a.apartment_id > 5
  AND TRY_CAST(RIGHT(a.apartment_code, 2) AS INT) % 3 = 0
  AND a.occupancy_type <> N'VACANT';
GO

SELECT
    occupancy_type,
    status,
    COUNT(*) AS cnt
FROM apartments
GROUP BY occupancy_type, status
ORDER BY status, occupancy_type;
GO
