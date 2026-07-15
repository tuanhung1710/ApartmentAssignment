## Title
bug: Gỡ thành viên phải xóa khỏi danh sách Thành viên hộ

## Labels
`bug`, `apartment`, `uc-apt-09`, `household-member`

## Body

### Mô tả (Vibe annotation #5)
**Page:** detail `id=4`  
Gỡ thành viên (vd. **Bùi Quốc An**) phải **biến mất** khỏi Thành viên hộ.

### Root cause (spec cũ)
- UC-09 dùng **soft delete** `is_active=0`
- Detail list cả inactive → vẫn thấy tên

### Expected (feedback)
- Gỡ/Xóa → **hard delete** row (hoặc ẩn hẳn khỏi list default)
- Confirm: xóa khỏi danh sách, không hoàn tác

### Code
- `handleRemoveMember` → `hardDelete`
- `findActiveByApartmentId` / `findByApartmentId` (detail chỉ active)
- `detail.jsp` nút **Xóa**

### Acceptance
- [ ] Sau Xóa, tên không còn trên detail
- [ ] History/audit REMOVE_MEMBER vẫn ghi được
- [ ] Gỡ owner **không** tự xóa TV (thao tác riêng)
