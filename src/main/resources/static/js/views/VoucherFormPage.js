import { voucherService } from '../services/voucherService.js';

/**
 * 优惠券表单页面（新建/编辑）
 */
export class VoucherFormPage {
    constructor(voucherId = null) {
        this.voucherId = voucherId;
        this.voucher = null;
        this.isEditMode = !!voucherId;
    }

    async init() {
        if (this.isEditMode) {
            await this.loadVoucher();
        }
        this.attachEventListeners();
    }

    async loadVoucher() {
        try {
            this.voucher = await voucherService.getVoucherById(this.voucherId);
        } catch (error) {
            console.error('加载优惠券失败:', error);
            alert('加载优惠券失败');
            window.location.hash = '#/vouchers';
        }
    }

    render() {
        return `
            <div class="voucher-form-page">
                <!-- 页面头部 -->
                <div class="page-header" style="margin-bottom: 24px;">
                    <button 
                        onclick="window.history.back()" 
                        style="background: transparent; border: none; color: #666; cursor: pointer; display: flex; align-items: center; gap: 8px; padding: 8px; margin-bottom: 16px;">
                        <span style="font-size: 18px;">←</span>
                        <span>返回</span>
                    </button>
                    <h1 style="font-size: 24px; font-weight: 600; color: #1a1a1a; margin: 0 0 8px 0;">
                        ${this.isEditMode ? '编辑优惠券' : '新建优惠券'}
                    </h1>
                    <p style="color: #666; margin: 0;">
                        ${this.isEditMode ? '修改优惠券信息' : '创建新的优惠券供用户兑换'}
                    </p>
                </div>

                <!-- 表单 -->
                <div style="background: white; border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb; max-width: 800px;">
                    <form id="voucher-form">
                        <!-- 优惠券名称 -->
                        <div style="margin-bottom: 24px;">
                            <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px;">
                                优惠券名称 <span style="color: #ef4444;">*</span>
                            </label>
                            <input 
                                type="text" 
                                name="name" 
                                id="voucher-name"
                                value="${this.voucher?.name || ''}"
                                placeholder="例如：星巴克 $10 优惠券"
                                required
                                style="width: 100%; padding: 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; box-sizing: border-box;">
                        </div>

                        <!-- 描述 -->
                        <div style="margin-bottom: 24px;">
                            <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px;">
                                描述 <span style="color: #ef4444;">*</span>
                            </label>
                            <textarea 
                                name="description" 
                                id="voucher-description"
                                placeholder="详细描述优惠券的使用范围和条件"
                                required
                                rows="4"
                                style="width: 100%; padding: 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; resize: vertical; box-sizing: border-box;">${this.voucher?.description || ''}</textarea>
                        </div>

                        <!-- 所需积分 -->
                        <div style="margin-bottom: 24px;">
                            <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px;">
                                所需积分 <span style="color: #ef4444;">*</span>
                            </label>
                            <div style="position: relative;">
                                <span style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); font-size: 18px;">⭐</span>
                                <input 
                                    type="number" 
                                    name="cost" 
                                    id="voucher-cost"
                                    value="${this.voucher?.cost || ''}"
                                    placeholder="500"
                                    required
                                    min="1"
                                    style="width: 100%; padding: 12px 12px 12px 40px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; box-sizing: border-box;">
                            </div>
                            <p style="color: #666; font-size: 12px; margin: 8px 0 0 0;">用户需要支付的积分数量</p>
                        </div>

                        <!-- 图标颜色 -->
                        <div style="margin-bottom: 24px;">
                            <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px;">
                                图标颜色（可选）
                            </label>
                            <div style="display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px;">
                                ${this.renderColorOptions()}
                            </div>
                        </div>

                        <!-- 图标 -->
                        <div style="margin-bottom: 24px;">
                            <label style="display: block; font-weight: 600; color: #374151; margin-bottom: 8px;">
                                图标（可选）
                            </label>
                            <div style="display: grid; grid-template-columns: repeat(8, 1fr); gap: 12px;">
                                ${this.renderIconOptions()}
                            </div>
                        </div>

                        <!-- 状态 -->
                        <div style="margin-bottom: 32px;">
                            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input 
                                    type="checkbox" 
                                    name="available" 
                                    id="voucher-available"
                                    ${this.voucher?.available !== false ? 'checked' : ''}
                                    style="width: 18px; height: 18px; cursor: pointer;">
                                <span style="font-weight: 600; color: #374151;">可兑换（启用此优惠券）</span>
                            </label>
                        </div>

                        <!-- 按钮 -->
                        <div style="display: flex; gap: 12px; justify-content: flex-end;">
                            <button 
                                type="button"
                                onclick="window.history.back()"
                                style="padding: 12px 24px; border: 1px solid #d1d5db; background: white; color: #374151; border-radius: 8px; cursor: pointer; font-weight: 500;">
                                取消
                            </button>
                            <button 
                                type="submit"
                                style="padding: 12px 24px; border: none; background: #16a34a; color: white; border-radius: 8px; cursor: pointer; font-weight: 500;">
                                ${this.isEditMode ? '保存更改' : '创建优惠券'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        `;
    }

