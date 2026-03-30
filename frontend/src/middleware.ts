import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(req: NextRequest) {
  const res = NextResponse.next();
  res.headers.set("X-Frame-Options", "DENY");
  res.headers.set("X-Content-Type-Options", "nosniff");
  res.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  res.headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
  res.headers.set("Content-Security-Policy", "default-src 'self'; img-src 'self' data: blob:; connect-src 'self' ws: wss: http: https:; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';");
  return res;
}

export const config = {
  matcher: ["/:path*"],
};
