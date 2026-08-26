import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import {
  Bot,
  Clock,
  Home,
  Map as MapIcon,
  MessageSquare,
  Settings,
} from 'lucide-react';
import HomeScreen from './components/HomeScreen';
import MapScreen from './components/MapScreen';
import TaskScreen from './components/TaskScreen';

function App() {
  const location = useLocation();

  return (
    <div
      dir="rtl"
      className="min-h-screen bg-zinc-950 font-sans text-zinc-50 flex justify-center"
    >
      <div className="relative flex min-h-screen w-full max-w-md flex-col overflow-hidden border-x border-zinc-800 bg-zinc-900 shadow-2xl">
        <header className="sticky top-0 z-10 flex items-center justify-between border-b border-zinc-800 bg-zinc-900 px-4 py-4">
          <Link to="/" className="flex items-center gap-2" aria-label="الرئيسية">
            <div className="rounded-lg bg-blue-500 p-1.5 text-white">
              <Bot size={20} aria-hidden="true" />
            </div>
            <h1 className="text-lg font-bold leading-none tracking-tight text-white">
              حكايات AI Hub
            </h1>
          </Link>
        </header>

        <main className="flex-1 overflow-y-auto pb-20 scroll-smooth">
          <Routes>
            <Route path="/" element={<HomeScreen />} />
            <Route path="/task" element={<TaskScreen />} />
            <Route path="/map" element={<MapScreen />} />
            <Route path="/history" element={<Placeholder title="السجل" />} />
            <Route path="/settings" element={<Placeholder title="الإعدادات" />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>

        <nav
          aria-label="التنقل الرئيسي"
          className="absolute bottom-0 z-20 flex w-full items-center justify-between border-t border-zinc-800 bg-zinc-900 px-6 py-3 pb-safe"
        >
          <NavItem
            to="/"
            icon={<Home size={24} />}
            label="الرئيسية"
            active={location.pathname === '/'}
          />
          <NavItem
            to="/task"
            icon={<MessageSquare size={24} />}
            label="المهام"
            active={location.pathname === '/task'}
          />
          <NavItem
            to="/map"
            icon={<MapIcon size={24} />}
            label="الخريطة"
            active={location.pathname === '/map'}
          />
          <NavItem
            to="/history"
            icon={<Clock size={24} />}
            label="السجل"
            active={location.pathname === '/history'}
          />
          <NavItem
            to="/settings"
            icon={<Settings size={24} />}
            label="الإعدادات"
            active={location.pathname === '/settings'}
          />
        </nav>
      </div>
    </div>
  );
}

function Placeholder({ title }: { title: string }) {
  return (
    <section className="flex min-h-[60vh] flex-col items-center justify-center px-6 text-center">
      <h2 className="text-2xl font-bold text-white">{title}</h2>
      <p className="mt-2 text-sm text-zinc-400">هذه الصفحة قيد التطوير.</p>
    </section>
  );
}

function NavItem({
  to,
  icon,
  label,
  active,
}: {
  to: string;
  icon: React.ReactNode;
  label: string;
  active: boolean;
}) {
  return (
    <Link
      to={to}
      aria-current={active ? 'page' : undefined}
      className={`flex flex-col items-center gap-1 transition-colors ${
        active ? 'text-blue-400' : 'text-zinc-500 hover:text-zinc-300'
      }`}
    >
      {icon}
      <span className="text-[10px] font-medium">{label}</span>
    </Link>
  );
}

export default App;
