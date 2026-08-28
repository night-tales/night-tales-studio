import { useState, useEffect } from 'react';
import { Key, Save, CheckCircle2, XCircle, Loader2 } from 'lucide-react';
import { auth, db } from '../lib/firebase';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { encryptKey, decryptKey } from '../lib/crypto';
import { handleFirestoreError, OperationType } from '../lib/firebaseUtils';

interface ApiKeys {
  openai: string;
  anthropic: string;
  gemini: string;
  deepseek: string;
  aimlapi: string;
}

const PROVIDERS = [
  { id: 'openai', label: 'OpenAI API Key', placeholder: 'sk-proj-...' },
  { id: 'anthropic', label: 'Anthropic API Key', placeholder: 'sk-ant-api03-...' },
  { id: 'gemini', label: 'Gemini API Key', placeholder: 'AIzaSy...' },
  { id: 'deepseek', label: 'DeepSeek API Key', placeholder: 'sk-...' },
  { id: 'aimlapi', label: 'AIML API Key', placeholder: '...' },
];

export default function SettingsScreen() {
  const [keys, setKeys] = useState<ApiKeys>({
    openai: '', anthropic: '', gemini: '', deepseek: '', aimlapi: ''
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testStatuses, setTestStatuses] = useState<Record<string, 'idle' | 'testing' | 'success' | 'error'>>({});

  useEffect(() => {
    loadKeys();
  }, []);

  const loadKeys = async () => {
    if (!auth.currentUser) return;
    try {
      const docRef = doc(db, 'userKeys', auth.currentUser.uid);
      const docSnap = await getDoc(docRef);
      if (docSnap.exists()) {
        const data = docSnap.data();
        setKeys({
          openai: decryptKey(data.openai, auth.currentUser.uid) || '',
          anthropic: decryptKey(data.anthropic, auth.currentUser.uid) || '',
          gemini: decryptKey(data.gemini, auth.currentUser.uid) || '',
          deepseek: decryptKey(data.deepseek, auth.currentUser.uid) || '',
          aimlapi: decryptKey(data.aimlapi, auth.currentUser.uid) || '',
        });
      }
    } catch (error) {
      handleFirestoreError(error, OperationType.GET, 'userKeys');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!auth.currentUser) return;
    setSaving(true);
    try {
      const docRef = doc(db, 'userKeys', auth.currentUser.uid);
      await setDoc(docRef, {
        openai: encryptKey(keys.openai, auth.currentUser.uid),
        anthropic: encryptKey(keys.anthropic, auth.currentUser.uid),
        gemini: encryptKey(keys.gemini, auth.currentUser.uid),
        deepseek: encryptKey(keys.deepseek, auth.currentUser.uid),
        aimlapi: encryptKey(keys.aimlapi, auth.currentUser.uid),
        updatedAt: new Date()
      });
      alert('تم حفظ المفاتيح بنجاح!');
    } catch (error) {
      handleFirestoreError(error, OperationType.WRITE, 'userKeys');
    } finally {
      setSaving(false);
    }
  };

  const testKey = async (provider: string) => {
    const key = keys[provider as keyof ApiKeys];
    if (!key) return;
    
    setTestStatuses(prev => ({ ...prev, [provider]: 'testing' }));
    try {
      const res = await fetch('/api/test-key', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ provider, apiKey: key })
      });
      if (!res.ok) throw new Error('Invalid');
      setTestStatuses(prev => ({ ...prev, [provider]: 'success' }));
    } catch {
      setTestStatuses(prev => ({ ...prev, [provider]: 'error' }));
    }
  };

  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950" dir="rtl">
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm flex items-center gap-2 text-zinc-100">
        <Key size={20} className="text-blue-500" />
        <h2 className="font-bold">إعدادات مفاتيح API</h2>
      </div>

      <div className="flex-1 overflow-y-auto p-4 md:p-8">
        <div className="max-w-2xl mx-auto space-y-6">
          <div className="bg-blue-500/10 border border-blue-500/20 rounded-xl p-4 text-blue-200 text-sm leading-relaxed">
            <p>يتم تخزين مفاتيحك بشكل مشفر في قاعدة البيانات، ولا يتم عرضها لأي شخص. يتم استخدامها فقط للاتصال بخدمات الذكاء الاصطناعي التي تختارها.</p>
          </div>

          {loading ? (
             <div className="flex justify-center p-8"><Loader2 className="animate-spin text-blue-500" /></div>
          ) : (
            <div className="space-y-4">
              {PROVIDERS.map((provider) => (
                <div key={provider.id} className="bg-zinc-900 border border-zinc-800 rounded-xl p-4 space-y-3">
                  <label className="text-sm font-medium text-zinc-300 block">{provider.label}</label>
                  <div className="flex gap-2">
                    <input
                      type="password"
                      value={keys[provider.id as keyof ApiKeys]}
                      onChange={(e) => setKeys({ ...keys, [provider.id]: e.target.value })}
                      placeholder={provider.placeholder}
                      className="flex-1 bg-zinc-950 border border-zinc-800 text-zinc-100 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500 font-mono transition-colors"
                      dir="ltr"
                    />
                    <button
                      onClick={() => testKey(provider.id)}
                      disabled={!keys[provider.id as keyof ApiKeys] || testStatuses[provider.id] === 'testing'}
                      className="px-4 py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 text-sm rounded-lg transition-colors flex items-center gap-2 disabled:opacity-50"
                    >
                      {testStatuses[provider.id] === 'testing' ? (
                        <Loader2 size={16} className="animate-spin" />
                      ) : (
                        'اختبار'
                      )}
                    </button>
                  </div>
                  {testStatuses[provider.id] === 'success' && (
                    <div className="flex items-center gap-1.5 text-emerald-400 text-xs">
                      <CheckCircle2 size={14} /> الاتصال ناجح
                    </div>
                  )}
                  {testStatuses[provider.id] === 'error' && (
                    <div className="flex items-center gap-1.5 text-red-400 text-xs">
                      <XCircle size={14} /> فشل الاتصال، تأكد من صحة المفتاح
                    </div>
                  )}
                </div>
              ))}

              <div className="pt-4">
                <button
                  onClick={handleSave}
                  disabled={saving}
                  className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-medium py-3 rounded-xl transition-colors flex items-center justify-center gap-2 shadow-lg shadow-blue-500/20"
                >
                  {saving ? <Loader2 size={20} className="animate-spin" /> : <Save size={20} />}
                  حفظ المفاتيح
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
