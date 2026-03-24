"use client";
import api from "@/lib/api";
import { Dashboard, ProjectAsset } from "@/types";
import dynamic from "next/dynamic";
import Link from "next/link";
import { useEffect, useState } from "react";

const ChatPanel = dynamic(() => import("@/components/ChatPanel").then((m) => m.ChatPanel), { ssr: false, loading: () => <div className="card">Carregando chat...</div> });

const emptyProject = { title: "", description: "", category: "", techStack: "", price: 0, monthlyRevenue: 0, activeUsers: 0 };

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [form, setForm] = useState(emptyProject);
  const [error, setError] = useState("");
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [docFile, setDocFile] = useState<File | null>(null);
  const [assets, setAssets] = useState<ProjectAsset[]>([]);

  const load = async () => {
    const data = (await api.get("/dashboard")).data as Dashboard;
    setDashboard(data);
    if (!selectedProjectId && data.myProjects[0]) setSelectedProjectId(data.myProjects[0].id);
  };

  const loadAssets = async (projectId: number) => {
    const { data } = await api.get(`/projects/${projectId}/assets`);
    setAssets(data.items || []);
  };

  const createProject = async () => {
    try {
      await api.post("/projects", form);
      setForm(emptyProject);
      setError("");
      await load();
    } catch (e: any) {
      setError(e?.response?.data?.error || "Falha ao publicar projeto");
    }
  };

  const upload = async (type: "IMAGE" | "DOCUMENT") => {
    if (!selectedProjectId) return;
    const file = type === "IMAGE" ? imageFile : docFile;
    if (!file) return;
    const formData = new FormData();
    formData.append("type", type);
    formData.append("file", file);
    await api.post(`/projects/${selectedProjectId}/assets`, formData, { headers: { "Content-Type": "multipart/form-data" } });
    if (type === "IMAGE") setImageFile(null);
    if (type === "DOCUMENT") setDocFile(null);
    await loadAssets(selectedProjectId);
  };

  useEffect(() => { load(); }, []);
  useEffect(() => { if (selectedProjectId) loadAssets(selectedProjectId); }, [selectedProjectId]);

  if (!dashboard) return <main className="px-6 py-16">Carregando...</main>;
  const blockedByCnpj = dashboard.user.documentType === "CNPJ";

  return (
    <main className="mx-auto max-w-6xl space-y-8 px-6 py-16">
      <section className="grid gap-6 lg:grid-cols-3">
        <div className="card lg:col-span-2"><p className="text-sm text-slate-400">Olá, {dashboard.user.name}</p><h1 className="text-3xl font-bold">Painel do seller</h1><p className="mt-3 text-slate-300">Role: {dashboard.user.role} · Status da assinatura: <strong>{dashboard.subscription.status}</strong></p>{blockedByCnpj && <p className="mt-3 rounded-xl border border-amber-500/30 bg-amber-500/10 p-3 text-sm text-amber-300">A plataforma é focada em dev individual: contas com CNPJ não podem publicar projetos. Use uma conta com CPF para vender ideias e microprodutos.</p>}<div className="mt-4 flex gap-3"><Link href="/subscription" className="btn-primary">Gerenciar assinatura</Link></div></div>
        <div className="card"><h2 className="text-xl font-semibold">KPIs</h2><div className="mt-4 space-y-2 text-slate-300"><p>Projetos: {dashboard.myProjects.length}</p><p>Ofertas: {dashboard.offers.length}</p><p>Pode publicar: {dashboard.subscription.canPublish && !blockedByCnpj ? "Sim" : "Não"}</p></div></div>
      </section>

      <section className="grid gap-6 lg:grid-cols-[1fr_1.2fr]">
        <div className="card space-y-3"><h2 className="text-2xl font-semibold">Novo projeto</h2>{["title","description","category","techStack","price","monthlyRevenue","activeUsers"].map((field) => <input key={field} className="input" placeholder={field} value={(form as any)[field]} onChange={(e) => setForm({ ...form, [field]: ["price","monthlyRevenue","activeUsers"].includes(field) ? Number(e.target.value) : e.target.value })} />)}{error && <p className="text-red-400">{error}</p>}<button className="btn-primary" onClick={createProject} disabled={blockedByCnpj}>Publicar</button></div>
        <div className="card"><h2 className="mb-4 text-2xl font-semibold">Meus projetos</h2><div className="space-y-4">{dashboard.myProjects.map((project) => <button key={project.id} className={`w-full rounded-xl border p-4 text-left ${selectedProjectId === project.id ? "border-emerald-500/70" : "border-slate-800"}`} onClick={() => setSelectedProjectId(project.id)}><div className="flex items-center justify-between"><h3 className="font-semibold">{project.title}</h3><span className="text-sm text-emerald-300">{project.status}</span></div><p className="mt-2 text-sm text-slate-300">{project.description}</p><div className="mt-3 grid gap-1 text-sm text-slate-400"><p>Score: {project.score ?? "-"}/100 · {project.qualification ?? "-"}</p><p>Nível do dev: {project.sellerLevel ?? "-"}</p></div></button>)}{dashboard.myProjects.length === 0 && <p className="text-slate-400">Nenhum projeto cadastrado ainda.</p>}</div></div>
      </section>

      <section className="card space-y-4">
        <h2 className="text-2xl font-semibold">Upload de ativos (simulação S3)</h2>
        {!selectedProjectId && <p className="text-slate-400">Selecione um projeto para anexar arquivos.</p>}
        {selectedProjectId && (
          <>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2 rounded-xl border border-slate-800 p-4">
                <h3 className="font-semibold">Imagem do projeto</h3>
                <input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files?.[0] || null)} />
                <button className="btn-secondary" onClick={() => upload("IMAGE")}>Enviar imagem</button>
              </div>
              <div className="space-y-2 rounded-xl border border-slate-800 p-4">
                <h3 className="font-semibold">ZIP / Documentos</h3>
                <input type="file" accept=".zip,.pdf,.doc,.docx,application/zip,application/pdf" onChange={(e) => setDocFile(e.target.files?.[0] || null)} />
                <button className="btn-secondary" onClick={() => upload("DOCUMENT")}>Enviar arquivo</button>
              </div>
            </div>

            <div className="space-y-2">
              <h3 className="font-semibold">Arquivos enviados</h3>
              {assets.length === 0 && <p className="text-sm text-slate-400">Nenhum arquivo enviado ainda.</p>}
              {assets.map((asset) => (
                <div key={asset.id} className="flex items-center justify-between rounded-xl border border-slate-800 p-3 text-sm">
                  <div>
                    <p>{asset.originalFilename}</p>
                    <p className="text-slate-400">{asset.type} · {(asset.sizeBytes / 1024).toFixed(1)} KB</p>
                  </div>
                  <a className="btn-secondary" href={asset.downloadUrl} target="_blank" rel="noreferrer">Baixar</a>
                </div>
              ))}
            </div>
          </>
        )}
      </section>

      <ChatPanel offers={dashboard.offers} currentUserId={dashboard.user.id} />
    </main>
  );
}
