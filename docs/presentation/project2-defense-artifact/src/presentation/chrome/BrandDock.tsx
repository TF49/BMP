type BrandDockProps = {
  kicker?: string;
  title?: string;
  subtitle?: string;
};

export function BrandDock({ kicker, title, subtitle }: BrandDockProps) {
  return (
    <div className="fixed left-5 top-[18px] z-30 max-w-[280px] rounded-[22px] border border-[var(--pres-line)] bg-[var(--pres-panel)] px-4 py-3.5 shadow-[var(--pres-shadow)] backdrop-blur-xl">
      <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
        {kicker}
      </div>
      <div className="mt-1 font-display text-base font-bold text-[var(--pres-text)]">
        {title}
      </div>
      <p className="mt-1 text-sm leading-relaxed text-[var(--pres-muted)]">
        {subtitle}
      </p>
    </div>
  );
}
