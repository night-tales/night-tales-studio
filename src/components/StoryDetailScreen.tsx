import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowRight, Eye, Heart } from 'lucide-react';
import { getStory } from '../lib/stories';
import type { Story } from '../types/story';

export default function StoryDetailScreen() {
  const { id } = useParams<{ id: string }>();
  const [story, setStory] = useState<Story | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    getStory(id).then(setStory).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="p-6 text-zinc-500">جاري تحميل القصة...</div>;
  if (!story) {
    return (
      <div className="p-6 text-center">
        <p className="text-zinc-400 mb-4">القصة غير موجودة</p>
        <Link to="/stories" className="text-blue-400">العودة إلى المكتبة</Link>
      </div>
    );
  }

  return (
    <article className="p-5">
      <Link to="/stories" className="inline-flex items-center gap-2 text-zinc-400 text-sm mb-6">
        <ArrowRight size={16} /> العودة إلى القصص
      </Link>
      <p className="text-blue-400 text-sm mb-2">{story.genre}</p>
      <h1 className="text-3xl font-bold leading-tight">{story.title}</h1>
      <p className="text-zinc-500 text-sm mt-2">بقلم {story.authorName}</p>
      <div className="flex gap-4 text-xs text-zinc-500 mt-4 pb-5 border-b border-zinc-800">
        <span className="flex items-center gap-1"><Heart size={14} />{story.likes}</span>
        <span className="flex items-center gap-1"><Eye size={14} />{story.views}</span>
      </div>
      <div className="mt-6 whitespace-pre-wrap text-zinc-200 leading-8 text-[15px]">{story.content}</div>
    </article>
  );
}
