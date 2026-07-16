/*
================================================================================
  Seed data demo - ApartmentManagement UNIFIED (TV1+TV2)
  Chạy SAU schema.sql:
    1) database/schema.sql   → drop/create DB + bảng
    2) database/seed.sql     → data demo (file này)

  Password demo: 123456 (plain text – MVP PRJ301)

  Tài khoản nhanh:
  -------------------------------------------------------------------------------
  admin / 123456       ADMIN
  manager / 123456     MANAGER
  staff / 123456       STAFF (Kỹ thuật)
  staff2 / 123456      STAFF (Lễ tân)
  resident1 / 123456   RESIDENT (A-0801)  ← case dashboard cư dân chính
  owner1 / 123456      RESIDENT (chủ A-0802)
  tenant1 / 123456     RESIDENT (thuê A-0802)
  resident2 / 123456   RESIDENT (A-0901)
  resident3 / 123456   RESIDENT (A-1005)
  resident_noapt       RESIDENT chưa gán căn → card "Chưa gán căn hộ"
  locked_user          is_active=0 → KHÔNG login (test ADMIN lockedUsers)
  -------------------------------------------------------------------------------

  Expected dashboard (sau seed, login demo):
  -------------------------------------------------------------------------------
  ADMIN    (admin)      : totalUsers=11 · lockedUsers=1 · buildings=3
                          totalApartments = 20*6 + 15*6 + 12*6 = 282
                          (~80% ACTIVE / ~20% INACTIVE — formula deterministic)
  MANAGER  (manager)    : pending=1 · processing=2 · draftFees=2
  STAFF    (staff id=3) : assignedJobs=1 · inProgressJobs=0 · completedWeek≥1
  STAFF    (staff2 id=4): assignedJobs=0 · inProgressJobs=1 · completedWeek=0
  RESIDENT (resident1)  : A-0801 · latestFee 7/2026 DRAFT · openRequests=1
                          · newAnnouncements≥3 (publish trong 30 ngày)
  RESIDENT (resident_noapt): Chưa gán căn hộ · Chưa có phí · openRequests=0
  -------------------------------------------------------------------------------
  Căn hộ: mỗi tầng 6 căn (xx01–xx06). 5 căn demo cố định id 1–5 (cư dân/phí/request).
  Status “random” ổn định: (floor*7 + unit*11 + building_id*3) % 5 = 0 → INACTIVE.
  Ghi chú: completedWeek / newAnnouncements dùng SYSUTCDATETIME() → ổn định theo “hôm nay”.
================================================================================
*/

USE ApartmentManagement;
GO

