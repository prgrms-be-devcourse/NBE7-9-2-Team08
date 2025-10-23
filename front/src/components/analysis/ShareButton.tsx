import { Button } from "@/components/ui/Button"
import { Share2 } from "lucide-react"
import { useState } from "react"

export function ShareButton() {
  const [copied, setCopied] = useState(false)

  const handleShare = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href)
      setCopied(true)
      // ✅ 2초 뒤 “복사됨” 상태 초기화
      setTimeout(() => setCopied(false), 2000)
    } catch (err) {
      console.error("❌ URL 복사 실패:", err)
      alert("URL 복사 중 오류가 발생했습니다.")
    }
  }

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleShare}
      className={`gap-2 bg-transparent transition ${
        copied ? "border-green-500 text-green-500" : ""
      }`}
    >
      <Share2 className="h-4 w-4" />
      {copied ? "복사됨!" : "공유"}
    </Button>
  )
}
