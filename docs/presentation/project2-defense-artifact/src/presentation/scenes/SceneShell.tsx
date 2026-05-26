import type { Chapter } from "@/data/types";
import { CountUp } from "@/components/react-bits/CountUp";
import { cn } from "@/lib/utils";

type SceneShellProps = {
  chapter: Chapter;
  isActive: boolean;
  supportSlot: React.ReactNode;
  titleSlot?: React.ReactNode;
  bodySlot?: React.ReactNode;
  className?: string;
};

export function SceneShell({
  chapter,
  isActive,
  supportSlot,
  titleSlot,
  bodySlot,
  className,
}: SceneShellProps) {
  return (
    <div
      className={cn(
        "rounded-[28px] border border-[var(--pres-line)] bg-[var(--pres-panel)] p-8 shadow-[var(--pres-shadow)] backdrop-blur-xl",
        className,
      )}
    >
      <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr]">
        <div>
          <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
            {chapter.eyebrow || chapter.step}
          </div>
          {titleSlot ?? (
            <h1 className="mt-2 font-display text-3xl font-bold leading-tight text-[var(--pres-text)] md:text-4xl">
              {chapter.title}
            </h1>
          )}
          <p className="mt-3 text-lg leading-relaxed text-[var(--pres-muted)]">
            {chapter.claim}
          </p>
          {bodySlot ?? (
            <div className="mt-5 space-y-3 text-[var(--pres-muted)]">
              {(chapter.body ?? []).map((p) => (
                <p key={p} className="leading-relaxed">
                  {p}
                </p>
              ))}
            </div>
          )}
          {chapter.metrics && chapter.metrics.length > 0 && (
            <div className="mt-6 grid gap-3 sm:grid-cols-2">
              {chapter.metrics.map((m) => (
                <article
                  key={m.label}
                  className="rounded-2xl border border-[var(--pres-line)] bg-[rgba(255,255,255,0.35)] p-4"
                >
                  <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
                    {m.label}
                  </div>
                  <div className="mt-1 font-display text-2xl font-bold text-[var(--pres-text)]">
                    <CountUp value={m.value ?? "--"} active={isActive} />
                  </div>
                  {m.note && (
                    <p className="mt-1 text-sm text-[var(--pres-muted)]">{m.note}</p>
                  )}
                </article>
              ))}
            </div>
          )}
        </div>
        <div>{supportSlot}</div>
      </div>
    </div>
  );
}
