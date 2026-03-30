"use client";

import api from "@/lib/api";
import Link from "next/link";
import { useState } from "react";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState("");

  const submit = async () => {
    await api.post("/auth/forgot-password", { email });
    setStatus("Se o e-mail existir na base, enviaremos um link de recuperação em instantes.");
  };

  return (
    <main className="mx-auto max-w-xl px-6 py-16">
      <div className="card space-y-4">
        <h1 className="text-3xl font-bold">Recuperar senha</h1>
        <p className="text-sm text-slate-400">Informe seu e-mail para receber o link de redefinição.</p>
        <input className="input" type="email" placeholder="seu-email@dominio.com" value={email} onChange={(e) => setEmail(e.target.value)} />
        <button className="btn-primary w-full" onClick={submit}>Enviar link</button>
        {status && <p className="text-emerald-300 text-sm">{status}</p>}
        <Link className="text-sm text-slate-400 hover:underline" href="/login">Voltar para login</Link>
      </div>
    </main>
  );
}
