"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { NotificationBell } from "./NotificationBell";

export function Header() {
  const [logged, setLogged] = useState(false);
  const [role, setRole] = useState<string | null>(null);
  useEffect(() => { setLogged(!!localStorage.getItem("accessToken")); setRole(localStorage.getItem("userRole")); }, []);
  return (
    <header className="border-b border-slate-800 bg-slate-950/90 sticky top-0 z-10">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <Link href="/" className="text-xl font-bold text-emerald-400">MicroSaaS Market</Link>
        <nav className="flex flex-wrap gap-3 text-sm items-center">
          <Link href="/projects" className="btn-secondary">Marketplace</Link>
          <Link href="/marketplace-rules" className="btn-secondary">Regras</Link>
          {logged ? <><NotificationBell /><Link href={role === "ADMIN" ? "/admin" : "/dashboard"} className="btn-primary">{role === "ADMIN" ? "Admin" : "Dashboard"}</Link><Link href="/help" className="btn-secondary">Ajuda</Link></> : <><Link href="/login" className="btn-secondary">Entrar</Link><Link href="/register" className="btn-primary">Criar conta</Link></>}
        </nav>
      </div>
    </header>
  );
}
