"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { AuthGuard } from "@/components/AuthGuard";
import { BookCover } from "@/components/BookCover";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";
import { api } from "@/lib/api";
import { copiesOf, denialHint } from "@/lib/books";
import type { Book, BorrowResponse } from "@/lib/types";

function BookDetail() {
  const params = useParams<{ id: string }>();
  const [book, setBook] = useState<Book | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [denied, setDenied] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setError(null);
    api<Book>(`/api/books/${params.id}`)
      .then(setBook)
      .catch((err) => setError(err instanceof Error ? err.message : "We couldn’t find that title."));
  }, [params.id]);

  async function borrow() {
    if (!book) return;
    setLoading(true);
    setError(null);
    setSuccess(null);
    setDenied(null);
    try {
      const result = await api<BorrowResponse>("/api/loans/borrow", {
        method: "POST",
        body: JSON.stringify({ bookId: book.id }),
      });
      if (result.success) {
        setSuccess("It’s checked out in your name. Fourteen days from today — you’ll see the date under My loans.");
        setBook({ ...book, copiesAvailable: Math.max(0, copiesOf(book) - 1) });
      } else {
        setDenied(result.denialReason || "No copies available");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "The desk couldn’t complete that borrow.");
    } finally {
      setLoading(false);
    }
  }

  if (!book && !error) {
    return <p className="px-4 py-24 text-center text-sm text-[var(--muted)]">Pulling the volume…</p>;
  }

  const copies = book ? copiesOf(book) : 0;
  const canBorrow = Boolean(book && copies > 0 && !loading);

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      <Link href="/search" className="text-sm text-[var(--muted)] hover:text-[var(--ink)]">
        ← Back to the shelves
      </Link>

      {book ? (
        <div className="mt-8 grid items-start gap-10 md:grid-cols-[minmax(200px,280px)_1fr]">
          <BookCover title={book.title} author={book.author} isbn={book.isbn} size="hero" />
          <div>
            {book.category ? (
              <p className="text-[0.7rem] uppercase tracking-[0.22em] text-[var(--muted)]">{book.category}</p>
            ) : null}
            <h1 className="font-display mt-2 text-4xl leading-tight tracking-tight sm:text-5xl">{book.title}</h1>
            <p className="mt-3 text-lg text-[var(--muted)]">{book.author}</p>
            <p className="mt-2 text-sm text-[var(--muted)]">ISBN {book.isbn}</p>
            <p className="mt-6 max-w-lg text-[1.02rem] leading-relaxed text-[var(--ink)]">
              {copies > 0
                ? `${copies} cop${copies === 1 ? "y is" : "ies are"} on the shelf. Take one for two weeks — bring it back so the next reader isn’t waiting.`
                : "Every copy is out right now. You can still read the record; borrowing opens again when a copy comes home."}
            </p>

            <div className="mt-8 max-w-lg space-y-3">
              <ErrorBanner testId="borrow-error" message={error} />
              {success ? (
                <p
                  data-testid="borrow-success"
                  className="rounded-sm border border-[color-mix(in_srgb,var(--forest)_25%,var(--line))] bg-[var(--ok-bg)] px-3.5 py-3 text-sm leading-relaxed text-[var(--forest)]"
                >
                  {success}
                </p>
              ) : null}
              {denied ? (
                <div className="rounded-sm border border-[color-mix(in_srgb,var(--danger)_25%,var(--line))] bg-[var(--danger-bg)] px-3.5 py-3 text-sm">
                  <p data-testid="borrow-denied" className="font-medium text-[var(--danger)]">
                    {denied}
                  </p>
                  {denialHint(denied) ? (
                    <p className="mt-1.5 leading-relaxed text-[var(--muted)]">{denialHint(denied)}</p>
                  ) : null}
                </div>
              ) : null}
              <LoadingButton
                data-testid="borrow-button"
                type="button"
                onClick={borrow}
                loading={loading}
                disabled={!canBorrow}
              >
                {copies > 0 ? "Take this copy" : "No copies on the shelf"}
              </LoadingButton>
              {success ? (
                <p className="text-sm">
                  <Link href="/loans" className="text-[var(--stamp)] underline underline-offset-4">
                    See it on My loans
                  </Link>
                </p>
              ) : null}
            </div>
          </div>
        </div>
      ) : (
        <div className="mt-8">
          <ErrorBanner testId="borrow-error" message={error} />
        </div>
      )}
    </div>
  );
}

export default function BookPage() {
  return (
    <AuthGuard>
      <BookDetail />
    </AuthGuard>
  );
}
