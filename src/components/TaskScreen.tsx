import { useState, useEffect } from 'react';
import { Plus, Trash2, CheckCircle2, Circle, ListTodo, Users, Tag, X } from 'lucide-react';
import { db, auth } from '../lib/firebase';
import { collection, query, orderBy, onSnapshot, addDoc, deleteDoc, doc, updateDoc, serverTimestamp, where, or } from 'firebase/firestore';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';

interface Task {
  id: string;
  title: string;
  completed: boolean;
  createdAt: any;
  userId: string;
  tags?: string[];
  sharedWithEmails?: string[];
}

export default function TaskScreen() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sharingTaskId, setSharingTaskId] = useState<string | null>(null);
  const [shareEmail, setShareEmail] = useState('');
  const user = auth.currentUser;

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }
    
    // We want tasks where userId == user.uid OR sharedWithEmails contains user.email
    // Note: Firestore 'or' query needs to be structured carefully
    const q = query(
      collection(db, 'tasks'),
      or(
        where('userId', '==', user.uid),
        where('sharedWithEmails', 'array-contains', user.email)
      ),
      orderBy('createdAt', 'desc')
    );
    
    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const tasksData = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as Task[];
        
        setTasks(tasksData);
        setLoading(false);
      },
      (error) => {
        handleFirestoreError(error, OperationType.GET, 'tasks');
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [user]);

  const handleAddTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTaskTitle.trim() || !user || isSubmitting) return;

    setIsSubmitting(true);
    
    // Extract hashtags from title
    const titleRegex = /(?:^|\s)(?:#)([a-zA-Z\d\u0600-\u06FF]+)/gm;
    const matches = Array.from(newTaskTitle.matchAll(titleRegex));
    const tags = matches.map(m => m[1]);
    const cleanTitle = newTaskTitle.replace(titleRegex, '').trim();

    try {
      await addDoc(collection(db, 'tasks'), {
        title: cleanTitle || newTaskTitle.trim(),
        completed: false,
        createdAt: serverTimestamp(),
        userId: user.uid,
        tags: tags,
        sharedWithEmails: []
      });
      setNewTaskTitle('');
    } catch (error) {
      handleFirestoreError(error, OperationType.CREATE, 'tasks');
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleTask = async (task: Task) => {
    try {
      await updateDoc(doc(db, 'tasks', task.id), {
        completed: !task.completed
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, 'tasks');
    }
  };

  const deleteTask = async (task: Task) => {
    if (task.userId !== user?.uid) return; // Only owner can delete
    try {
      await deleteDoc(doc(db, 'tasks', task.id));
    } catch (error) {
      handleFirestoreError(error, OperationType.DELETE, 'tasks');
    }
  };
  
  const handleShare = async (e: React.FormEvent, taskId: string, currentShared: string[]) => {
    e.preventDefault();
    if (!shareEmail.trim()) return;
    try {
      await updateDoc(doc(db, 'tasks', taskId), {
        sharedWithEmails: [...(currentShared || []), shareEmail.trim().toLowerCase()]
      });
      setShareEmail('');
      setSharingTaskId(null);
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, 'tasks');
    }
  };

  const removeShare = async (taskId: string, currentShared: string[], emailToRemove: string) => {
    try {
      await updateDoc(doc(db, 'tasks', taskId), {
        sharedWithEmails: currentShared.filter(e => e !== emailToRemove)
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.UPDATE, 'tasks');
    }
  };

  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950" dir="rtl">
      {/* Header */}
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm flex items-center gap-2 text-zinc-100">
        <ListTodo size={20} className="text-blue-500" />
        <h2 className="font-bold">إدارة المهام</h2>
      </div>

      {/* Input Area */}
      <div className="p-4 shrink-0 border-b border-zinc-800/50 bg-zinc-900/50">
        <form onSubmit={handleAddTask} className="flex gap-2">
          <input
            type="text"
            value={newTaskTitle}
            onChange={(e) => setNewTaskTitle(e.target.value)}
            placeholder="أضف مهمة... (يمكنك استخدام # لإضافة تصنيف)"
            className="flex-1 bg-zinc-800 border border-zinc-700 text-zinc-100 text-sm rounded-lg px-3 py-2.5 outline-none focus:border-blue-500 transition-colors"
          />
          <button 
            type="submit"
            disabled={!newTaskTitle.trim() || isSubmitting}
            className="bg-blue-600 text-white p-2.5 rounded-lg disabled:opacity-50 disabled:bg-zinc-700 transition-colors"
          >
            <Plus size={20} />
          </button>
        </form>
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3 pb-32 flex flex-col">
        {loading ? (
          <div className="flex justify-center items-center flex-1">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
        ) : tasks.length === 0 ? (
          <div className="flex flex-col items-center justify-center flex-1 text-zinc-500 gap-4">
            <div className="bg-zinc-900 p-6 rounded-full relative">
              <div className="absolute top-0 right-0 bg-blue-500 w-3 h-3 rounded-full animate-ping"></div>
              <ListTodo size={48} className="text-zinc-700" />
            </div>
            <div className="text-center">
              <p className="text-lg font-medium text-zinc-300 mb-1">لا توجد مهام بعد</p>
              <p className="text-sm text-zinc-500 max-w-[250px]">
                أضف مهمتك الأولى بالأعلى وسيقوم الوكيل بمتابعتها.
              </p>
            </div>
          </div>
        ) : (
          tasks.map(task => {
            const isOwner = task.userId === user?.uid;
            const isSharingOpen = sharingTaskId === task.id;
            
            return (
              <div key={task.id} className="flex flex-col gap-2">
                <div 
                  className={`bg-zinc-900 border ${task.completed ? 'border-zinc-800/50 opacity-60' : 'border-zinc-700'} rounded-xl p-4 flex items-start gap-3 transition-all`}
                >
                  <button 
                    onClick={() => toggleTask(task)}
                    className={`shrink-0 mt-0.5 ${task.completed ? 'text-green-500' : 'text-zinc-400 hover:text-blue-400'} transition-colors`}
                  >
                    {task.completed ? <CheckCircle2 size={22} /> : <Circle size={22} />}
                  </button>
                  
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm ${task.completed ? 'text-zinc-500 line-through' : 'text-zinc-200'} whitespace-pre-wrap leading-relaxed`}>
                      {task.title}
                    </p>
                    
                    {/* Tags */}
                    {task.tags && task.tags.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mt-2">
                        {task.tags.map(tag => (
                          <span key={tag} className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 text-xs font-medium border border-blue-500/20">
                            <Tag size={10} />
                            {tag}
                          </span>
                        ))}
                      </div>
                    )}
                    
                    {/* Shared Indicators */}
                    {task.sharedWithEmails && task.sharedWithEmails.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-2">
                        {task.sharedWithEmails.map(email => (
                          <span key={email} className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-zinc-800 text-zinc-400 text-[10px] font-medium border border-zinc-700">
                            <Users size={10} />
                            {email}
                            {isOwner && (
                              <button onClick={() => removeShare(task.id, task.sharedWithEmails!, email)} className="hover:text-red-400 ml-1">
                                <X size={10} />
                              </button>
                            )}
                          </span>
                        ))}
                      </div>
                    )}
                    {!isOwner && (
                      <div className="mt-2 inline-flex px-2 py-0.5 rounded bg-amber-500/10 text-amber-500/80 text-[10px] border border-amber-500/20">
                        مشاركة
                      </div>
                    )}
                  </div>
                  
                  <div className="shrink-0 flex items-center gap-1">
                    {isOwner && (
                      <>
                        <button 
                          onClick={() => setSharingTaskId(isSharingOpen ? null : task.id)}
                          className={`text-zinc-500 hover:text-blue-400 transition-colors p-2 rounded-lg hover:bg-zinc-800 ${isSharingOpen ? 'bg-zinc-800 text-blue-400' : ''}`}
                        >
                          <Users size={16} />
                        </button>
                        <button 
                          onClick={() => deleteTask(task)}
                          className="text-zinc-500 hover:text-red-400 transition-colors p-2 rounded-lg hover:bg-zinc-800"
                        >
                          <Trash2 size={16} />
                        </button>
                      </>
                    )}
                  </div>
                </div>
                
                {/* Share Input Area */}
                {isSharingOpen && (
                  <form onSubmit={(e) => handleShare(e, task.id, task.sharedWithEmails || [])} className="flex gap-2 px-2 pb-2">
                    <input
                      type="email"
                      value={shareEmail}
                      onChange={(e) => setShareEmail(e.target.value)}
                      placeholder="أدخل بريد الشخص للمشاركة..."
                      className="flex-1 bg-zinc-900 border border-zinc-700 text-zinc-100 text-xs rounded-lg px-3 py-2 outline-none focus:border-blue-500"
                    />
                    <button 
                      type="submit"
                      disabled={!shareEmail.trim()}
                      className="bg-blue-600 text-white px-3 py-2 text-xs rounded-lg disabled:opacity-50"
                    >
                      مشاركة
                    </button>
                  </form>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
