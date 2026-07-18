# UC-APT-07 – Gán người thuê (Đặc tả)

| Mục | Nội dung |
|-----|----------|
| **UC ID** | UC-APT-07 |
| **Tên** | Gán / đổi người thuê (kèm đại diện thuê) |
| **Actor** | Admin, Manager |
| **URL** | `GET/POST /apartment?action=assign-tenant` |
| **Bảng** | `apartment_residents` (`TENANT_REP` \| `TENANT`) |
| **Standards** | `coding-standards.md` |

---

## 1. User Story

**Là** Admin/Manager, **tôi muốn** gán người thuê cho căn hộ (có thể là **đại diện thuê** hoặc người thuê thường), kèm **ngày bắt đầu / kết thúc** và theo dõi **trạng thái thuê**, **để** quản lý hợp đồng thuê và hiển thị trên chi tiết căn.

---

## 2. Preconditions

1. Đã đăng nhập; role **ADMIN** hoặc **MANAGER**.
2. Căn hộ tồn tại.
3. Có bảng `apartment_residents`, `users`.
4. Có user `is_active = 1` để chọn.

---

## 3. Business Rules

| ID | Rule |
|----|------|
| **BR-T01** | Vai trò trong căn khi thuê: **`TENANT_REP`** (đại diện thuê) hoặc **`TENANT`** (người thuê). |
| **BR-T02** | **Đại diện thuê (`TENANT_REP`)**: tối đa **1** bản ghi **hiện tại** (`is_current = 1`) / căn. |
| **BR-T03** | **Người thuê (`TENANT`)**: cho phép **nhiều** bản ghi hiện tại / căn (MVP). |
| **BR-T04** | Gán `TENANT_REP` mới khi đã có rep khác → **đóng rep cũ** (`is_current=0`, `end_date`) rồi insert mới. |
| **BR-T05** | Không xóa cứng bản ghi cũ — giữ lịch sử. |
| **BR-T06** | **Ngày bắt đầu** (`start_date`): bắt buộc (default hôm nay). |
| **BR-T07** | **Ngày kết thúc** (`end_date`): optional. Null = thuê đang mở. Nếu có: `end_date >= start_date`. |
| **BR-T08** | **Trạng thái thuê** (tính từ data): |
| | • **CURRENT** — `is_current=1` và (`end_date` null hoặc `end_date >= hôm nay`) |
| | • **ENDED** — `is_current=0` hoặc `end_date < hôm nay` |
| **BR-T09** | Insert mới: `is_current = 1` khi end null hoặc end ≥ hôm nay; nếu end &lt; hôm nay → reject validate. |
| **BR-T10** | User gán phải active; không gán trùng user đã là tenant/rep **hiện tại** cùng role trên căn. |
| **BR-T11** | Chỉ ADMIN/MANAGER. |
| **BR-T12** | Ghi history: `ASSIGN_TENANT` / `CHANGE_TENANT_REP`. |
| **BR-T13** | Có tenant → sync occupancy `RENTED` (giữ OWNER nếu có = chủ nhà). |
| **BR-T14** | Form gán: **User có sẵn** hoặc **Người mới** (tạo RESIDENT + gán). Gợi ý từ thành viên hộ. Pass mặc định `123456`. |
| **BR-T15** | Người được gán thuê **phải xuất hiện** trong thành viên hộ: nếu chưa có (trùng họ tên) → auto thêm TV vai trò `Thành viên`; đã có → không thêm trùng. |

### Đại diện thuê vs người thuê

| role_in_apartment | Ý nghĩa | Số lượng current |
|-------------------|---------|------------------|
| `TENANT_REP` | Đại diện thuê (liên hệ chính) | 0..1 |
| `TENANT` | Người thuê / thành viên thuê | 0..N |

---

## 4. Validation

| Field | Rule | Message |
|-------|------|---------|
| apartmentId | required, tồn tại | ID/căn không hợp lệ |
| userId | required, user active | Chọn người thuê / User không hợp lệ hoặc đã khóa |
| roleInApartment | TENANT_REP \| TENANT | Vai trò thuê không hợp lệ |
| startDate | parse được; default today | Ngày bắt đầu không hợp lệ |
| endDate | empty OK; nếu có ≥ startDate; không &lt; today khi gán mới | Ngày kết thúc không hợp lệ / phải ≥ ngày bắt đầu |
| Trùng | user chưa là current cùng role trên căn | User đã là người thuê/đại diện hiện tại |
| Quyền | ADMIN/MANAGER | Không có quyền gán người thuê |

---

## 5. Ngày bắt đầu / kết thúc

| Field | DB | UI | Default |
|-------|-----|-----|---------|
| start_date | DATE NOT NULL (app) | input date | hôm nay |
| end_date | DATE NULL | input date optional | null (đang thuê) |

Khi **đổi đại diện thuê**: `end_date` của rep cũ = `start_date` của rep mới (hoặc today).

---

## 6. Trạng thái thuê (hiển thị)

| Trạng thái | Điều kiện |
|------------|-----------|
| **CURRENT** (Đang thuê) | is_current=1 AND (end_date IS NULL OR end_date >= today) |
| **ENDED** (Đã kết thúc) | is_current=0 OR end_date < today |

Form gán chỉ tạo bản ghi **CURRENT**.

---

## 7. Main Flow

1. Detail → **Gán người thuê**.  
2. Chọn user, vai trò (Đại diện / Người thuê), start, end (optional).  
3. Validate → nếu TENANT_REP và đã có rep khác → end old.  
4. Insert → history → redirect detail + flash.

---

## 8. Acceptance Criteria

| # | Tiêu chí |
|---|----------|
| AC-01 | Manager gán TENANT_REP lần đầu → detail hiện 1 đại diện |
| AC-02 | Đổi TENANT_REP → cũ closed, mới current |
| AC-03 | Gán TENANT thêm được nhiều người |
| AC-04 | start mặc định today; end optional |
| AC-05 | end < start → lỗi |
| AC-06 | Trùng user+role current → lỗi |
| AC-07 | STAFF không gán được |
| AC-08 | Flash + redirect detail |

---

## 9. Traceability

| Layer | File |
|-------|------|
| BA | `docs/uc-apt-07-gan-nguoi-thue.md` |
| DAO | `insertTenant`, `endCurrentTenantReps`, `findCurrentTenantRep` |
| Controller | `assign-tenant` GET/POST |
| View | `assign-tenant.jsp` + link detail |
