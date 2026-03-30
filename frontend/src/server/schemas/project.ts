export type ProjectInput = { title: string; description: string; category?: string | null; techStack?: string | null; price: number };

export function validateProject(input: ProjectInput) {
  if (!input.title || input.title.length < 3) throw new Error("Título inválido");
  if (!input.description || input.description.length < 20) throw new Error("Descrição inválida");
  if (!(input.price > 0)) throw new Error("Preço deve ser maior que zero");
}
