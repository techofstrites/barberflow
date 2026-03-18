"use client";

export function getToken(): string | null {
  return localStorage.getItem("token");
}

export function getTenantId(): string | null {
  return localStorage.getItem("tenantId");
}

export function setAuth(token: string, tenantId: string) {
  localStorage.setItem("token", token);
  localStorage.setItem("tenantId", tenantId);
}

export function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("tenantId");
}

export function isAuthenticated(): boolean {
  return !!getToken();
}
