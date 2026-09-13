"use client";

type Props = {
  testId: string;
  message: string | null;
};

export function ErrorBanner({ testId, message }: Props) {
  if (!message) return null;
  return (
    <div
      data-testid={testId}
      role="alert"
      className="rounded-sm border border-[color-mix(in_srgb,var(--danger)_25%,var(--line))] bg-[var(--danger-bg)] px-3.5 py-2.5 text-sm text-[var(--danger)]"
    >
      {message}
    </div>
  );
}