/*--------------------------------------------------------------------------------
  USERS
  - locked_user (id=11): is_active=0 → admin card lockedUsers = 1
  - resident_noapt (id=10): RESIDENT không gán căn → test empty state dashboard
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT users ON;

INSERT INTO users (user_id, username, password, full_name, email, phone, role, department, is_active)
VALUES
(1,  N'admin',          N'123456', N'Quản trị hệ thống',   N'admin@chungcu.vn',       N'0901000001', N'ADMIN',    NULL,           1),
(2,  N'manager',        N'123456', N'Nguyễn Văn Quản',     N'manager@chungcu.vn',     N'0901000002', N'MANAGER',  NULL,           1),
(3,  N'staff',          N'123456', N'Trần Thị Kỹ Thuật',   N'staff@chungcu.vn',       N'0901000003', N'STAFF',    N'Kỹ thuật',    1),
(4,  N'staff2',         N'123456', N'Lê Văn Lễ Tân',       N'staff2@chungcu.vn',      N'0901000004', N'STAFF',    N'Lễ tân',      1),
(5,  N'resident1',      N'123456', N'Phạm Minh Cư',        N'resident1@email.com',    N'0902000001', N'RESIDENT', NULL,           1),
(6,  N'owner1',         N'123456', N'Hoàng Chủ Hộ',        N'owner1@email.com',       N'0902000002', N'RESIDENT', NULL,           1),
(7,  N'tenant1',        N'123456', N'Võ Người Thuê',       N'tenant1@email.com',      N'0902000003', N'RESIDENT', NULL,           1),
(8,  N'resident2',      N'123456', N'Đặng Thị Lan',        N'resident2@email.com',    N'0902000004', N'RESIDENT', NULL,           1),
(9,  N'resident3',      N'123456', N'Bùi Quốc An',         N'resident3@email.com',    N'0902000005', N'RESIDENT', NULL,           1),
(10, N'resident_noapt', N'123456', N'Lý Chưa Gán Căn',     N'noapt@email.com',        N'0902000099', N'RESIDENT', NULL,           1),
(11, N'locked_user',    N'123456', N'Tài khoản bị khóa',   N'locked@chungcu.vn',      N'0901000099', N'STAFF',    N'Kỹ thuật',    0);

SET IDENTITY_INSERT users OFF;
GO

/*--------------------------------------------------------------------------------
  BUILDINGS – 3 tòa · mỗi tầng 6 căn (seed bulk bên dưới)
  A: 20 tầng × 6 = 120 · B: 15 × 6 = 90 · C: 12 × 6 = 72 · Tổng 282 căn
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT buildings ON;

INSERT INTO buildings (building_id, building_code, building_name, address, total_floors, description, status)
VALUES
(1, N'A', N'Tòa A – Sky View',
    N'88 Nguyễn Huệ, Quận 1, TP.HCM', 20,
    N'Tòa chính 20 tầng × 6 căn/tầng – demo resident/owner/tenant + bulk seed', N'ACTIVE'),
(2, N'B', N'Tòa B – Garden',
    N'88 Nguyễn Huệ, Quận 1, TP.HCM (block B)', 15,
    N'15 tầng × 6 căn – filter theo tòa / ACTIVE-INACTIVE', N'ACTIVE'),
(3, N'C', N'Tòa C – Riverside',
    N'90 Nguyễn Huệ, Quận 1, TP.HCM', 12,
    N'12 tầng × 6 căn – bulk seed test pagination + filter', N'ACTIVE');

SET IDENTITY_INSERT buildings OFF;
GO

/*--------------------------------------------------------------------------------
  APARTMENTS
  1) 5 căn demo cố định id 1–5 (cư dân / phí / request dashboard) — luôn ACTIVE
  2) Bulk: mọi tầng × 6 căn (xx01–xx06), trừ 5 mã demo
     Status “random” deterministic ~20% INACTIVE:
       (floor*7 + unit*11 + building_id*3) % 5 = 0 → INACTIVE
     Occupancy (TV2): INACTIVE → N/A; ACTIVE bulk trống → VACANT; demo 1-4 có cư dân OWNED/RENTED; A-1201 VACANT
     Diện tích: 52 + unit*4 + (floor%4)*3  (m²)
  Không gán cư dân/phí cho căn bulk (trừ 5 căn demo) → dashboard không lệch
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT apartments ON;

INSERT INTO apartments (apartment_id, apartment_code, building_id, building, floor_number, area_m2, occupancy_type, status, notes)
VALUES
(1, N'A-0801', 1, N'A', 8,  65.50, N'OWNED',  N'ACTIVE', N'Chủ ở – demo resident1'),
(2, N'A-0802', 1, N'A', 8,  72.00, N'RENTED', N'ACTIVE', N'Cho thuê – owner1 + tenant1'),
(3, N'A-0901', 1, N'A', 9,  65.50, N'OWNED',  N'ACTIVE', N'Demo resident2'),
(4, N'A-1005', 1, N'A', 10, 90.00, N'OWNED',  N'ACTIVE', N'Demo resident3'),
(5, N'A-1201', 1, N'A', 12, 75.00, N'VACANT', N'ACTIVE', N'Chưa gán cư dân active – VACANT');

SET IDENTITY_INSERT apartments OFF;
GO

/* Tầng 1..20 (đủ max tòa A) + unit 1..6 */
;WITH floors AS (
    SELECT 1 AS fl
    UNION ALL
    SELECT fl + 1 FROM floors WHERE fl < 20
),
units AS (
    SELECT 1 AS un
    UNION ALL
    SELECT un + 1 FROM units WHERE un < 6
),
bld AS (
    SELECT building_id, building_code, total_floors
    FROM buildings
)
INSERT INTO apartments (
    apartment_code, building_id, building, floor_number, area_m2,
    occupancy_type, status, notes
)
SELECT
    b.building_code + N'-'
        + RIGHT(N'00' + CAST(f.fl AS NVARCHAR(2)), 2)
        + RIGHT(N'00' + CAST(u.un AS NVARCHAR(2)), 2)
        AS apartment_code,
    b.building_id,
    b.building_code,
    f.fl,
    CAST(52 + (u.un * 4) + ((f.fl % 4) * 3) + ((b.building_id - 1) * 1.5) AS DECIMAL(8,2)) AS area_m2,
    CASE
        WHEN (f.fl * 7 + u.un * 11 + b.building_id * 3) % 5 = 0
            THEN N'N/A'       -- INACTIVE
        ELSE N'VACANT'        -- ACTIVE trống (chưa gán cư dân)
    END AS occupancy_type,
    CASE
        WHEN (f.fl * 7 + u.un * 11 + b.building_id * 3) % 5 = 0
            THEN N'INACTIVE'
        ELSE N'ACTIVE'
    END AS status,
    CASE
        WHEN (f.fl * 7 + u.un * 11 + b.building_id * 3) % 5 = 0 THEN
            CASE (f.fl + u.un + b.building_id) % 4
                WHEN 0 THEN N'Bảo trì hệ thống nước – tạm ngưng'
                WHEN 1 THEN N'Sửa chữa kết cấu / chống thấm'
                WHEN 2 THEN N'Trống lâu – chờ bàn giao / cho thuê lại'
                ELSE N'Chưa hoàn thiện nội thất'
            END
        ELSE NULL
    END AS notes
