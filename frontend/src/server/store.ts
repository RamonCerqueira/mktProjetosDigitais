import { readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import crypto from "node:crypto";

export type User = { id: string; name: string; email: string; passwordHash: string; role: "ADMIN" | "SELLER" | "BUYER"; createdAt: string };
export type Project = { id: string; sellerId: string; title: string; description: string; category?: string | null; techStack?: string | null; price: number; createdAt: string; updatedAt: string };
export type Asset = { id: string; projectId: string; type: "IMAGE" | "DOCUMENT"; originalFilename: string; storageKey: string; contentType: string; sizeBytes: number; createdAt: string };
export type Db = { users: User[]; projects: Project[]; assets: Asset[]; offers: any[]; notifications: any[] };

const dbPath = path.join(process.cwd(), ".data", "db.json");

async function ensure() {
  await mkdir(path.dirname(dbPath), { recursive: true });
  try {
    await readFile(dbPath, "utf-8");
  } catch {
    await writeFile(dbPath, JSON.stringify({ users: [], projects: [], assets: [], offers: [], notifications: [] }, null, 2));
  }
}

export async function readDb(): Promise<Db> {
  await ensure();
  return JSON.parse(await readFile(dbPath, "utf-8")) as Db;
}

export async function writeDb(db: Db) {
  await ensure();
  await writeFile(dbPath, JSON.stringify(db, null, 2));
}

export function id() {
  return crypto.randomUUID();
}

export function hashPassword(raw: string) {
  return crypto.createHash("sha256").update(raw).digest("hex");
}
