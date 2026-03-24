"use client";
import api from "@/lib/api";
import { AdminOverview, AdminProject, AdminTransaction, AdminUserDetail, AdminUserSummary } from "@/types";
import { useEffect, useState } from "react";

export default function AdminPage() {
  const [overview, setOverview] = useState<AdminOverview | null>(null);
  const [users, setUsers] = useState<AdminUserSummary[]>([]);
  const [projects, setProjects] = useState<AdminProject[]>([]);
  const [transactions, setTransactions] = useState<AdminTransaction[]>([]);
  const [audit, setAudit] = useState<AdminUserDetail["history"]>([]);
  const [selectedUser, setSelectedUser] = useState<AdminUserDetail | null>(null);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const [overviewRes, usersRes, projectsRes, transactionsRes, auditRes] = await Promise.all([
        api.get("/admin/overview"),
        api.get("/admin/users"),
        api.get("/admin/projects"),
        api.get("/admin/transactions"),
        api.get("/admin/audit-logs", { params: { limit: 20 } }),
      ]);
      setOverview(overviewRes.data);
      setUsers(usersRes.data);
      setProjects(projectsRes.data);
      setTransactions(transactionsRes.data);
      setAudit(auditRes.data);
      setError("");
    } catch (e: any) {
      setError(e?.response?.data?.error || "Acesso restrito ao administrador.");
    }
  };

  const openUser = async (id: number) => setSelectedUser((await api.get(`/admin/users/${id}`)).data);
  const moderateProject = async (id: number, suspicious: boolean) => {
    await api.post(`/admin/projects/${id}/moderate`, { verified: !suspicious, status: suspicious ? "HIDDEN" : "PUBLISHED", moderationNotes: suspicious ? "Ocultado por revisão administrativa" : "Projeto revisado e verificado" });
    await load();
  };

  useEffect(() => { load(); }, []);

  if (error) return <main className="mx-auto max-w-6xl px-6 py-16"><div className="card text-red-400">{error}</div></main>;
  if (!overview) return <main className="mx-auto max-w-6xl px-6 py-16">Carregando painel admin...</main>;

  return (
    <main className="mx-auto max-w-7xl space-y-8 px-6 py-16">
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="MRR" value={`R$ ${overview.financial.monthlyRecurringRevenue}`} />
        <StatCard title="Receita total" value={`R$ ${overview.financial.totalRevenue}`} />
        <StatCard title="Assinaturas ativas" value={`${overview.financial.activeSubscriptions}`} />
        <StatCard title="Churn" value={`${overview.financial.churnRate}%`} />
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.4fr_1fr]">
        <div className="card space-y-4"><h2 className="text-2xl font-semibold">Analytics</h2><div className="grid gap-4 md:grid-cols-3"><MiniMetric label="Visitantes" value={overview.conversion.visitors} /><MiniMetric label="Usuários" value={overview.conversion.users} /><MiniMetric label="Assinantes" value={overview.conversion.subscribers} /><MiniMetric label="Visitante → usuário" value={`${overview.conversion.visitorToUserRate}%`} /><MiniMetric label="Usuário → assinante" value={`${overview.conversion.userToSubscriberRate}%`} /><MiniMetric label="Retenção" value={`${overview.conversion.retentionRate}%`} /></div><Chart title="Novos usuários / dia" points={overview.newUsersByDay} /><Chart title="Projetos criados / dia" points={overview.projectsByDay} /></div>
        <div className="card space-y-4"><h2 className="text-2xl font-semibold">Ranking de sellers</h2>{overview.topSellers.map((seller) => <div key={seller.sellerId} className="rounded-xl border border-slate-800 p-4"><div className="flex items-center justify-between"><strong>{seller.sellerName}</strong><span>{seller.soldProjects} vendas</span></div><p className="mt-2 text-sm text-slate-400">Receita bruta R$ {seller.grossRevenue}</p></div>)}<div className="rounded-xl border border-slate-800 p-4 text-sm text-slate-300">Projetos totais: {overview.projects.totalProjects} · Vendidos: {overview.projects.soldProjects} · Suspeitos: {overview.projects.suspiciousProjects}</div></div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.2fr_1fr]">
        <div className="card overflow-auto"><h2 className="mb-4 text-2xl font-semibold">Gestão de usuários</h2><table className="w-full text-sm"><thead><tr className="text-left text-slate-400"><th className="pb-3">Usuário</th><th>Role</th><th>Status</th><th>Assinatura</th><th></th></tr></thead><tbody>{users.map((user) => <tr key={user.id} className="border-t border-slate-800"><td className="py-3"><div className="font-medium">{user.name}</div><div className="text-slate-400">{user.email}</div></td><td>{user.role}</td><td>{user.blocked ? "Bloqueado" : user.active ? "Ativo" : "Cancelado"}</td><td>{user.subscriptionStatus}</td><td><button className="btn-secondary" onClick={() => openUser(user.id)}>Detalhes</button></td></tr>)}</tbody></table></div>
        <div className="card"><h2 className="mb-4 text-2xl font-semibold">Detalhes do usuário</h2>{selectedUser ? <div className="space-y-3 text-sm"><p><strong>{selectedUser.user.name}</strong> · {selectedUser.user.email}</p><p>Documento: {selectedUser.documentType} {selectedUser.documentNumber}</p><p>Roles: {selectedUser.user.roles.join(", ")}</p><p>Endereço: {selectedUser.street || "-"}, {selectedUser.streetNumber || "-"} · {selectedUser.user.city || "-"}/{selectedUser.user.state || "-"}</p><div className="flex gap-2"><button className="btn-secondary" onClick={async () => { await api.post(`/admin/users/${selectedUser.user.id}/${selectedUser.user.blocked ? "unblock" : "block"}`); await load(); await openUser(selectedUser.user.id); }}>{selectedUser.user.blocked ? "Desbloquear" : "Bloquear"}</button><button className="btn-secondary" onClick={async () => { await api.post(`/admin/users/${selectedUser.user.id}/roles`, { roles: ["ADMIN"] }); await load(); await openUser(selectedUser.user.id); }}>Tornar ADMIN</button><button className="btn-primary" onClick={async () => { await api.post(`/admin/users/${selectedUser.user.id}/cancel`); await load(); await openUser(selectedUser.user.id); }}>Cancelar conta</button></div><div className="space-y-2 pt-2"><h3 className="font-semibold">Histórico</h3>{selectedUser.history.map((item) => <div key={item.id} className="rounded-lg border border-slate-800 p-3"><div>{item.action}</div><div className="text-slate-400">{item.resourceType} · {item.createdAt}</div></div>)}</div></div> : <p className="text-slate-400">Selecione um usuário para ver detalhes completos.</p>}</div>
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <div className="card overflow-auto"><h2 className="mb-4 text-2xl font-semibold">Moderação de projetos</h2>{projects.map((project) => <div key={project.id} className="mb-3 rounded-xl border border-slate-800 p-4"><div className="flex items-center justify-between gap-3"><div><strong>{project.title}</strong><p className="text-sm text-slate-400">Seller: {project.sellerName} · {project.status}</p></div><span className={`rounded-full px-3 py-1 text-xs ${project.verified ? "bg-emerald-500/10 text-emerald-300" : "bg-amber-500/10 text-amber-300"}`}>{project.verified ? "Verificado" : "Pendente"}</span></div><p className="mt-2 text-sm text-slate-400">Preço: R$ {project.price} · MRR: R$ {project.monthlyRevenue}</p><p className="mt-2 text-sm text-slate-400">{project.moderationNotes || (project.suspicious ? "Sinalizado como suspeito" : "Sem observações")}</p><div className="mt-3 flex gap-2"><button className="btn-secondary" onClick={() => moderateProject(project.id, false)}>Verificar</button><button className="btn-primary" onClick={() => moderateProject(project.id, true)}>Ocultar</button></div></div>)}</div>
        <div className="card overflow-auto"><h2 className="mb-4 text-2xl font-semibold">Financeiro e auditoria</h2><div className="space-y-3">{transactions.slice(0, 10).map((tx) => <div key={tx.id} className="rounded-xl border border-slate-800 p-4"><div className="flex items-center justify-between"><strong>{tx.projectTitle}</strong><span>{tx.status}</span></div><p className="text-sm text-slate-400">Comprador: {tx.buyerName} · Seller: {tx.sellerName}</p><p className="text-sm text-slate-400">Valor: R$ {tx.amount} · Comissão: R$ {tx.platformFee}</p></div>)}</div><div className="mt-6 space-y-2"><h3 className="font-semibold">Últimos logs</h3>{audit.map((entry) => <div key={entry.id} className="rounded-lg border border-slate-800 p-3 text-sm"><div>{entry.action}</div><div className="text-slate-400">{entry.actorEmail || "anonymous"} · {entry.createdAt}</div></div>)}</div></div>
      </section>
    </main>
  );
}

function StatCard({ title, value }: { title: string; value: string }) {
  return <div className="card"><p className="text-sm text-slate-400">{title}</p><h2 className="mt-2 text-3xl font-bold">{value}</h2></div>;
}

function MiniMetric({ label, value }: { label: string; value: string | number }) {
  return <div className="rounded-xl border border-slate-800 p-4"><div className="text-sm text-slate-400">{label}</div><div className="mt-1 text-xl font-semibold">{value}</div></div>;
}

function Chart({ title, points }: { title: string; points: Array<{ label: string; value: number }> }) {
  const max = Math.max(...points.map((point) => point.value), 1);
  return <div className="rounded-xl border border-slate-800 p-4"><h3 className="mb-3 font-semibold">{title}</h3><div className="space-y-2">{points.map((point) => <div key={point.label} className="space-y-1"><div className="flex items-center justify-between text-xs text-slate-400"><span>{point.label}</span><span>{point.value}</span></div><div className="h-2 rounded-full bg-slate-800"><div className="h-2 rounded-full bg-emerald-400" style={{ width: `${(point.value / max) * 100}%` }} /></div></div>)}</div></div>;
}
