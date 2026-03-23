"use client";
import Link from "next/link";
import { useEffect, useState } from "react";

export function Header() {
  const [logged, setLogged] = useState(false);
  useEffect(() => setLogged(!!localStorage.getItem("accessToken")), []);
  return (
    <header className="border-b border-slate-800 bg-slate-950/90 sticky top-0 z-10">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <Link href="/" className="text-xl font-bold text-emerald-400">MicroSaaS Market</Link>
        <nav className="flex gap-3 text-sm">
          <Link href="/projects" className="btn-secondary">Marketplace</Link>
          {logged ? <Link href="/dashboard" className="btn-primary">Dashboard</Link> : <><Link href="/login" className="btn-secondary">Entrar</Link><Link href="/register" className="btn-primary">Criar conta</Link></>}
        </nav>
      </div>
    </header>
  );
}
