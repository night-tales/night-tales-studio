import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Eye, Heart, Search } from 'lucide-react';
import { getStories } from '../lib/stories';
import type { Story, StoryGenre, StorySort } from '../types/story';

const genres: Array<{ value: StoryGenre | 'all'; label: string }> = [
  { value: 'all', label: 'الكل' },
  { value: 'horror', label: 'رعب' },
  { value: 'mystery', label: 'غموض' },
  { value: 'thriller', label: 'إثارة' },
  { value: 'fantasy', label: 'فانتازيا' },
  { value: 'sci-fi', label: 'خيال علمي' },
];

export default function StoriesScreen() {
  const [stories, setStories] = useState<Story[]>([]);
  const [search, setSearch] = useState('');
  const [genre, setGenre] = useState<StoryGenre | 'all'>('all');
  const [sortBy, setSortBy] = useState<StorySort>('newest');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');

    getStories({
      search,
      sortBy,
      genre: genre === 'all' ? undefined : genre,
    })
      .then((items) => active && setStories(items))
      .catch(() => active && setError('تعذر تحميل القصص حالياً'))
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [genre, search, sortBy]);

  const emptyLabel = useMemo(
    () => (search ? 'لا توجد قصص مطابقة للبحث' : 'لا توجد قصص منشورة بعد'),
    [search],
  );

  return (
    <section className="p-4 space-y-5">
      <div>
        <p className="text-blue-400 text-sm font-medium mb-1">Night Tales</p>
        <h2 className="text-2xl font-bold">مكتبة القصص</h2>
        <p className="text-zinc-400 text-sm mt-1">اكتشف الحكايات المرعبة والغامضة وشارك قصتك.</p>
      </div>

      <label className="flex items-center gap-2 rounded-xl bg-zinc-800 border border-zinc-700 px-3 py-2">
        <Search size={18} className="text-zinc-500" />
        <input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="ابحث عن قصة..."
          className="w-full bg-transparent outline-none text-sm placeholder:text-zinc-500"
        />
      </label>

      <div className="flex gap-2 overflow-x-auto pb-1">
        {genres.map((item) => (
          <button
            key={item.value}
            onClick={() => setGenre(item.value)}
            className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-medium ${
              genre === item.value ? 'bg-blue-500 text-white' : 'bg-zinc-800 text-zinc-400'
            }`}
          >
            {item.label}
          </button>
        ))}
      </div>

      <select
        value={sortBy}
        onChange={(event) => setSortBy(event.target.value as StorySort)}
        className="bg-zinc-800 border border-zinc-700 rounded-lg px-3 py-2 text-sm outline-none"
      >
        <option value="newest">الأحدث</option>
        <option value="oldest">الأقدم</option>
        <option value="popular">الأكثر شعبية</option>
      </select>

      {error ? <p className="text-red-400 text-sm">{error}</p> : null}
      {loading ? <p className="text-zinc-500 text-sm">جاري تحميل القصص...</p> : null}
      {!loading && !error && stories.length === 0 ? (
        <div className="text-center py-10 text-zinc-500">
          <BookOpen className="mx-auto mb-3" size={32} />
          <p>{emptyLabel}</p>
        </div>
      ) : null}

      <div className="space-y-3">
        {stories.map((story) => (
          <article key={story.id} className="rounded-2xl bg-zinc-800/80 border border-zinc-700 p-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h3 className="font-bold text-lg">{story.title}</h3>
                <p className="text-xs text-blue-400 mt-1">{genres.find((item) => item.value === story.genre)?.label}</p>
              </div>
              <span className="text-xs text-zinc-500">{story.authorName}</span>
            </div>
            <p className="text-sm text-zinc-400 leading-6 mt-3 line-clamp-3">{story.excerpt || story.content}</p>
            <div className="flex items-center justify-between mt-4">
              <div className="flex gap-3 text-xs text-zinc-500">
                <span className="flex items-center gap-1"><Heart size={14} />{story.likes}</span>
                <span className="flex items-center gap-1"><Eye size={14} />{story.views}</span>
              </div>
              <Link to={`/stories/${story.id}`} className="text-sm text-blue-400 hover:text-blue-300">قراءة القصة</Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
