import { db } from "@/server/db";
import { registerSchema } from "@/server/schemas/auth";
import { fail, ok } from "@/server/api/response";
import bcrypt from "bcryptjs";

export async function POST(req: Request) {
  const json = await req.json();
  const parsed = registerSchema.safeParse(json);
  if (!parsed.success) return fail(parsed.error.issues[0]?.message || "Payload inválido", 400);

  const exists = await db.user.findUnique({ where: { email: parsed.data.email } });
  if (exists) return fail("E-mail já cadastrado", 409);

  const passwordHash = await bcrypt.hash(parsed.data.password, 10);
  const user = await db.user.create({
    data: {
      name: parsed.data.name,
      email: parsed.data.email,
      role: parsed.data.role,
      passwordHash,
    },
    select: { id: true, name: true, email: true, role: true, createdAt: true },
  });

  return ok({ user }, 201);
}
