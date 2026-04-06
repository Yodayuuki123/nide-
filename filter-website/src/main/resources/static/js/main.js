/**
 * Philitee Filter - Front-end JavaScript
 * Aesop-inspired interactions & animations
 */
(function() {
    'use strict';

    // ===== Header Scroll Effect (hide category nav on scroll, add shadow) =====
    var header = document.getElementById('siteHeader');
    if (header) {
        var lastScroll = 0;
        var ticking = false;

        window.addEventListener('scroll', function() {
            if (!ticking) {
                window.requestAnimationFrame(function() {
                    var currentScroll = window.pageYOffset;
                    if (currentScroll > 50) {
                        header.classList.add('scrolled');
                    } else {
                        header.classList.remove('scrolled');
                    }
                    lastScroll = currentScroll;
                    ticking = false;
                });
                ticking = true;
            }
        });
    }

    // ===== Announcement Bar Close =====
    var announcementClose = document.querySelector('.announcement-close');
    if (announcementClose) {
        announcementClose.addEventListener('click', function() {
            var bar = document.querySelector('.announcement-bar');
            if (bar) {
                bar.style.transition = 'max-height 0.3s ease, opacity 0.3s ease, padding 0.3s ease';
                bar.style.maxHeight = '0';
                bar.style.opacity = '0';
                bar.style.padding = '0';
                bar.style.overflow = 'hidden';
                setTimeout(function() { bar.remove(); }, 300);
            }
        });
    }

    // ===== Back to Top =====
    var backToTop = document.createElement('button');
    backToTop.className = 'back-to-top';
    backToTop.innerHTML = '<i class="bi bi-arrow-up"></i>';
    backToTop.setAttribute('aria-label', 'Back to top');
    document.body.appendChild(backToTop);

    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 400) {
            backToTop.classList.add('show');
        } else {
            backToTop.classList.remove('show');
        }
    });

    backToTop.addEventListener('click', function() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    // ===== Intersection Observer for Animations =====
    // Supports: .fade-in, .fade-in-left, .fade-in-right, .scale-in
    var animateElements = document.querySelectorAll('.fade-in, .fade-in-left, .fade-in-right, .scale-in');
    if (animateElements.length > 0) {
        var animObserver = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                    animObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.08,
            rootMargin: '0px 0px -50px 0px'
        });

        animateElements.forEach(function(el) {
            animObserver.observe(el);
        });
    }

    // ===== Counter Animation for Stats =====
    var statNumbers = document.querySelectorAll('.stat-number[data-count]');
    if (statNumbers.length > 0) {
        var counterObserver = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    counterObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.5 });

        statNumbers.forEach(function(el) {
            counterObserver.observe(el);
        });
    }

    function animateCounter(el) {
        var target = parseInt(el.getAttribute('data-count'));
        var suffix = el.textContent.replace(/[0-9,]/g, '');
        var duration = 2000;
        var start = 0;
        var startTime = null;

        function easeOutCubic(t) {
            return 1 - Math.pow(1 - t, 3);
        }

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            var progress = Math.min((timestamp - startTime) / duration, 1);
            var easedProgress = easeOutCubic(progress);
            var current = Math.floor(easedProgress * target);

            if (target >= 1000) {
                el.textContent = current.toLocaleString() + suffix;
            } else {
                el.textContent = current + suffix;
            }

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                if (target >= 1000) {
                    el.textContent = target.toLocaleString() + suffix;
                } else {
                    el.textContent = target + suffix;
                }
            }
        }

        requestAnimationFrame(step);
    }

    // ===== Parallax Effect for Images =====
    var parallaxImages = document.querySelectorAll('.parallax-image img');
    if (parallaxImages.length > 0 && window.innerWidth > 768) {
        var parallaxTicking = false;
        window.addEventListener('scroll', function() {
            if (!parallaxTicking) {
                window.requestAnimationFrame(function() {
                    parallaxImages.forEach(function(img) {
                        var rect = img.parentElement.getBoundingClientRect();
                        var windowHeight = window.innerHeight;
                        if (rect.top < windowHeight && rect.bottom > 0) {
                            var scrollPercent = (windowHeight - rect.top) / (windowHeight + rect.height);
                            var translateY = (scrollPercent - 0.5) * 40;
                            img.style.transform = 'scale(1.08) translateY(' + translateY + 'px)';
                        }
                    });
                    parallaxTicking = false;
                });
                parallaxTicking = true;
            }
        });
    }

    // ===== Product Carousel (Drag to scroll + button controls) =====
    var carousel = document.getElementById('productCarousel');
    var prevBtn = document.getElementById('carouselPrev');
    var nextBtn = document.getElementById('carouselNext');

    if (carousel) {
        var isDown = false;
        var startX;
        var scrollLeft;
        var cardWidth = 0;
        var currentIndex = 0;

        // Calculate card width
        function getCardWidth() {
            var firstCard = carousel.querySelector('.product-card');
            if (firstCard) {
                var style = window.getComputedStyle(carousel);
                var gap = parseInt(style.gap) || 24;
                cardWidth = firstCard.offsetWidth + gap;
            }
            return cardWidth;
        }

        // Get total visible cards
        function getVisibleCards() {
            var wrapperWidth = carousel.parentElement.offsetWidth;
            return Math.floor(wrapperWidth / getCardWidth()) || 1;
        }

        // Get total cards
        function getTotalCards() {
            return carousel.querySelectorAll('.product-card').length;
        }

        // Slide to index
        function slideTo(index) {
            var total = getTotalCards();
            var visible = getVisibleCards();
            var maxIndex = Math.max(0, total - visible);
            currentIndex = Math.max(0, Math.min(index, maxIndex));
            var offset = currentIndex * getCardWidth();
            carousel.style.transform = 'translateX(-' + offset + 'px)';
            updateButtons();
        }

        function updateButtons() {
            var total = getTotalCards();
            var visible = getVisibleCards();
            if (prevBtn) prevBtn.disabled = currentIndex <= 0;
            if (nextBtn) nextBtn.disabled = currentIndex >= total - visible;
        }

        if (prevBtn) {
            prevBtn.addEventListener('click', function() {
                slideTo(currentIndex - 1);
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', function() {
                slideTo(currentIndex + 1);
            });
        }

        // Mouse drag
        carousel.addEventListener('mousedown', function(e) {
            isDown = true;
            carousel.style.cursor = 'grabbing';
            startX = e.pageX;
            e.preventDefault();
        });

        document.addEventListener('mouseup', function() {
            if (isDown) {
                isDown = false;
                carousel.style.cursor = 'grab';
            }
        });

        document.addEventListener('mousemove', function(e) {
            if (!isDown) return;
            var x = e.pageX;
            var walk = startX - x;
            if (Math.abs(walk) > 50) {
                if (walk > 0) {
                    slideTo(currentIndex + 1);
                } else {
                    slideTo(currentIndex - 1);
                }
                isDown = false;
                carousel.style.cursor = 'grab';
            }
        });

        // Touch support
        var touchStartX = 0;
        carousel.addEventListener('touchstart', function(e) {
            touchStartX = e.touches[0].clientX;
        }, { passive: true });

        carousel.addEventListener('touchend', function(e) {
            var touchEndX = e.changedTouches[0].clientX;
            var diff = touchStartX - touchEndX;
            if (Math.abs(diff) > 40) {
                if (diff > 0) {
                    slideTo(currentIndex + 1);
                } else {
                    slideTo(currentIndex - 1);
                }
            }
        }, { passive: true });

        // Initialize
        updateButtons();

        // Recalculate on resize
        window.addEventListener('resize', function() {
            slideTo(currentIndex);
        });
    }

    // ===== Smooth Reveal for Hero Section =====
    var hero = document.getElementById('heroSection');
    if (hero) {
        // Add loaded class after a brief delay for smooth entrance
        setTimeout(function() {
            hero.classList.add('loaded');
        }, 100);
    }

    // ===== Auto-dismiss Flash Messages =====
    var flashMessages = document.querySelectorAll('.alert-message');
    flashMessages.forEach(function(msg) {
        setTimeout(function() {
            msg.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            msg.style.opacity = '0';
            msg.style.transform = 'translateY(-10px)';
            setTimeout(function() { msg.remove(); }, 400);
        }, 5000);
    });

    // ===== Smooth Image Loading =====
    var lazyImages = document.querySelectorAll('img[loading="lazy"]');
    lazyImages.forEach(function(img) {
        img.style.opacity = '0';
        img.style.transition = 'opacity 0.5s ease';
        if (img.complete) {
            img.style.opacity = '1';
        } else {
            img.addEventListener('load', function() {
                img.style.opacity = '1';
            });
            img.addEventListener('error', function() {
                img.style.opacity = '1';
            });
        }
    });

    // ===== Category Nav Active State =====
    var currentPath = window.location.pathname;
    var categoryLinks = document.querySelectorAll('.category-nav-list a');
    categoryLinks.forEach(function(link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

})();

