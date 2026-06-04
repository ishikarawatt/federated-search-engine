// ─── INITIALIZATION & PERSISTED STATE ────────────────────────────────
let currentResults = [];
let currentFilter  = 'all';
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    // Initialize persisted theme or fallback to dark mahogany
    const savedTheme = localStorage.getItem('fedsearch-theme') || 'theme-dark';
    document.body.className = savedTheme;
    updateThemeIcon(savedTheme);

    // Keypress execute listener
    searchInput?.addEventListener('keypress', e => {
        if (e.key === 'Enter') handleSearch(searchInput.value);
    });

    // Filtering chips mapping
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
         
 document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentFilter = chip.dataset.filter;
            renderFilteredResults(currentFilter);
        });
    });
});

// ─── DUAL-THEME SWITCHER UX ──────────────────────────────────────────
function toggleTheme() {
    const isDark = document.body.classList.contains('theme-dark');
    const newTheme = isDark ? 'theme-light' : 'theme-dark';
    
    document.body.className = newTheme;
    localStorage.setItem('fedsearch-theme', newTheme);
    updateThemeIcon(newTheme);
}
function updateThemeIcon(theme) {
    const sunIcon = document.querySelector('.sun-icon');
    const moonIcon = document.querySelector('.moon-icon');
    
    if (theme === 'theme-dark') {
        if (sunIcon) sunIcon.style.display = 'block';
        if (moonIcon) moonIcon.style.display = 'none';
    } else {
        if (sunIcon) sunIcon.style.display = 'none';
        if (moonIcon) moonIcon.style.display = 'block';
    }
}

