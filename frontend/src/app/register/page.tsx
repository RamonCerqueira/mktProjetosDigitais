"use client";
import api from "@/lib/api";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function RegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "SELLER", documentType: "CPF", documentNumber: "" });
  const [error, setError] = useState("");
  const submit = async () => {
    try {
      const { data } = await api.post("/auth/register", form);
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      router.push("/dashboard");
    } catch (e: any) { setError(e?.response?.data?.error || "Falha no cadastro"); }
  };
  return <main className="mx-auto max-w-xl px-6 py-16"><div className="card space-y-4"><h1 className="text-3xl font-bold">Criar conta</h1><input className="input" placeholder="Nome" onChange={(e) => setForm({ ...form, name: e.target.value })} /><input className="input" placeholder="Email" onChange={(e) => setForm({ ...form, email: e.target.value })} /><input className="input" type="password" placeholder="Senha" onChange={(e) => setForm({ ...form, password: e.target.value })} /><select className="input" onChange={(e) => setForm({ ...form, role: e.target.value })}><option value="SELLER">Seller</option><option value="BUYER">Buyer</option></select><select className="input" onChange={(e) => setForm({ ...form, documentType: e.target.value })}><option value="CPF">CPF</option><option value="CNPJ">CNPJ</option></select><input className="input" placeholder="CPF ou CNPJ" onChange={(e) => setForm({ ...form, documentNumber: e.target.value })} />{error && <p className="text-red-400">{error}</p>}<button className="btn-primary w-full" onClick={submit}>Cadastrar</button></div></main>;
}
