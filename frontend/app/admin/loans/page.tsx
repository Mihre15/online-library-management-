"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { type AdminLoan } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function LoansManagement() {
  const router = useRouter();
  const [loans, setLoans] = useState<AdminLoan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<number | null>(null);
  const [filter, setFilter] = useState<string>("ALL");

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
      return;
    }
    loadLoans();
  }, [router]);

  async function loadLoans() {
    try {
      setLoading(true);
      const data = await api<AdminLoan[]>("/api/loans");
      setLoans(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load loans");
    } finally {
      setLoading(false);
    }
  }

  async function confirmReturn(loanId: number) {
    setPendingId(loanId);
    setError(null);
    try {
      await api(`/api/loans/${loanId}/confirm-return`, { method: "POST" });
      await loadLoans();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to confirm return");
    } finally {
      setPendingId(null);
    }
  }

  async function markOverdue(loanId: number) {
    setPendingId(loanId);
    setError(null);
    try {
      await api(`/api/loans/${loanId}/mark-overdue`, { method: "PUT" });
      await loadLoans();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to mark overdue");
    } finally {
      setPendingId(null);
    }
  }

  async function reportLost(loanId: number) {
    if (!confirm("Report this book as lost?")) return;
    setPendingId(loanId);
    setError(null);
    try {
      await api(`/api/loans/${loanId}/report-lost`, { method: "PUT" });
      await loadLoans();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to report lost");
    } finally {
      setPendingId(null);
    }
  }

  const filteredLoans = filter === "ALL" ? loans : loans.filter((l) => l.status === filter);
  const statusOptions = ["ALL", "ACTIVE", "OVERDUE", "RETURN_PENDING", "RETURNED", "LOST"];

  function statusBadgeClass(status: string) {
    switch (status) {
      case "ACTIVE": return "bg-green-100 text-green-700";
      case "OVERDUE": return "bg-red-100 text-red-700";
      case "RETURN_PENDING": return "bg-amber-100 text-amber-700";
      case "RETURNED": return "bg-gray-100 text-gray-700";
      case "LOST": return "bg-purple-100 text-purple-700";
      default: return "bg-gray-100 text-gray-700";
    }
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <Link href="/admin" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
            ← Back to Admin
          </Link>
          <h1 className="text-2xl font-bold">Loan Management</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <ErrorBanner testId="admin-loans-error" message={error} />

        <div className="mb-6 flex gap-2 flex-wrap">
          {statusOptions.map((status) => (
            <button
              key={status}
              onClick={() => setFilter(status)}
              data-testid={`loan-filter-${status}`}
              className={`text-xs px-3 py-1.5 rounded-full border ${
                filter === status
                  ? "bg-black text-white border-black"
                  : "border-[var(--border)] text-[var(--muted)] hover:border-black"
              }`}
            >
              {status}
            </button>
          ))}
        </div>

        {loading ? (
          <p className="text-[var(--muted)]">Loading loans...</p>
        ) : filteredLoans.length === 0 ? (
          <p className="text-[var(--muted)]">No loans found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-[var(--border)]">
                <tr>
                  <th className="text-left py-3 px-4">Member</th>
                  <th className="text-left py-3 px-4">Book</th>
                  <th className="text-left py-3 px-4">Borrowed</th>
                  <th className="text-left py-3 px-4">Due</th>
                  <th className="text-left py-3 px-4">Status</th>
                  <th className="text-right py-3 px-4">Fine</th>
                  <th className="text-right py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredLoans.map((loan) => (
                  <tr key={loan.loanId} data-testid={`admin-loan-row-${loan.loanId}`} className="border-b border-[var(--border)] hover:bg-[var(--hover)]">
                    <td className="py-3 px-4">
                      <div className="font-medium">{loan.member.fullName}</div>
                      <div className="text-xs text-[var(--muted)]">{loan.member.email}</div>
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-medium">{loan.book.title}</div>
                      <div className="text-xs text-[var(--muted)]">{loan.book.author}</div>
                    </td>
                    <td className="py-3 px-4 text-[var(--muted)]">{loan.borrowDate}</td>
                    <td className="py-3 px-4 text-[var(--muted)]">{loan.dueDate}</td>
                    <td className="py-3 px-4">
                      <span data-testid={`admin-loan-status-${loan.loanId}`} className={`text-xs px-2 py-0.5 rounded-full ${statusBadgeClass(loan.status)}`}>
                        {loan.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      {loan.fineAmount > 0 ? (
                        <span className="text-red-600">{loan.fineAmount.toFixed(2)} ETB</span>
                      ) : (
                        <span className="text-[var(--muted)]">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-right space-x-2 whitespace-nowrap">
                      {loan.status === "RETURN_PENDING" && (
                        <button
                          onClick={() => confirmReturn(loan.loanId)}
                          disabled={pendingId === loan.loanId}
                          data-testid={`confirm-return-${loan.loanId}`}
                          className="text-xs text-green-600 hover:underline"
                        >
                          Confirm Return
                        </button>
                      )}
                      {loan.status === "ACTIVE" && (
                        <button
                          onClick={() => markOverdue(loan.loanId)}
                          disabled={pendingId === loan.loanId}
                          data-testid={`mark-overdue-${loan.loanId}`}
                          className="text-xs text-amber-600 hover:underline"
                        >
                          Mark Overdue
                        </button>
                      )}
                      {(loan.status === "ACTIVE" || loan.status === "OVERDUE") && (
                        <button
                          onClick={() => reportLost(loan.loanId)}
                          disabled={pendingId === loan.loanId}
                          data-testid={`report-lost-${loan.loanId}`}
                          className="text-xs text-purple-600 hover:underline"
                        >
                          Report Lost
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
