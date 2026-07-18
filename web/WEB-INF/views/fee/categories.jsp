<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h2 class="h4 mb-1">Danh mục phí</h2>
        <p class="text-muted small mb-0">FeeCategory — Phí quản lý, gửi xe, bảo trì…</p>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fee?action=list">Quay lại phí</a>
</div>

<div class="card shadow-sm mb-3">
    <div class="card-header bg-white fw-semibold">Danh sách danh mục</div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
            <tr>
                <th>Tên</th>
                <th>Mô tả</th>
                <th>Trạng thái</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty categories}">
                    <tr>
                        <td colspan="3" class="text-center text-muted py-4">Chưa có danh mục nào.</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="cat" items="${categories}">
                        <tr>
                            <td><strong>${cat.name}</strong></td>
                            <td class="small text-muted">${empty cat.description ? '—' : cat.description}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${cat.isActive}"><span class="badge text-bg-success">ACTIVE</span></c:when>
                                    <c:otherwise><span class="badge text-bg-secondary">INACTIVE</span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>

<div class="card shadow-sm">
    <div class="card-header bg-white fw-semibold">Thêm danh mục</div>
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/fee" class="row g-3">
            <input type="hidden" name="action" value="category-create"/>
            <div class="col-md-4">
                <label class="form-label">Tên <span class="text-danger">*</span></label>
                <input type="text" class="form-control" name="name" required maxlength="100"/>
            </div>
            <div class="col-md-5">
                <label class="form-label">Mô tả</label>
                <input type="text" class="form-control" name="description" maxlength="500"
                       placeholder="Mô tả ngắn (không bắt buộc)"/>
            </div>
            <div class="col-md-3 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">Thêm danh mục</button>
            </div>
        </form>
    </div>
</div>
