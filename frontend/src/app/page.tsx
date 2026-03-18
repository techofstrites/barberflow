"use client";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Zap,
  MessageCircle,
  CalendarCheck,
  TrendingUp,
  Bot,
  CheckCircle,
  ArrowRight,
  Star,
  Scissors,
  Clock,
  Shield,
} from "lucide-react";

const features = [
  {
    icon: MessageCircle,
    title: "Chatbot no WhatsApp",
    description:
      "Seus clientes agendam pelo WhatsApp 24h por dia sem você precisar responder manualmente. O bot entende linguagem natural.",
  },
  {
    icon: Bot,
    title: "IA de Retenção",
    description:
      "O sistema identifica clientes que estão sumindo e dispara mensagens proativas no momento certo para trazê-los de volta.",
  },
  {
    icon: CalendarCheck,
    title: "Agenda Inteligente",
    description:
      "Gerencie toda a agenda dos seus profissionais em um painel simples. Sem conflito de horários, sem bagunça.",
  },
  {
    icon: TrendingUp,
    title: "Métricas de Negócio",
    description:
      "Visualize faturamento, taxa de no-show, clientes recorrentes e muito mais em dashboards claros e objetivos.",
  },
];

const steps = [
  {
    step: "01",
    title: "Cadastre sua barbearia",
    description: "Leva menos de 2 minutos. Insira o nome, crie sua conta e pronto.",
  },
  {
    step: "02",
    title: "Configure profissionais e serviços",
    description: "Adicione sua equipe, defina os serviços oferecidos e os horários disponíveis.",
  },
  {
    step: "03",
    title: "Conecte seu WhatsApp",
    description: "Integre o número da barbearia e o chatbot começa a atender automaticamente.",
  },
  {
    step: "04",
    title: "Acompanhe e cresça",
    description: "Monitore agendamentos, retenção de clientes e faturamento em tempo real.",
  },
];

const plans = [
  {
    name: "Starter",
    price: "R$ 97",
    period: "/mês",
    description: "Ideal para barbearias que estão começando",
    features: [
      "Até 100 mensagens/mês",
      "1 profissional",
      "Agendamento via WhatsApp",
      "Painel backoffice",
      "Suporte por email",
    ],
    cta: "Começar grátis",
    highlighted: false,
  },
  {
    name: "Growth",
    price: "R$ 197",
    period: "/mês",
    description: "Para barbearias que querem escalar",
    features: [
      "Até 500 mensagens/mês",
      "Até 5 profissionais",
      "IA de retenção de clientes",
      "Métricas avançadas",
      "Suporte prioritário",
    ],
    cta: "Assinar Growth",
    highlighted: true,
  },
  {
    name: "Enterprise",
    price: "R$ 397",
    period: "/mês",
    description: "Para redes e franquias",
    features: [
      "Mensagens ilimitadas",
      "Profissionais ilimitados",
      "Multi-unidade",
      "API personalizada",
      "Gerente de conta dedicado",
    ],
    cta: "Falar com vendas",
    highlighted: false,
  },
];

