import { ok } from "@/server/api/response";
import { readDb } from "@/server/store";

export async function GET() {
  const db = await readDb();
  return ok({
    financial: { monthlyRecurringRevenue: 9.99, totalRevenue: db.projects.reduce((a, p) => a + p.price, 0), totalCommission: 0, activeSubscriptions: db.users.length, churnRate: 0 },
    conversion: { visitors: 0, users: db.users.length, subscribers: db.users.length, visitorToUserRate: 0, userToSubscriberRate: 100, retentionRate: 100 },
    projects: { totalProjects: db.projects.length, soldProjects: 0, suspiciousProjects: 0 },
    newUsersByDay: [],
    projectsByDay: [],
    topSellers: [],
  });
}
