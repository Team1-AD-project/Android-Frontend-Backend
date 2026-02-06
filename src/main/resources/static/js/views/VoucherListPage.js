import { voucherService } from '../services/voucherService.js';

/**
 * 优惠券列表页面
 */
export class VoucherListPage {
    constructor() {
        this.vouchers = [];
        this.filteredVouchers = [];
        this.currentFilter = 'all';
    }

    async init() {
        await this.loadVouchers();
    }

    async loadVouchers() {
        try {
            this.vouchers = await voucherService.getVouchers();
            this.applyFilter();
            this.render();
        } catch (error) {
            console.error('加载优惠券失败:', error);
            this.renderError();
        }
    }

    applyFilter() {
        switch (this.currentFilter) {
            case 'available':
                this.filteredVouchers = this.vouchers.filter(v => v.available !== false);
                break;
            case 'unavailable':
                this.filteredVouchers = this.vouchers.filter(v => v.available === false);
                break;
            default:
                this.filteredVouchers = [...this.vouchers];
        }
    }

    render() {
        return `
            <div class="voucher-list-page">
                <!-- 页面头部 -->
                <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
                    <div>
                        <h1 style="font-size: 24px; font-weight: 600; color: #1a1a1a; margin: 0 0 8px 0;">优惠券管理</h1>
                        <p style="color: #666; margin: 0;">管理所有优惠券和兑换商品</p>
                    </div>
                    <button 
                        onclick="window.handleNewVoucher()" 
                        style="background: #16a34a; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 500; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 18px;">+</span>
                        <span>新建优惠券</span>
                    </button>
                </div>

                <!-- 统计卡片 -->
                ${this.renderStats()}

                <!-- 筛选标签 -->
                <div style="margin-bottom: 24px; display: flex; gap: 12px; border-bottom: 1px solid #e5e7eb; padding-bottom: 16px;">
                    ${this.renderFilterTabs()}
                </div>

                <!-- 优惠券列表 -->
                ${this.renderVoucherList()}
            </div>
        `;
    }

    renderStats() {
        const stats = {
            total: this.vouchers.length,
            available: this.vouchers.filter(v => v.available !== false).length,
            unavailable: this.vouchers.filter(v => v.available === false).length,
            totalValue: this.vouchers.reduce((sum, v) => sum + (v.cost || 0), 0)
        };

        return `
            <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px;">
                ${this.renderStatCard('总优惠券数', stats.total, '🎫', '#3b82f6')}
                ${this.renderStatCard('可兑换', stats.available, '✅', '#16a34a')}
                ${this.renderStatCard('已下架', stats.unavailable, '❌', '#ef4444')}
                ${this.renderStatCard('总积分价值', stats.totalValue, '⭐', '#f59e0b')}
            </div>
        `;
    }

    renderStatCard(label, value, icon, color) {
        return `
            <div style="background: white; border-radius: 12px; padding: 20px; border: 1px solid #e5e7eb;">
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                    <div style="font-size: 24px;">${icon}</div>
                    <div style="flex: 1;">
                        <div style="color: #666; font-size: 14px; margin-bottom: 4px;">${label}</div>
                        <div style="font-size: 24px; font-weight: 700; color: ${color};">${value.toLocaleString()}</div>
                    </div>
                </div>
            </div>
        `;
    }

    renderFilterTabs() {
        const tabs = [
            { id: 'all', label: '全部优惠券', count: this.vouchers.length },
            { id: 'available', label: '可兑换', count: this.vouchers.filter(v => v.available !== false).length },
            { id: 'unavailable', label: '已下架', count: this.vouchers.filter(v => v.available === false).length }
        ];

        return tabs.map(tab => `
            <button 
                onclick="window.handleFilterChange('${tab.id}')"
                style="padding: 8px 16px; border: none; background: ${this.currentFilter === tab.id ? '#16a34a' : 'transparent'}; 
                       color: ${this.currentFilter === tab.id ? 'white' : '#666'}; border-radius: 6px; cursor: pointer; font-weight: 500;">
                ${tab.label} (${tab.count})
            </button>
        `).join('');
    }

