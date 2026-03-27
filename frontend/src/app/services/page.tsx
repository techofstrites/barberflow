"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useServices, useCreateService, useUpdateService, useDeleteService } from "@/hooks/useServices";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, Scissors, Pencil, Trash2 } from "lucide-react";
import type { Service } from "@/types";

const schema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  price: z.coerce.number().positive("Preço deve ser positivo"),
  durationMinutes: z.coerce.number().int().positive("Duração deve ser positiva"),
});
type FormData = z.infer<typeof schema>;

// ── New service dialog ────────────────────────────────────────────────────────
function NewServiceDialog() {
  const [open, setOpen] = useState(false);
  const create = useCreateService();
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: FormData) {
    await create.mutateAsync(data);
    reset();
    setOpen(false);
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4 mr-2" />Novo serviço</Button>
      <DialogContent>
        <DialogHeader><DialogTitle>Novo Serviço</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input {...register("name")} placeholder="Corte masculino" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Preço (R$)</Label>
            <Input {...register("price")} type="number" step="0.01" placeholder="35.00" />
            {errors.price && <p className="text-xs text-destructive">{errors.price.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Duração (minutos)</Label>
            <Input {...register("durationMinutes")} type="number" placeholder="30" />
            {errors.durationMinutes && <p className="text-xs text-destructive">{errors.durationMinutes.message}</p>}
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : "Criar serviço"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ── Edit service dialog ───────────────────────────────────────────────────────
function EditServiceDialog({ service, onClose }: { service: Service; onClose: () => void }) {
  const update = useUpdateService();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { name: service.name, price: service.price, durationMinutes: service.durationMinutes },
  });

  async function onSubmit(data: FormData) {
    await update.mutateAsync({ serviceId: service.serviceId, ...data });
    onClose();
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent>
        <DialogHeader><DialogTitle>Editar Serviço</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input {...register("name")} placeholder="Corte masculino" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Preço (R$)</Label>
            <Input {...register("price")} type="number" step="0.01" placeholder="35.00" />
            {errors.price && <p className="text-xs text-destructive">{errors.price.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Duração (minutos)</Label>
            <Input {...register("durationMinutes")} type="number" placeholder="30" />
            {errors.durationMinutes && <p className="text-xs text-destructive">{errors.durationMinutes.message}</p>}
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : "Salvar alterações"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ── Service card ──────────────────────────────────────────────────────────────
function ServiceCard({ s, onEdit, onDelete }: { s: Service; onEdit: () => void; onDelete: () => void }) {
  return (
    <Card>
      <CardContent className="p-4 space-y-1">
        <div className="flex items-start justify-between">
          <p className="font-medium">{s.name}</p>
          <div className="flex gap-1 -mt-0.5">
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={onEdit}>
              <Pencil className="h-3.5 w-3.5" />
            </Button>
            <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive hover:text-destructive" onClick={onDelete}>
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
        <p className="text-sm text-muted-foreground">R$ {s.price.toFixed(2)} · {s.durationMinutes} min</p>
      </CardContent>
    </Card>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function ServicesPage() {
  const { data: services, isLoading } = useServices();
  const deleteService = useDeleteService();
  const [editing, setEditing] = useState<Service | null>(null);

  async function handleDelete(s: Service) {
    if (!confirm(`Excluir serviço "${s.name}"? Esta ação não pode ser desfeita.`)) return;
    await deleteService.mutateAsync(s.serviceId);
  }

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Serviços</h1>
            <p className="text-muted-foreground">{services?.length ?? 0} serviços cadastrados</p>
          </div>
          <NewServiceDialog />
        </div>

        {isLoading ? (
          <p className="text-muted-foreground">Carregando...</p>
        ) : services?.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center py-12 text-muted-foreground">
              <Scissors className="h-12 w-12 mb-4 opacity-30" />
              <p>Nenhum serviço cadastrado.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {services?.map((s) => (
              <ServiceCard
                key={s.serviceId}
                s={s}
                onEdit={() => setEditing(s)}
                onDelete={() => handleDelete(s)}
              />
            ))}
          </div>
        )}
      </div>

      {editing && (
        <EditServiceDialog service={editing} onClose={() => setEditing(null)} />
      )}
    </AppShell>
  );
}
