# UC-APT-03 – Vô hiệu hóa / Xóa căn hộ  
## Đặc tả Business Rules & Validation

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-03 |
| **Tên** | Xóa / Vô hiệu hóa căn hộ |
| **Actor** | Admin, Manager |
| **Ưu tiên nghiệp vụ** | **Disable (soft)** là mặc định · **Hard delete** chỉ trường hợp hẹp |
| **URL (implement)** | `POST /apartment` · `action=deactivate` \| `activate` \| `delete` |

---

## 1. Mục tiêu nghiệp vụ

- Ngừng sử dụng căn hộ **không làm mất lịch sử** (phí, yêu cầu, cư dân cũ).
- Chỉ **xóa cứng** khi căn “sạch” (không ràng buộc dữ liệu quan trọng).
- Mọi thao tác nhạy cảm phải **audit** (ai / lúc nào / căn nào / hành động).

---

## 2. Định nghĩa thuật ngữ

| Thuật ngữ | Nghĩa trong hệ thống |
|-----------|----------------------|
| **Disable / Vô hiệu hóa** | Đặt `status = INACTIVE` (soft delete). Bản ghi **còn** trong DB. |
| **Activate / Kích hoạt lại** | Đặt `status = ACTIVE`. |
| **Hard delete / Xóa cứng** | `DELETE` row khỏi bảng `apartments`. **Mất** bản ghi master. |
| **Cư dân hiện tại** | Bản ghi gán căn–user đang hiệu lực (`is_current = 1` / chưa end). |
| **Hợp đồng** | Thỏa thuận thuê/sở hữu gắn căn (module có thể chưa code đủ — rule vẫn áp dụng). |

---

## 3. Business Rules – Khi nào được **XÓA CỨNG**

Hard delete **chỉ** khi **tất cả** điều kiện sau đúng:

| ID | Điều kiện | Bắt buộc |
|----|-----------|----------|
| BR-DEL-01 | Actor là **ADMIN** hoặc **MANAGER** | Yes |
| BR-DEL-02 | Căn hộ **tồn tại** | Yes |
| BR-DEL-03 | Căn đang **INACTIVE** (đã vô hiệu hóa trước) *khuyến nghị bắt buộc* | Yes (MVP) |
| BR-DEL-04 | **Không** còn cư dân hiện tại gắn căn | Yes |
| BR-DEL-05 | **Không** còn hợp đồng **hiệu lực** (ACTIVE/OPEN) gắn căn | Yes (khi có module HĐ) |
| BR-DEL-06 | **Không** còn yêu cầu mở (PENDING / IN_PROGRESS…) *tuỳ chính sách team* | Optional MVP |
| BR-DEL-07 | **Không** còn phí tháng chưa đóng / draft gắn căn *tuỳ chính sách* | Optional MVP |
| BR-DEL-08 | User **xác nhận** (confirm UI) | Yes |

### Khi **CẤM** xóa cứng

| ID | Tình huống | Hành vi |
|----|------------|---------|
| BR-DEL-X1 | Còn cư dân hiện tại | Chặn · buộc **Disable** |
| BR-DEL-X2 | Còn hợp đồng hiệu lực | Chặn · buộc **Disable** |
| BR-DEL-X3 | Căn đang ACTIVE (chưa disable) | Chặn · yêu cầu disable trước |
| BR-DEL-X4 | STAFF / RESIDENT | Chặn |
| BR-DEL-X5 | id không tồn tại | Chặn + message |

> **Chính sách mặc định project PRJ301 (MVP):**  
> Ưu tiên **không hard delete** trong vận hành hàng ngày.  
> UI chính = **Vô hiệu hóa / Kích hoạt lại**.  
> Hard delete chỉ cho căn INACTIVE + không cư dân hiện tại (và không HĐ nếu đã có bảng).

---

## 4. Business Rules – Khi nào **CHỈ ĐƯỢC DISABLE**

Disable (`status → INACTIVE`) khi:

| ID | Tình huống | Disable? |
|----|------------|----------|
| BR-DIS-01 | Căn ACTIVE, admin/manager muốn ngừng dùng tạm/vĩnh viễn | ✅ Cho phép |
| BR-DIS-02 | Còn cư dân / hợp đồng / lịch sử phí | ✅ **Bắt buộc dùng Disable**, không xóa cứng |
| BR-DIS-03 | Căn đã INACTIVE | ❌ Không disable lại (no-op / message đã vô hiệu) |
| BR-DIS-04 | Cần giữ mã căn & lịch sử báo cáo | ✅ Disable |

### Kích hoạt lại (`INACTIVE → ACTIVE`)

| ID | Rule |
|----|------|
| BR-ACT-01 | ADMIN/MANAGER được kích hoạt lại nếu căn tồn tại và đang INACTIVE |
| BR-ACT-02 | Không tự gán lại cư dân; quan hệ cư dân giữ nguyên theo `is_current` |
| BR-ACT-03 | Không tự mở lại hợp đồng đã đóng — module HĐ tự xử lý |

---

## 5. Ảnh hưởng đến **Cư dân**

