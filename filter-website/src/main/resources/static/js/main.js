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
    var statNumbers = document.querySelectorAll('.global-stat-number[data-count], .stat-number[data-count]');
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
    var internalLinks = document.querySelectorAll('a[href^="/"]:not([target="_blank"])');
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

