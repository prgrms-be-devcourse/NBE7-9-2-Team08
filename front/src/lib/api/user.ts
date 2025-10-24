import { http } from './client'
import type { GetUserResponse } from './auth'

export interface UpdateNameRequest {
  name: string
}

export interface UpdateNameResponse {
  userDto: GetUserResponse
}

export interface UpdatePasswordRequest {
  password: string
  passwordCheck: string
}

export interface UpdatePasswordResponse {
  userDto: GetUserResponse
}

export const userApi = {
  /**
   * 이름 변경
   * POST /api/user/name
   */
  updateName: (data: UpdateNameRequest): Promise<UpdateNameResponse> =>
    http.post('/user/name', data),

  /**
   * 비밀번호 변경
   * POST /api/user/password
   */
  updatePassword: (data: UpdatePasswordRequest): Promise<UpdatePasswordResponse> =>
    http.post('/user/password', data),
}