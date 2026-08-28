import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bot, Search, Plus, Code, FileText, CheckCircle2 } from 'lucide-react';
import { collection, query, orderBy, limit, getDocs } from 'firebase/firestore';
import { db, auth } from '../lib/firebase';

interface Activity {
  id: string;
  title: string;
  timestamp: number;
}

export default function HomeScreen() {
  const navigate = useNavigate();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const user = auth.currentUser;

  useEffect(() => {
    const fetchActivities = async () => {
      if (!user) return;
      try {
        const q = query(
          collection(db, 'activities'),
          orderBy('timestamp', 'desc'),
          limit(5)
        );
        const querySnapshot = await getDocs(q);
        const data = querySnapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        })) as Activity[];
        setActivities(data);
      } catch (error) {
        console.error("Error fetching activities:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchActivities();
  }, [user]);

  return (
    <div className="p-4 flex flex-col gap-6" dir="rtl">
      
      {/* Welcome / Quick Action */}
      <section className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
        <div className="relative z-10">
          <h2 className="text-xl font-bold mb-1">مرحباً بك في AI Hub</h2>
          <p className="text-blue-100 text-sm mb-4">اكتب مهمتك ليقوم الوكيل المناسب بتنفيذها.</p>
          <div className="grid grid-cols-2 gap-2 mt-4">
            <button 
              onClick={() => navigate('/task')}
              className="bg-white text-blue-600 px-4 py-2 rounded-lg font-semibold text-sm flex items-center justify-center gap-2 hover:bg-zinc-50 transition-colors"
            >
              <Plus size={16} />
              مهمة جديدة
            </button>
            <button 
              onClick={() => navigate('/history')}
              className="bg-blue-700/50 text-white px-4 py-2 rounded-lg font-semibold text-sm flex items-center justify-center gap-2 hover:bg-blue-700/70 transition-colors"
            >
              <ClockIcon size={16} />
              السجل
            </button>
          </div>
        </div>
        <Bot size={100} className="absolute -left-4 -bottom-4 text-white opacity-20" />
      </section>

      {/* Quick Action Grid */}
      <section>
        <h3 className="text-zinc-100 font-semibold text-sm mb-3">إجراءات سريعة</h3>
        <div className="grid grid-cols-3 gap-3">
          <QuickAction icon={<FileText size={20}/>} label="تقرير" onClick={() => navigate('/task')} />
          <QuickAction icon={<Code size={20}/>} label="كود" onClick={() => navigate('/task')} />
          <QuickAction icon={<Search size={20}/>} label="بحث" onClick={() => navigate('/task')} />
        </div>
      </section>

      {/* Recent Activities */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-zinc-100 font-semibold text-sm">أحدث الأنشطة</h3>
          <button className="text-zinc-400 text-xs hover:text-white transition-colors">عرض الكل</button>
        </div>
        <div className="flex flex-col gap-2">
          {loading ? (
            <div className="text-zinc-500 text-sm text-center py-4">جاري التحميل...</div>
          ) : activities.length > 0 ? (
            activities.map(activity => (
              <div key={activity.id} className="flex items-center gap-3 bg-zinc-800/50 border border-zinc-800 p-3 rounded-xl">
                <CheckCircle2 size={18} className="text-green-500 shrink-0" />
                <div className="flex-1">
                  <h4 className="text-zinc-100 font-medium text-sm">{activity.title}</h4>
                  <p className="text-zinc-500 text-xs mt-0.5">{new Date(activity.timestamp).toLocaleDateString('ar-EG')}</p>
                </div>
              </div>
            ))
          ) : (
            <div className="text-zinc-500 text-sm text-center py-4">لا توجد أنشطة حديثة</div>
          )}
        </div>
      </section>
    </div>
  );
}

function ClockIcon({ size }: { size: number }) {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  );
}

function QuickAction({ icon, label, onClick }: { icon: React.ReactNode, label: string, onClick: () => void }) {
  return (
    <button 
      onClick={onClick}
      className="flex flex-col items-center justify-center gap-2 bg-zinc-800 border border-zinc-700 p-4 rounded-xl hover:border-blue-500/50 hover:bg-zinc-700/50 transition-all"
    >
      <div className="text-blue-400">
        {icon}
      </div>
      <span className="text-zinc-100 font-medium text-xs">{label}</span>
    </button>
  );
}
