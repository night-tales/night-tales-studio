import { useState, useEffect, useCallback } from 'react';
import { StoryProject, Scene } from './types';
import { generateId } from './utils';

const STORAGE_KEY = 'night_tales_projects';

export function useStore() {
  const [projects, setProjects] = useState<StoryProject[]>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (e) {
      console.error('Failed to parse stored projects', e);
    }
    return [];
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(projects));
  }, [projects]);

  const addProject = useCallback((title: string, prompt: string) => {
    const newProject: StoryProject = {
      id: generateId(),
      title,
      prompt,
      status: 'DRAFT',
      scenes: [],
    };
    setProjects((prev) => [...prev, newProject]);
    return newProject;
  }, []);

  const updateProject = useCallback((updatedProject: StoryProject) => {
    setProjects((prev) => prev.map(p => p.id === updatedProject.id ? updatedProject : p));
  }, []);

  const getProject = useCallback((id: string) => {
    return projects.find(p => p.id === id);
  }, [projects]);

  const mockGenerateProject = useCallback((projectId: string) => {
    const project = getProject(projectId);
    if (!project) return;
    
    updateProject({ ...project, status: 'GENERATING' });
    
    // Simulate generation delay
    setTimeout(() => {
      const p = getProject(projectId);
      if (!p) return;
      
      const scenes: Scene[] = [
        {
          id: generateId(),
          index: 0,
          description: `Establishing shot for ${p.title}. ${p.prompt}`,
          durationMs: 5000,
          imagePrompt: `Cinematic wide shot, ${p.prompt}`,
          narrationText: `It begins with a single thought: ${p.title}.`
        },
        {
          id: generateId(),
          index: 1,
          description: `Close up on main subject.`,
          durationMs: 3500,
          imagePrompt: `Close up portrait, emotional, cinematic lighting`,
          narrationText: `The journey ahead is long and uncertain.`
        }
      ];
      
      updateProject({ ...p, status: 'READY', scenes });
    }, 4000);
  }, [getProject, updateProject]);

  return {
    projects,
    addProject,
    updateProject,
    getProject,
    mockGenerateProject
  };
}
