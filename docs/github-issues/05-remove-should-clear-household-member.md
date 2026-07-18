## Title
bug: Gỡ owner / gỡ thuê / gỡ TV phải xóa khỏi Thành viên hộ

## Labels
`bug`, `apartment`, `household-member`, `uc-apt-09`

## Body
### Vibe annotation #5
- **Page:** detail `id=4`
- **Element:** thành viên vd. **Bùi Quốc An**
- **Hiện tượng:** Thực hiện **gỡ** (gỡ owner / gỡ thuê / gỡ thành viên) nhưng người đó **vẫn còn** (hoặc không bị xóa đúng) trong Thành viên hộ.

### Phân tích (code hiện tại)
| Action | `apartment_residents` | `household_members` |
|--------|----------------------|---------------------|
| `remove-member` | cascade gỡ OWNER/TENANT nếu trùng tên | **hardDelete** TV |
| `remove-owner` | delete current OWNER | **không đụng** TV (message: “TV vẫn giữ”) |
| `remove-tenant` | delete current TENANT* | **không đụng** TV (message: “TV không bị xóa”) |

Feedback: **gỡ phải xóa khỏi thành viên hộ** — rule hiện tại trái feedback (đặc biệt gỡ owner/thuê).

Ngoài ra `edit-member.jsp` vẫn ghi “Soft Delete” trong khi remove-member đã hard delete → UI/docs lệch.

### Expected
1. **Xóa TV** → biến mất khỏi list (hard delete) + cascade role residents nếu cùng người  
2. **Gỡ owner (OWNED)** → gỡ OWNER + xóa TV “Chủ hộ” / trùng tên owner khỏi hộ  
3. **Gỡ thuê** → gỡ TENANT* + xóa TV trùng tên người thuê khỏi hộ  
4. Confirm UI nêu rõ sẽ xóa khỏi thành viên hộ  

### Code
- `handleRemoveOwner` / `handleRemoveTenant` / `handleRemoveMember`
- `HouseholdMemberDAO.hardDelete` / `hardDeleteByNameAndRelationship`
- `detail.jsp` confirm gỡ owner/thuê/TV
- `edit-member.jsp` text soft-delete outdated

### Acceptance
- [ ] Gỡ TV → tên biến mất khỏi Thành viên hộ
- [ ] Gỡ owner (OWNED) → không còn dòng Chủ hộ của owner đó
- [ ] Gỡ thuê → không còn dòng TV của tenant đó
- [ ] History REMOVE_* ghi đủ
