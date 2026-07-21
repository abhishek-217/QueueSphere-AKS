import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'PATIENT',
    specialization: '',
    department: '',
    qualifications: '',
  });

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await register(form);
      navigate(data.role === 'PATIENT' ? '/doctors' : '/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 460 }}>
      <h2>Create an account</h2>
      {error && <div className="error-banner">{error}</div>}
      <form className="card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>I am a</label>
          <select value={form.role} onChange={update('role')}>
            <option value="PATIENT">Patient</option>
            <option value="DOCTOR">Doctor</option>
          </select>
        </div>
        <div className="form-group">
          <label>Full name</label>
          <input value={form.fullName} onChange={update('fullName')} required />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input type="email" value={form.email} onChange={update('email')} required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" value={form.password} onChange={update('password')} minLength={6} required />
        </div>
        <div className="form-group">
          <label>Phone number</label>
          <input value={form.phoneNumber} onChange={update('phoneNumber')} required />
        </div>

        {form.role === 'DOCTOR' && (
          <>
            <div className="form-group">
              <label>Specialization</label>
              <input value={form.specialization} onChange={update('specialization')} required />
            </div>
            <div className="form-group">
              <label>Department</label>
              <input value={form.department} onChange={update('department')} required />
            </div>
            <div className="form-group">
              <label>Qualifications</label>
              <input value={form.qualifications} onChange={update('qualifications')} />
            </div>
          </>
        )}

        <button className="btn" type="submit" disabled={loading}>
          {loading ? 'Creating account…' : 'Register'}
        </button>
      </form>
      <p className="muted">Already registered? <Link to="/login">Log in</Link></p>
    </div>
  );
}
