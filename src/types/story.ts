export type StoryGenre = 'horror' | 'mystery' | 'thriller' | 'fantasy' | 'sci-fi';
export type StorySort = 'newest' | 'oldest' | 'popular';

export interface Story {
  id: string;
  title: string;
  excerpt: string;
  content: string;
  genre: StoryGenre;
  authorId: string;
  authorName: string;
  authorImage?: string;
  isPublished: boolean;
  likes: number;
  views: number;
  commentsCount: number;
  createdAt: number;
  updatedAt: number;
}

export interface StoryInput {
  title: string;
  excerpt: string;
  content: string;
  genre: StoryGenre;
  isPublished?: boolean;
}

export interface StoryFilters {
  genre?: StoryGenre;
  search?: string;
  sortBy?: StorySort;
}
