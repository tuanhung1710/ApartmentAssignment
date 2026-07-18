<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<link href="https://cdn.jsdelivr.net/npm/quill@1.3.7/dist/quill.snow.css" rel="stylesheet">

<div class="card shadow-sm mt-3" id="ticketCommentsCard"
     data-api="<%= request.getContextPath() %>/request-comment"
     data-ws-path="<%= request.getContextPath() %>/ws/request-chat"
     data-request-id="${reqItem.requestId}"
     data-last-id="${empty lastCommentId ? 0 : lastCommentId}"
     data-user-id="${sessionScope.currentUser.userId}"
     data-user-name="<c:out value='${sessionScope.currentUser.fullName}'/>"
     data-user-role="<c:out value='${sessionScope.currentUser.role}'/>">
    <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <span>
            <i class="bi bi-chat-dots me-1"></i> Trao đổi realtime
            <span class="badge text-bg-light text-dark ms-1">Resident ↔ Quản lý</span>
        </span>
        <div class="d-flex align-items-center gap-3">
            <span class="small text-muted" title="Giờ Việt Nam (UTC+7)">
                <i class="bi bi-clock me-1"></i>
                <span id="commentLiveClock">--:--</span>
            </span>
            <span class="small" id="commentConnStatus">
                <span class="text-muted">Đang kết nối…</span>
            </span>
        </div>
    </div>
    <div class="card-body p-0">
        <div id="commentTimeline" class="comment-timeline px-3 py-3">
            <c:choose>
                <c:when test="${empty comments}">
                    <p class="text-muted small mb-0" id="commentEmptyHint">Chưa có tin nhắn. Hãy gửi tin đầu tiên.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="cmt" items="${comments}">
                        <div class="comment-row ${cmt.changedBy == sessionScope.currentUser.userId ? 'mine' : 'theirs'}"
                             data-comment-id="${cmt.historyId}">
                            <div class="comment-meta">
                                <strong><c:out value="${cmt.changedByName}"/></strong>
                                <c:if test="${not empty cmt.changedByRole}">
                                    <span class="badge text-bg-light text-dark ms-1"><c:out value="${cmt.changedByRole}"/></span>
                                </c:if>
                                <span class="small ms-1 comment-time">
                                    <t:rt value="${cmt.createdAt}"/>
                                </span>
                            </div>
                            <div class="comment-bubble comment-html">${cmt.note}</div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <c:if test="${canComment}">
        <div class="card-footer">
            <form id="commentForm" method="post"
                  action="${pageContext.request.contextPath}/request-comment"
                  class="d-flex flex-column gap-2">
                <input type="hidden" name="action" value="add"/>
                <input type="hidden" name="requestId" value="${reqItem.requestId}"/>
                <input type="hidden" id="commentContent" name="content" value=""/>
                <label class="form-label small mb-0" for="commentEditor">Tin nhắn / phản hồi trực tiếp</label>
                <div id="commentEditor" class="comment-quill-editor"></div>
                <div class="d-flex justify-content-between align-items-center">
                    <span class="small text-muted">
                        WebSocket realtime · Rich text · Tối đa 4000 ký tự
                    </span>
                    <button type="submit" class="btn btn-primary btn-sm" id="commentSendBtn">
                        <i class="bi bi-send me-1"></i> Gửi
                    </button>
                </div>
                <div class="text-danger small d-none" id="commentError"></div>
            </form>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/quill@1.3.7/dist/quill.min.js"></script>
