import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useStore } from './store';
import { Clapperboard, Plus } from 'lucide-react';
import ProjectList from './components/ProjectList';
import ProjectDetail from './components/ProjectDetail';
import NewProjectForm from './components/NewProjectForm';

function App() {
  const store = useStore();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col bg-zinc-50 font-sans">
      <header className="bg-white border-b border-zinc-200 px-6 py-4 flex items-center justify-between sticky top-0 z-10">
        <Link to="/" className="flex items-center gap-3">
          <div className="bg-indigo-600 text-white p-2 rounded-lg">
            <Clapperboard size={20} />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-zinc-900 leading-none">Night Tales Studio</h1>
            <p className="text-xs text-zinc-500 font-medium mt-1">AI Creative Studio</p>
          </div>
        </Link>
        <button 
          onClick={() => navigate('/new')}
          className="flex items-center gap-2 bg-zinc-900 hover:bg-zinc-800 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
        >
          <Plus size={16} />
          New Project
        </button>
      </header>

      <main className="flex-1 max-w-5xl w-full mx-auto p-6 md:p-8">
        <Routes>
          <Route path="/" element={<ProjectList projects={store.projects} />} />
          <Route path="/new" element={<NewProjectForm onAdd={(title, prompt) => {
            const p = store.addProject(title, prompt);
            navigate(`/project/${p.id}`);
          }} />} />
          <Route path="/project/:id" element={<ProjectDetail store={store} />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
