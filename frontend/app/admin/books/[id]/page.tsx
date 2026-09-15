"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { type Book } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";

export default function EditBookPage() {
  const router = useRouter();
  const params = useParams();
  const bookId = params.id as string;

  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [isbn, setIsbn] = useState("");
  const [category, setCategory] = useState("Fiction");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
      return;
    }
    loadBook();
  }, [router, bookId]);

  async function loadBook() {
    try {
      setInitialLoading(true);
      const book = await api<Book>(`/api/books/${bookId}`);
      setTitle(book.title);
      setAuthor(book.author);
      setIsbn(book.isbn);
      setCategory(book.category || "Fiction");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load book");
    } finally {
      setInitialLoading(false);
    }
  }

  const canSubmit =
    title.trim().length > 0 &&
    author.trim().length > 0 &&
    isbn.trim().length > 0 &&
    !loading;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;

    setError(null);
    setLoading(true);

    try {
      await api(`/api/books/${bookId}`, {
        method: "PUT",
        body: JSON.stringify({
          title: title.trim(),
          author: author.trim(),
          isbn: isbn.trim(),
          category: category.trim(),
          totalCopies: 0,
        }),
      });
      router.push("/admin/books");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update book");
    } finally {
      setLoading(false);
    }
  }

  if (initialLoading) {
    return (
      <div className="min-h-screen bg-[var(--bg)]">
        <nav className="border-b border-[var(--border)] bg-white">
          <div className="mx-auto max-w-7xl px-6 py-4">
            <Link href="/admin/books" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
              ← Back to Books
            </Link>
            <h1 className="text-2xl font-bold">Edit Book</h1>
          </div>
        </nav>
        <main className="mx-auto max-w-7xl px-6 py-12">
          <p className="text-[var(--muted)]">Loading...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <Link href="/admin/books" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
            ← Back to Books
          </Link>
          <h1 className="text-2xl font-bold">Edit Book</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <div className="max-w-md">
          <ErrorBanner testId="edit-book-error" message={error} />

          <form onSubmit={onSubmit} className="space-y-4">
            <label className="block text-sm">
              <span className="mb-1.5 block text-[var(--muted)]">Title *</span>
              <input
                type="text"
                data-testid="book-title-input"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="field w-full"
                placeholder="Book title"
              />
            </label>

            <label className="block text-sm">
              <span className="mb-1.5 block text-[var(--muted)]">Author *</span>
              <input
                type="text"
                data-testid="book-author-input"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                className="field w-full"
                placeholder="Author name"
              />
            </label>

            <label className="block text-sm">
              <span className="mb-1.5 block text-[var(--muted)]">ISBN *</span>
              <input
                type="text"
                data-testid="book-isbn-input"
                value={isbn}
                onChange={(e) => setIsbn(e.target.value)}
                className="field w-full"
                placeholder="ISBN-13"
              />
            </label>

            <label className="block text-sm">
              <span className="mb-1.5 block text-[var(--muted)]">Category</span>
              <select
                data-testid="book-category-select"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="field w-full"
              >
                <option>Fiction</option>
                <option>Non-Fiction</option>
                <option>Science</option>
                <option>Software</option>
                <option>Classics</option>
                <option>Design</option>
                <option>Reference</option>
              </select>
            </label>

            <p className="text-xs text-[var(--muted)]">
              To adjust copy counts, use the catalog's copy management feature.
            </p>

            <LoadingButton
              type="submit"
              data-testid="edit-book-submit"
              loading={loading}
              disabled={!canSubmit}
              className="w-full mt-6"
            >
              Save Changes
            </LoadingButton>
          </form>
        </div>
      </main>
    </div>
  );
}
