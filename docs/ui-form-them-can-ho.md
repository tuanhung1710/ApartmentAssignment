# UI Form Design – Thêm căn hộ (US-APT-01)

| Mục | Nội dung |
|-----|----------|
| **Màn hình** | Form Thêm căn hộ |
| **URL** | `GET /apartment?action=create` (mở form) · `POST /apartment` `action=create` (submit) |
| **Actor** | ADMIN, MANAGER |
| **View** | `web/WEB-INF/views/apartment/form.jsp` (nằm trong `layout.jsp`) |
| **Controller** | `ApartmentController.handleCreateForm` / `handleCreate` |

---

## 1. Mục tiêu UI

- Cho phép nhập đủ thông tin căn hộ mới một cách rõ ràng, ít nhầm.
- Hiển thị lỗi dễ đọc, **giữ lại dữ liệu** đã nhập khi lỗi.
- Mobile-friendly (Bootstrap 5 grid).

---

## 2. Danh sách field

| # | Label (UI) | Field name (HTML/Java) | Kiểu dữ liệu | Control | Required | Optional | Placeholder / Gợi ý | Default |
|---|------------|------------------------|--------------|---------|----------|----------|---------------------|---------|
| 1 | Mã căn hộ | `apartmentCode` | String (max 20) | Text | ✅ Yes | — | `VD: A-1201` | — |
| 2 | Tòa nhà | `building` | String (max 50) | Text | ✅ Yes | — | `VD: Tòa A` | — |
| 3 | Tầng | `floorNumber` | Integer | Number input | ✅ Yes | — | `0 = trệt` | — |
| 4 | Diện tích (m²) | `areaM2` | Decimal (10,2) | Number input | ✅ Yes | — | `VD: 75.50` | — |
| 5 | Loại hình | `occupancyType` | Enum String | Select | ✅ Yes | — | — | `OWNED` |
| 6 | Trạng thái | `status` | Enum String | Select | — | ✅ Optional* | — | `ACTIVE` |
| 7 | Ghi chú | `notes` | String (max 500) | Textarea | — | ✅ Optional | `Ghi chú thêm (không bắt buộc)` | rỗng |

\* `status` trên UI có select; nếu không gửi / rỗng → server gán `ACTIVE`.

### Hidden field

| name | value | Mục đích |
|------|-------|----------|
| `action` | `create` | `doPost` switch vào `handleCreate` |

### Options Select

**occupancyType**

| Value | Hiển thị |
|-------|----------|
| `OWNED` | OWNED – Sở hữu |
| `RENTED` | RENTED – Thuê |

**status**

| Value | Hiển thị |
|-------|----------|
| `ACTIVE` | ACTIVE – Đang hoạt động |
| `INACTIVE` | INACTIVE – Ngừng |

---

## 3. Validation (gắn với UI)

| Field | Client-side (HTML) | Server-side (bắt buộc) |
|-------|--------------------|------------------------|
| `apartmentCode` | `required`, `maxlength=20`, `pattern` | rỗng, format, unique |
| `building` | `required`, `maxlength=50` | rỗng, max 50 |
| `floorNumber` | `required`, `min=0`, `max=200`, `step=1` | null/parse, 0–200 |
| `areaM2` | `required`, `min=0.01`, `max=10000`, `step=0.01` | null/parse, (0, 10000] |
| `occupancyType` | `required` | OWNED \| RENTED |
| `status` | — | ACTIVE \| INACTIVE (default ACTIVE) |
| `notes` | `maxlength=500` | max 500 |

> Client chỉ hỗ trợ UX. **Luôn validate lại server** trong `validateCreate()`.

Chi tiết rule: xem `docs/validation-rules-them-can-ho.md`.

---

## 4. Button

| Button | Loại | Style | Vị trí | Hành vi |
|--------|------|-------|--------|---------|
| **Lưu** | `submit` | `btn btn-primary` | Dưới form, trái | POST lưu căn hộ |
| **Hủy** | link (`a`) | `btn btn-outline-secondary` | Cạnh Lưu | Về `?action=list`, **không** lưu |
| **Về danh sách** | link | `btn btn-outline-secondary btn-sm` | Header form | Về list |

Icon gợi ý:
- Lưu: `bi-check-lg`
- Về list: `bi-arrow-left`

---

## 5. Layout đề xuất

### 5.1. Cấu trúc trang (trong layout chung)

