## Title
bug: Lịch sử không cập nhật khi gán owner / người thuê / …

## Labels
`bug`, `apartment`, `history`, `uc-apt-05`

## Body

### Mô tả (Vibe annotation #4)
**Page:** detail `id=4` — block **Lịch sử**  
Sau gán/đổi owner, gán thuê… không thấy history đầy đủ.

### Root cause
1. Bảng `apartment_history` có thể chưa tạo (`apartment-detail-tables.sql`)
2. `writeHistory` trước đây nuốt lỗi im lặng
3. Detail fallback chỉ CREATE/UPDATE từ cột apartment khi list history rỗng

### Expected
- ASSIGN_OWNER / CHANGE_OWNER / REMOVE_OWNER / ASSIGN_TENANT / CHANGE_TENANT_REP / ADD_MEMBER / … hiện trên Lịch sử
- History fail → log + (optional) cảnh báo trên flash

### Code
- `writeHistory` (return boolean)
- `ApartmentHistoryDAO.insert` / `getLastError`
- handlers assign-owner / assign-tenant / remove-owner / members

### Acceptance
- [ ] Chạy SQL history table
- [ ] Gán owner/thuê → có dòng history trên detail
- [ ] Không fail im lặng hoàn toàn
