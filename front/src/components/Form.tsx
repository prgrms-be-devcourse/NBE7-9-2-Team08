'use client';
import { useState, FormEvent } from 'react';

type Field = {
  name: string;
  label: string;
  type?: string;
  placeholder?: string;
};

export default function Form({
  fields,
  onSubmit,
  submitText = 'Submit',
}: {
  fields: Field[];
  onSubmit: (values: Record<string,string>) => Promise<void> | void;
  submitText?: string;
}) {
  const [values, setValues] = useState<Record<string,string>>({});
  const [loading, setLoading] = useState(false);

  function update(name: string, value: string) {
    setValues(prev => ({...prev, [name]: value}));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      setLoading(true);
      await onSubmit(values);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{display:'grid', gap:12, maxWidth:420}}>
      {fields.map(f => (
        <label key={f.name} style={{display:'grid', gap:6}}>
          <span>{f.label}</span>
          <input
            className="input"
            type={f.type ?? 'text'}
            placeholder={f.placeholder ?? ''}
            value={values[f.name] ?? ''}
            onChange={(e) => update(f.name, e.target.value)}
            required
          />
        </label>
      ))}
      <button className="button" type="submit" disabled={loading}>
        {loading ? '처리중...' : submitText}
      </button>
    </form>
  );
}