FROM bld b
INNER JOIN floors f ON f.fl <= b.total_floors
CROSS JOIN units u
WHERE NOT (
    /* Trùng 5 căn demo đã IDENTITY_INSERT */
    (b.building_code = N'A' AND f.fl = 8  AND u.un = 1)
    OR (b.building_code = N'A' AND f.fl = 8  AND u.un = 2)
    OR (b.building_code = N'A' AND f.fl = 9  AND u.un = 1)
    OR (b.building_code = N'A' AND f.fl = 10 AND u.un = 5)
    OR (b.building_code = N'A' AND f.fl = 12 AND u.un = 1)
)
OPTION (MAXRECURSION 100);
GO

/*--------------------------------------------------------------------------------
  APARTMENT_RESIDENTS
  - A-0801: resident1 = OWNER
  - A-0802: owner1 = OWNER, tenant1 = TENANT_REP
  - A-0901: resident2 OWNER
  - A-1005: resident3 OWNER
  - resident_noapt: không có dòng → empty state
--------------------------------------------------------------------------------*/
INSERT INTO apartment_residents (apartment_id, user_id, role_in_apartment, is_current, start_date)
VALUES
(1, 5, N'OWNER',      1, '2024-01-01'),
(2, 6, N'OWNER',      1, '2023-06-01'),
(2, 7, N'TENANT_REP', 1, '2025-01-15'),
(3, 8, N'OWNER',      1, '2024-03-01'),
(4, 9, N'OWNER',      1, '2024-05-01');
GO

