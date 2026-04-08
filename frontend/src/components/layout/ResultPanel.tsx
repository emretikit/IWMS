type ResultPanelProps = {
  result: string;
};

export default function ResultPanel({ result }: ResultPanelProps) {
  return (
    <section className="card">
      <h3>API Result</h3>
      <pre>{result || 'Run an action to see response'}</pre>
    </section>
  );
}
