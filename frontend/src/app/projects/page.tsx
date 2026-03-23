"use client";
import api from "@/lib/api";
import { Project, ReverseGeocode, Transaction } from "@/types";
import { useEffect, useState } from "react";

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [search, setSearch] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [locationStatus, setLocationStatus] = useState("");
  const [actionError, setActionError] = useState("");

  const load = async (params?: { search?: string; city?: string; state?: string }) => {
    const { data } = await api.get("/marketplace/projects", { params });
    setProjects(data);
  };

  const useMyLocation = () => {
    if (!navigator.geolocation) {
      setLocationStatus("Geolocalização não suportada neste navegador.");
      return;
    }
    setLocationStatus("Solicitando permissão de localização...");
    navigator.geolocation.getCurrentPosition(async ({ coords }) => {
      try {
        const { data } = await api.get<ReverseGeocode>("/integrations/maps/reverse", { params: { lat: coords.latitude, lng: coords.longitude } });
        setCity(data.city || "");
        setState(data.state || "");
        setLocationStatus(`Mostrando projetos próximos de ${data.city}/${data.state}.`);
        await load({ search, city: data.city, state: data.state });
      } catch {
        setLocationStatus("Não foi possível identificar sua cidade/estado.");
      }
    }, () => setLocationStatus("Permissão de localização negada."));
  };

  const checkout = async (projectId: number) => {
    try {
      setActionError("");
      const { data } = await api.post<Transaction>(`/transactions/purchase/${projectId}`);
      if (data.checkoutUrl) window.open(data.checkoutUrl, "_blank", "noopener,noreferrer");
    } catch (e: any) {
      setActionError(e?.response?.data?.error || "Não foi possível iniciar o pagamento.");
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <main className="mx-auto max-w-6xl px-6 py-16">
      <div className="mb-8 grid gap-3 md:grid-cols-[2fr_1fr_120px_auto_auto]">
        <input className="input" placeholder="Busque por título" value={search} onChange={(e) => setSearch(e.target.value)} />
        <input className="input" placeholder="Cidade" value={city} onChange={(e) => setCity(e.target.value)} />
        <input className="input" placeholder="UF" value={state} onChange={(e) => setState(e.target.value.toUpperCase())} />
        <button className="btn-primary" onClick={() => load({ search, city, state })}>Buscar</button>
        <button className="btn-secondary" onClick={useMyLocation}>Usar minha localização</button>
      </div>
      {locationStatus && <p className="mb-4 text-sm text-slate-400">{locationStatus}</p>}
      {actionError && <p className="mb-4 text-sm text-red-400">{actionError}</p>}
      <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">{projects.map((project) => <div key={project.id} className="card space-y-4"><div><p className="text-sm text-emerald-300">{project.category}</p><h2 className="text-2xl font-semibold">{project.title}</h2></div><p className="text-slate-300">{project.description}</p><div className="flex justify-between text-sm text-slate-400"><span>{project.techStack}</span><span>{project.sellerCity}/{project.sellerState}</span></div><div className="flex justify-between text-sm text-slate-400"><span>MRR R$ {project.monthlyRevenue}</span><span>{project.sellerName}</span></div><div className="flex items-center justify-between gap-2"><strong className="text-2xl">R$ {project.price}</strong><div className="flex gap-2"><button className="btn-secondary">Negociar</button><button className="btn-primary" onClick={() => checkout(project.id)}>Pagar</button></div></div></div>)}</div>
    </main>
  );
}
