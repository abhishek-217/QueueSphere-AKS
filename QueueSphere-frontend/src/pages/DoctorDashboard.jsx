import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  getDoctorAppointments,
  callNextPatient,
  markNoShow,
  getDoctorAvailability,
  addDoctorAvailability,
} from '../api/endpoints';
import { useLiveQueue } from '../api/useLiveQueue';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export default function DoctorDashboard() {
  const { user } = useAuth();
  const doctorId = user?.doctorId;
  const [todayAppointments, setTodayAppointments] = useState([]);
  const [availability, setAvailability] = useState([]);
  const [availError, setAvailError] = useState('');
  const [availForm, setAvailForm] = useState({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '17:00',
    slotDurationMinutes: 15,
  });
  const [error, setError] = useState('');
  const { status, connected } = useLiveQueue(doctorId || null);

  const loadSchedule = async () => {
    if (!doctorId) return;
    try {
      const { data } = await getDoctorAppointments(doctorId);
      setTodayAppointments(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load schedule.');
    }
  };

  const loadAvailability = async () => {
    if (!doctorId) return;
    const { data } = await getDoctorAvailability(doctorId);
    setAvailability(data);
  };

  useEffect(() => { loadSchedule(); loadAvailability(); }, [doctorId]); // eslint-disable-line

  const handleCallNext = async () => {
    setError('');
    try {
      await callNextPatient(doctorId);
      loadSchedule();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not call next patient.');
    }
  };

  const handleAddAvailability = async (e) => {
    e.preventDefault();
    setAvailError('');
    try {
      await addDoctorAvailability(doctorId, availForm);
      loadAvailability();
    } catch (err) {
      setAvailError(err.response?.data?.message || 'Could not save availability.');
    }
  };

  return (
    <div className="container">
      <h2>Doctor Dashboard</h2>
      <p className="muted">Welcome, Dr. {user?.fullName}</p>

      {!doctorId && (
        <div className="error-banner">
          No doctor profile linked to this account. Please re-register as a doctor.
        </div>
      )}

      {error && <div className="error-banner">{error}</div>}

      {doctorId && (
        <>
          <div className="card" style={{ background: '#f0fdfa' }}>
            <h3 style={{ marginTop: 0 }}>Live Queue {connected ? '🟢' : '🟡'}</h3>
            {status ? (
              <>
                <div className="queue-number">Now serving: {status.nowServingNumber ?? '—'}</div>
                <div className="muted">{status.totalWaiting} waiting · ~{status.estimatedWaitMinutes} min</div>
                <button className="btn" style={{ marginTop: 12 }} onClick={handleCallNext}>
                  Call Next Patient →
                </button>

                <table style={{ width: '100%', marginTop: 16, borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--border)' }}>
                      <th style={{ padding: 6 }}>#</th>
                      <th style={{ padding: 6 }}>Patient</th>
                      <th style={{ padding: 6 }}>Status</th>
                      <th style={{ padding: 6 }}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {status.entries.map((e) => (
                      <tr key={e.appointmentId} style={{ borderBottom: '1px solid var(--border)' }}>
                        <td style={{ padding: 6 }}>{e.queueNumber}</td>
                        <td style={{ padding: 6 }}>{e.patientName}</td>
                        <td style={{ padding: 6 }}><span className={`badge ${e.status}`}>{e.status}</span></td>
                        <td style={{ padding: 6 }}>
                          {e.status === 'CHECKED_IN' && (
                            <button
                              className="btn danger"
                              style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                              onClick={() => markNoShow(e.appointmentId).then(loadSchedule)}
                            >
                              No-show
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            ) : (
              <p className="muted">No one checked in yet today.</p>
            )}
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>Weekly Availability</h3>
            {availError && <div className="error-banner">{availError}</div>}

            {availability.length > 0 ? (
              <ul style={{ paddingLeft: 18 }}>
                {availability.map((a) => (
                  <li key={a.id} className="muted">
                    {a.dayOfWeek}: {a.startTime.slice(0, 5)}–{a.endTime.slice(0, 5)}
                    {' '}({a.slotDurationMinutes} min slots)
                  </li>
                ))}
              </ul>
            ) : (
              <p className="muted">No working hours set yet — patients can book any future time until you add some.</p>
            )}

            <form onSubmit={handleAddAvailability} style={{ display: 'flex', gap: 10, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Day</label>
                <select
                  value={availForm.dayOfWeek}
                  onChange={(e) => setAvailForm({ ...availForm, dayOfWeek: e.target.value })}
                >
                  {DAYS.map((d) => <option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Start</label>
                <input
                  type="time"
                  value={availForm.startTime}
                  onChange={(e) => setAvailForm({ ...availForm, startTime: e.target.value })}
                />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>End</label>
                <input
                  type="time"
                  value={availForm.endTime}
                  onChange={(e) => setAvailForm({ ...availForm, endTime: e.target.value })}
                />
              </div>
              <div className="form-group" style={{ marginBottom: 0, width: 110 }}>
                <label>Slot (min)</label>
                <input
                  type="number"
                  min={5}
                  value={availForm.slotDurationMinutes}
                  onChange={(e) => setAvailForm({ ...availForm, slotDurationMinutes: Number(e.target.value) })}
                />
              </div>
              <button className="btn" type="submit">Add</button>
            </form>
          </div>

          <h3>Today's Full Schedule</h3>
          {todayAppointments.map((apt) => (
            <div className="card" key={apt.id}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  <strong>{apt.patientName}</strong>
                  <div className="muted">{new Date(apt.scheduledTime).toLocaleString()}</div>
                  {apt.reasonForVisit && <div className="muted">{apt.reasonForVisit}</div>}
                </div>
                <span className={`badge ${apt.status}`}>{apt.status}</span>
              </div>
            </div>
          ))}
          {todayAppointments.length === 0 && <p className="muted">No appointments scheduled today.</p>}
        </>
      )}
    </div>
  );
}

