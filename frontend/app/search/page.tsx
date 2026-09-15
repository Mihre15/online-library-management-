"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { AuthGuard } from "@/components/AuthGuard";
import { BookCover } from "@/components/BookCover";
import { ErrorBanner } from "@/components/ErrorBanner";
import { api } from "@/lib/api";
import { copiesOf } from "@/lib/books";
import type { Book } from "@/lib/types";

function Catalog() {
  const [query, setQuery] = useState("");
  const [books, setBooks] = useState<Book[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);
  const [shelf, setShelf] = useState("All");

  async function load(q: string) {
    setError(null);
    const params = new URLSearchParams();
    if (q.trim()) params.set("query", q.trim());
    const suffix = params.toString() ? `?${params}` : "";
    const results = await api<Book[]>(`/api/books${suffix}`);
    setBooks(results);
    setSearched(true);
    setShelf("All");
  }

  useEffect(() => {
    load("").catch((err) =>
      setError(err instanceof Error ? err.message : "The catalog didn’t load. Try again in a moment."),
    );
  }, []);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const q = String(new FormData(event.currentTarget).get("query") ?? query);
    setQuery(q);
    try {
      await load(q);
    } catch (err) {
      setBooks([]);
      setError(err instanceof Error ? err.message : "Search failed");
    }
  }

  const shelves = useMemo(() => {
    const names = new Set((books ?? []).map((b) => b.category).filter(Boolean) as string[]);
    return ["All", ...[...names].sort()];
  }, [books]);

  const visible = (books ?? []).filter((book) => shelf === "All" || book.category === shelf);
  const empty = searched && books !== null && books.length === 0;
  const browsing = !query.trim();

  return (
    <div className="mx-auto max-w-6xl px-4 pb-20 pt-8 sm:px-6">
      <section className="mb-10 max-w-2xl">
        <p className="text-[0.7rem] uppercase tracking-[0.24em] text-[var(--muted)]">This week on the floor</p>
        <h1 className="font-display mt-2 text-[2.35rem] leading-tight tracking-tight sm:text-5xl">
          Currently Available Books
        </h1>
        <p className="mt-4 text-[1.02rem] leading-relaxed text-[var(--muted)]">
          Walk the stacks the way you’d wander a bookshop: covers first, then a title, an author,
          or an ISBN if you already know what you want.
        </p>
      </section>

      <ErrorBanner testId="search-error" message={error} />

      <form onSubmit={onSubmit} className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-stretch">
        <input
          name="query"
          data-testid="search-input"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search titles, authors, ISBN…"
          className="field flex-1 !rounded-full !px-5"
        />
        <button
          type="submit"
          className="rounded-full bg-[var(--ink)] px-6 py-2.5 text-[0.92rem] text-[var(--paper)] hover:bg-black"
        >
          Search
        </button>
      </form>

      {books && books.length > 0 && shelves.length > 2 ? (
        <div className="mb-8 flex flex-wrap gap-2">
          {shelves.map((name) => (
            <button
              key={name}
              type="button"
              onClick={() => setShelf(name)}
              className={`rounded-full px-3.5 py-1.5 text-sm ${shelf === name
                  ? "bg-[var(--ink)] text-[var(--paper)]"
                  : "bg-[var(--card)] text-[var(--muted)] ring-1 ring-[var(--line)] hover:text-[var(--ink)]"
                }`}
            >
              {name}
            </button>
          ))}
        </div>
      ) : null}

      {books && books.length > 0 ? (
        <p className="mb-5 text-sm text-[var(--muted)]">
          {browsing
            ? `${visible.length} title${visible.length === 1 ? "" : "s"} on the floor`
            : `${visible.length} match${visible.length === 1 ? "" : "es"} for “${query.trim()}”`}
        </p>
      ) : null}

      {empty ? (
        <div
          data-testid="search-empty"
          className="rounded-sm border border-dashed border-[var(--line)] bg-[var(--card)] px-6 py-16 text-center"
        >
          <p className="font-display text-2xl">Not on these shelves</p>
          <p className="mx-auto mt-3 max-w-md text-sm leading-relaxed text-[var(--muted)]">
            Nothing matched that spelling. Try the author’s surname, a shorter title, or the ISBN
            from the back of the book.
          </p>
        </div>
      ) : null}

      {visible.length > 0 ? (
        <ul className="grid grid-cols-2 gap-x-4 gap-y-10 sm:grid-cols-3 sm:gap-x-6 lg:grid-cols-4">
          {visible.map((book) => {
            const copies = copiesOf(book);
            const available = copies > 0;
            return (
              <li key={book.id} data-testid={`book-row-${book.id}`} className="flex flex-col">
                <Link href={`/books/${book.id}`} className="group">
                  <div className="transition duration-300 group-hover:-translate-y-1 group-hover:shadow-xl">
                    <BookCover title={book.title} author={book.author} isbn={book.isbn} />
                  </div>
                  <h2 className="font-display mt-3 text-[1.05rem] leading-snug tracking-tight group-hover:underline decoration-[var(--line)] underline-offset-4">
                    {book.title}
                  </h2>
                  <p className="mt-1 text-sm text-[var(--muted)]">{book.author}</p>
                </Link>
                <div className="mt-3 flex flex-wrap items-center gap-2 text-sm">
                  <span
                    data-testid={`availability-badge-${book.id}`}
                    className={`rounded-full px-2.5 py-0.5 text-[0.78rem] ${available ? "bg-[var(--ok-bg)] text-[var(--forest)]" : "bg-[var(--paper-deep)] text-[var(--muted)]"
                      }`}
                  >
                    {available ? `Available (${copies})` : "Unavailable"}
                  </span>
                  {available ? (
                    <Link href={`/books/${book.id}`} className="font-medium text-[var(--stamp)] hover:underline">
                      Borrow
                    </Link>
                  ) : (
                    <span className="cursor-not-allowed text-[var(--muted)]" aria-disabled="true">
                      Borrow
                    </span>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      ) : null}
    </div>
  );
}

export default function SearchPage() {
  return (
    <AuthGuard>
      <Catalog />
    </AuthGuard>
  );
}
