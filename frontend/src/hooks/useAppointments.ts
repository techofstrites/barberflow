import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Appointment } from "@/types";
import { format } from "date-fns";

export function useAppointments(from: Date, to: Date) {
  return useQuery<Appointment[]>({
    queryKey: ["appointments", from.toISOString(), to.toISOString()],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/appointments", {
        params: {
          from: from.toISOString(),
          to: to.toISOString(),
        },
      });
      return data;
    },
  });
}

export function useCancelAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.delete(`/api/v1/appointments/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments"] }),
  });
}

export function useCompleteAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.post(`/api/v1/appointments/${id}/complete`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments"] }),
  });
}

export function useScheduleAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: {
      customerId: string;
      professionalId: string;
      serviceIds: string[];
      startAt: string;
    }) => api.post("/api/v1/appointments", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments"] }),
  });
}
