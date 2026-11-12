import { useState } from 'react';

/** Asks the browser for the current position and returns { latitude, longitude } via onLocate. */
export default function LocationButton({ onLocate, label = 'Use my current location', done = false }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const locate = () => {
    if (!navigator.geolocation) {
      setError('This browser does not support location. Enter coordinates manually.');
      return;
    }
    setBusy(true);
    setError('');
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setBusy(false);
        onLocate({
          latitude: Number(pos.coords.latitude.toFixed(6)),
          longitude: Number(pos.coords.longitude.toFixed(6)),
        });
      },
      (err) => {
        setBusy(false);
        setError(
          err.code === 1
            ? 'Location permission was denied. Allow it in the browser address bar, or enter coordinates manually.'
            : 'Could not get your location. Try again or enter coordinates manually.'
        );
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  return (
    <div className="loc">
      <button type="button" className="btn btn-ghost" onClick={locate} disabled={busy}>
        {busy ? 'Finding location…' : done ? 'Location saved. Update it' : label}
      </button>
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}
