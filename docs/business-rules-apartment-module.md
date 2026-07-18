# Task 15 – Business Rules tổng hợp  
## Module Apartment Management (TV2)

Tổng hợp rule từ UC-APT-01 … 10 + Permission Matrix.

---

## 1. Apartment (căn hộ)

| ID | Rule |
|----|------|
| BR-A01 | `apartment_code` **unique** toàn hệ thống. |
| BR-A02 | Mã căn / tòa / tầng **không đổi** sau khi tạo. UI **tách 3 cột**: Mã căn · Tòa · Tầng. |
| BR-A02b | **Create lẻ / init-floor**: mặc định **INACTIVE + N/A**; mã `{TOKEN}-{FF}{UU}` unit **01–06** / tầng. Tầng đủ 6 → chặn thêm. |
| BR-A02c | **Init-floor**: tạo các unit còn thiếu 01–06 trên tòa+tầng (bỏ qua unit đã có). |
| BR-A03 | `occupancy_type` ∈ {OWNED, RENTED, **VACANT**, **N/A**}. |
| BR-A03b | **INACTIVE** ⇔ occupancy **N/A** only. **ACTIVE** ⇔ OWNED \| RENTED \| VACANT (không N/A). |
| BR-A03c | **VACANT** = ACTIVE, sẵn sàng, **chưa có cư dân/role**. **N/A** = chưa vận hành (INACTIVE). |
| BR-A03d | **Auto-sync**: INACTIVE→N/A; ACTIVE+tenant→**RENTED**; **ACTIVE đang RENTED** (kể cả gỡ hết chủ/thuê) → **giữ RENTED** (ô trống, gán/đổi lại được); ACTIVE+OWNER only→**OWNED**; ACTIVE+TV (no role)→OWNED (trừ đang RENTED). **ACTIVE trống** sau activate/sửa: giữ OWNED/RENTED/VACANT đã chọn. **Sau gỡ trên căn không RENTED**: trống hoàn toàn → force **VACANT**. |
| BR-A03e | **OWNED (chủ ở)**: chỉ OWNER, **không** mục/gán người thuê. UI ẩn card thuê. |
| BR-A03f | **RENTED (cho thuê)**: OWNER = chủ nhà (có thể có) + TENANT/REP. Gán tenant **không** gỡ owner. |
| BR-A04 | `status` ∈ {ACTIVE, INACTIVE}; mặc định tạo = **INACTIVE**. |
| BR-A05 | `floor_number` ∈ [0, 200]; `area_m2` ∈ [15, 10000]; **UNITS_PER_FLOOR = 6**. |
| BR-A06 | Chỉ ADMIN/MANAGER tạo / sửa / đổi status / xóa / init-floor. |
| BR-A07 | Gán owner/tenant chỉ căn **ACTIVE**; không tự đổi occupancy (MVP). |

---

## 2. Status (trạng thái căn)

| ID | Rule |
|----|------|
| BR-S01 | **ACTIVE** = đang vận hành (occupancy OWNED/RENTED/VACANT). |
| BR-S02 | **INACTIVE** = chưa/vô hiệu (soft); occupancy **force N/A**. |
| BR-S03 | ACTIVE → INACTIVE: **deactivate** → status INACTIVE + occupancy N/A. |
| BR-S04 | INACTIVE → ACTIVE: **activate form** bắt buộc chọn OWNED/RENTED/VACANT — **lưu đúng** loại hình đã chọn (không re-sync ghi đè ngay). |
| BR-S04b | Activate chọn **OWNED/RENTED** dù chưa gán cư dân → detail/list hiển thị đúng OWNED/RENTED; gán cư dân sau mới khớp auto-sync. |
| BR-S05 | Hard **delete** chỉ khi INACTIVE (+ không cư dân current). |
| BR-S06 | Update form **không** đổi status; lifecycle chỉ qua activate/deactivate. ACTIVE: form Sửa **cho chọn** VACANT/OWNED/RENTED. |

---

## 3. Delete (xóa căn)

| ID | Rule |
|----|------|
| BR-D01 | Ưu tiên **soft disable** (INACTIVE) trong vận hành. |
| BR-D02 | Hard delete: phải INACTIVE trước. |
| BR-D03 | Hard delete cấm nếu còn `apartment_residents.is_current = 1`. |
| BR-D04 | Hard delete không xóa audit console đã ghi; history row có thể mất theo FK (MVP không FK cascade). |
| BR-D05 | STAFF/RESIDENT không delete. |

---

## 4. Owner (chủ sở hữu)

