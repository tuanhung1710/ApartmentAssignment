# Coding Standards – ApartmentManagement (PRJ301)

> Bắt buộc tuân thủ khi code. Stack: **Jakarta**, NetBeans Ant, Tomcat 10, SQL Server, JDBC, Lombok, JSP/JSTL/Bootstrap.

---

## 1. Thư viện & kiểu dữ liệu

1. **Luôn dùng Jakarta** (`jakarta.servlet.*`, không `javax.servlet.*`).
2. Entity cần **date** → dùng `java.sql.Date` (không `LocalDate` / `java.util.Date` trừ khi có lý do rõ).
3. **Không dùng `db.properties`**. Connection hardcode trong `DBContext` (đổi user/pass/url trên máy local).

### Dependencies (`web/WEB-INF/lib/`)

| Jar | Mục đích |
|-----|----------|
| `mssql-jdbc-*.jar` | SQL Server |
| `lombok.jar` | Entity annotations (+ enable annotation processing NetBeans) |
| `jakarta.servlet.jsp.jstl-api-*.jar` + `jakarta.servlet.jsp.jstl-*.jar` | JSTL |

---

## 2. Entity (Model)

1. **Luôn dùng Lombok**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor` (có thể thêm `@Getter/@Setter/@ToString` nếu team quen).
2. Thuộc tính dùng **wrapper**, không primitive: `Integer`, `Boolean`, `Double`, `BigDecimal`, …
3. **Không nhúng object quan hệ** (không `private Role role`). Chỉ lưu **FK id** hoặc cột phẳng từ DB:

```java
// SAI
private Role role;

// ĐÚNG (nếu DB có role_id)
private Integer roleId;

// ĐÚNG với schema hiện tại (cột role NVARCHAR)
private String role; // ADMIN | MANAGER | STAFF | RESIDENT
```

4. Tên field map gần với cột DB; map chi tiết trong `getFromResultSet`.

---

## 3. DBContext

- Package: `apartmentmanagement.dal.DBContext`
- Field protected: `connection`, `resultSet`, `statement`
- Constructor mở kết nối (SQL Server driver)
- `closeResources()` đóng RS → Statement → Connection
- `getConnection()` trả connection mới (`return new DBContext().connection`)
- Có `main` test in connection

**Không** đọc file properties.

---

## 4. DAO

1. `extends DBContext`
2. Mỗi method: `connection = getConnection()` → thao tác → `finally { closeResources(); }`
3. Luôn có **`getFromResultSet(ResultSet rs)`** để tái sử dụng map entity
4. Insert có generated key khi bảng `IDENTITY`
5. Lỗi: log / `System.out`, return giá trị an toàn (`null`, `-1`, `false`, list rỗng)

Mẫu method:

```java
public User getFromResultSet(ResultSet rs) throws SQLException { ... }

public int insert(User user) {
    String sql = "...";
    try {
        connection = getConnection();
        statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // set params...
        int affected = statement.executeUpdate();
        // generated keys...
    } catch (SQLException ex) {
        System.out.println("Error: " + ex.getMessage());
        return -1;
    } finally {
        closeResources();
    }
}
```

---

## 5. Servlet / Controller

1. Map URL bằng **`@WebServlet`** — **không** khai báo servlet trong `web.xml`.
2. Một resource/module = **một controller** (vd. `ManageAccountServlet`, không tách Create/Edit servlet).
3. Auth gộp **`AuthenController`** (login, logout, profile, đổi MK…).
4. `doGet` / `doPost`: lấy `action` → **`switch`** → gọi **hàm riêng** (không viết logic dài trong `case`).
5. Filter: `@WebFilter` (auth / encoding).

```java
String action = request.getParameter("action");
if (action == null) action = "list";
switch (action) {
    case "list": handleList(request, response); break;
    case "edit": handleEditForm(request, response); break;
    // ...
    default: response.sendError(404); break;
}
```

### Phân trang + filter (controller)

- Parse `page` (mặc định 1, `pageSize` cố định vd. 10)
- Gọi DAO `find...WithFilters(..., page, pageSize)` + `getTotalFiltered...`
- `totalPages = ceil(total / pageSize)`
- Set attribute: list, `currentPage`, `totalPages`, filter values → forward JSP

---

## 6. JSP

1. **Bootstrap** (CDN được)
2. **JSTL** + **EL**
3. View dưới `web/WEB-INF/views/...` (đi qua servlet)
4. Include common: header/sidebar/footer/css/js
5. **JavaScript trong JSP**: tránh `${...}` trong script (dễ vỡ). Dùng concatenation:

```jsp
<script>
    function confirmDeactivate(id) {
        if (confirm('...')) {
            window.location.href = '<%= request.getContextPath() %>/manage-user?action=deactivate&id=' + id;
        }
    }
</script>
```

Hoặc gán biến từ server một lần cẩn thận; **không** nhét EL phức tạp giữa JS.

### Pagination URL

Dùng `<c:url>` + `<c:param>` giữ filter, rồi `&page=`:

```jsp
<c:url value="/manage-user" var="paginationUrl">
    <c:param name="action" value="list" />
    <c:if test="${not empty param.role}">
        <c:param name="role" value="${param.role}" />
    </c:if>
