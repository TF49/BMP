import { cn } from "@/lib/utils";

/** Lightweight aurora backdrop inspired by react-bits Backgrounds/Aurora */
export function Aurora({ className }: { className?: string }) {
  return (
    <div
      className={cn("pointer-events-none fixed inset-0 z-0 overflow-hidden", className)}
      aria-hidden
    >
      <div className="absolute -left-[20%] top-[-10%] h-[50vh] w-[50vw] rounded-full bg-[var(--pres-accent)] opacity-[0.08] blur-[80px] motion-reduce:opacity-[0.04]" />
      <div className="absolute right-[-10%] top-[10%] h-[45vh] w-[45vw] rounded-full bg-[var(--pres-accent-3)] opacity-[0.1] blur-[72px] motion-reduce:opacity-[0.04]" />
      <div className="absolute bottom-[5%] left-[30%] h-[40vh] w-[40vw] rounded-full bg-[var(--pres-accent-2)] opacity-[0.08] blur-[64px] motion-reduce:opacity-[0.04]" />
    </div>
  );
}
