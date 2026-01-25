/**
 * 活动管理页面
 */
import Layout from '../components/Layout.js';
import activityService from '../services/activityService.js';

const ActivityListPage = {
    render: async () => {
        const root = document.getElementById('root');
        root.innerHTML = Layout.render(`<div class="loading"></div>`, 'activities');

        try {
            const activities = await activityService.getActivities();

            const content = `
                <div class="page-header">
                    <h1>活动管理</h1>
                    <button class="btn btn-primary" onclick="ActivityListPage.showAddForm()">+ 新增活动</button>
                </div>

                <div class="card">
                    <div class="card-body">
                        <table>
                            <thead>
                                <tr>
                                    <th>活动名称</th>
                                    <th>类型</th>
                                    <th>状态</th>
                                    <th>奖励积分</th>
                                    <th>参与人数</th>
                                    <th>开始时间</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${activities.map(activity => `
                                    <tr>
                                        <td>${activity.title}</td>
                                        <td>${activity.type === 'ONLINE' ? '线上' : '线下'}</td>
                                        <td><span class="status-tag ${activity.status?.toLowerCase()}">${ActivityListPage.getStatusText(activity.status)}</span></td>
                                        <td>${activity.rewardCredits || 0}</td>
                                        <td>${activity.currentParticipants || 0} / ${activity.maxParticipants || '∞'}</td>
                                        <td>${activity.startTime ? new Date(activity.startTime).toLocaleString() : '-'}</td>
                                        <td>
                                            <div class="action-buttons">
                                                ${activity.status === 'DRAFT' ? `<button class="btn btn-sm btn-primary" onclick="ActivityListPage.publishActivity('${activity.id}')">发布</button>` : ''}
                                                <button class="btn btn-sm btn-default" onclick="ActivityListPage.editActivity('${activity.id}')">编辑</button>
                                                <button class="btn btn-sm btn-danger" onclick="ActivityListPage.deleteActivity('${activity.id}')">删除</button>
                                            </div>
                                        </td>
                                    </tr>
                                `).join('')}
                                ${activities.length === 0 ? '<tr><td colspan="7" class="empty-state">暂无活动数据</td></tr>' : ''}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            root.innerHTML = Layout.render(content, 'activities');
        } catch (error) {
            console.error('加载活动失败:', error);
            root.innerHTML = Layout.render(`
                <div class="empty-state">
                    <div class="icon">❌</div>
                    <p>加载活动数据失败</p>
                    <button class="btn btn-primary" onclick="location.reload()">重试</button>
                </div>
            `, 'activities');
        }
    },

    getStatusText: (status) => {
        const statusMap = {
            'DRAFT': '草稿',
            'PUBLISHED': '已发布',
            'ONGOING': '进行中',
            'ENDED': '已结束'
        };
        return statusMap[status] || status;
    },

    publishActivity: async (id) => {
        if (confirm('确定要发布这个活动吗？')) {
            try {
                await activityService.publishActivity(id);
                alert('发布成功');
                ActivityListPage.render();
            } catch (error) {
                alert('发布失败: ' + error.message);
            }
        }
    },

    deleteActivity: async (id) => {
        if (confirm('确定要删除这个活动吗？')) {
            try {
                await activityService.deleteActivity(id);
                alert('删除成功');
                ActivityListPage.render();
            } catch (error) {
                alert('删除失败: ' + error.message);
            }
        }
    },

    editActivity: (id) => {
        alert('编辑活动功能开发中...');
    },

    showAddForm: () => {
        alert('新增活动功能开发中...');
    }
};

window.ActivityListPage = ActivityListPage;

export default ActivityListPage;
