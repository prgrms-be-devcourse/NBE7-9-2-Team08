// LoadingStenList
"use client"

import { CheckCircle2, Loader2 } from "lucide-react"

interface Step {
  label: string
  description: string
}

export default function LoadingStepList({
  steps,
  currentStep,
}: {
  steps: Step[]
  currentStep: number
}) {
  return (
    <div className="space-y-4">
      {steps.map((step, index) => (
        <div
          key={index}
          className={`flex items-start gap-4 rounded-xl border p-4 transition-all ${
            index < currentStep
              ? "border-primary/50 bg-primary/5"
              : index === currentStep
                ? "border-primary bg-primary/10"
                : "border-border bg-card/50"
          }`}
        >
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full">
            {index < currentStep ? (
              <CheckCircle2 className="h-5 w-5 text-primary" />
            ) : index === currentStep ? (
              <Loader2 className="h-5 w-5 animate-spin text-primary" />
            ) : (
              <div className="h-5 w-5 rounded-full border-2 border-muted" />
            )}
          </div>
          <div className="flex-1">
            <h3
              className={`font-semibold ${
                index <= currentStep ? "text-foreground" : "text-muted-foreground"
              }`}
            >
              {step.label}
            </h3>
            <p className="text-sm text-muted-foreground">{step.description}</p>
          </div>
        </div>
      ))}
    </div>
  )
}
