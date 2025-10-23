import { Card } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { CheckCircle2, AlertCircle } from "lucide-react"

export function AnalysisResultTabs({ strengths, improvements }: { strengths: string; improvements: string }) {
  const renderList = (text: string, Icon: any, color: string) =>
    text.split("\n").map((s, i) => {
      const clean = s.trim().replace(/^-+\s*/, "")
      return (
        <div key={i} className="flex items-start gap-2">
          <Icon className={`h-4 w-4 mt-[2px] ${color} shrink-0`} />
          <p className="text-muted-foreground text-sm leading-relaxed">{clean}</p>
        </div>
      )
    })

  return (
    <Card className="p-6 h-full">
      <Tabs defaultValue="strengths" className="h-full">
        <TabsList className="grid grid-cols-2 w-full">
          <TabsTrigger value="strengths">강점</TabsTrigger>
          <TabsTrigger value="improvements">개선사항</TabsTrigger>
        </TabsList>

        <TabsContent value="strengths" className="mt-6 h-full">
          <div className="space-y-3">{renderList(strengths, CheckCircle2, "text-green-500")}</div>
        </TabsContent>

        <TabsContent value="improvements" className="mt-6 h-full">
          <div className="space-y-3">{renderList(improvements, AlertCircle, "text-yellow-500")}</div>
        </TabsContent>
      </Tabs>
    </Card>
  )
}
