# Tạo GitHub Issues từ feedback Vibe

Repo: `tuanhung1710/ApartmentAssignment`  
Nhánh code: `tv2`

## Phân tích nhanh 5 annotations + 1 chore

| # | Feedback | Kết luận kỹ thuật |
|---|----------|-------------------|
| 1 | Không thể gán chủ sở hữu | **Bug runtime** — insert/end owner fail (thường thiếu bảng `apartment_residents` hoặc SQL error bị nuốt) |
| 2 | Đổi owner, không tenant, TV vẫn còn | **Gap nghiệp vụ** — `household_members` độc lập owner; cần BR sync/clear |
| 3 | Gán thuê không đổi TV | **Gap nghiệp vụ** — tenant ≠ household member; cần sync hoặc UI giải thích |
| 4 | Thiếu lịch sử gán owner/thuê | **Bug/ops** — `writeHistory` fail im lặng / chưa có bảng `apartment_history` |
| 5 | Gỡ TV phải biến mất | **Bug vs spec cũ** — soft delete + vẫn list inactive; cần hard delete hoặc ẩn |
| 6 | Comment thừa | **Chore cleanup** |

## Cách 1 — Script (cần token)

1. Tạo Personal Access Token (repo scope) hoặc:
   ```bash
   gh auth login
   ```
2. Chạy (Git Bash, tại root project):

```bash
export GH_TOKEN=ghp_xxx   # nếu không dùng gh auth
bash docs/github-issues/create-issues.sh
```

## Cách 2 — Tạo tay trên GitHub

Repo → **Issues** → New issue  
Copy title + body từ từng file `01-...md` … `06-...md`.

## Files

```text
docs/github-issues/01-bug-assign-owner-fail.md
docs/github-issues/02-owner-change-should-not-keep-stale-household-expectation.md
docs/github-issues/03-assign-tenant-does-not-update-household-members.md
docs/github-issues/04-missing-history-on-assign-owner-tenant.md
docs/github-issues/05-remove-member-should-hard-delete-from-household.md
docs/github-issues/06-cleanup-redundant-comments.md
docs/github-issues/create-issues.sh
```
