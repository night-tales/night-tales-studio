export interface Message {
  id: string;
  role: 'user' | 'assistant' | 'tool';
  content: string;
  agentName?: string;
  isStreaming?: boolean;
}

export interface Agent {
  id: string;
  name: string;
  provider: string;
  model: string;
  description: string;
}

export const AVAILABLE_AGENTS: Agent[] = [
  { id: 'chatgpt', name: 'ChatGPT (OpenAI)', provider: 'AIML', model: 'gpt-4o', description: 'General intelligence, planning, coding.' },
  { id: 'claude', name: 'Claude (Anthropic)', provider: 'AIML', model: 'claude-3-opus-20240229', description: 'Long context, document analysis, writing.' },
  { id: 'gemini', name: 'Gemini (Google)', provider: 'AIML', model: 'gemini-1.5-pro', description: 'Multimodal, search, Google ecosystem.' },
  { id: 'llama', name: 'Llama 3 (Meta)', provider: 'AIML', model: 'meta-llama/Llama-3-70b-chat-hf', description: 'Fast, open-source intelligence.' }
];

export interface TaskUiState {
  messages: Message[];
  loading: boolean;
  selectedAgentId: string;
}
