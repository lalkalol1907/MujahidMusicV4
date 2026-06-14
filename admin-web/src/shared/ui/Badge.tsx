import type { ReactNode } from "react";

type BadgeVariant = "default" | "success" | "danger" | "neutral";

interface BadgeProps {
  variant?: BadgeVariant;
  dot?: boolean;
  children: ReactNode;
}

export function Badge({ variant = "default", dot, children }: BadgeProps) {
  return (
    <span className={`badge ${variant !== "default" ? variant : ""}`}>
      {dot && <span className="badge-dot" />}
      {children}
    </span>
  );
}
