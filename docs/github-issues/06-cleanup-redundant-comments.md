## Title
chore: Dọn comment thừa / outdated trong module apartment

## Labels
`chore`, `cleanup`, `debt`

## Body
### Mô tả
Code còn nhiều comment class/method **outdated / thừa**, ví dụ:

```java
/**
 * DAO căn hộ – US-APT-01 Thêm căn hộ (+ list tối thiểu để quay về sau create).
 */
```

Trong khi `ApartmentDAO` đã filter/sort/page, update status, delete, reconcile occupancy…

Tương tự:
- Header Controller “UC-APT-01..10 / List TV + export” trong khi UC-10 đã gỡ
- `edit-member.jsp` còn text “soft delete” dù remove đã hard delete
- Comment dài lặp BR đã có trong `coding-standards.md` / docs UC

### Scope
- `ApartmentDAO`, `ApartmentResidentDAO`, `HouseholdMemberDAO`, `ApartmentHistoryDAO`
- `ApartmentController` class/method javadoc lệch hiện trạng
- JSP notes outdated (soft delete, list TV global)

### Expected
- Comment ngắn, đúng hiện trạng
- Xóa essay / comment copy-paste thừa
- **Không** đổi logic nghiệp vụ

### Acceptance
- [ ] Không còn “list tối thiểu / chỉ US-APT-01” khi đã full module
- [ ] Không còn mô tả soft-delete TV trên UI nếu hard delete
- [ ] Build/hành vi không đổi
