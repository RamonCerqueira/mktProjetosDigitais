import { fail, ok } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const db = await readDb();
  const user = db.users.find((u) => u.id === id);
  if (!user) return fail("Usuário não encontrado", 404);
  return ok({ user: { id: user.id, name: user.name, email: user.email, role: user.role, roles: [user.role], blocked: false, active: true, subscriptionStatus: "ACTIVE", createdAt: user.createdAt }, documentType: "CPF", documentNumber: "***", history: [] });
}
