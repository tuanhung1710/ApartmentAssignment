# Hướng dẫn code chi tiết – ApartmentManagement (PRJ301)

> Tài liệu này = **coding standards + cách làm từng bước**.  
> Stack: **Jakarta**, NetBeans Ant, Tomcat **10**, SQL Server, JDBC, Lombok, JSP/JSTL/Bootstrap.  
> Đọc xong bạn phải **tự viết được 1 use case** theo đúng khung.

---

## 0. Trước khi code — checklist máy

1. JDK **17**
2. NetBeans + project mở tại thư mục gốc (có `build.xml`, `nbproject/`)
3. Tomcat **10** (Jakarta) — không dùng Tomcat 9
4. SQL Server chạy, DB `ApartmentManagement`
5. Sửa `DBContext` user/password máy bạn
6. Run `DBContext.java` → thấy `Connected OK`
7. Clean and Build → Run → login được

---

## 1. Kiến trúc bắt buộc (nhớ thuộc)

```text
Browser
  → Filter (Auth / Encoding)
  → Controller (@WebServlet)     // nhận request, validate, phân quyền
  → DAO extends DBContext        // SQL only
  → SQL Server
  → Controller setAttribute
  → JSP (WEB-INF/views/...)      // chỉ hiển thị
```

| Lớp | Package | Được làm | Không được làm |
|-----|---------|----------|----------------|
| **Model** | `apartmentmanagement.model` | Field + Lombok | SQL, HTML, request |
| **DAO** | `apartmentmanagement.dao` | SQL, map ResultSet | check role, redirect, HTML |
| **Controller** | `apartmentmanagement.controller.*` | action switch, validate, gọi DAO, forward/redirect | SQL dài, HTML dài |
| **JSP** | `web/WEB-INF/views/...` | form, table, JSTL/EL | JDBC, logic nghiệp vụ nặng |
| **util** | `apartmentmanagement.util` | constants, flash helper | connection config |
| **dal** | `apartmentmanagement.dal` | `DBContext` only | — |

### Package map

```text
apartmentmanagement
├── dal          → DBContext
├── model        → Entity + Lombok
├── dao          → *DAO extends DBContext
├── filter       → AuthFilter, EncodingFilter
├── controller
│   ├── auth     → AuthenController
│   ├── apartment→ TV2
│   ├── fee      → TV3
│   ├── request  → TV4
│   └── admin    → TV5
└── util         → AppConstants, FlashUtil
```

---

## 2. Luật cứng (vi phạm = sai chuẩn team)

1. **Luôn Jakarta**: `jakarta.servlet.*` — **cấm** `javax.servlet.*`
2. **Không** dùng `db.properties` — connection hardcode trong `DBContext`
3. Entity dùng **wrapper**: `Integer`, `Boolean`, `BigDecimal` — không `int`/`boolean` primitive cho field model
4. Date trong entity: ưu tiên `java.sql.Date` / `java.sql.Timestamp` (đúng kiểu cột)
5. Entity **không nhúng object quan hệ** (không `private User owner`) — chỉ id / cột phẳng
6. Servlet map bằng **`@WebServlet`** — **không** khai báo servlet trong `web.xml`
7. 1 module / resource = **1 controller** (vd. `ApartmentController`, không tách CreateServlet + EditServlet)
8. `doGet` / `doPost`: lấy `action` → **`switch`** → gọi **hàm riêng**
9. DAO: mỗi method `connection = getConnection()` → SQL → **`finally { closeResources(); }`**
10. DAO luôn có **`getFromResultSet(ResultSet rs)`**
11. JSP nằm dưới `web/WEB-INF/views/...` — user không gọi JSP trực tiếp
12. JSP: Bootstrap + JSTL + EL; JS **tránh** nhét `${...}` phức tạp giữa script

---

## 3. Quy trình làm 1 Use Case (làm đúng thứ tự)

Ví dụ UC: **Thêm / Sửa / Xóa / List** bất kỳ.

