import { ok } from "@/server/api/response";

export async function GET() {
  return ok({ unreadCount: 0 });
}
