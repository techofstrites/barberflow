import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Appointment, AppointmentDetail } from "@/types";

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

export function useAppointmentDetails(from: Date, to: Date) {
  return useQuery<AppointmentDetail[]>({
    queryKey: ["appointment-details", from.toISOString(), to.toISOString()],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/appointments/details", {
        params: {
          from: from.toISOString(),
          to: to.toISOString(),
        },
      });
      return data;
    },
  });
}

export function useAvailableSlots(professionalId: string | null, date: string | null) {
  return useQuery<string[]>({
    queryKey: ["available-slots", professionalId, date],
    enabled: !!professionalId && !!date,
    queryFn: async () => {
      const { data } = await api.get(`/api/v1/professionals/${professionalId}/schedule/available-slots`, {
        params: { date },
      });
      return data;
    },
  });
}

export function useCancelAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.delete(`/api/v1/appointments/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments"] });
      qc.invalidateQueries({ queryKey: ["appointment-details"] });
    },
  });
}

export function useConfirmAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.post(`/api/v1/appointments/${id}/confirm`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments"] });
      qc.invalidateQueries({ queryKey: ["appointment-details"] });
    },
  });
}

export function useCompleteAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.post(`/api/v1/appointments/${id}/complete`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments"] });
      qc.invalidateQueries({ queryKey: ["appointment-details"] });
    },
  });
}

export function useDashboardStats() {
  return useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/dashboard/stats");
      return data as { appointmentsToday: number; revenueThisMonth: number; activeCustomers: number; returnRate: number };
    },
    refetchInterval: 60_000,
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
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments"] });
      qc.invalidateQueries({ queryKey: ["appointment-details"] });
    },
  });
}
