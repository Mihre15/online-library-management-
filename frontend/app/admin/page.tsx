"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";

export default function AdminDashboard() {
  const router = useRouter();

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <h1 className="text-2xl font-bold">Library Admin Dashboard</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {/* Books Management */}
          <Link href="/admin/books" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">📚 Manage Books</h2>
              <p className="text-[var(--muted)] text-sm">
                Add, edit, or delete books from the library catalog.
              </p>
            </div>
          </Link>

          {/* Members Management */}
          <Link href="/admin/members" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">👥 Manage Members</h2>
              <p className="text-[var(--muted)] text-sm">
                View members, suspend accounts, and manage fines.
              </p>
            </div>
          </Link>

          {/* Loans Management */}
          <Link href="/admin/loans" className="block">
            <div className="rounded-lg border border-[var(--border)] p-6 hover:border-blue-500 hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-2">🔄 Manage Loans</h2>
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
            <p className="text-3xl font-bold mt-2">—</p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Active Members</p>
            <p className="text-3xl font-bold mt-2">—</p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Active Loans</p>
            <p className="text-3xl font-bold mt-2">—</p>
          </div>
          <div className="rounded-lg border border-[var(--border)] p-6">
            <p className="text-[var(--muted)] text-sm font-medium">Overdue Items</p>
            <p className="text-3xl font-bold mt-2">—</p>
          </div>
        </div>
      </main>
    </div>
  );
}
