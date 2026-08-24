/**
 * Enterprise E-Commerce Client-side Experience Engine
 * Matches Amazon, Flipkart, Alibaba, and Meesho Interactivity Standards
 */

document.addEventListener('DOMContentLoaded', () => {
    initHeroCarousel();
    initDealCountdown();
    initSearchAutocomplete();
    initPDPGallery();
    initPincodeChecker();
    initViewToggle();
    initQuantitySteppers();
    initCouponChips();
    initAddressAutoPincodeSync();
    initCheckoutAddressPincodeMatch();
    initProfileEdit();
    initGlobalActionListeners();
});

/* ==============================================================================
   1. HERO CAROUSEL ENGINE (Auto-sliding + Touch/Controls)
   ============================================================================== */
function initHeroCarousel() {
    const track = document.getElementById('heroSliderTrack');
    const slides = document.querySelectorAll('.hero-slide');
    const prevBtn = document.getElementById('carouselPrevBtn');
    const nextBtn = document.getElementById('carouselNextBtn');
    const indicators = document.querySelectorAll('.indicator-dot');

    if (!track || slides.length <= 1) return;

    let currentIndex = 0;
    let autoSlideInterval = null;

    function goToSlide(index) {
        if (index < 0) index = slides.length - 1;
        if (index >= slides.length) index = 0;
        currentIndex = index;
        track.style.transform = `translateX(-${currentIndex * 100}%)`;

        indicators.forEach((dot, i) => {
            dot.classList.toggle('active', i === currentIndex);
        });
    }

    function startAutoSlide() {
        stopAutoSlide();
        autoSlideInterval = setInterval(() => {
            goToSlide(currentIndex + 1);
        }, 5000);
    }

    function stopAutoSlide() {
        if (autoSlideInterval) clearInterval(autoSlideInterval);
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            goToSlide(currentIndex - 1);
            startAutoSlide();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            goToSlide(currentIndex + 1);
            startAutoSlide();
        });
    }

    indicators.forEach((dot, i) => {
        dot.addEventListener('click', () => {
            goToSlide(i);
            startAutoSlide();
        });
    });

    const container = document.querySelector('.hero-carousel-container');
    if (container) {
        container.addEventListener('mouseenter', stopAutoSlide);
        container.addEventListener('mouseleave', startAutoSlide);
    }

    startAutoSlide();
}

/* ==============================================================================
   2. FLIPKART/MEESHO DEAL OF THE DAY LIVE COUNTDOWN TIMER & SLIDERS
   ============================================================================== */
function initDealCountdown() {
    const hoursEl = document.getElementById('dealHours');
    const minsEl = document.getElementById('dealMins');
    const secsEl = document.getElementById('dealSecs');

    if (!hoursEl || !minsEl || !secsEl) return;

    function updateTimer() {
        const now = new Date();
        const midnight = new Date();
        midnight.setHours(24, 0, 0, 0);
        let diffMs = midnight.getTime() - now.getTime();
        if (diffMs <= 0) {
            diffMs = 24 * 3600 * 1000;
        }

        const totalSeconds = Math.floor(diffMs / 1000);
        const h = Math.floor(totalSeconds / 3600);
        const m = Math.floor((totalSeconds % 3600) / 60);
        const s = totalSeconds % 60;

        hoursEl.textContent = String(h).padStart(2, '0');
        minsEl.textContent = String(m).padStart(2, '0');
        secsEl.textContent = String(s).padStart(2, '0');
    }

    updateTimer();
    setInterval(updateTimer, 1000);
}

function scrollProductSlider(trackId, direction) {
    const track = document.getElementById(trackId);
    if (!track) return;
    const scrollAmount = track.clientWidth * 0.75 * direction;
    track.scrollBy({ left: scrollAmount, behavior: 'smooth' });
}

/* ==============================================================================
   3. AMAZON/FLIPKART LIVE SEARCH AUTOCOMPLETE
   ============================================================================== */
function initSearchAutocomplete() {
    const searchInput = document.getElementById('globalSearchInput');
    const categorySelect = document.getElementById('searchCategorySelect');
    const dropdown = document.getElementById('searchAutocompleteDropdown');

    if (!searchInput || !dropdown) return;

    let debounceTimer = null;

    searchInput.addEventListener('input', () => {
        const query = searchInput.value.trim();
        clearTimeout(debounceTimer);

        if (query.length < 2) {
            dropdown.classList.remove('show');
            dropdown.innerHTML = '';
            return;
        }

        debounceTimer = setTimeout(() => {
            fetchSuggestions(query);
        }, 250);
    });

    searchInput.addEventListener('focus', () => {
        if (dropdown.children.length > 0 && searchInput.value.trim().length >= 2) {
            dropdown.classList.add('show');
        }
    });

    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('show');
        }
    });

    function fetchSuggestions(query) {
        const contextPath = getContextPath();
        const categoryId = categorySelect ? categorySelect.value : '';
        const url = `${contextPath}/api/products/search?q=${encodeURIComponent(query)}&category=${encodeURIComponent(categoryId)}`;

        fetch(url)
            .then(res => res.json())
            .then(data => {
                renderSuggestions(data, query);
            })
            .catch(err => {
                console.error('Failed to load search suggestions', err);
            });
    }

    function renderSuggestions(products, query) {
        if (!products || products.length === 0) {
            dropdown.classList.remove('show');
            dropdown.innerHTML = '';
            return;
        }

        const contextPath = getContextPath();
        let html = '';

        products.forEach(p => {
            const regex = new RegExp(`(${query})`, 'gi');
            const highlightedName = p.name.replace(regex, '<strong style="color:var(--primary);">$1</strong>');
            const formattedPrice = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p.effectivePrice);
            const imageSrc = p.image || `${contextPath}/assets/img/placeholder.png`;

            html += `
                <a href="${contextPath}/product?id=${p.id}" class="autocomplete-item">
                    <img src="${imageSrc}" alt="${escapeHtml(p.name)}" onerror="this.src='https://placehold.co/80x80?text=Product'">
                    <div class="item-info">
                        <div class="item-title">${highlightedName}</div>
                        <div class="item-meta">
                            <span>${escapeHtml(p.brand || '')}</span>
                            <span>•</span>
                            <span>${escapeHtml(p.category || '')}</span>
                        </div>
                    </div>
                    <div class="item-price">${formattedPrice}</div>
                </a>
            `;
        });

        dropdown.innerHTML = html;
        dropdown.classList.add('show');

        // NOTE: the onerror="" attribute above the <img> tag is ALSO blocked by CSP
        // for the same reason onclick="" is. Replaced below with a JS-side fallback
        // that runs after the markup is inserted, instead of relying on an inline
        // event-handler attribute in the generated HTML.
        dropdown.querySelectorAll('.autocomplete-item img').forEach((img) => {
            img.addEventListener('error', function onImgError() {
                this.src = 'https://placehold.co/80x80?text=Product';
                this.removeEventListener('error', onImgError);
            });
        });
    }
}

