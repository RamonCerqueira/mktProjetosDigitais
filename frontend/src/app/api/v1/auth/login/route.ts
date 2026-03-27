import { db } from "@/server/db";
import { loginSchema } from "@/server/schemas/auth";
import { fail, ok } from "@/server/api/response";
import bcrypt from "bcryptjs";
import { signToken } from "@/server/api/auth";

export async function POST(req: Request) {
  const json = await req.json();
  const parsed = loginSchema.safeParse(json);
  if (!parsed.success) return fail(parsed.error.issues[0]?.message || "Payload inválido", 400);

  const user = await db.user.findUnique({ where: { email: parsed.data.email } });
  if (!user) return fail("Credenciais inválidas", 401);

  const valid = await bcrypt.compare(parsed.data.password, user.passwordHash);
  if (!valid) return fail("Credenciais inválidas", 401);

  const token = signToken({ sub: user.id, role: user.role, email: user.email });
  return ok({ token, user: { id: user.id, name: user.name, email: user.email, role: user.role } });
}
