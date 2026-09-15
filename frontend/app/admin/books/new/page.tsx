"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { ErrorBanner } from "@/components/ErrorBanner";
import { LoadingButton } from "@/components/LoadingButton";

export default function AddBookPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [isbn, setIsbn] = useState("");
  const [category, setCategory] = useState("Fiction");
  const [totalCopies, setTotalCopies] = useState("1");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
    }
  }, [router]);

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
      await api("/api/books", {
        method: "POST",
        body: JSON.stringify({
          title: title.trim(),
          author: author.trim(),
          isbn: isbn.trim(),
          category: category.trim(),
          totalCopies: parseInt(totalCopies),
        }),
      });
      router.push("/admin/books");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add book");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <Link href="/admin/books" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
            ← Back to Books
          </Link>
          <h1 className="text-2xl font-bold">Add New Book</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <div className="max-w-md">
          <ErrorBanner testId="add-book-error" message={error} />

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

            <label className="block text-sm">
              <span className="mb-1.5 block text-[var(--muted)]">Total Copies</span>
              <input
                type="number"
                min="1"
                data-testid="book-copies-input"
                value={totalCopies}
                onChange={(e) => setTotalCopies(e.target.value)}
                className="field w-full"
              />
            </label>

            <LoadingButton
              type="submit"
              data-testid="add-book-submit"
              loading={loading}
              disabled={!canSubmit}
              className="w-full mt-6"
            >
              Add Book
            </LoadingButton>
          </form>
        </div>
      </main>
    </div>
  );
}
