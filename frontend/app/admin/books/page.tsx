"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { type Book } from "@/lib/types";
import { copiesOf } from "@/lib/books";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function BooksManagement() {
  const router = useRouter();
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
      return;
    }
    loadBooks();
  }, [router]);

  async function loadBooks() {
    try {
      setLoading(true);
      const data = await api<Book[]>("/api/books");
      setBooks(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load books");
    } finally {
      setLoading(false);
    }
  }

  async function deleteBook(id: number) {
    if (!confirm("Are you sure you want to delete this book?")) return;
    try {
      await api(`/api/books/${id}`, { method: "DELETE" });
      setBooks(books.filter(b => b.id !== id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete book");
    }
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4 flex justify-between items-center">
          <div>
            <Link href="/admin" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
              ← Back to Admin
            </Link>
            <h1 className="text-2xl font-bold">Book Management</h1>
          </div>
          <Link
            href="/admin/books/new"
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            + Add Book
          </Link>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <ErrorBanner message={error} />

        {loading ? (
          <p className="text-[var(--muted)]">Loading books...</p>
        ) : books.length === 0 ? (
          <p className="text-[var(--muted)]">No books yet. <Link href="/admin/books/new" className="text-blue-600 hover:underline">Add one</Link>.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-[var(--border)]">
                <tr>
                  <th className="text-left py-3 px-4">Title</th>
                  <th className="text-left py-3 px-4">Author</th>
                  <th className="text-left py-3 px-4">ISBN</th>
                  <th className="text-left py-3 px-4">Category</th>
                  <th className="text-center py-3 px-4">Copies</th>
                  <th className="text-center py-3 px-4">Available</th>
                  <th className="text-right py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {books.map(book => (
                  <tr key={book.id} className="border-b border-[var(--border)] hover:bg-[var(--hover)]">
                    <td className="py-3 px-4 font-medium">{book.title}</td>
                    <td className="py-3 px-4">{book.author}</td>
                    <td className="py-3 px-4 text-xs text-[var(--muted)]">{book.isbn}</td>
                    <td className="py-3 px-4 text-sm">{book.category}</td>
                    <td className="py-3 px-4 text-center">{book.totalCopies}</td>
                    <td className="py-3 px-4 text-center">{copiesOf(book)}</td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <Link
                        href={`/admin/books/${book.id}`}
                        className="text-blue-600 hover:underline text-xs"
                      >
                        Edit
                      </Link>
                      <button
                        onClick={() => deleteBook(book.id)}
                        className="text-red-600 hover:underline text-xs"
                      >
                        Delete
                      </button>
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
