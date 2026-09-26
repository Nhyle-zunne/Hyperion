import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

// Register Service Worker for offline PWA execution
if ('serviceWorker' in navigator && !window.location.host.includes('localhost:5173')) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/sw.js')
      .then((reg) => {
        console.log('Hyperion Service Worker registered with scope:', reg.scope);
      })
      .catch((err) => {
        console.warn('Service Worker registration skipped or failed:', err);
      });
  });
}

// Global hook for PWA install prompt
declare global {
  interface Window {
    deferredPrompt?: any;
    triggerPwaInstall?: () => Promise<boolean>;
  }
}

window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  window.deferredPrompt = e;
  window.dispatchEvent(new Event('pwa-install-ready'));
});

window.triggerPwaInstall = async () => {
  if (window.deferredPrompt) {
    window.deferredPrompt.prompt();
    const { outcome } = await window.deferredPrompt.userChoice;
    window.deferredPrompt = null;
    return outcome === 'accepted';
  }
  return false;
};

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
