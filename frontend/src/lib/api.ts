import axios from "axios";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export const api = axios.create({ baseURL: API_BASE });

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    const tenantId = localStorage.getItem("tenantId");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    if (tenantId) config.headers["X-Tenant-Id"] = tenantId;
  }
  return config;
});
