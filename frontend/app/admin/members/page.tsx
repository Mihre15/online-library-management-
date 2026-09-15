"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { isLoggedIn, isAdmin } from "@/lib/auth";
import { api } from "@/lib/api";
import { type Member } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function MembersManagement() {
  const router = useRouter();
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<number | null>(null);

  useEffect(() => {
    if (!isLoggedIn() || !isAdmin()) {
      router.replace("/login");
      return;
    }
    loadMembers();
  }, [router]);

  async function loadMembers() {
    try {
      setLoading(true);
      const data = await api<Member[]>("/api/members");
      setMembers(data);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load members");
    } finally {
      setLoading(false);
    }
  }

  async function toggleStatus(member: Member) {
    setPendingId(member.id);
    setError(null);
    try {
      const action = member.status === "SUSPENDED" ? "reactivate" : "suspend";
      const updated = await api<Member>(`/api/members/${member.id}/${action}`, {
        method: "PUT",
      });
      setMembers((current) =>
        current.map((m) => (m.id === member.id ? updated : m))
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update member");
    } finally {
      setPendingId(null);
    }
  }

  return (
    <div className="min-h-screen bg-[var(--bg)]">
      <nav className="border-b border-[var(--border)] bg-white">
        <div className="mx-auto max-w-7xl px-6 py-4">
          <Link href="/admin" className="text-[var(--muted)] hover:underline text-sm mb-2 block">
            ← Back to Admin
          </Link>
          <h1 className="text-2xl font-bold">Member Management</h1>
        </div>
      </nav>

      <main className="mx-auto max-w-7xl px-6 py-12">
        <ErrorBanner message={error} />

        {loading ? (
          <p className="text-[var(--muted)]">Loading members...</p>
        ) : members.length === 0 ? (
          <p className="text-[var(--muted)]">No members yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-[var(--border)]">
                <tr>
                  <th className="text-left py-3 px-4">Name</th>
                  <th className="text-left py-3 px-4">Email</th>
                  <th className="text-left py-3 px-4">Role</th>
                  <th className="text-left py-3 px-4">Status</th>
                  <th className="text-left py-3 px-4">Registered</th>
                  <th className="text-right py-3 px-4">Fines</th>
                  <th className="text-right py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {members.map((member) => (
                  <tr key={member.id} className="border-b border-[var(--border)] hover:bg-[var(--hover)]">
                    <td className="py-3 px-4 font-medium">{member.fullName}</td>
                    <td className="py-3 px-4 text-[var(--muted)]">{member.email}</td>
                    <td className="py-3 px-4">
                      <span className={`text-xs px-2 py-0.5 rounded-full ${
                        member.role === "ADMIN" ? "bg-blue-100 text-blue-700" : "bg-gray-100 text-gray-700"
                      }`}>
                        {member.role}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`text-xs px-2 py-0.5 rounded-full ${
                        member.status === "SUSPENDED" ? "bg-red-100 text-red-700" : "bg-green-100 text-green-700"
                      }`}>
                        {member.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-[var(--muted)]">{member.registeredAt}</td>
                    <td className="py-3 px-4 text-right">
                      {member.outstandingFines > 0 ? (
                        <span className="text-red-600">{member.outstandingFines.toFixed(2)} ETB</span>
                      ) : (
                        <span className="text-[var(--muted)]">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-right">
                      {member.role !== "ADMIN" && (
                        <button
                          onClick={() => toggleStatus(member)}
                          disabled={pendingId === member.id}
                          className={`text-xs hover:underline ${
                            member.status === "SUSPENDED" ? "text-green-600" : "text-red-600"
                          }`}
                        >
                          {pendingId === member.id
                            ? "..."
                            : member.status === "SUSPENDED" ? "Reactivate" : "Suspend"}
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