/*--------------------------------------------------------------------------------
  HOUSEHOLD_MEMBERS
--------------------------------------------------------------------------------*/
INSERT INTO household_members (apartment_id, full_name, relationship, phone, id_number, date_of_birth, is_active)
VALUES
(1, N'Phạm Minh Cư',     N'Chủ hộ',               N'0902000001', N'001099000111', '1990-05-12', 1),
(1, N'Nguyễn Thị Hoa',   N'Vợ',                   N'0902000011', N'001099000112', '1992-08-20', 1),
(1, N'Phạm Bé Na',       N'Con',                  NULL,          NULL,            '2018-01-05', 1),
(2, N'Võ Người Thuê',    N'Người thuê đại diện',  N'0902000003', N'001088000221', '1988-11-03', 1),
(2, N'Võ Bé Bắp',        N'Con',                  NULL,          NULL,            '2016-07-19', 1),
(3, N'Đặng Thị Lan',     N'Chủ hộ',               N'0902000004', N'001095000331', '1995-02-14', 1),
(4, N'Bùi Quốc An',      N'Chủ hộ',               N'0902000005', N'001091000441', '1991-09-30', 1);
GO

/*--------------------------------------------------------------------------------
  MONTHLY_FEES
  - resident1 (apt 1): 5 PAID · 6 UNPAID · 7 DRAFT  → latest = 7/2026 DRAFT
  - manager draftFees = 2 (apt 1 + apt 2 tháng 7)
--------------------------------------------------------------------------------*/
INSERT INTO monthly_fees
    (apartment_id, fee_month, fee_year, service_fee, water_fee, parking_fee, status, note, created_by, published_at, paid_at)
VALUES
-- Tháng 5/2026 đã thanh toán
(1, 5, 2026, 800000, 150000, 100000, N'PAID',   N'Đã thu', 2, '2026-05-02', '2026-05-10'),
(2, 5, 2026, 900000, 180000, 200000, N'PAID',   NULL,      2, '2026-05-02', '2026-05-12'),
(3, 5, 2026, 800000, 120000, 100000, N'PAID',   NULL,      2, '2026-05-02', '2026-05-08'),
-- Tháng 6/2026 đã công bố
(1, 6, 2026, 800000, 160000, 100000, N'UNPAID', N'Chưa đóng', 2, '2026-06-02', NULL),
(2, 6, 2026, 900000, 190000, 200000, N'PAID',   NULL,         2, '2026-06-02', '2026-06-15'),
(3, 6, 2026, 800000, 130000, 100000, N'UNPAID', NULL,         2, '2026-06-02', NULL),
(4, 6, 2026, 1000000,200000, 150000, N'UNPAID', NULL,         2, '2026-06-02', NULL),
-- Tháng 7/2026 draft (chưa gửi cư dân) → manager draftFees=2
(1, 7, 2026, 800000, 155000, 100000, N'DRAFT',  N'Đang nhập', 2, NULL, NULL),
(2, 7, 2026, 900000, 175000, 200000, N'DRAFT',  NULL,         3, NULL, NULL);
GO

