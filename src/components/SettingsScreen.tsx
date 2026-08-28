import { useEffect, useState } from 'react';
import { CheckCircle2, Key, Loader2, XCircle } from 'lucide-react';

const PROVIDERS = [
  { id: 'openai', label: 'OpenAI' },
  { id: 'anthropic', label: 'Anthropic' },
  { id: 'gemini', label: 'Gemini' },
  { id: 'deepseek', label: 'DeepSeek' },
  { id: 'aimlapi', label: 'AIML API' },
];

export default function SettingsScreen() {
  const [configured, setConfigured] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadProviderStatus = async () => {
      try {
        const response = await fetch('/api/providers');
        if (!response.ok) throw new Error('Failed to load provider status');
        const data = await response.json();
        setConfigured(new Set<string>(data.providers || []));
      } catch (error) {
        console.error('Failed to load provider status', error);
        setConfigured(new Set());
      } finally {
        setLoading(false);
      }
    };

    loadProviderStatus();
  }, []);

  return (
    <div className="flex flex-col h-full absolute inset-0 bg-zinc-950" dir="rtl">
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 shrink-0 z-10 sticky top-0 shadow-sm flex items-center gap-2 text-zinc-100">
        <Key size={20} className="text-blue-500" />
        <h2 className="font-bold">إعدادات مزودي الذكاء الاصطناعي</h2>
      </div>

      <div className="flex-1 overflow-y-auto p-4 md:p-8">
        <div className="max-w-2xl mx-auto space-y-6">
          <div className="bg-blue-500/10 border border-blue-500/20 rounded-xl p-4 text-blue-200 text-sm leading-relaxed">
            <p>
              مفاتيح مزودي الذكاء الاصطناعي أصبحت إعدادات خادمية ولا تُخزّن أو تُفك تشفيرها داخل المتصفح.
              يحدد المسؤول المزودين المتاحين من بيئة الخادم.
            </p>
          </div>

          {loading ? (
            <div className="flex justify-center p-8"><Loader2 className="animate-spin text-blue-500" /></div>
          ) : (
            <div className="space-y-3">
              {PROVIDERS.map((provider) => {
                const isConfigured = configured.has(provider.id);
                return (
                  <div key={provider.id} className="bg-zinc-900 border border-zinc-800 rounded-xl p-4 flex items-center justify-between">
                    <span className="text-sm font-medium text-zinc-200">{provider.label}</span>
                    {isConfigured ? (
                      <span className="flex items-center gap-1.5 text-emerald-400 text-xs">
                        <CheckCircle2 size={15} /> متاح
                      </span>
                    ) : (
                      <span className="flex items-center gap-1.5 text-zinc-500 text-xs">
                        <XCircle size={15} /> غير مهيأ
                      </span>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