### Bước 1 — Chốt BA (5 phút, trên giấy)

Trả lời trước khi gõ code:

| Câu hỏi | Ví dụ (Thêm căn hộ) |
|---------|---------------------|
| Ai được làm? | ADMIN, MANAGER |
| URL? | `/apartment` |
| action GET? | `create` (mở form) |
| action POST? | `create` (lưu) |
| Field nào? | building, floor, area, occupancy, status, notes (**mã tự sinh**) |
| Validate gì? | required tòa/tầng/area, min/max, unique mã sinh ra |
| Message lỗi/thành công? | xem US-APT-01 |
| Sau OK đi đâu? | redirect detail/list + flash (kèm định danh) |

### Bước 2 — DB

- Có bảng/cột chưa? Nếu chưa → viết SQL trong `database/`
- Cột nào `IDENTITY`, `UNIQUE`, `NOT NULL`?

### Bước 3 — Model

- File: `src/java/apartmentmanagement/model/Xxx.java`
- Lombok đủ 4 annotation
- Field wrapper + đúng nghĩa cột

### Bước 4 — DAO

- File: `src/java/apartmentmanagement/dao/XxxDAO.java`
- `extends DBContext`
- `getFromResultSet`
- method: `insert` / `update` / `findById` / `findAll` / `exists...`

### Bước 5 — Controller

- File: `controller/<module>/XxxController.java`
- `@WebServlet("/path")`
- `doGet` / `doPost` + switch action
- handler: login → role → bind → validate → DAO → forward/redirect

### Bước 6 — JSP

- `web/WEB-INF/views/<module>/list.jsp`, `form.jsp`
- `name` input **khớp** `getParameter`
- forward qua `layout.jsp` (`contentPage`)

### Bước 7 — Test

- Happy path
- Thiếu field
- Sai quyền
- Trùng dữ liệu (nếu có)
- Lỗi DB không crash trang trắng

---

## 4. MODEL — mẫu bắt buộc

```java
package apartmentmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    private Integer apartmentId;
    private String apartmentCode;
    private String building;
    private Integer floorNumber;
    private BigDecimal areaM2;
    /** OWNED | RENTED */
    private String occupancyType;
    /** ACTIVE | INACTIVE */
    private String status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
```

### Đúng / Sai

```java
// SAI
private int floorNumber;
private Role role;
private User owner;

// ĐÚNG
private Integer floorNumber;
private String role;       // nếu DB lưu NVARCHAR role
private Integer ownerId;   // nếu chỉ cần FK
```

---

## 5. DBContext — chỉ dùng, không copy lung tung

- Package: `apartmentmanagement.dal.DBContext`
- Field protected: `connection`, `statement`, `resultSet`
- `getConnection()` mở connection
- `closeResources()` đóng RS → Statement → Connection
- **Không** đọc file properties
- Đổi user/pass trên máy local trước khi chạy

DAO **extends** class này, không tự `DriverManager` ở chỗ khác.

---

## 6. DAO — mẫu bắt buộc (copy khung này)

