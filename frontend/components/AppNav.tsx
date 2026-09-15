"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { clearSession, isLoggedIn, isAdmin } from "@/lib/auth";
import { BRAND } from "@/lib/brand";

export function AppNav() {
  const pathname = usePathname();
  const router = useRouter();
  const [authed, setAuthed] = useState(false);
  const [admin, setAdmin] = useState(false);

  useEffect(() => {
    setAuthed(isLoggedIn());
    setAdmin(isAdmin());
  }, [pathname]);

  if (pathname === "/login") {
    return null;
  }

  function logout() {
    clearSession();
    setAuthed(false);
    router.replace("/login");
  }

  const onCatalog = pathname.startsWith("/search") || pathname.startsWith("/books");

  return (
    <header className="sticky top-0 z-20 border-b border-[var(--line)] bg-[color-mix(in_srgb,var(--paper)_88%,white)] backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3.5 sm:px-6">
        <Link href={authed ? "/search" : "/login"} className="group flex min-w-0 items-baseline gap-2">
          <span className="font-display text-[1.35rem] leading-none tracking-tight">{BRAND.library}</span>
          <span className="hidden truncate text-[0.68rem] uppercase tracking-[0.18em] text-[var(--muted)] sm:inline">
            {BRAND.university}
          </span>
        </Link>
        {authed ? (
          <nav className="flex items-center gap-1 text-[0.92rem] sm:gap-2">
            <Link
              href="/search"
              className={`rounded-full px-3 py-1.5 ${onCatalog ? "bg-[var(--ink)] text-[var(--paper)]" : "text-[var(--muted)] hover:text-[var(--ink)]"}`}
            >
              Catalog
            </Link>
            <Link
              href="/loans"
              className={`rounded-full px-3 py-1.5 ${pathname.startsWith("/loans") ? "bg-[var(--ink)] text-[var(--paper)]" : "text-[var(--muted)] hover:text-[var(--ink)]"}`}
            >
              My loans
            </Link>
            {admin && (
              <Link
                href="/admin"
                className={`rounded-full px-3 py-1.5 ${pathname.startsWith("/admin") ? "bg-blue-600 text-white" : "text-[var(--muted)] hover:text-[var(--ink)]"}`}
              >
                Admin
              </Link>
            )}
            <button
              type="button"
              onClick={logout}
              className="ml-1 px-2 py-1.5 text-[var(--muted)] hover:text-[var(--ink)]"
            >
              Sign out
            </button>
          </nav>
        ) : null}
      </div>
    </header>
  );
}
