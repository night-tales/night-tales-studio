import { useState, useEffect } from 'react';
import { Plus, Trash2, CheckCircle2, Circle, ListTodo } from 'lucide-react';
import { db, auth } from '../lib/firebase';
import { collection, query, orderBy, onSnapshot, addDoc, deleteDoc, doc, updateDoc, serverTimestamp, where } from 'firebase/firestore';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';

interface Task {
  id: string;
  title: string;
  completed: boolean;
  createdAt: any;
  userId: string;
}

export default function TaskScreen() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const user = auth.currentUser;

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }

    const q = query(
      collection(db, 'tasks'),
      where('userId', '==', user.uid),
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
    try {
      await addDoc(collection(db, 'tasks'), {
        title: newTaskTitle.trim(),
        completed: false,
        createdAt: serverTimestamp(),
        userId: user.uid
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

  const deleteTask = async (id: string) => {
    try {
      await deleteDoc(doc(db, 'tasks', id));
    } catch (error) {
      handleFirestoreError(error, OperationType.DELETE, 'tasks');
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
            placeholder="أضف مهمة جديدة..."
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
          tasks.map(task => (
            <div 
              key={task.id} 
              className={`bg-zinc-900 border ${task.completed ? 'border-zinc-800/50 opacity-60' : 'border-zinc-700'} rounded-xl p-4 flex items-center gap-3 transition-all`}
            >
              <button 
                onClick={() => toggleTask(task)}
                className={`shrink-0 ${task.completed ? 'text-green-500' : 'text-zinc-400 hover:text-blue-400'} transition-colors`}
              >
                {task.completed ? <CheckCircle2 size={24} /> : <Circle size={24} />}
              </button>
              
              <div className="flex-1 min-w-0">
                <p className={`text-sm ${task.completed ? 'text-zinc-500 line-through' : 'text-zinc-200'} whitespace-pre-wrap leading-relaxed`}>
                  {task.title}
                </p>
              </div>

              <button 
                onClick={() => deleteTask(task.id)}
                className="shrink-0 text-zinc-500 hover:text-red-400 transition-colors p-2 rounded-lg hover:bg-zinc-800"
              >
                <Trash2 size={18} />
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
