import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Customer } from "@/types";

export function useCustomers() {
  return useQuery<Customer[]>({
    queryKey: ["customers"],
    queryFn: async () => {
      const { data } = await api.get("/api/v1/customers");
      return data;
    },
  });
}

export function useCreateCustomer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { phone: string; name: string; consentGiven: boolean }) =>
      api.post("/api/v1/customers", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["customers"] }),
  });
}
