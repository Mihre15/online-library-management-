"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";
import { api } from "@/lib/api";
import { isLoggedIn, saveSession } from "@/lib/auth";
import { ApiError, type LoginResponse } from "@/lib/types";

export default function RegisterPage() {
  const router = useRouter();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isLoggedIn()) {
      router.replace("/search");
    }
  }, [router]);

  const passwordsMatch = password === confirmPassword && password.length > 0;
  const canSubmit =
    fullName.trim().length > 0 &&
    email.trim().length > 0 &&
    passwordsMatch &&
    !loading;

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!canSubmit) return;

    if (!passwordsMatch) {
      setError("Passwords do not match.");
      return;
    }

    setError(null);
    setLoading(true);

    try {
      // Call registration endpoint
      const result = await api<LoginResponse>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          fullName: fullName.trim(),
          email: email.trim(),
          password,
        }),
      });

      // Save session and redirect
      saveSession(result.token, result.studentId);
      router.replace("/search");
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError("This email is already registered. Please sign in instead.");
      } else if (err instanceof ApiError && err.status === 400) {
        setError(
          "Please check your information and try again."
        );
      } else {
        setError(
          err instanceof Error
            ? err.message
            : "Registration failed. Please try again."
        );
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
              Join our community, borrow from the teaching collection, and bring
              titles back so the next reader isn't waiting in the doorway.
            </p>
          </div>
        </div>
      </aside>

      <main className="flex flex-col justify-center px-8 py-14 sm:px-12 lg:px-14 xl:px-20">
        <p className="text-[0.7rem] uppercase tracking-[0.26em] text-[var(--muted)]">
          The Stacks
        </p>
        <h1 className="font-display mt-3 text-4xl tracking-tight">Create an account</h1>
        <p className="mt-3 max-w-md text-[0.95rem] leading-relaxed text-[var(--muted)]">
          Join us to borrow books from our collection and manage your loans.
        </p>

        <form onSubmit={onSubmit} className="mt-8 max-w-md space-y-4">
          <ErrorBanner testId="register-error" message={error} />

          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Full name</span>
            <input
              data-testid="register-fullname"
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="field"
              placeholder="Your full name"
            />
          </label>

          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Campus email</span>
            <input
              data-testid="register-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="field"
              placeholder="your.email@example.com"
            />
          </label>

          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Password</span>
            <input
              data-testid="register-password"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="field"
              placeholder="••••••••"
            />
          </label>

          <label className="block text-sm">
            <span className="mb-1.5 block text-[var(--muted)]">Confirm password</span>
            <input
              data-testid="register-confirm-password"
              type="password"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="field"
              placeholder="••••••••"
            />
            {confirmPassword && !passwordsMatch && (
              <p className="mt-1 text-xs text-red-500">Passwords do not match</p>
            )}
          </label>

          <LoadingButton
            data-testid="register-submit"
            type="submit"
            loading={loading}
            disabled={!canSubmit}
            className="mt-2 w-full"
          >
            Create account
          </LoadingButton>

          <p className="text-center text-sm text-[var(--muted)]">
            Already have an account?{" "}
            <Link href="/login" className="font-medium hover:underline">
              Sign in
            </Link>
          </p>
        </form>
      </main>
    </div>
  );
}
