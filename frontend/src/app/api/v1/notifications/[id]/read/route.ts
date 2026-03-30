import { ok } from "@/server/api/response";

export async function POST(_: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  return ok({ id, readAt: new Date().toISOString() });
}
