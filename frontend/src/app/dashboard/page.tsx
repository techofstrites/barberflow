"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useAppointments, useCancelAppointment, useCompleteAppointment, useScheduleAppointment } from "@/hooks/useAppointments";
import { useCustomers } from "@/hooks/useCustomers";
import { useProfessionals } from "@/hooks/useProfessionals";
import { useServices } from "@/hooks/useServices";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { format, startOfDay, endOfDay, addDays } from "date-fns";
import { ptBR } from "date-fns/locale";
import { Plus, CheckCircle, XCircle, Calendar } from "lucide-react";
import type { Appointment } from "@/types";

const STATUS_LABELS: Record<string, string> = {
  PENDING_CONFIRMATION: "Aguardando",
  CONFIRMED: "Confirmado",
  IN_PROGRESS: "Em andamento",
  COMPLETED: "Concluído",
  CANCELLED: "Cancelado",
  NO_SHOW: "Não compareceu",
};

const STATUS_VARIANTS: Record<string, "default" | "secondary" | "destructive" | "outline"> = {
  PENDING_CONFIRMATION: "secondary",
  CONFIRMED: "default",
  IN_PROGRESS: "default",
  COMPLETED: "outline",
  CANCELLED: "destructive",
  NO_SHOW: "destructive",
};

function AppointmentCard({ appt, onComplete, onCancel }: {
  appt: Appointment;
  onComplete: (id: string) => void;
  onCancel: (id: string) => void;
}) {
  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-1 flex-1">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium">
                {format(new Date(appt.startAt), "HH:mm")} – {format(new Date(appt.endAt), "HH:mm")}
              </span>
              <Badge variant={STATUS_VARIANTS[appt.status]}>{STATUS_LABELS[appt.status]}</Badge>
              {appt.suggestedByAI && <Badge variant="outline">IA</Badge>}
            </div>
            <div className="text-sm text-muted-foreground">
              {appt.services.map((s) => s.name).join(", ")}
            </div>
            <div className="text-xs text-muted-foreground">
              R$ {appt.services.reduce((acc, s) => acc + s.price, 0).toFixed(2)} · {appt.paymentStatus}
            </div>
          </div>
          <div className="flex gap-2">
            {appt.status === "CONFIRMED" && (
              <Button size="sm" variant="outline" onClick={() => onComplete(appt.id)}>
                <CheckCircle className="h-4 w-4" />
              </Button>
            )}
            {!["COMPLETED", "CANCELLED", "NO_SHOW"].includes(appt.status) && (
              <Button size="sm" variant="outline" onClick={() => onCancel(appt.id)}>
                <XCircle className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function NewAppointmentDialog() {
  const [open, setOpen] = useState(false);
  const { data: customers } = useCustomers();
  const { data: professionals } = useProfessionals();
  const { data: services } = useServices();
  const schedule = useScheduleAppointment();

  const [customerId, setCustomerId] = useState("");
  const [professionalId, setProfessionalId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [startAt, setStartAt] = useState("");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    await schedule.mutateAsync({ customerId, professionalId, serviceIds: [serviceId], startAt: new Date(startAt).toISOString() });
    setOpen(false);
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button><Plus className="h-4 w-4 mr-2" />Novo agendamento</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Novo Agendamento</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Cliente</Label>
            <Select value={customerId} onValueChange={setCustomerId}>
              <SelectTrigger><SelectValue placeholder="Selecionar cliente" /></SelectTrigger>
              <SelectContent>
                {customers?.map((c) => <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Profissional</Label>
            <Select value={professionalId} onValueChange={setProfessionalId}>
              <SelectTrigger><SelectValue placeholder="Selecionar profissional" /></SelectTrigger>
              <SelectContent>
                {professionals?.map((p) => <SelectItem key={p.id} value={p.id}>{p.name}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Serviço</Label>
            <Select value={serviceId} onValueChange={setServiceId}>
              <SelectTrigger><SelectValue placeholder="Selecionar serviço" /></SelectTrigger>
              <SelectContent>
                {services?.map((s) => <SelectItem key={s.serviceId} value={s.serviceId}>{s.name} — R$ {s.price.toFixed(2)}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Data e hora</Label>
            <Input type="datetime-local" value={startAt} onChange={(e) => setStartAt(e.target.value)} />
          </div>
          <Button type="submit" className="w-full" disabled={schedule.isPending}>
            {schedule.isPending ? "Agendando..." : "Agendar"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function DashboardPage() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const from = startOfDay(selectedDate);
  const to = endOfDay(selectedDate);
  const { data: appointments, isLoading } = useAppointments(from, to);
  const complete = useCompleteAppointment();
  const cancel = useCancelAppointment();

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Agendamentos</h1>
            <p className="text-muted-foreground">{format(selectedDate, "EEEE, dd 'de' MMMM", { locale: ptBR })}</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" onClick={() => setSelectedDate((d) => addDays(d, -1))}>‹</Button>
              <Input
                type="date"
                className="w-40"
                value={format(selectedDate, "yyyy-MM-dd")}
                onChange={(e) => setSelectedDate(new Date(e.target.value + "T12:00:00"))}
              />
              <Button variant="outline" size="icon" onClick={() => setSelectedDate((d) => addDays(d, 1))}>›</Button>
            </div>
            <NewAppointmentDialog />
          </div>
        </div>

        {isLoading ? (
          <p className="text-muted-foreground">Carregando...</p>
        ) : appointments?.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center py-12 text-muted-foreground">
              <Calendar className="h-12 w-12 mb-4 opacity-30" />
              <p>Nenhum agendamento para este dia.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-3">
            {appointments?.map((appt) => (
              <AppointmentCard
                key={appt.id}
                appt={appt}
                onComplete={(id) => complete.mutate(id)}
                onCancel={(id) => cancel.mutate(id)}
              />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  );
}
