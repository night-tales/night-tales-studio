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
  { id: 'gpt-4o', name: 'GPT-4o (OpenAI)', provider: 'openai', model: 'gpt-4o', description: 'Advanced intelligence from OpenAI.' },
  { id: 'claude', name: 'Claude 3.5 Sonnet (Anthropic)', provider: 'anthropic', model: 'claude-3-5-sonnet-20240620', description: 'Advanced reasoning from Anthropic.' },
  { id: 'gemini', name: 'Gemini 1.5 Pro (Google)', provider: 'gemini', model: 'gemini-1.5-pro', description: 'Multimodal capabilities from Google.' },
  { id: 'deepseek', name: 'DeepSeek Chat', provider: 'deepseek', model: 'deepseek-chat', description: 'Fast and smart coding assistant.' },
  { id: 'aimlapi', name: 'Llama 3 70B (AIMLAPI)', provider: 'aimlapi', model: 'meta-llama/Llama-3-70b-chat-hf', description: 'Open source models via AIMLAPI.' }
];

export interface TaskUiState {
  messages: Message[];
  loading: boolean;
  selectedAgentId: string;
}
