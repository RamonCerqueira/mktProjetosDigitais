"use client";

import { useEffect, useMemo, useState } from "react";

type MarketProject = {
  id: number;
  title: string;
  category: string;
  description: string;
  seller: string;
  city: string;
  sales: number;
  rating: number;
  image: string;
  avatar: string;
  priceLabel: string;
};

const featuredProjects: MarketProject[] = [
  {
    id: 1,
    title: "LigaFut - App de Resultados de Futebol",
    category: "Micro-SaaS",
    description: "A.B.A de gestão esportiva com assinaturas e alertas em tempo real.",
    seller: "Rafael Oliveira",
    city: "São Paulo - SP",
    sales: 83,
    rating: 4.9,
    image: "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=12",
    priceLabel: "R$ 8.500"
  },
  {
    id: 2,
    title: "SaaS de Agendamento",
    category: "Web Apps",
    description: "Plataforma para clínicas e serviços com agenda, checkout e CRM.",
    seller: "Camila Martins",
    city: "Rio de Janeiro - RJ",
    sales: 30,
    rating: 4.8,
    image: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=5",
    priceLabel: "R$ 6.900"
  },
  {
    id: 3,
    title: "E-commerce com Next.js",
    category: "E-commerce",
    description: "Loja moderna com dashboard financeiro e integração de pagamentos.",
    seller: "Lucas Mendes",
    city: "Belo Horizonte - MG",
    sales: 26,
    rating: 4.7,
    image: "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=17",
    priceLabel: "R$ 7.200"
  },
  {
    id: 4,
    title: "Marketing Automation SaaS",
    category: "Automação",
    description: "Fluxos inteligentes de email, WhatsApp e campanhas com analytics.",
    seller: "Ana Costa",
    city: "Porto Alegre - RS",
    sales: 40,
    rating: 4.8,
    image: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=44",
    priceLabel: "R$ 5.400"
  },
  {
    id: 5,
    title: "Gestão Financeira SaaS",
    category: "Micro-SaaS",
    description: "Fluxo de caixa, DRE e projeções para PMEs em um painel unificado.",
    seller: "Felipe Santos",
    city: "Curitiba - PR",
    sales: 55,
    rating: 4.9,
    image: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=33",
    priceLabel: "R$ 9.100"
  },
  {
    id: 6,
    title: "Plataforma de Cursos Online",
    category: "Educação",
    description: "Portal EAD com aulas, avaliações, certificados e comunidade.",
    seller: "André Lima",
    city: "Brasília - DF",
    sales: 20,
    rating: 4.7,
    image: "https://images.unsplash.com/photo-1501504905252-473c47e087f8?auto=format&fit=crop&w=1200&q=80",
    avatar: "https://i.pravatar.cc/80?img=15",
    priceLabel: "R$ 4.700"
  }
];

const categories = ["Todos", "Micro-SaaS", "Web Apps", "Mobile", "E-commerce", "Automação", "Educação"];
const states = ["Todos", "SP", "RJ", "MG", "PR", "RS", "DF"];
const liveMessages = [
  "✅ Novo projeto publicado em Micro-SaaS",
  "💸 Venda concluída em São Paulo",
  "📈 3 compradores online visualizaram seu anúncio",
  "🔔 Nova proposta recebida em Web Apps"
];

