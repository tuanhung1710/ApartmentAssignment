/*
================================================================================
  Seed data demo - ApartmentManagement
  Chạy SAU schema.sql
  Password demo: 123456 (plain text cho MVP PRJ301; production nên hash)
================================================================================
*/

USE ApartmentManagement;
GO

/*--------------------------------------------------------------------------------
  USERS
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT users ON;

INSERT INTO users (user_id, username, password, full_name, email, phone, role, department, is_active)
VALUES
(1, N'admin',     N'123456', N'Quản trị hệ thống',   N'admin@chungcu.vn',    N'0901000001', N'ADMIN',    NULL,           1),
(2, N'manager',   N'123456', N'Nguyễn Văn Quản',     N'manager@chungcu.vn',  N'0901000002', N'MANAGER',  NULL,           1),
(3, N'staff',     N'123456', N'Trần Thị Kỹ Thuật',   N'staff@chungcu.vn',    N'0901000003', N'STAFF',    N'Kỹ thuật',    1),
(4, N'staff2',    N'123456', N'Lê Văn Lễ Tân',       N'staff2@chungcu.vn',   N'0901000004', N'STAFF',    N'Lễ tân',      1),
(5, N'resident1', N'123456', N'Phạm Minh Cư',        N'resident1@email.com', N'0902000001', N'RESIDENT', NULL,           1),
(6, N'owner1',    N'123456', N'Hoàng Chủ Hộ',        N'owner1@email.com',    N'0902000002', N'RESIDENT', NULL,           1),
(7, N'tenant1',   N'123456', N'Võ Người Thuê',       N'tenant1@email.com',   N'0902000003', N'RESIDENT', NULL,           1),
(8, N'resident2', N'123456', N'Đặng Thị Lan',        N'resident2@email.com', N'0902000004', N'RESIDENT', NULL,           1),
(9, N'resident3', N'123456', N'Bùi Quốc An',         N'resident3@email.com', N'0902000005', N'RESIDENT', NULL,           1);

SET IDENTITY_INSERT users OFF;
GO

/*--------------------------------------------------------------------------------
  APARTMENTS
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT apartments ON;

INSERT INTO apartments (apartment_id, apartment_code, building, floor_number, area_m2, occupancy_type, status, notes)
VALUES
(1, N'A-0801', N'A', 8,  65.50, N'OWNED',  N'ACTIVE', N'Chủ ở'),
(2, N'A-0802', N'A', 8,  72.00, N'RENTED', N'ACTIVE', N'Cho thuê – có chủ + người đại diện thuê'),
(3, N'A-0901', N'A', 9,  65.50, N'OWNED',  N'ACTIVE', NULL),
(4, N'A-1005', N'A', 10, 90.00, N'OWNED',  N'ACTIVE', NULL),
(5, N'A-1201', N'A', 12, 75.00, N'RENTED', N'ACTIVE', NULL),
(6, N'B-0302', N'B', 3,  55.00, N'OWNED',  N'INACTIVE', N'Bảo trì – demo inactive');

SET IDENTITY_INSERT apartments OFF;
GO

/*--------------------------------------------------------------------------------
  APARTMENT_RESIDENTS
  - A-0801: resident1 là OWNER
  - A-0802: owner1 = OWNER, tenant1 = TENANT_REP (case thuê)
  - A-0901: resident2 OWNER
  - A-1005: resident3 OWNER
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
(1, N'Phạm Minh Cư',     N'Chủ hộ',   N'0902000001', N'001099000111', '1990-05-12', 1),
(1, N'Nguyễn Thị Hoa',   N'Vợ',       N'0902000011', N'001099000112', '1992-08-20', 1),
(1, N'Phạm Bé Na',       N'Con',      NULL,          NULL,            '2018-01-05', 1),
(2, N'Võ Người Thuê',    N'Người thuê đại diện', N'0902000003', N'001088000221', '1988-11-03', 1),
(2, N'Võ Bé Bắp',        N'Con',      NULL,          NULL,            '2016-07-19', 1),
(3, N'Đặng Thị Lan',     N'Chủ hộ',   N'0902000004', N'001095000331', '1995-02-14', 1),
(4, N'Bùi Quốc An',      N'Chủ hộ',   N'0902000005', N'001091000441', '1991-09-30', 1);
GO

/*--------------------------------------------------------------------------------
  MONTHLY_FEES - vài tháng demo
--------------------------------------------------------------------------------*/
INSERT INTO monthly_fees
    (apartment_id, fee_month, fee_year, service_fee, water_fee, parking_fee, status, note, created_by, published_at, paid_at)
