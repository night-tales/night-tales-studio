import { Routes, Route, Link, useLocation } from 'react-router-dom';
import { Home, MessageSquare, Clock, Settings, Bot, Map as MapIcon, LogOut } from 'lucide-react';
import HomeScreen from './components/HomeScreen';
import TaskScreen from './components/TaskScreen';
import MapScreen from './components/MapScreen';
import HistoryScreen from './components/HistoryScreen';
import SettingsScreen from './components/SettingsScreen';
import AuthScreen from './components/AuthScreen';
import { onAuthStateChanged, signOut, User } from 'firebase/auth';
import { auth } from './lib/firebase';
import { useEffect, useState } from 'react';

function App() {
  const location = useLocation();
  const [user, setUser] = useState<User | null>(null);
  const [authLoading, setAuthLoading] = useState(true);

  useEffect(() => onAuthStateChanged(auth, currentUser => {
    setUser(currentUser);
    setAuthLoading(false);
  }), []);

  if (authLoading) {
    return <div className="min-h-screen bg-zinc-950 text-zinc-400 flex items-center justify-center">جاري التحقق من الهوية...</div>;
  }

  if (!user) return <AuthScreen />;

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-50 font-sans flex justify-center">
      {/* Mobile App Container Simulation */}
      <div className="w-full max-w-md bg-zinc-900 min-h-screen flex flex-col relative shadow-2xl overflow-hidden border-x border-zinc-800">
        
        {/* Header */}
        <header className="bg-zinc-900 border-b border-zinc-800 px-4 py-4 flex items-center justify-between sticky top-0 z-10">
          <Link to="/" className="flex items-center gap-2">
            <div className="bg-blue-500 text-white p-1.5 rounded-lg">
              <Bot size={20} />
            </div>
            <h1 className="text-lg font-bold tracking-tight leading-none text-white">Night Tales Studio</h1>
          </Link>
          <button onClick={() => signOut(auth)} className="text-zinc-500 hover:text-zinc-200" aria-label="تسجيل الخروج"><LogOut size={18} /></button>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto pb-20 scroll-smooth">
          <Routes>
            <Route path="/" element={<HomeScreen />} />
            <Route path="/task" element={<TaskScreen />} />
            <Route path="/map" element={<MapScreen />} />
            <Route path="/history" element={<HistoryScreen />} />
            <Route path="/settings" element={<SettingsScreen />} />
          </Routes>
        </main>

        {/* Bottom Navigation */}
        <nav className="absolute bottom-0 w-full bg-zinc-900 border-t border-zinc-800 px-6 py-3 flex justify-between items-center z-20 pb-safe">
          <NavItem to="/" icon={<Home size={24} />} label="الرئيسية" active={location.pathname === '/'} />
          <NavItem to="/task" icon={<MessageSquare size={24} />} label="المهام" active={location.pathname === '/task'} />
          <NavItem to="/map" icon={<MapIcon size={24} />} label="الخريطة" active={location.pathname === '/map'} />
          <NavItem to="/history" icon={<Clock size={24} />} label="السجل" active={location.pathname === '/history'} />
          <NavItem to="/settings" icon={<Settings size={24} />} label="الإعدادات" active={location.pathname === '/settings'} />
        </nav>
      </div>
    </div>
  );
}

function NavItem({ to, icon, label, active }: { to: string, icon: React.ReactNode, label: string, active: boolean }) {
  return (
    <Link to={to} className={`flex flex-col items-center gap-1 transition-colors ${active ? 'text-blue-400' : 'text-zinc-500 hover:text-zinc-300'}`}>
      {icon}
      <span className="text-[10px] font-medium">{label}</span>
    </Link>
  );
}

export default App;
