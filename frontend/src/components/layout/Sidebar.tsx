type MenuPanel = { key: string; label: string };

type SidebarProps = {
  usernameLabel: string;
  panels: MenuPanel[];
  activePanel: string;
  onSelectPanel: (key: string) => void;
  showLogout: boolean;
  onLogout: () => void;
};

export default function Sidebar({
  usernameLabel,
  panels,
  activePanel,
  onSelectPanel,
  showLogout,
  onLogout,
}: SidebarProps) {
  return (
    <aside className="sidebar">
      <h1>IWMS Frontend</h1>
      <p className="meta">{usernameLabel}</p>
      <div className="menu">
        {panels.map((panel) => (
          <button key={panel.key} className={activePanel === panel.key ? 'active' : ''} onClick={() => onSelectPanel(panel.key)}>
            {panel.label}
          </button>
        ))}
      </div>
      {showLogout && (
        <button className="danger" onClick={onLogout}>
          Logout
        </button>
      )}
    </aside>
  );
}
