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

export type DayOfWeek = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export interface WorkingHours {
  dayOfWeek: DayOfWeek;
  startTime: string; // "HH:mm"
  endTime: string;   // "HH:mm"
}

export interface Schedule {
  professionalId: string;
  workingHours: WorkingHours[];
}

export interface Service {
  serviceId: string;
  name: string;
  price: number;
  durationMinutes: number;
}