```java
package apartmentmanagement.dao;

import apartmentmanagement.dal.DBContext;
import apartmentmanagement.model.Apartment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO extends DBContext {

    // 1) LUÔN có hàm map
    public Apartment getFromResultSet(ResultSet rs) throws SQLException {
        return Apartment.builder()
                .apartmentId(rs.getInt("apartment_id"))
                .apartmentCode(rs.getString("apartment_code"))
                // ... map đủ cột
                .build();
    }

    // 2) SELECT 1 bản ghi
    public Apartment findById(int id) {
        String sql = "SELECT * FROM apartments WHERE apartment_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findById error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return null;
    }

    // 3) SELECT list
    public List<Apartment> findAll() {
        List<Apartment> list = new ArrayList<>();
        String sql = "SELECT * FROM apartments ORDER BY apartment_id DESC";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(getFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.findAll error: " + e.getMessage());
        } finally {
            closeResources();
        }
        return list; // lỗi → list rỗng
    }

    // 4) INSERT + generated key
    public int insert(Apartment a) {
        String sql = "INSERT INTO apartments (apartment_code, building, floor_number, area_m2, occupancy_type, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, a.getApartmentCode());
            statement.setString(2, a.getBuilding());
            statement.setInt(3, a.getFloorNumber());
            statement.setBigDecimal(4, a.getAreaM2());
            statement.setString(5, a.getOccupancyType());
            statement.setString(6, a.getStatus());
            statement.setString(7, a.getNotes());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1); // id mới
                }
                return 0;
            }
            return -1;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.insert error: " + e.getMessage());
            return -1;
        } finally {
            closeResources();
        }
    }

    // 5) UPDATE — không đổi apartment_code / building / floor_number
    // Định danh: [tòa] - [tầng] [mã căn]
    public boolean update(Apartment a) {
        String sql = "UPDATE apartments SET area_m2 = ?, occupancy_type = ?, status = ?, notes = ?, "
                + "updated_at = SYSUTCDATETIME() WHERE apartment_id = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setBigDecimal(1, a.getAreaM2());
            statement.setString(2, a.getOccupancyType());
            statement.setString(3, a.getStatus());
            statement.setString(4, a.getNotes());
            statement.setInt(5, a.getApartmentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.update error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    // 6) EXISTS / check unique
    public boolean existsByCode(String code) {
        String sql = "SELECT 1 FROM apartments WHERE apartment_code = ?";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, code);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("ApartmentDAO.existsByCode error: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }
}
```

### Quy ước return an toàn

| Loại method | Thành công | Thất bại |
|-------------|------------|----------|
| `insert` | id `>= 0` | `-1` |
| `update` / `delete` | `true` | `false` |
| `find*` | object / list | `null` / list rỗng |
| `exists*` | `true/false` | `false` (+ log) |

### Cấm trong DAO

- `request` / `response` / `session`
- `FlashUtil`
- check `role`
- `System.out` rồi nuốt lỗi im lặng quá mức — phải log tên method

---

## 7. CONTROLLER — mẫu bắt buộc

### 7.1. Khung class

```java
package apartmentmanagement.controller.apartment;

import apartmentmanagement.dao.ApartmentDAO;
import apartmentmanagement.model.Apartment;
import apartmentmanagement.model.User;
import apartmentmanagement.util.AppConstants;
import apartmentmanagement.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ApartmentController", urlPatterns = {"/apartment"})
public class ApartmentController extends HttpServlet {

    private final ApartmentDAO apartmentDAO = new ApartmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        switch (action) {
            case "list":
                handleList(request, response);
                break;
            case "create":
                handleCreateForm(request, response);
                break;
            case "edit":
                handleEditForm(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        switch (action) {
            case "create":
                handleCreate(request, response);
                break;
            case "update":
                handleUpdate(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
}
```

### 7.2. Phân tách GET / POST

| Việc | Method HTTP | action gợi ý |
|------|-------------|--------------|
| Xem list | GET | `list` |
| Mở form thêm | GET | `create` |
| Mở form sửa | GET | `edit` |
| Submit thêm | POST | `create` |
| Submit sửa | POST | `update` |
| Xóa / đổi status | POST (ưu tiên) hoặc GET có confirm | `delete` / `deactivate` |

### 7.3. Thứ tự code trong handler POST (bắt buộc)

```text
1. requireUser()              // chưa login → redirect login
2. check role                 // không đủ quyền → 403 / flash + redirect
3. bindForm(request)          // getParameter → Model
4. validateXxx(model)         // List<String> errors
5. if errors → forward form   // set errors + form
6. business check (unique...) // DAO exists
7. DAO insert/update
8. OK  → FlashUtil.success + redirect
   Fail→ FlashUtil.error / errors + forward form
```

### 7.4. Forward trang có layout (copy đúng)