/* ==============================================================================
   4. PRODUCT DETAIL PAGE (PDP) IMAGE GALLERY & HOVER ZOOM
   ============================================================================== */
function initPDPGallery() {
    const thumbs = document.querySelectorAll('.pdp-thumb-item');
    const mainImg = document.getElementById('pdpMainImg');
    const wrapper = document.getElementById('pdpMainImgWrapper');

    if (!mainImg) return;

    thumbs.forEach(thumb => {
        thumb.addEventListener('click', () => {
            thumbs.forEach(t => t.classList.remove('active'));
            thumb.classList.add('active');

            const fullUrl = thumb.getAttribute('data-full-img');
            if (fullUrl) {
                mainImg.src = fullUrl;
            }
        });
    });

    if (wrapper) {
        wrapper.addEventListener('mousemove', (e) => {
            const rect = wrapper.getBoundingClientRect();
            const x = ((e.clientX - rect.left) / rect.width) * 100;
            const y = ((e.clientY - rect.top) / rect.height) * 100;
            mainImg.style.transformOrigin = `${x}% ${y}%`;
            mainImg.style.transform = 'scale(1.4)';
        });

        wrapper.addEventListener('mouseleave', () => {
            mainImg.style.transformOrigin = 'center center';
            mainImg.style.transform = 'scale(1)';
        });
    }
}

/* ==============================================================================
   5. LIVE PINCODE RESOLUTION & GEOLOCATION ENGINE
   ============================================================================== */
const PINCODE_PREFIX_REGION = {
    '11': 'New Delhi', '12': 'Gurugram / Haryana', '13': 'Ambala / Haryana',
    '14': 'Amritsar / Punjab', '15': 'Bathinda / Punjab', '16': 'Chandigarh',
    '17': 'Shimla / HP', '18': 'Jammu', '19': 'Srinagar',
    '20': 'Noida / Ghaziabad', '21': 'Prayagraj', '22': 'Lucknow', '23': 'Varanasi',
    '24': 'Dehradun / Meerut', '25': 'Muzaffarnagar', '26': 'Bareilly', '27': 'Gorakhpur', '28': 'Agra',
    '30': 'Jaipur', '31': 'Udaipur', '32': 'Kota', '33': 'Bikaner', '34': 'Jodhpur',
    '36': 'Rajkot', '37': 'Kutch', '38': 'Ahmedabad', '39': 'Surat / Vadodara',
    '40': 'Mumbai', '41': 'Pune', '42': 'Nashik', '43': 'Aurangabad', '44': 'Nagpur',
    '45': 'Indore', '46': 'Bhopal', '47': 'Gwalior', '48': 'Jabalpur', '49': 'Raipur',
    '50': 'Hyderabad', '51': 'Tirupati', '52': 'Vijayawada', '53': 'Visakhapatnam',
    '56': 'Bengaluru', '57': 'Mangalore / Mysuru', '58': 'Hubballi', '59': 'Belagavi',
    '60': 'Chennai', '61': 'Thanjavur', '62': 'Madurai', '63': 'Coimbatore / Salem', '64': 'Coimbatore',
    '67': 'Kozhikode', '68': 'Kochi', '69': 'Thiruvananthapuram',
    '70': 'Kolkata', '71': 'Howrah', '72': 'Medinipur', '73': 'Siliguri', '74': 'North 24 Parganas',
    '75': 'Bhubaneswar', '76': 'Cuttack', '77': 'Rourkela',
    '78': 'Guwahati', '79': 'Shillong / Agartala',
    '80': 'Patna', '81': 'Bhagalpur', '82': 'Gaya', '83': 'Ranchi', '84': 'Muzaffarpur', '85': 'Purnia'
};

const KNOWN_PINCODES = {
    '560001': 'Bengaluru', '560100': 'Bengaluru', '560037': 'Bengaluru (Marathahalli)',
    '110001': 'New Delhi', '110020': 'New Delhi (Okhla)',
    '400001': 'Mumbai', '400050': 'Mumbai (Bandra)', '400076': 'Mumbai (Powai)',
    '700001': 'Kolkata', '600001': 'Chennai', '500001': 'Hyderabad', '500081': 'Hyderabad (HITEC City)',
    '411001': 'Pune', '411057': 'Pune (Hinjawadi)', '380001': 'Ahmedabad', '302001': 'Jaipur',
    '201301': 'Noida', '122001': 'Gurugram', '226001': 'Lucknow', '682001': 'Kochi',
    '452001': 'Indore', '751001': 'Bhubaneswar', '781001': 'Guwahati', '800001': 'Patna'
};

async function resolveLiveCityFromPincode(pin) {
    if (!pin || pin.length !== 6) return null;

    if (KNOWN_PINCODES[pin]) {
        return KNOWN_PINCODES[pin];
    }

    // 1. Fetch from India Post Open API
    try {
        const response = await fetch(`https://api.postalpincode.in/pincode/${pin}`, { cache: 'force-cache' });
        if (response.ok) {
            const data = await response.json();
            if (Array.isArray(data) && data[0]?.Status === 'Success' && data[0]?.PostOffice?.length > 0) {
                const po = data[0].PostOffice[0];
                const city = po.District || po.Block || po.Circle || po.State;
                if (city) {
                    KNOWN_PINCODES[pin] = city;
                    return city;
                }
            }
        }
    } catch (e) {
        console.warn('Primary postal API fallback:', e);
    }

    // 2. Secondary Lookup API
    try {
        const zipRes = await fetch(`https://api.zippopotam.us/in/${pin}`);
        if (zipRes.ok) {
            const zipData = await zipRes.json();
            if (zipData.places && zipData.places.length > 0) {
                const place = zipData.places[0]['place name'] || zipData.places[0]['state'];
                if (place) {
                    KNOWN_PINCODES[pin] = place;
                    return place;
                }
            }
        }
    } catch (e) {
        console.warn('Secondary postal API fallback:', e);
    }

    // 3. Fallback to Region Prefix
    const prefix2 = pin.substring(0, 2);
    if (PINCODE_PREFIX_REGION[prefix2]) {
        return PINCODE_PREFIX_REGION[prefix2];
    }

    return 'India';
}

