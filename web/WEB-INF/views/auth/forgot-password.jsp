<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <title>Quên mật khẩu | Skyland Apartment</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/public.css" rel="stylesheet">
    <style>
        .forgot-wrap { flex: 1; padding: 2rem 0 3rem; }
        .forgot-shell {
            max-width: 720px;
            margin: 0 auto;
        }
        .forgot-card {
            background: #fff;
            border-radius: 20px;
            box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1);
            padding: 1.75rem;
            border: 1px solid rgba(226, 232, 240, 0.9);
        }
        .forgot-side {
            background: linear-gradient(160deg, #eff6ff 0%, #f8fafc 55%, #ffffff 100%);
            border: 1px solid #dbe7f5;
            border-radius: 16px;
            padding: 1.25rem 1.35rem;
            height: 100%;
        }
        .forgot-side .tip-item {
            display: flex;
            gap: 0.75rem;
            margin-bottom: 1rem;
        }
        .forgot-side .tip-item:last-child { margin-bottom: 0; }
        .tip-icon {
            width: 34px;
            height: 34px;
            border-radius: 10px;
            background: #1a4388;
            color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-size: 0.95rem;
        }
        .stepper {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 0.5rem;
            margin-bottom: 1.5rem;
            padding: 0.85rem 1rem;
            background: #f8fafc;
            border-radius: 14px;
            border: 1px solid #e2e8f0;
        }
        .stepper-item {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            min-width: 0;
            flex: 1;
        }
        .stepper-item .num {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 0.8rem;
            font-weight: 700;
            background: #e2e8f0;
            color: #64748b;
            flex-shrink: 0;
        }
        .stepper-item .label {
            font-size: 0.82rem;
            font-weight: 600;
            color: #94a3b8;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .stepper-item.active .num,
        .stepper-item.done .num {
            background: #1a4388;
            color: #fff;
        }
        .stepper-item.active .label { color: #1a4388; }
        .stepper-item.done .label { color: #334155; }
        .stepper-line {
            height: 2px;
            width: 18px;
            background: #e2e8f0;
            flex-shrink: 0;
        }
        .stepper-line.done { background: #93b4e0; }
        .channel-pick {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 0.75rem;
        }
        .channel-option {
            position: relative;
        }
        .channel-option input {
            position: absolute;
            opacity: 0;
            pointer-events: none;
        }
        .channel-option label {
            display: flex;
            align-items: center;
            gap: 0.65rem;
            border: 2px solid #e2e8f0;
            border-radius: 12px;
            padding: 0.85rem 1rem;
            cursor: pointer;
            transition: border-color .15s, box-shadow .15s, background .15s;
            background: #fff;
            margin: 0;
            height: 100%;
        }
        .channel-option label i {
            font-size: 1.15rem;
            color: #1a4388;
        }
        .channel-option input:checked + label {
            border-color: #1a4388;
            background: #f0f6ff;
            box-shadow: 0 0 0 3px rgba(26, 67, 136, 0.12);
        }
        .otp-demo-box {
            background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
            border: 1px dashed #93b4e0;
            border-radius: 14px;
            padding: 1.1rem 1.25rem;
        }
        .otp-code {
            font-size: 1.85rem;
            font-weight: 700;
            letter-spacing: 0.35em;
            color: #1a4388;
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            padding-left: 0.35em;
        }
        .otp-input {
            letter-spacing: 0.35em;
            font-weight: 700;
            font-size: 1.35rem;
        }
        .success-ring {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            background: linear-gradient(145deg, #1a4388, #2f6fd1);
            color: #fff;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 1.75rem;
            box-shadow: 0 12px 28px rgba(26, 67, 136, 0.28);
        }
        @media (max-width: 575.98px) {
            .channel-pick { grid-template-columns: 1fr; }
            .stepper-item .label { display: none; }
            .stepper { justify-content: center; gap: 0.75rem; }
            .stepper-item { flex: 0 0 auto; }
            .forgot-card { padding: 1.25rem; }
        }
    </style>
</head>
<body class="public-body">

<c:set var="publicActive" value="login" scope="request"/>
<%@ include file="/WEB-INF/views/auth/public-nav.jsp" %>

<section class="public-hero" style="padding: 2.5rem 0 2rem;">
    <div class="container">
        <p class="mb-2 opacity-75 small">
            <i class="bi bi-shield-lock me-1"></i> Khôi phục tài khoản
        </p>
        <h1 style="font-size: 1.85rem;">Quên mật khẩu</h1>
        <p class="mb-0 opacity-90" style="max-width: 36rem;">
            Xác thực bằng email hoặc số điện thoại đã đăng ký, nhập mã OTP rồi đặt mật khẩu mới.
        </p>
    </div>
</section>

<main class="forgot-wrap">
    <div class="container forgot-shell">

        <c:if test="${not empty error}">
            <div class="alert alert-danger border-0 rounded-3 shadow-sm d-flex gap-2" role="alert">
                <i class="bi bi-exclamation-triangle-fill mt-1"></i>
                <div>${error}</div>
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success border-0 rounded-3 shadow-sm d-flex gap-2" role="alert">
                <i class="bi bi-check-circle-fill mt-1"></i>
                <div>${success}</div>
            </div>
        </c:if>
        <c:if test="${not empty info}">
            <div class="alert alert-info border-0 rounded-3 shadow-sm d-flex gap-2" role="alert">
                <i class="bi bi-info-circle-fill mt-1"></i>
                <div>${info}</div>
            </div>
        </c:if>

        <c:set var="stepNow" value="${empty step ? 'identify' : step}"/>
        <c:set var="s1" value="${stepNow == 'identify' ? 'active' : ((stepNow == 'otp' || stepNow == 'reset' || stepNow == 'done') ? 'done' : '')}"/>
        <c:set var="s2" value="${stepNow == 'otp' ? 'active' : ((stepNow == 'reset' || stepNow == 'done') ? 'done' : '')}"/>
        <c:set var="s3" value="${stepNow == 'reset' ? 'active' : (stepNow == 'done' ? 'done' : '')}"/>

        <div class="stepper" aria-label="Tiến trình khôi phục mật khẩu">
            <div class="stepper-item ${s1}">
                <span class="num">1</span>
                <span class="label">Xác minh</span>
            </div>
            <div class="stepper-line ${s1 == 'done' || s2 == 'active' || s2 == 'done' ? 'done' : ''}"></div>
            <div class="stepper-item ${s2}">
                <span class="num">2</span>
                <span class="label">Nhập OTP</span>
            </div>
            <div class="stepper-line ${s2 == 'done' || s3 == 'active' || s3 == 'done' ? 'done' : ''}"></div>
            <div class="stepper-item ${s3}">
                <span class="num">3</span>
                <span class="label">Mật khẩu mới</span>
            </div>
        </div>

        <c:choose>

            <%-- STEP 1: xác minh danh tính --%>
            <c:when test="${empty step || step == 'identify'}">
                <div class="forgot-card">
                    <div class="row g-4 align-items-stretch">
                        <div class="col-lg-7">
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <span class="icon-circle mb-0" style="width:42px;height:42px;">
                                    <i class="bi bi-person-vcard"></i>
                                </span>
                                <div>
                                    <h2 class="mb-0 h5 fw-bold">Xác minh tài khoản</h2>
                                    <small class="text-muted">Khớp username với email / SĐT đã lưu</small>
                                </div>
                            </div>

                            <c:if test="${otpCooldownLocked}">
                                <div class="alert alert-warning border-0 rounded-3 d-flex gap-2 mb-3" role="status">
                                    <i class="bi bi-hourglass-split mt-1"></i>
                                    <div>
                                        Đang chờ gửi lại OTP do nhập sai quá 5 lần.
                                        Còn <strong id="otpCooldownLabel">${otpCooldownSeconds}</strong> giây.
                                    </div>
                                </div>
                            </c:if>

                            <form method="post" action="${pageContext.request.contextPath}/auth" class="row g-3" id="selfOtpForm">
                                <input type="hidden" name="action" value="forgot-send-otp">

                                <div class="col-12">
                                    <label class="form-label fw-medium mb-2">Kênh nhận OTP</label>
                                    <div class="channel-pick">
                                        <div class="channel-option">
                                            <input type="radio" name="channel" id="chPhone"
                                                   value="phone" ${empty channel || channel == 'phone' ? 'checked' : ''}
                                                   ${otpCooldownLocked ? 'disabled' : ''}>
                                            <label for="chPhone">
                                                <i class="bi bi-phone"></i>
                                                <span>
                                                    <strong class="d-block">Số điện thoại</strong>
                                                    <small class="text-muted">SĐT đã đăng ký</small>
                                                </span>
                                            </label>
                                        </div>
                                        <div class="channel-option">
                                            <input type="radio" name="channel" id="chEmail"
                                                   value="email" ${channel == 'email' ? 'checked' : ''}
                                                   ${otpCooldownLocked ? 'disabled' : ''}>
                                            <label for="chEmail">
                                                <i class="bi bi-envelope"></i>
                                                <span>
                                                    <strong class="d-block">Email</strong>
                                                    <small class="text-muted">Email đã đăng ký</small>
                                                </span>
                                            </label>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-12">
                                    <label class="form-label fw-medium" for="selfUsername">
                                        Tên đăng nhập <span class="text-danger">*</span>
                                    </label>
                                    <input type="text" class="form-control form-control-lg" id="selfUsername" name="username"
                                           required value="${username}" autocomplete="username" placeholder="VD: resident1"
                                           ${otpCooldownLocked ? 'disabled' : ''}>
                                </div>

                                <div class="col-12" id="phoneField">
                                    <label class="form-label fw-medium" for="selfPhone">
                                        Số điện thoại đăng ký <span class="text-danger">*</span>
                                    </label>
                                    <input type="text" class="form-control form-control-lg" id="selfPhone" name="phone"
                                           value="${phone}" autocomplete="tel" placeholder="VD: 0902000001"
                                           ${otpCooldownLocked ? 'disabled' : ''}>
                                </div>

                                <div class="col-12 d-none" id="emailField">
                                    <label class="form-label fw-medium" for="selfEmail">
                                        Email đăng ký <span class="text-danger">*</span>
                                    </label>
                                    <input type="email" class="form-control form-control-lg" id="selfEmail" name="email"
                                           value="${email}" autocomplete="email" placeholder="email@example.com"
                                           ${otpCooldownLocked ? 'disabled' : ''}>
                                </div>

                                <div class="col-12 d-flex flex-column flex-sm-row gap-2 pt-1">
                                    <button type="submit" class="btn btn-primary-custom rounded-pill px-4 py-2"
                                            id="sendOtpBtn" ${otpCooldownLocked ? 'disabled' : ''}>
                                        <i class="bi bi-key me-1"></i>
                                        <span id="sendOtpBtnLabel">
                                            <c:choose>
                                                <c:when test="${otpCooldownLocked}">Chờ ${otpCooldownSeconds}s</c:when>
                                                <c:otherwise>Gửi mã OTP</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </button>
                                    <a class="btn btn-outline-secondary rounded-pill px-3 py-2"
                                       href="${pageContext.request.contextPath}/auth?action=login">
                                        <i class="bi bi-arrow-left me-1"></i> Đăng nhập
                                    </a>
                                </div>
                            </form>
                        </div>

                        <div class="col-lg-5">
                            <div class="forgot-side">
                                <h3 class="h6 fw-bold text-primary-custom mb-3">
                                    <i class="bi bi-info-circle me-1"></i> Hướng dẫn nhanh
                                </h3>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-1-circle"></i></span>
                                    <div class="small">
                                        <strong class="d-block text-dark">Chọn kênh</strong>
                                        <span class="text-muted">Email hoặc SĐT trùng với hồ sơ tài khoản.</span>
                                    </div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-2-circle"></i></span>
                                    <div class="small">
                                        <strong class="d-block text-dark">Nhận mã 6 số</strong>
                                        <span class="text-muted">OTP demo hiển thị trên màn hình (không gửi SMS/email thật).</span>
                                    </div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-3-circle"></i></span>
                                    <div class="small">
                                        <strong class="d-block text-dark">Đặt mật khẩu mới</strong>
                                        <span class="text-muted">Tối thiểu 6 ký tự, dùng ngay để đăng nhập.</span>
                                    </div>
                                </div>
                                <div class="note-box mt-3 mb-0 small">
                                    <i class="bi bi-lightbulb me-1 text-warning"></i>
                                    Thử nhanh: <code>resident1</code> · SĐT <code>0902000001</code>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- STEP 2: nhập OTP --%>
            <c:when test="${step == 'otp'}">
                <div class="forgot-card">
                    <div class="row g-4 align-items-stretch">
                        <div class="col-lg-7">
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <span class="icon-circle mb-0" style="width:42px;height:42px;">
                                    <i class="bi bi-123"></i>
                                </span>
                                <div>
                                    <h2 class="mb-0 h5 fw-bold">Nhập mã OTP</h2>
                                    <small class="text-muted">
                                        Tài khoản <strong>${otpUsername}</strong>
                                        ·
                                        <c:choose>
                                            <c:when test="${otpChannel == 'email'}">email</c:when>
                                            <c:otherwise>SĐT</c:otherwise>
                                        </c:choose>
                                        <strong>${otpContactMasked}</strong>
                                    </small>
                                </div>
                            </div>

                            <c:if test="${not empty demoOtp}">
                                <div class="otp-demo-box mb-3 text-center">
                                    <div class="small text-muted mb-1">
                                        <i class="bi bi-display me-1"></i>
                                        Mã OTP demo (thay cho SMS / email thật)
                                    </div>
                                    <div class="otp-code">${demoOtp}</div>
                                    <div class="small text-muted mt-1">Hiệu lực ~5 phút · tối đa 5 lần nhập sai</div>
                                </div>
                            </c:if>

                            <form method="post" action="${pageContext.request.contextPath}/auth" class="row g-3">
                                <input type="hidden" name="action" value="forgot-verify-otp">
                                <div class="col-12 col-sm-8 col-md-6">
                                    <label class="form-label fw-medium" for="otpCode">Mã OTP 6 số</label>
                                    <input type="text" class="form-control form-control-lg text-center otp-input"
                                           id="otpCode" name="otp" required maxlength="6" pattern="[0-9]{6}"
                                           inputmode="numeric" autocomplete="one-time-code" placeholder="000000"
                                           autofocus>
                                </div>
                                <div class="col-12 d-flex flex-column flex-sm-row gap-2">
                                    <button type="submit" class="btn btn-primary-custom rounded-pill px-4 py-2">
                                        <i class="bi bi-shield-check me-1"></i> Xác nhận OTP
                                    </button>
                                    <a class="btn btn-outline-secondary rounded-pill px-3 py-2"
                                       href="${pageContext.request.contextPath}/auth?action=forgot-password">
                                        Gửi lại / đổi kênh
                                    </a>
                                </div>
                            </form>
                        </div>

                        <div class="col-lg-5">
                            <div class="forgot-side">
                                <h3 class="h6 fw-bold text-primary-custom mb-3">
                                    <i class="bi bi-shield-lock me-1"></i> Bảo mật OTP
                                </h3>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-clock"></i></span>
                                    <div class="small text-muted">
                                        Mã hết hạn sau <strong class="text-dark">5 phút</strong>. Hết hạn thì gửi lại từ bước 1.
                                    </div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-exclamation-triangle"></i></span>
                                    <div class="small text-muted">
                                        Sai quá <strong class="text-dark">5 lần</strong> phải chờ
                                        <strong class="text-dark">1 phút</strong> rồi mới gửi OTP mới.
                                    </div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-eye-slash"></i></span>
                                    <div class="small text-muted">
                                        Không chia sẻ mã OTP với người khác, kể cả khi được nhờ hỗ trợ.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- STEP 3: đặt mật khẩu mới --%>
            <c:when test="${step == 'reset'}">
                <div class="forgot-card">
                    <div class="row g-4 align-items-stretch">
                        <div class="col-lg-7">
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <span class="icon-circle mb-0" style="width:42px;height:42px;">
                                    <i class="bi bi-lock"></i>
                                </span>
                                <div>
                                    <h2 class="mb-0 h5 fw-bold">Đặt mật khẩu mới</h2>
                                    <small class="text-muted">Tài khoản <strong>${otpUsername}</strong> đã xác thực OTP</small>
                                </div>
                            </div>

                            <form method="post" action="${pageContext.request.contextPath}/auth" class="row g-3">
                                <input type="hidden" name="action" value="forgot-reset">
                                <div class="col-12">
                                    <label class="form-label fw-medium" for="newPassword">
                                        Mật khẩu mới <span class="text-danger">*</span>
                                    </label>
                                    <input type="password" class="form-control form-control-lg" id="newPassword" name="newPassword"
                                           required minlength="6" autocomplete="new-password" placeholder="Tối thiểu 6 ký tự">
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-medium" for="confirmPassword">
                                        Xác nhận mật khẩu <span class="text-danger">*</span>
                                    </label>
                                    <input type="password" class="form-control form-control-lg" id="confirmPassword" name="confirmPassword"
                                           required minlength="6" autocomplete="new-password" placeholder="Nhập lại mật khẩu">
                                </div>
                                <div class="col-12">
                                    <button type="submit" class="btn btn-primary-custom rounded-pill px-4 py-2">
                                        <i class="bi bi-check2-circle me-1"></i> Lưu mật khẩu mới
                                    </button>
                                </div>
                            </form>
                        </div>

                        <div class="col-lg-5">
                            <div class="forgot-side">
                                <h3 class="h6 fw-bold text-primary-custom mb-3">
                                    <i class="bi bi-check2-square me-1"></i> Gợi ý mật khẩu
                                </h3>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-fonts"></i></span>
                                    <div class="small text-muted">Dài tối thiểu 6 ký tự; nên kết hợp chữ và số.</div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-person-x"></i></span>
                                    <div class="small text-muted">Tránh dùng ngày sinh, SĐT hoặc username làm mật khẩu.</div>
                                </div>
                                <div class="tip-item">
                                    <span class="tip-icon"><i class="bi bi-arrow-repeat"></i></span>
                                    <div class="small text-muted">Sau khi lưu, đăng nhập lại bằng mật khẩu mới ngay.</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>

            <%-- STEP: hoàn tất --%>
            <c:when test="${step == 'done'}">
                <div class="forgot-card text-center py-4 px-3">
                    <div class="success-ring mx-auto mb-3">
                        <i class="bi bi-check-lg"></i>
                    </div>
                    <h2 class="h4 fw-bold mb-2">${doneTitle}</h2>
                    <p class="text-muted mb-4 mx-auto" style="max-width: 28rem;">
                        ${doneMessage}
                    </p>
                    <div class="d-flex flex-column flex-sm-row gap-2 justify-content-center">
                        <a class="btn btn-primary-custom rounded-pill px-4 py-2"
                           href="${pageContext.request.contextPath}/auth?action=login">
                            <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
                        </a>
                        <a class="btn btn-outline-secondary rounded-pill px-3 py-2"
                           href="${pageContext.request.contextPath}/auth?action=home">
                            Trang chủ
                        </a>
                    </div>
                </div>
            </c:when>

        </c:choose>
    </div>
</main>

<%@ include file="/WEB-INF/views/auth/public-footer.jsp" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        var chPhone = document.getElementById('chPhone');
        var chEmail = document.getElementById('chEmail');
        var phoneField = document.getElementById('phoneField');
        var emailField = document.getElementById('emailField');
        var selfPhone = document.getElementById('selfPhone');
        var selfEmail = document.getElementById('selfEmail');
        if (chPhone && chEmail && phoneField && emailField) {
            function syncChannel() {
                var useEmail = chEmail.checked;
                phoneField.classList.toggle('d-none', useEmail);
                emailField.classList.toggle('d-none', !useEmail);
                if (selfPhone) {
                    selfPhone.required = !useEmail;
                }
                if (selfEmail) {
                    selfEmail.required = useEmail;
                }
            }
            chPhone.addEventListener('change', syncChannel);
            chEmail.addEventListener('change', syncChannel);
            syncChannel();
        }

        // Countdown khóa gửi OTP sau khi sai quá 5 lần
        var remaining = parseInt('<%= request.getAttribute("otpCooldownSeconds") != null
                ? request.getAttribute("otpCooldownSeconds") : "0" %>', 10);
        if (!remaining || remaining <= 0) {
            return;
        }
        var btn = document.getElementById('sendOtpBtn');
        var btnLabel = document.getElementById('sendOtpBtnLabel');
        var label = document.getElementById('otpCooldownLabel');
        var form = document.getElementById('selfOtpForm');
        var timer = setInterval(function () {
            remaining -= 1;
            if (remaining <= 0) {
                clearInterval(timer);
                if (label) {
                    label.textContent = '0';
                }
                if (btn) {
                    btn.disabled = false;
                }
                if (btnLabel) {
                    btnLabel.textContent = 'Gửi mã OTP';
                }
                if (form) {
                    var fields = form.querySelectorAll('input, button');
                    for (var i = 0; i < fields.length; i++) {
                        fields[i].disabled = false;
                    }
                }
                // sync required/channel sau khi unlock
                if (chPhone && chEmail) {
                    chPhone.dispatchEvent(new Event('change'));
                }
                return;
            }
            if (label) {
                label.textContent = String(remaining);
            }
            if (btnLabel) {
                btnLabel.textContent = 'Chờ ' + remaining + 's';
            }
        }, 1000);
    })();
</script>
</body>
</html>
