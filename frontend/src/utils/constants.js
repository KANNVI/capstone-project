export const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
export const URGENCIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const STATUS_STEPS = ['PENDING', 'DONOR_ACCEPTED', 'DONATION_IN_PROGRESS', 'COMPLETED'];

export const STATUS_LABEL = {
  PENDING: 'Looking for donors',
  DONOR_ACCEPTED: 'Donor accepted',
  DONATION_IN_PROGRESS: 'Donation in progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  SENT: 'Waiting for reply',
  ACCEPTED: 'Accepted',
  REJECTED: 'Declined',
  IN_PROGRESS: 'Donating',
  EXPIRED: 'Closed',
};

export const fmtDate = (d) =>
  d ? new Date(d).toLocaleString(undefined, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : '';
export const fmtDay = (d) =>
  d ? new Date(d).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' }) : '';