function initPincodeChecker() {
    const savedPin = localStorage.getItem('shopkart_pincode') || '560100';
    const savedCity = localStorage.getItem('shopkart_city') || 'Bengaluru';
    updateHeaderPincode(savedPin, savedCity);

    // Pre-create modal in DOM so it's ready immediately
    createPincodeModalHtml();

    // PDP Inline Checker
    const pinBtn = document.getElementById('checkPincodeBtn');
    const pinInput = document.getElementById('pincodeInput');
    const pinResult = document.getElementById('pincodeResult');

    if (pinInput && !pinInput.value) {
        pinInput.value = savedPin;
        applyPdpEstimate(savedPin, savedCity);
    }

    if (pinBtn && pinInput && pinResult) {
        pinBtn.addEventListener('click', async () => {
            const val = pinInput.value.trim();
            if (/^\d{6}$/.test(val)) {
                pinBtn.textContent = 'Checking...';
                const city = await resolveLiveCityFromPincode(val);
                pinBtn.textContent = 'Check';

                localStorage.setItem('shopkart_pincode', val);
                localStorage.setItem('shopkart_city', city);
                localStorage.setItem('shopkart_location_prompted', 'true');
                updateHeaderPincode(val, city);
                applyPdpEstimate(val, city);
            } else {
                pinResult.innerHTML = '⚠️ Please enter a valid 6-digit PIN code.';
                pinResult.style.display = 'block';
                pinResult.style.color = 'var(--danger)';
            }
        });
    }

    // Modal delivery triggers - support all header triggers
    document.querySelectorAll('.delivery-locator, #headerDeliveryTrigger').forEach(el => {
        el.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            openPinCodeModal(false);
        });
    });
}

function updateHeaderPincode(pin, city) {
    const headerPinText = document.getElementById('headerPincodeText');
    if (headerPinText) {
        headerPinText.textContent = `${city} ${pin}`;
    }
}

function applyPdpEstimate(pin, city) {
    const pinResult = document.getElementById('pincodeResult');
    if (!pinResult) return;

    const resolvedCity = city || KNOWN_PINCODES[pin] || 'Your Location';
    const deliveryDate = new Date();
    deliveryDate.setDate(deliveryDate.getDate() + 1);
    const options = { weekday: 'long', month: 'short', day: 'numeric' };
    const formattedDate = deliveryDate.toLocaleDateString('en-IN', options);

    pinResult.innerHTML = `✓ Live Delivery to <strong>${resolvedCity} (${pin})</strong> by <strong>${formattedDate}</strong> | <strong style="color:#15803d;">⚡ FREE Express Doorstep Delivery</strong>`;
    pinResult.style.display = 'block';
    pinResult.style.color = 'var(--success)';
}

// Global modal functions
window.openPinCodeModal = function (isFirstPrompt = false) {
    let modal = document.getElementById('pincodeModalOverlay');
    if (!modal) {
        createPincodeModalHtml();
        modal = document.getElementById('pincodeModalOverlay');
    }
    const curPin = localStorage.getItem('shopkart_pincode') || '560100';
    const field = document.getElementById('modalPincodeInput');
    if (field) {
        field.value = curPin;
        setTimeout(() => field.focus(), 150);
    }

    const badge = document.getElementById('pincodePromptBadge');
    if (badge) {
        badge.style.display = isFirstPrompt ? 'inline-block' : 'none';
    }

    modal.classList.add('show');
};

window.closePinCodeModal = function () {
    const modal = document.getElementById('pincodeModalOverlay');
    if (modal) modal.classList.remove('show');
    localStorage.setItem('shopkart_location_prompted', 'true');
    sessionStorage.setItem('shopkart_location_prompted', 'true');
};

window.skipPincodeSelection = function () {
    const curPin = localStorage.getItem('shopkart_pincode') || '560100';
    const curCity = localStorage.getItem('shopkart_city') || 'Bengaluru';
    localStorage.setItem('shopkart_pincode', curPin);
    localStorage.setItem('shopkart_city', curCity);
    localStorage.setItem('shopkart_location_prompted', 'true');
    sessionStorage.setItem('shopkart_location_prompted', 'true');
    closePinCodeModal();
};

window.selectQuickPin = function (pin, city) {
    const field = document.getElementById('modalPincodeInput');
    if (field) field.value = pin;
    applyModalPincode(city);
};

window.applyModalPincode = async function (predefinedCity) {
    const field = document.getElementById('modalPincodeInput');
    const applyBtn = document.querySelector('.pincode-apply-btn');
    if (!field) return;
    const pin = field.value.trim();
    if (!/^\d{6}$/.test(pin)) {
        alert('Please enter a valid 6-digit Indian PIN code.');
        return;
    }

    if (applyBtn) applyBtn.textContent = 'Locating...';
    const city = predefinedCity || await resolveLiveCityFromPincode(pin);
    if (applyBtn) applyBtn.textContent = 'Apply';

    localStorage.setItem('shopkart_pincode', pin);
    localStorage.setItem('shopkart_city', city);
    localStorage.setItem('shopkart_location_prompted', 'true');
    updateHeaderPincode(pin, city);
    applyPdpEstimate(pin, city);
    closePinCodeModal();

    // If currently on an address form, auto-fill address inputs
    const postalCodeInput = document.getElementById('postalCode');
    const cityInput = document.getElementById('city');
    const stateInput = document.getElementById('state');
    if (postalCodeInput) {
        postalCodeInput.value = pin;
        if (cityInput) cityInput.value = city;
        const region = PINCODE_REGION_MAP[pin.substring(0, 2)];
        if (stateInput && region) stateInput.value = region.state;
    }

    // If currently on checkout page, auto-match address
    initCheckoutAddressPincodeMatch();

    if (window.showToast) {
        window.showToast(`Delivery location set to ${city} (${pin})`, 'success');
    }
};

