/** Small bar that shows how far a donor is, relative to the search radius. */
export default function DistanceBar({ km, max = 25 }) {
  const pct = Math.min(100, Math.max(6, (km / max) * 100));
  return (
    <div className="dist" title={`${km} km away`}>
      <div className="dist-track"><div className="dist-fill" style={{ width: `${pct}%` }} /></div>
      <span className="dist-km">{km} km</span>
    </div>
  );
}