/*--------------------------------------------------------------------------------
  REQUESTS + HISTORY – đủ trạng thái demo dashboard 4 role
  #1 COMPLETED by staff(3) trong 7 ngày → staff.completedWeek ≥ 1
  #2 IN_PROGRESS by staff2(4)           → staff2.inProgress + manager.processing
  #3 PENDING by tenant1                 → manager.pending = 1
  #4 ASSIGNED to staff(3)               → staff.assignedJobs = 1 + manager.processing
  #5 REJECTED
  #6 CANCELLED by resident1
  resident1 openRequests = chỉ #2 (IN_PROGRESS) = 1
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT requests ON;

INSERT INTO requests (
    request_id, apartment_id, created_by, request_type, title, description,
    location_detail, urgency, vehicle_type, plate_number,
    scheduled_at, move_note, status, reject_reason,
    assigned_to, approved_by, approved_at, completed_at, created_at
)
VALUES
-- 1. Sửa chữa đã hoàn thành (staff id=3) — completed_at trong 7 ngày gần đây
(1, 1, 5, N'REPAIR',
 N'Sửa vòi nước bếp', N'Vòi nước bếp bị rò rỉ liên tục',
 N'Bếp', N'MEDIUM', NULL, NULL, NULL, NULL,
 N'COMPLETED', NULL, 3, 2,
 DATEADD(day, -3, SYSUTCDATETIME()),
 DATEADD(day, -1, SYSUTCDATETIME()),
 DATEADD(day, -4, SYSUTCDATETIME())),

-- 2. Trông xe đang xử lý (staff2 id=4) — open request của resident1
(2, 1, 5, N'PARKING',
 N'Đăng ký gửi xe máy', N'Đăng ký thêm 1 chỗ gửi xe máy tháng 7',
 NULL, NULL, N'Xe máy', N'59X1-12345', NULL, NULL,
 N'IN_PROGRESS', NULL, 4, 2,
 DATEADD(day, -10, SYSUTCDATETIME()), NULL,
 DATEADD(day, -12, SYSUTCDATETIME())),

-- 3. Chuyển đồ vào chờ duyệt
(3, 2, 7, N'MOVE_IN',
 N'Chuyển đồ vào căn A-0802', N'Chuyển sofa + tủ lạnh vào căn hộ',
 NULL, NULL, NULL, NULL, DATEADD(day, 3, SYSUTCDATETIME()), N'Dự kiến 2 tiếng, có xe tải nhỏ',
 N'PENDING', NULL, NULL, NULL, NULL, NULL,
 DATEADD(day, -2, SYSUTCDATETIME())),

-- 4. Sửa chữa đã gán nhân viên (staff id=3)
(4, 3, 8, N'REPAIR',
 N'Hỏng đèn hành lang trong căn', N'Đèn phòng khách không sáng',
 N'Phòng khách', N'LOW', NULL, NULL, NULL, NULL,
 N'ASSIGNED', NULL, 3, 2,
 DATEADD(day, -2, SYSUTCDATETIME()), NULL,
 DATEADD(day, -3, SYSUTCDATETIME())),

-- 5. Chuyển đồ ra bị từ chối (sai giờ)
(5, 4, 9, N'MOVE_OUT',
 N'Chuyển đồ ra ngoài giờ', N'Xin chuyển đồ lúc 22:00',
 NULL, NULL, NULL, NULL, DATEADD(day, -5, SYSUTCDATETIME()), N'Gấp',
 N'REJECTED', N'Ngoài khung giờ cho phép (08:00–17:00). Vui lòng đăng ký lại.',
 NULL, 2, DATEADD(day, -6, SYSUTCDATETIME()), NULL,
 DATEADD(day, -7, SYSUTCDATETIME())),

-- 6. Request bị cư dân hủy
(6, 1, 5, N'REPAIR',
 N'Sửa ổ cắm (đã tự xử lý)', N'Ổ cắm phòng ngủ lỏng – đã tự thay',
 N'Phòng ngủ', N'LOW', NULL, NULL, NULL, NULL,
 N'CANCELLED', NULL, NULL, NULL, NULL, NULL,
 DATEADD(day, -5, SYSUTCDATETIME()));

SET IDENTITY_INSERT requests OFF;
GO

INSERT INTO request_history (request_id, changed_by, old_status, new_status, note, created_at)
VALUES
-- Request 1 (hoàn thành gần đây)
(1, 5, NULL,           N'PENDING',     N'Cư dân gửi yêu cầu',               DATEADD(day, -4, SYSUTCDATETIME())),
(1, 2, N'PENDING',     N'APPROVED',    N'Phê duyệt sửa chữa',               DATEADD(day, -3, SYSUTCDATETIME())),
(1, 2, N'APPROVED',    N'ASSIGNED',    N'Gán cho staff kỹ thuật',           DATEADD(hour, -70, SYSUTCDATETIME())),
(1, 3, N'ASSIGNED',    N'IN_PROGRESS', N'Đã xuống kiểm tra, đang thay vòi', DATEADD(day, -2, SYSUTCDATETIME())),
(1, 3, N'IN_PROGRESS', N'COMPLETED',   N'Hoàn tất, đã test không rò',       DATEADD(day, -1, SYSUTCDATETIME())),
-- Request 2
(2, 5, NULL,           N'PENDING',     N'Đăng ký trông xe',                 DATEADD(day, -12, SYSUTCDATETIME())),
(2, 2, N'PENDING',     N'APPROVED',    N'Đồng ý thêm chỗ gửi xe',           DATEADD(day, -10, SYSUTCDATETIME())),
(2, 2, N'APPROVED',    N'ASSIGNED',    N'Gán lễ tân xử lý thẻ xe',          DATEADD(hour, -238, SYSUTCDATETIME())),
(2, 4, N'ASSIGNED',    N'IN_PROGRESS', N'Đang làm thẻ gửi xe',              DATEADD(day, -9, SYSUTCDATETIME())),
-- Request 3
(3, 7, NULL,           N'PENDING',     N'Đăng ký chuyển đồ vào',            DATEADD(day, -2, SYSUTCDATETIME())),
-- Request 4
(4, 8, NULL,           N'PENDING',     N'Báo hỏng đèn',                     DATEADD(day, -3, SYSUTCDATETIME())),
(4, 2, N'PENDING',     N'APPROVED',    N'Duyệt',                            DATEADD(day, -2, SYSUTCDATETIME())),
(4, 2, N'APPROVED',    N'ASSIGNED',    N'Gán kỹ thuật',                     DATEADD(hour, -46, SYSUTCDATETIME())),
-- Request 5
(5, 9, NULL,           N'PENDING',     N'Đăng ký chuyển đồ ra',             DATEADD(day, -7, SYSUTCDATETIME())),
(5, 2, N'PENDING',     N'REJECTED',    N'Ngoài khung giờ quy định',         DATEADD(day, -6, SYSUTCDATETIME())),
-- Request 6
(6, 5, NULL,           N'PENDING',     N'Gửi yêu cầu',                      DATEADD(day, -5, SYSUTCDATETIME())),
(6, 5, N'PENDING',     N'CANCELLED',   N'Cư dân tự hủy – đã tự xử lý',      DATEADD(day, -5, DATEADD(hour, 5, SYSUTCDATETIME())));
GO

/*--------------------------------------------------------------------------------
  ANNOUNCEMENTS
  - 4 bản published trong 30 ngày → home public + resident newAnnouncements ≥ 3
  - 1 bản unpublished → không hiện public / không đếm dashboard
--------------------------------------------------------------------------------*/
INSERT INTO announcements (title, content, category, is_published, created_by, published_at)
VALUES
(N'Nội quy chuyển đồ vào/ra',
 N'Cư dân muốn chuyển đồ vào hoặc ra bắt buộc đăng ký trước với ban quản lý trên hệ thống.
Khung giờ cho phép: 08:00 – 17:00 các ngày trong tuần (T2–T7).
Không chuyển đồ sau 17:00 và ngày Chủ nhật (trừ trường hợp khẩn cấp có phê duyệt).
Xe tải chỉ được vào khu vực sảnh B theo hướng dẫn bảo vệ.',
 N'MOVE_RULE', 1, 1, DATEADD(day, -20, SYSUTCDATETIME())),

