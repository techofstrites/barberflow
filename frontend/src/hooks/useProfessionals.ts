import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Professional, Schedule, WorkingHours } from "@/types";

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
      api.post<Professional>("/api/v1/professionals", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["professionals"] }),
  });
}

export function useUpdateProfessional() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...data }: { id: string; name: string; specialties: string[] }) =>
      api.put<Professional>(`/api/v1/professionals/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["professionals"] }),
  });
}

export function useSchedule(professionalId: string | null) {
  return useQuery<Schedule>({
    queryKey: ["schedule", professionalId],
    enabled: !!professionalId,
    queryFn: async () => {
      const { data } = await api.get(`/api/v1/professionals/${professionalId}/schedule`);
      return data;
    },
  });
}

export function useUpsertSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ professionalId, workingHours }: { professionalId: string; workingHours: WorkingHours[] }) =>
      api.put<Schedule>(`/api/v1/professionals/${professionalId}/schedule`, { workingHours }),
    onSuccess: (response, { professionalId }) =>
      qc.setQueryData(["schedule", professionalId], response.data),
  });
}
