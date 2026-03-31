/**
 * Philitee Filter - Admin Panel JavaScript
 * Enhanced: Image picker with search/pagination, toast notifications, delete confirmations
 */
(function() {
    'use strict';

    // ===== Global Toast Notification System (B-17) =====
    window.showToast = function(message, type) {
        type = type || 'success';
        var toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 99999; display: flex; flex-direction: column; gap: 8px;';
            document.body.appendChild(toastContainer);
        }

        var iconMap = {
            success: '<i class="bi bi-check-circle-fill text-success me-2"></i>',
            error: '<i class="bi bi-x-circle-fill text-danger me-2"></i>',
            warning: '<i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>',
            info: '<i class="bi bi-info-circle-fill text-primary me-2"></i>'
        };

        var toast = document.createElement('div');
        toast.className = 'toast-notification';
        toast.style.cssText = 'background: #fff; border: 1px solid #e2e8f0; border-radius: 4px; padding: 12px 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.12); display: flex; align-items: center; font-size: 14px; min-width: 280px; max-width: 420px; animation: slideInRight 0.3s ease;';
        toast.innerHTML = (iconMap[type] || iconMap.info) + '<span>' + message + '</span>';

        toastContainer.appendChild(toast);

        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(20px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(function() { toast.remove(); }, 300);
        }, 3000);
    };

    // Add toast animation CSS
    if (!document.getElementById('toastAnimStyle')) {
        var style = document.createElement('style');
        style.id = 'toastAnimStyle';
        style.textContent = '@keyframes slideInRight { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: translateX(0); } }';
        document.head.appendChild(style);
    }

    // ===== Global Delete Confirmation (B-18) =====
    window.confirmDelete = function(message, callback) {
        // Create modal if not exists
        var modalEl = document.getElementById('deleteConfirmModal');
        if (!modalEl) {
            modalEl = document.createElement('div');
            modalEl.id = 'deleteConfirmModal';
            modalEl.className = 'modal fade';
            modalEl.tabIndex = -1;
            modalEl.innerHTML = '<div class="modal-dialog modal-sm modal-dialog-centered">' +
                '<div class="modal-content">' +
                '<div class="modal-header border-0 pb-0">' +
                '<h6 class="modal-title"><i class="bi bi-exclamation-triangle text-warning me-2"></i>Confirm Delete</h6>' +
                '<button type="button" class="btn-close" data-bs-dismiss="modal"></button>' +
                '</div>' +
                '<div class="modal-body" id="deleteConfirmMessage"></div>' +
                '<div class="modal-footer border-0 pt-0">' +
                '<button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>' +
                '<button type="button" class="btn btn-sm btn-danger" id="deleteConfirmBtn">Delete</button>' +
                '</div></div></div>';
            document.body.appendChild(modalEl);
        }

        document.getElementById('deleteConfirmMessage').textContent = message || 'Are you sure you want to delete this item?';
        var modal = bootstrap.Modal.getOrCreateInstance(modalEl);

        var confirmBtn = document.getElementById('deleteConfirmBtn');
        // Remove old listeners by replacing node
        var newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
        newBtn.addEventListener('click', function() {
            modal.hide();
            if (typeof callback === 'function') callback();
        });

        modal.show();
    };

    // ===== TinyMCE Initialization (generic - for settings page richtext editors) =====
    if (typeof tinymce !== 'undefined') {
        var richEditors = document.querySelectorAll('.richtext-editor');
        if (richEditors.length > 0) {
            tinymce.init({
                selector: '.richtext-editor',
                height: 350,
                menubar: false,
                plugins: 'lists link image table code fullscreen preview',
                toolbar: 'undo redo | blocks | bold italic underline | alignleft aligncenter alignright | bullist numlist | link image table | code fullscreen',
                content_style: 'body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; font-size: 14px; } img { max-width: 100%; height: auto; }',
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
                convert_urls: false,
                relative_urls: false,
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

    // ===== Image Picker (Enhanced with Search and Pagination - B-15) =====
    var imagePickerMode = 'main';
    var imagePickerPage = 1;
    var imagePickerTotalPages = 1;
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

        var searchInput = document.getElementById('pickerSearchInput');
        if (searchInput) searchInput.value = '';

        loadPickerImages();
        var modalEl = document.getElementById('imagePickerModal');
        if (modalEl) {
            var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    };

    // Bind search button
    var pickerSearchBtn = document.getElementById('pickerSearchBtn');
    if (pickerSearchBtn) {
        pickerSearchBtn.addEventListener('click', function() {
            var input = document.getElementById('pickerSearchInput');
            imagePickerKeyword = input ? input.value : '';
            imagePickerPage = 1;
            loadPickerImages();
        });
    }

    var pickerSearchInput = document.getElementById('pickerSearchInput');
    if (pickerSearchInput) {
        pickerSearchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                imagePickerKeyword = this.value;
                imagePickerPage = 1;
                loadPickerImages();
            }
        });
    }

    // Bind pagination buttons
    var pickerPrevBtn = document.getElementById('pickerPrevPage');
    if (pickerPrevBtn) {
        pickerPrevBtn.addEventListener('click', function() {
            if (imagePickerPage > 1) {
                imagePickerPage--;
                loadPickerImages();
            }
        });
    }

    var pickerNextBtn = document.getElementById('pickerNextPage');
    if (pickerNextBtn) {
        pickerNextBtn.addEventListener('click', function() {
            if (imagePickerPage < imagePickerTotalPages) {
                imagePickerPage++;
                loadPickerImages();
            }
        });
    }

    function loadPickerImages() {
        var url = '/admin/api/images/list?page=' + imagePickerPage + '&size=20';
        if (imagePickerKeyword) {
            url += '&keyword=' + encodeURIComponent(imagePickerKeyword);
        }

        var grid = document.getElementById('imagePickerGrid');
        if (grid) grid.innerHTML = '<p class="text-muted text-center py-4">Loading...</p>';

        fetch(url)
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                if (!grid) return;
                grid.innerHTML = '';

                var records = data.records || data.content || [];
                var totalPages = data.pages || data.totalPages || 1;
                var currentPage = data.current || data.number || 1;
                var totalRecords = data.total || data.totalElements || 0;

                imagePickerTotalPages = totalPages;

                if (records.length > 0) {
                    records.forEach(function(img) {
                        var div = document.createElement('div');
                        div.className = 'image-picker-item';
                        div.setAttribute('data-id', img.id);
                        var finalUrl = img.url || ('/wp-content/uploads' + (img.filePath || ''));
                        div.setAttribute('data-url', finalUrl);
                        div.innerHTML = '<img src="' + finalUrl + '" alt="' + (img.altText || img.fileName || '') + '" onerror="this.src=\'/images/placeholder.png\'">';
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
                } else {
                    grid.innerHTML = '<p class="text-muted text-center py-4">No images found.</p>';
                }

                // Update pagination info
                var pageInfo = document.getElementById('pickerPageInfo');
                if (pageInfo) {
                    pageInfo.textContent = 'Page ' + currentPage + ' of ' + totalPages + ' (' + totalRecords + ' images)';
                }

                var prevBtn = document.getElementById('pickerPrevPage');
                var nextBtn = document.getElementById('pickerNextPage');
                if (prevBtn) prevBtn.disabled = (currentPage <= 1);
                if (nextBtn) nextBtn.disabled = (currentPage >= totalPages);
            })
            .catch(function(err) {
                console.error('Failed to load images:', err);
                if (grid) grid.innerHTML = '<p class="text-danger text-center py-4">Failed to load images.</p>';
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

            showToast('Image selected successfully', 'success');
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
                uploadModalFile(e.dataTransfer.files[0]);
            }
        });

        modalFileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                uploadModalFile(this.files[0]);
            }
        });
    }

    function uploadModalFile(file) {
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
                selectedImageId = data.image ? data.image.id : null;
                selectedImageUrl = data.url;
                var confirmBtn = document.getElementById('confirmImageSelect');
                if (confirmBtn && selectedImageId) confirmBtn.disabled = false;

                showToast('Image uploaded successfully', 'success');

                // Refresh library and switch to library tab
                loadPickerImages();
                var libraryTabLink = document.querySelector('a[href="#tabLibrary"]');
                if (libraryTabLink) {
                    bootstrap.Tab.getOrCreateInstance(libraryTabLink).show();
                }
            } else {
                showToast('Upload failed: ' + (data.message || 'Unknown error'), 'error');
            }
        })
        .catch(function(err) {
            if (progressDiv) progressDiv.style.display = 'none';
            showToast('Upload failed: ' + err.message, 'error');
        });
    }

    // ===== Delete Image (AJAX) =====
    window.deleteImage = function(id, btn) {
        confirmDelete('Are you sure you want to delete this image? This action cannot be undone.', function() {
            fetch('/admin/api/images/delete/' + id, { method: 'POST' })
                .then(function(resp) { return resp.json(); })
                .then(function(data) {
                    if (data.success) {
                        var item = btn.closest('.image-grid-item');
                        if (item) item.remove();
                        else window.location.reload();
                        showToast('Image deleted successfully', 'success');
                    } else {
                        showToast('Delete failed: ' + (data.message || 'Unknown error'), 'error');
                    }
                })
                .catch(function(err) {
                    showToast('Delete failed: ' + err.message, 'error');
                });
        });
    };

    // ===== Auto-dismiss Alerts =====
    document.querySelectorAll('.alert-dismissible').forEach(function(alert) {
        setTimeout(function() {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // ===== Show toast for URL params (success/error messages from server redirects) =====
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('success')) {
        showToast(decodeURIComponent(urlParams.get('success')) || 'Operation successful', 'success');
    }
    if (urlParams.has('error')) {
        showToast(decodeURIComponent(urlParams.get('error')) || 'Operation failed', 'error');
    }

})();
