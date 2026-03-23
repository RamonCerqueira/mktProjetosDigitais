"use client";
import api from "@/lib/api";
import { ChatPanel } from "@/components/ChatPanel";
import { Dashboard } from "@/types";
import Link from "next/link";
import { useEffect, useState } from "react";

const emptyProject = { title: "", description: "", category: "", techStack: "", price: 0, monthlyRevenue: 0 };

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [form, setForm] = useState(emptyProject);
  const [error, setError] = useState("");
  const load = async () => setDashboard((await api.get("/dashboard")).data);
  const createProject = async () => {
    try { await api.post("/projects", form); setForm(emptyProject); setError(""); await load(); }
    catch (e: any) { setError(e?.response?.data?.error || "Falha ao publicar projeto"); }
  };
  useEffect(() => { load(); }, []);
  if (!dashboard) return <main className="px-6 py-16">Carregando...</main>;
  return (
    <main className="mx-auto max-w-6xl space-y-8 px-6 py-16">
      <section className="grid gap-6 lg:grid-cols-3">
        <div className="card lg:col-span-2"><p className="text-sm text-slate-400">Olá, {dashboard.user.name}</p><h1 className="text-3xl font-bold">Painel do seller</h1><p className="mt-3 text-slate-300">Role: {dashboard.user.role} · Status da assinatura: <strong>{dashboard.subscription.status}</strong></p><div className="mt-4 flex gap-3"><Link href="/subscription" className="btn-primary">Gerenciar assinatura</Link></div></div>
        <div className="card"><h2 className="text-xl font-semibold">KPIs</h2><div className="mt-4 space-y-2 text-slate-300"><p>Projetos: {dashboard.myProjects.length}</p><p>Ofertas: {dashboard.offers.length}</p><p>Pode publicar: {dashboard.subscription.canPublish ? "Sim" : "Não"}</p></div></div>
      </section>
      <section className="grid gap-6 lg:grid-cols-[1fr_1.2fr]">
        <div className="card space-y-3"><h2 className="text-2xl font-semibold">Novo projeto</h2>{["title","description","category","techStack","price","monthlyRevenue"].map((field) => <input key={field} className="input" placeholder={field} value={(form as any)[field]} onChange={(e) => setForm({ ...form, [field]: ["price","monthlyRevenue"].includes(field) ? Number(e.target.value) : e.target.value })} />)}{error && <p className="text-red-400">{error}</p>}<button className="btn-primary" onClick={createProject}>Publicar</button></div>
        <div className="card"><h2 className="mb-4 text-2xl font-semibold">Meus projetos</h2><div className="space-y-4">{dashboard.myProjects.map((project) => <div key={project.id} className="rounded-xl border border-slate-800 p-4"><div className="flex items-center justify-between"><h3 className="font-semibold">{project.title}</h3><span className="text-sm text-emerald-300">{project.status}</span></div><p className="mt-2 text-sm text-slate-300">{project.description}</p></div>)}{dashboard.myProjects.length === 0 && <p className="text-slate-400">Nenhum projeto cadastrado ainda.</p>}</div></div>
      </section>
      <ChatPanel offers={dashboard.offers} currentUserId={dashboard.user.id} />
    </main>
  );
}
