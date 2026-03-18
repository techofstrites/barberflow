export interface Appointment {
  id: string;
  customerId: string;
  professionalId: string;
  services: ServiceItem[];
  startAt: string;
  endAt: string;
  status: "PENDING_CONFIRMATION" | "CONFIRMED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED" | "NO_SHOW";
  paymentStatus: "UNPAID" | "PARTIALLY_PAID" | "PAID";
  suggestedByAI: boolean;
}

export interface ServiceItem {
  serviceId: string;
  name: string;
  price: number;
  durationMinutes: number;
}

export interface Customer {
  id: string;
  phone: string;
  name: string;
  consentGiven: boolean;
}

export interface Professional {
  id: string;
  name: string;
  specialties: string[];
  active: boolean;
}

export interface Service {
  serviceId: string;
  name: string;
  price: number;
  durationMinutes: number;
}