// ===== Search Overlay =====
function openSearch() {
    var overlay = document.getElementById('searchOverlay');
    if (overlay) {
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
        setTimeout(function() {
            var input = overlay.querySelector('input');
            if (input) input.focus();
        }, 400);
    }
}

function closeSearch(event) {
    var overlay = document.getElementById('searchOverlay');
    if (overlay) {
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    }
}

// ===== Search Results Modal =====
function doSearchModal(form) {
    var input = form.querySelector('input[name="keyword"]');
    var keyword = input ? input.value.trim() : '';
    if (!keyword) return false;

    // Close search overlay if open
    closeSearch();
    closeMobileMenu();

    // Open results modal
    var overlay = document.getElementById('searchResultsOverlay');
    var body = document.getElementById('searchResultsBody');
    if (!overlay || !body) {
        window.location.href = '/search?keyword=' + encodeURIComponent(keyword);
        return false;
    }

    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
    body.innerHTML = '<div class="search-results-loading"><i class="bi bi-arrow-repeat spin"></i> Searching...</div>';

    fetch('/api/search?keyword=' + encodeURIComponent(keyword))
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            var html = '<div class="search-results-info">Found <strong>' + data.total + '</strong> results for "' + escapeHtml(data.keyword) + '"</div>';
            if (data.products && data.products.length > 0) {
                html += '<div class="search-results-grid">';
                data.products.forEach(function(p) {
                    html += '<a href="/products/' + p.slug + '" class="search-result-item">';
                    if (p.mainImage) {
                        html += '<div class="search-result-img"><img src="' + p.mainImage + '" alt="' + escapeHtml(p.name) + '" loading="lazy"></div>';
                    } else {
                        html += '<div class="search-result-img"><div class="search-result-placeholder"><i class="bi bi-image"></i></div></div>';
                    }
                    html += '<div class="search-result-info">';
                    if (p.category) html += '<span class="search-result-cat">' + escapeHtml(p.category) + '</span>';
                    html += '<h6>' + escapeHtml(p.name) + '</h6>';
                    if (p.sku) html += '<span class="search-result-sku">SKU: ' + escapeHtml(p.sku) + '</span>';
                    html += '</div></a>';
                });
                html += '</div>';
            } else {
                html += '<div class="search-results-empty"><i class="bi bi-search"></i><p>No products found. Try different keywords.</p></div>';
            }
            html += '<div class="search-results-footer"><a href="/search?keyword=' + encodeURIComponent(data.keyword) + '" class="bf-btn bf-btn-outline">View Full Results Page</a></div>';
            body.innerHTML = html;
        })
        .catch(function() {
            body.innerHTML = '<div class="search-results-empty"><p>Search failed. Please try again.</p></div>';
        });

    return false;
}