VALUES
-- Tháng 5/2026 đã thanh toán
(1, 5, 2026, 800000, 150000, 100000, N'PAID',      N'Đã thu', 2, '2026-05-02', '2026-05-10'),
(2, 5, 2026, 900000, 180000, 200000, N'PAID',      NULL,      2, '2026-05-02', '2026-05-12'),
(3, 5, 2026, 800000, 120000, 100000, N'PAID',      NULL,      2, '2026-05-02', '2026-05-08'),
-- Tháng 6/2026 đã công bố, chưa TT / đã TT
(1, 6, 2026, 800000, 160000, 100000, N'UNPAID',    N'Chưa đóng', 2, '2026-06-02', NULL),
(2, 6, 2026, 900000, 190000, 200000, N'PAID',      NULL,         2, '2026-06-02', '2026-06-15'),
(3, 6, 2026, 800000, 130000, 100000, N'UNPAID',    NULL,         2, '2026-06-02', NULL),
(4, 6, 2026, 1000000,200000, 150000, N'UNPAID',    NULL,         2, '2026-06-02', NULL),
-- Tháng 7/2026 draft (chưa gửi cư dân)
(1, 7, 2026, 800000, 155000, 100000, N'DRAFT',     N'Đang nhập', 2, NULL, NULL),
(2, 7, 2026, 900000, 175000, 200000, N'DRAFT',     NULL,         3, NULL, NULL);
GO

/*--------------------------------------------------------------------------------
  REQUESTS + HISTORY - đủ trạng thái để demo
--------------------------------------------------------------------------------*/
SET IDENTITY_INSERT requests ON;

INSERT INTO requests (
    request_id, apartment_id, created_by, request_type, title, description,
    location_detail, urgency, vehicle_type, plate_number,
    scheduled_at, move_note, status, reject_reason,
    assigned_to, approved_by, approved_at, completed_at, created_at
)
VALUES
-- 1. Sửa chữa đã hoàn thành
(1, 1, 5, N'REPAIR',
 N'Sửa vòi nước bếp', N'Vòi nước bếp bị rò rỉ liên tục',
 N'Bếp', N'MEDIUM', NULL, NULL, NULL, NULL,
 N'COMPLETED', NULL, 3, 2, '2026-06-10T09:00:00', '2026-06-12T16:00:00', '2026-06-09T08:30:00'),

-- 2. Trông xe đang xử lý
(2, 1, 5, N'PARKING',
 N'Đăng ký gửi xe máy', N'Đăng ký thêm 1 chỗ gửi xe máy tháng 7',
 NULL, NULL, N'Xe máy', N'59X1-12345', NULL, NULL,
 N'IN_PROGRESS', NULL, 4, 2, '2026-06-20T10:00:00', NULL, '2026-06-19T14:00:00'),

-- 3. Chuyển đồ vào chờ duyệt
(3, 2, 7, N'MOVE_IN',
 N'Chuyển đồ vào căn A-0802', N'Chuyển sofa + tủ lạnh vào căn hộ',
 NULL, NULL, NULL, NULL, '2026-07-15T09:00:00', N'Dự kiến 2 tiếng, có xe tải nhỏ',
 N'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-07-08T11:00:00'),

-- 4. Sửa chữa đã gán nhân viên
(4, 3, 8, N'REPAIR',
 N'Hỏng đèn hành lang trong căn', N'Đèn phòng khách không sáng',
 N'Phòng khách', N'LOW', NULL, NULL, NULL, NULL,
 N'ASSIGNED', NULL, 3, 2, '2026-07-05T08:00:00', NULL, '2026-07-04T17:20:00'),

-- 5. Chuyển đồ ra bị từ chối (sai giờ)
(5, 4, 9, N'MOVE_OUT',
 N'Chuyển đồ ra ngoài giờ', N'Xin chuyển đồ lúc 22:00',
 NULL, NULL, NULL, NULL, '2026-07-01T22:00:00', N'Gấp',
 N'REJECTED', N'Ngoài khung giờ cho phép (08:00–17:00). Vui lòng đăng ký lại.',
 NULL, 2, '2026-06-28T09:00:00', NULL, '2026-06-27T20:00:00'),

-- 6. Request bị cư dân hủy
(6, 1, 5, N'REPAIR',
 N'Sửa ổ cắm (đã tự xử lý)', N'Ổ cắm phòng ngủ lỏng – đã tự thay',
 N'Phòng ngủ', N'LOW', NULL, NULL, NULL, NULL,
 N'CANCELLED', NULL, NULL, NULL, NULL, NULL, '2026-07-01T07:00:00');

SET IDENTITY_INSERT requests OFF;
GO

