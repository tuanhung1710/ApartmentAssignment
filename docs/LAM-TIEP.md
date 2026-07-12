# Làm tiếp project (đọc file này trước)

> Mỗi lần mở lại Claude / NetBeans / máy mới — đọc file này 2 phút rồi làm tiếp.

---

## 1. Thông tin project

| Mục | Giá trị |
|-----|---------|
| Thư mục | `D:\PRJ301\tv2` |
| Repo gốc | `https://github.com/tuanhung1710/ApartmentAssignment.git` |
| Nhánh local | `tv2` (đã tạo; push GitHub có thể chưa được nếu thiếu quyền) |
| Stack | NetBeans Ant · Tomcat **10** · Jakarta · SQL Server · JDBC · Lombok · JSP |
| Module bạn | **TV2 – Căn hộ** |
| UC đã làm | UC-01..04 + **UC-05 Chi tiết căn hộ** |

---

## 2. Đã xong những gì

### Code
- [x] Model `Apartment`
- [x] `ApartmentDAO` (+ findWithFilters, countWithFilters, updateStatus, delete…)
- [x] `ApartmentController` (CRUD-ish + list filter/sort/page)
- [x] JSP list đầy đủ filter · search · sort · pagination · empty · loading
- [x] Constants occupancy/status
- [x] SQL `database/apartments.sql`

### Docs (trong `docs/`)
- [x] `user-story-them-can-ho.md` — UC-APT-01
- [x] `ui-form-them-can-ho.md` / `validation-rules-them-can-ho.md` / `test-cases-them-can-ho.md`
- [x] `uc-apt-02-cap-nhat-can-ho.md` — UC-APT-02
- [x] `uc-apt-03-vo-hieu-hoa-can-ho.md` — UC-APT-03
- [x] `uc-apt-04-danh-sach-can-ho.md` — UC-APT-04
- [x] `uc-apt-05-chi-tiet-can-ho.md` — UC-APT-05
- [x] `coding-standards.md` (root)
- [x] `LAM-TIEP.md` — file này

### Chưa xong / việc tiếp
- [ ] Run app + smoke list + **detail** (chủ/thuê/thành viên/lịch sử empty OK)
- [x] UC **Sửa / cập nhật căn hộ** (UC-APT-02)
- [x] UC **Vô hiệu hóa / Xóa** căn hộ (UC-APT-03)
- [x] UC **Danh sách căn hộ** (UC-APT-04)
- [x] UC **Chi tiết căn hộ** (UC-APT-05)
- [ ] Gán cư dân / Căn hộ của tôi (nếu đề yêu cầu)
- [ ] Push nhánh `tv2` lên GitHub (cần quyền Collaborator)

---

## 3. Mỗi lần ngồi vào máy — 5 phút setup

```text
1. Mở NetBeans → Open Project → D:\PRJ301\tv2
2. SQL Server đang chạy
3. DBContext password đúng máy bạn
4. Tomcat 10 đã gắn
5. Clean and Build → Run
6. Login manager / 123456
```

Kiểm tra nhánh git (terminal tại `D:\PRJ301\tv2`):

```bash
git status
git branch
```

Phải thấy đang ở `tv2`.

---

## 4. Làm tiếp hôm nay — chọn 1 hướng

### Hướng A — Chưa test được app
1. Chạy `database/apartments.sql`
2. (Nếu login fail) chạy schema/seed users từ team
3. Sửa `DBContext` → Run File → Connected OK
4. Run app → smoke test trong `test-cases-them-can-ho.md` mục D:
   - TC-001, 101, 103, 109, 116

### Hướng B — App đã chạy OK → code UC mới
Làm **Sửa căn hộ** theo `coding-standards.md` mục 12:

```text
DAO update / findById
→ Controller action=edit (GET) + action=update (POST)
→ form.jsp tái sử dụng (hidden id)
→ list.jsp nút Sửa
→ test
```

### Hướng C — Viết thêm docs / test
- Chạy full test cases, tick Pass/Fail trong `test-cases-them-can-ho.md`

### Hướng D — Nộp / Git
```bash
git add .
git commit -m "feat(tv2): mô tả ngắn"
git push -u origin tv2
```
Nếu 403 → nhờ `tuanhung1710` add Collaborator cho account GitHub của bạn.

---

## 5. File quan trọng (đọc khi quên)

| Cần | Mở file |
|-----|---------|
| Cách code | `coding-standards.md` |
| BA Thêm căn hộ | `docs/user-story-them-can-ho.md` |
| UI | `docs/ui-form-them-can-ho.md` |
| Validate | `docs/validation-rules-them-can-ho.md` |
| Test | `docs/test-cases-them-can-ho.md` |
| Controller | `src/java/.../controller/apartment/ApartmentController.java` |
| DAO | `src/java/.../dao/ApartmentDAO.java` |
| Form | `web/WEB-INF/views/apartment/form.jsp` |
| DB connect | `src/java/.../dal/DBContext.java` |

### Flow nhớ 10 giây
```text
JSP form → Controller (login, role, bind, validate)
→ DAO SQL → redirect list + flash
```

---

## 6. Khi chat lại với Claude / AI

Gửi **1 trong các câu sau** (copy được):

```text
Tiếp TV2 project D:\PRJ301\tv2 nhánh tv2.
Đọc docs/LAM-TIEP.md rồi cùng tôi [test smoke / code sửa căn hộ / fix lỗi ...].
```

Hoặc ngắn:

```text
Làm tiếp apartment TV2 — đọc LAM-TIEP.md
```

Kèm thêm nếu có:
- Đang kẹt lỗi gì (dán log)
- Đã test được chưa (rồi / chưa)
- Muốn làm UC nào tiếp

---

## 7. Prompt gợi ý khi tiếp tục

**Test:**
```text
Hướng dẫn tôi chạy smoke test Thêm căn hộ từng case
```

**Code UC mới:**
```text
Cùng tôi code UC Sửa căn hộ theo coding-standards.md
```

**Fix:**
```text
Run app bị lỗi: [dán lỗi]
```

**Git:**
```text
Giúp tôi commit nhánh tv2 và hướng dẫn push
```

---

## 8. Cập nhật file này khi xong việc

Mỗi lần làm xong 1 phần, sửa mục **2. Đã xong** (tick thêm) và ghi 1 dòng:

```text
## Nhật ký
- 2026-07-12: xong create + docs UI/validation/test
- YYYY-MM-DD: ...
```

### Nhật ký

- 2026-07-12: Clone repo, nhánh `tv2`, UC Thêm căn hộ (code + BA + UI + validation + test cases), cập nhật coding-standards.
- 2026-07-12: UC-APT-02 Cập nhật căn hộ — đặc tả `uc-apt-02-cap-nhat-can-ho.md` + DAO.update + edit/update controller + form/list.
- 2026-07-12: UC-APT-03 Vô hiệu hóa/Xóa — BR docs + deactivate/activate/delete + audit console + list buttons.
- 2026-07-12: UC-APT-04 Danh sách — filter/search/sort/pagination/empty/loading + docs.
- 2026-07-12: UC-APT-05 Chi tiết — detail.jsp + residents/members/history DAO + role check + coding-standards.