function closeSearchResults(event) {
    if (event && event.target !== event.currentTarget) return;
    var overlay = document.getElementById('searchResultsOverlay');
    if (overlay) {
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// Close search when clicking overlay background
document.addEventListener('click', function(e) {
    var overlay = document.getElementById('searchOverlay');
    if (overlay && overlay.classList.contains('active')) {
        if (e.target === overlay) {
            closeSearch();
        }
    }
});

// Close overlays with Escape key
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeSearch();
        closeSearchResults();
        closeMobileMenu();
    }
});

// ===== Mobile Menu =====
function openMobileMenu() {
    var menu = document.getElementById('mobileMenu');
    var overlay = document.getElementById('mobileOverlay');
    if (menu) menu.classList.add('active');
    if (overlay) overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeMobileMenu() {
    var menu = document.getElementById('mobileMenu');
    var overlay = document.getElementById('mobileOverlay');
    if (menu) menu.classList.remove('active');
    if (overlay) overlay.classList.remove('active');
    document.body.style.overflow = '';
}

function toggleMobileSubmenu(el) {
    var submenu = el.nextElementSibling;
    if (submenu) {
        submenu.classList.toggle('open');
        // Rotate arrow icon
        var arrow = el.querySelector('.bi-chevron-down');
        if (arrow) {
            arrow.style.transform = submenu.classList.contains('open') ? 'rotate(180deg)' : '';
        }
    }
}

// ===== Product Gallery (Detail page) =====
function switchGalleryImage(thumbEl) {
    var mainImg = document.getElementById('galleryMainImage');
    if (mainImg && thumbEl.dataset.img) {
        // Fade transition
        mainImg.style.opacity = '0';
        setTimeout(function() {
            mainImg.src = thumbEl.dataset.img;
            mainImg.style.opacity = '1';
        }, 200);

        // Update active state
        var allThumbs = document.querySelectorAll('.gallery-thumb');
        allThumbs.forEach(function(t) { t.classList.remove('active'); });
        thumbEl.classList.add('active');
    }
}

// ===== Image Zoom on Product Detail =====
(function() {
    var galleryMain = document.getElementById('galleryMainContainer');
    if (galleryMain && window.innerWidth > 768) {
        var mainImg = galleryMain.querySelector('img');
        if (mainImg) {
            galleryMain.addEventListener('mousemove', function(e) {
                var rect = galleryMain.getBoundingClientRect();
                var x = ((e.clientX - rect.left) / rect.width) * 100;
                var y = ((e.clientY - rect.top) / rect.height) * 100;
                mainImg.style.transformOrigin = x + '% ' + y + '%';
                mainImg.style.transform = 'scale(1.5)';
            });

            galleryMain.addEventListener('mouseleave', function() {
                mainImg.style.transformOrigin = 'center center';
                mainImg.style.transform = 'scale(1)';
            });
        }
    }
})();

// ===== Mega Menu Hover (Aesop-style dropdown) =====
(function() {
    var categoryNavItems = document.querySelectorAll('.has-dropdown');
    categoryNavItems.forEach(function(item) {
        var megaMenu = item.querySelector('.mega-menu');
        if (!megaMenu) return;

        var showTimeout, hideTimeout;

        item.addEventListener('mouseenter', function() {
            clearTimeout(hideTimeout);
            showTimeout = setTimeout(function() {
                megaMenu.classList.add('active');
            }, 150);
        });

        item.addEventListener('mouseleave', function() {
            clearTimeout(showTimeout);
            hideTimeout = setTimeout(function() {
                megaMenu.classList.remove('active');
            }, 200);
        });
    });
})();

// ===== Smooth Page Transition =====
(function() {
    // Add loaded class to body for entrance animation
    document.body.classList.add('page-loaded');

    // Smooth link transitions (internal links only)
    var internalLinks = document.querySelectorAll('a[href^="/"]:not([target="_blank"]):not(.maincategory-item):not(.maincategory-sub-link):not(.maincategory-sub-view-all)');
    internalLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            var href = link.getAttribute('href');
            if (href && href !== '#' && !href.startsWith('javascript')) {
                e.preventDefault();
                document.body.style.opacity = '0';
                document.body.style.transition = 'opacity 0.25s ease';
                setTimeout(function() {
                    window.location.href = href;
                }, 250);
            }
        });
    });
})();

