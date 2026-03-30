import { ok } from "@/server/api/response";

export async function POST() {
  return ok({ unreadCount: 0 });
}
