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

// ── Time options ─────────────────────────────────────────────────────────────
function generateTimes(): string[] {
  const times: string[] = [];
  for (let h = 6; h <= 22; h++) {
    times.push(`${String(h).padStart(2, "0")}:00`);
    if (h < 22) times.push(`${String(h).padStart(2, "0")}:30`);
  }
  return times;
}
const TIME_OPTIONS = generateTimes();

// ── Day labels ────────────────────────────────────────────────────────────────
const DAYS: { key: DayOfWeek; label: string }[] = [
  { key: "MONDAY",    label: "Segunda" },
  { key: "TUESDAY",   label: "Terça" },
  { key: "WEDNESDAY", label: "Quarta" },
  { key: "THURSDAY",  label: "Quinta" },
  { key: "FRIDAY",    label: "Sexta" },
  { key: "SATURDAY",  label: "Sábado" },
  { key: "SUNDAY",    label: "Domingo" },
];

// ── Schedule editor ───────────────────────────────────────────────────────────
type DayConfig = { enabled: boolean; startTime: string; endTime: string };
type ScheduleState = Record<DayOfWeek, DayConfig>;

function buildInitialSchedule(workingHours: WorkingHours[]): ScheduleState {
  const defaults: ScheduleState = Object.fromEntries(
    DAYS.map(({ key }) => [key, { enabled: false, startTime: "09:00", endTime: "18:00" }])
  ) as ScheduleState;

  for (const wh of workingHours) {
    defaults[wh.dayOfWeek] = { enabled: true, startTime: wh.startTime, endTime: wh.endTime };
  }
  return defaults;
}

function ScheduleEditor({
  value,
  onChange,
}: {
  value: ScheduleState;
  onChange: (s: ScheduleState) => void;
}) {
  function toggle(day: DayOfWeek) {
    onChange({ ...value, [day]: { ...value[day], enabled: !value[day].enabled } });
  }
  function setTime(day: DayOfWeek, field: "startTime" | "endTime", time: string) {
    onChange({ ...value, [day]: { ...value[day], [field]: time } });
  }

  return (
    <div className="space-y-2">
      {DAYS.map(({ key, label }) => {
        const cfg = value[key];
        return (
          <div key={key} className="flex items-center gap-3 rounded-lg border px-3 py-2">
            <input
              type="checkbox"
              id={`day-${key}`}
              checked={cfg.enabled}
              onChange={() => toggle(key)}
              className="h-4 w-4 accent-primary cursor-pointer"
            />
            <label htmlFor={`day-${key}`} className="w-20 text-sm font-medium cursor-pointer select-none">
              {label}
            </label>

            {cfg.enabled ? (
              <div className="flex items-center gap-2 flex-1">
                <select
                  value={cfg.startTime}
                  onChange={(e) => setTime(key, "startTime", e.target.value)}
                  className="flex h-8 rounded-md border border-input bg-background px-2 text-sm focus:outline-none focus:ring-1 focus:ring-ring"
                >
                  {TIME_OPTIONS.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
                <span className="text-muted-foreground text-sm">até</span>
                <select
                  value={cfg.endTime}
                  onChange={(e) => setTime(key, "endTime", e.target.value)}
                  className="flex h-8 rounded-md border border-input bg-background px-2 text-sm focus:outline-none focus:ring-1 focus:ring-ring"
                >
                  {TIME_OPTIONS.filter((t) => t > cfg.startTime).map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>
            ) : (
              <span className="text-sm text-muted-foreground flex-1">Não atende</span>
            )}
          </div>
        );
      })}
    </div>
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

  // For edit mode, load existing schedule
  const { data: existingSchedule } = useSchedule(professional?.id ?? null);

  const [schedule, setSchedule] = useState<ScheduleState>(() =>
    buildInitialSchedule(existingSchedule?.workingHours ?? [])
  );

  // Re-init schedule state when existing schedule loads
  const [scheduleInitialized, setScheduleInitialized] = useState(false);
  if (existingSchedule && !scheduleInitialized) {
    setSchedule(buildInitialSchedule(existingSchedule.workingHours));
    setScheduleInitialized(true);
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
    const workingHours: WorkingHours[] = DAYS
      .filter(({ key }) => schedule[key].enabled)
      .map(({ key }) => ({
        dayOfWeek: key,
        startTime: schedule[key].startTime,
        endTime: schedule[key].endTime,
      }));

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
    setScheduleInitialized(false);
    onClose();
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) { reset(); setScheduleInitialized(false); onClose(); } }}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Editar profissional" : "Novo profissional"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 mt-2">
          {/* Basic info */}
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

          {/* Schedule */}
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-sm font-medium">
              <Clock className="h-4 w-4" />
              Dias e horários de atendimento
            </div>
            <ScheduleEditor value={schedule} onChange={setSchedule} />
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
              <Card key={p.id}>
                <CardContent className="p-4 space-y-3">
                  <div className="flex items-start justify-between">
                    <p className="font-medium">{p.name}</p>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7 -mt-0.5"
                      onClick={() => setEditing(p)}
                    >
                      <Pencil className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {p.specialties.map((s) => (
                      <Badge key={s} variant="secondary">{s}</Badge>
                    ))}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Create dialog */}
      <ProfessionalDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
      />

      {/* Edit dialog */}
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
