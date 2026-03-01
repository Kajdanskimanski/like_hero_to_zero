/**
 * Theme-Toggle-Modul: Schaltet zwischen hellem und dunklem Modus um.
 * Das gewählte Theme wird in localStorage gespeichert und beim nächsten
 * Seitenaufruf automatisch wiederhergestellt. Fällt localStorage leer aus,
 * wird die Systempräferenz des Nutzers (prefers-color-scheme) als Fallback genutzt.
 *
 * In ein IIFE (Immediately Invoked Function Expression) gekapselt,
 * um den globalen Namespace nicht zu verschmutzen.
 */
(function() {
    // Theme aus localStorage laden oder System-Präferenz als Fallback verwenden
    const initTheme = () => {
        const savedTheme = localStorage.getItem('theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        if (savedTheme) {
            document.documentElement.setAttribute('data-theme', savedTheme);
        } else if (prefersDark) {
            document.documentElement.setAttribute('data-theme', 'dark');
        }

        updateToggleIcon();
    };

    // Theme zwischen 'dark' und 'light' umschalten und in localStorage persistieren
    const toggleTheme = () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        updateToggleIcon();
    };

    // Icon und Tooltip des Toggle-Buttons passend zum aktuellen Theme setzen
    const updateToggleIcon = () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const toggleBtn = document.getElementById('theme-toggle');

        if (toggleBtn) {
            if (currentTheme === 'dark') {
                toggleBtn.innerHTML = '<span class="theme-toggle-icon">☀️</span>';
                toggleBtn.title = 'Zu hellem Modus wechseln';
            } else {
                toggleBtn.innerHTML = '<span class="theme-toggle-icon">🌙</span>';
                toggleBtn.title = 'Zu dunklem Modus wechseln';
            }
        }
    };

    const setupToggleButton = () => {
        const toggleBtn = document.getElementById('theme-toggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', toggleTheme);
        }
    };

    // Sicherstellen dass das DOM bereit ist, bevor auf Elemente zugegriffen wird
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            initTheme();
            setupToggleButton();
        });
    } else {
        // DOM bereits geladen (z.B. bei nachträglich eingefügten Scripts)
        initTheme();
        setupToggleButton();
    }
})();