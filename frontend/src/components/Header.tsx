"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

const navLinks = [
  { href: "/#como-funciona", label: "Como Funciona" },
  { href: "/subscription", label: "Preços" },
  { href: "/help", label: "Ajuda" }
];

export function Header() {
  const [logged, setLogged] = useState(false);

  useEffect(() => {
    setLogged(!!localStorage.getItem("accessToken"));
  }, []);

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/95 backdrop-blur-md">
      <div className="mx-auto flex w-full max-w-7xl items-center gap-4 px-4 py-4 lg:px-8">
        <Link href="/" className="flex items-center gap-3 text-3xl font-bold text-blue-950">
          <span className="grid size-11 place-items-center rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 text-white shadow">✦</span>
          <span className="text-[38px] leading-none">Logo</span>
        </Link>

        <nav className="hidden flex-1 items-center justify-center gap-1 rounded-full border border-slate-200 bg-slate-50 px-3 py-2 md:flex">
          {navLinks.map((item) => (
            <Link key={item.href} href={item.href} className="rounded-full px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-white hover:text-blue-700">
              {item.label}
            </Link>
          ))}
          <button className="ml-1 grid size-9 place-items-center rounded-full bg-white text-slate-600 shadow-sm transition hover:text-blue-700" aria-label="Buscar">
            🔍
          </button>
        </nav>

        <div className="ml-auto flex items-center gap-3">
          <Link href="/login" className="rounded-full px-5 py-2 font-semibold text-slate-700 transition hover:bg-slate-100">
            Entrar
          </Link>
          <span className="relative grid size-10 place-items-center rounded-full bg-gradient-to-r from-blue-400 to-blue-600 text-white shadow">
            👤
            <span className="absolute -right-1 -top-1 size-2 rounded-full bg-emerald-400" />
          </span>
          <Link href={logged ? "/dashboard" : "/register"} className="rounded-full bg-gradient-to-r from-blue-500 to-blue-700 px-6 py-2 font-semibold text-white shadow transition hover:brightness-110">
            {logged ? "Dashboard" : "Cadastrar-se"}
          </Link>
        </div>
      </div>
    </header>
  );
}
