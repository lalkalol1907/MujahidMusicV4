import { clearToken } from "@/shared/api";

export function logout() {
  clearToken();
  window.location.href = "/login";
}
