import { cn } from "@/lib/utils";

type ShinyTextProps = {
  text: string;
  className?: string;
};

/** Inspired by react-bits ShinyText */
export function ShinyText({ text, className }: ShinyTextProps) {
  return (
    <span
      className={cn(
        "relative inline-block bg-[length:200%_auto] bg-clip-text text-transparent",
        "animate-[shine_4s_linear_infinite] bg-gradient-to-r from-[var(--pres-text)] via-[var(--pres-accent)] to-[var(--pres-text)]",
        "motion-reduce:animate-none motion-reduce:text-[var(--pres-text)]",
        className,
      )}
    >
      {text}
    </span>
  );
}
