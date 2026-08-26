import { Link } from 'react-router-dom';
import { StoryProject } from '../types';
import { Film, LayoutGrid, Clock, AlertCircle } from 'lucide-react';
import { formatDuration } from '../utils';

export default function ProjectList({ projects }: { projects: StoryProject[] }) {
  if (projects.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center bg-white rounded-xl border border-zinc-200 border-dashed">
        <div className="bg-zinc-100 p-4 rounded-full mb-4">
          <Film className="w-8 h-8 text-zinc-400" />
        </div>
        <h2 className="text-xl font-semibold text-zinc-900 mb-2">No projects yet</h2>
        <p className="text-zinc-500 max-w-sm mb-6">Create your first AI story project to start generating scenes, scripts, and timelines.</p>
        <Link to="/new" className="bg-zinc-900 text-white px-5 py-2.5 rounded-md font-medium hover:bg-zinc-800 transition-colors">
          Create New Project
        </Link>
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-2xl font-semibold text-zinc-900 mb-6">Your Projects</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {projects.map(project => (
          <Link key={project.id} to={`/project/${project.id}`} className="group bg-white rounded-xl border border-zinc-200 overflow-hidden hover:shadow-md transition-all">
            <div className="aspect-video bg-zinc-100 flex items-center justify-center p-6 relative overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-indigo-50/50 to-zinc-100/50 mix-blend-multiply" />
              <Film className="w-10 h-10 text-zinc-300 relative z-10 group-hover:scale-110 transition-transform duration-500" />
              <div className="absolute top-3 right-3 z-10">
                <StatusBadge status={project.status} />
              </div>
            </div>
            <div className="p-5">
              <h3 className="font-semibold text-zinc-900 truncate mb-1 text-lg">{project.title}</h3>
              <p className="text-zinc-500 text-sm line-clamp-2 mb-4 leading-relaxed h-10">{project.prompt}</p>
              
              <div className="flex items-center gap-4 text-xs font-medium text-zinc-500 pt-4 border-t border-zinc-100">
                <div className="flex items-center gap-1.5">
                  <LayoutGrid size={14} className="text-zinc-400" />
                  {project.scenes.length} scenes
                </div>
                <div className="flex items-center gap-1.5">
                  <Clock size={14} className="text-zinc-400" />
                  {formatDuration(project.scenes.reduce((acc, s) => acc + s.durationMs, 0))}
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

function StatusBadge({ status }: { status: StoryProject['status'] }) {
  switch (status) {
    case 'DRAFT':
      return <span className="bg-zinc-200 text-zinc-700 text-[10px] uppercase font-bold tracking-wider px-2 py-1 rounded-sm">Draft</span>;
    case 'GENERATING':
      return <span className="bg-blue-100 text-blue-700 text-[10px] uppercase font-bold tracking-wider px-2 py-1 rounded-sm animate-pulse flex items-center gap-1"><Clock size={10} /> Generating</span>;
    case 'READY':
      return <span className="bg-green-100 text-green-700 text-[10px] uppercase font-bold tracking-wider px-2 py-1 rounded-sm">Ready</span>;
    case 'RENDERING':
      return <span className="bg-purple-100 text-purple-700 text-[10px] uppercase font-bold tracking-wider px-2 py-1 rounded-sm animate-pulse">Rendering</span>;
    case 'FAILED':
      return <span className="bg-red-100 text-red-700 text-[10px] uppercase font-bold tracking-wider px-2 py-1 rounded-sm flex items-center gap-1"><AlertCircle size={10} /> Failed</span>;
    default:
      return null;
  }
}
