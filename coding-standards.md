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

## 7. Package map

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

## 8. Checklist PR / commit

- [ ] Jakarta only  
- [ ] Entity: Lombok + wrapper + không nested object  
- [ ] Date: `java.sql.Date`  
- [ ] DAO: open/close + `getFromResultSet`  
- [ ] Servlet: `@WebServlet` + `action` switch + method tách  
- [ ] JSP: Bootstrap + JSTL + EL; JS không lạm dụng `${}`  
- [ ] Không commit password máy cá nhân nếu fork public (nhắc đổi sau demo)  
