import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type TiltedCardProps = {
  children: ReactNode;
  className?: string;
};

/** Inspired by react-bits TiltedCard */
export function TiltedCard({ children, className }: TiltedCardProps) {
  return (
    <div
      className={cn(
        "rounded-2xl border border-[var(--pres-line)] bg-[var(--pres-panel)] p-4 shadow-[var(--pres-shadow)] backdrop-blur-md",
        "transition-transform duration-200 ease-out hover:[transform:perspective(800px)_rotateX(2deg)_rotateY(-2deg)]",
        "motion-reduce:transition-none motion-reduce:hover:transform-none cursor-default",
        className,
      )}
    >
      {children}
    </div>
  );
}
