"use client";
import { useState, useEffect, useRef, useCallback } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Send, ChevronDown, ChevronUp, Trash2 } from "lucide-react";
import { api } from "@/lib/api";

// ── Types ──────────────────────────────────────────────────────────────────────

interface ButtonOption { id: string; title: string }
interface ListRow { id: string; title: string; description?: string }
interface ListSection { title: string; rows: ListRow[] }

interface BotMessage {
  type: "text" | "buttons" | "list";
  body: string;
  buttons?: ButtonOption[];
  buttonText?: string;
  sections?: ListSection[];
}

interface ChatMessage {
  id: string;
  direction: "outbound" | "inbound"; // outbound = user, inbound = bot
  content: BotMessage | { type: "text"; body: string };
  timestamp: Date;
}

// ── Webhook payload builder ────────────────────────────────────────────────────

function buildWebhookPayload(
  fromPhone: string,
  phoneNumberId: string,
  text: string,
  interactiveId?: string
) {
  const message = interactiveId
    ? {
        from: fromPhone,
        id: `test_${Date.now()}`,
        type: "interactive",
        timestamp: String(Math.floor(Date.now() / 1000)),
        interactive: {
          type: "button_reply",
          button_reply: { id: interactiveId, title: text },
        },
      }
    : {
        from: fromPhone,
        id: `test_${Date.now()}`,
        type: "text",
        timestamp: String(Math.floor(Date.now() / 1000)),
        text: { body: text },
      };

  return {
    object: "whatsapp_business_account",
    entry: [
      {
        id: "test_entry",
        changes: [
          {
            field: "messages",
            value: {
              messages: [message],
              metadata: {
                display_phone_number: "5500000000000",
                phone_number_id: phoneNumberId,
              },
            },
          },
        ],
      },
    ],
  };
}

// ── Message bubble components ──────────────────────────────────────────────────

function UserBubble({ body }: { body: string }) {
  return (
    <div className="flex justify-end mb-2">
      <div className="max-w-[75%] rounded-2xl rounded-tr-sm bg-[#dcf8c6] dark:bg-[#056162] px-4 py-2 text-sm text-foreground shadow-sm">
        {body}
      </div>
    </div>
  );
}

function BotTextBubble({ body }: { body: string }) {
  return (
    <div className="flex justify-start mb-2">
      <div className="max-w-[75%] rounded-2xl rounded-tl-sm bg-card border px-4 py-2 text-sm shadow-sm whitespace-pre-wrap">
        {body}
      </div>
    </div>
  );
}

