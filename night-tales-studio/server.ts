import express from "express";
import path from "path";
import cors from "cors";
import dotenv from "dotenv";
import { createServer as createViteServer } from "vite";
import OpenAI from "openai";

dotenv.config();

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

app.post("/api/chat", async (req, res) => {
  try {
    const { messages, model = "gpt-4o", provider = "openai", apiKey: clientApiKey } = req.body;
    
    if (provider === "anthropic") {
      const apiKey = clientApiKey || process.env.ANTHROPIC_API_KEY;
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
      const apiKey = clientApiKey || process.env.GEMINI_API_KEY;
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
      let apiKey = clientApiKey || process.env.OPENAI_API_KEY;

      if (provider === "deepseek") {
        baseURL = "https://api.deepseek.com";
        apiKey = clientApiKey || process.env.DEEPSEEK_API_KEY;
      } else if (provider === "aimlapi") {
        baseURL = "https://api.aimlapi.com/v1";
        apiKey = clientApiKey || process.env.AIML_API_KEY;
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
    console.error("Chat API Error:", error);
    res.status(500).json({ error: error.message || "Something went wrong" });
  }
});

app.post("/api/test-key", async (req, res) => {
  try {
    const { provider, apiKey } = req.body;
    if (!apiKey) return res.status(400).json({ error: "Missing API Key" });

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
    console.error("Test API Error:", error);
    res.status(401).json({ error: error.message || "Invalid Key" });
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
