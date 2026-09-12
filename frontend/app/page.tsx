"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { isLoggedIn } from "@/lib/auth";

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    router.replace(isLoggedIn() ? "/search" : "/login");
  }, [router]);

  return <p className="px-4 py-24 text-center text-sm text-[var(--muted)]">One moment…</p>;
}
