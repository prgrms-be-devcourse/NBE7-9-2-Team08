import { User } from "@/types/user"

export async function fetchUserById(id: number): Promise<User | null> {
  try {
    const res = await fetch(`http://localhost:8080/api/users`)
    if (!res.ok) return null
    return res.json()
  } catch (err) {
    console.error("유저 정보 불러오기 실패:", err)
    return null
  }
}