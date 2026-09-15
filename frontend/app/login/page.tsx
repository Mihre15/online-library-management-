"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";
import { api } from "@/lib/api";
import { isLoggedIn, saveSession } from "@/lib/auth";
import { ApiError, type LoginResponse } from "@/lib/types";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isLoggedIn()) {
      router.replace("/search");
    }
  }, [router]);

  const canSubmit = email.trim().length > 0 && password.length > 0 && !loading;

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!canSubmit) return;
    setError(null);
    setLoading(true);
    try {
      const result = await api<LoginResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email: email.trim(), password }),
      });
      saveSession(result.token, result.studentId);
      router.replace("/search");
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setError("That email and password didn’t match our records.");
      } else {
        setError(err instanceof Error ? err.message : "The desk couldn’t sign you in just now.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-[minmax(0,1.15fr)_minmax(26rem,1fr)]">
      <aside className="relative hidden min-h-screen overflow-hidden lg:block">
        <div className="spine-wall absolute inset-0" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/25 to-black/20" />
        <div className="relative flex h-full flex-col justify-between p-10 text-[#f3eee6]">
          <p className="text-[0.7rem] uppercase tracking-[0.28em]">CTBE collection</p>
          <div>
            <p className="font-display text-4xl leading-[1.15] xl:text-5xl">
              The Digital Library
              <br />
              for the Faculty of CTBE
            </p>
            <p className="mt-5 max-w-sm text-sm leading-relaxed text-white/75">
              Borrow from the teaching collection, keep an eye on due dates, and bring titles
              back so the next reader isn’t waiting in the doorway.
            </p>
          </div>
        </div>
      </aside>

      <main className="flex flex-col justify-center px-8 py-14 sm:px-12 lg:px-14 xl:px-20">
        <p className="text-[0.7rem] uppercase tracking-[0.26em] text-[var(--muted)]">The Stacks</p>
        <h1 className="font-display mt-3 text-4xl tracking-tight">Sign in</h1>
        <p className="mt-3 max-w-md text-[0.95rem] leading-relaxed text-[var(--muted)]">
          Use the email on your student record. We’ll take you straight to the catalog.
        </p>

        <form onSubmit={onSubmit} className="mt-8 max-w-md space-y-4">
          <ErrorBanner testId="login-error" message={error} />
          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Campus email</span>
            <input
              data-testid="login-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="field"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Password</span>
            <input
              data-testid="login-password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="field"
            />
          </label>
          <LoadingButton
            data-testid="login-submit"
            type="submit"
            loading={loading}
            disabled={!canSubmit}
            className="mt-2 w-full"
          >
            Enter the stacks
          </LoadingButton>

          <p className="text-center text-sm text-[var(--muted)]">
            Don't have an account?{" "}
            <Link href="/register" className="font-medium hover:underline">
              Create one
            </Link>
          </p>
        </form>
      </main>
    </div>
  );
}
