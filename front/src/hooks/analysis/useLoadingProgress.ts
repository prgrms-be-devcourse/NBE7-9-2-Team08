"use client"

import { useEffect, useRef, useState, useMemo } from "react"
import { useRouter } from "next/navigation"

type AnalysisErrorKind = "repo" | "auth" | "rate" | "duplicate" | "server" | "network";
const defaultAnalysisError = {
  type: "server" as AnalysisErrorKind,
  message: "분석 처리 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.",
};

const stashAnalysisError = (payload: { type: AnalysisErrorKind; message: string }) => {
  try {
    sessionStorage.setItem("analysisError", JSON.stringify(payload));
  } catch {
    /* ignore */
  }
};

const mapErrorCodeToAlert = (
  code?: string,
  fallback?: string
): { type: AnalysisErrorKind; message: string } => {
  switch (code) {
    case "GITHUB_REPO_NOT_FOUND":
      return { type: "repo", message: "리포지토리 URL을 다시 한번 확인해 주세요." };
    case "GITHUB_INVALID_TOKEN":
      return { type: "auth", message: "GitHub 인증에 실패했어요. 다시 로그인 후 시도해 주세요." };
    case "GITHUB_RATE_LIMIT_EXCEEDED":
      return { type: "rate", message: "요청이 많아요. 잠시 기다렸다 다시 시도해 주세요." };
    case "FORBIDDEN":
      return { type: "auth", message: "해당 리포지토리에 접근 권한이 없어요." };
    case "GITHUB_API_FAILED":
      return { type: "repo", message: "요청을 처리할 수 없었어요. 입력값을 다시 확인해 주세요." };
    case "GITHUB_API_SERVER_ERROR":
      return { type: "server", message: "GitHub 서버에서 오류가 발생했어요. 잠시 후 다시 시도해 주세요." };
    default:
      return {
        type: defaultAnalysisError.type,
        message: fallback || defaultAnalysisError.message,
      };
  }
};


export function useAnalysisProgress(repoUrl?: string | null) {
  const router = useRouter()
  const [progress, setProgress] = useState(0)
  const [currentStep, setCurrentStep] = useState(0)
  const [statusMessage, setStatusMessage] = useState("분석 준비 중...")
  const [isCompleted, setIsCompleted] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // ✅ repositoryId를 state + ref 모두 저장
  const [repositoryId, setRepositoryId] = useState<number | null>(null)
  const repositoryIdRef = useRef<number | null>(null)

  const steps = useMemo(
    () => [
      { label: "분석 시작", description: "요청이 접수되었습니다." },
      { label: "GitHub 연결 중", description: "리포지토리 데이터 가져오는 중..." },
      { label: "커밋 히스토리 분석", description: "커밋 활동성 및 패턴 확인 중..." },
      { label: "문서화 품질 분석", description: "README 및 문서 검토 중..." },
      { label: "보안 구성 분석", description: "민감 정보, 빌드 파일 검토 중..." },
      { label: "테스트 구성 분석", description: "테스트 커버리지 및 폴더 구조 확인 중..." },
      { label: "CI/CD 설정 분석", description: "자동화 및 배포 파이프라인 검토 중..." },
      { label: "커뮤니티 활동 분석", description: "이슈/PR 및 협업 지표 분석 중..." },
      { label: "최종 리포트 생성", description: "결과를 정리하고 있습니다." },
    ],
    []
  )

  useEffect(() => {
    if (!repoUrl) return

    const user = localStorage.getItem("user")
    const userId = user ? JSON.parse(user)?.id : null
    if (!userId) {
      router.push("/login")
      return
    }

    const baseUrl = process.env.NEXT_PUBLIC_BACKEND_URL
    const eventSource = new EventSource(`${baseUrl}/api/analysis/stream/${userId}`, {
      withCredentials: true,
    })

    console.log("[SSE] 연결 시도 중...")

    eventSource.onopen = () => {
      console.log("[SSE] 연결 성공")
      setStatusMessage("분석 시작")
      setProgress(5)

      // ✅ 분석 요청 시작
      setTimeout(async () => {
        try {
          const res = await fetch(`${baseUrl}/api/analysis?userId=${userId}`, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ githubUrl: repoUrl }),
          })
          
          if (!res.ok) {
            const errorData = await res.json();
            if (res.status === 409) {
              const duplicatePayload = {
                type: "duplicate" as AnalysisErrorKind,
                message: "이미 분석을 진행 중이에요. 잠시 후 다시 확인해 주세요.",
              };
              setError(duplicatePayload.message);
              setStatusMessage("중복 요청이 감지되었어요.");
              stashAnalysisError(duplicatePayload);
              eventSource.close();
          
              setTimeout(() => {
                router.push("/analysis");
              }, 3000);
              return;
            }
          
            const alertPayload = mapErrorCodeToAlert(errorData?.code, errorData?.message);
            setError(alertPayload.message);
            setStatusMessage("요청 처리 중 문제가 발생했어요.");
            stashAnalysisError(alertPayload);
            eventSource.close();
          
            setTimeout(() => {
              router.push("/analysis");
            }, 3000);
            return;
          }

          const data = await res.json()
          const repoId = data.data.repositoryId
          setRepositoryId(repoId)
          repositoryIdRef.current = repoId

          console.log("✅ 분석 요청 성공:", data)
        } catch (e) {
          console.error("❌ 분석 요청 실패:", e)
          setError("분석 요청 실패. 다시 시도해 주세요.")
        }
      }, 500)
    }

    eventSource.addEventListener("status", (e) => {
      const message = e.data
      console.log("[SSE][status]", message)
      setStatusMessage(message)

      let stepIndex = steps.findIndex((s) =>
        message.replace(/\s+/g, "").includes(s.label.replace(/\s+/g, ""))
      )

      if (message.includes("커뮤니티 활동 분석")) {
        stepIndex = steps.length - 1 // "최종 리포트 생성" 단계 인덱스로 이동
      }

      if (stepIndex !== -1) {
        setCurrentStep(stepIndex)
        setProgress(Math.min(((stepIndex + 1) / steps.length) * 100, 99))
      }
    })

    eventSource.addEventListener("complete", (e) => {
      console.log("[SSE][complete]", e.data)
      setStatusMessage("분석 완료!")
      setProgress(100)
      setIsCompleted(true)
      eventSource.close()

      // ✅ ref에서 최신 repositoryId로 즉시 라우팅
      setTimeout(() => {
        const repoId = repositoryIdRef.current
        if (repoId) {
          console.log(`🚀 라우팅 시도 → /analysis/${repoId}`)
          router.push(`/analysis/${repoId}`)
        } else {
          console.warn("⚠️ repositoryId가 존재하지 않아 라우팅하지 않음")
        }
      }, 1500)
    })

    eventSource.onerror = (err) => {
      console.error("[SSE][error]", err)
      setError("❌ SSE 연결이 끊어졌습니다.")
      eventSource.close()
    }

    return () => {
      console.log("[SSE] 연결 종료")
      eventSource.close()
    }
  }, [repoUrl, router, steps])

  return { progress, currentStep, steps, statusMessage, isCompleted, error }
}
