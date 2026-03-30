import { fail, ok } from "@/server/api/response";
import { readDb, writeDb, id, hashPassword } from "@/server/store";
import { validateRegister } from "@/server/schemas/auth";

export async function POST(req: Request) {
  try {
    const json = await req.json();
    validateRegister(json);

    const db = await readDb();
    if (db.users.some((u) => u.email.toLowerCase() === json.email.toLowerCase())) return fail("E-mail já cadastrado", 409);

    const user = {
      id: id(),
      name: json.name,
      email: json.email,
      role: json.role || "BUYER",
      passwordHash: hashPassword(json.password),
      createdAt: new Date().toISOString(),
    };
    db.users.push(user);
    await writeDb(db);

    return ok({ user: { id: user.id, name: user.name, email: user.email, role: user.role, createdAt: user.createdAt } }, 201);
  } catch (e: any) {
    return fail(e?.message || "Payload inválido", 400);
  }
}
