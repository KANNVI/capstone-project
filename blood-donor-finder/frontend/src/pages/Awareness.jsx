const CAMPS = [
  ['15 Nov 2026', 'Campus Blood Donation Drive', 'Gurugram'],
  ['28 Nov 2026', 'Community Health Camp', 'Noida'],
  ['14 Dec 2026', 'Red Cross Winter Camp', 'Delhi'],
];

export default function Awareness() {
  return (
    <>
      <h2>Awareness &amp; camps</h2>
      <div className="card"><h3>Who can donate?</h3>
        <ul><li>Age 18–65, weight at least 45 kg, in good health.</li>
          <li>At least 90 days (about 3 months) between whole-blood donations.</li>
          <li>O-negative red cells suit every blood group; AB-positive patients can receive from any group.</li></ul></div>
      <div className="card"><h3>Myths and facts</h3>
        <ul><li><b>Myth:</b> Donating makes you weak. <b>Fact:</b> The body restores the volume within hours and red cells within weeks.</li>
          <li><b>Myth:</b> You can catch an infection. <b>Fact:</b> Sterile, single-use equipment is used.</li></ul></div>
      <div className="card"><h3>Upcoming camps</h3>
        {CAMPS.map(([d, n, c]) => <div className="row line" key={n}><span><b>{n}</b> <span className="mu">· {c}</span></span><span className="tag">{d}</span></div>)}</div>
      <p className="mu">Always follow the screening advice of the blood bank's medical staff.</p>
    </>
  );
}
