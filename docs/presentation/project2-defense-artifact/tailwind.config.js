/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: ["class"],
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        presentation: {
          bg: "var(--pres-bg)",
          soft: "var(--pres-bg-soft)",
          text: "var(--pres-text)",
          muted: "var(--pres-muted)",
          panel: "var(--pres-panel)",
          accent: "var(--pres-accent)",
          accent2: "var(--pres-accent-2)",
          accent3: "var(--pres-accent-3)",
        },
      },
      fontFamily: {
        display: ["var(--font-display)", "Noto Sans SC", "sans-serif"],
        body: ["var(--font-body)", "PingFang SC", "sans-serif"],
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        shine: {
          "0%": { backgroundPosition: "200% center" },
          "100%": { backgroundPosition: "-200% center" },
        },
      },
      animation: {
        shine: "shine 4s linear infinite",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
};
