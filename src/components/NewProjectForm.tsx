import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Wand2 } from 'lucide-react';

export default function NewProjectForm({ onAdd }: { onAdd: (title: string, prompt: string) => void }) {
  const [title, setTitle] = useState('');
  const [prompt, setPrompt] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (title.trim() && prompt.trim()) {
      onAdd(title.trim(), prompt.trim());
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-8">
        <h2 className="text-3xl font-bold text-zinc-900 mb-2">New Story Project</h2>
        <p className="text-zinc-500 text-lg">Describe the story you want to create and let AI generate the scenes and timeline.</p>
      </div>

      <form onSubmit={handleSubmit} className="bg-white p-6 md:p-8 rounded-xl border border-zinc-200 shadow-sm">
        <div className="mb-6">
          <label htmlFor="title" className="block text-sm font-semibold text-zinc-900 mb-2">Project Title</label>
          <input
            id="title"
            type="text"
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full px-4 py-3 rounded-lg border border-zinc-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all outline-none"
            placeholder="e.g. The Neon City Heist"
          />
        </div>

        <div className="mb-8">
          <label htmlFor="prompt" className="block text-sm font-semibold text-zinc-900 mb-2">Story Prompt</label>
          <textarea
            id="prompt"
            required
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            rows={5}
            className="w-full px-4 py-3 rounded-lg border border-zinc-300 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all outline-none resize-none"
            placeholder="A cyberpunk crew plans an impossible heist at the top of a megacorporation's skyscraper..."
          />
        </div>

        <div className="flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="px-5 py-2.5 rounded-lg text-sm font-medium text-zinc-600 hover:bg-zinc-100 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-medium transition-colors flex items-center gap-2"
          >
            <Wand2 size={16} />
            Create Project
          </button>
        </div>
      </form>
    </div>
  );
}
