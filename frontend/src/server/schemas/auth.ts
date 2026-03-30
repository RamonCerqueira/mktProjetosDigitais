export type RegisterInput = { name: string; email: string; password: string; role?: "ADMIN" | "SELLER" | "BUYER" };
export type LoginInput = { email: string; password: string };

export function validateRegister(input: RegisterInput) {
  if (!input.name || input.name.length < 2) throw new Error("Nome inválido");
  if (!input.email || !input.email.includes("@")) throw new Error("E-mail inválido");
  if (!input.password || input.password.length < 8) throw new Error("Senha deve ter no mínimo 8 caracteres");
}

export function validateLogin(input: LoginInput) {
  if (!input.email || !input.email.includes("@")) throw new Error("E-mail inválido");
  if (!input.password || input.password.length < 8) throw new Error("Senha inválida");
}