| ID | Rule | Chi tiết |
|----|------|----------|
| BR-RES-01 | Disable **không** xóa bản ghi `apartment_residents` | Giữ lịch sử ai từng ở |
| BR-RES-02 | Disable **không** tự `is_current = 0` (MVP) | Tuỳ team: có thể cảnh báo “vẫn còn cư dân hiện tại” nhưng vẫn cho disable |
| BR-RES-03 | **Khuyến nghị vận hành:** trước disable, staff/manager gỡ / kết thúc cư dân hiện tại | Tránh RESIDENT vẫn thấy căn “của tôi” khi căn INACTIVE |
| BR-RES-04 | Hard delete **cấm** nếu còn cư dân hiện tại | Tránh orphan / vỡ FK |
| BR-RES-05 | RESIDENT **không** được disable/xóa căn | Chỉ ADMIN/MANAGER |
| BR-RES-06 | Sau disable: màn “Căn hộ của tôi” (UC sau) **không** hiển thị căn INACTIVE là căn đang ở hợp lệ | Rule hiển thị module RESIDENT |

### Cảnh báo UI khi disable mà còn cư dân hiện tại

```text
Căn hộ vẫn còn N cư dân hiện tại. 
Vô hiệu hóa sẽ giữ lịch sử gán cư dân nhưng căn không còn ACTIVE.
Bạn có chắc muốn tiếp tục?
```

*(MVP: confirm chung; có thể đếm N nếu bảng residents có.)*

---

## 6. Ảnh hưởng đến **Hợp đồng**

> Hệ thống có thể **chưa** có bảng `contracts` đầy đủ. Rule dưới đây là **chuẩn nghiệp vụ**; khi có module HĐ phải enforce trong code.

| ID | Rule |
|----|------|
| BR-CON-01 | Disable căn **không** xóa hợp đồng lịch sử |
| BR-CON-02 | Nếu còn hợp đồng **hiệu lực** → **cấm hard delete** |
| BR-CON-03 | Disable căn **không** tự “hủy hợp đồng”; cần quy trình HĐ riêng (đóng/thanh lý) |
| BR-CON-04 | Không cho tạo hợp đồng **mới** trên căn INACTIVE (rule module HĐ) |
| BR-CON-05 | Báo cáo/hợp đồng quá khứ vẫn resolve theo `apartment_id` nếu chỉ disable |

### Ma trận nhanh

| Trạng thái căn | Tạo HĐ mới | Đóng HĐ | Xóa cứng căn |
|----------------|------------|---------|--------------|
| ACTIVE | Cho phép (module HĐ) | Cho phép | Không (trừ quy trình đặc biệt) |
| INACTIVE | Không | Cho phép hoàn tất | Chỉ khi không HĐ hiệu lực + đủ điều kiện DEL |

---

## 7. **Audit Log**

### 7.1. Bắt buộc ghi nhận khi

| Hành động | Audit? |
|-----------|--------|
| Deactivate (Disable) | ✅ Bắt buộc |
| Activate (Kích hoạt lại) | ✅ Bắt buộc |
| Hard delete | ✅ Bắt buộc |
| Thất bại do rule (optional) | Khuyến nghị |

### 7.2. Trường audit tối thiểu

| Field | Mô tả |
|-------|--------|
| `timestamp` | Thời điểm (UTC/local server) |
| `actorUserId` / username | Ai thao tác |
| `actorRole` | ADMIN/MANAGER |
| `action` | DEACTIVATE \| ACTIVATE \| DELETE |
| `apartmentId` | PK căn |
| `apartmentCode` | Mã căn (dễ đọc log) |
| `fromStatus` → `toStatus` | Trạng thái trước/sau (delete: → DELETED) |
| `result` | SUCCESS \| DENIED \| ERROR |
| `message` | Lý do chặn / lỗi (nếu có) |
| `ip` (optional) | Địa chỉ client |

### 7.3. Lưu trữ (lộ trình)

| Giai đoạn | Cách lưu |
|-----------|----------|
| **MVP (hiện tại)** | Application log: `System.out` / Tomcat log dạng dòng `AUDIT \| ...` |
| **Mở rộng** | Bảng `audit_logs` (append-only), không cho user xóa |
| **Tuân thủ** | Không ghi password; không ghi dữ liệu nhạy cảm thừa |

### 7.4. Rule audit

| ID | Rule |
|----|------|
| BR-AUD-01 | Mọi deactivate/activate/delete **thành công** phải có 1 dòng audit |
| BR-AUD-02 | Audit **không** thay thế backup DB |
| BR-AUD-03 | Hard delete vẫn giữ audit (log) dù row căn đã mất — vì vậy log phải chứa **apartmentCode** |
| BR-AUD-04 | User thường **không** sửa/xóa audit |

---

## 8. Validation Rules (UC-APT-03)

