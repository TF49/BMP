import { Children, isValidElement, useEffect, useRef } from "react";
import gsap from "gsap";
import { useReducedMotion } from "@/hooks/useReducedMotion";
import { cn } from "@/lib/utils";

type AnimatedListProps = {
  children: React.ReactNode;
  className?: string;
  active?: boolean;
  stagger?: number;
};

/** Inspired by react-bits AnimatedList */
export function AnimatedList({
  children,
  className,
  active = true,
  stagger = 0.08,
}: AnimatedListProps) {
  const ref = useRef<HTMLDivElement>(null);
  const reduced = useReducedMotion();

  useEffect(() => {
    const root = ref.current;
    if (!root || !active) return;
    const items = root.querySelectorAll("[data-animated-item]");
    if (reduced) {
      items.forEach((el) => {
        (el as HTMLElement).style.opacity = "1";
        (el as HTMLElement).style.transform = "none";
      });
      return;
    }
    gsap.fromTo(
      items,
      { opacity: 0, y: 20 },
      {
        opacity: 1,
        y: 0,
        duration: 0.5,
        stagger,
        ease: "power2.out",
      },
    );
  }, [children, active, reduced, stagger]);

  return (
    <div ref={ref} className={cn(className)}>
      {Children.map(children, (child) =>
        isValidElement(child) ? (
          <div data-animated-item key={child.key ?? undefined}>
            {child}
          </div>
        ) : (
          child
        ),
      )}
    </div>
  );
}
