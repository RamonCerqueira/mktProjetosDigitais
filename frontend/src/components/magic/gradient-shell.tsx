import { cn } from "@/lib/utils";

export function GradientShell({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <div className={cn("relative overflow-hidden rounded-2xl border border-emerald-400/20 bg-gradient-to-br from-emerald-500/10 via-slate-900 to-slate-950", className)}>
      <div className="pointer-events-none absolute -inset-1 bg-[radial-gradient(circle_at_top_right,rgba(16,185,129,0.25),transparent_40%)]" />
      <div className="relative">{children}</div>
    </div>
  );
}
