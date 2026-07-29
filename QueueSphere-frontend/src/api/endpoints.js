import api from './client';

// ---- Auth ----
export const registerUser = (payload) => api.post('/auth/register', payload);
export const loginUser = (payload) => api.post('/auth/login', payload);

// ---- Doctors ----
export const getDoctors = (specialization) =>
  api.get('/doctors', { params: specialization ? { specialization } : {} });
export const getDoctor = (id) => api.get(`/doctors/${id}`);
export const getDoctorAvailability = (id) => api.get(`/doctors/${id}/availability`);
export const addDoctorAvailability = (id, payload) => api.post(`/doctors/${id}/availability`, payload);

// ---- Appointments ----
export const bookAppointment = (payload) => api.post('/appointments', payload);
export const getMyAppointments = () => api.get('/appointments/me');
export const cancelAppointment = (id) => api.delete(`/appointments/${id}`);
export const checkInAppointment = (id) => api.post(`/appointments/${id}/check-in`);
export const getDoctorAppointments = (doctorId, date) =>
  api.get(`/appointments/doctor/${doctorId}`, { params: date ? { date } : {} });

// ---- Queue ----
export const getQueueStatus = (doctorId) => api.get(`/queue/public/${doctorId}`);
export const callNextPatient = (doctorId) => api.post(`/queue/doctor/${doctorId}/call-next`);
export const markNoShow = (queueEntryId) => api.post(`/queue/entry/${queueEntryId}/no-show`);
