## Title
bug: Đổi chủ sở hữu / không còn người thuê mà Thành viên hộ vẫn còn data cũ

## Labels
`bug`, `apartment`, `household-member`, `uc-apt-06`

## Body
### Vibe annotation #2
- **Page:** `/ApartmentManagement/apartment?action=detail&id=4`
- **Block:** Thành viên hộ
- **Hiện tượng:** Đổi chủ sở hữu xong, **không có người thuê**, nhưng danh sách thành viên hộ **vẫn còn** người cũ.

### Phân tích (code hiện tại)
3 nguồn data độc lập:
| Nguồn | Bảng |
|-------|------|
| Owner / chủ nhà | `apartment_residents` OWNER |
| Tenant | `apartment_residents` TENANT* |
| Thành viên hộ | `household_members` |

`handleAssignOwner` (OWNED):
- Sync **1 dòng Chủ hộ** theo owner mới (`ensureActiveMember`)
- Có thể gỡ dòng Chủ hộ owner cũ theo tên
- **Không** clear toàn bộ TV còn lại khi đổi owner / khi căn không còn tenant

→ Nhân khẩu cũ (Thành viên) vẫn nằm trên detail → user hiểu nhầm “vẫn còn người ở”.

### Expected (theo feedback)
Khi **đổi owner** và căn **không còn người thuê** (OWNED / trống thuê):
- Xử lý nhân khẩu cũ (clear soft/hard theo BR chốt)
- Đồng bộ dòng **Chủ hộ** theo owner mới (OWNED)
- RENTED: landlord **không** vào TV (đã có rule riêng)

### Code
- `ApartmentController.handleAssignOwner`
- `HouseholdMemberDAO.softDeleteAllActiveByApartment` / `ensureActiveMember` / `hardDeleteByNameAndRelationship`
- `detail.jsp` block Thành viên hộ

### Acceptance
- [ ] Đổi owner (không tenant) → TV không còn “gắn hộ cũ” gây hiểu nhầm
- [ ] OWNED: owner mới xuất hiện TV với vai trò Chủ hộ
- [ ] History/audit ghi rõ clear + sync TV