(N'Lịch thu phí dịch vụ tháng hiện tại',
 N'Ban quản lý sẽ công bố phí dịch vụ, nước, trông xe trước ngày 05 hàng tháng.
Cư dân vui lòng đăng nhập hệ thống để xem chi tiết và thanh toán đúng hạn.',
 N'FEE', 1, 2, DATEADD(day, -5, SYSUTCDATETIME())),

(N'Bảo trì hệ thống nước tầng 8–9',
 N'Dự kiến bảo trì đường ống nước tầng 8 và 9 trong khung 08:00–12:00.
Trong thời gian này có thể mất nước tạm thời. Mong cư dân thông cảm.',
 N'MAINTENANCE', 1, 2, DATEADD(day, -2, SYSUTCDATETIME())),

(N'Chào mừng đến hệ thống quản lý chung cư',
 N'Đây là cổng đăng ký yêu cầu sửa chữa, trông xe, chuyển đồ và xem phí hàng tháng (sau đăng nhập).
Mọi thắc mắc liên hệ ban quản lý: hotline 1900-xxxx hoặc quầy lễ tân tầng 1.',
 N'GENERAL', 1, 1, DATEADD(day, -1, SYSUTCDATETIME())),

-- Bản nháp / chưa publish — không hiện trang chủ public
(N'[NHÁP] Thông báo nội bộ ban quản lý',
 N'Nội dung nháp – không công bố cư dân.',
 N'GENERAL', 0, 1, NULL);