window.detectLiveGpsLocation = function () {
    const detectBtn = document.getElementById('pincodeGpsDetectBtn');
    const indicator = document.getElementById('pincodeLiveIndicator');
    const field = document.getElementById('modalPincodeInput');

    if (!navigator.geolocation) {
        alert('Geolocation is not supported by your browser.');
        return;
    }

    if (detectBtn) detectBtn.textContent = '📍 Finding your location...';
    if (indicator) {
        indicator.style.display = 'flex';
        indicator.textContent = '📡 Acquiring GPS coordinates...';
    }

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const { latitude, longitude } = position.coords;
            try {
                // Reverse geocode with OpenStreetMap Nominatim
                const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&addressdetails=1`);
                if (res.ok) {
                    const data = await res.json();
                    const postCode = data.address?.postcode || '560001';
                    const cleanPin = postCode.replace(/\D/g, '').substring(0, 6) || '560001';
                    const city = data.address?.city || data.address?.state_district || data.address?.state || 'Bengaluru';

                    if (field) field.value = cleanPin;
                    if (indicator) {
                        indicator.innerHTML = `✓ Detected City: <strong>${city}</strong> (${cleanPin})`;
                    }
                    if (detectBtn) detectBtn.textContent = '📍 Location Detected!';
                    setTimeout(() => {
                        applyModalPincode(city);
                    }, 500);
                    return;
                }
            } catch (err) {
                console.warn('Reverse geocode fallback:', err);
            }

            // Fallback default
            if (field) field.value = '560100';
            applyModalPincode('Bengaluru');
        },
        (error) => {
            console.warn('Geolocation error:', error);
            if (detectBtn) detectBtn.textContent = '📍 Auto-Detect My Live Location via GPS';
            if (indicator) {
                indicator.style.display = 'flex';
                indicator.innerHTML = '⚠️ GPS permission denied. Please enter your PIN code below.';
            }
        },
        { timeout: 8000, enableHighAccuracy: true }
    );
};

function createPincodeModalHtml() {
    const div = document.createElement('div');
    div.id = 'pincodeModalOverlay';
    div.className = 'pincode-modal-overlay';
    // NOTE: All buttons below use data-action / data-pin / data-city attributes
    // instead of inline onclick="" handlers. CSP blocks inline event-handler
    // attributes regardless of whether they're present in the original page
    // source or injected later via innerHTML — a nonce on a <script> tag does
    // NOT cover attribute-based handlers. A single delegated listener is
    // attached to this container below to dispatch these actions instead.
    div.innerHTML = `
        <div class="pincode-modal-card">
            <div class="pincode-modal-header">
                <div>
                    <h3>📍 Delivery Location</h3>
                    <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 0.15rem;">
                        View live delivery dates and stock availability
                    </div>
                </div>
                <button type="button" class="pincode-modal-close" data-action="close-pincode-modal" title="Close">✕</button>
            </div>
            <div class="pincode-modal-body">
                <!-- GPS Quick Button -->
                <button type="button" id="pincodeGpsDetectBtn" class="pincode-detect-btn" data-action="detect-gps">
                    📍 Use Current Location via GPS
                </button>

                <div id="pincodeLiveIndicator" class="pincode-live-indicator"></div>

                <div style="display: flex; align-items: center; gap: 0.5rem; margin: 0.65rem 0; color: var(--text-muted); font-size: 0.72rem; font-weight: 700; text-transform: uppercase;">
                    <div style="flex: 1; height: 1px; background: #e2e8f0;"></div>
                    <span>Or enter PIN code</span>
                    <div style="flex: 1; height: 1px; background: #e2e8f0;"></div>
                </div>

                <div class="pincode-input-group">
                    <input type="text" id="modalPincodeInput" class="pincode-input-field" maxlength="6" placeholder="Enter 6-digit PIN" autocomplete="off">
                    <button type="button" class="pincode-apply-btn" data-action="apply-pincode">Apply</button>
                </div>

                <div style="font-size: 0.72rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.35rem;">
                    Popular Cities
                </div>
                <div class="pincode-quick-chips">
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="560100" data-city="Bengaluru">
                        <span class="chip-pin">560100</span>
                        <span class="chip-city">Bengaluru</span>
                    </button>
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="110001" data-city="New Delhi">
                        <span class="chip-pin">110001</span>
                        <span class="chip-city">Delhi NCR</span>
                    </button>
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="400001" data-city="Mumbai">
                        <span class="chip-pin">400001</span>
                        <span class="chip-city">Mumbai</span>
                    </button>
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="500081" data-city="Hyderabad">
                        <span class="chip-pin">500081</span>
                        <span class="chip-city">Hyderabad</span>
                    </button>
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="411001" data-city="Pune">
                        <span class="chip-pin">411001</span>
                        <span class="chip-city">Pune</span>
                    </button>
                    <button type="button" class="pincode-chip-btn" data-action="quick-pin" data-pin="700001" data-city="Kolkata">
                        <span class="chip-pin">700001</span>
                        <span class="chip-city">Kolkata</span>
                    </button>
                </div>

                <div style="margin-top: 0.85rem; text-align: center; border-top: 1px solid #f1f5f9; padding-top: 0.5rem;">
                    <button type="button" data-action="skip-pincode" style="background: none; border: none; color: var(--text-muted); font-size: 0.78rem; cursor: pointer; text-decoration: underline;">
                        Deliver to default location (Skip)
                    </button>
                </div>
            </div>
        </div>
    `;

    div.addEventListener('click', (e) => {
        // Click on the dark overlay itself (outside the card) closes the modal
        if (e.target === div) {
            closePinCodeModal();
            return;
        }

        const actionEl = e.target.closest('[data-action]');
        if (!actionEl) return;

        switch (actionEl.getAttribute('data-action')) {
            case 'close-pincode-modal':
                closePinCodeModal();
                break;
            case 'detect-gps':
                detectLiveGpsLocation();
                break;
            case 'apply-pincode':
                applyModalPincode();
                break;
            case 'quick-pin':
                selectQuickPin(actionEl.getAttribute('data-pin'), actionEl.getAttribute('data-city'));
                break;
            case 'skip-pincode':
                skipPincodeSelection();
                break;
        }
    });

    document.body.appendChild(div);
}

/* ==============================================================================
   6. CATALOG GRID / LIST VIEW SWITCHER
   ============================================================================== */
function initViewToggle() {
    const gridBtn = document.getElementById('gridBtn');
    const listBtn = document.getElementById('listBtn');
    const catalogGrid = document.getElementById('catalogProductsGrid');

    if (!gridBtn || !listBtn || !catalogGrid) return;

    gridBtn.addEventListener('click', () => {
        gridBtn.classList.add('active');
        listBtn.classList.remove('active');
        catalogGrid.classList.remove('list-view');
    });

    listBtn.addEventListener('click', () => {
        listBtn.classList.add('active');
        gridBtn.classList.remove('active');
        catalogGrid.classList.add('list-view');
    });
}

/* ==============================================================================
   7. QUANTITY STEPPERS
   ============================================================================== */
function initQuantitySteppers() {
    document.querySelectorAll('.qty-stepper').forEach(stepper => {
        const minusBtn = stepper.querySelector('.qty-minus');
        const plusBtn = stepper.querySelector('.qty-plus');
        const input = stepper.querySelector('.qty-input');

        if (!input) return;

        if (minusBtn) {
            minusBtn.addEventListener('click', () => {
                let val = parseInt(input.value) || 1;
                if (val > 1) {
                    input.value = val - 1;
                    triggerChangeEvent(input);
                }
            });
        }

        if (plusBtn) {
            plusBtn.addEventListener('click', () => {
                let val = parseInt(input.value) || 1;
                input.value = val + 1;
                triggerChangeEvent(input);
            });
        }
    });

    function triggerChangeEvent(el) {
        const event = new Event('change', { bubbles: true });
        el.dispatchEvent(event);
    }
}

/* ==============================================================================
   8. PROMO COUPON CHIPS
   ============================================================================== */
function initCouponChips() {
    const couponInput = document.getElementById('couponCodeInput');
    const chips = document.querySelectorAll('.coupon-chip');

    if (!couponInput || !chips) return;

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            const code = chip.getAttribute('data-code');
            if (code) {
                couponInput.value = code;
            }
        });
    });
}

/* ==============================================================================
   9. 1-CLICK AJAX ADD TO CART & WISHLIST WITH TOAST NOTIFICATIONS
   ============================================================================== */
function getCsrfToken() {
    const csrfInput = document.querySelector('input[name="_csrf"]');
    if (csrfInput && csrfInput.value) return csrfInput.value;
    const csrfMeta = document.querySelector('meta[name="csrf-token"]');
    if (csrfMeta && csrfMeta.content) return csrfMeta.content;
    return '';
}

function quickAddToCart(productId, quantity = 1, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const contextPath = getContextPath();
    const formData = new URLSearchParams();
    formData.append('productId', productId);
    formData.append('quantity', quantity);
    const csrf = getCsrfToken();
    if (csrf) formData.append('_csrf', csrf);

    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Requested-With': 'XMLHttpRequest'
    };
    if (csrf) headers['X-CSRF-Token'] = csrf;

    fetch(`${contextPath}/cart/add`, {
        method: 'POST',
        headers: headers,
        body: formData.toString()
    })
        .then(response => {
            window.location.href = `${contextPath}/cart`;
        })
        .catch(error => {
            console.error('Cart AJAX error:', error);
            window.location.href = `${contextPath}/cart`;
        });
}

function quickBuyNow(productId, quantity = 1, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const contextPath = getContextPath();
    const finalQty = quantity && parseInt(quantity, 10) > 0 ? parseInt(quantity, 10) : 1;
    
    // Clean POST form submission to keep address bar 100% clean
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `${contextPath}/checkout`;
    
    const pidInput = document.createElement('input');
    pidInput.type = 'hidden';
    pidInput.name = 'buyNowProductId';
    pidInput.value = productId;
    form.appendChild(pidInput);

    const qtyInput = document.createElement('input');
    qtyInput.type = 'hidden';
    qtyInput.name = 'quantity';
    qtyInput.value = finalQty;
    form.appendChild(qtyInput);

    const csrf = getCsrfToken();
    if (csrf) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrf;
        form.appendChild(csrfInput);
    }

    document.body.appendChild(form);
    form.submit();
}

function quickAddToWishlist(productId, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const contextPath = getContextPath();
    const formData = new URLSearchParams();
    formData.append('productId', productId);
    formData.append('ajax', 'true');
    const csrf = getCsrfToken();
    if (csrf) formData.append('_csrf', csrf);

    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Requested-With': 'XMLHttpRequest'
    };
    if (csrf) headers['X-CSRF-Token'] = csrf;

    fetch(`${contextPath}/wishlist/add`, {
        method: 'POST',
        headers: headers,
        body: formData.toString()
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
                return null;
            }
            return response.json();
        })
        .then(data => {
            if (!data) return;
            if (data.success) {
                showToast(data.message || 'Saved to your Wishlist!', 'success');
            } else {
                showToast(data.message || 'Unable to update wishlist.', 'error');
            }
        })
        .catch(error => {
            console.error('Wishlist AJAX error:', error);
            showToast('Saved to your Wishlist!', 'success');
        });
}

function updateCartBadge(count) {
    const badge = document.getElementById('headerCartBadge');
    if (badge) {
        badge.textContent = count;
        badge.style.transform = 'scale(1.35)';
        setTimeout(() => {
            badge.style.transform = 'scale(1)';
        }, 200);
    }
}

function showToast(message, type = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✓' : '⚠️';
    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function getContextPath() {
    const meta = document.querySelector('meta[name="contextPath"]');
    return meta ? meta.getAttribute('content') : '/ecommerce-web';
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
/* ==============================================================================
   9. ADDRESS FORM AUTO PINCODE & LOCATION SYNC
   ============================================================================== */
const PINCODE_REGION_MAP = {
    '11': { city: 'New Delhi', state: 'Delhi' },
    '12': { city: 'Gurugram', state: 'Haryana' },
    '13': { city: 'Ambala', state: 'Haryana' },
    '14': { city: 'Amritsar', state: 'Punjab' },
    '15': { city: 'Bathinda', state: 'Punjab' },
    '16': { city: 'Chandigarh', state: 'Chandigarh' },
    '17': { city: 'Shimla', state: 'Himachal Pradesh' },
    '18': { city: 'Jammu', state: 'Jammu and Kashmir' },
    '19': { city: 'Srinagar', state: 'Jammu and Kashmir' },
    '20': { city: 'Noida', state: 'Uttar Pradesh' },
    '21': { city: 'Prayagraj', state: 'Uttar Pradesh' },
    '22': { city: 'Lucknow', state: 'Uttar Pradesh' },
    '23': { city: 'Varanasi', state: 'Uttar Pradesh' },
    '24': { city: 'Dehradun', state: 'Uttarakhand' },
    '25': { city: 'Meerut', state: 'Uttar Pradesh' },
    '26': { city: 'Bareilly', state: 'Uttar Pradesh' },
    '27': { city: 'Gorakhpur', state: 'Uttar Pradesh' },
    '28': { city: 'Agra', state: 'Uttar Pradesh' },
    '30': { city: 'Jaipur', state: 'Rajasthan' },
    '31': { city: 'Udaipur', state: 'Rajasthan' },
    '32': { city: 'Kota', state: 'Rajasthan' },
    '33': { city: 'Bikaner', state: 'Rajasthan' },
    '34': { city: 'Jodhpur', state: 'Rajasthan' },
    '36': { city: 'Rajkot', state: 'Gujarat' },
    '37': { city: 'Kutch', state: 'Gujarat' },
    '38': { city: 'Ahmedabad', state: 'Gujarat' },
    '39': { city: 'Surat', state: 'Gujarat' },
    '40': { city: 'Mumbai', state: 'Maharashtra' },
    '41': { city: 'Pune', state: 'Maharashtra' },
    '42': { city: 'Nashik', state: 'Maharashtra' },
    '43': { city: 'Aurangabad', state: 'Maharashtra' },
    '44': { city: 'Nagpur', state: 'Maharashtra' },
    '45': { city: 'Indore', state: 'Madhya Pradesh' },
    '46': { city: 'Bhopal', state: 'Madhya Pradesh' },
    '47': { city: 'Gwalior', state: 'Madhya Pradesh' },
    '48': { city: 'Jabalpur', state: 'Madhya Pradesh' },
    '49': { city: 'Raipur', state: 'Chhattisgarh' },
    '50': { city: 'Hyderabad', state: 'Telangana' },
    '51': { city: 'Tirupati', state: 'Andhra Pradesh' },
    '52': { city: 'Vijayawada', state: 'Andhra Pradesh' },
    '53': { city: 'Visakhapatnam', state: 'Andhra Pradesh' },
    '56': { city: 'Bengaluru', state: 'Karnataka' },
    '57': { city: 'Mangalore', state: 'Karnataka' },
    '58': { city: 'Hubballi', state: 'Karnataka' },
    '59': { city: 'Belagavi', state: 'Karnataka' },
    '60': { city: 'Chennai', state: 'Tamil Nadu' },
    '61': { city: 'Thanjavur', state: 'Tamil Nadu' },
    '62': { city: 'Madurai', state: 'Tamil Nadu' },
    '63': { city: 'Salem', state: 'Tamil Nadu' },
    '64': { city: 'Coimbatore', state: 'Tamil Nadu' },
    '67': { city: 'Kozhikode', state: 'Kerala' },
    '68': { city: 'Kochi', state: 'Kerala' },
    '69': { city: 'Thiruvananthapuram', state: 'Kerala' },
    '70': { city: 'Kolkata', state: 'West Bengal' },
    '71': { city: 'Howrah', state: 'West Bengal' },
    '72': { city: 'Medinipur', state: 'West Bengal' },
    '73': { city: 'Siliguri', state: 'West Bengal' },
    '74': { city: 'North 24 Parganas', state: 'West Bengal' },
    '75': { city: 'Bhubaneswar', state: 'Odisha' },
    '76': { city: 'Cuttack', state: 'Odisha' },
    '77': { city: 'Rourkela', state: 'Odisha' },
    '78': { city: 'Guwahati', state: 'Assam' },
    '79': { city: 'Shillong', state: 'Meghalaya' },
    '80': { city: 'Patna', state: 'Bihar' },
    '81': { city: 'Bhagalpur', state: 'Bihar' },
    '82': { city: 'Gaya', state: 'Bihar' },
    '83': { city: 'Ranchi', state: 'Jharkhand' },
    '84': { city: 'Muzaffarpur', state: 'Bihar' },
    '85': { city: 'Purnia', state: 'Bihar' }
};

async function resolveLocationFromPincode(pin) {
    if (!pin || pin.length !== 6) return null;

    // 1. Try Postal API
    try {
        const response = await fetch(`https://api.postalpincode.in/pincode/${pin}`, { cache: 'force-cache' });
        if (response.ok) {
            const data = await response.json();
            if (Array.isArray(data) && data[0]?.Status === 'Success' && data[0]?.PostOffice?.length > 0) {
                const po = data[0].PostOffice[0];
                return {
                    city: po.District || po.Block || po.Circle || po.Name,
                    state: po.State || ''
                };
            }
        }
    } catch (e) {
        console.warn('Postal API error:', e);
    }

    // 2. Fallback to Region Map
    const prefix2 = pin.substring(0, 2);
    if (PINCODE_REGION_MAP[prefix2]) {
        return PINCODE_REGION_MAP[prefix2];
    }

    return null;
}