| ID | Rule | Message gợi ý |
|----|------|----------------|
| VR-D01 | Đã login | (redirect login) |
| VR-D02 | Role ADMIN hoặc MANAGER | Bạn không có quyền vô hiệu hóa/xóa căn hộ. |
| VR-D03 | `id` parse được, > 0 | ID căn hộ không hợp lệ. |
| VR-D04 | Căn tồn tại | Không tìm thấy căn hộ. |
| VR-D05 | Deactivate: status hiện tại phải ACTIVE | Căn hộ đã ở trạng thái không hoạt động. |
| VR-D06 | Activate: status hiện tại phải INACTIVE | Căn hộ đang hoạt động. |
| VR-D07 | Hard delete: status phải INACTIVE | Chỉ xóa được căn đã vô hiệu hóa. |
| VR-D08 | Hard delete: không còn cư dân hiện tại | Không thể xóa: căn vẫn còn cư dân hiện tại. Hãy vô hiệu hóa hoặc gỡ cư dân trước. |
| VR-D09 | Hard delete: không còn HĐ hiệu lực *(khi có module)* | Không thể xóa: còn hợp đồng hiệu lực. |
| VR-D10 | Confirm phía client trước deactivate/delete | (UI confirm) |

---

## 9. Main Flow (Disable – luồng chính)

1. Manager/Admin vào list căn hộ.  
2. Chọn căn **ACTIVE** → **Vô hiệu hóa**.  
3. Confirm.  
4. Hệ thống check quyền + id + status.  
5. `UPDATE status = INACTIVE`, `updated_at = now`.  
6. Ghi **Audit** DEACTIVATE SUCCESS.  
7. Flash success → redirect list.  
8. List hiển thị badge INACTIVE.

### Activate
Tương tự ngược lại: INACTIVE → ACTIVE + audit ACTIVATE.

### Hard delete (phụ)
INACTIVE + không cư dân (+ không HĐ) → confirm → DELETE + audit DELETE → list.

---

## 10. Ảnh hưởng module khác (tóm tắt)

| Module | Khi Disable | Khi Hard delete |
|--------|-------------|-----------------|
| Cư dân | Giữ quan hệ; nên ẩn căn inactive khỏi “đang ở” | Cấm nếu còn current resident |
| Hợp đồng | Giữ lịch sử; không tạo HĐ mới | Cấm nếu HĐ hiệu lực |
| Phí tháng | Giữ lịch sử phí | Cẩn trọng FK — ưu tiên không xóa |
| Yêu cầu (request) | Giữ ticket cũ | Có thể vỡ FK — ưu tiên disable |
| Báo cáo | Vẫn thống kê được căn inactive | Mất master → chỉ còn snapshot/audit |

---

## 11. Messages

| Code | Message |
|------|---------|
| S-D01 | Đã vô hiệu hóa căn hộ. |
| S-D02 | Đã kích hoạt lại căn hộ. |
| S-D03 | Đã xóa căn hộ. |
| E-D01 | Bạn không có quyền vô hiệu hóa/xóa căn hộ. |
| E-D02 | ID căn hộ không hợp lệ. |
| E-D03 | Không tìm thấy căn hộ. |
| E-D04 | Căn hộ đã ở trạng thái không hoạt động. |
| E-D05 | Căn hộ đang hoạt động. |
| E-D06 | Chỉ xóa được căn đã vô hiệu hóa. |
| E-D07 | Không thể xóa: căn vẫn còn cư dân hiện tại. |
| E-D08 | Không thể thực hiện. Vui lòng thử lại. |

---

## 12. Test Case gợi ý (smoke)

| ID | Scenario | Expected |
|----|----------|----------|
| TC-APT-D-001 | Disable căn ACTIVE | status INACTIVE + audit + flash |
| TC-APT-D-002 | Activate căn INACTIVE | status ACTIVE |
| TC-APT-D-003 | Disable căn đã INACTIVE | Message E-D04 |
| TC-APT-D-004 | STAFF bấm disable | Chặn |
| TC-APT-D-005 | Hard delete căn ACTIVE | Chặn E-D06 |
| TC-APT-D-006 | Hard delete INACTIVE không cư dân | Xóa OK |
| TC-APT-D-007 | Hard delete còn cư dân | Chặn E-D07 |
| TC-APT-D-008 | id sai | E-D02/E-D03 |

---

## 13. Quyết định implement MVP (code hiện tại)

| Hạng mục | MVP code |
|----------|----------|
| Disable / Activate | ✅ `updateStatus` |
| Hard delete | ✅ có điều kiện INACTIVE + không current resident (query an toàn nếu chưa có bảng) |
| Hợp đồng | ⚠ Rule trong doc; code skip nếu chưa có bảng `contracts` |
| Audit | ✅ log dòng `AUDIT \| ...` ra console Tomcat |
| Ảnh hưởng UC-01/02 | Không xóa create/update; chỉ thêm action |

---

## 14. Traceability

| Artifact | Path |
|----------|------|
| Đặc tả BR | `docs/uc-apt-03-vo-hieu-hoa-can-ho.md` |
| DAO | `ApartmentDAO.updateStatus`, `deleteById`, `countCurrentResidents` |
| Controller | `handleDeactivate`, `handleActivate`, `handleDelete` |
| UI | `list.jsp` nút Vô hiệu hóa / Kích hoạt / Xóa |
| Audit helper | `ApartmentController.audit(...)` (console MVP) |
