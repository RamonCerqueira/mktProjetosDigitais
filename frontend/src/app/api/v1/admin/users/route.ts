import { ok } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function GET() {
  const db = await readDb();
  return ok(db.users.map((u) => ({ id: u.id, name: u.name, email: u.email, role: u.role, roles: [u.role], blocked: false, active: true, subscriptionStatus: "ACTIVE", createdAt: u.createdAt, city: null, state: null })));
}
