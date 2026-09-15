import { clearSession, getToken } from "@/lib/auth";
import { ApiError } from "@/lib/types";

type ErrorBody = { message?: string };

async function parseError(res: Response): Promise<string> {
  try {
    const body = (await res.json()) as ErrorBody;
    return body.message || res.statusText || "Request failed";
  } catch {
    return res.statusText || "Request failed";
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(path, { ...init, headers });

  if (res.status === 401 && !path.startsWith("/api/auth/login")) {
    clearSession();
    if (typeof window !== "undefined") {
      window.location.replace("/login");
    }
    throw new ApiError("Invalid or expired token", 401);
  }

  if (!res.ok) {
    throw new ApiError(await parseError(res), res.status);
  }

  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}
