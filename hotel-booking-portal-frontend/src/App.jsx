import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Hotels from './pages/Hotels';
import NotFound from './pages/NotFound';

// Temporary page. It is replaced by the real hotel details page in the next step.
function ComingSoon() {
  return (
    <div className="container page">
      <h1>Hotel details</h1>
      <p>This page is built in the next step.</p>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/hotels" element={<Hotels />} />
        <Route path="/hotels/:id" element={<ComingSoon />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;