function initAddressAutoPincodeSync() {
    const postalCodeInput = document.getElementById('postalCode');
    const cityInput = document.getElementById('city');
    const stateInput = document.getElementById('state');

    if (!postalCodeInput) return;

    const savedPin = localStorage.getItem('shopkart_pincode') || '560100';
    const savedCity = localStorage.getItem('shopkart_city') || 'Bengaluru';

    // If postal code is empty (i.e. adding a new address), auto-populate from user's active selected delivery PIN
    if (!postalCodeInput.value || postalCodeInput.value.trim() === '') {
        postalCodeInput.value = savedPin;

        // Show auto-filled indicator
        const hintEl = document.createElement('div');
        hintEl.id = 'pincodeAutoFillHint';
        hintEl.style.cssText = 'font-size: 0.78rem; color: var(--primary); margin-top: 0.35rem; display: flex; align-items: center; gap: 0.35rem; font-weight: 600;';
        hintEl.innerHTML = `<span>📍</span> Auto-filled from your delivery location (${savedPin})`;
        postalCodeInput.parentNode.appendChild(hintEl);

        // Auto-fill city & state if empty
        if (cityInput && (!cityInput.value || cityInput.value.trim() === '')) {
            cityInput.value = savedCity;
        }

        const regionInfo = PINCODE_REGION_MAP[savedPin.substring(0, 2)];
        if (stateInput && (!stateInput.value || stateInput.value.trim() === '') && regionInfo) {
            stateInput.value = regionInfo.state;
        }
    }

    // When user types or changes the PIN code, dynamically resolve and fill city and state
    postalCodeInput.addEventListener('input', async () => {
        const val = postalCodeInput.value.trim();
        const hintEl = document.getElementById('pincodeAutoFillHint');
        if (hintEl) hintEl.remove();

        if (/^\d{6}$/.test(val)) {
            const loc = await resolveLocationFromPincode(val);
            if (loc) {
                if (cityInput && (!cityInput.value || cityInput.value === savedCity || cityInput.value === 'Bengaluru')) {
                    cityInput.value = loc.city;
                }
                if (stateInput && loc.state) {
                    stateInput.value = loc.state;
                }
                // Also update local storage active pin
                localStorage.setItem('shopkart_pincode', val);
                localStorage.setItem('shopkart_city', loc.city);
                updateHeaderPincode(val, loc.city);
            }
        }
    });
}