const testimonials = [
  {
    name: "Ricardo Mendes",
    role: "Dono — Barbearia Clássica",
    text: "Antes eu perdia cliente por não responder WhatsApp rápido. Agora o bot agenda sozinho e eu só apareço pra cortar.",
    rating: 5,
  },
  {
    name: "Fernando Costa",
    role: "Sócio — BarberKing",
    text: "Em 3 meses o sistema recuperou 40 clientes inativos automaticamente. O retorno foi imediato.",
    rating: 5,
  },
  {
    name: "Paulo Souza",
    role: "Dono — Corte & Estilo",
    text: "A agenda online reduziu os no-shows pela metade. Os clientes recebem lembrete automático e confirmam pelo chat.",
    rating: 5,
  },
];

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Navbar */}
      <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <Zap className="h-6 w-6 text-primary" />
            <span className="text-xl font-bold">BarberFlow</span>
          </div>
          <nav className="hidden items-center gap-6 text-sm md:flex">
            <a href="#features" className="text-muted-foreground transition-colors hover:text-foreground">
              Funcionalidades
            </a>
            <a href="#how-it-works" className="text-muted-foreground transition-colors hover:text-foreground">
              Como funciona
            </a>
            <a href="#pricing" className="text-muted-foreground transition-colors hover:text-foreground">
              Preços
            </a>
          </nav>
          <div className="flex items-center gap-3">
            <Button variant="ghost" asChild>
              <Link href="/login">Entrar</Link>
            </Button>
            <Button asChild>
              <Link href="/register">Cadastrar barbearia</Link>
            </Button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="mx-auto max-w-6xl px-4 pb-24 pt-20 text-center">
        <Badge variant="secondary" className="mb-6 inline-flex gap-1.5 px-4 py-1.5 text-sm">
          <Zap className="h-3.5 w-3.5" /> Powered by IA
        </Badge>
        <h1 className="mx-auto max-w-3xl text-4xl font-extrabold leading-tight tracking-tight sm:text-5xl lg:text-6xl">
          Sua barbearia no piloto{" "}
          <span className="bg-gradient-to-r from-primary to-blue-400 bg-clip-text text-transparent">
            automático
          </span>
        </h1>
        <p className="mx-auto mt-6 max-w-2xl text-lg text-muted-foreground">
          Chatbot no WhatsApp, agendamento inteligente e IA que recupera clientes inativos. Tudo em um
          sistema feito para barbearias brasileiras.
        </p>
        <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
          <Button size="lg" asChild className="h-12 px-8 text-base">
            <Link href="/register">
              Começar grátis por 14 dias <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
          <Button size="lg" variant="outline" asChild className="h-12 px-8 text-base">
            <a href="#how-it-works">Ver como funciona</a>
          </Button>
        </div>
        <p className="mt-4 text-sm text-muted-foreground">
          Sem cartão de crédito · Cancele quando quiser
        </p>

        {/* Mock dashboard preview */}
        <div className="mt-16 overflow-hidden rounded-2xl border bg-card shadow-2xl">
          <div className="flex items-center gap-2 border-b bg-muted/50 px-4 py-3">
            <div className="h-3 w-3 rounded-full bg-red-400" />
            <div className="h-3 w-3 rounded-full bg-yellow-400" />
            <div className="h-3 w-3 rounded-full bg-green-400" />
            <span className="ml-2 text-xs text-muted-foreground">barberflow.app/dashboard</span>
          </div>
          <div className="grid grid-cols-2 gap-4 p-6 sm:grid-cols-4">
            {[
              { label: "Agendamentos hoje", value: "12" },
              { label: "Receita do mês", value: "R$ 4.280" },
              { label: "Clientes ativos", value: "87" },
              { label: "Taxa de retorno", value: "73%" },
            ].map((stat) => (
              <div key={stat.label} className="rounded-lg border bg-background p-4 text-left">
                <p className="text-xs text-muted-foreground">{stat.label}</p>
                <p className="mt-1 text-2xl font-bold">{stat.value}</p>
              </div>
            ))}
          </div>
          <div className="space-y-2 px-6 pb-6">
            {[
              { time: "09:00", name: "João Silva", service: "Corte + Barba", status: "Confirmado", color: "bg-green-500" },
              { time: "10:00", name: "Pedro Alves", service: "Corte masculino", status: "Aguardando", color: "bg-yellow-500" },
              { time: "11:30", name: "Lucas Rocha", service: "Barba completa", status: "Confirmado", color: "bg-green-500" },
            ].map((appt) => (
              <div
                key={appt.time}
                className="flex items-center justify-between rounded-lg border bg-background px-4 py-3 text-sm"
              >
                <span className="w-12 font-mono text-muted-foreground">{appt.time}</span>
                <span className="flex-1 font-medium">{appt.name}</span>
                <span className="hidden flex-1 text-muted-foreground sm:block">{appt.service}</span>
                <span className={`flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs text-white ${appt.color}`}>
                  {appt.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Separator />

      {/* Features */}
      <section id="features" className="mx-auto max-w-6xl px-4 py-24">
        <div className="text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Tudo que sua barbearia precisa
          </h2>
          <p className="mt-4 text-muted-foreground">
            Do atendimento no WhatsApp à análise de dados, o BarberFlow cuida de tudo.
          </p>
        </div>
        <div className="mt-16 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          {features.map(({ icon: Icon, title, description }) => (
            <div
              key={title}
              className="rounded-xl border bg-card p-6 transition-shadow hover:shadow-md"
            >
              <div className="mb-4 inline-flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <Icon className="h-5 w-5" />
              </div>
              <h3 className="mb-2 font-semibold">{title}</h3>
              <p className="text-sm leading-relaxed text-muted-foreground">{description}</p>
            </div>
          ))}
        </div>
      </section>

      <Separator />

      {/* How it works */}
      <section id="how-it-works" className="bg-muted/30 py-24">
        <div className="mx-auto max-w-6xl px-4">
          <div className="text-center">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Como funciona</h2>
            <p className="mt-4 text-muted-foreground">Comece em minutos, sem precisar de técnico.</p>
          </div>
          <div className="mt-16 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
            {steps.map(({ step, title, description }) => (
              <div key={step} className="text-center">
                <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-primary text-xl font-bold text-primary-foreground">
                  {step}
                </div>
                <h3 className="mb-2 font-semibold">{title}</h3>
                <p className="text-sm leading-relaxed text-muted-foreground">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <Separator />

      {/* Testimonials */}
      <section className="mx-auto max-w-6xl px-4 py-24">
        <div className="text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Barbearias que já automatizaram
          </h2>
        </div>
        <div className="mt-12 grid gap-6 sm:grid-cols-3">
          {testimonials.map(({ name, role, text, rating }) => (
            <div key={name} className="rounded-xl border bg-card p-6">
              <div className="mb-4 flex gap-0.5">
                {Array.from({ length: rating }).map((_, i) => (
                  <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                ))}
              </div>
              <p className="mb-4 text-sm leading-relaxed text-muted-foreground">"{text}"</p>
              <div>
                <p className="text-sm font-semibold">{name}</p>
                <p className="text-xs text-muted-foreground">{role}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <Separator />

      {/* Pricing */}
      <section id="pricing" className="bg-muted/30 py-24">
        <div className="mx-auto max-w-6xl px-4">
          <div className="text-center">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Planos e preços</h2>
            <p className="mt-4 text-muted-foreground">
              14 dias grátis em qualquer plano. Sem surpresas na fatura.
            </p>
          </div>
          <div className="mt-12 grid gap-6 lg:grid-cols-3">
            {plans.map(({ name, price, period, description, features: planFeatures, cta, highlighted }) => (
              <div
                key={name}
                className={`relative rounded-2xl border p-8 ${
                  highlighted
                    ? "border-primary bg-primary shadow-xl shadow-primary/10 text-primary-foreground"
                    : "bg-card"
                }`}
              >
                {highlighted && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2">
                    <Badge className="bg-yellow-400 px-3 py-1 text-yellow-900 hover:bg-yellow-400">
                      Mais popular
                    </Badge>
                  </div>
                )}
                <h3 className="text-lg font-bold">{name}</h3>
                <p
                  className={`mt-1 text-sm ${
                    highlighted ? "text-primary-foreground/80" : "text-muted-foreground"
                  }`}
                >
                  {description}
                </p>
                <div className="mt-6 flex items-end gap-1">
                  <span className="text-4xl font-extrabold">{price}</span>
                  <span
                    className={`pb-1 text-sm ${
                      highlighted ? "text-primary-foreground/70" : "text-muted-foreground"
                    }`}
                  >
                    {period}
                  </span>
                </div>
                <ul className="mt-6 space-y-3">
                  {planFeatures.map((f) => (
                    <li key={f} className="flex items-start gap-2 text-sm">
                      <CheckCircle
                        className={`mt-0.5 h-4 w-4 shrink-0 ${
                          highlighted ? "text-primary-foreground" : "text-primary"
                        }`}
                      />
                      <span>{f}</span>
                    </li>
                  ))}
                </ul>
                <Button
                  className={`mt-8 w-full ${
                    highlighted ? "bg-white text-primary hover:bg-white/90" : ""
                  }`}
                  variant={highlighted ? "outline" : "default"}
                  asChild
                >
                  <Link href="/register">{cta}</Link>
                </Button>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA final */}
      <section className="mx-auto max-w-6xl px-4 py-24 text-center">
        <div className="mx-auto max-w-2xl">
          <div className="mb-6 flex flex-wrap justify-center gap-6 text-muted-foreground">
            <span className="flex items-center gap-2 text-sm">
              <Clock className="h-4 w-4 text-primary" /> Setup em 5 minutos
            </span>
            <span className="flex items-center gap-2 text-sm">
              <Shield className="h-4 w-4 text-primary" /> Dados seguros
            </span>
            <span className="flex items-center gap-2 text-sm">
              <Scissors className="h-4 w-4 text-primary" /> Feito para barbearias
            </span>
          </div>
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Pronto para automatizar sua barbearia?
          </h2>
          <p className="mt-4 text-muted-foreground">
            Junte-se a centenas de barbearias que já estão crescendo com o BarberFlow.
          </p>
          <Button size="lg" className="mt-8 h-12 px-10 text-base" asChild>
            <Link href="/register">
              Criar conta grátis <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t bg-muted/30">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-4 py-8 sm:flex-row">
          <div className="flex items-center gap-2">
            <Zap className="h-5 w-5 text-primary" />
            <span className="font-semibold">BarberFlow</span>
          </div>
          <p className="text-sm text-muted-foreground">
            © {new Date().getFullYear()} BarberFlow. Todos os direitos reservados.
          </p>
          <div className="flex gap-4 text-sm text-muted-foreground">
            <Link href="/login" className="transition-colors hover:text-foreground">
              Entrar
            </Link>
            <Link href="/register" className="transition-colors hover:text-foreground">
              Cadastrar
            </Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
