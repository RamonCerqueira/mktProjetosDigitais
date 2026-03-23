"use client";
import api from "@/lib/api";
import { Project } from "@/types";
import { useEffect, useState } from "react";

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [search, setSearch] = useState("");
  const load = async (value = "") => {
    const { data } = await api.get("/marketplace/projects", { params: { search: value } });
    setProjects(data);
  };
  useEffect(() => { load(); }, []);
  return (
    <main className="mx-auto max-w-6xl px-6 py-16">
      <div className="mb-8 flex gap-3"><input className="input" placeholder="Busque por título" value={search} onChange={(e) => setSearch(e.target.value)} /><button className="btn-primary" onClick={() => load(search)}>Buscar</button></div>
      <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">{projects.map((project) => <div key={project.id} className="card space-y-4"><div><p className="text-sm text-emerald-300">{project.category}</p><h2 className="text-2xl font-semibold">{project.title}</h2></div><p className="text-slate-300">{project.description}</p><div className="flex justify-between text-sm text-slate-400"><span>{project.techStack}</span><span>MRR R$ {project.monthlyRevenue}</span></div><div className="flex items-center justify-between"><strong className="text-2xl">R$ {project.price}</strong><button className="btn-secondary">Negociar</button></div></div>)}</div>
    </main>
  );
}
