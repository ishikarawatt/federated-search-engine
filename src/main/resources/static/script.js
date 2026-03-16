lucide.createIcons();

async function handleSearch(query) {

    if(!query) return;

    const loader = document.getElementById('loader');
    const resultsSection = document.getElementById('resultsSection');
    const resultsGrid = document.getElementById('resultsGrid');
    const resultsCount = document.getElementById('resultsCount');

    // Show loader
    loader.style.display = "block";

    try {

        const response = await fetch(`/search?query=${query}`);
        const data = await response.json();

        // Hide loader
        loader.style.display = "none";

        // Show results section
        resultsSection.style.display = "block";

        // Scroll to results
        resultsSection.scrollIntoView({ behavior: 'smooth' });

        // Update results count
        resultsCount.innerText = `${data.length} results found`;

        // Render results
        resultsGrid.innerHTML = data.map(item => `
            <div class="result-card">
                <div style="display:flex; justify-content:space-between; margin-bottom: 15px;">
                    <span style="font-size: 0.7rem; font-weight:700; color:var(--accent); background:rgba(6,182,212,0.1); padding:4px 10px; border-radius:5px;">RESULT</span>
                </div>

                <h3 style="margin-bottom:10px; line-height:1.4;">${item}</h3>

                <p style="font-size:0.85rem; color:#94a3b8; margin-bottom:20px;">
                    Aggregated result returned from federated search sources.
                </p>

                <div style="display:flex; align-items:center; gap:10px; border-top: 1px solid var(--border); padding-top:15px;">
                    <button style="background:transparent; border:1px solid var(--border); color:white; padding:8px 15px; border-radius:10px; font-size:0.8rem; cursor:pointer; flex:1">
                        Preview
                    </button>

                    <button style="background:var(--primary); border:none; color:white; padding:8px 15px; border-radius:10px; font-size:0.8rem; cursor:pointer; flex:1">
                        View Source
                    </button>
                </div>
            </div>
        `).join("");

        lucide.createIcons();

    } catch (error) {

        loader.style.display = "none";
        alert("Error fetching results from server");

        console.error(error);
    }
}