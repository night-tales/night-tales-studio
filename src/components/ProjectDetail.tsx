import { useParams, Link } from 'react-router-dom';
import { useStore } from '../store';
import { ArrowLeft, Play, Wand2, Image as ImageIcon, Video, Music, Settings, Clock, Loader2 } from 'lucide-react';
import { formatDuration } from '../utils';

export default function ProjectDetail({ store }: { store: ReturnType<typeof useStore> }) {
  const { id } = useParams<{ id: string }>();
  const project = id ? store.getProject(id) : undefined;

  if (!project) {
    return (
      <div className="text-center py-20">
        <h2 className="text-xl font-semibold mb-4">Project not found</h2>
        <Link to="/" className="text-indigo-600 hover:underline">Return home</Link>
      </div>
    );
  }

  const handleGenerate = () => {
    if (project.status === 'DRAFT') {
      store.mockGenerateProject(project.id);
    }
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-zinc-900 transition-colors">
          <ArrowLeft size={16} />
          Back to Projects
        </Link>
        <div className="flex items-center gap-3">
          <button className="p-2 text-zinc-400 hover:text-zinc-900 bg-white rounded-md border border-zinc-200 transition-colors">
            <Settings size={18} />
          </button>
          <button 
            disabled={project.status !== 'READY'}
            className="flex items-center gap-2 bg-zinc-900 disabled:bg-zinc-300 disabled:cursor-not-allowed hover:bg-zinc-800 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
          >
            <Play size={16} />
            Export Final Video
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-zinc-200 overflow-hidden mb-8">
        <div className="p-6 md:p-8 border-b border-zinc-100">
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h1 className="text-3xl font-bold text-zinc-900">{project.title}</h1>
                <span className={`px-2.5 py-1 text-[10px] uppercase font-bold tracking-wider rounded-sm ${
                  project.status === 'READY' ? 'bg-green-100 text-green-700' :
                  project.status === 'GENERATING' ? 'bg-blue-100 text-blue-700 animate-pulse' :
                  'bg-zinc-200 text-zinc-700'
                }`}>
                  {project.status}
                </span>
              </div>
              <p className="text-zinc-600 text-lg leading-relaxed max-w-3xl">{project.prompt}</p>
            </div>
            
            {project.status === 'DRAFT' && (
              <button 
                onClick={handleGenerate}
                className="whitespace-nowrap flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-lg font-medium transition-colors shadow-sm"
              >
                <Wand2 size={18} />
                Generate Scenes
              </button>
            )}
            {project.status === 'GENERATING' && (
              <button 
                disabled
                className="whitespace-nowrap flex items-center gap-2 bg-indigo-400 text-white px-6 py-3 rounded-lg font-medium shadow-sm cursor-not-allowed"
              >
                <Loader2 size={18} className="animate-spin" />
                Generating...
              </button>
            )}
          </div>
        </div>
        
        <div className="bg-zinc-50 p-6">
          <div className="flex items-center gap-6 text-sm font-medium text-zinc-500">
            <div className="flex items-center gap-2">
              <Clock size={16} />
              Total Duration: {formatDuration(project.scenes.reduce((acc, s) => acc + s.durationMs, 0))}
            </div>
            <div className="flex items-center gap-2">
              <ImageIcon size={16} />
              {project.scenes.length} Scenes
            </div>
          </div>
        </div>
      </div>

      <div>
        <h2 className="text-xl font-bold text-zinc-900 mb-4 flex items-center gap-2">
          <Video size={20} className="text-zinc-400" />
          Timeline & Scenes
        </h2>
        
        {project.scenes.length === 0 ? (
          <div className="text-center py-16 bg-white border border-zinc-200 border-dashed rounded-xl">
            <Wand2 className="w-10 h-10 text-zinc-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-zinc-900 mb-1">No scenes generated yet</h3>
            <p className="text-zinc-500 max-w-sm mx-auto">Click "Generate Scenes" above to let AI plan your story blueprint and media assets.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {project.scenes.map((scene) => (
              <div key={scene.id} className="bg-white border border-zinc-200 rounded-xl p-5 flex flex-col md:flex-row gap-6 hover:border-indigo-200 transition-colors group">
                <div className="w-full md:w-64 aspect-video bg-zinc-100 rounded-lg flex items-center justify-center relative overflow-hidden shrink-0 border border-zinc-200">
                  <ImageIcon size={32} className="text-zinc-300 relative z-10" />
                  <div className="absolute inset-0 bg-gradient-to-tr from-zinc-200/50 to-transparent"></div>
                  <div className="absolute bottom-2 right-2 bg-black/60 backdrop-blur-sm text-white text-[10px] font-bold px-1.5 py-0.5 rounded">
                    {formatDuration(scene.durationMs)}
                  </div>
                </div>
                
                <div className="flex-1 flex flex-col justify-center">
                  <div className="text-xs font-bold text-indigo-600 mb-1 tracking-wider uppercase">Scene {scene.index + 1}</div>
                  <h4 className="text-lg font-semibold text-zinc-900 mb-2">{scene.description}</h4>
                  
                  {scene.imagePrompt && (
                    <div className="mt-3 bg-zinc-50 p-3 rounded-lg border border-zinc-100">
                      <div className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider mb-1 flex items-center gap-1"><ImageIcon size={12}/> Visual Prompt</div>
                      <p className="text-sm text-zinc-700 font-mono text-xs">{scene.imagePrompt}</p>
                    </div>
                  )}
                  
                  {scene.narrationText && (
                    <div className="mt-3 bg-indigo-50/50 p-3 rounded-lg border border-indigo-100">
                      <div className="text-[10px] font-bold text-indigo-500 uppercase tracking-wider mb-1 flex items-center gap-1"><Music size={12}/> Narration / Voiceover</div>
                      <p className="text-sm text-indigo-900 italic">"{scene.narrationText}"</p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