```text
┌─────────────────────────────────────────────────────────┐
│ Sidebar │  Header (user / role)                         │
│         │───────────────────────────────────────────────│
│ Dashboard│  [Flash success/error nếu có]                │
│ Căn hộ  │                                               │
│ ...     │  ┌ Tiêu đề: Thêm căn hộ     [Về danh sách] ┐ │
│         │  │ Phụ đề: Nhập thông tin...               │ │
│         │  └─────────────────────────────────────────┘ │
│         │  ┌ Alert lỗi (nếu errors[]) ───────────────┐ │
│         │  └─────────────────────────────────────────┘ │
│         │  ┌ Card form ──────────────────────────────┐ │
│         │  │  [Mã]     [Tòa nhà]     [Tầng]          │ │
│         │  │  [Diện tích] [Loại hình] [Trạng thái]   │ │
│         │  │  [Ghi chú ........................]     │ │
│         │  │  [Lưu]  [Hủy]                           │ │
│         │  └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 5.2. Grid Bootstrap

- Container: `card shadow-sm` > `card-body`
- Hàng field: `row g-3`
- Mỗi field chính: `col-md-4` (3 cột trên desktop, stack mobile)
- Ghi chú: `col-12` full width
- Nút: `d-flex gap-2 mt-4`

### 5.3. Thứ tự tab (UX)

1. Mã căn hộ (autofocus khuyến nghị)  
2. Tòa nhà → Tầng → Diện tích  
3. Loại hình → Trạng thái → Ghi chú  
4. Lưu  

### 5.4. Nhãn bắt buộc

- Field required: label kèm `<span class="text-danger">*</span>`
- `form-text` mô tả ngắn dưới field (format mã, mặc định status…)

### 5.5. Responsive

| Breakpoint | Hành vi |
|------------|---------|
| `< md` | 1 cột, full width |
| `≥ md` | 3 cột field / hàng |

---

## 6. Thông báo lỗi

### 6.1. Vị trí hiển thị

| Loại | Vị trí | Component |
|------|--------|-----------|
| Lỗi validate / trùng mã / insert fail | **Trên đầu form** (trong content) | `alert alert-danger` + danh sách `<ul>` |
| Thành công sau redirect list | Đầu content list (layout flash) | `alert alert-success` (FlashUtil) |
| Không đủ quyền | Flash + redirect list/dashboard | `alert alert-danger` |

### 6.2. Format block lỗi trên form

```text
⚠ Không thể lưu căn hộ:
  • Vui lòng nhập mã căn hộ.
  • Diện tích phải từ 15 m² trở lên (tối đa 10.000 m²).
```

JSP:

```jsp
<c:if test="${not empty errors}">
  <div class="alert alert-danger">...</div>
</c:if>
```

### 6.3. Bảng message (map field)

| Tình huống | Message |
|------------|---------|
| Thiếu mã | Vui lòng nhập mã căn hộ. |
| Sai format mã | Mã căn hộ chỉ gồm chữ, số, gạch ngang hoặc gạch dưới (tối đa 20 ký tự). |
| Trùng mã | Mã căn hộ đã tồn tại. |
| Thiếu tòa | Vui lòng nhập tòa nhà. |
| Tòa quá dài | Tên tòa nhà tối đa 50 ký tự. |
| Sai tầng | Tầng phải là số nguyên từ 0 đến 200. |
| Sai diện tích | Diện tích phải từ 15 m² trở lên (tối đa 10.000 m²). |
| Sai loại hình | Loại hình sử dụng không hợp lệ (OWNED / RENTED). |
| Sai trạng thái | Trạng thái không hợp lệ (ACTIVE / INACTIVE). |
| Notes dài | Ghi chú tối đa 500 ký tự. |
| Lỗi DB | Không thể thêm căn hộ. Vui lòng thử lại. |
| Không quyền | Bạn không có quyền thêm căn hộ. |
| Thành công | Thêm căn hộ thành công. |

### 6.4. Hành vi giữ form khi lỗi

- `request.setAttribute("form", form)`
- Input bind lại: `value="${form.apartmentCode}"` …
- Select: `selected` theo `${form.occupancyType}` / `${form.status}`

### 6.5. Không dùng

- `alert()` JS cho mọi lỗi validate (chỉ optional confirm sau này)
- Trang trắng / stacktrace cho user

---

## 7. Trạng thái màn hình

| State | UI |
|-------|-----|
| Lần đầu mở | Form trống + default OWNED/ACTIVE |
| Submit lỗi | Alert đỏ + field giữ value |
| Submit OK | Redirect list + flash xanh |
| Không quyền | Không vào form / flash + rời trang |
| Chưa login | Redirect login |

---

## 8. Liên kết list → form

Trên `list.jsp` (chỉ khi `canCreate = true`):

```text
[ + Thêm căn hộ ]  →  /apartment?action=create
```

---

## 9. Checklist implement UI

- [x] Field đủ theo bảng mục 2  
- [x] Required đánh dấu `*`  
- [x] Placeholder / form-text  
- [x] Select occupancy + status  
- [x] Nút Lưu + Hủy  
- [x] Layout Bootstrap 3 cột  
- [x] Alert errors  
- [x] Flash success sau redirect  
- [x] Giữ form khi lỗi  

**File hiện tại:** `web/WEB-INF/views/apartment/form.jsp`, `list.jsp`
