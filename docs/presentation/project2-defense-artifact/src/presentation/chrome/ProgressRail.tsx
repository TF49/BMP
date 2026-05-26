import { cn } from "@/lib/utils";

type ProgressRailProps = {
  chapters: { id: string; dotLabel?: string; title?: string }[];
  activeDot: number;
  onSelect: (id: string) => void;
};

export function ProgressRail({
  chapters,
  activeDot,
  onSelect,
}: ProgressRailProps) {
  return (
    <nav
      className="fixed right-[22px] top-1/2 z-30 flex -translate-y-1/2 flex-col items-center gap-3"
      aria-label="章节进度"
    >
      <div className="absolute bottom-2.5 top-2.5 w-0.5 bg-gradient-to-b from-[var(--pres-line)] to-[rgba(23,25,31,0.18)]" />
      {chapters.map((ch, index) => (
        <button
          key={ch.id}
          type="button"
          aria-label={ch.dotLabel || ch.title}
          aria-current={index === activeDot ? "step" : undefined}
          onClick={() => onSelect(ch.id)}
          className={cn(
            "group relative h-3 w-3 cursor-pointer rounded-full border-0 transition-transform duration-200",
            index === activeDot
              ? "scale-125 bg-gradient-to-br from-[var(--pres-accent)] to-[var(--pres-accent-3)]"
              : "bg-[rgba(23,25,31,0.18)] hover:scale-110",
          )}
        >
          <span className="pointer-events-none absolute right-6 top-1/2 -translate-y-1/2 whitespace-nowrap rounded-full border border-[var(--pres-line)] bg-[var(--pres-panel)] px-2.5 py-1 text-xs opacity-0 transition-opacity group-hover:opacity-100 group-aria-[current=step]:opacity-100">
            {ch.dotLabel || ch.title}
          </span>
        </button>
      ))}
    </nav>
  );
}
