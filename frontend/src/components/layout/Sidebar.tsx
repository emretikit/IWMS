import type { MenuPanel } from '../../types';

type SidebarProps = {
  panels: MenuPanel[];
  activePanel: string;
  isOpen: boolean;
  onSelectPanel: (key: string) => void;
  onClose: () => void;
  onLogout: () => void;
};

export default function Sidebar({ panels, activePanel, isOpen, onSelectPanel, onClose, onLogout }: SidebarProps) {
  return (
    <>
      <button type="button" className={isOpen ? 'drawer-backdrop open' : 'drawer-backdrop'} aria-label="Close navigation" onClick={onClose} />

      <aside className={isOpen ? 'sidebar-drawer open' : 'sidebar-drawer'}>
        <div className="drawer-header">
          <span className="drawer-title">Menu</span>
          <button type="button" className="drawer-close" aria-label="Close navigation" onClick={onClose}>
            x
          </button>
        </div>

        <nav className="drawer-nav">
          {panels.map((panel) => (
            <button
              key={panel.key}
              type="button"
              className={activePanel === panel.key ? 'drawer-link active' : 'drawer-link'}
              onClick={() => {
                onSelectPanel(panel.key);
                onClose();
              }}
            >
              {panel.label}
            </button>
          ))}
        </nav>

        <button type="button" className="drawer-logout" onClick={onLogout}>
          Logout
        </button>
      </aside>
    </>
  );
}