INSERT INTO request_history (request_id, changed_by, old_status, new_status, note, created_at)
VALUES
-- Request 1
(1, 5, NULL,          N'PENDING',     N'Cư dân gửi yêu cầu',              '2026-06-09T08:30:00'),
(1, 2, N'PENDING',    N'APPROVED',    N'Phê duyệt sửa chữa',              '2026-06-10T09:00:00'),
(1, 2, N'APPROVED',   N'ASSIGNED',    N'Gán cho staff kỹ thuật',          '2026-06-10T09:05:00'),
(1, 3, N'ASSIGNED',   N'IN_PROGRESS', N'Đã xuống kiểm tra, đang thay vòi','2026-06-11T10:00:00'),
(1, 3, N'IN_PROGRESS',N'COMPLETED',   N'Hoàn tất, đã test không rò',      '2026-06-12T16:00:00'),
-- Request 2
(2, 5, NULL,          N'PENDING',     N'Đăng ký trông xe',                '2026-06-19T14:00:00'),
(2, 2, N'PENDING',    N'APPROVED',    N'Đồng ý thêm chỗ gửi xe',          '2026-06-20T10:00:00'),
(2, 2, N'APPROVED',   N'ASSIGNED',    N'Gán lễ tân xử lý thẻ xe',         '2026-06-20T10:05:00'),
(2, 4, N'ASSIGNED',   N'IN_PROGRESS', N'Đang làm thẻ gửi xe',             '2026-06-21T09:00:00'),
-- Request 3
(3, 7, NULL,          N'PENDING',     N'Đăng ký chuyển đồ vào',           '2026-07-08T11:00:00'),
-- Request 4
(4, 8, NULL,          N'PENDING',     N'Báo hỏng đèn',                    '2026-07-04T17:20:00'),
(4, 2, N'PENDING',    N'APPROVED',    N'Duyệt',                           '2026-07-05T08:00:00'),
(4, 2, N'APPROVED',   N'ASSIGNED',    N'Gán kỹ thuật',                    '2026-07-05T08:10:00'),
-- Request 5
(5, 9, NULL,          N'PENDING',     N'Đăng ký chuyển đồ ra',            '2026-06-27T20:00:00'),
(5, 2, N'PENDING',    N'REJECTED',    N'Ngoài khung giờ quy định',        '2026-06-28T09:00:00'),
-- Request 6
(6, 5, NULL,          N'PENDING',     N'Gửi yêu cầu',                     '2026-07-01T07:00:00'),
(6, 5, N'PENDING',    N'CANCELLED',   N'Cư dân tự hủy – đã tự xử lý',     '2026-07-01T12:00:00');
GO

/*--------------------------------------------------------------------------------
  ANNOUNCEMENTS
--------------------------------------------------------------------------------*/
INSERT INTO announcements (title, content, category, is_published, created_by, published_at)
VALUES
(N'Nội quy chuyển đồ vào/ra',
 N'Cư dân muốn chuyển đồ vào hoặc ra bắt buộc đăng ký trước với ban quản lý trên hệ thống.
Khung giờ cho phép: 08:00 – 17:00 các ngày trong tuần (T2–T7).
Không chuyển đồ sau 17:00 và ngày Chủ nhật (trừ trường hợp khẩn cấp có phê duyệt).
Xe tải chỉ được vào khu vực sảnh B theo hướng dẫn bảo vệ.',
 N'MOVE_RULE', 1, 1, '2026-05-01T08:00:00'),

(N'Lịch thu phí dịch vụ tháng 7/2026',
 N'Ban quản lý sẽ công bố phí dịch vụ, nước, trông xe tháng 7 trước ngày 05/07.
Cư dân vui lòng đăng nhập hệ thống để xem chi tiết và thanh toán đúng hạn.',
 N'FEE', 1, 2, '2026-06-28T09:00:00'),

(N'Bảo trì hệ thống nước tầng 8–9',
 N'Dự kiến 20/07/2026 từ 08:00–12:00 sẽ bảo trì đường ống nước tầng 8 và 9.
Trong thời gian này có thể mất nước tạm thời. Mong cư dân thông cảm.',
 N'MAINTENANCE', 1, 2, '2026-07-05T10:00:00'),

(N'Chào mừng đến hệ thống quản lý chung cư',
 N'Đây là cổng đăng ký yêu cầu sửa chữa, trông xe, chuyển đồ và tra cứu phí hàng tháng.
Mọi thắc mắc liên hệ ban quản lý: hotline 1900-xxxx hoặc quầy lễ tân tầng 1.',
 N'GENERAL', 1, 1, '2026-01-01T00:00:00');
GO

/*--------------------------------------------------------------------------------
  SYSTEM_SETTINGS
--------------------------------------------------------------------------------*/
INSERT INTO system_settings (setting_key, setting_value, description, updated_by, updated_at)
VALUES
(N'move.time.start',      N'08:00', N'Giờ bắt đầu được phép chuyển đồ', 1, SYSUTCDATETIME()),
(N'move.time.end',        N'17:00', N'Giờ kết thúc được phép chuyển đồ', 1, SYSUTCDATETIME()),
(N'move.allowed.days',    N'2,3,4,5,6,7', N'Ngày trong tuần được chuyển đồ (2=T2 ... 8=CN)', 1, SYSUTCDATETIME()),
(N'site.name',            N'Skyland Apartment', N'Tên hiển thị trên web', 1, SYSUTCDATETIME()),
(N'site.contact_phone',   N'1900 2868', N'Hotline ban quản lý', 1, SYSUTCDATETIME()),
(N'site.contact_email',   N'bql@skyland.vn', N'Email ban quản lý', 1, SYSUTCDATETIME()),
(N'site.address',         N'Tòa nhà Skyland, 88 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', N'Địa chỉ BQL', 1, SYSUTCDATETIME());
GO

PRINT N'Seed data inserted successfully.';
PRINT N'--- Tài khoản demo (password: 123456) ---';
PRINT N'admin / manager / staff / staff2 / resident1 / owner1 / tenant1 / resident2 / resident3';
GO
