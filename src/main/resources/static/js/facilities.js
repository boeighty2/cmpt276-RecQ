// SFU RecQ - Epic 2: Facility Discovery

let allCourts = [];

const sportSelect = document.getElementById("sport");
const gymSelect = document.getElementById("gym");
const sessionSelect = document.getElementById("session");

const courtList = document.getElementById("courtList");
const statusMessage = document.getElementById("statusMessage");
const refreshButton = document.getElementById("refreshButton");

// Get court information from Epic 3 API
async function loadCourts() {
    statusMessage.textContent = "Loading court information...";

    try {
        const response = await fetch("/api/courts", {
            cache: "no-store"
        });

        if (!response.ok) {
            throw new Error("Failed to load courts");
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
            throw new Error("Invalid court data");
        }

        allCourts = data;

        updateFilters();
        displayCourts();

    } catch (error) {
        allCourts = [];
        courtList.replaceChildren();
        statusMessage.textContent =
            "Unable to load court information. Please try again.";

        console.error(error);
    }
}

// Update dropdown options
function updateSelect(select, values, defaultText) {
    const previousValue = select.value;

    select.replaceChildren(new Option(defaultText, ""));

    const uniqueValues = [...new Set(values.filter(Boolean))];

    uniqueValues.sort().forEach(value => {
        select.add(new Option(value, value));
    });

    const exists = [...select.options].some(
        option => option.value === previousValue
    );

    if (exists) {
        select.value = previousValue;
    }
}

// Populate Sport, Gym, and Session selections
function updateFilters() {
    updateSelect(
        sportSelect,
        allCourts.map(court => court.sportName),
        "All Sports"
    );

    updateSelect(
        gymSelect,
        allCourts.map(court => court.gymName),
        "All Gyms"
    );

    updateSelect(
        sessionSelect,
        allCourts.map(court => court.sessionName),
        "All Sessions"
    );
}

// Display courts based on selected filters
function displayCourts() {

    courtList.replaceChildren();

    const filteredCourts = allCourts.filter(court => {
        return (
            (!sportSelect.value ||
                court.sportName === sportSelect.value) &&

            (!gymSelect.value ||
                court.gymName === gymSelect.value) &&

            (!sessionSelect.value ||
                court.sessionName === sessionSelect.value)
        );
    });

    if (filteredCourts.length === 0) {
        statusMessage.textContent =
            "No courts found for the selected filters.";
        return;
    }

    statusMessage.textContent =
        `Showing ${filteredCourts.length} court(s).`;

    filteredCourts.forEach(court => {

        const card = document.createElement("article");
        card.className = "court-card";

        const title = document.createElement("h3");
        title.textContent = court.courtName;

        const location = document.createElement("p");
        location.textContent =
            `${court.gymName} | ${court.sportName}`;

        const session = document.createElement("p");
        session.textContent =
            `Session: ${court.sessionName}`;

        const status = document.createElement("p");
        status.textContent =
            `Status: ${court.status}`;

        status.className =
            `status-${String(court.status).toLowerCase()}`;

        card.append(title, location, session, status);

        courtList.appendChild(card);
    });
}

// Filter when selections change
sportSelect.addEventListener("change", displayCourts);
gymSelect.addEventListener("change", displayCourts);
sessionSelect.addEventListener("change", displayCourts);

// Refresh button
refreshButton.addEventListener("click", loadCourts);

// Load court information when the page opens
loadCourts();