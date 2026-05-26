import { cn } from "@/lib/utils";

type BentoItem = {
  label: string;
  title: string;
  body: string;
};

type MagicBentoProps = {
  items: BentoItem[];
  className?: string;
};

/** Inspired by react-bits MagicBento — hover lift grid */
export function MagicBento({ items, className }: MagicBentoProps) {
  return (
    <div
      className={cn(
        "grid gap-3 sm:grid-cols-2",
        className,
      )}
    >
      {items.map((item) => (
        <article
          key={item.title}
          className="group rounded-2xl border border-[var(--pres-line)] bg-[var(--pres-panel)] p-4 backdrop-blur-md transition-[transform,box-shadow,border-color] duration-200 hover:-translate-y-0.5 hover:border-[var(--pres-accent)] hover:shadow-[var(--pres-shadow)] cursor-default"
        >
          <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
            {item.label}
          </div>
          <strong className="mt-1 block font-display text-base text-[var(--pres-text)]">
            {item.title}
          </strong>
          <p className="mt-2 text-sm leading-relaxed text-[var(--pres-muted)]">
            {item.body}
          </p>
        </article>
      ))}
    </div>
  );
}
