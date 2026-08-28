import { useState, useRef, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Send, Bot, User, Paperclip, Loader2, StopCircle, Settings, Activity } from 'lucide-react';
import { AVAILABLE_AGENTS, Message, Agent } from '../models';
import { auth, db } from '../lib/firebase';
import { collection, addDoc, serverTimestamp, doc, getDoc, onSnapshot, query, where } from 'firebase/firestore';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';
import { decryptKey } from '../lib/crypto';
import { Link } from 'react-router-dom';

interface AiTask {
  id: string;
  title?: string;
  completion_percentage: number;
  userId: string;
}

export default function TaskScreen() {
  const location = useLocation();
  const initialAgentId = location.state?.agentId || AVAILABLE_AGENTS[0].id;
  
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [selectedAgentId, setSelectedAgentId] = useState(initialAgentId);
  const [isLoading, setIsLoading] = useState(false);
  const [availableAgents, setAvailableAgents] = useState<Agent[]>([]);
  const [loadingAgents, setLoadingAgents] = useState(true);
  const [ongoingTasks, setOngoingTasks] = useState<AiTask[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  useEffect(() => {
    const loadAvailableAgents = async () => {
      if (!auth.currentUser) {
        setLoadingAgents(false);
        return;
      }
      try {
        const docRef = doc(db, 'userKeys', auth.currentUser.uid);
        const docSnap = await getDoc(docRef);
        
        let validProviders = new Set<string>();
        if (docSnap.exists()) {
          const data = docSnap.data();
          const providersToCheck = ['openai', 'anthropic', 'gemini', 'deepseek', 'aimlapi'];
          for (const provider of providersToCheck) {
            if (data[provider] && decryptKey(data[provider], auth.currentUser.uid)) {
              validProviders.add(provider);
            }
          }
        }
        
        const filteredAgents = AVAILABLE_AGENTS.filter(agent => validProviders.has(agent.provider));
        setAvailableAgents(filteredAgents);
        
        // Update selected agent if current one is not available
        if (filteredAgents.length > 0) {
          if (!filteredAgents.find(a => a.id === selectedAgentId)) {
            setSelectedAgentId(filteredAgents[0].id);
          }
        }
      } catch (error) {
        console.error("Failed to load available agents", error);
      } finally {
        setLoadingAgents(false);
      }
    };

    loadAvailableAgents();
  }, []);

  useEffect(() => {
    if (!auth.currentUser) return;
    
    const q = query(
      collection(db, 'ai_tasks'),
      where('userId', '==', auth.currentUser.uid)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const tasksData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as AiTask[];
      
      // Filter ongoing tasks locally to avoid complex index requirements
      const activeTasks = tasksData.filter(task => 
        task.completion_percentage !== undefined && 
        task.completion_percentage >= 0 && 
        task.completion_percentage < 100
      );
      
      setOngoingTasks(activeTasks);
    }, (error) => {
      console.error("Error fetching tasks:", error);
    });

    return () => unsubscribe();
  }, []);

  const saveMessageToFirestore = async (msgRole: 'user' | 'assistant' | 'tool', content: string, agentName?: string) => {
    try {
      await addDoc(collection(db, 'messages'), {
        role: msgRole,
        content: content,
        agentName: agentName || null,
        createdAt: serverTimestamp(),
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.CREATE, 'messages');
    }
  };

  const getApiKeyForProvider = async (provider: string): Promise<string | undefined> => {
    if (!auth.currentUser) return undefined;
    try {
      const docRef = doc(db, 'userKeys', auth.currentUser.uid);
      const docSnap = await getDoc(docRef);
      if (docSnap.exists()) {
        const data = docSnap.data();
        const encryptedKey = data[provider];
        if (encryptedKey) {
          return decryptKey(encryptedKey, auth.currentUser.uid);
        }
      }
    } catch (error) {
      console.error("Failed to load API key", error);
    }
    return undefined;
  };

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userInput = input.trim();
    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: userInput
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);
    
    // Save user message to firestore in background
    saveMessageToFirestore('user', userInput);

    const agent = AVAILABLE_AGENTS.find(a => a.id === selectedAgentId);
    const provider = agent?.provider || 'openai';

    try {
      const apiKey = await getApiKeyForProvider(provider);

      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          provider: provider,
          model: agent?.model || "gpt-4o",
          apiKey: apiKey,
          messages: messages.concat(userMessage).map(m => ({ role: m.role, content: m.content }))
        })
      });

      if (!response.ok) {
        throw new Error('فشل الاتصال بالخادم');
      }

      const data = await response.json();
      const responseContent = data.choices?.[0]?.message?.content || 'No response';
      
      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: responseContent,
        agentName: agent?.name
      };

      setMessages(prev => [...prev, assistantMessage]);
      
      // Save assistant message to firestore
      saveMessageToFirestore('assistant', responseContent, agent?.name);
      
    } catch (error: any) {
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: `حدث خطأ: ${error.message}`,
        agentName: 'System'
      };
      setMessages(prev => [...prev, errorMessage]);
      saveMessageToFirestore('assistant', errorMessage.content, errorMessage.agentName);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full absolute inset-0" dir="rtl">
      
      {/* Agent Selector */}
      <div className="bg-zinc-900 border-b border-zinc-800 p-2 shrink-0 z-10 sticky top-0 flex items-center justify-between">
        {loadingAgents ? (
          <div className="flex items-center gap-2 text-zinc-400 text-sm px-2">
            <Loader2 size={16} className="animate-spin" />
            <span>جاري تحميل النماذج...</span>
          </div>
        ) : availableAgents.length > 0 ? (
          <select 
            value={selectedAgentId}
            onChange={(e) => setSelectedAgentId(e.target.value)}
            className="w-full bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm rounded-lg px-3 py-2 outline-none focus:border-blue-500 appearance-none"
          >
            {availableAgents.map(agent => (
              <option key={agent.id} value={agent.id}>{agent.name}</option>
            ))}
          </select>
        ) : (
          <div className="flex items-center justify-between w-full bg-red-900/20 border border-red-900/50 rounded-lg px-3 py-2">
            <span className="text-red-400 text-sm">لا توجد مفاتيح API مفعلة</span>
            <Link to="/settings" className="flex items-center gap-1 text-xs bg-red-500/20 text-red-300 hover:bg-red-500/30 px-2 py-1 rounded transition-colors">
              <Settings size={14} />
              <span>الإعدادات</span>
            </Link>
          </div>
        )}
      </div>

      {/* Ongoing Tasks Progress */}
      {ongoingTasks.length > 0 && (
        <div className="bg-zinc-900 border-b border-zinc-800 p-3 shrink-0 z-10 space-y-3">
          <div className="flex items-center gap-2 text-zinc-300 text-sm font-medium">
            <Activity size={16} className="text-blue-500" />
            <span>مهام قيد التنفيذ ({ongoingTasks.length})</span>
          </div>
          <div className="space-y-3 max-h-32 overflow-y-auto pr-1">
            {ongoingTasks.map(task => (
              <div key={task.id} className="space-y-1.5">
                <div className="flex justify-between items-center text-xs">
                  <span className="text-zinc-300 truncate">{task.title || 'مهمة ذكاء اصطناعي...'}</span>
                  <span className="text-zinc-400 font-mono">{task.completion_percentage}%</span>
                </div>
                <div className="w-full bg-zinc-800 rounded-full h-1.5 overflow-hidden">
                  <div 
                    className="bg-blue-500 h-1.5 rounded-full transition-all duration-500 ease-out" 
                    style={{ width: `${task.completion_percentage}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

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
            placeholder={availableAgents.length === 0 && !loadingAgents ? "يجب إضافة مفتاح API واحد على الأقل في الإعدادات..." : "اكتب مهمتك هنا..."}
            disabled={availableAgents.length === 0}
            className="flex-1 bg-transparent text-sm text-zinc-100 resize-none outline-none py-2 max-h-32 min-h-[40px] leading-relaxed disabled:opacity-50"
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
              disabled={!input.trim() || availableAgents.length === 0}
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
