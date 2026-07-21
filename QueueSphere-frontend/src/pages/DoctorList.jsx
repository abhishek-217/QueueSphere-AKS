import { useEffect, useState } from 'react';
import { getDoctors, bookAppointment, getDoctorAvailability } from '../api/endpoints';

export default function DoctorList() {
  const [doctors, setDoctors] = useState([]);
  const [specialization, setSpecialization] = useState('');
  const [selected, setSelected] = useState(null);
  const [availability, setAvailability] = useState([]);
  const [scheduledTime, setScheduledTime] = useState('');
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const loadDoctors = async () => {
    const { data } = await getDoctors(specialization || undefined);
    setDoctors(data);
  };

  useEffect(() => {
    loadDoctors();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadDoctors();
  };

  const openBooking = async (doctor) => {
    setSelected(doctor);
    setMessage('');
    setError('');
    setScheduledTime('');
    setReason('');
    const { data } = await getDoctorAvailability(doctor.id);
    setAvailability(data);
  };

  const handleBook = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await bookAppointment({
        doctorId: selected.id,
        scheduledTime,
        reasonForVisit: reason,
      });
      setMessage(`Appointment booked with Dr. ${selected.fullName}!`);
      setSelected(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not book this slot.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <h2>Find a Doctor</h2>
      {message && <div className="card" style={{ background: '#dcfce7' }}>{message}</div>}

      <form onSubmit={handleSearch} style={{ display: 'flex', gap: 10, marginBottom: 20 }}>
        <input
          placeholder="Filter by specialization (e.g. Cardiology)"
          value={specialization}
          onChange={(e) => setSpecialization(e.target.value)}
          style={{ flex: 1, padding: 9, borderRadius: 6, border: '1px solid var(--border)' }}
        />
        <button className="btn secondary" type="submit">Search</button>
      </form>

      <div className="grid">
        {doctors.map((doc) => (
          <div className="card" key={doc.id}>
            <h3 style={{ margin: '0 0 6px' }}>Dr. {doc.fullName}</h3>
            <div className="muted">{doc.specialization} · {doc.department}</div>
            {doc.qualifications && <div className="muted">{doc.qualifications}</div>}
            <div style={{ marginTop: 12 }}>
              <button className="btn" onClick={() => openBooking(doc)}>Book Appointment</button>
            </div>
          </div>
        ))}
        {doctors.length === 0 && <p className="muted">No doctors found.</p>}
      </div>

      {selected && (
        <div className="card" style={{ marginTop: 20, borderColor: 'var(--primary)' }}>
          <h3>Book with Dr. {selected.fullName}</h3>
          {availability.length > 0 ? (
            <p className="muted">
              Working hours: {availability.map((a) =>
                `${a.dayOfWeek.slice(0, 3)} ${a.startTime.slice(0, 5)}–${a.endTime.slice(0, 5)}`
              ).join(', ')}
            </p>
          ) : (
            <p className="muted">This doctor hasn't set fixed hours yet — any future time works.</p>
          )}
          {error && <div className="error-banner">{error}</div>}
          <form onSubmit={handleBook}>
            <div className="form-group">
              <label>Date & time</label>
              <input
                type="datetime-local"
                value={scheduledTime}
                onChange={(e) => setScheduledTime(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Reason for visit (optional)</label>
              <textarea rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
            </div>
            <button className="btn" type="submit" disabled={loading}>
              {loading ? 'Booking…' : 'Confirm Booking'}
            </button>
            <button
              type="button"
              className="btn secondary"
              style={{ marginLeft: 10 }}
              onClick={() => setSelected(null)}
            >
              Cancel
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
