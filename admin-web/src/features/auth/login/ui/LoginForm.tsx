import { FormEvent, useState } from "react";
import { Music2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { fetchStatus } from "@/entities/status";
import { setToken } from "@/shared/api";

export function LoginForm() {
  const navigate = useNavigate();
  const [key, setKey] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setToken(key.trim());
    try {
      await fetchStatus();
      navigate("/");
    } catch {
      setError("Invalid API key");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={onSubmit}>
        <div className="login-brand">
          <div className="login-brand-icon">
            <Music2 size={28} />
          </div>
          <h1>Welcome back</h1>
          <p className="muted">Enter your admin API key to continue</p>
        </div>

        <div className="login-form-stack">
          <input
            type="password"
            placeholder="ADMIN_API_KEY"
            value={key}
            onChange={(event) => setKey(event.target.value)}
            required
            autoFocus
          />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? "Verifying…" : "Sign in"}
          </button>
        </div>

        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
}
