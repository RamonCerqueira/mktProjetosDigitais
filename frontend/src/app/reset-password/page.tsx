"use client";

import api from "@/lib/api";
import { useEffect, useState } from "react";

export default function ResetPasswordPage() {
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const value = new URLSearchParams(window.location.search).get("token") || "";
    setToken(value);
  }, []);

  const submit = async () => {
    try {
      await api.post("/auth/reset-password", { token, newPassword });
      setStatus("Senha redefinida com sucesso. Faça login novamente.");
      setError("");
    } catch (e: any) {
      setError(e?.response?.data?.error || "Não foi possível redefinir a senha");
    }
  };

  return (
    <main className="mx-auto max-w-xl px-6 py-16">
      <div className="card space-y-4">
        <h1 className="text-3xl font-bold">Definir nova senha</h1>
        <p className="text-sm text-slate-400">Digite sua nova senha para concluir a recuperação.</p>
        <input className="input" type="password" placeholder="Nova senha" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        <button className="btn-primary w-full" onClick={submit} disabled={!token || !newPassword}>Atualizar senha</button>
        {status && <p className="text-emerald-300 text-sm">{status}</p>}
        {error && <p className="text-red-400 text-sm">{error}</p>}
      </div>
    </main>
  );
}
