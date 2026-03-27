import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Service } from "@/types";

export function useServices() {
  return useQuery<Service[]>({
    queryKey: ["services"],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/services");
      return data;
    },
  });
}

export function useCreateService() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; price: number; durationMinutes: number }) =>
      api.post("/api/v1/services", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["services"] }),
  });
}

export function useUpdateService() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ serviceId, ...data }: { serviceId: string; name: string; price: number; durationMinutes: number }) =>
      api.put(`/api/v1/services/${serviceId}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["services"] }),
  });
}

export function useDeleteService() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (serviceId: string) => api.delete(`/api/v1/services/${serviceId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["services"] }),
  });
}
