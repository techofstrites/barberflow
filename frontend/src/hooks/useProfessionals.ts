import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Professional } from "@/types";

export function useProfessionals() {
  return useQuery<Professional[]>({
    queryKey: ["professionals"],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/professionals");
      return data;
    },
  });
}

export function useCreateProfessional() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; specialties: string[] }) =>
      api.post("/api/v1/professionals", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["professionals"] }),
  });
}
