"use client";

import { useState } from "react";
import { clothFor, coverImageUrl } from "@/lib/books";

type Props = {
  title: string;
  author: string;
  isbn: string;
  size?: "card" | "hero" | "thumb";
};

export function BookCover({ title, author, isbn, size = "card" }: Props) {
  const remote = coverImageUrl(isbn);
  const [failed, setFailed] = useState(!remote);
  const cloth = clothFor(title, author);
  const frame =
    size === "hero" ? "w-[min(100%,280px)]" : size === "thumb" ? "w-[72px]" : "w-full";

  return (
    <div
      className={`${frame} book-cover relative overflow-hidden bg-[var(--cloth)]`}
      style={{ aspectRatio: "2 / 3" }}
    >
      {!failed && remote ? (
        <img
          src={remote}
          alt=""
          className="absolute inset-0 h-full w-full object-cover"
          onError={() => setFailed(true)}
        />
      ) : (
        <div
          className="absolute inset-0 flex flex-col justify-between p-[11%] pt-[14%]"
          style={{ background: cloth.bg, color: cloth.ink }}
        >
          <span
            className="block h-px w-10 opacity-80"
            style={{ background: cloth.rule }}
            aria-hidden
          />
          <div>
            <p className="font-[family-name:var(--font-display)] text-[0.92rem] leading-snug tracking-tight sm:text-[1.02rem]">
              {title}
            </p>
            <p className="mt-3 text-[0.65rem] uppercase tracking-[0.16em] opacity-80">{author}</p>
          </div>
        </div>
      )}
    </div>
  );
}
