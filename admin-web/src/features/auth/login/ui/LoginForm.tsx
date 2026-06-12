import { FormEvent, useState } from "react";
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
        <h1>Admin Login</h1>
        <p className="muted">Enter your ADMIN_API_KEY to continue.</p>
        <div className="form-row" style={{ marginTop: "1rem" }}>
          <input
            type="password"
            placeholder="API key"
            value={key}
            onChange={(event) => setKey(event.target.value)}
            required
          />
          <button className="btn" type="submit" disabled={loading}>
            {loading ? "Checking..." : "Login"}
          </button>
        </div>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
}
