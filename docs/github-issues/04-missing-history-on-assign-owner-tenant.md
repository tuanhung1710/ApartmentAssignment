## Title
bug: Lịch sử không cập nhật khi gán owner / người thuê / gỡ / …

## Labels
`bug`, `apartment`, `history`, `uc-apt-05`

## Body
### Vibe annotation #4
- **Page:** detail `id=4` — block **Lịch sử**
- **Hiện tượng:** Sau gán/đổi owner, gán thuê, gỡ… **không thấy** (đủ) dòng lịch sử.

### Phân tích (code hiện tại)
- Controller gọi `writeHistory(...)` cho ASSIGN/CHANGE/REMOVE owner·tenant·member
- `ApartmentHistoryDAO.insert` + `getLastError()`; `writeHistory` return boolean
- Flash đôi khi có cảnh báo “chưa ghi được lịch sử” nếu fail

Nguyên nhân hay gặp:
1. Bảng `apartment_history` thiếu / DB cũ chưa `schema.sql`
2. Insert history fail (SQL) bị log console, user chỉ nhìn list rỗng
3. Detail limit TOP N — action mới bị đẩy nếu sort sai (ít gặp)

### Expected
Mọi thao tác sau hiện trên Lịch sử detail:
- `ASSIGN_OWNER` / `CHANGE_OWNER` / `REMOVE_OWNER`
- `ASSIGN_TENANT` / `ASSIGN_TENANT_REP` / `CHANGE_TENANT_REP` / `REMOVE_TENANT`
- `ADD_MEMBER` / `UPDATE_MEMBER` / `REMOVE_MEMBER`
- `ACTIVATE` / `DEACTIVATE` / …

History fail → log + cảnh báo flash (không fail im lặng hoàn toàn).

### Code
- `ApartmentController.writeHistory`
- `ApartmentHistoryDAO.insert` / `findByApartmentId` / `getLastError`
- `detail.jsp` block Lịch sử

### Acceptance
- [ ] DB có `apartment_history` (schema.sql)
- [ ] Gán owner + gán thuê + gỡ → có dòng history tương ứng
- [ ] Fail insert history không nuốt im lặng
