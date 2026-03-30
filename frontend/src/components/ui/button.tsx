import * as React from "react";
import { cn } from "@/lib/utils";

type Variant = "default" | "secondary" | "ghost";
type Size = "default" | "sm" | "lg";

const variantClasses: Record<Variant, string> = {
  default: "bg-emerald-500 text-white hover:bg-emerald-600",
  secondary: "bg-slate-800 text-slate-100 hover:bg-slate-700",
  ghost: "hover:bg-slate-800",
};

const sizeClasses: Record<Size, string> = {
  default: "h-10 px-4 py-2",
  sm: "h-8 px-3",
  lg: "h-11 px-6",
};

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
}

export function Button({ className, variant = "default", size = "default", ...props }: ButtonProps) {
  return <button className={cn("inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors focus-visible:outline-none disabled:opacity-50", variantClasses[variant], sizeClasses[size], className)} {...props} />;
}