    renderVoucherList() {
        if (this.filteredVouchers.length === 0) {
            return `
                <div style="text-align: center; padding: 60px 20px; background: white; border-radius: 12px; border: 1px solid #e5e7eb;">
                    <div style="font-size: 64px; margin-bottom: 16px;">🎁</div>
                    <h3 style="color: #666; margin: 0 0 8px 0;">暂无优惠券</h3>
                    <p style="color: #999; margin: 0;">点击"新建优惠券"按钮创建第一个优惠券</p>
                </div>
            `;
        }

        return `
            <div style="background: white; border-radius: 12px; overflow: hidden; border: 1px solid #e5e7eb;">
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f9fafb; border-bottom: 1px solid #e5e7eb;">
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">图标</th>
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">优惠券名称</th>
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">描述</th>
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">所需积分</th>
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">状态</th>
                            <th style="padding: 16px; text-align: left; font-weight: 600; color: #374151;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${this.filteredVouchers.map(voucher => this.renderVoucherRow(voucher)).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }

    renderVoucherRow(voucher) {
        const icon = this.getVoucherIcon(voucher.name);
        const iconColor = this.getIconColor(voucher.name);
        const available = voucher.available !== false;

        return `
            <tr style="border-bottom: 1px solid #f3f4f6;">
                <td style="padding: 16px;">
                    <div style="width: 48px; height: 48px; border-radius: 12px; background: ${iconColor}; display: flex; align-items: center; justify-content: center; font-size: 24px;">
                        ${icon}
                    </div>
                </td>
                <td style="padding: 16px;">
                    <div style="font-weight: 600; color: #1a1a1a; margin-bottom: 4px;">${voucher.name || '未命名'}</div>
                </td>
                <td style="padding: 16px;">
                    <div style="color: #666; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                        ${voucher.description || '-'}
                    </div>
                </td>
                <td style="padding: 16px;">
                    <div style="display: flex; align-items: center; gap: 4px;">
                        <span style="font-size: 16px;">⭐</span>
                        <span style="font-weight: 600; color: #16a34a;">${voucher.cost || 0}</span>
                    </div>
                </td>
                <td style="padding: 16px;">
                    <span style="padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; 
                                 background: ${available ? '#d1fae5' : '#fee2e2'}; 
                                 color: ${available ? '#065f46' : '#991b1b'};">
                        ${available ? '可兑换' : '已下架'}
                    </span>
                </td>
                <td style="padding: 16px;">
                    <div style="display: flex; gap: 8px;">
                        <button 
                            onclick="window.handleEditVoucher('${voucher.id}')"
                            style="padding: 6px 12px; border: 1px solid #d1d5db; background: white; color: #374151; border-radius: 6px; cursor: pointer; font-size: 14px;">
                            编辑
                        </button>
                        <button 
                            onclick="window.handleDeleteVoucher('${voucher.id}')"
                            style="padding: 6px 12px; border: 1px solid #fca5a5; background: white; color: #dc2626; border-radius: 6px; cursor: pointer; font-size: 14px;">
                            删除
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    getVoucherIcon(name) {
        if (!name) return '🎁';
        const lowerName = name.toLowerCase();
        if (lowerName.includes('starbucks') || lowerName.includes('咖啡')) return '☕';
        if (lowerName.includes('subway') || lowerName.includes('subway')) return '🥪';
        if (lowerName.includes('canteen') || lowerName.includes('食堂')) return '🍲';
        if (lowerName.includes('tea') || lowerName.includes('茶')) return '🧋';
        return '🎁';
    }

    getIconColor(name) {
        if (!name) return '#16a34a';
        const lowerName = name.toLowerCase();
        if (lowerName.includes('starbucks')) return '#00704A';
        if (lowerName.includes('subway')) return '#FFC72C';
        if (lowerName.includes('canteen') || lowerName.includes('食堂')) return '#F97316';
        if (lowerName.includes('tea') || lowerName.includes('茶')) return '#DC2626';
        return '#16a34a';
    }

    renderError() {
        return `
            <div style="text-align: center; padding: 60px 20px;">
                <div style="font-size: 64px; margin-bottom: 16px;">😕</div>
                <h3 style="color: #666; margin: 0 0 8px 0;">加载失败</h3>
                <p style="color: #999; margin: 0 0 20px 0;">无法加载优惠券数据，请稍后重试</p>
                <button 
                    onclick="location.reload()"
                    style="padding: 10px 20px; background: #16a34a; color: white; border: none; border-radius: 6px; cursor: pointer;">
                    重新加载
                </button>
            </div>
        `;
    }
}

// 全局处理函数
window.handleNewVoucher = () => {
    window.location.hash = '#/voucher/new';
};

window.handleEditVoucher = (id) => {
    window.location.hash = `#/voucher/edit/${id}`;
};

window.handleDeleteVoucher = async (id) => {
    if (confirm('确定要删除这个优惠券吗？此操作无法撤销。')) {
        try {
            await voucherService.deleteVoucher(id);
            alert('删除成功');
            location.reload();
        } catch (error) {
            alert('删除失败: ' + error.message);
        }
    }
};

window.handleFilterChange = (filter) => {
    const page = new VoucherListPage();
    page.vouchers = window.voucherListPageInstance?.vouchers || [];
    page.currentFilter = filter;
    page.applyFilter();
    document.getElementById('app').innerHTML = page.render();
    window.voucherListPageInstance = page;
};
