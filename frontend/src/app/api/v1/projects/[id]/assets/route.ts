import { fail, ok } from "@/server/api/response";
import { readDb, writeDb, id } from "@/server/store";
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import crypto from "node:crypto";

const MAX_SIZE = Number(process.env.NEXT_PUBLIC_UPLOAD_MAX_BYTES || 10 * 1024 * 1024);
const IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);
const DOC_TYPES = new Set(["application/zip", "application/x-zip-compressed", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]);

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
  const { id: projectId } = await context.params;
  const db = await readDb();
  const items = db.assets.filter((a) => a.projectId === projectId).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  return ok({ items });
}

export async function POST(req: Request, context: { params: Promise<{ id: string }> }) {
  const { id: projectId } = await context.params;
  const formData = await req.formData();
  const type = String(formData.get("type") || "") as "IMAGE" | "DOCUMENT";
  const file = formData.get("file");
  if (!(file instanceof File)) return fail("Arquivo obrigatório", 400);
  if (file.size > MAX_SIZE) return fail("Arquivo excede o limite", 400);

  const mime = file.type.toLowerCase();
  if (type === "IMAGE" && !IMAGE_TYPES.has(mime)) return fail("Tipo de imagem inválido", 400);
  if (type === "DOCUMENT" && !DOC_TYPES.has(mime)) return fail("Tipo de documento inválido", 400);

  const db = await readDb();
  const project = db.projects.find((p) => p.id === projectId);
  if (!project) return fail("Projeto não encontrado", 404);

  const bytes = Buffer.from(await file.arrayBuffer());
  const key = `projects/${projectId}/${type.toLowerCase()}/${crypto.randomUUID()}-${file.name.replace(/[^a-zA-Z0-9._-]/g, "_")}`;
  const fullPath = path.join(process.cwd(), ".uploads", key);
  await mkdir(path.dirname(fullPath), { recursive: true });
  await writeFile(fullPath, bytes);

  const asset = {
    id: id(),
    projectId,
    type,
    originalFilename: file.name,
    storageKey: key,
    contentType: mime || "application/octet-stream",
    sizeBytes: file.size,
    createdAt: new Date().toISOString(),
  };

  db.assets.push(asset);
  await writeDb(db);
  return ok({ asset }, 201);
}
