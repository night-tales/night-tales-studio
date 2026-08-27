import express from "express";
import path from "path";
import cors from "cors";
import dotenv from "dotenv";
import { createServer as createViteServer } from "vite";
import OpenAI from "openai";

dotenv.config();

const app = express();
const PORT = Number(process.env.PORT || 3000);
const allowedOrigins = (process.env.ALLOWED_ORIGINS || "http://localhost:3000").split(",").map(origin => origin.trim()).filter(Boolean);

app.use(cors({
  origin: (origin, callback) => {
    if (!origin || allowedOrigins.includes(origin)) return callback(null, true);
    return callback(new Error("Origin not allowed"));
  },
}));
app.use(express.json({ limit: "1mb" }));

const rateWindowMs = 60_000;
const maxRequestsPerWindow = 30;
const rateBuckets = new Map<string, { count: number; resetAt: number }>();

app.get("/api/providers", (_req, res) => {
  const providers = [
    ["openai", process.env.OPENAI_API_KEY],
    ["anthropic", process.env.ANTHROPIC_API_KEY],
    ["gemini", process.env.GEMINI_API_KEY],
    ["deepseek", process.env.DEEPSEEK_API_KEY],
    ["aimlapi", process.env.AIML_API_KEY],
  ]
    .filter(([, key]) => Boolean(key))
    .map(([provider]) => provider);

  res.json({ providers });
});

app.use("/api/", (req, res, next) => {
  const now = Date.now();
  const key = req.ip || "unknown";
  const bucket = rateBuckets.get(key);

  if (!bucket || now >= bucket.resetAt) {
    rateBuckets.set(key, { count: 1, resetAt: now + rateWindowMs });
    return next();
  }

  if (bucket.count >= maxRequestsPerWindow) {
    return res.status(429).json({ error: "Too many requests. Please try again later." });
  }

  bucket.count += 1;
  return next();
});

app.post("/api/chat", async (req, res) => {
  try {
    const { messages, model = "gpt-4o", provider = "openai" } = req.body;

    if (!Array.isArray(messages) || messages.length === 0 || messages.length > 100) {
      return res.status(400).json({ error: "messages must be a non-empty array with at most 100 items" });
    }
    if (typeof provider !== "string" || typeof model !== "string") {
      return res.status(400).json({ error: "Invalid provider or model" });
    }
    
    if (provider === "anthropic") {
      const apiKey = process.env.ANTHROPIC_API_KEY;
      if (!apiKey) return res.status(500).json({ error: "ANTHROPIC_API_KEY is missing." });
      
      // Map 'assistant' role to 'assistant' and 'user' to 'user'
      // Anthropic does not allow system messages in the messages array, they must be at the top level (we skip system for now as our models.ts doesn't use it)
      const response = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: {
          'x-api-key': apiKey,
          'anthropic-version': '2023-06-01',
          'content-type': 'application/json'
        },
        body: JSON.stringify({
          model: model,
          max_tokens: 1024,
          messages: messages.map((m: any) => ({
            role: m.role === 'tool' ? 'user' : m.role,
            content: m.content
          }))
        })
      });
      const data = await response.json();
      if (data.error) throw new Error(data.error.message || 'Anthropic API Error');
      return res.json({ choices: [{ message: { content: data.content[0].text } }] });
      
    } else if (provider === "gemini") {
      const apiKey = process.env.GEMINI_API_KEY;
      if (!apiKey) return res.status(500).json({ error: "GEMINI_API_KEY is missing." });
      
      const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: messages.map((m: any) => ({
            role: m.role === 'assistant' ? 'model' : 'user',
            parts: [{ text: m.content }]
          }))
        })
      });
      const data = await response.json();
      if (data.error) throw new Error(data.error.message || 'Gemini API Error');
      return res.json({ choices: [{ message: { content: data.candidates[0].content.parts[0].text } }] });
      
    } else {
      // OpenAI-compatible providers: openai, deepseek, aimlapi
      let baseURL: string | undefined = undefined;
      let apiKey = process.env.OPENAI_API_KEY;

      if (provider === "deepseek") {
        baseURL = "https://api.deepseek.com";
        apiKey = process.env.DEEPSEEK_API_KEY;
      } else if (provider === "aimlapi") {
        baseURL = "https://api.aimlapi.com/v1";
        apiKey = process.env.AIML_API_KEY;
      }

      if (!apiKey) {
        return res.status(500).json({ error: `API Key missing for provider: ${provider}` });
      }

      const client = new OpenAI({
        baseURL: baseURL,
        apiKey: apiKey,
      });

      const completion = await client.chat.completions.create({
        model: model,
        messages: messages,
      });

      res.json(completion);
    }
  } catch (error: any) {
    console.error("Chat API Error:", error instanceof Error ? error.message : "Unknown error");
    res.status(500).json({ error: "AI request failed" });
  }
});

app.post("/api/test-key", async (req, res) => {
  try {
    const { provider } = req.body;
    const apiKey = provider === "openai" ? process.env.OPENAI_API_KEY
      : provider === "anthropic" ? process.env.ANTHROPIC_API_KEY
      : provider === "gemini" ? process.env.GEMINI_API_KEY
      : provider === "deepseek" ? process.env.DEEPSEEK_API_KEY
      : provider === "aimlapi" ? process.env.AIML_API_KEY
      : undefined;
    if (!apiKey) return res.status(503).json({ error: "Provider is not configured on the server" });

    if (provider === "openai") {
      const response = await fetch('https://api.openai.com/v1/models', {
        headers: { 'Authorization': `Bearer ${apiKey}` }
      });
      if (!response.ok) throw new Error("Invalid OpenAI API Key");
    } else if (provider === "anthropic") {
      // Make a dummy request with max_tokens: 1
      const response = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: {
          'x-api-key': apiKey,
          'anthropic-version': '2023-06-01',
          'content-type': 'application/json'
        },
        body: JSON.stringify({ model: 'claude-3-haiku-20240307', max_tokens: 1, messages: [{role: 'user', content: 'test'}] })
      });
      if (!response.ok) throw new Error("Invalid Anthropic API Key");
    } else if (provider === "gemini") {
      const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}`);
      if (!response.ok) throw new Error("Invalid Gemini API Key");
    } else if (provider === "deepseek") {
      const response = await fetch('https://api.deepseek.com/models', {
        headers: { 'Authorization': `Bearer ${apiKey}` }
      });
      if (!response.ok) throw new Error("Invalid DeepSeek API Key");
    } else if (provider === "aimlapi") {
      const response = await fetch('https://api.aimlapi.com/v1/models', {
        headers: { 'Authorization': `Bearer ${apiKey}` }
      });
      if (!response.ok) throw new Error("Invalid AIML API Key");
    } else {
      return res.status(400).json({ error: "Unknown provider" });
    }
    
    res.json({ success: true });
  } catch (error: any) {
    console.error("Test API Error:", error instanceof Error ? error.message : "Unknown error");
    res.status(401).json({ error: "Provider key validation failed" });
  }
});

async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
