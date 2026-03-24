const EFFECTIVE_DATE = '24 de março de 2026';

export default function MarketplaceRulesPage() {
  return (
    <main className="mx-auto max-w-4xl px-6 py-16 space-y-8">
      <header className="space-y-3">
        <h1 className="text-4xl font-bold">Regras do Marketplace</h1>
        <p className="text-sm text-slate-400">Vigência: {EFFECTIVE_DATE}</p>
        <p className="text-slate-300">
          Estas regras definem padrões mínimos de qualidade, transparência e conduta para vendedores,
          compradores e parceiros da plataforma.
        </p>
      </header>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">1. Publicação de anúncios</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>Anúncios devem refletir dados reais, verificáveis e atualizados sobre o ativo digital.</li>
          <li>É proibido prometer retorno garantido, ocultar passivos ou manipular indicadores.</li>
          <li>Documentos e provas de titularidade podem ser exigidos a qualquer momento.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">2. Ativos proibidos</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>Negócios ilegais, pirataria, dados obtidos de forma ilícita e propriedade intelectual violada.</li>
          <li>Produtos/serviços com práticas enganosas, spam em massa ou violação de termos de terceiros.</li>
          <li>Operações com indícios de lavagem de dinheiro, fraude documental ou identidade falsa.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">3. Regras de negociação</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>Negociações devem ser conduzidas com respeito, diligência e boa-fé objetiva.</li>
          <li>É vedado assédio, discriminação, ameaça, tentativa de extorsão ou pressão indevida.</li>
          <li>A plataforma pode exigir que tratativas críticas ocorram por canais auditáveis.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">4. Uso de escrow e meios de pagamento</h2>
        <p className="text-slate-300">
          Sempre que disponível, as partes devem priorizar mecanismos oficiais de pagamento e escrow da
          plataforma para reduzir risco operacional e facilitar análise de disputas. Tentativas de burlar
          controles financeiros poderão resultar em sanções.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">5. Moderação e sanções</h2>
        <p className="text-slate-300">
          A plataforma poderá remover anúncios, congelar funcionalidades, reduzir distribuição de ofertas,
          suspender contas e registrar evidências para autoridades competentes quando identificar risco,
          abuso ou descumprimento contratual/legal.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">6. Disputas e evidências</h2>
        <p className="text-slate-300">
          Em casos de disputa, serão considerados logs técnicos, mensagens da plataforma, comprovantes de
          pagamento, documentos de titularidade e demais evidências relevantes para tomada de decisão.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">7. Atualizações das regras</h2>
        <p className="text-slate-300">
          As regras podem ser alteradas para refletir requisitos regulatórios, evolução de riscos e melhorias
          operacionais. A versão vigente sempre estará publicada nesta página.
        </p>
      </section>
    </main>
  );
}
