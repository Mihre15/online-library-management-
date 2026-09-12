import { BRAND } from "@/lib/brand";

export function SiteFooter() {
  return (
    <footer className="mt-16 border-t border-[var(--line)] py-8 text-center text-[0.72rem] leading-relaxed text-[var(--muted)]">
      <p>{BRAND.university}</p>
      <p className="mt-1">
        {BRAND.college}
        <span className="mx-1.5">·</span>
        Library
      </p>
    </footer>
  );
}
