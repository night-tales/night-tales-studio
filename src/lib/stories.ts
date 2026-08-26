import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  where,
  type DocumentData,
  type QueryConstraint,
} from 'firebase/firestore';
import { auth, db } from './firebase';
import type { Story, StoryFilters, StoryInput } from '../types/story';

const STORIES_COLLECTION = 'stories';

function toMillis(value: DocumentData['createdAt']): number {
  if (typeof value === 'number') return value;
  if (value?.toMillis) return value.toMillis();
  return Date.now();
}

function mapStory(id: string, data: DocumentData): Story {
  return {
    id,
    title: String(data.title ?? ''),
    excerpt: String(data.excerpt ?? ''),
    content: String(data.content ?? ''),
    genre: data.genre,
    authorId: String(data.authorId ?? ''),
    authorName: String(data.authorName ?? 'كاتب مجهول'),
    authorImage: data.authorImage,
    isPublished: data.isPublished === true,
    likes: Number(data.likes ?? 0),
    views: Number(data.views ?? 0),
    commentsCount: Number(data.commentsCount ?? 0),
    createdAt: toMillis(data.createdAt),
    updatedAt: toMillis(data.updatedAt),
  };
}

export async function getStory(id: string): Promise<Story | null> {
  const snapshot = await getDoc(doc(db, STORIES_COLLECTION, id));
  return snapshot.exists() ? mapStory(snapshot.id, snapshot.data()) : null;
}

export async function getStories(filters: StoryFilters = {}): Promise<Story[]> {
  const constraints: QueryConstraint[] = [
    where('isPublished', '==', true),
    limit(30),
  ];

  if (filters.sortBy === 'oldest') {
    constraints.splice(1, 0, orderBy('createdAt', 'asc'));
  } else if (filters.sortBy === 'popular') {
    constraints.splice(1, 0, orderBy('likes', 'desc'));
  } else {
    constraints.splice(1, 0, orderBy('createdAt', 'desc'));
  }

  if (filters.genre) {
    constraints.unshift(where('genre', '==', filters.genre));
  }

  const snapshot = await getDocs(query(collection(db, STORIES_COLLECTION), ...constraints));
  const stories = snapshot.docs.map((item) => mapStory(item.id, item.data()));
  const search = filters.search?.trim().toLocaleLowerCase();

  if (!search) return stories;
  return stories.filter((story) =>
    `${story.title} ${story.excerpt} ${story.content}`.toLocaleLowerCase().includes(search),
  );
}

export async function createStory(input: StoryInput): Promise<string> {
  const user = auth.currentUser;
  if (!user) throw new Error('يجب تسجيل الدخول أولاً');

  const now = Date.now();
  const reference = await addDoc(collection(db, STORIES_COLLECTION), {
    ...input,
    authorId: user.uid,
    authorName: user.displayName ?? 'كاتب مجهول',
    authorImage: user.photoURL ?? null,
    isPublished: input.isPublished === true,
    likes: 0,
    views: 0,
    commentsCount: 0,
    createdAt: now,
    updatedAt: serverTimestamp(),
  });

  return reference.id;
}

export async function updateStory(id: string, input: Partial<StoryInput>): Promise<void> {
  const user = auth.currentUser;
  if (!user) throw new Error('يجب تسجيل الدخول أولاً');
  await updateDoc(doc(db, STORIES_COLLECTION, id), {
    ...input,
    updatedAt: serverTimestamp(),
  });
}
