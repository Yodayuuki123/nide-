/**
 * Philitee Filter - Admin Panel JavaScript
 */
(function() {
    'use strict';

    // ===== TinyMCE Initialization =====
    if (typeof tinymce !== 'undefined') {
        tinymce.init({
            selector: '#shortDescription, #description',
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

    // ===== Image Picker =====
    var imagePickerMode = 'main'; // 'main' or 'gallery'
    var imagePickerPage = 1;
    var selectedImageId = null;
    var selectedImageUrl = null;

    window.openImagePicker = function(mode) {
        imagePickerMode = mode;
        imagePickerPage = 1;
        selectedImageId = null;
        selectedImageUrl = null;
        document.getElementById('confirmImageSelect').disabled = true;
        loadImages(true);
        var modal = new bootstrap.Modal(document.getElementById('imagePickerModal'));
        modal.show();
    };

    function loadImages(reset) {
        if (reset) {
            imagePickerPage = 1;
        }

        fetch('/admin/api/images/list?page=' + imagePickerPage + '&size=20')
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
                        div.setAttribute('data-url', img.url || ('/wp-content/uploads' + img.filePath));
                        div.innerHTML = '<img src="' + (img.url || ('/wp-content/uploads' + img.filePath)) + '" alt="' + (img.altText || img.fileName) + '">';
                        div.addEventListener('click', function() {
                            // Deselect all
                            grid.querySelectorAll('.image-picker-item').forEach(function(item) {
                                item.classList.remove('selected');
                            });
                            // Select this one
                            div.classList.add('selected');
                            selectedImageId = img.id;
                            selectedImageUrl = img.url || ('/wp-content/uploads' + img.filePath);
                            document.getElementById('confirmImageSelect').disabled = false;
                        });
                        grid.appendChild(div);
                    });

                    // Show/hide load more button
                    var loadMoreBtn = document.getElementById('loadMoreImages');
                    if (loadMoreBtn) {
                        loadMoreBtn.style.display = (data.current < data.pages) ? 'inline-block' : 'none';
                    }
                } else if (reset) {
                    grid.innerHTML = '<p class="text-muted text-center py-4">No images found.</p>';
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
                // Set main image
                document.getElementById('mainImageId').value = selectedImageId;
                var preview = document.getElementById('mainImagePreview');
                preview.innerHTML = '<img src="' + selectedImageUrl + '" class="img-fluid rounded" style="max-height: 200px;">';
            } else if (imagePickerMode === 'gallery') {
                // Add to gallery
                var container = document.getElementById('galleryImagesContainer');
                // Check if already exists
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

            bootstrap.Modal.getInstance(document.getElementById('imagePickerModal')).hide();
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
                // Select the newly uploaded image
                selectedImageId = data.image.id;
                selectedImageUrl = data.url;
                document.getElementById('confirmImageSelect').disabled = false;

                // Reload image grid and switch to library tab
                loadImages(true);
                var libraryTab = document.querySelector('a[href="#tabLibrary"]');
                if (libraryTab) {
                    bootstrap.Tab.getOrCreateInstance(libraryTab).show();
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

    // ===== Image List Page: Upload Zone =====
    var pageUploadZone = document.getElementById('pageUploadZone');
    var pageFileInput = document.getElementById('pageFileInput');

    if (pageUploadZone && pageFileInput) {
        pageUploadZone.addEventListener('click', function() {
            pageFileInput.click();
        });

        pageUploadZone.addEventListener('dragover', function(e) {
            e.preventDefault();
            pageUploadZone.classList.add('dragover');
        });

        pageUploadZone.addEventListener('dragleave', function() {
            pageUploadZone.classList.remove('dragover');
        });

        pageUploadZone.addEventListener('drop', function(e) {
            e.preventDefault();
            pageUploadZone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) {
                uploadPageImage(e.dataTransfer.files[0]);
            }
        });

        pageFileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                uploadPageImage(this.files[0]);
            }
        });
    }

    function uploadPageImage(file) {
        var formData = new FormData();
        formData.append('file', file);

        fetch('/admin/api/images/upload', {
            method: 'POST',
            body: formData
        })
        .then(function(resp) { return resp.json(); })
        .then(function(data) {
            if (data.success) {
                window.location.reload();
            } else {
                alert('Upload failed: ' + (data.message || 'Unknown error'));
            }
        })
        .catch(function(err) {
            alert('Upload failed: ' + err.message);
        });
    }

    // ===== Delete Image (AJAX) =====
    window.deleteImage = function(id, btn) {
        if (!confirm('Are you sure you want to delete this image?')) return;

        fetch('/admin/api/images/delete/' + id, { method: 'POST' })
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                if (data.success) {
                    var item = btn.closest('.image-grid-item');
                    if (item) item.remove();
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
