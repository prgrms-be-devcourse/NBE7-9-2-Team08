// LoadingInfoBox
"use client"

export default function LoadingInfoBox() {
  return (
    <div className="mt-12 rounded-xl border border-border bg-muted/30 p-6 text-center">
      <p className="text-sm text-muted-foreground">
        일반적으로 20-30초가 소요됩니다. <br /> 
        커밋 히스토리, 이슈 관리, 문서화 품질, 협업 패턴 등을 분석하고 있습니다.
      </p>
    </div>
  )
}
