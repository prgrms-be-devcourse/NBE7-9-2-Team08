// community 도메인 API
export async function fetchRepositories() {
  const res = await fetch("http://localhost:8080/api/community/repositories", {
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}