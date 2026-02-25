import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

// Initialize app with environment check
console.log(`[Task Management] Starting app in ${process.env.REACT_APP_ENV || 'development'} mode`);
console.log(`[Task Management] API Base URL: ${process.env.REACT_APP_API_URL || 'http://localhost:8080'}`);

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
