import { useEffect, useRef, useState } from "react";
import { useReducedMotion } from "@/hooks/useReducedMotion";

type CountUpProps = {
  value: string;
  className?: string;
  active?: boolean;
};

function parseNumeric(value: string) {
  const match = value.match(/^(\d+(?:\.\d+)?)(.*)$/);
  if (!match) return null;
  return { num: parseFloat(match[1]), suffix: match[2] };
}

/** Inspired by react-bits CountUp — animates leading number in metric values */
export function CountUp({ value, className, active = true }: CountUpProps) {
  const reduced = useReducedMotion();
  const parsed = parseNumeric(value.trim());
  const [display, setDisplay] = useState(value);
  const raf = useRef(0);

  useEffect(() => {
    if (!active || !parsed || reduced) {
      setDisplay(value);
      return;
    }
    const start = performance.now();
    const duration = 900;
    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      const current = parsed.num * eased;
      const formatted =
        parsed.num % 1 === 0
          ? String(Math.round(current))
          : current.toFixed(1);
      setDisplay(`${formatted}${parsed.suffix}`);
      if (t < 1) raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current);
  }, [value, active, reduced, parsed]);

  return <span className={className}>{display}</span>;
}
