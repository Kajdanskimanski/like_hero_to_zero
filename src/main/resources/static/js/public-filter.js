// Referenzen auf die drei Dropdown-Elemente im Suchformular
const sourceSelect  = document.getElementById('source');
const countrySelect = document.getElementById('country');
const yearSelect    = document.getElementById('year');

/**
 * Lädt die verfügbaren Länder für eine gewählte Quelle vom Server
 * und befüllt das Länder-Dropdown dynamisch.
 * Das Jahres-Dropdown wird dabei immer zurückgesetzt.
 *
 * @param source          - Gewählte Datenquelle (z.B. "Our World in Data")
 * @param selectedCountry - Optional: Land das nach dem Laden vorausgewählt sein soll
 *                          (wird genutzt um nach einem Seitenreload den Formularstand wiederherzustellen)
 */
function loadCountries(source, selectedCountry = null) {
    countrySelect.innerHTML = '<option value="">-- Land auswählen --</option>';
    yearSelect.innerHTML    = '<option value="">-- Jahr auswählen --</option>';

    if (!source) return;

    fetch(`/api/countries?source=${encodeURIComponent(source)}`)
        .then(res => res.json())
        .then(countries => {
            countries.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                if (c === selectedCountry) opt.selected = true;
                countrySelect.appendChild(opt);
            });

            // Wenn ein Land vorausgewählt ist, direkt die passenden Jahre nachladen
            if (selectedCountry && countries.includes(selectedCountry)) {
                loadYears(source, selectedCountry);
            }
        });
}

/**
 * Lädt die verfügbaren Jahre für eine gewählte Quelle und ein gewähltes Land
 * und befüllt das Jahres-Dropdown dynamisch.
 *
 * @param source       - Gewählte Datenquelle
 * @param country      - Gewähltes Land
 * @param selectedYear - Optional: Jahr das nach dem Laden vorausgewählt sein soll
 */
function loadYears(source, country, selectedYear = null) {
    yearSelect.innerHTML = '<option value="">-- Jahr auswählen --</option>';

    if (!source || !country) return;

    fetch(`/api/years?source=${encodeURIComponent(source)}&country=${encodeURIComponent(country)}`)
        .then(res => res.json())
        .then(years => {
            years.forEach(y => {
                const opt = document.createElement('option');
                opt.value = y;
                opt.textContent = y;
                // Loose equality (==) da y ein Integer, selectedYear ein String sein kann
                if (y == selectedYear) opt.selected = true;
                yearSelect.appendChild(opt);
            });
        });
}

// Nach dem Laden der Seite: Formularstand aus URL-Parametern wiederherstellen,
// damit nach einer Suchanfrage alle Dropdowns korrekt vorausgefüllt sind
window.addEventListener('DOMContentLoaded', function () {
    const source  = sourceSelect.value;
    const country = countrySelect.value || null;
    const year    = yearSelect.value    || null;

    if (source) {
        loadCountries(source, country);
        if (country) loadYears(source, country, year);
    }
});

// Quelle geändert → Länder neu laden, Jahres-Dropdown wird dabei automatisch zurückgesetzt
sourceSelect.addEventListener('change', function () {
    loadCountries(this.value);
});

// Land geändert → Jahre für die aktuelle Quelle und das neue Land laden
countrySelect.addEventListener('change', function () {
    loadYears(sourceSelect.value, this.value);
});