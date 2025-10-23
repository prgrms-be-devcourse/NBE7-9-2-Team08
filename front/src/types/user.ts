export interface User {
  id: number
  email: string
  name: string
  imageUrl?: string
}

export interface StoredUser {
  id: number
  email: string
  name: string
  imageUrl: string | null
}