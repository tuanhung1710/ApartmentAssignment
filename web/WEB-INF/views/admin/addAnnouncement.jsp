<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
  UC-ADM-06: Add Announcement (Admin)
  Fields: Title, Content, Status (Published / Draft)
--%>

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h2 class="h4 mb-1">Thêm thông báo</h2>
        <p class="text-muted small mb-0">UC-ADM-06 · Add Announcement</p>
    </div>
    <a class="btn btn-sm btn-outline-secondary"
       href="${pageContext.request.contextPath}/admin?action=announcements">
        <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
    </a>
</div>

<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-1"></i> <c:out value="${error}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>

<div class="card border-0 shadow-sm">
    <div class="card-body">
        <form method="post" action="${pageContext.request.contextPath}/admin" class="row g-3">
            <input type="hidden" name="action" value="add-announcement"/>

            <div class="col-12">
                <label for="title" class="form-label">
                    Title <span class="text-danger">*</span>
                </label>
                <input type="text"
                       class="form-control"
                       id="title"
                       name="title"
                       required
                       maxlength="200"
                       value="<c:out value='${formTitle}'/>"
                       placeholder="Tiêu đề thông báo"/>
            </div>

            <div class="col-12">
                <label for="content" class="form-label">
                    Content <span class="text-danger">*</span>
                </label>
                <textarea class="form-control"
                          id="content"
                          name="content"
                          required
                          rows="8"
                          placeholder="Nội dung thông báo"><c:out value="${formContent}"/></textarea>
            </div>

            <div class="col-md-6">
                <label for="status" class="form-label">
                    Status <span class="text-danger">*</span>
                </label>
                <select class="form-select" id="status" name="status" required>
                    <option value="draft" ${empty formStatus || formStatus == 'draft' ? 'selected' : ''}>
                        Draft
                    </option>
                    <option value="published" ${formStatus == 'published' ? 'selected' : ''}>
                        Published
                    </option>
                </select>
                <div class="form-text">Map cột is_published (0 = Draft, 1 = Published)</div>
            </div>

            <div class="col-md-6">
                <label for="category" class="form-label">Category (tuỳ chọn)</label>
                <input type="text"
                       class="form-control"
                       id="category"
                       name="category"
                       maxlength="50"
                       value="<c:out value='${formCategory}'/>"
                       placeholder="vd: GENERAL, FEE, MAINTENANCE"/>
            </div>

            <div class="col-12 d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-1"></i> Thêm thông báo
                </button>
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/admin?action=announcements">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>
