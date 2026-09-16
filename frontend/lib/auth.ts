const TOKEN_KEY = "library.token";
const STUDENT_KEY = "library.studentId";
const ROLE_KEY = "library.role";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function getStudentId(): number | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(STUDENT_KEY);
  if (!raw) return null;
  const id = Number(raw);
  return Number.isFinite(id) ? id : null;
}

export function getRole(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ROLE_KEY);
}

export function isAdmin(): boolean {
  return getRole() === "ADMIN";
}

export function isLoggedIn(): boolean {
  return Boolean(getToken() && getStudentId());
}

export function saveSession(token: string, studentId: number): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(STUDENT_KEY, String(studentId));

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (payload.role) {
      localStorage.setItem(ROLE_KEY, payload.role);
    }
  } catch (e) {
    console.error("Failed to extract role from token", e);
  }
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(STUDENT_KEY);
  localStorage.removeItem(ROLE_KEY);
}
