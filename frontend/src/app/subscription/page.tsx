"use client";
import api from "@/lib/api";
import { Subscription } from "@/types";
import { useEffect, useState } from "react";

export default function SubscriptionPage() {
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const load = async () => setSubscription((await api.get("/subscription")).data);
  const activate = async () => { await api.post("/subscription/activate-mock"); await load(); };
  const cancel = async () => { await api.post("/subscription/cancel"); await load(); };
  const renew = async () => { await api.post("/subscription/renew"); await load(); };
  useEffect(() => { load(); }, []);
  return <main className="mx-auto max-w-3xl px-6 py-16"><div className="card space-y-4"><h1 className="text-3xl font-bold">Assinatura seller</h1><p className="text-slate-300">Plano único de R$ 9,99/mês com renovação automática para publicar e manter projetos visíveis.</p>{subscription && <div className="rounded-xl bg-slate-950 p-4"><p>Status: <strong>{subscription.status}</strong></p><p>Expira em: {subscription.expiresAt || "-"}</p><p>Pode publicar: {subscription.canPublish ? "Sim" : "Não"}</p><p>Renovação automática: {subscription.autoRenew ? "Ativa" : "Desligada"}</p><p>Referência externa: {subscription.externalReference || "-"}</p></div>}<div className="flex gap-3"><button className="btn-primary" onClick={activate}>Ativar fluxo mockado</button><button className="btn-secondary" onClick={renew}>Renovar agora</button><button className="btn-secondary" onClick={cancel}>Cancelar</button></div></div></main>;
}
