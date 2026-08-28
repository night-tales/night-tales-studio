import { useNavigate } from 'react-router-dom';
import { Bot, ChevronLeft, Search, Plus, Code, FileText } from 'lucide-react';
import { AVAILABLE_AGENTS } from '../models';

export default function HomeScreen() {
  const navigate = useNavigate();

  return (
    <div className="p-4 flex flex-col gap-6" dir="rtl">
      
      {/* Welcome / Quick Action */}
      <section className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
        <div className="relative z-10">
          <h2 className="text-xl font-bold mb-1">مرحباً بك في AI Hub</h2>
          <p className="text-blue-100 text-sm mb-4">اكتب مهمتك ليقوم الوكيل المناسب بتنفيذها.</p>
          <button 
            onClick={() => navigate('/task')}
            className="bg-white text-blue-600 px-4 py-2 rounded-lg font-semibold text-sm flex items-center gap-2 hover:bg-zinc-50 transition-colors"
          >
            <Plus size={16} />
            إنشاء مهمة جديدة
          </button>
        </div>
        <Bot size={100} className="absolute -left-4 -bottom-4 text-white opacity-20" />
      </section>

      {/* Agents */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-zinc-100 font-semibold text-sm">وكلاء الذكاء الاصطناعي</h3>
          <button className="text-zinc-400 text-xs hover:text-white transition-colors">عرض الكل</button>
        </div>
        <div className="grid grid-cols-2 gap-3">
          {AVAILABLE_AGENTS.slice(0, 4).map(agent => (
            <div key={agent.id} className="bg-zinc-800 border border-zinc-700 p-3 rounded-xl hover:border-blue-500/50 transition-colors cursor-pointer" onClick={() => navigate('/task', { state: { agentId: agent.id } })}>
              <div className="flex items-center gap-2 mb-2">
                <div className="bg-zinc-700 p-1.5 rounded-lg text-blue-400">
                  <Bot size={16} />
                </div>
                <h4 className="text-zinc-100 font-semibold text-xs line-clamp-1">{agent.name}</h4>
              </div>
              <p className="text-zinc-400 text-[10px] line-clamp-2">{agent.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Suggested Tasks */}
      <section>
        <h3 className="text-zinc-100 font-semibold text-sm mb-3">مهام مقترحة</h3>
        <div className="flex flex-col gap-2">
          <SuggestedTask icon={<FileText size={18}/>} title="كتابة مقال أو تقرير" desc="استخدم Claude لصياغة نصوص طويلة." />
          <SuggestedTask icon={<Code size={18}/>} title="كتابة وتصحيح كود" desc="استخدم ChatGPT للمساعدة في البرمجة." />
          <SuggestedTask icon={<Search size={18}/>} title="بحث وجمع بيانات" desc="استخدم Gemini للبحث في الويب." />
        </div>
      </section>
    </div>
  );
}

function SuggestedTask({ icon, title, desc }: { icon: React.ReactNode, title: string, desc: string }) {
  const navigate = useNavigate();
  return (
    <button 
      onClick={() => navigate('/task')}
      className="flex items-center gap-3 bg-zinc-800/50 border border-zinc-800 p-3 rounded-xl text-right hover:bg-zinc-800 transition-colors"
    >
      <div className="bg-zinc-800 text-zinc-300 p-2 rounded-lg shrink-0">
        {icon}
      </div>
      <div className="flex-1">
        <h4 className="text-zinc-100 font-medium text-sm">{title}</h4>
        <p className="text-zinc-500 text-xs mt-0.5">{desc}</p>
      </div>
      <ChevronLeft size={16} className="text-zinc-600" />
    </button>
  );
}
