import { STATUS_LABEL } from '../utils/constants';

export function StatusBadge({ status }) {
  return <span className={`badge st-${status}`}>{STATUS_LABEL[status] || status}</span>;
}

export function UrgencyBadge({ urgency }) {
  return <span className={`badge ug-${urgency}`}>{urgency.charAt(0) + urgency.slice(1).toLowerCase()}</span>;
}

export function GroupTag({ group, big = false }) {
  return <span className={`group-tag ${big ? 'big' : ''}`}>{group}</span>;
}
