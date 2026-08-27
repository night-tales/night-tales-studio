import { useState, useEffect } from 'react';
import { Bot, User, Clock, Search, Trash2 } from 'lucide-react';
import { db } from '../lib/firebase';
import { collection, query, orderBy, onSnapshot, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';

interface HistoryMessage {
  id: string;
  role: 'user' | 'assistant' | 'tool';
  content: string;
  agentName?: string;
  createdAt: any;
}

export default function HistoryScreen() {
  const [messages, setMessages] = useState<HistoryMessage[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const q = query(collection(db, 'messages'), orderBy('createdAt', 'desc'));
    
    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const msgs = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as HistoryMessage[];
        
        setMessages(msgs);
        setLoading(false);
      },
      (error) => {
        handleFirestoreError(error, OperationType.GET, 'messages');
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const clearHistory = async () => {
    if (!window.confirm('هل أنت متأكد من مسح جميع سجلات المحادثة؟')) return;
    try {
      const q = query(collection(db, 'messages'));
      const snapshot = await getDocs(q);
      const deletePromises = snapshot.docs.map(document => deleteDoc(doc(db, 'messages', document.id)));
      await Promise.all(deletePromises);
    } catch (error) {
      handleFirestoreError(error, OperationType.DELETE, 'messages');
    }
  };

  const filteredMessages = messages.filter(msg => 
    msg.content.toLowerCase().includes(searchQuery.toLowerCase()) || 
    msg.agentName?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950" dir="rtl">
      {/* Header */}
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm flex justify-between items-center">
        <div className="flex items-center gap-2 text-zinc-100">
          <Clock size={20} className="text-blue-500" />
          <h2 className="font-bold">مراجعات المحادثة</h2>
        </div>
        <button 
          onClick={clearHistory}
          className="text-red-400 hover:text-red-300 transition-colors p-2 rounded-lg hover:bg-red-400/10"
          title="مسح السجل"
        >
          <Trash2 size={18} />
        </button>
      </div>

      {/* Search */}
      <div className="p-4 shrink-0 border-b border-zinc-800/50 bg-zinc-900/50">
        <div className="relative">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="البحث في المحادثات..."
            className="w-full bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm rounded-lg pr-10 pl-3 py-2 outline-none focus:border-blue-500 transition-colors"
          />
          <Search size={16} className="absolute right-3 top-2.5 text-zinc-500" />
        </div>
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-32">
        {loading ? (
          <div className="flex justify-center items-center h-40">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
        ) : filteredMessages.length === 0 ? (
          <div className="flex flex-col items-center justify-center text-zinc-500 gap-3 h-40">
            <Clock size={48} className="opacity-20" />
            <p className="text-sm">لا توجد محادثات سابقة</p>
          </div>
        ) : (
          filteredMessages.map(msg => (
            <div key={msg.id} className="bg-zinc-900 border border-zinc-800 rounded-xl p-4 flex gap-3">
              <div className={`shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${msg.role === 'user' ? 'bg-blue-600' : 'bg-zinc-700'}`}>
                {msg.role === 'user' ? <User size={16} className="text-white"/> : <Bot size={16} className="text-zinc-300"/>}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start mb-1">
                  <span className="text-xs font-medium text-zinc-400">
                    {msg.role === 'user' ? 'أنت' : (msg.agentName || 'المساعد الذكي')}
                  </span>
                  {msg.createdAt && (
                    <span className="text-[10px] text-zinc-600">
                      {new Date(msg.createdAt?.seconds * 1000).toLocaleString('ar-SA')}
                    </span>
                  )}
                </div>
                <p className="text-sm text-zinc-200 whitespace-pre-wrap leading-relaxed line-clamp-3">
                  {msg.content}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