```java
FlashUtil.moveToRequest(request); // nếu cần hiện flash
request.setAttribute("pageTitle", "Tiêu đề tab");
request.setAttribute("contentPage", "/WEB-INF/views/apartment/form.jsp");
request.getRequestDispatcher("/WEB-INF/views/common/layout.jsp")
       .forward(request, response);
```

### 7.5. Redirect + flash (sau thao tác thành công)

```java
FlashUtil.success(request, "Thêm căn hộ thành công.");
response.sendRedirect(request.getContextPath() + "/apartment?action=list");
```

```java
FlashUtil.error(request, "Bạn không có quyền thêm căn hộ.");
response.sendRedirect(request.getContextPath() + "/apartment?action=list");
```

### 7.6. Khi nào forward, khi nào redirect?

| Tình huống | Dùng | Vì sao |
|------------|------|--------|
| Hiện form / list | **forward** | Cần attribute request |
| Validate fail | **forward** form | Giữ `form` + `errors` |
| Insert/Update **OK** | **redirect** | F5 không submit 2 lần (PRG) |
| Chưa login / xong logout | **redirect** | Đổi URL rõ ràng |

### 7.7. Helper dùng lại trong controller

```java
private User requireUser(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
    HttpSession session = request.getSession(false);
    User user = session == null ? null
            : (User) session.getAttribute(AppConstants.SESSION_USER);
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    }
    return user;
}

private String trim(String s) {
    return s == null ? null : s.trim();
}

private boolean canManageApartment(String role) {
    return AppConstants.ROLE_ADMIN.equals(role)
            || AppConstants.ROLE_MANAGER.equals(role);
}
```

### 7.8. Bind + Validate (tách hàm, đừng nhét trong case)

```java
private Apartment bindForm(HttpServletRequest request) {
    // Create: KHÔNG lấy apartmentCode từ client — server tự sinh
    String building = trim(request.getParameter("building"));
    // parse số cẩn thận — lỗi parse để null, validate sẽ bắt
    return Apartment.builder()
            .building(building)
            // floor, area, occupancy, status, notes...
            .build();
}

private List<String> validateCreate(Apartment form) {
    List<String> errors = new ArrayList<>();
    // Không validate mã user nhập — mã do generateApartmentCode()
    if (form.getBuilding() == null || form.getBuilding().isEmpty()) {
        errors.add("Vui lòng nhập tòa nhà.");
    }
    // floor, area, occupancy...
    return errors;
}

// Sinh mã: {TOKEN}-{FF}{UU}  (A tầng 4 unit 1 → A-0401)
// Hiển thị: [tòa] - [tầng] [mã]  (A - 4 A-0401)
// Trùng → "Đã tồn tại căn hộ với mã ..."
```

### 7.9. Phân trang + filter (khi list dài)

Controller:

```java
int page = 1;
try {
    page = Integer.parseInt(request.getParameter("page"));
} catch (Exception ignored) {
}
if (page < 1) page = 1;
int pageSize = AppConstants.DEFAULT_PAGE_SIZE; // 10

String keyword = trim(request.getParameter("keyword"));
// DAO: findWithFilters(keyword, page, pageSize)
// DAO: countWithFilters(keyword)
int total = ...;
int totalPages = (int) Math.ceil(total * 1.0 / pageSize);

request.setAttribute("items", items);
request.setAttribute("currentPage", page);
request.setAttribute("totalPages", totalPages);
request.setAttribute("keyword", keyword);
```

JSP pagination (giữ filter bằng `c:url` + `c:param`):

```jsp
<c:url value="/apartment" var="paginationUrl">
    <c:param name="action" value="list" />
    <c:if test="${not empty keyword}">
        <c:param name="keyword" value="${keyword}" />
    </c:if>
</c:url>
<a class="page-link" href="${paginationUrl}&page=${i}">${i}</a>
```

---

## 8. JSP — mẫu bắt buộc

