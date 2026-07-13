# UC-APT-09 – Cập nhật / Xóa (gỡ) thành viên sinh sống

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-09 |
| **Tên** | Update / Remove household member (soft delete) |
| **Actor** | Admin, Manager |
| **URL** | `GET/POST edit-member` · `POST remove-member` |
| **Bảng** | `household_members` |
| **Standards** | `coding-standards.md` |

---

## 1. User Story

**Là** Admin/Manager, **tôi muốn** cập nhật thông tin thành viên hoặc **gỡ** (soft delete) thành viên không còn sinh sống, **để** nhân khẩu căn hộ luôn đúng và giữ lịch sử.

---

## 2. Preconditions

1. Đã login; role ADMIN/MANAGER.
2. Thành viên tồn tại (`member_id`) và thuộc đúng căn.
3. Bảng `household_members` sẵn sàng.

---

## 3. Business Rules

| ID | Rule |
|----|------|
| **BR-U01** | Chỉ ADMIN/MANAGER được Update / Remove. |
| **BR-U02** | **Update:** sửa fullName, relationship, phone, CCCD, DOB; không đổi `apartment_id`. |
| **BR-U03** | **Remove = Soft Delete (mặc định):** `is_active = 0` — **không** xóa row. |
| **BR-U04** | Soft-deleted member vẫn xem được trên detail (badge Off) để audit; không tính “đang sinh sống”. |
| **BR-U05** | Không hard delete trong MVP (tránh mất lịch sử CCCD). |
| **BR-U06** | Validate update giống UC-08 (tên, quan hệ, CCCD, phone, DOB). |
| **BR-U07** | CCCD trùng: chặn nếu trùng member **active khác** trên cùng căn (trừ chính mình). |
| **BR-U08** | Không cho update/remove member của căn khác (check apartment_id). |
| **BR-U09** | Remove member đã inactive → message “đã gỡ”. |
| **BR-U10** | **Audit Log:** |
| | • Console `AUDIT \| ...` |
| | • `apartment_history`: `UPDATE_MEMBER` / `REMOVE_MEMBER` |
| **BR-U11** | Remove **không** ảnh hưởng owner/tenant (`apartment_residents`). |

### Update vs Remove vs Soft Delete

| Thao tác | Hành vi DB |
|----------|------------|
| **Update** | `UPDATE` các field thông tin |
| **Remove** | Soft delete: `is_active = 0` |
| **Soft Delete** | Chính là Remove trong UC này |
| Hard delete | **Không** dùng (MVP) |

---

## 4. Validation (Update)

| Field | Rule | Message |
|-------|------|---------|
| memberId | required, tồn tại | Không tìm thấy thành viên |
| apartmentId | khớp member | Thành viên không thuộc căn này |
| fullName | 2–100 | như UC-08 |
| relationship | required ≤50 | như UC-08 |
| idNumber | optional 9–12 số; unique active except self | CCCD… |
| phone | optional format | … |
| dateOfBirth | optional ≤ today | … |

### Remove validation

| Rule | Message |
|------|---------|
| member tồn tại + đúng căn | Không tìm thấy… |
| is_active = 1 | Thành viên đã được gỡ trước đó |
| Quyền ADMIN/MANAGER | Không có quyền… |

---

## 5. Audit Log

| Event | history.action | Console |
|-------|----------------|---------|
| Update OK | `UPDATE_MEMBER` | AUDIT … action=UPDATE_MEMBER … SUCCESS |
| Soft remove OK | `REMOVE_MEMBER` | AUDIT … action=REMOVE_MEMBER … SUCCESS |
| Denied | — | AUDIT … DENIED (optional) |

Payload note: tên TV, memberId, field tóm tắt.

---

## 6. Main Flow

### Update
1. Detail → **Sửa** trên dòng TV.  
2. Form prefill → sửa → Lưu.  
3. Validate → update → history + audit → redirect detail.

### Remove (Soft Delete)
1. Detail → **Gỡ** → confirm.  
2. POST remove-member.  
3. set is_active=0 → history + audit → redirect detail.

---

## 7. Acceptance Criteria

| # | Tiêu chí |
|---|----------|
| AC-01 | Sửa họ tên/quan hệ/CCCD/phone/DOB thành công |
| AC-02 | Validate fail giữ form + errors |
| AC-03 | CCCD trùng TV active khác → chặn |
| AC-04 | Gỡ → is_active=0, badge Off, row còn |
| AC-05 | Gỡ lần 2 → báo đã gỡ |
| AC-06 | STAFF không sửa/gỡ |
| AC-07 | History có UPDATE_MEMBER / REMOVE_MEMBER |
| AC-08 | F5 sau success không double submit (redirect) |

---

## 8. Messages

| Code | Message |
|------|---------|
| S-U01 | Cập nhật thành viên thành công. |
| S-U02 | Đã gỡ thành viên (ngừng sinh sống). |
| E-U01 | Bạn không có quyền… |
| E-U02 | Không tìm thấy thành viên. |
| E-U03 | Thành viên không thuộc căn này. |
| E-U04 | Thành viên đã được gỡ trước đó. |
| E-U05 | (validate field như UC-08) |
| E-U06 | Không thể thực hiện. Vui lòng thử lại. |

---

## 9. Traceability

| Layer | File |
|-------|------|
| BA | `docs/uc-apt-09-cap-nhat-xoa-thanh-vien.md` |
| DAO | `findById`, `update`, `softDelete`, `existsActiveIdNumberExceptId` |
| Controller | `edit-member`, `remove-member` |
| View | `edit-member.jsp` + nút Sửa/Gỡ trên detail |
