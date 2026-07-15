# Validation Rules – Thêm căn hộ (US-APT-01)

| Mục | Nội dung |
|-----|----------|
| **UC** | Thêm căn hộ |
| **Áp dụng** | `POST /apartment` · `action=create` |
| **Code** | `ApartmentController.validateCreate` + `existsByCode` + HTML form |
| **Fail behavior** | Không insert; forward form + `List<String> errors` + giữ `form` |

---

## 1. Tổng quan rule

| Nhóm | Rule ID | Mô tả ngắn |
|------|---------|------------|
| AuthZ | VR-AUTH-01 | Chỉ ADMIN, MANAGER |
| Code | VR-CODE-* | Mã căn hộ |
| Building | VR-BLD-* | Tòa nhà |
| Floor | VR-FLR-* | Tầng |
| Area | VR-AREA-* | Diện tích |
| Owner type | VR-OCC-* | occupancyType (OWNED/RENTED) |
| Status | VR-ST-* | ACTIVE/INACTIVE |
| Notes | VR-NOTE-* | Ghi chú |
| Duplicate | VR-DUP-01 | Unique apartment_code |
| Format | VR-FMT-* | Parse / charset / trim |
| Boundary | VR-BND-* | Min/max biên |
| DB | VR-DB-01 | Insert fail |

---

## 2. Auth / quyền (trước validate field)

| Rule ID | Điều kiện | Pass | Fail message |
|---------|-----------|------|--------------|
| VR-AUTH-01 | User đã login và role ∈ {ADMIN, MANAGER} | Tiếp tục | Bạn không có quyền thêm căn hộ. |
| VR-AUTH-02 | Chưa login | — | Redirect `/auth?action=login` |

---

## 3. Apartment Code (`apartmentCode`) — **tự sinh, không nhập form**

User **không** nhập mã khi create. Server sinh sau khi validate tòa + tầng.

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-CODE-01 | **Auto-generate** | `{TOKEN}-{FF}{UU}` · TOKEN từ tên tòa (A, Tòa B→B) · FF = tầng 2 số · UU = unit 2 số trên tầng | — |
| VR-CODE-02 | **Bỏ qua input client** | `form.setApartmentCode(null)` rồi gán mã sinh | (im lặng) |
| VR-CODE-03 | **Độ dài** | ≤ 20 (cột DB) | Không thể sinh mã căn hộ… |
| VR-CODE-04 | **Unit tăng nếu trùng** | Thử unit từ count+1 → 99, lấy mã `existsByCode == false` đầu tiên | — |
| VR-DUP-01 | **Duplicate check** | Sau khi sinh: `existsByCode` phải false | Đã tồn tại căn hộ với mã {code} ({identity}). |

### Format hiển thị định danh

`[tên tòa] - [số tầng] [mã căn]` · ví dụ: **`A - 4 A-0401`**

### Ví dụ sinh mã

| Tòa nhập | Tầng | Unit | Mã sinh | Hiển thị |
|----------|------|------|---------|----------|
| `A` | 4 | 1 | `A-0401` | `A - 4 A-0401` |
| `Tòa B` | 3 | 2 | `B-0302` | `Tòa B - 3 B-0302` |
| `A` | 12 | 1 | `A-1201` | `A - 12 A-1201` |
| Trùng mã đã có | — | — | Fail VR-DUP-01 | Message tồn tại |

---

## 4. Building (`building`)

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-BLD-01 | Required | Không rỗng sau trim | Vui lòng nhập tòa nhà. |
| VR-BLD-02 | Max length | ≤ 50 | Tên tòa nhà tối đa 50 ký tự. |
| VR-BLD-03 | Trim | trim trước validate | (im lặng) |

---

## 5. Floor (`floorNumber`)

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-FLR-01 | Required | Phải parse được thành Integer | Tầng phải là số nguyên từ 0 đến 200. |
| VR-FLR-02 | **Data format** | Integer (không thập phân hợp lệ kiểu `"1.5"` → fail parse) | Cùng VR-FLR-01 |
| VR-FLR-03 | **Min boundary** | ≥ **0** (tầng trệt = 0 OK) | Cùng VR-FLR-01 |
| VR-FLR-04 | **Max boundary** | ≤ **200** | Cùng VR-FLR-01 |
| VR-FLR-05 | Không chữ | `"tầng 1"`, `"abc"` → fail | Cùng VR-FLR-01 |

