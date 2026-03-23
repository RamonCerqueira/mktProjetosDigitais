export default function MarketplaceRulesPage() {
  return (
    <main className="mx-auto max-w-4xl px-6 py-16 space-y-6">
      <h1 className="text-4xl font-bold">Regras do Marketplace</h1>
      <ul className="list-disc space-y-3 pl-6 text-slate-300">
        <li>Somente projetos digitais com titularidade legítima e informações verificáveis podem ser anunciados.</li>
        <li>É proibido publicar receitas falsas, métricas manipuladas, ativos sem autorização ou ofertas destinadas a enganar compradores.</li>
        <li>Negociações devem ocorrer com respeito, sem assédio, spam, tentativa de evasão antifraude ou manipulação de escrow.</li>
        <li>A plataforma pode ocultar anúncios, bloquear usuários e registrar evidências operacionais quando detectar risco, abuso ou descumprimento contratual.</li>
      </ul>
    </main>
  );
}
