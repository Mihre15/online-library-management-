"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { ErrorBanner } from "@/components/ErrorBanner";
import type { AdminLoan, Book, Member } from "@/lib/types";

const OPEN_LOAN_STATUSES = new Set(["ACTIVE", "OVERDUE", "RETURN_PENDING"]);

type Stats = {
  totalBooks: number;
  activeMembers: number;
  activeLoans: number;
  overdueItems: number;
};

export default function AdminDashboard() {
  const router = useRouter();
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
      return;
    }
    loadStats();
  }, [router]);

  async function loadStats() {
    try {
      const [books, members, loans] = await Promise.all([
        api<Book[]>("/api/books"),
        api<Member[]>("/api/members"),
        api<AdminLoan[]>("/api/loans"),
      ]);
      setStats({
        totalBooks: books.length,
        activeMembers: members.filter((m) => m.status === "ACTIVE").length,
        activeLoans: loans.filter((l) => OPEN_LOAN_STATUSES.has(l.status)).length,
        overdueItems: loans.filter((l) => l.status === "OVERDUE").length,
      });
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load dashboard stats");
    }
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <h1 className="text-2xl font-bold">Library Admin Dashboard</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <ErrorBanner testId="admin-dashboard-error" message={error} />

        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {/* Books Management */}
          <Link href="/admin/books" data-testid="admin-books-card" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">Manage Books</h2>
              <p className="text-[var(--muted)] text-sm">
                Add, edit, or delete books from the library catalog.
              </p>
            </div>
          </Link>

          {/* Members Management */}
          <Link href="/admin/members" data-testid="admin-members-card" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">Manage Members</h2>
              <p className="text-[var(--muted)] text-sm">
                View members, suspend accounts, and manage fines.
              </p>
            </div>
          </Link>

          {/* Loans Management */}
          <Link href="/admin/loans" data-testid="admin-loans-card" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">Manage Loans</h2>
              <p className="text-[var(--muted)] text-sm">
                Track loans, process returns, and manage overdue items.
              </p>
            </div>
          </Link>
        </div>

        {/* Quick Stats */}
        <div className="mt-12 grid gap-6 md:grid-cols-4">
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Total Books</p>
            <p data-testid="stat-total-books" className="text-3xl font-bold mt-2">
              {stats ? stats.totalBooks : "—"}
            </p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Active Members</p>
            <p data-testid="stat-active-members" className="text-3xl font-bold mt-2">
              {stats ? stats.activeMembers : "—"}
            </p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Active Loans</p>
            <p data-testid="stat-active-loans" className="text-3xl font-bold mt-2">
              {stats ? stats.activeLoans : "—"}
            </p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Overdue Items</p>
            <p data-testid="stat-overdue-items" className="text-3xl font-bold mt-2">
              {stats ? stats.overdueItems : "—"}
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
