export default function HelpPage() {
  return (
    <main className="mx-auto max-w-5xl space-y-8 px-6 py-16">
      <h1 className="text-4xl font-bold">Documentação para usuários</h1>
      <section className="card space-y-3"><h2 className="text-2xl font-semibold">Como vender projetos</h2><p className="text-slate-300">Crie uma conta seller, mantenha uma assinatura ativa, publique um anúncio completo com descrição, stack, preço e MRR, e acompanhe ofertas pelo dashboard.</p></section>
      <section className="card space-y-3"><h2 className="text-2xl font-semibold">Como comprar</h2><p className="text-slate-300">Explore o marketplace, filtre por categoria/localização, negocie com o seller, conclua o pagamento e confirme a entrega para liberar o escrow.</p></section>
      <section className="card space-y-3"><h2 className="text-2xl font-semibold">Como funciona a assinatura</h2><p className="text-slate-300">A assinatura ativa habilita publicação e manutenção de projetos públicos. Se expirar ou for cancelada, os projetos do seller podem ser ocultados automaticamente.</p></section>
      <section className="card space-y-3"><h2 className="text-2xl font-semibold">Como funciona a segurança</h2><p className="text-slate-300">A plataforma aplica RBAC, JWT, trilha de auditoria, antifraude, rastreamento com trace ID, cache, filas e monitoramento operacional.</p></section>
    </main>
  );
}