</c:url>
<a class="page-link" href="${paginationUrl}&page=${i}">${i}</a>
```

---

## 7. Responsive (bắt buộc – thiết bị phổ biến)

App phải dùng được trên **mobile, tablet, desktop**. Dùng **Bootstrap 5 grid + utility**; CSS riêng chỉ bổ sung shell (`app.css`).

### 7.1. Breakpoint mục tiêu

| Nhóm | Độ rộng tham chiếu | Bootstrap |
|------|--------------------|----------|
| Mobile | 360 – 430 (iPhone SE → Pro Max) | `< md` / `< lg` |
| Tablet | 768 – 1024 (iPad dọc/ngang) | `md` – `lg` |
| Desktop / laptop | ≥ 1280 | `≥ lg` / `xl` |

Breakpoints BS5: `sm` 576 · `md` 768 · `lg` 992 · `xl` 1200 · `xxl` 1400.

### 7.2. Quy tắc bắt buộc

1. **Viewport** trong mọi layout / trang public:

   ```html
   <meta name="viewport" content="width=device-width, initial-scale=1">
   ```

2. **Không** set `width` cố định px cho layout chính / bảng full-page (vd. `width: 1200px`). Ưu tiên `%`, `max-width`, grid Bootstrap.

3. **Shell (TV1 – `layout.jsp` + `app.css`)**  
   - Desktop (`≥ lg` 992px): sidebar cố định.  
   - Mobile / tablet dọc (`< lg`): sidebar ẩn → **hamburger + Bootstrap Offcanvas** (cùng menu role).  
   - **Cấm** `display: none` sidebar mà không có menu thay thế.

4. **Grid content** – luôn stack trên mobile:

   | Thành phần | Class gợi ý |
   |------------|-------------|
   | Stat card 3 cột | `col-12 col-sm-6 col-lg-4` |
   | Stat card 4 cột | `col-12 col-sm-6 col-xl-3` |
   | Form 2 cột | `col-12 col-md-6` |
   | Label/value profile | `col-12 col-sm-3` / `col-12 col-sm-9` |

5. **Bảng list** – bọc luôn:

   ```jsp
   <div class="table-responsive">
       <table class="table table-hover align-middle">...</table>
   </div>
   ```

6. **Nút / toolbar** card header: cho wrap trên mobile  
   `d-flex flex-column flex-sm-row gap-2` (hoặc `flex-wrap`).

7. **Ẩn bớt chữ, giữ icon** khi hẹp (đã dùng ở topbar):  
   `d-none d-md-inline` cho tên user dài; role badge vẫn hiện.

8. **Form**: `input` full width trong cột; nút primary không tràn màn hình hẹp (`w-100 w-sm-auto` nếu cần).

9. **Ảnh / hero (trang public)**: `max-width: 100%`; ẩn cột ảnh trang trí trên mobile (`d-none d-md-block`).

10. **Không** phụ thuộc hover-only cho thao tác chính (mobile không hover). Dùng click/tap.

### 7.3. Ai chịu trách nhiệm

| Phần | Owner |
|------|--------|
| `layout.jsp`, `header`, `sidebar`, offcanvas, `app.css` shell | **TV1** |
| Grid / table / form trong JSP module | **Owner module** (TV2–TV5) |
| Trang login / public auth | **TV1** |

TV2–TV5 **không** sửa `layout.jsp` / `sidebar` để “fix mobile” — chỉ dùng class Bootstrap trong content fragment.

### 7.4. Checklist trước khi merge UI

- [ ] Có `viewport` meta  
- [ ] Mở DevTools: **390×844** (mobile) — menu mở được, không mất nav  
- [ ] **768×1024** (tablet) — form/table không tràn ngang vô hạn  
- [ ] **1280+** — sidebar + content ổn  
- [ ] Table có `.table-responsive`  
- [ ] Card/stat dùng `col-12` + breakpoint, không chỉ `col-md-*`  
- [ ] Không scroll ngang toàn trang (`overflow-x` body)

---

## 8. Package map

```
apartmentmanagement
├── dal          → DBContext
├── model        → Entity + Lombok
├── dao          → *DAO extends DBContext
├── filter       → AuthFilter, EncodingFilter
├── controller
│   ├── auth     → AuthenController
│   ├── apartment
│   ├── fee
│   ├── request
│   └── admin
└── util         → helper (flash message, constants) — không chứa connection config
```

---

## 9. Checklist PR / commit

- [ ] Jakarta only  
- [ ] Entity: Lombok + wrapper + không nested object  
- [ ] Date: `java.sql.Date`  
- [ ] DAO: open/close + `getFromResultSet`  
- [ ] Servlet: `@WebServlet` + `action` switch + method tách  
- [ ] JSP: Bootstrap + JSTL + EL; JS không lạm dụng `${}`  
- [ ] **Responsive:** viewport + grid `col-12…` + table-responsive + menu mobile (shell TV1)  
- [ ] Không commit password máy cá nhân nếu fork public (nhắc đổi sau demo)  

