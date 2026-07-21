import { useEffect, useState } from 'react';
import { getMyAppointments, cancelAppointment, checkInAppointment } from '../api/endpoints';
import { useLiveQueue } from '../api/useLiveQueue';

function LiveQueueCard({ doctorId, appointmentId }) {
  const { status, connected } = useLiveQueue(doctorId);

  if (!status) {
    return <p className="muted">Connecting to live queue{connected ? '' : '…'}</p>;
  }

  const myEntry = status.entries.find((e) => e.appointmentId === appointmentId);

  return (
    <div className="card" style={{ background: '#f0fdfa' }}>
      <div className="muted">Now serving</div>
      <div className="queue-number">{status.nowServingNumber ?? '—'}</div>
      {myEntry && (
        <>
          <div className="muted" style={{ marginTop: 8 }}>Your number: <strong>{myEntry.queueNumber}</strong></div>
          <div className="muted">Status: <span className={`badge ${myEntry.status}`}>{myEntry.status}</span></div>
        </>
      )}
      <div className="muted" style={{ marginTop: 8 }}>
        {status.totalWaiting} patient(s) waiting · ~{status.estimatedWaitMinutes} min estimated wait
      </div>
      <div className="muted" style={{ fontSize: '0.75rem', marginTop: 6 }}>
        {connected ? '🟢 live' : '🟡 reconnecting…'}
      </div>
    </div>
  );
}

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState('');

  const load = async () => {
    const { data } = await getMyAppointments();
    setAppointments(data);
  };

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    setError('');
    try {
      await cancelAppointment(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not cancel this appointment.');
    }
  };

  const handleCheckIn = async (id) => {
    setError('');
    try {
      await checkInAppointment(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not check in.');
    }
  };

  return (
    <div className="container">
      <h2>My Appointments</h2>
      {error && <div className="error-banner">{error}</div>}

      {appointments.map((apt) => (
        <div className="card" key={apt.id}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h3 style={{ margin: '0 0 4px' }}>Dr. {apt.doctorName}</h3>
              <div className="muted">{apt.specialization}</div>
              <div className="muted">{new Date(apt.scheduledTime).toLocaleString()}</div>
              {apt.reasonForVisit && <div className="muted">Reason: {apt.reasonForVisit}</div>}
            </div>
            <span className={`badge ${apt.status}`}>{apt.status}</span>
          </div>

          <div style={{ marginTop: 12, display: 'flex', gap: 10 }}>
            {apt.status === 'BOOKED' && (
              <>
                <button className="btn" onClick={() => handleCheckIn(apt.id)}>Check In</button>
                <button className="btn danger" onClick={() => handleCancel(apt.id)}>Cancel</button>
              </>
            )}
          </div>

          {(apt.status === 'CHECKED_IN' || apt.status === 'IN_PROGRESS') && (
            <div style={{ marginTop: 14 }}>
              <LiveQueueCard doctorId={apt.doctorId} appointmentId={apt.id} />
            </div>
          )}
        </div>
      ))}

      {appointments.length === 0 && <p className="muted">You have no appointments yet.</p>}
    </div>
  );
}
