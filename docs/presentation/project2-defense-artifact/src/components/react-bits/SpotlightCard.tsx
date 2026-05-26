import { useRef, type ReactNode } from "react";
import { cn } from "@/lib/utils";

type SpotlightCardProps = {
  children: ReactNode;
  className?: string;
};

/** Inspired by react-bits SpotlightCard */
export function SpotlightCard({ children, className }: SpotlightCardProps) {
  const ref = useRef<HTMLDivElement>(null);

  const onMove = (e: React.MouseEvent) => {
    const el = ref.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    el.style.setProperty("--spot-x", `${x}px`);
    el.style.setProperty("--spot-y", `${y}px`);
  };

  return (
    <div
      ref={ref}
      onMouseMove={onMove}
      className={cn(
        "relative overflow-hidden rounded-2xl border border-[var(--pres-line)] bg-[var(--pres-panel)] p-4 shadow-[var(--pres-shadow)] backdrop-blur-md",
        "before:pointer-events-none before:absolute before:inset-0 before:opacity-0 before:transition-opacity before:duration-300",
        "before:bg-[radial-gradient(circle_at_var(--spot-x)_var(--spot-y),rgba(34,104,246,0.15),transparent_55%)]",
        "hover:before:opacity-100 cursor-default",
        className,
      )}
    >
      {children}
    </div>
  );
}
