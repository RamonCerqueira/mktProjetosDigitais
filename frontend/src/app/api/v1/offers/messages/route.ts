import { ok } from "@/server/api/response";

export async function POST(req: Request) {
  const body = await req.json();
  return ok({ id: Date.now(), ...body, senderName: "Você", receiverName: "Participante", createdAt: new Date().toISOString(), negotiationKey: `offer-${body.offerId}` });
}
