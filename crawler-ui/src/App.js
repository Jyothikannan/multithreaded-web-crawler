import { useEffect, useRef, useState } from "react";

function App() {
  const [status, setStatus] = useState({});
  const [results, setResults] = useState([]);
  const [url, setUrl] = useState("");

  const resultsRef = useRef(null);

  // ✅ backend base URL from .env
  const API_BASE = process.env.REACT_APP_API_URL;

  const startCrawl = () => {
    fetch(`${API_BASE}/api/crawl`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        url: url,
        depth: 2
      })
    });
  };

  useEffect(() => {
    const interval = setInterval(() => {
      fetch(`${API_BASE}/api/crawl/status`)
        .then(res => res.json())
        .then(data => setStatus(data))
        .catch(err => console.log("status error:", err));

      fetch(`${API_BASE}/api/crawl/results`)
        .then(res => res.json())
        .then(data => setResults(data))
        .catch(err => console.log("results error:", err));
    }, 2000);

    return () => clearInterval(interval);
  }, [API_BASE]);

  // 🔥 Auto scroll like live feed
  useEffect(() => {
    if (resultsRef.current) {
      resultsRef.current.scrollTop = resultsRef.current.scrollHeight;
    }
  }, [results]);

  return (
    <div style={styles.container}>

      <div style={styles.bg}></div>

      <h1 style={styles.title}>🕸️ Crawler Monitor</h1>

      {/* INPUT */}
      <div style={styles.inputBox}>
        <input
          placeholder="Enter URL..."
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          style={styles.input}
        />
        <button onClick={startCrawl} style={styles.button}>
          ▶ Crawl
        </button>
      </div>

      {/* STATS */}
      <div style={styles.stats}>
        <Stat label="URLs" value={status.totalUrls || 0} />
        <Stat
          label="Status"
          value={status.isRunning ? "RUNNING" : "STOPPED"}
          glow={status.isRunning}
        />
        <Stat label="Hits" value={status.cacheHits || 0} />
        <Stat label="Miss" value={status.cacheMisses || 0} />
      </div>

      {/* RESULTS */}
      <div style={styles.terminal} ref={resultsRef}>
        {results.length === 0 && (
          <p style={{ color: "#888" }}>Waiting for crawl...</p>
        )}

        {results.slice(-50).map((item, i) => (
          <div key={i} style={styles.line}>
            <span style={styles.dot}>●</span>

            <span style={styles.domain}>
              {item.url ? new URL(item.url).hostname : ""}
            </span>

            <span style={styles.path}>
              {item.url ? new URL(item.url).pathname : ""}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

// 🔥 Stat component
function Stat({ label, value, glow }) {
  return (
    <div
      style={{
        ...styles.card,
        boxShadow: glow
          ? "0 0 15px #22c55e"
          : "0 0 10px rgba(0,0,0,0.5)"
      }}
    >
      <div style={styles.value}>{value}</div>
      <div style={styles.label}>{label}</div>
    </div>
  );
}

// 🎨 styles
const styles = {
  container: {
    background: "#020617",
    color: "#e2e8f0",
    minHeight: "100vh",
    padding: "20px",
    fontFamily: "monospace",
    position: "relative",
    overflow: "hidden"
  },

  bg: {
    position: "absolute",
    width: "100%",
    height: "100%",
    backgroundImage: "radial-gradient(#22c55e 1px, transparent 1px)",
    backgroundSize: "40px 40px",
    opacity: 0.08,
    animation: "move 20s linear infinite"
  },

  title: {
    textAlign: "center",
    marginBottom: "20px",
    position: "relative"
  },

  inputBox: {
    display: "flex",
    justifyContent: "center",
    gap: "10px",
    marginBottom: "20px",
    position: "relative"
  },

  input: {
    padding: "10px",
    width: "320px",
    background: "#020617",
    border: "1px solid #22c55e",
    color: "white",
    borderRadius: "6px"
  },

  button: {
    padding: "10px 15px",
    background: "#22c55e",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    color: "black"
  },

  stats: {
    display: "flex",
    justifyContent: "center",
    gap: "15px",
    marginBottom: "20px"
  },

  card: {
    padding: "15px",
    borderRadius: "10px",
    background: "#020617",
    border: "1px solid #22c55e",
    width: "120px",
    textAlign: "center"
  },

  value: {
    fontSize: "20px",
    fontWeight: "bold"
  },

  label: {
    fontSize: "12px",
    color: "#94a3b8"
  },

  terminal: {
    background: "#000",
    borderRadius: "10px",
    padding: "15px",
    height: "400px",
    overflowY: "auto",
    border: "1px solid #22c55e",
    fontSize: "13px"
  },

  line: {
    marginBottom: "6px"
  },

  dot: {
    color: "#22c55e",
    marginRight: "8px"
  },

  domain: {
    color: "#22c55e",
    marginRight: "10px"
  },

  path: {
    color: "#cbd5f5"
  }
};

export default App;