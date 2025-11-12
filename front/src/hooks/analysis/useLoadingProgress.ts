"use client"

import { useEffect, useRef, useState, useMemo } from "react"
import { useRouter } from "next/navigation"
import { ERROR_CODES, ERROR_MESSAGES, ErrorCode } from "@/types/api"
import { analysisApi } from "@/lib/api/analysis"

type AnalysisErrorKind = "repo" | "auth" | "rate" | "duplicate" | "server" | "network";

const defaultAnalysisError = {
  type: "server" as AnalysisErrorKind,
  message: "분석 처리 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.",
};

const errorCodeValues = new Set<string>(Object.values(ERROR_CODES))

const resolveErrorCode = (raw?: string): ErrorCode | undefined => {
  if (!raw) return undefined

  if (errorCodeValues.has(raw)) {
    return raw as ErrorCode
  }

  const matchedEntry = Object.entries(ERROR_CODES).find(([, value]) => value === raw)
  if (matchedEntry) {
    return matchedEntry[1]
  }

  return undefined
}

const mapErrorCodeToAlert = (
  code?: string,
  fallback?: string
): { type: AnalysisErrorKind; message: string } => {
  const resolvedCode = resolveErrorCode(code)

  if (!resolvedCode) {
    return {
      type: "server",
      message: fallback || defaultAnalysisError.message,
    }
  }

  // 메시지 매핑
  const message =
    ERROR_MESSAGES[resolvedCode] ||
    fallback ||
    defaultAnalysisError.message

    // 에러 유형 분류
    let type: AnalysisErrorKind = "server"

    const repoErrors = new Set<ErrorCode>([
      ERROR_CODES.GITHUB_REPO_NOT_FOUND,
      ERROR_CODES.GITHUB_API_FAILED,
      ERROR_CODES.GITHUB_REPO_TOO_LARGE,
    ])
  
    const authErrors = new Set<ErrorCode>([
      ERROR_CODES.GITHUB_INVALID_TOKEN,
      ERROR_CODES.UNAUTHORIZED,
      ERROR_CODES.FORBIDDEN,
    ])
  
    if (repoErrors.has(resolvedCode)) {
      type = "repo"
    } else if (authErrors.has(resolvedCode)) {
      type = "auth"
    } else if (resolvedCode === ERROR_CODES.GITHUB_RATE_LIMIT_EXCEEDED) {
      type = "rate"
    } else if (resolvedCode === ERROR_CODES.ANALYSIS_IN_PROGRESS) {
      type = "duplicate"
    } else if (resolvedCode === ERROR_CODES.NETWORK_ERROR) {
      type = "network"
    }
  
  return { type, message }
}

const stashAnalysisError = (payload: { type: AnalysisErrorKind; message: string }) => {
  try {
    sessionStorage.setItem("analysisError", JSON.stringify(payload))
  } catch {
    /* ignore */
  }
}

