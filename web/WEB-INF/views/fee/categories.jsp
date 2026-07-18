<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-start gap-2 mb-3">
    <div>
        <h2 class="h4 mb-1">Danh mục phí</h2>
        <p class="text-muted small mb-0">
            Phân loại khoản phí (quản lý, gửi xe, bảo trì…). Tên danh mục phải <strong>duy nhất</strong>.
        </p>
    </div>
    <a class="btn btn-outline-secondary btn-sm"
       href="${pageContext.request.contextPath}/fee?action=list">
        <i class="bi bi-arrow-left me-1"></i> Quay lại phí
    </a>
</div>

<div class="row g-3">
    <div class="col-lg-7">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span>Danh sách danh mục</span>
                <span class="badge text-bg-light text-secondary border">
                    ${empty categories ? 0 : categories.size()} mục
                </span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                    <tr>
                        <th style="width:28%">Tên</th>
                        <th>Mô tả</th>
                        <th style="width:18%">Trạng thái</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty categories}">
                            <tr>
                                <td colspan="3" class="text-center text-muted py-4">
                                    Chưa có danh mục nào. Hãy thêm ở form bên cạnh
                                    hoặc chạy <code>sql/fee-module.sql</code>.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="cat" items="${categories}">
                                <tr>
                                    <td><strong>${cat.name}</strong></td>
                                    <td class="small text-muted">${empty cat.description ? '—' : cat.description}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${cat.isActive}">
                                                <span class="badge text-bg-success">ACTIVE</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge text-bg-secondary">INACTIVE</span>
                                            </c:otherwise>
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
    </div>

    <div class="col-lg-5">
        <div class="card shadow-sm h-100">
            <div class="card-header bg-white fw-semibold">
                <i class="bi bi-plus-circle me-1 text-primary"></i> Thêm danh mục
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/fee" class="row g-3" novalidate>
                    <input type="hidden" name="action" value="category-create"/>
                    <div class="col-12">
                        <label class="form-label" for="catName">Tên <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="catName" name="name"
                               required maxlength="100"
                               placeholder="VD: Phí vệ sinh"
                               autocomplete="off"/>
                        <div class="form-text">Không trùng tên đã có trong danh sách.</div>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="catDesc">Mô tả</label>
                        <textarea class="form-control" id="catDesc" name="description" rows="3"
                                  maxlength="500"
                                  placeholder="Mô tả ngắn (không bắt buộc)"></textarea>
                    </div>
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-check2 me-1"></i> Thêm danh mục
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
