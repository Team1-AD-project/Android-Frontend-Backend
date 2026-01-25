/**
 * Advertisement List Page
 * Displays and manages advertisements
 */
import Layout from '../components/Layout.js';
import advertisementService from '../services/advertisementService.js';

const AdvertisementListPage = {
    init: async () => {
        const root = document.getElementById('root');
        root.innerHTML = Layout.render(`<div class="loading"></div>`, 'ads');

        try {
            const ads = await advertisementService.getAdvertisements();
            AdvertisementListPage.render(ads);
        } catch (error) {
            console.error('Failed to load advertisements:', error);
            root.innerHTML = Layout.render(`
                <div class="page-header">
                    <div class="page-title">
                        <h1>Advertisements</h1>
                        <p>Manage your promotional campaigns</p>
                    </div>
                </div>
                <div class="card">
                    <div class="card-body">
                        <div class="empty-state">
                            <h3>Failed to Load Data</h3>
                            <p>Please make sure the backend server is running.</p>
                            <button class="btn btn-primary" onclick="location.reload()">Retry</button>
                        </div>
                    </div>
                </div>
            `, 'ads');
        }
    },

    render: (ads = []) => {
        const root = document.getElementById('root');

        const content = `
            <div class="page-header">
                <div class="page-title">
                    <h1>Advertisements</h1>
                    <p>Manage your promotional campaigns</p>
                </div>
                <a href="#/ad/new" class="btn btn-primary">+ New Advertisement</a>
            </div>

            <div class="card">
                <div class="card-body" style="padding: 0;">
                    <table>
                        <thead>
                            <tr>
                                <th>Ad Name</th>
                                <th>Status</th>
                                <th>Start Date</th>
                                <th>End Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${ads.map(ad => `
                                <tr>
                                    <td>${ad.name || 'Untitled'}</td>
                                    <td><span class="status-tag ${(ad.status || '').toLowerCase()}">${ad.status || 'N/A'}</span></td>
                                    <td>${ad.startDate ? new Date(ad.startDate).toLocaleDateString() : '-'}</td>
                                    <td>${ad.endDate ? new Date(ad.endDate).toLocaleDateString() : '-'}</td>
                                    <td>
                                        <div class="action-buttons">
                                            <a href="#/ad/edit/${ad.id}" class="btn btn-sm btn-ghost">Edit</a>
                                            <button class="btn btn-sm btn-danger" onclick="window.deleteAd('${ad.id}')">Delete</button>
                                        </div>
                                    </td>
                                </tr>
                            `).join('')}
                            ${ads.length === 0 ? '<tr><td colspan="5" style="text-align: center; padding: 40px; color: var(--text-muted);">No advertisements yet</td></tr>' : ''}
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        root.innerHTML = Layout.render(content, 'ads');
    }
};

// Global delete function
window.deleteAd = async (id) => {
    if (confirm('Are you sure you want to delete this advertisement?')) {
        try {
            await advertisementService.deleteAdvertisement(id);
            alert('Deleted successfully!');
            AdvertisementListPage.init();
        } catch (error) {
            alert('Failed to delete: ' + error.message);
        }
    }
};

export default AdvertisementListPage;
