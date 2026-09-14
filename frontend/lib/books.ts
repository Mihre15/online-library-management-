import type { Book } from "@/lib/types";

export function copiesOf(book: Book): number {
  return book.copiesAvailable ?? book.availableCopies ?? 0;
}

export function coverImageUrl(isbn: string): string | null {
  const digits = isbn.replace(/[^0-9Xx]/g, "");
  if (digits.length < 10 || /^0+$/.test(digits)) return null;
  return `https://covers.openlibrary.org/b/isbn/${digits}-L.jpg?default=false`;
}

export function hashHue(input: string): number {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash * 31 + input.charCodeAt(i)) >>> 0;
  }
  return hash;
}

const CLOTH: Array<{ bg: string; ink: string; rule: string }> = [
  { bg: "#3a1d28", ink: "#f0e2c4", rule: "#c4a574" },
  { bg: "#1f3a34", ink: "#e7dcc4", rule: "#b7a078" },
  { bg: "#1c2740", ink: "#ead9b8", rule: "#c9b089" },
  { bg: "#4a2a12", ink: "#f3e6c8", rule: "#d0b07a" },
  { bg: "#2b2b2b", ink: "#efe6d2", rule: "#c2b090" },
  { bg: "#3e2748", ink: "#f0e4c9", rule: "#cbb58a" },
  { bg: "#17324a", ink: "#e8dcc0", rule: "#b9a47a" },
  { bg: "#4a1f1f", ink: "#f2e4c6", rule: "#d0ae7c" },
];

export function clothFor(title: string, author: string) {
  return CLOTH[hashHue(`${title}|${author}`) % CLOTH.length];
}

export function formatLoanStatus(status: string): string {
  const upper = status.toUpperCase();
  if (upper === "ACTIVE") return "Active";
  if (upper === "OVERDUE") return "Overdue";
  if (upper === "RETURN_PENDING") return "Return pending";
  if (upper === "RETURNED") return "Returned";
  if (upper === "LOST") return "Lost";
  return status.charAt(0) + status.slice(1).toLowerCase();
}

export function canReturn(status: string): boolean {
  const upper = status.toUpperCase();
  return upper === "ACTIVE" || upper === "OVERDUE";
}

export function formatDate(iso: string): string {
  const date = new Date(`${iso}T12:00:00`);
  if (Number.isNaN(date.getTime())) return iso;
  return date.toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
}

export function dueCopy(iso: string, status: string): string {
  const date = new Date(`${iso}T12:00:00`);
  if (Number.isNaN(date.getTime())) return `Due ${iso}`;
  const today = new Date();
  today.setHours(12, 0, 0, 0);
  const days = Math.round((date.getTime() - today.getTime()) / 86_400_000);
  const pretty = formatDate(iso);
  const upper = status.toUpperCase();
  if (upper === "RETURNED") return `Was due ${pretty}`;
  if (upper === "RETURN_PENDING") return `Return requested · due ${pretty}`;
  if (upper === "OVERDUE" || days < 0) {
    const n = Math.abs(days);
    return `Due ${pretty} · ${n} day${n === 1 ? "" : "s"} overdue`;
  }
  if (days === 0) return `Due today · ${pretty}`;
  if (days === 1) return `Due tomorrow · ${pretty}`;
  return `Due ${pretty} · ${days} days left`;
}

export function denialHint(reason: string): string {
  switch (reason) {
    case "Account suspended":
      return "Your card is on hold. A librarian will need to clear it before you can take anything out.";
    case "Outstanding fine too high":
      return "Settle the balance on your card first — the desk can take payment and reopen borrowing.";
    case "Borrow limit reached":
      return "Five open loans is the house maximum. Bring one back and this shelf is yours again.";
    case "No copies available":
      return "Every copy is out. Check again after a return, or ask at the desk about a hold.";
    default:
      return "";
  }
}