// ===== Smooth Scroll for Anchor Links =====
(function() {
    var anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            var targetId = link.getAttribute('href').substring(1);
            var target = document.getElementById(targetId);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });
})();

// ===== Cursor Trail Effect (subtle, Aesop-like) =====
(function() {
    if (window.innerWidth <= 768) return;

    var cursor = document.createElement('div');
    cursor.style.cssText = 'position:fixed;width:8px;height:8px;border-radius:50%;background:rgba(37,36,34,0.15);pointer-events:none;z-index:9999;transition:transform 0.15s ease,opacity 0.3s ease;opacity:0;';
    document.body.appendChild(cursor);

    document.addEventListener('mousemove', function(e) {
        cursor.style.left = e.clientX - 4 + 'px';
        cursor.style.top = e.clientY - 4 + 'px';
        cursor.style.opacity = '1';
    });

    document.addEventListener('mouseleave', function() {
        cursor.style.opacity = '0';
    });

    // Scale up on hoverable elements
    var hoverables = document.querySelectorAll('a, button, .product-card, .category-card, .gallery-item');
    hoverables.forEach(function(el) {
        el.addEventListener('mouseenter', function() {
            cursor.style.transform = 'scale(3)';
            cursor.style.background = 'rgba(37,36,34,0.08)';
        });
        el.addEventListener('mouseleave', function() {
            cursor.style.transform = 'scale(1)';
            cursor.style.background = 'rgba(37,36,34,0.15)';
        });
    });
})();

