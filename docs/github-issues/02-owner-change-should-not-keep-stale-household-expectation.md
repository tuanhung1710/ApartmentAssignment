## Title
bug/ux: Đổi chủ sở hữu / không tenant mà Thành viên hộ vẫn còn data cũ

## Labels
`bug`, `enhancement`, `apartment`, `household-member`, `uc-apt-06`

## Body

### Mô tả (Vibe annotation #2)
**Page:** `/ApartmentManagement/apartment?action=detail&id=4`  
Sau **đổi chủ sở hữu**, **không có người thuê**, block **Thành viên hộ** vẫn còn thành viên cũ.

### Root cause (thiết kế ban đầu)
3 nguồn data độc lập:
- Owner → `apartment_residents` (OWNER)
- Tenant → `apartment_residents` (TENANT*)
- Thành viên hộ → `household_members`

Gán owner **không** đồng bộ household (trước fix).

### Expected
- Đổi owner: clear/xử lý nhân khẩu cũ; đồng bộ dòng **Chủ hộ** theo owner mới
- Gỡ owner: **chỉ** gỡ gán OWNER; TV gỡ **riêng** bằng nút Xóa (không cùng lúc)

### Code liên quan
- `handleAssignOwner` (đổi owner → softDeleteAllActive + ensureActiveMember Chủ hộ)
- `handleRemoveOwner` (chỉ end owner, không xóa TV)
- `detail.jsp` block Thành viên hộ

### Acceptance
- [ ] Đổi owner không giữ TV “gắn owner cũ” gây hiểu nhầm
- [ ] Gỡ owner ≠ xóa TV cùng lúc
- [ ] User có thể Xóa TV thủ công sau
