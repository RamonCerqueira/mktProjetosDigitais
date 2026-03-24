"use client";

import api from "@/lib/api";
import { NotificationItem } from "@/types";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useEffect, useMemo, useState } from "react";

const wsBaseUrl = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api").replace(/\/api\/?$/, "");

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const userId = useMemo(() => (typeof window === "undefined" ? null : localStorage.getItem("userId")), []);

  useEffect(() => {
    if (!userId) return;

    const load = async () => {
      const { data } = await api.get("/notifications", { params: { limit: 10 } });
      setItems(data.items || []);
      setUnreadCount(data.unreadCount || 0);
    };

    load();

    const client = new Client({
      webSocketFactory: () => new SockJS(`${wsBaseUrl}/ws`),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      client.subscribe(`/topic/notifications/${userId}`, (frame) => {
        const incoming = JSON.parse(frame.body) as NotificationItem;
        setItems((current) => [incoming, ...current].slice(0, 20));
        setUnreadCount((current) => current + 1);
      });
    };

    client.activate();
    return () => {
      void client.deactivate();
    };
  }, [userId]);

  if (!userId) return null;

  const markAsRead = async (id: number) => {
    await api.post(`/notifications/${id}/read`);
    setItems((current) => current.map((item) => (item.id === id ? { ...item, readAt: new Date().toISOString() } : item)));
    setUnreadCount((current) => Math.max(0, current - 1));
  };

  const markAllAsRead = async () => {
    await api.post("/notifications/read-all");
    setItems((current) => current.map((item) => ({ ...item, readAt: item.readAt || new Date().toISOString() })));
    setUnreadCount(0);
  };

  return (
    <div className="relative">
      <button className="btn-secondary relative" onClick={() => setOpen((v) => !v)} aria-label="Notificações">
        🔔
        {unreadCount > 0 && (
          <span className="absolute -right-2 -top-2 min-w-5 rounded-full bg-red-500 px-1 text-center text-xs text-white">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-96 rounded-xl border border-slate-700 bg-slate-900 p-3 shadow-2xl">
          <div className="mb-2 flex items-center justify-between">
            <h3 className="font-semibold">Notificações</h3>
            <button className="text-xs text-emerald-400 hover:underline" onClick={markAllAsRead}>
              Marcar todas como lidas
            </button>
          </div>
          <div className="max-h-80 space-y-2 overflow-y-auto">
            {items.length === 0 && <p className="text-sm text-slate-400">Sem notificações.</p>}
            {items.map((item) => (
              <button
                key={item.id}
                className={`w-full rounded-lg border p-3 text-left text-sm ${item.readAt ? "border-slate-700 text-slate-300" : "border-emerald-500/40 bg-emerald-500/10"}`}
                onClick={() => !item.readAt && markAsRead(item.id)}
              >
                <div className="font-medium">{item.title}</div>
                <div className="mt-1 text-xs text-slate-400">{item.body}</div>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
