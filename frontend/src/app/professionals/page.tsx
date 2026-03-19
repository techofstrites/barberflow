"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import {
  useProfessionals,
  useCreateProfessional,
  useUpdateProfessional,
  useSchedule,
  useUpsertSchedule,
} from "@/hooks/useProfessionals";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, UserCheck, Pencil, Clock } from "lucide-react";
import type { DayOfWeek, Professional, WorkingHours } from "@/types";

// ── Day labels ────────────────────────────────────────────────────────────────
const DAYS: { key: DayOfWeek; label: string; short: string }[] = [
  { key: "MONDAY",    label: "Segunda",  short: "Seg" },
  { key: "TUESDAY",   label: "Terça",    short: "Ter" },
  { key: "WEDNESDAY", label: "Quarta",   short: "Qua" },
  { key: "THURSDAY",  label: "Quinta",   short: "Qui" },
  { key: "FRIDAY",    label: "Sexta",    short: "Sex" },
  { key: "SATURDAY",  label: "Sábado",   short: "Sáb" },
  { key: "SUNDAY",    label: "Domingo",  short: "Dom" },
];

// ── Generate all 30-min slots in a day ────────────────────────────────────────
function generateAllSlots(): string[] {
  const slots: string[] = [];
  for (let h = 6; h < 22; h++) {
    slots.push(`${String(h).padStart(2, "0")}:00`);
    slots.push(`${String(h).padStart(2, "0")}:30`);
  }
  return slots;
}
const ALL_SLOTS = generateAllSlots(); // ["06:00", "06:30", ..., "21:30"]

// ── Convert WorkingHours to selected slots ────────────────────────────────────
function workingHoursToSlots(wh: WorkingHours[]): Record<DayOfWeek, Set<string>> {
  const result: Record<DayOfWeek, Set<string>> = Object.fromEntries(
    DAYS.map(({ key }) => [key, new Set<string>()])
  ) as Record<DayOfWeek, Set<string>>;

  for (const w of wh) {
    for (const slot of ALL_SLOTS) {
      if (slot >= w.startTime && slot < w.endTime) {
        result[w.dayOfWeek].add(slot);
      }
    }
  }
  return result;
}

// ── Convert selected slots back to WorkingHours ───────────────────────────────
function slotsToWorkingHours(slots: Record<DayOfWeek, Set<string>>): WorkingHours[] {
  const result: WorkingHours[] = [];
  for (const { key: day } of DAYS) {
    const daySlots = [...slots[day]].sort();
    if (daySlots.length === 0) continue;

    // Group consecutive slots
    const groups: string[][] = [];
    let current: string[] = [daySlots[0]];
    for (let i = 1; i < daySlots.length; i++) {
      const prev = daySlots[i - 1];
      const curr = daySlots[i];
      const [ph, pm] = prev.split(":").map(Number);
      const [ch, cm] = curr.split(":").map(Number);
      const prevMins = ph * 60 + pm;
      const currMins = ch * 60 + cm;
      if (currMins - prevMins === 30) {
        current.push(curr);
      } else {
        groups.push(current);
        current = [curr];
      }
    }
    groups.push(current);

    for (const group of groups) {
      const startTime = group[0];
      const lastSlot = group[group.length - 1];
      const [lh, lm] = lastSlot.split(":").map(Number);
      const endMins = lh * 60 + lm + 30;
      const endTime = `${String(Math.floor(endMins / 60)).padStart(2, "0")}:${String(endMins % 60).padStart(2, "0")}`;
      result.push({ dayOfWeek: day, startTime, endTime });
    }
  }
  return result;
}

// ── Format working hours for display in card ─────────────────────────────────
function formatScheduleSummary(wh: WorkingHours[]): string {
  const dayMap = Object.fromEntries(DAYS.map(({ key, short }) => [key, short]));
  return wh
    .sort((a, b) => DAYS.findIndex(d => d.key === a.dayOfWeek) - DAYS.findIndex(d => d.key === b.dayOfWeek))
    .map(w => `${dayMap[w.dayOfWeek]} ${w.startTime}–${w.endTime}`)
    .join(", ");
}

// ── Slot grid editor ──────────────────────────────────────────────────────────
type SlotState = Record<DayOfWeek, Set<string>>;

