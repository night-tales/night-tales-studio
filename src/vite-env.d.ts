/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_GOOGLE_MAPS_API_KEY: string;
  // Add other client-side env variables here if needed
  // Note: Server-side variables like OPENAI_API_KEY should NOT be exposed to the client
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