| ID | Rule |
|----|------|
| BR-O01 | Tối đa **1** OWNER `is_current = 1` / căn. |
| BR-O02 | Đổi owner: **đóng** owner cũ (`is_current=0`, `end_date`) — không xóa row. |
| BR-O03 | Owner mới: `is_current=1`, `start_date`, `end_date=null`. |
| BR-O04 | User gán phải `is_active = 1`. |
| BR-O05 | Không gán trùng đúng owner hiện tại. |
| BR-O06 | Chỉ ADMIN/MANAGER. |
| BR-O07 | Gán owner: nếu user **đã có** trong thành viên hộ (trùng họ tên) → **không thêm** dòng TV mới; flash báo đã có. |

---

## 5. Tenant (người thuê)

| ID | Rule |
|----|------|
| BR-T01 | `TENANT_REP` (đại diện): tối đa **1** current / căn. |
| BR-T02 | `TENANT`: cho phép **nhiều** current. |
| BR-T03 | Đổi TENANT_REP: end rep cũ rồi insert mới. |
| BR-T04 | `start_date` bắt buộc (default today). |
| BR-T05 | `end_date` optional; nếu có ≥ start; không &lt; today khi gán mới. |
| BR-T06 | Trạng thái thuê hiển thị: **CURRENT** / **ENDED**. |
| BR-T07 | Chỉ ADMIN/MANAGER. |

---

## 6. Resident / Household member (thành viên sinh sống)

| ID | Rule |
|----|------|
| BR-R01 | Thành viên thuộc `household_members` (nhân khẩu), **không** bắt buộc là user login. |
| BR-R02 | fullName + **vai trò** bắt buộc; vai trò chỉ `Chủ hộ` \| `Thành viên` (cột DB `relationship`, không dùng quan hệ gia đình). |
| BR-R03 | CCCD optional; format 9–12 số; unique active / căn. |
| BR-R04 | Phone optional; format 9–11 số. |
| BR-R05 | DOB optional; không future. |
| BR-R06 | Thêm: `is_active = 1`. |
| BR-R07 | **Remove TV = hard delete** row `household_members` (UI biến mất). |
| BR-R08 | Xóa TV **là chủ sở hữu** (vai trò `Chủ hộ` hoặc trùng tên owner) → **gỡ luôn OWNER**. |
| BR-R09 | Gán owner sync 1 dòng TV **Chủ hộ**; xóa dòng Chủ hộ = gỡ owner tương ứng. |
| BR-R08 | Update không đổi `apartment_id`. |
| BR-R09 | Chỉ ADMIN/MANAGER add/edit/remove. |
| BR-R10 | STAFF xem TV trên detail căn (read-only); không còn list TV global. RESIDENT không quản lý TV. |

---

## 7. History (lịch sử)

| ID | Rule |
|----|------|
| BR-H01 | Ghi `apartment_history` khi: CREATE/UPDATE/DEACTIVATE/ACTIVATE/ASSIGN_*/ADD_MEMBER/UPDATE_MEMBER/REMOVE_MEMBER… |
| BR-H02 | Lỗi bảng history **không** chặn UC chính (try/catch). |
| BR-H03 | Console **AUDIT** cho thao tác nhạy cảm (status, delete, member remove/update). |
| BR-H04 | Detail hiển thị history; fallback create/update từ cột apartment nếu history trống. |

---

## 8. Permission (tóm)

| ID | Rule |
|----|------|
| BR-P01 | ADMIN ≈ MANAGER trên module căn (write). |
| BR-P02 | STAFF = read list/detail căn + TV trên detail (không write). |
| BR-P03 | RESIDENT = detail căn được gán current; không list quản trị. |
| BR-P04 | Mọi write check `canManage` trong controller. |
| BR-P05 | Chi tiết đầy đủ: `docs/permission-matrix-apartment.md`. |

---

## 9. Validation (tổng hợp field)

| Đối tượng | Field chính | Rule ngắn |
|-----------|-------------|-----------|
| Apartment | code | required, pattern, unique |
| Apartment | building | required ≤50 |
| Apartment | floor | 0–200 |
| Apartment | area | 0.01–10000 |
| Apartment | occupancy/status | enum |
| Owner/Tenant | userId | active user |
| Tenant | start/end | end ≥ start |
| Member | name + vai trò | required; vai trò ∈ {Chủ hộ, Thành viên} |
| Member | CCCD/phone/DOB | format + biên |
| Common | id params | parse >0, đúng quan hệ căn |

---

## 10. Traceability docs

| Chủ đề | File |
|--------|------|
| UC 01–10 | `docs/uc-apt-*.md`, user-story, validation, test create |
| Permission | `docs/permission-matrix-apartment.md` |
| BR tổng | `docs/business-rules-apartment-module.md` (file này) |
| Code chuẩn | `coding-standards.md` |
