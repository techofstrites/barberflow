"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useCustomers, useCreateCustomer } from "@/hooks/useCustomers";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, Users } from "lucide-react";

const schema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  phone: z.string().regex(/^\+[1-9]\d{7,14}$/, "Use formato E.164 (+5511999999999)"),
  consentGiven: z.boolean().default(false),
});
type FormData = z.infer<typeof schema>;

function NewCustomerDialog() {
  const [open, setOpen] = useState(false);
  const create = useCreateCustomer();
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
        <Button><Plus className="h-4 w-4 mr-2" />Novo cliente</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader><DialogTitle>Novo Cliente</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input {...register("name")} placeholder="João Silva" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="space-y-2">
            <Label>Telefone (E.164)</Label>
            <Input {...register("phone")} placeholder="+5511999999999" />
            {errors.phone && <p className="text-xs text-destructive">{errors.phone.message}</p>}
          </div>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="consent" {...register("consentGiven")} className="h-4 w-4" />
            <Label htmlFor="consent">Consentimento de comunicação</Label>
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : "Criar cliente"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function CustomersPage() {
  const { data: customers, isLoading } = useCustomers();

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Clientes</h1>
            <p className="text-muted-foreground">{customers?.length ?? 0} clientes cadastrados</p>
          </div>
          <NewCustomerDialog />
        </div>

        {isLoading ? (
          <p className="text-muted-foreground">Carregando...</p>
        ) : customers?.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center py-12 text-muted-foreground">
              <Users className="h-12 w-12 mb-4 opacity-30" />
              <p>Nenhum cliente cadastrado.</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {customers?.map((c) => (
              <Card key={c.id}>
                <CardContent className="p-4 space-y-1">
                  <p className="font-medium">{c.name}</p>
                  <p className="text-sm text-muted-foreground">{c.phone}</p>
                  {c.consentGiven && <Badge variant="secondary">Consentimento</Badge>}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  );
}
