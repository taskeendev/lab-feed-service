# lab-feed-service — Progress

ส่วนหนึ่งของ **Feature Lab** — community feed: โพสต์/comment/like โดย user ทุกคนที่ login
หลักออกแบบ: DB ของตัวเอง (feeddb) · ไม่มี FK ข้ามไป auth (เก็บ author_username จาก token —
coupling ผ่านสัญญา JWT ไม่ใช่ database) · อ่านสาธารณะ เขียนต้อง login

สถานะ: ⬜ ยังไม่เริ่ม · 🔨 กำลังทำ · ✅ เสร็จ

## บันได 5 ขั้น

- [x] 1. โครง service + schema (posts/comments/likes + CASCADE) + security/JWT/error pattern — 2026-06-12
- [ ] 2. Posts API: สร้าง/อ่าน (แบ่งหน้า + นับ like/comment + likedByMe)/ลบ (เจ้าของหรือ ADMIN)
- [ ] 3. Comments + Likes API
- [ ] 4. Integration tests (Testcontainers) + CI เขียว
- [ ] 5. lab-web: หน้า Feed + ติดป้าย live + redeploy (เกณฑ์เฟส)

## Log การทำงาน

- 2026-06-12 — ขั้น 1 เสร็จ: โครงสูตรมาตรฐานของระบบ (env/health/request-id/graceful/CI) + Postgres ของตัวเองที่ :5434; V1: posts (index created_at DESC สำหรับ feed), comments (CASCADE), likes (PK คู่ post_id+username = กัน like ซ้ำที่ระดับ DB); security: GET /api/posts/** เปิดสาธารณะ ที่เหลือต้อง token; GlobalExceptionHandler รวม AccessDenied fix ตั้งแต่วันแรก
