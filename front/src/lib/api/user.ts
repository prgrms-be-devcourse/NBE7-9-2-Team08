import { http } from './client'
import { User } from "@/types/user"

export interface ModifyNameRequest {
  name: string
}

export interface ModifyNameResponse {
  userDto: User
}

export interface ModifyPasswordRequest {
  password: string
  passwordCheck: string
}

export interface ModifyPasswordResponse {
  userDto: User
}

export const userApi = {
  /**
   * 사용자 이름 변경
   * POST /api/user/name
   */
  modifyName: (data: ModifyNameRequest): Promise<ModifyNameResponse> =>
    http.post('/api/user/name', data),

  /**
   * 사용자 비밀번호 변경
   * POST /api/user/password
   */
  modifyPassword: (data: ModifyPasswordRequest): Promise<ModifyPasswordResponse> =>
    http.post('/api/user/password', data),
}

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