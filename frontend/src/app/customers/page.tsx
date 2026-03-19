"use client";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { useCustomers, useCreateCustomer, useUpdateCustomer, useDeleteCustomer } from "@/hooks/useCustomers";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Plus, Users, Pencil, Trash2 } from "lucide-react";
import type { Customer } from "@/types";

// ── Create schema ─────────────────────────────────────────────────────────────
const createSchema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  phone: z.string().regex(/^\+[1-9]\d{7,14}$/, "Use formato E.164 (+5511999999999)"),
  consentGiven: z.boolean().default(false),
});
type CreateFormData = z.infer<typeof createSchema>;

// ── Edit schema ───────────────────────────────────────────────────────────────
const editSchema = z.object({
  name: z.string().min(1, "Nome obrigatório"),
  consentGiven: z.boolean().default(false),
});
type EditFormData = z.infer<typeof editSchema>;

// ── New customer dialog ───────────────────────────────────────────────────────
function NewCustomerDialog() {
  const [open, setOpen] = useState(false);
  const create = useCreateCustomer();
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<CreateFormData>({
    resolver: zodResolver(createSchema),
  });

  async function onSubmit(data: CreateFormData) {
    await create.mutateAsync(data);
    reset();
    setOpen(false);
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4 mr-2" />Novo cliente</Button>
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

// ── Edit customer dialog ──────────────────────────────────────────────────────
function EditCustomerDialog({ customer, onClose }: { customer: Customer; onClose: () => void }) {
  const update = useUpdateCustomer();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<EditFormData>({
    resolver: zodResolver(editSchema),
    defaultValues: { name: customer.name, consentGiven: customer.consentGiven },
  });

  async function onSubmit(data: EditFormData) {
    await update.mutateAsync({ id: customer.id, ...data });
    onClose();
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent>
        <DialogHeader><DialogTitle>Editar Cliente</DialogTitle></DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
          <div className="space-y-2">
            <Label>Telefone</Label>
            <Input value={customer.phone} disabled className="bg-muted" />
          </div>
          <div className="space-y-2">
            <Label>Nome</Label>
            <Input {...register("name")} placeholder="João Silva" />
            {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
          </div>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="edit-consent" {...register("consentGiven")} className="h-4 w-4" />
            <Label htmlFor="edit-consent">Consentimento de comunicação</Label>
          </div>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? "Salvando..." : "Salvar alterações"}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ── Customer card ─────────────────────────────────────────────────────────────
function CustomerCard({ c, onEdit, onDelete }: { c: Customer; onEdit: () => void; onDelete: () => void }) {
  return (
    <Card>
      <CardContent className="p-4 space-y-1">
        <div className="flex items-start justify-between">
          <p className="font-medium">{c.name}</p>
          <div className="flex gap-1 -mt-0.5">
            <Button variant="ghost" size="icon" className="h-7 w-7" onClick={onEdit}>
              <Pencil className="h-3.5 w-3.5" />
            </Button>
            <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive hover:text-destructive" onClick={onDelete}>
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
        <p className="text-sm text-muted-foreground">{c.phone}</p>
        {c.consentGiven && <Badge variant="secondary">Consentimento</Badge>}
      </CardContent>
    </Card>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────
export default function CustomersPage() {
  const { data: customers, isLoading } = useCustomers();
  const deleteCustomer = useDeleteCustomer();
  const [editing, setEditing] = useState<Customer | null>(null);

  async function handleDelete(c: Customer) {
    if (!confirm(`Excluir cliente "${c.name}"? Esta ação não pode ser desfeita.`)) return;
    await deleteCustomer.mutateAsync(c.id);
  }

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
              <CustomerCard
                key={c.id}
                c={c}
                onEdit={() => setEditing(c)}
                onDelete={() => handleDelete(c)}
              />
            ))}
          </div>
        )}
      </div>

      {editing && (
        <EditCustomerDialog customer={editing} onClose={() => setEditing(null)} />
      )}
    </AppShell>
  );
}
