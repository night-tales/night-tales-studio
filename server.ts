import express from 'express';
import path from 'path';
import cors from 'cors';
import dotenv from 'dotenv';
import { createServer as createViteServer } from 'vite';
import OpenAI from 'openai';

dotenv.config();

const app = express();
const PORT = Number(process.env.PORT) || 3000;
const MAX_MESSAGES = 50;
const MAX_MESSAGE_LENGTH = 20_000;

const allowedOrigins = process.env.CORS_ORIGIN
  ? process.env.CORS_ORIGIN.split(',').map((origin) => origin.trim()).filter(Boolean)
  : true;

app.disable('x-powered-by');
app.use(cors({ origin: allowedOrigins }));
app.use(express.json({ limit: '1mb' }));

interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

function isChatMessage(value: unknown): value is ChatMessage {
  if (!value || typeof value !== 'object') return false;

  const message = value as Record<string, unknown>;
  return (
    (message.role === 'user' ||
      message.role === 'assistant' ||
      message.role === 'system') &&
    typeof message.content === 'string' &&
    message.content.length > 0 &&
    message.content.length <= MAX_MESSAGE_LENGTH
  );
}

app.get('/api/health', (_req, res) => {
  res.json({ status: 'ok' });
});

app.post('/api/chat', async (req, res) => {
  const apiKey = process.env.AIML_API_KEY;
  if (!apiKey) {
    return res.status(503).json({ error: 'AI service is not configured.' });
  }

  const body = req.body as { messages?: unknown; model?: unknown };
  const messages = body.messages;
  const model = body.model ?? 'gpt-4o';

  if (
    !Array.isArray(messages) ||
    messages.length === 0 ||
    messages.length > MAX_MESSAGES ||
    !messages.every(isChatMessage)
  ) {
    return res.status(400).json({ error: 'Invalid chat messages.' });
  }

  if (typeof model !== 'string' || !/^[a-zA-Z0-9._:-]{1,100}$/.test(model)) {
    return res.status(400).json({ error: 'Invalid model.' });
  }

  try {
    const client = new OpenAI({
      baseURL: 'https://api.aimlapi.com/v1',
      apiKey,
    });

    const completion = await client.chat.completions.create({
      model,
      messages,
    });

    return res.json(completion);
  } catch (error: unknown) {
    console.error('Chat API Error:', error);
    return res.status(502).json({ error: 'AI provider request failed.' });
  }
});

async function startServer() {
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (_req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer().catch((error: unknown) => {
  console.error('Server startup failed:', error);
  process.exit(1);
});
