"use client"

import { Clock } from "lucide-react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import type { HistoryResponseDto } from "@/types/analysis"

interface Props {
  history: HistoryResponseDto
  selectedId: number | null
  onSelect: (val: number) => void
}

export function AnalysisHeader({ history, selectedId, onSelect }: Props) {
  return (
    <div className="mb-8">
      <h1 className="text-2xl font-bold mb-2">{history.repository.name}</h1>
      <p className="text-muted-foreground mb-4">{history.repository.description}</p>

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
    </div>
  )
}
