-- Đảm bảo bảng lịch sử + (tuỳ chọn) xem log
USE ApartmentManagement;
GO

IF OBJECT_ID(N'dbo.apartment_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.apartment_history (
        history_id    INT IDENTITY(1,1) PRIMARY KEY,
        apartment_id  INT NOT NULL,
        action        NVARCHAR(50) NOT NULL,
        old_status    NVARCHAR(20) NULL,
        new_status    NVARCHAR(20) NULL,
        note          NVARCHAR(500) NULL,
        actor_user_id INT NULL,
        actor_name    NVARCHAR(100) NULL,
        created_at    DATETIME2 NOT NULL CONSTRAINT DF_ah_created2 DEFAULT (SYSUTCDATETIME())
    );
    PRINT 'Created apartment_history';
END
ELSE
    PRINT 'apartment_history already exists';
GO

-- Kiểm tra nhanh
SELECT TOP 20 * FROM dbo.apartment_history ORDER BY history_id DESC;
GO
