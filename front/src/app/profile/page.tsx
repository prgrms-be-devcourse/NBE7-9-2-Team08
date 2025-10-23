"use client"

import { useState, useEffect } from "react"
import { useAuth } from "@/hooks/auth/useAuth"
import { userApi } from "@/lib/api/user"
import { Button } from "@/components/ui/Button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card } from "@/components/ui/card"
import { useToast } from "@/components/ui/Toast"
import { User, Mail, Lock, Save } from "lucide-react"

export default function ProfilePage() {
  const { user, fetchUserInfo, updateUserInfo } = useAuth()
  const toast = useToast()
  
  const [isEditingName, setIsEditingName] = useState(false)
  const [isEditingPassword, setIsEditingPassword] = useState(false)
  const [name, setName] = useState(user?.name || "")
  const [password, setPassword] = useState("")
  const [passwordCheck, setPasswordCheck] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  // ✅ 사용자 정보가 변경될 때마다 로컬 상태 업데이트
  useEffect(() => {
    if (user) {
      setName(user.name)
    }
  }, [user])

  const handleNameUpdate = async () => {
    if (!name.trim()) {
      toast.push("이름을 입력해주세요.")
      return
    }

    try {
      setIsLoading(true)
      const response = await userApi.updateName({ name: name.trim() })
      toast.push("이름이 성공적으로 변경되었습니다.")
      setIsEditingName(false)
      
      // ✅ 업데이트된 사용자 정보로 전역 상태 갱신
      if (response.userDto) {
        updateUserInfo(response.userDto)
        console.log('프로필 페이지에서 사용자 정보 업데이트 완료:', response.userDto)
      }
      
      // ✅ 페이지 새로고침으로 헤더 업데이트 보장
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } catch (error) {
      console.error("이름 변경 실패:", error)
      toast.push("이름 변경에 실패했습니다.")
    } finally {
      setIsLoading(false)
    }
  }

  const handlePasswordUpdate = async () => {
    if (!password.trim()) {
      toast.push("새 비밀번호를 입력해주세요.")
      return
    }

    if (password !== passwordCheck) {
      toast.push("비밀번호가 일치하지 않습니다.")
      return
    }

    if (password.length < 6) {
      toast.push("비밀번호는 6자 이상이어야 합니다.")
      return
    }

    try {
      setIsLoading(true)
      const response = await userApi.updatePassword({ 
        password: password.trim(), 
        passwordCheck: passwordCheck.trim() 
      })
      toast.push("비밀번호가 성공적으로 변경되었습니다.")
      setIsEditingPassword(false)
      setPassword("")
      setPasswordCheck("")
      
      // ✅ 업데이트된 사용자 정보로 전역 상태 갱신
      if (response.userDto) {
        updateUserInfo(response.userDto)
      }
      
      // ✅ 페이지 새로고침으로 헤더 업데이트 보장
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } catch (error) {
      console.error("비밀번호 변경 실패:", error)
      toast.push("비밀번호 변경에 실패했습니다.")
    } finally {
      setIsLoading(false)
    }
  }

  const cancelNameEdit = () => {
    setName(user?.name || "")
    setIsEditingName(false)
  }

  const cancelPasswordEdit = () => {
    setPassword("")
    setPasswordCheck("")
    setIsEditingPassword(false)
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">로그인이 필요합니다</h1>
          <p className="text-muted-foreground">마이페이지를 보려면 로그인해주세요.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="mx-auto max-w-2xl">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold mb-2">마이페이지</h1>
            <p className="text-muted-foreground">개인정보를 수정할 수 있습니다.</p>
          </div>

          <div className="space-y-6">
            {/* 이메일 정보 (수정 불가) */}
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <Mail className="h-5 w-5 text-muted-foreground" />
                <h2 className="text-lg font-semibold">이메일</h2>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">{user.email}</span>
                <span className="text-xs bg-muted px-2 py-1 rounded">고정</span>
              </div>
            </Card>

            {/* 이름 수정 */}
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <User className="h-5 w-5 text-muted-foreground" />
                <h2 className="text-lg font-semibold">이름</h2>
              </div>
              
              {isEditingName ? (
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="name">새 이름</Label>
                    <Input
                      id="name"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="이름을 입력하세요"
                      disabled={isLoading}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button 
                      onClick={handleNameUpdate} 
                      disabled={isLoading}
                      size="sm"
                    >
                      <Save className="h-4 w-4 mr-2" />
                      {isLoading ? "저장 중..." : "저장"}
                    </Button>
                    <Button 
                      variant="outline" 
                      onClick={cancelNameEdit}
                      disabled={isLoading}
                      size="sm"
                    >
                      취소
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground">{user.name}</span>
                  <Button 
                    variant="outline" 
                    onClick={() => setIsEditingName(true)}
                    size="sm"
                  >
                    수정
                  </Button>
                </div>
              )}
            </Card>

            {/* 비밀번호 수정 */}
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <Lock className="h-5 w-5 text-muted-foreground" />
                <h2 className="text-lg font-semibold">비밀번호</h2>
              </div>
              
              {isEditingPassword ? (
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="password">새 비밀번호</Label>
                    <Input
                      id="password"
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="새 비밀번호를 입력하세요"
                      disabled={isLoading}
                    />
                  </div>
                  <div>
                    <Label htmlFor="passwordCheck">비밀번호 확인</Label>
                    <Input
                      id="passwordCheck"
                      type="password"
                      value={passwordCheck}
                      onChange={(e) => setPasswordCheck(e.target.value)}
                      placeholder="비밀번호를 다시 입력하세요"
                      disabled={isLoading}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button 
                      onClick={handlePasswordUpdate} 
                      disabled={isLoading}
                      size="sm"
                    >
                      <Save className="h-4 w-4 mr-2" />
                      {isLoading ? "저장 중..." : "저장"}
                    </Button>
                    <Button 
                      variant="outline" 
                      onClick={cancelPasswordEdit}
                      disabled={isLoading}
                      size="sm"
                    >
                      취소
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground">••••••••</span>
                  <Button 
                    variant="outline" 
                    onClick={() => setIsEditingPassword(true)}
                    size="sm"
                  >
                    수정
                  </Button>
                </div>
              )}
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