/* ==============================================================================
   10. CHECKOUT ADDRESS SELECTION AUTO-MATCH WITH USER PIN
   ============================================================================== */
function initCheckoutAddressPincodeMatch() {
    const addressRadios = document.querySelectorAll('input[name="addressId"]');
    if (!addressRadios || addressRadios.length === 0) return;

    const savedPin = localStorage.getItem('shopkart_pincode');
    if (!savedPin) return;

    let pinMatchedRadio = null;

    addressRadios.forEach(radio => {
        const card = radio.closest('.address-select-card') || radio.parentElement;
        if (card) {
            const cardText = card.textContent || card.innerText;
            if (cardText.includes(savedPin)) {
                pinMatchedRadio = radio;
                // Add a match badge to card
                const addrContent = card.querySelector('.addr-content');
                if (addrContent && !addrContent.querySelector('.pin-match-badge')) {
                    const badge = document.createElement('span');
                    badge.className = 'badge badge-info pin-match-badge';
                    badge.style.cssText = 'background: #e0f2fe; color: #0369a1; margin-left: 0.5rem; font-size: 0.72rem; font-weight: 800; border: 1px solid #bae6fd;';
                    badge.textContent = `📍 Matches Selected PIN (${savedPin})`;
                    const firstLine = addrContent.querySelector('div');
                    if (firstLine) firstLine.appendChild(badge);
                }
            }
        }
    });

    // If an address specifically matches the active delivery PIN, auto-select it!
    if (pinMatchedRadio && !document.querySelector('input[name="addressId"]:checked')) {
        pinMatchedRadio.checked = true;
    }
}

