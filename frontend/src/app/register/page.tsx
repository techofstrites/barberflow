"use client";
import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Zap, Building2, User, CheckCircle2, ArrowLeft, ArrowRight, Eye, EyeOff } from "lucide-react";
import { cn } from "@/lib/utils";

// ─── Schemas ────────────────────────────────────────────────────────────────

const step1Schema = z.object({
  name: z.string().min(3, "Nome deve ter ao menos 3 caracteres"),
  slug: z
    .string()
    .min(3, "Identificador deve ter ao menos 3 caracteres")
    .max(50, "Máximo 50 caracteres")
    .regex(/^[a-z0-9-]+$/, "Use apenas letras minúsculas, números e hífens"),
});

const step2Schema = z
  .object({
    adminEmail: z.string().email("Email inválido"),
    adminPassword: z.string().min(8, "Mínimo 8 caracteres"),
    confirmPassword: z.string(),
  })
  .refine((d) => d.adminPassword === d.confirmPassword, {
    message: "As senhas não coincidem",
    path: ["confirmPassword"],
  });

type Step1Data = z.infer<typeof step1Schema>;
type Step2Data = z.infer<typeof step2Schema>;

// ─── Step indicator ──────────────────────────────────────────────────────────

function StepIndicator({ current }: { current: number }) {
  const steps = [
    { n: 1, label: "Barbearia", icon: Building2 },
    { n: 2, label: "Conta admin", icon: User },
    { n: 3, label: "Concluído", icon: CheckCircle2 },
  ];

  return (
    <div className="flex items-center justify-center gap-0">
      {steps.map(({ n, label, icon: Icon }, idx) => (
        <div key={n} className="flex items-center">
          <div className="flex flex-col items-center gap-1.5">
            <div
              className={cn(
                "flex h-10 w-10 items-center justify-center rounded-full border-2 transition-colors",
                current === n
                  ? "border-primary bg-primary text-primary-foreground"
                  : current > n
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-muted bg-muted text-muted-foreground"
              )}
            >
              {current > n ? (
                <CheckCircle2 className="h-5 w-5" />
              ) : (
                <Icon className="h-4 w-4" />
              )}
            </div>
            <span
              className={cn(
                "text-xs font-medium",
                current >= n ? "text-foreground" : "text-muted-foreground"
              )}
            >
              {label}
            </span>
          </div>
          {idx < steps.length - 1 && (
            <div
              className={cn(
                "mx-3 mb-5 h-[2px] w-16 transition-colors",
                current > n ? "bg-primary" : "bg-muted"
              )}
            />
          )}
        </div>
      ))}
    </div>
  );
}

// ─── Step 1 — Dados da barbearia ─────────────────────────────────────────────

function Step1({ onNext }: { onNext: (data: Step1Data) => void }) {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<Step1Data>({ resolver: zodResolver(step1Schema) });

  const name = watch("name", "");

  function handleNameChange(e: React.ChangeEvent<HTMLInputElement>) {
    const raw = e.target.value;
    setValue("name", raw);
    const slug = raw
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/[^a-z0-9\s-]/g, "")
      .trim()
      .replace(/\s+/g, "-");
    setValue("slug", slug);
  }

  return (
    <form onSubmit={handleSubmit(onNext)} className="space-y-5">
      <div className="space-y-2">
        <Label htmlFor="name">Nome da barbearia</Label>
        <Input
          id="name"
          placeholder="Ex: Barbearia Silva"
          {...register("name")}
          onChange={handleNameChange}
        />
        {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="slug">
          Identificador único{" "}
          <span className="text-xs text-muted-foreground">(URL da sua conta)</span>
        </Label>
        <div className="flex items-center overflow-hidden rounded-md border border-input bg-muted/50 focus-within:border-ring focus-within:ring-2 focus-within:ring-ring focus-within:ring-offset-2">
          <span className="select-none px-3 text-sm text-muted-foreground">barberflow.app/</span>
          <input
            id="slug"
            className="flex-1 bg-transparent py-2 pr-3 text-sm outline-none placeholder:text-muted-foreground"
            placeholder="barbearia-silva"
            {...register("slug")}
          />
        </div>
        {errors.slug && <p className="text-xs text-destructive">{errors.slug.message}</p>}
        <p className="text-xs text-muted-foreground">
          Apenas letras minúsculas, números e hífens. Não pode ser alterado depois.
        </p>
      </div>

      <Button type="submit" className="w-full">
        Continuar <ArrowRight className="ml-2 h-4 w-4" />
      </Button>
    </form>
  );
}

