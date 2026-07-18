## Title
bug: Gán/đổi chủ sở hữu báo "Không thể gán chủ sở hữu. Vui lòng thử lại."

## Labels
`bug`, `apartment`, `uc-apt-06`

## Body
### Vibe annotation #1
- **Page:** `/ApartmentManagement/apartment?action=assign-owner&id=4`
- **Selector:** `div[role="alert"]`
- **Message:** `Không thể gán chủ sở hữu. Vui lòng thử lại.`

### Phân tích (code hiện tại)
Luồng: `ApartmentController.handleAssignOwner` → `endCurrentOwners` / `deleteCurrent*` / `insertOwner`.

Message generic khi `insertOwner` / end owner trả `< 0`. Nguyên nhân hay gặp:
1. **DB chưa đủ bảng** `apartment_residents` → chạy `database/schema.sql` + `seed.sql`
2. **UNIQUE** `UQ_ar_apartment_user_role` (apt + user + role + is_current) khi đổi owner / gán lại
3. **FK user** không tồn tại / user inactive
4. Form UX mới (search select) gửi thiếu `userId` / `personSource` → fail validate hoặc insert

DAO đã có `getLastError()` — UI đôi khi vẫn chỉ hiện generic nếu controller không forward `detail`.

### Code
- `ApartmentController.handleAssignOwner` / `handleAssignOwnerForm`
- `ApartmentResidentDAO.insertOwner` / `endCurrentOwners` / `deleteCurrentOwners` / `getLastError`
- `web/.../assign-owner.jsp` (search + hidden fields)

### Expected
- Gán/đổi owner thành công khi DB OK + user active
- Lỗi DB hiện message rõ (thiếu bảng / UNIQUE / FK), không chỉ generic
- Form search luôn gửi đủ `userId` hoặc `personSource=new` + fields

### Acceptance
- [ ] Gán owner lần đầu OK (OWNED)
- [ ] Đổi owner: old closed/deleted current, new current
- [ ] RENTED: gán chủ nhà (landlord) OK, không bắt TV hộ
- [ ] Fail → flash/error chi tiết từ `getLastError()`
