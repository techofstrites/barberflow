"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useServices, useCreateService } from "@/hooks/useServices";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, Scissors } from "lucide-react";

const schema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  price: z.coerce.number().positive("Preço deve ser positivo"),
  durationMinutes: z.coerce.number().int().positive("Duração deve ser positiva"),
});
type FormData = z.infer<typeof schema>;

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
      <DialogTrigger asChild>
        <Button><Plus className="h-4 w-4 mr-2" />Novo serviço</Button>
      </DialogTrigger>
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

export default function ServicesPage() {
  const { data: services, isLoading } = useServices();

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
              <Card key={s.serviceId}>
                <CardContent className="p-4 space-y-1">
                  <p className="font-medium">{s.name}</p>
                  <p className="text-sm text-muted-foreground">R$ {s.price.toFixed(2)} · {s.durationMinutes} min</p>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  );
}
