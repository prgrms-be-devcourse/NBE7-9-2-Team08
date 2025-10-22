'use client';
import { createContext, useContext, useState, ReactNode, useCallback, useRef } from 'react';

type Toast = { id: number; message: string; };
const ToastCtx = createContext<{ push: (msg: string) => void } | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const counterRef = useRef(0); // useRef로 카운터 관리

  const push = useCallback((message: string) => {
    const id = Date.now() + counterRef.current++; // 고유한 ID 생성
    setToasts((t) => [...t, { id, message }]);
    setTimeout(() => setToasts((t) => t.filter(x => x.id !== id)), 3000);
  }, []);

  return (
    <ToastCtx.Provider value={{ push }}>
      {children}
      <div className="toast">
        {toasts.map(t => <div key={t.id} style={{marginTop: 6}}>{t.message}</div>)}
      </div>
    </ToastCtx.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastCtx);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
