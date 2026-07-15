## Title
chore: Dọn comment thừa / outdated trong module apartment

## Labels
`chore`, `cleanup`, `debt`

## Body

### Mô tả
Code có nhiều comment class/method **outdated**, ví dụ:

```java
/**
 * DAO căn hộ – US-APT-01 Thêm căn hộ (+ list tối thiểu để quay về sau create).
 */
```

Trong khi `ApartmentDAO` đã có filter/sort/page, update, status, delete…

### Scope
- `ApartmentDAO`, `ApartmentResidentDAO`, `HouseholdMemberDAO`, `ApartmentHistoryDAO`
- Comment header Controller lặp / không khớp hiện trạng
- Giữ JavaDoc rule không trivial (return -1, BR)

### Expected
- Comment ngắn, đúng hiện trạng; xóa essay thừa
- Không đổi logic

### Acceptance
- [ ] Không còn “list tối thiểu / chỉ US-APT-01” khi đã full CRUD
- [ ] Build không đổi hành vi
