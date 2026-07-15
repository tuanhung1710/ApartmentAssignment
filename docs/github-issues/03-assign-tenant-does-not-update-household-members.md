## Title
bug/ux: Gán người thuê không cập nhật Thành viên hộ

## Labels
`bug`, `enhancement`, `apartment`, `uc-apt-07`, `household-member`

## Body

### Mô tả (Vibe annotation #3)
**Page:** detail `id=4`  
Gán **Người thuê** xong, block Người thuê có data nhưng **Thành viên hộ** không đổi.

### Root cause
`handleAssignTenant` chỉ ghi `apartment_residents`, không ghi `household_members` (trước fix).

### Expected
- Gán TENANT / TENANT_REP → đồng bộ 1 dòng TV:
  - quan hệ `Người thuê` / `Đại diện thuê`
  - fullName + phone từ user
- Detail hiện cả block thuê + TV

### Code
- `ApartmentController.handleAssignTenant`
- `HouseholdMemberDAO.ensureActiveMember`
- `detail.jsp`

### Acceptance
- [ ] Gán thuê → TV có dòng tương ứng
- [ ] Gán lại cùng user+role không nhân bản vô hạn
