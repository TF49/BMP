import { cn } from "@/lib/utils";

type GradientTextProps = {
  text: string;
  className?: string;
};

/** Inspired by react-bits GradientText */
export function GradientText({ text, className }: GradientTextProps) {
  return (
    <span
      className={cn(
        "bg-gradient-to-r from-[var(--pres-accent)] via-[var(--pres-accent-3)] to-[var(--pres-accent-2)] bg-clip-text text-transparent",
        className,
      )}
    >
      {text}
    </span>
  );
}
