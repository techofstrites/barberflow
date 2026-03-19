"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import {
  useAppointmentDetails,
  useCancelAppointment,
  useConfirmAppointment,
  useCompleteAppointment,
  useScheduleAppointment,
  useAvailableSlots,
  useDashboardStats,
} from "@/hooks/useAppointments";
import { useCustomers } from "@/hooks/useCustomers";
import { useProfessionals } from "@/hooks/useProfessionals";
import { useServices } from "@/hooks/useServices";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { format, startOfDay, endOfDay, addDays } from "date-fns";
import { ptBR } from "date-fns/locale";
import { Plus, ChevronLeft, ChevronRight, CheckCircle, XCircle, Calendar, ThumbsUp } from "lucide-react";
import type { AppointmentDetail } from "@/types";

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatPhone(phone: string): string {
  const d = phone.replace(/\D/g, "");
  if (d.length === 13 && d.startsWith("55"))
    return `(${d.slice(2, 4)}) ${d.slice(4, 5)} ${d.slice(5, 9)}-${d.slice(9, 13)}`;
  if (d.length === 11)
    return `(${d.slice(0, 2)}) ${d.slice(2, 3)} ${d.slice(3, 7)}-${d.slice(7)}`;
  return phone;
}

function extractTime(dateStr: string) {
  // backend returns "dd/MM/yyyy HH:mm"
  return dateStr.slice(11, 16);
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL", maximumFractionDigits: 0 }).format(value);
}

// ── Status badge ──────────────────────────────────────────────────────────────

const STATUS_CONFIG: Record<string, { label: string; className: string }> = {
  PENDING_CONFIRMATION: { label: "Aguardando",      className: "bg-amber-400 text-white" },
  CONFIRMED:            { label: "Confirmado",       className: "bg-green-500 text-white" },
  IN_PROGRESS:          { label: "Em andamento",     className: "bg-blue-500 text-white" },
  COMPLETED:            { label: "Concluído",        className: "bg-slate-400 text-white" },
  CANCELLED:            { label: "Cancelado",        className: "bg-red-400 text-white" },
  NO_SHOW:              { label: "Não compareceu",   className: "bg-red-300 text-white" },
};

function StatusBadge({ status }: { status: string }) {
  const cfg = STATUS_CONFIG[status] ?? { label: status, className: "bg-slate-300 text-white" };
  return (
    <span className={`inline-flex items-center px-3 py-0.5 rounded-full text-xs font-medium ${cfg.className}`}>
      {cfg.label}
    </span>
  );
}

// ── Stat card ─────────────────────────────────────────────────────────────────

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <Card>
      <CardContent className="p-5">
        <p className="text-sm text-muted-foreground mb-1">{label}</p>
        <p className="text-3xl font-bold tracking-tight">{value}</p>
      </CardContent>
    </Card>
  );
}

// ── Appointment row ───────────────────────────────────────────────────────────

