## Title
bug: Gán/đổi chủ sở hữu báo lỗi (assign-owner id=4)

## Labels
`bug`, `apartment`, `uc-apt-06`

## Body

### Mô tả (Vibe annotation #1)
**Page:** `/ApartmentManagement/apartment?action=assign-owner&id=4`  
**Element:** `div[role="alert"]`  
**Message:** `Không thể gán chủ sở hữu. Vui lòng thử lại.` (hoặc message lỗi SQL chi tiết sau fix)

### Root cause
Luồng: `handleAssignOwner` → `endCurrentOwners` / `insertOwner`.

Thường gặp:
1. Bảng `apartment_residents` **chưa tạo** → chạy `database/apartment-detail-tables.sql`
2. DB connection fail / FK user không tồn tại
3. Lỗi SQL bị che bằng message generic (đã cải thiện `getLastError()`)

### Code
- `ApartmentController.handleAssignOwner`
- `ApartmentResidentDAO.insertOwner` / `endCurrentOwners` / `getLastError`

### Expected
- Gán/đổi owner thành công khi DB đủ bảng + user active
- Lỗi DB hiện message rõ (thiếu bảng / FK), không chỉ generic

### Acceptance
- [ ] Gán owner lần đầu OK
- [ ] Đổi owner: old `is_current=0`, new current
- [ ] Thiếu bảng → message hướng dẫn chạy SQL
