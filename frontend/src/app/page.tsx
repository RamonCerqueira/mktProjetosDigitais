import Link from "next/link";

const benefits = [
  "Assinatura única de R$ 9,90/mês para sellers publicarem projetos.",
  "Marketplace público com busca, filtros e favoritos.",
  "Negociação com ofertas e chat em tempo real via WebSocket.",
  "Comissão automática nas vendas para monetização da plataforma.",
];

export default function Home() {
  return (
    <main className="mx-auto flex max-w-6xl flex-col gap-12 px-6 py-16">
      <section className="grid gap-8 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="space-y-6">
          <span className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-4 py-2 text-sm text-emerald-300">Marketplace SaaS monetizado</span>
          <h1 className="text-5xl font-bold leading-tight">Compre, venda e monetize projetos digitais e micro-SaaS em um só lugar.</h1>
          <p className="max-w-2xl text-lg text-slate-300">Plataforma completa para sellers assinantes publicarem ativos digitais, receberem ofertas e fecharem transações com comissão da plataforma.</p>
          <div className="flex gap-4"><Link href="/register" className="btn-primary">Quero vender</Link><Link href="/projects" className="btn-secondary">Explorar projetos</Link></div>
        </div>
        <div className="card space-y-4">
          <h2 className="text-2xl font-semibold">O que está incluso</h2>
          <ul className="space-y-3 text-slate-300">{benefits.map((benefit) => <li key={benefit}>• {benefit}</li>)}</ul>
        </div>
      </section>
    </main>
  );
}