### 8.1. Form (POST về đúng controller)

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty errors}">
    <div class="alert alert-danger">
        <ul class="mb-0">
            <c:forEach var="err" items="${errors}">
                <li>${err}</li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/apartment">
    <input type="hidden" name="action" value="create">

    <label class="form-label">Tòa nhà <span class="text-danger">*</span></label>
    <input class="form-control" type="text" name="building"
           value="${form.building}" required maxlength="50">
    <%-- Mã căn: server tự sinh từ tòa + tầng; không có input apartmentCode khi create --%>

    <button type="submit" class="btn btn-primary">Lưu</button>
    <a class="btn btn-outline-secondary"
       href="${pageContext.request.contextPath}/apartment?action=list">Hủy</a>
</form>
```

### 8.2. Khớp tên field (rất dễ sai)

| JSP `name` | Controller `getParameter` | Model setter/field |
|------------|---------------------------|--------------------|
| `building` | `"building"` | `building` |
| `floorNumber` | `"floorNumber"` | `floorNumber` |
| `areaM2` | `"areaM2"` | `areaM2` |
| *(create: không có `apartmentCode`)* | server `generateApartmentCode` | `apartmentCode` |

Sai 1 ký tự → nhận `null` / rỗng.

### 8.3. List table

```jsp
<table class="table table-hover">
    <thead>...</thead>
    <tbody>
    <c:forEach var="apt" items="${apartments}">
        <tr>
            <td>
                <%-- Định danh: [tòa] - [tầng] [mã] --%>
                ${apt.building} - ${apt.floorNumber} ${apt.apartmentCode}
            </td>
            <td>
                <a href="${pageContext.request.contextPath}/apartment?action=edit&id=${apt.apartmentId}">
                    Sửa
                </a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
```

### 8.4. JavaScript trong JSP (tránh vỡ EL)

```jsp
<script>
    function confirmDeactivate(id) {
        if (confirm('Bạn có chắc muốn khóa căn hộ này?')) {
            window.location.href = '<%= request.getContextPath() %>/apartment?action=deactivate&id=' + id;
        }
    }
