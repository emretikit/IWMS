type ResultPanelProps = {
  result: string;
  title: string;
  isError: boolean;
  lastUpdated: string;
};

export default function ResultPanel({ result, title, isError, lastUpdated }: ResultPanelProps) {
  return (
    <section className="result-shell">
      <div className="result-header">
        <div>
          <p className="eyebrow">Activity feed</p>
          <h3>{title}</h3>
        </div>
        <span className={isError ? 'result-status error' : 'result-status success'}>{isError ? 'Needs attention' : 'Healthy'}</span>
      </div>
      <p className="meta">Last updated {lastUpdated}</p>
      <pre>{result || 'Use one of the action cards to see formatted API responses, validation errors and workflow traces here.'}</pre>
    </section>
  );
}