/* ===== Category Carousel Logic ===== */
(function () {
    var pages = document.querySelectorAll('.cat-carousel-page');
    var dots  = document.querySelectorAll('.cat-dot');
    var prevBtn = document.getElementById('catPrev');
    var nextBtn = document.getElementById('catNext');
    if (!pages.length || !prevBtn || !nextBtn) return;

    var current = 0;
    var total   = pages.length;
    var animating = false;

    function goTo(idx, direction) {
        if (animating || idx === current) return;
        animating = true;

        var outClass = direction === 'next' ? 'slide-out-left'  : 'slide-out-right';
        var inClass  = direction === 'next' ? 'slide-in-right'  : 'slide-in-left';

        var outPage = pages[current];
        var inPage  = pages[idx];

        outPage.classList.remove('active');
        outPage.classList.add(outClass);

        inPage.style.display = 'grid';
        inPage.classList.add(inClass);

        setTimeout(function () {
            outPage.classList.remove(outClass);
            outPage.style.display = '';
            inPage.classList.remove(inClass);
            inPage.classList.add('active');
            inPage.style.display = '';

            dots[current].classList.remove('active');
            dots[idx].classList.add('active');

            current = idx;
            updateButtons();
            animating = false;
        }, 380);
    }

    function updateButtons() {
        prevBtn.disabled = (current === 0);
        nextBtn.disabled = (current === total - 1);
    }

    prevBtn.addEventListener('click', function () {
        if (current > 0) goTo(current - 1, 'prev');
    });

    nextBtn.addEventListener('click', function () {
        if (current < total - 1) goTo(current + 1, 'next');
    });

    dots.forEach(function (dot) {
        dot.addEventListener('click', function () {
            var idx = parseInt(this.getAttribute('data-page'), 10);
            goTo(idx, idx > current ? 'next' : 'prev');
        });
    });

    var touchStartX = 0;
    var viewport = document.getElementById('catCarouselViewport');
    if (viewport) {
        viewport.addEventListener('touchstart', function (e) {
            touchStartX = e.changedTouches[0].clientX;
        }, { passive: true });
        viewport.addEventListener('touchend', function (e) {
            var dx = e.changedTouches[0].clientX - touchStartX;
            if (Math.abs(dx) > 50) {
                if (dx < 0 && current < total - 1) goTo(current + 1, 'next');
                if (dx > 0 && current > 0)         goTo(current - 1, 'prev');
            }
        }, { passive: true });
    }

    updateButtons();
})();

// ===== BFCache Fix: 解决从详情页返回时出现空白页的问题 =====
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        window.location.reload();
    }
});
