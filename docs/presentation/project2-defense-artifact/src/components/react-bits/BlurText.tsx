import { useEffect, useRef } from "react";
import gsap from "gsap";
import { useReducedMotion } from "@/hooks/useReducedMotion";
import { cn } from "@/lib/utils";

type BlurTextProps = {
  text: string;
  className?: string;
  delay?: number;
  active?: boolean;
};

/** Inspired by react-bits BlurText (TS-TW) */
export function BlurText({
  text,
  className,
  delay = 0,
  active = true,
}: BlurTextProps) {
  const ref = useRef<HTMLSpanElement>(null);
  const reduced = useReducedMotion();

  useEffect(() => {
    const el = ref.current;
    if (!el || !active) return;
    if (reduced) {
      el.style.opacity = "1";
      el.style.filter = "none";
      return;
    }
    gsap.fromTo(
      el,
      { opacity: 0, filter: "blur(12px)", y: 16 },
      { opacity: 1, filter: "blur(0px)", y: 0, duration: 0.7, delay, ease: "power2.out" },
    );
  }, [text, delay, active, reduced]);

  return (
    <span ref={ref} className={cn("inline-block", className)}>
      {text}
    </span>
  );
}