export function useAnalysisProgress(repoUrl?: string | null) {
  const router = useRouter()
  const [progress, setProgress] = useState(0)
  const [currentStep, setCurrentStep] = useState(0)
  const [statusMessage, setStatusMessage] = useState("분석 준비 중...")
  const [isCompleted, setIsCompleted] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [repositoryId, setRepositoryId] = useState<number | null>(null)
  const repositoryIdRef = useRef<number | null>(null)
  
  // ✅ 분석 요청 완료 여부만 추적 (SSE 연결과 무관)
  const hasRequestedAnalysis = useRef(false)

  const steps = useMemo(() => [
      { label: "분석 시작", description: "요청이 접수되었습니다." },
      { label: "GitHub 연결 중", description: "리포지토리 데이터 가져오는 중..." },
      { label: "커밋 히스토리 분석", description: "커밋 활동성 및 패턴 확인 중..." },
      { label: "문서화 품질 분석", description: "README 및 문서 검토 중..." },
      { label: "보안 구성 분석", description: "민감 정보, 빌드 파일 검토 중..." },
      { label: "테스트 구성 분석", description: "테스트 커버리지 및 폴더 구조 확인 중..." },
      { label: "CI/CD 설정 분석", description: "자동화 및 배포 파이프라인 검토 중..." },
      { label: "커뮤니티 활동 분석", description: "이슈/PR 및 협업 지표 분석 중..." },
      { label: "AI 평가", description: "수집된 리포지토리 데이터 평가 중..." },
      { label: "최종 리포트 생성", description: "결과를 정리하고 있습니다." },
    ], [])

  // ✅ 1. 분석 요청 useEffect (한 번만 실행)
  useEffect(() => {
    if (!repoUrl || hasRequestedAnalysis.current) return

    const user = localStorage.getItem("user")
    const userId = user ? JSON.parse(user)?.id : null
    if (!userId) {
      router.push("/login")
      return
    }

    hasRequestedAnalysis.current = true

    // ✅ 분석 요청만 먼저 보냄
    const requestAnalysis = async () => {
      try {
        console.log("📤 분석 요청 시작...")
        const data = await analysisApi.requestAnalysis({ githubUrl: repoUrl })
        const repoId = data.repositoryId
        setRepositoryId(repoId)
        repositoryIdRef.current = repoId
        console.log("✅ 분석 요청 성공:", data)
      } catch (err: any) {
        console.error("❌ 분석 요청 실패:", err)

        if (err.status === 409 || err.code === "ANALYSIS_IN_PROGRESS") {
          const duplicatePayload = {
            type: "duplicate" as AnalysisErrorKind,
            message: "이미 분석을 진행 중이에요. 잠시 후 다시 확인해 주세요.",
          }
          setError(duplicatePayload.message)
          setStatusMessage("중복 요청이 감지되었어요.")
          stashAnalysisError(duplicatePayload)
          setTimeout(() => router.push("/analysis"), 3000)
          return
        }

        const alertPayload = mapErrorCodeToAlert(err.code, err.message)
        setError(alertPayload.message)
        setStatusMessage("요청 처리 중 문제가 발생했어요.")
        stashAnalysisError(alertPayload)
        setTimeout(() => router.push("/analysis"), 3000)
      }
    }

    // ✅ 즉시 실행 (setTimeout 제거)
    requestAnalysis()
  }, [repoUrl, router])

  // ✅ 2. SSE 연결 useEffect (userId가 있으면 항상 유지)
  useEffect(() => {
    const user = localStorage.getItem("user")
    const userId = user ? JSON.parse(user)?.id : null
    if (!userId) return

    console.log("[SSE] 연결 시도 중...")

    // ✅ 이벤트 핸들러 정의
    const handleStatus = (e: any) => {
      const message = e.detail
      console.log("[SSE][status]", message)
      setStatusMessage(message)

      let stepIndex = steps.findIndex((s) =>
        message.replace(/\s+/g, "").includes(s.label.replace(/\s+/g, ""))
      )
      if (message.includes("커뮤니티 활동 분석")) {
        stepIndex = steps.length - 1
      }

      if (stepIndex !== -1) {
        setCurrentStep(stepIndex)
        setProgress(Math.min(((stepIndex + 1) / steps.length) * 100, 99))
      }
    }

    const handleComplete = (e: any) => {
      console.log("[SSE][complete]", e.detail)
      setStatusMessage("분석 완료!")
      setProgress(100)
      setIsCompleted(true)

      setTimeout(() => {
        const repoId = repositoryIdRef.current
        if (repoId) router.push(`/analysis/${repoId}`)
      }, 1500)
    }

    const handleError = (e: any) => {
      console.error("[SSE][error]", e.detail)
      setError("❌ 서버에 문제가 발생했어요.")
      setTimeout(() => router.push("/analysis"), 3000)
    }

    // ✅ 이벤트 리스너 등록
    window.addEventListener("SSE_STATUS", handleStatus)
    window.addEventListener("SSE_COMPLETE", handleComplete)
    window.addEventListener("SSE_ERROR", handleError)

    // ✅ SSE 연결 시작
    const disconnect = analysisApi.connectStream(userId)

    // ✅ cleanup - SSE 연결만 끊음
    return () => {
      console.log("[SSE] 연결 종료")
      disconnect()
      window.removeEventListener("SSE_STATUS", handleStatus)
      window.removeEventListener("SSE_COMPLETE", handleComplete)
      window.removeEventListener("SSE_ERROR", handleError)
    }
  }, [router, steps]) // ✅ repoUrl 의존성 제거

  return { progress, currentStep, steps, statusMessage, isCompleted, error }
}