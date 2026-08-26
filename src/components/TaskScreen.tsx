import { useState, useRef, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Send, Bot, User, Paperclip, Loader2, StopCircle } from 'lucide-react';
import { AVAILABLE_AGENTS, Message } from '../models';

export default function TaskScreen() {
  const location = useLocation();
  const initialAgentId = location.state?.agentId || AVAILABLE_AGENTS[0].id;
  
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [selectedAgentId, setSelectedAgentId] = useState(initialAgentId);
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim()
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    const agent = AVAILABLE_AGENTS.find(a => a.id === selectedAgentId);

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: agent?.model || "gpt-4o",
          messages: messages.concat(userMessage).map(m => ({ role: m.role, content: m.content }))
        })
      });

      if (!response.ok) {
        throw new Error('فشل الاتصال بالخادم');
      }

      const data = await response.json();
      
      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: data.choices?.[0]?.message?.content || 'No response',
        agentName: agent?.name
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error: any) {
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: `حدث خطأ: ${error.message}`,
        agentName: 'System'
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full absolute inset-0" dir="rtl">
      
      {/* Agent Selector */}
      <div className="bg-zinc-900 border-b border-zinc-800 p-2 shrink-0 z-10 sticky top-0">
        <select 
          value={selectedAgentId}
          onChange={(e) => setSelectedAgentId(e.target.value)}
          className="w-full bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm rounded-lg px-3 py-2 outline-none focus:border-blue-500 appearance-none"
        >
          {AVAILABLE_AGENTS.map(agent => (
            <option key={agent.id} value={agent.id}>{agent.name}</option>
          ))}
        </select>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-32">
        {messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-zinc-500 gap-3">
            <Bot size={48} className="opacity-20" />
            <p className="text-sm">كيف يمكنني مساعدتك اليوم؟</p>
          </div>
        ) : (
          messages.map(msg => (
            <div key={msg.id} className={`flex gap-3 max-w-[85%] ${msg.role === 'user' ? 'mr-auto flex-row-reverse' : 'ml-auto'}`}>
              <div className={`shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${msg.role === 'user' ? 'bg-blue-600' : 'bg-zinc-700'}`}>
                {msg.role === 'user' ? <User size={16} className="text-white"/> : <Bot size={16} className="text-zinc-300"/>}
              </div>
              <div className={`flex flex-col gap-1 ${msg.role === 'user' ? 'items-end' : 'items-start'}`}>
                {msg.agentName && <span className="text-[10px] text-zinc-500 px-1">{msg.agentName}</span>}
                <div className={`p-3 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap ${
                  msg.role === 'user' 
                    ? 'bg-blue-600 text-white rounded-tl-sm' 
                    : 'bg-zinc-800 text-zinc-100 rounded-tr-sm border border-zinc-700'
                }`}>
                  {msg.content}
                </div>
              </div>
            </div>
          ))
        )}
        {isLoading && (
          <div className="flex gap-3 max-w-[85%] ml-auto">
             <div className="shrink-0 w-8 h-8 rounded-full bg-zinc-700 flex items-center justify-center">
                <Bot size={16} className="text-zinc-300"/>
              </div>
              <div className="bg-zinc-800 border border-zinc-700 p-3 rounded-2xl rounded-tr-sm flex items-center gap-2">
                <Loader2 size={16} className="animate-spin text-zinc-400" />
                <span className="text-xs text-zinc-400">يفكر...</span>
              </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="absolute bottom-16 w-full bg-zinc-900 border-t border-zinc-800 p-3">
        <div className="flex items-end gap-2 bg-zinc-800 border border-zinc-700 rounded-xl p-1 focus-within:border-blue-500/50 transition-colors">
          <button className="p-2 text-zinc-400 hover:text-zinc-100 transition-colors shrink-0">
            <Paperclip size={20} />
          </button>
          
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
            placeholder="اكتب مهمتك هنا..."
            className="flex-1 bg-transparent text-sm text-zinc-100 resize-none outline-none py-2 max-h-32 min-h-[40px] leading-relaxed"
            rows={1}
            dir="auto"
          />
          
          {isLoading ? (
            <button className="p-2 bg-zinc-700 text-zinc-300 rounded-lg shrink-0 m-0.5">
              <StopCircle size={18} />
            </button>
          ) : (
            <button 
              onClick={handleSend}
              disabled={!input.trim()}
              className="p-2 bg-blue-600 text-white rounded-lg disabled:opacity-50 disabled:bg-zinc-700 disabled:text-zinc-500 transition-all shrink-0 m-0.5"
            >
              <Send size={18} className="rtl:-scale-x-100" />
            </button>
          )}
        </div>
      </div>

    </div>
  );
}
