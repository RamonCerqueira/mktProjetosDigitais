import { ok } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function GET() {
  const db = await readDb();
  return ok(db.projects.map((p) => ({ id: p.id, title: p.title, status: "PUBLISHED", verified: true, suspicious: false, moderationNotes: null, sellerName: db.users.find((u) => u.id === p.sellerId)?.name || "-", price: p.price, monthlyRevenue: p.price, createdAt: p.createdAt })));
}