<script>
(function () {
    var panel = document.getElementById('ticketCommentsCard');
    if (!panel) {
        return;
    }

    var COMMENT_API = panel.getAttribute('data-api') || '';
    var WS_PATH = panel.getAttribute('data-ws-path') || '';
    var REQUEST_ID = parseInt(panel.getAttribute('data-request-id') || '0', 10);
    var LAST_ID = parseInt(panel.getAttribute('data-last-id') || '0', 10);
    var CURRENT_USER_ID = parseInt(panel.getAttribute('data-user-id') || '0', 10);
    var CURRENT_USER_NAME = panel.getAttribute('data-user-name') || 'Bạn';
    var CURRENT_USER_ROLE = panel.getAttribute('data-user-role') || '';
    var MAX_LEN = 4000;
    var POLL_MS = 5000;
    var pendingLocalId = 0;

    var timeline = document.getElementById('commentTimeline');
    var form = document.getElementById('commentForm');
    var contentEl = document.getElementById('commentContent');
    var errEl = document.getElementById('commentError');
    var statusEl = document.getElementById('commentConnStatus');
    var emptyHint = document.getElementById('commentEmptyHint');
    var editorHost = document.getElementById('commentEditor');
    var liveClockEl = document.getElementById('commentLiveClock');
    var quill = null;

    function pad2(n) {
        return n < 10 ? '0' + n : String(n);
    }
    function formatHm(date) {
        return pad2(date.getHours()) + ':' + pad2(date.getMinutes());
    }
    function formatChatTime(date) {
        // HH:mm realtime; tooltip full do title
        return formatHm(date || new Date());
    }
    function tickLiveClock() {
        if (!liveClockEl) {
            return;
        }
        var now = new Date();
        liveClockEl.textContent = formatHm(now);
        liveClockEl.setAttribute('title',
            pad2(now.getDate()) + '/' + pad2(now.getMonth() + 1) + '/' + now.getFullYear()
            + ' ' + formatHm(now) + ' (giờ máy)');
    }
    tickLiveClock();
    setInterval(tickLiveClock, 1000);

    var ws = null;
    var wsReady = false;
    var usePollFallback = false;
    var pollTimer = null;
    var reconnectTimer = null;
    var reconnectAttempts = 0;

    if (editorHost && typeof Quill !== 'undefined') {
        quill = new Quill('#commentEditor', {
            theme: 'snow',
            placeholder: 'Nhập tin nhắn trao đổi trực tiếp với cư dân / ban quản lý...',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline'],
                    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                    ['link'],
                    ['clean']
                ]
            }
        });
    }

    function escapeHtml(str) {
        if (str == null) {
            return '';
        }
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function looksLikeHtml(str) {
        return str != null && /<\/?[a-z][\s\S]*>/i.test(str);
    }

    function setBubbleHtml(el, content) {
        if (!el) {
            return;
        }
        var raw = content == null ? '' : String(content);
        if (looksLikeHtml(raw)) {
            el.innerHTML = raw;
        } else {
            el.textContent = raw;
        }
    }

    function getEditorHtml() {
        if (!quill) {
            return contentEl ? (contentEl.value || '') : '';
        }
        var text = (quill.getText() || '').replace(/ /g, ' ').trim();
        if (!text) {
            return '';
        }
        return quill.root.innerHTML || '';
    }

    function clearEditor() {
        if (quill) {
            quill.setContents([]);
            quill.setText('');
        }
        if (contentEl) {
            contentEl.value = '';
        }
    }

    function setStatus(html, cls) {
        if (!statusEl) {
            return;
        }
        statusEl.innerHTML = html;
        statusEl.className = 'small' + (cls ? ' ' + cls : '');
    }

    function normalizeHm(timeLabel) {
        if (!timeLabel) {
            return formatHm(new Date());
        }
        var s = String(timeLabel).trim();
        var hmMatch = s.match(/(\d{1,2}:\d{2})(?::\d{2})?\s*$/);
        return hmMatch ? hmMatch[1] : s;
    }

    function removeOnePendingMine() {
        if (!timeline) {
            return;
        }
        var pendings = timeline.querySelectorAll('.comment-row.mine[data-pending="1"]');
        if (pendings.length > 0) {
            pendings[0].remove();
        }
    }

    function appendComment(c) {
        if (!c || !timeline) {
            return;
        }
        var cid = c.commentId;
        if (cid == null || cid === '' || cid === 0) {
            return;
        }
        if (timeline.querySelector('[data-comment-id="' + cid + '"]')) {
            return;
        }

        var mine = !!c.mine || (c.userId === CURRENT_USER_ID);
        if (mine && !c.pending) {
            removeOnePendingMine();
        }

        if (emptyHint) {
            emptyHint.remove();
            emptyHint = null;
        }
        var row = document.createElement('div');
        row.className = 'comment-row ' + (mine ? 'mine' : 'theirs');
        row.setAttribute('data-comment-id', cid);
        if (c.pending) {
            row.setAttribute('data-pending', '1');
        }

        var timeLabel = (c.createdAt && String(c.createdAt).trim())
            ? String(c.createdAt).trim()
            : formatHm(new Date());
        var hmDisplay = normalizeHm(timeLabel);

        var meta = document.createElement('div');
        meta.className = 'comment-meta';
        meta.innerHTML = '<strong>' + escapeHtml(c.userFullName || (mine ? CURRENT_USER_NAME : '')) + '</strong>'
            + ((c.userRole || (mine ? CURRENT_USER_ROLE : ''))
                ? '<span class="badge text-bg-light text-dark ms-1">'
                    + escapeHtml(c.userRole || CURRENT_USER_ROLE) + '</span>'
                : '')
            + '<span class="text-muted small ms-1 comment-time" title="' + escapeHtml(timeLabel) + '">'
            + '<i class="bi bi-clock me-1"></i>' + escapeHtml(hmDisplay) + '</span>';

        var bubble = document.createElement('div');
        bubble.className = 'comment-bubble comment-html';
        setBubbleHtml(bubble, c.content);

        row.appendChild(meta);
        row.appendChild(bubble);
        timeline.appendChild(row);

        var numId = parseInt(cid, 10);
        if (!isNaN(numId) && numId > LAST_ID) {
            LAST_ID = numId;
        }
        timeline.scrollTop = timeline.scrollHeight;
    }

    function appendMyMessageNow(html) {
        pendingLocalId += 1;
        appendComment({
            commentId: 'local-' + pendingLocalId + '-' + Date.now(),
            requestId: REQUEST_ID,
            userId: CURRENT_USER_ID,
            userFullName: CURRENT_USER_NAME,
            userRole: CURRENT_USER_ROLE,
            content: html,
            createdAt: formatHm(new Date()),
            mine: true,
            pending: true
        });
    }

    function showError(msg) {
        if (!errEl) {
            return;
        }
        if (!msg) {
            errEl.classList.add('d-none');
            errEl.textContent = '';
            return;
        }
        errEl.textContent = msg;
        errEl.classList.remove('d-none');
    }

    function buildWsUrl() {
        if (!WS_PATH || !REQUEST_ID) {
            return '';
        }
        var proto = (window.location.protocol === 'https:') ? 'wss://' : 'ws://';
        return proto + window.location.host + WS_PATH + '/' + REQUEST_ID;
    }

    function stopPoll() {
        if (pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    function startPollFallback() {
        usePollFallback = true;
        stopPoll();
        setStatus('<i class="bi bi-arrow-repeat me-1"></i>Fallback poll ~5s', 'text-warning');
        pollTimer = setInterval(pollHttp, POLL_MS);
        pollHttp();
    }

    function pollHttp() {
        if (document.hidden || !REQUEST_ID) {
            return;
        }
        var url = COMMENT_API + '?action=list&requestId=' + REQUEST_ID + '&afterId=' + LAST_ID;
        fetch(url, {
            method: 'GET',
            headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
            credentials: 'same-origin'
        }).then(function (res) {
            return res.json();
        }).then(function (data) {
            if (!data || !data.ok) {
                return;
            }
            var list = data.comments || [];
            for (var i = 0; i < list.length; i++) {
                appendComment(list[i]);
            }
        }).catch(function () {
            /* ignore */
        });
    }

    function connectWs() {
        if (!window.WebSocket || !REQUEST_ID) {
            startPollFallback();
            return;
        }
        var url = buildWsUrl();
        if (!url) {
            startPollFallback();
            return;
        }

        try {
            ws = new WebSocket(url);
        } catch (e) {
            startPollFallback();
            return;
        }

        setStatus('<i class="bi bi-hourglass-split me-1"></i>Đang kết nối WS…', 'text-muted');

        ws.onopen = function () {
            wsReady = true;
            usePollFallback = false;
            reconnectAttempts = 0;
            stopPoll();
            setStatus('<span class="text-success"><i class="bi bi-broadcast me-1"></i>Realtime (WebSocket)</span>');
        };

        ws.onmessage = function (ev) {
            var data;
            try {
                data = JSON.parse(ev.data);
            } catch (e) {
                return;
            }
            if (!data || !data.type) {
                return;
            }
            if (data.type === 'NEW_COMMENT' && data.comment) {
                appendComment(data.comment);
            } else if (data.type === 'ERROR') {
                showError(data.message || 'Lỗi chat.');
            } else if (data.type === 'JOINED') {
                setStatus('<span class="text-success"><i class="bi bi-broadcast me-1"></i>Realtime · ticket #' + data.requestId + '</span>');
            }
        };

        ws.onclose = function () {
            wsReady = false;
            ws = null;
            scheduleReconnect();
        };

        ws.onerror = function () {
            wsReady = false;
            try {
                if (ws) {
                    ws.close();
                }
            } catch (e) { /* ignore */ }
        };
    }

    function scheduleReconnect() {
        if (reconnectTimer) {
            return;
        }
        reconnectAttempts += 1;
        if (reconnectAttempts >= 3 && !usePollFallback) {
            startPollFallback();
        }
        var delay = Math.min(10000, 1000 * reconnectAttempts);
        setStatus('<span class="text-warning"><i class="bi bi-wifi-off me-1"></i>Mất WS · thử lại ' + (delay / 1000) + 's</span>');
        reconnectTimer = setTimeout(function () {
            reconnectTimer = null;
            if (!document.hidden) {
                connectWs();
            }
        }, delay);
    }

    function sendViaWs(html) {
        if (!ws || !wsReady || ws.readyState !== WebSocket.OPEN) {
            return false;
        }
        var payload = JSON.stringify({ type: 'SEND', content: html });
        try {
            ws.send(payload);
            return true;
        } catch (e) {
            return false;
        }
    }

    function sendViaHttp(html, btn) {
        var body = 'action=add&requestId=' + encodeURIComponent(REQUEST_ID)
            + '&content=' + encodeURIComponent(html);

        return fetch(COMMENT_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin',
            body: body
        }).then(function (res) {
            return res.json();
        }).then(function (data) {
            if (btn) {
                btn.disabled = false;
            }
            if (!data || !data.ok) {
                showError((data && data.message) ? data.message : 'Gửi tin thất bại.');
                return;
            }
            if (data.comment) {
                if (!data.comment.createdAt) {
                    data.comment.createdAt = formatHm(new Date());
                }
                appendComment(data.comment);
            }
        }).catch(function () {
            if (btn) {
                btn.disabled = false;
            }
            showError('Không gửi được. Thử lại.');
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            showError('');
            var html = getEditorHtml();
            if (!html) {
                showError('Nội dung không được để trống.');
                return;
            }
            if (html.length > MAX_LEN) {
                showError('Nội dung tối đa ' + MAX_LEN + ' ký tự (kể cả định dạng).');
                return;
            }
            if (contentEl) {
                contentEl.value = html;
            }
            var btn = document.getElementById('commentSendBtn');
            if (btn) {
                btn.disabled = true;
            }

            appendMyMessageNow(html);
            clearEditor();

            if (sendViaWs(html)) {
                if (btn) {
                    btn.disabled = false;
                }
                return;
            }

            sendViaHttp(html, btn);
        });
    }

    if (timeline) {
        timeline.scrollTop = timeline.scrollHeight;
    }

    document.addEventListener('visibilitychange', function () {
        if (!document.hidden && !wsReady && !usePollFallback) {
            connectWs();
        }
    });

    connectWs();
})();
</script>
