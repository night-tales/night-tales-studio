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
  const apiKey = process.env.AIML_API_KEY;
  if (!apiKey) {
    return res.status(500).json({ error: "AIML_API_KEY is missing." });
  }

  try {
    const { messages, model = "gpt-4o" } = req.body;
    
    // Connect to AIMLAPI using OpenAI SDK
    const client = new OpenAI({
      baseURL: "https://api.aimlapi.com/v1",
      apiKey: apiKey,
    });

    const completion = await client.chat.completions.create({
      model: model,
      messages: messages,
    });

    res.json(completion);
  } catch (error: any) {
    console.error("Chat API Error:", error);
    res.status(500).json({ error: error.message || "Something went wrong" });
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
