import type { MenuPanel, Role } from '../../types';

type SidebarProps = {
  usernameLabel: string;
  role?: Role;
  panels: MenuPanel[];
  activePanel: string;
  onSelectPanel: (key: string) => void;
  showLogout: boolean;
  onLogout: () => void;
};

export default function Sidebar({
  usernameLabel,
  role,
  panels,
  activePanel,
  onSelectPanel,
  showLogout,
  onLogout,
}: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="brand-block">
        <p className="eyebrow">Infinite Runtime</p>
        <h1>Internship Workspace</h1>
        <p className="meta">Production-grade control surface for internship operations.</p>
      </div>

      <div className="identity-card">
        <span className="role-badge">{role ?? 'GUEST'}</span>
        <p className="identity-name">{usernameLabel}</p>
        <p className="meta">Adaptive navigation changes with the authenticated role.</p>
      </div>

      <div className="menu">
        {panels.map((panel) => (
          <button key={panel.key} className={activePanel === panel.key ? 'active nav-button' : 'nav-button'} onClick={() => onSelectPanel(panel.key)}>
            <span>{panel.label}</span>
            <small>{panel.description}</small>
          </button>
        ))}
      </div>

      <div className="sidebar-note">
        <p className="eyebrow">Live workspace</p>
        <p className="meta">Each action below talks directly to the backend environment and returns traceable API feedback.</p>
      </div>

      {showLogout && (
        <button className="danger" onClick={onLogout}>
          Sign out
        </button>
      )}
    </aside>
  );
}
