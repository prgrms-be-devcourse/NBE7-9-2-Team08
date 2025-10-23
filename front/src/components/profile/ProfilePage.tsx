'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/hooks/auth/useAuth'
import { userApi } from '@/lib/api/user'
import { validateName, validatePasswordChangeForm } from '@/lib/utils/validation'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/input'
import { Card } from '@/components/ui/card'
import { useToast } from '@/components/ui/Toast'

export function ProfilePage() {
  const { user, fetchUserInfo } = useAuth()
  const toast = useToast()
  
  // 이름 변경 관련 상태
  const [displayName, setDisplayName] = useState('')
  const [isChangingName, setIsChangingName] = useState(false)
  const [nameError, setNameError] = useState('')
  const [nameSuccess, setNameSuccess] = useState(false)

  // 비밀번호 변경 관련 상태
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [newPasswordConfirm, setNewPasswordConfirm] = useState('')
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [passwordError, setPasswordError] = useState('')
  const [passwordSuccess, setPasswordSuccess] = useState(false)

  // 사용자 정보가 로드되면 표시 이름 설정
  useEffect(() => {
    if (user?.name) {
      setDisplayName(user.name)
    }
  }, [user])

  // 이름 변경 처리
  const handleChangeName = async () => {
    const validation = validateName(displayName)
    if (!validation.isValid) {
      setNameError(validation.message || '')
      setNameSuccess(false)
      return
    }

    setIsChangingName(true)
    setNameError('')
    setNameSuccess(false)

    try {
      await userApi.modifyName({ name: displayName })
      
      // 사용자 정보 업데이트
      await fetchUserInfo()
      
      setNameSuccess(true)
      toast.push('이름이 성공적으로 변경되었습니다.')
      
      // 3초 후 성공 메시지 숨기기
      setTimeout(() => setNameSuccess(false), 3000)
    } catch (error) {
      console.error('이름 변경 실패:', error)
      setNameError('이름 변경 중 오류가 발생했습니다.')
      setNameSuccess(false)
    } finally {
      setIsChangingName(false)
    }
  }

  // 비밀번호 변경 처리
  const handleChangePassword = async () => {
    const validation = validatePasswordChangeForm(newPassword, newPasswordConfirm)
    if (!validation.isValid) {
      setPasswordError(validation.message || '')
      setPasswordSuccess(false)
      return
    }

    setIsChangingPassword(true)
    setPasswordError('')
    setPasswordSuccess(false)

    try {
      await userApi.modifyPassword({ 
        password: newPassword, 
        passwordCheck: newPasswordConfirm 
      })
      
      setPasswordSuccess(true)
      toast.push('비밀번호가 성공적으로 변경되었습니다.')
      
      // 폼 초기화
      setCurrentPassword('')
      setNewPassword('')
      setNewPasswordConfirm('')
      
      // 3초 후 성공 메시지 숨기기
      setTimeout(() => setPasswordSuccess(false), 3000)
    } catch (error) {
      console.error('비밀번호 변경 실패:', error)
      setPasswordError('비밀번호 변경 중 오류가 발생했습니다.')
      setPasswordSuccess(false)
    } finally {
      setIsChangingPassword(false)
    }
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">사용자 정보를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">마이페이지</h1>
          <p className="text-gray-600 mt-2">계정 정보를 관리하세요</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 왼쪽 사이드바 */}
          <div className="lg:col-span-1">
            <Card className="p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">내 계정</h2>
              <nav className="space-y-2">
                <a 
                  href="#" 
                  className="block px-3 py-2 text-blue-600 bg-blue-50 rounded-md font-medium"
                >
                  프로필
                </a>
                <a 
                  href="#" 
                  className="block px-3 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                >
                  보안 (비밀번호)
                </a>
              </nav>
            </Card>
          </div>

          {/* 오른쪽 메인 콘텐츠 */}
          <div className="lg:col-span-2 space-y-8">
            {/* 프로필 정보 섹션 */}
            <Card className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900">프로필</h3>
                {nameSuccess && (
                  <div className="flex items-center text-green-600 bg-green-50 px-3 py-1 rounded-md">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm font-medium">변경사항이 저장되었습니다.</span>
                  </div>
                )}
              </div>

              <div className="flex items-start space-x-6">
                {/* 프로필 이미지 */}
                <div className="flex-shrink-0">
                  <div className="w-20 h-20 bg-gray-200 rounded-full flex items-center justify-center">
                    {user.imageUrl ? (
                      <img 
                        src={user.imageUrl} 
                        alt="프로필" 
                        className="w-20 h-20 rounded-full object-cover"
                      />
                    ) : (
                      <span className="text-2xl font-semibold text-gray-500">
                        {user.name.charAt(0).toUpperCase()}
                      </span>
                    )}
                  </div>
                  <div className="mt-3 space-y-2">
                    <Button variant="outline" size="sm" className="w-full">
                      사진 변경
                    </Button>
                    <Button variant="outline" size="sm" className="w-full text-red-600 hover:text-red-700">
                      삭제
                    </Button>
                  </div>
                </div>

                {/* 프로필 정보 폼 */}
                <div className="flex-1 space-y-6">
                  {/* 표시 이름 */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      표시 이름
                    </label>
                    <Input
                      type="text"
                      value={displayName}
                      onChange={(e) => setDisplayName(e.target.value)}
                      placeholder="고정 이름"
                      className={nameError ? 'border-red-500' : ''}
                    />
                    {nameError && (
                      <p className="text-red-500 text-sm mt-1">{nameError}</p>
                    )}
                    <p className="text-xs text-gray-500 mt-1">
                      최소 3글자, 최대 10글자, 특수문자 불가
                    </p>
                  </div>

                  {/* 이메일 */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      이메일
                    </label>
                    <Input
                      type="email"
                      value={user.email}
                      disabled
                      className="bg-gray-50"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      이메일은 변경할 수 없습니다
                    </p>
                  </div>

                  <div className="flex justify-end">
                    <Button
                      onClick={handleChangeName}
                      disabled={isChangingName || displayName === user.name}
                      className="bg-blue-600 hover:bg-blue-700"
                    >
                      {isChangingName ? '저장 중...' : '이름 변경'}
                    </Button>
                  </div>
                </div>
              </div>
            </Card>

            {/* 보안 - 비밀번호 변경 섹션 */}
            <Card className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900">보안 - 비밀번호 변경</h3>
                {passwordSuccess && (
                  <div className="flex items-center text-green-600 bg-green-50 px-3 py-1 rounded-md">
                    <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm font-medium">변경사항이 저장되었습니다.</span>
                  </div>
                )}
              </div>

              <div className="space-y-4">
                {/* 현재 비밀번호 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    현재 비밀번호
                  </label>
                  <Input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="********"
                  />
                </div>

                {/* 새 비밀번호 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    새 비밀번호
                  </label>
                  <Input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="8자 이상, 특수문자 포함"
                    className={passwordError ? 'border-red-500' : ''}
                  />
                </div>

                {/* 새 비밀번호 확인 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    새 비밀번호 확인
                  </label>
                  <Input
                    type="password"
                    value={newPasswordConfirm}
                    onChange={(e) => setNewPasswordConfirm(e.target.value)}
                    placeholder="********"
                    className={passwordError ? 'border-red-500' : ''}
                  />
                  {passwordError && (
                    <p className="text-red-500 text-sm mt-1">{passwordError}</p>
                  )}
                </div>

                <div className="flex justify-end">
                  <Button
                    onClick={handleChangePassword}
                    disabled={isChangingPassword || !newPassword || !newPasswordConfirm}
                    className="bg-blue-600 hover:bg-blue-700"
                  >
                    {isChangingPassword ? '변경 중...' : '비밀번호 변경'}
                  </Button>
                </div>
              </div>
            </Card>

            {/* 위험 구역 섹션 */}
            <Card className="p-6 border-red-200">
              <h3 className="text-xl font-semibold text-gray-900 mb-4">위험 구역</h3>
              <div className="space-y-4">
                <div className="p-4 bg-red-50 border border-red-200 rounded-md">
                  <p className="text-red-800 font-medium">
                    계정을 영구 삭제합니다. 복구할 수 없습니다.
                  </p>
                </div>
                <div className="flex justify-end">
                  <Button
                    variant="outline"
                    className="border-red-300 text-red-600 hover:bg-red-50 hover:border-red-400"
                  >
                    계정 삭제
                  </Button>
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
