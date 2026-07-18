## Title
bug: Gán người thuê mà Thành viên hộ không đổi / không thấy người vừa gán

## Labels
`bug`, `apartment`, `uc-apt-07`, `household-member`

## Body
### Vibe annotation #3
- **Page:** detail `id=4`
- **Block:** Người thuê + Thành viên hộ
- **Hiện tượng:** Gán người thuê xong, block Người thuê có thể có data nhưng **Thành viên hộ không thay đổi** (không thấy người vừa gán).

### Phân tích (code hiện tại)
`handleAssignTenant` **đã gọi** `householdMemberDAO.ensureActiveMember(..., "Thành viên", phone)` sau insert tenant.

Nếu UI vẫn không đổi, có thể:
1. `ensureActiveMember` return `0` vì `existsActiveByFullName` (trùng tên đã có) — user không thấy “thay đổi”
2. Insert TV fail im lặng (`memberSync < 0`) chỉ cảnh báo nhẹ trên flash
3. Form search mới gửi sai `userId`/`memberId` → gán user khác / fail
4. Detail chỉ list `is_active=1` — nếu soft-delete trước đó gây lệch tên

### Expected
- Gán TENANT / TENANT_REP → **luôn** có đúng người đó trong Thành viên hộ (vai trò `Thành viên`)
- Chưa có → insert; đã có → không nhân bản + flash rõ
- Fail sync TV → error/warn rõ, không im lặng

### Code
- `ApartmentController.handleAssignTenant`
- `HouseholdMemberDAO.ensureActiveMember` / `existsActiveByFullName`
- `assign-tenant.jsp` (search select hidden fields)
- `detail.jsp`

### Acceptance
- [ ] Gán thuê user mới → TV xuất hiện ngay trên detail
- [ ] Gán lại cùng người không tạo 2 dòng active trùng tên
- [ ] Flash nêu đã thêm TV / đã có sẵn / lỗi sync
