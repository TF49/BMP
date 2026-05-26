type ChapterDockProps = {
  step?: string;
  title?: string;
  claim?: string;
};

export function ChapterDock({ step, title, claim }: ChapterDockProps) {
  return (
    <div className="fixed bottom-[18px] left-5 z-30 w-[min(280px,calc(100vw-64px))] rounded-[22px] border border-[var(--pres-line)] bg-[var(--pres-panel)] px-4 py-3.5 shadow-[var(--pres-shadow)] backdrop-blur-xl">
      <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
        {step}
      </div>
      <div className="mt-1 font-display text-sm font-bold text-[var(--pres-text)]">
        {title}
      </div>
      <p className="mt-1 text-sm leading-relaxed text-[var(--pres-muted)]">
        {claim}
      </p>
    </div>
  );
}
