import { ok } from "@/server/api/response";

export async function GET() {
  return ok({ service: "next-fullstack", status: "ok", ts: new Date().toISOString() });
}
