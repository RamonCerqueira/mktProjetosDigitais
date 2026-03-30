import { ok, fail } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function GET(req: Request) {
  const userId = req.headers.get("x-user-id");
  if (!userId) return fail("x-user-id obrigatório", 401);
  const db = await readDb();
  const user = db.users.find((u) => u.id === userId);
  if (!user) return fail("Usuário não encontrado", 404);
  return ok({
    user: { id: user.id, name: user.name, email: user.email, role: user.role, documentType: "CPF" },
    subscription: { status: "ACTIVE", expiresAt: null, price: 9.99, canPublish: true, autoRenew: true, externalReference: null },
    myProjects: db.projects.filter((p) => p.sellerId === userId),
    offers: db.offers.filter((o: any) => o.buyerId === userId || o.sellerId === userId),
  });
}