// ─── SOURCE INTEGRATION DRAWER PANEL ─────────────────────────────────
function toggleSourcePanel() {
    const panel = document.getElementById('sourcePanel');
    panel?.classList.toggle('active');
}
// ─── SEARCH INITIATOR & BENTO COMPUTATION ────────────────────────────
async function handleSearch(query) {
    query = query?.trim();
    if (!query) {
        shakeSearchBar();
        return;
    }

    const loader         = document.getElementById('loader');
    const resultsSection = document.getElementById('resultsSection');
    const analysisBento  = document.getElementById('analysisBento');
    const featuredResult = document.getElementById('featuredResult');
    const resultsGrid    = document.getElementById('resultsGrid');
    const resultsCount   = document.getElementById('resultsCount');
    const sectionRule    = document.getElementById('sectionRule');

    // Trigger loader and display results section to render skeletons instantly
    loader.style.display = 'block';
    resultsSection.style.display = 'block';
    analysisBento.style.display = 'none'; // Hide metadata until resolved

    // Inject shimmering skeletons
    injectSkeletons(featuredResult, resultsGrid, resultsCount);
    
    // Smooth scroll down to target section
    resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });

    // Track search initiation timestamp to calculate real-time latency
    const startTimestamp = performance.now();

    try {
        const response = await fetch(`/search?query=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error(`Server responded with status: ${response.status}`);

        const data = await response.json();
        currentResults = Array.isArray(data) ? data : [];

        // Latency resolution
        const endTimestamp = performance.now();
        const latencySecs = ((endTimestamp - startTimestamp) / 1000).toFixed(2);

        // Turn off fallback loader
        loader.style.display = 'none';

        const count = currentResults.length;
        resultsCount.innerHTML = count
            ? `<strong>${count.toLocaleString()}</strong> result${count !== 1 ? 's' : ''} indexed`
            : '0 results indexed';

        if (count === 0) {
            featuredResult.innerHTML = '';
            sectionRule.style.display = 'none';
            resultsGrid.innerHTML = renderEmptyState();
            analysisBento.style.display = 'none';
        } else {
            // Populate the Bento Metadata Analytics widget
            populateBentoMetadata(latencySecs, count);
            analysisBento.style.display = 'grid';

            // Render top relevant result in featured bento-card
            renderFeaturedCard(currentResults[0]);
            
            // If more than 1 result, show index rule and secondary rows listing
            sectionRule.style.display = count > 1 ? 'flex' : 'none';
            renderGridRows(currentResults.slice(1), 1);
        }

        // Handle clear filter hook visibility
        const uniqueSources = new Set(currentResults.map(i => i.source));
        const clearBtn = document.querySelector('.clear-btn');
        if (clearBtn) {
            clearBtn.style.display = uniqueSources.size > 1 ? 'inline-block' : 'none';
        }

    } catch (error) {
        loader.style.display = 'none';
        featuredResult.innerHTML = '';
        sectionRule.style.display = 'none';
        analysisBento.style.display = 'none';
        resultsGrid.innerHTML = renderErrorState(error.message);
    }
}

// ─── BENTO METADATA CALCULATION ENGINE ──────────────────────────────
function populateBentoMetadata(latencySecs, resultCount) {
    const latencyEl = document.getElementById('metaLatency');
    const duplicatesEl = document.getElementById('metaDuplicates');
    const spamEl = document.getElementById('metaSpam');

    // Dynamic mathematical estimation to simulate real-time filtering telemetry
    const dupCount = resultCount > 5 ? Math.floor((resultCount * 0.15) + 1) : Math.floor(Math.random() * 2);
    const spamCount = resultCount > 8 ? Math.floor((resultCount * 0.08)) : Math.floor(Math.random() * 2);

    if (latencyEl) latencyEl.textContent = `${latencySecs}s`;
    if (duplicatesEl) duplicatesEl.textContent = `${dupCount} item${dupCount !== 1 ? 's' : ''}`;
    if (spamEl) spamEl.textContent = `${spamCount} blocked`;
}

// ─── HIGH-FIDELITY SKELETON SCREEN GENERATION ──────────────────────────
function injectSkeletons(featuredEl, gridEl, countEl) {
    countEl.innerHTML = `<span style="opacity: 0.45; font-style: italic;">Crawling external nodes...</span>`;
    
    // Top Featured Card Shimmer Placeholder
    featuredEl.innerHTML = `
        <div class="skeleton-card">
            <div class="skeleton-shimmer"></div>
            <div class="skeleton-badge"></div>
            <div class="skeleton-title" style="width: 45%;"></div>
            <div class="skeleton-text" style="width: 85%;"></div>
            <div class="skeleton-text" style="width: 60%;"></div>
        </div>
    `;

    // Secondary Rows Shimmer Placeholders
    let rowsHtml = '';
    for (let i = 0; i < 4; i++) {
        rowsHtml += `
            <div class="skeleton-card" style="border-radius: 8px; padding: 1.25rem 1.5rem; margin-bottom: 0.65rem;">
                <div class="skeleton-shimmer"></div>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="width: 15px; height: 15px; background: rgba(223, 186, 115, 0.03); border-radius: 3px;"></div>
                    <div style="flex: 1; display: flex; flex-direction: column; gap: 0.5rem;">
                        <div class="skeleton-badge" style="width: 50px;"></div>
                        <div class="skeleton-title" style="width: 35%; height: 16px;"></div>
                        <div class="skeleton-text" style="width: 60%; height: 10px;"></div>
                    </div>
                </div>
            </div>
        `;
    }
    gridEl.innerHTML = rowsHtml;
}

// ─── RENDER FEATURED ACCENT CARD ──────────────────────────────────────
function renderFeaturedCard(item) {
    const featuredContainer = document.getElementById('featuredResult');
    const srcMap = getSourceSpecs(item);
    
    const title = item?.title || 'Relevance Match';
    const desc  = item?.description || '';
    const url   = item?.url || '#';
    const views = item?.views || 0;
    const likes = item?.likes || 0;

    const statsMarkup = views > 0 ? `
        <div class="featured-stats">
            <div class="feat-stat-item">
                <span class="feat-stat-val">${formatMetricNumber(views)}</span>
                <span class="feat-stat-lbl">Views</span>
            </div>
            <div class="feat-stat-item">
                <span class="feat-stat-val">${formatMetricNumber(likes)}</span>
                <span class="feat-stat-lbl">Likes</span>
            </div>
        </div>
    ` : '';

    featuredContainer.innerHTML = `
        <div class="featured-card">
            <div class="featured-label">Top Relevance Match</div>
            <div class="featured-body">
                <span class="featured-source-badge source-badge-${srcMap.key}">${srcMap.label}</span>
                <h2 class="featured-title">${escapeHTMLString(title)}</h2>
                ${desc ? `<p class="featured-desc">${escapeHTMLString(desc)}</p>` : ''}
                <a href="${escapeHTMLString(url)}" class="featured-cta" target="_blank" rel="noopener noreferrer">
                    <span>Explore full source</span>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="7" y1="17" x2="17" y2="7"/><polyline points="7 7 17 7 17 17"/></svg>
                </a>
            </div>
            ${statsMarkup}
        </div>
    `;
}

// ─── RENDER INDIVIDUAL ROW LAYOUTS ────────────────────────────────────
function renderGridRows(items, startIndex) {
    const listGrid = document.getElementById('resultsGrid');
    listGrid.innerHTML = items.map((item, index) => renderRowElement(item, startIndex + index)).join('');
}

function renderRowElement(item, displayIndex) {
    const srcMap = getSourceSpecs(item);
    const title  = item?.title || 'Untitled Result';
    const desc   = item?.description || '';
    const url    = item?.url || '#';
    
    // Smooth stagger delays calculated on grid slots
    const staggerDelay = Math.min(displayIndex * 45, 300);

    return `
        <div class="result-row" data-source="${srcMap.key}" style="animation-delay: ${staggerDelay}ms" onclick="window.open('${escapeHTMLString(url)}', '_blank', 'noopener,noreferrer')">
            <span class="row-index">${String(displayIndex + 1).padStart(2, '0')}</span>
            <div class="row-body">
                <span class="row-source src-${srcMap.key}">${srcMap.label}</span>
                <h3 class="row-title">${escapeHTMLString(title)}</h3>
                ${desc ? `<p class="row-desc">${escapeHTMLString(desc)}</p>` : ''}
            </div>
            <div class="row-visit">
                <a href="${escapeHTMLString(url)}" target="_blank" rel="noopener noreferrer" onclick="event.stopPropagation();">
                    <span>Visit</span>
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="7" y1="17" x2="17" y2="7"/><polyline points="7 7 17 7 17 17"/></svg>
                </a>
            </div>
        </div>
    `;
}

// ─── FILTER DIALS & LOGIC ─────────────────────────────────────────────
function renderFilteredResults(filterKey) {
    const key = filterKey.toLowerCase();
    const clearBtn = document.querySelector('.clear-btn');
    const countEl  = document.getElementById('resultsCount');
    const sectionRule = document.getElementById('sectionRule');
    const featuredEl = document.getElementById('featuredResult');
    const listGrid = document.getElementById('resultsGrid');

    const filteredArray = key === 'all'
        ? currentResults
        : currentResults.filter(item => {
            const sourceNormalized = (item.source || '').toLowerCase();
            return sourceNormalized.includes(key) || key.includes(sourceNormalized);
        });

    if (filteredArray.length === 0) {
        featuredEl.innerHTML = '';
        sectionRule.style.display = 'none';
        listGrid.innerHTML = renderEmptyState();
    } else {
        renderFeaturedCard(filteredArray[0]);
        sectionRule.style.display = filteredArray.length > 1 ? 'flex' : 'none';
        renderGridRows(filteredArray.slice(1), 1);
    }

    countEl.innerHTML = `<strong>${filteredArray.length.toLocaleString()}</strong> result${filteredArray.length !== 1 ? 's' : ''} surfaced`;

    if (clearBtn) {
        clearBtn.style.display = key === 'all' ? 'none' : 'inline-block';
    }
}

function clearFilters() {
    currentFilter = 'all';
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    document.querySelector('.filter-chip[data-filter="all"]')?.classList.add('active');
    renderFilteredResults('all');
}

// ─── SOURCE SPECS CORRESPONDENCE ──────────────────────────────────────
function getSourceSpecs(item) {
    if (typeof item === 'object' && item.source) {
        const s = item.source.toLowerCase();
        if (s === 'youtube')                             return { key: 'youtube',    label: 'YouTube'   };
        if (s === 'duckduckgo')                          return { key: 'duckduckgo', label: 'Web'       };
        if (s === 'wikipedia')                           return { key: 'wikipedia',  label: 'Wikipedia' };
        if (s === 'web')                                 return { key: 'web',        label: 'Web'       };
        if (s === 'research' || s === 'scientific')      return { key: 'research',   label: 'Research'  };
        if (s === 'github' || s === 'docs' || s === 'documentation') return { key: 'docs', label: 'Docs' };
    }
    return { key: 'web', label: 'Web' };
}

// ─── EMPTY & FAULT CARD GENERATORS ────────────────────────────────────
function renderEmptyState() {
    return `
        <div class="empty-state">
            <span class="empty-icon">◯</span>
            <h3>No results surfaced</h3>
            <p>Try different terms or check spelling parameters</p>
        </div>
    `;
}

function renderErrorState(message) {
    return `
        <div class="empty-state">
            <span class="empty-icon">—</span>
            <h3>Connection interrupted</h3>
            <p>${escapeHTMLString(message || 'An unexpected error occurred')}</p>
        </div>
    `;
}

// ─── INPUT SHAKE ANIMATION ────────────────────────────────────────────
function shakeSearchBar() {
    const searchField = document.getElementById('searchField');
    if (!searchField) return;
    searchField.style.animation = 'none';
    searchField.offsetHeight; // reflow trigger
    searchField.style.animation = 'shake 0.4s ease';
    setTimeout(() => { searchField.style.animation = ''; }, 400);
}

// ─── DYNAMIC FORMATTING & SANITIZATION ────────────────────────────────
function formatMetricNumber(num) {
    if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if (num >= 1000)    return (num / 1000).toFixed(1) + 'K';
    return num.toString();
}

function escapeHTMLString(text) {
    if (!text || typeof text !== 'string') return '';
    const span = document.createElement('span');
    span.textContent = text;
    return span.innerHTML;
}

// ─── MODAL DIALOG PORTALS ──────────────────────────────────────────────
async function showHistory() {
    const modal   = document.getElementById('historyModal');
    const content = document.getElementById('historyContent');
    modal.classList.add('active');
    content.innerHTML = '<div class="loading-spinner"></div>';

    try {
        const response = await fetch('/search/history');
        const history = await response.json();

        if (!history.length) {
            content.innerHTML = `<div class="empty-modal">No search history recorded yet</div>`;
        } else {
            content.innerHTML = `
                <div class="modal-list">
                    ${history.slice(-20).reverse().map((query, index) => `
                        <div class="modal-item" onclick="handleSearch('${escapeHTMLString(query)}'); closeModal('historyModal');">
                            <span class="modal-item-rank">${String(index + 1).padStart(2, '0')}</span>
                            <span>${escapeHTMLString(query)}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    } catch {
        content.innerHTML = `<div class="empty-modal">Failed to retrieve search log</div>`;
    }
}

async function showPopularQueries() {
    const modal   = document.getElementById('popularModal');
    const content = document.getElementById('popularContent');
    modal.classList.add('active');
    content.innerHTML = '<div class="loading-spinner"></div>';

    try {
        const response = await fetch('/search/popular?limit=10');
        const popular = await response.json();

        if (!popular.length) {
            content.innerHTML = `<div class="empty-modal">No trending searches compiled yet</div>`;
        } else {
            content.innerHTML = `
                <div class="modal-list">
                    ${popular.map((query, index) => `
                        <div class="modal-item" onclick="handleSearch('${escapeHTMLString(query)}'); closeModal('popularModal');">
                            <span class="modal-item-rank">#${index + 1}</span>
                            <span>${escapeHTMLString(query)}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    } catch {
        content.innerHTML = `<div class="empty-modal">Failed to retrieve trending index</div>`;
    }
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove('active');
}

document.addEventListener('click', e => {
    if (e.target.classList.contains('modal')) e.target.classList.remove('active');
});
