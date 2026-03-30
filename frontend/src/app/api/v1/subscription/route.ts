import { ok } from "@/server/api/response";

export async function GET() {
  return ok({ status: "ACTIVE", expiresAt: null, price: 9.99, canPublish: true, autoRenew: true, externalReference: null });
}