function SlotGridEditor({
  value,
  onChange,
}: {
  value: SlotState;
  onChange: (s: SlotState) => void;
}) {
  function toggleDay(day: DayOfWeek) {
    const newSet = new Set(value[day]);
    if (newSet.size > 0) {
      newSet.clear();
    } else {
      // Enable default 09:00-18:00
      ALL_SLOTS.filter(s => s >= "09:00" && s < "18:00").forEach(s => newSet.add(s));
    }
    onChange({ ...value, [day]: newSet });
  }

  function toggleSlot(day: DayOfWeek, slot: string) {
    const newSet = new Set(value[day]);
    if (newSet.has(slot)) newSet.delete(slot);
    else newSet.add(slot);
    onChange({ ...value, [day]: newSet });
  }

  return (
    <div className="space-y-3">
      {DAYS.map(({ key, label }) => {
        const enabled = value[key].size > 0;
        return (
          <div key={key} className="rounded-lg border px-3 py-2 space-y-2">
            <div className="flex items-center gap-3">
              <input
                type="checkbox"
                id={`day-${key}`}
                checked={enabled}
                onChange={() => toggleDay(key)}
                className="h-4 w-4 accent-primary cursor-pointer"
              />
              <label htmlFor={`day-${key}`} className="text-sm font-medium cursor-pointer select-none flex-1">
                {label}
              </label>
              {enabled && (
                <span className="text-xs text-muted-foreground">
                  {value[key].size} horários
                </span>
              )}
            </div>
            {enabled && (
              <div className="flex flex-wrap gap-1 pl-7">
                {ALL_SLOTS.map((slot) => {
                  const selected = value[key].has(slot);
                  return (
                    <button
                      key={slot}
                      type="button"
                      onClick={() => toggleSlot(key, slot)}
                      className={`text-xs px-2 py-1 rounded border transition-colors ${
                        selected
                          ? "bg-primary text-primary-foreground border-primary"
                          : "bg-background text-muted-foreground border-border hover:bg-accent"
                      }`}
                    >
                      {slot}
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

// ── Professional card with schedule ──────────────────────────────────────────
function ProfessionalCardWithSchedule({ p, onEdit }: { p: Professional; onEdit: () => void }) {
  const { data: schedule } = useSchedule(p.id);
  const summary = schedule?.workingHours.length ? formatScheduleSummary(schedule.workingHours) : null;

  return (
    <Card>
      <CardContent className="p-4 space-y-3">
        <div className="flex items-start justify-between">
          <p className="font-medium">{p.name}</p>
          <Button variant="ghost" size="icon" className="h-7 w-7 -mt-0.5" onClick={onEdit}>
            <Pencil className="h-3.5 w-3.5" />
          </Button>
        </div>
        <div className="flex flex-wrap gap-1">
          {p.specialties.map((s) => (
            <Badge key={s} variant="secondary">{s}</Badge>
          ))}
        </div>
        {summary && (
          <div className="flex items-start gap-1.5 text-xs text-muted-foreground">
            <Clock className="h-3.5 w-3.5 mt-0.5 flex-shrink-0" />
            <span>{summary}</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// ── Form schema ───────────────────────────────────────────────────────────────
const schema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  specialties: z.string(),
});
type FormData = z.infer<typeof schema>;

// ── Dialog (create & edit) ────────────────────────────────────────────────────
function ProfessionalDialog({
  professional,
  open,
  onClose,
}: {
  professional?: Professional;
  open: boolean;
  onClose: () => void;
}) {
  const isEdit = !!professional;
  const create = useCreateProfessional();
  const update = useUpdateProfessional();
  const upsertSchedule = useUpsertSchedule();

  const { data: existingSchedule } = useSchedule(professional?.id ?? null);

  const [slots, setSlots] = useState<SlotState>(() =>
    Object.fromEntries(DAYS.map(({ key }) => [key, new Set<string>()])) as SlotState
  );
  const [slotsInitialized, setSlotsInitialized] = useState(false);

  if (existingSchedule && !slotsInitialized) {
    setSlots(workingHoursToSlots(existingSchedule.workingHours));
    setSlotsInitialized(true);
  }

  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: professional?.name ?? "",
      specialties: professional?.specialties.join(", ") ?? "",
    },
  });

  async function onSubmit(data: FormData) {
    const specialties = data.specialties.split(",").map((s) => s.trim()).filter(Boolean);
    const workingHours = slotsToWorkingHours(slots);

    let professionalId = professional?.id;

    if (isEdit && professionalId) {
      await update.mutateAsync({ id: professionalId, name: data.name, specialties });
    } else {
      const res = await create.mutateAsync({ name: data.name, specialties });
      professionalId = res.data.id;
    }

    if (professionalId) {
      await upsertSchedule.mutateAsync({ professionalId, workingHours });
    }

    reset();
    setSlotsInitialized(false);
    setSlots(Object.fromEntries(DAYS.map(({ key }) => [key, new Set<string>()])) as SlotState);
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={(v) => {
      if (!v) {
        reset();
        setSlotsInitialized(false);
        setSlots(Object.fromEntries(DAYS.map(({ key }) => [key, new Set<string>()])) as SlotState);
        onClose();
      }
    }}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar profissional" : "Novo profissional"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 mt-2">
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>Nome</Label>
              <Input {...register("name")} placeholder="Carlos Barbeiro" />
              {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
            </div>
            <div className="space-y-1.5">
              <Label>Especialidades (separadas por vírgula)</Label>
              <Input {...register("specialties")} placeholder="Corte, Barba, Coloração" />
            </div>
          </div>

          <div className="space-y-2">
            <div className="flex items-center gap-2 text-sm font-medium">
              <Clock className="h-4 w-4" />
              Horários de atendimento
            </div>
            <p className="text-xs text-muted-foreground">Marque os horários disponíveis para cada dia</p>
            <SlotGridEditor value={slots} onChange={setSlots} />
          </div>

          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : isEdit ? "Salvar alterações" : "Criar profissional"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function ProfessionalsPage() {
  const { data: professionals, isLoading } = useProfessionals();
  const [createOpen, setCreateOpen] = useState(false);
  const [editing, setEditing] = useState<Professional | null>(null);

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Profissionais</h1>
            <p className="text-muted-foreground">{professionals?.length ?? 0} profissionais ativos</p>
          </div>
          <Button onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4 mr-2" />Novo profissional
          </Button>
        </div>

        {isLoading ? (
          <p className="text-muted-foreground">Carregando...</p>
        ) : professionals?.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center py-12 text-muted-foreground">
              <UserCheck className="h-12 w-12 mb-4 opacity-30" />
              <p>Nenhum profissional cadastrado.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {professionals?.map((p) => (
              <ProfessionalCardWithSchedule
                key={p.id}
                p={p}
                onEdit={() => setEditing(p)}
              />
            ))}
          </div>
        )}
      </div>

      <ProfessionalDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
      />

      {editing && (
        <ProfessionalDialog
          professional={editing}
          open={!!editing}
          onClose={() => setEditing(null)}
        />
      )}
    </AppShell>
  );
}
