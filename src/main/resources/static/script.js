// ─── INIT ─────────────────────────────────────────────────────────────
let currentResults = [];
let currentFilter = 'all';

document.addEventListener('DOMContentLoaded', () => {
    if (typeof lucide !== 'undefined') lucide.createIcons();

    const searchInput = document.getElementById('searchInput');

    // Enter key
    searchInput?.addEventListener('keypress', e => {
        if (e.key === 'Enter') handleSearch(searchInput.value);
    });

    // Typing shimmer on search bar
    searchInput?.addEventListener('input', () => {
        const wrapper = searchInput.closest('.search-wrapper');
        wrapper?.classList.toggle('has-value', searchInput.value.length > 0);
    });

    // Filter chips
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentFilter = chip.dataset.filter;
            filterResults(currentFilter);
        });
    });
});

// ─── SEARCH HANDLER ───────────────────────────────────────────────────
async function handleSearch(query) {
    query = query?.trim();
    if (!query) {
        shakeSearchBar();
        return;
    }

    const loader        = document.getElementById('loader');
    const resultsSection= document.getElementById('resultsSection');
    const resultsGrid   = document.getElementById('resultsGrid');
    const resultsCount  = document.getElementById('resultsCount');

    // Show loading state
    loader.style.display = 'block';
    resultsSection.style.display = 'none';
    resultsGrid.innerHTML = '';

    try {
        const response = await fetch(`/search?query=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error(`Server responded ${response.status}`);

        const data = await response.json();
        showResults(data, loader, resultsSection, resultsGrid, resultsCount);

    } catch (error) {
        loader.style.display = 'none';
        resultsSection.style.display = 'block';
        resultsGrid.innerHTML = renderError(error.message);
    }
}

// ─── SHOW RESULTS ─────────────────────────────────────────────────────
function showResults(data, loader, section, grid, countEl) {
    loader.style.display = 'none';
    section.style.display = 'block';
    currentResults = data; // Store for filtering
    section.scrollIntoView({ behavior: 'smooth', block: 'start' });

    const count = Array.isArray(data) ? data.length : 0;

    countEl.innerHTML = count
        ? `<strong>${count.toLocaleString()}</strong> result${count !== 1 ? 's' : ''} found`
        : '';

    if (count === 0) {
        grid.innerHTML = renderEmptyState();
    } else {
        grid.innerHTML = data.map((item, index) => renderCard(item, index)).join('');
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }
    
    // Show/hide filter button based on sources
    const sources = new Set(data.map(item => item.source));
    const clearBtn = document.querySelector('.clear-filters-btn');
    if (clearBtn) {
        clearBtn.style.display = sources.size > 1 ? 'block' : 'none';
    }
}

// ─── RENDER CARD ──────────────────────────────────────────────────────
function renderCard(item, index) {
    const source  = getSource(item);
    const title   = typeof item === 'string' ? item : (item.title || 'Untitled Result');
    const desc    = typeof item === 'object' && item.description
        ? item.description
        : `Relevant information about "${title}". Click to explore the full content.`;
    const url     = typeof item === 'object' && item.url ? item.url : '#';
    const views   = typeof item === 'object' && item.views ? item.views : 0;
    const likes   = typeof item === 'object' && item.likes ? item.likes : 0;
    const delay   = Math.min(index * 55, 400);

    return `
        <article class="result-card" 
            data-source="${source.key}"
            style="animation-delay: ${delay}ms">
            
            <span class="source-badge ${source.class}">${source.label}</span>

            <h3>${escapeHtml(title)}</h3>
            <p>${escapeHtml(desc)}</p>
            
            ${views > 0 ? `
            <div class="card-stats">
                <span class="stat-item">
                    <i data-lucide="eye" style="width:13px;height:13px;"></i>
                    ${formatNumber(views)}
                </span>
                <span class="stat-item">
                    <i data-lucide="thumbs-up" style="width:13px;height:13px;"></i>
                    ${formatNumber(likes)}
                </span>
            </div>
            ` : ''}

            <div class="card-footer">
                <div class="card-meta">
                    <span class="source-label">Source: ${source.label}</span>
                </div>
                <a href="${escapeHtml(url)}" 
                   class="visit-link" 
                   target="_blank" 
                   rel="noopener noreferrer">
                    Visit <i data-lucide="arrow-up-right" style="width:13px;height:13px;"></i>
                </a>
            </div>
        </article>
    `;
}

// ─── SOURCE MAPPING ───────────────────────────────────────────────────
function getSource(item) {
    if (typeof item === 'object' && item.source) {
        const s = item.source.toLowerCase();
        if (s === 'youtube')            return { key: 'youtube',  class: 'youtube',  label: 'YouTube'  };
        if (s === 'duckduckgo')         return { key: 'duckduckgo', class: 'duckduckgo', label: 'Web' };
        if (s === 'wikipedia')          return { key: 'wikipedia', class: 'wikipedia', label: 'Wikipedia' };
        if (s === 'web')                return { key: 'web',      class: 'web',      label: 'Web'      };
        if (s === 'research' || s === 'scientific') return { key: 'papers',   class: 'papers',   label: 'Research'   };
        if (s === 'github' || s === 'docs' || s === 'documentation') return { key: 'docs',    class: 'docs',     label: 'Docs'     };
    }
    return { key: 'web', class: 'web', label: 'Web' };
}

// ─── FILTER ───────────────────────────────────────────────────────────
function filterResults(filterLabel) {
    const cards = document.querySelectorAll('.result-card');
    const key   = filterLabel.toLowerCase();
    const clearBtn = document.querySelector('.clear-filters-btn');

    cards.forEach(card => {
        const src = (card.dataset.source || '').toLowerCase();
        const show = key === 'all' || src.includes(key) || key.includes(src);
        card.style.display = show ? '' : 'none';
    });
    
    // Show/hide clear button
    if (clearBtn) {
        clearBtn.style.display = key === 'all' ? 'none' : 'block';
    }

    // Update count
    const visible = [...cards].filter(c => c.style.display !== 'none').length;
    const countEl = document.getElementById('resultsCount');
    if (countEl) {
        countEl.innerHTML = `<strong>${visible.toLocaleString()}</strong> result${visible !== 1 ? 's' : ''} found`;
    }
}

function clearFilters() {
    currentFilter = 'all';
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    document.querySelector('.filter-chip[data-filter="all"]')?.classList.add('active');
    filterResults('all');
}

// ─── HELPERS ──────────────────────────────────────────────────────────
function getRelativeDate(index) {
    const offsets = ['Just now', '2m ago', '5m ago', '12m ago', '1h ago', '3h ago',
                     'Today', 'Yesterday', '2d ago', '1w ago'];
    return offsets[index % offsets.length];
}

function formatNumber(num) {
    if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toString();
}

function shakeSearchBar() {
    const container = document.querySelector('.search-container');
    if (!container) return;
    container.style.animation = 'none';
    container.offsetHeight; // reflow
    container.style.animation = 'shake 0.4s ease';
    setTimeout(() => container.style.animation = '', 400);
}

function renderEmptyState() {
    return `
        <div class="empty-state">
            <span class="empty-state-icon">🪐</span>
            <h3>Nothing found in the cosmos</h3>
            <p>Try different search terms or broaden your query</p>
        </div>
    `;
}

function renderError(msg) {
    return `
        <div class="empty-state">
            <span class="empty-state-icon">⚡</span>
            <h3>Connection disrupted</h3>
            <p>${escapeHtml(msg || 'An unexpected error occurred')}</p>
        </div>
    `;
}

function escapeHtml(text) {
    if (!text || typeof text !== 'string') return '';
    const d = document.createElement('div');
    d.textContent = text;
    return d.innerHTML;
}

// ─── MODAL FUNCTIONS ─────────────────────────────────────────────────
async function showHistory() {
    const modal = document.getElementById('historyModal');
    const content = document.getElementById('historyContent');
    
    modal.classList.add('active');
    content.innerHTML = '<div class="loading-spinner"></div>';
    
    try {
        const response = await fetch('/search/history');
        const history = await response.json();
        
        if (history.length === 0) {
            content.innerHTML = `
                <div class="empty-modal-state">
                    <i data-lucide="search" style="width:48px;height:48px;opacity:0.3;"></i>
                    <p>No search history yet</p>
                </div>
            `;
        } else {
            content.innerHTML = `
                <div class="history-list">
                    ${history.slice(-20).reverse().map(query => `
                        <div class="history-item" onclick="handleSearch('${escapeHtml(query)}'); closeModal('historyModal');">
                            <i data-lucide="clock" style="width:16px;height:16px;"></i>
                            <span>${escapeHtml(query)}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
        
        if (typeof lucide !== 'undefined') lucide.createIcons();
    } catch (error) {
        content.innerHTML = `<p class="error-text">Failed to load history</p>`;
    }
}

async function showPopularQueries() {
    const modal = document.getElementById('popularModal');
    const content = document.getElementById('popularContent');
    
    modal.classList.add('active');
    content.innerHTML = '<div class="loading-spinner"></div>';
    
    try {
        const response = await fetch('/search/popular?limit=10');
        const popular = await response.json();
        
        if (popular.length === 0) {
            content.innerHTML = `
                <div class="empty-modal-state">
                    <i data-lucide="trending-up" style="width:48px;height:48px;opacity:0.3;"></i>
                    <p>No popular queries yet</p>
                </div>
            `;
        } else {
            content.innerHTML = `
                <div class="popular-list">
                    ${popular.map((query, index) => `
                        <div class="popular-item" onclick="handleSearch('${escapeHtml(query)}'); closeModal('popularModal');">
                            <span class="rank">#${index + 1}</span>
                            <i data-lucide="trending-up" style="width:16px;height:16px;"></i>
                            <span class="query-text">${escapeHtml(query)}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
        
        if (typeof lucide !== 'undefined') lucide.createIcons();
    } catch (error) {
        content.innerHTML = `<p class="error-text">Failed to load popular queries</p>`;
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('active');
}

// Close modal when clicking outside
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('active');
    }
});