export default function Home() {
  const [selectedCategory, setSelectedCategory] = useState("Todos");
  const [selectedState, setSelectedState] = useState("Todos");
  const [query, setQuery] = useState("");
  const [onlineDeals, setOnlineDeals] = useState(18);
  const [transactions, setTransactions] = useState(6500);
  const [messageIndex, setMessageIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setOnlineDeals((value) => (value > 35 ? 18 : value + 1));
      setTransactions((value) => value + Math.floor(Math.random() * 4));
      setMessageIndex((value) => (value + 1) % liveMessages.length);
    }, 2200);

    return () => clearInterval(interval);
  }, []);

  const visibleProjects = useMemo(() => {
    return featuredProjects.filter((project) => {
      const categoryMatch = selectedCategory === "Todos" || project.category === selectedCategory;
      const stateMatch = selectedState === "Todos" || project.city.includes(selectedState);
      const searchable = `${project.title} ${project.description} ${project.category} ${project.seller}`.toLowerCase();
      const queryMatch = !query || searchable.includes(query.toLowerCase());
      return categoryMatch && stateMatch && queryMatch;
    });
  }, [query, selectedCategory, selectedState]);

  return (
    <main className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-4 py-8 lg:px-8">
      <section className="rounded-[24px] border border-blue-100 bg-gradient-to-r from-blue-50 to-indigo-50 p-4 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm font-semibold text-blue-700">🚀 Anuncie seus projetos e ganhe receita recorrente!</p>
          <button className="btn-portal">Comece agora</button>
        </div>
      </section>

      <section className="rounded-2xl border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700">
        {liveMessages[messageIndex]}
      </section>

      <section className="hero relative overflow-hidden rounded-[32px] p-6 lg:p-10">
        <div className="grid gap-8 lg:grid-cols-[1.05fr_0.95fr] lg:items-center">
          <div className="space-y-6">
            <h1 className="text-4xl font-black leading-tight text-slate-900 md:text-6xl">Venda seus<br />Projetos Digitais</h1>
            <p className="max-w-xl text-xl text-slate-600">Transforme suas ideias em receita recorrente com um portal realmente vivo e ativo.</p>
            <div className="grid gap-3 rounded-2xl border border-slate-200 bg-white p-3 md:grid-cols-[1fr_180px_auto]">
              <input className="input-light" placeholder="O que você procura?" value={query} onChange={(e) => setQuery(e.target.value)} />
              <select className="input-light" value={selectedCategory} onChange={(e) => setSelectedCategory(e.target.value)}>
                {categories.map((category) => (
                  <option key={category}>{category}</option>
                ))}
              </select>
              <button className="btn-portal">Buscar</button>
            </div>
          </div>

          <div className="relative rounded-3xl border border-white/80 bg-white/80 p-4 shadow-2xl backdrop-blur">
            <img
              src="https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1400&q=80"
              alt="Profissional trabalhando em projetos digitais"
              className="h-[290px] w-full rounded-2xl object-cover"
            />
            <div className="absolute bottom-8 right-8 rounded-2xl bg-white/95 px-4 py-3 shadow-lg">
              <p className="text-xs font-semibold uppercase tracking-wide text-blue-600">Portal ao vivo</p>
              <p className="text-sm text-slate-600">🟢 {onlineDeals} negociações acontecendo agora</p>
            </div>
          </div>
        </div>

        <div className="mt-6 grid gap-4 rounded-2xl border border-slate-200 bg-white/95 p-4 shadow-lg md:grid-cols-[1.1fr_repeat(3,0.9fr)]">
          <div className="rounded-xl bg-gradient-to-r from-blue-500 to-blue-700 p-4 text-white">
            <p className="text-4xl font-black">Torne-se um Vendedor</p>
            <p className="text-base opacity-95">Pague apenas R$ 9,90/mês para listagem ilimitada.</p>
          </div>
          <div>
            <p className="text-4xl font-black text-slate-900">+245</p>
            <p className="text-slate-600">projetos listados</p>
          </div>
          <div>
            <p className="text-4xl font-black text-slate-900">+1.200</p>
            <p className="text-slate-600">devs cadastrados</p>
          </div>
          <div>
            <p className="text-4xl font-black text-slate-900">+{transactions.toLocaleString("pt-BR")}</p>
            <p className="text-slate-600">transações concluídas</p>
          </div>
        </div>
      </section>

      <section className="grid gap-6 lg:grid-cols-[260px_1fr]">
        <aside className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400">Categorias</h3>
          <div className="mt-3 space-y-2">
            {categories.map((category) => (
              <button key={category} onClick={() => setSelectedCategory(category)} className={`w-full rounded-xl px-3 py-2 text-left text-sm font-semibold transition ${selectedCategory === category ? "bg-blue-50 text-blue-700" : "text-slate-600 hover:bg-slate-100"}`}>
                {category}
              </button>
            ))}
          </div>

          <h3 className="mt-8 text-sm font-bold uppercase tracking-wider text-slate-400">Estado</h3>
          <div className="mt-3 space-y-2">
            {states.map((state) => (
              <button key={state} onClick={() => setSelectedState(state)} className={`w-full rounded-xl px-3 py-2 text-left text-sm font-semibold transition ${selectedState === state ? "bg-indigo-50 text-indigo-700" : "text-slate-600 hover:bg-slate-100"}`}>
                {state}
              </button>
            ))}
          </div>
        </aside>

        <div>
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-3xl font-extrabold text-slate-900">Projetos em Destaque</h2>
            <span className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700">Mais vendidos</span>
          </div>

          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {visibleProjects.map((project) => (
              <article key={project.id} className="group overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-xl">
                <div className="relative">
                  <img src={project.image} alt={project.title} className="h-44 w-full object-cover transition duration-300 group-hover:scale-[1.03]" />
                  <span className="absolute right-3 top-3 rounded-full bg-white/95 px-3 py-1 text-xs font-bold text-blue-700">{project.priceLabel}</span>
                </div>
                <div className="space-y-3 p-4">
                  <h3 className="text-2xl font-bold text-slate-900">{project.title}</h3>
                  <p className="text-sm text-slate-600">{project.description}</p>
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-semibold text-blue-600">{project.category}</span>
                    <span className="font-semibold text-amber-500">★ {project.rating}</span>
                  </div>
                  <p className="text-sm text-slate-500">{project.sales} vendas · {project.city}</p>

                  <div className="flex items-center justify-between border-t border-slate-100 pt-3">
                    <div className="flex items-center gap-2">
                      <img src={project.avatar} alt={`Avatar de ${project.seller}`} className="size-8 rounded-full" />
                      <span className="text-sm font-semibold text-slate-700">{project.seller}</span>
                    </div>
                    <button className="btn-portal">Ver Projeto</button>
                  </div>
                </div>
              </article>
            ))}
          </div>

          {visibleProjects.length === 0 && (
            <p className="mt-6 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-700">Nenhum projeto encontrado para os filtros selecionados.</p>
          )}
        </div>
      </section>
    </main>
  );
}
