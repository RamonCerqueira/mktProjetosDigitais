import { fail, ok } from "@/server/api/response";
import { signToken } from "@/server/api/auth";
import { readDb, hashPassword } from "@/server/store";
import { validateLogin } from "@/server/schemas/auth";

export async function POST(req: Request) {
  try {
    const json = await req.json();
    validateLogin(json);

    const db = await readDb();
    const user = db.users.find((u) => u.email.toLowerCase() === json.email.toLowerCase());
    if (!user) return fail("Credenciais inválidas", 401);

    const valid = hashPassword(json.password) === user.passwordHash;
    if (!valid) return fail("Credenciais inválidas", 401);

    const token = signToken({ sub: user.id, role: user.role, email: user.email });
    return ok({ token, user: { id: user.id, name: user.name, email: user.email, role: user.role } });
  } catch (e: any) {
    return fail(e?.message || "Payload inválido", 400);
  }
}