// ─── Step 2 — Conta do administrador ─────────────────────────────────────────

function Step2({
  onNext,
  onBack,
  isLoading,
  error,
}: {
  onNext: (data: Step2Data) => void;
  onBack: () => void;
  isLoading: boolean;
  error: string;
}) {
  const [showPwd, setShowPwd] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Step2Data>({ resolver: zodResolver(step2Schema) });

  return (
    <form onSubmit={handleSubmit(onNext)} className="space-y-5">
      <div className="space-y-2">
        <Label htmlFor="adminEmail">Email do administrador</Label>
        <Input id="adminEmail" type="email" placeholder="admin@minhabarbearia.com" {...register("adminEmail")} />
        {errors.adminEmail && <p className="text-xs text-destructive">{errors.adminEmail.message}</p>}
      </div>

      <div className="space-y-2">
        <Label htmlFor="adminPassword">Senha</Label>
        <div className="relative">
          <Input
            id="adminPassword"
            type={showPwd ? "text" : "password"}
            placeholder="Mínimo 8 caracteres"
            className="pr-10"
            {...register("adminPassword")}
          />
          <button
            type="button"
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            onClick={() => setShowPwd((v) => !v)}
            tabIndex={-1}
          >
            {showPwd ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.adminPassword && (
          <p className="text-xs text-destructive">{errors.adminPassword.message}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="confirmPassword">Confirmar senha</Label>
        <div className="relative">
          <Input
            id="confirmPassword"
            type={showConfirm ? "text" : "password"}
            placeholder="Repita a senha"
            className="pr-10"
            {...register("confirmPassword")}
          />
          <button
            type="button"
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            onClick={() => setShowConfirm((v) => !v)}
            tabIndex={-1}
          >
            {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
        )}
      </div>

      {error && (
        <div className="rounded-md border border-destructive/50 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      )}

      <div className="flex gap-3">
        <Button type="button" variant="outline" className="flex-1" onClick={onBack}>
          <ArrowLeft className="mr-2 h-4 w-4" /> Voltar
        </Button>
        <Button type="submit" className="flex-1" disabled={isLoading}>
          {isLoading ? "Criando conta..." : "Criar barbearia"}
        </Button>
      </div>
    </form>
  );
}

// ─── Step 3 — Sucesso ─────────────────────────────────────────────────────────

function Step3({ tenantId, email }: { tenantId: string; email: string }) {
  return (
    <div className="space-y-6 text-center">
      <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-green-100">
        <CheckCircle2 className="h-10 w-10 text-green-600" />
      </div>
      <div className="space-y-2">
        <h3 className="text-xl font-bold">Barbearia criada com sucesso!</h3>
        <p className="text-sm text-muted-foreground">
          Sua conta está pronta. Use os dados abaixo para fazer login.
        </p>
      </div>

      <div className="rounded-xl border bg-muted/40 px-6 py-4 text-left space-y-3">
        <div>
          <p className="text-xs text-muted-foreground">Email</p>
          <p className="text-sm font-medium">{email}</p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground">Tenant ID</p>
          <p className="font-mono text-xs break-all text-foreground">{tenantId}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            Guarde este ID — você precisará dele para fazer login.
          </p>
        </div>
      </div>

      <div className="space-y-3">
        <Button className="w-full" asChild>
          <Link href="/login">Ir para o login</Link>
        </Button>
        <p className="text-xs text-muted-foreground">
          Seu período de trial de 14 dias começa agora.
        </p>
      </div>
    </div>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function RegisterPage() {
  const [step, setStep] = useState(1);
  const [step1Data, setStep1Data] = useState<Step1Data | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState("");
  const [result, setResult] = useState<{ tenantId: string; email: string } | null>(null);

  async function handleStep2(step2Data: Step2Data) {
    if (!step1Data) return;
    setIsLoading(true);
    setApiError("");
    try {
      const response = await api.post("/api/v1/tenants", {
        slug: step1Data.slug,
        name: step1Data.name,
        adminEmail: step2Data.adminEmail,
        adminPassword: step2Data.adminPassword,
      });
      setResult({ tenantId: response.data.tenantId, email: step2Data.adminEmail });
      setStep(3);
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { detail?: string } } })?.response?.data?.detail ??
        "Erro ao criar barbearia. Verifique se o identificador já está em uso.";
      setApiError(message);
    } finally {
      setIsLoading(false);
    }
  }

  const stepTitles: Record<number, string> = {
    1: "Dados da barbearia",
    2: "Conta do administrador",
    3: "Tudo pronto!",
  };

  const stepDescriptions: Record<number, string> = {
    1: "Como sua barbearia será identificada no sistema.",
    2: "Crie as credenciais de acesso ao painel.",
    3: "",
  };

  return (
    <div className="flex min-h-screen bg-muted/40">
      {/* Sidebar ilustrativa (desktop) */}
      <div className="hidden flex-col justify-between bg-primary p-10 text-primary-foreground lg:flex lg:w-[420px]">
        <div>
          <Link href="/" className="flex items-center gap-2">
            <Zap className="h-6 w-6" />
            <span className="text-xl font-bold">BarberFlow</span>
          </Link>
        </div>
        <div className="space-y-8">
          <div>
            <h2 className="text-3xl font-extrabold leading-tight">
              Comece em minutos,<br />cresça pra sempre.
            </h2>
            <p className="mt-4 text-primary-foreground/70 text-sm leading-relaxed">
              Configure sua barbearia agora e tenha o chatbot do WhatsApp funcionando ainda hoje.
            </p>
          </div>
          <ul className="space-y-3 text-sm">
            {[
              "14 dias grátis, sem cartão",
              "Chatbot no WhatsApp incluso",
              "IA de retenção de clientes",
              "Suporte em português",
            ].map((item) => (
              <li key={item} className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 shrink-0" />
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </div>
        <p className="text-xs text-primary-foreground/50">
          © {new Date().getFullYear()} BarberFlow
        </p>
      </div>

      {/* Form area */}
      <div className="flex flex-1 items-center justify-center p-6">
        <div className="w-full max-w-md space-y-8">
          {/* Mobile logo */}
          <div className="flex items-center justify-center gap-2 lg:hidden">
            <Zap className="h-6 w-6 text-primary" />
            <span className="text-xl font-bold">BarberFlow</span>
          </div>

          <StepIndicator current={step} />

          <Card>
            <CardHeader className="pb-4">
              {step < 3 && (
                <>
                  <CardTitle className="text-xl">{stepTitles[step]}</CardTitle>
                  <p className="text-sm text-muted-foreground">{stepDescriptions[step]}</p>
                </>
              )}
            </CardHeader>
            <CardContent>
              {step === 1 && (
                <Step1
                  onNext={(data) => {
                    setStep1Data(data);
                    setStep(2);
                  }}
                />
              )}
              {step === 2 && (
                <Step2
                  onNext={handleStep2}
                  onBack={() => setStep(1)}
                  isLoading={isLoading}
                  error={apiError}
                />
              )}
              {step === 3 && result && (
                <Step3 tenantId={result.tenantId} email={result.email} />
              )}
            </CardContent>
          </Card>

          {step < 3 && (
            <p className="text-center text-sm text-muted-foreground">
              Já tem uma conta?{" "}
              <Link href="/login" className="font-medium text-primary hover:underline">
                Fazer login
              </Link>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