### Boundary Floor

| Giá trị | Kết quả |
|---------|---------|
| `-1` | Fail (dưới min) |
| `0` | Pass (biên dưới) |
| `1` | Pass |
| `200` | Pass (biên trên) |
| `201` | Fail (trên max) |
| `12.5` | Fail format |
| `` rỗng | Fail required/format |

---

## 6. Area (`areaM2`)

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-AREA-01 | Required | Parse được `BigDecimal` | Diện tích phải từ 15 m² trở lên (tối đa 10.000 m²). |
| VR-AREA-02 | **Data format** | Số thập phân; cho phép `,` → `.` trước parse | Cùng VR-AREA-01 |
| VR-AREA-03 | Scale | Làm tròn **2** chữ số thập phân (`HALF_UP`) khi bind | (im lặng) |
| VR-AREA-04 | **Min boundary** | ≥ **15** m² | Cùng VR-AREA-01 |
| VR-AREA-05 | **Max boundary** | ≤ **10000** | Cùng VR-AREA-01 |
| VR-AREA-06 | Dưới min / âm / zero | `0`, `14.99`, `-1` fail | Cùng VR-AREA-01 |

### Boundary Area

| Giá trị | Kết quả |
|---------|---------|
| `0` | Fail |
| `14.99` | Fail (dưới min) |
| `15` | Pass (biên dưới) |
| `15.00` | Pass (biên dưới) |
| `75.5` | Pass |
| `10000` | Pass (biên trên) |
| `10000.01` | Fail |
| `-0.01` | Fail |
| `abc` | Fail format |
| `75,50` | Pass (đổi `,` → `.`) |

---

## 7. Owner Type / Occupancy (`occupancyType`)

> Trên UI gọi **Loại hình**; BA/DB: `occupancy_type` · giá trị **OWNED | RENTED** (Owner Type).

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-OCC-01 | Required | Không null/rỗng | Loại hình sử dụng không hợp lệ (OWNED / RENTED). |
| VR-OCC-02 | Enum only | Chỉ `OWNED` hoặc `RENTED` | Cùng VR-OCC-01 |
| VR-OCC-03 | Case-sensitive | `owned` / `Owned` → **Fail** (đúng chuẩn app) | Cùng VR-OCC-01 |
| VR-OCC-04 | Default UI | Form GET mặc định `OWNED` | — |

| Input | Kết quả |
|-------|---------|
| `OWNED` | Pass |
| `RENTED` | Pass |
| `LEASE` | Fail |
| `` | Fail |
| `owned` | Fail |

---

## 8. Status (`status`)

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-ST-01 | Optional trên UI | Có thể không chọn / rỗng | — |
| VR-ST-02 | **Default** | null/rỗng → gán `ACTIVE` lúc bind | — |
| VR-ST-03 | Enum only | Chỉ `ACTIVE` hoặc `INACTIVE` | Trạng thái không hợp lệ (ACTIVE / INACTIVE). |
| VR-ST-04 | Case-sensitive | `active` → Fail | Cùng VR-ST-03 |

| Input | Sau bind | Kết quả validate |
|-------|----------|------------------|
| (không gửi) | `ACTIVE` | Pass |
| `ACTIVE` | `ACTIVE` | Pass |
| `INACTIVE` | `INACTIVE` | Pass |
| `DELETED` | `DELETED` | Fail |
| `active` | `active` | Fail |

---

## 9. Notes (`notes`) – bổ trợ

| Rule ID | Rule | Chi tiết | Message |
|---------|------|----------|---------|
| VR-NOTE-01 | Optional | Rỗng / null OK | — |
| VR-NOTE-02 | Max length | ≤ 500 sau trim | Ghi chú tối đa 500 ký tự. |
| VR-NOTE-03 | Lưu DB | Rỗng → lưu `null` | — |

---

## 10. Duplicate Check

