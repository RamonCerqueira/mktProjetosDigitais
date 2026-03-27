import { db } from "@/server/db";
import { fail, ok } from "@/server/api/response";
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import crypto from "node:crypto";

const MAX_SIZE = Number(process.env.NEXT_PUBLIC_UPLOAD_MAX_BYTES || 10 * 1024 * 1024);
const IMAGE_TYPES = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);
const DOC_TYPES = new Set(["application/zip", "application/x-zip-compressed", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]);

export async function GET(_: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const items = await db.projectAsset.findMany({ where: { projectId: id }, orderBy: { createdAt: "desc" } });
  return ok({ items });
}

export async function POST(req: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  const formData = await req.formData();
  const type = String(formData.get("type") || "");
  const file = formData.get("file");
  if (!(file instanceof File)) return fail("Arquivo obrigatório", 400);
  if (file.size > MAX_SIZE) return fail("Arquivo excede o limite", 400);

  const mime = file.type.toLowerCase();
  if (type === "IMAGE" && !IMAGE_TYPES.has(mime)) return fail("Tipo de imagem inválido", 400);
  if (type === "DOCUMENT" && !DOC_TYPES.has(mime)) return fail("Tipo de documento inválido", 400);

  const project = await db.project.findUnique({ where: { id } });
  if (!project) return fail("Projeto não encontrado", 404);

  const bytes = Buffer.from(await file.arrayBuffer());
  const key = `projects/${id}/${type.toLowerCase()}/${crypto.randomUUID()}-${file.name.replace(/[^a-zA-Z0-9._-]/g, "_")}`;
  const fullPath = path.join(process.cwd(), ".uploads", key);
  await mkdir(path.dirname(fullPath), { recursive: true });
  await writeFile(fullPath, bytes);

  const asset = await db.projectAsset.create({
    data: {
      projectId: id,
      type,
      originalFilename: file.name,
      storageKey: key,
      contentType: mime || "application/octet-stream",
      sizeBytes: file.size,
    },
  });

  return ok({ asset }, 201);
}