GO

/*--------------------------------------------------------------------------------
  SYSTEM_SETTINGS
--------------------------------------------------------------------------------*/
INSERT INTO system_settings (setting_key, setting_value, description, updated_by, updated_at)
VALUES
(N'move.time.start',    N'08:00', N'Giờ bắt đầu được phép chuyển đồ', 1, SYSUTCDATETIME()),
(N'move.time.end',      N'17:00', N'Giờ kết thúc được phép chuyển đồ', 1, SYSUTCDATETIME()),
(N'move.allowed.days',  N'2,3,4,5,6,7', N'Ngày trong tuần được chuyển đồ (2=T2 ... 8=CN)', 1, SYSUTCDATETIME()),
(N'site.name',          N'Skyland Apartment', N'Tên hiển thị trên web', 1, SYSUTCDATETIME()),
(N'site.contact_phone', N'1900 2868', N'Hotline ban quản lý', 1, SYSUTCDATETIME()),
(N'site.contact_email', N'bql@skyland.vn', N'Email ban quản lý', 1, SYSUTCDATETIME()),
(N'site.address',       N'Tòa nhà Skyland, 88 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', N'Địa chỉ BQL', 1, SYSUTCDATETIME());
GO


/*--------------------------------------------------------------------------------
  RECONCILE OCCUPANCY (TV2 rules) — safety net after seed inserts
--------------------------------------------------------------------------------*/
-- INACTIVE → N/A
UPDATE apartments
SET occupancy_type = N'N/A', updated_at = SYSUTCDATETIME()
WHERE status = N'INACTIVE' AND ISNULL(occupancy_type, N'') <> N'N/A';
GO

-- ACTIVE + tenant → RENTED
UPDATE a SET a.occupancy_type = N'RENTED', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND EXISTS (SELECT 1 FROM apartment_residents r
              WHERE r.apartment_id = a.apartment_id AND r.is_current = 1
                AND r.role_in_apartment IN (N'TENANT_REP', N'TENANT'))
  AND ISNULL(a.occupancy_type, N'') <> N'RENTED';
GO

-- ACTIVE + OWNER only → OWNED
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

-- ACTIVE empty → VACANT
UPDATE a SET a.occupancy_type = N'VACANT', a.updated_at = SYSUTCDATETIME()
FROM apartments a
WHERE a.status = N'ACTIVE'
  AND NOT EXISTS (SELECT 1 FROM apartment_residents r
                  WHERE r.apartment_id = a.apartment_id AND r.is_current = 1)
  AND NOT EXISTS (SELECT 1 FROM household_members hm
                  WHERE hm.apartment_id = a.apartment_id AND hm.is_active = 1)
  AND ISNULL(a.occupancy_type, N'') <> N'VACANT';
GO

/*--------------------------------------------------------------------------------
  VERIFY COUNTS (Messages tab trong SSMS)
  Kỳ vọng:
    users=11 · locked=1 · buildings=3 · apartments=282 (20*6+15*6+12*6)
    inactive ~20% (formula %5=0) · draftFees=2
    pending=1 · processing=2 · staff_assigned=1 · staff_inprog=0 · staff_done7d≥1
    staff2_inprog=1 · resident1_open=1 · ann_published_30d≥3 · ann_unpublished=1
--------------------------------------------------------------------------------*/
PRINT N'========== VERIFY SEED (so với expected) ==========';

