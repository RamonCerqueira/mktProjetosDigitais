export function signToken(payload: { sub: string; role: string; email: string }) {
  return Buffer.from(JSON.stringify({ ...payload, iat: Date.now() })).toString("base64url");
}
