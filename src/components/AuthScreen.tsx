import { FormEvent, useState } from 'react';
import { createUserWithEmailAndPassword, signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../lib/firebase';
import { Loader2, LogIn } from 'lucide-react';

export default function AuthScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      if (mode === 'login') {
        await signInWithEmailAndPassword(auth, email.trim(), password);
      } else {
        await createUserWithEmailAndPassword(auth, email.trim(), password);
      }
    } catch (e: any) {
      setError(e?.message || 'تعذر تسجيل الدخول');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-50 flex items-center justify-center p-4" dir="rtl">
      <form onSubmit={submit} className="w-full max-w-md bg-zinc-900 border border-zinc-800 rounded-2xl p-6 space-y-5">
        <div className="flex items-center gap-3">
          <div className="bg-blue-500 p-2 rounded-lg"><LogIn size={20} /></div>
          <div>
            <h1 className="font-bold text-lg">Night Tales Studio</h1>
            <p className="text-xs text-zinc-500">تسجيل دخول آمن</p>
          </div>
        </div>

        <input
          type="email"
          required
          value={email}
          onChange={e => setEmail(e.target.value)}
          placeholder="البريد الإلكتروني"
          className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-3 outline-none focus:border-blue-500"
          dir="ltr"
        />

        <input
          type="password"
          required
          minLength={6}
          value={password}
          onChange={e => setPassword(e.target.value)}
          placeholder="كلمة المرور"
          className="w-full bg-zinc-950 border border-zinc-800 rounded-lg px-3 py-3 outline-none focus:border-blue-500"
          dir="ltr"
        />

        {error && <p className="text-sm text-red-400 break-words">{error}</p>}

        <button disabled={loading} className="w-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded-lg py-3 font-medium flex items-center justify-center gap-2">
          {loading && <Loader2 size={18} className="animate-spin" />}
          {mode === 'login' ? 'تسجيل الدخول' : 'إنشاء حساب'}
        </button>

        <button
          type="button"
          onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}
          className="w-full text-sm text-zinc-400 hover:text-zinc-200"
        >
          {mode === 'login' ? 'ليس لديك حساب؟ إنشاء حساب' : 'لديك حساب؟ تسجيل الدخول'}
        </button>
      </form>
    </div>
  );
}
