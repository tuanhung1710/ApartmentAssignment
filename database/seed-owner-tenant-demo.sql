-- Seed demo: OWNED (chủ ở) vs RENTED (chủ nhà + người thuê)
-- DB: ApartmentManagement
-- User demo: owner1 (6), tenant1 (7), resident1 (5), resident3 (9)

USE ApartmentManagement;
GO

-- Đảm bảo user demo tồn tại
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'owner1')
    INSERT INTO users (username, password, full_name, email, phone, role, is_active)
    VALUES (N'owner1', N'123456', N'Hoàng Chủ Hộ', N'owner1@email.com', N'0902000002', N'RESIDENT', 1);

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'tenant1')
    INSERT INTO users (username, password, full_name, email, phone, role, is_active)
    VALUES (N'tenant1', N'123456', N'Võ Người Thuê', N'tenant1@email.com', N'0902000003', N'RESIDENT', 1);

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'resident1')
    INSERT INTO users (username, password, full_name, email, phone, role, is_active)
    VALUES (N'resident1', N'123456', N'Phạm Minh Cư', N'resident1@email.com', N'0902000001', N'RESIDENT', 1);
GO

DECLARE @owner1 INT = (SELECT TOP 1 user_id FROM users WHERE username = N'owner1');
DECLARE @tenant1 INT = (SELECT TOP 1 user_id FROM users WHERE username = N'tenant1');
DECLARE @resident1 INT = (SELECT TOP 1 user_id FROM users WHERE username = N'resident1');
DECLARE @resident3 INT = (SELECT TOP 1 user_id FROM users WHERE username = N'resident3');

DECLARE @a0801 INT = (SELECT TOP 1 apartment_id FROM apartments WHERE apartment_code = N'A-0801');
DECLARE @a0802 INT = (SELECT TOP 1 apartment_id FROM apartments WHERE apartment_code = N'A-0802');
DECLARE @a1005 INT = (SELECT TOP 1 apartment_id FROM apartments WHERE apartment_code = N'A-1005');

-- ========== A-0801: OWNED — chủ ở (owner1), KHÔNG tenant ==========
IF @a0801 IS NOT NULL AND @owner1 IS NOT NULL
BEGIN
    DELETE FROM apartment_residents WHERE apartment_id = @a0801;
    INSERT INTO apartment_residents (apartment_id, user_id, role_in_apartment, is_current, start_date)
    VALUES (@a0801, @owner1, N'OWNER', 1, '2024-01-01');

    UPDATE apartments
    SET occupancy_type = N'OWNED', status = N'ACTIVE',
        notes = N'Chủ ở – demo owner1 (không người thuê)',
        updated_at = SYSUTCDATETIME()
    WHERE apartment_id = @a0801;

    -- TV hộ demo
    IF NOT EXISTS (SELECT 1 FROM household_members WHERE apartment_id = @a0801 AND is_active = 1)
    BEGIN
        INSERT INTO household_members (apartment_id, full_name, relationship, phone, is_active)
        VALUES
            (@a0801, N'Hoàng Chủ Hộ', N'Chủ hộ', N'0902000002', 1),
            (@a0801, N'Hoàng Vợ', N'Vợ/Chồng', N'0902000012', 1),
            (@a0801, N'Hoàng Con', N'Con', NULL, 1);
    END
END

-- ========== A-0802: RENTED — chủ nhà (owner1) + đại diện thuê (tenant1) ==========
IF @a0802 IS NOT NULL AND @owner1 IS NOT NULL AND @tenant1 IS NOT NULL
BEGIN
    DELETE FROM apartment_residents WHERE apartment_id = @a0802;
    INSERT INTO apartment_residents (apartment_id, user_id, role_in_apartment, is_current, start_date)
    VALUES
        (@a0802, @owner1, N'OWNER', 1, '2024-01-01'),
        (@a0802, @tenant1, N'TENANT_REP', 1, '2025-01-15');

    UPDATE apartments
    SET occupancy_type = N'RENTED', status = N'ACTIVE',
        notes = N'Cho thuê – chủ nhà owner1 + tenant1',
        updated_at = SYSUTCDATETIME()
    WHERE apartment_id = @a0802;

    IF NOT EXISTS (SELECT 1 FROM household_members WHERE apartment_id = @a0802 AND is_active = 1)
    BEGIN
        INSERT INTO household_members (apartment_id, full_name, relationship, phone, is_active)
        VALUES
            (@a0802, N'Võ Người Thuê', N'Chủ hộ', N'0902000003', 1),
            (@a0802, N'Võ Vợ', N'Vợ/Chồng', NULL, 1);
    END
END

-- ========== A-1005: OWNED — chủ ở (resident3), không tenant ==========
IF @a1005 IS NOT NULL AND @resident3 IS NOT NULL
BEGIN
    DELETE FROM apartment_residents WHERE apartment_id = @a1005 AND role_in_apartment IN (N'TENANT', N'TENANT_REP');
    IF NOT EXISTS (
        SELECT 1 FROM apartment_residents
        WHERE apartment_id = @a1005 AND is_current = 1 AND role_in_apartment = N'OWNER'
    )
    BEGIN
        DELETE FROM apartment_residents WHERE apartment_id = @a1005;
        INSERT INTO apartment_residents (apartment_id, user_id, role_in_apartment, is_current, start_date)
        VALUES (@a1005, @resident3, N'OWNER', 1, '2024-05-01');
    END

    UPDATE apartments
    SET occupancy_type = N'OWNED', status = N'ACTIVE',
        notes = N'Chủ ở – demo resident3',
        updated_at = SYSUTCDATETIME()
    WHERE apartment_id = @a1005;
END
GO

-- Kiểm tra
SELECT a.apartment_code, a.occupancy_type, a.status,
       r.role_in_apartment, u.username, u.full_name
FROM apartments a
LEFT JOIN apartment_residents r ON r.apartment_id = a.apartment_id AND r.is_current = 1
LEFT JOIN users u ON u.user_id = r.user_id
WHERE a.apartment_code IN (N'A-0801', N'A-0802', N'A-1005')
ORDER BY a.apartment_code, r.role_in_apartment;
GO
