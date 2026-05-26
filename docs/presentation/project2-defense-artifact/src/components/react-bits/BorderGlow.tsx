import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type BorderGlowProps = {
  children: ReactNode;
  className?: string;
};

/** Inspired by react-bits BorderGlow */
export function BorderGlow({ children, className }: BorderGlowProps) {
  return (
    <div
      className={cn(
        "relative rounded-2xl p-[1px]",
        "bg-gradient-to-br from-[var(--pres-accent)] via-[var(--pres-accent-3)] to-[var(--pres-accent-2)]",
        "motion-reduce:bg-[var(--pres-line)]",
        className,
      )}
    >
      <div className="rounded-[calc(1rem-1px)] bg-[var(--pres-panel)] p-4 backdrop-blur-md">
        {children}
      </div>
    </div>
  );
}
