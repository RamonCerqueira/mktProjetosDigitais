import { db } from "@/server/db";
import { projectSchema } from "@/server/schemas/project";
import { fail, ok } from "@/server/api/response";

export async function GET() {
  const projects = await db.project.findMany({
    orderBy: { createdAt: "desc" },
    include: { seller: { select: { id: true, name: true } }, assets: true },
    take: 30,
  });
  return ok({ items: projects });
}

export async function POST(req: Request) {
  const json = await req.json();
  const parsed = projectSchema.safeParse(json);
  if (!parsed.success) return fail(parsed.error.issues[0]?.message || "Payload inválido", 400);

  const sellerId = req.headers.get("x-user-id");
  if (!sellerId) return fail("x-user-id obrigatório", 401);

  const project = await db.project.create({
    data: {
      sellerId,
      title: parsed.data.title,
      description: parsed.data.description,
      category: parsed.data.category || null,
      techStack: parsed.data.techStack || null,
      price: parsed.data.price,
    },
  });

  return ok({ project }, 201);
}
