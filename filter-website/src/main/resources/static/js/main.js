/**
 * Philitee Filter - Front-end JavaScript
 */
(function() {
    'use strict';

    // ===== Back to Top Button =====
    const backToTopBtn = document.querySelector('.back-to-top');
    if (backToTopBtn) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                backToTopBtn.classList.add('show');
            } else {
                backToTopBtn.classList.remove('show');
            }
        });

        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // ===== Product Image Gallery =====
    const galleryMain = document.getElementById('galleryMainImage');
    const galleryThumbs = document.querySelectorAll('.product-gallery-thumb');

    if (galleryMain && galleryThumbs.length > 0) {
        galleryThumbs.forEach(function(thumb) {
            thumb.addEventListener('click', function() {
                // Update main image
                var imgUrl = this.getAttribute('data-img');
                galleryMain.src = imgUrl;

                // Update active state
                galleryThumbs.forEach(function(t) { t.classList.remove('active'); });
                this.classList.add('active');
            });
        });
    }

    // ===== Smooth Scroll for Anchor Links =====
    document.querySelectorAll('a[href^="#"]').forEach(function(anchor) {
        anchor.addEventListener('click', function(e) {
            var target = document.querySelector(this.getAttribute('href'));
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });

    // ===== Auto-dismiss Flash Messages =====
    document.querySelectorAll('.flash-message .alert').forEach(function(alert) {
        setTimeout(function() {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // ===== Navbar Scroll Effect =====
    var navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('navbar-scrolled');
            } else {
                navbar.classList.remove('navbar-scrolled');
            }
        });
    }

    // ===== Lazy Load Images =====
    if ('IntersectionObserver' in window) {
        var lazyImages = document.querySelectorAll('img[loading="lazy"]');
        var imageObserver = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    var img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                    }
                    imageObserver.unobserve(img);
                }
            });
        });

        lazyImages.forEach(function(img) {
            imageObserver.observe(img);
        });
    }

})();
