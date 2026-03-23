"use client";
import api from "@/lib/api";
import { Message, Offer } from "@/types";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useEffect, useMemo, useState } from "react";

const socketUrl = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api").replace(/\/api$/, "/api/ws/chat");

export function ChatPanel({ offers, currentUserId }: { offers: Offer[]; currentUserId: number }) {
  const [selectedOfferId, setSelectedOfferId] = useState<number | null>(offers[0]?.id ?? null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [content, setContent] = useState("");
  const selectedOffer = useMemo(() => offers.find((offer) => offer.id === selectedOfferId) ?? null, [offers, selectedOfferId]);

  useEffect(() => { if (!selectedOfferId && offers[0]) setSelectedOfferId(offers[0].id); }, [offers, selectedOfferId]);

  useEffect(() => {
    if (!selectedOffer) return;
    let client: Client | undefined;
    const loadMessages = async () => setMessages((await api.get(`/offers/${selectedOffer.id}/messages`)).data);
    loadMessages();
    client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        client?.subscribe(`/topic/offers/${selectedOffer.negotiationKey}`, (frame) => {
          const message = JSON.parse(frame.body) as Message;
          setMessages((current) => [...current, message]);
        });
      },
    });
    client.activate();
    return () => client?.deactivate();
  }, [selectedOffer?.id, selectedOffer?.negotiationKey]);

  const send = async () => {
    if (!selectedOffer || !content.trim()) return;
    const receiverId = currentUserId === selectedOffer.buyerId ? selectedOffer.sellerId : selectedOffer.buyerId;
    await api.post("/offers/messages", { offerId: selectedOffer.id, receiverId, content });
    setContent("");
  };

  return (
    <div className="card grid gap-6 lg:grid-cols-[280px_1fr]">
      <div className="space-y-3 border-b border-slate-800 pb-4 lg:border-b-0 lg:border-r lg:pb-0 lg:pr-4">
        <h2 className="text-2xl font-semibold">Chat de negociação</h2>
        <div className="space-y-2">
          {offers.map((offer) => (
            <button key={offer.id} className={`w-full rounded-xl border px-4 py-3 text-left ${selectedOfferId === offer.id ? "border-emerald-500 bg-emerald-500/10" : "border-slate-800 bg-slate-950"}`} onClick={() => setSelectedOfferId(offer.id)}>
              <p className="font-medium">Negociação #{offer.id}</p>
              <p className="text-sm text-slate-400">Status: {offer.status}</p>
              <p className="text-sm text-slate-400">Valor: R$ {offer.amount}</p>
            </button>
          ))}
          {offers.length === 0 && <p className="text-sm text-slate-500">Nenhuma negociação ativa.</p>}
        </div>
      </div>
      <div className="space-y-4">
        <div>
          <h3 className="text-xl font-semibold">Mensagens</h3>
          {selectedOffer && <p className="text-sm text-slate-400">Entre {selectedOffer.buyerName} e {selectedOffer.sellerName}</p>}
        </div>
        <div className="max-h-[360px] space-y-3 overflow-y-auto rounded-xl border border-slate-800 bg-slate-950 p-4">
          {messages.map((message) => (
            <div key={message.id} className={`rounded-xl p-3 ${message.senderId === currentUserId ? "bg-emerald-500/10 border border-emerald-500/20" : "bg-slate-900 border border-slate-800"}`}>
              <div className="mb-1 flex items-center justify-between text-xs text-slate-400"><span>{message.senderName}</span><span>{new Date(message.createdAt).toLocaleString("pt-BR")}</span></div>
              <p>{message.content}</p>
            </div>
          ))}
          {messages.length === 0 && <p className="text-sm text-slate-500">Nenhuma mensagem ainda.</p>}
        </div>
        {selectedOffer && <div className="flex gap-3"><input className="input" placeholder="Digite sua mensagem" value={content} onChange={(e) => setContent(e.target.value)} /><button className="btn-primary" onClick={send}>Enviar</button></div>}
      </div>
    </div>
  );
}
