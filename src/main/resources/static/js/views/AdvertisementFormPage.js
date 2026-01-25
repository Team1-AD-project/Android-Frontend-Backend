/**
 * Advertisement Form Page
 * Create and edit advertisements
 */
import Layout from '../components/Layout.js';
import advertisementService from '../services/advertisementService.js';

const AdvertisementFormPage = {
    init: async () => {
        const root = document.getElementById('root');

        // Get ID from URL if editing
        const hash = window.location.hash;
        const match = hash.match(/#\/ad\/edit\/(.+)/);
        const id = match ? match[1] : null;

        let ad = { name: '', status: 'Active', startDate: '', endDate: '' };

        if (id) {
            root.innerHTML = Layout.render(`<div class="loading"></div>`, 'ads');
            try {
                ad = await advertisementService.getAdvertisementById(id);
            } catch (e) {
                console.error('Failed to fetch ad:', e);
            }
        }

        AdvertisementFormPage.render(ad, id);
    },

    render: (ad = {}, id = null) => {
        const root = document.getElementById('root');

        // Format dates for input fields
        const formatDate = (dateStr) => {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return date.toISOString().split('T')[0];
        };

        const content = `
            <div class="page-header">
                <div class="page-title">
                    <h1>${id ? 'Edit Advertisement' : 'New Advertisement'}</h1>
                    <p>${id ? 'Update advertisement details' : 'Create a new promotional campaign'}</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <form id="ad-form" style="max-width: 500px;">
                        <div class="form-group">
                            <label class="form-label">Ad Name</label>
                            <input type="text" class="form-input" name="name" value="${ad.name || ''}" placeholder="Enter advertisement name" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Status</label>
                            <select class="form-select" name="status">
                                <option value="Active" ${ad.status === 'Active' ? 'selected' : ''}>Active</option>
                                <option value="Inactive" ${ad.status === 'Inactive' ? 'selected' : ''}>Inactive</option>
                                <option value="Paused" ${ad.status === 'Paused' ? 'selected' : ''}>Paused</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Start Date</label>
                            <input type="date" class="form-input" name="startDate" value="${formatDate(ad.startDate)}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">End Date</label>
                            <input type="date" class="form-input" name="endDate" value="${formatDate(ad.endDate)}">
                        </div>
                        <div style="display: flex; gap: 12px; margin-top: 32px;">
                            <button type="submit" class="btn btn-primary">Save Advertisement</button>
                            <a href="#/ads" class="btn btn-ghost">Cancel</a>
                        </div>
                    </form>
                </div>
            </div>
        `;

        root.innerHTML = Layout.render(content, 'ads');

        // Form submission handler
        document.getElementById('ad-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(e.target);
            const adData = {
                id: id || undefined,
                name: formData.get('name'),
                status: formData.get('status'),
                startDate: formData.get('startDate'),
                endDate: formData.get('endDate')
            };

            try {
                await advertisementService.saveAdvertisement(adData);
                alert('Saved successfully!');
                window.location.hash = '#/ads';
            } catch (error) {
                alert('Failed to save: ' + error.message);
            }
        });
    }
};

export default AdvertisementFormPage;