function AppointmentRow({ appt, onConfirm, onComplete, onCancel }: {
  appt: AppointmentDetail;
  onConfirm: (id: string) => void;
  onComplete: (id: string) => void;
  onCancel: (id: string) => void;
}) {
  const isDone = ["COMPLETED", "CANCELLED", "NO_SHOW"].includes(appt.status);
  const canConfirm = appt.status === "PENDING_CONFIRMATION";
  const canComplete = appt.status === "CONFIRMED" || appt.status === "IN_PROGRESS";
  const canCancel = !isDone;

  return (
    <div className="grid grid-cols-[72px_1fr_1fr_auto] items-center gap-4 px-5 py-4 border-b last:border-0 hover:bg-muted/30 transition-colors group">
      <span className="text-sm text-muted-foreground font-mono tabular-nums">
        {extractTime(appt.startAt)}
      </span>
      <span className="font-semibold text-sm">{appt.customerName}</span>
      <span className="text-sm text-muted-foreground">{appt.services.join(" + ")}</span>
      <div className="flex items-center gap-2">
        <StatusBadge status={appt.status} />
        <div className="hidden group-hover:flex items-center gap-1">
          {canConfirm && (
            <button
              onClick={() => onConfirm(appt.id)}
              className="p-1 rounded text-muted-foreground hover:text-blue-500 transition-colors"
              title="Confirmar"
            >
              <ThumbsUp className="h-4 w-4" />
            </button>
          )}
          {canComplete && (
            <button
              onClick={() => onComplete(appt.id)}
              className="p-1 rounded text-muted-foreground hover:text-green-600 transition-colors"
              title="Concluir"
            >
              <CheckCircle className="h-4 w-4" />
            </button>
          )}
          {canCancel && (
            <button
              onClick={() => onCancel(appt.id)}
              className="p-1 rounded text-muted-foreground hover:text-red-500 transition-colors"
              title="Cancelar"
            >
              <XCircle className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

// ── New appointment dialog ────────────────────────────────────────────────────

function NewAppointmentDialog() {
  const [open, setOpen] = useState(false);
  const { data: customers } = useCustomers();
  const { data: professionals } = useProfessionals();
  const { data: services } = useServices();
  const schedule = useScheduleAppointment();

  const [customerId, setCustomerId] = useState("");
  const [professionalId, setProfessionalId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedSlot, setSelectedSlot] = useState("");

  const { data: availableSlots, isLoading: loadingSlots } = useAvailableSlots(
    professionalId || null,
    selectedDate || null
  );

  function formatSlotLabel(iso: string) {
    try {
      const d = new Date(iso);
      return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
    } catch { return iso; }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedSlot) return;
    await schedule.mutateAsync({ customerId, professionalId, serviceIds: [serviceId], startAt: selectedSlot });
    setOpen(false);
    setCustomerId(""); setProfessionalId(""); setServiceId(""); setSelectedDate(""); setSelectedSlot("");
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button><Plus className="h-4 w-4 mr-2" />Novo agendamento</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Novo Agendamento</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Cliente</Label>
            <Select value={customerId} onValueChange={setCustomerId}>
              <SelectTrigger><SelectValue placeholder="Selecionar cliente" /></SelectTrigger>
              <SelectContent>
                {customers?.map((c) => (
                  <SelectItem key={c.id} value={c.id}>{c.name} — {formatPhone(c.phone)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Profissional</Label>
            <Select value={professionalId} onValueChange={(v) => { setProfessionalId(v); setSelectedSlot(""); }}>
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
                {services?.map((s) => (
                  <SelectItem key={s.serviceId} value={s.serviceId}>{s.name} — R$ {s.price.toFixed(2)}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Data</Label>
            <Input type="date" value={selectedDate} onChange={(e) => { setSelectedDate(e.target.value); setSelectedSlot(""); }} min={format(new Date(), "yyyy-MM-dd")} />
          </div>
          {professionalId && selectedDate && (
            <div className="space-y-2">
              <Label>Horário disponível</Label>
              {loadingSlots ? (
                <p className="text-sm text-muted-foreground">Carregando horários...</p>
              ) : !availableSlots?.length ? (
                <p className="text-sm text-muted-foreground">Nenhum horário disponível nesta data.</p>
              ) : (
                <Select value={selectedSlot} onValueChange={setSelectedSlot}>
                  <SelectTrigger><SelectValue placeholder="Selecionar horário" /></SelectTrigger>
                  <SelectContent>
                    {availableSlots.map((slot) => (
                      <SelectItem key={slot} value={slot}>{formatSlotLabel(slot)}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            </div>
          )}
          <Button
            type="submit"
            className="w-full"
            disabled={schedule.isPending || !customerId || !professionalId || !serviceId || !selectedSlot}
          >
            {schedule.isPending ? "Agendando..." : "Agendar"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function DashboardPage() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const from = startOfDay(selectedDate);
  const to = endOfDay(selectedDate);

  const { data: appointments, isLoading } = useAppointmentDetails(from, to);
  const { data: stats } = useDashboardStats();
  const confirm = useConfirmAppointment();
  const complete = useCompleteAppointment();
  const cancel = useCancelAppointment();

  const isToday = format(selectedDate, "yyyy-MM-dd") === format(new Date(), "yyyy-MM-dd");

  return (
    <AppShell>
      <div className="space-y-6">

        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Dashboard</h1>
            <p className="text-muted-foreground text-sm capitalize">
              {format(selectedDate, "EEEE, dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
            </p>
          </div>
          <NewAppointmentDialog />
        </div>

        {/* KPI cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            label="Agendamentos hoje"
            value={isToday ? String(appointments?.length ?? stats?.appointmentsToday ?? 0) : String(appointments?.length ?? 0)}
          />
          <StatCard
            label="Receita do mês"
            value={stats ? formatCurrency(stats.revenueThisMonth) : "—"}
          />
          <StatCard
            label="Clientes ativos"
            value={stats ? String(stats.activeCustomers) : "—"}
          />
          <StatCard
            label="Taxa de retorno"
            value={stats ? `${stats.returnRate}%` : "—"}
          />
        </div>

        {/* Date navigation + appointment list */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-semibold">Agendamentos do dia</h2>
            <div className="flex items-center gap-1">
              <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setSelectedDate((d) => addDays(d, -1))}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Input
                type="date"
                className="h-8 w-36 text-sm"
                value={format(selectedDate, "yyyy-MM-dd")}
                onChange={(e) => setSelectedDate(new Date(e.target.value + "T12:00:00"))}
              />
              <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setSelectedDate((d) => addDays(d, 1))}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {isLoading ? (
            <div className="rounded-xl border bg-card p-8 text-center text-muted-foreground text-sm">
              Carregando...
            </div>
          ) : !appointments?.length ? (
            <div className="rounded-xl border bg-card flex flex-col items-center py-12 text-muted-foreground">
              <Calendar className="h-10 w-10 mb-3 opacity-25" />
              <p className="text-sm">Nenhum agendamento para este dia.</p>
            </div>
          ) : (
            <div className="rounded-xl border bg-card overflow-hidden">
              {appointments.map((appt) => (
                <AppointmentRow
                  key={appt.id}
                  appt={appt}
                  onConfirm={(id) => confirm.mutate(id)}
                  onComplete={(id) => complete.mutate(id)}
                  onCancel={(id) => cancel.mutate(id)}
                />
              ))}
            </div>
          )}
        </div>

      </div>
    </AppShell>
  );
}
