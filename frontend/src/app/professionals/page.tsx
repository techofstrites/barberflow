"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useProfessionals, useCreateProfessional } from "@/hooks/useProfessionals";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, UserCheck } from "lucide-react";

const schema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  specialties: z.string(),
});
type FormData = z.infer<typeof schema>;

function NewProfessionalDialog() {
  const [open, setOpen] = useState(false);
  const create = useCreateProfessional();
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: FormData) {
    const specialties = data.specialties.split(",").map((s) => s.trim()).filter(Boolean);
    await create.mutateAsync({ name: data.name, specialties });
    reset();
    setOpen(false);
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button><Plus className="h-4 w-4 mr-2" />Novo profissional</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Novo Profissional</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input {...register("name")} placeholder="Carlos Barbeiro" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Especialidades (separadas por vírgula)</Label>
            <Input {...register("specialties")} placeholder="Corte, Barba, Coloração" />
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : "Criar profissional"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function ProfessionalsPage() {
  const { data: professionals, isLoading } = useProfessionals();

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Profissionais</h1>
            <p className="text-muted-foreground">{professionals?.length ?? 0} profissionais ativos</p>
          </div>
          <NewProfessionalDialog />
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
                <CardContent className="p-4 space-y-2">
                  <p className="font-medium">{p.name}</p>
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
    </AppShell>
  );
}
