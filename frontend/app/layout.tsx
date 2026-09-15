import type { Metadata } from "next";
import { DM_Sans, Fraunces } from "next/font/google";
import { AppNav } from "@/components/AppNav";
import "./globals.css";

const display = Fraunces({
  subsets: ["latin"],
  variable: "--font-display",
});

const sans = DM_Sans({
  subsets: ["latin"],
  variable: "--font-sans",
});

export const metadata: Metadata = {
  title: "CTBE Library · Addis Ababa University",
  description:
    "Library management for Addis Ababa University’s College of Technology and Built Environment — browse, borrow, and return teaching copies.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${display.variable} ${sans.variable}`}>
      <body className="min-h-screen antialiased">
        <AppNav />
        {children}
      </body>
    </html>
  );
}
