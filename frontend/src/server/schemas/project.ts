import { z } from "zod";

export const projectSchema = z.object({
  title: z.string().min(3).max(120),
  description: z.string().min(20).max(4000),
  category: z.string().max(60).optional().nullable(),
  techStack: z.string().max(120).optional().nullable(),
  price: z.number().positive(),
});