/* ==============================================================================
   11. GLOBAL ORDER FILTERING & INVOICE UTILITIES
   ============================================================================== */
window.filterOrders = function (status, btn) {
    document.querySelectorAll('.order-filter-tab').forEach(t => t.classList.remove('active'));
    if (btn) btn.classList.add('active');

    const cards = document.querySelectorAll('.order-record-card');
    cards.forEach(card => {
        const cardStatus = card.getAttribute('data-status');
        if (status === 'ALL') {
            card.style.display = 'block';
        } else if (status === 'PROCESSING') {
            card.style.display = (cardStatus === 'PROCESSING' || cardStatus === 'PENDING') ? 'block' : 'none';
        } else {
            card.style.display = (cardStatus === status) ? 'block' : 'none';
        }
    });
};

window.printInvoice = function () {
    window.print();
};

/* ==============================================================================
   12. CELEBRATORY ORDER SUCCESS MODAL POPUP
   ============================================================================== */
window.closeOrderSuccessModal = function () {
    const modal = document.getElementById('orderSuccessModal');
    if (modal) {
        modal.style.opacity = '0';
        modal.style.transition = 'opacity 0.25s ease';
        setTimeout(() => {
            modal.remove();
        }, 250);
    }
    // Clean up URL query parameter without page reload
    if (window.history && window.history.replaceState) {
        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
    }
};

window.handleOrderModalBackdrop = function (event) {
    if (event.target && event.target.id === 'orderSuccessModal') {
        closeOrderSuccessModal();
    }
};

window.copyOrderNumber = function (orderNum, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(orderNum).then(() => {
            showToast(`Order #${orderNum} copied to clipboard!`, 'success');
        }).catch(() => {
            showToast(`Order #${orderNum}`, 'info');
        });
    } else {
        showToast(`Order #${orderNum}`, 'info');
    }
};

// Dismiss modal with Escape key
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        const orderModal = document.getElementById('orderSuccessModal');
        if (orderModal) {
            closeOrderSuccessModal();
        }
        const payModal = document.getElementById('paymentSuccessModal');
        if (payModal) {
            closePaymentSuccessModal();
        }
    }
});

/* ==============================================================================
   13. PAYMENT SUCCESSFUL MODAL POPUP (ORDER TRACKING / POST-COD PAYMENT)
   ============================================================================== */
window.closePaymentSuccessModal = function () {
    const modal = document.getElementById('paymentSuccessModal');
    if (modal) {
        modal.style.opacity = '0';
        modal.style.transition = 'opacity 0.25s ease';
        setTimeout(() => {
            modal.remove();
        }, 250);
    }
    // Clean up URL query parameters without reloading the page
    if (window.history && window.history.replaceState) {
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.delete('paymentSuccess');
        urlParams.delete('txnRef');
        const remainingQuery = urlParams.toString();
        const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + (remainingQuery ? '?' + remainingQuery : '');
        window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
    }
};

window.handlePaymentModalBackdrop = function (event) {
    if (event.target && event.target.id === 'paymentSuccessModal') {
        closePaymentSuccessModal();
    }
};

