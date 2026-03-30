import { fail, ok } from "@/server/api/response";
import { readDb, writeDb, id } from "@/server/store";
import { validateProject } from "@/server/schemas/project";

export async function GET(req: Request) {
  const url = new URL(req.url);
  const q = (url.searchParams.get("q") || "").toLowerCase();
  const page = Number(url.searchParams.get("page") || "1");
  const limit = Math.min(50, Number(url.searchParams.get("limit") || "20"));

  const db = await readDb();
  const filtered = db.projects.filter((p) => !q || p.title.toLowerCase().includes(q));
  const start = (Math.max(page, 1) - 1) * limit;
  const items = filtered.slice(start, start + limit);
  return ok({ items, pagination: { page, limit, total: filtered.length } });
}

export async function POST(req: Request) {
  try {
    const json = await req.json();
    validateProject(json);
    const sellerId = req.headers.get("x-user-id");
    if (!sellerId) return fail("x-user-id obrigatório", 401);

    const db = await readDb();
    const project = {
      id: id(),
      sellerId,
      title: json.title,
      description: json.description,
      category: json.category || null,
      techStack: json.techStack || null,
      price: Number(json.price),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    db.projects.push(project);
    await writeDb(db);

    return ok({ project }, 201);
  } catch (e: any) {
    return fail(e?.message || "Payload inválido", 400);
  }
}