SELECT N'users_total' AS metric, COUNT(*) AS value FROM users
UNION ALL SELECT N'users_locked', COUNT(*) FROM users WHERE is_active = 0
UNION ALL SELECT N'buildings', COUNT(*) FROM buildings
UNION ALL SELECT N'buildings_active', COUNT(*) FROM buildings WHERE status = N'ACTIVE'
UNION ALL SELECT N'apartments', COUNT(*) FROM apartments
UNION ALL SELECT N'apartments_A', COUNT(*) FROM apartments WHERE building_id = 1
UNION ALL SELECT N'apartments_B', COUNT(*) FROM apartments WHERE building_id = 2
UNION ALL SELECT N'apartments_C', COUNT(*) FROM apartments WHERE building_id = 3
UNION ALL SELECT N'apartments_active', COUNT(*) FROM apartments WHERE status = N'ACTIVE'
UNION ALL SELECT N'apartments_inactive', COUNT(*) FROM apartments WHERE status = N'INACTIVE'
UNION ALL SELECT N'occ_owned', COUNT(*) FROM apartments WHERE occupancy_type = N'OWNED'
UNION ALL SELECT N'occ_rented', COUNT(*) FROM apartments WHERE occupancy_type = N'RENTED'
UNION ALL SELECT N'occ_vacant', COUNT(*) FROM apartments WHERE occupancy_type = N'VACANT'
UNION ALL SELECT N'occ_na', COUNT(*) FROM apartments WHERE occupancy_type = N'N/A'
UNION ALL SELECT N'draft_fees', COUNT(*) FROM monthly_fees WHERE status = N'DRAFT'
UNION ALL SELECT N'req_pending', COUNT(*) FROM requests WHERE status = N'PENDING'
UNION ALL SELECT N'req_processing', COUNT(*) FROM requests WHERE status IN (N'ASSIGNED', N'IN_PROGRESS')
UNION ALL SELECT N'staff_assigned', COUNT(*) FROM requests WHERE assigned_to = 3 AND status = N'ASSIGNED'
UNION ALL SELECT N'staff_in_progress', COUNT(*) FROM requests WHERE assigned_to = 3 AND status = N'IN_PROGRESS'
UNION ALL SELECT N'staff_completed_7d', COUNT(*) FROM requests
    WHERE assigned_to = 3 AND status = N'COMPLETED'
      AND completed_at >= DATEADD(day, -7, SYSUTCDATETIME())
UNION ALL SELECT N'staff2_in_progress', COUNT(*) FROM requests WHERE assigned_to = 4 AND status = N'IN_PROGRESS'
UNION ALL SELECT N'resident1_open_req', COUNT(*) FROM requests
    WHERE created_by = 5 AND status NOT IN (N'COMPLETED', N'CANCELLED', N'REJECTED')
UNION ALL SELECT N'ann_published_30d', COUNT(*) FROM announcements
    WHERE is_published = 1
      AND (published_at IS NULL OR published_at >= DATEADD(day, -30, SYSUTCDATETIME()))
UNION ALL SELECT N'ann_unpublished', COUNT(*) FROM announcements WHERE is_published = 0;

SELECT TOP 1 a.apartment_code AS resident1_apartment
FROM apartments a
INNER JOIN apartment_residents ar ON a.apartment_id = ar.apartment_id
WHERE ar.user_id = 5 AND ar.is_current = 1;

SELECT TOP 1
    CAST(mf.fee_month AS NVARCHAR(2)) + N'/' + CAST(mf.fee_year AS NVARCHAR(4))
        + N' · ' + CAST(mf.total_amount AS NVARCHAR(20)) + N' đ · ' + mf.status AS resident1_latest_fee
FROM monthly_fees mf
INNER JOIN apartment_residents ar ON mf.apartment_id = ar.apartment_id
WHERE ar.user_id = 5 AND ar.is_current = 1
ORDER BY mf.fee_year DESC, mf.fee_month DESC, mf.fee_id DESC;

PRINT N'Seed data inserted successfully.';
PRINT N'--- Tài khoản demo (password: 123456) ---';
PRINT N'admin | manager | staff | staff2 | resident1 | owner1 | tenant1 | resident2 | resident3 | resident_noapt';
PRINT N'locked_user (is_active=0 — không login được)';
PRINT N'--- Dashboard expected: xem comment đầu file seed.sql ---';
GO
