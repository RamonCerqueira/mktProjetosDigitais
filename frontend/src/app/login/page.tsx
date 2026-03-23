"use client";
import api from "@/lib/api";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const submit = async () => {
    try {
      const { data } = await api.post("/auth/login", { email, password });
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      router.push("/dashboard");
    } catch (e: any) { setError(e?.response?.data?.error || "Falha no login"); }
  };
  return <main className="mx-auto max-w-xl px-6 py-16"><div className="card space-y-4"><h1 className="text-3xl font-bold">Entrar</h1><input className="input" placeholder="Email" onChange={(e) => setEmail(e.target.value)} /><input className="input" type="password" placeholder="Senha" onChange={(e) => setPassword(e.target.value)} />{error && <p className="text-red-400">{error}</p>}<button className="btn-primary w-full" onClick={submit}>Acessar</button></div></main>;
}
