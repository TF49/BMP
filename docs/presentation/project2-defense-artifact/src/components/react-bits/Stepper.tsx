import { cn } from "@/lib/utils";

type Step = { label: string; title: string; body: string };

type StepperProps = {
  steps: Step[];
  className?: string;
  active?: boolean;
};

/** Inspired by react-bits Stepper */
export function Stepper({ steps, className }: StepperProps) {
  return (
    <ol className={cn("space-y-3", className)}>
      {steps.map((step, i) => (
        <li key={step.title} className="flex gap-3">
          <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-[var(--pres-accent)] text-sm font-semibold text-white">
            {i + 1}
          </span>
          <div>
            <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
              {step.label}
            </div>
            <strong className="font-display text-[var(--pres-text)]">{step.title}</strong>
            <p className="mt-1 text-sm text-[var(--pres-muted)]">{step.body}</p>
          </div>
        </li>
      ))}
    </ol>
  );
}