</script>
```

**Không** viết kiểu dễ vỡ:

```js
// TRÁNH: EL lẫn JS phức tạp
window.location = `${pageContext...}` // dễ lỗi
```

### 8.5. Flash đã có sẵn

Layout đã include `flash.jsp`.  
Chỉ cần `FlashUtil.success/error` + `moveToRequest` trước forward list.

---

## 9. Auth & Role

### 9.1. Session user

```java
session.getAttribute(AppConstants.SESSION_USER); // key = "currentUser"
```

Roles:

```java
AppConstants.ROLE_ADMIN
AppConstants.ROLE_MANAGER
AppConstants.ROLE_STAFF
AppConstants.ROLE_RESIDENT
```

### 9.2. AuthFilter

- Public: `/auth`, assets tĩnh
- Path `/apartment`, `/fee`, `/request`, `/admin`… map role ở `ROLE_RULES`
- Filter = cửa ngoài (đã login + role path)
- **Quyền chi tiết từng action** (vd. chỉ ADMIN/MANAGER được create) → check **trong controller**

### 9.3. Khi thêm URL module mới

1. Thêm `@WebServlet("/your-path")`
2. Cập nhật `AuthFilter.ROLE_RULES` nếu path mới
3. Check role trong từng handler nhạy cảm

---

## 10. Constants & Flash

### AppConstants

Thêm hằng status/type vào đây — **không** rải magic string khắp code:

```java
public static final String APT_STATUS_ACTIVE = "ACTIVE";
public static final String OCCUPANCY_OWNED = "OWNED";
public static final int DEFAULT_PAGE_SIZE = 10;
```

### FlashUtil

```java
FlashUtil.success(request, "Thành công");
FlashUtil.error(request, "Thất bại");
FlashUtil.moveToRequest(request); // gọi trước forward trang có flash.jsp
```

---

## 11. Hướng dẫn NetBeans — tạo file đúng chỗ

### 11.1. Tạo Model

1. `Source Packages` → `apartmentmanagement.model`
2. New → Java Class → `Apartment`
3. Dán Lombok annotations + fields

### 11.2. Tạo DAO

1. Package `apartmentmanagement.dao`
2. Class `ApartmentDAO`
3. `extends DBContext`
4. Viết `getFromResultSet` trước, method SQL sau

### 11.3. Tạo Controller

1. Package `apartmentmanagement.controller.apartment`
2. New → Java Class (hoặc Servlet)
3. Extends `HttpServlet`
4. Gắn `@WebServlet(urlPatterns = {"/apartment"})`
5. Override `doGet` / `doPost`

### 11.4. Tạo JSP

1. Folder `Web Pages/WEB-INF/views/apartment/`
2. New → JSP → `form.jsp`, `list.jsp`
3. **Không** để JSP ở ngoài `WEB-INF` nếu muốn chặn gọi trực tiếp

### 11.5. Enable Lombok

Project Properties → Build → Compiling:

- Enable Annotation Processing
- Enable Annotation Processing in Editor

Jar `lombok-*.jar` đã có trong Libraries.

---

## 12. Bài thực hành: tự viết UC “Sửa căn hộ”

Làm theo đúng chuẩn — copy khung, đừng invent style mới.

### 12.0. Định danh căn (toàn module)

| Mục | Quy ước |
|-----|---------|
| Hiển thị | **`[tên tòa] - [số tầng] [mã căn]`** · vd. `A - 4 A-0401` |
| Mã lưu DB | `{TOKEN}-{FF}{UU}` · vd. tòa A, tầng 4, unit 01 → **`A-0401`** |
| Create | User **chỉ nhập** tòa + tầng (+ area…); **không** nhập mã |
| Sinh mã | Server: token từ tên tòa + floor 2 số + unit 2 số (tăng nếu trùng) |
| Trùng mã | Chặn insert + message *Đã tồn tại căn hộ với mã …* |
| Update | **Không** đổi code / building / floor |

### 12.1. BA nhanh (Sửa)

- Actor: ADMIN, MANAGER
- GET `/apartment?action=edit&id=1` → form có data cũ
- POST `action=update` → validate → update DB → redirect
- **Không cho đổi định danh căn**: `apartmentCode` + `building` + `floorNumber`
- Field **được sửa**: `areaM2`, `occupancyType`, `status`, `notes`

### 12.2. DAO thêm

```java
public boolean update(Apartment a) { ... }
// optional:
public boolean existsByCodeExceptId(String code, int id) { ... }
```

### 12.3. Controller thêm

```java
// doGet
case "edit": handleEditForm(...); break;

