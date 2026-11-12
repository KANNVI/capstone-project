import { useEffect, useRef } from 'react';

/** Runs fn now and then every `ms` milliseconds (simple "live" updates without websockets). */
export default function usePolling(fn, ms = 15000) {
  const saved = useRef(fn);
  saved.current = fn;
  useEffect(() => {
    saved.current();
    const id = setInterval(() => saved.current(), ms);
    return () => clearInterval(id);
  }, [ms]);
}