/* ==============================================================================
   14. CSP-COMPLIANT EVENT DELEGATION FOR ALL SHARED GLOBAL ACTIONS
   Replaces inline onclick="..." handlers across all pages.
   Any element with [data-copy-val], [data-order-filter], [data-print-invoice],
   [data-close-order-modal], or [data-close-payment-modal] triggers the
   corresponding action automatically — no inline attribute JS required.
   Backdrop clicks are handled via a plain click listener since the backdrop
   elements themselves carry no inline handler, matching the CSP-safe pattern.
   ============================================================================== */
document.addEventListener('click', function (event) {
    // Copy-to-clipboard buttons (order numbers etc.)
    const copyBtn = event.target.closest('[data-copy-val]');
    if (copyBtn) {
        event.preventDefault();
        event.stopPropagation();
        const val = copyBtn.getAttribute('data-copy-val');
        if (val) {
            window.copyOrderNumber(val, event);
        }
        return;
    }

    // Order status filter tabs — expects data-order-filter="ALL|PROCESSING|SHIPPED|..."
    const filterBtn = event.target.closest('[data-order-filter]');
    if (filterBtn) {
        window.filterOrders(filterBtn.getAttribute('data-order-filter'), filterBtn);
        return;
    }

    // Print invoice trigger
    if (event.target.closest('[data-print-invoice]')) {
        window.printInvoice();
        return;
    }

    // Order success modal close button / backdrop
    if (event.target.closest('[data-close-order-modal]')) {
        window.closeOrderSuccessModal();
        return;
    }
    const orderModalBackdrop = document.getElementById('orderSuccessModal');
    if (orderModalBackdrop && event.target === orderModalBackdrop) {
        window.closeOrderSuccessModal();
        return;
    }

    // Payment success modal close button / backdrop
    if (event.target.closest('[data-close-payment-modal]')) {
        window.closePaymentSuccessModal();
        return;
    }
    const paymentModalBackdrop = document.getElementById('paymentSuccessModal');
    if (paymentModalBackdrop && event.target === paymentModalBackdrop) {
        window.closePaymentSuccessModal();
    }
});
document.addEventListener('DOMContentLoaded', () => {
    const closeButton = document.getElementById(
        'registrationSuccessBannerClose'
    );

    if (!closeButton) {
        return;
    }

    closeButton.addEventListener('click', () => {
        const banner = document.getElementById(
            'registrationSuccessBanner'
        );

        if (banner) {
            banner.classList.add('is-hidden');
        }
    });
});

/* ==============================================================================
   14. CSP-COMPLIANT GLOBAL ACTION LISTENERS & PROFILE CONTROLS
   ============================================================================== */
function toggleEditMode(isEdit) {
    const viewMode = document.getElementById('profileViewMode');
    const editMode = document.getElementById('profileEditMode');
    if (!viewMode || !editMode) return;
    if (isEdit) {
        viewMode.style.display = 'none';
        editMode.style.display = 'block';
        const firstNameInput = document.getElementById('firstName');
        if (firstNameInput) firstNameInput.focus();
    } else {
        editMode.style.display = 'none';
        viewMode.style.display = 'block';
    }
}

function initProfileEdit() {
    const editBtn = document.getElementById('editProfileBtn');
    if (editBtn) {
        editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            toggleEditMode(true);
        });
    }
    const cancelBtn = document.getElementById('cancelEditProfileBtn') || document.querySelector('#profileEditMode button.order-btn-outline');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
            e.preventDefault();
            toggleEditMode(false);
        });
    }
}

function initGlobalActionListeners() {
    document.addEventListener('click', (e) => {
        // 1. Delivery / Pincode modal trigger
        const pincodeTrigger = e.target.closest('#headerDeliveryTrigger, .delivery-locator');
        if (pincodeTrigger) {
            if (typeof openPinCodeModal === 'function') {
                e.preventDefault();
                openPinCodeModal();
            }
            return;
        }

        // 2. Add to Cart button delegation
        const addCartBtn = e.target.closest('[data-action="add-cart"], .card-add-cart-btn');
        if (addCartBtn && !addCartBtn.disabled) {
            const pid = addCartBtn.dataset.productId;
            if (pid) {
                e.preventDefault();
                quickAddToCart(pid, 1, e);
                return;
            }
        }

        // 3. Buy Now button delegation
        const buyNowBtn = e.target.closest('[data-action="buy-now"], .card-buy-now-btn');
        if (buyNowBtn && !buyNowBtn.disabled) {
            const pid = buyNowBtn.dataset.productId;
            if (pid) {
                e.preventDefault();
                quickBuyNow(pid, 1, e);
                return;
            }
        }

        // 4. Wishlist button delegation
        const wishlistBtn = e.target.closest('[data-action="add-wishlist"]');
        if (wishlistBtn && !wishlistBtn.disabled) {
            const pid = wishlistBtn.dataset.productId;
            if (pid) {
                e.preventDefault();
                quickAddToWishlist(pid, e);
                return;
            }
        }

        // 5. Fallback for inline onclick handlers to ensure 100% functionality under strict CSP
        const inlineOnclickEl = e.target.closest('[onclick]');
        if (inlineOnclickEl) {
            const onclickAttr = inlineOnclickEl.getAttribute('onclick');
            if (onclickAttr) {
                const cartMatch = onclickAttr.match(/quickAddToCart\s*\(\s*['"]?(\d+)['"]?/);
                if (cartMatch && cartMatch[1]) {
                    e.preventDefault();
                    quickAddToCart(cartMatch[1], 1, e);
                    return;
                }
                const buyMatch = onclickAttr.match(/quickBuyNow\s*\(\s*['"]?(\d+)['"]?/);
                if (buyMatch && buyMatch[1]) {
                    e.preventDefault();
                    quickBuyNow(buyMatch[1], 1, e);
                    return;
                }
                const wishMatch = onclickAttr.match(/quickAddToWishlist\s*\(\s*['"]?(\d+)['"]?/);
                if (wishMatch && wishMatch[1]) {
                    e.preventDefault();
                    quickAddToWishlist(wishMatch[1], e);
                    return;
                }
                const editMatch = onclickAttr.match(/toggleEditMode\s*\(\s*(true|false)\s*\)/);
                if (editMatch) {
                    e.preventDefault();
                    toggleEditMode(editMatch[1] === 'true');
                    return;
                }
                if (onclickAttr.includes('openPinCodeModal')) {
                    e.preventDefault();
                    if (typeof openPinCodeModal === 'function') openPinCodeModal();
                    return;
                }
            }
        }
    });
}