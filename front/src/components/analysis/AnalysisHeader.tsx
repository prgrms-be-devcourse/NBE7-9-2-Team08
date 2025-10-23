"use client"

import { useToast } from "@/components/ui/Toast"
import { Clock, Trash2 } from "lucide-react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/Button"
import { analysisApi } from "@/lib/api/analysis"
import type { HistoryResponseDto } from "@/types/analysis"
import { useState } from "react"

interface Props {
  history: HistoryResponseDto
  selectedId: number | null
  onSelect: (val: number) => void
  userId: number | null
  repoId: number
  onDeleted?: () => void
}

export function AnalysisHeader({ history, selectedId, onSelect, userId, repoId, onDeleted }: Props) {
  const { push } = useToast()
  const [deleting, setDeleting] = useState(false)
  const isOwner = userId === history.repository.ownerId
  
  console.log("userId: ", userId);
  console.log("selectedId: ", selectedId);
  console.log("repoId: ", repoId);

  const handleDelete = async () => {
    if (!userId || !repoId || !selectedId) return

    const confirmed = window.confirm("정말로 이 분석 결과를 삭제하시겠습니까?")
    if (!confirmed) return

    try {
      setDeleting(true)

      if (history.analysisVersions.length === 1) {
        await analysisApi.deleteRepository(userId, repoId)
        push("리포지토리가 삭제되었습니다.")
      } else {
        await analysisApi.deleteAnalysisResult(userId, repoId, selectedId)
        push("분석 결과가 삭제되었습니다.")
      }

      onDeleted?.()
    } catch (error) {
      console.error("❌ 삭제 실패:", error)
      push("삭제 중 오류가 발생했습니다.") // ✅ 동일하게 push 사용
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="mb-8">
      <div>
        <h1 className="text-2xl font-bold mb-2">{history.repository.name}</h1>
        <p className="text-muted-foreground mb-4">{history.repository.description}</p>
      </div>
      
      <div className="flex items-center gap-3">
        <Select value={selectedId?.toString() || ""} onValueChange={(val) => onSelect(Number(val))}>
          <SelectTrigger className="w-[260px]">
            <SelectValue placeholder="분석 버전 선택" />
          </SelectTrigger>
          <SelectContent>
            {history.analysisVersions.map((ver) => (
              <SelectItem key={ver.analysisId} value={ver.analysisId.toString()}>
                <div className="flex items-center gap-2">
                  <Clock className="h-3 w-3" />
                  <span>{ver.versionLabel}</span>
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

      
        {/* 🗑️ 삭제 버튼: 본인만 노출 */}
        {isOwner && (
          <Button
            variant="destructive"
            size="sm"
            onClick={handleDelete}
            disabled={deleting}
            className="flex items-center gap-1"
          >
            <Trash2 className="h-4 w-4" />
            {deleting ? "삭제 중..." : "삭제"}
          </Button>
        )}
      </div>
    </div>
  )
}