// doPost
case "update": handleUpdate(...); break;
```

`handleEditForm`:

```text
requireUser → canManage → parse id → findById
null? flash error + redirect list
else set form + forward form.jsp (có thể tái sử dụng form create)
```

`handleUpdate`:

```text
requireUser → canManage → bind (có apartmentId)
→ load existing by id
→ GHI ĐÈ identity từ DB: code + building + floor (bỏ qua input client)
→ validate (area/occupancy/status/notes; identity đã lấy từ DB)
→ dao.update (SQL KHÔNG set building/floor/code)
→ success redirect / fail forward form
```

### 12.4. JSP (form edit)

- Form dùng chung: hidden `apartmentId`
- hidden `action` = `update` khi sửa, `create` khi thêm
- **Edit mode**:
  - Dòng định danh readonly: `${form.building} - ${form.floorNumber} ${form.apartmentCode}`
  - `apartmentCode`, `building`, `floorNumber`: `readonly` (không sửa)
  - Chỉ `areaM2` (+ occupancy/status/notes) là input được sửa
- List thêm nút:

```jsp
<a href="${pageContext.request.contextPath}/apartment?action=edit&id=${apt.apartmentId}">Sửa</a>
```

### 12.5. Test

1. Sửa diện tích / notes thành công; mã–tòa–tầng giữ nguyên
2. id không tồn tại
3. STAFF không sửa được
4. Validate area (identity không validate từ input user khi update)
5. F5 sau success không update 2 lần
6. Client cố POST building/floor/code giả → server bỏ qua, DB identity không đổi

---

## 13. Checklist PR / trước khi commit

- [ ] `jakarta.*` only  
- [ ] Entity: Lombok + wrapper + không nested object  
- [ ] DAO: `getFromResultSet` + open/close trong `finally`  
- [ ] Servlet: `@WebServlet` + `action` switch + method tách  
- [ ] POST success dùng **redirect + flash**  
- [ ] Validate fail **forward** + giữ form  
- [ ] JSP: Bootstrap + JSTL + EL; `name` khớp parameter  
- [ ] Check role đúng BA  
- [ ] Không commit password máy cá nhân nếu repo public  
- [ ] Clean and Build OK trên NetBeans  

```bash
git add .
git commit -m "feat(tv2): mô tả ngắn use case"
git push -u origin tv2
```

---

## 14. Lỗi thường gặp & cách xử lý

| Triệu chứng | Nguyên nhân | Cách xử |
|-------------|-------------|---------|
| `package javax.servlet does not exist` / lệch API | Dùng javax hoặc Tomcat 9 | Đổi `jakarta.*`, Tomcat 10 |
| `Cannot find symbol getXxx()` trên model | Lombok chưa process | Bật Annotation Processing, rebuild |
| `NullPointerException` ở DAO | `getConnection()` null | Check SQL Server + DBContext pass |
| `Invalid object name 'apartments'` | Chưa chạy SQL tạo bảng | Chạy `database/*.sql` |
| Form submit không vào case | Sai `name="action"` / method GET-POST | Kiểm tra hidden action + doPost |
| Input luôn rỗng sau lỗi | Không set `form` attribute | `request.setAttribute("form", form)` |
| Thêm 2 lần khi F5 | Dùng forward sau insert | Đổi thành redirect |
| 403 / redirect login | AuthFilter / chưa session | Login lại, check ROLE_RULES |
| JSP 404 contentPage | Sai đường dẫn views | Đúng `/WEB-INF/views/...` |
| Tiếng Việt lỗi font | charset | `pageEncoding=UTF-8` + EncodingFilter |

---

## 15. File mẫu trong project (đọc theo thứ tự)

Khi làm module căn hộ / học pattern:

1. `docs/user-story-them-can-ho.md` — BA  
2. `model/Apartment.java`  
3. `dao/ApartmentDAO.java`  
4. `controller/apartment/ApartmentController.java`  
5. `views/apartment/form.jsp` + `list.jsp`  
6. `dao/UserDAO.java` + `controller/auth/AuthenController.java` — pattern auth  
7. `util/AppConstants.java` + `FlashUtil.java`  
8. `filter/AuthFilter.java`  

---

## 16. Tóm tắt 1 trang (in ra dán bàn)

```text
1. Model (Lombok + wrapper)
2. DAO extends DBContext
   - getFromResultSet
   - getConnection → SQL ? → finally closeResources
3. Controller @WebServlet
   - doGet/doPost + action switch + hàm riêng
   - login → role → bind → validate → DAO
   - lỗi: forward form | OK: redirect + flash
4. JSP trong WEB-INF/views
   - name khớp getParameter
   - layout qua contentPage
5. Jakarta + Tomcat 10 + không db.properties
```

**Công thức use case:**

```text
BA → DB → Model → DAO → Controller → JSP → Test
```

---

## 17. Việc bạn làm tiếp theo (TV2)

| # | Việc | Output |
|---|------|--------|
| 1 | Chạy được list + create (đã có) | Test 5 case |
| 2 | Đọc Controller/DAO create theo guide này | Hiểu pattern |
| 3 | Tự code **Sửa căn hộ** theo mục 12 | `edit`/`update` |
| 4 | (Tuỳ đề) Xóa/khóa, gán cư dân, my apartment | Thêm action |
| 5 | Commit nhánh `tv2` | Push khi có quyền |

Làm **từng UC một**, luôn lặp đúng 7 bước mục 3 — đó là cách code của project này.
