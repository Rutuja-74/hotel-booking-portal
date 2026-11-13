import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Hotels from './pages/Hotels';
import HotelDetails from './pages/HotelDetails';
import BookingConfirmation from './pages/BookingConfirmation';
import ManageBooking from './pages/ManageBooking';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/hotels" element={<Hotels />} />
        <Route path="/hotels/:id" element={<HotelDetails />} />
        <Route path="/booking/:reference" element={<BookingConfirmation />} />
        <Route path="/manage-booking" element={<ManageBooking />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}