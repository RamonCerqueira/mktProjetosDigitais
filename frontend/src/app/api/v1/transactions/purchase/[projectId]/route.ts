import { fail, ok } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function POST(_: Request, context: { params: Promise<{ projectId: string }> }) {
  const { projectId } = await context.params;
  const db = await readDb();
  const project = db.projects.find((p) => p.id === projectId);
  if (!project) return fail("Projeto não encontrado", 404);
  return ok({ id: `tx_${projectId}`, projectId, amount: project.price, platformFee: project.price * 0.1, sellerNetAmount: project.price * 0.9, status: "PENDING", checkoutUrl: `/checkout/${projectId}` });
}
