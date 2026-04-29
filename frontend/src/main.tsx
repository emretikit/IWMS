import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.tsx';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter
      future={{
        // `v7_startTransition` bayrağı, React Router'ın durum güncellemelerini
        // `React.startTransition` içinde sarmalamasını sağlar. Bu, özellikle
        // veri yükleme ve sayfa geçişleri sırasında kullanıcı arayüzünün
        // daha akıcı olmasına yardımcı olur.
        v7_startTransition: true,
        // `v7_relativeSplatPath` bayrağı, "splat" (joker karakterli) rotalar
        // içindeki göreceli yol çözümlemesini düzeltir. Bu, karmaşık veya
        // iç içe geçmiş rotalarda beklenmedik davranışları önler.
        v7_relativeSplatPath: true,
      }}
    >
      <App />
    </BrowserRouter>
  </StrictMode>,
);
