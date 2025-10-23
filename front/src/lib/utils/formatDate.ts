/** ✅ 백엔드 날짜 문자열 ("2025-10-09 09:16:10.5274") → JS Date (KST 그대로 해석) */
export function parseBackendDate(dateString?: string): Date | null {
  if (!dateString) return null;

  let s = dateString.trim();

  // ✅ 공백 → 'T'
  if (s.includes(" ")) {
    s = s.replace(" ", "T");
  }

  // ✅ 마이크로초 절삭 (3자리까지만)
  s = s.replace(/(\.\d{1,3})\d*/g, "$1");

  const d = new Date(s);
  if (isNaN(d.getTime())) {
    console.error("❌ Invalid Date — JS cannot parse:", s);
    return null;
  }

  return d;
}

/** ✅ yyyy-MM-dd HH:mm (KST 변환 생략 버전) */
export function formatDateTimeKST(dateString?: string): string {
  const parsed = parseBackendDate(dateString);
  if (!parsed) return "날짜 정보 없음";

  const y = parsed.getFullYear();
  const m = String(parsed.getMonth() + 1).padStart(2, "0");
  const d = String(parsed.getDate()).padStart(2, "0");
  const hh = String(parsed.getHours()).padStart(2, "0");
  const mm = String(parsed.getMinutes()).padStart(2, "0");

  return `${y}-${m}-${d} ${hh}:${mm}`;
}

/** ✅ 상대 시간 포맷 ("몇 분 전", "어제", "3일 전" 등) */
export function formatRelativeTimeKST(dateString?: string): string {
  const parsed = parseBackendDate(dateString);
  if (!parsed) return "날짜 정보 없음";

  const now = new Date();
  const diffMs = now.getTime() - parsed.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHr = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHr / 24);

  if (diffSec < 60) return "방금 전";
  if (diffMin < 60) return `${diffMin}분 전`;
  if (diffHr < 24) return `${diffHr}시간 전`;
  if (diffDay === 1) return "어제";
  if (diffDay < 7) return `${diffDay}일 전`;
  if (diffDay < 30) return `${Math.floor(diffDay / 7)}주 전`;
  if (diffDay < 365) return `${Math.floor(diffDay / 30)}개월 전`;

  // 1년 이상 → 일반 날짜로 표시
  return formatDateTimeKST(dateString).split(" ")[0]; // yyyy-MM-dd
}