    renderColorOptions() {
        const colors = [
            { name: 'green', value: '#16a34a' },
            { name: 'starbucks', value: '#00704A' },
            { name: 'subway', value: '#FFC72C' },
            { name: 'canteen', value: '#F97316' },
            { name: 'tea', value: '#DC2626' },
            { name: 'blue', value: '#3b82f6' }
        ];

        return colors.map(color => `
            <div 
                onclick="window.selectColor('${color.value}')"
                class="color-option"
                data-color="${color.value}"
                style="width: 60px; height: 60px; border-radius: 12px; background: ${color.value}; cursor: pointer; border: 3px solid transparent; transition: all 0.2s;"
                onmouseover="this.style.transform='scale(1.1)'"
                onmouseout="this.style.transform='scale(1)'">
            </div>
        `).join('');
    }

    renderIconOptions() {
        const icons = ['☕', '🥪', '🍲', '🧋', '🎁', '🍔', '🍕', '🍜'];
        
        return icons.map(icon => `
            <div 
                onclick="window.selectIcon('${icon}')"
                class="icon-option"
                data-icon="${icon}"
                style="width: 50px; height: 50px; border-radius: 12px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; font-size: 24px; cursor: pointer; border: 3px solid transparent; transition: all 0.2s;"
                onmouseover="this.style.transform='scale(1.1)'"
                onmouseout="this.style.transform='scale(1)'">
                ${icon}
            </div>
        `).join('');
    }

    attachEventListeners() {
        const form = document.getElementById('voucher-form');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.handleSubmit(e);
            });
        }
    }

    async handleSubmit(e) {
        const formData = new FormData(e.target);
        const voucherData = {
            name: formData.get('name'),
            description: formData.get('description'),
            cost: parseInt(formData.get('cost')),
            color: this.selectedColor || this.voucher?.color || '#16a34a',
            icon: this.selectedIcon || this.voucher?.icon || '🎁',
            available: formData.get('available') === 'on'
        };

        try {
            if (this.isEditMode) {
                await voucherService.updateVoucher(this.voucherId, voucherData);
                alert('更新成功');
            } else {
                await voucherService.createVoucher(voucherData);
                alert('创建成功');
            }
            window.location.hash = '#/vouchers';
        } catch (error) {
            console.error('保存失败:', error);
            alert('保存失败: ' + error.message);
        }
    }
}

// 全局处理函数
window.selectColor = (color) => {
    document.querySelectorAll('.color-option').forEach(el => {
        el.style.borderColor = 'transparent';
    });
    const selected = document.querySelector(`[data-color="${color}"]`);
    if (selected) {
        selected.style.borderColor = '#1a1a1a';
        window.voucherFormPageInstance.selectedColor = color;
    }
};

window.selectIcon = (icon) => {
    document.querySelectorAll('.icon-option').forEach(el => {
        el.style.borderColor = 'transparent';
    });
    const selected = document.querySelector(`[data-icon="${icon}"]`);
    if (selected) {
        selected.style.borderColor = '#16a34a';
        window.voucherFormPageInstance.selectedIcon = icon;
    }
};
