import jwt from "jsonwebtoken";

const JWT_SECRET = process.env.JWT_SECRET || "dev-secret";

export function signToken(payload: { sub: string; role: string; email: string }) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "1h" });
}
