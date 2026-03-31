/**
 * Philitee Filter - Admin Panel JavaScript
 */
(function() {
    'use strict';

    // ===== TinyMCE Initialization (generic - for settings page richtext editors) =====
    // Product form does its own enhanced TinyMCE init, so skip if already initialized
    if (typeof tinymce !== 'undefined') {
        var richEditors = document.querySelectorAll('.richtext-editor');
        if (richEditors.length > 0) {
            tinymce.init({
                selector: '.richtext-editor',
                height: 350,
                menubar: false,
                plugins: 'lists link image table code fullscreen preview',
                toolbar: 'undo redo | blocks | bold italic underline | alignleft aligncenter alignright | bullist numlist | link image table | code fullscreen',
                content_style: 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; font-size: 14px; }',
                images_upload_handler: function(blobInfo) {
                    return new Promise(function(resolve, reject) {
                        var formData = new FormData();
                        formData.append('file', blobInfo.blob(), blobInfo.filename());

                        fetch('/admin/api/images/upload', {
                            method: 'POST',
                            body: formData
                        })
                        .then(function(resp) { return resp.json(); })
                        .then(function(data) {
                            if (data.success) {
                                resolve(data.url);
                            } else {
                                reject('Upload failed: ' + (data.message || 'Unknown error'));
                            }
                        })
                        .catch(function(err) {
                            reject('Upload failed: ' + err.message);
                        });
                    });
                },
                promotion: false,
                branding: false
            });
        }
    }

    // ===== Slug Auto-Generation =====
    var nameInput = document.getElementById('productName');
    var slugInput = document.getElementById('productSlug');

    if (nameInput && slugInput) {
        nameInput.addEventListener('blur', function() {
            if (!slugInput.value || slugInput.value.trim() === '') {
                slugInput.value = generateSlug(nameInput.value);
            }
        });
    }

    // Also for category forms
    var catNameInput = document.getElementById('categoryName');
    var catSlugInput = document.getElementById('categorySlug');
    if (catNameInput && catSlugInput) {
        catNameInput.addEventListener('blur', function() {
            if (!catSlugInput.value || catSlugInput.value.trim() === '') {
                catSlugInput.value = generateSlug(catNameInput.value);
            }
        });
    }

    function generateSlug(text) {
        return text
            .toLowerCase()
            .trim()
            .replace(/[^\w\s-]/g, '')
            .replace(/[\s_]+/g, '-')
            .replace(/-+/g, '-')
            .replace(/^-+|-+$/g, '');
    }

    // ===== Image Picker (Enhanced with Search and Pagination) =====
    var imagePickerMode = 'main'; // 'main' or 'gallery'
    var imagePickerPage = 1;
    var imagePickerKeyword = '';
    var selectedImageId = null;
    var selectedImageUrl = null;

    window.openImagePicker = function(mode) {
        imagePickerMode = mode;
        imagePickerPage = 1;
        imagePickerKeyword = '';
        selectedImageId = null;
        selectedImageUrl = null;
        
        var confirmBtn = document.getElementById('confirmImageSelect');
        if (confirmBtn) confirmBtn.disabled = true;
        
        // Add search input to modal if not exists
        setupImagePickerSearch();
        
        loadImages(true);
        var modalEl = document.getElementById('imagePickerModal');
        if (modalEl) {
            var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    };

    function setupImagePickerSearch() {
        var libraryTab = document.getElementById('tabLibrary');
        if (!libraryTab || libraryTab.querySelector('.picker-search-container')) return;

        var searchHtml = '<div class="picker-search-container mb-3 d-flex gap-2">' +
            '<input type="text" id="pickerSearchInput" class="form-control form-control-sm" placeholder="Search image name...">' +
            '<button type="button" id="pickerSearchBtn" class="btn btn-primary btn-sm">Search</button>' +
            '</div>';
        libraryTab.insertAdjacentHTML('afterbegin', searchHtml);

        document.getElementById('pickerSearchBtn').addEventListener('click', function() {
            imagePickerKeyword = document.getElementById('pickerSearchInput').value;
            loadImages(true);
        });

        document.getElementById('pickerSearchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                imagePickerKeyword = this.value;
                loadImages(true);
            }
        });
    }

    function loadImages(reset) {
        if (reset) {
            imagePickerPage = 1;
        }

        var url = '/admin/api/images/list?page=' + imagePickerPage + '&size=20';
        if (imagePickerKeyword) {
            url += '&keyword=' + encodeURIComponent(imagePickerKeyword);
        }

        fetch(url)
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                var grid = document.getElementById('imagePickerGrid');
                if (reset) {
                    grid.innerHTML = '';
                }

                if (data.records && data.records.length > 0) {
                    data.records.forEach(function(img) {
                        var div = document.createElement('div');
                        div.className = 'image-picker-item';
                        div.setAttribute('data-id', img.id);
                        var finalUrl = img.url || ('/wp-content/uploads' + img.filePath);
                        div.setAttribute('data-url', finalUrl);
                        div.innerHTML = '<img src="' + finalUrl + '" alt="' + (img.altText || img.fileName) + '">';
                        div.addEventListener('click', function() {
                            grid.querySelectorAll('.image-picker-item').forEach(function(item) {
                                item.classList.remove('selected');
                            });
                            div.classList.add('selected');
                            selectedImageId = img.id;
                            selectedImageUrl = finalUrl;
                            var confirmBtn = document.getElementById('confirmImageSelect');
                            if (confirmBtn) confirmBtn.disabled = false;
                        });
                        grid.appendChild(div);
                    });

                    var loadMoreBtn = document.getElementById('loadMoreImages');
                    if (loadMoreBtn) {
                        loadMoreBtn.style.display = (data.current < data.pages) ? 'inline-block' : 'none';
                    }
                } else if (reset) {
                    grid.innerHTML = '<p class="text-muted text-center py-4">No images found.</p>';
                    var loadMoreBtn = document.getElementById('loadMoreImages');
                    if (loadMoreBtn) loadMoreBtn.style.display = 'none';
                }
            })
            .catch(function(err) {
                console.error('Failed to load images:', err);
            });
    }

    // Load More button
    var loadMoreBtn = document.getElementById('loadMoreImages');
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            imagePickerPage++;
            loadImages(false);
        });
    }

    // Confirm Image Selection
    var confirmBtn = document.getElementById('confirmImageSelect');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            if (!selectedImageId) return;

            if (imagePickerMode === 'main') {
                var mainIdInput = document.getElementById('mainImageId');
                if (mainIdInput) mainIdInput.value = selectedImageId;
                var preview = document.getElementById('mainImagePreview');
                if (preview) preview.innerHTML = '<img src="' + selectedImageUrl + '" class="img-fluid rounded" style="max-height: 200px;">';
            } else if (imagePickerMode === 'gallery') {
                var container = document.getElementById('galleryImagesContainer');
                if (container) {
                    var existing = container.querySelector('[data-id="' + selectedImageId + '"]');
                    if (!existing) {
                        var div = document.createElement('div');
                        div.className = 'gallery-thumb-item position-relative';
                        div.setAttribute('data-id', selectedImageId);
                        div.innerHTML = '<img src="' + selectedImageUrl + '" class="rounded" style="width: 80px; height: 80px; object-fit: cover;">' +
                            '<input type="hidden" name="imageIds" value="' + selectedImageId + '">' +
                            '<button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0" style="padding: 0 4px; font-size: 0.7rem; line-height: 1.4;" onclick="removeGalleryImage(this)"><i class="bi bi-x"></i></button>';
                        container.appendChild(div);
                    }
                }
            }

            var modalEl = document.getElementById('imagePickerModal');
            if (modalEl) {
                var modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
            }
        });
    }

    // Remove gallery image
    window.removeGalleryImage = function(btn) {
        btn.closest('.gallery-thumb-item').remove();
    };

    // ===== Modal Upload =====
    var modalUploadZone = document.getElementById('modalUploadZone');
    var modalFileInput = document.getElementById('modalFileInput');

    if (modalUploadZone && modalFileInput) {
        modalUploadZone.addEventListener('click', function() {
            modalFileInput.click();
        });

        modalUploadZone.addEventListener('dragover', function(e) {
            e.preventDefault();
            modalUploadZone.classList.add('dragover');
        });

        modalUploadZone.addEventListener('dragleave', function() {
            modalUploadZone.classList.remove('dragover');
        });

        modalUploadZone.addEventListener('drop', function(e) {
            e.preventDefault();
            modalUploadZone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) {
                uploadFile(e.dataTransfer.files[0]);
            }
        });

        modalFileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                uploadFile(this.files[0]);
            }
        });
    }

    function uploadFile(file) {
        var formData = new FormData();
        formData.append('file', file);

        var progressDiv = document.getElementById('uploadProgress');
        var progressBar = progressDiv ? progressDiv.querySelector('.progress-bar') : null;
        if (progressDiv) progressDiv.style.display = 'block';
        if (progressBar) progressBar.style.width = '50%';

        fetch('/admin/api/images/upload', {
            method: 'POST',
            body: formData
        })
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            if (progressBar) progressBar.style.width = '100%';
            setTimeout(function() {
                if (progressDiv) progressDiv.style.display = 'none';
                if (progressBar) progressBar.style.width = '0%';
            }, 500);

            if (data.success) {
                selectedImageId = data.image.id;
                selectedImageUrl = data.url;
                var confirmBtn = document.getElementById('confirmImageSelect');
                if (confirmBtn) confirmBtn.disabled = false;

                loadImages(true);
                var libraryTabLink = document.querySelector('a[href="#tabLibrary"]');
                if (libraryTabLink) {
                    bootstrap.Tab.getOrCreateInstance(libraryTabLink).show();
                }
            } else {
                alert('Upload failed: ' + (data.message || 'Unknown error'));
            }
        })
        .catch(function(err) {
            if (progressDiv) progressDiv.style.display = 'none';
            alert('Upload failed: ' + err.message);
        });
    }

    // ===== Delete Image (AJAX) - Simplified for use with confirmation modals if needed =====
    window.deleteImage = function(id, btn) {
        if (!confirm('Are you sure you want to delete this image?')) return;

        fetch('/admin/api/images/delete/' + id, { method: 'POST' })
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                if (data.success) {
                    var item = btn.closest('.image-grid-item');
                    if (item) item.remove();
                    else window.location.reload();
                } else {
                    alert('Delete failed: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(function(err) {
                alert('Delete failed: ' + err.message);
            });
    };

    // ===== Auto-dismiss Alerts =====
    document.querySelectorAll('.alert-dismissible').forEach(function(alert) {
        setTimeout(function() {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

})();
