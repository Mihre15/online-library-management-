"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  loading?: boolean;
  children: ReactNode;
  variant?: "primary" | "quiet";
};

export function LoadingButton({
  loading,
  children,
  disabled,
  className = "",
  variant = "primary",
  ...rest
}: Props) {
  const isDisabled = Boolean(disabled || loading);
  const tone = isDisabled
    ? "cursor-not-allowed bg-[var(--paper-deep)] text-[var(--muted)]"
    : variant === "quiet"
      ? "border border-[var(--ink)] bg-transparent text-[var(--ink)] hover:bg-[var(--ink)] hover:text-[var(--paper)]"
      : "bg-[var(--stamp)] text-[var(--paper)] hover:bg-[var(--stamp-hover)]";

  return (
    <button
      {...rest}
      disabled={isDisabled}
      className={`inline-flex items-center justify-center gap-2 rounded-full px-5 py-2.5 text-[0.92rem] tracking-wide transition ${tone} ${className}`}
    >
      {loading ? (
        <span
          className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current/30 border-t-current"
          aria-hidden
        />
      ) : null}
      {children}
    </button>
  );
}
