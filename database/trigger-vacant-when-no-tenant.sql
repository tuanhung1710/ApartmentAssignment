/*
  Optional DB helpers for tenant contracts.

  Rule hiện tại (coding-standards / detail UI):
  - Căn RENTED khi gỡ/đổi thuê → GIỮ occupancy RENTED
  - Detail vẫn hiện card Chủ nhà + Người thuê (ô trống) để gán lại
  - KHÔNG ép RENTED trống về VACANT (sẽ mất chỗ ghi thuê trên UI)

  File này chỉ còn:
  1) usp_ExpirePastDueTenants — đóng is_current khi end_date < hôm nay
  2) DROP trigger cũ (nếu từng cài) để không còn ép VACANT

  KHÔNG dùng GO — CREATE PROC qua EXEC(N'...').
*/

USE ApartmentManagement;

/*----- 1) Đóng hợp đồng thuê quá hạn -----*/
IF OBJECT_ID(N'dbo.usp_ExpirePastDueTenants', N'P') IS NOT NULL
    DROP PROCEDURE dbo.usp_ExpirePastDueTenants;

EXEC(N'
CREATE PROCEDURE dbo.usp_ExpirePastDueTenants
AS
BEGIN
    SET NOCOUNT ON;

    DELETE h
    FROM apartment_residents h
    INNER JOIN apartment_residents c
        ON c.apartment_id = h.apartment_id
       AND c.user_id = h.user_id
       AND c.role_in_apartment = h.role_in_apartment
    WHERE h.is_current = 0
      AND c.is_current = 1
      AND c.role_in_apartment IN (N''TENANT_REP'', N''TENANT'')
      AND c.end_date IS NOT NULL
      AND c.end_date < CAST(SYSUTCDATETIME() AS DATE);

    UPDATE apartment_residents
    SET is_current = 0
    WHERE is_current = 1
      AND role_in_apartment IN (N''TENANT_REP'', N''TENANT'')
      AND end_date IS NOT NULL
      AND end_date < CAST(SYSUTCDATETIME() AS DATE);
END
');

/*----- 2) Gỡ proc/trigger cũ ép RENTED → VACANT (nếu đã cài) -----*/
IF OBJECT_ID(N'dbo.usp_MarkVacantActiveWhenNoTenants', N'P') IS NOT NULL
    DROP PROCEDURE dbo.usp_MarkVacantActiveWhenNoTenants;

IF OBJECT_ID(N'dbo.tr_apartment_residents_vacant_when_no_tenant', N'TR') IS NOT NULL
    DROP TRIGGER dbo.tr_apartment_residents_vacant_when_no_tenant;

/*----- 3) Chạy expire một lần -----*/
EXEC dbo.usp_ExpirePastDueTenants;
