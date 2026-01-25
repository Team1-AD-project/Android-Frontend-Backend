import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const advertisementService = {
    async getAdvertisements() {
        const response = await httpService.get(API_CONFIG.ENDPOINTS.ADVERTISEMENTS);
        return response.data || [];
    },

    async getAdvertisementById(id) {
        const response = await httpService.get(`${API_CONFIG.ENDPOINTS.ADVERTISEMENTS}/${id}`);
        return response.data;
    },

    async saveAdvertisement(adData) {
        if (adData.id) {
            const response = await httpService.put(`${API_CONFIG.ENDPOINTS.ADVERTISEMENTS}/${adData.id}`, adData);
            return response.data;
        } else {
            const response = await httpService.post(API_CONFIG.ENDPOINTS.ADVERTISEMENTS, adData);
            return response.data;
        }
    },

    async deleteAdvertisement(id) {
        const response = await httpService.delete(`${API_CONFIG.ENDPOINTS.ADVERTISEMENTS}/${id}`);
        return response;
    }
};

export default advertisementService;