| Rule ID | Field | Cách check | Thời điểm | Message |
|---------|-------|------------|-----------|---------|
| VR-DUP-01 | `apartmentCode` | `SELECT 1 FROM apartments WHERE apartment_code = ?` | Sau validate field, trước insert | Mã căn hộ đã tồn tại. |

**Ghi chú:**
- So khớp đúng chuỗi sau trim (phụ thuộc collation SQL Server: thường case-insensitive với CI collation).
- Không cho insert khi trùng — kể cả race condition vẫn bị UNIQUE constraint DB chặn → VR-DB-01.

---

## 11. Data Format (tổng hợp)

| Rule ID | Field | Format hợp lệ |
|---------|-------|---------------|
| VR-FMT-01 | `apartmentCode` | Regex `^[A-Za-z0-9][A-Za-z0-9_-]{0,19}$` |
| VR-FMT-02 | `floorNumber` | Integer thập phân không; parse `Integer.valueOf` |
| VR-FMT-03 | `areaM2` | Decimal; `,` hoặc `.` ; scale 2 |
| VR-FMT-04 | `occupancyType` | Exact `OWNED` \| `RENTED` |
| VR-FMT-05 | `status` | Exact `ACTIVE` \| `INACTIVE` |
| VR-FMT-06 | All string | `trim()` trước validate |
| VR-FMT-07 | Charset form | UTF-8 (EncodingFilter / pageEncoding) |

---

## 12. Boundary Value (bảng tổng)

| Field | Min (pass) | Max (pass) | Dưới min (fail) | Trên max (fail) |
|-------|------------|------------|-----------------|-----------------|
| `apartmentCode` length | 1 | 20 | 0 | 21 |
| `building` length | 1 | 50 | 0 | 51 |
| `floorNumber` | 0 | 200 | -1 | 201 |
| `areaM2` | 15 | 10000 | 14.99, 0, -1 | 10000.01 |
| `notes` length | 0 | 500 | — | 501 |

---

## 13. Thứ tự chạy validate (server)

```text
1. VR-AUTH (login + role)
2. bindForm (trim + parse + default status)
3. validateCreate:
     CODE → BUILDING → FLOOR → AREA → OCCUPANCY → STATUS → NOTES
4. VR-DUP-01 existsByCode
5. insert → VR-DB-01 nếu fail
```

Có thể trả **nhiều lỗi cùng lúc** trong 1 list (validateCreate gom hết field rules).  
Duplicate check chạy **sau** khi field rules pass.

---

## 14. DB constraint bổ sung (tầng DB)

| Constraint | Ý nghĩa |
|------------|---------|
| `UQ_apartments_code` | Unique code |
| `CK_apartments_floor` | 0–200 |
| `CK_apartments_area` | > 0 và ≤ 10000 |
| `CK_apartments_occupancy` | OWNED/RENTED |
| `CK_apartments_status` | ACTIVE/INACTIVE |

App validate trước; DB là lớp bảo vệ cuối.

| Rule ID | Khi insert exception / return -1 | Message |
|---------|----------------------------------|---------|
| VR-DB-01 | Lỗi JDBC / connection null / unique race | Không thể thêm căn hộ. Vui lòng thử lại. |

---

## 15. Map rule → code

| Rule | Vị trí code |
|------|-------------|
| Auth | `handleCreate` + `canCreate` |
| Trim/parse/default | `bindForm` |
| Field rules | `validateCreate` |
| Pattern code | `CODE_PATTERN` |
| Area min/max | `AREA_MIN`, `AREA_MAX` |
| Duplicate | `apartmentDAO.existsByCode` |
| Insert | `apartmentDAO.insert` |
| HTML hỗ trợ | `form.jsp` required/min/max/pattern |

---

## 16. Out of scope validate (UC này không check)

- Owner user id / gán cư dân  
- Building phải thuộc danh mục tòa có sẵn  
- Upload ảnh  
- Mã căn auto-generate  

---

**Tài liệu liên quan:**  
- `docs/user-story-them-can-ho.md`  
- `docs/ui-form-them-can-ho.md`  
- `docs/test-cases-them-can-ho.md`
