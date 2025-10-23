import { Card } from "@/components/ui/card"
import { RadarChartComponent } from "@/components/ui/radar-chart"

export function AnalysisRadarCard({ data }: { data: any[] }) {
  return (
    <Card className="p-6 flex items-center justify-center h-full">
      <RadarChartComponent data={data} />
    </Card>
  )
}
