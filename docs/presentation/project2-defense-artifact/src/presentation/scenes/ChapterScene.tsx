import type { Chapter } from "@/data/types";
import { AnimatedList } from "@/components/react-bits/AnimatedList";
import { BlurText } from "@/components/react-bits/BlurText";
import { BorderGlow } from "@/components/react-bits/BorderGlow";
import { GradientText } from "@/components/react-bits/GradientText";
import { MagicBento } from "@/components/react-bits/MagicBento";
import { ShinyText } from "@/components/react-bits/ShinyText";
import { SpotlightCard } from "@/components/react-bits/SpotlightCard";
import { Stepper } from "@/components/react-bits/Stepper";
import { TiltedCard } from "@/components/react-bits/TiltedCard";
import { SceneShell } from "./SceneShell";

type ChapterSceneProps = {
  chapter: Chapter;
  isActive: boolean;
  onGoOpening?: () => void;
};

function SupportBlock({
  chapter,
  isActive,
}: {
  chapter: Chapter;
  isActive: boolean;
}) {
  const support = chapter.support ?? {};
  const items =
    support.items?.length ?
      support.items
    : [{ label: "Slot", title: "Support", body: "Replace with diagram or screenshot." }];

  const cards = (
    <div className="grid gap-3">
      {items.map((item) => (
        <SpotlightCard key={`${item.label}-${item.title}`}>
          <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
            {item.label}
          </div>
          <strong className="mt-1 block text-[var(--pres-text)]">{item.title}</strong>
          <p className="mt-2 text-sm leading-relaxed text-[var(--pres-muted)]">
            {item.body}
          </p>
        </SpotlightCard>
      ))}
    </div>
  );

  return (
    <div>
      <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
        {support.label}
      </div>
      <div className="mt-1 font-display text-lg font-semibold text-[var(--pres-text)]">
        {support.title}
      </div>
      <p className="mt-2 text-sm leading-relaxed text-[var(--pres-muted)]">
        {support.copy}
      </p>
      <div className="mt-4">
        {chapter.id === "booking_design" || chapter.id === "payment_backend" ? (
          <AnimatedList active={isActive} className="space-y-3">
            {items.map((item) => (
              <SpotlightCard key={`${item.label}-${item.title}`}>
                <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
                  {item.label}
                </div>
                <strong className="mt-1 block">{item.title}</strong>
                <p className="mt-2 text-sm text-[var(--pres-muted)]">{item.body}</p>
              </SpotlightCard>
            ))}
          </AnimatedList>
        ) : (
          cards
        )}
      </div>
    </div>
  );
}

export function ChapterScene({
  chapter,
  isActive,
  onGoOpening,
}: ChapterSceneProps) {
  const id = chapter.id;

  if (id === "opening") {
    return (
      <SceneShell
        chapter={chapter}
        isActive={isActive}
        titleSlot={
          <h1 className="mt-2 font-display text-3xl font-bold md:text-4xl">
            <BlurText text={chapter.title ?? ""} active={isActive} />
          </h1>
        }
        bodySlot={
          <div className="mt-5 space-y-3">
            <p className="text-lg">
              <GradientText text={chapter.body?.[0] ?? ""} />
            </p>
            {(chapter.body ?? []).slice(1).map((p) => (
              <p key={p} className="leading-relaxed text-[var(--pres-muted)]">
                {p}
              </p>
            ))}
          </div>
        }
        supportSlot={<SupportBlock chapter={chapter} isActive={isActive} />}
      />
    );
  }

  if (id === "background") {
    return (
      <SceneShell
        chapter={chapter}
        isActive={isActive}
        bodySlot={
          <AnimatedList active={isActive} className="mt-5 space-y-3">
            {(chapter.body ?? []).map((p) => (
              <p key={p} className="leading-relaxed text-[var(--pres-muted)]">
                {p}
              </p>
            ))}
          </AnimatedList>
        }
        supportSlot={<SupportBlock chapter={chapter} isActive={isActive} />}
      />
    );
  }

  if (id === "split") {
    const items = (chapter.support?.items ?? []).map((i) => ({
      label: i.label ?? "",
      title: i.title ?? "",
      body: i.body ?? "",
    }));
    return (
      <SceneShell
        chapter={chapter}
        isActive={isActive}
        supportSlot={
          <div>
            <p className="mb-4 text-sm text-[var(--pres-muted)]">
              {chapter.support?.copy}
            </p>
            <MagicBento items={items} />
          </div>
        }
      />
    );
  }

  if (id === "booking_design") {
    const steps = (chapter.support?.items ?? []).map((i) => ({
      label: i.label ?? "",
      title: i.title ?? "",
      body: i.body ?? "",
    }));
    return (
      <SceneShell
        chapter={chapter}
        isActive={isActive}
        supportSlot={
          <BorderGlow>
            <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
              {chapter.support?.label}
            </div>
            <div className="mt-1 font-display text-lg font-semibold">
              {chapter.support?.title}
            </div>
            <p className="mt-2 text-sm text-[var(--pres-muted)]">
              {chapter.support?.copy}
            </p>
            <div className="mt-4">
              <Stepper steps={steps} active={isActive} />
            </div>
          </BorderGlow>
        }
      />
    );
  }

  if (id === "booking_web" || id === "payment_uniapp") {
    const items = chapter.support?.items ?? [];
    const grid = (
      <div className="grid gap-3">
        {items.map((item) => (
          <TiltedCard key={item.title}>
            <div className="text-[0.7rem] uppercase tracking-wider text-[var(--pres-muted)]">
              {item.label}
            </div>
            <strong className="mt-1 block">{item.title}</strong>
            <p className="mt-2 text-sm text-[var(--pres-muted)]">{item.body}</p>
          </TiltedCard>
        ))}
      </div>
    );
    return (
      <SceneShell
        chapter={chapter}
        isActive={isActive}
        supportSlot={
          <div>
            <div className="mb-3 text-sm text-[var(--pres-muted)]">
              {chapter.support?.copy}
            </div>
            {id === "payment_uniapp" ? <BorderGlow>{grid}</BorderGlow> : grid}
          </div>
        }
      />
    );
  }

  if (id === "closing") {
    return (
      <div className="rounded-[28px] border border-[var(--pres-line)] bg-[var(--pres-panel)] p-10 shadow-[var(--pres-shadow)] backdrop-blur-xl">
        <div className="text-[0.74rem] uppercase tracking-wider text-[var(--pres-muted)]">
          {chapter.step}
        </div>
        <h1 className="mt-3 font-display text-4xl font-bold">
          <ShinyText text={chapter.title ?? ""} />
        </h1>
        <p className="mt-4 text-xl text-[var(--pres-muted)]">{chapter.claim}</p>
        <div className="mt-6 space-y-3 text-[var(--pres-muted)]">
          {(chapter.body ?? []).map((p) => (
            <p key={p}>{p}</p>
          ))}
        </div>
        <div className="mt-8 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={onGoOpening}
            className="cursor-pointer rounded-full bg-[var(--pres-accent)] px-5 py-2.5 text-sm font-medium text-white transition-colors hover:opacity-90"
          >
            返回开场
          </button>
        </div>
      </div>
    );
  }

  return (
    <SceneShell
      chapter={chapter}
      isActive={isActive}
      supportSlot={<SupportBlock chapter={chapter} isActive={isActive} />}
    />
  );
}