function BotButtonsBubble({
  msg,
  onButtonClick,
}: {
  msg: BotMessage & { type: "buttons" };
  onButtonClick: (id: string, title: string) => void;
}) {
  return (
    <div className="flex justify-start mb-2">
      <div className="max-w-[75%]">
        <div className="rounded-2xl rounded-tl-sm bg-card border px-4 py-2 text-sm shadow-sm whitespace-pre-wrap mb-1">
          {msg.body}
        </div>
        <div className="flex flex-col gap-1">
          {msg.buttons?.map((btn) => (
            <button
              key={btn.id}
              onClick={() => onButtonClick(btn.id, btn.title)}
              className="text-sm text-center border rounded-xl px-4 py-1.5 bg-card hover:bg-accent transition-colors shadow-sm font-medium text-primary"
            >
              {btn.title}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function BotListBubble({
  msg,
  onRowClick,
}: {
  msg: BotMessage & { type: "list" };
  onRowClick: (id: string, title: string) => void;
}) {
  const [open, setOpen] = useState(false);
  return (
    <div className="flex justify-start mb-2">
      <div className="max-w-[80%]">
        <div className="rounded-2xl rounded-tl-sm bg-card border px-4 py-2 text-sm shadow-sm whitespace-pre-wrap mb-1">
          {msg.body}
        </div>
        <button
          onClick={() => setOpen(!open)}
          className="flex items-center gap-1 text-sm text-center border rounded-xl px-4 py-1.5 bg-card hover:bg-accent transition-colors shadow-sm font-medium text-primary w-full justify-center"
        >
          {msg.buttonText ?? "Ver opções"}
          {open ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
        </button>
        {open && (
          <div className="mt-1 border rounded-xl bg-card shadow-sm overflow-hidden">
            {msg.sections?.map((section, si) => (
              <div key={si}>
                {section.title && (
                  <div className="px-3 py-1 text-xs font-semibold text-muted-foreground bg-muted/50 border-b">
                    {section.title}
                  </div>
                )}
                {section.rows.map((row) => (
                  <button
                    key={row.id}
                    onClick={() => { onRowClick(row.id, row.title); setOpen(false); }}
                    className="w-full text-left px-3 py-2 text-sm hover:bg-accent transition-colors border-b last:border-0"
                  >
                    <span className="font-medium">{row.title}</span>
                    {row.description && (
                      <span className="block text-xs text-muted-foreground">{row.description}</span>
                    )}
                  </button>
                ))}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

const DEFAULT_PHONE_NUMBER_ID = "982029531668676";

export default function TestChatPage() {
  const [phoneNumber, setPhoneNumber] = useState("5551999999999");
  const [phoneNumberId, setPhoneNumberId] = useState(DEFAULT_PHONE_NUMBER_ID);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState("");
  const [sending, setSending] = useState(false);
  const [polling, setPolling] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const pollIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const addMessage = useCallback((direction: "outbound" | "inbound", content: BotMessage | { type: "text"; body: string }) => {
    setMessages((prev) => [
      ...prev,
      { id: `${Date.now()}_${Math.random()}`, direction, content, timestamp: new Date() },
    ]);
  }, []);

  // Poll for bot responses
  const pollMessages = useCallback(async () => {
    if (!phoneNumber || polling) return;
    setPolling(true);
    try {
      const { data } = await api.get<BotMessage[]>(
        `/api/v1/whatsapp-test/messages?phoneNumber=${encodeURIComponent(phoneNumber)}`
      );
      if (data && data.length > 0) {
        data.forEach((msg) => addMessage("inbound", msg));
      }
    } catch {
      // silently ignore — test endpoint may not be active
    } finally {
      setPolling(false);
    }
  }, [phoneNumber, polling, addMessage]);

  // Start polling when phone number is set
  useEffect(() => {
    if (!phoneNumber) return;
    if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);
    pollIntervalRef.current = setInterval(pollMessages, 3000);
    return () => {
      if (pollIntervalRef.current) clearInterval(pollIntervalRef.current);
    };
  }, [phoneNumber, pollMessages]);

  // Auto-scroll
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function sendToWebhook(text: string, interactiveId?: string) {
    const payload = buildWebhookPayload(phoneNumber, phoneNumberId, text, interactiveId);
    await api.post("/api/v1/webhooks/whatsapp", payload);
  }

  async function handleSend() {
    const text = inputText.trim();
    if (!text || !phoneNumber) return;
    setSending(true);
    setInputText("");
    addMessage("outbound", { type: "text", body: text });
    try {
      await sendToWebhook(text);
    } catch (e) {
      console.error(e);
    } finally {
      setSending(false);
    }
  }

  async function handleButtonClick(id: string, title: string) {
    addMessage("outbound", { type: "text", body: title });
    await sendToWebhook(title, id);
  }

  async function handleListRowClick(id: string, title: string) {
    addMessage("outbound", { type: "text", body: title });
    await sendToWebhook(title, id);
  }

  async function handleClearConversation() {
    try {
      await api.delete(`/api/v1/whatsapp-test/conversation?phoneNumber=${encodeURIComponent(phoneNumber)}`);
    } catch { /* ignore */ }
    setMessages([]);
  }

  function renderBotMessage(msg: BotMessage, key: string) {
    if (msg.type === "buttons") {
      return <BotButtonsBubble key={key} msg={msg as BotMessage & { type: "buttons" }} onButtonClick={handleButtonClick} />;
    }
    if (msg.type === "list") {
      return <BotListBubble key={key} msg={msg as BotMessage & { type: "list" }} onRowClick={handleListRowClick} />;
    }
    return <BotTextBubble key={key} body={msg.body} />;
  }

  return (
    <AppShell>
      <div className="flex flex-col h-full max-w-2xl mx-auto gap-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Simulador WhatsApp</h1>
            <p className="text-muted-foreground text-sm">Teste o chatbot sem gastar mensagens na API do Meta</p>
          </div>
          <Button variant="outline" size="sm" onClick={handleClearConversation}>
            <Trash2 className="h-4 w-4 mr-1" />
            Limpar
          </Button>
        </div>

        {/* Config */}
        <Card>
          <CardContent className="p-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Número do cliente (simular)</Label>
                <Input
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="5551999999999"
                  className="h-8 text-sm"
                />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">WhatsApp Phone Number ID</Label>
                <Input
                  value={phoneNumberId}
                  onChange={(e) => setPhoneNumberId(e.target.value)}
                  placeholder="982029531668676"
                  className="h-8 text-sm"
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Chat area */}
        <Card className="flex-1 flex flex-col min-h-0">
          <CardHeader className="py-3 px-4 border-b flex-shrink-0">
            <CardTitle className="text-sm font-medium flex items-center gap-2">
              <div className="h-8 w-8 rounded-full bg-[#25d366] flex items-center justify-center text-white text-xs font-bold">W</div>
              <div>
                <p className="text-sm font-semibold">BarberFlow Bot</p>
                <p className="text-xs text-muted-foreground">online</p>
              </div>
            </CardTitle>
          </CardHeader>

          {/* Messages */}
          <CardContent
            className="flex-1 overflow-y-auto p-4 space-y-0"
            style={{ backgroundImage: "radial-gradient(circle, hsl(var(--muted)) 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          >
            {messages.length === 0 && (
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">
                Envie uma mensagem para iniciar a conversa
              </div>
            )}
            {messages.map((msg) =>
              msg.direction === "outbound"
                ? <UserBubble key={msg.id} body={(msg.content as { body: string }).body} />
                : renderBotMessage(msg.content as BotMessage, msg.id)
            )}
            <div ref={messagesEndRef} />
          </CardContent>

          {/* Input */}
          <div className="flex gap-2 p-3 border-t flex-shrink-0">
            <Input
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Digite uma mensagem..."
              onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && handleSend()}
              disabled={sending || !phoneNumber}
              className="flex-1"
            />
            <Button onClick={handleSend} disabled={sending || !inputText.trim() || !phoneNumber} size="icon">
              <Send className="h-4 w-4" />
            </Button>
          </div>
        </Card>
      </div>
    </AppShell>
  );
}
