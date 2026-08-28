import { useState, useEffect, useRef } from 'react';
import { Send, Bot, User as UserIcon, Loader2 } from 'lucide-react';
import { db, auth } from '../lib/firebase';
import { collection, query, orderBy, onSnapshot, addDoc, serverTimestamp, where } from 'firebase/firestore';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';

interface Message {
  id: string;
  role: 'user' | 'assistant' | 'tool';
  content: string;
  createdAt: any;
  userId: string;
}

export default function ChatScreen() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [input, setInput] = useState('');
  const [isSending, setIsSending] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const user = auth.currentUser;

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }
    
    const q = query(
      collection(db, 'messages'),
      where('userId', '==', user.uid),
      orderBy('createdAt', 'asc')
    );
    
    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const msgs = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as Message[];
        
        setMessages(msgs);
        setLoading(false);
        setTimeout(() => {
          scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      },
      (error) => {
        handleFirestoreError(error, OperationType.GET, 'messages');
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [user]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || !user || isSending) return;

    const userMessageContent = input.trim();
    setInput('');
    setIsSending(true);

    try {
      // 1. Add user message to Firestore
      await addDoc(collection(db, 'messages'), {
        role: 'user',
        content: userMessageContent,
        createdAt: serverTimestamp(),
        userId: user.uid
      });

      // 2. Call AI Backend
      const apiMessages = [
        { role: 'system', content: 'You are an AI assistant for task management and general help. Respond in Arabic.' },
        ...messages.map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: userMessageContent }
      ];

      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          messages: apiMessages,
          model: 'gemini-1.5-pro',
          provider: 'gemini'
        })
      });

      if (!response.ok) throw new Error('Network response was not ok');
      const data = await response.json();
      const aiResponse = data.choices[0].message.content;

      // 3. Add AI message to Firestore
      await addDoc(collection(db, 'messages'), {
        role: 'assistant',
        content: aiResponse,
        createdAt: serverTimestamp(),
        userId: user.uid
      });

    } catch (error) {
      console.error("Chat error:", error);
      // Fallback message if AI fails
      await addDoc(collection(db, 'messages'), {
        role: 'assistant',
        content: 'عذراً، حدث خطأ في الاتصال بالوكيل الذكي.',
        createdAt: serverTimestamp(),
        userId: user.uid
      });
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950" dir="rtl">
      {/* Header */}
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm flex items-center gap-2 text-zinc-100">
        <Bot size={20} className="text-blue-500" />
        <h2 className="font-bold">المحادثة الذكية</h2>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 pb-32 flex flex-col gap-4">
        {loading ? (
          <div className="flex justify-center items-center flex-1">
            <Loader2 className="animate-spin text-blue-500" size={32} />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center flex-1 text-zinc-500 gap-4">
            <div className="bg-zinc-900 p-6 rounded-full">
              <Bot size={48} className="text-zinc-700" />
            </div>
            <div className="text-center">
              <p className="text-lg font-medium text-zinc-300 mb-1">ابدأ المحادثة</p>
              <p className="text-sm text-zinc-500 max-w-[250px]">
                يمكنني مساعدتك في تنظيم مهامك أو الإجابة على استفساراتك.
              </p>
            </div>
          </div>
        ) : (
          messages.map(msg => (
            <div key={msg.id} className={`flex items-end gap-2 ${msg.role === 'user' ? 'flex-row' : 'flex-row-reverse'}`}>
              <div className={`shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${msg.role === 'user' ? 'bg-blue-600' : 'bg-zinc-800 border border-zinc-700'}`}>
                {msg.role === 'user' ? <UserIcon size={16} className="text-white" /> : <Bot size={16} className="text-blue-400" />}
              </div>
              <div className={`px-4 py-2.5 rounded-2xl max-w-[85%] ${msg.role === 'user' ? 'bg-blue-600 text-white rounded-br-sm' : 'bg-zinc-900 border border-zinc-800 text-zinc-200 rounded-bl-sm'}`}>
                <p className="text-sm whitespace-pre-wrap leading-relaxed">{msg.content}</p>
              </div>
            </div>
          ))
        )}
        {isSending && (
           <div className="flex items-end gap-2 flex-row-reverse">
             <div className="shrink-0 w-8 h-8 rounded-full flex items-center justify-center bg-zinc-800 border border-zinc-700">
               <Bot size={16} className="text-blue-400" />
             </div>
             <div className="px-4 py-3 rounded-2xl max-w-[85%] bg-zinc-900 border border-zinc-800 text-zinc-200 rounded-bl-sm flex items-center gap-2">
               <span className="w-2 h-2 bg-zinc-600 rounded-full animate-bounce"></span>
               <span className="w-2 h-2 bg-zinc-600 rounded-full animate-bounce delay-75"></span>
               <span className="w-2 h-2 bg-zinc-600 rounded-full animate-bounce delay-150"></span>
             </div>
           </div>
        )}
        <div ref={scrollRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 shrink-0 border-t border-zinc-800/50 bg-zinc-900/50 absolute bottom-0 w-full mb-16">
        <form onSubmit={handleSend} className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="تحدث مع الوكيل..."
            className="flex-1 bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm rounded-xl px-4 py-3 outline-none focus:border-blue-500 transition-colors"
          />
          <button 
            type="submit"
            disabled={!input.trim() || isSending}
            className="bg-blue-600 hover:bg-blue-700 text-white p-3 rounded-xl disabled:opacity-50 transition-colors flex items-center justify-center"
          >
            <Send size={20} className={isSending ? 'opacity-0' : ''} />
            {isSending && <Loader2 size={20} className="absolute animate-spin" />}
          </button>
        </form>
      </div>
    </div>
  );
}
