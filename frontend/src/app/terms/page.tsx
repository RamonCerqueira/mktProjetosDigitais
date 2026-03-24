const EFFECTIVE_DATE = '24 de março de 2026';

export default function TermsPage() {
  return (
    <main className="mx-auto max-w-4xl px-6 py-16 space-y-8">
      <header className="space-y-3">
        <h1 className="text-4xl font-bold">Termos de Uso</h1>
        <p className="text-sm text-slate-400">Vigência: {EFFECTIVE_DATE}</p>
        <p className="text-slate-300">
          Estes Termos de Uso regulam o acesso e a utilização da plataforma de marketplace digital
          (“Plataforma”). Ao criar conta, navegar, anunciar, negociar ou contratar serviços na Plataforma,
          você declara que leu, compreendeu e concorda com estes termos.
        </p>
      </header>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">1. Elegibilidade e cadastro</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>Você deve possuir capacidade civil para contratar, conforme legislação brasileira aplicável.</li>
          <li>
            É obrigatório fornecer dados verdadeiros, completos e atualizados, inclusive em processos de
            verificação de identidade, titularidade e prevenção à fraude.
          </li>
          <li>Você é responsável por proteger credenciais, sessão e dispositivos vinculados à conta.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">2. Objeto e papel da Plataforma</h2>
        <p className="text-slate-300">
          A Plataforma intermedeia conexão entre compradores e vendedores de ativos/projetos digitais,
          oferecendo recursos de descoberta, negociação, comunicação, pagamento e suporte operacional.
          Salvo disposição expressa em contrário, a Plataforma não é parte direta do contrato principal entre
          as partes da transação.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">3. Obrigações de vendedores e compradores</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>
            Vendedores devem comprovar titularidade e licitude dos ativos anunciados, além de apresentar
            métricas e informações de negócio de forma íntegra, rastreável e verificável.
          </li>
          <li>
            Compradores devem conduzir due diligence própria, avaliar riscos e formalizar condições de
            aquisição com clareza antes da conclusão da operação.
          </li>
          <li>
            Ambas as partes devem manter conduta profissional, vedadas práticas de assédio, ameaça,
            discriminação, spam, engenharia social e fraude.
          </li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">4. Pagamentos, taxas e reembolsos</h2>
        <p className="text-slate-300">
          Taxas de intermediação, assinaturas e serviços adicionais são informados previamente na
          Plataforma. Liquidação financeira, regras de escrow, chargeback e reembolsos dependem das
          políticas aplicáveis ao método de pagamento, à natureza da disputa e às evidências apresentadas
          pelas partes.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">5. Compliance, antifraude e auditoria</h2>
        <p className="text-slate-300">
          Podemos adotar mecanismos de monitoramento, análise de risco, trilhas de auditoria e controles de
          segurança para cumprir obrigações legais, prevenir lavagem de dinheiro, mitigar fraudes e proteger
          o ecossistema. O uso da Plataforma autoriza esse tratamento dentro dos limites legais e da Política
          de Privacidade.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">6. Medidas de enforcement</h2>
        <p className="text-slate-300">
          Em caso de violação destes Termos, de normas legais ou das Regras do Marketplace, a Plataforma
          poderá aplicar medidas como remoção de conteúdo, limitação de funcionalidades, suspensão
          temporária, bloqueio de conta e retenção cautelar de valores, quando cabível.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">7. Propriedade intelectual</h2>
        <p className="text-slate-300">
          Marcas, software, layout, banco de dados e demais elementos da Plataforma são protegidos por
          direitos de propriedade intelectual. É proibida reprodução, engenharia reversa ou uso indevido sem
          autorização prévia e expressa.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">8. Limitação de responsabilidade</h2>
        <p className="text-slate-300">
          Na extensão permitida em lei, a Plataforma não garante lucro, desempenho financeiro futuro ou
          ausência absoluta de risco nas negociações. Cada parte permanece responsável por suas decisões
          comerciais, tributárias, jurídicas e contábeis.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">9. Alterações e contato</h2>
        <p className="text-slate-300">
          Estes Termos podem ser atualizados para refletir mudanças legais, regulatórias ou operacionais.
          Alterações relevantes serão comunicadas pelos canais oficiais da Plataforma.
        </p>
      </section>
    </main>
  );
}
