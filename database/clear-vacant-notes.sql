-- Xóa ghi chú căn VACANT; giữ notes khi INACTIVE (N/A)
USE ApartmentManagement;
GO

UPDATE apartments
SET notes = NULL,
    updated_at = SYSUTCDATETIME()
WHERE occupancy_type = N'VACANT'
  AND notes IS NOT NULL
  AND LTRIM(RTRIM(notes)) <> N'';
GO

SELECT apartment_code, occupancy_type, status, notes
FROM apartments
WHERE occupancy_type = N'VACANT' AND notes IS NOT NULL;
-- Kỳ vọng: 0 rows

SELECT TOP 10 apartment_code, occupancy_type, status, notes
FROM apartments
WHERE status = N'INACTIVE' AND notes IS NOT NULL
ORDER BY apartment_code;
GO
