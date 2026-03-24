const EFFECTIVE_DATE = '24 de março de 2026';

export default function PrivacyPage() {
  return (
    <main className="mx-auto max-w-4xl px-6 py-16 space-y-8">
      <header className="space-y-3">
        <h1 className="text-4xl font-bold">Política de Privacidade</h1>
        <p className="text-sm text-slate-400">Vigência: {EFFECTIVE_DATE}</p>
        <p className="text-slate-300">
          Esta Política de Privacidade descreve como coletamos, utilizamos, compartilhamos, armazenamos e
          protegemos dados pessoais no contexto da operação do marketplace. O tratamento observa a LGPD
          (Lei nº 13.709/2018) e demais normas aplicáveis no Brasil.
        </p>
      </header>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">1. Dados coletados</h2>
        <ul className="list-disc space-y-2 pl-6 text-slate-300">
          <li>Dados cadastrais e de contato (nome, e-mail, telefone, informações corporativas).</li>
          <li>Dados de autenticação e segurança (hash de senha, logs de acesso, dispositivo e IP).</li>
          <li>Dados de transação e assinatura (planos, cobranças, status de pagamentos e histórico).</li>
          <li>Dados de conformidade (documentos, validações antifraude e evidências de auditoria).</li>
          <li>Comunicações realizadas na Plataforma para suporte, negociação e gestão de disputa.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">2. Finalidades e bases legais</h2>
        <p className="text-slate-300">
          Tratamos dados para executar contratos, cumprir obrigações legais/regulatórias, prevenir fraudes,
          assegurar segurança da informação, melhorar a experiência do usuário e exercer direitos em
          processos administrativos, arbitrais ou judiciais.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">3. Compartilhamento de dados</h2>
        <p className="text-slate-300">
          Poderemos compartilhar dados com provedores de pagamento, serviços de verificação, hospedagem,
          mensageria, analytics, parceiros antifraude e autoridades públicas, sempre dentro das finalidades
          legítimas e do mínimo necessário para a operação.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">4. Retenção e descarte</h2>
        <p className="text-slate-300">
          Os dados são retidos pelo prazo necessário ao cumprimento de obrigação legal, execução contratual
          e proteção do legítimo interesse, incluindo prazos prescricionais. Após esse período, adotamos
          descarte seguro ou anonimização, quando tecnicamente possível.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">5. Direitos do titular</h2>
        <p className="text-slate-300">
          O titular pode solicitar confirmação de tratamento, acesso, correção, anonimização, portabilidade,
          revogação de consentimento e informações sobre compartilhamento, observadas as hipóteses legais
          de retenção e limitação previstas na LGPD.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">6. Segurança da informação</h2>
        <p className="text-slate-300">
          Empregamos controles técnicos e organizacionais proporcionais ao risco, como segregação de
          acesso, trilhas de auditoria, monitoramento de eventos, gestão de vulnerabilidades e resposta a
          incidentes.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">7. Transferências internacionais</h2>
        <p className="text-slate-300">
          Quando houver processamento em infraestrutura fora do Brasil, adotaremos mecanismos contratuais e
          práticas de governança compatíveis com a proteção de dados exigida pela LGPD.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-2xl font-semibold">8. Atualizações</h2>
        <p className="text-slate-300">
          Esta Política pode ser atualizada periodicamente para refletir evoluções legais e operacionais.
          Publicaremos a versão vigente nesta página com a respectiva data de atualização.
        </p>
      </section>
    </main>
  );
}
