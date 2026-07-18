# Tạo GitHub Issues từ feedback Vibe (detail id=4)

Repo: `tuanhung1710/ApartmentAssignment`  
Nhánh code: `tv2`

## Phân tích 6 issues

| # | Feedback Vibe | Kết luận |
|---|---------------|----------|
| 1 | Không thể gán chủ sở hữu | **Bug runtime** assign-owner (DB/UNIQUE/form hidden) |
| 2 | Đổi owner, không tenant, TV vẫn còn | **Bug nghiệp vụ** — chưa clear household khi đổi owner |
| 3 | Gán thuê mà TV không đổi | **Bug** — sync `ensureActiveMember` không hiệu lực / im lặng |
| 4 | Thiếu lịch sử gán owner/thuê… | **Bug/ops** — `apartment_history` / `writeHistory` fail |
| 5 | Gỡ phải xóa khỏi thành viên hộ | **Bug vs code** — remove-owner/tenant cố ý không đụng TV |
| 6 | Comment thừa (vd. US-APT-01 list tối thiểu) | **Chore cleanup** |

## Cách tạo issues

### 1) Đăng nhập GitHub CLI (bắt buộc 1 lần)

```bash
gh auth login
```

### 2) Chạy script

```bash
bash docs/github-issues/create-issues.sh
```

### 3) Tạo tay

Repo → **Issues** → New issue  
Copy title + body từ `01-...md` … `06-...md`.

## Files

```text
docs/github-issues/01-bug-assign-owner-fail.md
docs/github-issues/02-owner-change-should-not-keep-stale-household-expectation.md
docs/github-issues/03-assign-tenant-does-not-update-household-members.md
docs/github-issues/04-missing-history-on-assign-owner-tenant.md
docs/github-issues/05-remove-should-clear-household-member.md
docs/github-issues/06-cleanup-redundant-comments.md
docs/github-issues/create-issues.sh
```
