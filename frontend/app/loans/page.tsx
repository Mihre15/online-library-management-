"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { AuthGuard } from "@/components/AuthGuard";
import { BookCover } from "@/components/BookCover";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";
import { api } from "@/lib/api";
import { getStudentId } from "@/lib/auth";
import { canReturn, dueCopy, formatLoanStatus } from "@/lib/books";
import type { ReturnResponse, StudentLoan } from "@/lib/types";

function fineValue(loan: StudentLoan): number {
  return Number(loan.fineCharged ?? 0);
}

function Loans() {
  const [loans, setLoans] = useState<StudentLoan[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<number | null>(null);

  async function load() {
    const studentId = getStudentId();
    if (studentId == null) return;
    const rows = await api<StudentLoan[]>(`/api/students/${studentId}/loans`);
    setLoans(rows);
  }

  useEffect(() => {
    load().catch((err) =>
      setError(err instanceof Error ? err.message : "Your loans didn’t load. Give it another try."),
    );
  }, []);

  async function returnLoan(loanId: number) {
    setPendingId(loanId);
    setError(null);
    setNotice(null);
    try {
      const result = await api<ReturnResponse>(`/api/loans/${loanId}/return`, { method: "POST" });
      setLoans((current) =>
        (current ?? []).map((loan) =>
          loan.loanId === loanId
            ? { ...loan, status: result.status, fineCharged: result.fineCharged }
            : loan,
        ),
      );
      setNotice(result.message);
    } catch (err) {
      setError(err instanceof Error ? err.message : "That return didn’t go through.");
    } finally {
      setPendingId(null);
    }
  }

  const openCount = useMemo(
    () => (loans ?? []).filter((loan) => canReturn(loan.status)).length,
    [loans],
  );

  return (
    <div className="mx-auto max-w-4xl px-4 pb-20 pt-8 sm:px-6">
      <p className="text-[0.7rem] uppercase tracking-[0.24em] text-[var(--muted)]">Your card</p>
      <h1 className="font-display mt-2 text-4xl tracking-tight">My loans</h1>
      <p className="mt-3 max-w-xl text-[1.02rem] leading-relaxed text-[var(--muted)]">
        {loans == null
          ? "Fetching what’s out in your name."
          : openCount === 0
            ? "Nothing is out right now. The catalog is waiting when you are."
            : `You have ${openCount} title${openCount === 1 ? "" : "s"} out. Bring them back on time so the next reader isn’t standing at an empty hook.`}
      </p>

      <ErrorBanner testId="loans-error" message={error} />
      {notice ? (
        <p className="mt-4 rounded-sm border border-[var(--line)] bg-[var(--card)] px-3.5 py-3 text-sm text-[var(--muted)]">
          {notice}
        </p>
      ) : null}

      {loans && loans.length === 0 ? (
        <div className="mt-10 rounded-sm border border-dashed border-[var(--line)] bg-[var(--card)] px-6 py-14 text-center">
          <p className="font-display text-2xl">A quiet card</p>
          <p className="mt-2 text-sm text-[var(--muted)]">
            When you borrow, titles land here with due dates and a way to return them.
          </p>
          <Link href="/search" className="mt-5 inline-block text-[var(--stamp)] underline underline-offset-4">
            Browse the shelves
          </Link>
        </div>
      ) : null}

      {loans && loans.length > 0 ? (
        <ul className="mt-10 divide-y divide-[var(--line)] border-y border-[var(--line)]">
          {loans.map((loan) => {
            const statusLabel = formatLoanStatus(loan.status);
            const showFine = !canReturn(loan.status) && fineValue(loan) > 0;
            const overdue = statusLabel === "Overdue";
            const returnPending = loan.status.toUpperCase() === "RETURN_PENDING";
            return (
              <li
                key={loan.loanId}
                data-testid={`loan-row-${loan.loanId}`}
                className="flex gap-4 py-5 sm:gap-5"
              >
                <Link href={`/books/${loan.book.id}`} className="shrink-0">
                  <BookCover
                    title={loan.book.title}
                    author={loan.book.author}
                    isbn={loan.book.isbn}
                    size="thumb"
                  />
                </Link>
                <div className="flex min-w-0 flex-1 flex-col justify-center gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div className="min-w-0">
                    <Link href={`/books/${loan.book.id}`} className="font-display text-xl leading-snug hover:underline">
                      {loan.book.title}
                    </Link>
                    <p className="mt-0.5 text-sm text-[var(--muted)]">{loan.book.author}</p>
                    <p className="mt-1 text-sm text-[var(--muted)]">{dueCopy(loan.dueDate, loan.status)}</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2 sm:justify-end">
                    <span
                      data-testid={`loan-status-${loan.loanId}`}
                      className={`rounded-full px-2.5 py-0.5 text-[0.78rem] ${
                        overdue
                          ? "bg-[var(--warn-bg)] text-[#7a3b12]"
                          : returnPending
                            ? "bg-[var(--warn-bg)] text-[#7a3b12]"
                          : statusLabel === "Returned"
                            ? "bg-[var(--paper-deep)] text-[var(--muted)]"
                            : "bg-[var(--ok-bg)] text-[var(--forest)]"
                      }`}
                    >
                      {statusLabel}
                    </span>
                    {showFine ? (
                      <span data-testid={`fine-amount-${loan.loanId}`} className="text-sm">
                        Fine: {fineValue(loan).toFixed(2)} ETB
                      </span>
                    ) : null}
                    {canReturn(loan.status) ? (
                      <LoadingButton
                        data-testid={`return-button-${loan.loanId}`}
                        type="button"
                        variant="quiet"
                        loading={pendingId === loan.loanId}
                        onClick={() => returnLoan(loan.loanId)}
                        className="!px-4 !py-1.5"
                      >
                        Return
                      </LoadingButton>
                    ) : null}
                    {returnPending ? (
                      <span className="text-sm text-[var(--muted)]">
                        Bring this book to the library desk for confirmation.
                      </span>
                    ) : null}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      ) : null}
    </div>
  );
}

export default function LoansPage() {
  return (
    <AuthGuard>
      <Loans />
    </AuthGuard>
  );
}
